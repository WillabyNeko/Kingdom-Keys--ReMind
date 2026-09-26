package online.remind.remind.handler;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import online.kingdomkeys.kingdomkeys.data.PlayerData;
import online.kingdomkeys.kingdomkeys.data.WorldData;
import online.kingdomkeys.kingdomkeys.lib.Party;
import online.remind.remind.KingdomKeysReMind;
import online.remind.remind.lib.StringsRM;
import online.remind.remind.integration.AerialSlamAnimationBridge;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = KingdomKeysReMind.MODID)
public class AerialSlamSequenceHandler {

    private static final double MAX_TARGET_DISTANCE = 12.0D;
    private static final double APPROACH_SPEED = 0.90D;
    private static final double ATTACK_DISTANCE = 2.75D;
    private static final int APPROACH_TIMEOUT = 14;
    private static final int SLAM_RC_DURATION = 40;

    private static final float LAUNCH_DAMAGE_MULTIPLIER = 0.90F;
    private static final float SLAM_DAMAGE_MULTIPLIER = 1.60F;
    private static final float SHOCKWAVE_DAMAGE_MULTIPLIER = 0.75F;

    private static final Map<UUID, AerialSlamState> ACTIVE = new HashMap<>();
    private static final ThreadLocal<Boolean> APPLYING_SCRIPTED_DAMAGE = ThreadLocal.withInitial(() -> false);

    private enum Phase {
        SLASH_APPROACH,
        LAUNCHED,
        WAITING_FOR_SLAM,
        SLAM,
        IMPACT_RECOVERY
    }

    private static final int PLAYER_HANG_TICKS = 5; // Half-second aerial pause after impact

    private static class AerialSlamState {
        private final UUID targetUUID;
        private final ResourceKey<Level> dimension;
        private final float baseDamage;
        private int tick;
        private int waitTicks;
        private Phase phase;

        private AerialSlamState(UUID targetUUID, ResourceKey<Level> dimension, float baseDamage) {
            this.targetUUID = targetUUID;
            this.dimension = dimension;
            this.baseDamage = baseDamage;
            this.tick = 0;
            this.waitTicks = 0;
            this.phase = Phase.SLASH_APPROACH;
        }
    }

    public static void start(ServerPlayer player, LivingEntity target, float baseDamage) {
        if (player == null || target == null || !player.isAlive() || !target.isAlive()) return;
        if (ACTIVE.containsKey(player.getUUID())) return;

        ACTIVE.put(player.getUUID(), new AerialSlamState(target.getUUID(), target.level().dimension(), baseDamage));
    }

    public static boolean isActive(ServerPlayer player) {
        return ACTIVE.containsKey(player.getUUID());
    }

    public static boolean canUseSlam(Player player) {
        AerialSlamState state = ACTIVE.get(player.getUUID());
        return state != null && state.phase == Phase.WAITING_FOR_SLAM;
    }

    public static boolean isApplyingScriptedDamage() {
        return APPLYING_SCRIPTED_DAMAGE.get();
    }

    public static void cancel(ServerPlayer player) {
        ACTIVE.remove(player.getUUID());
        PlayerData.get(player).removeReactionCommand(ResourceLocation.parse(StringsRM.AerialSlamRC));
    }

