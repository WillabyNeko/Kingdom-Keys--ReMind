package online.remind.remind.entity.enemies;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
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

import java.util.Locale;


public class BombEntity extends Monster implements GeoEntity {

    private static final int LIGHT_UPDATE_INTERVAL_TICKS = 4;

    private BlockPos activeLightBlockPos = null;
    private int lightUpdateTicks = 0;

    private static final int MAX_FIRE_GROWTH_STAGE = 3;
    private static final double FIRE_GROWTH_PER_STAGE = 0.25D;

    private static final ResourceLocation FIRE_GROWTH_SCALE_ID =
            ResourceLocation.fromNamespaceAndPath(
                    "kkremind",
                    "bomb_fire_growth"
            );

    // ============================================================
    // VARIANTS
    // ============================================================

    public static final int VARIANT_BOMB = 0;
    public static final int VARIANT_GRENADE = 1;
    public static final int VARIANT_VOLCANO = 2;


    // ============================================================
    // ACTIONS
    // ============================================================

    public static final int ACTION_NONE = 0;
    public static final int ACTION_ATTACK = 1;
    public static final int ACTION_CAST = 2;
    public static final int ACTION_SELF_DESTRUCT = 3;
    public static final int ACTION_DEATH = 4;


    // ============================================================
    // SPELL TIERS
    // ============================================================

    public static final int SPELL_FIRE = 0;
    public static final int SPELL_FIRA = 1;
    public static final int SPELL_FIRAGA = 2;
    public static final int SPELL_FIRAZA = 3;

    private static final ResourceLocation MAGIC_FIRE =
            ResourceLocation.fromNamespaceAndPath(
                    "kingdomkeys",
                    "magic_fire"
            );

    private static final ResourceLocation MAGIC_FIRA =
            ResourceLocation.fromNamespaceAndPath(
                    "kingdomkeys",
                    "magic_fira"
            );

    private static final ResourceLocation MAGIC_FIRAGA =
            ResourceLocation.fromNamespaceAndPath(
                    "kingdomkeys",
                    "magic_firaga"
            );

    private static final ResourceLocation MAGIC_FIRAZA =
            ResourceLocation.fromNamespaceAndPath(
                    "kingdomkeys",
                    "magic_firaza"
            );

    /*
     * Easy-to-tune level gates.
     */
    private static final int LEVEL_FIRA = 20;
    private static final int LEVEL_FIRAGA = 40;
    private static final int LEVEL_FIRAZA = 60;



    // ============================================================
    // TIMING
    // ============================================================

    private static final int ATTACK_ACTION_TICKS = 23;
    private static final int ATTACK_HIT_DELAY = 20;

    private static final int CAST_ACTION_TICKS = 30;
    private static final int CAST_RELEASE_DELAY = 20;

    private static final int SELF_DESTRUCT_CONSIDER_MIN_TICKS = 20;
    private static final int SELF_DESTRUCT_CONSIDER_MAX_TICKS = 50;
    private static final float SELF_DESTRUCT_BASE_CHANCE = 0.20F;
    private static final int SELF_DESTRUCT_TICKS = 20;

    private static final float SELF_DESTRUCT_HEALTH_PERCENT = 0.25F;

    private static final double MAX_CAST_RANGE = 16.0D;
    private static final double MIN_CAST_RANGE = 3.0D;


    // ============================================================
    // SYNCED DATA
    // ============================================================

    private static final EntityDataAccessor<Integer> VARIANT =
            SynchedEntityData.defineId(BombEntity.class, EntityDataSerializers.INT);

    private static final EntityDataAccessor<Integer> ACTION =
            SynchedEntityData.defineId(BombEntity.class, EntityDataSerializers.INT);

    private static final EntityDataAccessor<Integer> ACTION_TICKS =
            SynchedEntityData.defineId(BombEntity.class, EntityDataSerializers.INT);



    // ============================================================
    // GECKOLIB
    // ============================================================

    private static final RawAnimation IDLE_ANIM =
            RawAnimation.begin().thenLoop("idle");

    private static final RawAnimation ATTACK_ANIM =
            RawAnimation.begin().thenPlay("attack");

    private static final RawAnimation CAST_ANIM =
            RawAnimation.begin().thenPlay("cast");

    private static final RawAnimation SELF_DESTRUCT_ANIM =
            RawAnimation.begin().thenPlayAndHold("self-destruct");

    private static final RawAnimation DEATH_ANIM =
            RawAnimation.begin().thenPlayAndHold("death");

    private final AnimatableInstanceCache cache =
            GeckoLibUtil.createInstanceCache(this);



    // ============================================================
    // SERVER COMBAT STATE
    // ============================================================

    private int spellCooldown = 40;

    private int pendingMeleeTargetId = -1;
    private int pendingMeleeDelay = 0;

    private int pendingCastTargetId = -1;
    private int pendingCastDelay = 0;
    private int pendingSpellTier = SPELL_FIRE;

    private int selfDestructTicks = 0;
    private int fireGrowthStage = 0;

    private int selfDestructConsiderTicks = -1;


    // ============================================================
    // CONSTRUCTOR
    // ============================================================

