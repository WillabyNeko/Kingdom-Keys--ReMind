package online.remind.remind.entity.spirits;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import online.kingdomkeys.kingdomkeys.data.PlayerData;
import online.remind.remind.KingdomKeysReMind;
import online.remind.remind.capabilities.GlobalDataRM;
import online.remind.remind.capabilities.ModDataRM;
import online.remind.remind.client.sound.ModSoundsRM;
import online.remind.remind.dreameater.DreamEaterExpHandler;
import online.remind.remind.entity.ModEntitiesRM;
import online.remind.remind.entity.projectile.CactuarNeedleProjectile;
import online.remind.remind.item.ModItemsRM;
import online.remind.remind.network.PacketHandlerRM;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.Comparator;
import java.util.Optional;
import java.util.UUID;

public class CactuarSpiritEntity extends PathfinderMob implements GeoEntity {

    private static final String CACTUAR_DREAM_EATER_RL =
            KingdomKeysReMind.MODID + ":dreameater_cactuar";

    private static final int ACTION_IDLE = 0;
    private static final int ACTION_KICK = 1;
    private static final int ACTION_NEEDLES = 2;

    private static final int KICK_ANIM_TICKS = 24;
    private static final int NEEDLES_ANIM_TICKS = 46;

    private static final double FOLLOW_DISTANCE_SQR = 7.0D;

    private static final EntityDataAccessor<Optional<UUID>> OWNER_UUID =
            SynchedEntityData.defineId(CactuarSpiritEntity.class, EntityDataSerializers.OPTIONAL_UUID);

    private static final EntityDataAccessor<Integer> ACTION =
            SynchedEntityData.defineId(CactuarSpiritEntity.class, EntityDataSerializers.INT);

    private static final EntityDataAccessor<Integer> ACTION_TICKS =
            SynchedEntityData.defineId(CactuarSpiritEntity.class, EntityDataSerializers.INT);

    private static final RawAnimation IDLE_ANIM =
            RawAnimation.begin().thenLoop("idle");

    private static final RawAnimation KICK_ANIM =
            RawAnimation.begin().thenPlay("attack_normal");

    private static final RawAnimation NEEDLES_ANIM =
            RawAnimation.begin().thenPlay("needles");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private String currentMainAnimation = "";

    private int kickCooldown = 0;
    private int needlesCooldown = 0;

    private LivingEntity target;

    private int needleTargetId = -1;
    private int needleVolleyTicks = 0;
    private int needleVolleyInterval = 0;
    private int needleHitInterval = 0;
    private int needleHitsRemaining = 0;
    private float needleDamagePerHit = 0.0F;


    private static final int CACTUS_FEED_EXP = 15;
    private static final int CACTUAR_NEEDLE_FEED_EXP = 50;
    private static final int CACTUS_FEED_COOLDOWN_TICKS = 10;

    private int cactusFeedCooldown = 20;

