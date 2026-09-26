package online.remind.remind.entity.spirits;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
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
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import online.kingdomkeys.kingdomkeys.ability.ModAbilities;
import online.kingdomkeys.kingdomkeys.client.sound.ModSounds;
import online.kingdomkeys.kingdomkeys.data.PlayerData;
import online.kingdomkeys.kingdomkeys.effects.ModMobEffects;
import online.kingdomkeys.kingdomkeys.network.PacketHandler;
import online.kingdomkeys.kingdomkeys.network.stc.SCAeroSoundPacket;
import online.kingdomkeys.kingdomkeys.world.DialogueHandler;
import online.remind.remind.KingdomKeysReMind;
import online.remind.remind.capabilities.GlobalDataRM;
import online.remind.remind.capabilities.ModDataRM;
import online.remind.remind.client.sound.ModSoundsRM;
import online.remind.remind.config.ModConfigs;
import online.remind.remind.dreameater.DreamEaterExpHandler;
import online.remind.remind.dreameater.DreamEaterPetHelper;
import online.remind.remind.dreameater.ModDreamEaters;
import online.remind.remind.effect.ModMobEffectsRM;
import online.remind.remind.entity.ModEntitiesRM;
import online.remind.remind.entity.spirits.goal.ChirithyGoal;
import online.remind.remind.network.PacketHandlerRM;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

public class ChirithyEntity extends BaseDreamEaterEntity implements GeoEntity {

    private Player owner;
    private UUID ownerUUID;

    private double chirithyHP;
    private double chirithyStrength;
    private double chirithyMagic;
    private double chirithyDefense;

    private int ownerCureCooldown = 0;
    private int selfCureCooldown = 100;
    private int aeroCooldown = 60;
    private int esunaCooldown = 0;
    private int autoLifeCooldown = 60;
    private int castCooldown = 0;

    private int chirithyGiftCooldown = 0;
    private int recentOwnerDamageTicks = 0;

    private float mpHasteMult;

    // Support magic progression is based entirely on Chirithy's own level.
    private static final int LEVEL_CURE = 1;
    private static final int LEVEL_AERO = 5;
    private static final int LEVEL_ESUNA = 10;
    private static final int LEVEL_CURA = 15;
    private static final int LEVEL_AERORA = 25;
    private static final int LEVEL_AUTO_LIFE = 30;
    private static final int LEVEL_CURAGA = 40;
    private static final int LEVEL_AEROGA = 50;

    private static final int CHIRITHY_GIFT_COOLDOWN_TICKS = 10;
    private static final int AMETHYST_GIFT_EXP = 12;
    private static final int GHAST_TEAR_GIFT_EXP = 25;

    public static final int
            IDLE = 0,
            WALK = 1,
            CAST = 2;

    protected static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenPlay("idle");
    protected static final RawAnimation WALK_ANIM = RawAnimation.begin().thenLoop("walk");
    protected static final RawAnimation CAST_ANIM = RawAnimation.begin().thenPlay("cast");

    private static final EntityDataAccessor<Integer> VARIANT =
            SynchedEntityData.defineId(ChirithyEntity.class, EntityDataSerializers.INT);

    public final AnimationState castAnimationState = new AnimationState();

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private static class ChirithyStats {
        final int hp;
        final int strength;
        final int magic;
        final int defense;

        ChirithyStats(int hp, int strength, int magic, int defense) {
            this.hp = hp;
            this.strength = strength;
            this.magic = magic;
            this.defense = defense;
        }
    }

    private static ChirithyStats getChirithyStatsForLevel(int level) {
        level = Math.max(1, Math.min(level, GlobalDataRM.DREAM_EATER_MAX_LEVEL));

        int hp = 22 + (int) Math.round((level - 1) * 1.25D);

        // Chirithy is pure support. STR should stay low.
        int strength = 1;

        if (level >= 50) {
            strength = 2;
        }

        if (level >= 90) {
            strength = 3;
        }

        int magic = 8 + (int) Math.round((level - 1) * 0.55D);
        int defense = 4 + (int) Math.round((level - 1) * 0.35D);

        return new ChirithyStats(hp, strength, magic, defense);
    }

