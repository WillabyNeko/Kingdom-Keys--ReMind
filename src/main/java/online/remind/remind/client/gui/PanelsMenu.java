package online.remind.remind.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import online.kingdomkeys.kingdomkeys.client.gui.elements.MenuBackground;
import online.kingdomkeys.kingdomkeys.client.gui.elements.MenuBox;
import online.kingdomkeys.kingdomkeys.client.gui.elements.buttons.MenuButton;
import online.kingdomkeys.kingdomkeys.data.PlayerData;
import online.kingdomkeys.kingdomkeys.lib.Strings;
import online.kingdomkeys.kingdomkeys.network.PacketHandler;
import online.kingdomkeys.kingdomkeys.network.cts.CSOpenMenu;

import java.awt.Color;

public class PanelsMenu extends MenuBackground {

    private MenuBox menuBox;

    public PanelsMenu() {
        super("Panel System", new Color(154, 154, 154));
        minecraft = Minecraft.getInstance();
    }

    @Override
    public void init() {
        super.init();
        this.clearWidgets();

        int topY = (int) (height * 0.17F) + 10;
        int maxWidth = 260;
        int menuWidth = Math.min(maxWidth, Math.max(180, width - 40));
        int x = (width - menuWidth) / 2;
        int boxHeight = 135;
        int y = Math.min(topY, Math.max(10, height - boxHeight - 70));

        this.menuBox = new MenuBox(x - 10, y - 10, menuWidth + 20, boxHeight, 1F, new Color(155, 155, 155));

        int buttonWidth = Math.min(150, menuWidth - 20);
        int buttonX = x + (menuWidth - buttonWidth) / 2;
        int buttonY = y + 18;

        addRenderableWidget(new MenuButton(buttonX, buttonY, buttonWidth, "Panel Shop", MenuButton.ButtonType.BUTTON, false, e -> minecraft.setScreen(new PanelShopMenu())));
        buttonY += 22;
        addRenderableWidget(new MenuButton(buttonX, buttonY, buttonWidth, "Panel Editor", MenuButton.ButtonType.BUTTON, false, e -> minecraft.setScreen(new PanelEditorMenu())));
        buttonY += 22;
        addRenderableWidget(new MenuButton(buttonX, buttonY, buttonWidth, "Organization Settings", MenuButton.ButtonType.BUTTON, false, e -> minecraft.setScreen(new PanelOrganizationMenu())));
        buttonY += 22;
        addRenderableWidget(new MenuButton(buttonX, buttonY, buttonWidth, Strings.Gui_Menu_Back, MenuButton.ButtonType.BUTTON, false, e -> PacketHandler.sendToServer(new CSOpenMenu())));
    }

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTicks) {
        super.render(gui, mouseX, mouseY, partialTicks);

        if (menuBox != null) {
            menuBox.renderWidget(gui, mouseX, mouseY, partialTicks);
        }

        int hearts = 0;

        if (minecraft != null && minecraft.player != null) {
            PlayerData playerData = PlayerData.get(minecraft.player);
            hearts = playerData != null ? playerData.getHearts() : 0;
        }

        gui.drawCenteredString(this.font, "Choose what you want to manage", width / 2, (int) (height * 0.17F) + 5, 0xFFAAAAAA);
        gui.drawCenteredString(this.font, "Hearts: " + hearts, width / 2, (int) (height * 0.17F) + 16, 0xFFFFD700);

        for (var renderable : this.renderables) {
            renderable.render(gui, mouseX, mouseY, partialTicks);
        }
    }
}
