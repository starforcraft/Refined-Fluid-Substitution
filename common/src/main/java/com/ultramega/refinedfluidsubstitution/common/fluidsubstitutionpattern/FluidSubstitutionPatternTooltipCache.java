package com.ultramega.refinedfluidsubstitution.common.fluidsubstitutionpattern;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.jspecify.annotations.Nullable;

public final class FluidSubstitutionPatternTooltipCache {
    private static final int MAX_ENTRIES = 1000;
    private static final Map<UUID, Map<Integer, FluidSubstitutionPatternResolver.Substitution>> CACHE = new HashMap<>();

    private FluidSubstitutionPatternTooltipCache() {
    }

    public static boolean contains(final UUID patternId) {
        return CACHE.containsKey(patternId);
    }

    public static void put(final UUID patternId,
                           final List<FluidSubstitutionPatternResolver.Substitution> substitutions) {
        if (CACHE.size() > MAX_ENTRIES) {
            CACHE.clear();
        }

        final Map<Integer, FluidSubstitutionPatternResolver.Substitution> substitutionsBySlot = new HashMap<>();
        for (final FluidSubstitutionPatternResolver.Substitution substitution : substitutions) {
            substitutionsBySlot.put(substitution.slot(), substitution);
        }
        CACHE.put(patternId, Map.copyOf(substitutionsBySlot));
    }

    public static FluidSubstitutionPatternResolver.@Nullable Substitution getSubstitution(final UUID patternId,
                                                                                          final int slot) {
        final Map<Integer, FluidSubstitutionPatternResolver.Substitution> substitutionsBySlot = CACHE.get(patternId);
        return substitutionsBySlot == null ? null : substitutionsBySlot.get(slot);
    }
}
