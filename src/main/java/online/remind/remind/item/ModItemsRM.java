package online.remind.remind.item;

import com.google.common.collect.ImmutableMap;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SmithingTemplateItem;
import net.minecraft.world.item.armortrim.TrimPattern;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import online.kingdomkeys.kingdomkeys.ability.ModAbilities;
import online.kingdomkeys.kingdomkeys.item.*;
import online.remind.remind.KingdomKeysReMind;
import online.remind.remind.ability.ModAbilitiesRM;
import online.remind.remind.capabilities.GlobalDataRM;
import online.remind.remind.config.ModConfigs;
import online.remind.remind.entity.ModEntitiesRM;
import online.remind.remind.magic.ModMagicsRM;
import online.remind.remind.shotlock.ModShotlocksRM;

import java.util.function.Supplier;


public class ModItemsRM{
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.createItems(KingdomKeysReMind.MODID);

    public static int copperCoinValue = ModConfigs.copperCoinValue;
    public static int silverCoinValue = ModConfigs.silverCoinValue;
    public static int goldCoinValue = ModConfigs.goldCoinValue;

    public static final Supplier<Item>
            // Spell Orbs
        hasteSpell = ITEMS.register("haste_spell", () -> new RMMagicSpellItem(new Item.Properties(), ModMagicsRM.HASTE.location())),
        slowSpell = ITEMS.register("slow_spell", () -> new RMMagicSpellItem(new Item.Properties(), ModMagicsRM.SLOW.location())),
        holySpell = ITEMS.register("holy_spell", () -> new RMMagicSpellItem(new Item.Properties(), ModMagicsRM.HOLY.location())),
        ruinSpell = ITEMS.register("ruin_spell", () -> new RMMagicSpellItem(new Item.Properties(), ModMagicsRM.RUIN.location())),
        ultimaSpell = ITEMS.register("ultima_spell", () -> new RMMagicSpellItem(new Item.Properties(), ModMagicsRM.ULTIMA.location())),
        cometSpell = ITEMS.register("comet_spell", () -> new RMMagicSpellItem(new Item.Properties(), ModMagicsRM.COMET.location())),
        berserkSpell = ITEMS.register("berserk_spell",() -> new RMMagicSpellItem(new Item.Properties(),ModMagicsRM.BERSERK.location())),
        autoLifeSpell = ITEMS.register("autolife_spell", () -> new RMMagicSpellItem(new Item.Properties(), ModMagicsRM.AUTO_LIFE.location())),
        drainSpell = ITEMS.register("drain_spell", () -> new RMMagicSpellItem(new Item.Properties(), ModMagicsRM.DRAIN.location())),
        osmoseSpell = ITEMS.register("osmose_spell", () -> new RMMagicSpellItem(new Item.Properties(), ModMagicsRM.OSMOSE.location())),
        silenceSpell = ITEMS.register("silence_spell", () -> new RMMagicSpellItem(new Item.Properties(), ModMagicsRM.SILENCE.location())),
        esunaSpell = ITEMS.register("esuna_spell", () -> new RMMagicSpellItem(new Item.Properties(), ModMagicsRM.ESUNA.location())),
        dispelSpell = ITEMS.register("dispel_spell", () -> new RMMagicSpellItem(new Item.Properties(), ModMagicsRM.DISPEL.location())),
        faithSpell = ITEMS.register("faith_spell", () -> new RMMagicSpellItem(new Item.Properties(), ModMagicsRM.FAITH.location())),
        regenSpell = ITEMS.register("regen_spell", () -> new RMMagicSpellItem(new Item.Properties(), ModMagicsRM.REGEN.location())),
        stealSpell = ITEMS.register("steal_spell", () -> new RMMagicSpellItem(new Item.Properties(), ModMagicsRM.STEAL.location())),
        confuseSpell = ITEMS.register("confuse_spell", () -> new RMMagicSpellItem(new Item.Properties(), ModMagicsRM.CONFUSE.location())),
        flareSpell = ITEMS.register("flare_spell", () -> new RMMagicSpellItem(new Item.Properties(), ModMagicsRM.FLARE.location())),
        zettaflareSpell = ITEMS.register("zettaflare_spell", () -> new RMMagicSpellItem(new Item.Properties(), ModMagicsRM.ZETTAFLARE.location())),
        recallSpell = ITEMS.register("recall_spell", () -> new RMMagicSpellItem(new Item.Properties(), ModMagicsRM.RECALL.location())),

