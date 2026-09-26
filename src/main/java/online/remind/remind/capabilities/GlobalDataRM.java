package online.remind.remind.capabilities;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.util.INBTSerializable;
import online.kingdomkeys.kingdomkeys.magic.ModMagic;
import online.remind.remind.KingdomKeysReMind;
import online.remind.remind.lib.StringsRM;
import online.remind.remind.panels.PanelGrid;
import online.remind.remind.panels.PanelRegistry;
import online.remind.remind.panels.PanelSlot;
import online.remind.remind.panels.PanelStats;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class GlobalDataRM implements INBTSerializable<CompoundTag> {

    private final List<String> situationSpells = new ArrayList<>();

    private boolean defeatedJumboCactuar;
    private boolean defeatedTonberryKing;

    private int hasteTicks;
    private int hasteLevel;
    private int slowTicks;
    private int slowLevel;
    private int berserkLevel;
    private int berserkTicks;
    private int isAutoLifeActive;

    private int prestigeLvl;
    private int strBonus;
    private int magBonus;
    private int defBonus;
    private int NGPlusWarriorCount;
    private int NGPlusMysticCount;
    private int NGPlusGuardianCount;

    private int lastHpBoostBonus;
    private int lastMpBoostBonus;

    private int strPanel;
    private int magPanel;
    private int defPanel;
    private String panelChoice;
    private int panelsStatus;

    private final LinkedHashMap<String, Integer> ownedOrganizationPanels = new LinkedHashMap<>();
    private final LinkedHashMap<String, Integer> organizationPanelAbilityBonuses = new LinkedHashMap<>();

    public static final int ORGANIZATION_PANEL_MAX_WIDTH = 15;
    public static final int ORGANIZATION_PANEL_MAX_HEIGHT = 8;
    public static final int ORGANIZATION_PANEL_STARTING_SLOTS = 15;
    public static final int ORGANIZATION_PANEL_MAX_SLOTS =
            ORGANIZATION_PANEL_MAX_WIDTH * ORGANIZATION_PANEL_MAX_HEIGHT;

    // Dream Eater Leveling
    private final java.util.LinkedHashMap<String, Integer> dreamEaterLevels = new java.util.LinkedHashMap<>();
    private final java.util.LinkedHashMap<String, Integer> dreamEaterExp = new java.util.LinkedHashMap<>();

    public static final int DREAM_EATER_MAX_LEVEL = 100;

    public static final String DREAM_EATER_CHIRITHY = KingdomKeysReMind.MODID + ":dreameater_chirithy";
    public static final String DREAM_EATER_MEOW_WOW = KingdomKeysReMind.MODID + ":dreameater_meowwow";
    public static final String DREAM_EATER_KOMORY_BAT = KingdomKeysReMind.MODID + ":dreameater_komory_bat";

    // Non-KH Dream Eaters
    public static final String DREAM_EATER_CACTUAR = KingdomKeysReMind.MODID + ":dreameater_cactuar";
    public static final String DREAM_EATER_TONBERRY = KingdomKeysReMind.MODID + ":dreameater_tonberry";

    private int unlockedOrganizationPanelSlots = ORGANIZATION_PANEL_STARTING_SLOTS;

    private PanelGrid organizationPanelGrid = new PanelGrid(
            ORGANIZATION_PANEL_MAX_WIDTH,
            ORGANIZATION_PANEL_MAX_HEIGHT
    );

    private int lastOrganizationPanelAPBonus;
    private int lastOrganizationPanelLevelBonus;

    private int ngpStatus;
    private int darkModeEXP;
    private int lightFormEXP;
    private int rageFormEXP;
    private int darkModeLvl;
    private int lightFormLvl;
    private int rageFormLvl;

    private int stepTicks;
    private byte stepType;

    private int RCCooldown;
    private int SCooldown;
    private double situationValue;
    private String style = "";
    private int styleTicks;

    private int MPOG;
    private int riskchargeCount;

    private boolean dreamEaterSummoned = false;
    private UUID dreamEaterUUID = new UUID(0L, 0L);
    private String dreamEaterRL = KingdomKeysReMind.MODID + ":" + StringsRM.none;
    private int dreamEaterSummonCooldownTicks;

    private boolean donorGiven;
    private boolean darkMode;

    private final java.util.List<String> unlockedDreamEaters = new java.util.ArrayList<>();

    private LinkedHashMap<String, Integer> learnedMagics = new LinkedHashMap<>();

    public int getLastOrganizationPanelAPBonus() {
        return lastOrganizationPanelAPBonus;
    }

    public void setLastOrganizationPanelAPBonus(int amount) {
        this.lastOrganizationPanelAPBonus = amount;
    }

    public int getLastOrganizationPanelLevelBonus() {
        return lastOrganizationPanelLevelBonus;
    }

    public void setLastOrganizationPanelLevelBonus(int amount) {
        this.lastOrganizationPanelLevelBonus = amount;
    }

    public int getOrganizationPanelGridWidth() {
        return ORGANIZATION_PANEL_MAX_WIDTH;
    }

    public int getOrganizationPanelGridHeight() {
        return ORGANIZATION_PANEL_MAX_HEIGHT;
    }

    public int getUnlockedOrganizationPanelSlots() {
        return unlockedOrganizationPanelSlots;
    }

    public void setUnlockedOrganizationPanelSlots(int amount) {
        this.unlockedOrganizationPanelSlots = Math.max(
                ORGANIZATION_PANEL_STARTING_SLOTS,
                Math.min(amount, ORGANIZATION_PANEL_MAX_SLOTS)
        );
    }

    public boolean expandOrganizationPanelGrid() {
        if (this.unlockedOrganizationPanelSlots >= ORGANIZATION_PANEL_MAX_SLOTS) {
            return false;
        }

        this.unlockedOrganizationPanelSlots++;
        return true;
    }

    public boolean isOrganizationPanelSlotUnlocked(int x, int y) {
        if (x < 0 || y < 0) {
            return false;
        }

        if (x >= ORGANIZATION_PANEL_MAX_WIDTH || y >= ORGANIZATION_PANEL_MAX_HEIGHT) {
            return false;
        }

        int unlockIndex = getOrganizationPanelSlotUnlockIndex(x, y);

        return unlockIndex >= 0 && unlockIndex < unlockedOrganizationPanelSlots;
    }

    private int getOrganizationPanelSlotUnlockIndex(int x, int y) {
        /*
         * First 15 slots:
         * 5x3 starter block.
         */
        if (x < 5 && y < 3) {
            return y * 5 + x;
        }

        /*
         * Remaining slots unlock left-to-right, top-to-bottom,
         * skipping the starter 5x3 block.
         */
        int index = ORGANIZATION_PANEL_STARTING_SLOTS;

        for (int yy = 0; yy < ORGANIZATION_PANEL_MAX_HEIGHT; yy++) {
            for (int xx = 0; xx < ORGANIZATION_PANEL_MAX_WIDTH; xx++) {
                boolean starterSlot = xx < 5 && yy < 3;

                if (starterSlot) {
                    continue;
                }

                if (xx == x && yy == y) {
                    return index;
                }

                index++;
            }
        }

        return -1;
    }

    private void normalizeOrganizationPanelGrid() {
        if (this.organizationPanelGrid == null) {
            this.organizationPanelGrid = new PanelGrid(
                    ORGANIZATION_PANEL_MAX_WIDTH,
                    ORGANIZATION_PANEL_MAX_HEIGHT
            );
            return;
        }

        if (this.organizationPanelGrid.getWidth() == ORGANIZATION_PANEL_MAX_WIDTH
                && this.organizationPanelGrid.getHeight() == ORGANIZATION_PANEL_MAX_HEIGHT) {
            return;
        }

        PanelGrid oldGrid = this.organizationPanelGrid;
        PanelGrid newGrid = new PanelGrid(
                ORGANIZATION_PANEL_MAX_WIDTH,
                ORGANIZATION_PANEL_MAX_HEIGHT
        );

        for (PanelSlot slot : oldGrid.getPlacedPanels()) {
            newGrid.place(slot.getPanelId(), slot.getX(), slot.getY());
        }

        this.organizationPanelGrid = newGrid;
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag storage = new CompoundTag();

        storage.putInt("haste_ticks", this.getHasteTicks());
        storage.putInt("haste_level", this.getHasteLevel());
        storage.putInt("slow_ticks", this.getSlowTicks());
        storage.putInt("slow_level", this.getSlowLevel());
        storage.putInt("berserk_ticks", this.getBerserkTicks());
        storage.putInt("berserk_level", this.getBerserkLevel());
        storage.putInt("autolife_active", this.getAutoLifeActive());

        storage.putInt("prestige_level", this.getPrestigeLvl());

        storage.putInt("NGPlus_STR_Bonus", this.getSTRBonus());
        storage.putInt("NGPlus_MAG_Bonus", this.getMAGBonus());
        storage.putInt("NGPlus_DEF_Bonus", this.getDEFBonus());

        storage.putInt("NGPlus_Warrior", this.getNGPWarriorCount());
        storage.putInt("NGPlus_Mystic", this.getNGPMysticCount());
        storage.putInt("NGPlus_Guardian", this.getNGPGuardianCount());

        storage.putInt("lastHpBoostBonus", this.getLastHpBoostBonus());
        storage.putInt("lastMpBoostBonus", this.getLastMpBoostBonus());

        storage.putInt("Panels_STR", this.getSTRPanel());
        storage.putInt("Panels_DEF", this.getDEFPanel());
        storage.putInt("Panels_MAG", this.getMAGPanel());
        storage.putInt("Panels_Enabled", this.getPanelsEnabled());

        normalizeOrganizationPanelGrid();
        storage.put("Organization_Panel_Grid", this.organizationPanelGrid.save());

        CompoundTag ownedPanelsTag = new CompoundTag();

        for (Map.Entry<String, Integer> entry : this.ownedOrganizationPanels.entrySet()) {
            ownedPanelsTag.putInt(entry.getKey(), entry.getValue());
        }

        CompoundTag panelAbilityBonusesTag = new CompoundTag();

        for (Map.Entry<String, Integer> entry : this.organizationPanelAbilityBonuses.entrySet()) {
            panelAbilityBonusesTag.putInt(entry.getKey(), entry.getValue());
        }

        storage.put("Organization_Panel_Ability_Bonuses", panelAbilityBonusesTag);

        storage.put("Organization_Owned_Panels", ownedPanelsTag);
        storage.putInt("Organization_Last_Panel_AP_Bonus", this.getLastOrganizationPanelAPBonus());
        storage.putInt("Organization_Last_Panel_Level_Bonus", this.getLastOrganizationPanelLevelBonus());
        storage.putInt("Organization_Unlocked_Panel_Slots", this.unlockedOrganizationPanelSlots);


        storage.putInt("NGPlus_Enabled", this.getNGPEnabled());

        storage.putInt("riskcharge_count", this.getRiskchargeCount());
        storage.putDouble("situation_value", this.getSituationValue());
        storage.putString("style", this.getStyle());

        if (dreamEaterUUID != null) {
            storage.putUUID("DreamEaterUUID", this.getDreamEaterUUID());
        }

        storage.putBoolean("dreamEaterSummoned", this.hasDreamEaterSummoned());
        storage.putString("dreamEaterRL", this.getDreamEaterRL());

        storage.putBoolean("defeatedJumboCactuar", this.hasDefeatedJumboCactuar());
        storage.putBoolean("defeatedTonberryKing", this.hasDefeatedTonberryKing());

        storage.putBoolean("donor_grant", this.getDonorGiven());

        CompoundTag magicNames = new CompoundTag();

        for (Map.Entry<String, Integer> entry : this.getLearndedMagics().entrySet()) {
            magicNames.putInt(entry.getKey(), entry.getValue());
        }

        storage.put("magic_names", magicNames);


        normalizeUnlockedDreamEaters();

        net.minecraft.nbt.ListTag unlockedDreamEatersTag = new net.minecraft.nbt.ListTag();

        for (String dreamEaterRL : this.unlockedDreamEaters) {
            unlockedDreamEatersTag.add(net.minecraft.nbt.StringTag.valueOf(dreamEaterRL));
        }

        storage.put("unlockedDreamEaters", unlockedDreamEatersTag);

        CompoundTag dreamEaterProgressTag = new CompoundTag();

        java.util.LinkedHashSet<String> allDreamEaters = new java.util.LinkedHashSet<>();
        allDreamEaters.addAll(this.dreamEaterLevels.keySet());
        allDreamEaters.addAll(this.dreamEaterExp.keySet());

        for (String dreamEaterRL : allDreamEaters) {
            if (dreamEaterRL == null || dreamEaterRL.isEmpty()) {
                continue;
            }

            CompoundTag progressTag = new CompoundTag();
            progressTag.putInt("Level", getDreamEaterLevel(dreamEaterRL));
            progressTag.putInt("Exp", getDreamEaterExp(dreamEaterRL));

            dreamEaterProgressTag.put(dreamEaterRL, progressTag);
        }

        storage.put("DreamEaterProgress", dreamEaterProgressTag);

        return storage;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
        CompoundTag properties = nbt;

        this.setHasteTicks(properties.getInt("haste_ticks"), properties.getInt("haste_level"));
        this.setSlowTicks(properties.getInt("slow_ticks"), properties.getInt("slow_level"));
        this.setBerserkTicks(properties.getInt("berserk_ticks"), properties.getInt("berserk_level"));
        this.setAutoLifeActive(properties.getInt("autolife_active"));

        this.setPrestigeLvl(properties.getInt("prestige_level"));

        this.setSTRBonus(properties.getInt("NGPlus_STR_Bonus"));
        this.setMAGBonus(properties.getInt("NGPlus_MAG_Bonus"));
        this.setDEFBonus(properties.getInt("NGPlus_DEF_Bonus"));

        this.setNGPWarriorCount(properties.getInt("NGPlus_Warrior"));
        this.setNGPMysticCount(properties.getInt("NGPlus_Mystic"));
        this.setNGPGuardianCount(properties.getInt("NGPlus_Guardian"));

        this.setLastHpBoostBonus(properties.getInt("lastHpBoostBonus"));
        this.setLastMpBoostBonus(properties.getInt("lastMpBoostBonus"));

        this.setSTRPanel(properties.getInt("Panels_STR"));
        this.setMAGPanel(properties.getInt("Panels_MAG"));
        this.setDEFPanel(properties.getInt("Panels_DEF"));
        this.setPanelsEnabled(properties.getInt("Panels_Enabled"));

        if (properties.contains("Organization_Panel_Grid", Tag.TAG_COMPOUND)) {
            this.organizationPanelGrid = PanelGrid.load(properties.getCompound("Organization_Panel_Grid"));
        } else {
            this.organizationPanelGrid = new PanelGrid(
                    ORGANIZATION_PANEL_MAX_WIDTH,
                    ORGANIZATION_PANEL_MAX_HEIGHT
            );
        }

        normalizeOrganizationPanelGrid();

        this.unlockedOrganizationPanelSlots = properties.contains("Organization_Unlocked_Panel_Slots")
                ? properties.getInt("Organization_Unlocked_Panel_Slots")
                : ORGANIZATION_PANEL_STARTING_SLOTS;

        this.unlockedOrganizationPanelSlots = Math.max(
                ORGANIZATION_PANEL_STARTING_SLOTS,
                Math.min(this.unlockedOrganizationPanelSlots, ORGANIZATION_PANEL_MAX_SLOTS)
        );

        this.ownedOrganizationPanels.clear();

        if (properties.contains("Organization_Owned_Panels", Tag.TAG_COMPOUND)) {
            CompoundTag ownedPanelsTag = properties.getCompound("Organization_Owned_Panels");

            for (String key : ownedPanelsTag.getAllKeys()) {
                this.ownedOrganizationPanels.put(key, ownedPanelsTag.getInt(key));
            }
        } else {
            giveDefaultOrganizationPanels();
        }

        this.organizationPanelAbilityBonuses.clear();

        if (properties.contains("Organization_Panel_Ability_Bonuses", Tag.TAG_COMPOUND)) {
            CompoundTag panelAbilityBonusesTag = properties.getCompound("Organization_Panel_Ability_Bonuses");

            for (String key : panelAbilityBonusesTag.getAllKeys()) {
                this.organizationPanelAbilityBonuses.put(key, panelAbilityBonusesTag.getInt(key));
            }
        }

        this.setLastOrganizationPanelAPBonus(properties.getInt("Organization_Last_Panel_AP_Bonus"));
        this.setLastOrganizationPanelLevelBonus(properties.getInt("Organization_Last_Panel_Level_Bonus"));

        this.setNGPEnabled(properties.getInt("NGPlus_Enabled"));
        this.setRiskchargeCount(properties.getInt("riskcharge_count"));

        this.setSituationValue(properties.getDouble("situation_value"));
        this.setStyle(properties.getString("style"));

        this.setDonorGiven(properties.getBoolean("donor_grant"));

        // ----------------------------------- Dream Eaters - Load ------------------------------------------------------

        if (properties.contains("DreamEaterUUID")) {
            this.setDreamEaterUUID(properties.getUUID("DreamEaterUUID"));
        } else {
            this.dreamEaterUUID = null;
        }

        this.setDreamEaterRL(properties.getString("dreamEaterRL"));
        this.setDefeatedJumboCactuar(properties.getBoolean("defeatedJumboCactuar"));
        this.setDefeatedTonberryKing(properties.getBoolean("defeatedTonberryKing"));

        this.unlockedDreamEaters.clear();

        if (properties.contains("unlockedDreamEaters", Tag.TAG_LIST)) {
            net.minecraft.nbt.ListTag unlockedDreamEatersTag =
                    properties.getList("unlockedDreamEaters", net.minecraft.nbt.Tag.TAG_STRING);

            for (int i = 0; i < unlockedDreamEatersTag.size(); i++) {
                String id = unlockedDreamEatersTag.getString(i);

                if (id == null || id.isEmpty()) {
                    continue;
                }

                if (id.length() > 128) {
                    continue;
                }

                if (!id.contains(":")) {
                    continue;
                }

                this.unlockedDreamEaters.add(id);

                if (this.unlockedDreamEaters.size() >= 64) {
                    break;
                }
            }
        }



        normalizeUnlockedDreamEaters();

        // -----------------------------------------------------------------------------------

        // ---------------------------------------Dream Eater Leveling--------------------------------------------

        this.dreamEaterLevels.clear();
        this.dreamEaterExp.clear();

        if (properties.contains("DreamEaterProgress", Tag.TAG_COMPOUND)) {
            CompoundTag dreamEaterProgressTag = properties.getCompound("DreamEaterProgress");

            for (String dreamEaterRL : dreamEaterProgressTag.getAllKeys()) {
                if (dreamEaterRL == null || dreamEaterRL.isEmpty()) {
                    continue;
                }

                if (dreamEaterRL.length() > 128 || !dreamEaterRL.contains(":")) {
                    continue;
                }

                CompoundTag progressTag = dreamEaterProgressTag.getCompound(dreamEaterRL);

                int level = Math.max(1, Math.min(
                        progressTag.getInt("Level"),
                        DREAM_EATER_MAX_LEVEL
                ));

                int exp = Math.max(0, progressTag.getInt("Exp"));

                this.dreamEaterLevels.put(dreamEaterRL, level);
                this.dreamEaterExp.put(dreamEaterRL, exp);
            }
        }

        // Chirithy exists by default.
        ensureDreamEaterProgress(DREAM_EATER_CHIRITHY);

        // Any unlocked Dream Eater should have progress.
        for (String dreamEaterRL : this.unlockedDreamEaters) {
            ensureDreamEaterProgress(dreamEaterRL);
        }

        // -----------------------------------------------------------------------------------

        learnedMagics.clear();

        if (properties.contains("magic_names", Tag.TAG_COMPOUND)) {
            CompoundTag magicNames = properties.getCompound("magic_names");

            for (String magicName : magicNames.getAllKeys()) {
                if (ModMagic.registry.containsKey(ResourceLocation.parse(magicName))) {
                    this.getLearndedMagics().put(magicName, magicNames.getInt(magicName));
                }
            }
        }
    }

    public int getHasteLevel() {
        return hasteLevel;
    }

    public void setHasteLevel(int level) {
        this.hasteLevel = level;
    }

    public int getHasteTicks() {
        return hasteTicks;
    }

    public void setHasteTicks(int i, int level) {
        hasteTicks = i;
        hasteLevel = level;
    }

    public void remHasteTicks(int ticks) {
        hasteTicks -= ticks;
    }

    public int getSlowLevel() {
        return slowLevel;
    }

    public void setSlowLevel(int level) {
        this.slowLevel = level;
    }

    public int getSlowTicks() {
        return slowTicks;
    }

    public void setSlowTicks(int i, int level) {
        slowTicks = i;
        slowLevel = level;
    }

    public void remSlowTicks(int ticks) {
        slowTicks -= ticks;
    }

    public void setSlowCaster(String name) {
    }

    public int getBerserkLevel() {
        return berserkLevel;
    }

    public void setBerserkLevel(int level) {
        this.berserkLevel = level;
    }

    public int getBerserkTicks() {
        return berserkTicks;
    }

    public void setBerserkTicks(int i, int level) {
        berserkTicks = i;
        berserkLevel = level;
    }

    public void remBerserkTicks(int ticks) {
        berserkTicks -= ticks;
    }

    public int setAutoLifeActive(int autoLifeActive) {
        isAutoLifeActive = autoLifeActive;
        return autoLifeActive;
    }

    public int getAutoLifeActive() {
        return isAutoLifeActive;
    }

    public void remAutoLifeActive(int use) {
        isAutoLifeActive -= use;
    }

    public void setStepTicks(int i, byte type) {
        stepTicks = i;
        stepType = type;
    }

    public void remStepTicks(int ticks) {
        stepTicks -= ticks;
    }

    public int getStepTicks() {
        return stepTicks;
    }

    public byte getStepType() {
        return stepType;
    }

    public int getRiskchargeCount() {
        return riskchargeCount;
    }

    public void setRiskchargeCount(int i) {
        riskchargeCount = i;
    }

    public int getPrestigeLvl() {
        return prestigeLvl;
    }

    public void setPrestigeLvl(int i) {
        prestigeLvl = i;
    }

    public void addPrestigeLvl(int i) {
        prestigeLvl += i;
    }

    public int getSTRBonus() {
        return strBonus;
    }

    public void setSTRBonus(int i) {
        strBonus = i;
    }

    public int getMAGBonus() {
        return magBonus;
    }

    public void setMAGBonus(int i) {
        magBonus = i;
    }

    public int getDEFBonus() {
        return defBonus;
    }

    public void setDEFBonus(int i) {
        defBonus = i;
    }

    public void addSTRBonus(int i) {
        strBonus += i;
    }

    public void addMAGBonus(int i) {
        magBonus += i;
    }

    public void addDEFBonus(int i) {
        defBonus += i;
    }

    public int getNGPWarriorCount() {
        return NGPlusWarriorCount;
    }

    public void setNGPWarriorCount(int i) {
        NGPlusWarriorCount = i;
    }

    public int getNGPMysticCount() {
        return NGPlusMysticCount;
    }

    public void setNGPMysticCount(int i) {
        NGPlusMysticCount = i;
    }

    public int getNGPGuardianCount() {
        return NGPlusGuardianCount;
    }

    public void setNGPGuardianCount(int i) {
        NGPlusGuardianCount = i;
    }

    public void addNGPWarriorCount(int i) {
        NGPlusWarriorCount += i;
    }

    public void addNGPMysticCount(int i) {
        NGPlusMysticCount += i;
    }

    public void addNGPGuardianCount(int i) {
        NGPlusGuardianCount += i;
    }

    public int getRCCooldownTicks() {
        return this.RCCooldown;
    }

    public void setRCCooldownTicks(int ticks) {
        this.RCCooldown = ticks;
    }

    public void remRCCooldownTicks(int ticks) {
        this.RCCooldown = Math.max(RCCooldown - ticks, 0);
    }

    public int getSCooldownTicks() {
        return this.SCooldown;
    }

    public void setSCooldownTicks(int ticks) {
        this.SCooldown = ticks;
    }

    public void remSCooldownTicks(int ticks) {
        this.SCooldown = Math.max(SCooldown - ticks, 0);
    }

    public int getLastHpBoostBonus() {
        return lastHpBoostBonus;
    }

    public void setLastHpBoostBonus(int i) {
        lastHpBoostBonus = i;
    }

    public int getLastMpBoostBonus() {
        return lastMpBoostBonus;
    }

    public void setLastMpBoostBonus(int i) {
        lastMpBoostBonus = i;
    }

    public int getSTRPanel() {
        return strPanel;
    }

    public void setSTRPanel(int i) {
        strPanel = i;
    }

    public int getMAGPanel() {
        return magPanel;
    }

    public void setMAGPanel(int i) {
        magPanel = i;
    }

    public int getDEFPanel() {
        return defPanel;
    }

    public void setDEFPanel(int i) {
        defPanel = i;
    }

    public void addSTRPanel(int i) {
        strPanel += i;
    }

    public void addMAGPanel(int i) {
        magPanel += i;
    }

    public void addDEFPanel(int i) {
        defPanel += i;
    }

    public int getPanelsEnabled() {
        return panelsStatus;
    }

    public void setPanelsEnabled(int i) {
        panelsStatus = i;
    }

    public PanelGrid getOrganizationPanelGrid() {
        normalizeOrganizationPanelGrid();
        return organizationPanelGrid;
    }

    public void setOrganizationPanelGrid(PanelGrid grid) {
        if (grid == null) {
            this.organizationPanelGrid = new PanelGrid(
                    ORGANIZATION_PANEL_MAX_WIDTH,
                    ORGANIZATION_PANEL_MAX_HEIGHT
            );
        } else {
            this.organizationPanelGrid = grid;
        }

        normalizeOrganizationPanelGrid();
    }

    public LinkedHashMap<String, Integer> getOwnedOrganizationPanels() {
        return ownedOrganizationPanels;
    }

    public int getOwnedOrganizationPanelCount(ResourceLocation panelId) {
        if (panelId == null) {
            return 0;
        }

        return this.ownedOrganizationPanels.getOrDefault(panelId.toString(), 0);
    }

    public void setOwnedOrganizationPanelCount(ResourceLocation panelId, int count) {
        if (panelId == null) {
            return;
        }

        if (count <= 0) {
            this.ownedOrganizationPanels.remove(panelId.toString());
        } else {
            this.ownedOrganizationPanels.put(panelId.toString(), count);
        }
    }

    public void addOwnedOrganizationPanel(ResourceLocation panelId, int amount) {
        if (panelId == null || amount <= 0) {
            return;
        }

        int current = getOwnedOrganizationPanelCount(panelId);
        setOwnedOrganizationPanelCount(panelId, current + amount);
    }

    public boolean consumeOwnedOrganizationPanel(ResourceLocation panelId) {
        int current = getOwnedOrganizationPanelCount(panelId);

        if (current <= 0) {
            return false;
        }

        setOwnedOrganizationPanelCount(panelId, current - 1);
        return true;
    }

    public void refundOwnedOrganizationPanel(ResourceLocation panelId) {
        addOwnedOrganizationPanel(panelId, 1);
    }

    public void giveDefaultOrganizationPanels() {
        if (!this.ownedOrganizationPanels.isEmpty()) {
            return;
        }

        /*
         * Default panels are intentionally zero right now.
         * Players receive panels from rewards, commands, shops, or missions.
         */
    }

    public PanelStats getOrganizationPanelStats() {
        normalizeOrganizationPanelGrid();
        return this.organizationPanelGrid.calculateStats();
    }

    public boolean placeOrganizationPanel(ResourceLocation panelId, int x, int y) {
        normalizeOrganizationPanelGrid();

        var data = PanelRegistry.get(panelId);

        if (data == null) {
            return false;
        }

        /*
         * Shape-aware unlocked-slot check.
         *
         * Only occupied/body cells need unlocked slots.
         * Link-area cells are intentionally ignored here so other panels,
         * like Level Up, can be placed inside them.
         */
        for (int localY = 0; localY < data.getHeight(); localY++) {
            for (int localX = 0; localX < data.getWidth(); localX++) {
                if (!data.occupies(localX, localY)) {
                    continue;
                }

                int gridX = x + localX;
                int gridY = y + localY;

                if (!isOrganizationPanelSlotUnlocked(gridX, gridY)) {
                    return false;
                }
            }
        }

        if (!consumeOwnedOrganizationPanel(panelId)) {
            return false;
        }

        boolean placed = this.organizationPanelGrid.place(panelId, x, y);

        if (!placed) {
            refundOwnedOrganizationPanel(panelId);
            return false;
        }

        return true;
    }

    public boolean removeOrganizationPanelAt(int x, int y) {
        normalizeOrganizationPanelGrid();

        PanelSlot slot = this.organizationPanelGrid.getAt(x, y);

        if (slot == null) {
            return false;
        }

        ResourceLocation panelId = slot.getPanelId();

        boolean removed = this.organizationPanelGrid.removeAt(x, y);

        if (removed) {
            refundOwnedOrganizationPanel(panelId);
        }

        return removed;
    }

    public int getNGPEnabled() {
        return ngpStatus;
    }

    public void setNGPEnabled(int i) {
        ngpStatus = i;
    }

    public String getPanelChoice() {
        return panelChoice;
    }

    public void setPanelChoice(String choice) {
        panelChoice = choice;
    }

    public double getMPOG() {
        return MPOG;
    }

    public void setMPOG(int i) {
        this.MPOG = i;
    }

    public boolean hasDreamEaterSummoned() {
        return dreamEaterSummoned;
    }

    public void setHasDreamEaterSummoned(boolean val) {
        this.dreamEaterSummoned = val;
    }

    public UUID getDreamEaterUUID() {
        return dreamEaterUUID;
    }

    public void setDreamEaterUUID(UUID uuid) {
        this.dreamEaterUUID = uuid;
    }

    public String getDreamEaterRL() {
        return this.dreamEaterRL;
    }

    public void setDreamEaterRL(String i) {
        dreamEaterRL = i;
    }

    public boolean getDonorGiven() {
        return donorGiven;
    }

    public void setDonorGiven(boolean i) {
        donorGiven = i;
    }

    public boolean isDarkMode() {
        return darkMode;
    }

    public void setDarkMode(boolean value) {
        this.darkMode = value;
    }

    public double getSituationValue() {
        return situationValue;
    }

    public void setSituationValue(double i) {
        this.situationValue = i;
    }

    public void addSituationValue(double i) {
        this.situationValue += i;
    }

    public void remSituationValue(double i) {
        situationValue -= i;
    }

    public List<String> getSituationSpells() {
        return situationSpells;
    }

    public void addSituationSpell(String spell) {
        situationSpells.add(spell);
    }

    public void clearSituationSpells() {
        situationSpells.clear();
    }

    public String getStyle() {
        return style;
    }

    public void setStyle(String style) {
        this.style = style;
    }

    public int getStyleTicks() {
        return this.styleTicks;
    }

    public void setStyleTicks(int ticks) {
        this.styleTicks = ticks;
    }

    public void remStyleTicks(int ticks) {
        this.styleTicks = Math.max(styleTicks - ticks, 0);
    }

    public LinkedHashMap<String, Integer> getLearndedMagics() {
        return learnedMagics;
    }

    public void setLearnedMagics(LinkedHashMap<String, Integer> learnedMagics) {
        this.learnedMagics = learnedMagics;
    }

    public int getLearnedMagicLevel(ResourceLocation magic) {
        for (Map.Entry<String, Integer> entry : learnedMagics.entrySet()) {
            if (entry.getKey().equals(magic.toString())) {
                return entry.getValue();
            }
        }

        return -1;
    }

    public void setLearnedMagicLevel(ResourceLocation magic, int level) {
        learnedMagics.put(magic.toString(), level);
    }



    public boolean hasOrganizationPanelEquipped(ResourceLocation panelId) {
        if (panelId == null || this.organizationPanelGrid == null) {
            return false;
        }

        return this.organizationPanelGrid.hasPanel(panelId);
    }

    public int countOrganizationPanelEquipped(ResourceLocation panelId) {
        if (panelId == null || this.organizationPanelGrid == null) {
            return 0;
        }

        return this.organizationPanelGrid.countPanel(panelId);
    }

    public int getOrganizationPanelAbilityBonus(String ability) {
        if (ability == null) {
            return 0;
        }

        return this.organizationPanelAbilityBonuses.getOrDefault(ability, 0);
    }

    public void setOrganizationPanelAbilityBonus(String ability, int amount) {
        if (ability == null) {
            return;
        }

        if (amount <= 0) {
            this.organizationPanelAbilityBonuses.remove(ability);
        } else {
            this.organizationPanelAbilityBonuses.put(ability, amount);
        }
    }

    public LinkedHashMap<String, Integer> getOrganizationPanelAbilityBonuses() {
        return this.organizationPanelAbilityBonuses;
    }

    public boolean hasDreamEaterUnlocked(String dreamEaterRL) {
        if (dreamEaterRL == null || dreamEaterRL.isEmpty()) {
            return false;
        }

        if (DREAM_EATER_CHIRITHY.equals(dreamEaterRL)) {
            return true;
        }

        return this.unlockedDreamEaters.contains(dreamEaterRL);
    }

    public void unlockDreamEater(String dreamEaterRL) {
        if (dreamEaterRL == null || dreamEaterRL.isEmpty()) {
            return;
        }

        if (dreamEaterRL.length() > 128) {
            return;
        }

        if (!dreamEaterRL.contains(":")) {
            return;
        }

        normalizeUnlockedDreamEaters();

        if (!this.unlockedDreamEaters.contains(dreamEaterRL)) {
            this.unlockedDreamEaters.add(dreamEaterRL);
        }

        ensureDreamEaterProgress(dreamEaterRL);
    }

    public void setUnlockedDreamEaters(java.util.Collection<String> dreamEaters) {
        this.unlockedDreamEaters.clear();

        if (dreamEaters != null) {
            for (String dreamEaterRL : dreamEaters) {
                if (dreamEaterRL == null || dreamEaterRL.isEmpty()) {
                    continue;
                }

                if (dreamEaterRL.length() > 128) {
                    continue;
                }

                if (!dreamEaterRL.contains(":")) {
                    continue;
                }

                this.unlockedDreamEaters.add(dreamEaterRL);
            }
        }

        normalizeUnlockedDreamEaters();
    }

    public java.util.List<String> getUnlockedDreamEaters() {
        return this.unlockedDreamEaters;
    }

    private void normalizeUnlockedDreamEaters() {
        java.util.LinkedHashSet<String> cleaned = new java.util.LinkedHashSet<>();

        for (String id : this.unlockedDreamEaters) {
            if (id == null || id.isEmpty()) {
                continue;
            }

            if (id.length() > 128) {
                continue;
            }

            if (!id.contains(":")) {
                continue;
            }

            cleaned.add(id);

            if (cleaned.size() >= 64) {
                break;
            }
        }

        this.unlockedDreamEaters.clear();
        this.unlockedDreamEaters.addAll(cleaned);
    }

    public void ensureDreamEaterProgress(String dreamEaterRL) {
        if (dreamEaterRL == null || dreamEaterRL.isEmpty()) {
            return;
        }

        if (dreamEaterRL.length() > 128 || !dreamEaterRL.contains(":")) {
            return;
        }

        this.dreamEaterLevels.putIfAbsent(dreamEaterRL, 1);
        this.dreamEaterExp.putIfAbsent(dreamEaterRL, 0);
    }

    public int getDreamEaterLevel(String dreamEaterRL) {
        if (dreamEaterRL == null || dreamEaterRL.isEmpty()) {
            return 1;
        }

        ensureDreamEaterProgress(dreamEaterRL);

        return Math.max(1, Math.min(
                this.dreamEaterLevels.getOrDefault(dreamEaterRL, 1),
                DREAM_EATER_MAX_LEVEL
        ));
    }

    public int getDreamEaterExp(String dreamEaterRL) {
        if (dreamEaterRL == null || dreamEaterRL.isEmpty()) {
            return 0;
        }

        ensureDreamEaterProgress(dreamEaterRL);

        return Math.max(0, this.dreamEaterExp.getOrDefault(dreamEaterRL, 0));
    }

    public int getDreamEaterExpToNextLevel(String dreamEaterRL) {
        int level = getDreamEaterLevel(dreamEaterRL);

        if (level >= DREAM_EATER_MAX_LEVEL) {
            return 0;
        }

        return 50 + (level * level * 10);
    }

    public boolean addDreamEaterExp(String dreamEaterRL, int amount) {
        if (dreamEaterRL == null || dreamEaterRL.isEmpty() || amount <= 0) {
            return false;
        }

        ensureDreamEaterProgress(dreamEaterRL);

        int level = getDreamEaterLevel(dreamEaterRL);
        int exp = getDreamEaterExp(dreamEaterRL);

        if (level >= DREAM_EATER_MAX_LEVEL) {
            this.dreamEaterLevels.put(dreamEaterRL, DREAM_EATER_MAX_LEVEL);
            this.dreamEaterExp.put(dreamEaterRL, 0);
            return false;
        }

        exp += amount;
        boolean leveledUp = false;

        while (level < DREAM_EATER_MAX_LEVEL) {
            int needed = 50 + (level * level * 10);

            if (exp < needed) {
                break;
            }

            exp -= needed;
            level++;
            leveledUp = true;
        }

        if (level >= DREAM_EATER_MAX_LEVEL) {
            level = DREAM_EATER_MAX_LEVEL;
            exp = 0;
        }

        this.dreamEaterLevels.put(dreamEaterRL, level);
        this.dreamEaterExp.put(dreamEaterRL, exp);

        return leveledUp;
    }

    public java.util.LinkedHashMap<String, Integer> getDreamEaterLevels() {
        return this.dreamEaterLevels;
    }

    public java.util.LinkedHashMap<String, Integer> getDreamEaterExpMap() {
        return this.dreamEaterExp;
    }

    public void setDreamEaterProgress(
            java.util.Map<String, Integer> levels,
            java.util.Map<String, Integer> exp
    ) {
        this.dreamEaterLevels.clear();
        this.dreamEaterExp.clear();

        if (levels != null) {
            for (java.util.Map.Entry<String, Integer> entry : levels.entrySet()) {
                String id = entry.getKey();

                if (id == null || id.isEmpty() || id.length() > 128 || !id.contains(":")) {
                    continue;
                }

                int level = Math.max(1, Math.min(entry.getValue(), DREAM_EATER_MAX_LEVEL));
                this.dreamEaterLevels.put(id, level);
            }
        }

        if (exp != null) {
            for (java.util.Map.Entry<String, Integer> entry : exp.entrySet()) {
                String id = entry.getKey();

                if (id == null || id.isEmpty() || id.length() > 128 || !id.contains(":")) {
                    continue;
                }

                int value = Math.max(0, entry.getValue());
                this.dreamEaterExp.put(id, value);
            }
        }

        for (String id : this.dreamEaterLevels.keySet()) {
            this.dreamEaterExp.putIfAbsent(id, 0);
        }
    }

    public boolean hasDefeatedJumboCactuar() {
        return this.defeatedJumboCactuar;
    }

    public void setDefeatedJumboCactuar(boolean defeated) {
        this.defeatedJumboCactuar = defeated;
    }

    public boolean hasDefeatedTonberryKing() {
        return this.defeatedTonberryKing;
    }

    public void setDefeatedTonberryKing(boolean defeated) {
        this.defeatedTonberryKing = defeated;
    }

    public int getDreamEaterSummonCooldownTicks() {
        return dreamEaterSummonCooldownTicks;
    }

    public void setDreamEaterSummonCooldownTicks(int ticks) {
        this.dreamEaterSummonCooldownTicks = Math.max(0, ticks);
    }

    public void remDreamEaterSummonCooldownTicks(int amount) {
        this.dreamEaterSummonCooldownTicks = Math.max(0, dreamEaterSummonCooldownTicks - amount);
    }

}