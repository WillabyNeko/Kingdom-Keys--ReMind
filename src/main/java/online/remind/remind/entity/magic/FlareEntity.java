package online.remind.remind.entity.magic;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import online.kingdomkeys.kingdomkeys.data.WorldData;
import online.kingdomkeys.kingdomkeys.entity.TrainingDummyEntity;
import online.kingdomkeys.kingdomkeys.lib.DamageCalculation;
import online.kingdomkeys.kingdomkeys.lib.Party;
import online.kingdomkeys.kingdomkeys.util.Utils;
import online.remind.remind.entity.ModEntitiesRM;

import java.util.List;

public class FlareEntity extends ThrowableProjectile {

    private static final int CHARGE_DURATION = 12;
    private static final int LINGER_DURATION = 6;
    private static final float DAMAGE_RADIUS = 3.0F;
    private static final float DIRECT_DAMAGE_MULTIPLIER = 1.50F;
    private static final float SPLASH_DAMAGE_MULTIPLIER = 0.70F;
    private static final float DAMAGE_CAP = 99.0F;
    private static final double AIM_RANGE = 24.0D;

    private float dmgMult = 1.0F;
    private int targetId = -1;
    private int flareTicks = 0;
    private boolean detonated = false;

    public FlareEntity(EntityType<? extends ThrowableProjectile> type, Level world) {
        super(type, world);
        this.noPhysics = true;
        this.setInvisible(true);
    }

    public FlareEntity(Level world) {
        super(ModEntitiesRM.TYPE_FLARE.get(), world);
        this.noPhysics = true;
        this.setInvisible(true);
    }

    public FlareEntity(Level world, LivingEntity caster, float dmgMult, LivingEntity lockOnTarget) {
        super(ModEntitiesRM.TYPE_FLARE.get(), caster, world);
        this.dmgMult = dmgMult;
        this.noPhysics = true;
        this.setInvisible(true);

        if (lockOnTarget != null && lockOnTarget.isAlive()) {
            this.targetId = lockOnTarget.getId();
            Vec3 targetPos = lockOnTarget.getBoundingBox().getCenter();
            this.setPos(targetPos.x, targetPos.y, targetPos.z);
        } else {
            HitResult hit = caster.pick(AIM_RANGE, 1.0F, false);
            Vec3 targetPos = hit.getType() != HitResult.Type.MISS ? hit.getLocation() : caster.getEyePosition().add(caster.getLookAngle().scale(AIM_RANGE));
            this.setPos(targetPos.x, targetPos.y, targetPos.z);
        }
    }

    @Override
    protected double getDefaultGravity() {
        return 0.0D;
    }

    @Override
    public void tick() {
        if (!level().isClientSide) {
            if (getOwner() == null || level().getServer() == null) {
                discard();
                return;
            }

            if (!detonated) {
                followLockedTarget();
                spawnChargeParticles();
                flareTicks++;

                if (flareTicks >= CHARGE_DURATION) {
                    detonate();
                }
            } else {
                spawnAftershockParticles();
                flareTicks++;

                if (flareTicks >= CHARGE_DURATION + LINGER_DURATION) {
                    discard();
                }
            }
        }

        setDeltaMovement(Vec3.ZERO);
        super.tick();
    }

    private void followLockedTarget() {
        if (targetId < 0) {
            return;
        }

        Entity entity = level().getEntity(targetId);

        if (!(entity instanceof LivingEntity target) || !target.isAlive()) {
            targetId = -1;
            return;
        }

        Vec3 targetPos = target.getBoundingBox().getCenter();
        setPos(targetPos.x, targetPos.y, targetPos.z);
    }

    private void spawnChargeParticles() {
        if (!(level() instanceof ServerLevel serverLevel)) {
            return;
        }

        float progress = Math.min(1.0F, flareTicks / (float) CHARGE_DURATION);
        double radius = 2.6D * (1.0D - progress) + 0.15D;

        for (int i = 0; i < 18; i++) {
            double angle = (Math.PI * 2.0D * i / 18.0D) + tickCount * 0.22D;
            double yOffset = Math.sin(angle * 2.0D) * radius * 0.35D;
            double x = getX() + Math.cos(angle) * radius;
            double y = getY() + yOffset;
            double z = getZ() + Math.sin(angle) * radius;
            serverLevel.sendParticles(ParticleTypes.WITCH, x, y, z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        }

        serverLevel.sendParticles(ParticleTypes.END_ROD, getX(), getY(), getZ(), 4, radius * 0.15D, radius * 0.15D, radius * 0.15D, 0.0D);

        if (flareTicks >= CHARGE_DURATION - 3) {
            serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK, getX(), getY(), getZ(), 8, 0.20D, 0.20D, 0.20D, 0.05D);
        }
    }

