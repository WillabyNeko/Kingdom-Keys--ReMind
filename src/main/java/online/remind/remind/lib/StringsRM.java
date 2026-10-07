package online.remind.remind.lib;

import online.remind.remind.KingdomKeysReMind;

public class StringsRM {

	public static final byte
		darkStepType = 0, 
		lightStepType = 1,
        twilightStepType = 2,
        rageStepType = 3,
        orgStepType = 4;
	
    public static final String
            // Prefixes
        ABMA_Prefix = "ability_",
        DFMA_Prefix = "form_",
        KBMA_Prefix = "keyblade_",
        KCMA_Prefix = "keychain_",
        SLMA_Prefix = "shotlock_",
        ARMA_Prefix = "armor_",
        ACMA_Prefix = "accessory_",
        MENU_Prefix = "menu_button",
        RCMA_Prefix = "rc_",
        ATMA_Prefix = "attack_",
        MAMA_Prefix = "magic_",
        LIMA_Prefix = "limit_",
        DE_Prefix = "dreameater_",


        //Forms
        rageForm = StringsRM.DFMA_Prefix+"rage",
        darkForm = StringsRM.DFMA_Prefix+"dark",
        lightForm = StringsRM.DFMA_Prefix+"light",
        twilight = StringsRM.DFMA_Prefix+"twilight",

        fireStorm = StringsRM.DFMA_Prefix+"firestorm",
        diamondDust = StringsRM.DFMA_Prefix+"diamond_dust",
        thunderBolt = StringsRM.DFMA_Prefix+"thunder_bolt",
        feverPitch = StringsRM.DFMA_Prefix+"fever_pitch",
        criticalImpact = StringsRM.DFMA_Prefix+"critical_impact",
        spellweaver = StringsRM.DFMA_Prefix+"spellweaver",
        darkImpulse = StringsRM.DFMA_Prefix+"dark_impulse",
        bloodlust = StringsRM.DFMA_Prefix+"bloodlust",
        exSoldier = StringsRM.DFMA_Prefix+"ex_soldier",

        regenForm = StringsRM.DFMA_Prefix+"regen",
        draconicLiberation = StringsRM.DFMA_Prefix+"draconic_liberation",

        // Magic
        Magic_Haste = StringsRM.MAMA_Prefix+"haste",
        Magic_Hastera = StringsRM.MAMA_Prefix+"hastera",
        Magic_Hastega = StringsRM.MAMA_Prefix+"hastega",
        Magic_Slow= StringsRM.MAMA_Prefix+"slow",
        Magic_Slowra = StringsRM.MAMA_Prefix+"slowra",
        Magic_Slowga = StringsRM.MAMA_Prefix+"slowga",
        Magic_Holy= StringsRM.MAMA_Prefix+"holy",
        Magic_Holyra = StringsRM.MAMA_Prefix+"holyra",
        Magic_Holyga = StringsRM.MAMA_Prefix+"holyga",
        Magic_Ruin = StringsRM.MAMA_Prefix+"ruin",
        Magic_Ruinra = StringsRM.MAMA_Prefix+"ruinra",
        Magic_Ruinga = StringsRM.MAMA_Prefix+"ruinga",
        Magic_Esuna = StringsRM.MAMA_Prefix+"esuna",
        Magic_Group_Esuna = StringsRM.MAMA_Prefix+"group_esuna",
        Magic_Dispel = StringsRM.MAMA_Prefix+"dispel",
        Magic_Berserk = StringsRM.MAMA_Prefix+"berserk",
        Magic_Berserkra = StringsRM.MAMA_Prefix+"berserkra",
        Magic_Berserkga = StringsRM.MAMA_Prefix+"berserkga",
        Magic_Auto_Life = StringsRM.MAMA_Prefix+"auto-life",
        Magic_Regen = StringsRM.MAMA_Prefix+"regen",
        Magic_Regenra = StringsRM.MAMA_Prefix+"regenra",
        Magic_Regenga = StringsRM.MAMA_Prefix+"regenga",
        Magic_Confuse = StringsRM.MAMA_Prefix+"confuse",
        Magic_Confusera = StringsRM.MAMA_Prefix+"confusera",
        Magic_Confusega = StringsRM.MAMA_Prefix+"confusega",
        Magic_Silence = StringsRM.MAMA_Prefix+"silence",
        Magic_Silencera = StringsRM.MAMA_Prefix+"silencera",
        Magic_Silencega = StringsRM.MAMA_Prefix+"silencega",
        Magic_Drain = StringsRM.MAMA_Prefix+"drain",
        Magic_Drainra = StringsRM.MAMA_Prefix+"drainra",
        Magic_Drainga = StringsRM.MAMA_Prefix+"drainga",
        Magic_Osmose = StringsRM.MAMA_Prefix+"osmose",
        Magic_Osmosera = StringsRM.MAMA_Prefix+"osmosera",
        Magic_Osmosega = StringsRM.MAMA_Prefix+"osmosega",

