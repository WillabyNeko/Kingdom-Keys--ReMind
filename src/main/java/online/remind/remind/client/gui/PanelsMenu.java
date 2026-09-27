package online.remind.remind.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;
import online.kingdomkeys.kingdomkeys.client.gui.elements.MenuBackground;
import online.kingdomkeys.kingdomkeys.client.gui.elements.MenuBox;
import online.kingdomkeys.kingdomkeys.client.gui.elements.MenuColourBox;
import online.kingdomkeys.kingdomkeys.client.gui.elements.buttons.MenuButton;
import online.kingdomkeys.kingdomkeys.client.gui.elements.buttons.MenuScrollBar;
import online.kingdomkeys.kingdomkeys.client.sound.ModSounds;
import online.kingdomkeys.kingdomkeys.data.PlayerData;
import online.kingdomkeys.kingdomkeys.lib.Strings;
import online.kingdomkeys.kingdomkeys.network.PacketHandler;
import online.kingdomkeys.kingdomkeys.network.cts.CSOpenMenu;
import online.kingdomkeys.kingdomkeys.network.cts.CSSyncAllClientDataPacket;
import online.kingdomkeys.kingdomkeys.network.stc.SCSyncPlayerData;
import online.kingdomkeys.kingdomkeys.util.Utils;
import online.remind.remind.KingdomKeysReMind;
import online.remind.remind.capabilities.GlobalDataRM;
import online.remind.remind.capabilities.ModDataRM;
import online.remind.remind.network.PacketHandlerRM;
import online.remind.remind.network.PanelPacketAction;
import online.remind.remind.network.cts.CSBoostPacket;
import online.remind.remind.network.cts.CSBuyOrganizationPanelPacket;
import online.remind.remind.network.cts.CSOrganizationPanelPacket;
import online.remind.remind.network.cts.CSPanelPacket;
import online.remind.remind.panels.*;

import java.awt.*;

public class PanelsMenu extends MenuBackground {


	private static final int BASE_ORG_SLOT_SIZE = 18;

	private static final int BASE_ORG_PANEL_PICKER_SLOT_SIZE = 22;
	private static final int BASE_ORG_PANEL_PICKER_GAP = 4;
	private static final int BASE_ORG_PANEL_PICKER_ROW_GAP = 5;

	private static final int BASE_SHOP_VISIBLE_BUTTONS = 5;
	private static final int BASE_SHOP_BUTTON_GAP = 16;

	private static final int ORG_PANEL_PICKER_COLUMNS = 7;
	private static final int ORG_INVENTORY_ROW_HEIGHT = 16;
	private static final int SHOP_ROW_HEIGHT = 16;
	private static final ResourceLocation[] ORG_PICKER_PANELS = new ResourceLocation[]{PanelRegistry.STRENGTH_UNIT, PanelRegistry.MAGIC_UNIT, PanelRegistry.DEFENSE_UNIT, PanelRegistry.AP_UNIT, PanelRegistry.SIGHT_UNIT, PanelRegistry.LEVEL_UP,
			PanelRegistry.STRENGTH_UNIT_L, PanelRegistry.MAGIC_UNIT_L, PanelRegistry.DEFENSE_UNIT_L, PanelRegistry.AP_UNIT_L,
			PanelRegistry.LEVEL_DOUBLER_L_RIGHT, PanelRegistry.LEVEL_DOUBLER_L_LEFT, PanelRegistry.LEVEL_DOUBLER_L_TOP_RIGHT, PanelRegistry.LEVEL_DOUBLER_L_TOP_LEFT, PanelRegistry.LEVEL_DOUBLER_LINE,
			PanelRegistry.POWER_LINK, PanelRegistry.MAGIC_LINK, PanelRegistry.GUARD_LINK, PanelRegistry.LEVEL_LINK,
			PanelRegistry.HEARTS_POWER_PANEL,
			PanelRegistry.ULTIMA_WEAPON_PANEL,
			PanelRegistry.HIGH_JUMP_PANEL, PanelRegistry.DODGE_ROLL_PANEL, PanelRegistry.AERIAL_DODGE_PANEL, PanelRegistry.QUICK_RUN_PANEL, PanelRegistry.GLIDE_PANEL,
			PanelRegistry.COMBO_PLUS_PANEL, PanelRegistry.HASTE_PANEL, PanelRegistry.FIRE_BOOST_PANEL, PanelRegistry.BLIZZARD_BOOST_PANEL, PanelRegistry.THUNDER_BOOST_PANEL, PanelRegistry.WATER_BOOST_PANEL, PanelRegistry.LIGHT_BOOST_PANEL, PanelRegistry.DARK_BOOST_PANEL, PanelRegistry.DRAW_PANEL, PanelRegistry.JACKPOT_PANEL, PanelRegistry.LUCKY_LUCKY_PANEL,};
	// ------------- Pretty Stuff ----------------------
	MenuBox box;
	MenuBox shopBox;
	MenuBox editorBox;
	MenuBox detailBox;
	int ticks = 0;
	MenuColourBox str, mag, def, ap;
	MenuColourBox[] playerWidgets = {str, mag, def, ap};
	private int orgSlotSize = BASE_ORG_SLOT_SIZE;
	private int orgPickerSlotSize = BASE_ORG_PANEL_PICKER_SLOT_SIZE;
	private int orgPickerGap = BASE_ORG_PANEL_PICKER_GAP;
	private int orgPickerRowGap = BASE_ORG_PANEL_PICKER_ROW_GAP;
	private int shopVisibleButtons = BASE_SHOP_VISIBLE_BUTTONS;
	private int shopButtonGap = BASE_SHOP_BUTTON_GAP;
	private int orgPickerColumns = ORG_PANEL_PICKER_COLUMNS;
	private boolean compactPanelLayout = false;
	private boolean emergencyPanelLayout = false;
	private int shopScrollOffset = 0;
	private int orgPickerX;
	private int orgPickerY;
	private int orgControlsX;
	private int orgControlsY;
	private int orgPanelAreaX;
	private int orgPanelAreaY;
	private int orgPanelAreaWidth;
	private int orgPanelAreaHeight;
	private int shopBoxX;
	private int shopBoxY;
	private int shopBoxW;
	private int shopBoxH;
	private int editorBoxX;
	private int editorBoxY;
	private int editorBoxW;
	private int editorBoxH;
	private int detailBoxX;
	private int detailBoxY;
	private int detailBoxW;
	private int detailBoxH;
	private MenuScrollBar orgInventoryScrollBar;
	private int orgInventoryListX;
	private int orgInventoryListY;
	private int orgInventoryListW;
	private int orgInventoryListH;
	private MenuScrollBar shopScrollBar;
	private int shopListX;
	private int shopListY;
	private int shopListW;
	private int shopListH;
	private ResourceLocation selectedOrgPanel = PanelRegistry.STRENGTH_UNIT; // DUMMY
	private int orgGridX;
	private int orgGridY;
	private int shopInfoBoxY;
	private int shopPanelX;
	private int shopPanelWidth;
	private int lastKnownHearts = -1;
	private MenuButton backButton, strUp, magUp, defUp, apUp, giveAbility, lvl, req0, valorUp, wisdomUp, limitUp, masterUp, finalUp, reqV, reqW, reqL, reqM, reqF, armorUp, accessoryUp, rejectOrg, reset, toggleOff, toggleOn, orgPlaceSTR, orgPlaceMAG, orgPlaceDEF, orgPlaceAP, orgPlaceLV, orgRemove00, orgClear;

	private enum PanelCategory {
		STATS,
		GROWTH,
		ABILITIES,
		BOOSTS,
		LINKS,
		SPECIAL
	}

	private record PanelMenuEntry(
			ResourceLocation panelId,
			String label,
			String shortLabel,
			String description,
			int cost,
			PanelCategory category
	) {
	}

