package online.remind.remind.entity.spirits;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import online.kingdomkeys.kingdomkeys.data.PlayerData;
import online.remind.remind.capabilities.GlobalDataRM;
import online.remind.remind.capabilities.ModDataRM;
import online.remind.remind.client.sound.ModSoundsRM;
import online.remind.remind.dreameater.DreamEaterExpHandler;
import online.remind.remind.entity.ModEntitiesRM;
import online.remind.remind.item.ModItemsRM;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class TonberrySpiritEntity extends PathfinderMob implements GeoEntity {
    private static final int LIGHT_UPDATE_INTERVAL_TICKS = 4;
    private BlockPos activeLightBlockPos = null;
    private int lightUpdateTicks = 0;

    private static final int ACTION_NONE = 0;
    private static final int ACTION_STAB = 1;
    private static final int ACTION_GRUDGE = 2;
    private static final int ACTION_DEATH = 3;

    private static final int STAB_ACTION_TICKS = 26;
    private static final int STAB_DAMAGE_DELAY_TICKS = 13;

    private static final int GRUDGE_ACTION_TICKS = 46;
    private static final int GRUDGE_DAMAGE_DELAY_TICKS = 24;
    private static final int GRUDGE_COOLDOWN_TICKS = 120;

    private static final int DEATH_ACTION_TICKS = 70;

    private static final int GOLD_FEED_EXP = 15;
    private static final int EMERALD_FEED_EXP = 30;
    private static final int CHEFS_KNIFE_FEED_EXP = 50;
    private static final int FEED_COOLDOWN_TICKS = 10;

    private static final int STAT_UPDATE_INTERVAL_TICKS = 20;

    private static final double FOLLOW_START_DISTANCE = 8.0D;
    private static final double FOLLOW_STOP_DISTANCE = 4.0D;

    /*
     * Tonberry Spirit stat identity:
     *
     * Slow.
     * Very tanky.
     * High physical damage.
     * Low magic contribution.
     *
     * These values intentionally let it compete with Cactuar at the same Dream Eater level,
     * while still feeling different. Cactuar is faster and multi-hit. Tonberry is slower,
     * heavier, and hits harder per attack.
     */
    private static final double BASE_MAX_HEALTH = 55.0D;
    private static final double LEVEL_TO_MAX_HEALTH = 2.75D;
    private static final double OWNER_HP_TO_MAX_HEALTH = 0.75D;
    private static final double OWNER_DEF_TO_MAX_HEALTH = 1.85D;
    private static final double MAX_HEALTH_CAP = 650.0D;

    private static final double BASE_ATTACK = 8.0D;
    private static final double LEVEL_TO_ATTACK = 0.50D;
    private static final double OWNER_STR_TO_ATTACK = 0.55D;
    private static final double ATTACK_CAP = 120.0D;

    private static final double BASE_ARMOR = 8.0D;
    private static final double LEVEL_TO_ARMOR = 0.18D;
    private static final double OWNER_DEF_TO_ARMOR = 0.20D;
    private static final double ARMOR_CAP = 38.0D;

    private static final double BASE_SPEED = 0.135D;
    private static final double LEVEL_TO_SPEED = 0.0006D;
    private static final double OWNER_MAG_TO_SPEED = 0.00025D;
    private static final double SPEED_CAP = 0.225D;

    private static final double BASE_KNOCKBACK_RESISTANCE = 0.55D;
    private static final double LEVEL_TO_KNOCKBACK_RESISTANCE = 0.002D;
    private static final double KNOCKBACK_RESISTANCE_CAP = 0.92D;

    private static final EntityDataAccessor<Optional<UUID>> OWNER_UUID =
            SynchedEntityData.defineId(TonberrySpiritEntity.class, EntityDataSerializers.OPTIONAL_UUID);

    private static final EntityDataAccessor<Integer> ACTION =
            SynchedEntityData.defineId(TonberrySpiritEntity.class, EntityDataSerializers.INT);

    private static final EntityDataAccessor<Integer> ACTION_TICKS =
            SynchedEntityData.defineId(TonberrySpiritEntity.class, EntityDataSerializers.INT);

    private static final RawAnimation IDLE =
            RawAnimation.begin().thenLoop("idle");

    private static final RawAnimation WALK =
            RawAnimation.begin().thenLoop("walk");

    private static final RawAnimation STAB =
            RawAnimation.begin().thenPlay("stab");

    private static final RawAnimation GRUDGE =
            RawAnimation.begin().thenPlay("grudge");

    private static final RawAnimation DEATH =
            RawAnimation.begin().thenPlayAndHold("death");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private int statUpdateTicks = 0;
    private int feedCooldownTicks = 0;
    private int customDeathTicks = -1;

    private int pendingStabTargetId = -1;
    private int pendingStabDamageDelay = 0;

    private int grudgeCooldownTicks = 0;
    private int pendingGrudgeTargetId = -1;
    private int pendingGrudgeDamageDelay = 0;
    private float pendingGrudgeDamage = 0.0F;

    private boolean hasAppliedOwnerScaling = false;

    public TonberrySpiritEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        this.xpReward = 0;
    }

    public TonberrySpiritEntity(Level level, Player owner) {
        this(ModEntitiesRM.TYPE_TONBERRY_SPIRIT.get(), level);
        this.setOwnerUUID(owner.getUUID());
        this.moveTo(owner.getX(), owner.getY() + 0.1D, owner.getZ(), owner.getYRot(), owner.getXRot());
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 80.0D)
                .add(Attributes.ATTACK_DAMAGE, 12.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.16D)
                .add(Attributes.FOLLOW_RANGE, 36.0D)
                .add(Attributes.ARMOR, 10.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.55D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);

        builder.define(OWNER_UUID, Optional.empty());
        builder.define(ACTION, ACTION_NONE);
        builder.define(ACTION_TICKS, 0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new TonberrySpiritMeleeGoal(this, 1.0D, false));
        this.goalSelector.addGoal(2, new FollowOwnerGoal(this, 1.05D));
        this.goalSelector.addGoal(3, new RandomStrollGoal(this, 0.45D));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(
                this,
                Monster.class,
                10,
                true,
                false,
                this::isValidSpiritTarget
        ));
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide) {
            tickTonberryLight();
        }

        if (!this.level().isClientSide
                && (this.customDeathTicks >= 0
                || this.getHealth() <= 0.0F
                || this.getTonberryAction() == ACTION_DEATH)) {

            tickCustomDeath();
            return;
        }

        if (this.isPlayingStabAnimation()
                || this.isPlayingGrudgeAnimation()) {
            lockMovement();
        }

        if (!this.level().isClientSide) {
            tickAction();
            tickDelayedStab();
            tickGrudge();
            tickFeedingCooldown();
            tickStats();
            discardIfOwnerMissing();
        }
    }

    // Tonberry Spirit light
    private void tickTonberryLight() {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        if (this.lightUpdateTicks > 0) {
            this.lightUpdateTicks--;
            return;
        }

        this.lightUpdateTicks = LIGHT_UPDATE_INTERVAL_TICKS;

        BlockPos lightPos = getTonberryLightPosition();

        if (this.activeLightBlockPos != null
                && !this.activeLightBlockPos.equals(lightPos)) {

            removeTonberryLightBlock(
                    serverLevel,
                    this.activeLightBlockPos
            );

            this.activeLightBlockPos = null;
        }

        if (!canPlaceTonberryLight(
                serverLevel,
                lightPos
        )) {
            return;
        }

        serverLevel.setBlock(
                lightPos,
                Blocks.LIGHT
                        .defaultBlockState()
                        .setValue(
                                LightBlock.LEVEL,
                                getTonberryLightLevel()
                        ),
                3
        );

        this.activeLightBlockPos =
                lightPos.immutable();
    }

    private BlockPos getTonberryLightPosition() {
        return this.blockPosition().above();
    }

    private int getTonberryLightLevel() {
        return 12;
    }

    private boolean canPlaceTonberryLight(
            ServerLevel serverLevel,
            BlockPos pos
    ) {
        BlockState state =
                serverLevel.getBlockState(pos);

        return state.isAir()
                || state.is(Blocks.LIGHT);
    }

    private void removeTonberryLightBlock(
            ServerLevel serverLevel,
            BlockPos pos
    ) {
        BlockState state =
                serverLevel.getBlockState(pos);

        if (state.is(Blocks.LIGHT)) {
            serverLevel.removeBlock(
                    pos,
                    false
            );
        }
    }

    private void removeTonberryLightBlock() {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        if (this.activeLightBlockPos == null) {
            return;
        }

        removeTonberryLightBlock(
                serverLevel,
                this.activeLightBlockPos
        );

        this.activeLightBlockPos = null;
    }

    // Handles Tonberry's custom death sequence
    private void tickCustomDeath() {

        if (this.customDeathTicks < 0) {
            this.customDeathTicks = 0;

            setTonberryAction(
                    ACTION_DEATH,
                    DEATH_ACTION_TICKS
            );

            this.setTarget(null);

            this.pendingStabTargetId = -1;
            this.pendingStabDamageDelay = 0;

            this.pendingGrudgeTargetId = -1;
            this.pendingGrudgeDamageDelay = 0;
            this.pendingGrudgeDamage = 0.0F;
        }

        lockMovement();

        this.customDeathTicks++;

        this.entityData.set(
                ACTION_TICKS,
                Math.max(
                        0,
                        DEATH_ACTION_TICKS - this.customDeathTicks
                )
        );

        if (this.customDeathTicks >= DEATH_ACTION_TICKS) {

            if (!this.isRemoved()) {
                this.level().broadcastEntityEvent(
                        this,
                        (byte) 60
                );

                this.remove(
                        Entity.RemovalReason.KILLED
                );
            }
        }
    }

    // Removes Tonberry Spirit's light when the entity disappears
    @Override
    public void remove(RemovalReason reason) {
        removeTonberryLightBlock();
        super.remove(reason);
    }



    private void tickFeedingCooldown() {
        if (this.feedCooldownTicks > 0) {
            this.feedCooldownTicks--;
        }
    }

    private void tickStats() {
        if (this.statUpdateTicks > 0) {
            this.statUpdateTicks--;
            return;
        }

        this.statUpdateTicks = STAT_UPDATE_INTERVAL_TICKS;
        applyDreamEaterStats();
    }

    private void applyDreamEaterStats() {
        int level = getDreamEaterLevel();
        Player owner = getOwnerPlayer();
        PlayerData ownerData = owner != null ? PlayerData.get(owner) : null;

        applyOwnerScaling(level, ownerData);
    }

    private void applyOwnerScaling(int level, @Nullable PlayerData ownerData) {
        level = Mth.clamp(level, 1, GlobalDataRM.DREAM_EATER_MAX_LEVEL);

        double ownerMaxHP = getOwnerMaxHP(ownerData);
        double ownerStrength = getOwnerStrength(ownerData);
        double ownerMagic = getOwnerMagic(ownerData);
        double ownerDefense = getOwnerDefense(ownerData);

        double maxHealth = BASE_MAX_HEALTH
                + level * LEVEL_TO_MAX_HEALTH
                + ownerMaxHP * OWNER_HP_TO_MAX_HEALTH
                + ownerDefense * OWNER_DEF_TO_MAX_HEALTH;

        double attack = BASE_ATTACK
                + level * LEVEL_TO_ATTACK
                + ownerStrength * OWNER_STR_TO_ATTACK;

        double armor = BASE_ARMOR
                + level * LEVEL_TO_ARMOR
                + ownerDefense * OWNER_DEF_TO_ARMOR;

        double speed = BASE_SPEED
                + Math.min(0.065D, level * LEVEL_TO_SPEED)
                + Math.min(0.025D, ownerMagic * OWNER_MAG_TO_SPEED);

        double knockbackResistance = BASE_KNOCKBACK_RESISTANCE
                + level * LEVEL_TO_KNOCKBACK_RESISTANCE;

        maxHealth = Mth.clamp(maxHealth, 1.0D, MAX_HEALTH_CAP);
        attack = Mth.clamp(attack, 1.0D, ATTACK_CAP);
        armor = Mth.clamp(armor, 0.0D, ARMOR_CAP);
        speed = Mth.clamp(speed, BASE_SPEED, SPEED_CAP);
        knockbackResistance = Mth.clamp(knockbackResistance, 0.0D, KNOCKBACK_RESISTANCE_CAP);

        float oldMaxHealth = this.getMaxHealth();
        float oldHealth = this.getHealth();
        boolean wasFullHealth = oldMaxHealth <= 0.0F || oldHealth >= oldMaxHealth - 0.5F;

        setAttributeBaseValue(Attributes.MAX_HEALTH, maxHealth);
        setAttributeBaseValue(Attributes.ATTACK_DAMAGE, attack);
        setAttributeBaseValue(Attributes.ARMOR, armor);
        setAttributeBaseValue(Attributes.MOVEMENT_SPEED, speed);
        setAttributeBaseValue(Attributes.KNOCKBACK_RESISTANCE, knockbackResistance);

        float newMaxHealth = this.getMaxHealth();

        /*
         * First scaling pass should not leave Tonberry sitting at the weak default HP.
         * After that, preserve HP percent unless it was already full.
         */
        if (!this.hasAppliedOwnerScaling || wasFullHealth) {
            this.setHealth(newMaxHealth);
        } else if (oldMaxHealth > 0.0F) {
            float healthPercent = Mth.clamp(oldHealth / oldMaxHealth, 0.0F, 1.0F);
            this.setHealth(Mth.clamp(newMaxHealth * healthPercent, 1.0F, newMaxHealth));
        } else if (this.getHealth() > newMaxHealth) {
            this.setHealth(newMaxHealth);
        }

        this.hasAppliedOwnerScaling = true;
    }

    private double getOwnerMaxHP(@Nullable PlayerData ownerData) {
        if (ownerData == null) {
            return 20.0D;
        }

        return Math.max(20.0D, ownerData.getMaxHP());
    }

    private double getOwnerStrength(@Nullable PlayerData ownerData) {
        if (ownerData == null || ownerData.getStrengthStat() == null) {
            return 1.0D;
        }

        return Math.max(1.0D, ownerData.getStrengthStat().getStat());
    }

    private double getOwnerMagic(@Nullable PlayerData ownerData) {
        if (ownerData == null || ownerData.getMagicStat() == null) {
            return 1.0D;
        }

        return Math.max(1.0D, ownerData.getMagicStat().getStat());
    }

    private double getOwnerDefense(@Nullable PlayerData ownerData) {
        if (ownerData == null || ownerData.getDefenseStat() == null) {
            return 1.0D;
        }

        return Math.max(1.0D, ownerData.getDefenseStat().getStat());
    }

    private void setAttributeBaseValue(net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute> attribute, double value) {
        AttributeInstance instance = this.getAttribute(attribute);

        if (instance != null) {
            instance.setBaseValue(value);
        }
    }

    private int getDreamEaterLevel() {
        Player owner = getOwnerPlayer();

        if (owner == null) {
            return 1;
        }

        GlobalDataRM globalData = ModDataRM.getGlobal(owner);

        if (globalData == null) {
            return 1;
        }

        return Math.max(1, globalData.getDreamEaterLevel(GlobalDataRM.DREAM_EATER_TONBERRY));
    }

    private void discardIfOwnerMissing() {
        if (this.tickCount < 40) {
            return;
        }

        Player owner = getOwnerPlayer();

        if (getOwnerUUID() == null || owner == null) {
            this.discard();
            return;
        }

        GlobalDataRM data = ModDataRM.getGlobal(owner);

        if (data == null
                || !data.hasDreamEaterSummoned()
                || data.getDreamEaterUUID() == null
                || !this.getUUID().equals(data.getDreamEaterUUID())) {
            this.discard();
        }
    }

    private void tickAction() {
        int ticks = this.getTonberryActionTicks();

        if (ticks <= 0) {
            if (this.getTonberryAction() != ACTION_NONE && !this.isDeadOrDying()) {
                setTonberryAction(ACTION_NONE, 0);
            }

            return;
        }

        this.entityData.set(ACTION_TICKS, ticks - 1);

        if (ticks - 1 <= 0 && !this.isDeadOrDying()) {
            setTonberryAction(ACTION_NONE, 0);
        }
    }

    private void tickDelayedStab() {
        if (this.pendingStabDamageDelay <= 0) {
            return;
        }

        this.pendingStabDamageDelay--;

        if (this.pendingStabDamageDelay <= 0) {
            applyPendingStabDamage();
        }
    }

    private void tickGrudge() {
        if (this.grudgeCooldownTicks > 0) {
            this.grudgeCooldownTicks--;
        }

        if (this.pendingGrudgeDamageDelay <= 0) {
            return;
        }

        this.pendingGrudgeDamageDelay--;

        if (this.pendingGrudgeDamageDelay <= 0) {
            applyPendingGrudgeDamage();
        }
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        if (this.level().isClientSide) {
            return false;
        }

        if (!(target instanceof LivingEntity livingTarget)) {
            return false;
        }

        if (!isValidSpiritTarget(livingTarget)) {
            return false;
        }

        if (this.isDeadOrDying()
                || this.isPlayingStabAnimation()
                || this.isPlayingGrudgeAnimation()
                || this.pendingStabTargetId != -1) {
            return false;
        }

        startDelayedStab(livingTarget);
        return true;
    }

    private void startDelayedStab(LivingEntity target) {
        this.pendingStabTargetId = target.getId();
        this.pendingStabDamageDelay = STAB_DAMAGE_DELAY_TICKS;

        triggerStabAnimation();

        this.level().playSound(
                null,
                this.getX(),
                this.getY(),
                this.getZ(),
                SoundEvents.PLAYER_ATTACK_SWEEP,
                this.getSoundSource(),
                0.35F,
                0.65F
        );
    }

    private void applyPendingStabDamage() {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            clearPendingStab();
            return;
        }

        Entity entity = serverLevel.getEntity(this.pendingStabTargetId);

        if (!(entity instanceof LivingEntity target) || !isValidSpiritTarget(target)) {
            clearPendingStab();
            return;
        }

        if (!isTargetInStabRange(target)) {
            clearPendingStab();
            return;
        }

        target.invulnerableTime = 0;
        boolean hit = super.doHurtTarget(target);

        if (hit && this.random.nextFloat() < getGrudgeChance()) {
            tryStartSpiritGrudge(target);
        }

        serverLevel.playSound(
                null,
                target.getX(),
                target.getY(),
                target.getZ(),
                SoundEvents.PLAYER_ATTACK_CRIT,
                this.getSoundSource(),
                0.45F,
                0.75F
        );

        clearPendingStab();
    }

    private boolean isTargetInStabRange(LivingEntity target) {
        double reach = this.getBbWidth() + target.getBbWidth() + 1.05D;
        return this.distanceToSqr(target) <= reach * reach;
    }

    private void clearPendingStab() {
        this.pendingStabTargetId = -1;
        this.pendingStabDamageDelay = 0;
    }

    private float getGrudgeChance() {
        int level = getDreamEaterLevel();
        return Math.min(0.42F, 0.14F + level * 0.003F);
    }

    private void tryStartSpiritGrudge(LivingEntity target) {
        if (this.grudgeCooldownTicks > 0) {
            return;
        }

        if (this.pendingGrudgeTargetId != -1) {
            return;
        }

        if (!isValidSpiritTarget(target)) {
            return;
        }

        this.pendingGrudgeTargetId = target.getId();
        this.pendingGrudgeDamage = calculateSpiritGrudgeDamage();
        this.pendingGrudgeDamageDelay = GRUDGE_DAMAGE_DELAY_TICKS;
        this.grudgeCooldownTicks = GRUDGE_COOLDOWN_TICKS;

        triggerGrudgeAnimation();

        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.playSound(
                    null,
                    this.getX(),
                    this.getY(),
                    this.getZ(),
                    SoundEvents.WITHER_AMBIENT,
                    SoundSource.NEUTRAL,
                    0.45F,
                    0.8F
            );
        }
    }

    private float calculateSpiritGrudgeDamage() {
        int level = getDreamEaterLevel();
        Player owner = getOwnerPlayer();
        PlayerData ownerData = owner != null ? PlayerData.get(owner) : null;

        double ownerStrength = getOwnerStrength(ownerData);
        double ownerDefense = getOwnerDefense(ownerData);

        /*
         * Dream Eater version does NOT use player lifetime kills.
         * It scales from Tonberry level + owner STR/DEF so it remains useful
         * without becoming a server-breaking instant kill.
         */
        double damage = 12.0D
                + level * 0.85D
                + ownerStrength * 0.50D
                + ownerDefense * 0.18D;

        return (float) Mth.clamp(damage, 8.0D, 180.0D);
    }

    private void applyPendingGrudgeDamage() {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            clearPendingGrudge();
            return;
        }

        Entity entity = serverLevel.getEntity(this.pendingGrudgeTargetId);

        if (!(entity instanceof LivingEntity target) || !isValidSpiritTarget(target)) {
            clearPendingGrudge();
            return;
        }

        target.invulnerableTime = 0;
        target.hurt(this.damageSources().mobAttack(this), this.pendingGrudgeDamage);

        serverLevel.playSound(
                null,
                target.getX(),
                target.getY(),
                target.getZ(),
                SoundEvents.WITHER_SHOOT,
                SoundSource.NEUTRAL,
                0.45F,
                0.9F
        );

        clearPendingGrudge();
    }

    private void clearPendingGrudge() {
        this.pendingGrudgeTargetId = -1;
        this.pendingGrudgeDamage = 0.0F;
        this.pendingGrudgeDamageDelay = 0;
    }

    private boolean isValidSpiritTarget(@Nullable LivingEntity target) {
        if (target == null || !target.isAlive()) {
            return false;
        }

        if (target == this) {
            return false;
        }

        UUID ownerUUID = getOwnerUUID();

        if (ownerUUID != null && target.getUUID().equals(ownerUUID)) {
            return false;
        }

        if (target instanceof Player) {
            return false;
        }

        if (target instanceof TonberrySpiritEntity) {
            return false;
        }

        if (target instanceof ChirithyEntity) {
            return false;
        }

        if (target instanceof MeowWowEntity) {
            return false;
        }

        if (target instanceof KomoryBatEntity) {
            return false;
        }

        if (target instanceof CactuarSpiritEntity) {
            return false;
        }

        return target instanceof Monster;
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack heldStack = player.getItemInHand(hand);

        int feedExp = getFeedExp(heldStack);

        if (feedExp > 0) {
            return feedTonberry(player, heldStack, feedExp);
        }

        return super.mobInteract(player, hand);
    }

    private InteractionResult feedTonberry(Player player, ItemStack heldStack, int feedExp) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.SUCCESS;
        }

        if (!isOwner(serverPlayer)) {
            serverPlayer.displayClientMessage(
                    Component.literal("This Tonberry Spirit does not belong to you.")
                            .withStyle(ChatFormatting.RED),
                    true
            );

            return InteractionResult.CONSUME;
        }

        if (this.feedCooldownTicks > 0) {
            return InteractionResult.CONSUME;
        }

        if (!serverPlayer.getAbilities().instabuild) {
            heldStack.shrink(1);
        }

        this.feedCooldownTicks = FEED_COOLDOWN_TICKS;

        DreamEaterExpHandler.giveDreamEaterExp(
                serverPlayer,
                GlobalDataRM.DREAM_EATER_TONBERRY,
                feedExp,
                this
        );

        return InteractionResult.CONSUME;
    }

    private int getFeedExp(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return 0;
        }

        if (stack.is(Items.GOLD_INGOT)) {
            return GOLD_FEED_EXP;
        }

        if (stack.is(Items.EMERALD)) {
            return EMERALD_FEED_EXP;
        }

        if (stack.is( ModItemsRM.chefsKnife.get())){
            return CHEFS_KNIFE_FEED_EXP;
        }

        return 0;
    }

    public void setOwnerUUID(@Nullable UUID uuid) {
        this.entityData.set(OWNER_UUID, Optional.ofNullable(uuid));
    }

    @Nullable
    public UUID getOwnerUUID() {
        return this.entityData.get(OWNER_UUID).orElse(null);
    }

    @Nullable
    public Player getOwnerPlayer() {
        UUID ownerUUID = getOwnerUUID();

        if (ownerUUID == null || this.level().getServer() == null) {
            return null;
        }

        return this.level().getServer().getPlayerList().getPlayer(ownerUUID);
    }

    private boolean isOwner(Player player) {
        UUID ownerUUID = getOwnerUUID();
        return ownerUUID != null && ownerUUID.equals(player.getUUID());
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        return isValidSpiritTarget(target) && super.canAttack(target);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        Entity attacker = source.getEntity();

        if (attacker instanceof Player player && isOwner(player)) {
            return false;
        }

        return super.hurt(source, amount);
    }

    @Override
    public void die(DamageSource damageSource) {

        if (!this.level().isClientSide
                && this.customDeathTicks < 0) {

            this.customDeathTicks = 0;

            setTonberryAction(
                    ACTION_DEATH,
                    DEATH_ACTION_TICKS
            );

            lockMovement();
        }

        super.die(damageSource);
    }

    @Override
    protected void tickDeath() {
        lockMovement();
    }

    private void lockMovement() {
        this.getNavigation().stop();
        this.setDeltaMovement(Vec3.ZERO);
        this.hasImpulse = true;
        this.hurtMarked = true;
    }

    public void triggerStabAnimation() {
        if (this.isDeadOrDying()) {
            return;
        }

        setTonberryAction(ACTION_STAB, STAB_ACTION_TICKS);
    }

    public void triggerGrudgeAnimation() {
        if (this.isDeadOrDying()) {
            return;
        }

        setTonberryAction(ACTION_GRUDGE, GRUDGE_ACTION_TICKS);
    }

    protected void setTonberryAction(int action, int ticks) {
        this.entityData.set(ACTION, action);
        this.entityData.set(ACTION_TICKS, Math.max(0, ticks));
    }

    public int getTonberryAction() {
        return this.entityData.get(ACTION);
    }

    public int getTonberryActionTicks() {
        return this.entityData.get(ACTION_TICKS);
    }

    public boolean isPlayingStabAnimation() {
        return getTonberryAction() == ACTION_STAB && getTonberryActionTicks() > 0;
    }

    public boolean isPlayingGrudgeAnimation() {
        return getTonberryAction() == ACTION_GRUDGE && getTonberryActionTicks() > 0;
    }

    public boolean isPlayingDeathAnimation() {
        return getTonberryAction() == ACTION_DEATH || this.isDeadOrDying();
    }

    private boolean shouldPlayWalkAnimation() {
        if (this.isPlayingStabAnimation()
                || this.isPlayingGrudgeAnimation()
                || this.isPlayingDeathAnimation()) {
            return false;
        }

        if (this.getNavigation() != null && !this.getNavigation().isDone()) {
            return true;
        }

        return this.getDeltaMovement().horizontalDistanceSqr() > 0.00001D;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSoundsRM.TONBERRY_ALIVE.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSoundsRM.TONBERRY_DEATH.get();
    }

    public static void removeExistingTonberrySpirit(ServerLevel level, UUID ownerUUID) {
        if (level == null || ownerUUID == null) {
            return;
        }

        List<TonberrySpiritEntity> spirits = level.getEntitiesOfClass(
                TonberrySpiritEntity.class,
                level.getWorldBorder().getCollisionShape().bounds(),
                spirit -> ownerUUID.equals(spirit.getOwnerUUID())
        );

        for (TonberrySpiritEntity spirit : spirits) {
            spirit.discard();
        }

        for (ServerLevel serverLevel : level.getServer().getAllLevels()) {
            if (serverLevel == level) {
                continue;
            }

            List<TonberrySpiritEntity> otherSpirits = serverLevel.getEntitiesOfClass(
                    TonberrySpiritEntity.class,
                    serverLevel.getWorldBorder().getCollisionShape().bounds(),
                    spirit -> ownerUUID.equals(spirit.getOwnerUUID())
            );

            for (TonberrySpiritEntity spirit : otherSpirits) {
                spirit.discard();
            }
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 2, state -> {
            if (this.isPlayingDeathAnimation()) {
                state.setAnimation(DEATH);
                return PlayState.CONTINUE;
            }

            if (this.isPlayingGrudgeAnimation()) {
                state.setAnimation(GRUDGE);
                return PlayState.CONTINUE;
            }

            if (this.isPlayingStabAnimation()) {
                state.setAnimation(STAB);
                return PlayState.CONTINUE;
            }

            if (this.shouldPlayWalkAnimation()) {
                state.setAnimation(WALK);
                return PlayState.CONTINUE;
            }

            state.setAnimation(IDLE);
            return PlayState.CONTINUE;
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    private static class TonberrySpiritMeleeGoal extends MeleeAttackGoal {
        private final TonberrySpiritEntity tonberry;

        public TonberrySpiritMeleeGoal(TonberrySpiritEntity tonberry, double speedModifier, boolean followingTargetEvenIfNotSeen) {
            super(tonberry, speedModifier, followingTargetEvenIfNotSeen);
            this.tonberry = tonberry;
        }

        @Override
        protected void checkAndPerformAttack(LivingEntity target) {
            if (this.canPerformAttack(target)) {
                this.resetAttackCooldown();
                this.mob.swing(this.mob.getUsedItemHand());
                this.tonberry.doHurtTarget(target);
            }
        }
    }

    private static class FollowOwnerGoal extends Goal {
        private final TonberrySpiritEntity spirit;
        private final double speedModifier;
        private Player owner;
        private int repathTicks;

        public FollowOwnerGoal(TonberrySpiritEntity spirit, double speedModifier) {
            this.spirit = spirit;
            this.speedModifier = speedModifier;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            Player ownerPlayer = this.spirit.getOwnerPlayer();

            if (ownerPlayer == null
                    || !ownerPlayer.isAlive()
                    || ownerPlayer.level() != this.spirit.level()) {
                return false;
            }

            if (this.spirit.distanceToSqr(ownerPlayer) < FOLLOW_START_DISTANCE * FOLLOW_START_DISTANCE) {
                return false;
            }

            this.owner = ownerPlayer;
            return true;
        }

        @Override
        public boolean canContinueToUse() {
            if (this.owner == null
                    || !this.owner.isAlive()
                    || this.owner.level() != this.spirit.level()) {
                return false;
            }

            if (this.spirit.getNavigation().isDone()) {
                return false;
            }

            return this.spirit.distanceToSqr(this.owner) > FOLLOW_STOP_DISTANCE * FOLLOW_STOP_DISTANCE;
        }

        @Override
        public void start() {
            this.repathTicks = 0;
        }

        @Override
        public void stop() {
            this.owner = null;
            this.spirit.getNavigation().stop();
        }

        @Override
        public void tick() {
            if (this.owner == null) {
                return;
            }

            this.spirit.getLookControl().setLookAt(
                    this.owner,
                    10.0F,
                    this.spirit.getMaxHeadXRot()
            );

            if (--this.repathTicks <= 0) {
                this.repathTicks = 10;
                this.spirit.getNavigation().moveTo(
                        this.owner,
                        this.speedModifier
                );
            }
        }
    }
}