    public BombEntity(
            EntityType<? extends BombEntity> type,
            Level level
    ) {
        super(type, level);

        int detectedVariant = detectVariant(type);

        this.setVariant(detectedVariant);


        this.xpReward = switch (detectedVariant) {
            case VARIANT_GRENADE -> 30;
            case VARIANT_VOLCANO -> 50;
            default -> 15;
        };
    }

    private int detectVariant(EntityType<?> type) {
        ResourceLocation id =
                BuiltInRegistries.ENTITY_TYPE.getKey(type);

        if (id == null) {
            return VARIANT_BOMB;
        }

        return switch (id.getPath()) {
            case "grenade" -> VARIANT_GRENADE;
            case "volcano" -> VARIANT_VOLCANO;
            default -> VARIANT_BOMB;
        };
    }


    // ============================================================
    // DATA
    // ============================================================

    @Override
    protected void defineSynchedData(
            SynchedEntityData.Builder builder
    ) {
        super.defineSynchedData(builder);

        builder.define(VARIANT, VARIANT_BOMB);
        builder.define(ACTION, ACTION_NONE);
        builder.define(ACTION_TICKS, 0);
    }

    public int getVariant() {
        return this.entityData.get(VARIANT);
    }

    public void setVariant(int variant) {
        this.entityData.set(
                VARIANT,
                Mth.clamp(
                        variant,
                        VARIANT_BOMB,
                        VARIANT_VOLCANO
                )
        );
    }

    public boolean isBomb() {
        return getVariant() == VARIANT_BOMB;
    }

    public boolean isGrenade() {
        return getVariant() == VARIANT_GRENADE;
    }

    public boolean isVolcano() {
        return getVariant() == VARIANT_VOLCANO;
    }

    public int getBombAction() {
        return this.entityData.get(ACTION);
    }

    public int getBombActionTicks() {
        return this.entityData.get(ACTION_TICKS);
    }

    /**
     * Kingdom Keys owns hostile mob levels.
     *
     * Its EntityEvents assigns a level to Monsters through GlobalData and
     * applies the matching attribute scaling when the entity joins the world.
     *
     * Bomb therefore does NOT keep a second/custom level value.
     */
    public int getKingdomKeysMobLevel() {
        GlobalData mobData =
                GlobalData.get(this);

        if (mobData == null) {
            return 1;
        }

        return Math.max(
                1,
                mobData.getLevel()
        );
    }

    public boolean isSelfDestructing() {
        return this.getBombAction() == ACTION_SELF_DESTRUCT
                && this.selfDestructTicks > 0;
    }


    // ============================================================
    // ATTRIBUTES
    // ============================================================

