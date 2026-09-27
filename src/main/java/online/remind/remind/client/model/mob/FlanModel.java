package online.remind.remind.client.model.mob;

import net.minecraft.resources.ResourceLocation;
import online.remind.remind.KingdomKeysReMind;
import online.remind.remind.entity.enemies.FlanEntity;
import software.bernie.geckolib.model.GeoModel;

public class FlanModel extends GeoModel<FlanEntity> {

    private static final ResourceLocation MODEL = ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "geo/entity/flan.geo.json");
    private static final ResourceLocation ANIMATION = ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "animations/entity/flan.animation.json");

    @Override
    public ResourceLocation getModelResource(FlanEntity animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(FlanEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "textures/entity/" + animatable.getVariantName() + ".png");
    }

    @Override
    public ResourceLocation getAnimationResource(FlanEntity animatable) {
        return ANIMATION;
    }
}