    private void detonate() {
        detonated = true;

        if (level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.FLASH, getX(), getY(), getZ(), 1, 0.0D, 0.0D, 0.0D, 0.0D);
            serverLevel.sendParticles(ParticleTypes.END_ROD, getX(), getY(), getZ(), 40, 0.65D, 0.65D, 0.65D, 0.14D);
            serverLevel.sendParticles(ParticleTypes.FLAME, getX(), getY(), getZ(), 45, 0.85D, 0.85D, 0.85D, 0.10D);
            serverLevel.sendParticles(ParticleTypes.WITCH, getX(), getY(), getZ(), 30, 1.0D, 1.0D, 1.0D, 0.08D);
            serverLevel.sendParticles(ParticleTypes.EXPLOSION, getX(), getY(), getZ(), 8, 0.45D, 0.45D, 0.45D, 0.0D);
        }

        dealAreaDamage();
    }

    private void spawnAftershockParticles() {
        if (!(level() instanceof ServerLevel serverLevel)) {
            return;
        }

        double progress = (flareTicks - CHARGE_DURATION) / (double) LINGER_DURATION;
        double radius = 1.0D + progress * 2.5D;

        for (int i = 0; i < 24; i++) {
            double angle = Math.PI * 2.0D * i / 24.0D;
            double x = getX() + Math.cos(angle) * radius;
            double z = getZ() + Math.sin(angle) * radius;
            serverLevel.sendParticles(ParticleTypes.FLAME, x, getY(), z, 1, 0.0D, 0.05D, 0.0D, 0.0D);
        }
    }

    private void dealAreaDamage() {
        if (getOwner() == null || level().getServer() == null) {
            return;
        }

        List<Entity> entities = level().getEntities(this, getBoundingBox().inflate(DAMAGE_RADIUS));

        if (entities.isEmpty()) {
            return;
        }

        Vec3 center = position();
        double radiusSquared = DAMAGE_RADIUS * DAMAGE_RADIUS;

        for (Entity entity : entities) {
            if (!(entity instanceof LivingEntity target) || !canDamageTarget(target)) {
                continue;
            }

            Vec3 targetCenter = target.getBoundingBox().getCenter();

            if (targetCenter.distanceToSqr(center) > radiusSquared) {
                continue;
            }

            float multiplier = target.getId() == targetId ? DIRECT_DAMAGE_MULTIPLIER : SPLASH_DAMAGE_MULTIPLIER;
            float damage;

            if (getOwner() instanceof Player ownerPlayer) {
                damage = DamageCalculation.getMagicDamage(ownerPlayer) * multiplier;
                damage = Math.min(damage, DAMAGE_CAP);
            } else {
                damage = 2.0F * multiplier;
            }

            damage *= dmgMult;
            target.invulnerableTime = 0;
            target.hurt(damageSources().indirectMagic(this, getOwner()), damage);
        }
    }

    private boolean canDamageTarget(LivingEntity target) {
        if (target == getOwner() || !target.isAlive()) {
            return false;
        }

        if (!Utils.isHostile(target) && !(target instanceof Slime) && !(target instanceof EnderMan) && !(target instanceof TrainingDummyEntity)) {
            return false;
        }

        if (getOwner() != null && getOwner().getServer() != null) {
            WorldData worldData = WorldData.get(getOwner().getServer());

            if (worldData != null) {
                Party party = worldData.getPartyFromMember(getOwner().getUUID());

                if (party != null && !party.getFriendlyFire() && party.getMember(target.getUUID()) != null) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    protected void onHit(HitResult result) {
    }

    @Override
    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putFloat("FlareDamageMultiplier", dmgMult);
        compound.putInt("FlareTargetId", targetId);
        compound.putInt("FlareTicks", flareTicks);
        compound.putBoolean("FlareDetonated", detonated);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);

        if (compound.contains("FlareDamageMultiplier")) {
            dmgMult = compound.getFloat("FlareDamageMultiplier");
        }

        targetId = compound.getInt("FlareTargetId");
        flareTicks = compound.getInt("FlareTicks");
        detonated = compound.getBoolean("FlareDetonated");
    }
}