        hasteraSpell = ITEMS.register("haste1_spell", () -> new RMMagicSpellItem(new Item.Properties(), ModMagicsRM.HASTERA.location())),
        slowraSpell = ITEMS.register("slow1_spell", () -> new RMMagicSpellItem(new Item.Properties(), ModMagicsRM.SLOWRA.location())),
        holyraSpell = ITEMS.register("holy1_spell", () -> new RMMagicSpellItem(new Item.Properties(), ModMagicsRM.HOLYRA.location())),
        ruinraSpell = ITEMS.register("ruin1_spell", () -> new RMMagicSpellItem(new Item.Properties(), ModMagicsRM.RUINRA.location())),
        meteorSpell = ITEMS.register("comet1_spell", () -> new RMMagicSpellItem(new Item.Properties(), ModMagicsRM.METEOR.location())),
        berserkraSpell = ITEMS.register("berserk1_spell",() -> new RMMagicSpellItem(new Item.Properties(), ModMagicsRM.BERSERKRA.location())),
        drainraSpell = ITEMS.register("drain1_spell", () -> new RMMagicSpellItem(new Item.Properties(), ModMagicsRM.DRAINRA.location())),
        osmoseraSpell = ITEMS.register("osmose1_spell", () -> new RMMagicSpellItem(new Item.Properties(), ModMagicsRM.OSMOSERA.location())),
        silenceraSpell = ITEMS.register("silence1_spell", () -> new RMMagicSpellItem(new Item.Properties(), ModMagicsRM.SILENCERA.location())),
        groupEsunaSpell = ITEMS.register("esuna1_spell", () -> new RMMagicSpellItem(new Item.Properties(), ModMagicsRM.GROUP_ESUNA.location())),
        regenraSpell = ITEMS.register("regen1_spell", () -> new RMMagicSpellItem(new Item.Properties(), ModMagicsRM.REGENRA.location())),
        confuse1Spell = ITEMS.register("confuse1_spell", () -> new RMMagicSpellItem(new Item.Properties(), ModMagicsRM.CONFUSERA.location())),


        hastegaSpell = ITEMS.register("haste2_spell", () -> new RMMagicSpellItem(new Item.Properties(), ModMagicsRM.HASTEGA.location())),
        slowgaSpell = ITEMS.register("slow2_spell", () -> new RMMagicSpellItem(new Item.Properties(), ModMagicsRM.SLOWGA.location())),
        holygaSpell = ITEMS.register("holy2_spell", () -> new RMMagicSpellItem(new Item.Properties(), ModMagicsRM.HOLYGA.location())),
        ruingaSpell = ITEMS.register("ruin2_spell", () -> new RMMagicSpellItem(new Item.Properties(), ModMagicsRM.RUINGA.location())),
        berserkgaSpell = ITEMS.register("berserk2_spell",() -> new RMMagicSpellItem(new Item.Properties(), ModMagicsRM.BERSERKGA.location())),
        draingaSpell = ITEMS.register("drain2_spell", () -> new RMMagicSpellItem(new Item.Properties(), ModMagicsRM.DRAINGA.location())),
        osmosegaSpell = ITEMS.register("osmose2_spell", () -> new RMMagicSpellItem(new Item.Properties(), ModMagicsRM.OSMOSEGA.location())),
        silencegaSpell = ITEMS.register("silence2_spell", () -> new RMMagicSpellItem(new Item.Properties(), ModMagicsRM.SILENCEGA.location())),
        regengaSpell = ITEMS.register("regen2_spell", () -> new RMMagicSpellItem(new Item.Properties(), ModMagicsRM.REGENGA.location())),
        confuse2Spell = ITEMS.register("confuse2_spell", () -> new RMMagicSpellItem(new Item.Properties(), ModMagicsRM.CONFUSEGA.location())),


