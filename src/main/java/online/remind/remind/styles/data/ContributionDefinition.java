package online.remind.remind.styles.data;

import net.minecraft.resources.ResourceLocation;
import online.remind.remind.styles.StyleElement;

import java.util.Map;
import java.util.Set;

/**
 * @param target         The actual registry ID this contribution applies to (e.g. kingdomkeys:magic_fire)
 * @param elements       Elements this contribution applies to (FIRE, STRIKE, PHYSICAL, etc.)
 * @param specificStyles Specific Styles this contribution applies to (kkremind:form_firestorm, kkremind:form_dark_impulse, etc.)
 * @param baseValue      Base SGauge value (Level 1)
 * @param perLevelBonus  SGauge added per level above 1
 * @param levelOverrides Optional overrides for specific levels
 */
public record ContributionDefinition(ResourceLocation target,
                                     Set<StyleElement> elements,
                                     Set<ResourceLocation> specificStyles,
                                     int baseValue,
                                     int perLevelBonus,
                                     Map<Integer, Integer> levelOverrides) {

    /**
     * Computes the SGauge value for a given spell/RC level.
     * Level is 1-based (Fire=1, Fira=2, Firaga=3, Firaza=4).
     * DEFUNCT, IGNORE
     */
    public int computeValue(int level) {
        Integer override = levelOverrides.get(level);
        if (override != null) {
            return override;
        }
        return baseValue + perLevelBonus * (level - 1);
    }

}