        Magic_Steal = StringsRM.MAMA_Prefix+"steal",
        Magic_Death = StringsRM.MAMA_Prefix+"death",
        Magic_Comet = StringsRM.MAMA_Prefix+"comet",
        Magic_Meteor = StringsRM.MAMA_Prefix+"meteor",
        Magic_Faith = StringsRM.MAMA_Prefix+"faith",
        Magic_Ultima= StringsRM.MAMA_Prefix+"ultima",
        Magic_Zettaflare = StringsRM.MAMA_Prefix+"zettaflare",
        Magic_Recall = StringsRM.MAMA_Prefix+"recall",

        Magic_Balloon = StringsRM.MAMA_Prefix+"balloon",
        Magic_Balloonra = StringsRM.MAMA_Prefix+"balloonra",
        Magic_Balloonga = StringsRM.MAMA_Prefix+"balloonga",
        Magic_Warp = StringsRM.MAMA_Prefix+"warp",

        Magic_Ported_Notice = "magic.kkremind.ported.notice",

        // Attacks
        Attack_Quick_Blitz = StringsRM.ATMA_Prefix+"quick_blitz",
        Attack_Sliding_Dash = StringsRM.ATMA_Prefix+"sliding_dash",
        Attack_Fire_Strike = StringsRM.ATMA_Prefix+"fire_strike",
        Attack_Blizzard_Strike = StringsRM.ATMA_Prefix+"blizzard_strike",
        Attack_Thunder_Strike = StringsRM.ATMA_Prefix+"thunder_strike",
        Attack_Water_Strike = StringsRM.ATMA_Prefix+"water_strike",
        Attack_Aero_Strike = StringsRM.ATMA_Prefix+"aero_strike",
        Attack_Light_Strike = StringsRM.ATMA_Prefix+"light_strike",
        Attack_Dark_Strike = StringsRM.ATMA_Prefix+"dark_strike",
        Attack_Binding_Strike = StringsRM.ATMA_Prefix+"binding_strike",
        Attack_Confusion_Strike = StringsRM.ATMA_Prefix+"confusion_strike",
        Attack_Blitz = StringsRM.ATMA_Prefix+"blitz",
        Attack_Slot_Edge = StringsRM.ATMA_Prefix+"slot_edge",
        Attack_Fire_Surge = StringsRM.ATMA_Prefix+"fire_surge",
        Attack_Fira_Surge = StringsRM.ATMA_Prefix+"fira_surge",
        Attack_Firaga_Surge = StringsRM.ATMA_Prefix+"firaga_surge",
        Attack_Blizzard_Surge = StringsRM.ATMA_Prefix+"blizzard_surge",
        Attack_Blizzara_Surge = StringsRM.ATMA_Prefix+"blizzara_surge",
        Attack_Blizzaga_Surge = StringsRM.ATMA_Prefix+"blizzaga_surge",
        Attack_Thunder_Surge = StringsRM.ATMA_Prefix+"thunder_surge",
        Attack_Thundara_Surge = StringsRM.ATMA_Prefix+"thundara_surge",
        Attack_Thundaga_Surge = StringsRM.ATMA_Prefix+"thundaga_surge",
        Attack_Water_Surge = StringsRM.ATMA_Prefix+"water_surge",
        Attack_Watera_Surge = StringsRM.ATMA_Prefix+"watera_surge",
        Attack_Waterga_Surge = StringsRM.ATMA_Prefix+"waterga_surge",
        Attack_Aero_Surge = StringsRM.ATMA_Prefix+"aero_surge",
        Attack_Aerora_Surge = StringsRM.ATMA_Prefix+"aerora_surge",
        Attack_Aeroga_Surge = StringsRM.ATMA_Prefix+"aeroga_surge",
        Attack_Light_Surge = StringsRM.ATMA_Prefix+"light_surge",
        Attack_Lightra_Surge = StringsRM.ATMA_Prefix+"lightra_surge",
        Attack_Lightga_Surge = StringsRM.ATMA_Prefix+"lightga_surge",
        Attack_Dark_Surge = StringsRM.ATMA_Prefix+"dark_surge",
        Attack_Darkra_Surge = StringsRM.ATMA_Prefix+"darkra_surge",
        Attack_Darkga_Surge = StringsRM.ATMA_Prefix+"darkga_surge",
        Attack_Zantetsuken = StringsRM.ATMA_Prefix+"zantetsuken",
        Attack_Swift_Strike = StringsRM.ATMA_Prefix+"swift_strike",







