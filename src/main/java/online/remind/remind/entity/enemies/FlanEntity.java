package online.remind.remind.entity.enemies;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import online.kingdomkeys.kingdomkeys.damagesource.KKDamageTypes;
import online.kingdomkeys.kingdomkeys.data.GlobalData;
import online.kingdomkeys.kingdomkeys.magic.Magic;
import online.kingdomkeys.kingdomkeys.magic.ModMagic;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.EnumSet;

public class FlanEntity extends Monster implements GeoEntity {

    public static final int VARIANT_FIRE = 0;
    public static final int VARIANT_ICE = 1;
    public static final int VARIANT_THUNDER = 2;
    public static final int VARIANT_WIND = 3;
    public static final int VARIANT_WATER = 4;
    public static final int VARIANT_LIGHT = 5;
    public static final int VARIANT_DARK = 6;
    public static final int VARIANT_COUNT = 7;

    private static final int SPELL_TIER_BASE = 0;
    private static final int SPELL_TIER_RA = 1;
    private static final int SPELL_TIER_GA = 2;
    private static final int SPELL_TIER_ZA = 3;

    private static final int LEVEL_RA = 30;
    private static final int LEVEL_GA = 50;
    private static final int LEVEL_ZA = 80;

    private static final int CAST_DURATION = 30;
    private static final int CAST_RELEASE_TICK = 10;
    private static final double MIN_CAST_RANGE = 3.0D;
    private static final double MAX_CAST_RANGE = 16.0D;

    private static final EntityDataAccessor<Integer> VARIANT = SynchedEntityData.defineId(FlanEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> CASTING = SynchedEntityData.defineId(FlanEntity.class, EntityDataSerializers.BOOLEAN);

    private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("animation.flan.idle");
    private static final RawAnimation MOVE_ANIM = RawAnimation.begin().thenLoop("animation.flan.move");
    private static final RawAnimation ATTACK_ANIM = RawAnimation.begin().thenPlay("animation.flan.attack");
    private static final RawAnimation CAST_ANIM = RawAnimation.begin().thenPlay("animation.flan.cast");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private int attackAnimationTicks = 0;
    private int spellCooldown = 60;
    private int castTicks = 0;
    private int pendingSpellTier = SPELL_TIER_BASE;
    private boolean spellReleased = false;
    private String currentAnimation = "none";

    public FlanEntity(EntityType<? extends FlanEntity> type, Level level) {
        super(type, level);
        this.xpReward = 18;
    }



    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(VARIANT, VARIANT_FIRE);
        builder.define(CASTING, false);
    }

    public int getVariant() {
        return this.entityData.get(VARIANT);
    }

    public void setVariant(int variant) {
        this.entityData.set(VARIANT, Math.max(0, Math.min(VARIANT_COUNT - 1, variant)));
    }

    public boolean isCasting() {
        return this.entityData.get(CASTING);
    }

    private void setCasting(boolean casting) {
        this.entityData.set(CASTING, casting);
    }

    public boolean isFireFlan() {
        return getVariant() == VARIANT_FIRE;
    }

    public boolean isIceFlan() {
        return getVariant() == VARIANT_ICE;
    }

    public boolean isThunderFlan() {
        return getVariant() == VARIANT_THUNDER;
    }

    public boolean isWindFlan() {
        return getVariant() == VARIANT_WIND;
    }

    public boolean isWaterFlan() {
        return getVariant() == VARIANT_WATER;
    }

    public boolean isLightFlan() {
        return getVariant() == VARIANT_LIGHT;
    }

    public boolean isDarkFlan() {
        return getVariant() == VARIANT_DARK;
    }

    private boolean isPhysicalDamage(DamageSource source) {
        return source.getEntity() instanceof Player;
    }

    public String getVariantName() {
        return switch (getVariant()) {
            case VARIANT_ICE -> "ice_flan";
            case VARIANT_THUNDER -> "thunder_flan";
            case VARIANT_WIND -> "wind_flan";
            case VARIANT_WATER -> "water_flan";
            case VARIANT_LIGHT -> "light_flan";
            case VARIANT_DARK -> "dark_flan";
            default -> "fire_flan";
        };
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 100.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.18D)
                .add(Attributes.ATTACK_DAMAGE, 4.0D)
                .add(Attributes.ATTACK_KNOCKBACK, 0.15D)
                .add(Attributes.ARMOR, 1.0D)
                .add(Attributes.FOLLOW_RANGE, 24.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.20D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new FlanHopAttackGoal(this, 0.90D));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.65D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public void tick() {
        super.tick();

        if (attackAnimationTicks > 0) {
            attackAnimationTicks--;
        }

        if (!level().isClientSide) {
            tickMagicCasting();
        }
    }

    private void tickMagicCasting() {
        if (isCasting()) {
            tickActiveCast();
            return;
        }

        if (spellCooldown > 0) {
            spellCooldown--;
            return;
        }

        tryStartCast();
    }

