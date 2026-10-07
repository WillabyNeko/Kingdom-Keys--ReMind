package online.remind.remind.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import online.remind.remind.KingdomKeysReMind;
import online.remind.remind.entity.enemies.BombEntity;
import online.remind.remind.entity.enemies.CactuarEntity;
import online.remind.remind.entity.enemies.TonberryEntity;

@EventBusSubscriber(
        modid = KingdomKeysReMind.MODID,
        bus = EventBusSubscriber.Bus.MOD
)
public class ModSpawnPlacementsRM {

    private static final int LAVA_SEARCH_RADIUS = 8;

    private ModSpawnPlacementsRM() {
    }

    @SubscribeEvent
    public static void registerSpawnPlacements(RegisterSpawnPlacementsEvent event) {
        event.register(
                ModEntitiesRM.TYPE_CACTUAR.get(),
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                CactuarEntity::checkCactuarSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.REPLACE
        );

        event.register(
                ModEntitiesRM.TYPE_BOMB.get(),
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                ModSpawnPlacementsRM::checkBombFamilySpawnRules,
                RegisterSpawnPlacementsEvent.Operation.REPLACE
        );

        event.register(
                ModEntitiesRM.TYPE_GRENADE.get(),
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                ModSpawnPlacementsRM::checkBombFamilySpawnRules,
                RegisterSpawnPlacementsEvent.Operation.REPLACE
        );

        event.register(
                ModEntitiesRM.TYPE_VOLCANO.get(),
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                ModSpawnPlacementsRM::checkBombFamilySpawnRules,
                RegisterSpawnPlacementsEvent.Operation.REPLACE
        );

        event.register(ModEntitiesRM.TYPE_FIRE_FLAN.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(ModEntitiesRM.TYPE_ICE_FLAN.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(ModEntitiesRM.TYPE_THUNDER_FLAN.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(ModEntitiesRM.TYPE_WIND_FLAN.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(ModEntitiesRM.TYPE_WATER_FLAN.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(ModEntitiesRM.TYPE_LIGHT_FLAN.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(ModEntitiesRM.TYPE_DARK_FLAN.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
    }

    private static boolean checkBombFamilySpawnRules(EntityType<? extends BombEntity> type, ServerLevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        // Normal hostile mob spawn rules
        if (!Monster.checkAnyLightMonsterSpawnRules(type, level, spawnType, pos, random)) {
            return false;
        }

        BlockPos below = pos.below();

        // Don't spawn inside fluids
        if (!level.getFluidState(pos).isEmpty()) {
            return false;
        }

        // Don't spawn directly above lava/water
        if (!level.getFluidState(below).isEmpty()) {
            return false;
        }

        // Require a real solid surface underneath the entity
        if (!level.getBlockState(below).isFaceSturdy(
                level,
                below,
                Direction.UP
        )) {
            return false;
        }

        // Nether: allowed anywhere that passed the ground checks above
        if (level.getLevel()
                .dimension()
                .equals(Level.NETHER)) {
            return true;
        }

        // Overworld/etc: must additionally have lava nearby
        return hasNearbyLava(
                level,
                pos,
                LAVA_SEARCH_RADIUS
        );
    }

    private static boolean checkFlanSpawnRules(
            EntityType<? extends Monster> type,
            ServerLevelAccessor level,
            MobSpawnType spawnType,
            BlockPos pos,
            RandomSource random
    ) {
        // Keep vanilla monster spawning rules
        if (!Monster.checkMonsterSpawnRules(
                type,
                level,
                spawnType,
                pos,
                random
        )) {
            return false;
        }

        BlockPos below = pos.below();

        // Cannot spawn inside lava/water
        if (!level.getFluidState(pos).isEmpty()) {
            return false;
        }

        // Cannot spawn directly on top of lava/water
        if (!level.getFluidState(below).isEmpty()) {
            return false;
        }

        // Must have a real solid floor
        if (!level.getBlockState(below).isFaceSturdy(
                level,
                below,
                Direction.UP
        )) {
            return false;
        }

        return true;
    }

    private static boolean hasNearbyLava(
            ServerLevelAccessor level,
            BlockPos origin,
            int radius
    ) {
        BlockPos min =
                origin.offset(
                        -radius,
                        -radius,
                        -radius
                );

        BlockPos max =
                origin.offset(
                        radius,
                        radius,
                        radius
                );

        for (BlockPos checkPos :
                BlockPos.betweenClosed(min, max)) {

            var fluidState =
                    level.getFluidState(checkPos);

            if (fluidState.is(FluidTags.LAVA)) {

                return true;
            }
        }

        return false;
    }
}
