package online.remind.remind.handler;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import online.kingdomkeys.kingdomkeys.data.PlayerData;
import online.remind.remind.KingdomKeysReMind;
import online.remind.remind.integration.ArsArcanumAnimationBridge;
import online.remind.remind.lib.StringsRM;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = KingdomKeysReMind.MODID)
public class ArsArcanumSequenceHandler {

    private static final ThreadLocal<Boolean> APPLYING_SCRIPTED_DAMAGE = ThreadLocal.withInitial(() -> false);

    public static boolean isApplyingScriptedDamage() {
        return APPLYING_SCRIPTED_DAMAGE.get();
    }


    private enum Phase {
        ARCANUM,
        WAITING_FOR_BASH,
        WAITING_FOR_FINISH
    }

    private static void showBashRC(ServerPlayer player) {
        PlayerData data = PlayerData.get(player);
        data.addReactionCommand(ResourceLocation.parse(StringsRM.ArsBashRC), player, 20);
    }

    private static void showFinishRC(ServerPlayer player) {
        PlayerData data = PlayerData.get(player);
        data.addReactionCommand(ResourceLocation.parse(StringsRM.ArsFinishRC), player, 20);
    }

    private static class ArsArcanumState {

        private final UUID targetUUID;
        private final ResourceKey<Level> dimension;
        private final float baseDamage;

        private int tick;
        private int hitIndex;
        private int bashCount;
        private int waitTicks;

        private Phase phase;

        private ArsArcanumState(
                UUID targetUUID,
                ResourceKey<Level> dimension,
                float baseDamage
        ) {
            this.targetUUID = targetUUID;
            this.dimension = dimension;
            this.baseDamage = baseDamage;

            this.tick = 0;
            this.hitIndex = 0;
            this.bashCount = 0;
            this.waitTicks = 0;

            this.phase = Phase.ARCANUM;
        }
    }

    private static final int[] ARCANUM_HIT_TICKS = {1, 6, 11, 16, 21, 26, 31}; // Only the initial 7 automatic hits

    private static final double MAX_TARGET_DISTANCE = 12.0D;

    private static final double DESIRED_DISTANCE = 2.25D;

    private static final double PULL_SPEED = 0.30D;

    private static final Map<UUID, ArsArcanumState> ACTIVE = new HashMap<>();


    /**
     * Starts Ars Arcanum.
     *
     * @param player     Player performing Ars Arcanum
     * @param target     Target being attacked
     * @param baseDamage Base damage before individual hit multipliers
     */
    public static void start(
            ServerPlayer player,
            LivingEntity target,
            float baseDamage
    ) {
        if (player == null || target == null) {
            return;
        }

        if (!player.isAlive() || !target.isAlive()) {
            return;
        }

        ACTIVE.put(
                player.getUUID(),
                new ArsArcanumState(
                        target.getUUID(),
                        target.level().dimension(),
                        baseDamage
                )
        );
    }


    public static boolean isActive(ServerPlayer player) {
        return ACTIVE.containsKey(player.getUUID());
    }


    public static void cancel(ServerPlayer player) {
        ACTIVE.remove(player.getUUID());
    }


    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (ACTIVE.isEmpty()) return;

        MinecraftServer server = event.getServer();
        Iterator<Map.Entry<UUID, ArsArcanumState>> iterator = ACTIVE.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<UUID, ArsArcanumState> entry = iterator.next();
            ServerPlayer player = server.getPlayerList().getPlayer(entry.getKey());
            ArsArcanumState state = entry.getValue();

            if (player == null || !player.isAlive()) {
                iterator.remove();
                continue;
            }

            LivingEntity target = getTarget(player, state);

            if (target == null) {
                iterator.remove();
                continue;
            }

            faceTarget(player, target);

            if (state.phase == Phase.ARCANUM) {
                state.tick++;
                pullTowardTarget(player, target);

                if (state.hitIndex < ARCANUM_HIT_TICKS.length) {
                    int requiredTick = ARCANUM_HIT_TICKS[state.hitIndex];

                    if (state.tick >= requiredTick) {
                        int hitNumber = state.hitIndex + 1;
                        performHit(player, target, state.baseDamage, hitNumber);
                        state.hitIndex++;
                    }
                }

                if (state.hitIndex >= ARCANUM_HIT_TICKS.length) {
                    state.phase = Phase.WAITING_FOR_BASH; // Pause the automatic sequence here
                    state.waitTicks = 0;
                    showBashRC(player); // Hand control over to the Bash RC
                }

                continue;
            }

            if (state.phase == Phase.WAITING_FOR_BASH) {
                state.waitTicks++; // Nothing attacks automatically while waiting for Bash

                if (state.waitTicks > 40) iterator.remove(); // RC expired without being used
                continue;
            }