    private static void showSlamRC(ServerPlayer player) {
        PlayerData.get(player).addReactionCommand(ResourceLocation.parse(StringsRM.AerialSlamRC), player, SLAM_RC_DURATION);
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (ACTIVE.isEmpty()) return;

        MinecraftServer server = event.getServer();
        Iterator<Map.Entry<UUID, AerialSlamState>> iterator = ACTIVE.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<UUID, AerialSlamState> entry = iterator.next();
            ServerPlayer player = server.getPlayerList().getPlayer(entry.getKey());
            AerialSlamState state = entry.getValue();

            if (player == null || !player.isAlive()) {
                iterator.remove();
                continue;
            }

            LivingEntity target = getTarget(player, state);

            if (target == null) {
                PlayerData.get(player).removeReactionCommand(ResourceLocation.parse(StringsRM.AerialSlamRC));
                iterator.remove();
                continue;
            }

            if (state.phase == Phase.SLASH_APPROACH) {
                state.tick++;
                faceTarget(player, target);

                if (state.tick == 1) AerialSlamAnimationBridge.playLaunch(player); // Begin the rushing slash animation

                double distance = player.distanceTo(target);

                if (distance > ATTACK_DISTANCE) dashTowardTarget(player, target); // Move forward while the slash plays

                if (distance <= ATTACK_DISTANCE) {
                    if (performLaunchSlash(player, target, state.baseDamage)) {
                        state.phase = Phase.LAUNCHED; // Only launch/continue if the attack actually connected
                        state.tick = 0;
                    } else {
                        iterator.remove();
                    }

                    continue;
                }

                if (state.tick >= APPROACH_TIMEOUT) {
                    iterator.remove();
                    continue;
                }

                continue;
            }

            if (state.phase == Phase.LAUNCHED) {
                state.tick++;
                faceTarget(player, target);

                if (state.tick >= 8) {
                    state.phase = Phase.WAITING_FOR_SLAM;
                    state.waitTicks = 0;
                    showSlamRC(player); // Target is airborne before Slam becomes available
                }

                continue;
            }

            if (state.phase == Phase.WAITING_FOR_SLAM) {
                state.waitTicks++;
                faceTarget(player, target);

                Vec3 motion = target.getDeltaMovement();

                if (state.waitTicks <= 28 && motion.y < 0.05D) {
                    target.setDeltaMovement(motion.x * 0.85D, 0.02D, motion.z * 0.85D); // Hold the target around the apex for good air time
                    target.hurtMarked = true;
                    target.resetFallDistance();
                }

                if (state.waitTicks > SLAM_RC_DURATION) {
                    PlayerData.get(player).removeReactionCommand(ResourceLocation.parse(StringsRM.AerialSlamRC));
                    iterator.remove();
                }

                continue;
            }

            if (state.phase == Phase.SLAM) {
                state.tick++;

                player.setDeltaMovement(0.0D, 0.0D, 0.0D); // Hold the player in the air during the slam
                player.hurtMarked = true;
                player.resetFallDistance();

                if ((state.tick > 2 && target.onGround()) || state.tick >= 30) {
                    createShockwave(player, target, state.baseDamage); // Target hits the ground
                    state.phase = Phase.IMPACT_RECOVERY;
                    state.tick = 0;
                }

                continue;
            }

            if (state.phase == Phase.IMPACT_RECOVERY) {
                state.tick++;

                if (state.tick <= PLAYER_HANG_TICKS) {
                    player.setDeltaMovement(0.0D, 0.0D, 0.0D); // Brief KH-style pause after the impact
                    player.hurtMarked = true;
                    player.resetFallDistance();
                    continue;
                }

                iterator.remove(); // Stop holding the player and let gravity take over normally
            }
        }
    }

    private static void dashTowardTarget(ServerPlayer player, LivingEntity target) {
        Vec3 difference = target.position().subtract(player.position());
        Vec3 horizontal = new Vec3(difference.x, 0.0D, difference.z);

        if (horizontal.lengthSqr() < 0.0001D) return;

        Vec3 direction = horizontal.normalize();

        player.setDeltaMovement(direction.x * APPROACH_SPEED, 0.08D, direction.z * APPROACH_SPEED);
        player.hurtMarked = true;
    }

    private static boolean performLaunchSlash(ServerPlayer player, LivingEntity target, float baseDamage) {
        boolean hit = applyScriptedDamage(player, target, baseDamage * LAUNCH_DAMAGE_MULTIPLIER);

        if (!hit) return false; // No successful hit means no knock-up

        target.setDeltaMovement(0.0D, 1.15D, 0.0D); // Launch only after damage connects
        target.hurtMarked = true;
        target.resetFallDistance();

        player.setDeltaMovement(0.0D, 0.30D, 0.0D); // Carry the player slightly upward with the slash
        player.hurtMarked = true;
        player.resetFallDistance();

        return true;
    }

    public static void useSlam(ServerPlayer player) {
        AerialSlamState state = ACTIVE.get(player.getUUID());
        if (state == null || state.phase != Phase.WAITING_FOR_SLAM) return;

        LivingEntity target = getTarget(player, state);

        if (target == null) {
            cancel(player);
            return;
        }

        PlayerData.get(player).removeReactionCommand(ResourceLocation.parse(StringsRM.AerialSlamRC));

        state.phase = Phase.SLAM;
        state.tick = 0;
        state.waitTicks = 0;

        player.teleportTo(target.getX(), target.getY() + target.getBbHeight() + 1.75D, target.getZ()); // Position above the airborne target
        player.resetFallDistance();
        faceTarget(player, target);

        AerialSlamAnimationBridge.playSlam(player); // Play the downward EFM slash

        applyScriptedDamage(player, target, state.baseDamage * SLAM_DAMAGE_MULTIPLIER); // Slam damage is still controlled by Re:Mind

        target.setDeltaMovement(0.0D, -1.0D, 0.0D); // Drive target into the ground
        target.hurtMarked = true;
        target.resetFallDistance();

        player.setDeltaMovement(0.0D, 0.0D, 0.0D); // Player hangs above the target
        player.hurtMarked = true;
        player.resetFallDistance();
    }

    private static void createShockwave(ServerPlayer player, LivingEntity target, float baseDamage) {
        if (!(target.level() instanceof ServerLevel level)) return;

        double x = target.getX();
        double y = target.getY() + 0.15D;
        double z = target.getZ();

        level.sendParticles(ParticleTypes.EXPLOSION, x, y + 0.5D, z, 4, 0.45D, 0.25D, 0.45D, 0.05D); // Strong center impact
        level.sendParticles(ParticleTypes.POOF, x, y, z, 35, 1.2D, 0.15D, 1.2D, 0.08D); // Ground dust
        level.sendParticles(ParticleTypes.CRIT, x, y + 0.35D, z, 30, 1.0D, 0.45D, 1.0D, 0.25D); // Impact sparks

        spawnImpactRing(level, x, y, z, 1.5D, 16); // Inner shockwave
        spawnImpactRing(level, x, y, z, 2.75D, 24); // Middle shockwave
        spawnImpactRing(level, x, y, z, 4.0D, 32); // Outer shockwave

        level.playSound(null, target.blockPosition(), SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 1.0F, 0.65F);

        AABB area = target.getBoundingBox().inflate(4.0D, 1.5D, 4.0D);

        for (LivingEntity nearby : level.getEntitiesOfClass(LivingEntity.class, area)) {
            if (nearby == player || nearby == target || !nearby.isAlive()) continue;
            if (!canHit(player, nearby)) continue;

            applyScriptedDamage(player, nearby, baseDamage * SHOCKWAVE_DAMAGE_MULTIPLIER);

            Vec3 push = nearby.position().subtract(target.position());
            Vec3 horizontal = new Vec3(push.x, 0.0D, push.z);

            if (horizontal.lengthSqr() > 0.0001D) {
                Vec3 direction = horizontal.normalize();
                nearby.setDeltaMovement(direction.x * 0.75D, 0.50D, direction.z * 0.75D); // Throw nearby enemies away from the impact
                nearby.hurtMarked = true;
            }
        }
    }

    private static void spawnImpactRing(ServerLevel level, double x, double y, double z, double radius, int particles) {
        for (int i = 0; i < particles; i++) {
            double angle = (Math.PI * 2.0D * i) / particles;
            double px = x + Math.cos(angle) * radius;
            double pz = z + Math.sin(angle) * radius;

            level.sendParticles(ParticleTypes.SWEEP_ATTACK, px, y + 0.1D, pz, 1, 0.0D, 0.0D, 0.0D, 0.0D);
            level.sendParticles(ParticleTypes.POOF, px, y, pz, 2, 0.12D, 0.05D, 0.12D, 0.02D);
        }
    }

    private static boolean applyScriptedDamage(ServerPlayer player, LivingEntity target, float damage) {
        target.invulnerableTime = 0;

        boolean hit = ScriptedAttackDamageHandler.hurt(() ->
                target.hurt(player.damageSources().playerAttack(player), damage)
        );

        target.invulnerableTime = 0;
        return hit;
    }

    private static boolean canHit(ServerPlayer player, LivingEntity target) {
        if (player.getServer() == null) return true;

        Party party = WorldData.get(player.getServer()).getPartyFromMember(player.getUUID());
        return party == null || party.getMember(target.getUUID()) == null || party.getFriendlyFire();
    }

    private static LivingEntity getTarget(ServerPlayer player, AerialSlamState state) {
        MinecraftServer server = player.getServer();
        if (server == null) return null;

        ServerLevel level = server.getLevel(state.dimension);
        if (level == null || player.level() != level) return null;

        Entity entity = level.getEntity(state.targetUUID);
        if (!(entity instanceof LivingEntity target) || !target.isAlive()) return null;
        if (player.distanceTo(target) > MAX_TARGET_DISTANCE) return null;

        return target;
    }

    private static void faceTarget(ServerPlayer player, LivingEntity target) {
        Vec3 direction = target.position().subtract(player.position());
        double horizontalDistance = Math.sqrt(direction.x * direction.x + direction.z * direction.z);

        float yaw = (float) (Math.atan2(direction.z, direction.x) * (180.0D / Math.PI)) - 90.0F;
        float pitch = (float) (-Math.atan2(direction.y, horizontalDistance) * (180.0D / Math.PI));

        player.setYRot(yaw);
        player.setYHeadRot(yaw);
        player.setXRot(pitch);
    }
}