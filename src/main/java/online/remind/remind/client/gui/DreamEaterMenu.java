package online.remind.remind.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import online.kingdomkeys.kingdomkeys.client.gui.elements.MenuBackground;
import online.kingdomkeys.kingdomkeys.client.gui.elements.MenuColourBox;
import online.kingdomkeys.kingdomkeys.client.gui.elements.buttons.MenuButton;
import online.kingdomkeys.kingdomkeys.client.sound.ModSounds;
import online.kingdomkeys.kingdomkeys.data.PlayerData;
import online.kingdomkeys.kingdomkeys.lib.Strings;
import online.kingdomkeys.kingdomkeys.network.PacketHandler;
import online.kingdomkeys.kingdomkeys.network.cts.CSOpenMenu;
import online.remind.remind.capabilities.GlobalDataRM;
import online.remind.remind.capabilities.ModDataRM;
import online.remind.remind.client.ClientUtilsRM;
import online.remind.remind.client.gui.dreameaters.AbilityLinks;
import online.remind.remind.client.gui.dreameaters.ChangeSpirit;
import online.remind.remind.client.gui.dreameaters.CreateSpirit;
import online.remind.remind.client.sound.MusicManager;
import online.remind.remind.dreameater.DreamEater;
import online.remind.remind.dreameater.DreamEaterInfo;
import online.remind.remind.dreameater.ModDreamEaters;

import java.awt.Color;
import java.util.Locale;

public class DreamEaterMenu extends MenuBackground {

    public DreamEaterMenu(String name, Color rgb) {
        super(name, rgb);
    }

    private int previewBoxX;
    private int previewBoxY;

    private LivingEntity previewDreamEaterEntity;
    private String previewDreamEaterKey = "";

    private MenuButton backButton, changeSpirit, createSpirit, abilityLinks;

    MenuColourBox level, exp, spiritHP, spiritSTR, spiritMAG, spiritDEF, name, abilities, none;
    MenuColourBox[] spiritWidgets = {level, exp, spiritHP, spiritSTR, spiritMAG, spiritDEF, name, none};

    public DreamEaterMenu() {
        super("Dream Eaters", new Color(236, 85, 236));
        minecraft = Minecraft.getInstance();
    }

    @Override
    public void onClose() {
        super.onClose();

        Screen next = Minecraft.getInstance().screen;

        if (!(next instanceof DreamEaterMenu
                || next instanceof ChangeSpirit
                || next instanceof CreateSpirit
                || next instanceof AbilityLinks)) {
            MusicManager.stop();
        }
    }

    protected void action(String string) {
        if (string.equals("back")) {
            MusicManager.stop();
            PacketHandler.sendToServer(new CSOpenMenu());
            return;
        }

        if (string.equals("changeSpirit")) {
            minecraft.setScreen(new ChangeSpirit());
            return;
        }

        if (string.equals("createSpirit")) {
            minecraft.setScreen(new CreateSpirit());
            return;
        }

        if (string.equals("abilityLinks")) {
            minecraft.setScreen(new AbilityLinks());
            return;
        }

        if (string.equals("wip") && minecraft.player != null) {
            minecraft.player.playSound(ModSounds.error.get());
        }
    }