        // Attack Orbs
        quickBlitzAttack = ITEMS.register("quick_blitz_attack", () -> new RMMagicSpellItem(new Item.Properties(), ModMagicsRM.QUICK_BLITZ.location())),
        slidingDashAttack = ITEMS.register("sliding_dash_attack", () -> new RMMagicSpellItem(new Item.Properties(), ModMagicsRM.SLIDING_DASH.location())),
        fireSurgeAttack = ITEMS.register("fire_surge_attack", () -> new RMMagicSpellItem(new Item.Properties(), ModMagicsRM.FIRE_SURGE.location())),
        thunderSurgeAttack = ITEMS.register("thunder_surge_attack", () -> new RMMagicSpellItem(new Item.Properties(), ModMagicsRM.THUNDER_SURGE.location())),
        blizzardSurgeAttack = ITEMS.register("blizzard_surge_attack", () -> new RMMagicSpellItem(new Item.Properties(), ModMagicsRM.BLIZZARD_SURGE.location())),
        waterSurgeAttack = ITEMS.register("water_surge_attack", () -> new RMMagicSpellItem(new Item.Properties(), ModMagicsRM.WATER_SURGE.location())),
        aeroSurgeAttack = ITEMS.register("aero_surge_attack", () -> new RMMagicSpellItem(new Item.Properties(), ModMagicsRM.AERO_SURGE.location())),
        lightSurgeAttack = ITEMS.register("light_surge_attack", () -> new RMMagicSpellItem(new Item.Properties(), ModMagicsRM.LIGHT_SURGE.location())),
        darkSurgeAttack = ITEMS.register("dark_surge_attack", () -> new RMMagicSpellItem(new Item.Properties(), ModMagicsRM.DARK_SURGE.location())),
        zantetsukenAttack = ITEMS.register("zantetsuken_attack", () -> new RMMagicSpellItem(new Item.Properties(), ModMagicsRM.ZANTETSUKEN.location())),
        arsArcanumAttack = ITEMS.register("ars_arcanum_attack", () -> new RMMagicSpellItem(new Item.Properties(), ModMagicsRM.ARS_ARCANUM.location())),
        aerialSlamAttack = ITEMS.register("aerial_slam_attack", () -> new RMMagicSpellItem(new Item.Properties(), ModMagicsRM.AERIAL_SLAM.location())),

        fireStrikeAttack = ITEMS.register("fire_strike_attack", () -> new RMMagicSpellItem(new Item.Properties(), ModMagicsRM.FIRE_STRIKE.location())),
        blizzardStrikeAttack = ITEMS.register("blizzard_strike_attack", () -> new RMMagicSpellItem(new Item.Properties(), ModMagicsRM.BLIZZARD_STRIKE.location())),
        thunderStrikeAttack = ITEMS.register("thunder_strike_attack", () -> new RMMagicSpellItem(new Item.Properties(), ModMagicsRM.THUNDER_STRIKE.location())),
        waterStrikeAttack = ITEMS.register("water_strike_attack", () -> new RMMagicSpellItem(new Item.Properties(), ModMagicsRM.WATER_STRIKE.location())),
        aeroStrikeAttack = ITEMS.register("aero_strike_attack", () -> new RMMagicSpellItem(new Item.Properties(), ModMagicsRM.AERO_STRIKE.location())),
        lightStrikeAttack = ITEMS.register("light_strike_attack", () -> new RMMagicSpellItem(new Item.Properties(), ModMagicsRM.LIGHT_STRIKE.location())),
        darkStrikeAttack = ITEMS.register("dark_strike_attack", () -> new RMMagicSpellItem(new Item.Properties(), ModMagicsRM.DARK_STRIKE.location())),
        bindingStrikeAttack = ITEMS.register("binding_strike_attack", () -> new RMMagicSpellItem(new Item.Properties(), ModMagicsRM.BINDING_STRIKE.location())),
        confusionStrikeAttack = ITEMS.register("confusion_strike_attack", () -> new RMMagicSpellItem(new Item.Properties(), ModMagicsRM.CONFUSION_STRIKE.location())),
        poisonStrikeAttack = ITEMS.register("poison_strike_attack", () -> new RMMagicSpellItem(new Item.Properties(), ModMagicsRM.POISON_STRIKE.location())),
        blitzAttack = ITEMS.register("blitz_attack", () -> new RMMagicSpellItem(new Item.Properties(), ModMagicsRM.BLITZ.location())),
        darkHazeAttack = ITEMS.register("dark_haze_attack", () -> new RMMagicSpellItem(new Item.Properties(), ModMagicsRM.DARK_HAZE.location())),
        sonicBladeAttack = ITEMS.register("sonic_blade_attack", () -> new RMMagicSpellItem(new Item.Properties(), ModMagicsRM.SONIC_BLADE.location())),
        chaosBladeAttack = ITEMS.register("chaos_blade_attack", () -> new RMMagicSpellItem(new Item.Properties(), ModMagicsRM.CHAOS_BLADE.location())),
        slotEdgeAttack = ITEMS.register("slot_edge_attack", () -> new RMMagicSpellItem(new Item.Properties(), ModMagicsRM.SLOT_EDGE.location())),