            if (state.phase == Phase.WAITING_FOR_FINISH) {
                state.waitTicks++; // Nothing attacks automatically while waiting for Finish

                if (state.waitTicks > 40) iterator.remove(); // RC expired without being used
            }
        }
    }


    private static void performHit(
            ServerPlayer player,
            LivingEntity target,
            float baseDamage,
            int hitNumber
    ) {


        // This is where EFM magic would happen
        ArsArcanumAnimationBridge.play(
                player,
                hitNumber
        );


        // Damage

        float multiplier =
                getDamageMultiplier(hitNumber);

        float damage =
                baseDamage * multiplier;

        // Allow Multi-hits to work
        target.invulnerableTime = 0;

        ScriptedAttackDamageHandler.hurt(() ->
                target.hurt(player.damageSources().playerAttack(player), damage)
        );

        target.invulnerableTime = 0;


        /*
         * ------------------------------------------------
         * FINISH
         * ------------------------------------------------
         */

        if (hitNumber == 13) {
            applyFinisherKnockback(
                    player,
                    target
            );
        }


        /*
         * VFX will go here:
         *
         * ArsArcanumVFX.spawnHit(
         *     player,
         *     target,
         *     hitNumber
         * );
         *
         *
         * Sound can go here too:
         *
         * ArsArcanumSounds.playHit(
         *     player,
         *     hitNumber
         * );
         */
    }


    private static float getDamageMultiplier(int hitNumber) {


        if (hitNumber == 13) {
            return 1F;
        }


        if (hitNumber >= 8) {
            return 0.5F;
        }


        return 0.25F;
    }


    private static void faceTarget(
            ServerPlayer player,
            LivingEntity target
    ) {
        Vec3 direction =
                target.position()
                        .subtract(player.position());

        double horizontalDistance =
                Math.sqrt(
                        direction.x * direction.x
                                + direction.z * direction.z
                );

        float yaw =
                (float) (
                        Math.atan2(
                                direction.z,
                                direction.x
                        )
                                * (180.0D / Math.PI)
                ) - 90.0F;

        float pitch =
                (float) (
                        -Math.atan2(
                                direction.y,
                                horizontalDistance
                        )
                                * (180.0D / Math.PI)
                );

        player.setYRot(yaw);
        player.setYHeadRot(yaw);
        player.setXRot(pitch);
    }


    private static void pullTowardTarget(
            ServerPlayer player,
            LivingEntity target
    ) {

        double distance =
                player.distanceTo(target);


        if (distance <= DESIRED_DISTANCE) {
            return;
        }

        Vec3 difference =
                target.position()
                        .subtract(player.position());

        Vec3 horizontal =
                new Vec3(
                        difference.x,
                        0.0D,
                        difference.z
                );

        if (horizontal.lengthSqr() < 0.0001D) {
            return;
        }

        Vec3 direction =
                horizontal.normalize();

        Vec3 currentMotion =
                player.getDeltaMovement();

        player.setDeltaMovement(
                direction.x * PULL_SPEED,
                currentMotion.y,
                direction.z * PULL_SPEED
        );

        player.hurtMarked = true;
    }


    private static void applyFinisherKnockback(
            ServerPlayer player,
            LivingEntity target
    ) {

        Vec3 difference =
                target.position()
                        .subtract(player.position());

        Vec3 horizontal =
                new Vec3(
                        difference.x,
                        0.0D,
                        difference.z
                );

        if (horizontal.lengthSqr() < 0.0001D) {
            return;
        }

        Vec3 direction =
                horizontal.normalize();

        Vec3 motion =
                target.getDeltaMovement();

        target.setDeltaMovement(
                motion.x + direction.x * 0.55D,
                Math.max(motion.y, 0.25D),
                motion.z + direction.z * 0.55D
        );

        target.hurtMarked = true;
    }


    private static LivingEntity getTarget(
            ServerPlayer player,
            ArsArcanumState state
    ) {
        MinecraftServer server = player.getServer();

        if (server == null) {
            return null;
        }

        ServerLevel level = server.getLevel(state.dimension);

        if (level == null) {
            return null;
        }

        /*
         * Player changed dimensions.
         */
        if (player.level() != level) {
            return null;
        }

        Entity entity = level.getEntity(state.targetUUID);

        if (!(entity instanceof LivingEntity target)) {
            return null;
        }

        if (!target.isAlive()) {
            return null;
        }

        /*
         * Don't let an RC suddenly chase something
         * absurdly far away.
         */
        if (player.distanceTo(target) > MAX_TARGET_DISTANCE) {
            return null;
        }

        return target;
    }

    public static void useBash(ServerPlayer player) {
        ArsArcanumState state = ACTIVE.get(player.getUUID());

        if (state == null || state.phase != Phase.WAITING_FOR_BASH || state.bashCount >= 5) return;

        LivingEntity target = getTarget(player, state);

        if (target == null) {
            cancel(player);
            return;
        }

        state.bashCount++;
        state.waitTicks = 0;

        int hitNumber = 7 + state.bashCount; // Bash attacks are hits 8-12

        faceTarget(player, target);
        pullTowardTarget(player, target);
        performHit(player, target, state.baseDamage, hitNumber);

        if (state.bashCount < 5) {
            showBashRC(player); // Bash 1-4 reopen the Bash RC
            return;
        }

        state.phase = Phase.WAITING_FOR_FINISH; // Bash 5 ends the Bash portion
        state.waitTicks = 0;
        showFinishRC(player); // Replace Bash with Finish
    }

    public static void useFinish(ServerPlayer player) {
        ArsArcanumState state = ACTIVE.get(player.getUUID());

        if (state == null || state.phase != Phase.WAITING_FOR_FINISH) return;

        LivingEntity target = getTarget(player, state);

        if (target == null) {
            cancel(player);
            return;
        }

        faceTarget(player, target);
        pullTowardTarget(player, target);
        performHit(player, target, state.baseDamage, 13); // Finish is the 13th and final hit

        ACTIVE.remove(player.getUUID()); // Ars Arcanum is finished
    }

    public static boolean canUseBash(Player player) {
        ArsArcanumState state = ACTIVE.get(player.getUUID());
        return state != null && state.phase == Phase.WAITING_FOR_BASH && state.bashCount < 5;
    }

    public static boolean canUseFinish(Player player) {
        ArsArcanumState state = ACTIVE.get(player.getUUID());
        return state != null && state.phase == Phase.WAITING_FOR_FINISH;
    }
}