    //Ability List
        darkPassage = KingdomKeysReMind.MODID+":"+ StringsRM.ABMA_Prefix+"dark_passage",
        darknessBoost = KingdomKeysReMind.MODID+":"+ StringsRM.ABMA_Prefix+"darkness_boost",
        lightBoost = KingdomKeysReMind.MODID+":"+ StringsRM.ABMA_Prefix+"light_boost",
        mpBoost = KingdomKeysReMind.MODID+":"+ StringsRM.ABMA_Prefix+"mp_boost",
        hpBoost = KingdomKeysReMind.MODID+":"+ StringsRM.ABMA_Prefix+"hp_boost",
        situationBoost = KingdomKeysReMind.MODID+":"+ StringsRM.ABMA_Prefix+"situation_boost",
        cure_converter = KingdomKeysReMind.MODID+":"+ StringsRM.ABMA_Prefix+"cure_converter",
        rageAwakened = KingdomKeysReMind.MODID+":"+ StringsRM.ABMA_Prefix+"rage_awakened",
        darkPower = KingdomKeysReMind.MODID+":"+ StringsRM.ABMA_Prefix+"dark_power",
        wayToLight = KingdomKeysReMind.MODID+":"+ StringsRM.ABMA_Prefix+"way_to_light",
        roadToDawn = KingdomKeysReMind.MODID+":"+ StringsRM.ABMA_Prefix+"road_to_dawn",
        lightStep = KingdomKeysReMind.MODID+":"+ StringsRM.ABMA_Prefix+"light_step",
        darkStep = KingdomKeysReMind.MODID+":"+ StringsRM.ABMA_Prefix+"dark_step",
        adrenaline = KingdomKeysReMind.MODID+":"+ StringsRM.ABMA_Prefix+"adrenaline",
        critical_surge = KingdomKeysReMind.MODID+":"+ StringsRM.ABMA_Prefix+"critical_surge",
        lightWithin = KingdomKeysReMind.MODID+":"+ StringsRM.ABMA_Prefix+"light_within",
        darknessWithin = KingdomKeysReMind.MODID+":"+ StringsRM.ABMA_Prefix+"darkness_within",
        dedication = KingdomKeysReMind.MODID+":"+ StringsRM.ABMA_Prefix+"dedication",
        hpWalker = KingdomKeysReMind.MODID+":"+ StringsRM.ABMA_Prefix+"hp_walker",
        mpWalker = KingdomKeysReMind.MODID+":"+ StringsRM.ABMA_Prefix+"mp_walker",
        focusWalker = KingdomKeysReMind.MODID+":"+ StringsRM.ABMA_Prefix+"focus_walker",
        heartWalker = KingdomKeysReMind.MODID+":"+ StringsRM.ABMA_Prefix+"heart_walker",
        expWalker = KingdomKeysReMind.MODID+":"+ StringsRM.ABMA_Prefix+"exp_walker",
        mpShield = KingdomKeysReMind.MODID+":"+ StringsRM.ABMA_Prefix+"mp_shield",
        vehemence = KingdomKeysReMind.MODID+":"+ StringsRM.ABMA_Prefix+"vehemence",
        riskCharge = KingdomKeysReMind.MODID+":"+ StringsRM.ABMA_Prefix+"riskcharge",
        attackHaste = KingdomKeysReMind.MODID+":"+ StringsRM.ABMA_Prefix+"attack_haste",
        heartsPower = KingdomKeysReMind.MODID+":"+ StringsRM.ABMA_Prefix+"hearts_power",
        friendsPower = KingdomKeysReMind.MODID+":"+ StringsRM.ABMA_Prefix+"friends_power",
        renewalBlock = KingdomKeysReMind.MODID+":"+ StringsRM.ABMA_Prefix+"renewal_block",
        focusBlock = KingdomKeysReMind.MODID+":"+ StringsRM.ABMA_Prefix+"focus_block",
        stopBlock = KingdomKeysReMind.MODID+":"+ StringsRM.ABMA_Prefix+"stop_block",
        confusionBlock = KingdomKeysReMind.MODID+":"+ StringsRM.ABMA_Prefix+"confusion_block",
        poisonBlock = KingdomKeysReMind.MODID+":"+ StringsRM.ABMA_Prefix+"poison_block",
        royalGuard = KingdomKeysReMind.MODID+":"+ StringsRM.ABMA_Prefix+"royal_guard",
        blockReplenisher = KingdomKeysReMind.MODID+":"+StringsRM.ABMA_Prefix+"block_replenisher",
        spellblade = KingdomKeysReMind.MODID+":"+ StringsRM.ABMA_Prefix+"spellblade",
        ultima_weapon_ability = KingdomKeysReMind.MODID+":"+ StringsRM.ABMA_Prefix+"ultima_weapon",
        munny_magic = KingdomKeysReMind.MODID+":"+StringsRM.ABMA_Prefix+"munny_magic",

