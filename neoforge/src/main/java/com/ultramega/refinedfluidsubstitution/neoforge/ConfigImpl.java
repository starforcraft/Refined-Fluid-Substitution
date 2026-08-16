package com.ultramega.refinedfluidsubstitution.neoforge;

import com.ultramega.refinedfluidsubstitution.common.Config;

import net.neoforged.neoforge.common.ModConfigSpec;

import static com.ultramega.refinedfluidsubstitution.common.FluidSubstitutionIdentifierUtil.createFluidSubstitutionTranslationKey;

public class ConfigImpl implements Config {
    private final ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
    private final ModConfigSpec spec;

    public ConfigImpl() {
        this.spec = this.builder.build();
    }

    public ModConfigSpec getSpec() {
        return this.spec;
    }

    private static String translationKey(final String value) {
        return createFluidSubstitutionTranslationKey("text.autoconfig", "option." + value);
    }
}