        fireSurge1Attack = ITEMS.register("fire_surge1_attack", () -> new RMMagicSpellItem(new Item.Properties(), ModMagicsRM.FIRA_SURGE.location())),
        thunderSurge1Attack = ITEMS.register("thunder_surge1_attack", () -> new RMMagicSpellItem(new Item.Properties(), ModMagicsRM.THUNDARA_SURGE.location())),
        blizzardSurge1Attack = ITEMS.register("blizzard_surge1_attack", () -> new RMMagicSpellItem(new Item.Properties(), ModMagicsRM.BLIZZARA_SURGE.location())),
        waterSurge1Attack = ITEMS.register("water_surge1_attack", () -> new RMMagicSpellItem(new Item.Properties(), ModMagicsRM.WATERRA_SURGE.location())),
        aeroSurge1Attack = ITEMS.register("aero_surge1_attack", () -> new RMMagicSpellItem(new Item.Properties(), ModMagicsRM.AERORA_SURGE.location())),
        lightSurge1Attack = ITEMS.register("light_surge1_attack", () -> new RMMagicSpellItem(new Item.Properties(), ModMagicsRM.LIGHTRA_SURGE.location())),
        darkSurge1Attack = ITEMS.register("dark_surge1_attack", () -> new RMMagicSpellItem(new Item.Properties(), ModMagicsRM.DARKRA_SURGE.location())),

        fireSurge2Attack = ITEMS.register("fire_surge2_attack", () -> new RMMagicSpellItem(new Item.Properties(), ModMagicsRM.FIRAGA_SURGE.location())),
        thunderSurge2Attack = ITEMS.register("thunder_surge2_attack", () -> new RMMagicSpellItem(new Item.Properties(), ModMagicsRM.THUNDAGA_SURGE.location())),
        blizzardSurge2Attack = ITEMS.register("blizzard_surge2_attack", () -> new RMMagicSpellItem(new Item.Properties(), ModMagicsRM.BLIZZAGA_SURGE.location())),
        waterSurge2Attack = ITEMS.register("water_surge2_attack", () -> new RMMagicSpellItem(new Item.Properties(), ModMagicsRM.WATERGA_SURGE.location())),
        aeroSurge2Attack = ITEMS.register("aero_surge2_attack", () -> new RMMagicSpellItem(new Item.Properties(), ModMagicsRM.AEROGA_SURGE.location())),
        lightSurge2Attack = ITEMS.register("light_surge2_attack", () -> new RMMagicSpellItem(new Item.Properties(), ModMagicsRM.LIGHTGA_SURGE.location())),
        darkSurge2Attack = ITEMS.register("dark_surge2_attack", () -> new RMMagicSpellItem(new Item.Properties(), ModMagicsRM.DARKGA_SURGE.location())),

    // Style/Form Exclusive
        braverAttack = ITEMS.register("braver_attack", () -> new RMMagicSpellItem(new Item.Properties(), ModMagicsRM.BRAVER.location())),
        crossSlashAttack = ITEMS.register("cross_slash_attack", () -> new RMMagicSpellItem(new Item.Properties(), ModMagicsRM.CROSS_SLASH.location())),
        swiftStrikeAttack = ITEMS.register("swift_strike_attack", () -> new RMMagicSpellItem(new Item.Properties(), ModMagicsRM.SWIFT_STRIKE.location())),
        deathSpell = ITEMS.register("death_lv_spell", () -> new RMMagicSpellItem(new Item.Properties(), ModMagicsRM.DEATH.location())),

        // Shotlock Orbs
        darkDivide = ITEMS.register("dark_divide_shotlock",() -> new ShotlockItem(new Item.Properties(), ModShotlocksRM.DARK_DIVIDE.location())),
        omnislash = ITEMS.register("omnislash_shotlock",() -> new ShotlockItem(new Item.Properties(), ModShotlocksRM.OMNISLASH.location())),

