package online.remind.remind.client.gui;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import online.kingdomkeys.kingdomkeys.client.gui.elements.MenuBackground;
import online.kingdomkeys.kingdomkeys.client.gui.elements.MenuColourBox;
import online.kingdomkeys.kingdomkeys.client.gui.elements.buttons.MenuButton;
import online.kingdomkeys.kingdomkeys.data.PlayerData;
import online.kingdomkeys.kingdomkeys.item.KKAccessoryItem;
import online.kingdomkeys.kingdomkeys.item.KKArmorItem;
import online.kingdomkeys.kingdomkeys.lib.Strings;
import online.kingdomkeys.kingdomkeys.network.PacketHandler;
import online.kingdomkeys.kingdomkeys.network.cts.CSOpenMenu;
import online.kingdomkeys.kingdomkeys.util.Utils;
import online.remind.remind.capabilities.GlobalDataRM;
import online.remind.remind.capabilities.ModDataRM;
import online.remind.remind.config.ModConfigs;
import online.remind.remind.lib.StringsRM;
import online.remind.remind.network.PacketHandlerRM;
import online.remind.remind.network.cts.CSBoostPacket;
import online.remind.remind.network.cts.CSPrestigePacket;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class PrestigeMenu extends MenuBackground {


    public int slot = -1;

    public Map<KKArmorItem, Integer> addedArmorList = new HashMap<KKArmorItem, Integer>();
    public Map<KKAccessoryItem, Integer> addedAccessoryList = new HashMap<KKAccessoryItem, Integer>();

    private MenuButton backButton, prestige, levelReq, toggleOff, toggleOn;

    MenuColourBox level, prestigeLevel, gainedHP, gainedMP, gainedSTR, gainedMAG, gainedDEF, currentPath, warriorPath, mysticPath, guardianPath;

    MenuColourBox[] playerWidgets = {level, prestigeLevel, gainedHP, gainedMP, gainedSTR, gainedMAG, gainedDEF, currentPath, warriorPath, mysticPath, guardianPath};


    public PrestigeMenu() {
        super("New Game +", new Color(248, 225, 81));
        minecraft = Minecraft.getInstance();
        this.slot = slot;
    }

    protected void action(String string) {
        if (string.equals("back"))
            PacketHandler.sendToServer(new CSOpenMenu());
        if (string.equals("confirm")) {
            PacketHandlerRM.sendToServer(new CSPrestigePacket());
            minecraft.setScreen(null);

        }
        if (string.equals("toggleOff")) {
            PacketHandlerRM.sendToServer(new CSBoostPacket(1));
            PacketHandler.sendToServer(new CSOpenMenu());
        }
        if (string.equals("toggleOn")) {
            PacketHandlerRM.sendToServer(new CSBoostPacket(3));
            PacketHandler.sendToServer(new CSOpenMenu());
        }
    }



    @Override
    public void init() {
        final PlayerData playerData = PlayerData.get(minecraft.player);
        GlobalDataRM addedData = ModDataRM.getGlobal(minecraft.player);

        super.init();
        this.renderables.clear();

        int topBarHeight = (int) (height * 0.17F);
        int button_statsY = topBarHeight + 5;

        int margin = 20;
        int maxContentWidth = 760;
        int contentWidth = Math.min(maxContentWidth, width - margin * 2);
        int contentLeft = (width - contentWidth) / 2;

        int sidebarWidth = Math.min(150, contentWidth / 4);
        int sidebarGap = 20;
        int columnGap = 10;

        int buttonPosX = contentLeft;
        int buttonWidth = sidebarWidth;

        int columnsLeft = buttonPosX + buttonWidth + sidebarGap;
        int columnsWidth = contentWidth - buttonWidth - sidebarGap;
        int columnWidth = (columnsWidth - columnGap) / 2;

        int col1X = columnsLeft;
        int col2X = col1X + columnWidth + columnGap;

        addRenderableWidget(backButton = new MenuButton(buttonPosX, button_statsY + 40, buttonWidth, Strings.Gui_Menu_Back, MenuButton.ButtonType.BUTTON, false, e -> action("back")));

        if (playerData.getLevel() == 100) {
            addRenderableWidget(prestige = new MenuButton(buttonPosX, button_statsY, buttonWidth, StringsRM.Gui_Menu_Button_PrestigeConfirm, MenuButton.ButtonType.BUTTON, true, e -> action("confirm")));
        } else {
            addRenderableWidget(levelReq = new MenuButton(buttonPosX, button_statsY, buttonWidth, "Levels Until NG+: " + (100 - playerData.getLevel()), MenuButton.ButtonType.BUTTON, false, e -> action("prestige")));
        }

        if (addedData.getNGPEnabled() == 1) {
            addRenderableWidget(toggleOff = new MenuButton(buttonPosX, button_statsY + 20, buttonWidth, "Toggle OFF", MenuButton.ButtonType.BUTTON, false, e -> action("toggleOff")));
        } else if (addedData.getNGPEnabled() == 0) {
            addRenderableWidget(toggleOn = new MenuButton(buttonPosX, button_statsY + 20, buttonWidth, "Toggle ON", MenuButton.ButtonType.BUTTON, false, e -> action("toggleOn")));
        }

        int c = 0;
        int d = 0;
        int spacer = 14;

        addRenderableWidget(level = new MenuColourBox(col1X, button_statsY + (c++ * spacer), columnWidth, Utils.translateToLocal(Strings.Gui_Menu_Status_Level), "" + playerData.getLevel(), 0x000088));
        addRenderableWidget(prestigeLevel = new MenuColourBox(col1X, button_statsY + (c++ * spacer), columnWidth, Utils.translateToLocal(StringsRM.Gui_Menu_Button_PrestigeLevel), "" + addedData.getPrestigeLvl(), 0xe3ce44));
        addRenderableWidget(currentPath = new MenuColourBox(col1X, button_statsY + (c++ * spacer), columnWidth, Utils.translateToLocal("Current Path: "), "" + playerData.getChosen(), 0xe3ce44));
        addRenderableWidget(warriorPath = new MenuColourBox(col1X, button_statsY + (c++ * spacer), columnWidth, Utils.translateToLocal("NG+ 🗡 Count: "), "" + addedData.getNGPWarriorCount(), 0xe3ce44));
        addRenderableWidget(mysticPath = new MenuColourBox(col1X, button_statsY + (c++ * spacer), columnWidth, Utils.translateToLocal("NG+ ⚚ Count: "), "" + addedData.getNGPMysticCount(), 0xe3ce44));
        addRenderableWidget(guardianPath = new MenuColourBox(col1X, button_statsY + (c++ * spacer), columnWidth, Utils.translateToLocal("NG+ 🛡 Count: "), "" + addedData.getNGPGuardianCount(), 0xe3ce44));

        addRenderableWidget(gainedHP = new MenuColourBox(col2X, button_statsY + (d++ * spacer), columnWidth, Utils.translateToLocal("Gained Max HP: "), "" + addedData.getPrestigeLvl() * 2 + " / " + ChatFormatting.GOLD + ModConfigs.hpCap, 0x3ECE44));
        addRenderableWidget(gainedMP = new MenuColourBox(col2X, button_statsY + (d++ * spacer), columnWidth, Utils.translateToLocal("Gained Max MP: "), "" + addedData.getPrestigeLvl() * 2 + " / " + ChatFormatting.GOLD + ModConfigs.mpCap, 0x3ECE44));
        addRenderableWidget(gainedSTR = new MenuColourBox(col2X, button_statsY + (d++ * spacer), columnWidth, Utils.translateToLocal("Gained STR: "), "" + addedData.getSTRBonus() + " / " + ChatFormatting.GOLD + ModConfigs.statCap, 0xaa190f));
        addRenderableWidget(gainedMAG = new MenuColourBox(col2X, button_statsY + (d++ * spacer), columnWidth, Utils.translateToLocal("Gained MAG: "), "" + addedData.getMAGBonus() + " / " + ChatFormatting.GOLD + ModConfigs.statCap, 0xaa190f));
        addRenderableWidget(gainedDEF = new MenuColourBox(col2X, button_statsY + (d++ * spacer), columnWidth, Utils.translateToLocal("Gained DEF: "), "" + addedData.getDEFBonus() + " / " + ChatFormatting.GOLD + ModConfigs.statCap, 0xaa190f));
    }


}
