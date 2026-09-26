package online.remind.remind.config;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;
import online.remind.remind.KingdomKeysReMind;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

@EventBusSubscriber(modid = KingdomKeysReMind.MODID, bus = EventBusSubscriber.Bus.MOD)
public class ModConfigs {
    private static CommonConfig COMMON;
    private static ClientConfigRM CLIENT;
    //public static ServerConfig SERVER;

    public static final ModConfigSpec COMMON_SPEC;
    public static final ModConfigSpec CLIENT_SPEC;
    //public static final ModConfigSpec SERVER_SPEC;

    static {
        {
            final Pair<CommonConfig, ModConfigSpec> specPair = new ModConfigSpec.Builder().configure(CommonConfig::new);
            COMMON = specPair.getLeft();
            COMMON_SPEC = specPair.getRight();
        }

        {
            final Pair<ClientConfigRM, ModConfigSpec> specPair = new ModConfigSpec.Builder().configure(ClientConfigRM::new);
            CLIENT = specPair.getLeft();
            CLIENT_SPEC = specPair.getRight();
        }
    }

    // HUD, read and written by Kingdom Keys' HUD editor through DreamEaterHUD's HUDDataStorage
    public static List<? extends Float> getDreamEaterHUDData() {
        return CLIENT.dreamEaterHUDData.get();
    }

    public static void setDreamEaterHUDData(List<? extends Float> data) {
        CLIENT.dreamEaterHUDData.set(data);
        CLIENT.dreamEaterHUDData.save();
    }

    public static void bakeCommon(){
        donorKeybladeGrant = COMMON.donorKeybladeGrant.get();
        rageFormPercent = COMMON.rageFormPercent.get();

        // NG+ Configs
        ngpEnabled = COMMON.ngpEnabled.get();
        statCap = COMMON.statCap.get();
        hpCap = COMMON.hpCap.get();
        mpCap = COMMON.mpCap.get();
        statBonus = COMMON.statBonus.get();

        // Panels Configs
        panelsEnabled = COMMON.panelsEnabled.get();
        levelsEnabled = COMMON.levelsEnabled.get();
        driveLevelsEnabled = COMMON.driveLevelsEnabled.get();
        panelBonus = COMMON.panelBonus.get();
        panelLimit = COMMON.panelLimit.get();

        // Dream Eater Configs
        spiritsEnabled = COMMON.spiritsEnabled.get();
        autoLifeCD = COMMON.autoLifeCD.get();
        xpMulti = COMMON.xpMulti.get();
        summonCooldown = COMMON.summonCooldown.get();

        // Ultima Weapon Ability Configs
        ultimaPositiveSTR = COMMON.ultimaPositiveSTR.get();
        ultimaNegativeSTR = COMMON.ultimaNegativeSTR.get();
        ultimaPositiveMAG = COMMON.ultimaPositiveMAG.get();
        ultimaNegativeMAG = COMMON.ultimaNegativeMAG.get();

        // Wallet/Coin Configs
        copperCoinValue = COMMON.copperCoinValue.get();
        silverCoinValue = COMMON.silverCoinValue.get();
        goldCoinValue = COMMON.goldCoinValue.get();
        emeraldCoinValue = COMMON.emeraldCoinValue.get();
        diamondCoinValue = COMMON.diamondCoinValue.get();
        netheriteCoinValue = COMMON.netheriteCoinValue.get();
        amethystCoinValue = COMMON.amethystCoinValue.get();
        heartCoinValue = COMMON.heartCoinValue.get();
        luxCoinValue = COMMON.luxCoinValue.get();

    }

    public static void bakeClient(){
        chirithySpellFeedback = CLIENT.chirithySpellFeedback.get();
    }

    public static boolean donorKeybladeGrant;

    // Forms
    public static double rageFormPercent;

    // NG+
    public static boolean ngpEnabled;
    public static int statCap;
    public static int statBonus;
    public static int hpCap;
    public static int mpCap;

    // Panels
    public static boolean panelsEnabled;
    public static boolean levelsEnabled;
    public static boolean driveLevelsEnabled;
    public static int panelBonus;
    public static int panelLimit;

    // Dream Eaters
    public static boolean spiritsEnabled;
    public static double autoLifeCD;
    public static double xpMulti;
    public static int summonCooldown;
    public static boolean chirithySpellFeedback;

    // Ultima Weapon Ability
    public static int ultimaPositiveSTR;
    public static int ultimaNegativeSTR;
    public static int ultimaPositiveMAG;
    public static int ultimaNegativeMAG;

    // Coin/Wallet
    public static int copperCoinValue;
    public static int silverCoinValue;
    public static int goldCoinValue;
    public static int emeraldCoinValue;
    public static int diamondCoinValue;
    public static int netheriteCoinValue;
    public static int amethystCoinValue;
    public static int heartCoinValue;
    public static int luxCoinValue;


    @SubscribeEvent
    public static void configEvent(ModConfigEvent event) {
        if (event.getConfig().getSpec() == COMMON_SPEC) {
            KingdomKeysReMind.LOGGER.info("LOAD COMMON CONFIG");
            bakeCommon();
        }

        if (event.getConfig().getSpec() == CLIENT_SPEC) {
            KingdomKeysReMind.LOGGER.info("LOAD CLIENT CONFIG");
            bakeClient();
        }
    }

}