    // Dream Eater Charms
        meowWowCharm = ITEMS.register("meow_wow_charm",() -> new DreamEaterCharmItem(new Item.Properties().stacksTo(1), GlobalDataRM.DREAM_EATER_MEOW_WOW, "Meow Wow")),
        komoryBatCharm = ITEMS.register("komory_bat_charm",() -> new DreamEaterCharmItem(new Item.Properties().stacksTo(1), GlobalDataRM.DREAM_EATER_KOMORY_BAT, "Komory Bat")),
        cactuarCharm = ITEMS.register("cactuar_charm",() -> new DreamEaterCharmItem(new Item.Properties().stacksTo(1), GlobalDataRM.DREAM_EATER_CACTUAR, "Cactuar")),
        tonberryCharm = ITEMS.register("tonberry_charm",() -> new DreamEaterCharmItem(new Item.Properties().stacksTo(1), GlobalDataRM.DREAM_EATER_TONBERRY, "Tonberry")),


    // Ability Orb?
        abilityOrb = ITEMS.register("ability_orb", () -> new AbilityOrbItem(new Item.Properties(), "")),

        // Org Panel System
        slotReleaser = ITEMS.register("slot_releaser", () -> new SlotReleaserItem(new Item.Properties().stacksTo(64))),


    // Keyblades
        xephiroKeyblade = ITEMS.register("xephiro_keyblade", () -> new KeybladeItem(new Item.Properties())),
        pureblood = ITEMS.register("pureblood", () -> new KeybladeItem(new Item.Properties())),
        elemental_crescendo = ITEMS.register("elemental_crescendo", () -> new KeybladeItem(new Item.Properties())),
        gazing_omen = ITEMS.register("gazing_omen", () -> new KeybladeItem(new Item.Properties())),
        crystalsLight = ITEMS.register("crystals_light", () -> new KeybladeItem(new Item.Properties())),
        blitzersDream = ITEMS.register("blitzers_dream", () -> new KeybladeItem(new Item.Properties())),
        legendsFang = ITEMS.register("legends_fang", () -> new KeybladeItem(new Item.Properties())),
        fierceDeityKey = ITEMS.register("fierce_deity_key", () -> new KeybladeItem(new Item.Properties())),
        lyric2025Tournament = ITEMS.register("lyric_2025_tournament", () -> new KeybladeItem(new Item.Properties())),
        voidlight = ITEMS.register("voidlight", () -> new KeybladeItem(new Item.Properties())),
        fortuna = ITEMS.register("fortuna", () -> new KeybladeItem(new Item.Properties())),
        nebulaRecords = ITEMS.register("nebula_records", () -> new KeybladeItem(new Item.Properties())),
        entropysRequiem = ITEMS.register("entropys_requiem", () -> new KeybladeItem(new Item.Properties())),
        maddeningCorruption = ITEMS.register("maddening_corruption", () -> new KeybladeItem(new Item.Properties())),

        kaliKeyblade = ITEMS.register("kali_keyblade", () -> new KeybladeItem(new Item.Properties())),
        roseKeyblade = ITEMS.register("rose_keyblade", () -> new KeybladeItem(new Item.Properties())),

        unionUltimaLight = ITEMS.register("union_ultima_l", () -> new KeybladeItem(new Item.Properties())),
        unionUltimaDark = ITEMS.register("union_ultima_d", () -> new KeybladeItem(new Item.Properties())),


        // Keychains
        xephiroKeybladeChain = ITEMS.register("xephiro_keyblade_chain", () -> new KeychainItem()),
        purebloodChain = ITEMS.register("pureblood_chain", () -> new KeychainItem()),
        elementalCrescendoChain = ITEMS.register("elemental_crescendo_chain", () -> new KeychainItem()),
        gazingOmenChain = ITEMS.register("gazing_omen_chain", () -> new KeychainItem()),
        crystalsLightChain = ITEMS.register("crystals_light_chain", () -> new KeychainItem()),
        blitzersDreamChain = ITEMS.register("blitzers_dream_chain", () -> new KeychainItem()),
        legendsFangChain = ITEMS.register("legends_fang_chain", () -> new KeychainItem()),
        fierceDeityKeyChain = ITEMS.register("fierce_deity_key_chain", () -> new KeychainItem()),
        lyric2025TournamentChain = ITEMS.register("lyric_2025_tournament_chain", () -> new KeychainItem()),
        voidlightChain = ITEMS.register("voidlight_chain", () -> new KeychainItem()),
        fortunaChain = ITEMS.register("fortuna_chain", () -> new KeychainItem()),
        nebulaRecordsChain = ITEMS.register("nebula_records_chain", () -> new KeychainItem()),
        entropysRequiemChain = ITEMS.register("entropys_requiem_chain", () -> new KeychainItem()),
        maddeningCorruptionChain = ITEMS.register("maddening_corruption_chain", () -> new KeychainItem()),
        kaliKeybladeChain = ITEMS.register("kali_keyblade_chain", () -> new KeychainItem()),
        roseKeybladeChain = ITEMS.register("rose_keyblade_chain", () -> new KeychainItem()),
        unionUltimaLightChain = ITEMS.register("union_ultima_l_chain", () -> new KeychainItem()),
        unionUltimaDarkChain = ITEMS.register("union_ultima_d_chain", () -> new KeychainItem()),


