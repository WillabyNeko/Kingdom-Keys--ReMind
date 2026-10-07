package online.remind.remind.entity;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import online.kingdomkeys.kingdomkeys.client.render.magic.InvisibleEntityRenderer;
import online.remind.remind.KingdomKeysReMind;
import online.remind.remind.client.model.*;
import online.remind.remind.client.model.mob.chirithyModel;
import online.remind.remind.client.model.reactioncommand.DarkMineModel;
import online.remind.remind.client.model.reactioncommand.LightBeamModel;
import online.remind.remind.client.render.*;
import online.remind.remind.client.render.mob.*;
import online.remind.remind.client.render.reactioncommand.DarkMineEntityRenderer;
import online.remind.remind.client.render.reactioncommand.LightBeamEntityRenderer;
import online.remind.remind.client.render.shotlock.BioShotEntityRenderer;
import online.remind.remind.entity.attacks.*;
import online.remind.remind.entity.enemies.*;
import online.remind.remind.entity.limits.firagaPillarEntity;
import online.remind.remind.entity.magic.*;
import online.remind.remind.entity.projectile.CactuarNeedleProjectile;
import online.remind.remind.entity.reactioncommand.*;
import online.remind.remind.entity.shotlock.*;
import online.remind.remind.entity.spirits.*;
import online.remind.remind.item.ICreativeTabRM;
import online.remind.remind.entity.effects.DreamEaterLevelUpEffectEntity;

import java.util.function.Supplier;

import static online.remind.remind.item.ModItemsRM.ITEMS;


