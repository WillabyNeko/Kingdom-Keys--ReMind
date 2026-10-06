package online.remind.remind.mixin;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;

import online.kingdomkeys.kingdomkeys.entity.mob.goal.PartyAllyGoals;

import online.remind.remind.entity.spirits.CactuarSpiritEntity;
import online.remind.remind.entity.spirits.ChirithyEntity;
import online.remind.remind.entity.spirits.KomoryBatEntity;
import online.remind.remind.entity.spirits.MeowWowEntity;
import online.remind.remind.entity.spirits.TonberrySpiritEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Mixin(value = PartyAllyGoals.class, remap = false)
public abstract class PartyAllyGoalsMixin {

    @Inject(
            method = "leader",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private static void kkremind$useActualSummonOwner(
            Mob mob,
            CallbackInfoReturnable<Player> cir
    ) {
        if (mob == null) {
            return;
        }

        boolean isReMindSummon =
                kkremind$isDreamEater(mob)
                        || kkremind$isRoseSummon(mob);

        /*
         * Not one of ours.
         *
         * Let Kingdom Keys use its normal party leader logic.
         */
        if (!isReMindSummon) {
            return;
        }

        UUID ownerUUID = kkremind$getOwnerUUID(mob);

        /*
         * This IS one of our summons, but it doesn't currently
         * have a valid owner.
         *
         * IMPORTANT:
         * Return null instead of allowing KK to fall back to the
         * party leader.
         */
        if (ownerUUID == null) {
            cir.setReturnValue(null);
            return;
        }

        MinecraftServer server = mob.level().getServer();

        if (server == null) {
            cir.setReturnValue(null);
            return;
        }

        ServerPlayer owner =
                server.getPlayerList().getPlayer(ownerUUID);

        /*
         * Don't let KK redirect the summon to the party leader
         * if its actual owner is offline/dead/in another dimension.
         */
        if (owner == null
                || !owner.isAlive()
                || owner.level() != mob.level()) {

            cir.setReturnValue(null);
            return;
        }

        /*
         * For this summon, its owner IS its effective party leader.
         *
         * PartyAllyGoals will now:
         *
         * - follow this player
         * - teleport toward this player
         * - defend this player
         * - attack things this player attacks
         */
        cir.setReturnValue(owner);
    }

    @Unique
    private static boolean kkremind$isDreamEater(Mob mob) {
        return mob instanceof ChirithyEntity
                || mob instanceof MeowWowEntity
                || mob instanceof KomoryBatEntity
                || mob instanceof CactuarSpiritEntity
                || mob instanceof TonberrySpiritEntity;
    }

    @Unique
    private static boolean kkremind$isRoseSummon(Mob mob) {
        CompoundTag data = mob.getPersistentData();

        return data.getBoolean("RoseSummon");
    }

    @Unique
    private static UUID kkremind$getOwnerUUID(Mob mob) {

        // =========================================================
        // Dream Eaters
        // =========================================================

        if (mob instanceof ChirithyEntity chirithy) {
            return chirithy.getOwnerUUID();
        }

        if (mob instanceof MeowWowEntity meowWow) {
            return meowWow.getOwnerUUID();
        }

        if (mob instanceof KomoryBatEntity komoryBat) {
            return komoryBat.getOwnerUUID();
        }

        if (mob instanceof CactuarSpiritEntity cactuar) {
            return cactuar.getOwnerUUID();
        }

        if (mob instanceof TonberrySpiritEntity tonberry) {
            return tonberry.getOwnerUUID();
        }

        // =========================================================
        // Rose summoned Shadows
        // =========================================================

        CompoundTag data = mob.getPersistentData();

        if (data.getBoolean("RoseSummon")
                && data.hasUUID("RoseSummoner")) {

            return data.getUUID("RoseSummoner");
        }

        return null;
    }
}