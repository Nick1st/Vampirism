package de.teamlapen.vampirism.api.entity.vampire;

import de.teamlapen.vampirism.api.EnumStrength;
import de.teamlapen.vampirism.api.VReference;
import de.teamlapen.vampirism.api.entity.factions.IFaction;
import de.teamlapen.vampirism.api.entity.factions.IFactionEntity;
import net.minecraft.world.IWorld;

import javax.annotation.Nonnull;

/**
 * Implemented by all vampire entities
 */
public interface IVampire extends IFactionEntity {

    /**
     * @param strength
     * @return True if the entity is not affected by that garlic level
     */
    boolean doesResistGarlic(EnumStrength strength);

    /**
     * Adds blood to the vampires blood stats
     * Consume blood. Any remaining blood might be filled into blood bottles or used otherwise
     *
     * @param amt           In blood food unit, not mB. See {@link de.teamlapen.vampirism.api.VReference#FOOD_TO_FLUID_BLOOD} for conversion
     * @param saturationMod Similar to the food saturation modifier
     */
    default void drinkBlood(int amt, float saturationMod) {
        drinkBlood(amt, saturationMod, true);
    }

    /**
     * Adds blood to the vampires blood stats.
     * If useRemaining is true, any remaining blood might be used otherwise. For example it might be put into blood bottles
     *
     * @param amt           In blood food unit, not mB. See {@link de.teamlapen.vampirism.api.VReference#FOOD_TO_FLUID_BLOOD} for conversion
     * @param saturationMod Similar to the food saturation modifier
     */
    void drinkBlood(int amt, float saturationMod, boolean useRemaining);

    @Override
    default IFaction getFaction() {
        return VReference.VAMPIRE_FACTION;
    }

    /**
     * @return Whether the entity is a skilled biter which  is able to suck blood more efficiently
     */
    default boolean isAdvancedBiter() {
        return false;
    }

    /**
     * Checks if the player is being affected by garlic.
     * Result is cached for a few ticks
     * <p>
     * For VampirePlayer instances for players with vampire level 0 this returns {@link EnumStrength#NONE}
     *
     * @return The strength of the garlic or {@link EnumStrength#NONE}
     */
    @Nonnull
    default EnumStrength isGettingGarlicDamage(IWorld world) {
        return isGettingGarlicDamage(world, false);
    }

    /**
     * Checks if the player is being affected by garlic.
     * The result is cached for several ticks unless you use forcerefresh
     * Careful, this checks quite a large area of blocks and should not be refreshed to often
     * <p>
     * For VampirePlayer instances for players with vampire level 0 this returns {@link EnumStrength#NONE}
     *
     * @param forceRefresh Don't use cached value
     * @return The strength of the garlic or {@link EnumStrength#NONE}
     */
    @Nonnull
    EnumStrength isGettingGarlicDamage(IWorld iWorld, boolean forceRefresh);

    /**
     * Checks if all requirements are met for the entity to be damaged by the sun, e.g. standing in the sun and not raining.
     * The result is cached for a few ticks unless you use forcerefresh
     * <p>
     * For VampirePlayer instances for players with vampire level 0 this returns false
     *
     * @param forceRefresh Don't use cached value
     */
    boolean isGettingSundamage(IWorld iWorld, boolean forceRefresh);

    /**
     * Checks if all requirements are met for the entity to be damaged by the sun, e.g. standing in the sun and not raining.
     * The result is cached for a few ticks.
     * <p>
     * For VampirePlayer instances for players with vampire level 0 this returns false
     */
    default boolean isGettingSundamage(IWorld iWorld) {
        return isGettingSundamage(iWorld, false);
    }

    /**
     * If the entity currently does not care about being damaged by the sun, because it is e.g. angry or has sunscreen
     */
    boolean isIgnoringSundamage();

    /**
     * Consumes blood (removes).
     * Unless allowPartial is true, blood is only consumed if enough is available
     *
     * @param amt          In blood food unit, not mB. See {@link de.teamlapen.vampirism.api.VReference#FOOD_TO_FLUID_BLOOD} for conversion
     * @param allowPartial If true, the method removes as much blood as available up to the given limit
     * @return If amt was removed
     */
    boolean useBlood(int amt, boolean allowPartial);

    /**
     * @return If the creature wants blood or could use some
     */
    boolean wantsBlood();
}
