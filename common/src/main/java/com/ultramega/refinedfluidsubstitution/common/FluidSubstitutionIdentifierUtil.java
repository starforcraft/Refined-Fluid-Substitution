package com.ultramega.refinedfluidsubstitution.common;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

public final class FluidSubstitutionIdentifierUtil {
    public static final String MOD_ID = "refinedfluidsubstitution";

    private FluidSubstitutionIdentifierUtil() {
    }

    public static ResourceLocation createFluidSubstitutionIdentifier(final String value) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, value);
    }

    public static MutableComponent createFluidSubstitutionTranslation(final String category, final String value) {
        return Component.translatable(createFluidSubstitutionTranslationKey(category, value));
    }

    public static String createFluidSubstitutionTranslationKey(final String category, final String value) {
        return String.format("%s.%s.%s", category, MOD_ID, value);
    }
}