    public static AttributeSupplier.Builder createBombAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 100.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.22D)
                .add(Attributes.ATTACK_DAMAGE, 6.0D)
                .add(Attributes.ATTACK_KNOCKBACK, 0.25D)
                .add(Attributes.ARMOR, 2.0D)
                .add(Attributes.FOLLOW_RANGE, 32.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.15D);
    }

    public static AttributeSupplier.Builder createGrenadeAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 200.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.23D)
                .add(Attributes.ATTACK_DAMAGE, 10.0D)
                .add(Attributes.ATTACK_KNOCKBACK, 0.40D)
                .add(Attributes.ARMOR, 4.0D)
                .add(Attributes.FOLLOW_RANGE, 36.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.25D);
    }

    public static AttributeSupplier.Builder createVolcanoAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 320.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.24D)
                .add(Attributes.ATTACK_DAMAGE, 14.0D)
                .add(Attributes.ATTACK_KNOCKBACK, 0.60D)
                .add(Attributes.ARMOR, 6.0D)
                .add(Attributes.FOLLOW_RANGE, 40.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.40D);
    }


    // ============================================================
    // AI
    // ============================================================

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(
                0,
                new FloatGoal(this)
        );

        this.goalSelector.addGoal(
                2,
                new MeleeAttackGoal(
                        this,
                        1.10D,
                        true
                )
        );

        this.goalSelector.addGoal(
                5,
                new WaterAvoidingRandomStrollGoal(
                        this,
                        0.85D
                )
        );

        this.goalSelector.addGoal(
                6,
                new LookAtPlayerGoal(
                        this,
                        Player.class,
                        12.0F
                )
        );

        this.goalSelector.addGoal(
                7,
                new RandomLookAroundGoal(this)
        );

        this.targetSelector.addGoal(
                1,
                new HurtByTargetGoal(this)
        );

        this.targetSelector.addGoal(
                2,
                new NearestAttackableTargetGoal<>(
                        this,
                        Player.class,
                        true
                )
        );
    }


    // ============================================================
    // MAIN TICK
    // ============================================================

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide) {
            return;
        }

        if (this.isDeadOrDying()) {
            return;
        }

        tickDynamicLight();
        tickAmbientParticles();

        tickCooldowns();
        tickActionTimer();
        tickPendingMelee();
        tickPendingCast();

        if (this.getBombAction() == ACTION_SELF_DESTRUCT) {
            tickSelfDestruct();
            lockActionMovement();
            return;
        }

        if (isActionBusy()) {
            lockActionMovement();
            return;
        }

        tickCombatDecision();
    }

    private void tickAmbientParticles() {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        /*
         * Don't spam particles every tick.
         * Every 3 ticks = ~6-7 bursts per second.
         */
        if (this.tickCount % 3 != 0) {
            return;
        }

        /*
         * Grenades get blue Soul Fire.
         * Bombs and Volcanos get normal Fire.
         */
        var particle =
                this.isGrenade()
                        ? ParticleTypes.SOUL_FIRE_FLAME
                        : ParticleTypes.FLAME;

        double scale =
                this.getScale();

        /*
         * Spawn around the body rather than directly at the feet.
         */
        double x =
                this.getX()
                        + (this.getRandom().nextDouble() - 0.5D)
                        * 0.55D
                        * scale;

        double y =
                this.getY()
                        + this.getBbHeight()
                        * (0.25D
                        + this.getRandom().nextDouble()
                        * 0.60D);

        double z =
                this.getZ()
                        + (this.getRandom().nextDouble() - 0.5D)
                        * 0.55D
                        * scale;

        serverLevel.sendParticles(
                particle,
                x,
                y,
                z,
                1,
                0.03D * scale,
                0.03D * scale,
                0.03D * scale,
                0.01D
        );
    }

    private void tickCooldowns() {
        if (this.spellCooldown > 0) {
            this.spellCooldown--;
        }

        if (this.selfDestructConsiderTicks > 0) {
            this.selfDestructConsiderTicks--;
        }
    }

    private void tickActionTimer() {
        int action =
                this.entityData.get(ACTION);

        /*
         * These end through death/self-destruct handling instead.
         */
        if (action == ACTION_DEATH
                || action == ACTION_SELF_DESTRUCT) {
            return;
        }

        int ticks =
                this.entityData.get(ACTION_TICKS);

        if (ticks <= 0) {
            if (action != ACTION_NONE) {
                this.entityData.set(
                        ACTION,
                        ACTION_NONE
                );
            }

            return;
        }

        ticks--;

        this.entityData.set(
                ACTION_TICKS,
                ticks
        );

        if (ticks <= 0) {
            this.entityData.set(
                    ACTION,
                    ACTION_NONE
            );
        }
    }

    private boolean isActionBusy() {
        int action =
                this.getBombAction();

        if (action == ACTION_SELF_DESTRUCT
                || action == ACTION_DEATH) {
            return true;
        }

        return action != ACTION_NONE
                && this.getBombActionTicks() > 0;
    }

    private void lockActionMovement() {
        this.getNavigation().stop();

        Vec3 movement =
                this.getDeltaMovement();

        /*
         * Stop horizontal travel without fighting gravity.
         */
        this.setDeltaMovement(
                0.0D,
                movement.y,
                0.0D
        );

        this.hasImpulse = true;
        this.hurtMarked = true;
    }


    // ============================================================
    // COMBAT DECISION
    // ============================================================

    private void tickCombatDecision() {
        LivingEntity target =
                this.getTarget();

        if (target == null
                || !target.isAlive()) {
            return;
        }

        /*
         * Low health makes the Bomb CONSIDER self-destructing,
         * rather than immediately committing to it.
         */
        float healthPercent =
                this.getHealth()
                        / this.getMaxHealth();

        if (healthPercent
                <= SELF_DESTRUCT_HEALTH_PERCENT) {

            /*
             * First time entering panic health:
             * start a short hesitation timer.
             */
            if (this.selfDestructConsiderTicks < 0) {
                resetSelfDestructConsiderTimer();
            }

            /*
             * Once the hesitation timer expires,
             * decide whether to actually self-destruct.
             */
            else if (this.selfDestructConsiderTicks == 0) {

                /*
                 * Chance rises as health gets lower.
                 *
                 * ~25% HP = 20%
                 * ~20% HP = 30%
                 * ~15% HP = 40%
                 * ~10% HP = 50%
                 *  ~5% HP = 60%
                 *   0% HP = 70%
                 */
                float desperation =
                        1.0F
                                - healthPercent
                                / SELF_DESTRUCT_HEALTH_PERCENT;

                float chance =
                        SELF_DESTRUCT_BASE_CHANCE
                                + desperation
                                * 0.50F;

                if (this.getRandom().nextFloat()
                        < chance) {

                    startSelfDestruct();
                    return;
                }

                /*
                 * It chose not to explode this time.
                 * Wait a bit before reconsidering.
                 */
                resetSelfDestructConsiderTimer();
            }
        }
        else {
            /*
             * If Fire healing or anything else brings it
             * above 25% HP, abandon the panic state entirely.
             */
            this.selfDestructConsiderTicks = -1;
        }

        double distanceSqr =
                this.distanceToSqr(target);

        double minCastSqr =
                MIN_CAST_RANGE
                        * MIN_CAST_RANGE;

        double maxCastSqr =
                MAX_CAST_RANGE
                        * MAX_CAST_RANGE;

        /*
         * Cast at range when available.
         *
         * MeleeAttackGoal handles closing distance / melee otherwise.
         */
        if (this.spellCooldown <= 0
                && distanceSqr >= minCastSqr
                && distanceSqr <= maxCastSqr
                && this.hasLineOfSight(target)) {

            startCast(target);
        }
    }


    // ============================================================
    // MELEE
    // ============================================================

    @Override
    public boolean doHurtTarget(Entity entity) {
        if (this.level().isClientSide) {
            return true;
        }

        if (!(entity instanceof LivingEntity target)
                || !target.isAlive()) {
            return false;
        }

        if (isActionBusy()) {
            return true;
        }

        setAction(
                ACTION_ATTACK,
                ATTACK_ACTION_TICKS
        );

        this.pendingMeleeTargetId =
                target.getId();

        this.pendingMeleeDelay =
                ATTACK_HIT_DELAY;

        return true;
    }

    private void tickPendingMelee() {
        if (this.pendingMeleeDelay <= 0) {
            return;
        }

        this.pendingMeleeDelay--;

        if (this.pendingMeleeDelay > 0) {
            return;
        }

        LivingEntity target =
                getLivingEntityById(
                        this.pendingMeleeTargetId
                );

        this.pendingMeleeTargetId = -1;

        if (target == null
                || !target.isAlive()) {
            return;
        }

        /*
         * If the target escaped the animation, the attack misses.
         */
        if (this.distanceToSqr(target) > 12.25D) {
            return;
        }

        float damage =
                (float) this.getAttributeValue(
                        Attributes.ATTACK_DAMAGE
                );

        boolean hit =
                target.hurt(
                        this.damageSources()
                                .mobAttack(this),
                        damage
                );

        if (hit) {
            Vec3 away =
                    target.position()
                            .subtract(
                                    this.position()
                            );

            if (away.lengthSqr() > 0.0001D) {
                away = away.normalize();
            }

            target.setDeltaMovement(
                    target.getDeltaMovement()
                            .add(
                                    away.x * 0.35D,
                                    0.18D,
                                    away.z * 0.35D
                            )
            );

            target.hurtMarked = true;
        }
    }


    // ============================================================
    // FIRE / FIRA / FIRAGA / FIRAZA
    // ============================================================

    private void startCast(LivingEntity target) {
        int spellTier =
                getSpellTierForLevel(
                        this.getKingdomKeysMobLevel()
                );

        setAction(
                ACTION_CAST,
                CAST_ACTION_TICKS
        );

        this.pendingCastTargetId =
                target.getId();

        this.pendingCastDelay =
                CAST_RELEASE_DELAY;

        this.pendingSpellTier =
                spellTier;

        this.spellCooldown =
                getSpellCooldownForVariant();

        this.getNavigation().stop();

        this.getLookControl().setLookAt(
                target,
                30.0F,
                30.0F
        );

    }

    private void tickPendingCast() {
        if (this.pendingCastDelay <= 0) {
            return;
        }

        this.pendingCastDelay--;

        if (this.pendingCastDelay > 0) {
            return;
        }

        LivingEntity target =
                getLivingEntityById(
                        this.pendingCastTargetId
                );

        this.pendingCastTargetId = -1;

        if (target == null
                || !target.isAlive()) {
            return;
        }

        if (this.distanceToSqr(target)
                > MAX_CAST_RANGE
                * MAX_CAST_RANGE
                * 1.50D) {
            return;
        }

        if (!this.hasLineOfSight(target)) {
            return;
        }

        castFireSpell(
                target,
                this.pendingSpellTier
        );
    }

    private int getSpellTierForLevel(int level) {
        if (level >= LEVEL_FIRAZA) {
            return SPELL_FIRAZA;
        }

        if (level >= LEVEL_FIRAGA) {
            return SPELL_FIRAGA;
        }

        if (level >= LEVEL_FIRA) {
            return SPELL_FIRA;
        }

        return SPELL_FIRE;
    }

    public String getCurrentFireSpellName() {
        return getFireSpellName(
                getSpellTierForLevel(
                        this.getKingdomKeysMobLevel()
                )
        );
    }

    private String getFireSpellName(int tier) {
        return switch (tier) {
            case SPELL_FIRA -> "Fira";
            case SPELL_FIRAGA -> "Firaga";
            case SPELL_FIRAZA -> "Firaza";
            default -> "Fire";
        };
    }

    private int getSpellCooldownForVariant() {
        return switch (this.getVariant()) {
            case VARIANT_GRENADE -> 80;
            case VARIANT_VOLCANO -> 60;
            default -> 100;
        };
    }

    /**
     * Resolve the exact Kingdom Keys magic registry entry for the spell
     * tier this Bomb is currently allowed to cast.
     */
    private ResourceLocation getFireMagicId(int tier) {
        return switch (tier) {
            case SPELL_FIRA -> MAGIC_FIRA;
            case SPELL_FIRAGA -> MAGIC_FIRAGA;
            case SPELL_FIRAZA -> MAGIC_FIRAZA;
            default -> MAGIC_FIRE;
        };
    }

    /**
     * Cast the REAL Kingdom Keys spell from this mob.
     *
     * The cast is released at tick 20 of our GeckoLib "cast" animation.
     * Kingdom Keys then owns the projectile/effect, damage behavior,
     * particles, sounds, element handling, and lock-on behavior.
     */
    private void castFireSpell(
            LivingEntity target,
            int tier
    ) {
        if (this.level().isClientSide
                || target == null
                || !target.isAlive()) {
            return;
        }

        ResourceLocation magicId =
                getFireMagicId(tier);

        Magic magic =
                ModMagic.registry.get(
                        magicId
                );

        if (magic == null) {
            System.err.println(
                    "[Kingdom Keys Re:Mind] Bomb could not find Kingdom Keys magic: "
                            + magicId
            );
            return;
        }

        /*
         * Face the target before the actual spell is emitted.
         */
        this.getLookControl().setLookAt(
                target,
                30.0F,
                30.0F
        );

        this.setYRot(
                this.getYHeadRot()
        );

        /*
         * Official Kingdom Keys mob-casting entry point.
         *
         * No fake PlayerData caster, no MP cost, and no Re:Mind recreation
         * of Fire/Fira/Firaga/Firaza.
         */
        magic.castFromMob(
                this,
                target
        );
    }

    // ============================================================