@EventBusSubscriber(modid = KingdomKeysReMind.MODID, bus = EventBusSubscriber.Bus.MOD)
public class ModEntitiesRM {
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, KingdomKeysReMind.MODID);

    // Magic
    public static final Supplier<EntityType<HolyEntity>> TYPE_HOLY = createEntityType(HolyEntity::new, MobCategory.MISC,"entity_holy", 0.5F, 0.5F);
    public static final Supplier<EntityType<RuinEntity>> TYPE_RUIN = createEntityType(RuinEntity::new, MobCategory.MISC,"entity_ruin", 0.5F, 0.5F);
    public static final Supplier<EntityType<UltimaEntity>> TYPE_ULTIMA = createEntityType(UltimaEntity::new, MobCategory.MISC, "entity_ultima", 1F, 1F);
    public static final Supplier<EntityType<CometEntity>> TYPE_COMET = createEntityType(CometEntity::new, MobCategory.MISC, "entity_comet", 2F, 2F);
    public static final Supplier<EntityType<OsmoseEntity>> TYPE_OSMOSE = createEntityType(OsmoseEntity::new, MobCategory.MISC,"entity_osmose", 0.5F, 0.5F);
    public static final Supplier<EntityType<DrainEntity>> TYPE_DRAIN = createEntityType(DrainEntity::new, MobCategory.MISC,"entity_drain", 0.5F, 0.5F);
    public static final Supplier<EntityType<SilenceEntity>> TYPE_SILENCE = createEntityType(SilenceEntity::new, MobCategory.MISC,"entity_silence", 0.5F, 0.5F);
    public static final Supplier<EntityType<FaithEntity>> TYPE_FAITH = createEntityType(FaithEntity::new, MobCategory.MISC,"entity_faith", 1.5F, 1.5F);
    public static final Supplier<EntityType<MeteorEntity>> TYPE_METEOR = createEntityType(MeteorEntity::new, MobCategory.MISC,"entity_meteor", 1.5F, 1.5F);
    public static final Supplier<EntityType<FlareEntity>> TYPE_FLARE = createEntityType(FlareEntity::new, MobCategory.MISC,"entity_flare", 1.5F, 1.5F);
    public static final DeferredHolder<EntityType<?>, EntityType<ZettaflareBeamEntity>> TYPE_ZETTAFLARE_BEAM = ENTITIES.register("entity_zettaflare_beam", () -> EntityType.Builder.<ZettaflareBeamEntity>of(ZettaflareBeamEntity::new, MobCategory.MISC).sized(0.25F, 0.25F).clientTrackingRange(64).updateInterval(1).build("entity_zettaflare_beam"));


    // Attack Commands
    public static final Supplier<EntityType<quickBlitzCollider>> TYPE_QUICK_BLITZ = createEntityType(quickBlitzCollider::new, MobCategory.MISC,"quick_blitz_collider", 1.5F, 1.5F);
    public static final DeferredHolder<EntityType<?>, EntityType<BlitzCollider>> TYPE_BLITZ = ENTITIES.register("blitz", () -> EntityType.Builder.<BlitzCollider>of(BlitzCollider::new, MobCategory.MISC).sized(1.5F, 1.8F).clientTrackingRange(64).updateInterval(1).build("blitz"));
    public static final DeferredHolder<EntityType<?>, EntityType<DarkHazeCollider>> TYPE_DARK_HAZE = ENTITIES.register("dark_haze", () -> EntityType.Builder.<DarkHazeCollider>of(DarkHazeCollider::new, MobCategory.MISC).sized(1.5F, 1.8F).clientTrackingRange(64).updateInterval(1).build("dark_haze"));
    public static final DeferredHolder<EntityType<?>, EntityType<SonicBladeCollider>> TYPE_SONIC_BLADE = ENTITIES.register("sonic_blade", () -> EntityType.Builder.<SonicBladeCollider>of(SonicBladeCollider::new, MobCategory.MISC).sized(1.5F, 1.8F).clientTrackingRange(64).updateInterval(1).build("sonic_blade"));
    public static final DeferredHolder<EntityType<?>, EntityType<ChaosBladeCollider>> TYPE_CHAOS_BLADE = ENTITIES.register("chaos_blade", () -> EntityType.Builder.<ChaosBladeCollider>of(ChaosBladeCollider::new, MobCategory.MISC).sized(1.5F, 1.8F).clientTrackingRange(64).updateInterval(1).build("chaos_blade"));
    public static final DeferredHolder<EntityType<?>, EntityType<SlotEdgeCollider>> TYPE_SLOT_EDGE = ENTITIES.register("slot_edge", () -> EntityType.Builder.<SlotEdgeCollider>of(SlotEdgeCollider::new, MobCategory.MISC).sized(1.5F, 1.8F).clientTrackingRange(64).updateInterval(1).build("slot_edge"));
    public static final Supplier<EntityType<slidingDashCollider>> TYPE_SLIDING_DASH = createEntityType(slidingDashCollider::new, MobCategory.MISC,"sliding_dash_collider", 1.5F, 1.5F);
    public static final Supplier<EntityType<fireSurgeCollider>> TYPE_FIRE_SURGE = createEntityType(fireSurgeCollider::new, MobCategory.MISC,"fire_surge_collider", 1.5F, 1.5F);
    public static final Supplier<EntityType<thunderSurgeCollider>> TYPE_THUNDER_SURGE = createEntityType(thunderSurgeCollider::new, MobCategory.MISC,"thunder_surge_collider", 1.5F, 1.5F);
    public static final Supplier<EntityType<blizzardSurgeCollider>> TYPE_BLIZZARD_SURGE = createEntityType(blizzardSurgeCollider::new, MobCategory.MISC,"blizzard_surge_collider", 1.5F, 1.5F);
    public static final Supplier<EntityType<waterSurgeCollider>> TYPE_WATER_SURGE = createEntityType(waterSurgeCollider::new, MobCategory.MISC,"water_surge_collider", 1.5F, 1.5F);
    public static final Supplier<EntityType<aeroSurgeCollider>> TYPE_AERO_SURGE = createEntityType(aeroSurgeCollider::new, MobCategory.MISC,"aero_surge_collider", 1.5F, 1.5F);
    public static final Supplier<EntityType<lightSurgeCollider>> TYPE_LIGHT_SURGE = createEntityType(lightSurgeCollider::new, MobCategory.MISC,"light_surge_collider", 1.5F, 1.5F);
    public static final Supplier<EntityType<darkSurgeCollider>> TYPE_DARK_SURGE = createEntityType(darkSurgeCollider::new, MobCategory.MISC,"dark_surge_collider", 1.5F, 1.5F);
    public static final Supplier<EntityType<zantetsukenCollider>> TYPE_ZANTETSUKEN = createEntityType(zantetsukenCollider::new, MobCategory.MISC,"zantetsuken_collider", 1.5F, 1.5F);
    public static final DeferredHolder<EntityType<?>, EntityType<ElementStrikeCollider>> TYPE_ELEMENT_STRIKE = ENTITIES.register("element_strike", () -> EntityType.Builder.<ElementStrikeCollider>of(ElementStrikeCollider::new, MobCategory.MISC).sized(1.0F, 1.5F).clientTrackingRange(64).updateInterval(1).build("element_strike"));

    // Limits
    public static final Supplier<EntityType<firagaPillarEntity>> TYPE_FIRAGA_PILLAR = createEntityType(firagaPillarEntity::new, MobCategory.MISC,"firaga_pillar", 2f, 5f);
    public static final DeferredHolder<EntityType<?>, EntityType<CrossSlashEffectEntity>>
            CROSS_SLASH_EFFECT = ENTITIES.register(
            "cross_slash_effect",
            () -> EntityType.Builder
                    .<CrossSlashEffectEntity>of(
                            CrossSlashEffectEntity::new,
                            MobCategory.MISC
                    )
                    .sized(0.1F, 0.1F)
                    .clientTrackingRange(64)
                    .updateInterval(1)
                    .build("cross_slash_effect")
    );



    // Shotlocks
    public static final Supplier<EntityType<BioBarrageShotEntity>> TYPE_BIO_SHOT = createEntityType(BioBarrageShotEntity::new, MobCategory.MISC, "entity_bio_shot", 0.5F, 0.5F);
    public static final Supplier<EntityType<FlameSalvoCoreEntity>> TYPE_SHOTLOCK_FLAME_SALVO = createEntityType(FlameSalvoCoreEntity::new, MobCategory.MISC, "entity_shotlock_flame_salvo_core", 0.5F, 0.5F);
    public static final Supplier<EntityType<BubbleBlasterCoreEntity>> TYPE_SHOTLOCK_BUBBLE_BLASTER = createEntityType(BubbleBlasterCoreEntity::new, MobCategory.MISC, "entity_shotlock_bubble_blaster_core", 0.5F, 0.5F);
    public static final Supplier<EntityType<ThunderstormCoreEntity>> TYPE_SHOTLOCK_THUNDERSTORM = createEntityType(ThunderstormCoreEntity::new, MobCategory.MISC, "entity_shotlock_thunderstorm_core", 0.5F, 0.5F);
    public static final Supplier<EntityType<BioBarrageCoreEntity>> TYPE_SHOTLOCK_BIO_BARRAGE = createEntityType(BioBarrageCoreEntity::new, MobCategory.MISC, "entity_shotlock_bio_barrage_core", 0.5F, 0.5F);
    public static final Supplier<EntityType<MeteorShowerCoreEntity>> TYPE_SHOTLOCK_METEOR_SHOWER = createEntityType(MeteorShowerCoreEntity::new, MobCategory.MISC, "entity_shotlock_meteor_shower_core", 0.5F, 0.5F);
    public static final Supplier<EntityType<DarkFiragaCoreEntity>> TYPE_SHOTLOCK_DARK_FIRAGA = createEntityType(DarkFiragaCoreEntity::new, MobCategory.MISC, "entity_shotlock_dark_firaga_core", 0.5F, 0.5F);
    public static final Supplier<EntityType<DarkDivideCoreEntity>> TYPE_SHOTLOCK_DARK_DIVIDE = createEntityType(DarkDivideCoreEntity::new, MobCategory.MISC, "entity_shotlock_dark_divide_core", 0.5F, 0.5F);
    public static final Supplier<EntityType<DarkDivideShotEntity>> TYPE_SHOTLOCK_DARK_DIVIDE_SHOT = createEntityType(DarkDivideShotEntity::new, MobCategory.MISC, "entity_shotlock_dark_divide_shot", 0.5F, 0.5F);
    public static final Supplier<EntityType<DarkFiragaShotEntity>> TYPE_SHOTLOCK_DARK_FIRAGA_SHOT = createEntityType(DarkFiragaShotEntity::new, MobCategory.MISC, "entity_shotlock_dark_firaga_shot", 0.5F, 0.5F);

    // Reaction Commands
    public static final Supplier<EntityType<LightBeamEntity>> TYPE_LIGHT_BEAM = createEntityType(LightBeamEntity::new, MobCategory.MISC, "entity_rc_light_beam", 3F,4F);
    public static final Supplier<EntityType<DarkMineEntity>> TYPE_DARK_MINE = createEntityType(DarkMineEntity::new, MobCategory.MISC, "entity_rc_dark_mine", 2F,2.5F);
    public static final Supplier<EntityType<DualShotEntity>> TYPE_DUAL_SHOT = createEntityType(DualShotEntity::new, MobCategory.MISC, "entity_rc_dual_shot", 1.5F,1.5F);
    public static final Supplier<EntityType<ThornsEntity>> TYPE_THORNS = createEntityType(ThornsEntity::new, MobCategory.MISC, "entity_rc_thorns", 1.5F,1.5F);

    public static final Supplier<EntityType<ravenousSaberCollider>> TYPE_RAVE_SABER = createEntityType(ravenousSaberCollider::new, MobCategory.MISC, "entity_rc_ravesaber", 1.5F,1.5F);

    // Misc
    public static final Supplier<EntityType<DarkFiragaEntity>> TYPE_DARK_FIRAGA = createEntityType(DarkFiragaEntity::new, MobCategory.MISC,"entity_dark_firaga", 1f, 1f);

    public static final Supplier<EntityType<CounterRushCore>> TYPE_COUNTER_RUSH = createEntityType(CounterRushCore::new, MobCategory.MISC,"entity_counter_rush", 1f, 1f);

    // Dream Eaters
    public static final Item.Properties PROPERTIES = new Item.Properties();



    public static final Supplier<EntityType<ChirithyEntity>> TYPE_CHIRITHY =
            ENTITIES.register("chirithy", () ->
                    EntityType.Builder.<ChirithyEntity>of(ChirithyEntity::new, MobCategory.MONSTER)
                            .sized(1F, 1F)
                            .setShouldReceiveVelocityUpdates(true)
                            .setUpdateInterval(1)
                            .setTrackingRange(64)
                            .build("chirithy")
            );

    public static final Supplier<EntityType<MeowWowEntity>> TYPE_MEOW_WOW =
            ENTITIES.register("meow_wow", () ->
                    EntityType.Builder.<MeowWowEntity>of(MeowWowEntity::new, MobCategory.MONSTER)
                            .sized(1F, 1F)
                            .setShouldReceiveVelocityUpdates(true)
                            .setUpdateInterval(1)
                            .setTrackingRange(64)
                            .build("meow_wow")
            );

    public static final Supplier<EntityType<KomoryBatEntity>> TYPE_KOMORY_BAT =
            ENTITIES.register("komory_bat", () ->
                    EntityType.Builder.<KomoryBatEntity>of(KomoryBatEntity::new, MobCategory.MONSTER)
                            .sized(0.5F, 0.5F)
                            .setShouldReceiveVelocityUpdates(true)
                            .setUpdateInterval(1)
                            .setTrackingRange(64)
                            .build("komory_bat")
            );

    public static final Supplier<EntityType<CactuarSpiritEntity>> TYPE_CACTUAR_SPIRIT =
            ENTITIES.register("cactuar_spirit", () ->
                    EntityType.Builder.<CactuarSpiritEntity>of(CactuarSpiritEntity::new, MobCategory.MONSTER)
                            .sized(0.75F, 0.75F)
                            .setShouldReceiveVelocityUpdates(true)
                            .setUpdateInterval(1)
                            .setTrackingRange(64)
                            .build("cactuar_spirit")
            );

    public static final Supplier<EntityType<TonberrySpiritEntity>> TYPE_TONBERRY_SPIRIT =
            ENTITIES.register("tonberry_spirit", () ->
                    EntityType.Builder.<TonberrySpiritEntity>of(TonberrySpiritEntity::new, MobCategory.CREATURE)
                            .sized(0.8F, 1.6F)
                            .setShouldReceiveVelocityUpdates(true)
                            .setUpdateInterval(1)
                            .setTrackingRange(64)
                            .build("tonberry_spirit")
            );


    public static final Supplier<EntityType<DreamEaterLevelUpEffectEntity>>
            TYPE_DREAM_EATER_LEVEL_UP_EFFECT =
            createEntityType(
                    DreamEaterLevelUpEffectEntity::new,
                    MobCategory.MISC,
                    "dream_eater_level_up_effect",
                    0.1F,
                    0.1F
            );


    // Enemies

    public static final DeferredHolder<EntityType<?>, EntityType<CactuarEntity>> TYPE_CACTUAR =
            ENTITIES.register("cactuar", () ->
                    EntityType.Builder.<CactuarEntity>of(CactuarEntity::new, MobCategory.MONSTER)
                            .sized(0.75F, 0.75F)
                            .clientTrackingRange(8)
                            .updateInterval(2)
                            .build("cactuar")
            );

    public static final DeferredHolder<EntityType<?>, EntityType<CactuarEntity>> TYPE_JUMBO_CACTUAR =
            ENTITIES.register("jumbo_cactuar", () ->
                    EntityType.Builder.<CactuarEntity>of(CactuarEntity::new, MobCategory.MONSTER)
                            .sized(6.0F, 14.0F)
                            .clientTrackingRange(12)
                            .updateInterval(2)
                            .build("jumbo_cactuar")
            );

    public static final DeferredHolder<EntityType<?>, EntityType<CactuarNeedleProjectile>> TYPE_CACTUAR_NEEDLE =
            ENTITIES.register("cactuar_needle", () ->
                    EntityType.Builder.<CactuarNeedleProjectile>of(CactuarNeedleProjectile::new, MobCategory.MISC)
                            .sized(0.25F, 0.25F)
                            .clientTrackingRange(64)
                            .updateInterval(1)
                            .build("cactuar_needle")
            );

    public static final DeferredHolder<EntityType<?>, EntityType<TonberryEntity>> TYPE_TONBERRY =
            ENTITIES.register("tonberry", () ->
                    EntityType.Builder.<TonberryEntity>of(TonberryEntity::new, MobCategory.MONSTER)
                            .sized(0.8F, 1.6F)
                            .clientTrackingRange(8)
                            .updateInterval(2)
                            .build("tonberry")
            );

    public static final DeferredHolder<EntityType<?>, EntityType<TonberryKingEntity>> TYPE_TONBERRY_KING =
            ENTITIES.register("tonberry_king", () ->
                    EntityType.Builder.<TonberryKingEntity>of(TonberryKingEntity::new, MobCategory.MONSTER)
                            .sized(2.2F, 2.6F)
                            .clientTrackingRange(8)
                            .updateInterval(2)
                            .build("tonberry_king")
            );

    public static final DeferredHolder<EntityType<?>, EntityType<BombEntity>> TYPE_BOMB =
            ENTITIES.register("bomb", () ->
                    EntityType.Builder.<BombEntity>of(BombEntity::new, MobCategory.MONSTER)
                            .sized(0.9F, 0.9F)
                            .fireImmune()
                            .clientTrackingRange(8)
                            .updateInterval(2)
                            .build("bomb")
            );

    public static final Supplier<Item> BOMB_SPAWN_EGG =
            ITEMS.register(
                    "bomb_spawn_egg",
                    () -> new DeferredSpawnEggItem(
                            ModEntitiesRM.TYPE_BOMB,
                            0xFFFFFF,
                            0xFFFFFF,
                            new Item.Properties()
                    )
            );

    public static final Supplier<Item> GRENADE_SPAWN_EGG =
            ITEMS.register(
                    "grenade_spawn_egg",
                    () -> new DeferredSpawnEggItem(
                            ModEntitiesRM.TYPE_GRENADE,
                            0xFFFFFF,
                            0xFFFFFF,
                            new Item.Properties()
                    )
            );

    public static final Supplier<Item> VOLCANO_SPAWN_EGG =
            ITEMS.register(
                    "volcano_spawn_egg",
                    () -> new DeferredSpawnEggItem(
                            ModEntitiesRM.TYPE_VOLCANO,
                            0xFFFFFF,
                            0xFFFFFF,
                            new Item.Properties()
                    )
            );

    public static final DeferredHolder<EntityType<?>, EntityType<BombEntity>> TYPE_GRENADE =
            ENTITIES.register("grenade", () ->
                    EntityType.Builder.<BombEntity>of(BombEntity::new, MobCategory.MONSTER)
                            .sized(0.9F, 0.9F)
                            .fireImmune()
                            .clientTrackingRange(8)
                            .updateInterval(2)
                            .build("grenade")
            );

    public static final DeferredHolder<EntityType<?>, EntityType<BombEntity>> TYPE_VOLCANO =
            ENTITIES.register("volcano", () ->
                    EntityType.Builder.<BombEntity>of(BombEntity::new, MobCategory.MONSTER)
                            .sized(0.9F, 0.9F)
                            .fireImmune()
                            .clientTrackingRange(8)
                            .updateInterval(2)
                            .build("volcano")
            );

    public static final Supplier<EntityType<FlanEntity>> TYPE_FIRE_FLAN =
            createEntityType((type, level) -> {
                FlanEntity flan = new FlanEntity(type, level);
                flan.setVariant(FlanEntity.VARIANT_FIRE);
                return flan;
            }, MobCategory.MONSTER, "fire_flan", 3.0F, 3.0F);

    public static final Supplier<EntityType<FlanEntity>> TYPE_ICE_FLAN =
            createEntityType((type, level) -> {
                FlanEntity flan = new FlanEntity(type, level);
                flan.setVariant(FlanEntity.VARIANT_ICE);
                return flan;
            }, MobCategory.MONSTER, "ice_flan", 3.0F, 3.0F);

    public static final Supplier<EntityType<FlanEntity>> TYPE_THUNDER_FLAN =
            createEntityType((type, level) -> {
                FlanEntity flan = new FlanEntity(type, level);
                flan.setVariant(FlanEntity.VARIANT_THUNDER);
                return flan;
            }, MobCategory.MONSTER, "thunder_flan", 3.0F, 3.0F);

    public static final Supplier<EntityType<FlanEntity>> TYPE_WIND_FLAN =
            createEntityType((type, level) -> {
                FlanEntity flan = new FlanEntity(type, level);
                flan.setVariant(FlanEntity.VARIANT_WIND);
                return flan;
            }, MobCategory.MONSTER, "wind_flan", 3.0F, 3.0F);

    public static final Supplier<EntityType<FlanEntity>> TYPE_WATER_FLAN =
            createEntityType((type, level) -> {
                FlanEntity flan = new FlanEntity(type, level);
                flan.setVariant(FlanEntity.VARIANT_WATER);
                return flan;
            }, MobCategory.MONSTER, "water_flan", 3.0F, 3.0F);

    public static final Supplier<EntityType<FlanEntity>> TYPE_LIGHT_FLAN =
            createEntityType((type, level) -> {
                FlanEntity flan = new FlanEntity(type, level);
                flan.setVariant(FlanEntity.VARIANT_LIGHT);
                return flan;
            }, MobCategory.MONSTER, "light_flan", 3.0F, 3.0F);

    public static final Supplier<EntityType<FlanEntity>> TYPE_DARK_FLAN =
            createEntityType((type, level) -> {
                FlanEntity flan = new FlanEntity(type, level);
                flan.setVariant(FlanEntity.VARIANT_DARK);
                return flan;
            }, MobCategory.MONSTER, "dark_flan", 3.0F, 3.0F);



    public static final Supplier<Item> CHIRITHY_EGG = ITEMS.register("chirithy_spawn_egg", () -> new DeferredSpawnEggItem(TYPE_CHIRITHY, 0xAAAAFF, 0xFF00FF, PROPERTIES));


    public static <T extends Entity, M extends EntityType<T>>Supplier<EntityType<T>> createEntityType(EntityType.EntityFactory<T> factory, MobCategory classification, String name, float sizeX, float sizeY) {        return ENTITIES.register(name, () -> EntityType.Builder.of(factory, classification)
                .setShouldReceiveVelocityUpdates(false)
                .setUpdateInterval(1)
                .setTrackingRange(8)
                .sized(sizeX, sizeY)
                .build(name));
    }
    @OnlyIn(Dist.CLIENT)
    public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(HolyModel.LAYER_LOCATION, HolyModel::createBodyLayer);
        event.registerLayerDefinition(RuinModel.LAYER_LOCATION, RuinModel::createBodyLayer);
        event.registerLayerDefinition(BalloonModel.LAYER_LOCATION, BalloonModel::createBodyLayer);
        event.registerLayerDefinition(BalloongaModel.LAYER_LOCATION, BalloongaModel::createBodyLayer);
        event.registerLayerDefinition(UltimaModel.LAYER_LOCATION, UltimaModel::createBodyLayer);
        event.registerLayerDefinition(CometModel.LAYER_LOCATION, CometModel::createBodyLayer);
        event.registerLayerDefinition(OsmoseModel.LAYER_LOCATION, OsmoseModel::createBodyLayer);
        event.registerLayerDefinition(DrainModel.LAYER_LOCATION, DrainModel::createBodyLayer);
        event.registerLayerDefinition(SilenceModel.LAYER_LOCATION, SilenceModel::createBodyLayer);
        event.registerLayerDefinition(WarpModel.LAYER_LOCATION, WarpModel::createBodyLayer);
        event.registerLayerDefinition(SparkModel.LAYER_LOCATION, SparkModel::createBodyLayer);
        event.registerLayerDefinition(mineModel.LAYER_LOCATION, mineModel::createBodyLayer);

        event.registerLayerDefinition(BerserkAuraModel.LAYER_LOCATION, BerserkAuraModel::createBodyLayer);
        event.registerLayerDefinition(AutoLifeModel.LAYER_LOCATION, AutoLifeModel::createBodyLayer);
        event.registerLayerDefinition(ConfuseModel.LAYER_LOCATION, ConfuseModel::createBodyLayer);

        event.registerLayerDefinition(LightBeamModel.LAYER_LOCATION, LightBeamModel::createBodyLayer);
        event.registerLayerDefinition(DarkMineModel.LAYER_LOCATION, DarkMineModel::createBodyLayer);

        event.registerLayerDefinition(chirithyModel.LAYER_LOCATION, chirithyModel::createBodyLayer);

    }

    @OnlyIn(Dist.CLIENT)
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {

        event.registerEntityRenderer(TYPE_HOLY.get(), HolyEntityRenderer::new);
        event.registerEntityRenderer(TYPE_RUIN.get(), RuinEntityRenderer::new);
        event.registerEntityRenderer(TYPE_COMET.get(), CometEntityRenderer::new);
        event.registerEntityRenderer(TYPE_ULTIMA.get(), UltimaEntityRenderer::new);
        event.registerEntityRenderer(TYPE_OSMOSE.get(),InvisibleEntityRenderer::new);
        event.registerEntityRenderer(TYPE_DRAIN.get(),InvisibleEntityRenderer::new);
        event.registerEntityRenderer(TYPE_SILENCE.get(),InvisibleEntityRenderer::new);
        event.registerEntityRenderer(TYPE_FAITH.get(),InvisibleEntityRenderer::new);
        event.registerEntityRenderer(TYPE_METEOR.get(),InvisibleEntityRenderer::new);
        event.registerEntityRenderer(TYPE_FLARE.get(),InvisibleEntityRenderer::new);

        event.registerEntityRenderer(TYPE_QUICK_BLITZ.get(),InvisibleEntityRenderer::new);
        event.registerEntityRenderer(TYPE_BLITZ.get(),InvisibleEntityRenderer::new);
        event.registerEntityRenderer(TYPE_DARK_HAZE.get(),InvisibleEntityRenderer::new);
        event.registerEntityRenderer(TYPE_SONIC_BLADE.get(),InvisibleEntityRenderer::new);
        event.registerEntityRenderer(TYPE_CHAOS_BLADE.get(),InvisibleEntityRenderer::new);
        event.registerEntityRenderer(TYPE_SLOT_EDGE.get(),InvisibleEntityRenderer::new);
        event.registerEntityRenderer(TYPE_SLIDING_DASH.get(),InvisibleEntityRenderer::new);
        event.registerEntityRenderer(TYPE_AERO_SURGE.get(),InvisibleEntityRenderer::new);
        event.registerEntityRenderer(TYPE_BLIZZARD_SURGE.get(),InvisibleEntityRenderer::new);
        event.registerEntityRenderer(TYPE_DARK_SURGE.get(),InvisibleEntityRenderer::new);
        event.registerEntityRenderer(TYPE_FIRE_SURGE.get(),InvisibleEntityRenderer::new);
        event.registerEntityRenderer(TYPE_LIGHT_SURGE.get(),InvisibleEntityRenderer::new);
        event.registerEntityRenderer(TYPE_WATER_SURGE.get(),InvisibleEntityRenderer::new);
        event.registerEntityRenderer(TYPE_ZANTETSUKEN.get(),InvisibleEntityRenderer::new);
        event.registerEntityRenderer(TYPE_ELEMENT_STRIKE.get(),InvisibleEntityRenderer::new);


        event.registerEntityRenderer(TYPE_RAVE_SABER.get(), InvisibleEntityRenderer::new);

        event.registerEntityRenderer(TYPE_FIRAGA_PILLAR.get(), InvisibleEntityRenderer::new);



        event.registerEntityRenderer(TYPE_BIO_SHOT.get(), BioShotEntityRenderer::new);
        event.registerEntityRenderer(TYPE_SHOTLOCK_DARK_FIRAGA_SHOT.get(), BioShotEntityRenderer::new);
        event.registerEntityRenderer(TYPE_SHOTLOCK_DARK_DIVIDE_SHOT.get(), BioShotEntityRenderer::new);

        event.registerEntityRenderer(TYPE_SHOTLOCK_FLAME_SALVO.get(), InvisibleEntityRenderer::new);
        event.registerEntityRenderer(TYPE_SHOTLOCK_BUBBLE_BLASTER.get(), InvisibleEntityRenderer::new);
        event.registerEntityRenderer(TYPE_SHOTLOCK_BIO_BARRAGE.get(), InvisibleEntityRenderer::new);
        event.registerEntityRenderer(TYPE_SHOTLOCK_THUNDERSTORM.get(), InvisibleEntityRenderer::new);
        event.registerEntityRenderer(TYPE_SHOTLOCK_METEOR_SHOWER.get(), InvisibleEntityRenderer::new);
        event.registerEntityRenderer(TYPE_SHOTLOCK_DARK_FIRAGA.get(), InvisibleEntityRenderer::new);
        event.registerEntityRenderer(TYPE_SHOTLOCK_DARK_DIVIDE.get(), InvisibleEntityRenderer::new);

        event.registerEntityRenderer(
                TYPE_ZETTAFLARE_BEAM.get(),
                ZettaflareBeamRenderer::new
        );

        event.registerEntityRenderer(
                ModEntitiesRM.CROSS_SLASH_EFFECT.get(),
                CrossSlashEffectRenderer::new
        );

        event.registerEntityRenderer(TYPE_LIGHT_BEAM.get(), LightBeamEntityRenderer::new);
        event.registerEntityRenderer(TYPE_DARK_MINE.get(), DarkMineEntityRenderer::new);
        event.registerEntityRenderer(TYPE_DUAL_SHOT.get(),InvisibleEntityRenderer::new);
        event.registerEntityRenderer(TYPE_THORNS.get(),InvisibleEntityRenderer::new);

        event.registerEntityRenderer(TYPE_DARK_FIRAGA.get(),InvisibleEntityRenderer::new);
        event.registerEntityRenderer(TYPE_COUNTER_RUSH.get(),InvisibleEntityRenderer::new);

        event.registerEntityRenderer(TYPE_CHIRITHY.get(),ChirithyRenderer::new);
        event.registerEntityRenderer(TYPE_MEOW_WOW.get(), MeowWowRenderer::new);
        event.registerEntityRenderer(TYPE_KOMORY_BAT.get(), KomoryBatRenderer::new);

        event.registerEntityRenderer(TYPE_CACTUAR_SPIRIT.get(), CactuarSpiritRenderer::new);
        event.registerEntityRenderer(TYPE_TONBERRY_SPIRIT.get(), TonberrySpiritRenderer::new);

        event.registerEntityRenderer(TYPE_CACTUAR.get(), CactuarRenderer::new);
        event.registerEntityRenderer(TYPE_JUMBO_CACTUAR.get(), CactuarRenderer::new);
        event.registerEntityRenderer(TYPE_CACTUAR_NEEDLE.get(), CactuarNeedleRenderer::new);

        event.registerEntityRenderer(ModEntitiesRM.TYPE_TONBERRY.get(), TonberryRenderer::new);
        event.registerEntityRenderer(ModEntitiesRM.TYPE_TONBERRY_KING.get(), TonberryRenderer::new);

        event.registerEntityRenderer(TYPE_DREAM_EATER_LEVEL_UP_EFFECT.get(), DreamEaterLevelUpEffectRenderer::new);

        event.registerEntityRenderer(TYPE_BOMB.get(), BombRenderer::new);
        event.registerEntityRenderer(TYPE_GRENADE.get(), BombRenderer::new);
        event.registerEntityRenderer(TYPE_VOLCANO.get(), BombRenderer::new);

        event.registerEntityRenderer(TYPE_FIRE_FLAN.get(), FlanRenderer::new);
        event.registerEntityRenderer(TYPE_ICE_FLAN.get(), FlanRenderer::new);
        event.registerEntityRenderer(TYPE_THUNDER_FLAN.get(), FlanRenderer::new);
        event.registerEntityRenderer(TYPE_WIND_FLAN.get(), FlanRenderer::new);
        event.registerEntityRenderer(TYPE_WATER_FLAN.get(), FlanRenderer::new);
        event.registerEntityRenderer(TYPE_LIGHT_FLAN.get(), FlanRenderer::new);
        event.registerEntityRenderer(TYPE_DARK_FLAN.get(), FlanRenderer::new);


    }

    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event){
        event.put(TYPE_CHIRITHY.get(), ChirithyEntity.registerAttributes().build());
        event.put(TYPE_MEOW_WOW.get(), MeowWowEntity.createAttributes().build());
        event.put(TYPE_KOMORY_BAT.get(), KomoryBatEntity.createAttributes().build());
        event.put(TYPE_CACTUAR_SPIRIT.get(), CactuarSpiritEntity.createAttributes().build());
        event.put(TYPE_TONBERRY_SPIRIT.get(), TonberrySpiritEntity.createAttributes().build());


        event.put(TYPE_CACTUAR.get(), CactuarEntity.createNormalAttributes().build());
        event.put(TYPE_JUMBO_CACTUAR.get(), CactuarEntity.createJumboAttributes().build());

        event.put(ModEntitiesRM.TYPE_TONBERRY.get(), TonberryEntity.createAttributes().build());
        event.put(ModEntitiesRM.TYPE_TONBERRY_KING.get(), TonberryKingEntity.createAttributes().build());
        event.put(TYPE_BOMB.get(), BombEntity.createBombAttributes().build());
        event.put(TYPE_GRENADE.get(), BombEntity.createGrenadeAttributes().build());
        event.put(TYPE_VOLCANO.get(), BombEntity.createVolcanoAttributes().build());

        event.put(TYPE_FIRE_FLAN.get(), FlanEntity.createAttributes().build());
        event.put(TYPE_ICE_FLAN.get(), FlanEntity.createAttributes().build());
        event.put(TYPE_THUNDER_FLAN.get(), FlanEntity.createAttributes().build());
        event.put(TYPE_WIND_FLAN.get(), FlanEntity.createAttributes().build());
        event.put(TYPE_WATER_FLAN.get(), FlanEntity.createAttributes().build());
        event.put(TYPE_LIGHT_FLAN.get(), FlanEntity.createAttributes().build());
        event.put(TYPE_DARK_FLAN.get(), FlanEntity.createAttributes().build());
    }

}