        lightInfusion = KingdomKeysReMind.MODID+":"+ StringsRM.ABMA_Prefix+"light_infusion",
        darkInfusion = KingdomKeysReMind.MODID+":"+ StringsRM.ABMA_Prefix+"dark_infusion",
        twilightInfusion = KingdomKeysReMind.MODID+":"+ StringsRM.ABMA_Prefix+"twilight_infusion",

        mpSlow = KingdomKeysReMind.MODID+":"+ StringsRM.ABMA_Prefix+"mp_slow",
        mpSlowra = KingdomKeysReMind.MODID+":"+ StringsRM.ABMA_Prefix+"mp_slowra",
        mpSlowga = KingdomKeysReMind.MODID+":"+ StringsRM.ABMA_Prefix+"mp_slowga",
        oneHP = KingdomKeysReMind.MODID+":"+ StringsRM.ABMA_Prefix+"one_hp",
        ribbon = KingdomKeysReMind.MODID+":"+ StringsRM.ABMA_Prefix+"ribbon",
        xpConverter = KingdomKeysReMind.MODID+":"+ StringsRM.ABMA_Prefix+"exp_converter",

        //Placeholders for Spirit System
        none = "none",
        chirithy = StringsRM.DE_Prefix+"chirithy",
        meowWow = StringsRM.DE_Prefix+"meowwow",
        komoryBat = StringsRM.DE_Prefix+"komory_bat",
        cactuar = StringsRM.DE_Prefix+"cactuar",
        tonberry = StringsRM.DE_Prefix+"tonberry",


    // Grand Magics
        seekerMine = KingdomKeysReMind.MODID+":"+StringsRM.ABMA_Prefix+"seeker_mine",

        // FF Keyblade Abilities
        Tidus = KingdomKeysReMind.MODID+":"+ StringsRM.ABMA_Prefix+"tidus",
        Jecht = KingdomKeysReMind.MODID+":"+ StringsRM.ABMA_Prefix+"jecht",

        // Custom Abilities
        Lyric1 = KingdomKeysReMind.MODID+":"+ StringsRM.ABMA_Prefix+"lyric1",
        Lyric2 = KingdomKeysReMind.MODID+":"+ StringsRM.ABMA_Prefix+"lyric2",

        Xephiro = KingdomKeysReMind.MODID+":"+ StringsRM.ABMA_Prefix+"xephiro",
        Rose = KingdomKeysReMind.MODID+":"+ StringsRM.ABMA_Prefix+"rose",

