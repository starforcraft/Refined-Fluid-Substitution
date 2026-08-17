package com.ultramega.refinedfluidsubstitution.common.mixin;

import com.ultramega.refinedfluidsubstitution.common.util.PatternProviderNetworkNodeExtension;

import com.refinedmods.refinedstorage.api.autocrafting.Pattern;
import com.refinedmods.refinedstorage.api.network.impl.node.patternprovider.PatternProviderNetworkNode;

import java.util.Arrays;

import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = PatternProviderNetworkNode.class, remap = false)
public abstract class PatternProviderNetworkNodeMixin implements PatternProviderNetworkNodeExtension {
    @Shadow(remap = false)
    @Final
    @Mutable
    private Pattern @Nullable[] patterns;

    @Override
    public void rfs$ensurePatternCapacity(final int capacity) {
        if (this.patterns == null || this.patterns.length >= capacity) {
            return;
        }
        this.patterns = Arrays.copyOf(this.patterns, capacity);
    }

    @Override
    public int rfs$getPatternCapacity() {
        return this.patterns == null ? 0 : this.patterns.length;
    }
}
