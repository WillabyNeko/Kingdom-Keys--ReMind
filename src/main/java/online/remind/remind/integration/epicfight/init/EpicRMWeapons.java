package online.remind.remind.integration.epicfight.init;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import online.kingdomkeys.kingdomkeys.KingdomKeys;
import online.kingdomkeys.kingdomkeys.data.PlayerData;
import online.kingdomkeys.kingdomkeys.integration.epicfight.enums.EpicKKWeaponEnum;
import online.kingdomkeys.kingdomkeys.integration.epicfight.enums.KKStyles;
import online.kingdomkeys.kingdomkeys.integration.epicfight.init.*;
import online.kingdomkeys.kingdomkeys.integration.epicfight.skills.KKSkills;
import online.kingdomkeys.kingdomkeys.lib.Strings;
import online.remind.remind.KingdomKeysReMind;
import yesman.epicfight.EpicFight;
import yesman.epicfight.api.animation.LivingMotions;
import yesman.epicfight.api.event.EpicFightEventHooks;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.registry.deferred.ItemPresetRegister;
import yesman.epicfight.registry.deferred.holders.DeferredWeapon;
import yesman.epicfight.registry.entries.EpicFightMovesets;
import yesman.epicfight.registry.entries.EpicFightProviderConditionals;
import yesman.epicfight.registry.entries.EpicFightSounds;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.capabilities.item.WeaponCapability;
import yesman.epicfight.world.capabilities.item.WeaponCategory;

import java.util.function.Function;

public class EpicRMWeapons {

    public static final ItemPresetRegister WEAPONS = ItemPresetRegister.create(KingdomKeysReMind.MODID);

    public static final DeferredWeapon KEYBLADE = WEAPONS.registerWeapon("xephiro",
            () -> WeaponCapability.builder()
                    .category(EpicKKWeaponEnum.KK_KEYBLADE)
                    .hitSound(EpicFightSounds.BLADE_HIT)
                    .collider(KKCollider.KEYBLADE)
                    .setTierValues(0, 10d, 0.7, 0.3)
                    .parent(EpicKKWeapons.KEYBLADE)
                    .addTag(EpicFight.identifier("xephiro"))
                    .addTag(EpicFight.identifier("kk_keyblade"))
                    .addTag(EpicFight.identifier("kk_weapon"))
                    .addMoveset(RMStyle.XEPHIRO_SINGLE, EpicRMMovesets.XEPHIRO_1HAND_MOVESET)
                    .addConditionals(EpicRMConditionals.XEPHIRO_STYLE)
    );



    public enum EpicRMWeaponEnum implements WeaponCategory {
        XEPHIRO;
        private final int id;

        EpicRMWeaponEnum() {
            this.id = WeaponCategory.ENUM_MANAGER.assign(this);
        }

        @Override
        public int universalOrdinal() {
            return id;
        }
    }
}
//TODO: Help with EFM on KK and find more imports or methods to fix efm for ReMind.