        Regen = KingdomKeysReMind.MODID+":"+ StringsRM.ABMA_Prefix+"regen",
        Exceed = KingdomKeysReMind.MODID+":"+ StringsRM.ABMA_Prefix+"exceed",
        SilenceHeart = KingdomKeysReMind.MODID+":"+ StringsRM.ABMA_Prefix+"silence_heart",

        // Reprisals
        counterHammer = KingdomKeysReMind.MODID+":"+ StringsRM.ABMA_Prefix+"counter_hammer",
        counterBlast = KingdomKeysReMind.MODID+":"+ StringsRM.ABMA_Prefix+"counter_blast",
        counterRush = KingdomKeysReMind.MODID+":"+ StringsRM.ABMA_Prefix+"counter_rush",

        // Shotlocks
        flameSalvo = "flame_salvo",
        bubbleBlaster = "bubble_blaster",

        thunderStorm = "thunderstorm",
        bioBarrage = "bio_barrage",
        meteorShower = "meteor_shower",
        darkDivide = "dark_divide",

        heartlessAngel = "heartless_angel",
        omnislash = "omnislash",

        // Reaction Commands
        riskchargeRC = KingdomKeysReMind.MODID+":"+StringsRM.RCMA_Prefix+"riskcharge",
        ragingBurst = KingdomKeysReMind.MODID+":"+StringsRM.RCMA_Prefix+"raging_burst",
        testRC = KingdomKeysReMind.MODID+":"+StringsRM.RCMA_Prefix+"test",
        LightBeamRC = KingdomKeysReMind.MODID+":"+StringsRM.RCMA_Prefix+"light_beam",
        DarkMineRC = KingdomKeysReMind.MODID+":"+StringsRM.RCMA_Prefix+"dark_mine",
        TwilightRC = KingdomKeysReMind.MODID+":"+StringsRM.RCMA_Prefix+"twilight",
        RageRC = KingdomKeysReMind.MODID+":"+StringsRM.RCMA_Prefix+"rage",
        DualShotRC = KingdomKeysReMind.MODID+":"+StringsRM.RCMA_Prefix+"dual_shot",
        DarkFiragaRC = KingdomKeysReMind.MODID+":"+StringsRM.RCMA_Prefix+"dark_firaga",
        XemnasRC = KingdomKeysReMind.MODID+":"+StringsRM.RCMA_Prefix+"xemnas",
        XaldinRC = KingdomKeysReMind.MODID+":"+StringsRM.RCMA_Prefix+"xaldin",
        XigbarRC = KingdomKeysReMind.MODID+":"+StringsRM.RCMA_Prefix+"xigbar",
        vexenRC = KingdomKeysReMind.MODID+":"+StringsRM.RCMA_Prefix+"vexen",
        ZexionRC = KingdomKeysReMind.MODID+":"+StringsRM.RCMA_Prefix+"zexion",
        BlitzRC = KingdomKeysReMind.MODID+":"+StringsRM.RCMA_Prefix+"blitz",
        SlotEdgeRC = KingdomKeysReMind.MODID+":"+StringsRM.RCMA_Prefix+"slot_edge",
        SonicBladeRC = KingdomKeysReMind.MODID+":"+StringsRM.RCMA_Prefix+"sonic_blade",
        ChaosBladeRC = KingdomKeysReMind.MODID+":"+StringsRM.RCMA_Prefix+"chaos_blade",
        ArsBashRC = KingdomKeysReMind.MODID+":"+StringsRM.RCMA_Prefix+"ars_bash",
        ArsFinishRC = KingdomKeysReMind.MODID+":"+StringsRM.RCMA_Prefix+"ars_finish",
        AerialSlamRC = KingdomKeysReMind.MODID+":"+StringsRM.RCMA_Prefix+"aerial_slam",

        CounterHammerRC = KingdomKeysReMind.MODID+":"+StringsRM.RCMA_Prefix+"counter_hammer",
        CounterBlastRC = KingdomKeysReMind.MODID+":"+StringsRM.RCMA_Prefix+"counter_blast",
        CounterRushRC = KingdomKeysReMind.MODID+":"+StringsRM.RCMA_Prefix+"counter_rush",

