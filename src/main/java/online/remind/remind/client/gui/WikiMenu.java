package online.remind.remind.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import online.kingdomkeys.kingdomkeys.client.gui.elements.MenuBackground;
import online.kingdomkeys.kingdomkeys.client.gui.elements.MenuColourBox;
import online.kingdomkeys.kingdomkeys.client.gui.elements.buttons.MenuButton;
import online.kingdomkeys.kingdomkeys.client.gui.elements.buttons.MenuScrollBar;
import online.kingdomkeys.kingdomkeys.data.PlayerData;
import online.kingdomkeys.kingdomkeys.lib.Strings;
import online.kingdomkeys.kingdomkeys.network.PacketHandler;
import online.kingdomkeys.kingdomkeys.network.cts.CSOpenMenu;
import online.kingdomkeys.kingdomkeys.util.Utils;
import online.remind.remind.KingdomKeysReMind;
import online.remind.remind.capabilities.GlobalDataRM;
import online.remind.remind.capabilities.ModDataRM;
import online.remind.remind.lib.StringsRM;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.ArrayList;

public class WikiMenu extends MenuBackground {

    public enum Wiki {
        NONE, KEYBLADES, ATTACK, MAGIC, FORMS, ARMOR, ACCESSORIES, ABILITIES, SHOTLOCKS
    }

    MenuScrollBar scrollBar;

   /* public ArrayList<MenuColourBox> keybladesList = new ArrayList<>();
    public ArrayList<MenuColourBox> attackList = new ArrayList<>();
    public ArrayList<MenuColourBox> magicList = new ArrayList<>();
    public ArrayList<MenuColourBox> formsList = new ArrayList<>();
    public ArrayList<MenuColourBox> armorList = new ArrayList<>();
    public ArrayList<MenuColourBox> accessoriesList = new ArrayList<>();
    public ArrayList<MenuColourBox> abilitiesList = new ArrayList<>();
    public ArrayList<MenuColourBox> shotlocksList = new ArrayList<>();
*/
    private Wiki activePage = Wiki.NONE;
    WikiLib wikiLib;
    private GlobalDataRM globalData;

    private MenuButton backButton, attack, magic, forms, armor, accessory, shotlock, keyblades, ability;

    public WikiMenu(String name, Color rgb) {
        super(name, rgb);
    }


    public WikiMenu() {
        super("Journal - Re:Mind", new Color(44, 196, 168));
        minecraft = Minecraft.getInstance();
        globalData = ModDataRM.getGlobal(minecraft.player);
    }

    private void setPage(Wiki page) {
        this.activePage = page;
        items.clear();
    }

    protected void action(String string) {
        switch (string) {
            case "back" -> PacketHandler.sendToServer(new CSOpenMenu());
            case "keyblades" -> setPage(Wiki.KEYBLADES);
            case "attack" -> setPage(Wiki.ATTACK);
            case "magic" -> setPage(Wiki.MAGIC);
            case "ability" -> setPage(Wiki.ABILITIES);
            case "forms" -> setPage(Wiki.FORMS);
            case "armor" -> setPage(Wiki.ARMOR);
            case "accessory" -> setPage(Wiki.ACCESSORIES);
            case "shotlock" -> setPage(Wiki.SHOTLOCKS);
        }
    }

    private void addKeybladeElements() {
        wikiLib.KEYBLADES_HEADER.add();
        wikiLib.SANGUINE_GAZE.add();
        wikiLib.PUREBLOOD.add();
        wikiLib.ELEMENTAL_CRESCENDO.add();
        wikiLib.GAZING_OMEN.add();
        wikiLib.CRYSTALS_LIGHT.add();
        wikiLib.BLITZERS_DREAM.add();
        wikiLib.LEGENDS_FANG.add();
        wikiLib.FIERCE_DEITY_KEY.add();
        wikiLib.WRONGFUL_INHERITOR.add();
    }