        // And this is where I'd put my Org Weapons... IF I HAD ONE!



        // KK Armors
        aquaChaplet = ITEMS.register("aqua_chaplet", () -> new KKArmorItem(new Item.Properties().stacksTo(1),1, ImmutableMap.of(KKResistanceType.water,50))),
        herosGlove = ITEMS.register("heros_glove", () -> new KKArmorItem(new Item.Properties().stacksTo(1),4, ImmutableMap.of(KKResistanceType.fire,20,KKResistanceType.ice,20,KKResistanceType.darkness,20))),
        herosBelt = ITEMS.register("heros_belt", () -> new KKArmorItem(new Item.Properties().stacksTo(1),3, ImmutableMap.of(KKResistanceType.lightning,20,KKResistanceType.ice,20,KKResistanceType.darkness,20))),
        masterBelt = ITEMS.register("master_belt", () -> new KKArmorItem(new Item.Properties().stacksTo(1),7, ImmutableMap.of(KKResistanceType.darkness,20, KKResistanceType.light,20))),
        ultima_ribbon = ITEMS.register("ultima_ribbon", () -> new KKArmorItem(new Item.Properties().stacksTo(1),5, ImmutableMap.of(KKResistanceType.fire,50,KKResistanceType.ice,50,KKResistanceType.lightning,50,KKResistanceType.darkness,50, KKResistanceType.light,50, KKResistanceType.water,50, KKResistanceType.air, 50))),

        // KK Accessories
        luckOfTheDraw = ITEMS.register("luck_of_the_draw", () -> new KKAccessoryItem(new Item.Properties().stacksTo(1), 0,0,0,new ResourceLocation[] {ModAbilities.LUCKY_STRIKE.location(), ModAbilities.TREASURE_MAGNET.location()})),
        lightHeart = ITEMS.register("light_heart", () -> new KKAccessoryItem(new Item.Properties().stacksTo(1), 5,0,0,new ResourceLocation[] {ModAbilitiesRM.WAY_TO_LIGHT.location()})),
        darkHeart = ITEMS.register("dark_heart", () -> new KKAccessoryItem(new Item.Properties().stacksTo(1), 5,0,0,new ResourceLocation[] {ModAbilitiesRM.DARK_POWER.location()})),
        ragingHeart = ITEMS.register("raging_heart", () -> new KKAccessoryItem(new Item.Properties().stacksTo(1), 5,0,0,new ResourceLocation[] {ModAbilitiesRM.RAGE_AWAKENED.location()})),
        celestriad = ITEMS.register("celestriad", () -> new KKAccessoryItem(new Item.Properties().stacksTo(1), 5,2,3,new ResourceLocation[] {ModAbilities.FIRE_BOOST.location(),ModAbilities.BLIZZARD_BOOST.location(),ModAbilities.THUNDER_BOOST.location()})),
        forestClasp = ITEMS.register("forest_clasp", () -> new KKAccessoryItem(new Item.Properties().stacksTo(1),8,2,3,new ResourceLocation[] {ModAbilitiesRM.HP_WALKER.location()})),
        laughterPin = ITEMS.register("laughter_pin", () -> new KKAccessoryItem(new Item.Properties().stacksTo(1),6,3,2,new ResourceLocation[] {ModAbilitiesRM.MP_WALKER.location()})),
        crystalRegalia = ITEMS.register("crystal_regalia", () -> new KKAccessoryItem(new Item.Properties().stacksTo(1),16,5,5,new ResourceLocation[] {ModAbilities.MP_HASTEGA.location()})),
        crystalRegaliaPlus = ITEMS.register("crystal_regalia_plus", () -> new KKAccessoryItem(new Item.Properties().stacksTo(1),25,6,6,new ResourceLocation[] {ModAbilities.MP_HASTEGA.location()})),
        flanniversaryBadge = ITEMS.register("flanniversary_badge", () -> new KKAccessoryItem(new Item.Properties().stacksTo(1),0,4,4,new ResourceLocation[] {ModAbilities.MP_HASTERA.location(),ModAbilities.MP_THRIFT.location()})),
        mickeyClasp = ITEMS.register("mickey_clasp", () -> new KKAccessoryItem(new Item.Properties().stacksTo(1),0,3,5,new ResourceLocation[] {ModAbilities.MP_HASTEGA.location(),ModAbilities.ENDLESS_MAGIC.location()})),
        breakthrough = ITEMS.register("breakthrough", () -> new KKAccessoryItem(new Item.Properties().stacksTo(1),15,7,0, new ResourceLocation[] {ModAbilitiesRM.CURE_CONVERTER.location()})),
        hasteBracer = ITEMS.register("haste_bracer", () -> new KKAccessoryItem(new Item.Properties().stacksTo(1),5,3,0, new ResourceLocation[] {ModAbilitiesRM.ATTACK_HASTE.location()})),
        sacrificeBracer = ITEMS.register("sacrifice_bracer", () -> new KKAccessoryItem(new Item.Properties().stacksTo(1),5,5,5, new ResourceLocation[] {ModAbilitiesRM.VEHEMENCE.location()})),
        darkRing = ITEMS.register("dark_ring", () -> new KKAccessoryItem(new Item.Properties().stacksTo(1),5,5,5, new ResourceLocation[] {ModAbilitiesRM.DARKNESS_BOOST.location()})),
        lightRing = ITEMS.register("light_ring", () -> new KKAccessoryItem(new Item.Properties().stacksTo(1),5,5,5, new ResourceLocation[] {ModAbilitiesRM.LIGHT_BOOST.location()})),