	private static final PanelMenuEntry[] PANEL_ENTRIES = new PanelMenuEntry[] {
			new PanelMenuEntry(PanelRegistry.STRENGTH_UNIT, "STR Unit", "S", "+1 STR while placed", 1000, PanelCategory.STATS),
			new PanelMenuEntry(PanelRegistry.MAGIC_UNIT, "MAG Unit", "M", "+1 MAG while placed", 1000, PanelCategory.STATS),
			new PanelMenuEntry(PanelRegistry.DEFENSE_UNIT, "DEF Unit", "D", "+1 DEF while placed", 1000, PanelCategory.STATS),
			new PanelMenuEntry(PanelRegistry.AP_UNIT, "AP Unit", "AP", "+2 AP while placed", 500, PanelCategory.STATS),
			new PanelMenuEntry(PanelRegistry.LEVEL_UP, "Level Up", "LV", "+1 LV while placed", 2000, PanelCategory.STATS),

			new PanelMenuEntry(PanelRegistry.STRENGTH_UNIT_L, "STR Unit L", "S+", "+3 STR while placed", 2000, PanelCategory.STATS),
			new PanelMenuEntry(PanelRegistry.MAGIC_UNIT_L, "MAG Unit L", "M+", "+3 MAG while placed", 2000, PanelCategory.STATS),
			new PanelMenuEntry(PanelRegistry.DEFENSE_UNIT_L, "DEF Unit L", "D+", "+3 DEF while placed", 2000, PanelCategory.STATS),
			new PanelMenuEntry(PanelRegistry.AP_UNIT_L, "AP Unit L", "AP+", "+5 AP while placed", 1000, PanelCategory.STATS),
			//new PanelMenuEntry(PanelRegistry.LEVEL_DOUBLER, "Level Doubler", "LV2", "+2 LV while placed", 4000, PanelCategory.STATS),

			new PanelMenuEntry(PanelRegistry.POWER_LINK, "Power Link", "P-L", "+1 STR for each adjacent STR panel", 2500, PanelCategory.LINKS),
			new PanelMenuEntry(PanelRegistry.MAGIC_LINK, "Magic Link", "M-L", "+1 MAG for each adjacent MAG panel", 2500, PanelCategory.LINKS),
			new PanelMenuEntry(PanelRegistry.GUARD_LINK, "Guard Link", "G-L", "+1 DEF for each adjacent DEF panel", 2500, PanelCategory.LINKS),
			//new PanelMenuEntry(PanelRegistry.LEVEL_LINK, "Level Link", "L-L", "+1 LV for each adjacent LV panel", 4500, PanelCategory.LINKS),

			new PanelMenuEntry(PanelRegistry.HEARTS_POWER_PANEL, "Hearts Are Power", "♡", "Enables Hearts Are Power while equipped", 50000, PanelCategory.SPECIAL),
			new PanelMenuEntry(PanelRegistry.ULTIMA_WEAPON_PANEL, "Ultima Weapon", "UW", "Enables Ultima Weapon while equipped", 50000, PanelCategory.SPECIAL),

			new PanelMenuEntry(PanelRegistry.HIGH_JUMP_PANEL, "High Jump", "HJ", "Enables High Jump while equipped", 2500, PanelCategory.GROWTH),
			new PanelMenuEntry(PanelRegistry.DODGE_ROLL_PANEL, "Dodge Roll", "DR", "Enables Dodge Roll while equipped", 2500, PanelCategory.GROWTH),
			new PanelMenuEntry(PanelRegistry.AERIAL_DODGE_PANEL, "Aerial Dodge", "AD", "Enables Aerial Dodge while equipped", 3000, PanelCategory.GROWTH),
			new PanelMenuEntry(PanelRegistry.QUICK_RUN_PANEL, "Quick Run", "QR", "Enables Quick Run while equipped", 3000, PanelCategory.GROWTH),
			new PanelMenuEntry(PanelRegistry.GLIDE_PANEL, "Glide", "GL", "Enables Glide while equipped", 4000, PanelCategory.GROWTH),

			new PanelMenuEntry(PanelRegistry.COMBO_PLUS_PANEL, "Combo Plus", "C+", "+1 Combo Plus while equipped", 2500, PanelCategory.ABILITIES),
			new PanelMenuEntry(PanelRegistry.FIRE_BOOST_PANEL, "Fire Boost", "FB", "+1 Fire Boost while equipped", 3000, PanelCategory.BOOSTS),
			new PanelMenuEntry(PanelRegistry.BLIZZARD_BOOST_PANEL, "Blizzard Boost", "BB", "+1 Blizzard Boost while equipped", 3000, PanelCategory.BOOSTS),
			new PanelMenuEntry(PanelRegistry.THUNDER_BOOST_PANEL, "Thunder Boost", "TB", "+1 Thunder Boost while equipped", 3000, PanelCategory.BOOSTS),
			new PanelMenuEntry(PanelRegistry.DRAW_PANEL, "Draw", "DRW", "+1 Draw while equipped", 2000, PanelCategory.BOOSTS),
			new PanelMenuEntry(PanelRegistry.JACKPOT_PANEL, "Jackpot", "JP", "+1 Jackpot while equipped", 2500, PanelCategory.BOOSTS),
			new PanelMenuEntry(PanelRegistry.LUCKY_LUCKY_PANEL, "Lucky Lucky", "LL", "+1 Lucky Lucky while equipped", 4000, PanelCategory.BOOSTS)
	};


	public PanelsMenu(String name, Color rgb) {
		super(name, rgb);
	}

	public PanelsMenu() {
		super("Panel System", new Color(154, 154, 154));
		minecraft = Minecraft.getInstance();
	}

	private int getCurrentHearts() {
		if (minecraft == null || minecraft.player == null) {
			return -1;
		}

		PlayerData playerData = PlayerData.get(minecraft.player);

		if (playerData == null) {
			return -1;
		}

		return playerData.getHearts();
	}

	public void reloadMenu() {
		GUIHelperRM.openPanelMenu();
	}

