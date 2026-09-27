package online.remind.remind.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.PacketDistributor;
import online.kingdomkeys.kingdomkeys.client.gui.elements.MenuBackground;
import online.kingdomkeys.kingdomkeys.client.gui.elements.MenuBox;
import online.kingdomkeys.kingdomkeys.client.gui.elements.buttons.MenuButton;
import online.kingdomkeys.kingdomkeys.client.gui.elements.buttons.MenuScrollBar;
import online.kingdomkeys.kingdomkeys.client.sound.ModSounds;
import online.kingdomkeys.kingdomkeys.data.PlayerData;
import online.remind.remind.KingdomKeysReMind;
import online.remind.remind.capabilities.GlobalDataRM;
import online.remind.remind.capabilities.ModDataRM;
import online.remind.remind.network.PanelPacketAction;
import online.remind.remind.network.cts.CSOrganizationPanelPacket;
import online.remind.remind.panels.*;

import java.awt.Color;

public class PanelEditorMenu extends MenuBackground {

    private static final int INVENTORY_ROW_HEIGHT = 16;
    private static final ResourceLocation[] INVENTORY_PANELS = new ResourceLocation[]{
            PanelRegistry.STRENGTH_UNIT, PanelRegistry.MAGIC_UNIT, PanelRegistry.DEFENSE_UNIT, PanelRegistry.AP_UNIT, PanelRegistry.SIGHT_UNIT, PanelRegistry.LEVEL_UP,
            PanelRegistry.STRENGTH_UNIT_L, PanelRegistry.MAGIC_UNIT_L, PanelRegistry.DEFENSE_UNIT_L, PanelRegistry.AP_UNIT_L,
            PanelRegistry.LEVEL_DOUBLER_L_RIGHT, PanelRegistry.LEVEL_DOUBLER_L_LEFT, PanelRegistry.LEVEL_DOUBLER_L_TOP_RIGHT, PanelRegistry.LEVEL_DOUBLER_L_TOP_LEFT, PanelRegistry.LEVEL_DOUBLER_LINE,
            PanelRegistry.POWER_LINK, PanelRegistry.MAGIC_LINK, PanelRegistry.GUARD_LINK, PanelRegistry.LEVEL_LINK,
            PanelRegistry.HEARTS_POWER_PANEL, PanelRegistry.ULTIMA_WEAPON_PANEL,
            PanelRegistry.HIGH_JUMP_PANEL, PanelRegistry.DODGE_ROLL_PANEL, PanelRegistry.AERIAL_DODGE_PANEL, PanelRegistry.QUICK_RUN_PANEL, PanelRegistry.GLIDE_PANEL,
            PanelRegistry.COMBO_PLUS_PANEL, PanelRegistry.HASTE_PANEL, PanelRegistry.FIRE_BOOST_PANEL, PanelRegistry.BLIZZARD_BOOST_PANEL, PanelRegistry.THUNDER_BOOST_PANEL,
            PanelRegistry.WATER_BOOST_PANEL, PanelRegistry.LIGHT_BOOST_PANEL, PanelRegistry.DARK_BOOST_PANEL, PanelRegistry.DRAW_PANEL, PanelRegistry.JACKPOT_PANEL, PanelRegistry.LUCKY_LUCKY_PANEL
    };

    private MenuBox editorBox;
    private MenuScrollBar inventoryScrollBar;
    private ResourceLocation selectedPanel = PanelRegistry.STRENGTH_UNIT;

    private int editorX;
    private int editorY;
    private int editorW;
    private int editorH;
    private int inventoryX;
    private int inventoryY;
    private int inventoryW;
    private int inventoryH;
    private int gridX;
    private int gridY;
    private int slotSize = 14;
    private int selectedInfoY;
    private int statsY;

    public PanelEditorMenu() {
        super("Panel Editor", new Color(155, 155, 155));
        minecraft = Minecraft.getInstance();
    }