    private void tryStartCast() {
        LivingEntity target = getTarget();

        if (target == null || !target.isAlive() || !hasLineOfSight(target)) {
            spellCooldown = 20;
            return;
        }

        double distanceSqr = distanceToSqr(target);

        if (distanceSqr < MIN_CAST_RANGE * MIN_CAST_RANGE || distanceSqr > MAX_CAST_RANGE * MAX_CAST_RANGE) {
            spellCooldown = 10;
            return;
        }

        pendingSpellTier = getSpellTierForLevel(getKingdomKeysMobLevel());
        castTicks = CAST_DURATION;
        spellReleased = false;
        setCasting(true);
        getNavigation().stop();
    }

    private void tickActiveCast() {
        LivingEntity target = getTarget();

        if (target == null || !target.isAlive()) {
            finishCast();
            return;
        }

        getNavigation().stop();
        getLookControl().setLookAt(target, 30.0F, 30.0F);
        setYRot(getYHeadRot());

        if (!spellReleased && castTicks <= CAST_RELEASE_TICK) {
            spellReleased = true;
            castElementSpell(target, pendingSpellTier);
        }

        castTicks--;

        if (castTicks <= 0) {
            finishCast();
        }
    }

    private void finishCast() {
        setCasting(false);
        castTicks = 0;
        spellReleased = false;
        spellCooldown = 80 + random.nextInt(41);
    }

    public int getKingdomKeysMobLevel() {
        GlobalData mobData = GlobalData.get(this);

        if (mobData == null) {
            return 1;
        }

        return Math.max(1, mobData.getLevel());
    }

    private int getSpellTierForLevel(int level) {
        if (level >= LEVEL_ZA) {
            return SPELL_TIER_ZA;
        }

        if (level >= LEVEL_GA) {
            return SPELL_TIER_GA;
        }

        if (level >= LEVEL_RA) {
            return SPELL_TIER_RA;
        }

        return SPELL_TIER_BASE;
    }

    private ResourceLocation getMagicIdForVariant(int tier) {
        return switch (getVariant()) {
            case VARIANT_FIRE -> ResourceLocation.fromNamespaceAndPath("kingdomkeys", switch (tier) {
                case SPELL_TIER_RA -> "magic_fira";
                case SPELL_TIER_GA -> "magic_firaga";
                case SPELL_TIER_ZA -> "magic_firaza";
                default -> "magic_fire";
            });

            case VARIANT_ICE -> ResourceLocation.fromNamespaceAndPath("kingdomkeys", switch (tier) {
                case SPELL_TIER_RA -> "magic_blizzara";
                case SPELL_TIER_GA -> "magic_blizzaga";
                case SPELL_TIER_ZA -> "magic_blizzaza";
                default -> "magic_blizzard";
            });

            case VARIANT_THUNDER -> ResourceLocation.fromNamespaceAndPath("kingdomkeys", switch (tier) {
                case SPELL_TIER_RA -> "magic_thundara";
                case SPELL_TIER_GA -> "magic_thundaga";
                case SPELL_TIER_ZA -> "magic_thundaza";
                default -> "magic_thunder";
            });

            case VARIANT_WATER -> ResourceLocation.fromNamespaceAndPath("kingdomkeys", switch (tier) {
                case SPELL_TIER_RA -> "magic_watera";
                case SPELL_TIER_GA -> "magic_waterga";
                case SPELL_TIER_ZA -> "magic_waterza";
                default -> "magic_water";
            });

            case VARIANT_WIND -> ResourceLocation.fromNamespaceAndPath("kingdomkeys", switch (tier) {
                case SPELL_TIER_RA -> "magic_aerora";
                case SPELL_TIER_GA, SPELL_TIER_ZA -> "magic_aeroga";
                default -> "magic_aero";
            });

            case VARIANT_LIGHT -> ResourceLocation.fromNamespaceAndPath("kkremind", switch (tier) {
                case SPELL_TIER_RA -> "magic_holyra";
                case SPELL_TIER_GA, SPELL_TIER_ZA -> "magic_holyga";
                default -> "magic_holy";
            });

            case VARIANT_DARK -> ResourceLocation.fromNamespaceAndPath("kkremind", switch (tier) {
                case SPELL_TIER_RA -> "magic_ruinra";
                case SPELL_TIER_GA, SPELL_TIER_ZA -> "magic_ruinga";
                default -> "magic_ruin";
            });

            default -> ResourceLocation.fromNamespaceAndPath("kingdomkeys", "magic_fire");
        };
    }

    private void castElementSpell(LivingEntity target, int tier) {
        if (level().isClientSide || target == null || !target.isAlive()) {
            return;
        }

        if (!hasLineOfSight(target)) {
            return;
        }

        ResourceLocation magicId = getMagicIdForVariant(tier);
        Magic magic = ModMagic.registry.get(magicId);

        if (magic == null) {
            System.err.println("[Kingdom Keys Re:Mind] Flan could not find magic: " + magicId);
            return;
        }

        magic.castFromMob(this, target);
    }

    public void startAttackAnimation() {
        attackAnimationTicks = 10;
    }

    public boolean isAttacking() {
        return attackAnimationTicks > 0;
    }