    private void addAttackElements() {
        wikiLib.ATTACK_HEADER.add();
        boolean condition = globalData.getLearndedMagics().containsKey((KingdomKeysReMind.MODID + ":" + "attack_quick_blitz"));
        wikiLib.QUICK_BLITZ.setCondition(condition).add();
        condition = globalData.getLearndedMagics().containsKey((KingdomKeysReMind.MODID + ":" + "attack_sliding_dash"));
        wikiLib.SLIDING_DASH.setCondition(condition).add();
        condition = globalData.getLearndedMagics().containsKey((KingdomKeysReMind.MODID + ":" + "attack_fire_surge"));
        wikiLib.FIRE_SURGE.setCondition(condition).add();
        condition = globalData.getLearndedMagics().containsKey((KingdomKeysReMind.MODID + ":" + "attack_thunder_surge"));
        wikiLib.THUNDER_SURGE.setCondition(condition).add();
        condition = globalData.getLearndedMagics().containsKey((KingdomKeysReMind.MODID + ":" + "attack_blizzard_surge"));
        wikiLib.BLIZZARD_SURGE.setCondition(condition).add();
        condition = globalData.getLearndedMagics().containsKey((KingdomKeysReMind.MODID + ":" + "attack_water_surge"));
        wikiLib.WATER_SURGE.setCondition(condition).add();
        condition = globalData.getLearndedMagics().containsKey((KingdomKeysReMind.MODID + ":" + "attack_aero_surge"));
        wikiLib.AERO_SURGE.setCondition(condition).add();
        condition = globalData.getLearndedMagics().containsKey((KingdomKeysReMind.MODID + ":" + "attack_light_surge"));
        wikiLib.LIGHT_SURGE.setCondition(condition).add();
        condition = globalData.getLearndedMagics().containsKey((KingdomKeysReMind.MODID + ":" + "attack_dark_surge"));
        wikiLib.DARK_SURGE.setCondition(condition).add();
        condition = globalData.getLearndedMagics().containsKey((KingdomKeysReMind.MODID + ":" + "attack_zantetsuken"));
        wikiLib.ZANTETSUKEN.setCondition(condition).add();
    }

    private void addMagicElements() {
        wikiLib.MAGICS_HEADER.add();
        boolean condition = globalData.getLearndedMagics().containsKey((KingdomKeysReMind.MODID + ":" + "magic_esuna"));
        wikiLib.ESUNA.setCondition(condition).add();
        condition = globalData.getLearndedMagics().containsKey((KingdomKeysReMind.MODID + ":" + "magic_dispel"));
        wikiLib.DISPEL.setCondition(condition).add();
        condition = globalData.getLearndedMagics().containsKey((KingdomKeysReMind.MODID + ":" + "magic_haste"));
        wikiLib.HASTE.setCondition(condition).add();
        condition = globalData.getLearndedMagics().containsKey((KingdomKeysReMind.MODID + ":" + "magic_slow"));
        wikiLib.SLOW.setCondition(condition).add();
        condition = globalData.getLearndedMagics().containsKey((KingdomKeysReMind.MODID + ":" + "magic_steal"));
        wikiLib.STEAL.setCondition(condition).add();
        condition = globalData.getLearndedMagics().containsKey((KingdomKeysReMind.MODID + ":" + "magic_spark"));
        wikiLib.SPARK.setCondition(condition).add();
        condition = globalData.getLearndedMagics().containsKey((KingdomKeysReMind.MODID + ":" + "magic_berserk"));
        wikiLib.BERSERK.setCondition(condition).add();
        condition = globalData.getLearndedMagics().containsKey((KingdomKeysReMind.MODID + ":" + "magic_drain"));
        wikiLib.DRAIN.setCondition(condition).add();
        condition = globalData.getLearndedMagics().containsKey((KingdomKeysReMind.MODID + ":" + "magic_osmose"));
        wikiLib.OSMOSE.setCondition(condition).add();
        condition = globalData.getLearndedMagics().containsKey((KingdomKeysReMind.MODID + ":" + "magic_silence"));
        wikiLib.SILENCE.setCondition(condition).add();
        condition = globalData.getLearndedMagics().containsKey((KingdomKeysReMind.MODID + ":" + "magic_holy"));
        wikiLib.HOLY.setCondition(condition).add();
        condition = globalData.getLearndedMagics().containsKey((KingdomKeysReMind.MODID + ":" + "magic_ruin"));
        wikiLib.RUIN.setCondition(condition).add();
        condition = globalData.getLearndedMagics().containsKey((KingdomKeysReMind.MODID + ":" + "magic_balloon"));
        wikiLib.BALLOON.setCondition(condition).add();
        condition = globalData.getLearndedMagics().containsKey((KingdomKeysReMind.MODID + ":" + "magic_auto-life"));
        wikiLib.AUTO_LIFE.setCondition(condition).add();
        condition = globalData.getLearndedMagics().containsKey((KingdomKeysReMind.MODID + ":" + "magic_mine_shield"));
        wikiLib.MINE_SHIELD.setCondition(condition).add();
        condition = globalData.getLearndedMagics().containsKey((KingdomKeysReMind.MODID + ":" + "magic_mine_square"));
        wikiLib.MINE_SQUARE.setCondition(condition).add();
        condition = globalData.getLearndedMagics().containsKey((KingdomKeysReMind.MODID + ":" + "magic_regen"));
        wikiLib.REGEN.setCondition(condition).add();
        condition = globalData.getLearndedMagics().containsKey((KingdomKeysReMind.MODID + ":" + "magic_faith"));
        wikiLib.FAITH.setCondition(condition).add();
        condition = globalData.getLearndedMagics().containsKey((KingdomKeysReMind.MODID + ":" + "magic_comet"));
        wikiLib.COMET.setCondition(condition).add();
        condition = globalData.getLearndedMagics().containsKey((KingdomKeysReMind.MODID + ":" + "magic_warp"));
        wikiLib.WARP.setCondition(condition).add();
        condition = globalData.getLearndedMagics().containsKey((KingdomKeysReMind.MODID + ":" + "magic_confuse"));
        wikiLib.CONFUSE.setCondition(condition).add();
        condition = globalData.getLearndedMagics().containsKey((KingdomKeysReMind.MODID + ":" + "magic_ultima"));
        wikiLib.ULTIMA.setCondition(condition).add();
        //TODO: Figure out a fun and balanced way to make Death obtainable outside of commands?
        //condition = globalData.getLearndedMagics().containsKey((KingdomKeysReMind.MODID + ":" + "magic_death"));
        //wikiLib.DEATH.setCondition(condition).add();

    }

