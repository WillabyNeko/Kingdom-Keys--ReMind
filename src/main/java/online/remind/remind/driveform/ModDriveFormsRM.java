package online.remind.remind.driveform;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.DeferredRegister;
import online.kingdomkeys.kingdomkeys.KingdomKeys;
import online.kingdomkeys.kingdomkeys.driveform.DriveForm;
import online.kingdomkeys.kingdomkeys.lib.KKSupplier;
import online.remind.remind.KingdomKeysReMind;
import online.remind.remind.lib.StringsRM;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

public class ModDriveFormsRM {
    public static DeferredRegister<DriveForm> DRIVE_FORMS = DeferredRegister.create(ResourceLocation.fromNamespaceAndPath(KingdomKeys.MODID, "driveforms"), KingdomKeysReMind.MODID);

    static int order = 10;

    public static Set<ResourceLocation> styles = new HashSet<>();

     public static final KKSupplier<DriveForm>

        // Forms list
             LIGHT = register(StringsRM.DFMA_Prefix + "light", () -> new DriveFormLight(ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, StringsRM.DFMA_Prefix + "light"), order++, ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "textures/models/armor/light.png"), false, true)),
             DARK = register(StringsRM.DFMA_Prefix + "dark", () -> new DriveFormDark(ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, StringsRM.DFMA_Prefix + "dark"), order++, ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "textures/models/armor/dark.png"), false, true)),
             RAGE = register(StringsRM.DFMA_Prefix + "rage", () -> new DriveFormRage(ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, StringsRM.DFMA_Prefix + "rage"), order++, ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "textures/models/armor/rage.png"), false, false)),
             TWILIGHT = register(StringsRM.DFMA_Prefix + "twilight", () -> new DriveFormTwilight(ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, StringsRM.DFMA_Prefix + "twilight"), order++, ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "textures/models/armor/twilight.png"), true, true)),

        // Formchanges/Style Changes
            FIRESTORM = register(StringsRM.DFMA_Prefix+"firestorm", () -> new StyleFireStorm(ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, StringsRM.DFMA_Prefix + "firestorm"), order++, ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "textures/models/armor/regen.png"), false, true)),
            DIAMOND_DUST = register(StringsRM.DFMA_Prefix+"diamond_dust", () -> new StyleDiamondDust(ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, StringsRM.DFMA_Prefix + "diamond_dust"), order++, ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "textures/models/armor/regen.png"), false, true)),
            THUNDER_BOLT = register(StringsRM.DFMA_Prefix+"thunder_bolt", () -> new StyleThunderBolt(ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, StringsRM.DFMA_Prefix + "thunder_bolt"), order++, ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "textures/models/armor/regen.png"), false, true)),

            FEVER_PITCH = register(StringsRM.DFMA_Prefix+"fever_pitch", () -> new StyleFeverPitch(ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, StringsRM.DFMA_Prefix + "fever_pitch"), order++, ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "textures/models/armor/regen.png"), false, true)),
            CRITICAL_IMPACT = register(StringsRM.DFMA_Prefix+"critical_impact", () -> new StyleCriticalImpact(ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, StringsRM.DFMA_Prefix + "critical_impact"), order++, ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "textures/models/armor/regen.png"), false, true)),
            SPELLWEAVER = register(StringsRM.DFMA_Prefix+"spellweaver", () -> new StyleSpellweaver(ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, StringsRM.DFMA_Prefix + "spellweaver"), order++, ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "textures/models/armor/regen.png"), false, true)),

            // Level 2 Styles
            DARK_IMPULSE = register(StringsRM.DFMA_Prefix+"dark_impulse", () -> new StyleDarkImpulse(ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, StringsRM.DFMA_Prefix + "dark_impulse"), order++, ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "textures/models/armor/regen.png"), false, true)),

            // Re:Mind Original Styles
            BLOOSTLUST = register(StringsRM.DFMA_Prefix+"bloodlust", () -> new StyleBloodlust(ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, StringsRM.DFMA_Prefix + "bloodlust"), order++, ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "textures/models/armor/regen.png"), false, true)),
            EXSOLDIER = register(StringsRM.DFMA_Prefix+"ex_soldier", () -> new StyleExSOLDIER(ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, StringsRM.DFMA_Prefix + "ex_soldier"), order++, ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "textures/models/armor/regen.png"), false, true)),

        // Commission Forms

            REGEN = register(StringsRM.DFMA_Prefix + "regen", () -> new DriveFormRegen(ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, StringsRM.DFMA_Prefix + "regen"), order++, ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "textures/models/armor/regen.png"), false, true)),
            DRACONIC_LIBERATION = register(StringsRM.DFMA_Prefix + "draconic_liberation", () -> new DriveFormDraconic(ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, StringsRM.DFMA_Prefix + "draconic_liberation"), order++, ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "textures/models/armor/regen.png"), false, true));

     private static KKSupplier<DriveForm> register(String name, Supplier<DriveForm> driveFormSupplier) {
         return new KKSupplier<>(ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, name), DRIVE_FORMS.register(name, driveFormSupplier));
     }
}
