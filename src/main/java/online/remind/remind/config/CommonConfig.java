package online.remind.remind.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class CommonConfig {

    public ModConfigSpec.BooleanValue donorKeybladeGrant;

    public ModConfigSpec.DoubleValue rageFormPercent;

    // NG+
    public ModConfigSpec.BooleanValue ngpEnabled;
    public ModConfigSpec.IntValue statCap;
    public ModConfigSpec.IntValue statBonus;
    public ModConfigSpec.IntValue hpCap;
    public ModConfigSpec.IntValue mpCap;

    // Panels
    public ModConfigSpec.BooleanValue panelsEnabled;
    public ModConfigSpec.BooleanValue levelsEnabled;
    public ModConfigSpec.BooleanValue driveLevelsEnabled;
    public ModConfigSpec.IntValue panelBonus;
    public ModConfigSpec.IntValue panelLimit;

    // Dream Eater Configs
    public ModConfigSpec.BooleanValue spiritsEnabled;
    public ModConfigSpec.DoubleValue autoLifeCD;
    public ModConfigSpec.DoubleValue xpMulti;
    public ModConfigSpec.IntValue summonCooldown;



    // UW Ability
    public ModConfigSpec.IntValue ultimaPositiveSTR;
    public ModConfigSpec.IntValue ultimaNegativeSTR;

    public ModConfigSpec.IntValue ultimaPositiveMAG;
    public ModConfigSpec.IntValue ultimaNegativeMAG;

    // Coin/Wallet
    public ModConfigSpec.IntValue copperCoinValue;
    public ModConfigSpec.IntValue silverCoinValue;
    public ModConfigSpec.IntValue goldCoinValue;
    public ModConfigSpec.IntValue emeraldCoinValue;
    public ModConfigSpec.IntValue diamondCoinValue;
    public ModConfigSpec.IntValue netheriteCoinValue;
    public ModConfigSpec.IntValue amethystCoinValue;
    public ModConfigSpec.IntValue heartCoinValue;
    public ModConfigSpec.IntValue luxCoinValue;





    CommonConfig(final ModConfigSpec.Builder builder) {
        builder.push("General");

        donorKeybladeGrant = builder
                .comment("Enables Donators to get commissioned keyblades upon first join. True by Default.")
                .define("Give Donors Keyblades", true);

        builder.pop();
        builder.push("Forms");

        rageFormPercent = builder
                .comment("Changes the base chance for Rage Form's Reaction Command to appear. Setting this to 0 will disable the Reaction Command.")
                .comment("Default: 10.0.")
                .defineInRange("Base Rage Form Chance", 10.0,0,100);

        builder.pop();
        builder.push("NG+");

        ngpEnabled = builder
                .comment("Dictates if New Game + is enabled or not.")
                .comment("Default: true")
                .define("New Game + Enabled", true);
        statCap = builder
                .comment("Sets the maximum NG+ can give you stat wise. (This excludes HP and MP)")
                .comment("Default: 50")
                .defineInRange("Stat Cap:", 50, 0, 9999);
        statBonus = builder
                .comment("Sets the stat bonus per NG+ cycle.")
                .comment("Default: 1")
                .defineInRange("Stat Bonus:", 0, 0, 9999);
        hpCap = builder
                .comment("Sets the maximum HP that NG+ can give you.")
                .comment("Default: 100")
                .defineInRange("HP Cap:", 100, 0, 9999);
        mpCap = builder
                .comment("Sets the maximum MP that NG+ can give you. WARNING! Setting this too high will break the balance of certain spells!")
                .comment("Default: 100")
                .defineInRange("MP Cap:", 100, 0, 9999);
        builder.pop();
        builder.push("Panels");

        panelsEnabled = builder
                .comment("Dictates if Panels are enabled or not. NOTE: If false, you will get rid of the only way for Org members to level up forms aside from gathering the orbs.")
                .comment("Default: true")
                .define("Panels Enabled", true);
        levelsEnabled = builder
                .comment("Allows players to level their player level via Panels.")
                .comment("Default:true")
                .define("Leveling via Panels", true);
        driveLevelsEnabled = builder
                .comment("Allows players to level their Drive Form levels via Panels. This also allows for Easier Org Only Runs if set to true.")
                .comment("Default:true")
                .define("Drive Leveling via Panels", true);
        panelBonus = builder
                .comment("Sets the stat bonus for STR, MAG, and DEF in the panels menu. Note: Setting this to 0 will disable the boost entirely, and setting this too high can/will break balance!")
                .comment("Default: 1")
                .defineInRange("Panel Stat Bonus:", 1, 0, 9999);
        panelLimit = builder
                .comment("Sets the max stats given by the Panel System. Note: High numbers can/will break balance!")
                .comment("Default: 50")
                .defineInRange("Panels Cap:", 50, 1, 9999);

        builder.pop();
        builder.push("Spirits/Dream Eaters");
        spiritsEnabled = builder
                .comment("If set to 'true', allows the usage of the Dream Eater menus.")
                .comment("Default: true")
                .define("Spirits Enabled", true);
        autoLifeCD = builder
                .comment("Sets how long before Chirithy can cast Auto-Life on you again in minutes.")
                .comment("Default: 5.0.")
                .defineInRange("Chirithy Auto-Life Cooldown", 5.0,1,1000);
        xpMulti = builder
                .comment("Sets the Serverside EXP Multi for Dream Eaters.")
                .comment("Default: 1.0, Setting this to 0 WILL DISABLE DREAM EATERS LEVELING UP!")
                .defineInRange("Dream Eater EXP Multiplier", 1.0, 0, 1000);
        summonCooldown= builder
                .comment("Adjusts Dream Eater summoning cooldown in seconds.")
                .comment("Default: 15")
                .defineInRange("Summon Cooldown in Seconds", 15,0,9999);


        builder.pop();
        builder.push("Ultima Weapon Ability");

        ultimaPositiveSTR = builder
                .comment("Minimum STR Ultima Weapon boosts to when STR is positive but below this value")
                .defineInRange("ultimaPositiveSTR", 13, 0, 9999);

        ultimaNegativeSTR = builder
                .comment("Minimum STR Ultima Weapon boosts to when STR is negative")
                .defineInRange("ultimaNegativeSTR", 6, 0, 9999);

        ultimaPositiveMAG = builder
                .comment("Minimum MAG Ultima Weapon boosts to when MAG is positive but below this value")
                .defineInRange("ultimaPositiveMAG", 13, 0, 9999);

        ultimaNegativeMAG = builder
                .comment("Minimum MAG Ultima Weapon boosts to when MAG is negative")
                .defineInRange("ultimaNegativeMAG", 6, 0, 9999);

        builder.pop();

        builder.push("Coins and Wallet Configs [WIP] - MAY NOT WORK");

        copperCoinValue = builder
                .comment("Sets the value for the Copper Coin. Default: 1")
                .defineInRange("copperCoinValue", 1, 1, 999999);

        silverCoinValue = builder
                .comment("Sets the value for the Silver Coin. Default: 5")
                .defineInRange("silverCoinValue", 5, 1, 999999);

        goldCoinValue = builder
                .comment("Sets the value for the Gold Coin. Default: 10")
                .defineInRange("goldCoinValue", 10, 1, 999999);

        emeraldCoinValue = builder
                .comment("Sets the value for the Emerald Coin. Default: 50")
                .defineInRange("emeraldCoinValue", 50, 1, 999999);

        diamondCoinValue = builder
                .comment("Sets the value for the Diamond Coin. Default: 100")
                .defineInRange("diamondCoinValue", 100, 1, 999999);

        netheriteCoinValue = builder
                .comment("Sets the value for the Netherite Coin. Default: 500")
                .defineInRange("netheriteCoinValue", 500, 1, 999999);

        amethystCoinValue = builder
                .comment("Sets the value for the Amethyst Coin. Default: 1000")
                .defineInRange("amethystCoinValue", 1000, 1, 999999);

        heartCoinValue = builder
                .comment("Sets the value for the Heart Coin. Default: 1000")
                .defineInRange("heartCoinValue", 1000, 1, 999999);

        luxCoinValue = builder
                .comment("Sets the value for the Lux Coin. Default: 1000")
                .defineInRange("luxCoinValue", 1000, 1, 999999);

        builder.pop();
    }
}
