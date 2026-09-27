package online.remind.remind.client.render.mob;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import online.remind.remind.client.model.mob.FlanModel;
import online.remind.remind.entity.enemies.FlanEntity;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class FlanRenderer extends GeoEntityRenderer<FlanEntity> {

    private static final float MODEL_SCALE = 3.0F;

    public FlanRenderer(EntityRendererProvider.Context context) {
        super(context, new FlanModel());

        this.shadowRadius = 0.6F;

        this.scaleWidth = MODEL_SCALE;
        this.scaleHeight = MODEL_SCALE;
    }
}