package com.ultramega.refinedfluidsubstitution.common.mixin;

import com.ultramega.refinedfluidsubstitution.common.fluidsubstitutionpattern.FluidSubstitutionPatternItem;
import com.ultramega.refinedfluidsubstitution.common.registry.Items;

import com.refinedmods.refinedstorage.common.autocrafting.patterngrid.PatternGridBlockEntity;
import com.refinedmods.refinedstorage.common.support.FilteredContainer;

import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = PatternGridBlockEntity.class, remap = false)
public abstract class PatternGridBlockEntityMixin {
    @Shadow(remap = false)
    @Final
    private FilteredContainer patternInput;

    @Shadow(remap = false)
    @Final
    private FilteredContainer patternOutput;

    @Unique
    private boolean rfs$creatingFluidSubstitutionPattern;

    @Inject(method = "isValidPattern", at = @At("HEAD"), cancellable = true)
    private static void rfs$acceptFluidSubstitutionPattern(final ItemStack stack,
                                                            final CallbackInfoReturnable<Boolean> cir) {
        if (stack.getItem() instanceof FluidSubstitutionPatternItem) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "createPattern", at = @At("HEAD"))
    private void rfs$rememberPatternKind(final CallbackInfo ci) {
        this.rfs$creatingFluidSubstitutionPattern = rfs$isFluidSubstitutionPattern(this.patternInput.getItem(0))
            || rfs$isFluidSubstitutionPattern(this.patternOutput.getItem(0));
    }

    @Inject(method = "createPattern", at = @At("RETURN"))
    private void rfs$restoreFluidSubstitutionPatternItem(final CallbackInfo ci) {
        if (!this.rfs$creatingFluidSubstitutionPattern) {
            return;
        }
        final ItemStack output = this.patternOutput.getItem(0);
        if (!output.isEmpty() && !rfs$isFluidSubstitutionPattern(output)) {
            this.patternOutput.setItem(0, output.transmuteCopy(Items.INSTANCE.getFluidSubstitutionPattern()));
        }
        this.rfs$creatingFluidSubstitutionPattern = false;
    }

    @Unique
    private static boolean rfs$isFluidSubstitutionPattern(final ItemStack stack) {
        return stack.getItem() instanceof FluidSubstitutionPatternItem;
    }
}