    private void addDriveElements() {
        wikiLib.FORMS_HEADER.add();
        boolean condition = playerData.getDriveFormMap().containsKey((KingdomKeysReMind.MODID+":"+ "form_light"));
        wikiLib.LIGHT.setCondition(condition).add();
        condition = playerData.getDriveFormMap().containsKey((KingdomKeysReMind.MODID+":"+ "form_dark"));
        wikiLib.DARK.setCondition(condition).add();
        condition = playerData.getDriveFormMap().containsKey((KingdomKeysReMind.MODID+":"+ "form_rage"));
        wikiLib.RAGE.setCondition(condition).add();
        condition = playerData.getDriveFormMap().containsKey((KingdomKeysReMind.MODID+":"+ "form_twilight"));
        wikiLib.TWILIGHT.setCondition(condition).add();

    }

    int scrollTop, scrollBot;

    @Override
    public void init() {
        super.init();
        this.renderables.clear();
        this.items.clear();

        int topBarHeight = (int) (height * 0.17F);
        int button_statsY = topBarHeight + 5;

        int margin = 20;
        int maxContentWidth = 760;
        int contentWidth = Math.min(maxContentWidth, width - margin *2);
        int contentLeft = (width - contentWidth) / 2;
        int contentRight = (contentLeft + contentWidth);

        float subButtonPosX = (float) width * 0.03F + 10;

        int buttonWidth = Math.min(150, contentWidth) / 2;
        int buttonPosX = contentLeft;

        int contentGap = 25;
        int col1X = buttonPosX + buttonWidth + contentGap;
        int wikiWidth = contentRight - col1X - 20;

        scrollTop = topBarHeight;
        scrollBot = (int) (scrollTop + middleHeight);
        scrollBar = new MenuScrollBar(width - 12, scrollTop, scrollBot, (int) middleHeight, 0, false);

        addRenderableWidget(scrollBar);

        int i = 0;
        addRenderableWidget(backButton = new MenuButton((int) buttonPosX, button_statsY+18*i++, (int) buttonWidth, (Strings.Gui_Menu_Back), MenuButton.ButtonType.BUTTON, false, (e) -> {
            action("back");
        }));

        addRenderableWidget(keyblades = new MenuButton((int) buttonPosX, button_statsY + 18*i++, (int) buttonWidth, (StringsRM.Gui_Menu_Button_Keyblades), MenuButton.ButtonType.BUTTON, true, (e) -> {
            action("keyblades");
        }));

        addRenderableWidget(attack = new MenuButton((int) buttonPosX, button_statsY + 18*i++, (int) buttonWidth, (StringsRM.Gui_Menu_Button_Attack), MenuButton.ButtonType.BUTTON, true, (e) -> {
            action("attack");
        }));

        addRenderableWidget(magic = new MenuButton((int) buttonPosX, button_statsY + 18*i++, (int) buttonWidth, (StringsRM.Gui_Menu_Button_Magic), MenuButton.ButtonType.BUTTON, true, (e) -> {
            action("magic");
        }));
        addRenderableWidget(ability = new MenuButton((int) buttonPosX, button_statsY + 18*i++, (int) buttonWidth, (StringsRM.Gui_Menu_Button_Ability), MenuButton.ButtonType.BUTTON, true, (e) -> {
            action("ability");
        }));
        addRenderableWidget(forms = new MenuButton((int) buttonPosX, button_statsY + 18*i++, (int) buttonWidth, (StringsRM.Gui_Menu_Button_Forms), MenuButton.ButtonType.BUTTON, true, (e) -> {
            action("forms");
        }));
        addRenderableWidget(armor = new MenuButton((int) buttonPosX, button_statsY + 18*i++, (int) buttonWidth, (StringsRM.Gui_Menu_Button_Armor), MenuButton.ButtonType.BUTTON, false, (e) -> {
            action("armor");
        }));
        addRenderableWidget(accessory = new MenuButton((int) buttonPosX, button_statsY + 18*i++, (int) buttonWidth, (StringsRM.Gui_Menu_Button_Accessories), MenuButton.ButtonType.BUTTON, false, (e) -> {
            action("accessory");
        }));
        addRenderableWidget(shotlock = new MenuButton((int) buttonPosX, button_statsY + 18*i++, (int) buttonWidth, (StringsRM.Gui_Menu_Button_Shotlocks), MenuButton.ButtonType.BUTTON, true, (e) -> {
            action("shotlock");
        }));

        playerData = PlayerData.get(minecraft.player);
        globalData = ModDataRM.getGlobal(minecraft.player);
        wikiLib = new WikiLib(col1X, wikiWidth);

        //Just in case we clear them even tho they should be empty when creating the instance
        wikiLib.keybladesList.clear();
        wikiLib.attackList.clear();
        wikiLib.magicList.clear();
        wikiLib.abilitiesList.clear();
        wikiLib.formsList.clear();
        wikiLib.armorList.clear();
        wikiLib.accessoriesList.clear();
        wikiLib.shotlocksList.clear();

        addKeybladeElements();
        addAttackElements();
        addMagicElements();
        addDriveElements();
    }

