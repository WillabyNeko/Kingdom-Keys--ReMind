package online.remind.remind.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.PacketDistributor;
import online.kingdomkeys.kingdomkeys.client.gui.elements.MenuBackground;
import online.kingdomkeys.kingdomkeys.client.gui.elements.MenuBox;
import online.kingdomkeys.kingdomkeys.client.gui.elements.buttons.MenuButton;
import online.kingdomkeys.kingdomkeys.client.sound.ModSounds;
import online.kingdomkeys.kingdomkeys.data.PlayerData;
import online.kingdomkeys.kingdomkeys.network.PacketHandler;
import online.kingdomkeys.kingdomkeys.network.cts.CSSyncAllClientDataPacket;
import online.remind.remind.KingdomKeysReMind;
import online.remind.remind.capabilities.GlobalDataRM;
import online.remind.remind.capabilities.ModDataRM;
import online.remind.remind.network.PacketHandlerRM;
import online.remind.remind.network.PanelPacketAction;
import online.remind.remind.network.cts.CSBoostPacket;
import online.remind.remind.network.cts.CSBuyOrganizationPanelPacket;
import online.remind.remind.network.cts.CSOrganizationPanelPacket;
import online.remind.remind.network.cts.CSPanelPacket;
import online.remind.remind.panels.PanelRegistry;

import java.awt.Color;

public class PanelOrganizationMenu extends MenuBackground {

    private MenuBox box;
    private int boxX;
    private int boxY;
    private int boxW;
    private int boxH;

    public PanelOrganizationMenu() {
        super("Organization Settings", new Color(155, 155, 155));
        minecraft = Minecraft.getInstance();
    }

    @Override
    public void init() {
        super.init();
        this.clearWidgets();

        int topY = (int) (height * 0.17F) + 10;
        this.boxW = Math.min(380, Math.max(220, width - 40));
        this.boxH = Math.min(250, Math.max(190, height - topY - 70));
        this.boxX = (width - boxW) / 2;
        this.boxY = Math.min(topY, Math.max(10, height - boxH - 65));
        this.box = new MenuBox(boxX, boxY, boxW, boxH, 1F, new Color(155, 155, 155));

        GlobalDataRM data = minecraft != null && minecraft.player != null ? ModDataRM.getGlobal(minecraft.player) : null;

        int buttonWidth = Math.min(200, boxW - 40);
        int buttonX = boxX + (boxW - buttonWidth) / 2;
        int y = boxY + 52;

        if (data == null || data.getUnlockedOrganizationPanelSlots() < 120) {
            addRenderableWidget(new MenuButton(buttonX, y, buttonWidth, "Buy Slot Releaser", MenuButton.ButtonType.BUTTON, false, e -> buySlotReleaser()));
            y += 22;
        }

        String boostText = data != null && data.getPanelsEnabled() == 1 ? "Turn Boost OFF" : "Turn Boost ON";
        addRenderableWidget(new MenuButton(buttonX, y, buttonWidth, boostText, MenuButton.ButtonType.BUTTON, false, e -> toggleBoost()));
        y += 22;

        addRenderableWidget(new MenuButton(buttonX, y, buttonWidth, "Unequip All Panels", MenuButton.ButtonType.BUTTON, false, e -> clearPanels()));
        y += 22;

        addRenderableWidget(new MenuButton(buttonX, y, buttonWidth, "Leave Organization", MenuButton.ButtonType.BUTTON, false, e -> leaveOrganization()));
        y += 22;

        addRenderableWidget(new MenuButton(buttonX, y, buttonWidth, "Back", MenuButton.ButtonType.BUTTON, false, e -> minecraft.setScreen(new PanelsMenu())));
    }

    private void buySlotReleaser() {
        if (minecraft == null || minecraft.player == null) {
            return;
        }

        PlayerData playerData = PlayerData.get(minecraft.player);
        GlobalDataRM data = ModDataRM.getGlobal(minecraft.player);

        if (data != null && data.getUnlockedOrganizationPanelSlots() >= 120) {
            minecraft.player.sendSystemMessage(Component.literal("You already have all the Slot Releasers."));
            minecraft.player.playSound(ModSounds.error.get());
            return;
        }

        if (playerData != null && playerData.getHearts() >= 10000) {
            PacketHandlerRM.sendToServer(new CSBuyOrganizationPanelPacket(ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "slot_releaser"), 1));
            PacketHandler.sendToServer(new CSSyncAllClientDataPacket());
            minecraft.player.playSound(ModSounds.itemget.get());
        } else {
            minecraft.player.sendSystemMessage(Component.literal("You do not have enough Hearts to do this."));
            minecraft.player.playSound(ModSounds.error.get());
        }
    }

    private void toggleBoost() {
        if (minecraft == null || minecraft.player == null) {
            return;
        }

        GlobalDataRM data = ModDataRM.getGlobal(minecraft.player);

        if (data != null && data.getPanelsEnabled() == 1) {
            PacketHandlerRM.sendToServer(new CSBoostPacket(2));
        } else {
            PacketHandlerRM.sendToServer(new CSBoostPacket(4));
        }

        PacketHandler.sendToServer(new CSSyncAllClientDataPacket());
        minecraft.player.playSound(ModSounds.menu_select.get());
    }

    private void clearPanels() {
        if (minecraft == null || minecraft.player == null) {
            return;
        }

        PacketDistributor.sendToServer(new CSOrganizationPanelPacket(PanelPacketAction.CLEAR, PanelRegistry.STRENGTH_UNIT, 0, 0));
        minecraft.player.playSound(ModSounds.menu_back.get());
    }

    private void leaveOrganization() {
        if (minecraft == null || minecraft.player == null) {
            return;
        }

        PlayerData playerData = PlayerData.get(minecraft.player);

        if (playerData != null && playerData.getHearts() >= 13000) {
            PacketHandlerRM.sendToServer(new CSPanelPacket(14));
            minecraft.player.playSound(ModSounds.menu_back.get());
            minecraft.setScreen(new PanelsMenu());
        } else {
            minecraft.player.sendSystemMessage(Component.literal("You do not have enough Hearts to do this."));
            minecraft.player.playSound(ModSounds.error.get());
        }
    }

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTicks) {
        super.render(gui, mouseX, mouseY, partialTicks);

        if (box != null) {
            box.renderWidget(gui, mouseX, mouseY, partialTicks);
        }

        if (minecraft != null && minecraft.player != null) {
            PlayerData playerData = PlayerData.get(minecraft.player);
            GlobalDataRM data = ModDataRM.getGlobal(minecraft.player);

            int hearts = playerData != null ? playerData.getHearts() : 0;
            int slots = data != null ? data.getUnlockedOrganizationPanelSlots() : 0;
            boolean enabled = data != null && data.getPanelsEnabled() == 1;

            gui.drawCenteredString(this.font, "Organization Settings", boxX + boxW / 2, boxY + 12, 0xFFFF9900);
            gui.drawCenteredString(this.font, "Hearts: " + hearts + "   Slots: " + slots + "/120   Boost: " + (enabled ? "ON" : "OFF"), boxX + boxW / 2, boxY + 28, 0xFFAAAAAA);
        }

        for (var renderable : this.renderables) {
            renderable.render(gui, mouseX, mouseY, partialTicks);
        }
    }
}
