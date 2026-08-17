package com.ultramega.refinedfluidsubstitution.common.mixin;

import com.ultramega.refinedfluidsubstitution.common.fluidsubstitutionpattern.FluidSubstitutionPatternItem;
import com.ultramega.refinedfluidsubstitution.common.fluidsubstitutionpattern.FluidSubstitutionPatternResolver;
import com.ultramega.refinedfluidsubstitution.common.util.MainNetworkNodeAccessor;
import com.ultramega.refinedfluidsubstitution.common.util.PatternProviderNetworkNodeExtension;

import com.refinedmods.refinedstorage.api.autocrafting.Pattern;
import com.refinedmods.refinedstorage.api.autocrafting.PatternLayout;
import com.refinedmods.refinedstorage.api.network.impl.node.patternprovider.PatternProviderNetworkNode;
import com.refinedmods.refinedstorage.common.autocrafting.autocrafter.AutocrafterBlockEntity;
import com.refinedmods.refinedstorage.common.support.FilteredContainer;

import java.util.LinkedHashMap;
import java.util.Map;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(
    value = AutocrafterBlockEntity.class,
    targets = "com.ultramega.cabletiers.common.autocrafting.autocrafter.TieredAutocrafterBlockEntity",
    remap = false
)
public abstract class AutocrafterBlockEntityMixin {
    @Unique
    private static final int MAX_HELPERS_PER_PATTERN = 9;

    /** Fluid-container inspection must not happen synchronously from PatternInventory#setChanged.
     * NeoForge shift-click insertion performs that change while a root item-transfer transaction is still open,
     * whereas Refined Storage's Platform#drainContainer opens another root transaction. Deferring one tick keeps the two transactions from nesting */
    @Unique
    private boolean rfs$fluidSubstitutionRefreshPending;

    @Shadow(remap = false)
    public abstract FilteredContainer getPatternContainer();

    @Inject(method = "onPatternChanged", at = @At("RETURN"))
    private void rfs$queueFluidSubstitutionRefresh(final CallbackInfo ci) {
        this.rfs$fluidSubstitutionRefreshPending = true;
    }

    @Inject(method = "doWork", at = @At("HEAD"))
    private void rfs$refreshFluidSubstitutionsOutsideTransferTransaction(final CallbackInfo ci) {
        if (!this.rfs$fluidSubstitutionRefreshPending) {
            return;
        }
        this.rfs$fluidSubstitutionRefreshPending = false;
        this.rfs$updateFluidSubstitutionPatterns();
    }

    @Unique
    private void rfs$updateFluidSubstitutionPatterns() {
        final BlockEntity autocrafter = (BlockEntity) (Object) this;
        final Level level = autocrafter.getLevel();
        if (level == null || level.isClientSide() || !(autocrafter instanceof MainNetworkNodeAccessor networkNodeAccessor)) {
            return;
        }

        final FilteredContainer patterns = this.getPatternContainer();
        final int physicalPatternCount = patterns.getContainerSize();
        final PatternProviderNetworkNode node = (PatternProviderNetworkNode) networkNodeAccessor.rfs$getMainNetworkNode();
        final PatternProviderNetworkNodeExtension extension = (PatternProviderNetworkNodeExtension) node;

        final Map<PatternLayout, Pattern> helpers = new LinkedHashMap<>();
        for (int slot = 0; slot < physicalPatternCount; ++slot) {
            final ItemStack stack = patterns.getItem(slot);
            if (!(stack.getItem() instanceof FluidSubstitutionPatternItem)) {
                continue;
            }

            final int patternSlot = slot;
            FluidSubstitutionPatternResolver.resolve(stack, level).ifPresent(resolved -> {
                node.tryUpdatePattern(patternSlot, resolved.pattern());
                for (final Pattern helper : resolved.helperPatterns()) {
                    helpers.putIfAbsent(helper.layout(), helper);
                }
            });
        }

        if (!helpers.isEmpty()) {
            extension.rfs$ensurePatternCapacity(physicalPatternCount * (1 + MAX_HELPERS_PER_PATTERN));
        }

        int helperIndex = physicalPatternCount;
        for (final Pattern helper : helpers.values()) {
            if (helperIndex >= extension.rfs$getPatternCapacity()) {
                break;
            }
            node.tryUpdatePattern(helperIndex++, helper);
        }
        while (helperIndex < extension.rfs$getPatternCapacity()) {
            node.tryUpdatePattern(helperIndex++, null);
        }
    }
}
