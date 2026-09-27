package online.remind.remind.magic;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import online.kingdomkeys.kingdomkeys.magic.Magic;
import online.remind.remind.client.sound.ModSoundsRM;
import online.remind.remind.entity.magic.FlareEntity;

public class magicFlare extends Magic {

    public magicFlare(ResourceLocation registryName, boolean hasToSelect, int tier, ResourceLocation gmAbility) {
        super(registryName, hasToSelect, gmAbility);
        setTier(tier);
    }

    @Override
    public void magicUse(LivingEntity player, LivingEntity caster, float fullMPBlastMult, LivingEntity lockOnTarget) {
        float dmgMult = getDamageMult();
        dmgMult *= fullMPBlastMult;

        FlareEntity flare = new FlareEntity(player.level(), player, dmgMult, lockOnTarget);
        flare.setOwner(caster);
        player.level().addFreshEntity(flare);
    }

    @Override
    public void playMagicCastSound(LivingEntity player, LivingEntity caster) {
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), ModSoundsRM.PLAYER_CAST.get(), SoundSource.PLAYERS, 1F, 1F);
    }
}