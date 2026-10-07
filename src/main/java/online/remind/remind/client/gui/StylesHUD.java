package online.remind.remind.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import online.kingdomkeys.kingdomkeys.KingdomKeys;
import online.kingdomkeys.kingdomkeys.api.event.client.CommandMenuEvent;
import online.kingdomkeys.kingdomkeys.client.ClientUtils;
import online.kingdomkeys.kingdomkeys.data.PlayerData;
import online.kingdomkeys.kingdomkeys.driveform.DriveForm;
import online.remind.remind.KingdomKeysReMind;
import online.remind.remind.capabilities.GlobalDataRM;
import online.remind.remind.capabilities.ModDataRM;
import online.remind.remind.driveform.ModDriveFormsRM;

import java.util.HashSet;
import java.util.Set;

public class StylesHUD {
    private static final ResourceLocation STYLES_TEXTURE = ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "textures/gui/styles_menu.png");

    @SubscribeEvent
    public void onSubmenuRender(CommandMenuEvent.SubmenuRender event) {
        event.setCanceled(event.getSubMenu().getId().equals(ResourceLocation.fromNamespaceAndPath(KingdomKeys.MODID,"root")));

        GuiGraphics guiGraphics = event.getGuiGraphics();
        guiGraphics.setColor(event.getSubMenu().getColour().getRed() / 255F, event.getSubMenu().getColour().getGreen() / 255F, event.getSubMenu().getColour().getBlue() / 255F, 1);
        guiGraphics.blit(event.getSubMenu().getTexture(), event.getSubMenu().getX(), event.getSubMenu().getY(), 0, 70, 74, 15);
        PlayerData playerData = PlayerData.get(Minecraft.getInstance().player);
        GlobalDataRM globalData = ModDataRM.getGlobal(Minecraft.getInstance().player);
        if (playerData == null || globalData == null)
            return;

        float color = 1F; //Reset the color
        RenderSystem.setShaderColor(color,color,color, 1F);

        int uMax = 67;
        double val = globalData.getSituationValue() * uMax / 100; //TODO change that hardcoded 100 if u increase it eventually

        PoseStack poseStack = guiGraphics.pose();

        ResourceLocation form = playerData.getActiveDriveForm();
        DriveForm driveForm = ModDriveFormsRM.DRIVE_FORMS.getRegistry().get().get(form);

        poseStack.pushPose();
        {
            poseStack.translate(2, 2, 0); //Position for the bar
            guiGraphics.blit(STYLES_TEXTURE, 0, 0, 0, 0, (int) val, 11);
        }
        poseStack.popPose();

        guiGraphics.setColor(event.getSubMenu().getColour().getRed() / 255F, event.getSubMenu().getColour().getGreen() / 255F, event.getSubMenu().getColour().getBlue() / 255F, 1);
        Component title = event.getSubMenu().getTitle();
        Set<ResourceLocation> styles = new HashSet<>();
        styles.add(ModDriveFormsRM.FIRESTORM.get().getRegistryName());
        styles.add(ModDriveFormsRM.DIAMOND_DUST.get().getRegistryName());
        styles.add(ModDriveFormsRM.THUNDER_BOLT.get().getRegistryName());
        styles.add(ModDriveFormsRM.FEVER_PITCH.get().getRegistryName());
        styles.add(ModDriveFormsRM.CRITICAL_IMPACT.get().getRegistryName());
        styles.add(ModDriveFormsRM.SPELLWEAVER.get().getRegistryName());
        styles.add(ModDriveFormsRM.DARK_IMPULSE.get().getRegistryName());
        styles.add(ModDriveFormsRM.BLOOSTLUST.get().getRegistryName());
        styles.add(ModDriveFormsRM.EXSOLDIER.get().getRegistryName());

        if(styles.contains(form)){
            title = Component.translatable(driveForm.getTranslationKey()).withStyle(ClientUtils.KK_Font_EXP);
            //System.out.println(title);
        }

        if (online.kingdomkeys.kingdomkeys.config.ModConfigs.cmHeaderTextVisible) {
            //System.out.println(form);
            switch (form.toString()){
                default:
                    guiGraphics.setColor(event.getSubMenu().getColour().getRed() / 255F,event.getSubMenu().getColour().getGreen() / 255F,event.getSubMenu().getColour().getBlue() / 255F,1);
                    break;
                case "kkremind:form_firestorm":
                    guiGraphics.setColor(1,0.75f,0,1);
                    break;
                case "kkremind:form_diamond_dust":
                    guiGraphics.setColor(0,0.75f,1,1);
                    break;
                case "kkremind:form_thunder_bolt":
                    guiGraphics.setColor(0,1f,0,1);
                    break;
                case "kkremind:form_fever_pitch":
                    guiGraphics.setColor(0,1f,0.5f ,1);
                    break;
                case "kkremind:form_critical_impact":
                    guiGraphics.setColor(0.9f,1f,0.0f ,1);
                    break;
                case "kkremind:form_spellweaver":
                    guiGraphics.setColor(0.85f,0.55f,0.85f ,1);
                    break;
                case "kkremind:form_dark_impulse":
                    guiGraphics.setColor(0.75f,0.0f,0.0f ,1);
                    break;
                case "kkremind:form_bloodlust":
                    guiGraphics.setColor(0.85f,0.0f,0.0f ,1);
                    break;
                case "kkremind:form_ex_soldier":
                    guiGraphics.setColor(0.0f,0.85f,0.55f ,1);
                    break;
            }
            guiGraphics.drawCenteredString(Minecraft.getInstance().font, title, event.getSubMenu().getX() + ((event.getSubMenu().getWidth() - 8) / 2) + 1, event.getSubMenu().getY() + 4, 0xFFFFFF);

        }

        RenderSystem.setShaderColor(1,1,1,1F);

    }
}