    @Override
    public void init() {
        super.init();
        this.clearWidgets();

        int topY = (int) (height * 0.17F) + 8;
        int margin = 16;
        int bottomReserved = 118;

        this.editorW = Math.min(820, Math.max(320, width - margin * 2));
        this.editorH = Math.max(205, Math.min(340, height - topY - bottomReserved));
        this.editorX = (width - editorW) / 2;
        this.editorY = topY;
        this.editorBox = new MenuBox(editorX, editorY, editorW, editorH, 1F, new Color(155, 155, 155));

        GlobalDataRM data = minecraft != null && minecraft.player != null ? ModDataRM.getGlobal(minecraft.player) : null;
        PanelGrid grid = data != null ? data.getOrganizationPanelGrid() : null;
        int gridCols = grid != null ? grid.getWidth() : 5;
        int gridRows = grid != null ? grid.getHeight() : 8;

        int innerLeft = editorX + 16;
        int innerRight = editorX + editorW - 16;
        int contentTop = editorY + 52;
        int contentBottom = editorY + editorH - 12;

        this.inventoryX = innerLeft;
        this.inventoryY = contentTop;
        this.inventoryW = Math.min(180, Math.max(120, editorW / 4));

        this.selectedInfoY = Math.max(inventoryY + 64, contentBottom - 42);
        this.inventoryH = Math.max(58, selectedInfoY - inventoryY - 8);

        int inventoryToGridGap = 24;
        this.gridX = inventoryX + inventoryW + inventoryToGridGap;
        this.gridY = contentTop;

        int availableGridW = Math.max(80, innerRight - gridX);
        int availableGridH = Math.max(80, contentBottom - gridY - 48);

        int slotByWidth = gridCols > 0 ? availableGridW / gridCols : 14;
        int slotByHeight = gridRows > 0 ? availableGridH / gridRows : 14;
        this.slotSize = Math.max(8, Math.min(18, Math.min(slotByWidth, slotByHeight)));

        int gridPixelHeight = gridRows * slotSize;
        this.statsY = gridY + gridPixelHeight + 10;

        this.inventoryScrollBar = new MenuScrollBar(inventoryX + inventoryW - 12, inventoryY, inventoryY + inventoryH, inventoryH, 0, false);
        this.inventoryScrollBar.setContentHeight(INVENTORY_PANELS.length * INVENTORY_ROW_HEIGHT + 4);
        addRenderableWidget(inventoryScrollBar);

        int backWidth = 80;
        int backX = editorX + editorW - backWidth - 14;
        int backY = editorY + editorH - 24;
        addRenderableWidget(new MenuButton(backX, backY, backWidth, "Back", MenuButton.ButtonType.BUTTON, false, e -> minecraft.setScreen(new PanelsMenu())));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (inventoryScrollBar != null) {
            boolean onBar = mouseX >= inventoryScrollBar.getX() && mouseX <= inventoryScrollBar.getX() + inventoryScrollBar.getWidth() && mouseY >= inventoryScrollBar.getY() && mouseY <= inventoryScrollBar.getBottom();

            if (onBar) {
                inventoryScrollBar.mouseClicked(mouseX, mouseY, button);
                return true;
            }
        }

        ResourceLocation clickedPanel = getClickedInventoryPanel((int) mouseX, (int) mouseY);

        if (clickedPanel != null && button == 0) {
            GlobalDataRM data = minecraft != null && minecraft.player != null ? ModDataRM.getGlobal(minecraft.player) : null;

            if (data != null && data.getOwnedOrganizationPanelCount(clickedPanel) <= 0) {
                minecraft.player.playSound(ModSounds.error.get());
                return true;
            }

            selectedPanel = clickedPanel;
            minecraft.player.playSound(ModSounds.menu_select.get());
            return true;
        }

        int cellX = getGridMouseX((int) mouseX);
        int cellY = getGridMouseY((int) mouseY);

        if (cellX >= 0 && cellY >= 0 && minecraft != null && minecraft.player != null) {
            GlobalDataRM data = ModDataRM.getGlobal(minecraft.player);

            if (button == 0) {
                if (data != null && !data.isOrganizationPanelSlotUnlocked(cellX, cellY)) {
                    minecraft.player.playSound(ModSounds.error.get());
                    return true;
                }

                if (data != null && data.getOwnedOrganizationPanelCount(selectedPanel) <= 0) {
                    minecraft.player.playSound(ModSounds.error.get());
                    return true;
                }

                PacketDistributor.sendToServer(new CSOrganizationPanelPacket(PanelPacketAction.PLACE, selectedPanel, cellX, cellY));
                minecraft.player.playSound(ModSounds.menu_select.get());
                return true;
            }

            if (button == 1) {
                PacketDistributor.sendToServer(new CSOrganizationPanelPacket(PanelPacketAction.REMOVE, selectedPanel, cellX, cellY));
                minecraft.player.playSound(ModSounds.menu_back.get());
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (inventoryScrollBar != null) {
            inventoryScrollBar.mouseReleased(mouseX, mouseY, button);
        }

        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (inventoryScrollBar != null) {
            inventoryScrollBar.mouseDragged(mouseX, mouseY, button, dragX, dragY);
        }

        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double deltaX, double deltaY) {
        if (inventoryScrollBar != null && mouseX >= inventoryX && mouseX <= inventoryX + inventoryW && mouseY >= inventoryY && mouseY <= inventoryY + inventoryH) {
            inventoryScrollBar.mouseScrolled(mouseX, mouseY, deltaX, deltaY);
            return true;
        }

        return super.mouseScrolled(mouseX, mouseY, deltaX, deltaY);
    }

    private ResourceLocation getClickedInventoryPanel(int mouseX, int mouseY) {
        if (inventoryScrollBar == null) {
            return null;
        }

        if (mouseX < inventoryX || mouseX >= inventoryScrollBar.getX() || mouseY < inventoryY || mouseY >= inventoryY + inventoryH) {
            return null;
        }

        int localY = mouseY - inventoryY + (int) inventoryScrollBar.scrollOffset;
        int index = localY / INVENTORY_ROW_HEIGHT;

        return index >= 0 && index < INVENTORY_PANELS.length ? INVENTORY_PANELS[index] : null;
    }

    private int getGridMouseX(int mouseX) {
        GlobalDataRM data = minecraft != null && minecraft.player != null ? ModDataRM.getGlobal(minecraft.player) : null;
        PanelGrid grid = data != null ? data.getOrganizationPanelGrid() : null;

        if (grid == null || mouseX < gridX) {
            return -1;
        }

        int x = (mouseX - gridX) / slotSize;
        return x >= 0 && x < grid.getWidth() ? x : -1;
    }

    private int getGridMouseY(int mouseY) {
        GlobalDataRM data = minecraft != null && minecraft.player != null ? ModDataRM.getGlobal(minecraft.player) : null;
        PanelGrid grid = data != null ? data.getOrganizationPanelGrid() : null;

        if (grid == null || mouseY < gridY) {
            return -1;
        }

        int y = (mouseY - gridY) / slotSize;
        return y >= 0 && y < grid.getHeight() ? y : -1;
    }

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTicks) {
        super.render(gui, mouseX, mouseY, partialTicks);

        if (editorBox != null) {
            editorBox.renderWidget(gui, mouseX, mouseY, partialTicks);
        }

        gui.drawString(this.font, "Panel Editor", editorX + 12, editorY + 8, 0xFFFF9900, false);
        String helpText = editorW < 620 ? "Select panel - Left place - Right remove" : "Select a panel - Left-click to place - Right-click to remove";
        gui.drawString(this.font, helpText, editorX + 12, editorY + 20, 0xFFAAAAAA, false);

        renderInventory(gui, mouseX, mouseY);
        renderGrid(gui, mouseX, mouseY);
        renderStats(gui);
        renderSelectedInfo(gui);

        for (var renderable : this.renderables) {
            renderable.render(gui, mouseX, mouseY, partialTicks);
        }

        int hoveredGridX = getGridMouseX(mouseX);
        int hoveredGridY = getGridMouseY(mouseY);

        if (selectedPanel != null && hoveredGridX >= 0 && hoveredGridY >= 0) {
            PanelData data = PanelRegistry.get(selectedPanel);

            if (data != null) {
                int px = gridX + hoveredGridX * slotSize;
                int py = gridY + hoveredGridY * slotSize;
                int color = getPanelColor(data);
                int alpha = ((color >>> 24) & 0xFF) / 2;
                int ghostColor = (color & 0x00FFFFFF) | (alpha << 24);
                drawPanelShape(gui, data, px, py, ghostColor);
                drawPanelShapeLabel(gui, data, selectedPanel, px, py);
            }
        }
    }

    private void renderInventory(GuiGraphics gui, int mouseX, int mouseY) {
        if (inventoryScrollBar == null) {
            return;
        }

        gui.drawString(this.font, "Inventory", inventoryX, inventoryY - 11, 0xFFFFD700, false);
        int scroll = (int) inventoryScrollBar.scrollOffset;
        int right = inventoryScrollBar.getX() - 2;

        gui.enableScissor(inventoryX, inventoryY, inventoryX + inventoryW, inventoryY + inventoryH);

        for (int i = 0; i < INVENTORY_PANELS.length; i++) {
            int y = inventoryY + i * INVENTORY_ROW_HEIGHT - scroll;

            if (y + INVENTORY_ROW_HEIGHT < inventoryY || y > inventoryY + inventoryH) {
                continue;
            }

            ResourceLocation panelId = INVENTORY_PANELS[i];
            PanelData panelData = PanelRegistry.get(panelId);

            if (panelData == null) {
                continue;
            }

            GlobalDataRM data = minecraft != null && minecraft.player != null ? ModDataRM.getGlobal(minecraft.player) : null;
            int count = data != null ? data.getOwnedOrganizationPanelCount(panelId) : 0;
            boolean selected = panelId.equals(selectedPanel);
            boolean hovered = mouseX >= inventoryX && mouseX < right && mouseY >= y && mouseY < y + INVENTORY_ROW_HEIGHT;

            gui.fill(inventoryX, y, right, y + INVENTORY_ROW_HEIGHT - 1, selected ? 0xAA660000 : hovered ? 0xAA222266 : 0xAA111144);

            if (selected) {
                gui.fill(inventoryX, y, inventoryX + 3, y + INVENTORY_ROW_HEIGHT - 1, 0xFFFFAA00);
            }

            int iconSize = 11;
            int iconX = inventoryX + 4;
            int iconY = y + 2;
            gui.fill(iconX, iconY, iconX + iconSize, iconY + iconSize, 0xAA111111);
            gui.fill(iconX + 1, iconY + 1, iconX + iconSize - 1, iconY + iconSize - 1, count > 0 ? getPanelColor(panelData) : 0xAA333333);

            String name = getPanelDisplayName(panelId.getPath());
            String countText = "x" + count;
            gui.drawString(this.font, selected ? "> " + name : name, inventoryX + 19, y + 4, selected ? 0xFFFFDD55 : count > 0 ? 0xFFFFFFFF : 0xFF888888, false);
            gui.drawString(this.font, countText, right - this.font.width(countText) - 4, y + 4, count > 0 ? 0xFF55FF55 : 0xFFFF5555, false);

            if (hovered) {
                gui.renderTooltip(this.font, Component.literal(name + " - " + getPanelDescription(panelData)), mouseX, mouseY);
            }
        }

        gui.disableScissor();
    }

    private void renderGrid(GuiGraphics gui, int mouseX, int mouseY) {
        if (minecraft == null || minecraft.player == null) {
            return;
        }

        GlobalDataRM data = ModDataRM.getGlobal(minecraft.player);

        if (data == null || data.getOrganizationPanelGrid() == null) {
            return;
        }

        PanelGrid grid = data.getOrganizationPanelGrid();
        gui.drawString(this.font, "Organization Panels", gridX, gridY - 12, 0xFFFFFFFF, false);

        for (int y = 0; y < grid.getHeight(); y++) {
            for (int x = 0; x < grid.getWidth(); x++) {
                int sx = gridX + x * slotSize;
                int sy = gridY + y * slotSize;
                boolean hovered = mouseX >= sx && mouseX < sx + slotSize && mouseY >= sy && mouseY < sy + slotSize;
                boolean unlocked = data.isOrganizationPanelSlotUnlocked(x, y);
                int border = unlocked ? hovered ? 0xFFFFFF00 : 0xFF555555 : 0xFF222222;
                int fill = unlocked ? hovered ? 0x55222222 : 0xAA111111 : 0xAA050505;

                gui.fill(sx, sy, sx + slotSize, sy + slotSize, fill);
                gui.fill(sx, sy, sx + slotSize, sy + 1, border);
                gui.fill(sx, sy + slotSize - 1, sx + slotSize, sy + slotSize, border);
                gui.fill(sx, sy, sx + 1, sy + slotSize, border);
                gui.fill(sx + slotSize - 1, sy, sx + slotSize, sy + slotSize, border);

                if (!unlocked) {
                    gui.drawString(this.font, "×", sx + slotSize / 2 - 2, sy + slotSize / 2 - 4, 0xFF252525, false);
                }
            }
        }

        for (PanelSlot slot : grid.getPlacedPanels()) {
            PanelData panelData = PanelRegistry.get(slot.getPanelId());

            if (panelData == null) {
                continue;
            }

            int px = gridX + slot.getX() * slotSize;
            int py = gridY + slot.getY() * slotSize;
            drawPanelShape(gui, panelData, px, py, getPanelColor(panelData));
            drawPanelIconOrLabel(gui, slot.getPanelId(), panelData, px, py);
        }

        int hoveredX = getGridMouseX(mouseX);
        int hoveredY = getGridMouseY(mouseY);

        if (hoveredX >= 0 && hoveredY >= 0) {
            PanelSlot hovered = grid.getAt(hoveredX, hoveredY);

            if (hovered != null) {
                PanelData panelData = PanelRegistry.get(hovered.getPanelId());

                if (panelData != null) {
                    gui.renderTooltip(this.font, Component.literal(getPanelDisplayName(hovered.getPanelId().getPath()) + " - " + getPanelDescription(panelData)), mouseX, mouseY);
                }
            }
        }
    }

    private void renderStats(GuiGraphics gui) {
        if (minecraft == null || minecraft.player == null) {
            return;
        }

        GlobalDataRM data = ModDataRM.getGlobal(minecraft.player);

        if (data == null || data.getOrganizationPanelGrid() == null) {
            return;
        }

        PanelStats stats = data.getOrganizationPanelStats();
        PanelGrid grid = data.getOrganizationPanelGrid();
        PlayerData playerData = PlayerData.get(minecraft.player);
        int realLevel = playerData != null ? playerData.getLevel() : 0;
        int effectiveLevel = realLevel + stats.getLevelBonus();
        int y = statsY;

        gui.drawString(this.font, "Grid Bonuses", gridX, y, 0xFFFFD700, false);
        y += 11;
        gui.drawString(this.font, "STR +" + stats.getStrength() + "  MAG +" + stats.getMagic() + "  DEF +" + stats.getDefense(), gridX, y, 0xFFFFFFFF, false);
        y += 10;
        gui.drawString(this.font, "AP +" + stats.getAp() + "  LV +" + stats.getLevelBonus() + " (" + realLevel + " > " + effectiveLevel + ")", gridX, y, 0xFFFFFFFF, false);
    }

    private void renderSelectedInfo(GuiGraphics gui) {
        if (selectedPanel == null || minecraft == null || minecraft.player == null) {
            return;
        }

        GlobalDataRM data = ModDataRM.getGlobal(minecraft.player);
        int count = data != null ? data.getOwnedOrganizationPanelCount(selectedPanel) : 0;
        PanelData panelData = PanelRegistry.get(selectedPanel);

        int y = selectedInfoY;
        gui.drawString(this.font, "Selected: " + getPanelDisplayName(selectedPanel.getPath()) + " x" + count, inventoryX, y, 0xFFFFD700, false);

        if (panelData != null) {
            gui.drawString(this.font, getPanelDescription(panelData), inventoryX, y + 11, 0xFFAAAAAA, false);
        }
    }

    private int getPanelColor(PanelData data) {
        return switch (data.getType()) {
            case LEVEL -> 0xAAFFFFFF;
            case STRENGTH -> 0xAAFF5555;
            case MAGIC -> 0xAA5555FF;
            case DEFENSE -> 0xAA55FF55;
            case AP -> 0xAAFF55FF;
            case ABILITY -> 0xAAAAAAFF;
            case SPELL -> 0xAA55FFFF;
            case WEAPON -> 0xAAFFAA55;
            case GEAR -> 0xAAAA8855;
            case LINK -> 0xAAFFFF55;
        };
    }

    private ResourceLocation getPanelIcon(ResourceLocation panelId) {
        if (panelId == null) {
            return null;
        }

        String path = panelId.getPath();

        return switch (path) {
            case "strength_unit", "strength_unit_l" -> ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "textures/gui/panels/icons/power_unit.png");
            case "power_link" -> ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "textures/gui/panels/icons/power_link.png");
            case "magic_unit", "magic_unit_l" -> ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "textures/gui/panels/icons/magic_unit.png");
            case "magic_link" -> ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "textures/gui/panels/icons/magic_link.png");
            case "defense_unit", "defense_unit_l" -> ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "textures/gui/panels/icons/guard_unit.png");
            case "guard_link" -> ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "textures/gui/panels/icons/guard_link.png");
            case "level_up" -> ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "textures/gui/panels/icons/level_up.png");
            case "sight_unit" -> ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "textures/gui/panels/icons/sight_unit.png");
            case "haste_panel" -> ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "textures/gui/panels/icons/haste.png");
            case "level_doubler", "level_doubler_line", "level_doubler_l_top_left", "level_doubler_l_top_right", "level_doubler_l_left", "level_doubler_l_right" -> ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "textures/gui/panels/icons/lv_doubler.png");
            case "fire_boost_panel" -> ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "textures/gui/panels/icons/fire_panel.png");
            case "blizzard_boost_panel" -> ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "textures/gui/panels/icons/blizzard_panel.png");
            case "thunder_boost_panel" -> ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "textures/gui/panels/icons/thunder_panel.png");
            case "water_boost_panel" -> ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "textures/gui/panels/icons/water_panel.png");
            case "light_boost_panel" -> ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "textures/gui/panels/icons/light_panel.png");
            case "dark_boost_panel" -> ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "textures/gui/panels/icons/dark_panel.png");
            case "draw_panel" -> ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "textures/gui/panels/icons/treasure_magnet_panel.png");
            case "jackpot_panel", "lucky_lucky_panel" -> ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "textures/gui/panels/icons/ability_unit.png");
            case "high_jump_panel" -> ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "textures/gui/panels/icons/high_jump_panel.png");
            case "quick_run_panel" -> ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "textures/gui/panels/icons/quick_run_panel.png");
            case "dodge_roll_panel" -> ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "textures/gui/panels/icons/dodge_roll_panel.png");
            case "aerial_dodge_panel" -> ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "textures/gui/panels/icons/aerial_dodge_panel.png");
            case "glide_panel" -> ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "textures/gui/panels/icons/glide_panel.png");
            case "hearts_power_panel" -> ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "textures/gui/panels/icons/hearts_are_power_panel.png");
            case "ultima_weapon_panel" -> ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "textures/gui/panels/icons/ultima_weapon.png");
            default -> null;
        };
    }

    private void drawPanelShape(GuiGraphics gui, PanelData data, int px, int py, int color) {
        for (int localY = 0; localY < data.getHeight(); localY++) {
            for (int localX = 0; localX < data.getWidth(); localX++) {
                if (!data.occupies(localX, localY)) {
                    continue;
                }

                int cellX = px + localX * slotSize;
                int cellY = py + localY * slotSize;
                gui.fill(cellX + 2, cellY + 2, cellX + slotSize - 2, cellY + slotSize - 2, color);

                if (data.linksAt(localX, localY)) {
                    gui.fill(cellX + 1, cellY + 1, cellX + slotSize - 1, cellY + 2, 0xFF55FFFF);
                    gui.fill(cellX + 1, cellY + 1, cellX + 2, cellY + slotSize - 1, 0xFF55FFFF);
                    gui.fill(cellX + 1, cellY + slotSize - 2, cellX + slotSize - 1, cellY + slotSize - 1, 0xFF007799);
                    gui.fill(cellX + slotSize - 2, cellY + 1, cellX + slotSize - 1, cellY + slotSize - 1, 0xFF007799);
                }
            }
        }
    }

    private int[] getFirstOccupiedCell(PanelData data) {
        for (int localY = 0; localY < data.getHeight(); localY++) {
            for (int localX = 0; localX < data.getWidth(); localX++) {
                if (data.occupies(localX, localY)) {
                    return new int[]{localX, localY};
                }
            }
        }

        return null;
    }

    private int[] getIconCell(ResourceLocation panelId, PanelData data) {
        if (PanelRegistry.POWER_LINK.equals(panelId) || PanelRegistry.MAGIC_LINK.equals(panelId) || PanelRegistry.GUARD_LINK.equals(panelId) || PanelRegistry.LEVEL_LINK.equals(panelId)) {
            int centerX = data.getWidth() / 2;
            int centerY = data.getHeight() / 2;

            if (data.occupies(centerX, centerY)) {
                return new int[]{centerX, centerY};
            }
        }

        return getFirstOccupiedCell(data);
    }

    private void drawPanelIconOrLabel(GuiGraphics gui, ResourceLocation panelId, PanelData data, int panelX, int panelY) {
        int[] cell = getIconCell(panelId, data);

        if (cell == null) {
            return;
        }

        int cellX = panelX + cell[0] * slotSize;
        int cellY = panelY + cell[1] * slotSize;
        ResourceLocation icon = getPanelIcon(panelId);

        if (icon != null) {
            int iconSize = Math.max(8, slotSize - 4);
            gui.blit(icon, cellX + 2, cellY + 2, 0, 0, iconSize, iconSize, iconSize, iconSize);
            return;
        }

        String label = getPanelShortName(panelId.getPath());
        int color = data.getType() == PanelType.LINK ? 0x000000 : 0xFFFFFF;

        gui.pose().pushPose();
        gui.pose().translate(cellX + 2, cellY + Math.max(3, slotSize / 2 - 4), 0);
        gui.pose().scale(0.5F, 0.5F, 1F);
        gui.drawString(font, label, 0, 0, color, false);
        gui.pose().popPose();
    }

    private void drawPanelShapeLabel(GuiGraphics gui, PanelData data, ResourceLocation panelId, int px, int py) {
        int[] cell = getFirstOccupiedCell(data);

        if (cell == null) {
            return;
        }

        String label = getPanelShortName(panelId.getPath());
        int color = data.getType() == PanelType.LINK ? 0x000000 : 0xFFFFFF;
        int x = px + cell[0] * slotSize;
        int y = py + cell[1] * slotSize;

        gui.pose().pushPose();
        gui.pose().translate(x + 2, y + Math.max(3, slotSize / 2 - 4), 0);
        gui.pose().scale(0.5F, 0.5F, 1F);
        gui.drawString(font, label, 2, 0, color, false);
        gui.pose().popPose();
    }

    private String getPanelShortName(String path) {
        return switch (path) {
            case "level_up" -> "LV";
            case "strength_unit" -> "S";
            case "magic_unit" -> "M";
            case "defense_unit" -> "D";
            case "sight_unit" -> "CR";
            case "ap_unit" -> "AP";
            case "strength_unit_l" -> "S+";
            case "magic_unit_l" -> "M+";
            case "defense_unit_l" -> "D+";
            case "ap_unit_l" -> "AP+";
            case "level_doubler" -> "LV2";
            case "level_doubler_l_right" -> "L2R";
            case "level_doubler_l_left" -> "L2L";
            case "level_doubler_l_top_right" -> "L2TR";
            case "level_doubler_l_top_left" -> "L2TL";
            case "level_doubler_line" -> "L2-";
            case "power_link" -> "P-L";
            case "magic_link" -> "M-L";
            case "guard_link" -> "G-L";
            case "level_link" -> "L-L";
            case "hearts_power_panel" -> "♡";
            case "ultima_weapon_panel" -> "UW";
            case "high_jump_panel" -> "HJ";
            case "dodge_roll_panel" -> "DR";
            case "aerial_dodge_panel" -> "AD";
            case "quick_run_panel" -> "QR";
            case "glide_panel" -> "GL";
            case "combo_plus_panel" -> "C+";
            case "haste_panel" -> "H";
            case "fire_boost_panel" -> "FB";
            case "blizzard_boost_panel" -> "BB";
            case "thunder_boost_panel" -> "TB";
            case "water_boost_panel" -> "WB";
            case "light_boost_panel" -> "LB";
            case "dark_boost_panel" -> "DB";
            case "draw_panel" -> "DRW";
            case "jackpot_panel" -> "JP";
            case "lucky_lucky_panel" -> "LL";
            default -> path.length() > 3 ? path.substring(0, 3).toUpperCase() : path.toUpperCase();
        };
    }

    private String getPanelDisplayName(String path) {
        return switch (path) {
            case "level_up" -> "Level Up";
            case "strength_unit" -> "STR Unit";
            case "magic_unit" -> "MAG Unit";
            case "defense_unit" -> "DEF Unit";
            case "sight_unit" -> "Sight Unit";
            case "ap_unit" -> "AP Unit";
            case "strength_unit_l" -> "STR+ Unit";
            case "magic_unit_l" -> "MAG+ Unit";
            case "defense_unit_l" -> "DEF+ Unit";
            case "ap_unit_l" -> "AP+ Unit";
            case "level_doubler" -> "LV Doubler";
            case "level_doubler_l_right" -> "LV DBL L-R";
            case "level_doubler_l_left" -> "LV DBL L-L";
            case "level_doubler_l_top_right" -> "LV DBL TR";
            case "level_doubler_l_top_left" -> "LV DBL TL";
            case "level_doubler_line" -> "LV DBL Line";
            case "power_link" -> "PWR Link";
            case "magic_link" -> "MAG Link";
            case "guard_link" -> "DEF Link";
            case "level_link" -> "LV Link";
            case "hearts_power_panel" -> "Hearts Are Power";
            case "ultima_weapon_panel" -> "Ultima Weapon";
            case "high_jump_panel" -> "High Jump";
            case "dodge_roll_panel" -> "Dodge Roll";
            case "aerial_dodge_panel" -> "Aerial Dodge";
            case "quick_run_panel" -> "Quick Run";
            case "glide_panel" -> "Glide";
            case "combo_plus_panel" -> "Combo Plus";
            case "haste_panel" -> "Haste";
            case "fire_boost_panel" -> "Fire Boost";
            case "blizzard_boost_panel" -> "Blizzard Boost";
            case "thunder_boost_panel" -> "Thunder Boost";
            case "water_boost_panel" -> "Water Boost";
            case "light_boost_panel" -> "Light Boost";
            case "dark_boost_panel" -> "Dark Boost";
            case "draw_panel" -> "Draw";
            case "jackpot_panel" -> "Jackpot";
            case "lucky_lucky_panel" -> "Lucky Lucky";
            default -> path;
        };
    }

    private String getPanelDescription(PanelData data) {
        return switch (data.getId().getPath()) {
            case "strength_unit" -> "+1 STR";
            case "magic_unit" -> "+1 MAG";
            case "defense_unit" -> "+1 DEF";
            case "ap_unit" -> "+2 AP";
            case "sight_unit" -> "+Crit DMG/%";
            case "level_up" -> "+1 LV";
            case "strength_unit_l" -> "+3 STR";
            case "magic_unit_l" -> "+3 MAG";
            case "defense_unit_l" -> "+3 DEF";
            case "ap_unit_l" -> "+5 AP";
            case "level_doubler", "level_doubler_l_right", "level_doubler_l_left", "level_doubler_l_top_right", "level_doubler_l_top_left", "level_doubler_line" -> "+2 LV";
            case "power_link" -> "+1 STR for each adjacent STR panel";
            case "magic_link" -> "+1 MAG for each adjacent MAG panel";
            case "guard_link" -> "+1 DEF for each adjacent DEF panel";
            case "level_link" -> "+1 LV for each adjacent LV panel";
            case "hearts_power_panel" -> "Activates Hearts Are Power";
            case "ultima_weapon_panel" -> "Activates Ultima Weapon";
            case "combo_plus_panel" -> "+1 Combo Plus";
            case "haste_panel" -> "+Attack Speed";
            case "fire_boost_panel" -> "+1 Fire Boost";
            case "blizzard_boost_panel" -> "+1 Blizzard Boost";
            case "thunder_boost_panel" -> "+1 Thunder Boost";
            case "water_boost_panel" -> "+1 Water Boost";
            case "light_boost_panel" -> "+1 Light Boost";
            case "dark_boost_panel" -> "+1 Darkness Boost";
            case "draw_panel" -> "+1 Draw";
            case "jackpot_panel" -> "+1 Jackpot";
            case "lucky_lucky_panel" -> "+1 Lucky Lucky";
            default -> "";
        };
    }
}