        expRing = ITEMS.register("exp_ring", () -> new KKAccessoryItem(new Item.Properties().stacksTo(1),5,1,1, new ResourceLocation[] {ModAbilitiesRM.EXP_WALKER.location(), ModAbilities.EXPERIENCE_BOOST.location(), ModAbilitiesRM.XP_CONVERTER.location()})),
        focusSash = ITEMS.register("focus_sash", () -> new KKAccessoryItem(new Item.Properties().stacksTo(1),3,0,3, new ResourceLocation[] {ModAbilitiesRM.FOCUS_WALKER.location()})),
        heartLocket = ITEMS.register("heart_locket", () -> new KKAccessoryItem(new Item.Properties().stacksTo(1),5,1,1, new ResourceLocation[] {ModAbilitiesRM.HEART_WALKER.location()})),
        friendBinder = ITEMS.register("friendbinder", () -> new KKAccessoryItem(new Item.Properties().stacksTo(1),10,0,0, new ResourceLocation[] {ModAbilitiesRM.FRIEND_POWER.location()})),
        nothingnessBinder = ITEMS.register("nothingness_binder", () -> new KKAccessoryItem(new Item.Properties().stacksTo(1),10,0,0, new ResourceLocation[] {ModAbilitiesRM.HEARTS_POWER.location()})),
        silverArmlet = ITEMS.register("silver_armlet", () -> new KKAccessoryItem(new Item.Properties().stacksTo(1),10,0,0, new ResourceLocation[] {ModAbilitiesRM.SPELLBLADE.location(),ModAbilitiesRM.MP_BOOST.location()})),
        daredevil = ITEMS.register("daredevil", () -> new KKAccessoryItem(new Item.Properties().stacksTo(1),0,0,0, new ResourceLocation[] {ModAbilitiesRM.ONE_HP.location(), ModAbilities.EXPERIENCE_BOOST.location(), ModAbilities.EXPERIENCE_BOOST.location()})),
        ribbon_ff7 = ITEMS.register("ribbon_ff7", () -> new KKAccessoryItem(new Item.Properties().stacksTo(1),10,0,0, new ResourceLocation[] {ModAbilitiesRM.RIBBON.location()})),
        furyRing = ITEMS.register("fury_ring", () -> new KKAccessoryItem(new Item.Properties().stacksTo(1),0,2,0, new ResourceLocation[] {ModAbilities.BERSERK_CHARGE.location(), ModAbilitiesRM.MP_SLOW.location(), ModAbilitiesRM.MP_SLOWRA.location(), ModAbilitiesRM.MP_SLOWGA.location()})),
        braveWarrior = ITEMS.register("brave_warrior", () -> new KKAccessoryItem(new Item.Properties().stacksTo(1), 2,1,0,new ResourceLocation[] {ModAbilitiesRM.HP_BOOST.location(),ModAbilitiesRM.HP_BOOST.location()})),
        omegaArts = ITEMS.register("omega_arts", () -> new KKAccessoryItem(new Item.Properties().stacksTo(1), 3,4,0,new ResourceLocation[] {ModAbilitiesRM.HP_BOOST.location(), ModAbilitiesRM.HP_BOOST.location()})),
        rayOfLight = ITEMS.register("ray_of_light", () -> new KKAccessoryItem(new Item.Properties().stacksTo(1), 3,0,4,new ResourceLocation[] {ModAbilitiesRM.HP_BOOST.location(), ModAbilitiesRM.MP_BOOST.location()})),
        alluringSkull = ITEMS.register("alluring_skull", () -> new KKAccessoryItem(new Item.Properties().stacksTo(1), 3,0,0,new ResourceLocation[] {ModAbilities.ENCOUNTER_PLUS.location(), ModAbilities.ENCOUNTER_PLUS.location()})),
        whiteFang = ITEMS.register("white_fang", () -> new KKAccessoryItem(new Item.Properties().stacksTo(1), 1,1,0,new ResourceLocation[] {ModAbilities.CRITICAL_BOOST.location()})),