    public CactuarSpiritEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        this.setPersistenceRequired();
    }

    public CactuarSpiritEntity(Level level, Player owner) {
        this(ModEntitiesRM.TYPE_CACTUAR_SPIRIT.get(), level);

        if (owner != null) {
            this.setOwnerUUID(owner.getUUID());
            this.setPersistenceRequired();
            this.applyOwnerScaling(owner);
            this.setHealth(this.getMaxHealth());
        }
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 32.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.33D)
                .add(Attributes.ATTACK_DAMAGE, 6.0D)
                .add(Attributes.ARMOR, 3.0D)
                .add(Attributes.FOLLOW_RANGE, 32.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.15D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(OWNER_UUID, Optional.empty());
        builder.define(ACTION, ACTION_IDLE);
        builder.define(ACTION_TICKS, 0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
    }

    public UUID getOwnerUUID() {
        return this.entityData.get(OWNER_UUID).orElse(null);
    }

    public void setOwnerUUID(UUID ownerUUID) {
        this.entityData.set(OWNER_UUID, Optional.ofNullable(ownerUUID));
    }

    public int getCactuarAction() {
        return this.entityData.get(ACTION);
    }

    public void setCactuarAction(int action) {
        this.entityData.set(ACTION, action);
    }

    public int getCactuarActionTicks() {
        return this.entityData.get(ACTION_TICKS);
    }

    public void setCactuarActionTicks(int ticks) {
        this.entityData.set(ACTION_TICKS, ticks);
    }

    private boolean isActionBusy() {
        return this.getCactuarAction() != ACTION_IDLE && this.getCactuarActionTicks() > 0;
    }

    private void playActionAnimation(int action, int ticks) {
        this.setCactuarAction(action);
        this.setCactuarActionTicks(ticks);
        this.currentMainAnimation = "";
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);

        UUID ownerUUID = this.getOwnerUUID();

        if (ownerUUID != null) {
            tag.putUUID("Owner", ownerUUID);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);

        if (tag.hasUUID("Owner")) {
            this.setOwnerUUID(tag.getUUID("Owner"));
        } else {
            this.setOwnerUUID(null);
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide) {
            return;
        }

        UUID ownerId = this.getOwnerUUID();

        if (ownerId == null) {
            this.discard();
            return;
        }

        ServerPlayer owner = getOwnerPlayerFromServer(ownerId);

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

        if (!isSelectedDreamEaterCactuar(data)) {
            clearDreamEaterData(owner, data);
            this.discard();
            return;
        }

        if (this.tickCount % 40 == 0) {
            this.applyOwnerScaling(owner);
        }

        if (this.cactusFeedCooldown > 0) {
            this.cactusFeedCooldown--;
        }

        tickCooldowns();
        tickAction();
        tickNeedleVolley(owner);

        followOwner(owner);
        updateCombatTarget(owner);

        if (!isActionBusy()) {
            tryStartKick(owner);
            tryStartNeedles(owner);
        }
    }

    private void tickCooldowns() {
        if (this.kickCooldown > 0) {
            this.kickCooldown--;
        }

        if (this.needlesCooldown > 0) {
            this.needlesCooldown--;
        }
    }

    private void tickAction() {
        if (this.getCactuarActionTicks() > 0) {
            this.setCactuarActionTicks(this.getCactuarActionTicks() - 1);
        }

        if (this.getCactuarActionTicks() <= 0 && this.getCactuarAction() != ACTION_IDLE) {
            this.setCactuarAction(ACTION_IDLE);
        }
    }

    private void followOwner(Player owner) {
        if (this.distanceToSqr(owner) > FOLLOW_DISTANCE_SQR) {
            this.getNavigation().moveTo(owner, 1.18D);
        } else {
            this.getNavigation().stop();
        }
    }

    private void updateCombatTarget(Player owner) {
        if (this.target != null && canCactuarAttack(owner, this.target)) {
            return;
        }

        this.target = null;

        LivingEntity ownerAttacker = owner.getLastHurtByMob();

        if (canCactuarAttack(owner, ownerAttacker)) {
            this.target = ownerAttacker;
            return;
        }

        LivingEntity ownerTarget = owner.getLastHurtMob();

        if (canCactuarAttack(owner, ownerTarget)) {
            this.target = ownerTarget;
            return;
        }

        AABB searchBox = this.getBoundingBox().inflate(12.0D, 5.0D, 12.0D);

        this.target = this.level().getEntitiesOfClass(
                        LivingEntity.class,
                        searchBox,
                        entity -> canCactuarAttack(owner, entity)
                )
                .stream()
                .min(Comparator.comparingDouble(this::distanceToSqr))
                .orElse(null);
    }

    private void tryStartKick(Player owner) {
        if (this.target == null || !canCactuarAttack(owner, this.target)) {
            return;
        }

        if (this.kickCooldown > 0) {
            return;
        }

        double distanceSqr = this.distanceToSqr(this.target);

        if (distanceSqr > 2.6D * 2.6D) {
            return;
        }

        playActionAnimation(ACTION_KICK, KICK_ANIM_TICKS);

        this.getLookControl().setLookAt(this.target, 30.0F, 30.0F);

        float damage = Math.max(2.0F, (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE));
        this.target.hurt(this.damageSources().mobAttack(this), damage);

        this.level().playSound(
                null,
                this.target.blockPosition(),
                SoundEvents.PLAYER_ATTACK_SWEEP,
                SoundSource.NEUTRAL,
                0.8F,
                1.35F
        );

        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                    ParticleTypes.SWEEP_ATTACK,
                    this.target.getX(),
                    this.target.getY() + this.target.getBbHeight() * 0.45D,
                    this.target.getZ(),
                    2,
                    0.2D,
                    0.2D,
                    0.2D,
                    0.01D
            );
        }

        this.kickCooldown = 32;
    }

    private void tryStartNeedles(Player owner) {
        if (this.target == null || !canCactuarAttack(owner, this.target)) {
            return;
        }

        if (this.needlesCooldown > 0) {
            return;
        }

        double distanceSqr = this.distanceToSqr(this.target);

        if (distanceSqr < 3.0D * 3.0D || distanceSqr > 13.0D * 13.0D) {
            return;
        }

        playActionAnimation(ACTION_NEEDLES, NEEDLES_ANIM_TICKS);

        this.needleTargetId = this.target.getId();
        this.needleVolleyTicks = 38;
        this.needleVolleyInterval = 0;
        this.needleHitInterval = 0;
        this.needleHitsRemaining = 20;
        this.needleDamagePerHit = Math.max(
                0.25F,
                (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE) / 12.0F
        );

        this.level().playSound(
                null,
                this.getX(),
                this.getY(),
                this.getZ(),
                SoundEvents.ARROW_SHOOT,
                SoundSource.NEUTRAL,
                0.8F,
                1.7F
        );

        this.needlesCooldown = 90;
    }

    private void tickNeedleVolley(Player owner) {
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

        if (!(entity instanceof LivingEntity targetEntity) || !canCactuarAttack(owner, targetEntity)) {
            clearNeedleVolley();
            return;
        }

        this.getLookControl().setLookAt(targetEntity, 30.0F, 30.0F);

        if (this.needleVolleyInterval <= 0) {
            spawnNeedleArrows(targetEntity, 4, 1.85F, 3.0F);
            this.needleVolleyInterval = 2;
        } else {
            this.needleVolleyInterval--;
        }

        if (this.needleHitsRemaining > 0) {
            if (this.needleHitInterval <= 0) {
                doNeedleMultiHit(serverLevel, targetEntity);
                this.needleHitsRemaining--;
                this.needleHitInterval = 1;
            } else {
                this.needleHitInterval--;
            }
        }

        this.needleVolleyTicks--;

        if (this.needleVolleyTicks <= 0) {
            clearNeedleVolley();
        }
    }

    private void doNeedleMultiHit(ServerLevel serverLevel, LivingEntity targetEntity) {
        targetEntity.invulnerableTime = 0;
        targetEntity.hurt(this.damageSources().mobAttack(this), this.needleDamagePerHit);

        serverLevel.sendParticles(
                ParticleTypes.CRIT,
                targetEntity.getX(),
                targetEntity.getY() + targetEntity.getBbHeight() * 0.55D,
                targetEntity.getZ(),
                5,
                0.35D,
                0.35D,
                0.35D,
                0.08D
        );

        if (this.tickCount % 4 == 0) {
            this.level().playSound(
                    null,
                    targetEntity.getX(),
                    targetEntity.getY(),
                    targetEntity.getZ(),
                    SoundEvents.ARROW_HIT,
                    SoundSource.NEUTRAL,
                    0.45F,
                    1.65F
            );
        }
    }

    private void clearNeedleVolley() {
        this.needleTargetId = -1;
        this.needleVolleyTicks = 0;
        this.needleVolleyInterval = 0;
        this.needleHitInterval = 0;
        this.needleHitsRemaining = 0;
        this.needleDamagePerHit = 0.0F;
    }

    private void spawnNeedleArrows(LivingEntity targetEntity, int count, float speed, float spread) {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        Vec3 start = new Vec3(
                this.getX(),
                this.getY() + this.getBbHeight() * 0.72D,
                this.getZ()
        );

        Vec3 targetPos = new Vec3(
                targetEntity.getX(),
                targetEntity.getY() + targetEntity.getBbHeight() * 0.55D,
                targetEntity.getZ()
        );

        Vec3 baseDirection = targetPos.subtract(start).normalize();

        for (int i = 0; i < count; i++) {
            CactuarNeedleProjectile needle = new CactuarNeedleProjectile(
                    ModEntitiesRM.TYPE_CACTUAR_NEEDLE.get(),
                    this.level(),
                    this
            );

            double offsetX = (this.random.nextDouble() - 0.5D) * 0.65D;
            double offsetY = (this.random.nextDouble() - 0.5D) * 0.45D;
            double offsetZ = (this.random.nextDouble() - 0.5D) * 0.65D;

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

    private boolean canCactuarAttack(Player owner, LivingEntity entity) {
        if (owner == null || entity == null) {
            return false;
        }

        if (!entity.isAlive()) {
            return false;
        }

        if (entity == this || entity == owner) {
            return false;
        }

        if (entity instanceof Player) {
            return false;
        }

        if (entity instanceof ChirithyEntity) {
            return false;
        }

        if (entity instanceof MeowWowEntity) {
            return false;
        }

        if (entity instanceof KomoryBatEntity) {
            return false;
        }

        if (entity instanceof CactuarSpiritEntity) {
            return false;
        }

        if (entity instanceof TamableAnimal tamable) {
            UUID tameOwner = tamable.getOwnerUUID();

            if (tameOwner != null && tameOwner.equals(owner.getUUID())) {
                return false;
            }
        }

        if (entity.isAlliedTo(owner) || owner.isAlliedTo(entity)) {
            return false;
        }

        if (owner.getLastHurtByMob() == entity) {
            return true;
        }

        if (owner.getLastHurtMob() == entity) {
            return true;
        }

        if (entity instanceof Mob mob && mob.getTarget() == owner) {
            return true;
        }

        return entity instanceof Enemy;
    }

    private void applyOwnerScaling(Player owner) {
        if (owner == null) {
            return;
        }

        PlayerData ownerData = PlayerData.get(owner);

        if (ownerData == null) {
            return;
        }

        int ownerLevel = Math.max(1, ownerData.getLevel());

        double hp = 20.0D + (ownerData.getMaxHP() * 0.45D) + (ownerLevel * 0.55D);
        double str = 4.0D + (ownerData.getStrengthStat().getStat() * 0.45D) + (ownerLevel * 0.10D);
        double def = 2.0D + (ownerData.getDefenseStat().getStat() * 0.35D);

        if (this.getAttribute(Attributes.MAX_HEALTH) != null) {
            this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(hp);
        }

        if (this.getAttribute(Attributes.ATTACK_DAMAGE) != null) {
            this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(str);
        }

        if (this.getAttribute(Attributes.ARMOR) != null) {
            this.getAttribute(Attributes.ARMOR).setBaseValue(def);
        }

        if (this.getHealth() > this.getMaxHealth()) {
            this.setHealth(this.getMaxHealth());
        }
    }

    private boolean isSelectedDreamEaterCactuar(GlobalDataRM data) {
        if (data == null || data.getDreamEaterRL() == null) {
            return false;
        }

        String rl = data.getDreamEaterRL();

        return CACTUAR_DREAM_EATER_RL.equals(rl)
                || (KingdomKeysReMind.MODID + ":cactuar").equals(rl);
    }

    private ServerPlayer getOwnerPlayerFromServer(UUID ownerId) {
        MinecraftServer server = this.getServer();

        if (server == null) {
            return null;
        }

        for (ServerLevel serverLevel : server.getAllLevels()) {
            ServerPlayer player = (ServerPlayer) serverLevel.getPlayerByUUID(ownerId);

            if (player != null) {
                return player;
            }
        }

        return null;
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

        if (attacker != null && ownerId.equals(attacker.getUUID())) {
            return true;
        }

        if (directEntity != null && ownerId.equals(directEntity.getUUID())) {
            return true;
        }

        if (directEntity instanceof Projectile projectile) {
            Entity projectileOwner = projectile.getOwner();

            return projectileOwner != null && ownerId.equals(projectileOwner.getUUID());
        }

        return false;
    }

    public static void removeExistingCactuarSpirit(ServerLevel level, UUID ownerUUID) {
        if (level == null || ownerUUID == null) {
            return;
        }

        MinecraftServer server = level.getServer();

        for (ServerLevel serverLevel : server.getAllLevels()) {
            for (Entity entity : serverLevel.getAllEntities()) {
                if (entity instanceof CactuarSpiritEntity cactuarSpirit) {
                    if (ownerUUID.equals(cactuarSpirit.getOwnerUUID())) {
                        cactuarSpirit.discard();
                    }
                }
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

    private PlayState mainPredicate(AnimationState<CactuarSpiritEntity> state) {
        if (this.getCactuarActionTicks() > 0) {
            if (this.getCactuarAction() == ACTION_KICK) {
                setMainAnimation(state, "kick", KICK_ANIM);
                return PlayState.CONTINUE;
            }

            if (this.getCactuarAction() == ACTION_NEEDLES) {
                setMainAnimation(state, "needles", NEEDLES_ANIM);
                return PlayState.CONTINUE;
            }
        }

        setMainAnimation(state, "idle", IDLE_ANIM);
        return PlayState.CONTINUE;
    }

    private void setMainAnimation(
            AnimationState<CactuarSpiritEntity> state,
            String name,
            RawAnimation animation
    ) {
        if (name.equals(this.currentMainAnimation)) {
            return;
        }

        this.currentMainAnimation = name;
        state.getController().forceAnimationReset();
        state.getController().setAnimation(animation);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    private boolean isCactuarSpiritOwner(Player player) {
        if (player == null) {
            return false;
        }

        if (this.getOwnerUUID() == null) {
            return false;
        }

        return this.getOwnerUUID().equals(player.getUUID());
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack heldStack = player.getItemInHand(hand);

        int feedExp = getCactuarFeedExp(heldStack);

        if (feedExp <= 0) {
            return super.mobInteract(player, hand);
        }

        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.SUCCESS;
        }

        if (!isCactuarSpiritOwner(serverPlayer)) {
            serverPlayer.displayClientMessage(
                    Component.literal("This Cactuar Spirit does not belong to you.")
                            .withStyle(ChatFormatting.RED),
                    true
            );

            return InteractionResult.CONSUME;
        }

        if (this.cactusFeedCooldown > 0) {
            return InteractionResult.CONSUME;
        }

        if (!serverPlayer.getAbilities().instabuild) {
            heldStack.shrink(1);
        }

        this.cactusFeedCooldown = CACTUS_FEED_COOLDOWN_TICKS;

        DreamEaterExpHandler.giveDreamEaterExp(
                serverPlayer,
                GlobalDataRM.DREAM_EATER_CACTUAR,
                feedExp,
                this
        );

        return InteractionResult.CONSUME;
    }

    private int getCactuarFeedExp(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return 0;
        }

        if (stack.is(Items.CACTUS)) {
            return CACTUS_FEED_EXP;
        }

        if (stack.is(ModItemsRM.cactuarNeedle.get())) {
            return CACTUAR_NEEDLE_FEED_EXP;
        }

        return 0;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSoundsRM.CACTUAR_ALIVE.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSoundsRM.CACTUAR_DEATH.get();
    }


}