// FIRE DAMAGE / GROWTH
// ============================================================

    @Override
    public boolean hurt(
            DamageSource source,
            float amount
    ) {

        if (source.is(DamageTypes.IN_FIRE) || source.is(DamageTypes.ON_FIRE) || source.is(DamageTypes.FIREBALL) || source.is(DamageTypes.CAMPFIRE)){
            this.heal(amount);
            return false;
        }


        if (isFireDamage(source)) {

            if (this.level().isClientSide) {
                return true;
            }

            if (this.isDeadOrDying()
                    || this.getBombAction() == ACTION_SELF_DESTRUCT) {
                return false;
            }

            /*
             * Heal by exactly as much damage the Fire hit
             * would have dealt.
             *
             * LivingEntity#heal automatically caps at max health.
             */
            this.heal(amount * 1.5f);

            growFromFireDamage();

            return false;
        }

        if (isIceOrWaterDamage(source)) {
            amount *= 2F;
        }

        /*
         * Everything that isn't Fire behaves normally.
         */
        return super.hurt(
                source,
                amount
        );
    }

    private boolean isIceOrWaterDamage(
            DamageSource source
    ) {
        return source.is(KKDamageTypes.WATER)
                || source.is(KKDamageTypes.ICE);
    }

    private boolean isFireDamage(
            DamageSource source
    ) {

        return source.is(KKDamageTypes.FIRE);

    }

    private void growFromFireDamage() {
        if (this.fireGrowthStage
                >= MAX_FIRE_GROWTH_STAGE) {
            return;
        }

        this.fireGrowthStage++;

        applyFireGrowthScale();

        /*
         * Little visual burst so the growth doesn't feel silent.
         */
        if (this.level()
                instanceof ServerLevel serverLevel) {

            serverLevel.sendParticles(
                    ParticleTypes.FLAME,
                    this.getX(),
                    this.getY()
                            + this.getBbHeight()
                            * 0.50D,
                    this.getZ(),
                    12
                            + this.fireGrowthStage
                            * 4,
                    0.25D
                            * this.getScale(),
                    0.25D
                            * this.getScale(),
                    0.25D
                            * this.getScale(),
                    0.03D
            );

            this.level().playSound(
                    null,
                    this.getX(),
                    this.getY(),
                    this.getZ(),
                    SoundEvents.FIRECHARGE_USE,
                    SoundSource.HOSTILE,
                    0.8F,
                    0.9F
                            - this.fireGrowthStage
                            * 0.10F
            );
        }

        /*
         * Third successful fire hit:
         *
         * Grow first, then immediately enter the existing
         * Self-Destruct windup.
         */
        if (this.fireGrowthStage
                >= MAX_FIRE_GROWTH_STAGE) {

            startSelfDestruct();
        }
    }

    private void applyFireGrowthScale() {
        AttributeInstance scale =
                this.getAttribute(
                        Attributes.SCALE
                );

        if (scale == null) {
            return;
        }

        /*
         * Don't overwrite the Bomb's base scale.
         *
         * This means another system can still change its normal
         * scale without this growth mechanic fighting it.
         */
        scale.removeModifier(
                FIRE_GROWTH_SCALE_ID
        );

        if (this.fireGrowthStage <= 0) {
            return;
        }

        double growth =
                this.fireGrowthStage
                        * FIRE_GROWTH_PER_STAGE;

        scale.addOrUpdateTransientModifier(
                new AttributeModifier(
                        FIRE_GROWTH_SCALE_ID,
                        growth,
                        AttributeModifier.Operation.ADD_MULTIPLIED_BASE
                )
        );
    }

    // ============================================================
    // SELF-DESTRUCT
    // ============================================================

    private void startSelfDestruct() {
        if (this.getBombAction()
                == ACTION_SELF_DESTRUCT) {
            return;
        }

        clearPendingAttacks();

        this.getNavigation().stop();

        this.setAction(
                ACTION_SELF_DESTRUCT,
                SELF_DESTRUCT_TICKS
        );

        this.selfDestructTicks =
                SELF_DESTRUCT_TICKS;

        this.setDeltaMovement(
                Vec3.ZERO
        );

        this.level().playSound(
                null,
                this.getX(),
                this.getY(),
                this.getZ(),
                SoundEvents.TNT_PRIMED,
                SoundSource.HOSTILE,
                1.0F,
                this.isVolcano()
                        ? 0.65F
                        : this.isGrenade()
                        ? 0.80F
                        : 1.0F
        );
    }

    private void resetSelfDestructConsiderTimer() {
        this.selfDestructConsiderTicks =
                Mth.nextInt(
                        this.getRandom(),
                        SELF_DESTRUCT_CONSIDER_MIN_TICKS,
                        SELF_DESTRUCT_CONSIDER_MAX_TICKS
                );
    }

    private void tickSelfDestruct() {
        if (this.selfDestructTicks <= 0) {
            doSelfDestructExplosion();
            return;
        }

        this.selfDestructTicks--;

        this.entityData.set(
                ACTION_TICKS,
                this.selfDestructTicks
        );

        if (this.level()
                instanceof ServerLevel serverLevel) {

            /*
             * Windup becomes more violent near zero.
             */
            int elapsed =
                    SELF_DESTRUCT_TICKS
                            - this.selfDestructTicks;

            if (elapsed % 2 == 0) {
                int particleCount =
                        2
                                + elapsed / 4;

                serverLevel.sendParticles(
                        ParticleTypes.FLAME,
                        this.getX(),
                        this.getY()
                                + this.getBbHeight()
                                * 0.55D,
                        this.getZ(),
                        particleCount,
                        0.30D,
                        0.30D,
                        0.30D,
                        0.03D
                );
            }

            if (this.selfDestructTicks <= 6) {
                serverLevel.sendParticles(
                        ParticleTypes.LAVA,
                        this.getX(),
                        this.getY()
                                + this.getBbHeight()
                                * 0.55D,
                        this.getZ(),
                        3,
                        0.25D,
                        0.25D,
                        0.25D,
                        0.05D
                );
            }
        }

        if (this.selfDestructTicks <= 0) {
            doSelfDestructExplosion();
        }
    }

    private void doSelfDestructExplosion() {
        if (!(this.level()
                instanceof ServerLevel serverLevel)) {
            return;
        }

        float radius =
                getSelfDestructRadius();

        float maxDamage =
                getSelfDestructDamage();

        AABB area =
                this.getBoundingBox()
                        .inflate(
                                radius,
                                radius * 0.65D,
                                radius
                        );

        for (LivingEntity target
                : serverLevel.getEntitiesOfClass(
                LivingEntity.class,
                area,
                this::canSelfDestructHit
        )) {

            double distance =
                    this.position()
                            .distanceTo(
                                    target.position()
                            );

            if (distance > radius) {
                continue;
            }

            float falloff =
                    1.0F
                            - (float) (
                            distance
                                    / radius
                    );

            /*
             * Edge still deals 25% instead of dropping to zero.
             */
            falloff =
                    Math.max(
                            0.25F,
                            falloff
                    );

            float damage =
                    maxDamage
                            * falloff;

            target.hurt(
                    this.damageSources()
                            .mobAttack(this),
                    damage
            );

            Vec3 knock =
                    target.position()
                            .subtract(
                                    this.position()
                            );

            if (knock.lengthSqr() > 0.0001D) {
                knock = knock.normalize();
            }

            double knockStrength =
                    0.65D
                            + getVariant()
                            * 0.25D;

            target.setDeltaMovement(
                    target.getDeltaMovement()
                            .add(
                                    knock.x
                                            * knockStrength,
                                    0.35D
                                            + getVariant()
                                            * 0.10D,
                                    knock.z
                                            * knockStrength
                            )
            );

            target.hurtMarked = true;
        }

        /*
         * Visual-only explosion.
         *
         * NO level.explode(), so this never destroys terrain.
         */
        serverLevel.sendParticles(
                ParticleTypes.EXPLOSION,
                this.getX(),
                this.getY()
                        + this.getBbHeight()
                        * 0.50D,
                this.getZ(),
                this.isVolcano()
                        ? 12
                        : this.isGrenade()
                        ? 8
                        : 5,
                radius * 0.35D,
                radius * 0.20D,
                radius * 0.35D,
                0.0D
        );

        serverLevel.sendParticles(
                ParticleTypes.FLAME,
                this.getX(),
                this.getY()
                        + this.getBbHeight()
                        * 0.50D,
                this.getZ(),
                this.isVolcano()
                        ? 100
                        : this.isGrenade()
                        ? 70
                        : 45,
                radius * 0.45D,
                radius * 0.30D,
                radius * 0.45D,
                0.16D
        );

        serverLevel.sendParticles(
                ParticleTypes.LARGE_SMOKE,
                this.getX(),
                this.getY()
                        + this.getBbHeight()
                        * 0.50D,
                this.getZ(),
                this.isVolcano()
                        ? 50
                        : this.isGrenade()
                        ? 35
                        : 22,
                radius * 0.35D,
                radius * 0.25D,
                radius * 0.35D,
                0.08D
        );

        this.level().playSound(
                null,
                this.getX(),
                this.getY(),
                this.getZ(),
                SoundEvents.GENERIC_EXPLODE,
                SoundSource.HOSTILE,
                this.isVolcano()
                        ? 2.0F
                        : this.isGrenade()
                        ? 1.6F
                        : 1.25F,
                this.isVolcano()
                        ? 0.65F
                        : this.isGrenade()
                        ? 0.80F
                        : 1.0F
        );

        this.discard();
    }

    private boolean canSelfDestructHit(
            LivingEntity target
    ) {
        if (target == this
                || !target.isAlive()) {
            return false;
        }

        return !(target instanceof BombEntity);
    }

    private float getSelfDestructRadius() {
        return switch (this.getVariant()) {
            case VARIANT_GRENADE -> 5.0F;
            case VARIANT_VOLCANO -> 6.0F;
            default -> 4.0F;
        };
    }

    private float getSelfDestructDamage() {
        /*
         * ATTACK_DAMAGE has already been scaled by Kingdom Keys for the mob's
         * actual assigned level. Variant controls how explosive that scaled
         * power becomes.
         */
        float scaledAttack =
                (float) this.getAttributeValue(
                        Attributes.ATTACK_DAMAGE
                );

        float multiplier =
                switch (this.getVariant()) {
                    case VARIANT_GRENADE -> 3.6F;
                    case VARIANT_VOLCANO -> 4.2F;
                    default -> 3F;
                };

        return scaledAttack
                * multiplier;
    }

    // ============================================================
