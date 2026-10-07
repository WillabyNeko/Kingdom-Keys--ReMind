package online.remind.remind.handler;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Mth;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RenderLivingEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import online.kingdomkeys.kingdomkeys.ability.ModAbilities;
import online.kingdomkeys.kingdomkeys.api.event.client.CommandMenuEvent;
import online.kingdomkeys.kingdomkeys.api.event.client.MenuButtonRegisterEvent;
import online.kingdomkeys.kingdomkeys.api.event.client.TargetSelectorEvent;
import online.kingdomkeys.kingdomkeys.client.TrailRenderer;
import online.kingdomkeys.kingdomkeys.client.gui.StopGui;
import online.kingdomkeys.kingdomkeys.client.gui.elements.CommandMenuItem;
import online.kingdomkeys.kingdomkeys.client.gui.elements.buttons.MenuButton;
import online.kingdomkeys.kingdomkeys.client.gui.menu.MenuScreen;
import online.kingdomkeys.kingdomkeys.client.gui.menu.styles.StylesMenu;
import online.kingdomkeys.kingdomkeys.client.gui.overlay.CommandMenuGui;
import online.kingdomkeys.kingdomkeys.data.PlayerData;
import online.kingdomkeys.kingdomkeys.driveform.ModDriveForms;
import online.kingdomkeys.kingdomkeys.lib.Strings;
import online.kingdomkeys.kingdomkeys.util.Utils;
import online.remind.remind.KingdomKeysReMind;
import online.remind.remind.ability.ModAbilitiesRM;
import online.remind.remind.capabilities.GlobalDataRM;
import online.remind.remind.capabilities.ModDataRM;
import online.remind.remind.client.ClientUtilsRM;
import online.remind.remind.client.gui.*;
import online.remind.remind.config.ModConfigs;
import online.remind.remind.dreameater.DreamEater;
import online.remind.remind.dreameater.ModDreamEaters;
import online.remind.remind.driveform.ModDriveFormsRM;
import online.remind.remind.effect.ModMobEffectsRM;
import online.remind.remind.entity.ModEntitiesRM;
import online.remind.remind.lib.StringsRM;
import org.joml.Vector3f;

