package online.remind.remind.magic;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.DeferredRegister;
import online.kingdomkeys.kingdomkeys.KingdomKeys;
import online.kingdomkeys.kingdomkeys.lib.KKSupplier;
import online.kingdomkeys.kingdomkeys.magic.Magic;
import online.remind.remind.KingdomKeysReMind;
import online.remind.remind.lib.StringsRM;
import online.remind.remind.magic.attacks.*;

import java.util.function.Supplier;

public class ModMagicsRM {

	//The Command
	public static DeferredRegister<Magic> MAGIC = DeferredRegister.create(ResourceLocation.fromNamespaceAndPath(KingdomKeys.MODID, "magics"), KingdomKeysReMind.MODID);

	//Normal Spells
	public static final KKSupplier<Magic>
		HASTE = register("magic_haste", () -> new magicHaste(ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "magic_haste"), true, 0, null)),
		HASTERA = register("magic_hastera", () -> new magicHaste(ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "magic_hastera"), true, 1, null)),
		HASTEGA = register("magic_hastega", () -> new magicHaste(ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "magic_hastega"), true, 2, null)),

		// Slow
		SLOW = register("magic_slow", () -> new magicSlow(ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "magic_slow"), false, 0, null)),
		SLOWRA = register("magic_slowra", () -> new magicSlow(ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "magic_slowra"), false, 1, null)),
		SLOWGA = register("magic_slowga", () -> new magicSlow(ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "magic_slowga"), false, 2, null)),

		// Holy
		HOLY = register("magic_holy", () -> new magicHoly(ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "magic_holy"), false, 0, null)),
		HOLYRA = register("magic_holyra", () -> new magicHoly(ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "magic_holyra"), false, 1, null)),
		HOLYGA = register("magic_holyga", () -> new magicHoly(ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "magic_holyga"), false, 2, null)),

		// Ruin
		RUIN = register("magic_ruin", () -> new magicRuin(ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "magic_ruin"), false, 0, null)),
		RUINRA = register("magic_ruinra", () -> new magicRuin(ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "magic_ruinra"), false, 1, null)),
		RUINGA = register("magic_ruinga", () -> new magicRuin(ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "magic_ruinga"), false, 2, null)),

		// Balloon
		BALLOON = register("magic_balloon", () -> new magicPorted(ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "magic_balloon"), false, 0, null)),
		BALLOONRA = register("magic_balloonra", () -> new magicPorted(ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "magic_balloonra"), false, 1, null)),
		BALLOONGA = register("magic_balloonga", () -> new magicPorted(ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "magic_balloonga"), false, 2, null)),

		ULTIMA = register("magic_ultima", () -> new magicUltima(ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "magic_ultima"), false, 0, null)),

		FLARE = register("magic_flare", () -> new magicFlare(ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "magic_flare"), false, 0, null)),
		ZETTAFLARE = register("magic_zettaflare", () -> new magicZettaflare(ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "magic_zettaflare"), false, 0, null)),

		RECALL = register("magic_recall", () -> new magicRecall(ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "magic_recall"), false, 0, null)),

		COMET = register("magic_comet", () -> new magicComet(ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "magic_comet"), false, 0, null)),
		METEOR = register("magic_meteor", () -> new magicComet(ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "magic_meteor"), false, 1, null)),

		BERSERK = register("magic_berserk", () -> new magicBerserk(ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "magic_berserk"), true, 0, null)),
		BERSERKRA = register("magic_berserkra", () -> new magicBerserk(ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "magic_berserkra"), true, 1, null)),
		BERSERKGA = register("magic_berserkga", () -> new magicBerserk(ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "magic_berserkga"), true, 2, null)),

		AUTO_LIFE = register("magic_auto-life", () -> new magicAutoLife(ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "magic_auto-life"), true, 0, null)),

		OSMOSE = register("magic_osmose", () -> new magicOsmose(ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "magic_osmose"), false, 0, null)),
		OSMOSERA = register("magic_osmosera", () -> new magicOsmose(ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "magic_osmosera"), false, 1, null)),
		OSMOSEGA = register("magic_osmosega", () -> new magicOsmose(ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "magic_osmosega"), false, 2, null)),

		DRAIN = register("magic_drain", () -> new magicDrain(ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "magic_drain"), false, 0, null)),
		DRAINRA = register("magic_drainra", () -> new magicDrain(ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "magic_drainra"), false, 1, null)),
		DRAINGA = register("magic_drainga", () -> new magicDrain(ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "magic_drainga"), false, 2, null)),

		SILENCE = register("magic_silence", () -> new magicSilence(ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "magic_silence"), false, 0, null)),
		SILENCERA = register("magic_silencera", () -> new magicSilence(ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "magic_silencera"), false, 1, null)),
		SILENCEGA = register("magic_silencega", () -> new magicSilence(ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "magic_silencega"), false, 2, null)),

		WARP = register("magic_warp", () -> new magicPorted(ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "magic_warp"), false, 0, null)),

		ESUNA = register("magic_esuna", () -> new magicPorted(ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "magic_esuna"), true, 0, null)),
		GROUP_ESUNA = register("magic_group_esuna", () -> new magicPorted(ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "magic_group_esuna"), true, 1, null)),

		DISPEL = register("magic_dispel", () -> new magicDispel(ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "magic_dispel"), false, 0, null)),

		REGEN = register("magic_regen", () -> new magicRegen(ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "magic_regen"), true, 0, null)),
		REGENRA = register("magic_regenra", () -> new magicRegen(ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "magic_regenra"), true, 1, null)),
		REGENGA = register("magic_regenga", () -> new magicRegen(ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "magic_regenga"), true, 2, null)),

		FAITH = register("magic_faith", () -> new magicFaith(ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "magic_faith"), 0, null)),

		DEATH = register("magic_death", () -> new magicDeath(ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "magic_death"), false, 0, null)),

		// Confuse
		CONFUSE = register("magic_confuse", () -> new magicConfuse(ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "magic_confuse"), false, 0, null)),
		CONFUSERA = register("magic_confusera", () -> new magicConfuse(ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "magic_confusera"), false, 1, null)),
		CONFUSEGA = register("magic_confusega", () -> new magicConfuse(ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "magic_confusega"), false, 2, null)),

		STEAL = register("magic_steal", () -> new magicSteal(ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "magic_steal"), false, 0, null)),

		// TODO: Add BBS/DDD Attack Commands and FF Related Commands

		QUICK_BLITZ = register("attack_quick_blitz", () -> new attackQuickBlitz(ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "attack_quick_blitz"), false, 0, null)),

		SLIDING_DASH = register("attack_sliding_dash", () -> new attackSlidingDash(ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "attack_sliding_dash"), false, 0, null)),

	FIRE_STRIKE = register("attack_fire_strike", () -> new attackFireStrike(ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "attack_fire_strike"), false, 0, null)),
			BLIZZARD_STRIKE = register("attack_blizzard_strike", () -> new attackBlizzardStrike(ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "attack_blizzard_strike"), false, 0, null)),
			THUNDER_STRIKE = register("attack_thunder_strike", () -> new attackThunderStrike(ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "attack_thunder_strike"), false, 0, null)),
			WATER_STRIKE = register("attack_water_strike", () -> new attackWaterStrike(ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "attack_water_strike"), false, 0, null)),
			AERO_STRIKE = register("attack_aero_strike", () -> new attackAeroStrike(ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "attack_aero_strike"), false, 0, null)),
			LIGHT_STRIKE = register("attack_light_strike", () -> new attackLightStrike(ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "attack_light_strike"), false, 0, null)),
			DARK_STRIKE = register("attack_dark_strike", () -> new attackDarkStrike(ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "attack_dark_strike"), false, 0, null)),
			BINDING_STRIKE = register("attack_binding_strike", () -> new attackBindingStrike(ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "attack_binding_strike"), false, 0, null)),
			CONFUSION_STRIKE = register("attack_confusion_strike", () -> new attackConfusionStrike(ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "attack_confusion_strike"), false, 0, null)),
			POISON_STRIKE = register("attack_poison_strike", () -> new attackPoisonStrike(ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "attack_poison_strike"), false, 0, null)),

		FIRE_SURGE = register("attack_fire_surge", () -> new attackFireSurge(ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "attack_fire_surge"), false, 0, null)),
		FIRA_SURGE = register("attack_fira_surge", () -> new attackFireSurge(ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "attack_fira_surge"), false, 1, null)),
		FIRAGA_SURGE = register("attack_firaga_surge", () -> new attackFireSurge(ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "attack_firaga_surge"), false, 2, null)),

		THUNDER_SURGE = register("attack_thunder_surge", () -> new attackThunderSurge(ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "attack_thunder_surge"), false, 0, null)),
		THUNDARA_SURGE = register("attack_thundara_surge", () -> new attackThunderSurge(ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "attack_thundara_surge"), false, 1, null)),
		THUNDAGA_SURGE = register("attack_thundaga_surge", () -> new attackThunderSurge(ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "attack_thundaga_surge"), false, 2, null)),

		BLIZZARD_SURGE = register("attack_blizzard_surge", () -> new attackBlizzardSurge(ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "attack_blizzard_surge"), false, 0, null)),
		BLIZZARA_SURGE = register("attack_blizzara_surge", () -> new attackBlizzardSurge(ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "attack_blizzara_surge"), false, 1, null)),
		BLIZZAGA_SURGE = register("attack_blizzaga_surge", () -> new attackBlizzardSurge(ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "attack_blizzaga_surge"), false, 2, null)),

		WATER_SURGE = register("attack_water_surge", () -> new attackWaterSurge(ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "attack_water_surge"), false, 0, null)),
		WATERRA_SURGE = register("attack_watera_surge", () -> new attackWaterSurge(ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "attack_watera_surge"), false, 1, null)),
		WATERGA_SURGE = register("attack_waterga_surge", () -> new attackWaterSurge(ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "attack_waterga_surge"), false, 2, null)),

		AERO_SURGE = register("attack_aero_surge", () -> new attackAeroSurge(ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "attack_aero_surge"), false, 0, null)),
		AERORA_SURGE = register("attack_aerora_surge", () -> new attackAeroSurge(ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "attack_aerora_surge"), false, 1, null)),
		AEROGA_SURGE = register("attack_aeroga_surge", () -> new attackAeroSurge(ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "attack_aeroga_surge"), false, 2, null)),

		LIGHT_SURGE = register("attack_light_surge", () -> new attackLightSurge(ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "attack_light_surge"), false, 0, null)),
		LIGHTRA_SURGE = register("attack_lightra_surge", () -> new attackLightSurge(ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "attack_lightra_surge"), false, 1, null)),
		LIGHTGA_SURGE = register("attack_lightga_surge", () -> new attackLightSurge(ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "attack_lightga_surge"), false, 2, null)),

		DARK_SURGE = register("attack_dark_surge", () -> new attackDarkSurge(ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "attack_dark_surge"), false, 0, null)),
		DARKRA_SURGE = register("attack_darkra_surge", () -> new attackDarkSurge(ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "attack_darkra_surge"), false, 1, null)),
		DARKGA_SURGE = register("attack_darkga_surge", () -> new attackDarkSurge(ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "attack_darkga_surge"), false, 2, null)),

		ZANTETSUKEN = register("attack_zantetsuken", () -> new attackZantetsuken(ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "attack_zantetsuken"), false, 0, null)),



		AERIAL_SLAM = register("attack_aerial_slam", () -> new attackAerialSlam(ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "attack_aerial_slam"), false, 0, null)),
		BLITZ = register("attack_blitz", () -> new attackBlitz(ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "attack_blitz"), false, 0, null)),
		DARK_HAZE = register("attack_dark_haze", () -> new attackDarkHaze(ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "attack_dark_haze"), false, 0, null)),
		SONIC_BLADE = register("attack_sonic_blade", () -> new attackSonicBlade(ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "attack_sonic_blade"), false, 0, null)),
		CHAOS_BLADE = register("attack_chaos_blade", () -> new attackChaosBlade(ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "attack_chaos_blade"), false, 0, null)),
		SLOT_EDGE = register("attack_slot_edge", () -> new attackSlotEdge(ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "attack_slot_edge"), false, 0, null)),
		ARS_ARCANUM = register("attack_ars_arcanum", () -> new attackArsArcanum(ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "attack_ars_arcanum"), false, 0, null)),

		// FF Attacks

		// Cloud
		BRAVER = register("attack_braver", () -> new attackBraver(ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "attack_braver"), false, 0, null)),
		CROSS_SLASH = register("attack_cross_slash", () -> new attackCrossSlash(ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "attack_cross_slash"), false, 0, null)),

	// Sephiroth
		SWIFT_STRIKE = register("attack_swift_strike", () -> new attackSwiftStrike(ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "attack_swift_strike"), false, 1, null)); // SEPHIROTH!

	// Add more magic later...

	private static KKSupplier<Magic> register(String name, Supplier<Magic> magicSupplier) {
		return new KKSupplier<>(ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, name), MAGIC.register(name, magicSupplier));
	}

}






