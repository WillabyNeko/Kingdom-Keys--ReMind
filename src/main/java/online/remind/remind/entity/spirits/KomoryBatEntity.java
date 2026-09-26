package online.remind.remind.entity.spirits;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import online.kingdomkeys.kingdomkeys.client.sound.ModSounds;
import online.kingdomkeys.kingdomkeys.data.PlayerData;
import online.kingdomkeys.kingdomkeys.effects.ModMobEffects;
import online.kingdomkeys.kingdomkeys.util.Utils;
import online.remind.remind.capabilities.GlobalDataRM;
import online.remind.remind.capabilities.ModDataRM;
import online.remind.remind.client.sound.ModSoundsRM;
import online.remind.remind.dreameater.DreamEater;
import online.remind.remind.dreameater.DreamEaterExpHandler;
import online.remind.remind.dreameater.DreamEaterPetHelper;
import online.remind.remind.dreameater.ModDreamEaters;
import online.remind.remind.effect.ModMobEffectsRM;
import online.remind.remind.entity.ModEntitiesRM;
import online.remind.remind.entity.magic.DrainEntity;
import online.remind.remind.lib.StringsRM;
import online.remind.remind.magic.magicDrain;
import online.remind.remind.network.PacketHandlerRM;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.Comparator;
import java.util.Optional;
import java.util.UUID;

public class KomoryBatEntity extends PathfinderMob implements GeoEntity {

    public static final int VARIANT_NORMAL = 0;
    public static final int VARIANT_ORG = 1;

    private static final double SONIC_BOOM_RANGE = 14.0D;
    private static final double SONIC_BOOM_RADIUS = 1.35D;
    private static final int SONIC_BOOM_WINDUP_TICKS = 22;
    private static final int SONIC_BOOM_HIT_TICK = 11;

    private int confusingWavesCooldown = 20 * 9 ;
    private int zeroGravityCooldown = 20 * 5;
    private int drainCooldown = 20 * 10;
    private int hasteCooldown = 20 * 12;
    private int supportCastCooldown = 20 * 7; // 7 Seconds Delay on Summon

    private double komoryHP = 32.0D;
    private double komoryStrength = 8.0D;
    private double komoryMagic = 10.0D;
    private double komoryDefense = 6.0D;

    private static final int KOMORY_BAT_FEED_COOLDOWN_TICKS = 10;
    private static final int SPIDER_EYE_FEED_EXP = 15;
    private static final int PHANTOM_MEMBRANE_FEED_EXP = 45;

    private int komoryBatFeedCooldown = 0;

    private static final EntityDataAccessor<Integer> VARIANT =
            SynchedEntityData.defineId(KomoryBatEntity.class, EntityDataSerializers.INT);

    private static final EntityDataAccessor<Optional<UUID>> OWNER_UUID =
            SynchedEntityData.defineId(KomoryBatEntity.class, EntityDataSerializers.OPTIONAL_UUID);

    private static final EntityDataAccessor<Integer> ATTACK_ANIM_TICKS =
            SynchedEntityData.defineId(KomoryBatEntity.class, EntityDataSerializers.INT);

    private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation WALK_ANIM = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation ATTACK_ANIM = RawAnimation.begin().thenPlay("attack");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private int attackCooldown = 20 * 5; // 5 Seconds before initial attack
    private boolean didSonicBoomHit = false;
    private LivingEntity target;

