package online.remind.remind.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import online.kingdomkeys.kingdomkeys.client.gui.elements.MenuBackground;
import online.kingdomkeys.kingdomkeys.client.gui.elements.MenuBox;
import online.kingdomkeys.kingdomkeys.client.gui.elements.buttons.MenuButton;
import online.kingdomkeys.kingdomkeys.client.gui.elements.buttons.MenuScrollBar;
import online.kingdomkeys.kingdomkeys.client.sound.ModSounds;
import online.kingdomkeys.kingdomkeys.data.PlayerData;
import online.kingdomkeys.kingdomkeys.network.PacketHandler;
import online.kingdomkeys.kingdomkeys.network.cts.CSSyncAllClientDataPacket;
import online.remind.remind.capabilities.GlobalDataRM;
import online.remind.remind.capabilities.ModDataRM;
import online.remind.remind.network.PacketHandlerRM;
import online.remind.remind.network.cts.CSBuyOrganizationPanelPacket;
import online.remind.remind.panels.PanelRegistry;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class PanelShopMenu extends MenuBackground {

    private static final int ROW_HEIGHT = 16;

    private enum Category {
        ALL,
        STATS,
        GROWTH,
        ABILITIES,
        BOOSTS,
        LINKS,
        SPECIAL
    }

    private record PanelShopEntry(ResourceLocation panelId, String label, int cost, String description, Category category) {
    }

    private static final PanelShopEntry[] ENTRIES = new PanelShopEntry[]{
            new PanelShopEntry(PanelRegistry.STRENGTH_UNIT, "STR Unit", 1000, "+1 STR while placed", Category.STATS),
            new PanelShopEntry(PanelRegistry.MAGIC_UNIT, "MAG Unit", 1000, "+1 MAG while placed", Category.STATS),
            new PanelShopEntry(PanelRegistry.DEFENSE_UNIT, "DEF Unit", 1000, "+1 DEF while placed", Category.STATS),
            new PanelShopEntry(PanelRegistry.AP_UNIT, "AP Unit", 500, "+2 AP while placed", Category.STATS),
            new PanelShopEntry(PanelRegistry.SIGHT_UNIT, "Sight Unit", 1000, "Improves critical damage/chance while placed", Category.STATS),
            new PanelShopEntry(PanelRegistry.LEVEL_UP, "Level Up", 2000, "+1 LV while placed", Category.STATS),
            new PanelShopEntry(PanelRegistry.STRENGTH_UNIT_L, "STR Unit L", 2000, "+3 STR while placed", Category.STATS),
            new PanelShopEntry(PanelRegistry.MAGIC_UNIT_L, "MAG Unit L", 2000, "+3 MAG while placed", Category.STATS),
            new PanelShopEntry(PanelRegistry.DEFENSE_UNIT_L, "DEF Unit L", 2000, "+3 DEF while placed", Category.STATS),
            new PanelShopEntry(PanelRegistry.AP_UNIT_L, "AP Unit L", 1000, "+5 AP while placed", Category.STATS),
            new PanelShopEntry(PanelRegistry.LEVEL_DOUBLER_L_RIGHT, "LV Doubler L Right", 4000, "L-shaped Level Doubler", Category.STATS),
            new PanelShopEntry(PanelRegistry.LEVEL_DOUBLER_L_LEFT, "LV Doubler L Left", 4000, "Mirrored L-shaped Level Doubler", Category.STATS),
            new PanelShopEntry(PanelRegistry.LEVEL_DOUBLER_L_TOP_RIGHT, "LV Doubler Top R", 4000, "Top-right L-shaped Level Doubler", Category.STATS),
            new PanelShopEntry(PanelRegistry.LEVEL_DOUBLER_L_TOP_LEFT, "LV Doubler Top L", 4000, "Top-left L-shaped Level Doubler", Category.STATS),
            new PanelShopEntry(PanelRegistry.LEVEL_DOUBLER_LINE, "LV Doubler Line", 4000, "Long Level Doubler", Category.STATS),
            new PanelShopEntry(PanelRegistry.POWER_LINK, "Power Link", 2500, "Boosts adjacent STR panels", Category.LINKS),
            new PanelShopEntry(PanelRegistry.MAGIC_LINK, "Magic Link", 2500, "Boosts adjacent MAG panels", Category.LINKS),
            new PanelShopEntry(PanelRegistry.GUARD_LINK, "Guard Link", 2500, "Boosts adjacent DEF panels", Category.LINKS),
            new PanelShopEntry(PanelRegistry.HEARTS_POWER_PANEL, "Hearts Are Power", 50000, "Enables Hearts Are Power while equipped", Category.SPECIAL),
            new PanelShopEntry(PanelRegistry.ULTIMA_WEAPON_PANEL, "Ultima Weapon", 50000, "Enables Ultima Weapon while equipped", Category.SPECIAL),
            new PanelShopEntry(PanelRegistry.HIGH_JUMP_PANEL, "High Jump", 2500, "Enables High Jump while equipped", Category.GROWTH),
            new PanelShopEntry(PanelRegistry.DODGE_ROLL_PANEL, "Dodge Roll", 2500, "Enables Dodge Roll while equipped", Category.GROWTH),
            new PanelShopEntry(PanelRegistry.AERIAL_DODGE_PANEL, "Aerial Dodge", 3000, "Enables Aerial Dodge while equipped", Category.GROWTH),
            new PanelShopEntry(PanelRegistry.QUICK_RUN_PANEL, "Quick Run", 3000, "Enables Quick Run while equipped", Category.GROWTH),
            new PanelShopEntry(PanelRegistry.GLIDE_PANEL, "Glide", 4000, "Enables Glide while equipped", Category.GROWTH),
            new PanelShopEntry(PanelRegistry.COMBO_PLUS_PANEL, "Combo Plus", 2500, "+1 ground combo hit while equipped", Category.ABILITIES),
            new PanelShopEntry(PanelRegistry.HASTE_PANEL, "Haste", 2500, "Increases attack speed while equipped", Category.ABILITIES),
            new PanelShopEntry(PanelRegistry.FIRE_BOOST_PANEL, "Fire Boost", 3000, "Boosts Fire damage", Category.BOOSTS),
            new PanelShopEntry(PanelRegistry.BLIZZARD_BOOST_PANEL, "Blizzard Boost", 3000, "Boosts Blizzard damage", Category.BOOSTS),
            new PanelShopEntry(PanelRegistry.THUNDER_BOOST_PANEL, "Thunder Boost", 3000, "Boosts Thunder damage", Category.BOOSTS),
            new PanelShopEntry(PanelRegistry.WATER_BOOST_PANEL, "Water Boost", 3000, "Boosts Water damage", Category.BOOSTS),
            new PanelShopEntry(PanelRegistry.LIGHT_BOOST_PANEL, "Light Boost", 3000, "Boosts Light damage", Category.BOOSTS),
            new PanelShopEntry(PanelRegistry.DARK_BOOST_PANEL, "Darkness Boost", 3000, "Boosts Dark damage", Category.BOOSTS),
            new PanelShopEntry(PanelRegistry.DRAW_PANEL, "Draw", 2000, "Improves pickup range", Category.BOOSTS),
            new PanelShopEntry(PanelRegistry.JACKPOT_PANEL, "Jackpot", 2500, "Improves prize drops", Category.BOOSTS),
            new PanelShopEntry(PanelRegistry.LUCKY_LUCKY_PANEL, "Lucky Lucky", 4000, "Improves rare drops", Category.BOOSTS)
    };

    private MenuBox listBox;
    private MenuBox infoBox;
    private MenuScrollBar scrollBar;
    private ResourceLocation selectedPanel = PanelRegistry.STRENGTH_UNIT;
    private Category category = Category.ALL;
    private int listX;
    private int listY;
    private int listW;
    private int listH;
    private int infoX;
    private int infoY;
    private int infoW;
    private int infoH;
    private int actionButtonY;

    public PanelShopMenu() {
        super("Panel Shop", new Color(92, 92, 151));
        minecraft = Minecraft.getInstance();
    }

    @Override
    public void init() {
        super.init();
        this.clearWidgets();

        int topY = (int) (height * 0.17F) + 8;
        int margin = 16;
        int bottomReserved = 118;
        int maxWidth = 820;
        int totalWidth = Math.min(maxWidth, width - margin * 2);
        int left = (width - totalWidth) / 2;
        int gap = 10;

        int listWidth = Math.min(360, Math.max(210, totalWidth / 2));
        int detailWidth = totalWidth - listWidth - gap;

        int availableHeight = height - topY - bottomReserved;
        int usableHeight = Math.max(140, availableHeight);
        boolean compactHeight = usableHeight <= 170;

        this.listBox = new MenuBox(left, topY, listWidth, usableHeight, 1F, new Color(92, 92, 151));
        this.infoBox = new MenuBox(left + listWidth + gap, topY, detailWidth, usableHeight, 1F, new Color(155, 155, 155));

        this.listX = left + 10;
        this.listY = topY + 58;
        this.listW = listWidth - 20;
        this.listH = Math.max(54, usableHeight - 68);

        this.infoX = left + listWidth + gap + 12;
        this.infoY = topY + 34;
        this.infoW = detailWidth - 24;
        this.infoH = usableHeight - 44;

        this.scrollBar = new MenuScrollBar(listX + listW - 12, listY, listY + listH, listH, 0, false);
        this.scrollBar.setContentHeight(getVisibleEntries().size() * ROW_HEIGHT + 4);
        addRenderableWidget(scrollBar);

        int tabsY = topY + 18;
        int tabGap = 8;
        int tabWidth = 50;
        int totalTabsWidth = (tabWidth * 4) + (tabGap * 3);
        int tabsX = listX + (listW - totalTabsWidth) / 2;

        addRenderableWidget(new MenuButton(tabsX, tabsY, tabWidth, "All", MenuButton.ButtonType.BUTTON, false, e -> setCategory(Category.ALL)));
        addRenderableWidget(new MenuButton(tabsX + tabWidth + tabGap, tabsY, tabWidth, "Stats", MenuButton.ButtonType.BUTTON, false, e -> setCategory(Category.STATS)));
        addRenderableWidget(new MenuButton(tabsX + (tabWidth + tabGap) * 2, tabsY, tabWidth, "Growth", MenuButton.ButtonType.BUTTON, false, e -> setCategory(Category.GROWTH)));
        addRenderableWidget(new MenuButton(tabsX + (tabWidth + tabGap) * 3, tabsY, tabWidth, "More", MenuButton.ButtonType.BUTTON, false, e -> cycleMoreCategory()));

        if (compactHeight) {
            int buttonGap = 6;
            int buttonWidth = Math.min(80, Math.max(60, (detailWidth - 24 - buttonGap) / 2));
            int buttonsWidth = buttonWidth * 2 + buttonGap;
            int buttonX = left + listWidth + gap + (detailWidth - buttonsWidth) / 2;
            this.actionButtonY = topY + usableHeight - 24;

            addRenderableWidget(new MenuButton(buttonX, actionButtonY, buttonWidth, "Buy Selected", MenuButton.ButtonType.BUTTON, false, e -> buySelected()));
            addRenderableWidget(new MenuButton(buttonX + buttonWidth + buttonGap, actionButtonY, buttonWidth, "Back", MenuButton.ButtonType.BUTTON, false, e -> minecraft.setScreen(new PanelsMenu())));
        } else {
            int buttonWidth = 80;
            int buttonX = left + listWidth + gap + (detailWidth - buttonWidth) / 2;
            this.actionButtonY = topY + usableHeight - 42;

            addRenderableWidget(new MenuButton(buttonX, actionButtonY, buttonWidth, "Buy Selected", MenuButton.ButtonType.BUTTON, false, e -> buySelected()));
            addRenderableWidget(new MenuButton(buttonX, actionButtonY + 20, buttonWidth, "Back", MenuButton.ButtonType.BUTTON, false, e -> minecraft.setScreen(new PanelsMenu())));
        }
    }

    private void setCategory(Category newCategory) {
        this.category = newCategory;
        this.scrollBar.scrollOffset = 0;
        this.scrollBar.setContentHeight(getVisibleEntries().size() * ROW_HEIGHT + 4);
    }

    private void cycleMoreCategory() {
        this.category = switch (this.category) {
            case ABILITIES -> Category.BOOSTS;
            case BOOSTS -> Category.LINKS;
            case LINKS -> Category.SPECIAL;
            default -> Category.ABILITIES;
        };
        this.scrollBar.scrollOffset = 0;
        this.scrollBar.setContentHeight(getVisibleEntries().size() * ROW_HEIGHT + 4);
    }

    private List<PanelShopEntry> getVisibleEntries() {
        List<PanelShopEntry> entries = new ArrayList<>();

        for (PanelShopEntry entry : ENTRIES) {
            if (category == Category.ALL || entry.category() == category) {
                entries.add(entry);
            }
        }

        return entries;
    }

    private PanelShopEntry getSelectedEntry() {
        for (PanelShopEntry entry : ENTRIES) {
            if (entry.panelId().equals(selectedPanel)) {
                return entry;
            }
        }

        return ENTRIES[0];
    }

    private void buySelected() {
        if (minecraft == null || minecraft.player == null) {
            return;
        }

        PanelShopEntry entry = getSelectedEntry();
        PlayerData playerData = PlayerData.get(minecraft.player);

        if (playerData != null && playerData.getHearts() >= entry.cost()) {
            PacketHandlerRM.sendToServer(new CSBuyOrganizationPanelPacket(entry.panelId(), 1));
            PacketHandler.sendToServer(new CSSyncAllClientDataPacket());
            minecraft.player.playSound(ModSounds.itemget.get());
        } else {
            minecraft.player.sendSystemMessage(Component.literal("You do not have enough Hearts to do this."));
            minecraft.player.playSound(ModSounds.error.get());
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (scrollBar != null) {
            boolean onBar = mouseX >= scrollBar.getX() && mouseX <= scrollBar.getX() + scrollBar.getWidth() && mouseY >= scrollBar.getY() && mouseY <= scrollBar.getBottom();

            if (onBar) {
                scrollBar.mouseClicked(mouseX, mouseY, button);
                return true;
            }
        }

        if (button == 0) {
            List<PanelShopEntry> entries = getVisibleEntries();
            int scroll = scrollBar != null ? (int) scrollBar.scrollOffset : 0;

            if (mouseX >= listX && mouseX < listX + listW - 12 && mouseY >= listY && mouseY < listY + listH) {
                int index = ((int) mouseY - listY + scroll) / ROW_HEIGHT;

                if (index >= 0 && index < entries.size()) {
                    selectedPanel = entries.get(index).panelId();
                    minecraft.player.playSound(ModSounds.menu_select.get());
                    return true;
                }
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (scrollBar != null) {
            scrollBar.mouseReleased(mouseX, mouseY, button);
        }

        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (scrollBar != null) {
            scrollBar.mouseDragged(mouseX, mouseY, button, dragX, dragY);
        }

        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double deltaX, double deltaY) {
        if (scrollBar != null && mouseX >= listX && mouseX <= listX + listW && mouseY >= listY && mouseY <= listY + listH) {
            scrollBar.mouseScrolled(mouseX, mouseY, deltaX, deltaY);
            return true;
        }

        return super.mouseScrolled(mouseX, mouseY, deltaX, deltaY);
    }

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTicks) {
        super.render(gui, mouseX, mouseY, partialTicks);

        if (listBox != null) {
            listBox.renderWidget(gui, mouseX, mouseY, partialTicks);
        }

        if (infoBox != null) {
            infoBox.renderWidget(gui, mouseX, mouseY, partialTicks);
        }

        gui.drawString(this.font, "Panel Shop", listX, listY - 52, 0xFFFF9900, false);
        gui.drawString(this.font, "Category: " + category.name(), listX, listY - 14, 0xFFAAAAAA, false);

        renderList(gui, mouseX, mouseY);
        renderInfo(gui);

        for (var renderable : this.renderables) {
            renderable.render(gui, mouseX, mouseY, partialTicks);
        }
    }

    private void renderList(GuiGraphics gui, int mouseX, int mouseY) {
        if (scrollBar == null) {
            return;
        }

        List<PanelShopEntry> entries = getVisibleEntries();
        scrollBar.setContentHeight(entries.size() * ROW_HEIGHT + 4);
        int scroll = (int) scrollBar.scrollOffset;
        int right = scrollBar.getX() - 2;

        gui.enableScissor(listX, listY, listX + listW, listY + listH);

        for (int i = 0; i < entries.size(); i++) {
            int y = listY + i * ROW_HEIGHT - scroll;

            if (y + ROW_HEIGHT < listY || y > listY + listH) {
                continue;
            }

            PanelShopEntry entry = entries.get(i);
            boolean selected = entry.panelId().equals(selectedPanel);
            boolean hovered = mouseX >= listX && mouseX < right && mouseY >= y && mouseY < y + ROW_HEIGHT;
            int bg = selected ? 0xAA660000 : hovered ? 0xAA222266 : 0xAA111144;
            int border = selected ? 0xFFFFAA00 : hovered ? 0xFFFFFFFF : 0xFF222255;

            gui.fill(listX, y, right, y + ROW_HEIGHT - 1, bg);
            gui.fill(listX, y, right, y + 1, border);

            PlayerData playerData = minecraft != null && minecraft.player != null ? PlayerData.get(minecraft.player) : null;
            int hearts = playerData != null ? playerData.getHearts() : 0;
            int nameColor = hearts >= entry.cost() ? 0xFFFFFFFF : 0xFFAAAAAA;
            int costColor = hearts >= entry.cost() ? 0xFF55FF55 : 0xFFFF5555;

            gui.drawString(this.font, selected ? "> " + entry.label() : entry.label(), listX + 5, y + 4, nameColor, false);

            String cost = String.valueOf(entry.cost());
            gui.drawString(this.font, cost, right - this.font.width(cost) - 4, y + 4, costColor, false);
        }

        gui.disableScissor();
    }

    private void renderInfo(GuiGraphics gui) {
        if (minecraft == null || minecraft.player == null) {
            return;
        }

        PanelShopEntry entry = getSelectedEntry();
        PlayerData playerData = PlayerData.get(minecraft.player);
        GlobalDataRM data = ModDataRM.getGlobal(minecraft.player);

        int hearts = playerData != null ? playerData.getHearts() : 0;
        int owned = data != null ? data.getOwnedOrganizationPanelCount(entry.panelId()) : 0;
        int y = infoY;

        gui.drawString(this.font, "Selected Panel", infoX, y, 0xFFFF9900, false);
        y += 18;
        gui.drawString(this.font, entry.label(), infoX, y, 0xFFFFD700, false);
        y += 14;
        gui.drawString(this.font, "Cost: " + entry.cost() + " Hearts", infoX, y, hearts >= entry.cost() ? 0xFF55FF55 : 0xFFFF5555, false);
        y += 12;
        gui.drawString(this.font, "Owned: " + owned, infoX, y, owned > 0 ? 0xFFFFFF55 : 0xFFFFFFFF, false);
        y += 16;

        int descriptionBottom = actionButtonY - 6;

        for (var line : this.font.split(Component.literal(entry.description()), Math.max(50, infoW))) {
            if (y + 10 >= descriptionBottom) {
                break;
            }

            gui.drawString(this.font, line, infoX, y, 0xFFAAAAAA, false);
            y += 10;
        }
    }
}