    @Override
    public void init() {
        super.init();

        MusicManager.start();
        this.clearWidgets();

        int topBarHeight = (int) (height * 0.17F);
        int button_statsY = topBarHeight + 5;

        int margin = 20;
        int maxContentWidth = 760;
        int contentWidth = Math.min(maxContentWidth, width - margin * 2);

        int contentLeft = (width - contentWidth) / 2;
        int contentRight = contentLeft + contentWidth;

        int buttonPosX = contentLeft;
        int buttonWidth = Math.min(150, contentWidth / 4);

        int gap = 25;

        int statsX = buttonPosX + buttonWidth + gap;
        int statsWidth = 100;

        int previewX = statsX + 170;
        int previewY = button_statsY + 5;

        if (previewX + 130 > contentRight) {
            previewX = contentRight - 130;
        }

        this.previewBoxX = previewX;
        this.previewBoxY = previewY;

        int i = 0;
        int c = 0;
        int spacer = 14;

        addRenderableWidget(changeSpirit = new MenuButton(buttonPosX, button_statsY + 18 * i++, buttonWidth, "Change Spirit", MenuButton.ButtonType.BUTTON, true, e -> action("changeSpirit")));
        addRenderableWidget(createSpirit = new MenuButton(buttonPosX, button_statsY + 18 * i++, buttonWidth, "Create Spirit", MenuButton.ButtonType.BUTTON, false, e -> action("createSpirit")));
        addRenderableWidget(abilityLinks = new MenuButton(buttonPosX, button_statsY + 18 * i++, buttonWidth, "Ability Links", MenuButton.ButtonType.BUTTON, true, e -> action("abilityLinks")));
        addRenderableWidget(backButton = new MenuButton(buttonPosX, button_statsY + 18 * i++, buttonWidth, Strings.Gui_Menu_Back, MenuButton.ButtonType.BUTTON, false, e -> action("back")));

        if (minecraft == null || minecraft.player == null) {
            return;
        }

        GlobalDataRM global = ModDataRM.getGlobal(minecraft.player);
        PlayerData playerData = PlayerData.get(minecraft.player);

        if (global == null || playerData == null) {
            return;
        }

        DreamEater dreamEater = getEquippedDreamEater(global);
        String dreamEaterRL = getEquippedDreamEaterRL(global, dreamEater);

        if (isNoDreamEaterEquipped(dreamEaterRL, dreamEater)) {
            addRenderableWidget(name = new MenuColourBox(statsX, button_statsY + (c++ * spacer), statsWidth, "Name:", "N/A", 0xffffff));
            return;
        }

        DreamEaterDisplayStats stats = getDisplayStats(global, playerData, dreamEater, dreamEaterRL);

        addRenderableWidget(name = new MenuColourBox(statsX, button_statsY + (c++ * spacer), statsWidth, "Name:", getDreamEaterDisplayName(dreamEater, dreamEaterRL), 0xffffff));
        addRenderableWidget(level = new MenuColourBox(statsX, button_statsY + (c++ * spacer), statsWidth, "Level:", String.valueOf(stats.level), 0xffffff));

        String expText = stats.expNeeded <= 0 ? "MAX" : stats.exp + " / " + stats.expNeeded;

        addRenderableWidget(exp = new MenuColourBox(statsX, button_statsY + (c++ * spacer), statsWidth, "EXP:", expText, 0xAEEAFF));
        addRenderableWidget(spiritHP = new MenuColourBox(statsX, button_statsY + (c++ * spacer), statsWidth, "Max HP:", formatStat(stats.maxHP), 0x31bf14));
        addRenderableWidget(spiritSTR = new MenuColourBox(statsX, button_statsY + (c++ * spacer), statsWidth, "STR:", formatStat(stats.strength), 0xbf1414));
        addRenderableWidget(spiritMAG = new MenuColourBox(statsX, button_statsY + (c++ * spacer), statsWidth, "MAG:", formatStat(stats.magic), 0x000088));
        addRenderableWidget(spiritDEF = new MenuColourBox(statsX, button_statsY + (c++ * spacer), statsWidth, "DEF:", formatStat(stats.defense), 0xbf8d14));
    }

    private DreamEater getEquippedDreamEater(GlobalDataRM global) {
        if (global == null) {
            return null;
        }

        String dreamEaterRL = global.getDreamEaterRL();

        if (dreamEaterRL == null || dreamEaterRL.isEmpty()) {
            return null;
        }

        try {
            return ModDreamEaters.registry.get(ResourceLocation.parse(dreamEaterRL));
        } catch (Exception e) {
            return null;
        }
    }

    private String getEquippedDreamEaterRL(GlobalDataRM global, DreamEater dreamEater) {
        if (global != null) {
            String dreamEaterRL = global.getDreamEaterRL();

            if (dreamEaterRL != null && !dreamEaterRL.isEmpty()) {
                return dreamEaterRL;
            }
        }

        return getDreamEaterRL(dreamEater);
    }

    private String getDreamEaterRL(DreamEater dreamEater) {
        if (dreamEater == null || dreamEater.getRegistryName() == null) {
            return "";
        }

        return dreamEater.getRegistryName().toString();
    }

    private boolean isNoDreamEaterEquipped(String dreamEaterRL, DreamEater dreamEater) {
        if (dreamEaterRL == null || dreamEaterRL.isEmpty()) {
            return true;
        }

        if ("none".equalsIgnoreCase(dreamEaterRL)
                || "kkremind:none".equalsIgnoreCase(dreamEaterRL)) {
            return true;
        }

        if (dreamEater == null) {
            return false;
        }

        String name = dreamEater.getName();

        return name == null
                || name.isEmpty()
                || "none".equalsIgnoreCase(name)
                || "kkremind:none".equalsIgnoreCase(name);
    }

