package com.ultramega.refinedfluidsubstitution.common.mixin;

import com.ultramega.refinedfluidsubstitution.common.fluidsubstitutionpattern.FluidSubstitutionPatternItem;

import com.refinedmods.refinedstorage.common.content.DataComponents;

import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "com.refinedmods.refinedstorage.common.autocrafting.patterngrid.PatternOutputContainer", remap = false)
public abstract class PatternOutputContainerMixin {
    @Inject(method = "canPlaceItem", at = @At("HEAD"), cancellable = true)
    private void acceptMappedFluidSubstitutionPattern(final int slot,
                                                      final ItemStack stack,
                                                      final CallbackInfoReturnable<Boolean> cir) {
        if (stack.getItem() instanceof FluidSubstitutionPatternItem && stack.has(DataComponents.INSTANCE.getPatternState())) {
            cir.setReturnValue(true);
        }
    }
}