    private PlayState animationPredicate(AnimationState<FlanEntity> state) {
        if (isCasting()) {
            setAnimation(state, "cast", CAST_ANIM);
            return PlayState.CONTINUE;
        }

        if (isAttacking()) {
            setAnimation(state, "attack", ATTACK_ANIM);
            return PlayState.CONTINUE;
        }

        if (state.isMoving()) {
            setAnimation(state, "move", MOVE_ANIM);
            return PlayState.CONTINUE;
        }

        setAnimation(state, "idle", IDLE_ANIM);
        return PlayState.CONTINUE;
    }

    private void setAnimation(AnimationState<FlanEntity> state, String name, RawAnimation animation) {
        if (name.equals(this.currentAnimation)) {
            return;
        }

        this.currentAnimation = name;
        state.getController().forceAnimationReset();
        state.getController().setAnimation(animation);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main", 0, this::animationPredicate));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("FlanVariant", getVariant());
        compound.putInt("FlanSpellCooldown", spellCooldown);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);

        if (compound.contains("FlanVariant")) {
            setVariant(compound.getInt("FlanVariant"));
        }

        if (compound.contains("FlanSpellCooldown")) {
            spellCooldown = Math.max(0, compound.getInt("FlanSpellCooldown"));
        }

        setCasting(false);
        castTicks = 0;
        spellReleased = false;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (isOwnElementDamage(source)) {
            if (!level().isClientSide) {
                heal(amount);
            }

            return false;
        }

        if (isWeaknessDamage(source)) {
            return super.hurt(source, amount);
        }

        if (isPhysicalDamage(source)) {
            return super.hurt(source, amount * 0.05F);
        }

        return super.hurt(source, amount * 0.05F);
    }

    private boolean isOwnElementDamage(DamageSource source) {
        return switch (getVariant()) {
            case VARIANT_FIRE -> source.is(KKDamageTypes.FIRE);
            case VARIANT_ICE -> source.is(KKDamageTypes.ICE);
            case VARIANT_THUNDER -> source.is(KKDamageTypes.LIGHTNING);
            case VARIANT_WIND -> source.is(KKDamageTypes.AIR);
            case VARIANT_WATER -> source.is(KKDamageTypes.WATER);
            case VARIANT_LIGHT -> source.is(KKDamageTypes.LIGHT);
            case VARIANT_DARK -> source.is(KKDamageTypes.DARKNESS);
            default -> false;
        };
    }

    private boolean isWeaknessDamage(DamageSource source) {
        return switch (getVariant()) {
            case VARIANT_FIRE -> source.is(KKDamageTypes.ICE);
            case VARIANT_ICE -> source.is(KKDamageTypes.FIRE);
            case VARIANT_THUNDER -> source.is(KKDamageTypes.WATER);
            case VARIANT_WATER -> source.is(KKDamageTypes.LIGHTNING);
            case VARIANT_WIND -> source.is(KKDamageTypes.LIGHTNING);
            case VARIANT_LIGHT -> source.is(KKDamageTypes.DARKNESS);
            case VARIANT_DARK -> source.is(KKDamageTypes.LIGHT);
            default -> false;
        };
    }

    private static class FlanHopAttackGoal extends Goal {

        private final FlanEntity flan;
        private final double speed;
        private LivingEntity target;
        private int hopCooldown;
        private int attackCooldown;

        public FlanHopAttackGoal(FlanEntity flan, double speed) {
            this.flan = flan;
            this.speed = speed;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (flan.isCasting()) {
                return false;
            }

            LivingEntity currentTarget = flan.getTarget();

            if (currentTarget == null || !currentTarget.isAlive()) {
                return false;
            }

            this.target = currentTarget;
            return true;
        }

        @Override
        public boolean canContinueToUse() {
            return !flan.isCasting() && target != null && target.isAlive() && flan.getTarget() == target;
        }

        @Override
        public void start() {
            hopCooldown = 0;
            attackCooldown = 0;
        }

        @Override
        public void stop() {
            target = null;
            flan.getNavigation().stop();
        }

        @Override
        public void tick() {
            if (flan.isCasting() || target == null) {
                return;
            }

            flan.getLookControl().setLookAt(target, 30.0F, 30.0F);

            if (hopCooldown > 0) {
                hopCooldown--;
            }

            if (attackCooldown > 0) {
                attackCooldown--;
            }

            if (flan.onGround() && hopCooldown <= 0) {
                flan.getNavigation().moveTo(target, speed);

                double dx = target.getX() - flan.getX();
                double dz = target.getZ() - flan.getZ();
                double length = Math.sqrt(dx * dx + dz * dz);

                if (length > 0.001D) {
                    double horizontalSpeed = 0.18D;
                    flan.setDeltaMovement(dx / length * horizontalSpeed, 0.34D, dz / length * horizontalSpeed);
                    flan.hasImpulse = true;
                }

                hopCooldown = 14 + flan.getRandom().nextInt(7);
            }

            double attackReach = flan.getBbWidth() * 1.5D + target.getBbWidth();

            if (attackCooldown <= 0 && flan.distanceToSqr(target) <= attackReach * attackReach) {
                flan.startAttackAnimation();
                flan.doHurtTarget(target);
                attackCooldown = 24;
            }
        }


    }
}