    private DreamEaterDisplayStats getDisplayStats(
            GlobalDataRM global,
            PlayerData playerData,
            DreamEater dreamEater,
            String dreamEaterRL
    ) {
        int dreamEaterLevel = Math.max(1, global.getDreamEaterLevel(dreamEaterRL));
        int dreamEaterExp = global.getDreamEaterExp(dreamEaterRL);
        int dreamEaterExpNeeded = global.getDreamEaterExpToNextLevel(dreamEaterRL);

        if (global.hasDreamEaterSummoned() && global.getDreamEaterUUID() != null) {
            Entity entity = ClientUtilsRM.getEntityByUUIDClient(global.getDreamEaterUUID());

            if (entity instanceof LivingEntity livingEntity) {
                float maxHP = livingEntity.getMaxHealth();
                float strength = getEntityAttribute(livingEntity, Attributes.ATTACK_DAMAGE, 0F);
                float defense = getEntityAttribute(livingEntity, Attributes.ARMOR, 0F);

                /*
                 * Minecraft has no vanilla magic attribute.
                 * MAG is projected from the central Dream Eater metadata.
                 */
                float magic = getProjectedMagicForDreamEater(dreamEater, dreamEaterLevel);

                return new DreamEaterDisplayStats(
                        dreamEaterLevel,
                        dreamEaterExp,
                        dreamEaterExpNeeded,
                        maxHP,
                        strength,
                        magic,
                        defense
                );
            }
        }

        return getProjectedStatsForDreamEater(
                dreamEater,
                playerData,
                dreamEaterLevel,
                dreamEaterExp,
                dreamEaterExpNeeded
        );
    }

    private float getEntityAttribute(
            LivingEntity entity,
            net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute> attribute,
            float fallback
    ) {
        if (entity == null || entity.getAttribute(attribute) == null) {
            return fallback;
        }

        return (float) entity.getAttributeValue(attribute);
    }

    private DreamEaterDisplayStats getProjectedStatsForDreamEater(
            DreamEater dreamEater,
            PlayerData playerData,
            int dreamEaterLevel,
            int dreamEaterExp,
            int dreamEaterExpNeeded
    ) {
        DreamEaterInfo.DreamEaterStats stats =
                DreamEaterInfo.getProjectedStats(dreamEater, playerData, dreamEaterLevel);

        return new DreamEaterDisplayStats(
                dreamEaterLevel,
                dreamEaterExp,
                dreamEaterExpNeeded,
                stats.maxHP(),
                stats.strength(),
                stats.magic(),
                stats.defense()
        );
    }

    private float getProjectedMagicForDreamEater(DreamEater dreamEater, int dreamEaterLevel) {
        return DreamEaterInfo.getProjectedMagic(dreamEater, dreamEaterLevel);
    }

    private String getDreamEaterDisplayName(DreamEater dreamEater, String dreamEaterRL) {
        String displayName = null;

        if (dreamEater != null) {
            displayName = DreamEaterInfo.getDisplayName(dreamEater);
        }

        if (shouldPrettifyDisplayName(displayName)) {
            displayName = DreamEaterInfo.getDisplayName(dreamEaterRL);
        }

        if (shouldPrettifyDisplayName(displayName)) {
            String rawName = dreamEater != null ? dreamEater.getName() : null;

            if (rawName == null || rawName.isEmpty()) {
                rawName = dreamEaterRL;
            }

            displayName = prettifyDreamEaterName(rawName);
        }

        if (displayName == null || displayName.isEmpty()) {
            return "Dream Eater";
        }

        return displayName;
    }

    private boolean shouldPrettifyDisplayName(String displayName) {
        if (displayName == null || displayName.trim().isEmpty()) {
            return true;
        }

        String cleaned = displayName.trim();

        if (cleaned.contains(":")) {
            return true;
        }

        if (cleaned.contains("_")) {
            return true;
        }

        if (cleaned.startsWith("dreameater")) {
            return true;
        }

        /*
         * If the helper ever returns a raw lowercase key like "tonberry",
         * still make the menu display "Tonberry".
         */
        return cleaned.equals(cleaned.toLowerCase(Locale.ROOT)) && !cleaned.contains(" ");
    }

    private String prettifyDreamEaterName(String rawName) {
        if (rawName == null || rawName.trim().isEmpty()) {
            return "Dream Eater";
        }

        String cleaned = rawName.trim();

        if (cleaned.contains(":")) {
            cleaned = cleaned.substring(cleaned.indexOf(':') + 1);
        }

        if (cleaned.startsWith("dreameater_")) {
            cleaned = cleaned.substring("dreameater_".length());
        }

        cleaned = cleaned.replace('_', ' ').trim();

        if (cleaned.isEmpty()) {
            return "Dream Eater";
        }

        StringBuilder builder = new StringBuilder();
        boolean capitalizeNext = true;

        for (char c : cleaned.toCharArray()) {
            if (Character.isWhitespace(c)) {
                builder.append(c);
                capitalizeNext = true;
                continue;
            }

            builder.append(capitalizeNext ? Character.toUpperCase(c) : c);
            capitalizeNext = false;
        }

        return builder.toString();
    }