    public ChirithyEntity(EntityType<? extends PathfinderMob> type, Level worldIn) {
        super((EntityType<? extends TamableAnimal>) type, worldIn);
    }

    public ChirithyEntity(Level worldIn, Player owner) {
        this(ModEntitiesRM.TYPE_CHIRITHY.get(), worldIn);

        if (owner != null) {
            this.owner = owner;
            this.setOwnerUUID(owner.getUUID());
            this.setTame(true, true);

            updateStatsFromOwner();

            this.setHealth(this.getMaxHealth());
        }
    }

    private void startCasting() {
        if (!castAnimationState.isStarted()) {
            castAnimationState.start(this.tickCount);
        }
    }

    private void stopCasting() {
        castAnimationState.stop();
    }

    public int getVariant() {
        return this.entityData.get(VARIANT);
    }

    public void setVariant(int variant) {
        this.entityData.set(VARIANT, variant);
    }

    public void updateStatsFromOwner() {
        if (owner == null) {
            return;
        }

        GlobalDataRM globalData = ModDataRM.getGlobal(owner);

        if (globalData == null) {
            return;
        }

        int chirithyLevel = globalData.getDreamEaterLevel(GlobalDataRM.DREAM_EATER_CHIRITHY);
        ChirithyStats stats = getChirithyStatsForLevel(chirithyLevel);

        hp = stats.hp;
        str = stats.strength;
        mag = stats.magic;
        def = stats.defense;

        chirithyHP = stats.hp;
        chirithyStrength = stats.strength;
        chirithyMagic = stats.magic;
        chirithyDefense = stats.defense;

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
            this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.5D);
        }

        if (this.getHealth() > this.getMaxHealth()) {
            this.setHealth(this.getMaxHealth());
        }

        this.setStr(stats.strength);
        this.setMag(stats.magic);
        this.setDef(stats.defense);
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

        Player owner = this.level().getPlayerByUUID(ownerId);

        if (owner == null) {
            this.discard();
            return;
        }

        this.owner = owner;

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
            data.setHasDreamEaterSummoned(false);
            data.setDreamEaterUUID(null);
            PacketHandlerRM.syncGlobalToAllAround(owner, data);
            this.discard();
            return;
        }

        updateStatsFromOwner();

        if (!this.isAlive()) {
            data.setHasDreamEaterSummoned(false);
            data.setDreamEaterUUID(null);
            PacketHandlerRM.syncGlobalToAllAround(owner, data);
            return;
        }

        String selectedDreamEater = data.getDreamEaterRL();
        String chirithyRL = ModDreamEaters.CHIRITHY.get().getRegistryName().toString();

        if (!chirithyRL.equals(selectedDreamEater)) {
            data.setHasDreamEaterSummoned(false);
            data.setDreamEaterUUID(null);
            PacketHandlerRM.syncGlobalToAllAround(owner, data);
            this.discard();
            return;
        }

        if (this.chirithyGiftCooldown > 0) {
            this.chirithyGiftCooldown--;
        }

        if (owner.hurtTime > 0) {
            this.recentOwnerDamageTicks = 20 * 5;
        } else if (this.recentOwnerDamageTicks > 0) {
            this.recentOwnerDamageTicks--;
        }

        castSupportMagic();
    }

    private void castSupportMagic() {
        if (owner == null || !owner.isAlive()) {
            return;
        }

        PlayerData ownerData = PlayerData.get(owner);
        GlobalDataRM globalData = ModDataRM.getGlobal(owner);

        if (ownerData == null || globalData == null) {
            return;
        }

        updateMpHasteMult(ownerData);
        tickSupportCooldowns();

        int chirithyLevel = Math.max(
                LEVEL_CURE,
                globalData.getDreamEaterLevel(GlobalDataRM.DREAM_EATER_CHIRITHY)
        );

        float healthPercent = owner.getHealth() / owner.getMaxHealth();
        boolean ownerKO = owner.hasEffect(ModMobEffects.KO);
        boolean emergency = ownerKO || healthPercent <= 0.25F;

        // Emergency healing can interrupt another support spell's cast delay.
        if (emergency && tryCastCure(chirithyLevel, true)) {
            return;
        }

        if (castCooldown > 0) {
            return;
        }

        // Ailment removal has priority over ordinary healing and buffs.
        if (tryCastEsuna(chirithyLevel)) {
            return;
        }

        if (tryCastCure(chirithyLevel, false)) {
            return;
        }

        if (tryCastAutoLife(chirithyLevel)) {
            return;
        }

        if (tryCastAero(chirithyLevel)) {
            return;
        }

        trySelfHeal();
    }

    private void updateMpHasteMult(PlayerData ownerData) {
        mpHasteMult = 0F;

        if (ownerData == null) {
            return;
        }

        int mpHastes = ownerData.getNumberOfAbilitiesEquipped(ModAbilities.MP_HASTE);
        int mpHasteras = ownerData.getNumberOfAbilitiesEquipped(ModAbilities.MP_HASTERA);
        int mpHastegas = ownerData.getNumberOfAbilitiesEquipped(ModAbilities.MP_HASTEGA);

        mpHasteMult = (mpHastes * 0.15F) + (mpHasteras * 0.25F) + (mpHastegas * 0.35F);
    }

    private void tickSupportCooldowns() {
        int reduction = 1 + Math.max(0, (int) (mpHasteMult * 2F));

        if (ownerCureCooldown > 0) {
            ownerCureCooldown = Math.max(0, ownerCureCooldown - reduction);
        }

        if (selfCureCooldown > 0) {
            selfCureCooldown = Math.max(0, selfCureCooldown - reduction);
        }

        if (aeroCooldown > 0) {
            aeroCooldown = Math.max(0, aeroCooldown - reduction);
        }

        if (esunaCooldown > 0) {
            esunaCooldown = Math.max(0, esunaCooldown - reduction);
        }

        if (autoLifeCooldown > 0) {
            autoLifeCooldown = Math.max(0, autoLifeCooldown - reduction);
        }

        if (castCooldown > 0) {
            castCooldown = Math.max(0, castCooldown - 1);
        }
    }

    private int getCureTier(int chirithyLevel) {
        if (chirithyLevel >= LEVEL_CURAGA) {
            return 2;
        }

        if (chirithyLevel >= LEVEL_CURA) {
            return 1;
        }

        return 0;
    }

    private int getAeroTier(int chirithyLevel) {
        if (chirithyLevel >= LEVEL_AEROGA) {
            return 2;
        }

        if (chirithyLevel >= LEVEL_AERORA) {
            return 1;
        }

        return 0;
    }

    private boolean tryCastCure(
            int chirithyLevel,
            boolean emergencyOnly
    ) {
        if (owner == null || !owner.isAlive()) {
            return false;
        }

        if (chirithyLevel < LEVEL_CURE || ownerCureCooldown > 0) {
            return false;
        }

        // Normal Cure respects the global cast delay; emergency Cure may interrupt it.
        if (!emergencyOnly && castCooldown > 0) {
            return false;
        }

        float healthPercent = owner.getHealth() / owner.getMaxHealth();
        boolean ownerKO = owner.hasEffect(ModMobEffects.KO);
        boolean ownerCritical = healthPercent <= 0.25F;
        boolean ownerNeedsNormalHeal = healthPercent <= 0.75F;

        if (emergencyOnly) {
            if (!ownerKO && !ownerCritical) {
                return false;
            }
        } else if (ownerKO || ownerCritical || !ownerNeedsNormalHeal) {
            return false;
        }

        int cureTier = getCureTier(chirithyLevel);
        float healAmount;
        String spellName;

        switch (cureTier) {
            case 2:
                healAmount = (float) (chirithyMagic * 1.25F);
                spellName = "Curaga";

                owner.level().playSound(
                        null,
                        owner.getX(),
                        owner.getY(),
                        owner.getZ(),
                        ModSounds.curaga.get(),
                        SoundSource.PLAYERS,
                        1F,
                        1F
                );
                break;

            case 1:
                healAmount = (float) (chirithyMagic * 1.1F);
                spellName = "Cura";

                owner.level().playSound(
                        null,
                        owner.getX(),
                        owner.getY(),
                        owner.getZ(),
                        ModSounds.cura.get(),
                        SoundSource.PLAYERS,
                        1F,
                        1F
                );
                break;

            case 0:
            default:
                healAmount = (float) chirithyMagic;
                spellName = "Cure";

                owner.level().playSound(
                        null,
                        owner.getX(),
                        owner.getY(),
                        owner.getZ(),
                        ModSounds.cure.get(),
                        SoundSource.PLAYERS,
                        1F,
                        1F
                );
                break;
        }

        owner.heal(healAmount);

        if (owner.hasEffect(ModMobEffects.KO)) {
            owner.removeEffect(ModMobEffects.KO);
        }

        if (owner.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                    ParticleTypes.HAPPY_VILLAGER,
                    owner.getX(),
                    owner.getY() + 2.3D,
                    owner.getZ(),
                    8,
                    0.25D,
                    0.25D,
                    0.25D,
                    0.02D
            );
        }

        if (ModConfigs.chirithySpellFeedback) {
            owner.sendSystemMessage(Component.literal("<Chirithy> " + spellName + "!"));
        }
        this.startCasting();

        ownerCureCooldown = emergencyOnly ? 20 * 20 : 30 * 20;
        castCooldown = 30;

        return true;
    }

    private boolean tryCastAero(int chirithyLevel) {
        if (chirithyLevel < LEVEL_AERO) {
            return false;
        }

        if (aeroCooldown > 0 || castCooldown > 0) {
            return false;
        }

        // Remember recent damage so Aero is not limited to the short hurtTime window.
        if (recentOwnerDamageTicks <= 0) {
            return false;
        }

        // Do not spend a cast refreshing an Aero that is already active.
        if (owner.hasEffect(ModMobEffects.AERO)) {
            return false;
        }

        int aeroTier = getAeroTier(chirithyLevel);
        int durationTicks;
        String spellName;

        switch (aeroTier) {
            case 2:
                durationTicks = 20 * 45;
                spellName = "Aeroga";
                break;

            case 1:
                durationTicks = 20 * 35;
                spellName = "Aerora";
                break;

            case 0:
            default:
                durationTicks = 20 * 25;
                spellName = "Aero";
                break;
        }

        owner.addEffect(new MobEffectInstance(
                ModMobEffects.AERO,
                durationTicks,
                aeroTier,
                false,
                false,
                true
        ));

        PacketHandler.sendToAll(new SCAeroSoundPacket(owner.getId()));

        owner.level().playSound(
                null,
                owner.getX(),
                owner.getY(),
                owner.getZ(),
                ModSounds.aero1.get(),
                SoundSource.PLAYERS,
                1F,
                1F
        );

        if (ModConfigs.chirithySpellFeedback) {
            owner.sendSystemMessage(
                    Component.literal("<Chirithy> " + spellName + "! Winds guard you!")
            );
        }

        this.startCasting();

        aeroCooldown = 260;
        castCooldown = 25;
        recentOwnerDamageTicks = 0;

        return true;
    }

    private boolean tryCastEsuna(int chirithyLevel) {
        if (chirithyLevel < LEVEL_ESUNA) {
            return false;
        }

        if (esunaCooldown > 0 || castCooldown > 0) {
            return false;
        }

        List<Holder<MobEffect>> toRemove = new ArrayList<>();

        for (MobEffectInstance effect : owner.getActiveEffects()) {
            if (isEsunaRemovable(effect)) {
                toRemove.add(effect.getEffect());
            }
        }

        if (toRemove.isEmpty()) {
            return false;
        }

        for (Holder<MobEffect> effect : toRemove) {
            owner.removeEffect(effect);
        }

        owner.level().playSound(
                null,
                owner.getX(),
                owner.getY(),
                owner.getZ(),
                ModSoundsRM.ESUNA.get(),
                SoundSource.PLAYERS,
                1F,
                1F
        );

        if (ModConfigs.chirithySpellFeedback) {
            owner.sendSystemMessage(Component.literal("<Chirithy> No more ailments!"));
        }

        if (owner.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                    ParticleTypes.ENCHANT,
                    owner.getX(),
                    owner.getY() + 1.3D,
                    owner.getZ(),
                    12,
                    0.35D,
                    0.55D,
                    0.35D,
                    0.03D
            );
        }

        this.startCasting();

        esunaCooldown = 10 * 20;
        castCooldown = 25;

        return true;
    }

    private boolean isEsunaRemovable(MobEffectInstance effect) {
        if (effect == null) {
            return false;
        }

        Holder<MobEffect> effectHolder = effect.getEffect();

        if (effectHolder.value().getCategory() == MobEffectCategory.HARMFUL) {
            return true;
        }

        // Base Kingdom Keys Freeze is not consistently classified as harmful.
        return ModMobEffects.FREEZE.getKey().equals(effectHolder.getKey());
    }

    private boolean tryCastAutoLife(int chirithyLevel) {
        if (chirithyLevel < LEVEL_AUTO_LIFE) {
            return false;
        }

        if (autoLifeCooldown > 0 || castCooldown > 0) {
            return false;
        }

        if (owner.hasEffect(ModMobEffectsRM.AUTO_LIFE)) {
            return false;
        }

        // Healing and Esuna should take priority when the owner is already in danger.
        if (owner.getHealth() <= owner.getMaxHealth() * 0.5F
                || owner.hasEffect(ModMobEffects.KO)) {
            return false;
        }

        owner.addEffect(new MobEffectInstance(
                ModMobEffectsRM.AUTO_LIFE,
                Integer.MAX_VALUE,
                0,
                false,
                false
        ));

        owner.level().playSound(
                null,
                owner.getX(),
                owner.getY(),
                owner.getZ(),
                ModSoundsRM.AUTOLIFE.get(),
                SoundSource.PLAYERS,
                1F,
                1F
        );

        if (ModConfigs.chirithySpellFeedback) {
            owner.sendSystemMessage(
                    Component.literal("<Chirithy> Not gonna let you die! Auto-Life!")
            );
        }

        this.startCasting();

        autoLifeCooldown = (int) (ModConfigs.autoLifeCD * 1200);
        castCooldown = 35;

        return true;
    }

    private boolean trySelfHeal() {
        if (selfCureCooldown > 0 || castCooldown > 0) {
            return false;
        }

        if (!this.isAlive() || this.getHealth() >= this.getMaxHealth()) {
            return false;
        }

        if (owner.isHurt() || owner.hurtTime > 0 || owner.hasEffect(ModMobEffects.KO)) {
            return false;
        }

        this.heal((float) chirithyMagic);

        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                    ParticleTypes.HAPPY_VILLAGER,
                    this.getX(),
                    this.getY() + this.getBbHeight() + 0.4D,
                    this.getZ(),
                    5,
                    0.25D,
                    0.25D,
                    0.25D,
                    0.02D
            );
        }

        this.level().playSound(
                null,
                this.getX(),
                this.getY(),
                this.getZ(),
                ModSounds.cure.get(),
                SoundSource.NEUTRAL,
                1F,
                1F
        );

        if (ModConfigs.chirithySpellFeedback) {
            owner.sendSystemMessage(Component.literal("<Chirithy> Gotta patch myself up!"));
        }
        this.startCasting();

        selfCureCooldown = 300;
        castCooldown = 25;

        return true;
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        PlayerData playerData = PlayerData.get(player);
        if(!playerData.hasFlag(ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID,"chirithy_intro"))){
            if(player.isCrouching() && !player.level().isClientSide()){

                DialogueHandler.start((ServerPlayer) player, this, ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID,"dreameater_introduction"));
                return super.mobInteract(player, hand);
            }
        } else { // For Default dialog
            if(player.isCrouching() && !player.level().isClientSide()) {
                DialogueHandler.start((ServerPlayer) player, this, ResourceLocation.fromNamespaceAndPath(KingdomKeysReMind.MODID,"chirithy_guide"));
                return super.mobInteract(player, hand);
            }
        }


        ItemStack heldStack = player.getItemInHand(hand);
        int giftExp = getChirithyGiftExp(heldStack);

        if (giftExp > 0) {
            return giveChirithyGift(player, heldStack, giftExp);
        }

        InteractionResult result = DreamEaterPetHelper.tryPetDreamEater(this, player, hand, this.getOwnerUUID(), "Chirithy");

        if (result != InteractionResult.PASS) {
            return result;
        }

        return super.mobInteract(player, hand);
    }

    private InteractionResult giveChirithyGift(
            Player player,
            ItemStack heldStack,
            int giftExp
    ) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.SUCCESS;
        }

        if (!isChirithyOwner(serverPlayer)) {
            serverPlayer.displayClientMessage(
                    Component.literal("This Chirithy does not belong to you.")
                            .withStyle(ChatFormatting.RED),
                    true
            );

            return InteractionResult.CONSUME;
        }

        if (this.chirithyGiftCooldown > 0) {
            return InteractionResult.CONSUME;
        }

        if (!serverPlayer.getAbilities().instabuild) {
            heldStack.shrink(1);
        }

        this.chirithyGiftCooldown = CHIRITHY_GIFT_COOLDOWN_TICKS;

        DreamEaterExpHandler.giveDreamEaterExp(
                serverPlayer,
                GlobalDataRM.DREAM_EATER_CHIRITHY,
                giftExp,
                this
        );

        // Refresh stats immediately if the gift caused a level-up.
        this.updateStatsFromOwner();

        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                    ParticleTypes.HAPPY_VILLAGER,
                    this.getX(),
                    this.getY() + this.getBbHeight() + 0.4D,
                    this.getZ(),
                    8,
                    0.25D,
                    0.25D,
                    0.25D,
                    0.02D
            );
        }

        serverPlayer.displayClientMessage(
                Component.literal("Chirithy gained " + giftExp + " EXP!")
                        .withStyle(ChatFormatting.AQUA),
                true
        );

        return InteractionResult.CONSUME;
    }

    private int getChirithyGiftExp(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return 0;
        }

        if (stack.is(Items.AMETHYST_SHARD)) {
            return AMETHYST_GIFT_EXP;
        }

        if (stack.is(Items.GHAST_TEAR)) {
            return GHAST_TEAR_GIFT_EXP;
        }

        return 0;
    }

    private boolean isChirithyOwner(Player player) {
        if (player == null || this.getOwnerUUID() == null) {
            return false;
        }

        return this.getOwnerUUID().equals(player.getUUID());
    }

    @Override
    protected void updateWalkAnimation(float pPartialTick) {
        float f;

        if (this.getPose() == Pose.STANDING) {
            f = Math.min(pPartialTick * 6F, 1F);
        } else {
            f = 0F;
        }

        this.walkAnimation.update(f, 0.2F);
    }

    @Override
    protected void registerGoals() {

        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, 5F));
        this.goalSelector.addGoal(3, new ChirithyGoal(this, 0.85D, 2.0F, 10.0F, false));
        this.goalSelector.addGoal(4, new RandomStrollGoal(this, 0.25D));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(0, new Goal() {
            {
                this.setFlags(EnumSet.of(Goal.Flag.MOVE));
            }

            @Override
            public boolean canUse() {
                return ChirithyEntity.this.isDialogueLocked();
            }

            @Override
            public boolean canContinueToUse() {
                return ChirithyEntity.this.isDialogueLocked();
            }

            @Override
            public void tick() {
                ChirithyEntity.this.getNavigation().stop();
            }});
    }

    public static AttributeSupplier.Builder registerAttributes() {
        return Mob.createLivingAttributes()
                .add(Attributes.FOLLOW_RANGE, 50.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.5D)
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.ARMOR, 0.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D)
                .add(Attributes.ATTACK_KNOCKBACK, 0.0D)
                .add(Attributes.ATTACK_DAMAGE, 1.0D);
    }

    public int getMagic() {
        return (int) chirithyMagic;
    }

    public int getChirithyLevel() {
        if (owner == null) {
            return 1;
        }

        GlobalDataRM globalData = ModDataRM.getGlobal(owner);

        if (globalData == null) {
            return 1;
        }

        return globalData.getDreamEaterLevel(GlobalDataRM.DREAM_EATER_CHIRITHY);
    }

    @Override
    public int getMaxSpawnClusterSize() {
        return 1;
    }

    @Override
    public boolean causeFallDamage(float pFallDistance, float pMultiplier, DamageSource pSource) {
        return false;
    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        return null;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        Entity attacker = source.getEntity();
        UUID ownerId = this.getOwnerUUID();

        if (attacker != null && ownerId != null && attacker.getUUID().equals(ownerId)) {
            return false;
        }

        if (chirithyDefense > 0) {
            amount = (float) Math.round((amount * 100 / (300 + chirithyDefense)));
        }

        return super.hurt(source, amount);
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected void doPush(Entity entity) {
    }

    private boolean dialogueLocked = false;

    public void setDialogueLocked(boolean locked) {
        this.dialogueLocked = locked;

        if (locked) {
            this.getNavigation().stop();
            this.setDeltaMovement(0.0D, this.getDeltaMovement().y, 0.0D);
        }
    }

    public boolean isDialogueLocked() {
        return dialogueLocked;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(VARIANT, 0);
    }

    @Override
    public void setOwnerUUID(@Nullable UUID uuid) {
        this.ownerUUID = uuid;
        super.setOwnerUUID(uuid);
    }

    @Override
    @Nullable
    public UUID getOwnerUUID() {
        return this.ownerUUID != null ? this.ownerUUID : super.getOwnerUUID();
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return false;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        // Keeping this empty because your original class did not define GeckoLib controllers here.
        // If your renderer expects controllers, we can wire idle/walk/cast next.
    }

    public static void removeExistingChirithy(ServerLevel level, UUID ownerUUID) {
        MinecraftServer server = level.getServer();

        for (ServerLevel serverLevel : server.getAllLevels()) {
            for (Entity entity : serverLevel.getAllEntities()) {
                if (entity instanceof ChirithyEntity chirithy) {
                    if (ownerUUID.equals(chirithy.getOwnerUUID())) {
                        chirithy.discard();
                    }
                }
            }
        }
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);

        if (this.getOwnerUUID() != null) {
            compound.putUUID("DreamEaterOwner", this.getOwnerUUID());
        }

        compound.putInt("Variant", this.getVariant());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);

        if (compound.hasUUID("DreamEaterOwner")) {
            this.setOwnerUUID(compound.getUUID("DreamEaterOwner"));
        }

        if (compound.contains("Variant")) {
            this.setVariant(compound.getInt("Variant"));
        }
    }
}