    public KomoryBatEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        this.moveControl = new FlyingMoveControl(this, 10, true);
        this.setNoGravity(true);
    }

    public KomoryBatEntity(Level level, Player owner) {
        this(ModEntitiesRM.TYPE_KOMORY_BAT.get(), level);
        this.setOwnerUUID(owner.getUUID());
        this.setNoGravity(true);
        this.setPersistenceRequired();
        this.applyOwnerScaling(owner);
        this.setHealth(this.getMaxHealth());
    }

    private void flyToward(Vec3 targetPos, double speed) {
        Vec3 currentPos = this.position();
        Vec3 difference = targetPos.subtract(currentPos);
        double distance = difference.length();

        if (distance < 0.15D) {
            this.setDeltaMovement(this.getDeltaMovement().scale(0.65D));
            return;
        }

        Vec3 wantedMotion = difference.normalize().scale(speed);
        Vec3 currentMotion = this.getDeltaMovement();

        Vec3 newMotion = currentMotion.scale(0.72D).add(wantedMotion.scale(0.28D));

        if (newMotion.length() > speed) {
            newMotion = newMotion.normalize().scale(speed);
        }

        this.setDeltaMovement(newMotion);
        this.hasImpulse = true;

        this.getLookControl().setLookAt(targetPos.x, targetPos.y, targetPos.z, 20.0F, 20.0F);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 34.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.36D)
                .add(Attributes.FLYING_SPEED, 0.52D)
                .add(Attributes.ATTACK_DAMAGE, 7.0D)
                .add(Attributes.ARMOR, 4.0D)
                .add(Attributes.FOLLOW_RANGE, 32.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.1D);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        FlyingPathNavigation navigation = new FlyingPathNavigation(this, level);
        navigation.setCanOpenDoors(false);
        navigation.setCanFloat(true);
        navigation.setCanPassDoors(false);
        return navigation;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
    }

    private void tickKomorySupportCooldowns() {
        if (this.confusingWavesCooldown > 0) {
            this.confusingWavesCooldown--;
        }

        if (this.zeroGravityCooldown > 0) {
            this.zeroGravityCooldown--;
        }

        if (this.drainCooldown > 0) {
            this.drainCooldown--;
        }

        if (this.hasteCooldown > 0) {
            this.hasteCooldown--;
        }

        if (this.supportCastCooldown > 0) {
            this.supportCastCooldown--;
        }
    }

    @Override
    public void tick() {
        super.tick();

        this.setNoGravity(true);

        if (this.level().isClientSide) {
            return;
        }

        UUID ownerId = this.getOwnerUUID();

        if (ownerId == null) {
            this.discard();
            return;
        }

        ServerPlayer owner = getOwnerPlayerFromServer(ownerId);

        if (this.komoryBatFeedCooldown > 0) {
            this.komoryBatFeedCooldown--;
        }

        // The central follow manager handles large teleports/dimension changes.
        // Never clear persistent summon data here just because owner and summon
        // are temporarily in different levels.
        if (owner == null || owner.level() != this.level()) {
            this.discard();
            return;
        }

        GlobalDataRM data = ModDataRM.getGlobal(owner);

        if (data == null) {
            this.discard();
            return;
        }

        UUID registeredDreamEaterUUID = data.getDreamEaterUUID();

        if (!data.hasDreamEaterSummoned()
                || registeredDreamEaterUUID == null
                || !this.getUUID().equals(registeredDreamEaterUUID)) {
            this.discard();
            return;
        }

        if (owner.isDeadOrDying()) {
            clearDreamEaterData(owner, data);
            this.discard();
            return;
        }

        if (!this.isAlive() || this.isDeadOrDying()) {
            clearDreamEaterData(owner, data);
            this.discard();
            return;
        }

        if (!isSelectedDreamEaterKomoryBat(data)) {
            clearDreamEaterData(owner, data);
            this.discard();
            return;
        }

        if (this.tickCount % 40 == 0) {
            this.applyOwnerScaling(owner);
            this.updateVariantFromOwner(owner);
        }

        if (this.attackCooldown > 0) {
            this.attackCooldown--;
        }

        tickKomorySupportCooldowns();

        if (this.getAttackAnimTicks() > 0) {
            this.setAttackAnimTicks(this.getAttackAnimTicks() - 1);

            if (!this.didSonicBoomHit && this.getAttackAnimTicks() <= SONIC_BOOM_HIT_TICK) {
                this.didSonicBoomHit = true;
                this.doSonicBoomAttack(owner);
            }
        }

        followOwner(owner);
        updateCombatTarget(owner);
        tryStartSonicBoom(owner);
        castKomorySupportMoves(owner);

    }

    private void castKomorySupportMoves(Player owner) {
        if (owner == null || !owner.isAlive()) {
            return;
        }

        if (this.supportCastCooldown > 0) {
            return;
        }

        if (this.getAttackAnimTicks() > 0) {
            return;
        }

        PlayerData ownerData = PlayerData.get(owner);

        if (ownerData == null) {
            return;
        }

        int level = Math.max(1, ownerData.getLevel());

        // Haste - Lv 20
        if (level >= 20 && this.hasteCooldown <= 0 && !owner.hasEffect(ModMobEffectsRM.HASTE_RM)) {
            castHaste(owner);
            return;
        }

        LivingEntity target = this.target;

        if (target == null || !canKomoryBatAttack(owner, target)) {
            return;
        }

        // Drain - Lv 8
        // Uses it more when Komory or owner is hurt, but can still use it offensively.
        if (level >= 8 && this.drainCooldown <= 0) {
            if (this.getHealth() < this.getMaxHealth() || owner.getHealth() < owner.getMaxHealth() || this.random.nextFloat() < 0.35F) {
                castDrain(owner, target);
                return;
            }
        }

        // Zero Gravity / Zero Gravira / Zero Graviga - Lv 5 / 15 / 25
        if (level >= 5 && this.zeroGravityCooldown <= 0 && !target.hasEffect(ModMobEffects.ZERO_GRAVITY)) {
            castZeroGravity(owner, target, getZeroGravityTier(level));
            return;
        }

        // Confusing Waves - Lv 1
        if (this.confusingWavesCooldown <= 0 && !target.hasEffect(ModMobEffectsRM.CONFUSE)) {
            castConfusingWaves(owner, target);
        }
    }

    private void castConfusingWaves(Player owner, LivingEntity target) {
        double radius = 4.0D;
        int duration = 20 * 8;

        AABB box = target.getBoundingBox().inflate(radius, 2.5D, radius);

        boolean hitSomething = false;

        for (LivingEntity entity : this.level().getEntitiesOfClass(
                LivingEntity.class,
                box,
                entity -> canKomoryBatAttack(owner, entity)
        )) {
            entity.addEffect(new MobEffectInstance(
                    ModMobEffectsRM.CONFUSE,
                    duration,
                    0,
                    false,
                    false,
                    true
            ));

            hitSomething = true;
        }

        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                    ParticleTypes.WITCH,
                    target.getX(),
                    target.getY() + target.getBbHeight() * 0.65D,
                    target.getZ(),
                    35,
                    1.3D,
                    0.45D,
                    1.3D,
                    0.04D
            );

            serverLevel.sendParticles(
                    ParticleTypes.PORTAL,
                    target.getX(),
                    target.getY() + target.getBbHeight() * 0.65D,
                    target.getZ(),
                    20,
                    1.0D,
                    0.35D,
                    1.0D,
                    0.08D
            );
        }

        this.level().playSound(
                null,
                target.blockPosition(),
                SoundEvents.ILLUSIONER_CAST_SPELL,
                SoundSource.NEUTRAL,
                0.75F,
                1.35F
        );

        this.confusingWavesCooldown = hitSomething ? 20 * 16 : 20 * 5;
        this.supportCastCooldown = 20 * 3;
    }

    private void castZeroGravity(Player owner, LivingEntity target, int tier) {
        double radius = 3.5D + tier;
        int duration = 20 * (4 + (tier * 2));
        int amplifier = Math.max(0, tier - 1);

        AABB box = target.getBoundingBox().inflate(radius, 2.5D, radius);

        boolean hitSomething = false;

        for (LivingEntity entity : this.level().getEntitiesOfClass(
                LivingEntity.class,
                box,
                entity -> canKomoryBatAttack(owner, entity)
        )) {
            entity.addEffect(new MobEffectInstance(
                    ModMobEffects.ZERO_GRAVITY,
                    duration,
                    amplifier,
                    false,
                    false,
                    true
            ));


            entity.setDeltaMovement(entity.getDeltaMovement().add(0.0D, 0.25D + (tier * 0.07D), 0.0D));
            entity.hasImpulse = true;

            hitSomething = true;
        }

        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                    ParticleTypes.END_ROD,
                    target.getX(),
                    target.getY() + target.getBbHeight() * 0.5D,
                    target.getZ(),
                    45,
                    radius * 0.35D,
                    0.7D,
                    radius * 0.35D,
                    0.04D
            );

            serverLevel.sendParticles(
                    ParticleTypes.REVERSE_PORTAL,
                    target.getX(),
                    target.getY() + target.getBbHeight() * 0.5D,
                    target.getZ(),
                    30,
                    radius * 0.25D,
                    0.45D,
                    radius * 0.25D,
                    0.05D
            );
        }

        this.level().playSound(
                null,
                target.blockPosition(),
                ModSounds.zeroGravity.get(),
                SoundSource.NEUTRAL,
                0.65F,
                1.0F
        );


        this.zeroGravityCooldown = hitSomething ? 20 * 22 : 20 * 10;
        this.supportCastCooldown = 20 * 6;
    }

    private int getZeroGravityTier(int level) {
        if (level >= 25) {
            return 3;
        }

        if (level >= 15) {
            return 2;
        }

        return 1;
    }

    private String getZeroGravityName(int tier) {
        if (tier >= 3) {
            return "Zero Graviga";
        }

        if (tier >= 2) {
            return "Zero Gravira";
        }

        return "Zero Gravity";
    }

    private void castDrain(Player owner, LivingEntity target) {
        if (owner == null || target == null) {
            return;
        }

        if (!canKomoryBatAttack(owner, target)) {
            return;
        }

        if (this.level().isClientSide) {
            return;
        }

        float dmgMult = 1.0F;
        this.level().playSound(
                null,
                owner.blockPosition(),
                ModSoundsRM.DRAIN.get(),
                SoundSource.PLAYERS,
                0.65F,
                1F
        );
        DrainEntity drain = new DrainEntity(
                this.level(),
                this,       // origin: Komory Bat
                owner,      // caster/owner: player
                dmgMult,
                target      // lock-on target
        );

        Vec3 start = this.position().add(0.0D, this.getBbHeight() * 0.55D, 0.0D);
        Vec3 targetPos = target.position().add(0.0D, target.getBbHeight() * 0.55D, 0.0D);
        Vec3 direction = targetPos.subtract(start);

        if (direction.lengthSqr() > 0.001D) {
            direction = direction.normalize();

            drain.setPos(start.x, start.y, start.z);
            drain.shoot(direction.x, direction.y, direction.z, 0.75F, 0.0F);
        }

        this.level().addFreshEntity(drain);


        this.drainCooldown = 20 * 14;
        this.supportCastCooldown = 20 * 2;
    }

    private float getKomoryDrainDamage(Player owner) {
        PlayerData ownerData = PlayerData.get(owner);

        if (ownerData == null) {
            return Math.max(2.0F, (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.75F);
        }

        int level = Mth.clamp(ownerData.getLevel(), 1, 100);

        // Lv 1 Komory Bat MAG from your chart was 10.8.
        // Lv 3 MAG was 16, so early growth is +2.6 per level.
        // This keeps Drain magic-based but controlled.
        float komoryMagic;

        if (level <= 3) {
            komoryMagic = 10.8F + ((level - 1) * 2.6F);
        } else {
            komoryMagic = 16.0F + ((level - 3) * 0.55F);
        }

        return Math.max(2.0F, komoryMagic * 0.45F);
    }

    private void castHaste(Player owner) {
        if (owner == null || !owner.isAlive()) {
            return;
        }

        int duration = 20 * 30; // 30 seconds

        owner.addEffect(new MobEffectInstance(
                ModMobEffectsRM.HASTE_RM,
                duration,
                0,
                false,
                false,
                true
        ));

        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                    ParticleTypes.ELECTRIC_SPARK,
                    owner.getX(),
                    owner.getY() + owner.getBbHeight() * 0.7D,
                    owner.getZ(),
                    35,
                    0.45D,
                    0.65D,
                    0.45D,
                    0.08D
            );

            serverLevel.sendParticles(
                    ParticleTypes.HAPPY_VILLAGER,
                    owner.getX(),
                    owner.getY() + owner.getBbHeight() * 0.85D,
                    owner.getZ(),
                    10,
                    0.35D,
                    0.35D,
                    0.35D,
                    0.02D
            );
        }

        this.level().playSound(
                null,
                owner.blockPosition(),
                ModSoundsRM.HASTE.get(),
                SoundSource.PLAYERS,
                0.65F,
                1.55F
        );

        this.hasteCooldown = 20 * 55;
        this.supportCastCooldown = 20 * 3;
    }



    @Nullable
    private ServerPlayer getOwnerPlayerFromServer(UUID ownerId) {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return null;
        }

        MinecraftServer server = serverLevel.getServer();

        if (server == null) {
            return null;
        }

        return server.getPlayerList().getPlayer(ownerId);
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack heldStack = player.getItemInHand(hand);

        int feedExp = getKomoryBatFeedExp(heldStack);

        if (feedExp > 0) {
            return feedKomoryBat(player, heldStack, feedExp);
        }

        InteractionResult petResult = DreamEaterPetHelper.tryPetDreamEater(
                this,
                player,
                hand,
                this.getOwnerUUID(),
                "Komory Bat"
        );

        if (petResult != InteractionResult.PASS) {
            return petResult;
        }

        return super.mobInteract(player, hand);
    }

    private InteractionResult feedKomoryBat(Player player, ItemStack heldStack, int feedExp) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.SUCCESS;
        }

        if (!isKomoryBatOwner(serverPlayer)) {
            serverPlayer.displayClientMessage(
                    Component.literal("This Komory Bat does not belong to you.")
                            .withStyle(ChatFormatting.RED),
                    true
            );

            return InteractionResult.CONSUME;
        }

        if (this.komoryBatFeedCooldown > 0) {
            return InteractionResult.CONSUME;
        }

        if (!serverPlayer.getAbilities().instabuild) {
            heldStack.shrink(1);
        }

        this.komoryBatFeedCooldown = KOMORY_BAT_FEED_COOLDOWN_TICKS;

        DreamEaterExpHandler.giveDreamEaterExp(
                serverPlayer,
                GlobalDataRM.DREAM_EATER_KOMORY_BAT,
                feedExp,
                this
        );

        return InteractionResult.CONSUME;
    }

    private int getKomoryBatFeedExp(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return 0;
        }

        if (stack.is(Items.SPIDER_EYE)) {
            return SPIDER_EYE_FEED_EXP;
        }

        if (stack.is(Items.PHANTOM_MEMBRANE)) {
            return PHANTOM_MEMBRANE_FEED_EXP;
        }

        return 0;
    }

    private boolean isKomoryBatOwner(Player player) {
        if (player == null) {
            return false;
        }

        if (this.getOwnerUUID() == null) {
            return false;
        }

        return this.getOwnerUUID().equals(player.getUUID());
    }

    private void followOwner(Player owner) {
        double distSqr = this.distanceToSqr(owner);

        if (this.getAttackAnimTicks() > 0) {
            return;
        }

        Vec3 hoverPos = owner.position().add(0.0D, 2.35D, 0.0D);

        if (distSqr > 4.0D) {
            flyToward(hoverPos, 0.22D);
        } else {
            Vec3 bob = new Vec3(
                    Math.sin(this.tickCount * 0.08D) * 0.015D,
                    Math.sin(this.tickCount * 0.12D) * 0.025D,
                    Math.cos(this.tickCount * 0.08D) * 0.015D
            );

            this.setDeltaMovement(this.getDeltaMovement().scale(0.75D).add(bob));
        }
    }

    private void updateCombatTarget(Player owner) {
        if (this.target != null) {
            if (canKomoryBatAttack(owner, this.target) && this.distanceToSqr(this.target) < 256.0D) {
                return;
            }

            this.target = null;
        }

        LivingEntity ownerAttacker = owner.getLastHurtByMob();

        if (canKomoryBatAttack(owner, ownerAttacker) && owner.distanceToSqr(ownerAttacker) < 256.0D) {
            this.target = ownerAttacker;
            return;
        }

        LivingEntity ownerTarget = owner.getLastHurtMob();

        if (canKomoryBatAttack(owner, ownerTarget) && owner.distanceToSqr(ownerTarget) < 256.0D) {
            this.target = ownerTarget;
            return;
        }

        AABB box = owner.getBoundingBox().inflate(12.0D, 6.0D, 12.0D);

        this.target = this.level().getEntitiesOfClass(
                        LivingEntity.class,
                        box,
                        entity -> canKomoryBatAttack(owner, entity)
                )
                .stream()
                .min(Comparator.comparingDouble(entity -> entity.distanceToSqr(owner)))
                .orElse(null);
    }

    private void tryStartSonicBoom(Player owner) {
        if (this.attackCooldown > 0 || this.getAttackAnimTicks() > 0) {
            return;
        }

        if (this.target == null || !canKomoryBatAttack(owner, this.target)) {
            this.target = null;
            return;
        }

        Vec3 targetPos = this.target.position().add(0.0D, this.target.getBbHeight() * 0.55D, 0.0D);
        double distSqr = this.distanceToSqr(this.target);

        // Ranged attack: only move closer if outside sonic boom range.
        if (distSqr > SONIC_BOOM_RANGE * SONIC_BOOM_RANGE) {
            flyToward(targetPos, 0.28D);
            return;
        }

        // Stop and face the target before blasting.

        this.getNavigation().stop();
        faceTargetForAttack(this.target);

        Vec3 awayFromTarget = this.position().subtract(this.target.position());

        if (awayFromTarget.lengthSqr() > 0.001D) {
            // Small recoil/rear-back motion, not a melee lunge.
            this.setDeltaMovement(awayFromTarget.normalize().scale(0.10D).add(0.0D, 0.05D, 0.0D));
        } else {
            this.setDeltaMovement(0.0D, 0.05D, 0.0D);
        }

        this.setAttackAnimTicks(SONIC_BOOM_WINDUP_TICKS);
        this.didSonicBoomHit = false;
        this.attackCooldown = 20 * 5;
        this.supportCastCooldown = 20 * 3;
    }

    @Override
    public void travel(Vec3 travelVector) {
        if (this.isEffectiveAi() || this.isControlledByLocalInstance()) {
            this.move(MoverType.SELF, this.getDeltaMovement());
            this.setDeltaMovement(this.getDeltaMovement().scale(0.91D));
        } else {
            super.travel(travelVector);
        }
    }

    private void faceTargetForAttack(LivingEntity target) {
        if (target == null) {
            return;
        }

        Vec3 eyePos = this.position().add(0.0D, this.getBbHeight() * 0.55D, 0.0D);
        Vec3 targetPos = target.position().add(0.0D, target.getBbHeight() * 0.55D, 0.0D);

        double dx = targetPos.x - eyePos.x;
        double dy = targetPos.y - eyePos.y;
        double dz = targetPos.z - eyePos.z;

        double horizontalDistance = Math.sqrt(dx * dx + dz * dz);

        float yaw = (float) (Mth.atan2(dz, dx) * (180F / Math.PI)) - 90F;
        float pitch = (float) -(Mth.atan2(dy, horizontalDistance) * (180F / Math.PI));

        // Whole body turn
        this.setYRot(yaw);
        this.yRotO = yaw;
        this.yBodyRot = yaw;
        this.yBodyRotO = yaw;

        // Head follows body
        this.yHeadRot = yaw;
        this.yHeadRotO = yaw;

        // Slight vertical aim for flying/ranged attack
        this.setXRot(Mth.clamp(pitch, -35F, 35F));
        this.xRotO = this.getXRot();

        this.getLookControl().setLookAt(target, 30.0F, 30.0F);
    }

    private void doSonicBoomAttack(Player owner) {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        if (this.target == null || !this.target.isAlive()) {
            return;
        }

        Vec3 origin = this.position().add(0.0D, this.getBbHeight() * 0.55D, 0.0D);
        Vec3 targetPoint = this.target.position().add(0.0D, this.target.getBbHeight() * 0.55D, 0.0D);

        Vec3 direction = targetPoint.subtract(origin);

        if (direction.lengthSqr() < 0.001D) {
            return;
        }

        direction = direction.normalize();

        float damage = getKomoryPhysicalDamage(0.65F);

        Vec3 end = origin.add(direction.scale(SONIC_BOOM_RANGE));

        AABB searchBox = new AABB(origin, end).inflate(SONIC_BOOM_RADIUS + 0.75D);

        for (LivingEntity entity : this.level().getEntitiesOfClass(
                LivingEntity.class,
                searchBox,
                entity -> canKomoryBatAttack(owner, entity)
        )) {
            Vec3 entityCenter = entity.position().add(0.0D, entity.getBbHeight() * 0.5D, 0.0D);
            Vec3 fromOrigin = entityCenter.subtract(origin);

            double distanceAlongBeam = fromOrigin.dot(direction);

            if (distanceAlongBeam < 0.0D || distanceAlongBeam > SONIC_BOOM_RANGE) {
                continue;
            }

            Vec3 closestPoint = origin.add(direction.scale(distanceAlongBeam));
            double distanceFromBeam = entityCenter.distanceTo(closestPoint);

            double allowedRadius = SONIC_BOOM_RADIUS + (entity.getBbWidth() * 0.5D);

            if (distanceFromBeam > allowedRadius) {
                continue;
            }

            entity.hurt(this.damageSources().mobAttack(this), damage);

            Vec3 knockback = direction.scale(0.75D).add(0.0D, 0.12D, 0.0D);
            entity.push(knockback.x, knockback.y, knockback.z);
        }

        this.level().playSound(
                null,
                this.getX(),
                this.getY(),
                this.getZ(),
                SoundEvents.WARDEN_SONIC_BOOM,
                SoundSource.NEUTRAL,
                0.8F,
                1.45F
        );

        spawnSonicBoomLine(serverLevel, origin, direction);
    }

    private void spawnSonicBoomLine(ServerLevel serverLevel, Vec3 origin, Vec3 direction) {
        for (double distance = 1.0D; distance <= SONIC_BOOM_RANGE; distance += 0.9D) {
            Vec3 pos = origin.add(direction.scale(distance));

            serverLevel.sendParticles(
                    ParticleTypes.CLOUD,
                    pos.x,
                    pos.y,
                    pos.z,
                    6,
                    0.25D,
                    0.12D,
                    0.25D,
                    0.035D
            );

            if (distance == 1.0D || distance > SONIC_BOOM_RANGE - 1.2D) {
                serverLevel.sendParticles(
                        ParticleTypes.SONIC_BOOM,
                        pos.x,
                        pos.y,
                        pos.z,
                        1,
                        0.0D,
                        0.0D,
                        0.0D,
                        0.0D
                );
            }
        }
    }

    private void applyOwnerScaling(Player owner) {
        PlayerData ownerData = PlayerData.get(owner);
        int level = 1;

        if (owner instanceof Player player) {
            GlobalDataRM globalData = ModDataRM.getGlobal(player);

            if (globalData != null) {
                level = globalData.getDreamEaterLevel(GlobalDataRM.DREAM_EATER_KOMORY_BAT);
            }
        }

        KomoryBatStats stats = getKomoryBatStatsForLevel(level);

        this.komoryHP = stats.hp;
        this.komoryStrength = stats.strength;
        this.komoryMagic = stats.magic;
        this.komoryDefense = stats.defense;

        if (this.getAttribute(Attributes.MAX_HEALTH) != null) {
            this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(this.komoryHP);
        }

        if (this.getAttribute(Attributes.ATTACK_DAMAGE) != null) {
            this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(this.komoryStrength);
        }

        if (this.getAttribute(Attributes.ARMOR) != null) {
            this.getAttribute(Attributes.ARMOR).setBaseValue(this.komoryDefense);
        }

        if (this.getAttribute(Attributes.FLYING_SPEED) != null) {
            this.getAttribute(Attributes.FLYING_SPEED).setBaseValue(0.52D);
        }

        if (this.getHealth() > this.getMaxHealth()) {
            this.setHealth(this.getMaxHealth());
        }
    }

    private static KomoryBatStats getKomoryBatStatsForLevel(int level) {
        level = Mth.clamp(level, 1, 100);

        /*
         * Lv 1:
         * HP 32.7 / STR 8.2 / MAG 10.8 / DEF 5.9
         *
         * Lv 3:
         * HP 34 / STR 12 / MAG 16 / DEF 6
         */
        double hp;
        double strength;
        double magic;
        double defense;

        if (level <= 3) {
            hp = 32.7D + ((level - 1) * 0.65D);
            strength = 8.2D + ((level - 1) * 1.9D);
            magic = 10.8D + ((level - 1) * 2.6D);
            defense = 5.9D + ((level - 1) * 0.05D);
        } else {
            /*
             * Controlled post-Lv3 growth.
             * Keeps Lv3 exact, then scales to Lv100 without exploding.
             */
            int extraLevels = level - 3;

            hp = 34.0D + (extraLevels * 2.15D);
            strength = 12.0D + (extraLevels * 0.48D);
            magic = 16.0D + (extraLevels * 0.62D);
            defense = 6.0D + (extraLevels * 0.22D);
        }

        return new KomoryBatStats(hp, strength, magic, defense);
    }

    private static class KomoryBatStats {
        private final double hp;
        private final double strength;
        private final double magic;
        private final double defense;

        private KomoryBatStats(double hp, double strength, double magic, double defense) {
            this.hp = hp;
            this.strength = strength;
            this.magic = magic;
            this.defense = defense;
        }
    }

    private float getKomoryPhysicalDamage(float multiplier) {
        return Math.max(1.0F, (float) (this.komoryStrength * multiplier));
    }

    private float getKomoryMagicDamage(float multiplier) {
        return Math.max(1.0F, (float) (this.komoryMagic * multiplier));
    }

    private void updateVariantFromOwner(Player owner) {
        PlayerData ownerData = PlayerData.get(owner);

        if (ownerData == null) {
            return;
        }

        this.setVariant(ownerData.getAlignment() != Utils.OrgMember.NONE ? VARIANT_ORG : VARIANT_NORMAL);
    }

    private boolean isSelectedDreamEaterKomoryBat(GlobalDataRM data) {
        String dreamEaterRL = data.getDreamEaterRL();

        if (dreamEaterRL == null || dreamEaterRL.isEmpty()) {
            return false;
        }

        DreamEater dreamEater = ModDreamEaters.registry.get(ResourceLocation.parse(dreamEaterRL));

        if (dreamEater == null) {
            return false;
        }

        return StringsRM.komoryBat.equals(dreamEater.getName());
    }

    private void clearDreamEaterData(Player owner, GlobalDataRM data) {
        data.setHasDreamEaterSummoned(false);
        data.setDreamEaterUUID(null);
        PacketHandlerRM.syncGlobalToAllAround(owner, data);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (isDamageFromOwner(source)) {
            return false;
        }

        return super.hurt(source, amount);
    }

    private boolean isDamageFromOwner(DamageSource source) {
        UUID ownerId = this.getOwnerUUID();

        if (ownerId == null || source == null) {
            return false;
        }

        Entity attacker = source.getEntity();
        Entity directEntity = source.getDirectEntity();

        // Direct player melee / magic source
        if (attacker != null && ownerId.equals(attacker.getUUID())) {
            return true;
        }

        // Direct entity is the owner
        if (directEntity != null && ownerId.equals(directEntity.getUUID())) {
            return true;
        }

        // Projectile shot by owner
        if (directEntity instanceof Projectile projectile) {
            Entity projectileOwner = projectile.getOwner();

            return projectileOwner != null && ownerId.equals(projectileOwner.getUUID());
        }

        return false;
    }

    private boolean canKomoryBatAttack(Player owner, LivingEntity entity) {
        if (owner == null || entity == null) {
            return false;
        }

        if (!entity.isAlive()) {
            return false;
        }

        // Never attack itself or the owner
        if (entity == this || entity == owner) {
            return false;
        }

        // Never attack players
        if (entity instanceof Player) {
            return false;
        }

        // Never attack Dream Eaters
        if (entity instanceof BaseDreamEaterEntity) {
            return false;
        }

        if (entity instanceof MeowWowEntity) {
            return false;
        }

        if (entity instanceof KomoryBatEntity) {
            return false;
        }

        // Never attack owner's tamed mobs/pets
        if (entity instanceof TamableAnimal tameable) {
            UUID tameOwner = tameable.getOwnerUUID();

            if (tameOwner != null && tameOwner.equals(owner.getUUID())) {
                return false;
            }
        }

        // Respect vanilla ally/team logic
        if (entity.isAlliedTo(owner) || owner.isAlliedTo(entity)) {
            return false;
        }

        // Always defend the owner
        if (owner.getLastHurtByMob() == entity) {
            return true;
        }

        // Help attack what the owner attacked
        if (owner.getLastHurtMob() == entity) {
            return true;
        }

        // If a mob is actively targeting the owner, attack it
        if (entity instanceof Mob mob && mob.getTarget() == owner) {
            return true;
        }

        // Otherwise only attack hostile mobs
        return entity instanceof Enemy;
    }

    public static void removeExistingKomoryBat(ServerLevel level, UUID ownerUUID) {
        MinecraftServer server = level.getServer();

        for (ServerLevel serverLevel : server.getAllLevels()) {
            for (Entity entity : serverLevel.getAllEntities()) {
                if (entity instanceof KomoryBatEntity komoryBat) {
                    if (ownerUUID.equals(komoryBat.getOwnerUUID())) {
                        komoryBat.discard();
                    }
                }
            }
        }
    }

    @Override
    public void die(DamageSource source) {
        UUID ownerId = this.getOwnerUUID();

        if (ownerId != null) {
            Player owner = this.level().getPlayerByUUID(ownerId);

            if (owner != null) {
                GlobalDataRM data = ModDataRM.getGlobal(owner);

                if (data != null) {
                    clearDreamEaterData(owner, data);
                }
            }
        }

        super.die(source);
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    protected void updateWalkAnimation(float partialTick) {
        float f = this.getPose() == Pose.STANDING ? Math.min(partialTick * 6.0F, 1.0F) : 0.0F;
        this.walkAnimation.update(f, 0.2F);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(VARIANT, VARIANT_NORMAL);
        builder.define(OWNER_UUID, Optional.empty());
        builder.define(ATTACK_ANIM_TICKS, 0);
    }

    public int getVariant() {
        return this.entityData.get(VARIANT);
    }

    public void setVariant(int variant) {
        this.entityData.set(VARIANT, variant);
    }

    @Nullable
    public UUID getOwnerUUID() {
        return this.entityData.get(OWNER_UUID).orElse(null);
    }

    public void setOwnerUUID(@Nullable UUID uuid) {
        this.entityData.set(OWNER_UUID, Optional.ofNullable(uuid));
    }

    public int getAttackAnimTicks() {
        return this.entityData.get(ATTACK_ANIM_TICKS);
    }

    public void setAttackAnimTicks(int ticks) {
        this.entityData.set(ATTACK_ANIM_TICKS, ticks);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);

        UUID ownerId = this.getOwnerUUID();

        if (ownerId != null) {
            tag.putUUID("DreamEaterOwner", ownerId);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);

        if (tag.hasUUID("DreamEaterOwner")) {
            this.setOwnerUUID(tag.getUUID("DreamEaterOwner"));
        }

        this.setNoGravity(true);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main_controller", 1, state -> {
            if (this.getAttackAnimTicks() > 0) {
                return state.setAndContinue(ATTACK_ANIM);
            }

            double dx = this.getX() - this.xOld;
            double dy = this.getY() - this.yOld;
            double dz = this.getZ() - this.zOld;
            double movedSqr = dx * dx + dy * dy + dz * dz;

            if (movedSqr > 0.00001D || this.getDeltaMovement().lengthSqr() > 0.00001D) {
                return state.setAndContinue(WALK_ANIM);
            }

            return state.setAndContinue(IDLE_ANIM);
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}