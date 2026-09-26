package online.remind.remind.entity.enemies;

import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import online.kingdomkeys.kingdomkeys.util.Utils;
import online.remind.remind.KingdomKeysReMind;
import online.remind.remind.capabilities.GlobalDataRM;
import online.remind.remind.capabilities.ModDataRM;
import online.remind.remind.client.sound.ModSoundsRM;
import online.remind.remind.entity.projectile.CactuarNeedleProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import online.remind.remind.entity.ModEntitiesRM;
import online.remind.remind.item.ModItemsRM;
import online.remind.remind.network.PacketHandlerRM;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class CactuarEntity extends Monster implements GeoEntity {

    private static final ResourceKey<Biome> DESERT_BIOME =
            ResourceKey.create(
                    Registries.BIOME,
                    ResourceLocation.fromNamespaceAndPath("minecraft", "desert")
            );

    private static final double JUMBO_SPAWN_CHANCE = 0.05D;
    private static final double JUMBO_NEARBY_CHECK_RANGE = 128.0D;

    private static final String JUMBO_BOSS_MUSIC_SOUND = KingdomKeysReMind.MODID + ":records/dont_be_afraid_ff8";
    private static final int JUMBO_BOSS_MUSIC_REFRESH_TICKS = 20 * 110;
    private static final int JUMBO_SUPPRESS_MC_MUSIC_TICKS = 20 * 5;
    private static final double JUMBO_BOSS_MUSIC_RANGE = 96.0D;

    private final Set<UUID> jumboBossMusicPlayers = new HashSet<>();
    private int jumboBossMusicRefreshTicks = JUMBO_BOSS_MUSIC_REFRESH_TICKS;
    private int jumboSuppressMcMusicTicks = JUMBO_SUPPRESS_MC_MUSIC_TICKS;

    public static final int VARIANT_NORMAL = 0;
    public static final int VARIANT_JUMBO = 1;

    public static final int ACTION_NONE = 0;
    public static final int ACTION_KICK = 1;
    public static final int ACTION_NEEDLES = 2;
    public static final int ACTION_JUMBO_STOMP = 3;
    public static final int ACTION_KER_PLUNK = 4;
    public static final int ACTION_DEATH = 5;

    private static final int NORMAL_NEEDLES_TICKS = 48;
    private static final int JUMBO_NEEDLES_TICKS = 64;

    private int needleVolleyTicks = 0;
    private int needleVolleyInterval = 0;
    private int needleTargetId = -1;

    private int needleHitInterval = 0;
    private int needleHitsRemaining = 0;
    private float needleDamagePerHit = 0.0F;
    private boolean needleVolleyJumbo = false;
    private boolean needleDamageApplied = false;
    private static final int KER_PLUNK_TICKS = 90;
    private static final int DEATH_ANIMATION_TICKS = 70;

    private static final EntityDataAccessor<Integer> VARIANT =
            SynchedEntityData.defineId(CactuarEntity.class, EntityDataSerializers.INT);

    private static final EntityDataAccessor<Integer> ACTION =
            SynchedEntityData.defineId(CactuarEntity.class, EntityDataSerializers.INT);

    private static final EntityDataAccessor<Integer> ACTION_TICKS =
            SynchedEntityData.defineId(CactuarEntity.class, EntityDataSerializers.INT);

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private static final RawAnimation IDLE_ANIM =
            RawAnimation.begin().thenLoop("idle");

    private static final RawAnimation KICK_ANIM =
            RawAnimation.begin().thenPlay("attack_normal");

    private static final RawAnimation NEEDLES_ANIM =
            RawAnimation.begin().thenPlay("needles");

    private static final RawAnimation JUMBO_STOMP_ANIM =
            RawAnimation.begin().thenPlay("jumbo_attack");

    private static final RawAnimation KER_PLUNK_ANIM =
            RawAnimation.begin().thenPlay("ker_plunk");

    private static final RawAnimation DEATH_ANIM =
            RawAnimation.begin().thenPlayAndHold("death");

    private String currentAnimation = "none";

    // Ticks * Number of Seconds

    private int needlesCooldown = 20 * 10;
    private int escapeCooldown = 100;

    private int jumboStompCooldown = 60;
    private int jumboNeedlesCooldown = 20 * 20;
    private int kerPlunkCooldown = 20 * 10;
    private int kerPlunkImpactTicks = 0;

    private boolean escaping = false;
    private int escapeTicks = 0;

    public CactuarEntity(EntityType<? extends CactuarEntity> type, Level level) {
        super(type, level);

        if (isJumboType(type)) {
            this.setVariant(VARIANT_JUMBO);
        } else {
            this.setVariant(VARIANT_NORMAL);
        }
    }

    private boolean isJumboType(EntityType<?> type) {
        try {
            return type == ModEntitiesRM.TYPE_JUMBO_CACTUAR.get();
        } catch (Exception ignored) {
            return false;
        }
    }

    private final ServerBossEvent bossEvent =
            new ServerBossEvent(
                    Component.literal("Jumbo Cactuar"),
                    BossEvent.BossBarColor.GREEN,
                    BossEvent.BossBarOverlay.PROGRESS
            );

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);

        if (this.isJumbo()) {
            this.bossEvent.addPlayer(player);
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSoundsRM.CACTUAR_ALIVE.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSoundsRM.CACTUAR_DEATH.get();
    }

    @Override
    public float getVoicePitch() {
        if (this.isJumbo()) {
            return 0.45F + (this.random.nextFloat() * 0.08F);
        }

        return 0.95F + (this.random.nextFloat() * 0.10F);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);

        if (this.isJumbo()) {
            this.bossEvent.removePlayer(player);
            stopJumboBossMusic(player);
        }
    }

    @Override
    public void remove(RemovalReason reason) {
        if (!this.level().isClientSide && this.isJumbo()) {
            stopJumboBossMusicForAll();

            if (this.bossEvent != null) {
                this.bossEvent.removeAllPlayers();
            }
        }

        super.remove(reason);
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        if (this.isJumbo()) {
            return false;
        }

        return super.removeWhenFarAway(distanceToClosestPlayer);
    }

    private void updateBossBar() {
        if (!this.isJumbo()) {
            this.bossEvent.removeAllPlayers();
            return;
        }

        this.bossEvent.setName(this.hasCustomName() ? this.getDisplayName() : Component.literal("Jumbo Cactuar"));

        float progress = this.getMaxHealth() <= 0.0F ? 0.0F : this.getHealth() / this.getMaxHealth();
        this.bossEvent.setProgress(Mth.clamp(progress, 0.0F, 1.0F));
    }

    private boolean isKKWaterDamage(DamageSource source) {
        if (source == null || source.typeHolder().unwrapKey().isEmpty()) {
            return false;
        }

        String namespace = source.typeHolder().unwrapKey().get().location().getNamespace();
        String path = source.typeHolder().unwrapKey().get().location().getPath();

        return ("kingdomkeys".equals(namespace) || "kkremind".equals(namespace))
                && path.contains("water");
    }

    public boolean isJumbo() {
        return this.getVariant() == VARIANT_JUMBO;
    }

    public int getVariant() {
        return this.entityData.get(VARIANT);
    }

    public void setVariant(int variant) {
        this.entityData.set(VARIANT, variant);
    }

    public int getCactuarAction() {
        return this.entityData.get(ACTION);
    }

    public int getCactuarActionTicks() {
        return this.entityData.get(ACTION_TICKS);
    }

    public boolean isCactuarDeathAnimating() {
        return this.deathTime > 0 || this.getHealth() <= 0.0F || this.getCactuarAction() == ACTION_DEATH;
    }

    public float getDeathBackFallProgress(float partialTick) {
        if (!this.isCactuarDeathAnimating()) {
            return 0.0F;
        }

        int action = this.getCactuarAction();
        int actionTicks = this.getCactuarActionTicks();

        if (action == ACTION_DEATH && actionTicks > 0) {
            float used = DEATH_ANIMATION_TICKS - actionTicks;
            return Math.max(0.0F, Math.min(1.0F, used / 28.0F));
        }

        if (this.deathTime > 0) {
            return Math.max(0.0F, Math.min(1.0F, (this.deathTime + partialTick) / 28.0F));
        }

        return 0.0F;
    }

    private boolean isKerPlunking() {
        return this.getCactuarAction() == ACTION_KER_PLUNK && this.getCactuarActionTicks() > 0;
    }

    private boolean isNeedlesAnimating() {
        return this.getCactuarAction() == ACTION_NEEDLES && this.getCactuarActionTicks() > 0;
    }

    private boolean isActionBusy() {
        int action = this.getCactuarAction();

        return action != ACTION_NONE
                && action != ACTION_DEATH
                && this.getCactuarActionTicks() > 0;
    }

    private void setAction(int action, int ticks) {
        if (this.level().isClientSide) {
            return;
        }

        this.entityData.set(ACTION, action);
        this.entityData.set(ACTION_TICKS, Math.max(0, ticks));
        this.currentAnimation = "none";
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(VARIANT, VARIANT_NORMAL);
        builder.define(ACTION, ACTION_NONE);
        builder.define(ACTION_TICKS, 0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));

        if (!this.isJumbo()) {
            this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.35D, true));
            this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.9D));
        } else {
            this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 0.85D, true));
            this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.45D));
        }

        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    public static AttributeSupplier.Builder createNormalAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 75.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.30D)
                .add(Attributes.ATTACK_DAMAGE, 2.0D)
                .add(Attributes.ATTACK_KNOCKBACK, 0.25D)
                .add(Attributes.FOLLOW_RANGE, 18.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.10D);
    }

    public static AttributeSupplier.Builder createJumboAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 3000.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.18D)
                .add(Attributes.ATTACK_DAMAGE, 21.0D)
                .add(Attributes.ATTACK_KNOCKBACK, 2.0D)
                .add(Attributes.FOLLOW_RANGE, 64.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.95D);
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide && this.isJumbo()) {
            updateBossBar();
        }

        if (!this.level().isClientSide && this.isJumbo()) {
            tickJumboBossMusic();
        }

        if (this.level().isClientSide) {
            return;
        }

        if (this.isDeadOrDying()) {
            return;
        }

        tickActionAnimation();

        if (this.isKerPlunking()) {
            lockKerPlunkMovement();
        }

        tickCooldowns();
        tickNeedleVolley();

        if (this.isJumbo()) {
            tickJumboCombat();
        } else {
            tickNormalCombat();
        }
    }

    private void lockKerPlunkMovement() {
        this.getNavigation().stop();
        this.setDeltaMovement(Vec3.ZERO);
        this.hasImpulse = true;
        this.hurtMarked = true;
    }

    private void tickActionAnimation() {
        int action = this.entityData.get(ACTION);

        if (action == ACTION_DEATH) {
            return;
        }

        int ticks = this.entityData.get(ACTION_TICKS);

        if (ticks <= 0) {
            if (action != ACTION_NONE) {
                this.entityData.set(ACTION, ACTION_NONE);
                this.currentAnimation = "none";
            }

            return;
        }

        ticks--;
        this.entityData.set(ACTION_TICKS, ticks);

        if (ticks <= 0) {
            this.entityData.set(ACTION, ACTION_NONE);
            this.currentAnimation = "none";
        }
    }

    private void tickCooldowns() {
        if (this.needlesCooldown > 0) {
            this.needlesCooldown--;
        }

        if (this.escapeCooldown > 0) {
            this.escapeCooldown--;
        }

        if (this.jumboStompCooldown > 0) {
            this.jumboStompCooldown--;
        }

        if (this.jumboNeedlesCooldown > 0) {
            this.jumboNeedlesCooldown--;
        }

        if (this.kerPlunkCooldown > 0) {
            this.kerPlunkCooldown--;
        }

        if (this.kerPlunkImpactTicks > 0) {
            this.kerPlunkImpactTicks--;

            if (this.kerPlunkImpactTicks == 35) {
                doKerPlunkImpact();
            }
        }
    }

    private void tickNeedleVolley() {
        if (this.needleVolleyTicks <= 0) {
            return;
        }

        if (this.getCactuarAction() != ACTION_NEEDLES || this.getCactuarActionTicks() <= 0) {
            clearNeedleVolley();
            return;
        }

        if (!(this.level() instanceof ServerLevel serverLevel)) {
            clearNeedleVolley();
            return;
        }

        Entity entity = serverLevel.getEntity(this.needleTargetId);

        if (!(entity instanceof LivingEntity target) || !target.isAlive()) {
            clearNeedleVolley();
            return;
        }

        /*
         * Visible needles.
         * Normal = smaller bursts.
         * Jumbo = much denser bursts.
         */
        if (this.needleVolleyInterval <= 0) {
            if (this.needleVolleyJumbo) {
                spawnNeedleArrows(target, 10, 2.25F, 4.5F);
                this.needleVolleyInterval = 1;
            } else {
                spawnNeedleArrows(target, 4, 1.85F, 3.0F);
                this.needleVolleyInterval = 2;
            }
        } else {
            this.needleVolleyInterval--;
        }

        /*
         * Rapid multi-hit damage.
         * This is the part that makes it FEEL like 1,000 / 10,000 hits.
         */
        if (this.needleHitsRemaining > 0) {
            if (this.needleHitInterval <= 0) {
                doNeedleMultiHit(serverLevel, target);

                this.needleHitsRemaining--;

                if (this.needleVolleyJumbo) {
                    this.needleHitInterval = 0; // every tick
                } else {
                    this.needleHitInterval = 1; // every other tick
                }
            } else {
                this.needleHitInterval--;
            }
        }

        this.needleVolleyTicks--;

        if (this.needleVolleyTicks <= 0) {
            clearNeedleVolley();
        }
    }

    private void clearNeedleVolley() {
        this.needleVolleyTicks = 0;
        this.needleVolleyInterval = 0;
        this.needleTargetId = -1;
        this.needleVolleyJumbo = false;
        this.needleHitInterval = 0;
        this.needleHitsRemaining = 0;
        this.needleDamagePerHit = 0.0F;
    }

    private void tickNormalCombat() {
        if (this.escaping) {
            tickEscape();
            return;
        }

        if (this.isActionBusy()) {
            return;
        }

        LivingEntity target = this.getTarget();

        if (target == null || !target.isAlive()) {
            return;
        }

        double distanceSqr = this.distanceToSqr(target);

        if (this.needlesCooldown <= 0 && distanceSqr <= 144.0D && this.hasLineOfSight(target)) {
            doThousandNeedles(target);
            return;
        }

        if (this.escapeCooldown <= 0) {
            boolean lowHealth = this.getHealth() <= this.getMaxHealth() * 0.01F;
            boolean randomEscape = this.random.nextFloat() < 0.01F;
            //System.out.println(randomEscape +", " + lowHealth); // DEBUG LINE

            if (lowHealth || randomEscape) {
                startEscape(target);
            }
        }
    }

    private void tickJumboCombat() {
        if (this.isActionBusy()) {
            if (this.isKerPlunking()) {
                lockKerPlunkMovement();
            }

            return;
        }

        LivingEntity target = this.getTarget();

        if (target == null || !target.isAlive()) {
            return;
        }

        double distanceSqr = this.distanceToSqr(target);

        if (this.kerPlunkCooldown <= 0 && distanceSqr <= 225.0D) {
            doKerPlunkStart(target);
            return;
        }

        if (this.jumboNeedlesCooldown <= 0 && distanceSqr <= 225.0D && this.hasLineOfSight(target)) {
            doTenThousandNeedles(target);
            return;
        }

        if (this.jumboStompCooldown <= 0 && distanceSqr <= 36.0D) {
            doJumboStomp();
        }
    }

    private void doThousandNeedles(LivingEntity target) {
        playActionAnimation(ACTION_NEEDLES, NORMAL_NEEDLES_TICKS);
        startNeedleVolley(target, false, NORMAL_NEEDLES_TICKS);
        this.setDeltaMovement(0,0,0);

        this.level().playSound(
                null,
                this.getX(),
                this.getY(),
                this.getZ(),
                SoundEvents.ARROW_SHOOT,
                SoundSource.HOSTILE,
                1.0F,
                1.6F
        );

        this.needlesCooldown = 120;
    }

    private void doTenThousandNeedles(LivingEntity target) {
        playActionAnimation(ACTION_NEEDLES, JUMBO_NEEDLES_TICKS);
        startNeedleVolley(target, true, JUMBO_NEEDLES_TICKS);
        this.setDeltaMovement(0,0,0);

        this.level().playSound(
                null,
                this.getX(),
                this.getY(),
                this.getZ(),
                SoundEvents.ARROW_SHOOT,
                SoundSource.HOSTILE,
                1.5F,
                0.7F
        );

        this.jumboNeedlesCooldown = 180;
    }

    private void startNeedleVolley(LivingEntity target, boolean jumbo, int ticks) {
        this.needleTargetId = target.getId();
        this.needleVolleyJumbo = jumbo;
        this.needleVolleyTicks = ticks;
        this.needleVolleyInterval = 0;
        this.needleHitInterval = 0;

        if (jumbo) {
            /*
             * 10,000 Needles feeling:
             * 70 tiny hits across the animation.
             * Total damage: 35.0F
             */
            this.needleHitsRemaining = 70;
            this.needleDamagePerHit = 0.75F;
        } else {
            /*
             * 1,000 Needles feeling:
             * 25 tiny hits across the animation.
             * Total damage: 10.0F
             */
            this.needleHitsRemaining = 25;
            this.needleDamagePerHit = 0.25F;
        }
    }

    private void doNeedleMultiHit(ServerLevel serverLevel, LivingEntity target) {
        /*
         * Minecraft normally blocks rapid repeated hurt calls with invulnerability frames.
         * Resetting this lets the tiny hits actually register.
         */
        target.invulnerableTime = 0;

        target.hurt(this.damageSources().mobAttack(this), this.needleDamagePerHit);

        Vec3 hitPos = new Vec3(
                target.getX(),
                target.getY() + target.getBbHeight() * 0.55D,
                target.getZ()
        );

        int particles = this.needleVolleyJumbo ? 10 : 4;

        serverLevel.sendParticles(
                ParticleTypes.CRIT,
                hitPos.x,
                hitPos.y,
                hitPos.z,
                particles,
                this.needleVolleyJumbo ? 0.75D : 0.35D,
                this.needleVolleyJumbo ? 0.75D : 0.35D,
                this.needleVolleyJumbo ? 0.75D : 0.35D,
                this.needleVolleyJumbo ? 0.14D : 0.08D
        );

        if (this.tickCount % (this.needleVolleyJumbo ? 2 : 4) == 0) {
            this.level().playSound(
                    null,
                    target.getX(),
                    target.getY(),
                    target.getZ(),
                    SoundEvents.ARROW_HIT,
                    SoundSource.HOSTILE,
                    this.needleVolleyJumbo ? 0.9F : 0.55F,
                    this.needleVolleyJumbo ? 1.8F : 1.35F
            );
        }
    }

    private void spawnNeedleArrows(LivingEntity target, int count, float speed, float spread) {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        Vec3 start = new Vec3(
                this.getX(),
                this.getY() + this.getBbHeight() * 0.72D,
                this.getZ()
        );

        Vec3 targetPos = new Vec3(
                target.getX(),
                target.getY() + target.getBbHeight() * 0.55D,
                target.getZ()
        );

        Vec3 baseDirection = targetPos.subtract(start).normalize();

        for (int i = 0; i < count; i++) {
            CactuarNeedleProjectile needle = new CactuarNeedleProjectile(
                    ModEntitiesRM.TYPE_CACTUAR_NEEDLE.get(),
                    this.level(),
                    this
            );

            double offsetX = (this.random.nextDouble() - 0.5D) * 0.85D;
            double offsetY = (this.random.nextDouble() - 0.5D) * 0.55D;
            double offsetZ = (this.random.nextDouble() - 0.5D) * 0.85D;

            Vec3 spawnPos = start.add(offsetX, offsetY, offsetZ);

            needle.setPos(spawnPos.x, spawnPos.y, spawnPos.z);
            needle.setBaseDamage(0.0D);
            needle.setNoGravity(true);

            double randomX = baseDirection.x + ((this.random.nextDouble() - 0.5D) * 0.12D);
            double randomY = baseDirection.y + ((this.random.nextDouble() - 0.5D) * 0.09D);
            double randomZ = baseDirection.z + ((this.random.nextDouble() - 0.5D) * 0.12D);

            needle.shoot(randomX, randomY, randomZ, speed, spread);

            serverLevel.addFreshEntity(needle);
        }
    }

    private void doJumboStomp() {
        playActionAnimation(ACTION_JUMBO_STOMP, 35);

        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        AABB area = this.getBoundingBox().inflate(4.5D, 1.5D, 4.5D);

        for (LivingEntity target : this.level().getEntitiesOfClass(LivingEntity.class, area, this::canHitTarget)) {
            target.hurt(this.damageSources().mobAttack(this), 16.0F);

            Vec3 knock = target.position().subtract(this.position()).normalize().scale(1.2D);
            target.setDeltaMovement(target.getDeltaMovement().add(knock.x, 0.45D, knock.z));
            target.hurtMarked = true;
        }

        serverLevel.sendParticles(
                ParticleTypes.POOF,
                this.getX(),
                this.getY(),
                this.getZ(),
                60,
                3.5D,
                0.25D,
                3.5D,
                0.08D
        );

        this.level().playSound(
                null,
                this.getX(),
                this.getY(),
                this.getZ(),
                SoundEvents.GENERIC_EXPLODE,
                SoundSource.HOSTILE,
                1.3F,
                0.55F
        );

        this.jumboStompCooldown = 90;
    }

    private void doKerPlunkStart(LivingEntity target) {
        playActionAnimation(ACTION_KER_PLUNK, KER_PLUNK_TICKS);

        this.getNavigation().stop();
        this.setDeltaMovement(Vec3.ZERO);
        this.hasImpulse = true;
        this.hurtMarked = true;

        this.kerPlunkImpactTicks = 65;
        this.kerPlunkCooldown = 260;
    }

    private void doKerPlunkImpact() {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        this.getNavigation().stop();
        this.setDeltaMovement(Vec3.ZERO);

        AABB area = this.getBoundingBox().inflate(14.0D, 2.0D, 14.0D);

        for (LivingEntity target : this.level().getEntitiesOfClass(LivingEntity.class, area, this::canHitTarget)) {
            target.hurt(this.damageSources().mobAttack(this), 28.0F);

            Vec3 knock = target.position().subtract(this.position()).normalize().scale(2.75D);
            target.setDeltaMovement(target.getDeltaMovement().add(knock.x, 1.5D, knock.z));
            target.hurtMarked = true;
        }

        serverLevel.sendParticles(
                ParticleTypes.EXPLOSION,
                this.getX(),
                this.getY() + 0.2D,
                this.getZ(),
                10,
                3.0D,
                0.25D,
                3.0D,
                0.0D
        );

        serverLevel.sendParticles(
                ParticleTypes.POOF,
                this.getX(),
                this.getY(),
                this.getZ(),
                120,
                5.0D,
                0.4D,
                5.0D,
                0.12D
        );

        this.level().playSound(
                null,
                this.getX(),
                this.getY(),
                this.getZ(),
                SoundEvents.GENERIC_EXPLODE,
                SoundSource.HOSTILE,
                2.0F,
                0.45F
        );
    }

    private boolean canHitTarget(LivingEntity target) {
        if (target == this) {
            return false;
        }

        if (target instanceof CactuarEntity) {
            return false;
        }

        return target.isAlive();
    }

    private void startEscape(LivingEntity target) {
        this.escaping = true;
        this.escapeTicks = 0;
        this.setTarget(null);

        Vec3 away = this.position().subtract(target.position()).normalize();

        this.setDeltaMovement(
                away.x * 0.85D,
                0.15D,
                away.z * 0.85D
        );

        this.hasImpulse = true;
        this.hurtMarked = true;
    }

    private void tickEscape() {
        this.escapeTicks++;

        LivingEntity lastTarget = this.getLastHurtByMob();

        Vec3 away;

        if (lastTarget != null) {
            away = this.position().subtract(lastTarget.position()).normalize();
        } else {
            away = this.getLookAngle().scale(-1.0D);
        }

        double moveX = this.getX() + away.x * 16.0D;
        double moveZ = this.getZ() + away.z * 16.0D;

        this.getNavigation().moveTo(moveX, this.getY(), moveZ, 1.55D);

        if (this.escapeTicks > 100) {
            if (this.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(
                        ParticleTypes.POOF,
                        this.getX(),
                        this.getY() + 0.8D,
                        this.getZ(),
                        18,
                        0.4D,
                        0.6D,
                        0.4D,
                        0.05D
                );
            }

            this.discard();
        }
    }

    @Override
    public boolean doHurtTarget(Entity entity) {
        boolean hit = super.doHurtTarget(entity);

        if (!hit || this.level().isClientSide) {
            return hit;
        }

        if (this.isActionBusy()) {
            return true;
        }

        if (this.isJumbo()) {
            playActionAnimation(ACTION_JUMBO_STOMP, 28);
        } else {
            playActionAnimation(ACTION_KICK, 14);
        }

        return true;
    }

    private void playActionAnimation(int action, int ticks) {
        if (this.level().isClientSide) {
            return;
        }

        this.entityData.set(ACTION, action);
        this.entityData.set(ACTION_TICKS, ticks);
        this.currentAnimation = "none";
    }

    @Override
    public void die(DamageSource source) {
        if (!this.level().isClientSide) {
            clearNeedleVolley();
            this.entityData.set(ACTION, ACTION_DEATH);
            this.entityData.set(ACTION_TICKS, DEATH_ANIMATION_TICKS);
            this.currentAnimation = "none";
            this.getNavigation().stop();
            this.setDeltaMovement(Vec3.ZERO);
            lockDeathYaw();
        }

        if (!this.level().isClientSide && !this.isJumbo()) {
            tryAwakenJumboCactuar(source);
        }

        if (!this.level().isClientSide && this.isJumbo()) {
            stopJumboBossMusicForAll();
        }

        super.die(source);

        if (!this.level().isClientSide) {
            this.entityData.set(ACTION, ACTION_DEATH);
            this.entityData.set(ACTION_TICKS, DEATH_ANIMATION_TICKS);
            this.currentAnimation = "none";
            lockDeathYaw();
        }
    }

    private void tryAwakenJumboCactuar(DamageSource damageSource) {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        if (!(damageSource.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        if (!serverLevel.getBiome(this.blockPosition()).is(DESERT_BIOME)) {
            return;
        }

        if (this.random.nextDouble() > JUMBO_SPAWN_CHANCE) {
            return;
        }

        if (hasNearbyJumboCactuar(serverLevel)) {
            return;
        }

        CactuarEntity jumbo = ModEntitiesRM.TYPE_JUMBO_CACTUAR.get().create(serverLevel);

        if (jumbo == null) {
            return;
        }

        BlockPos spawnPos = findJumboSpawnPos(serverLevel, player);

        if (spawnPos == null) {
            return;
        }

        jumbo.setVariant(VARIANT_JUMBO);
        jumbo.moveTo(
                spawnPos.getX() + 0.5D,
                spawnPos.getY(),
                spawnPos.getZ() + 0.5D,
                player.getYRot() + 180.0F,
                0.0F
        );

        jumbo.setHealth(jumbo.getMaxHealth());

        serverLevel.addFreshEntity(jumbo);

        serverLevel.playSound(
                null,
                spawnPos,
                SoundEvents.ENDER_DRAGON_GROWL,
                SoundSource.HOSTILE,
                1.5F,
                1.25F
        );

        serverLevel.sendParticles(
                ParticleTypes.EXPLOSION,
                spawnPos.getX() + 0.5D,
                spawnPos.getY() + 1.0D,
                spawnPos.getZ() + 0.5D,
                12,
                2.5D,
                1.5D,
                2.5D,
                0.05D
        );

        player.displayClientMessage(
                Component.literal("The desert trembles... Jumbo Cactuar has appeared!")
                        .withStyle(ChatFormatting.GOLD),
                false
        );
    }

    private boolean hasNearbyJumboCactuar(ServerLevel serverLevel) {
        AABB box = this.getBoundingBox().inflate(JUMBO_NEARBY_CHECK_RANGE);

        return !serverLevel.getEntitiesOfClass(
                CactuarEntity.class,
                box,
                entity -> entity.isJumbo() && entity.isAlive()
        ).isEmpty();
    }

    private BlockPos findJumboSpawnPos(ServerLevel serverLevel, ServerPlayer player) {
        for (int attempts = 0; attempts < 24; attempts++) {
            double angle = this.random.nextDouble() * Math.PI * 2.0D;
            double distance = 18.0D + this.random.nextDouble() * 10.0D;

            int x = Mth.floor(player.getX() + Math.cos(angle) * distance);
            int z = Mth.floor(player.getZ() + Math.sin(angle) * distance);

            BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(x, player.blockPosition().getY() + 8, z);

            while (pos.getY() > serverLevel.getMinBuildHeight() + 2) {
                BlockPos below = pos.below();

                if (serverLevel.getBiome(pos).is(DESERT_BIOME)
                        && serverLevel.getBlockState(pos).isAir()
                        && serverLevel.getBlockState(pos.above()).isAir()
                        && serverLevel.getBlockState(below).isSolidRender(serverLevel, below)) {
                    return pos.immutable();
                }

                pos.move(0, -1, 0);
            }
        }

        return null;
    }

    @Override
    protected void tickDeath() {
        this.deathTime++;

        if (!this.level().isClientSide) {
            lockDeathYaw();

            this.getNavigation().stop();
            this.setDeltaMovement(Vec3.ZERO);

            if (this.entityData.get(ACTION) != ACTION_DEATH) {
                this.entityData.set(ACTION, ACTION_DEATH);
                this.currentAnimation = "none";
            }

            int remaining = Math.max(1, DEATH_ANIMATION_TICKS - this.deathTime);
            this.entityData.set(ACTION_TICKS, remaining);

            if (this.deathTime >= DEATH_ANIMATION_TICKS) {
                this.level().broadcastEntityEvent(this, (byte) 60);
                this.remove(Entity.RemovalReason.KILLED);
            }
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(
                this,
                "main_controller",
                0,
                this::mainPredicate
        ));
    }

    private PlayState mainPredicate(AnimationState<CactuarEntity> state) {
        int action = this.getCactuarAction();
        int actionTicks = this.getCactuarActionTicks();

        if (this.deathTime > 0 || this.getHealth() <= 0.0F || action == ACTION_DEATH) {
            setAnimation(state, "death", DEATH_ANIM);
            return PlayState.CONTINUE;
        }

        if (action != ACTION_NONE && actionTicks > 0) {
            switch (action) {
                case ACTION_KICK -> setAnimation(state, "attack_normal", KICK_ANIM);
                case ACTION_NEEDLES -> setAnimation(state, "needles", NEEDLES_ANIM);
                case ACTION_JUMBO_STOMP -> setAnimation(state, "jumbo_attack", JUMBO_STOMP_ANIM);
                case ACTION_KER_PLUNK -> setAnimation(state, "ker_plunk", KER_PLUNK_ANIM);
                default -> setAnimation(state, "idle", IDLE_ANIM);
            }

            return PlayState.CONTINUE;
        }

        setAnimation(state, "idle", IDLE_ANIM);
        return PlayState.CONTINUE;
    }

    private void setAnimation(AnimationState<CactuarEntity> state, String name, RawAnimation animation) {
        if (name.equals(this.currentAnimation)) {
            return;
        }

        this.currentAnimation = name;
        state.getController().forceAnimationReset();
        state.getController().setAnimation(animation);
    }

    @Override
    protected void updateWalkAnimation(float partialTick) {
        float speed;

        if (this.getPose() == Pose.STANDING) {
            speed = Math.min(partialTick * 6.0F, 1.0F);
        } else {
            speed = 0.0F;
        }

        this.walkAnimation.update(speed, 0.2F);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        return super.hurt(source, amount);
    }

    private ServerPlayer getJumboCactuarKiller(DamageSource damageSource) {
        if (damageSource != null && damageSource.getEntity() instanceof ServerPlayer serverPlayer) {
            return serverPlayer;
        }

        if (this.getKillCredit() instanceof ServerPlayer serverPlayer) {
            return serverPlayer;
        }

        return null;
    }

    @Override
    protected void dropCustomDeathLoot(
            ServerLevel level,
            DamageSource damageSource,
            boolean recentlyHit
    ) {
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);

        ServerPlayer killer = getJumboCactuarKiller(damageSource);

        if (killer == null){
            return;
        }

        GlobalDataRM globalData = ModDataRM.getGlobal(killer);

        if (globalData == null) {
            return;
        }

        if (globalData.hasDefeatedJumboCactuar()) {
            return;
        }



        PacketHandlerRM.syncGlobalToAllAround(killer, globalData);

        if (this.isJumbo()) {

            ItemStack charm = new ItemStack(ModItemsRM.cactuarCharm.get());

            Utils.giveItems(killer, true, charm);

            globalData.setDefeatedJumboCactuar(true);

            /*
             * Jumbo Cactuar
             *
             * Cactus: 16–32
             * Cactuar Needle: guaranteed 1
             */

            int cactusAmount = 16 + this.random.nextInt(17);

            this.spawnAtLocation(
                    new ItemStack(Items.CACTUS, cactusAmount)
            );

            this.spawnAtLocation(
                    new ItemStack(ModItemsRM.cactuarNeedle.get(), 1)
            );

            return;
        }

        /*
         * Normal Cactuar
         *
         * Cactus: 1–3
         * Cactuar Needle: 20% chance for 1
         */

        int cactusAmount = 1 + this.random.nextInt(3);

        this.spawnAtLocation(
                new ItemStack(Items.CACTUS, cactusAmount)
        );

        if (this.random.nextFloat() < 0.20F) {
            this.spawnAtLocation(
                    new ItemStack(ModItemsRM.cactuarNeedle.get(), 1)
            );
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("CactuarVariant", this.getVariant());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);

        if (tag.contains("CactuarVariant")) {
            this.setVariant(tag.getInt("CactuarVariant"));
        }
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    private void lockDeathYaw() {
        this.setYRot(0.0F);
        this.yRotO = 0.0F;

        this.setXRot(0.0F);
        this.xRotO = 0.0F;

        this.setYHeadRot(0.0F);
        this.yHeadRotO = 0.0F;

        this.yBodyRot = 0.0F;
        this.yBodyRotO = 0.0F;
    }

    public static boolean checkCactuarSpawnRules(
            EntityType<? extends CactuarEntity> type,
            ServerLevelAccessor level,
            MobSpawnType spawnType,
            BlockPos pos,
            RandomSource random
    ) {
        if (level.getDifficulty() == Difficulty.PEACEFUL) {
            return false;
        }

        if (!level.getBiome(pos).is(DESERT_BIOME)) {
            return false;
        }

        if (!level.getBlockState(pos.below()).is(Blocks.SAND)
                && !level.getBlockState(pos.below()).is(Blocks.RED_SAND)
                && !level.getBlockState(pos.below()).is(Blocks.SANDSTONE)
                && !level.getBlockState(pos.below()).is(Blocks.RED_SANDSTONE)) {
            return false;
        }

        if (!level.getBlockState(pos).isAir()) {
            return false;
        }

        if (!level.getBlockState(pos.above()).isAir()) {
            return false;
        }

        return true;
    }
    private void tickJumboBossMusic() {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        if (this.bossEvent == null) {
            return;
        }

        if (!shouldPlayJumboBossMusic()) {
            stopJumboBossMusicForAll();
            this.jumboBossMusicRefreshTicks = JUMBO_BOSS_MUSIC_REFRESH_TICKS;
            this.jumboSuppressMcMusicTicks = JUMBO_SUPPRESS_MC_MUSIC_TICKS;
            return;
        }

        Set<UUID> allowedPlayers = new HashSet<>();

        for (ServerPlayer player : this.bossEvent.getPlayers()) {
            if (canPlayerHearJumboBossMusic(player)) {
                UUID uuid = player.getUUID();
                allowedPlayers.add(uuid);

                suppressMinecraftMusic(player);

                if (!this.jumboBossMusicPlayers.contains(uuid)) {
                    startJumboBossMusic(player, false);
                }
            } else {
                stopJumboBossMusic(player);
            }
        }

        for (UUID uuid : new HashSet<>(this.jumboBossMusicPlayers)) {
            if (allowedPlayers.contains(uuid)) {
                continue;
            }

            ServerPlayer player = serverLevel.getServer().getPlayerList().getPlayer(uuid);

            if (player != null) {
                stopJumboBossMusic(player);
            } else {
                this.jumboBossMusicPlayers.remove(uuid);
            }
        }

        this.jumboSuppressMcMusicTicks--;

        if (this.jumboSuppressMcMusicTicks <= 0) {
            suppressMinecraftMusicForCurrentListeners(serverLevel);
            this.jumboSuppressMcMusicTicks = JUMBO_SUPPRESS_MC_MUSIC_TICKS;
        }

        this.jumboBossMusicRefreshTicks--;

        if (this.jumboBossMusicRefreshTicks <= 0) {
            refreshJumboBossMusic(serverLevel);
            this.jumboBossMusicRefreshTicks = JUMBO_BOSS_MUSIC_REFRESH_TICKS;
        }
    }

    private boolean shouldPlayJumboBossMusic() {
        if (!this.isJumbo()) {
            return false;
        }

        if (!this.isAlive()) {
            return false;
        }

        LivingEntity target = this.getTarget();

        if (target == null || !target.isAlive()) {
            return false;
        }

        return this.distanceToSqr(target) <= JUMBO_BOSS_MUSIC_RANGE * JUMBO_BOSS_MUSIC_RANGE;
    }

    private boolean canPlayerHearJumboBossMusic(ServerPlayer player) {
        if (player == null) {
            return false;
        }

        if (!player.isAlive()) {
            return false;
        }

        if (player.level() != this.level()) {
            return false;
        }

        return player.distanceToSqr(this) <= JUMBO_BOSS_MUSIC_RANGE * JUMBO_BOSS_MUSIC_RANGE;
    }

    private void refreshJumboBossMusic(ServerLevel serverLevel) {
        for (UUID uuid : new HashSet<>(this.jumboBossMusicPlayers)) {
            ServerPlayer player = serverLevel.getServer().getPlayerList().getPlayer(uuid);

            if (player == null) {
                this.jumboBossMusicPlayers.remove(uuid);
                continue;
            }

            if (!canPlayerHearJumboBossMusic(player)) {
                stopJumboBossMusic(player);
                continue;
            }

            startJumboBossMusic(player, true);
        }
    }

    private void startJumboBossMusic(ServerPlayer player, boolean restart) {
        if (player == null) {
            return;
        }

        UUID uuid = player.getUUID();

        if (!restart && this.jumboBossMusicPlayers.contains(uuid)) {
            suppressMinecraftMusic(player);
            return;
        }

        suppressMinecraftMusic(player);

        if (restart) {
            stopJumboBossMusic(player);
            suppressMinecraftMusic(player);
        }

        /*
         * Boss music uses the RECORD channel so /stopsound @s music
         * can cancel vanilla Minecraft music without cancelling this.
         */
        runJumboMusicCommand(
                player,
                "playsound " + JUMBO_BOSS_MUSIC_SOUND + " record @s ~ ~ ~ 0.55 1.0 0.0"
        );

        this.jumboBossMusicPlayers.add(uuid);
    }

    private void stopJumboBossMusic(ServerPlayer player) {
        if (player == null) {
            return;
        }

        runJumboMusicCommand(
                player,
                "stopsound @s record " + JUMBO_BOSS_MUSIC_SOUND
        );

        this.jumboBossMusicPlayers.remove(player.getUUID());
    }

    private void suppressMinecraftMusicForCurrentListeners(ServerLevel serverLevel) {
        for (UUID uuid : new HashSet<>(this.jumboBossMusicPlayers)) {
            ServerPlayer player = serverLevel.getServer().getPlayerList().getPlayer(uuid);

            if (player == null) {
                this.jumboBossMusicPlayers.remove(uuid);
                continue;
            }

            if (!canPlayerHearJumboBossMusic(player)) {
                stopJumboBossMusic(player);
                continue;
            }

            suppressMinecraftMusic(player);
        }
    }

    private void suppressMinecraftMusic(ServerPlayer player) {
        if (player == null) {
            return;
        }

        /*
         * This cancels vanilla/background Minecraft music only.
         * It does not stop Jumbo music because Jumbo music plays on the RECORD channel.
         */
        runJumboMusicCommand(
                player,
                "stopsound @s music"
        );
    }

    private void stopJumboBossMusicForAll() {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            this.jumboBossMusicPlayers.clear();
            return;
        }

        for (UUID uuid : new HashSet<>(this.jumboBossMusicPlayers)) {
            ServerPlayer player = serverLevel.getServer().getPlayerList().getPlayer(uuid);

            if (player != null) {
                stopJumboBossMusic(player);
            } else {
                this.jumboBossMusicPlayers.remove(uuid);
            }
        }

        this.jumboBossMusicPlayers.clear();
    }

    private void runJumboMusicCommand(ServerPlayer player, String command) {
        if (player == null || command == null || command.isEmpty()) {
            return;
        }

        MinecraftServer server = player.getServer();

        if (server == null) {
            return;
        }

        try {
            CommandSourceStack source = player.createCommandSourceStack()
                    .withSuppressedOutput()
                    .withPermission(4);

            server.getCommands().performPrefixedCommand(source, command);
        } catch (Exception ignored) {
        }
    }
}