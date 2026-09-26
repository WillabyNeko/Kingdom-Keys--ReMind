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
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import online.kingdomkeys.kingdomkeys.ability.ModAbilities;
import online.kingdomkeys.kingdomkeys.client.sound.ModSounds;
import online.kingdomkeys.kingdomkeys.data.PlayerData;
import online.kingdomkeys.kingdomkeys.effects.ModMobEffects;
import online.kingdomkeys.kingdomkeys.entity.magic.BalloonEntity;
import online.kingdomkeys.kingdomkeys.entity.magic.BalloongaEntity;
import online.kingdomkeys.kingdomkeys.lib.Strings;
import online.kingdomkeys.kingdomkeys.util.Utils;
import online.remind.remind.KingdomKeysReMind;
import online.remind.remind.capabilities.GlobalDataRM;
import online.remind.remind.capabilities.ModDataRM;
import online.remind.remind.client.sound.ModSoundsRM;
import online.remind.remind.dreameater.DreamEaterExpHandler;
import online.remind.remind.dreameater.DreamEaterPetHelper;
import online.remind.remind.effect.ModMobEffectsRM;
import online.remind.remind.entity.ModEntitiesRM;
import online.remind.remind.network.PacketHandlerRM;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;
import online.remind.remind.dreameater.DreamEater;
import online.remind.remind.dreameater.ModDreamEaters;
import online.remind.remind.lib.StringsRM;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class MeowWowEntity extends PathfinderMob implements GeoEntity {

    public static final int VARIANT_NORMAL = 0;
    public static final int VARIANT_ORG = 1;

    private static final EntityDataAccessor<Integer> VARIANT =
            SynchedEntityData.defineId(MeowWowEntity.class, EntityDataSerializers.INT);

    private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("animation.meow_wow.idle");
    private static final RawAnimation WALK_ANIM = RawAnimation.begin().thenLoop("animation.meow_wow.walk");
    private static final RawAnimation ATTACK_ANIM = RawAnimation.begin().thenPlay("animation.meow_wow.attack");

    private static final EntityDataAccessor<Optional<UUID>> OWNER_UUID =
            SynchedEntityData.defineId(
                    MeowWowEntity.class,
                    EntityDataSerializers.OPTIONAL_UUID
            );

    private static final EntityDataAccessor<Integer> ATTACK_ANIM_TICKS =
            SynchedEntityData.defineId(
                    MeowWowEntity.class,
                    EntityDataSerializers.INT
            );

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private int attackCooldown = 0;
    private int cureCooldown = 0;
    private int balloonCooldown = 0;
    private int slowCooldown = 0;
    private int castCooldown = 0;
    private float mpHasteMult = 0F;
    private int ownerMissingTicks = 0;

    private static final int MEOW_WOW_FEED_COOLDOWN_TICKS = 10;
    private static final int COD_FEED_EXP = 12;
    private static final int SALMON_FEED_EXP = 15;

    private int meowWowFeedCooldown = 0;

    private static final ResourceLocation MAGIC_BALLOON =
            ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "magic_balloon");

    private static final ResourceLocation MAGIC_SLOW =
            ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID, "magic_slow");

    private LivingEntity hornDiveTarget = null;
    private int hornDiveTicks = 0;
    private boolean hornDiveHit = false;

    public MeowWowEntity(EntityType<? extends MeowWowEntity> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 0;
    }

    public MeowWowEntity(Level level, Player owner) {
        this(ModEntitiesRM.TYPE_MEOW_WOW.get(), level);
        this.setOwnerUUID(owner.getUUID());
        this.moveTo(owner.getX(), owner.getY(), owner.getZ(), owner.getYRot(), 0.0F);
        this.applyOwnerScaling(owner);
        this.updateVariantFromOwner(owner);
        this.setHealth(this.getMaxHealth());
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 36.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.34D)
                .add(Attributes.ATTACK_DAMAGE, 8.4D)
                .add(Attributes.ARMOR, 6.6D)
                .add(Attributes.FOLLOW_RANGE, 24.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.2D);
    }
    public static MeowWowEntity summon(Level level, Player owner) {
        MeowWowEntity meowWow = ModEntitiesRM.TYPE_MEOW_WOW.get().create(level);

        if (meowWow == null) {
            return null;
        }

        meowWow.setOwnerUUID(owner.getUUID());
        meowWow.moveTo(owner.getX(), owner.getY(), owner.getZ(), owner.getYRot(), 0.0F);
        meowWow.applyOwnerScaling(owner);
        meowWow.updateVariantFromOwner(owner);
        meowWow.setHealth(meowWow.getMaxHealth());

        if (!level.isClientSide) {
            level.addFreshEntity(meowWow);
        }

        return meowWow;
    }

    public static void removeExistingMeowWow(ServerLevel level, UUID ownerUUID) {
        if (level == null || ownerUUID == null || level.getServer() == null) {
            return;
        }

        for (ServerLevel serverLevel : level.getServer().getAllLevels()) {
            for (Entity entity : serverLevel.getAllEntities()) {
                if (entity instanceof MeowWowEntity meowWow
                        && ownerUUID.equals(meowWow.getOwnerUUID())) {
                    meowWow.discard();
                }
            }
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(OWNER_UUID, Optional.empty());
        builder.define(ATTACK_ANIM_TICKS, 0);
        builder.define(VARIANT, VARIANT_NORMAL);
    }

    public int getVariant() {
        return this.entityData.get(VARIANT);
    }

    public void setVariant(int variant) {
        this.entityData.set(VARIANT, variant);
    }

    private void updateVariantFromOwner(LivingEntity owner) {
        if (!(owner instanceof Player player)) {
            this.setVariant(VARIANT_NORMAL);
            return;
        }

        PlayerData playerData = PlayerData.get(player);

        if (playerData != null && playerData.getAlignment() != Utils.OrgMember.NONE) {
            this.setVariant(VARIANT_ORG);
        } else {
            this.setVariant(VARIANT_NORMAL);
        }
    }

    private float getMeowWowMagic(PlayerData ownerData) {
        int level = Math.max(1, ownerData.getLevel());
        return (float) getMeowWowStatsForLevel(level).magic;
    }


    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new FollowOwnerGoal(this, 1.25D, 4.0F, 2.0F));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.25D, true));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.85D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
    }
    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack heldStack = player.getItemInHand(hand);

        int feedExp = getMeowWowFeedExp(heldStack);

        if (feedExp > 0) {
            return feedMeowWow(player, heldStack, feedExp);
        }

        InteractionResult petResult = DreamEaterPetHelper.tryPetDreamEater(
                this,
                player,
                hand,
                this.getOwnerUUID(),
                "Meow Wow"
        );

        if (petResult != InteractionResult.PASS) {
            return petResult;
        }

        return super.mobInteract(player, hand);
    }

    private InteractionResult feedMeowWow(Player player, ItemStack heldStack, int feedExp) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.SUCCESS;
        }

        if (!isMeowWowOwner(serverPlayer)) {
            serverPlayer.displayClientMessage(
                    Component.literal("This Meow Wow does not belong to you.")
                            .withStyle(ChatFormatting.RED),
                    true
            );

            return InteractionResult.CONSUME;
        }

        if (this.meowWowFeedCooldown > 0) {
            return InteractionResult.CONSUME;
        }

        if (!serverPlayer.getAbilities().instabuild) {
            heldStack.shrink(1);
        }

        this.meowWowFeedCooldown = MEOW_WOW_FEED_COOLDOWN_TICKS;

        DreamEaterExpHandler.giveDreamEaterExp(
                serverPlayer,
                GlobalDataRM.DREAM_EATER_MEOW_WOW,
                feedExp,
                this
        );

        return InteractionResult.CONSUME;
    }

    private int getMeowWowFeedExp(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return 0;
        }

        if (stack.is(Items.COD)) {
            return COD_FEED_EXP;
        }

        if (stack.is(Items.SALMON)) {
            return SALMON_FEED_EXP;
        }

        return 0;
    }

    private boolean isMeowWowOwner(Player player) {
        if (player == null) {
            return false;
        }

        if (this.getOwnerUUID() == null) {
            return false;
        }

        return this.getOwnerUUID().equals(player.getUUID());
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide) {
            return;
        }

        UUID ownerId = this.getOwnerUUID();

        // No owner UUID = invalid summon
        if (ownerId == null) {
            this.discard();
            return;
        }

        LivingEntity ownerLiving = this.getOwnerLiving();

        // Large teleports/dimension changes are handled by DreamEaterFollowManager.
        // Do not clear persistent summon data here.
        if (!(ownerLiving instanceof Player owner) || owner.level() != this.level()) {
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

        // Owner died = clear summon data and despawn
        if (owner.isDeadOrDying()) {
            data.setHasDreamEaterSummoned(false);
            data.setDreamEaterUUID(null);
            PacketHandlerRM.syncGlobalToAllAround(owner, data);
            this.discard();
            return;
        }

        // If Meow Wow itself is dead, clear the owner's summon data.
        if (!this.isAlive()) {
            data.setHasDreamEaterSummoned(false);
            data.setDreamEaterUUID(null);
            PacketHandlerRM.syncGlobalToAllAround(owner, data);
            return;
        }

        // Despawn if the player's selected Dream Eater is no longer Meow Wow.
        if (!this.isSelectedDreamEaterMeowWow(data)) {
            data.setHasDreamEaterSummoned(false);
            data.setDreamEaterUUID(null);
            PacketHandlerRM.syncGlobalToAllAround(owner, data);
            this.discard();
            return;
        }

        if (this.attackCooldown > 0) {
            this.attackCooldown--;
        }

        if (this.meowWowFeedCooldown > 0) {
            this.meowWowFeedCooldown--;
        }

        if (this.getAttackAnimTicks() > 0) {
            this.setAttackAnimTicks(this.getAttackAnimTicks() - 1);
        }

        if (this.hornDiveTicks > 0) {
            this.hornDiveTicks--;
            this.tryFinishHornDiveHit();
        }

        // Same role as Chirithy's updateStatsFromOwner(), but for Meow Wow.
        if (this.tickCount % 40 == 0) {
            this.applyOwnerScaling(owner);
            this.updateVariantFromOwner(owner);
        }

        this.updateCombatTarget();
        this.castSupportMagic();
        this.tryStartHornDive();
    }

    private boolean isSelectedDreamEaterMeowWow(GlobalDataRM data) {
        String dreamEaterRL = data.getDreamEaterRL();

        if (dreamEaterRL == null || dreamEaterRL.isEmpty()) {
            return false;
        }

        DreamEater dreamEater = ModDreamEaters.registry.get(ResourceLocation.parse(dreamEaterRL));

        if (dreamEater == null) {
            return false;
        }

        return StringsRM.meowWow.equals(dreamEater.getName());
    }

    private void handleOwnerCleanupAndScaling() {
        if (this.tickCount % 40 != 0) {
            return;
        }

        UUID ownerUUID = this.getOwnerUUID();

        if (ownerUUID == null) {
            return;
        }

        LivingEntity owner = this.getOwnerLiving();

        if (owner == null || !owner.isAlive() || owner.level() != this.level()) {
            this.ownerMissingTicks += 40;

            if (this.ownerMissingTicks >= 20 * 30) {
                this.discard();
            }

            return;
        }

        this.ownerMissingTicks = 0;
        this.applyOwnerScaling(owner);
        this.updateVariantFromOwner(owner);
    }

    private void updateCombatTarget() {
        if (this.tickCount % 10 != 0) {
            return;
        }

        LivingEntity owner = this.getOwnerLivingSameLevel();

        if (owner != null) {
            LivingEntity ownerAttacked = owner.getLastHurtMob();

            if (this.isValidSpiritTarget(ownerAttacked) && owner.distanceToSqr(ownerAttacked) <= 32.0D * 32.0D) {
                this.setTarget(ownerAttacked);
                return;
            }

            LivingEntity ownerAttacker = owner.getLastHurtByMob();

            if (this.isValidSpiritTarget(ownerAttacker) && owner.distanceToSqr(ownerAttacker) <= 32.0D * 32.0D) {
                this.setTarget(ownerAttacker);
                return;
            }
        }

        LivingEntity currentTarget = this.getTarget();

        if (this.isValidSpiritTarget(currentTarget)) {
            return;
        }

        List<Monster> nearbyMonsters = this.level().getEntitiesOfClass(
                Monster.class,
                this.getBoundingBox().inflate(12.0D),
                this::isValidSpiritTarget
        );

        if (!nearbyMonsters.isEmpty()) {
            nearbyMonsters.sort(Comparator.comparingDouble(this::distanceToSqr));
            this.setTarget(nearbyMonsters.getFirst());
        } else {
            this.setTarget(null);
        }
    }

    private void castSupportMagic() {
        LivingEntity ownerLiving = this.getOwnerLivingSameLevel();

        if (!(ownerLiving instanceof Player owner) || !owner.isAlive()) {
            return;
        }

        PlayerData ownerData = PlayerData.get(owner);

        if (ownerData == null) {
            return;
        }

        this.updateMpHasteMult(ownerData);
        this.tickSupportCooldowns();

        if (this.castCooldown > 0) {
            this.castCooldown--;
            return;
        }

        int ownerLevel = Math.max(1, ownerData.getLevel());

        // Cure / Cura / Curaga based on owner's level.
        if (this.cureCooldown <= 0 && (owner.isHurt() || owner.hasEffect(ModMobEffects.KO))) {
            this.castCureOnOwner(owner, ownerData, this.getCureTierFromOwnerLevel(ownerLevel));
            return;
        }

        if (this.cureCooldown <= 0 && this.getHealth() < this.getMaxHealth() && !owner.isHurt()) {
            this.castCureOnSelf(owner, ownerData, this.getCureTierFromOwnerLevel(ownerLevel));
            return;
        }

        LivingEntity target = this.getSupportMagicTarget(owner);

        if (target == null) {
            return;
        }

        // Slow first, but only if the main target is not already slowed.
        if (this.slowCooldown <= 0 && ownerLevel >= 1 && !target.hasEffect(ModMobEffectsRM.SLOW_RM)) {
            this.castSlowOnTarget(owner, ownerData, target, this.getSlowTierFromOwnerLevel(ownerLevel));
            return;
        }

        // Base-KK Balloon / Balloonra / Balloonga.
        if (this.balloonCooldown <= 0 && ownerLevel >= 1) {
            this.castBalloonOnTarget(owner, ownerData, target, this.getBalloonTierFromOwnerLevel(ownerLevel));
        }
    }



    private void updateMpHasteMult(PlayerData ownerData) {
        this.mpHasteMult = 0F;

        if (ownerData.isAbilityEquipped(ModAbilities.MP_HASTE)
                || ownerData.isAbilityEquipped(ModAbilities.MP_HASTERA)
                || ownerData.isAbilityEquipped(ModAbilities.MP_HASTEGA)) {

            int mpHastes = ownerData.getNumberOfAbilitiesEquipped(ModAbilities.MP_HASTE);
            int mpHasteras = ownerData.getNumberOfAbilitiesEquipped(ModAbilities.MP_HASTERA);
            int mpHastegas = ownerData.getNumberOfAbilitiesEquipped(ModAbilities.MP_HASTEGA);

            this.mpHasteMult = (mpHastes * 0.15F)
                    + (mpHasteras * 0.3F)
                    + (mpHastegas * 0.45F);
        }
    }

    private void tickSupportCooldowns() {
        int reduction = 1 + Math.max(0, (int) this.mpHasteMult);

        if (this.cureCooldown > 0) {
            this.cureCooldown = Math.max(0, this.cureCooldown - reduction);
        }

        if (this.balloonCooldown > 0) {
            this.balloonCooldown = Math.max(0, this.balloonCooldown - reduction);
        }

        if (this.slowCooldown > 0) {
            this.slowCooldown = Math.max(0, this.slowCooldown - reduction);
        }
    }

    private void castSlowOnTarget(Player owner, PlayerData ownerData, LivingEntity target, int slowLevel) {
        float radius = 3.0F + slowLevel;
        int time = (int) (ownerData.getMaxMP() * ((slowLevel * 0.75F) + 5) + 5);

        boolean hitSomething = false;

        List<Entity> list = this.level().getEntities(
                this,
                target.getBoundingBox().inflate(radius, radius, radius)
        );

        for (Entity entity : list) {
            if (!(entity instanceof LivingEntity livingTarget)) {
                continue;
            }

            if (!this.isValidSpiritTarget(livingTarget)) {
                continue;
            }

            livingTarget.addEffect(new MobEffectInstance(
                    ModMobEffectsRM.SLOW_RM,
                    time,
                    slowLevel,
                    false,
                    false,
                    false
            ));

            hitSomething = true;
        }

        if (this.isValidSpiritTarget(target)) {
            target.addEffect(new MobEffectInstance(
                    ModMobEffectsRM.SLOW_RM,
                    time,
                    slowLevel,
                    false,
                    false,
                    false
            ));

            hitSomething = true;
        }

        if (this.level() instanceof ServerLevel serverLevel) {
            int particleCount = 40;

            for (int i = 0; i < particleCount; i++) {
                double angle = 2D * Math.PI * i / particleCount;
                double xOffset = Math.cos(angle) * radius;
                double zOffset = Math.sin(angle) * radius;
                double yOffset = 1.0D + this.getRandom().nextDouble() * 0.5D;

                serverLevel.sendParticles(
                        ParticleTypes.SOUL,
                        target.getX() + xOffset,
                        target.getY() + yOffset,
                        target.getZ() + zOffset,
                        0,
                        0.02D,
                        0D,
                        0D,
                        1D
                );

                serverLevel.sendParticles(
                        ParticleTypes.EFFECT,
                        target.getX() + xOffset,
                        target.getY() + yOffset,
                        target.getZ() + zOffset,
                        0,
                        0.02D,
                        0D,
                        0D,
                        0D
                );
            }
        }

        this.level().playSound(
                null,
                target.blockPosition(),
                ModSoundsRM.SLOW.get(),
                SoundSource.PLAYERS,
                1F,
                1F
        );

        this.setAttackAnimTicks(24);
        this.slowCooldown = hitSomething ? 220 : 40;
        this.castCooldown = 20 * 8;
    }

    private int getCureTierFromOwnerLevel(int ownerLevel) {
        if (ownerLevel >= 20) {
            return 2; // Curaga
        }

        if (ownerLevel >= 10) {
            return 1; // Cura
        }

        return 0; // Cure
    }

    private int getBalloonTierFromOwnerLevel(int ownerLevel) {
        if (ownerLevel >= 25) {
            return 2; // Balloonga
        }

        if (ownerLevel >= 16) {
            return 1; // Balloonra
        }

        return 0; // Balloon
    }

    private int getSlowTierFromOwnerLevel(int ownerLevel) {
        return 0; // Slow only for now.
    }

    private LivingEntity getSupportMagicTarget(Player owner) {
        LivingEntity currentTarget = this.getTarget();

        if (this.isValidSpiritTarget(currentTarget)) {
            return currentTarget;
        }

        List<Monster> nearbyMonsters = this.level().getEntitiesOfClass(
                Monster.class,
                owner.getBoundingBox().inflate(14.0D),
                this::isValidSpiritTarget
        );

        if (nearbyMonsters.isEmpty()) {
            return null;
        }

        nearbyMonsters.sort(Comparator.comparingDouble(owner::distanceToSqr));
        return nearbyMonsters.getFirst();
    }



    private void castCureOnOwner(Player owner, PlayerData ownerData, int cureTier) {
        float mag = getMeowWowMagic(ownerData);
        float healAmount;
        String spellName;

        switch (cureTier) {
            case 2:
                healAmount = mag * 1.15F;
                spellName = "Curaga";
                owner.level().playSound(null, owner.getX(), owner.getY(), owner.getZ(), ModSounds.curaga.get(), SoundSource.PLAYERS, 1F, 1F);
                break;

            case 1:
                healAmount = mag * 0.95F;
                spellName = "Cura";
                owner.level().playSound(null, owner.getX(), owner.getY(), owner.getZ(), ModSounds.cura.get(), SoundSource.PLAYERS, 1F, 1F);
                break;

            default:
                healAmount = mag * 0.75f;
                spellName = "Cure";
                owner.level().playSound(null, owner.getX(), owner.getY(), owner.getZ(), ModSounds.cure.get(), SoundSource.PLAYERS, 1F, 1F);
                break;
        }

        if (owner.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                    ParticleTypes.HAPPY_VILLAGER,
                    owner.getX(),
                    owner.getY() + 2.3D,
                    owner.getZ(),
                    5,
                    0D,
                    0D,
                    0D,
                    0D
            );
        }

        owner.heal(healAmount);

        if (owner.hasEffect(ModMobEffects.KO)) {
            owner.removeEffect(ModMobEffects.KO);
        }

        this.setAttackAnimTicks(24);
        this.cureCooldown = 20 * 20;
        this.castCooldown = 20 * 8;
    }

    private void castCureOnSelf(Player owner, PlayerData ownerData, int cureTier) {
        float mag = getMeowWowMagic(ownerData);
        float healAmount;

        switch (cureTier) {
            case 2:
                healAmount = mag * 1.15F;
                this.level().playSound(null, this.getX(), this.getY(), this.getZ(), ModSounds.curaga.get(), SoundSource.NEUTRAL, 1F, 1F);
                break;

            case 1:
                healAmount = mag * 0.95F;
                this.level().playSound(null, this.getX(), this.getY(), this.getZ(), ModSounds.cura.get(), SoundSource.NEUTRAL, 1F, 1F);
                break;

            default:
                healAmount = mag * 0.75F;
                this.level().playSound(null, this.getX(), this.getY(), this.getZ(), ModSounds.cure.get(), SoundSource.NEUTRAL, 1F, 1F);
                break;
        }

        this.heal(healAmount);

        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                    ParticleTypes.HAPPY_VILLAGER,
                    this.getX(),
                    this.getY() + 2.3D,
                    this.getZ(),
                    5,
                    0D,
                    0D,
                    0D,
                    0D
            );
        }

        this.setAttackAnimTicks(24);
        this.cureCooldown = 20 *20;
        this.castCooldown = 20 * 8;
    }
    private void castBalloonOnTarget(Player owner, PlayerData ownerData, LivingEntity target, int balloonTier) {
        float dmgMult = 1.0F + ownerData.getNumberOfAbilitiesEquipped(ModAbilities.WATER_BOOST) * 0.2F;
        String spellName;

        this.getLookControl().setLookAt(target, 30.0F, 30.0F);
        this.swing(InteractionHand.MAIN_HAND);

        switch (balloonTier) {
            case 2:
                spellName = "Balloonga";
                this.spawnBalloongaProjectile(owner, target, dmgMult, 0.0D);
                break;

            case 1:
                spellName = "Balloonra";
                for (double offset = -40.0D; offset <= 40.0D; offset += 20.0D) {
                    this.spawnBalloonProjectile(owner, target, dmgMult, offset);
                }
                break;

            default:
                spellName = "Balloon";
                for (double offset = -25.0D; offset <= 25.0D; offset += 25.0D) {
                    this.spawnBalloonProjectile(owner, target, dmgMult, offset);
                }
                break;
        }

        this.level().playSound(
                null,
                this.getX(),
                this.getY(),
                this.getZ(),
                ModSounds.balloon.get(),
                SoundSource.PLAYERS,
                1F,
                1F
        );

        this.setAttackAnimTicks(24);
        this.balloonCooldown = 20 * 12;
        this.castCooldown = 20 * 8;
    }

    private void spawnBalloonProjectile(Player owner, LivingEntity target, float dmgMult, double yawOffsetDegrees) {
        ThrowableProjectile balloon = new BalloonEntity(this.level(), owner, dmgMult);

        this.positionAndShootProjectileAtTarget(balloon, target, yawOffsetDegrees);

        this.level().addFreshEntity(balloon);
    }

    private void spawnBalloongaProjectile(Player owner, LivingEntity target, float dmgMult, double yawOffsetDegrees) {
        ThrowableProjectile balloonga = new BalloongaEntity(this.level(), owner, dmgMult);

        this.positionAndShootProjectileAtTarget(balloonga, target, yawOffsetDegrees);

        this.level().addFreshEntity(balloonga);
    }

    private void positionAndShootProjectileAtTarget(ThrowableProjectile projectile, LivingEntity target, double yawOffsetDegrees) {
        Vec3 start = new Vec3(
                this.getX(),
                this.getEyeY() - 0.1D,
                this.getZ()
        );

        Vec3 end = new Vec3(
                target.getX(),
                target.getEyeY(),
                target.getZ()
        );

        Vec3 direction = end.subtract(start);

        if (direction.lengthSqr() <= 0.0001D) {
            direction = this.getLookAngle();
        } else {
            direction = direction.normalize();
        }

        direction = this.rotateVectorY(direction, yawOffsetDegrees).normalize();

        projectile.setPos(start.x, start.y, start.z);
        projectile.shoot(direction.x, direction.y, direction.z, 0.5F, 0.0F);
    }

    private Vec3 rotateVectorY(Vec3 vector, double degrees) {
        double radians = Math.toRadians(degrees);
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);

        return new Vec3(
                vector.x * cos - vector.z * sin,
                vector.y,
                vector.x * sin + vector.z * cos
        );
    }

    private void tryStartHornDive() {
        if (this.attackCooldown > 0 || this.hornDiveTicks > 0 || !this.onGround()) {
            return;
        }

        LivingEntity target = this.getTarget();

        if (!this.isValidSpiritTarget(target)) {
            return;
        }

        double distanceSqr = this.distanceToSqr(target);

        if (distanceSqr < 2.75D * 2.75D || distanceSqr > 8.0D * 8.0D) {
            return;
        }

        if (!this.hasLineOfSight(target)) {
            return;
        }

        this.getNavigation().stop();
        this.getLookControl().setLookAt(target, 30.0F, 30.0F);

        Vec3 direction = target.position().subtract(this.position());

        if (direction.lengthSqr() <= 0.0001D) {
            return;
        }

        direction = direction.normalize();

        this.setDeltaMovement(direction.x * 0.85D, 0.42D, direction.z * 0.85D);
        this.hasImpulse = true;

        this.setAttackAnimTicks(24);
        this.attackCooldown = 20 * 3;
        this.hornDiveTicks = 18;
        this.hornDiveTarget = target;
        this.hornDiveHit = false;
    }

    private void tryFinishHornDiveHit() {
        if (this.hornDiveHit || !this.isValidSpiritTarget(this.hornDiveTarget)) {
            return;
        }

        if (this.distanceToSqr(this.hornDiveTarget) > 2.2D * 2.2D) {
            return;
        }

        this.dealSpiritDamage(this.hornDiveTarget, 1.35F);
        this.hornDiveHit = true;
    }

    @Override
    public boolean doHurtTarget(Entity entity) {
        if (!(entity instanceof LivingEntity target)) {
            return false;
        }

        if (!this.isValidSpiritTarget(target)) {
            return false;
        }

        if (this.attackCooldown > 0) {
            return false;
        }

        this.getNavigation().stop();
        this.getLookControl().setLookAt(target, 30.0F, 30.0F);

        this.setAttackAnimTicks(18);
        this.attackCooldown = 24;

        this.dealSpiritDamage(target, 1.0F);

        Vec3 recoil = this.position().subtract(target.position());

        if (recoil.lengthSqr() > 0.0001D) {
            recoil = recoil.normalize();
            this.setDeltaMovement(this.getDeltaMovement().add(recoil.x * 0.25D, 0.28D, recoil.z * 0.25D));
            this.hasImpulse = true;
        }

        return true;
    }

    private void dealSpiritDamage(LivingEntity target, float multiplier) {
        float damage = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE) * multiplier;

        target.hurt(this.damageSources().mobAttack(this), damage);

        Vec3 knockback = new Vec3(
                target.getX() - this.getX(),
                0.0D,
                target.getZ() - this.getZ()
        );

        if (knockback.lengthSqr() > 0.0001D) {
            knockback = knockback.normalize().scale(0.45D * multiplier);
            target.push(knockback.x, 0.15D, knockback.z);
        }
    }

    private void applyOwnerScaling(LivingEntity owner) {
        int level = 1;

        if (owner instanceof Player player) {
            GlobalDataRM globalData = ModDataRM.getGlobal(player);

            if (globalData != null) {
                level = globalData.getDreamEaterLevel(GlobalDataRM.DREAM_EATER_MEOW_WOW);
            }
        }

        MeowWowStats stats = getMeowWowStatsForLevel(level);

        if (this.getAttribute(Attributes.MAX_HEALTH) != null) {
            this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(stats.hp);
        }

        if (this.getAttribute(Attributes.ATTACK_DAMAGE) != null) {
            this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(stats.strength);
        }

        if (this.getAttribute(Attributes.ARMOR) != null) {
            this.getAttribute(Attributes.ARMOR).setBaseValue(stats.defense);
        }

        if (this.getAttribute(Attributes.MOVEMENT_SPEED) != null) {
            this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.34D);
        }

        if (this.getHealth() > this.getMaxHealth()) {
            this.setHealth(this.getMaxHealth());
        }
    }

    private static MeowWowStats getMeowWowStatsForLevel(int level) {
        level = Mth.clamp(level, 1, 100);

        // Exact known in-game table values
        if (level < 3) {
            return new MeowWowStats(36.0D, 8.4D, 11.1D, 6.6D);
        }

        if (level < 6) {
            return new MeowWowStats(37.0D, 12.0D, 16.0D, 6.0D);
        }

        if (level < 8) {
            return new MeowWowStats(46.0D, 15.0D, 20.0D, 8.0D);
        }

        if (level < 10) {
            return new MeowWowStats(52.0D, 17.0D, 22.0D, 9.0D);
        }

        if (level < 12) {
            return new MeowWowStats(58.0D, 19.0D, 25.0D, 10.0D);
        }

        if (level < 14) {
            return new MeowWowStats(63.0D, 21.0D, 27.0D, 11.0D);
        }

        if (level < 16) {
            return new MeowWowStats(69.0D, 23.0D, 30.0D, 12.0D);
        }

        if (level < 18) {
            return new MeowWowStats(75.0D, 24.0D, 32.0D, 12.0D);
        }

        if (level < 20) {
            return new MeowWowStats(81.0D, 26.0D, 35.0D, 13.0D);
        }

        if (level < 22) {
            return new MeowWowStats(86.0D, 28.0D, 37.0D, 14.0D);
        }

        if (level < 24) {
            return new MeowWowStats(92.0D, 30.0D, 40.0D, 15.0D);
        }

        if (level < 26) {
            return new MeowWowStats(98.0D, 32.0D, 42.0D, 16.0D);
        }

        // Lv 26 base from the table:
        // HP 104 / STR 34 / MAG 45 / DEF 17
        //
        // From Lv 27-100, continue growth in a controlled way.
        int extraLevels = level - 26;

        double hp = 104.0D + (extraLevels * 2.5D);
        double strength = 34.0D + (extraLevels * 0.50D);
        double magic = 45.0D + (extraLevels * 0.65D);
        double defense = 17.0D + (extraLevels * 0.25D);

        return new MeowWowStats(hp, strength, magic, defense);
    }

    private static class MeowWowStats {
        private final double hp;
        private final double strength;
        private final double magic;
        private final double defense;

        private MeowWowStats(double hp, double strength, double magic, double defense) {
            this.hp = hp;
            this.strength = strength;
            this.magic = magic;
            this.defense = defense;
        }
    }

    private boolean isValidSpiritTarget(LivingEntity target) {
        if (target == null || !target.isAlive()) {
            return false;
        }

        if (target == this) {
            return false;
        }

        LivingEntity owner = this.getOwnerLiving();

        if (owner != null && target == owner) {
            return false;
        }

        if (target instanceof Player player && (player.isCreative() || player.isSpectator())) {
            return false;
        }

        if (target instanceof MeowWowEntity otherMeowWow) {
            UUID myOwner = this.getOwnerUUID();
            UUID otherOwner = otherMeowWow.getOwnerUUID();

            if (myOwner != null && myOwner.equals(otherOwner)) {
                return false;
            }
        }

        return !this.isAlliedTo(target);
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        return this.isValidSpiritTarget(target) && super.canAttack(target);
    }

    @Override
    public boolean isAlliedTo(Entity entity) {
        if (super.isAlliedTo(entity)) {
            return true;
        }

        LivingEntity owner = this.getOwnerLiving();

        if (owner != null && entity == owner) {
            return true;
        }

        if (owner instanceof Player ownerPlayer && entity instanceof Player otherPlayer) {
            return ownerPlayer.isAlliedTo(otherPlayer);
        }

        if (entity instanceof MeowWowEntity otherMeowWow) {
            UUID myOwner = this.getOwnerUUID();
            UUID otherOwner = otherMeowWow.getOwnerUUID();

            return myOwner != null && myOwner.equals(otherOwner);
        }

        return false;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        Entity attacker = source.getEntity();

        if (attacker != null && this.isAlliedTo(attacker)) {
            return false;
        }

        return super.hurt(source, amount);
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return this.getOwnerUUID() == null;
    }

    @Override
    public boolean requiresCustomPersistence() {
        return this.getOwnerUUID() != null;
    }

    public UUID getOwnerUUID() {
        return this.entityData.get(OWNER_UUID).orElse(null);
    }

    public void setOwnerUUID(UUID uuid) {
        this.entityData.set(OWNER_UUID, Optional.ofNullable(uuid));
    }

    private int getAttackAnimTicks() {
        return this.entityData.get(ATTACK_ANIM_TICKS);
    }

    private void setAttackAnimTicks(int ticks) {
        this.entityData.set(ATTACK_ANIM_TICKS, Math.max(0, ticks));
    }

    public LivingEntity getOwnerLiving() {
        UUID ownerUUID = this.getOwnerUUID();

        if (ownerUUID == null) {
            return null;
        }

        MinecraftServer server = this.level().getServer();

        if (server != null) {
            ServerPlayer player = server.getPlayerList().getPlayer(ownerUUID);

            if (player != null) {
                return player;
            }
        }

        if (this.level() instanceof ServerLevel serverLevel) {
            Entity entity = serverLevel.getEntity(ownerUUID);

            if (entity instanceof LivingEntity livingEntity) {
                return livingEntity;
            }
        }

        return null;
    }

    public LivingEntity getOwnerLivingSameLevel() {
        LivingEntity owner = this.getOwnerLiving();

        if (owner == null || owner.level() != this.level()) {
            return null;
        }

        return owner;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);

        UUID ownerUUID = this.getOwnerUUID();

        if (ownerUUID != null) {
            tag.putUUID("OwnerUUID", ownerUUID);
        }

        tag.putInt("CureCooldown", this.cureCooldown);
        tag.putInt("CastCooldown", this.castCooldown);
        tag.putInt("BalloonCooldown", this.balloonCooldown);
        tag.putInt("SlowCooldown", this.slowCooldown);
        tag.putInt("Variant", this.getVariant());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);

        if (tag.hasUUID("OwnerUUID")) {
            this.setOwnerUUID(tag.getUUID("OwnerUUID"));
        }

        this.cureCooldown = tag.getInt("CureCooldown");

        if (tag.contains("CastCooldown")) {
            this.castCooldown = tag.getInt("CastCooldown");
        }

        if (tag.contains("BalloonCooldown")) {
            this.balloonCooldown = tag.getInt("BalloonCooldown");
        }

        if (tag.contains("SlowCooldown")) {
            this.slowCooldown = tag.getInt("SlowCooldown");
        }

        if (tag.contains("Variant")) {
            this.setVariant(tag.getInt("Variant"));
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main_controller", 1, state -> {
            if (this.getAttackAnimTicks() > 0) {
                return state.setAndContinue(ATTACK_ANIM);
            }

            if (state.isMoving()) {
                return state.setAndContinue(WALK_ANIM);
            }

            return state.setAndContinue(IDLE_ANIM);
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    private static class FollowOwnerGoal extends Goal {

        private final MeowWowEntity meowWow;
        private final double speedModifier;
        private final float startDistance;
        private final float stopDistance;

        private LivingEntity owner;

        private FollowOwnerGoal(MeowWowEntity meowWow, double speedModifier, float startDistance, float stopDistance) {
            this.meowWow = meowWow;
            this.speedModifier = speedModifier;
            this.startDistance = startDistance;
            this.stopDistance = stopDistance;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            this.owner = this.meowWow.getOwnerLivingSameLevel();

            if (this.owner == null || !this.owner.isAlive()) {
                return false;
            }

            if (this.owner instanceof Player player && player.isSpectator()) {
                return false;
            }

            return this.meowWow.distanceToSqr(this.owner) > this.startDistance * this.startDistance;
        }

        @Override
        public boolean canContinueToUse() {
            if (this.owner == null || !this.owner.isAlive()) {
                return false;
            }

            if (this.owner.level() != this.meowWow.level()) {
                return false;
            }

            return this.meowWow.distanceToSqr(this.owner) > this.stopDistance * this.stopDistance;
        }


        @Override
        public void tick() {
            this.meowWow.getLookControl().setLookAt(
                    this.owner,
                    10.0F,
                    this.meowWow.getMaxHeadXRot()
            );

            this.meowWow.getNavigation().moveTo(
                    this.owner,
                    this.speedModifier
            );
        }

        @Override
        public void stop() {
            this.owner = null;
            this.meowWow.getNavigation().stop();
        }
    }
}