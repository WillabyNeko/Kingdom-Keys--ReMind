package online.remind.remind.dreameater;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import online.remind.remind.KingdomKeysReMind;
import online.remind.remind.capabilities.GlobalDataRM;
import online.remind.remind.capabilities.ModDataRM;
import online.remind.remind.config.ModConfigs;
import online.remind.remind.network.PacketHandlerRM;

@EventBusSubscriber(modid = KingdomKeysReMind.MODID)
public final class DreamEaterSummonCooldown {

    private DreamEaterSummonCooldown() {}

    public static void start(ServerPlayer player) {
        GlobalDataRM globalData = ModDataRM.getGlobal(player);
        if (globalData == null) return;

        int cooldownTicks = ModConfigs.summonCooldown * 20;

        globalData.setDreamEaterSummonCooldownTicks(cooldownTicks);
        PacketHandlerRM.syncGlobalToAllAround(player, globalData);
    }

    public static boolean isOnCooldown(Player player) {
        GlobalDataRM globalData = ModDataRM.getGlobal(player);
        return globalData != null && globalData.getDreamEaterSummonCooldownTicks() > 0;
    }

    public static long getRemainingSeconds(Player player) {
        GlobalDataRM globalData = ModDataRM.getGlobal(player);
        if (globalData == null) return 0L;

        int ticks = globalData.getDreamEaterSummonCooldownTicks();
        return (ticks + 19L) / 20L;
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        GlobalDataRM globalData = ModDataRM.getGlobal(player);
        if (globalData == null) return;

        int ticks = globalData.getDreamEaterSummonCooldownTicks();
        if (ticks <= 0) return;

        globalData.remDreamEaterSummonCooldownTicks(1);

        if (player.tickCount % 20 == 0 || globalData.getDreamEaterSummonCooldownTicks() == 0) {
            PacketHandlerRM.syncGlobalToAllAround(player, globalData);
        }
    }

    public static int getRemainingTicks(Player player) {
        GlobalDataRM globalData = ModDataRM.getGlobal(player);
        if (globalData == null) return 0;

        return globalData.getDreamEaterSummonCooldownTicks();
    }
}