	protected void action(String string) {
		PlayerData playerData = PlayerData.get(minecraft.player);
		GlobalDataRM globalData = ModDataRM.getGlobal(minecraft.player);

		switch (string) {
			case "back" -> PacketHandler.sendToServer(new CSOpenMenu());

			case "reg" -> {
				minecraft.setScreen(new PanelsMenu());
				minecraft.player.playSound(ModSounds.error.get());
			}
			case "buy_str_unit" -> {
				PacketHandlerRM.sendToServer(new CSBuyOrganizationPanelPacket(PanelRegistry.STRENGTH_UNIT, 1));
				PacketHandler.sendToServer(new CSSyncAllClientDataPacket());
				minecraft.player.playSound(ModSounds.itemget.get());
			}

			case "buy_mag_unit" -> {
				PacketHandlerRM.sendToServer(new CSBuyOrganizationPanelPacket(PanelRegistry.MAGIC_UNIT, 1));
				PacketHandler.sendToServer(new CSSyncAllClientDataPacket());
				minecraft.player.playSound(ModSounds.itemget.get());
			}

			case "buy_def_unit" -> {
				PacketHandlerRM.sendToServer(new CSBuyOrganizationPanelPacket(PanelRegistry.DEFENSE_UNIT, 1));
				PacketHandler.sendToServer(new CSSyncAllClientDataPacket());
				minecraft.player.playSound(ModSounds.itemget.get());
			}

			case "buy_ap_unit" -> {
				PacketHandlerRM.sendToServer(new CSBuyOrganizationPanelPacket(PanelRegistry.AP_UNIT, 1));
				PacketHandler.sendToServer(new CSSyncAllClientDataPacket());
				minecraft.player.playSound(ModSounds.itemget.get());
			}

			case "buy_level_up" -> {
				PacketHandlerRM.sendToServer(new CSBuyOrganizationPanelPacket(PanelRegistry.LEVEL_UP, 1));
				PacketHandler.sendToServer(new CSSyncAllClientDataPacket());
				minecraft.player.playSound(ModSounds.itemget.get());
			}

			case "buy_slot_releaser" -> {
				if (globalData.getUnlockedOrganizationPanelSlots() < 120) {
					if (playerData.getHearts() >= 10000) {
						PacketHandlerRM.sendToServer(new CSBuyOrganizationPanelPacket(ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "slot_releaser"), 1));
						PacketHandler.sendToServer(new CSSyncAllClientDataPacket());
						minecraft.player.playSound(ModSounds.itemget.get());
					} else {
						player.sendSystemMessage(Component.literal("You do not have enough Hearts to do this."));
						player.playSound(ModSounds.error.get());
					}
				} else {
					player.sendSystemMessage(Component.literal("You already have all the Slot Releasers."));
					player.playSound(ModSounds.error.get());
				}
			}
			case "buy_str_unit_l" -> {
				PacketHandlerRM.sendToServer(new CSBuyOrganizationPanelPacket(PanelRegistry.STRENGTH_UNIT_L, 1));
				PacketHandler.sendToServer(new CSSyncAllClientDataPacket());
				minecraft.player.playSound(ModSounds.itemget.get());
			}

			case "buy_mag_unit_l" -> {
				PacketHandlerRM.sendToServer(new CSBuyOrganizationPanelPacket(PanelRegistry.MAGIC_UNIT_L, 1));
				PacketHandler.sendToServer(new CSSyncAllClientDataPacket());
				minecraft.player.playSound(ModSounds.itemget.get());
			}

			case "buy_def_unit_l" -> {
				PacketHandlerRM.sendToServer(new CSBuyOrganizationPanelPacket(PanelRegistry.DEFENSE_UNIT_L, 1));
				PacketHandler.sendToServer(new CSSyncAllClientDataPacket());
				minecraft.player.playSound(ModSounds.itemget.get());
			}

			case "buy_ap_unit_l" -> {
				PacketHandlerRM.sendToServer(new CSBuyOrganizationPanelPacket(PanelRegistry.AP_UNIT_L, 1));
				PacketHandler.sendToServer(new CSSyncAllClientDataPacket());
				minecraft.player.playSound(ModSounds.itemget.get());
			}

			case "buy_level_doubler" -> {
				PacketHandlerRM.sendToServer(new CSBuyOrganizationPanelPacket(PanelRegistry.LEVEL_DOUBLER, 1));
				PacketHandler.sendToServer(new CSSyncAllClientDataPacket());
				minecraft.player.playSound(ModSounds.itemget.get());
			}

			case "buy_power_link" -> {
				PacketHandlerRM.sendToServer(new CSBuyOrganizationPanelPacket(PanelRegistry.POWER_LINK, 1));
				PacketHandler.sendToServer(new CSSyncAllClientDataPacket());
				minecraft.player.playSound(ModSounds.itemget.get());
			}

			case "buy_magic_link" -> {
				PacketHandlerRM.sendToServer(new CSBuyOrganizationPanelPacket(PanelRegistry.MAGIC_LINK, 1));
				PacketHandler.sendToServer(new CSSyncAllClientDataPacket());
				minecraft.player.playSound(ModSounds.itemget.get());
			}

			case "buy_guard_link" -> {
				PacketHandlerRM.sendToServer(new CSBuyOrganizationPanelPacket(PanelRegistry.GUARD_LINK, 1));
				PacketHandler.sendToServer(new CSSyncAllClientDataPacket());
				minecraft.player.playSound(ModSounds.itemget.get());
			}

			case "buy_level_link" -> {
				PacketHandlerRM.sendToServer(new CSBuyOrganizationPanelPacket(PanelRegistry.LEVEL_LINK, 1));
				PacketHandler.sendToServer(new CSSyncAllClientDataPacket());
				minecraft.player.playSound(ModSounds.itemget.get());
			}

			// Abilities

			case "buy_hearts_power_panel" -> {
				PacketHandlerRM.sendToServer(new CSBuyOrganizationPanelPacket(PanelRegistry.HEARTS_POWER_PANEL, 1));
				PacketHandler.sendToServer(new CSSyncAllClientDataPacket());
				minecraft.player.playSound(ModSounds.itemget.get());
			}


			case "buy_ultima_weapon_panel" -> {
				PacketHandlerRM.sendToServer(new CSBuyOrganizationPanelPacket(PanelRegistry.ULTIMA_WEAPON_PANEL, 1));
				PacketHandler.sendToServer(new CSSyncAllClientDataPacket());
				minecraft.player.playSound(ModSounds.itemget.get());
			}

			case "buy_high_jump_panel" -> {
				PacketHandlerRM.sendToServer(new CSBuyOrganizationPanelPacket(PanelRegistry.HIGH_JUMP_PANEL, 1));
				PacketHandler.sendToServer(new CSSyncAllClientDataPacket());
				minecraft.player.playSound(ModSounds.itemget.get());
			}

			case "buy_dodge_roll_panel" -> {
				PacketHandlerRM.sendToServer(new CSBuyOrganizationPanelPacket(PanelRegistry.DODGE_ROLL_PANEL, 1));
				PacketHandler.sendToServer(new CSSyncAllClientDataPacket());
				minecraft.player.playSound(ModSounds.itemget.get());
			}

			case "buy_aerial_dodge_panel" -> {
				PacketHandlerRM.sendToServer(new CSBuyOrganizationPanelPacket(PanelRegistry.AERIAL_DODGE_PANEL, 1));
				PacketHandler.sendToServer(new CSSyncAllClientDataPacket());
				minecraft.player.playSound(ModSounds.itemget.get());
			}

			case "buy_quick_run_panel" -> {
				PacketHandlerRM.sendToServer(new CSBuyOrganizationPanelPacket(PanelRegistry.QUICK_RUN_PANEL, 1));
				PacketHandler.sendToServer(new CSSyncAllClientDataPacket());
				minecraft.player.playSound(ModSounds.itemget.get());
			}

			case "buy_glide_panel" -> {
				PacketHandlerRM.sendToServer(new CSBuyOrganizationPanelPacket(PanelRegistry.GLIDE_PANEL, 1));
				PacketHandler.sendToServer(new CSSyncAllClientDataPacket());
				minecraft.player.playSound(ModSounds.itemget.get());
			}

			// Forms

			case "valorUp" -> {
				PacketHandlerRM.sendToServer(new CSPanelPacket(5));
				PacketHandler.sendToServer(new CSSyncAllClientDataPacket());
				minecraft.player.playSound(ModSounds.itemget.get());
				this.reloadMenu();
			}
			case "wisdomUp" -> {
				PacketHandlerRM.sendToServer(new CSPanelPacket(6));
				PacketHandler.sendToServer(new CSSyncAllClientDataPacket());
				minecraft.player.playSound(ModSounds.itemget.get());
				this.reloadMenu();
			}
			case "limitUp" -> {
				PacketHandlerRM.sendToServer(new CSPanelPacket(7));
				PacketHandler.sendToServer(new CSSyncAllClientDataPacket());
				minecraft.player.playSound(ModSounds.itemget.get());
				this.reloadMenu();
			}
			case "masterUp" -> {
				PacketHandlerRM.sendToServer(new CSPanelPacket(8));
				PacketHandler.sendToServer(new CSSyncAllClientDataPacket());
				minecraft.player.playSound(ModSounds.itemget.get());
				this.reloadMenu();
			}
			case "finalUp" -> {
				PacketHandlerRM.sendToServer(new CSPanelPacket(9));
				PacketHandler.sendToServer(new CSSyncAllClientDataPacket());
				minecraft.player.playSound(ModSounds.itemget.get());
				this.reloadMenu();
			}
			case "lvl" -> {
				PacketHandlerRM.sendToServer(new CSPanelPacket(10));
				PacketHandler.sendToServer(new CSSyncAllClientDataPacket());
				this.reloadMenu();
			}
			case "armorUp" -> {
				PacketHandlerRM.sendToServer(new CSPanelPacket(12));
				PacketHandler.sendToServer(new CSSyncAllClientDataPacket());
				init();
			}
			case "accessoryUp" -> {
				PacketHandlerRM.sendToServer(new CSPanelPacket(13));
				PacketHandler.sendToServer(new CSSyncAllClientDataPacket());
				init();
			}
			case "rejectOrg" -> {
				if (playerData.getHearts() >= 13000) {
					PacketHandlerRM.sendToServer(new CSPanelPacket(14));
					//PacketHandler.sendToServer(new CSSyncAllClientDataPacket());
					PacketHandler.sendToServer(new CSOpenMenu());
				} else {
					player.sendSystemMessage(Component.literal("You do not have enough Hearts to do this."));
					player.playSound(ModSounds.error.get());
				}
			}
			case "reset" -> {
				PacketHandlerRM.sendToServer(new CSPanelPacket(11));
				PacketHandler.sendToServer(new CSSyncAllClientDataPacket());
				PacketHandler.sendToServer(new CSOpenMenu());
			}
			case "toggleOff" -> {
				PacketHandlerRM.sendToServer(new CSBoostPacket(2));
				PacketHandler.sendToServer(new CSSyncAllClientDataPacket());
				PacketHandler.sendToServer(new CSOpenMenu());
			}
			case "toggleOn" -> {
				PacketHandlerRM.sendToServer(new CSBoostPacket(4));
				PacketHandler.sendToServer(new CSSyncAllClientDataPacket());
				PacketHandler.sendToServer(new CSOpenMenu());
			}
			case "orgPlaceSTR" -> {
				PacketDistributor.sendToServer(new CSOrganizationPanelPacket(PanelPacketAction.PLACE, PanelRegistry.STRENGTH_UNIT, 0, 0));
				minecraft.player.playSound(ModSounds.menu_select.get());
				this.reloadMenu();
			}

			case "orgPlaceMAG" -> {
				PacketDistributor.sendToServer(new CSOrganizationPanelPacket(PanelPacketAction.PLACE, PanelRegistry.MAGIC_UNIT, 1, 0));
				minecraft.player.playSound(ModSounds.menu_select.get());
				this.reloadMenu();
			}

			case "orgPlaceDEF" -> {
				PacketDistributor.sendToServer(new CSOrganizationPanelPacket(PanelPacketAction.PLACE, PanelRegistry.DEFENSE_UNIT, 2, 0));
				minecraft.player.playSound(ModSounds.menu_select.get());
				this.reloadMenu();
			}

			case "orgPlaceAP" -> {
				PacketDistributor.sendToServer(new CSOrganizationPanelPacket(PanelPacketAction.PLACE, PanelRegistry.AP_UNIT, 3, 0));
				minecraft.player.playSound(ModSounds.menu_select.get());
				this.reloadMenu();
			}

			case "orgPlaceLV" -> {
				PacketDistributor.sendToServer(new CSOrganizationPanelPacket(PanelPacketAction.PLACE, PanelRegistry.LEVEL_UP, 4, 0));
				minecraft.player.playSound(ModSounds.menu_select.get());
				this.reloadMenu();
			}

			case "orgRemove00" -> {
				PacketDistributor.sendToServer(new CSOrganizationPanelPacket(PanelPacketAction.REMOVE, PanelRegistry.STRENGTH_UNIT, 0, 0));
				minecraft.player.playSound(ModSounds.menu_back.get());
				this.reloadMenu();
			}

			case "orgClear" -> {
				PacketDistributor.sendToServer(new CSOrganizationPanelPacket(PanelPacketAction.CLEAR, PanelRegistry.STRENGTH_UNIT, 0, 0));
				minecraft.player.playSound(ModSounds.menu_back.get());
				this.reloadMenu();
			}

			case "buy_selected_panel" -> {
				if (selectedOrgPanel != null) {
					if (playerData.getHearts() >= getSelectedShopEntry().cost()) {
						PacketHandlerRM.sendToServer(new CSBuyOrganizationPanelPacket(selectedOrgPanel, 1));
						PacketHandler.sendToServer(new CSSyncAllClientDataPacket());
						minecraft.player.playSound(ModSounds.itemget.get());
					} else {
						player.sendSystemMessage(Component.literal("You do not have enough Hearts to do this."));
						player.playSound(ModSounds.error.get());
					}
				}
			}

		}

	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (shopScrollBar != null) {
			boolean clickedShopScrollbar = mouseX >= shopScrollBar.getX() && mouseX <= shopScrollBar.getX() + shopScrollBar.getWidth() && mouseY >= shopScrollBar.getY() && mouseY <= shopScrollBar.getBottom();

			if (clickedShopScrollbar) {
				shopScrollBar.mouseClicked(mouseX, mouseY, button);
				return true;
			}
		}

		PanelShopEntry clickedShopEntry = getClickedShopEntry((int) mouseX, (int) mouseY);

		if (clickedShopEntry != null && button == 0) {
			selectedOrgPanel = clickedShopEntry.panelId();

			if (minecraft != null && minecraft.player != null) {
				minecraft.player.playSound(ModSounds.menu_select.get());
			}

			return true;
		}

		if (orgInventoryScrollBar != null) {
			boolean clickedScrollbar = mouseX >= orgInventoryScrollBar.getX() && mouseX <= orgInventoryScrollBar.getX() + orgInventoryScrollBar.getWidth() && mouseY >= orgInventoryScrollBar.getY() && mouseY <= orgInventoryScrollBar.getBottom();

			if (clickedScrollbar) {
				orgInventoryScrollBar.mouseClicked(mouseX, mouseY, button);
				return true;
			}
		}

		ResourceLocation clickedPickerPanel = getClickedPanelPicker((int) mouseX, (int) mouseY);

		if (clickedPickerPanel != null && button == 0) {
			GlobalDataRM addedData = ModDataRM.getGlobal(minecraft.player);

			if (addedData != null && addedData.getOwnedOrganizationPanelCount(clickedPickerPanel) <= 0) {
				if (minecraft != null && minecraft.player != null) {
					minecraft.player.playSound(ModSounds.error.get());
				}

				return true;
			}

			selectedOrgPanel = clickedPickerPanel;

			if (minecraft != null && minecraft.player != null) {
				minecraft.player.playSound(ModSounds.menu_select.get());
			}

			return true;
		}

		int gridX = getOrgGridMouseX((int) mouseX);
		int gridY = getOrgGridMouseY((int) mouseY);

		if (gridX >= 0 && gridY >= 0) {
			GlobalDataRM addedData = ModDataRM.getGlobal(minecraft.player);

			if (button == 0) {
				if (addedData != null && !addedData.isOrganizationPanelSlotUnlocked(gridX, gridY)) {
					if (minecraft != null && minecraft.player != null) {
						minecraft.player.playSound(ModSounds.error.get());
					}

					return true;
				}

				// Left click = place selected panel
				PacketDistributor.sendToServer(new CSOrganizationPanelPacket(PanelPacketAction.PLACE, selectedOrgPanel, gridX, gridY));

				if (minecraft != null && minecraft.player != null) {
					minecraft.player.playSound(ModSounds.menu_select.get());
				}

				return true;
			}

			if (button == 1) {
				// Right click = remove panel
				PacketDistributor.sendToServer(new CSOrganizationPanelPacket(PanelPacketAction.REMOVE, selectedOrgPanel, gridX, gridY));

				if (minecraft != null && minecraft.player != null) {
					minecraft.player.playSound(ModSounds.menu_back.get());
				}

				return true;
			}
		}

		return super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		if (shopScrollBar != null) {
			shopScrollBar.mouseReleased(mouseX, mouseY, button);
		}
		if (orgInventoryScrollBar != null) {
			orgInventoryScrollBar.mouseReleased(mouseX, mouseY, button);
		}