    ArrayList<MenuColourBox> items = new ArrayList<>();

    @Override
    public void render(@NotNull GuiGraphics gui, int mouseX, int mouseY, float partialTicks) {
        super.render(gui, mouseX, mouseY, partialTicks);

        items = (ArrayList<MenuColourBox>) getListFromPage().clone();

        for (int i = 0; i < items.size(); i += 2) {
            //Left col
            if (items.get(i) != null) {
                items.get(i).visible = true;
                items.get(i).active = false;
                items.get(i).setY((int) (topBarHeight) + (i) * 7 + 2);
            }
            if (i + 1 < items.size()) {
                if (items.get(i + 1) != null) {
                    items.get(i + 1).visible = true;
                    items.get(i + 1).active = false;
                    items.get(i + 1).setY((int) (topBarHeight) + (i) * 7 + 2);
                }
            }
        }
        if (!items.isEmpty()) {
            int listHeight = (items.get(items.size() - 1).getY() + 20) - items.get(0).getY() + 3;
            scrollBar.setContentHeight(listHeight);
        }

        gui.enableScissor(0, (int) topBarHeight, width, (int) (topBarHeight + middleHeight));
        for (MenuColourBox item : items) {
            if (item != null) {
                item.setY((int) (item.getY() - scrollBar.scrollOffset));
                if (item.getY() < scrollBar.getBottom() && item.getY() >= scrollBar.getY() - 20) {
                    item.active = true;
                    item.render(gui, mouseX, mouseY, partialTicks);
                }
            }
        }
        gui.disableScissor();

    }

    private ArrayList<MenuColourBox> getListFromPage() {
        return switch (activePage) {
            case NONE -> new ArrayList();
            case KEYBLADES -> wikiLib.keybladesList;
            case ATTACK -> wikiLib.attackList;
            case MAGIC -> wikiLib.magicList;
            case FORMS -> wikiLib.formsList;
            case ARMOR -> wikiLib.armorList;
            case ACCESSORIES -> wikiLib.accessoriesList;
            case ABILITIES -> wikiLib.abilitiesList;
            case SHOTLOCKS -> wikiLib.shotlocksList;
        };
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        scrollBar.mouseClicked(mouseX, mouseY, mouseButton);
        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public boolean mouseReleased(double pMouseX, double pMouseY, int pButton) {
        scrollBar.mouseReleased(pMouseX, pMouseY, pButton);

        return super.mouseReleased(pMouseX, pMouseY, pButton);
    }

    @Override
    public boolean mouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY) {
        scrollBar.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY);

        return super.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double deltaX, double deltaY) {
        scrollBar.mouseScrolled(mouseX, mouseY, deltaX, deltaY);
        return false;
    }
}