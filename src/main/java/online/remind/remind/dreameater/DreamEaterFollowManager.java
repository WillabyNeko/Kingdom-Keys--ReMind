package online.remind.remind.dreameater;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import online.kingdomkeys.kingdomkeys.data.PlayerData;
import online.kingdomkeys.kingdomkeys.util.Utils;
import online.remind.remind.KingdomKeysReMind;
import online.remind.remind.capabilities.GlobalDataRM;
import online.remind.remind.capabilities.ModDataRM;
import online.remind.remind.entity.spirits.CactuarSpiritEntity;
import online.remind.remind.entity.spirits.ChirithyEntity;
import online.remind.remind.entity.spirits.KomoryBatEntity;
import online.remind.remind.entity.spirits.MeowWowEntity;
import online.remind.remind.entity.spirits.TonberrySpiritEntity;
import online.remind.remind.lib.StringsRM;
import online.remind.remind.network.PacketHandlerRM;
import online.remind.remind.network.cts.CSSummonSpiritPacket;

import java.util.UUID;

@EventBusSubscriber(modid = KingdomKeysReMind.MODID, bus = EventBusSubscriber.Bus.GAME)
public final class DreamEaterFollowManager {

    private static final double REPLACE_DISTANCE = 48.0D;
    private static final double REPLACE_DISTANCE_SQR =
            REPLACE_DISTANCE * REPLACE_DISTANCE;

    private DreamEaterFollowManager() {
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        // We do not need to scan every tick.
        if (player.tickCount % 5 != 0) {
            return;
        }

        tick(player);
    }

    private static void tick(ServerPlayer player) {
        if (!player.isAlive()) {
            return;
        }

        GlobalDataRM data = ModDataRM.getGlobal(player);

        if (data == null || !data.hasDreamEaterSummoned()) {
            return;
        }

        UUID trackedUUID = data.getDreamEaterUUID();

        if (trackedUUID == null) {
            return;
        }

        Entity summon = findTrackedSummon(player, trackedUUID);

        // If the tracked entity is no longer loaded, recreate the physical summon.
        if (summon == null) {
            replaceSummon(player, data, trackedUUID, null);
            return;
        }

        // Nearby and in the same dimension: leave it entirely to normal AI.
        if (summon.level() == player.level()
                && summon.distanceToSqr(player) <= REPLACE_DISTANCE_SQR) {
            return;
        }

        // Huge same-dimension jump or any dimension change:
        // replace the entity instead of teleporting the existing instance.
        replaceSummon(player, data, trackedUUID, summon);
    }

    private static Entity findTrackedSummon(
            ServerPlayer player,
            UUID uuid
    ) {
        if (player.getServer() == null) {
            return null;
        }

        for (ServerLevel level : player.getServer().getAllLevels()) {
            Entity entity = level.getEntity(uuid);

            if (entity != null && !entity.isRemoved()) {
                return entity;
            }
        }

        return null;
    }

    private static void replaceSummon(
            ServerPlayer player,
            GlobalDataRM data,
            UUID oldUUID,
            Entity oldSummon
    ) {
        float healthPercent = 1.0F;

        if (oldSummon instanceof LivingEntity living
                && living.getMaxHealth() > 0.0F) {
            healthPercent = Math.max(
                    0.01F,
                    Math.min(1.0F, living.getHealth() / living.getMaxHealth())
            );
        }

        if (oldUUID != null) {
            CSSummonSpiritPacket.removeSpiritFromParty(player, oldUUID);
        }

        if (oldSummon != null && !oldSummon.isRemoved()) {
            oldSummon.discard();
        }

        LivingEntity replacement = createSelectedSummon(player, data);

        if (replacement == null) {
            return;
        }

        player.serverLevel().addFreshEntity(replacement);

        replacement.setHealth(
                Math.max(
                        1.0F,
                        replacement.getMaxHealth() * healthPercent
                )
        );

        data.setDreamEaterUUID(replacement.getUUID());
        data.setHasDreamEaterSummoned(true);

        CSSummonSpiritPacket.addSpiritToParty(player, replacement);
        PacketHandlerRM.syncGlobalToAllAround(player, data);
    }

    private static LivingEntity createSelectedSummon(
            ServerPlayer player,
            GlobalDataRM data
    ) {
        String dreamEaterRL = data.getDreamEaterRL();

        if (dreamEaterRL == null || dreamEaterRL.isEmpty()) {
            return null;
        }

        DreamEater dreamEater;

        try {
            dreamEater = ModDreamEaters.registry.get(
                    ResourceLocation.parse(dreamEaterRL)
            );
        } catch (Exception ignored) {
            return null;
        }

        if (dreamEater == null
                || StringsRM.none.equals(dreamEater.getName())) {
            return null;
        }

        PlayerData kkData = PlayerData.get(player);
        ServerLevel level = player.serverLevel();

        switch (dreamEater.getName()) {
            case StringsRM.chirithy: {
                ChirithyEntity summon =
                        new ChirithyEntity(level, player);

                summon.setOwnerUUID(player.getUUID());
                summon.setPos(
                        player.getX() + 1.0D,
                        player.getY() + 2.0D,
                        player.getZ() + 1.0D
                );

                summon.setVariant(
                        kkData != null
                                && kkData.getAlignment() != Utils.OrgMember.NONE
                                ? 0
                                : 1
                );

                return summon;
            }

            case StringsRM.meowWow: {
                MeowWowEntity summon =
                        new MeowWowEntity(level, player);

                summon.setOwnerUUID(player.getUUID());
                summon.setPos(
                        player.getX() + 1.0D,
                        player.getY() + 1.0D,
                        player.getZ() + 1.0D
                );

                summon.setVariant(
                        kkData != null
                                && kkData.getAlignment() != Utils.OrgMember.NONE
                                ? MeowWowEntity.VARIANT_ORG
                                : MeowWowEntity.VARIANT_NORMAL
                );

                return summon;
            }

            case StringsRM.komoryBat: {
                KomoryBatEntity summon =
                        new KomoryBatEntity(level, player);

                summon.setOwnerUUID(player.getUUID());
                summon.setPos(
                        player.getX() + 1.0D,
                        player.getY() + 2.4D,
                        player.getZ() + 1.0D
                );

                summon.setVariant(
                        kkData != null
                                && kkData.getAlignment() != Utils.OrgMember.NONE
                                ? KomoryBatEntity.VARIANT_ORG
                                : KomoryBatEntity.VARIANT_NORMAL
                );

                return summon;
            }

            case "dreameater_cactuar":
            case "cactuar": {
                CactuarSpiritEntity summon =
                        new CactuarSpiritEntity(level, player);

                summon.setOwnerUUID(player.getUUID());
                summon.setPos(
                        player.getX() + 1.0D,
                        player.getY(),
                        player.getZ() + 1.0D
                );

                return summon;
            }

            case "dreameater_tonberry":
            case "tonberry": {
                TonberrySpiritEntity summon =
                        new TonberrySpiritEntity(level, player);

                summon.setOwnerUUID(player.getUUID());
                summon.setPos(
                        player.getX() + 1.0D,
                        player.getY() + 0.1D,
                        player.getZ() + 1.0D
                );

                return summon;
            }

            default:
                return null;
        }
    }
}
