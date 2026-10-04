package online.remind.remind.driveform;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import online.kingdomkeys.kingdomkeys.config.ModConfigs;
import online.kingdomkeys.kingdomkeys.data.PlayerData;
import online.kingdomkeys.kingdomkeys.driveform.DriveForm;
import online.kingdomkeys.kingdomkeys.item.ModItems;
import online.kingdomkeys.kingdomkeys.network.PacketHandler;
import online.kingdomkeys.kingdomkeys.network.stc.SCSyncPlayerData;
import online.remind.remind.KingdomKeysReMind;
import online.remind.remind.ability.ModAbilitiesRM;
import online.remind.remind.lib.StringsRM;

@EventBusSubscriber(modid = KingdomKeysReMind.MODID)
public class DriveFormTwilight extends DriveForm {
	public DriveFormTwilight(ResourceLocation registryName, int order, ResourceLocation skinRL, boolean hasKeychain, boolean baseGrowthAbilities) {
		super(registryName, order, hasKeychain, baseGrowthAbilities);
		this.color = new float[] {0.25F,0.25F,0.25F};
		this.skinRL = skinRL;
	}

	private static final TagKey<EntityType<?>> BOSS_TAG = TagKey.create(
			Registries.ENTITY_TYPE,
			ResourceLocation.fromNamespaceAndPath("c", "bosses")
	);

	// Twilight Form EXP Gain
	@SubscribeEvent
	public static void getTwilightFormXP(LivingDeathEvent event) {
		if (!event.getEntity().level().isClientSide && event.getEntity() instanceof LivingEntity killed) {
			// Logic to detect if the mob is a boss (Tag check)
			if (killed.getType().is(BOSS_TAG)) {
				if (event.getSource().getEntity() instanceof Player player) {
					PlayerData playerData = PlayerData.get(player);
					if (playerData != null && playerData.isFormActive(ModDriveFormsRM.TWILIGHT)) {
						double mult = 2;
						playerData.setDriveFormExp(player, playerData.getActiveDriveForm(), (int) (playerData.getDriveFormExp(playerData.getActiveDriveForm()) + (5 * mult)));
						PacketHandler.sendTo(new SCSyncPlayerData(player), (ServerPlayer) player);
					}
				}
			}
		}
	}

	@Override
	public boolean isSlotVisible(Player player) {
		PlayerData playerData = PlayerData.get(player);
		if (playerData != null) {
			if (playerData.isAbilityEquipped(ModAbilitiesRM.ROAD_TO_DAWN)) {
				return true;
			} else if (playerData.getDriveFormLevel(ModDriveFormsRM.DARK.location()) == 7 && playerData.getDriveFormLevel(ModDriveFormsRM.LIGHT.location()) == 7) {
				if (playerData.isFormActive(ModDriveFormsRM.DARK))
					if (playerData.getEquippedKeychain(DriveForm.NONE).getItem() == ModItems.oblivionChain.get()) {
						return true;
					}

				if (playerData.isFormActive(ModDriveFormsRM.LIGHT))
					if (playerData.getEquippedKeychain(DriveForm.NONE).getItem() == ModItems.oathkeeperChain.get()) {
						return true;
					}

				return playerData.isFormActive(ModDriveFormsRM.TWILIGHT);
			}

		}
		return false;
	}

	@Override
	public boolean displayInCommandMenu(Player player) {
		PlayerData playerData = PlayerData.get(player);
		if (playerData != null) {
			if (playerData.isAbilityEquipped(ModAbilitiesRM.ROAD_TO_DAWN)){
				return true;
			}
		}
		return false;
	}
}