    private String formatStat(float value) {
        if (Math.abs(value - Math.round(value)) < 0.01F) {
            return String.valueOf(Math.round(value));
        }

        return String.format(Locale.ROOT, "%.1f", value);
    }

    private static class DreamEaterDisplayStats {
        private final int level;
        private final int exp;
        private final int expNeeded;
        private final float maxHP;
        private final float strength;
        private final float magic;
        private final float defense;

        private DreamEaterDisplayStats(
                int level,
                int exp,
                int expNeeded,
                float maxHP,
                float strength,
                float magic,
                float defense
        ) {
            this.level = level;
            this.exp = exp;
            this.expNeeded = expNeeded;
            this.maxHP = maxHP;
            this.strength = strength;
            this.magic = magic;
            this.defense = defense;
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        renderDreamEaterPreview(guiGraphics, mouseX, mouseY);
    }

    private void renderDreamEaterPreview(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (minecraft == null || minecraft.player == null || minecraft.level == null) {
            return;
        }

        GlobalDataRM global = ModDataRM.getGlobal(minecraft.player);
        PlayerData playerData = PlayerData.get(minecraft.player);

        if (global == null || playerData == null) {
            return;
        }

        DreamEater dreamEater = getEquippedDreamEater(global);
        String dreamEaterRL = getEquippedDreamEaterRL(global, dreamEater);

        if (isNoDreamEaterEquipped(dreamEaterRL, dreamEater)) {
            return;
        }

        LivingEntity entity = getOrCreatePreviewEntity(dreamEater, dreamEaterRL, playerData);

        if (entity == null) {
            return;
        }

        int dreamEaterLevel = Math.max(1, global.getDreamEaterLevel(dreamEaterRL));
        int dreamEaterExp = global.getDreamEaterExp(dreamEaterRL);
        int dreamEaterExpNeeded = global.getDreamEaterExpToNextLevel(dreamEaterRL);


        int boxX = previewBoxX;
        int boxY = previewBoxY;
        int boxW = 130;
        int boxH = 145;

        guiGraphics.fill(boxX - 4, boxY - 16, boxX + boxW + 4, boxY + boxH + 4, 0xAA000000);

        guiGraphics.drawString(
                minecraft.font,
                getDreamEaterDisplayName(dreamEater, dreamEaterRL),
                boxX + 8,
                boxY - 12,
                0xFFFFFF,
                false
        );

        guiGraphics.drawString(
                minecraft.font,
                "Lv. " + dreamEaterLevel,
                boxX + 8,
                boxY,
                0xFFD966,
                false
        );

        String expLine = dreamEaterExpNeeded <= 0
                ? "EXP MAX"
                : "EXP " + dreamEaterExp + " / " + dreamEaterExpNeeded;

        guiGraphics.drawString(
                minecraft.font,
                expLine,
                boxX + 8,
                boxY + 10,
                0xffae00,
                false
        );

        if (dreamEaterExpNeeded > 0) {
            int barX = boxX + 8;
            int barY = boxY + 21;
            int barW = boxW - 16;
            int barH = 5;

            float progress = Math.min(1.0F, Math.max(0.0F, dreamEaterExp / (float) dreamEaterExpNeeded));
            int filledW = (int) (barW * progress);

            guiGraphics.fill(barX - 1, barY - 1, barX + barW + 1, barY + barH + 1, 0xAA000000);
            guiGraphics.fill(barX, barY, barX + barW, barY + barH, 0xFF222222);
            guiGraphics.fill(barX, barY, barX + filledW, barY + barH, 0xFFf0ac19);
        }

        int scale = getPreviewScale(dreamEater);

        entity.tickCount = minecraft.player.tickCount;

        InventoryScreen.renderEntityInInventoryFollowsMouse(
                guiGraphics,
                boxX,
                boxY + 24,
                boxX + boxW,
                boxY + boxH,
                scale,
                0.25F,
                mouseX,
                mouseY,
                entity
        );
    }

    private LivingEntity getOrCreatePreviewEntity(
            DreamEater dreamEater,
            String dreamEaterRL,
            PlayerData playerData
    ) {
        if (minecraft == null
                || minecraft.level == null
                || minecraft.player == null
                || dreamEater == null
                || playerData == null) {
            return null;
        }

        String key = dreamEaterRL + ":" + playerData.getAlignment();

        if (previewDreamEaterEntity != null && key.equals(previewDreamEaterKey)) {
            return previewDreamEaterEntity;
        }

        previewDreamEaterKey = key;
        previewDreamEaterEntity = DreamEaterInfo.createPreviewEntity(
                dreamEater,
                minecraft.level,
                minecraft.player,
                playerData
        );

        return previewDreamEaterEntity;
    }

    private int getPreviewScale(DreamEater dreamEater) {
        return DreamEaterInfo.getPreviewScale(dreamEater);
    }
}