        // Coins
        copperCoin = ITEMS.register("copper_coin", () -> new RMCoinItem(new Item.Properties().stacksTo(64), () -> ModConfigs.copperCoinValue, "munny")),
        silverCoin = ITEMS.register("silver_coin", () -> new RMCoinItem(new Item.Properties().stacksTo(64), () -> ModConfigs.silverCoinValue, "munny")),
        goldCoin = ITEMS.register("gold_coin", () -> new RMCoinItem(new Item.Properties().stacksTo(64), () -> ModConfigs.goldCoinValue, "munny")),
        emeraldCoin = ITEMS.register("emerald_coin", () -> new RMCoinItem(new Item.Properties().stacksTo(64), () -> ModConfigs.emeraldCoinValue, "munny")),
        diamondCoin = ITEMS.register("diamond_coin", () -> new RMCoinItem(new Item.Properties().stacksTo(64), () -> ModConfigs.diamondCoinValue, "munny")),
        netheriteCoin = ITEMS.register("netherite_coin", () -> new RMCoinItem(new Item.Properties().stacksTo(64), () -> ModConfigs.netheriteCoinValue, "munny")),
        amethystCoin = ITEMS.register("amethyst_coin", () -> new RMCoinItem(new Item.Properties().stacksTo(64), () -> ModConfigs.amethystCoinValue, "munny")),
        heartCoin = ITEMS.register("heart_coin", () -> new RMCoinItem(new Item.Properties().stacksTo(64), () -> ModConfigs.heartCoinValue, "hearts")),
        luxCoin = ITEMS.register("lux_coin", () -> new RMCoinItem(new Item.Properties().stacksTo(64), () -> ModConfigs.luxCoinValue, "lux")),

        // Dream Eater Gifts
        chefsKnife = ITEMS.register("chefs_knife", () -> new ChefsKnifeItem(new Item.Properties().stacksTo(64))),
        cactuarNeedle = ITEMS.register("cactuar_needle", () -> new CactuarNeedleItem(new Item.Properties().stacksTo(64)));


        // Mob Eggs







        // Music Discs

        // Armor Trims



    public static final ResourceKey<TrimPattern> HEARTLESS_TRIM_PATTERN =
            ResourceKey.create(
                    Registries.TRIM_PATTERN,
                    ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "heartless")
            );

    public static final DeferredHolder<Item, SmithingTemplateItem> HEARTLESS_ARMOR_TRIM_SMITHING_TEMPLATE =
            ITEMS.register(
                    "heartless_armor_trim_smithing_template",
                    () -> SmithingTemplateItem.createArmorTrimTemplate(HEARTLESS_TRIM_PATTERN)
            );

    public static final ResourceKey<TrimPattern> NOBODY_TRIM_PATTERN =
            ResourceKey.create(
                    Registries.TRIM_PATTERN,
                    ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "nobody")
            );

    public static final DeferredHolder<Item, SmithingTemplateItem> NOBODY_ARMOR_TRIM_SMITHING_TEMPLATE =
            ITEMS.register(
                    "nobody_armor_trim_smithing_template",
                    () -> SmithingTemplateItem.createArmorTrimTemplate(NOBODY_TRIM_PATTERN)
            );

    public static void register(IEventBus eventBus){
        ITEMS.register(eventBus);
    }
}