		return super.mouseReleased(mouseX, mouseY, button);
	}

	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
		if (shopScrollBar != null) {
			shopScrollBar.mouseDragged(mouseX, mouseY, button, dragX, dragY);
		}
		if (orgInventoryScrollBar != null) {
			orgInventoryScrollBar.mouseDragged(mouseX, mouseY, button, dragX, dragY);
		}

		return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double deltaX, double deltaY) {
		if (shopScrollBar != null) {
			boolean insideShop = mouseX >= shopListX && mouseX <= shopListX + shopListW && mouseY >= shopListY && mouseY <= shopListY + shopListH;

			if (insideShop) {
				shopScrollBar.mouseScrolled(mouseX, mouseY, deltaX, deltaY);
				return true;
			}
		}

		if (orgInventoryScrollBar != null) {
			boolean insideInventory = mouseX >= orgInventoryListX && mouseX <= orgInventoryListX + orgInventoryListW && mouseY >= orgInventoryListY && mouseY <= orgInventoryListY + orgInventoryListH;

			if (insideInventory) {
				orgInventoryScrollBar.mouseScrolled(mouseX, mouseY, deltaX, deltaY);
				return true;
			}
		}

		return super.mouseScrolled(mouseX, mouseY, deltaX, deltaY);
	}

	private ResourceLocation getClickedPanelPicker(int mouseX, int mouseY) {
		if (orgInventoryScrollBar == null) {
			return null;
		}

		int scrollBarX = orgInventoryScrollBar.getX();

		boolean insideList = mouseX >= orgInventoryListX && mouseX < scrollBarX && mouseY >= orgInventoryListY && mouseY < orgInventoryListY + orgInventoryListH;

		if (!insideList) {
			return null;
		}

		int localY = mouseY - orgInventoryListY + (int) orgInventoryScrollBar.scrollOffset;
		int index = localY / ORG_INVENTORY_ROW_HEIGHT;

		if (index < 0 || index >= ORG_PICKER_PANELS.length) {
			return null;
		}

		return ORG_PICKER_PANELS[index];
	}


	@Override
	public void tick() {
		super.tick();
		ticks++;

		int currentHearts = getCurrentHearts();

		if (currentHearts != -1 && currentHearts != lastKnownHearts) {
			init();
		}
	}

	@Override
	public void init() {
		updateAdaptivePanelLayout();

		final PlayerData playerData = PlayerData.get(minecraft.player);
		GlobalDataRM addedData = ModDataRM.getGlobal(minecraft.player);
		ticks = 0;
		lastKnownHearts = playerData != null ? playerData.getHearts() : -1;

		this.clearWidgets();

		float topBarHeight = (float) height * 0.17F;
		int topY = (int) topBarHeight + 5;
		int outerMargin = emergencyPanelLayout ? 8 : compactPanelLayout ? 12 : 20;
		int gap = emergencyPanelLayout ? 6 : compactPanelLayout ? 8 : 10;
		int maxLayoutWidth = 1100;
		int layoutWidth = Math.max(1, Math.min(maxLayoutWidth, width - outerMargin * 2));
		int layoutLeft = (width - layoutWidth) / 2;
		int usableBottom = height - 64;

		int shopWidth = emergencyPanelLayout ? Math.max(110, layoutWidth / 4) : compactPanelLayout ? Math.max(130, Math.min(175, layoutWidth / 5)) : Math.max(150, Math.min(200, layoutWidth / 5));
		if (shopWidth > layoutWidth - 240) shopWidth = Math.max(100, layoutWidth / 3);

		this.shopBoxX = layoutLeft;
		this.shopBoxY = topY;
		this.shopBoxW = shopWidth;
		this.shopBoxH = emergencyPanelLayout ? 150 : compactPanelLayout ? 175 : 195;

		this.detailBoxX = shopBoxX;
		this.detailBoxY = shopBoxY + shopBoxH + gap;
		this.detailBoxW = shopBoxW;
		this.detailBoxH = emergencyPanelLayout ? 95 : compactPanelLayout ? 110 : 125;

		this.editorBoxX = shopBoxX + shopBoxW + gap;
		this.editorBoxY = topY;
		this.editorBoxW = Math.max(1, layoutLeft + layoutWidth - editorBoxX);
		this.editorBoxH = emergencyPanelLayout ? 245 : compactPanelLayout ? 290 : 335;

		if (editorBoxY + editorBoxH > usableBottom) editorBoxH = Math.max(180, usableBottom - editorBoxY);
		if (detailBoxY + detailBoxH > usableBottom) detailBoxH = Math.max(70, usableBottom - detailBoxY);

		this.shopBox = new MenuBox(shopBoxX, shopBoxY, shopBoxW, shopBoxH, 1F, new Color(92, 92, 151));
		this.detailBox = new MenuBox(detailBoxX, detailBoxY, detailBoxW, detailBoxH, 1F, new Color(255, 255, 255));
		this.editorBox = new MenuBox(editorBoxX, editorBoxY, editorBoxW, editorBoxH, 1F, new Color(155, 155, 155));

		int orgGridCols = 5;
		int orgGridRows = 8;
		if (addedData != null && addedData.getOrganizationPanelGrid() != null) {
			orgGridCols = addedData.getOrganizationPanelGrid().getWidth();
			orgGridRows = addedData.getOrganizationPanelGrid().getHeight();
		}

		int controlButtonWidth = emergencyPanelLayout ? 78 : compactPanelLayout ? 100 : 125;
		int controlButtonGap = emergencyPanelLayout ? 15 : compactPanelLayout ? 17 : 20;
		int editorInnerLeft = editorBoxX + 16;
		int editorInnerRight = editorBoxX + editorBoxW - 14;
		int editorInnerWidth = Math.max(1, editorInnerRight - editorInnerLeft);

		this.orgInventoryListW = emergencyPanelLayout ? 100 : compactPanelLayout ? 125 : 150;
		int inventoryGridGap = emergencyPanelLayout ? 8 : compactPanelLayout ? 12 : 16;
		int gridControlsGap = emergencyPanelLayout ? 10 : compactPanelLayout ? 14 : 20;

		int maxGridWidth = editorInnerWidth - orgInventoryListW - inventoryGridGap - controlButtonWidth - gridControlsGap;
		int maxSlotSize = orgGridCols > 0 ? maxGridWidth / orgGridCols : orgSlotSize;
		if (maxSlotSize < orgSlotSize) orgSlotSize = Math.max(7, maxSlotSize);

		int gridWidth = orgGridCols * orgSlotSize;
		int gridHeight = orgGridRows * orgSlotSize;

		this.orgInventoryListX = editorInnerLeft;
		this.orgInventoryListY = editorBoxY + 28;
		this.orgInventoryListH = Math.max(90, editorBoxH - 48);
		this.orgPickerX = orgInventoryListX;
		this.orgPickerY = orgInventoryListY;

		this.orgInventoryScrollBar = new MenuScrollBar(orgInventoryListX + orgInventoryListW - 12, orgInventoryListY, orgInventoryListY + orgInventoryListH, orgInventoryListH, 0, false);
		this.orgInventoryScrollBar.setContentHeight(ORG_PICKER_PANELS.length * ORG_INVENTORY_ROW_HEIGHT + 4);
		addRenderableWidget(this.orgInventoryScrollBar);

		this.orgGridX = orgInventoryListX + orgInventoryListW + inventoryGridGap;
		this.orgGridY = editorBoxY + 28;

		this.orgControlsX = orgGridX + gridWidth + gridControlsGap;
		this.orgControlsY = orgGridY + 18;

		if (orgControlsX + controlButtonWidth > editorInnerRight) orgControlsX = editorInnerRight - controlButtonWidth;
		if (orgControlsX < orgGridX + gridWidth + 4) orgControlsX = orgGridX + gridWidth + 4;

		int controlCount = addedData != null && addedData.getUnlockedOrganizationPanelSlots() != 120 ? 6 : 5;
		int neededControlHeight = controlCount * controlButtonGap;
		int maxControlY = editorBoxY + editorBoxH - neededControlHeight - 8;
		if (maxControlY < editorBoxY + 30) maxControlY = editorBoxY + 30;
		orgControlsY = Math.max(editorBoxY + 30, Math.min(orgControlsY, maxControlY));

		this.orgPanelAreaX = orgGridX;
		this.orgPanelAreaY = orgGridY;
		this.orgPanelAreaWidth = gridWidth;
		this.orgPanelAreaHeight = gridHeight;

		this.shopListX = shopBoxX + 8;
		this.shopListY = shopBoxY + 22;
		this.shopListW = shopBoxW - 16;
		this.shopListH = shopBoxH - 32;
		this.shopPanelX = detailBoxX + 8;
		this.shopPanelWidth = detailBoxW - 16;
		this.shopInfoBoxY = detailBoxY + 15;

		this.shopScrollBar = new MenuScrollBar(shopListX + shopListW - 12, shopListY, shopListY + shopListH, shopListH, 0, false);
		this.shopScrollBar.setContentHeight(getPanelShopEntries().length * SHOP_ROW_HEIGHT + 4);
		addRenderableWidget(this.shopScrollBar);

		boolean tightControlLayout = emergencyPanelLayout || compactPanelLayout || this.height <= 720 || (minecraft != null && minecraft.getWindow() != null && minecraft.getWindow().getGuiScale() >= 4.0D);
		String slotReleaserText = tightControlLayout ? "Slot Releaser" : "Buy Slot Releaser";
		String boostText = addedData != null && addedData.getPanelsEnabled() == 1 ? (tightControlLayout ? "Boost OFF" : "Turn Boost OFF") : (tightControlLayout ? "Boost ON" : "Turn Boost ON");

		int controlX = orgControlsX;
		int controlY = orgControlsY;

		addRenderableWidget(new MenuButton(controlX, controlY, controlButtonWidth, "Buy Selected", MenuButton.ButtonType.BUTTON, false, e -> action("buy_selected_panel")));
		controlY += controlButtonGap;
		addRenderableWidget(new MenuButton(controlX, controlY, controlButtonWidth, "Unequip All", MenuButton.ButtonType.BUTTON, false, e -> action("orgClear")));
		controlY += controlButtonGap;

		if (addedData != null && addedData.getUnlockedOrganizationPanelSlots() != 120) {
			addRenderableWidget(new MenuButton(controlX, controlY, controlButtonWidth, slotReleaserText, MenuButton.ButtonType.BUTTON, false, e -> action("buy_slot_releaser")));
			controlY += controlButtonGap;
		}

		if (addedData != null && addedData.getPanelsEnabled() == 1) {
			addRenderableWidget(toggleOff = new MenuButton(controlX, controlY, controlButtonWidth, boostText, MenuButton.ButtonType.BUTTON, false, e -> action("toggleOff")));
		} else {
			addRenderableWidget(toggleOn = new MenuButton(controlX, controlY, controlButtonWidth, boostText, MenuButton.ButtonType.BUTTON, false, e -> action("toggleOn")));
		}

		controlY += controlButtonGap;
		addRenderableWidget(rejectOrg = new MenuButton(controlX, controlY, controlButtonWidth, "Leave Org", MenuButton.ButtonType.BUTTON, false, e -> action("rejectOrg")));
		controlY += controlButtonGap;
		addRenderableWidget(backButton = new MenuButton(controlX, controlY, controlButtonWidth, Strings.Gui_Menu_Back, MenuButton.ButtonType.BUTTON, false, e -> action("back")));

		super.init();
	}

	private void addPanelShopSelectButton(int x, int y, int width, PanelShopEntry entry) {
		boolean selected = entry.panelId().equals(selectedOrgPanel);

		addRenderableWidget(new MenuButton(x, y, width, selected ? "> " + entry.label() : entry.label(), MenuButton.ButtonType.BUTTON, false, e -> {
			selectedOrgPanel = entry.panelId();
			init();
		}));
	}


	private int getOrgGridMouseX(int mouseX) {
		if (minecraft == null || minecraft.player == null) {
			return -1;
		}

		GlobalDataRM addedData = ModDataRM.getGlobal(minecraft.player);

		if (addedData == null || addedData.getOrganizationPanelGrid() == null) {
			return -1;
		}

		int localX = mouseX - orgGridX;

		if (localX < 0) {
			return -1;
		}

		int gridX = localX / orgSlotSize;

		if (gridX < 0 || gridX >= addedData.getOrganizationPanelGrid().getWidth()) {
			return -1;
		}

		return gridX;
	}

	private int getOrgGridMouseY(int mouseY) {
		if (minecraft == null || minecraft.player == null) {
			return -1;
		}

		GlobalDataRM addedData = ModDataRM.getGlobal(minecraft.player);

		if (addedData == null || addedData.getOrganizationPanelGrid() == null) {
			return -1;
		}

		int localY = mouseY - orgGridY;

		if (localY < 0) {
			return -1;
		}

		int gridY = localY / orgSlotSize;

		if (gridY < 0 || gridY >= addedData.getOrganizationPanelGrid().getHeight()) {
			return -1;
		}

		return gridY;
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
			case "strength_unit", "strength_unit_l" ->
					ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "textures/gui/panels/icons/power_unit.png");
			case "power_link" ->
					ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "textures/gui/panels/icons/power_link.png");
			case "magic_unit", "magic_unit_l" ->
					ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "textures/gui/panels/icons/magic_unit.png");
			case "magic_link" ->
					ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "textures/gui/panels/icons/magic_link.png");
			case "defense_unit", "defense_unit_l" ->
					ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "textures/gui/panels/icons/guard_unit.png");
			case "guard_link" ->
					ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "textures/gui/panels/icons/guard_link.png");
			case "level_up" ->
					ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "textures/gui/panels/icons/level_up.png");
			case "sight_unit" ->
					ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "textures/gui/panels/icons/sight_unit.png");
			case "haste_panel" ->
					ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "textures/gui/panels/icons/haste.png");
			case "level_doubler", "level_doubler_line", "level_doubler_l_top_left", "level_doubler_l_top_right",
				 "level_doubler_l_left", "level_doubler_l_right" ->
					ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "textures/gui/panels/icons/lv_doubler.png");
			case "fire_boost_panel" ->
					ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "textures/gui/panels/icons/fire_panel.png");
			case "blizzard_boost_panel" ->
					ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "textures/gui/panels/icons/blizzard_panel.png");
			case "thunder_boost_panel" ->
					ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "textures/gui/panels/icons/thunder_panel.png");
			case "water_boost_panel" ->
					ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "textures/gui/panels/icons/water_panel.png");
			case "light_boost_panel" ->
					ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "textures/gui/panels/icons/light_panel.png");
			case "dark_boost_panel" ->
					ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "textures/gui/panels/icons/dark_panel.png");
			case "draw_panel" ->
					ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "textures/gui/panels/icons/treasure_magnet_panel.png");
			case "jackpot_panel" ->
					ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "textures/gui/panels/icons/ability_unit.png");
			case "lucky_lucky_panel" ->
					ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "textures/gui/panels/icons/ability_unit.png");
			case "high_jump_panel" ->
					ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "textures/gui/panels/icons/high_jump_panel.png");
			case "quick_run_panel" ->
					ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "textures/gui/panels/icons/quick_run_panel.png");
			case "dodge_roll_panel" ->
					ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "textures/gui/panels/icons/dodge_roll_panel.png");
			case "aerial_dodge_panel" ->
					ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "textures/gui/panels/icons/aerial_dodge_panel.png");
			case "glide_panel" ->
					ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "textures/gui/panels/icons/glide_panel.png");
			case "hearts_power_panel" ->
					ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "textures/gui/panels/icons/hearts_are_power_panel.png");
			case "ultima_weapon_panel" ->
					ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "textures/gui/panels/icons/ultima_weapon.png");
			default -> null;
		};
	}


	private void drawPanelShape(GuiGraphics gui, PanelData data, int px, int py, int color) {
		for (int localY = 0; localY < data.getHeight(); localY++) {
			for (int localX = 0; localX < data.getWidth(); localX++) {
				if (!data.occupies(localX, localY)) {
					continue;
				}

				int cellX = px + localX * orgSlotSize;
				int cellY = py + localY * orgSlotSize;

				boolean linkCell = data.linksAt(localX, localY);

				/*
				 * Base body color.
				 */
				gui.fill(
						cellX + 2,
						cellY + 2,
						cellX + orgSlotSize - 2,
						cellY + orgSlotSize - 2,
						color
				);

				/*
				 * Link area overlay.
				 * Cyan/blue highlight means Level Up can be placed here.
				 */
				if (linkCell) {
					int linkBorderBright = 0xFF55FFFF;
					int linkBorderDark = 0xFF007799;

					// Outer cyan border
					gui.fill(cellX + 1, cellY + 1, cellX + orgSlotSize - 1, cellY + 2, linkBorderBright); // top
					gui.fill(cellX + 1, cellY + 1, cellX + 2, cellY + orgSlotSize - 1, linkBorderBright); // left

					gui.fill(cellX + 1, cellY + orgSlotSize - 2, cellX + orgSlotSize - 1, cellY + orgSlotSize - 1, linkBorderDark); // bottom
					gui.fill(cellX + orgSlotSize - 2, cellY + 1, cellX + orgSlotSize - 1, cellY + orgSlotSize - 1, linkBorderDark); // right

					// Optional tiny corner shine
					//gui.fill(cellX + 2, cellY + 2, cellX + 4, cellY + 3, 0xFFFFFFFF);
				}


			}
		}
	}

	private boolean isLevelDoublerPanel(ResourceLocation panelId) {
		return PanelRegistry.LEVEL_DOUBLER.equals(panelId)
				|| PanelRegistry.LEVEL_DOUBLER_L_RIGHT.equals(panelId)
				|| PanelRegistry.LEVEL_DOUBLER_L_LEFT.equals(panelId)
				|| PanelRegistry.LEVEL_DOUBLER_L_TOP_RIGHT.equals(panelId)
				|| PanelRegistry.LEVEL_DOUBLER_L_TOP_LEFT.equals(panelId)
				|| PanelRegistry.LEVEL_DOUBLER_LINE.equals(panelId);
	}

	private boolean isStatLinkPanel(ResourceLocation panelId) {
		return PanelRegistry.POWER_LINK.equals(panelId)
				|| PanelRegistry.MAGIC_LINK.equals(panelId)
				|| PanelRegistry.GUARD_LINK.equals(panelId)
				|| PanelRegistry.LEVEL_LINK.equals(panelId);
	}

	private int[] getIconCell(ResourceLocation panelId, PanelData data) {
		if (isStatLinkPanel(panelId)) {
			int centerX = data.getWidth() / 2;
			int centerY = data.getHeight() / 2;

			if (data.occupies(centerX, centerY)) {
				return new int[] {centerX, centerY};
			}
		}

		return getFirstOccupiedCell(data);
	}

	private void drawPanelShapeLabel(GuiGraphics gui, PanelData data, ResourceLocation panelId, int px, int py) {
		int[] labelCell = getFirstOccupiedCell(data);

		if (labelCell == null) {
			return;
		}

		String label = getPanelShortName(panelId.getPath());
		int labelColor = data.getType() == PanelType.LINK ? 0x000000 : 0xFFFFFF;

		int labelX = px + labelCell[0] * orgSlotSize;
		int labelY = py + labelCell[1] * orgSlotSize;

		gui.pose().pushPose();
		{
			gui.pose().translate(labelX + 2, labelY + Math.max(3, orgSlotSize / 2 - 4), 0);
			gui.pose().scale(0.5F, 0.5F, 1F);
			gui.drawString(font, label, 2, 0, labelColor, false);
		}
		gui.pose().popPose();
	}

	private int[] getFirstOccupiedCell(PanelData data) {
		for (int localY = 0; localY < data.getHeight(); localY++) {
			for (int localX = 0; localX < data.getWidth(); localX++) {
				if (data.occupies(localX, localY)) {
					return new int[] {localX, localY};
				}
			}
		}

		return null;
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

	private void renderOrganizationPanelGrid(GuiGraphics gui, int mouseX, int mouseY) {

		if (minecraft == null || minecraft.player == null) {
			return;
		}


		GlobalDataRM addedData = ModDataRM.getGlobal(minecraft.player);

		if (addedData == null) {
			return;
		}

		PanelGrid grid = addedData.getOrganizationPanelGrid();
		orgSlotSize = 14;

		gui.drawString(this.font, "Organization Panels", orgGridX, orgGridY - 12, 0xFFFFFF, false);

		// Draw empty grid slots
		for (int y = 0; y < grid.getHeight(); y++) {
			for (int x = 0; x < grid.getWidth(); x++) {
				int sx = orgGridX + x * orgSlotSize;
				int sy = orgGridY + y * orgSlotSize;

				boolean hovered = mouseX >= sx && mouseX < sx + orgSlotSize && mouseY >= sy && mouseY < sy + orgSlotSize;

				boolean unlocked = addedData.isOrganizationPanelSlotUnlocked(x, y);

				int borderColor = unlocked ? hovered ? 0xFFFFFF00 : 0xFF555555 : 0xFF222222;

				int fillColor = unlocked ? hovered ? 0x55222222 : 0xAA111111 : 0xAA050505;

				gui.fill(sx, sy, sx + orgSlotSize, sy + orgSlotSize, fillColor);

				// border
				gui.fill(sx, sy, sx + orgSlotSize, sy + 1, borderColor);
				gui.fill(sx, sy + orgSlotSize - 1, sx + orgSlotSize, sy + orgSlotSize, borderColor);
				gui.fill(sx, sy, sx + 1, sy + orgSlotSize, borderColor);
				gui.fill(sx + orgSlotSize - 1, sy, sx + orgSlotSize, sy + orgSlotSize, borderColor);

				if (!unlocked) {
					gui.drawString(this.font, "×", sx + (orgSlotSize / 2) - 2, sy + (orgSlotSize / 2) - 4, 0xFF252525, false);
				}
			}
		}


		// Draw placed panels
		// Draw placed panels
		for (PanelSlot slot : grid.getPlacedPanels()) {
			PanelData data = PanelRegistry.get(slot.getPanelId());

			if (data == null) {
				continue;
			}

			int px = orgGridX + slot.getX() * orgSlotSize;
			int py = orgGridY + slot.getY() * orgSlotSize;
			int color = getPanelColor(data);

			/*
			 * 1. Draw every occupied cell.
			 */
			for (int localY = 0; localY < data.getHeight(); localY++) {
				for (int localX = 0; localX < data.getWidth(); localX++) {
					if (!data.occupies(localX, localY)) {
						continue;
					}

					int cellX = px + localX * orgSlotSize;
					int cellY = py + localY * orgSlotSize;

					gui.fill(
							cellX + 2,
							cellY + 2,
							cellX + orgSlotSize - 2,
							cellY + orgSlotSize - 2,
							color
					);

					boolean linkCell = data.linksAt(localX, localY);

					if (linkCell) {
						int linkBorderBright = 0xFF55FFFF;
						int linkBorderDark = 0xFF007799;

						gui.fill(cellX + 1, cellY + 1, cellX + orgSlotSize - 1, cellY + 2, linkBorderBright);
						gui.fill(cellX + 1, cellY + 1, cellX + 2, cellY + orgSlotSize - 1, linkBorderBright);

						gui.fill(cellX + 1, cellY + orgSlotSize - 2, cellX + orgSlotSize - 1, cellY + orgSlotSize - 1, linkBorderDark);
						gui.fill(cellX + orgSlotSize - 2, cellY + 1, cellX + orgSlotSize - 1, cellY + orgSlotSize - 1, linkBorderDark);
					}
				}
			}

			/*
			 * 2. Draw icon once, on the first occupied cell.
			 */
			drawPanelIconOrLabel(gui, slot.getPanelId(), data, px, py);
		}

		// Hover tooltip
		int gridCellX = getOrgGridMouseX(mouseX);
		int gridCellY = getOrgGridMouseY(mouseY);

		if (gridCellX >= 0 && gridCellY >= 0) {
			PanelSlot hoveredSlot = grid.getAt(gridCellX, gridCellY);

			if (hoveredSlot != null) {
				PanelData data = PanelRegistry.get(hoveredSlot.getPanelId());

				if (data != null) {
					gui.renderTooltip(this.font, Component.literal(getPanelDisplayName(hoveredSlot.getPanelId().getPath()) + " [" + data.getWidth() + "x" + data.getHeight() + "]" + " - " + getPanelDescription(data)), mouseX, mouseY);
				}
			}
		}
	}

	private boolean isLinkPanel(ResourceLocation panelId) {
		return PanelRegistry.POWER_LINK.equals(panelId)
				|| PanelRegistry.MAGIC_LINK.equals(panelId)
				|| PanelRegistry.GUARD_LINK.equals(panelId)
				|| PanelRegistry.LEVEL_LINK.equals(panelId);
	}

	private void drawCenteredPanelLabel(
			GuiGraphics gui,
			ResourceLocation panelId,
			PanelData data,
			int panelX,
			int panelY
	) {
		String label = getPanelShortName(panelId.getPath());

		int panelPixelW = data.getWidth() * orgSlotSize;
		int panelPixelH = data.getHeight() * orgSlotSize;

		/*
		 * Scale down so P-L / M-L / G-L fits inside one slot-ish area.
		 */
		float scale = 0.65F;

		int textW = this.font.width(label);
		int textH = this.font.lineHeight;

		float centerX = panelX + (panelPixelW / 2.0F);
		float centerY = panelY + (panelPixelH / 2.0F);

		float drawX = centerX - ((textW * scale) / 2.0F);
		float drawY = centerY - ((textH * scale) / 2.0F);

		gui.pose().pushPose();
		{
			gui.pose().translate(drawX, drawY, 120);
			gui.pose().scale(scale, scale, 1.0F);

			/*
			 * Black shadow/backplate so it stays readable over panels.
			 */
			gui.drawString(this.font, label, 1, 1, 0xFF000000, false);
			gui.drawString(this.font, label, 0, 0, 0xFFFFFFFF, false);
		}
		gui.pose().popPose();
	}

	private void drawPanelIconOrLabel(
			GuiGraphics gui,
			ResourceLocation panelId,
			PanelData data,
			int panelX,
			int panelY
	) {
		int[] iconCell = getIconCell(panelId, data);

		if (iconCell == null) {
			return;
		}

		int cellX = panelX + iconCell[0] * orgSlotSize;
		int cellY = panelY + iconCell[1] * orgSlotSize;

		ResourceLocation icon = getPanelIcon(panelId);

		if (icon != null) {
			int iconSize = Math.max(8, orgSlotSize - 4);

			gui.blit(
					icon,
					cellX + 2,
					cellY + 2,
					0,
					0,
					iconSize,
					iconSize,
					iconSize,
					iconSize
			);

			return;
		}

		String label = getPanelShortName(panelId.getPath());
		int labelColor = data.getType() == PanelType.LINK ? 0x000000 : 0xFFFFFF;

		gui.pose().pushPose();
		{
			gui.pose().translate(cellX + 2, cellY + Math.max(3, orgSlotSize / 2 - 4), 0);
			gui.pose().scale(0.5F, 0.5F, 1F);
			gui.drawString(font, label, 0, 0, labelColor, false);
		}
		gui.pose().popPose();
	}

	private void renderOrganizationPanelKeyGuide(GuiGraphics gui) {

		if (emergencyPanelLayout) {
			return;
		}

		int gridCols = 5;

		if (minecraft != null && minecraft.player != null) {
			GlobalDataRM addedData = ModDataRM.getGlobal(minecraft.player);

			if (addedData != null && addedData.getOrganizationPanelGrid() != null) {
				gridCols = addedData.getOrganizationPanelGrid().getWidth();
			}
		}

		int x = orgControlsX;
		int y = orgGridY - 16;

		if (x < editorBoxX || x >= editorBoxX + editorBoxW) {
			return;
		}

		gui.drawString(this.font, "Panel Controls", x, y, 0xFFD700, false);
		gui.drawString(this.font, "Right Click: Remove", x, y + 14, 0xAAAAAA, false);
		gui.drawString(this.font, "Left Click: Place", x, y + 25, 0xAAAAAA, false);
	}

	private String getPanelDescription(PanelData data) {
		String path = data.getId().getPath();

		return switch (path) {
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
			case "level_doubler" -> "+2 LV";
			case "level_doubler_l_right" -> "+2 LV, L-shape right";
			case "level_doubler_l_left" -> "+2 LV, L-shape left";
			case "level_doubler_l_top_right" -> "+2 LV, top L-shape right";
			case "level_doubler_l_top_left" -> "+2 LV, top L-shape left";
			case "level_doubler_line" -> "+2 LV, line shape";

			case "power_link" -> "+1 STR for each adjacent STR panel";
			case "magic_link" -> "+1 MAG for each adjacent MAG panel";
			case "guard_link" -> "+1 DEF for each adjacent DEF panel";
			case "level_link" -> "+1 LV for each adjacent LV panel";

			case "hearts_power_panel" -> "Activates 'Hearts Are Power' while equipped";
			case "ultima_weapon_panel" -> "Activates Ultima Weapon while equipped";

			case "combo_plus_panel" -> "+1 Combo Plus";
			case "haste_panel" -> "+Atk Spd";
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

	private void renderOrganizationPanelStats(GuiGraphics gui) {
		if (minecraft == null || minecraft.player == null) {
			return;
		}

		GlobalDataRM addedData = ModDataRM.getGlobal(minecraft.player);

		if (addedData == null) {
			return;
		}

		if (emergencyPanelLayout) {
			return;
		}

		PanelStats stats = addedData.getOrganizationPanelStats();
		PanelGrid grid = addedData.getOrganizationPanelGrid();

		int gridRows = grid != null ? grid.getHeight() : 4;

		int x = orgGridX;
		int y = orgGridY + (gridRows * orgSlotSize) + 4;

		PlayerData playerData = PlayerData.get(minecraft.player);

		int realLevel = playerData != null ? playerData.getLevel() : 0;
		int effectiveLevel = realLevel + stats.getLevelBonus();

		gui.drawString(this.font, "Grid Bonuses", x, y, 0xFFD700, false);
		y += compactPanelLayout ? 10 : 12;

		if (compactPanelLayout) {
			gui.drawString(this.font, "STR +" + stats.getStrength() + "  MAG +" + stats.getMagic() + "  DEF +" + stats.getDefense(), x, y, 0xFFFFFF, false);

			y += 10;

			gui.drawString(this.font, "AP +" + stats.getAp() + "  LV +" + stats.getLevelBonus() + " (" + realLevel + " > " + effectiveLevel + ")", x, y, 0xFFFFFF, false);

			return;
		} else {
			gui.drawString(this.font, "STR +" + stats.getStrength(), x, y, 0xFFFFFF, false);
			y += 10;
			gui.drawString(this.font, "MAG +" + stats.getMagic(), x, y, 0xFFFFFF, false);
			y += 10;
			gui.drawString(this.font, "DEF +" + stats.getDefense(), x, y, 0xFFFFFF, false);
			y += 10;
			gui.drawString(this.font, "AP +" + stats.getAp() + "  LV +" + stats.getLevelBonus() + " (" + realLevel + " > " + effectiveLevel + ")", x, y, 0xFFFFFF, false);
		}





	}

	private void renderOrganizationPanelPicker(GuiGraphics gui, int mouseX, int mouseY) {
		if (orgInventoryScrollBar == null) {
			return;
		}

		gui.drawString(this.font, "Inventory", orgInventoryListX, orgInventoryListY - 10, 0xFFD700, false);
		gui.drawString(this.font, "Selected: " + getPanelShortName(selectedOrgPanel.getPath()), orgInventoryListX + 0, orgInventoryListY + orgInventoryListH + 5, 0xFFD700, false);

		orgInventoryScrollBar.setContentHeight(ORG_PICKER_PANELS.length * ORG_INVENTORY_ROW_HEIGHT + 4);

		int scrollOffset = (int) orgInventoryScrollBar.scrollOffset;
		int scrollBarX = orgInventoryScrollBar.getX();

		gui.enableScissor(orgInventoryListX, orgInventoryListY, orgInventoryListX + orgInventoryListW, orgInventoryListY + orgInventoryListH);

		for (int i = 0; i < ORG_PICKER_PANELS.length; i++) {
			int rowY = orgInventoryListY + (i * ORG_INVENTORY_ROW_HEIGHT) - scrollOffset;

			if (rowY + ORG_INVENTORY_ROW_HEIGHT < orgInventoryListY || rowY > orgInventoryListY + orgInventoryListH) {
				continue;
			}

			renderPanelInventoryRow(gui, mouseX, mouseY, i, ORG_PICKER_PANELS[i], orgInventoryListX, rowY, scrollBarX - orgInventoryListX - 2, ORG_INVENTORY_ROW_HEIGHT);
		}

		gui.disableScissor();
	}

	private void renderPanelInventoryRow(GuiGraphics gui, int mouseX, int mouseY, int index, ResourceLocation panelId, int x, int y, int width, int height) {
		PanelData data = PanelRegistry.get(panelId);

		if (data == null) {
			return;
		}

		GlobalDataRM addedData = null;

		if (minecraft != null && minecraft.player != null) {
			addedData = ModDataRM.getGlobal(minecraft.player);
		}

		int count = addedData != null ? addedData.getOwnedOrganizationPanelCount(panelId) : 0;

		boolean hovered = mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;

		boolean selected = selectedOrgPanel.equals(panelId);

		int bgColor;

		if (selected) {
			bgColor = 0xAA660000;
		} else if (hovered) {
			bgColor = 0xAA222266;
		} else {
			bgColor = 0xAA111144;
		}

		int borderColor = selected ? 0xFFFF0000 : hovered ? 0xFFFFFFFF : 0xFF222255;

		gui.fill(x, y, x + width, y + height - 1, bgColor);

		gui.fill(x, y, x + width, y + 1, borderColor);
		gui.fill(x, y + height - 2, x + width, y + height - 1, 0xFF000022);

		// Small colored panel icon.
		int iconSize = 11;
		int iconX = x + 4;
		int iconY = y + 2;

		int fillColor = count > 0 ? getPanelColor(data) : 0xAA333333;

		gui.fill(iconX, iconY, iconX + iconSize, iconY + iconSize, 0xAA111111);
		gui.fill(iconX + 1, iconY + 1, iconX + iconSize - 1, iconY + iconSize - 1, fillColor);

		String name = getPanelDisplayName(panelId.getPath());
		String countText = "x" + count;

		int nameColor = count > 0 ? 0xFFFFFFFF : 0xFF888888;
		int countColor = count > 0 ? 0xFF55FF55 : 0xFFFF5555;

		gui.drawString(this.font, name, x + 19, y + 4, nameColor, false);

		gui.drawString(this.font, countText, x + width - this.font.width(countText) - 5, y + 4, countColor, false);

		if (hovered) {
			gui.renderTooltip(this.font, Component.literal(getPanelDisplayName(panelId.getPath()) + " [" + data.getWidth() + "x" + data.getHeight() + "]" + " - " + getPanelDescription(data)), mouseX, mouseY);
		}
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
			case "level_doubler_l_right" -> "LV DBL 'L'-R";
			case "level_doubler_l_left" -> "LV DBL 'L'-L";
			case "level_doubler_l_top_right" -> "LV DBL TR";
			case "level_doubler_l_top_left" -> "LV DBL TL";
			case "level_doubler_line" -> "LV DBL -";
			case "power_link" -> "PWR LNK";
			case "magic_link" -> "MAG LNK";
			case "guard_link" -> "DEF LNK";
			case "level_link" -> "LVL LNK";
			case "hearts_power_panel" -> "Hearts Are Power";
			case "ultima_weapon_panel" -> "Ultima Weapon";
			case "high_jump_panel" -> "High Jump";
			case "dodge_roll_panel" -> "Dodge Roll";
			case "aerial_dodge_panel" -> "Aerial Dodge";
			case "quick_run_panel" -> "Quick Run";
			case "glide_panel" -> "Glide";
			case "combo_plus_panel" -> "Combo Plus";
			case "haste_panel" -> "Haste";
			case "fire_boost_panel" -> "FIRE Boost";
			case "blizzard_boost_panel" -> "BLIZ Boost";
			case "thunder_boost_panel" -> "THUN Boost";
			case "water_boost_panel" -> "WTR Boost";
			case "light_boost_panel" -> "LGT Boost";
			case "dark_boost_panel" -> "DRK Boost";
			case "draw_panel" -> "Draw Panel";
			case "jackpot_panel" -> "Jackpot Panel";
			case "lucky_lucky_panel" -> "Lucky Lucky Panel";
			default -> path;
		};
	}

	private void renderHeartCount(GuiGraphics gui) {
		int hearts = getCurrentHearts();

		if (hearts < 0) {
			return;
		}

		gui.drawString(this.font, "Hearts: " + hearts, 8, 8, 0xFFD700, false);
	}

	@Override
	public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTicks) {

		super.render(gui, mouseX, mouseY, partialTicks);

		if (shopBox != null) {
			shopBox.renderWidget(gui, mouseX, mouseY, partialTicks);
		}

		if (detailBox != null) {
			detailBox.renderWidget(gui, mouseX, mouseY, partialTicks);
		}

		if (editorBox != null) {
			editorBox.renderWidget(gui, mouseX, mouseY, partialTicks);
		}

		renderPanelShopList(gui, mouseX, mouseY);

		if (detailBox != null) {
			gui.drawString(this.font, "Selected Panel", detailBoxX + 8, detailBoxY + 6, 0xFFFF9900, false);
		}

		if (editorBox != null) {
			gui.drawString(this.font, "Panel Editor", editorBoxX + 10, editorBoxY + 6, 0xFFFF9900, false);
		}

		renderOrganizationPanelPicker(gui, mouseX, mouseY);
		renderOrganizationPanelGrid(gui, mouseX, mouseY);

		renderOrganizationPanelKeyGuide(gui);
		renderOrganizationPanelStats(gui);
		renderPanelInfoBox(gui);


		for (var renderable : this.renderables) {
			renderable.render(gui, mouseX, mouseY, partialTicks);
		}

		if(selectedOrgPanel != null) {
			PanelData data = PanelRegistry.get(selectedOrgPanel);

			if (data != null) {
				if (mouseX > orgGridX && mouseY > orgGridY) {
					int px = mouseX - orgSlotSize / 2;
					int py = mouseY - orgSlotSize / 2;
					int color = getPanelColor(data);
					int alpha = (color >>> 24) & 0xFF;
					alpha *= 0.5;
					int newColor = (color & 0x00FFFFFF) | (alpha << 24);
					drawPanelShape(gui, data, px, py, newColor);
					drawPanelShapeLabel(gui, data, selectedOrgPanel, px, py);
				}
			}
		}

	}

	private PanelShopEntry[] getPanelShopEntries() {
		return new PanelShopEntry[]{new PanelShopEntry(PanelRegistry.STRENGTH_UNIT, "STR Unit", 1000, "+1 STR while placed"),
				new PanelShopEntry(PanelRegistry.MAGIC_UNIT, "MAG Unit", 1000, "+1 MAG while placed"),
				new PanelShopEntry(PanelRegistry.DEFENSE_UNIT, "DEF Unit", 1000, "+1 DEF while placed"),
				new PanelShopEntry(PanelRegistry.AP_UNIT, "AP Unit", 500, "+1 AP while placed"),
				new PanelShopEntry(PanelRegistry.SIGHT_UNIT, "Sight Unit", 1000, "+1 LV while placed"),
				new PanelShopEntry(PanelRegistry.LEVEL_UP, "Level Up", 2000, "+1 LV while placed"),

				new PanelShopEntry(PanelRegistry.STRENGTH_UNIT_L, "STR Unit L", 2000, "Large STR panel"),
				new PanelShopEntry(PanelRegistry.MAGIC_UNIT_L, "MAG Unit L", 2000, "Large MAG panel"),
				new PanelShopEntry(PanelRegistry.DEFENSE_UNIT_L, "DEF Unit L", 2000, "Large DEF panel"),
				new PanelShopEntry(PanelRegistry.AP_UNIT_L, "AP Unit L", 1000, "Large AP panel"),
				new PanelShopEntry(PanelRegistry.LEVEL_DOUBLER_L_RIGHT, "LV Doubler L Right", 4000, "L-shaped LV Doubler"),
				new PanelShopEntry(PanelRegistry.LEVEL_DOUBLER_L_LEFT, "LV Doubler L Left", 4000, "Mirrored L-shaped LV Doubler"),
				new PanelShopEntry(PanelRegistry.LEVEL_DOUBLER_L_TOP_RIGHT, "LV Doubler Top R", 4000, "Top L-shaped LV Doubler"),
				new PanelShopEntry(PanelRegistry.LEVEL_DOUBLER_L_TOP_LEFT, "LV Doubler Top L", 4000, "Mirrored top L-shaped LV Doubler"),
				new PanelShopEntry(PanelRegistry.LEVEL_DOUBLER_LINE, "LV Doubler Line", 4000, "Long LV Doubler"),

				new PanelShopEntry(PanelRegistry.POWER_LINK, "Power Link", 2500, "Boosts nearby STR panels"),
				new PanelShopEntry(PanelRegistry.MAGIC_LINK, "Magic Link", 2500, "Boosts nearby MAG panels"),
				new PanelShopEntry(PanelRegistry.GUARD_LINK, "Guard Link", 2500, "Boosts nearby DEF panels"),
				//new PanelShopEntry(PanelRegistry.LEVEL_LINK, "Level Link", 2500, "Boosts nearby LV panels"),
				new PanelShopEntry(PanelRegistry.HEARTS_POWER_PANEL, "Hearts Are Power", 50000, "Enables Hearts Are Power while equipped"),
				new PanelShopEntry(PanelRegistry.ULTIMA_WEAPON_PANEL, "Ultima Weapon", 50000, "Enables Ultima Weapon while equipped"),
				new PanelShopEntry(PanelRegistry.HIGH_JUMP_PANEL, "High Jump", 2500, "Enables High Jump while equipped"),
				new PanelShopEntry(PanelRegistry.DODGE_ROLL_PANEL, "Dodge Roll", 2500, "Enables Dodge Roll while equipped"),
				new PanelShopEntry(PanelRegistry.AERIAL_DODGE_PANEL, "Aerial Dodge", 3000, "Enables Aerial Dodge while equipped"),
				new PanelShopEntry(PanelRegistry.QUICK_RUN_PANEL, "Quick Run", 3000, "Enables Quick Run while equipped"),
				new PanelShopEntry(PanelRegistry.GLIDE_PANEL, "Glide", 4000, "Enables Glide while equipped"),

				new PanelShopEntry(PanelRegistry.COMBO_PLUS_PANEL, "Combo Plus", 2500, "+1 ground combo hit while equipped"),
				new PanelShopEntry(PanelRegistry.HASTE_PANEL, "Haste", 2500, "Increases Attack Speed while equipped"),
				new PanelShopEntry(PanelRegistry.FIRE_BOOST_PANEL, "Fire Boost", 3000, "Boosts Fire damage"),
				new PanelShopEntry(PanelRegistry.BLIZZARD_BOOST_PANEL, "Blizzard Boost", 3000, "Boosts Blizzard damage"),
				new PanelShopEntry(PanelRegistry.THUNDER_BOOST_PANEL, "Thunder Boost", 3000, "Boosts Thunder damage"),
				new PanelShopEntry(PanelRegistry.WATER_BOOST_PANEL, "Water Boost", 3000, "Boosts Water damage"),
				new PanelShopEntry(PanelRegistry.LIGHT_BOOST_PANEL, "Light Boost", 3000, "Boosts Light damage"),
				new PanelShopEntry(PanelRegistry.DARK_BOOST_PANEL, "Darkness Boost", 3000, "Boosts Dark damage"),
				new PanelShopEntry(PanelRegistry.DRAW_PANEL, "Draw", 2000, "Improves pickup range"),
				new PanelShopEntry(PanelRegistry.JACKPOT_PANEL, "Jackpot", 2500, "Improves prize drops"),
				new PanelShopEntry(PanelRegistry.LUCKY_LUCKY_PANEL, "Lucky Lucky", 4000, "Improves rare drops"),

		};
	}

	private PanelShopEntry getSelectedShopEntry() {
		for (PanelShopEntry entry : getPanelShopEntries()) {
			if (entry.panelId().equals(selectedOrgPanel)) {
				return entry;
			}
		}

		return null;
	}

	private void updateAdaptivePanelLayout() {
		compactPanelLayout = this.width < 1280 || this.height < 620;
		emergencyPanelLayout = this.width < 760 || this.height < 500;

		if (emergencyPanelLayout) {
			orgSlotSize = 10;
			orgPickerSlotSize = 18;
			orgPickerGap = 2;
			orgPickerRowGap = 3;
			orgPickerColumns = 6;
			shopVisibleButtons = 3;
			shopButtonGap = 14;
			return;
		}

		if (compactPanelLayout) {
			orgSlotSize = 16;
			orgPickerSlotSize = 20;
			orgPickerGap = 3;
			orgPickerRowGap = 4;
			orgPickerColumns = 7;
			shopVisibleButtons = 4;
			shopButtonGap = 15;
			return;
		}

		orgSlotSize = BASE_ORG_SLOT_SIZE;
		orgPickerSlotSize = BASE_ORG_PANEL_PICKER_SLOT_SIZE;
		orgPickerGap = BASE_ORG_PANEL_PICKER_GAP;
		orgPickerRowGap = BASE_ORG_PANEL_PICKER_ROW_GAP;
		shopVisibleButtons = BASE_SHOP_VISIBLE_BUTTONS;
		shopButtonGap = BASE_SHOP_BUTTON_GAP;
	}

	private void renderPanelInfoBox(GuiGraphics gui) {
		PanelShopEntry entry = getSelectedShopEntry();

		if (entry == null || minecraft == null || minecraft.player == null) {
			return;
		}

		PlayerData playerData = PlayerData.get(minecraft.player);
		GlobalDataRM addedData = ModDataRM.getGlobal(minecraft.player);

		int hearts = playerData != null ? playerData.getHearts() : 0;
		int owned = addedData != null ? addedData.getOwnedOrganizationPanelCount(entry.panelId()) : 0;

		int x = this.shopPanelX;
		int y = this.shopInfoBoxY;
		int width = this.shopPanelWidth;
		int height = Math.max(50, detailBoxH - 20);

		gui.fill(x, y, x + width, y + height, 0x88000000);

		gui.fill(x, y, x + width, y + 1, 0xFF777777);
		gui.fill(x, y + height - 1, x + width, y + height, 0xFF777777);
		gui.fill(x, y, x + 1, y + height, 0xFF777777);
		gui.fill(x + width - 1, y, x + width, y + height, 0xFF777777);

		int textY = y + 6;

		gui.drawString(this.font, entry.label(), x + 8, textY, 0xFFFFD700, false);
		textY += 12;

		int costColor = hearts >= entry.cost() ? 0xFF55FF55 : 0xFFFF5555;

		gui.drawString(this.font, "Cost: " + entry.cost() + " Hearts", x + 8, textY, costColor, false);
		textY += 11;

		gui.drawString(this.font, "Owned: " + owned, x + 8, textY, 0xFFFFFFFF, false);
		textY += 11;

		if (!compactPanelLayout) {
			gui.drawString(this.font, "Hearts: " + hearts, x + 8, textY, 0xFFFF5555, false);
			textY += 13;
		} else {
			textY += 2;
		}

		drawWrappedString(gui, entry.description(), x + 8, textY, width - 16, 0xFFAAAAAA);
	}

	private void drawWrappedString(GuiGraphics gui, String text, int x, int y, int maxWidth, int color) {
		if (text == null || text.isEmpty()) {
			return;
		}

		for (net.minecraft.util.FormattedCharSequence line : this.font.split(Component.literal(text), maxWidth)) {
			gui.drawString(this.font, line, x, y, color, false);
			y += 10;
		}
	}

	private PanelShopEntry getClickedShopEntry(int mouseX, int mouseY) {
		if (shopScrollBar == null) {
			return null;
		}

		int scrollBarX = shopScrollBar.getX();

		boolean insideList = mouseX >= shopListX && mouseX < scrollBarX && mouseY >= shopListY && mouseY < shopListY + shopListH;

		if (!insideList) {
			return null;
		}

		int localY = mouseY - shopListY + (int) shopScrollBar.scrollOffset;
		int index = localY / SHOP_ROW_HEIGHT;

		PanelShopEntry[] entries = getPanelShopEntries();

		if (index < 0 || index >= entries.length) {
			return null;
		}

		return entries[index];
	}

	private void renderPanelShopList(GuiGraphics gui, int mouseX, int mouseY) {
		if (shopScrollBar == null) {
			return;
		}

		gui.drawString(this.font, "Panel Shop", shopBoxX + 8, shopBoxY + 6, 0xFFFF9900, false);

		PanelShopEntry[] entries = getPanelShopEntries();

		shopScrollBar.setContentHeight(entries.length * SHOP_ROW_HEIGHT + 4);

		int scrollOffset = (int) shopScrollBar.scrollOffset;
		int scrollBarX = shopScrollBar.getX();

		gui.enableScissor(shopListX, shopListY, shopListX + shopListW, shopListY + shopListH);

		for (int i = 0; i < entries.length; i++) {
			int rowY = shopListY + (i * SHOP_ROW_HEIGHT) - scrollOffset;

			if (rowY + SHOP_ROW_HEIGHT < shopListY || rowY > shopListY + shopListH) {
				continue;
			}

			renderPanelShopRow(gui, mouseX, mouseY, entries[i], shopListX, rowY, scrollBarX - shopListX - 2, SHOP_ROW_HEIGHT);
		}

		gui.disableScissor();
	}

	private void renderPanelShopRow(GuiGraphics gui, int mouseX, int mouseY, PanelShopEntry entry, int x, int y, int width, int height) {
		boolean hovered = mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;

		boolean selected = selectedOrgPanel != null && selectedOrgPanel.equals(entry.panelId());

		PlayerData playerData = minecraft != null && minecraft.player != null ? PlayerData.get(minecraft.player) : null;

		int hearts = playerData != null ? playerData.getHearts() : 0;
		boolean canAfford = hearts >= entry.cost();

		int bgColor;

		if (selected) {
			bgColor = 0xAA660000;
		} else if (hovered) {
			bgColor = 0xAA222266;
		} else {
			bgColor = 0xAA111144;
		}

		int borderColor = selected ? 0xFFFF0000 : hovered ? 0xFFFFFFFF : 0xFF222255;

		gui.fill(x, y, x + width, y + height - 1, bgColor);

		gui.fill(x, y, x + width, y + 1, borderColor);
		gui.fill(x, y + height - 2, x + width, y + height - 1, 0xFF000022);

		String selector = selected ? "› " : "";
		String name = selector + entry.label();

		int nameColor = canAfford ? 0xFFFFFFFF : 0xFFAAAAAA;
		int costColor = canAfford ? 0xFF55FF55 : 0xFFFF5555;

		gui.drawString(this.font, name, x + 6, y + 4, nameColor, false);

		String costText = String.valueOf(entry.cost());

		gui.drawString(this.font, costText, x + width - this.font.width(costText) - 5, y + 4, costColor, false);

		if (hovered) {
			gui.renderTooltip(this.font, Component.literal(entry.label() + " - " + entry.cost() + " Hearts"), mouseX, mouseY);
		}
	}

	private record PanelShopEntry(ResourceLocation panelId, String label, int cost, String description) {
	}
}
