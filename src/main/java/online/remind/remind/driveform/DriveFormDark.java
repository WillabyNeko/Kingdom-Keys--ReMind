package online.remind.remind.driveform;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import online.kingdomkeys.kingdomkeys.config.ModConfigs;
import online.kingdomkeys.kingdomkeys.data.PlayerData;
import online.kingdomkeys.kingdomkeys.driveform.DriveForm;
import online.kingdomkeys.kingdomkeys.driveform.ModDriveForms;
import online.kingdomkeys.kingdomkeys.network.PacketHandler;
import online.kingdomkeys.kingdomkeys.network.stc.SCSyncPlayerData;
import online.remind.remind.KingdomKeysReMind;
import online.remind.remind.ability.ModAbilitiesRM;
import online.remind.remind.capabilities.GlobalDataRM;
import online.remind.remind.capabilities.ModDataRM;
import online.remind.remind.lib.StringsRM;
import online.remind.remind.network.PacketHandlerRM;

@EventBusSubscriber(modid = KingdomKeysReMind.MODID)
public class DriveFormDark extends DriveForm {

    public DriveFormDark(ResourceLocation registeryName, int order, ResourceLocation skinRL, boolean hasKeychain, boolean baseGrowthAbilities) {
        super(registeryName, order, hasKeychain, baseGrowthAbilities);
        this.color = new float[]{0.25F, 0F, 0.25F};
        this.skinRL = skinRL;
    }

    @SubscribeEvent
    public static void getDarkModeXP(LivingDeathEvent event) {
        if (!event.getEntity().level().isClientSide && event.getEntity() instanceof Monster) {
            if (event.getSource().getEntity() instanceof Player) {
                Player player = (Player) event.getSource().getEntity();
                PlayerData playerData = PlayerData.get(player);
                GlobalDataRM formData = ModDataRM.getGlobal(player);

                if (playerData != null && playerData.isFormActive(ModDriveFormsRM.DARK)) {
                    double mult = 1;
                    //double mult = 1;
                    playerData.setDriveFormExp(player, playerData.getActiveDriveForm(), (int) (playerData.getDriveFormExp(playerData.getActiveDriveForm()) + (1 * mult)));

                    PacketHandlerRM.syncGlobalToAllAround(player, formData);
                    PacketHandler.sendTo(new SCSyncPlayerData(player), (ServerPlayer) player);
                }
            }
        }
    }

    @Override
    public boolean displayInCommandMenu(Player player){
        return PlayerData.get(player).isAbilityEquipped(ModAbilitiesRM.DARK_POWER);
    }

    @Override
    public void endDrive(Player player) {
        super.endDrive(player);
        PlayerData playerData = PlayerData.get(player);

    }



    @Override
    public ResourceLocation getTextureLocation(Player player) {
        PlayerData playerData = PlayerData.get(player);
        GlobalDataRM global = ModDataRM.getGlobal(player);
        if (global != null){
            if(global.isDarkMode()){

                return ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "textures/models/armor/dark_mode.png");
            } else {
                return ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "textures/models/armor/dark.png");
            }
        }

        /*if (playerData != null && playerData.getEquippedKeychain(DriveForm.NONE) != null) {
            if (playerData.getEquippedKeychain(DriveForm.NONE).getItem() == ModItems.soulEaterChain.get() || playerData.getEquippedKeychain(DriveForm.NONE).getItem() == ModItems.keybladeOfPeoplesHeartsChain.get()) {
                this.skinRL = ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "textures/models/armor/dark_mode.png");
            } else {
                this.skinRL = ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "textures/models/armor/dark.png");
            }
        }*/
            return super.getTextureLocation(player);
    }



}