// DYNAMIC LIGHT
// ============================================================

    private void tickDynamicLight() {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        /*
         * Don't update the light every single tick.
         * Four ticks is frequent enough to follow the mob smoothly
         * without constantly hammering block/light updates.
         */
        this.lightUpdateTicks++;

        if (this.lightUpdateTicks
                < LIGHT_UPDATE_INTERVAL_TICKS) {
            return;
        }

        this.lightUpdateTicks = 0;

        /*
         * Put the invisible light around the center of the Bomb's body
         * rather than down at its feet.
         */
        BlockPos desiredPos =
                BlockPos.containing(
                        this.getX(),
                        this.getY()
                                + this.getBbHeight() * 0.55D,
                        this.getZ()
                );

        int desiredLevel =
                getDynamicLightLevel();

        /*
         * If we're still occupying the same block, just make sure
         * its light level is correct.
         */
        if (desiredPos.equals(this.activeLightBlockPos)) {

            BlockState state =
                    serverLevel.getBlockState(desiredPos);

            if (state.is(Blocks.LIGHT)) {

                if (state.getValue(LightBlock.LEVEL)
                        != desiredLevel) {

                    serverLevel.setBlock(
                            desiredPos,
                            state.setValue(
                                    LightBlock.LEVEL,
                                    desiredLevel
                            ),
                            Block.UPDATE_CLIENTS
                    );
                }

                return;
            }

            /*
             * Something replaced our light block.
             * Forget about the old position and try again.
             */
            this.activeLightBlockPos = null;
        }

        removeDynamicLight();


        BlockState stateAtNewPos =
                serverLevel.getBlockState(desiredPos);

        if (!stateAtNewPos.isAir()) {
            return;
        }

        BlockState lightState =
                Blocks.LIGHT
                        .defaultBlockState()
                        .setValue(
                                LightBlock.LEVEL,
                                desiredLevel
                        );

        serverLevel.setBlock(
                desiredPos,
                lightState,
                Block.UPDATE_CLIENTS
        );

        this.activeLightBlockPos =
                desiredPos.immutable();
    }

    private int getDynamicLightLevel() {

        /*
         * Self-destructing Bombs flare to maximum brightness.
         */
        if (this.getBombAction()
                == ACTION_SELF_DESTRUCT) {

            return 15;
        }

        int baseLight =
                switch (this.getVariant()) {
                    case VARIANT_GRENADE -> 10;
                    case VARIANT_VOLCANO -> 15;
                    default -> 12;
                };

        /*
         * Fire absorption makes the Bomb progressively hotter/brighter.
         *
         * Bomb:
         *   12 -> 13 -> 14 -> 15
         *
         * Grenade:
         *   10 -> 11 -> 12 -> 13
         *
         * Volcano is already capped at 15.
         */
        return Mth.clamp(
                baseLight
                        + this.fireGrowthStage,
                0,
                15
        );
    }

    private void removeDynamicLight() {
        if (this.activeLightBlockPos == null
                || !(this.level()
                instanceof ServerLevel serverLevel)) {
            return;
        }

        BlockState state =
                serverLevel.getBlockState(
                        this.activeLightBlockPos
                );

        if (state.is(Blocks.LIGHT)) {

            serverLevel.setBlock(
                    this.activeLightBlockPos,
                    Blocks.AIR.defaultBlockState(),
                    Block.UPDATE_CLIENTS
            );
        }

        this.activeLightBlockPos = null;
    }

    @Override
    public void remove(RemovalReason reason) {
        if (!this.level().isClientSide) {
            removeDynamicLight();
        }

        super.remove(reason);
    }


    // ============================================================
    // HELPERS
    // ============================================================

    private void setAction(
            int action,
            int ticks
    ) {
        if (this.level().isClientSide) {
            return;
        }

        this.entityData.set(
                ACTION,
                action
        );

        this.entityData.set(
                ACTION_TICKS,
                Math.max(
                        0,
                        ticks
                )
        );
    }

    private LivingEntity getLivingEntityById(
            int entityId
    ) {
        if (entityId < 0
                || !(this.level()
                instanceof ServerLevel serverLevel)) {
            return null;
        }

        Entity entity =
                serverLevel.getEntity(
                        entityId
                );

        if (entity
                instanceof LivingEntity livingEntity) {
            return livingEntity;
        }

        return null;
    }

    private void clearPendingAttacks() {
        this.pendingMeleeTargetId = -1;
        this.pendingMeleeDelay = 0;

        this.pendingCastTargetId = -1;
        this.pendingCastDelay = 0;
    }


    // ============================================================
    // DEATH
    // ============================================================

    @Override
    public void die(DamageSource source) {
        if (!this.level().isClientSide
                && this.getBombAction()
                != ACTION_SELF_DESTRUCT) {

            clearPendingAttacks();

            this.entityData.set(
                    ACTION,
                    ACTION_DEATH
            );

            this.entityData.set(
                    ACTION_TICKS,
                    20
            );

            this.getNavigation().stop();
        }

        super.die(source);
    }


    // ============================================================
    // ANIMATION
    // ============================================================

    @Override
    public void registerControllers(
            AnimatableManager.ControllerRegistrar controllers
    ) {
        controllers.add(
                new AnimationController<>(
                        this,
                        "controller",
                        2,
                        state -> {
                            int action =
                                    this.getBombAction();

                            int actionTicks =
                                    this.getBombActionTicks();

                            if (this.deathTime > 0
                                    || this.getHealth() <= 0.0F
                                    || action == ACTION_DEATH) {

                                state.setAnimation(
                                        DEATH_ANIM
                                );

                                return PlayState.CONTINUE;
                            }

                            if (action == ACTION_SELF_DESTRUCT) {
                                state.setAnimation(
                                        SELF_DESTRUCT_ANIM
                                );

                                return PlayState.CONTINUE;
                            }

                            if (action != ACTION_NONE
                                    && actionTicks > 0) {

                                if (action == ACTION_ATTACK) {
                                    state.setAnimation(
                                            ATTACK_ANIM
                                    );

                                    return PlayState.CONTINUE;
                                }

                                if (action == ACTION_CAST) {
                                    state.setAnimation(
                                            CAST_ANIM
                                    );

                                    return PlayState.CONTINUE;
                                }
                            }

                            /*
                             * Bomb's normal state is always its looping idle.
                             */
                            state.setAnimation(
                                    IDLE_ANIM
                            );

                            return PlayState.CONTINUE;
                        }
                )
        );
    }

    @Override
    protected void updateWalkAnimation(
            float partialTick
    ) {
        /*
         * There is no separate walk animation in your Bomb animation file.
         * Keep vanilla walk state quiet and let GeckoLib's idle loop handle
         * its floating motion while it travels.
         */
        float speed =
                this.getPose() == Pose.STANDING
                        ? Math.min(
                        partialTick
                                * 4.0F,
                        1.0F
                )
                        : 0.0F;

        this.walkAnimation.update(
                speed,
                0.2F
        );
    }

    @Override
    protected AABB makeBoundingBox() {
        AABB box =
                super.makeBoundingBox();

        /*
         * Keep the bottom of the hitbox at the entity's actual
         * ground position so movement / gravity remain stable.
         *
         * Only extend upward to better cover the floating model.
         */
        double extraHeight =
                0.40D * this.getScale();

        return new AABB(
                box.minX,
                box.minY,
                box.minZ,

                box.maxX,
                box.maxY + extraHeight,
                box.maxZ
        );
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }


    // ============================================================
    // SAVE / LOAD
    // ============================================================

    @Override
    public void addAdditionalSaveData(
            CompoundTag tag
    ) {
        super.addAdditionalSaveData(tag);

        tag.putInt(
                "BombVariant",
                this.getVariant()
        );

        tag.putInt(
                "BombFireGrowthStage",
                this.fireGrowthStage
        );

    }

    @Override
    public void readAdditionalSaveData(
            CompoundTag tag
    ) {
        super.readAdditionalSaveData(tag);

        if (tag.contains("BombVariant")) {
            this.setVariant(
                    tag.getInt(
                            "BombVariant"
                    )
            );
        }

        if (tag.contains("BombFireGrowthStage")) {
            this.fireGrowthStage =
                    Mth.clamp(
                            tag.getInt(
                                    "BombFireGrowthStage"
                            ),
                            0,
                            MAX_FIRE_GROWTH_STAGE
                    );

            applyFireGrowthScale();
        }
    }
}
