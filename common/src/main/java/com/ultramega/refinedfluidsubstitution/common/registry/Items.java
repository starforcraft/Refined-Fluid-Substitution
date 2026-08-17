package com.ultramega.refinedfluidsubstitution.common.registry;

import com.ultramega.refinedfluidsubstitution.common.fluidsubstitutionpattern.FluidSubstitutionPatternItem;

import java.util.function.Supplier;
import javax.annotation.Nullable;

import static java.util.Objects.requireNonNull;

public final class Items {
    public static final Items INSTANCE = new Items();

    @Nullable
    private Supplier<FluidSubstitutionPatternItem> fluidSubstitutionPattern;

    private Items() {
    }

    public FluidSubstitutionPatternItem getFluidSubstitutionPattern() {
        return requireNonNull(this.fluidSubstitutionPattern).get();
    }

    public void setFluidSubstitutionPattern(final Supplier<FluidSubstitutionPatternItem> supplier) {
        this.fluidSubstitutionPattern = supplier;
    }
}
