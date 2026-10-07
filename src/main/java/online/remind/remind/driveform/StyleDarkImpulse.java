package online.remind.remind.driveform;

import net.minecraft.resources.ResourceLocation;

public class StyleDarkImpulse extends StyleForm {

    public StyleDarkImpulse(ResourceLocation registryName, int order, ResourceLocation skinRL, boolean hasKeychain, boolean baseGrowthAbilities) {
        super(registryName, order, skinRL, hasKeychain, baseGrowthAbilities);

        // Only visual attributes belong here
        this.color = new float[]{1f, 1f, 0f};
    }
}