import java.awt.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class ClientEventsRM {

	private static final UUID XEPHIRO_UUID =
			UUID.fromString("70b48fbd-b67f-4f3e-9369-09cef36d51a3");

	private static final UUID DEV_UUID =
			UUID.fromString("380df991-f603-344c-a090-369bad2a924a");

	private static final Map<UUID, Integer> LAST_LIGHT_STEP_PARTICLE_TICK =
			new HashMap<>();
	private static final int LIGHT_STEP_TRAIL_LENGTH = 14;
	private static final float LIGHT_STEP_TRAIL_WIDTH = 0.04F;
	private static final double LIGHT_STEP_TRAIL_HEIGHT = 1.0D;


	private static final double LIGHT_STEP_SWIRL_RADIUS = 0.30D;
	private static final double LIGHT_STEP_SWIRL_SPEED = Math.PI * 0.40D;


	private static final double LIGHT_STEP_CYAN_PHASE = 0.0D;
	private static final double LIGHT_STEP_GOLD_LEFT_PHASE = (Math.PI * 2.0D) / 3.0D;
	private static final double LIGHT_STEP_GOLD_RIGHT_PHASE = (Math.PI * 4.0D) / 3.0D;

	private static final Map<UUID, LightStepTrailSet> LIGHT_STEP_TRAILS =
			new HashMap<>();

	private static class LightStepTrailSet {
		private final TrailRenderer.Trail center =
				new TrailRenderer.Trail(LIGHT_STEP_TRAIL_LENGTH);

		private final TrailRenderer.Trail left =
				new TrailRenderer.Trail(LIGHT_STEP_TRAIL_LENGTH);

		private final TrailRenderer.Trail right =
				new TrailRenderer.Trail(LIGHT_STEP_TRAIL_LENGTH);


		private double swirlPhase = 0.0D;


		private int lastUpdatedTick = Integer.MIN_VALUE;
	}

	/*
	 * Epic Fight is OPTIONAL.
	 *
	 * We deliberately resolve its WHITE_AFTERIMAGE particle through
	 * reflection so this class contains no yesman.epicfight references.
	 *
	 * Epic Fight itself uses this exact particle for its Technician
	 * afterimage. Its particle implementation captures the EFM player
	 * snapshot, including the current skeletal pose and supported gear.
	 */
	private static ParticleOptions EPIC_FIGHT_WHITE_AFTERIMAGE = null;
	private static boolean EPIC_FIGHT_AFTERIMAGE_LOOKED_UP = false;
	private static boolean EPIC_FIGHT_AFTERIMAGE_WARNED = false;

	private static final int DARK_STEP_AFTERIMAGE_TICKS = 40;

	private static final Map<UUID, DarkStepAfterImage> DARK_STEP_AFTER_IMAGES =
			new HashMap<>();


	private static boolean RENDERING_DARK_STEP_GHOST = false;

	private static class DarkStepAfterImage {
		private final Vec3 origin;
		private final long startGameTime;
		private final DarkStepGhostPlayer ghost;

		private DarkStepAfterImage(
				Vec3 origin,
				long startGameTime,
				DarkStepGhostPlayer ghost
		) {
			this.origin = origin;
			this.startGameTime = startGameTime;
			this.ghost = ghost;
		}
	}

	private static class DarkStepGhostPlayer extends RemotePlayer {
		private final PlayerSkin ghostSkin;

		private DarkStepGhostPlayer(
				ClientLevel level,
				GameProfile profile,
				PlayerSkin ghostSkin
		) {
			super(level, profile);
			this.ghostSkin = ghostSkin;
			this.setInvisible(true);
			this.setCustomNameVisible(false);
		}

		@Override
		public PlayerSkin getSkin() {
			return ghostSkin;
		}

		@Override
		public boolean isInvisibleTo(Player player) {
			return false;
		}

		@Override
		public boolean shouldShowName() {
			return false;
		}
	}

	public static void startDarkStepAfterImage(Player player) {
		if (!(player instanceof AbstractClientPlayer clientPlayer)) {
			return;
		}

		if (!(player.level() instanceof ClientLevel clientLevel)) {
			return;
		}

		Vec3 origin = player.position();

		/*
		 * Epic Fight installed?
		 *
		 * Use Epic Fight's OWN afterimage particle instead of recreating its
		 * animator/render pipeline ourselves. This call happens before the
		 * Dark Step teleport packet, so EFM captures the player at the origin.
		 */
		if (trySpawnEpicFightAfterImage(
				clientPlayer,
				origin
		)) {
			return;
		}

		/*
		 * No Epic Fight (or its afterimage could not be resolved):
		 * fall back to the working vanilla translucent ghost.
		 */
		DarkStepGhostPlayer ghost =
				new DarkStepGhostPlayer(
						clientLevel,
						new GameProfile(
								UUID.randomUUID(),
								"DarkStepGhost"
						),
						clientPlayer.getSkin()
				);


		ghost.setPos(origin.x, origin.y, origin.z);

		ghost.setYRot(clientPlayer.getYRot());
		ghost.yRotO = clientPlayer.yRotO;

		ghost.setXRot(clientPlayer.getXRot());
		ghost.xRotO = clientPlayer.xRotO;

		ghost.yBodyRot = clientPlayer.yBodyRot;
		ghost.yBodyRotO = clientPlayer.yBodyRotO;

		ghost.yHeadRot = clientPlayer.yHeadRot;
		ghost.yHeadRotO = clientPlayer.yHeadRotO;

		ghost.setPose(clientPlayer.getPose());
		ghost.setSprinting(clientPlayer.isSprinting());
		ghost.setShiftKeyDown(clientPlayer.isShiftKeyDown());
		ghost.setDeltaMovement(clientPlayer.getDeltaMovement());
		ghost.tickCount = clientPlayer.tickCount;

		DARK_STEP_AFTER_IMAGES.put(
				player.getUUID(),
				new DarkStepAfterImage(
						origin,
						player.level().getGameTime(),
						ghost
				)
		);
	}


	/**
	 * Spawn Epic Fight's own WHITE_AFTERIMAGE particle without linking
	 * ClientEventsRM against Epic Fight at class-load time.
	 *
	 * Epic Fight passes the entity id through the particle X-speed argument
	 * as Double.longBitsToDouble(entityId). We mirror that exact call.
	 */
	private static boolean trySpawnEpicFightAfterImage(
			AbstractClientPlayer player,
			Vec3 origin
	) {
		if (!ModList.get().isLoaded("epicfight")) {
			return false;
		}

		ParticleOptions particle =
				getEpicFightWhiteAfterimageParticle();

		if (particle == null) {
			return false;
		}

		try {
			player.level().addParticle(
					particle,
					origin.x,
					origin.y,
					origin.z,
					Double.longBitsToDouble(
							player.getId()
					),
					0.0D,
					0.0D
			);

			return true;

		} catch (Throwable throwable) {
			warnEpicFightAfterimageFailure(
					"spawn",
					throwable
			);

			return false;
		}
	}

	/**
	 * Resolves:
	 *
	 * yesman.epicfight.registry.entries.EpicFightParticles.WHITE_AFTERIMAGE
	 *
	 * and invokes the DeferredHolder's get() method reflectively.
	 *
	 * The returned object is vanilla ParticleOptions, so after this point
	 * no Epic Fight type is needed.
	 */
	private static ParticleOptions getEpicFightWhiteAfterimageParticle() {
		if (EPIC_FIGHT_AFTERIMAGE_LOOKED_UP) {
			return EPIC_FIGHT_WHITE_AFTERIMAGE;
		}

		EPIC_FIGHT_AFTERIMAGE_LOOKED_UP = true;

		try {
			Class<?> particlesClass =
					Class.forName(
							"yesman.epicfight.registry.entries.EpicFightParticles",
							false,
							ClientEventsRM.class.getClassLoader()
					);

			Field whiteAfterimageField =
					particlesClass.getField(
							"WHITE_AFTERIMAGE"
					);

			Object deferredHolder =
					whiteAfterimageField.get(null);

			if (deferredHolder == null) {
				return null;
			}

			Method getMethod =
					deferredHolder
							.getClass()
							.getMethod("get");

			Object resolvedParticle =
					getMethod.invoke(
							deferredHolder
					);

			if (resolvedParticle instanceof ParticleOptions particleOptions) {
				EPIC_FIGHT_WHITE_AFTERIMAGE =
						particleOptions;

				return particleOptions;
			}

			warnEpicFightAfterimageFailure(
					"lookup returned "
							+ (resolvedParticle == null
							? "null"
							: resolvedParticle.getClass().getName()),
					null
			);

		} catch (Throwable throwable) {
			warnEpicFightAfterimageFailure(
					"lookup",
					throwable
			);
		}

		return null;
	}

	/*
	 * Do NOT silently swallow this anymore.
	 *
	 * The previous compat attempt could fail and immediately fall back to the
	 * vanilla ghost with no indication of what happened. One warning makes
	 * an EFM API/version mismatch obvious in latest.log without spamming it.
	 */
	private static void warnEpicFightAfterimageFailure(
			String stage,
			Throwable throwable
	) {
		if (EPIC_FIGHT_AFTERIMAGE_WARNED) {
			return;
		}

		EPIC_FIGHT_AFTERIMAGE_WARNED = true;

		System.err.println(
				"[Kingdom Keys Re:Mind] Epic Fight Dark Step afterimage "
						+ stage
						+ " failed. Falling back to vanilla afterimage."
		);

		if (throwable != null) {
			throwable.printStackTrace();
		}
	}


	public enum RMButtons {
		PRESTIGE, DREAMEATER, CREDITS, WIKI, PANEL, WALLET
	}

	public enum RMStyleButtons {
		XEPHIRO
	}

	@SubscribeEvent
	public void commandMenuItemUpdate(CommandMenuEvent.ItemUpdate event){
		Player player = Minecraft.getInstance().player;
		PlayerData playerData = PlayerData.get(player);
		if (ModDriveForms.registry.get(PlayerData.get(Minecraft.getInstance().player).getActiveDriveForm()).getClass().getSimpleName().contains("Style")) {
			if (event.getId().equals(CommandMenuGui.INSTANCE.revert)){
				if (playerData != null){
					if (playerData.getAlignment() != Utils.OrgMember.NONE){
						event.getItem().setVisible(false);
					}
				}
				event.getItem().setActive(false);
			}
		}
	}

	@SubscribeEvent
	public void menuButton(MenuButtonRegisterEvent event){
		MenuScreen screen = event.getScreen();
		ArrayList<MenuButton> buttons = event.getButtons();

		float topBarHeight = (float) screen.height * 0.17F;
		int start = (int)(topBarHeight) +5;
		int pos = 0;

		float buttonPosX = (float) screen.width * 0.80F;
		float buttonWidth = ((float) screen.width * 0.1744F) - 22;

		if (ModConfigs.ngpEnabled) {
			buttons.add(new MenuButton((int) buttonPosX, start, (int) buttonWidth, (StringsRM.Gui_Menu_Button_Prestige), MenuButton.ButtonType.BUTTON, true, (e) -> {
				action(RMButtons.PRESTIGE);
			}));
		}
		if (ModConfigs.spiritsEnabled) {
			buttons.add(new MenuButton((int) buttonPosX, start + 18 * ++pos, (int) buttonWidth, (StringsRM.Gui_Menu_Button_DreamEater), MenuButton.ButtonType.BUTTON, true, (e) -> {
				action(RMButtons.DREAMEATER);
			}));
		}

		// Panel
		if (ModConfigs.panelsEnabled) {
			if (PlayerData.get(Minecraft.getInstance().player).getAlignment() != Utils.OrgMember.NONE) {
				buttons.add(new MenuButton((int) buttonPosX, start + 18 * ++pos, (int) buttonWidth, (StringsRM.Gui_Menu_Button_Panel), MenuButton.ButtonType.BUTTON, true, (e) -> {
					action(RMButtons.PANEL);
				}));
			}
		}

		// Wiki
		buttons.add(new MenuButton((int) buttonPosX, start + 18  * ++pos, (int) buttonWidth, (StringsRM.Gui_Menu_Button_Wiki), MenuButton.ButtonType.BUTTON, true, (e) -> {
			action(RMButtons.WIKI);
		}));
		// Wallet
		buttons.add(new MenuButton((int) buttonPosX, start + 18 * ++pos, (int) buttonWidth, (StringsRM.Gui_Menu_Button_Wallet), MenuButton.ButtonType.BUTTON, true, (e) -> {
			action(RMButtons.WALLET);
		}));
		// Credits
		buttons.add(new MenuButton((int) buttonPosX, start + 18 * ++pos, (int) buttonWidth, (StringsRM.Gui_Menu_Button_Credits), MenuButton.ButtonType.BUTTON, false, (e) -> {
			action(RMButtons.CREDITS);
		}));

	}

	protected void action(RMButtons buttonID){
		switch (buttonID){
			case PRESTIGE -> Minecraft.getInstance().setScreen(new PrestigeMenu());
			case DREAMEATER -> Minecraft.getInstance().setScreen(new DreamEaterMenu());
			case CREDITS -> Minecraft.getInstance().setScreen(new CreditsScreen());
			case PANEL -> Minecraft.getInstance().setScreen(new PanelsMenu());
			case WIKI -> Minecraft.getInstance().setScreen(new WikiMenu());
			case WALLET -> Minecraft.getInstance().setScreen(new WalletMenu());
		}
	}

	// VFX for Steps
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void RenderEntity(RenderLivingEvent.Pre event){
		if (event.getEntity() != null){

			if (RENDERING_DARK_STEP_GHOST
					&& event.getEntity() instanceof DarkStepGhostPlayer) {
				return;
			}

			if (event.getEntity() instanceof Player) {
				Player player = (Player) event.getEntity();
				PlayerData playerData = PlayerData.get(player);
				GlobalDataRM globalData = ModDataRM.getGlobal(event.getEntity());
				if (playerData != null && globalData != null){

					// Light and Dark Step VFX
					if(globalData.getStepTicks() > 0) {
						event.setCanceled(true);
						player.invulnerableTime = globalData.getStepTicks();

						if (globalData.getStepType() == StringsRM.orgStepType) {
							if (playerData.getAlignment().equals(Utils.OrgMember.XEMNAS)){
								player.level().addAlwaysVisibleParticle(ParticleTypes.END_ROD, player.getX() + player.level().random.nextDouble() - 0.5D, player.getY()+ player.level().random.nextDouble() *2D, player.getZ()  + player.level().random.nextDouble() - 0.5D, 0,0,0);
								player.level().addAlwaysVisibleParticle(new DustParticleOptions(new Vector3f(0F,0F,0F),1F),player.getX() + player.level().random.nextDouble() - 0.5D, player.getY()+ player.level().random.nextDouble() *2D, player.getZ() + player.level().random.nextDouble() - 0.5D, 0, 0, 0);
							}
							if (playerData.getAlignment().equals(Utils.OrgMember.XIGBAR)){
								player.level().addAlwaysVisibleParticle(ParticleTypes.ASH, player.getX() + player.level().random.nextDouble() - 0.5D, player.getY()+ player.level().random.nextDouble() *2D, player.getZ()  + player.level().random.nextDouble() - 0.5D, 3,3,3);
							}
							if (playerData.getAlignment().equals(Utils.OrgMember.XALDIN)){
								player.level().addAlwaysVisibleParticle(new DustParticleOptions(new Vector3f(0.9f,0.9F,1F),1F),player.getX() + player.level().random.nextDouble() - 0.5D, player.getY()+ player.level().random.nextDouble() *2D, player.getZ() + player.level().random.nextDouble() - 0.5D, 0, 0, 0);
								player.level().addAlwaysVisibleParticle(ParticleTypes.POOF, player.getX() + player.level().random.nextDouble() - 0.5D, player.getY()+ player.level().random.nextDouble() *2D, player.getZ()  + player.level().random.nextDouble() - 0.5D, 3,3,3);
							}
							if (playerData.getAlignment().equals(Utils.OrgMember.VEXEN)){
								player.level().addAlwaysVisibleParticle(ParticleTypes.SNOWFLAKE, player.getX() + player.level().random.nextDouble() - 0.5D, player.getY()+ player.level().random.nextDouble() *2D, player.getZ()  + player.level().random.nextDouble() - 0.5D, 0,0,0);
								player.level().addAlwaysVisibleParticle(ParticleTypes.ITEM_SNOWBALL, player.getX() + player.level().random.nextDouble() - 0.5D, player.getY()+ player.level().random.nextDouble() *2D, player.getZ()  + player.level().random.nextDouble() - 0.5D, 0,0,0);
								player.level().addAlwaysVisibleParticle(new DustParticleOptions(new Vector3f(0F,0F,0F),1F),player.getX() + player.level().random.nextDouble() - 0.5D, player.getY()+ player.level().random.nextDouble() *2D, player.getZ() + player.level().random.nextDouble() - 0.5D, 0, 0, 0);
							}
							if (playerData.getAlignment().equals(Utils.OrgMember.LEXAEUS)){
								player.level().addAlwaysVisibleParticle(new DustParticleOptions(new Vector3f(0F,0F,0F),1F),player.getX() + player.level().random.nextDouble() - 0.5D, player.getY()+ player.level().random.nextDouble() *2D, player.getZ() + player.level().random.nextDouble() - 0.5D, 0, 0, 0);
								player.level().addAlwaysVisibleParticle(new DustParticleOptions(new Vector3f(1F,0.95F,0F),1F),player.getX() + player.level().random.nextDouble() - 0.5D, player.getY()+ player.level().random.nextDouble() *2D, player.getZ() + player.level().random.nextDouble() - 0.5D, 0, 0, 0);
								player.level().addAlwaysVisibleParticle(new DustParticleOptions(new Vector3f(1F,0.25F,0.35F),1F),player.getX() + player.level().random.nextDouble() - 0.5D, player.getY()+ player.level().random.nextDouble() *2D, player.getZ() + player.level().random.nextDouble() - 0.5D, 0, 0, 0);
							}
							if (playerData.getAlignment().equals(Utils.OrgMember.ZEXION)){
								player.level().addAlwaysVisibleParticle(ParticleTypes.SOUL_FIRE_FLAME, player.getX() + player.level().random.nextDouble() - 0.5D, player.getY()+ player.level().random.nextDouble() *2D, player.getZ()  + player.level().random.nextDouble() - 0.5D, 0,0,0);
								player.level().addAlwaysVisibleParticle(ParticleTypes.SOUL, player.getX() + player.level().random.nextDouble() - 0.5D, player.getY()+ player.level().random.nextDouble() *2D, player.getZ()  + player.level().random.nextDouble() - 0.5D, 0,0,0);
								player.level().addAlwaysVisibleParticle(new DustParticleOptions(new Vector3f(0F,0F,0F),1F),player.getX() + player.level().random.nextDouble() - 0.5D, player.getY()+ player.level().random.nextDouble() *2D, player.getZ() + player.level().random.nextDouble() - 0.5D, 0, 0, 0);
							}
							if (playerData.getAlignment().equals(Utils.OrgMember.SAIX)){
								player.level().addAlwaysVisibleParticle(ParticleTypes.SOUL_FIRE_FLAME, player.getX() + player.level().random.nextDouble() - 0.5D, player.getY()+ player.level().random.nextDouble() *2D, player.getZ()  + player.level().random.nextDouble() - 0.5D, 0,0,0);
								player.level().addAlwaysVisibleParticle(new DustParticleOptions(new Vector3f(0F,0F,0F),1F),player.getX() + player.level().random.nextDouble() - 0.5D, player.getY()+ player.level().random.nextDouble() *2D, player.getZ() + player.level().random.nextDouble() - 0.5D, 0, 0, 0);
								player.level().addAlwaysVisibleParticle(new DustParticleOptions(new Vector3f(0.2F,0.2F,1F),1F),player.getX() + player.level().random.nextDouble() - 0.5D, player.getY()+ player.level().random.nextDouble() *2D, player.getZ() + player.level().random.nextDouble() - 0.5D, 0, 0, 0);
							}
							if (playerData.getAlignment().equals(Utils.OrgMember.AXEL)){
								player.level().addAlwaysVisibleParticle(ParticleTypes.FLAME, player.getX() + player.level().random.nextDouble() - 0.5D, player.getY()+ player.level().random.nextDouble() *2D, player.getZ()  + player.level().random.nextDouble() - 0.5D, 0,0,0);
								player.level().addAlwaysVisibleParticle(ParticleTypes.SMALL_FLAME, player.getX() + player.level().random.nextDouble() - 0.5D, player.getY()+ player.level().random.nextDouble() *2D, player.getZ()  + player.level().random.nextDouble() - 0.5D, 0,0,0);
								player.level().addAlwaysVisibleParticle(new DustParticleOptions(new Vector3f(0F,0F,0F),1F),player.getX() + player.level().random.nextDouble() - 0.5D, player.getY()+ player.level().random.nextDouble() *2D, player.getZ() + player.level().random.nextDouble() - 0.5D, 0, 0, 0);
							}
							if (playerData.getAlignment().equals(Utils.OrgMember.DEMYX)){
								player.level().addAlwaysVisibleParticle(ParticleTypes.BUBBLE, player.getX() + player.level().random.nextDouble() - 0.5D, player.getY()+ player.level().random.nextDouble() *2D, player.getZ()  + player.level().random.nextDouble() - 0.5D, 0,0,0);
								player.level().addAlwaysVisibleParticle(ParticleTypes.NOTE, player.getX() + player.level().random.nextDouble() - 0.5D, player.getY()+ player.level().random.nextDouble() *2D, player.getZ()  + player.level().random.nextDouble() - 0.5D, 0,0,0);
								player.level().addAlwaysVisibleParticle(ParticleTypes.DRIPPING_WATER, player.getX() + player.level().random.nextDouble() - 0.5D, player.getY()+ player.level().random.nextDouble() *2D, player.getZ()  + player.level().random.nextDouble() - 0.5D, 0,0,0);
							}
							if (playerData.getAlignment().equals(Utils.OrgMember.LUXORD)){
								player.level().addAlwaysVisibleParticle(new DustParticleOptions(new Vector3f(0F,0F,0F),1F),player.getX() + player.level().random.nextDouble() - 0.5D, player.getY()+ player.level().random.nextDouble() *2D, player.getZ() + player.level().random.nextDouble() - 0.5D, 0, 0, 0);
								player.level().addAlwaysVisibleParticle(ParticleTypes.ENCHANT, player.getX() + player.level().random.nextDouble() - 0.5D, player.getY()+ player.level().random.nextDouble() *2D, player.getZ()  + player.level().random.nextDouble() - 0.5D, 0,0,0);
							}
							if (playerData.getAlignment().equals(Utils.OrgMember.MARLUXIA)){
								player.level().addAlwaysVisibleParticle(new DustParticleOptions(new Vector3f(1F,0.4F,0.5F),1F),player.getX() + player.level().random.nextDouble() - 0.5D, player.getY()+ player.level().random.nextDouble() *2D, player.getZ() + player.level().random.nextDouble() - 0.5D, 0, 0, 0);
								player.level().addAlwaysVisibleParticle(new DustParticleOptions(new Vector3f(1F,0.2F,0.3F),1F),player.getX() + player.level().random.nextDouble() - 0.5D, player.getY()+ player.level().random.nextDouble() *2D, player.getZ() + player.level().random.nextDouble() - 0.5D, 0, 0, 0);
							}
							if (playerData.getAlignment().equals(Utils.OrgMember.LARXENE)){
								player.level().addAlwaysVisibleParticle(ParticleTypes.ELECTRIC_SPARK, player.getX() + player.level().random.nextDouble() - 0.5D, player.getY()+ player.level().random.nextDouble() *2D, player.getZ()  + player.level().random.nextDouble() - 0.5D, 0,0,0);
								player.level().addAlwaysVisibleParticle(ParticleTypes.CRIT, player.getX() + player.level().random.nextDouble() - 0.5D, player.getY()+ player.level().random.nextDouble() *2D, player.getZ()  + player.level().random.nextDouble() - 0.5D, 0,0,0);
								player.level().addAlwaysVisibleParticle(new DustParticleOptions(new Vector3f(0F,0F,0F),1F),player.getX() + player.level().random.nextDouble() - 0.5D, player.getY()+ player.level().random.nextDouble() *2D, player.getZ() + player.level().random.nextDouble() - 0.5D, 0, 0, 0);
							}
							if (playerData.getAlignment().equals(Utils.OrgMember.ROXAS)){
								player.level().addAlwaysVisibleParticle(ParticleTypes.ENCHANTED_HIT, player.getX() + player.level().random.nextDouble() - 0.5D, player.getY()+ player.level().random.nextDouble() *2D, player.getZ()  + player.level().random.nextDouble() - 0.5D, 0,0,0);
								player.level().addAlwaysVisibleParticle(ParticleTypes.END_ROD, player.getX() + player.level().random.nextDouble() - 0.5D, player.getY()+ player.level().random.nextDouble() *2D, player.getZ()  + player.level().random.nextDouble() - 0.5D, 0,0,0);

							}



						} else if (globalData.getStepType() == StringsRM.twilightStepType){
							player.level().addAlwaysVisibleParticle(new DustParticleOptions(new Vector3f(1F,1F,1F),1F),player.getX() + player.level().random.nextDouble() - 0.5D, player.getY()+ player.level().random.nextDouble() *2D, player.getZ() + player.level().random.nextDouble() - 0.5D, 0, 0, 0);
							player.level().addAlwaysVisibleParticle(new DustParticleOptions(new Vector3f(0F,0F,0F),1F),player.getX() + player.level().random.nextDouble() - 0.5D, player.getY()+ player.level().random.nextDouble() *2D, player.getZ() + player.level().random.nextDouble() - 0.5D, 0, 0, 0);
							player.level().addAlwaysVisibleParticle(new DustParticleOptions(new Vector3f(0.5F,0.5F,0.5F),1F),player.getX() + player.level().random.nextDouble() - 0.5D, player.getY()+ player.level().random.nextDouble() *2D, player.getZ() + player.level().random.nextDouble() - 0.5D, 0, 0, 0);
						} else if (globalData.getStepType() == StringsRM.rageStepType){
							player.level().addParticle(new DustParticleOptions(new Vector3f(0.1F,0F,0F),1F),player.getX() + player.level().random.nextDouble() - 0.55D, player.getY()+ player.level().random.nextDouble() *2D, player.getZ() + player.level().random.nextDouble() - 0.55D, 0, 0, 0);
							player.level().addParticle(new DustParticleOptions(new Vector3f(0.3F,0F,0F),1F),player.getX() + player.level().random.nextDouble() - 0.55D, player.getY()+ player.level().random.nextDouble() *2D, player.getZ() + player.level().random.nextDouble() - 0.55D, 0, 0, 0);
							player.level().addParticle(new DustParticleOptions(new Vector3f(0F,0F,0F),1F),player.getX() + player.level().random.nextDouble() - 0.55D, player.getY()+ player.level().random.nextDouble() *2D, player.getZ() + player.level().random.nextDouble() - 0.55D, 0, 0, 0);
						} else if (globalData.getStepType() == StringsRM.darkStepType && !playerData.isFormActive(ModDriveFormsRM.TWILIGHT)) {
							player.level().addAlwaysVisibleParticle(ParticleTypes.SQUID_INK, player.getX() + player.level().random.nextDouble() - 0.5D, player.getY()+ player.level().random.nextDouble() *2D, player.getZ() + player.level().random.nextDouble() - 0.5D, 0, 0, 0);
							player.level().addAlwaysVisibleParticle(new DustParticleOptions(new Vector3f(0.5F,0F,0.5F),1F),player.getX() + player.level().random.nextDouble() - 0.5D, player.getY()+ player.level().random.nextDouble() *2D, player.getZ() + player.level().random.nextDouble() - 0.5D, 0, 0, 0);
							player.level().addAlwaysVisibleParticle(new DustParticleOptions(new Vector3f(0.5F,0F,1F),1F),player.getX() + player.level().random.nextDouble() - 0.5D, player.getY()+ player.level().random.nextDouble() *2D, player.getZ() + player.level().random.nextDouble() - 0.5D, 0, 0, 0);
							player.level().addAlwaysVisibleParticle(new DustParticleOptions(new Vector3f(0.2F,0F,0F),1F),player.getX() + player.level().random.nextDouble() - 0.55D, player.getY()+ player.level().random.nextDouble() *2D, player.getZ() + player.level().random.nextDouble() - 0.55D, 0, 0, 0);
						} else if (globalData.getStepType() == StringsRM.lightStepType
								&& !playerData.isFormActive(ModDriveFormsRM.TWILIGHT)) {

							UUID uuid = player.getUUID();


							int lastParticleTick =
									LAST_LIGHT_STEP_PARTICLE_TICK.getOrDefault(
											uuid,
											Integer.MIN_VALUE
									);

							if (lastParticleTick != player.tickCount) {
								LAST_LIGHT_STEP_PARTICLE_TICK.put(
										uuid,
										player.tickCount
								);

								Vec3 movement = player.getDeltaMovement();

								Vec3 direction =
										movement.lengthSqr() > 1.0E-6D
												? movement.normalize()
												: player.getLookAngle().normalize();


								double trailSpeed = 0.10D;

								double velocityX =
										-direction.x * trailSpeed;

								double velocityY =
										-direction.y * trailSpeed * 0.35D;

								double velocityZ =
										-direction.z * trailSpeed;

								RandomSource random =
										player.level().random;


								double baseX = player.getX();
								double baseY = player.getY() + 0.9D;
								double baseZ = player.getZ();


								for (int i = 0; i < 3; i++) {
									double spreadX =
											(random.nextDouble() - 0.5D) * 0.35D;

									double spreadY =
											(random.nextDouble() - 0.5D) * 0.45D;

									double spreadZ =
											(random.nextDouble() - 0.5D) * 0.35D;

									player.level().addAlwaysVisibleParticle(
											new DustParticleOptions(
													new Vector3f(
															0.0F,
															0.9F,
															0.9F
													),
													0.75F
											),
											baseX + spreadX,
											baseY + spreadY,
											baseZ + spreadZ,
											velocityX,
											velocityY,
											velocityZ
									);
								}


								for (int i = 0; i < 2; i++) {
									double spreadX =
											(random.nextDouble() - 0.5D) * 0.45D;

									double spreadY =
											(random.nextDouble() - 0.5D) * 0.50D;

									double spreadZ =
											(random.nextDouble() - 0.5D) * 0.45D;

									player.level().addAlwaysVisibleParticle(
											new DustParticleOptions(
													new Vector3f(
															1.0F,
															1.0F,
															0.7F
													),
													0.65F
											),
											baseX + spreadX,
											baseY + spreadY,
											baseZ + spreadZ,
											velocityX * 0.8D,
											velocityY * 0.8D,
											velocityZ * 0.8D
									);
								}


								player.level().addAlwaysVisibleParticle(
										ParticleTypes.END_ROD,
										baseX
												+ (random.nextDouble() - 0.5D) * 0.25D,
										baseY
												+ (random.nextDouble() - 0.5D) * 0.35D,
										baseZ
												+ (random.nextDouble() - 0.5D) * 0.25D,
										velocityX * 0.5D,
										velocityY * 0.5D,
										velocityZ * 0.5D
								);
							}
						}
					}

					// Rage Form Active and Walk particles
					if (playerData.isFormActive(ModDriveFormsRM.RAGE)){
						player.level().addParticle(new DustParticleOptions(new Vector3f(0.1F,0F,0F),1F),player.getX() + player.level().random.nextDouble() - 0.55D, player.getY()+ player.level().random.nextDouble() *2D, player.getZ() + player.level().random.nextDouble() - 0.55D, 0, 0, 0);

						if (player.onGround()){
							player.level().addAlwaysVisibleParticle(new DustParticleOptions(new Vector3f(0.2F,0F,0F),1F),player.getX(), player.getY(), player.getZ(), 0, 0, 0);

						}
					}

					// Regen Form Active
					if (playerData.isFormActive(ModDriveFormsRM.REGEN)){
						player.level().addParticle(new DustParticleOptions(new Vector3f(0f,0f,0f),1),player.getX() + player.level().random.nextDouble() - 0.45D, player.getY()+ player.level().random.nextDouble() *2D, player.getZ() + player.level().random.nextDouble() - 0.45D, -1, -1, -1);
						player.level().addParticle(new DustParticleOptions(new Vector3f(1f,1f,1f),1),player.getX() + player.level().random.nextDouble() - 0.45D, player.getY()+ player.level().random.nextDouble() *2D, player.getZ() + player.level().random.nextDouble() - 0.45D, -1, -1, -1);

					}

					// Twilight Form Active
					if (playerData.isFormActive(ModDriveFormsRM.TWILIGHT)){
						player.level().addParticle(new DustParticleOptions(new Vector3f(0.45F,0.45F,0.45F),0.25F),player.getX() + player.level().random.nextDouble() - 0.45D, player.getY()+ player.level().random.nextDouble() *2D, player.getZ() + player.level().random.nextDouble() - 0.45D, -1, -1, -1);
						player.level().addParticle(new DustParticleOptions(new Vector3f(0.55F,0.55F,0.55F),0.25F),player.getX() + player.level().random.nextDouble() - 0.45D, player.getY()+ player.level().random.nextDouble() *2D, player.getZ() + player.level().random.nextDouble() - 0.45D, -1, -1, -1);

					}

					// Firestorm Active
					if (playerData.isFormActive(ModDriveFormsRM.FIRESTORM)){
						player.level().addAlwaysVisibleParticle(ParticleTypes.SMALL_FLAME, player.getX() + player.level().random.nextDouble() - 0.5D, player.getY()+ player.level().random.nextDouble() *2D, player.getZ()  + player.level().random.nextDouble() - 0.5D, 0,0,0);
					}

					// Diamond Dust Active
					if (playerData.isFormActive(ModDriveFormsRM.DIAMOND_DUST)){
						player.level().addAlwaysVisibleParticle(ParticleTypes.SNOWFLAKE, player.getX() + player.level().random.nextDouble() - 0.5D, player.getY()+ player.level().random.nextDouble() *2D, player.getZ()  + player.level().random.nextDouble() - 0.5D, 0,0,0);
					}

					// Thunder Bolt Active
					if (playerData.isFormActive(ModDriveFormsRM.THUNDER_BOLT)){
						player.level().addAlwaysVisibleParticle(ParticleTypes.ELECTRIC_SPARK, player.getX() + player.level().random.nextDouble() - 0.5D, player.getY()+ player.level().random.nextDouble() *2D, player.getZ()  + player.level().random.nextDouble() - 0.5D, 0,0,0);
					}

					// Fever Pitch Active
					if (playerData.isFormActive(ModDriveFormsRM.FEVER_PITCH)){
						player.level().addParticle(new DustParticleOptions(new Vector3f(0f,1f,0.50F),0.5f),player.getX() + player.level().random.nextDouble() - 0.45D, player.getY()+ player.level().random.nextDouble() *2D, player.getZ() + player.level().random.nextDouble() - 0.45D, -1, -1, -1);
						player.level().addParticle(new DustParticleOptions(new Vector3f(0f,1f,0.85F),0.25f),player.getX() + player.level().random.nextDouble() - 0.45D, player.getY()+ player.level().random.nextDouble() *2D, player.getZ() + player.level().random.nextDouble() - 0.45D, -1, -1, -1);

					}

					// Critical Impact Active
					if (playerData.isFormActive(ModDriveFormsRM.CRITICAL_IMPACT)){
						player.level().addParticle(new DustParticleOptions(new Vector3f(0.75f,0.75f,0.15F),0.5f),player.getX() + player.level().random.nextDouble() - 0.45D, player.getY()+ player.level().random.nextDouble() *2D, player.getZ() + player.level().random.nextDouble() - 0.45D, -1, -1, -1);
						player.level().addParticle(new DustParticleOptions(new Vector3f(0.45f,0.45f,0f),0.35f),player.getX() + player.level().random.nextDouble() - 0.45D, player.getY()+ player.level().random.nextDouble() *2D, player.getZ() + player.level().random.nextDouble() - 0.45D, -1, -1, -1);

					}
					// Spellweaver Active
					if (playerData.isFormActive(ModDriveFormsRM.SPELLWEAVER)){
						player.level().addParticle(new DustParticleOptions(new Vector3f(0.95f,0.75f,0.95F),0.5f),player.getX() + player.level().random.nextDouble() - 0.45D, player.getY()+ player.level().random.nextDouble() *2D, player.getZ() + player.level().random.nextDouble() - 0.45D, -1, -1, -1);
						player.level().addParticle(new DustParticleOptions(new Vector3f(0.45f,0.65f,65f),0.35f),player.getX() + player.level().random.nextDouble() - 0.45D, player.getY()+ player.level().random.nextDouble() *2D, player.getZ() + player.level().random.nextDouble() - 0.45D, -1, -1, -1);

					}

					// Dark Impulse Active
					if (playerData.isFormActive(ModDriveFormsRM.DARK_IMPULSE)){
						player.level().addParticle(new DustParticleOptions(new Vector3f(0.25f,0.0f,0.0F),0.2f),player.getX() + player.level().random.nextDouble() - 0.45D, player.getY()+ player.level().random.nextDouble() *2D, player.getZ() + player.level().random.nextDouble() - 0.45D, -1, -1, -1);
						player.level().addParticle(new DustParticleOptions(new Vector3f(0.50f,0.0f,0.50f),0.35f),player.getX() + player.level().random.nextDouble() - 0.45D, player.getY()+ player.level().random.nextDouble() *2D, player.getZ() + player.level().random.nextDouble() - 0.45D, -1, -1, -1);
						player.level().addParticle(new DustParticleOptions(new Vector3f(0f,0f,0f),0.65f),player.getX() + player.level().random.nextDouble() - 0.45D, player.getY()+ player.level().random.nextDouble() *2D, player.getZ() + player.level().random.nextDouble() - 0.45D, -1, -1, -1);

					}

					// Bloodlust Active
					if (playerData.isFormActive(ModDriveFormsRM.BLOOSTLUST)){
						player.level().addParticle(new DustParticleOptions(new Vector3f(0.95f,0f,0f),0.5f),player.getX() + player.level().random.nextDouble() - 0.45D, player.getY()+ player.level().random.nextDouble() *2D, player.getZ() + player.level().random.nextDouble() - 0.45D, -1, -1, -1);
						player.level().addParticle(new DustParticleOptions(new Vector3f(0.25f,0f,0f),0.65f),player.getX() + player.level().random.nextDouble() - 0.45D, player.getY()+ player.level().random.nextDouble() *2D, player.getZ() + player.level().random.nextDouble() - 0.45D, -1, -1, -1);
						player.level().addParticle(new DustParticleOptions(new Vector3f(0.5f,0f,0f),0.35f),player.getX() + player.level().random.nextDouble() - 0.45D, player.getY()+ player.level().random.nextDouble() *2D, player.getZ() + player.level().random.nextDouble() - 0.45D, -1, -1, -1);
					}

					// Draconic Liberation Active
					if (playerData.isFormActive(ModDriveFormsRM.DRACONIC_LIBERATION)){
						player.level().addParticle(new DustParticleOptions(new Vector3f(0.75f,0f,0.75f),0.5f),player.getX() + player.level().random.nextDouble() - 0.45D, player.getY()+ player.level().random.nextDouble() *2D, player.getZ() + player.level().random.nextDouble() - 0.45D, -1, -1, -1);
						player.level().addParticle(new DustParticleOptions(new Vector3f(0f,0f,0f),0.65f),player.getX() + player.level().random.nextDouble() - 0.45D, player.getY()+ player.level().random.nextDouble() *2D, player.getZ() + player.level().random.nextDouble() - 0.45D, -1, -1, -1);
						player.level().addParticle(new DustParticleOptions(new Vector3f(0.25f,0f,0.25f),0.35f),player.getX() + player.level().random.nextDouble() - 0.45D, player.getY()+ player.level().random.nextDouble() *2D, player.getZ() + player.level().random.nextDouble() - 0.45D, -1, -1, -1);
						player.level().addParticle(new DustParticleOptions(new Vector3f(1f,1f,1f),0.35f),player.getX() + player.level().random.nextDouble() - 0.45D, player.getY()+ player.level().random.nextDouble() *2D, player.getZ() + player.level().random.nextDouble() - 0.45D, -1, -1, -1);
					}

					if (playerData.isFormActive(ModDriveFormsRM.EXSOLDIER)){
						if (player.getHealth() > 0.25F) {
							player.level().addParticle(new DustParticleOptions(new Vector3f(0.0f, 0.65f, 0.65f), 0.5f), player.getX() + player.level().random.nextDouble() - 0.45D, player.getY() + player.level().random.nextDouble() * 2D, player.getZ() + player.level().random.nextDouble() - 0.45D, -1, -1, -1);
							player.level().addParticle(new DustParticleOptions(new Vector3f(0.0f, 0.55f, 0.85f), 0.5f), player.getX() + player.level().random.nextDouble() - 0.45D, player.getY() + player.level().random.nextDouble() * 2D, player.getZ() + player.level().random.nextDouble() - 0.45D, -1, -1, -1);
							player.level().addParticle(new DustParticleOptions(new Vector3f(0.0f, 0.85f, 0.55f), 0.5f), player.getX() + player.level().random.nextDouble() - 0.45D, player.getY() + player.level().random.nextDouble() * 2D, player.getZ() + player.level().random.nextDouble() - 0.45D, -1, -1, -1);
						} else {
							player.level().addParticle(new DustParticleOptions(new Vector3f(0.0f, 0.65f, 0.65f), 0.75f), player.getX() + player.level().random.nextDouble() - 0.45D, player.getY() + player.level().random.nextDouble() * 2D, player.getZ() + player.level().random.nextDouble() - 0.45D, -1, -1, -1);
							player.level().addParticle(new DustParticleOptions(new Vector3f(0.0f, 0.55f, 0.85f), 0.75f), player.getX() + player.level().random.nextDouble() - 0.45D, player.getY() + player.level().random.nextDouble() * 2D, player.getZ() + player.level().random.nextDouble() - 0.45D, -1, -1, -1);
							player.level().addParticle(new DustParticleOptions(new Vector3f(0.0f, 0.85f, 0.55f), 0.75f), player.getX() + player.level().random.nextDouble() - 0.45D, player.getY() + player.level().random.nextDouble() * 2D, player.getZ() + player.level().random.nextDouble() - 0.45D, -1, -1, -1);
						}

					}


					// When I can get particles in other hand
					//if (playerData.getActiveDriveForm().equals(ModDriveFormsRM.DARK.get().getRegistryName().toString())){
					//player.level().addAlwaysVisibleParticle(new DustParticleOptions(new Vector3f(0.5F,0F,0.5F),1F),player.getX(), player.getY()+ player.level().random.nextDouble() *2D, player.getZ() + player.level().random.nextDouble() - 0.5D, 0, 0, 0);
					//}

					// Spellblade Visual Effects

					if (playerData.isAbilityEquipped(ModAbilitiesRM.SPELLBLADE)) {
						if (playerData.getNumberOfAbilitiesEquipped(ModAbilities.FIRE_BOOST) >= 4) {
							player.level().addParticle(new DustParticleOptions(new Vector3f(0.55F, 0.0f, 0.0F), 0.25F), player.getX() + player.level().random.nextDouble() - 0.45D, player.getY() + player.level().random.nextDouble() * 2D, player.getZ() + player.level().random.nextDouble() - 0.45D, -1, -1, -1);
						}
						if (playerData.getNumberOfAbilitiesEquipped(ModAbilities.BLIZZARD_BOOST) >= 4) {
							player.level().addParticle(new DustParticleOptions(new Vector3f(0.0F, 0.95f, 1F), 0.25F), player.getX() + player.level().random.nextDouble() - 0.45D, player.getY() + player.level().random.nextDouble() * 2D, player.getZ() + player.level().random.nextDouble() - 0.45D, -1, -1, -1);
						}
						if (playerData.getNumberOfAbilitiesEquipped(ModAbilities.THUNDER_BOOST) >= 4) {
							player.level().addParticle(new DustParticleOptions(new Vector3f(1.0F, 1.00f, 0F), 0.25F), player.getX() + player.level().random.nextDouble() - 0.45D, player.getY() + player.level().random.nextDouble() * 2D, player.getZ() + player.level().random.nextDouble() - 0.45D, -1, -1, -1);
						}
						if (playerData.getNumberOfAbilitiesEquipped(ModAbilities.WATER_BOOST) >= 4) {
							player.level().addAlwaysVisibleParticle(ParticleTypes.BUBBLE, player.getX() + player.level().random.nextDouble() - 0.5D, player.getY() + player.level().random.nextDouble() * 2D, player.getZ() + player.level().random.nextDouble() - 0.5D, 0, 0, 0);
						}
					}

					// Haste and Slow Visual
					if (globalData != null) {

						if (globalData.getHasteTicks() > 0) {
							player.level().addParticle(new DustParticleOptions(new Vector3f(1F, 0.83F, 0F), 0.25F), player.getX() + player.level().random.nextDouble() - 0.45D, player.getY() + player.level().random.nextDouble() * 2D, player.getZ() + player.level().random.nextDouble() - 0.45D, -1, -1, -1);
							//System.out.println("Haste is active");
						}

						if (globalData.getSlowTicks() > 0) {
							player.level().addParticle(new DustParticleOptions(new Vector3f(0F, 0.83F, 1F), 0.25F), player.getX() + player.level().random.nextDouble() - 0.45D, player.getY() + player.level().random.nextDouble() * 2D, player.getZ() + player.level().random.nextDouble() - 0.45D, -1, -1, -1);

						}
					}
				}
			}
		}
	}


	@SubscribeEvent
	public void onRenderLightStepTrails(RenderLevelStageEvent event) {
		if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES) {
			return;
		}

		Minecraft mc = Minecraft.getInstance();

		if (mc.level == null || mc.player == null) {
			LIGHT_STEP_TRAILS.clear();
			return;
		}

		PoseStack poseStack = event.getPoseStack();
		MultiBufferSource.BufferSource buffer =
				mc.renderBuffers().bufferSource();

		Vec3 cameraPos =
				mc.gameRenderer.getMainCamera().getPosition();

		float partialTick =
				event.getPartialTick()
						.getGameTimeDeltaPartialTick(false);

		Set<UUID> playersStillLoaded = new HashSet<>();

		for (Player player : mc.level.players()) {
			playersStillLoaded.add(player.getUUID());


			if (player.distanceToSqr(mc.player) > 100.0D * 100.0D) {
				LIGHT_STEP_TRAILS.remove(player.getUUID());
				continue;
			}

			GlobalDataRM globalData =
					ModDataRM.getGlobal(player);

			if (globalData == null) {
				LIGHT_STEP_TRAILS.remove(player.getUUID());
				continue;
			}

			boolean lightStepActive =
					globalData.getStepTicks() > 0
							&& globalData.getStepType() == StringsRM.lightStepType;

			LightStepTrailSet trails =
					LIGHT_STEP_TRAILS.get(player.getUUID());


			if (trails == null) {
				if (!lightStepActive) {
					continue;
				}

				trails = new LightStepTrailSet();
				LIGHT_STEP_TRAILS.put(
						player.getUUID(),
						trails
				);
			}


			if (trails.lastUpdatedTick != player.tickCount) {
				if (lightStepActive) {

					trails.swirlPhase += LIGHT_STEP_SWIRL_SPEED;

					trails.center.pushHead(
							getLightStepTrailPoint(
									player,
									trails.swirlPhase + LIGHT_STEP_CYAN_PHASE
							)
					);

					trails.left.pushHead(
							getLightStepTrailPoint(
									player,
									trails.swirlPhase + LIGHT_STEP_GOLD_LEFT_PHASE
							)
					);

					trails.right.pushHead(
							getLightStepTrailPoint(
									player,
									trails.swirlPhase + LIGHT_STEP_GOLD_RIGHT_PHASE
							)
					);
				} else {
					trails.center.pushHead(null);
					trails.left.pushHead(null);
					trails.right.pushHead(null);
				}

				trails.lastUpdatedTick =
						player.tickCount;
			}

			boolean hasTrail =
					hasTrailPoints(trails.center)
							|| hasTrailPoints(trails.left)
							|| hasTrailPoints(trails.right);

			if (!hasTrail) {
				LIGHT_STEP_TRAILS.remove(
						player.getUUID()
				);
				continue;
			}


			poseStack.pushPose();
			{
				poseStack.translate(
						-cameraPos.x,
						-cameraPos.y,
						-cameraPos.z
				);


				renderLightStepTrail(
						trails.center,
						partialTick,
						poseStack,
						buffer,
						0.0F,
						0.9F,
						0.9F
				);

				renderLightStepTrail(
						trails.left,
						partialTick,
						poseStack,
						buffer,
						1.0F,
						1.0F,
						0.7F
				);

				renderLightStepTrail(
						trails.right,
						partialTick,
						poseStack,
						buffer,
						1.0F,
						1.0F,
						0.7F
				);
			}
			poseStack.popPose();
		}


		LIGHT_STEP_TRAILS.keySet().removeIf(
				uuid -> !playersStillLoaded.contains(uuid)
		);
	}

	private static Vec3 getLightStepTrailPoint(
			Player player,
			double phase
	) {

		Vec3 tangent =
				player.getDeltaMovement();

		if (tangent.lengthSqr() < 1.0E-6D) {
			tangent = player.getLookAngle();
		}

		if (tangent.lengthSqr() < 1.0E-6D) {
			tangent = new Vec3(0.0D, 0.0D, 1.0D);
		}

		tangent = tangent.normalize();


		Vec3 referenceUp =
				Math.abs(tangent.y) > 0.95D
						? new Vec3(1.0D, 0.0D, 0.0D)
						: new Vec3(0.0D, 1.0D, 0.0D);

		Vec3 right =
				tangent.cross(referenceUp);

		if (right.lengthSqr() < 1.0E-6D) {
			right = new Vec3(1.0D, 0.0D, 0.0D);
		} else {
			right = right.normalize();
		}


		Vec3 swirlUp =
				right.cross(tangent);

		if (swirlUp.lengthSqr() < 1.0E-6D) {
			swirlUp = new Vec3(0.0D, 1.0D, 0.0D);
		} else {
			swirlUp = swirlUp.normalize();
		}

		double cos =
				Math.cos(phase);

		double sin =
				Math.sin(phase);

		Vec3 radialOffset =
				right.scale(cos * LIGHT_STEP_SWIRL_RADIUS)
						.add(
								swirlUp.scale(
										sin * LIGHT_STEP_SWIRL_RADIUS
								)
						);


		return player.position()
				.add(
						0.0D,
						LIGHT_STEP_TRAIL_HEIGHT,
						0.0D
				)
				.add(radialOffset);
	}

	private static void renderLightStepTrail(
			TrailRenderer.Trail trail,
			float partialTick,
			PoseStack poseStack,
			MultiBufferSource.BufferSource buffer,
			float r,
			float g,
			float b
	) {
		Vec3[] points =
				trail.interpolated(partialTick);

		TrailRenderer.render(
				points,
				Vec3.ZERO,
				poseStack.last().pose(),
				buffer.getBuffer(
						RenderType.debugQuads()
				),
				r,
				g,
				b,
				LIGHT_STEP_TRAIL_WIDTH
		);
	}

	private static boolean hasTrailPoints(
			TrailRenderer.Trail trail
	) {
		for (Vec3 point : trail.points) {
			if (point != null) {
				return true;
			}
		}

		return false;
	}

	// Dark Step Stuff

	/*
	 * Render the afterimage independently of the real player's render.
	 *
	 * This matters especially in first person: Minecraft normally does not
	 * render the local player's body there, so RenderLivingEvent.Pre is not a
	 * reliable place to draw an origin ghost. AFTER_ENTITIES runs regardless
	 * of whether the local player model itself was rendered.
	 */
	/*
	 * Render the Dark Step afterimage directly as a PlayerModel.
	 *
	 * We deliberately DO NOT call EntityRenderDispatcher#render here.
	 * A fake RemotePlayer goes through every normal/modded player-render hook,
	 * which is what was leaving the afterimage vertically inverted in this
	 * render stage (and also allowed a name tag to appear).
	 *
	 * Instead we use the normal player model + the same core transform vanilla
	 * LivingEntityRenderer uses:
	 *
	 *   rotate to body yaw
	 *   scale(-1, -1, 1)
	 *   translate(0, -1.501, 0)
	 *
	 * That makes the saved origin the ghost's FEET position, so the model
	 * extends upward from the ground instead of downward into it.
	 */
	@SubscribeEvent
	public void onRenderDarkStepAfterImages(RenderLevelStageEvent event) {
		if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES) {
			return;
		}

		if (DARK_STEP_AFTER_IMAGES.isEmpty()) {
			return;
		}

		Minecraft mc = Minecraft.getInstance();

		if (mc.level == null || mc.player == null) {
			DARK_STEP_AFTER_IMAGES.clear();
			return;
		}

		float partialTick =
				event.getPartialTick()
						.getGameTimeDeltaPartialTick(false);

		Vec3 cameraPos =
				mc.gameRenderer
						.getMainCamera()
						.getPosition();

		PoseStack poseStack =
				event.getPoseStack();

		MultiBufferSource.BufferSource buffer =
				mc.renderBuffers()
						.bufferSource();

		ArrayList<UUID> expired =
				new ArrayList<>();

		for (Map.Entry<UUID, DarkStepAfterImage> entry
				: DARK_STEP_AFTER_IMAGES.entrySet()) {

			DarkStepAfterImage image =
					entry.getValue();

			float age =
					(mc.level.getGameTime() - image.startGameTime)
							+ partialTick;

			if (age >= DARK_STEP_AFTERIMAGE_TICKS) {
				expired.add(entry.getKey());
				continue;
			}

			float fade =
					1.0F
							- age / DARK_STEP_AFTERIMAGE_TICKS;

			/*
			 * Slightly stronger than the old invisible-entity pass so the
			 * silhouette reads clearly against terrain.
			 */
			float alpha =
					0.72F * fade;

			int alphaByte =
					Mth.clamp(
							(int) (alpha * 255.0F),
							0,
							255
					);

			/*
			 * Purple-blue Dark Step tint.
			 *
			 * ARGB: alpha, red, green, blue.
			 */
			int ghostColor =
					(alphaByte << 24)
							| (125 << 16)
							| (55 << 8)
							| 255;

			PlayerRenderer renderer =
					(PlayerRenderer) mc
							.getEntityRenderDispatcher()
							.getRenderer(image.ghost);

			PlayerModel<AbstractClientPlayer> model =
					renderer.getModel();

			/*
			 * Freeze animation around the pose captured when Dark Step began.
			 * We use zero limb travel so it reads as an afterimage rather than
			 * continuing to run in place after the teleport.
			 */
			model.prepareMobModel(
					image.ghost,
					0.0F,
					0.0F,
					partialTick
			);

			float bodyYaw =
					Mth.rotLerp(
							partialTick,
							image.ghost.yBodyRotO,
							image.ghost.yBodyRot
					);

			float headYaw =
					Mth.rotLerp(
							partialTick,
							image.ghost.yHeadRotO,
							image.ghost.yHeadRot
					);

			float netHeadYaw =
					Mth.wrapDegrees(
							headYaw - bodyYaw
					);

			float headPitch =
					Mth.lerp(
							partialTick,
							image.ghost.xRotO,
							image.ghost.getXRot()
					);

			model.setupAnim(
					image.ghost,
					0.0F,
					0.0F,
					image.ghost.tickCount + partialTick,
					netHeadYaw,
					headPitch
			);

			poseStack.pushPose();
			{
				/*
				 * World position relative to the active camera.
				 */
				poseStack.translate(
						image.origin.x - cameraPos.x,
						image.origin.y - cameraPos.y,
						image.origin.z - cameraPos.z
				);

				/*
				 * PlayerRenderer normally offsets crouching players slightly.
				 */
				if (image.ghost.isCrouching()) {
					poseStack.translate(
							0.0D,
							-0.125D,
							0.0D
					);
				}

				poseStack.mulPose(
						Axis.YP.rotationDegrees(
								180.0F - bodyYaw
						)
				);

				poseStack.scale(
						-1.0F,
						-1.0F,
						1.0F
				);

				poseStack.translate(
						0.0D,
						-1.501D,
						0.0D
				);

				VertexConsumer consumer =
						buffer.getBuffer(
								RenderType.entityTranslucent(
										image.ghost
												.getSkin()
												.texture()
								)
						);

				model.renderToBuffer(
						poseStack,
						consumer,
						0x00F000F0,
						OverlayTexture.NO_OVERLAY,
						ghostColor
				);
			}
			poseStack.popPose();
		}

		for (UUID uuid : expired) {
			DARK_STEP_AFTER_IMAGES.remove(uuid);
		}
	}

	@SubscribeEvent
	public void onLivingUpdate(EntityTickEvent.Pre event) {
		if (event.getEntity() instanceof Player player) {
			if (player.hasEffect(ModMobEffectsRM.STONE)) {
				if (event.getEntity().level().isClientSide && player == Minecraft.getInstance().player) {
					if (Minecraft.getInstance().screen == null)
						Minecraft.getInstance().setScreen(new StopGui());
				}
				event.setCanceled(true);
			}

			PlayerData playerData = PlayerData.get(player);
			if (playerData != null){
				if (player.hasEffect(ModMobEffectsRM.CONFUSE)) {
					MobEffectInstance confuse = player.getEffect(ModMobEffectsRM.CONFUSE);
					int amp = confuse.getAmplifier();
					RandomSource rand = player.getRandom();

					if (rand.nextInt(Math.max(2, 14 - amp)) == 0) {
						CommandMenuGui.down();
					}
					if (rand.nextInt(Math.max(5, 15 - amp)) == 0) {
						CommandMenuGui.up();
					}
					if (rand.nextInt(Math.max(3, 18 - amp)) == 0) {
						if (rand.nextInt(Math.max(5, 15 - amp)) != 0) {
							if (playerData.getEquippedItems() != null) {
								CommandMenuGui.enter();
							}
						}
					}
					if (rand.nextInt(Math.max(4, 16 - amp)) == 0) {
						if (rand.nextInt(Math.max(5, 15 - amp)) != 0) {
							CommandMenuGui.cancel();
						}
					}
				}
			}
		}
	}
}