        FinishRC = KingdomKeysReMind.MODID+":"+StringsRM.RCMA_Prefix+"finish",
        FireStormRC = KingdomKeysReMind.MODID+":"+StringsRM.RCMA_Prefix+"firestorm",
        DiamondDustRC = KingdomKeysReMind.MODID+":"+StringsRM.RCMA_Prefix+"diamond_dust",
        ThunderBoltRC = KingdomKeysReMind.MODID+":"+StringsRM.RCMA_Prefix+"thunder_bolt",
        FeverPitchRC = KingdomKeysReMind.MODID+":"+StringsRM.RCMA_Prefix+"fever_pitch",
        CriticalImpactRC = KingdomKeysReMind.MODID+":"+StringsRM.RCMA_Prefix+"critical_impact",
        SpellweaverRC = KingdomKeysReMind.MODID+":"+StringsRM.RCMA_Prefix+"spellweaver",
        DarkImpulseRC = KingdomKeysReMind.MODID+":"+StringsRM.RCMA_Prefix+"dark_impulse",
        BloodlustRC = KingdomKeysReMind.MODID+":"+StringsRM.RCMA_Prefix+"bloodlust",
        ExSoldierRC = KingdomKeysReMind.MODID+":"+StringsRM.RCMA_Prefix+"ex_soldier",

        RegenRC = KingdomKeysReMind.MODID+":"+StringsRM.RCMA_Prefix+"regen",
        ExceedRC = KingdomKeysReMind.MODID+":"+StringsRM.RCMA_Prefix+"exceed",
        RoseRC = KingdomKeysReMind.MODID+":"+StringsRM.RCMA_Prefix+"rose",

        // Limits

        firagaWall = "firaga_wall",


        //Keyblades
        xephiroKeyblade = KingdomKeysReMind.MODID+":"+ StringsRM.KBMA_Prefix+"xephiro_keyblade",

        //Keychains
        xephiroKeybladeChain = KingdomKeysReMind.MODID+":"+ StringsRM.KCMA_Prefix+"xephiro_keyblade_chain",

        // KK Armor


        // KK Accessories
        luckOfTheDraw = KingdomKeysReMind.MODID+":"+ StringsRM.ACMA_Prefix+"luck_of_the_draw",
        lightHeart = KingdomKeysReMind.MODID+":"+ StringsRM.ACMA_Prefix+"lightHeart",
        darkHeart = KingdomKeysReMind.MODID+":"+ StringsRM.ACMA_Prefix+"darkHeart",

        // GUI
        Gui_Menu_Button_Prestige = StringsRM.MENU_Prefix + ".prestige",
        Gui_Menu_Button_PrestigeLevel = StringsRM.MENU_Prefix + ".prestigeLevel",
        Gui_Menu_Button_PrestigeConfirm = StringsRM.MENU_Prefix +".prestigeConfirm",
        Gui_Menu_Button_DreamEater = StringsRM.MENU_Prefix + ".dreamEater",
        Gui_Menu_Button_Panel = StringsRM.MENU_Prefix + ".panel",
        Gui_Menu_Button_Wiki = StringsRM.MENU_Prefix + ".wiki",
        Gui_Menu_Button_Keyblades = StringsRM.MENU_Prefix + ".keyblades",
        Gui_Menu_Button_Attack = StringsRM.MENU_Prefix + ".attack",
        Gui_Menu_Button_Magic = StringsRM.MENU_Prefix + ".magic",
        Gui_Menu_Button_Ability = StringsRM.MENU_Prefix + ".ability",
        Gui_Menu_Button_Forms = StringsRM.MENU_Prefix + ".forms",
        Gui_Menu_Button_Armor = StringsRM.MENU_Prefix + ".armor",
        Gui_Menu_Button_Accessories = StringsRM.MENU_Prefix + ".accessory",
        Gui_Menu_Button_Shotlocks = StringsRM.MENU_Prefix + ".shotlock",
        Gui_Menu_Button_Credits = StringsRM.MENU_Prefix + ".creditsScreen",
        Gui_Menu_Button_Wallet = StringsRM.MENU_Prefix + ".wallet";
}
