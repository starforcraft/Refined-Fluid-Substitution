package com.ultramega.refinedfluidsubstitution.common.mixin;

import com.ultramega.refinedfluidsubstitution.common.fluidsubstitutionpattern.FluidSubstitutionPatternResolver;
import com.ultramega.refinedfluidsubstitution.common.fluidsubstitutionpattern.FluidSubstitutionPatternTooltipCache;

import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.common.api.RefinedStorageClientApi;
import com.refinedmods.refinedstorage.common.autocrafting.PatternResolver;

import java.util.List;

import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "com.refinedmods.refinedstorage.common.autocrafting.CraftingPatternClientTooltipComponent", remap = false)
public abstract class CraftingPatternClientTooltipComponentMixin {
    @Unique
    private static final float FLUID_BADGE_SCALE = 0.375F;
    @Unique
    private static final int FLUID_BADGE_SIZE = 6;
    @Unique
    private static final int FLUID_BADGE_OFFSET = 11;
    @Unique
    private static final int FLUID_BADGE_BACKGROUND = 0xC0000000;

    @Shadow(remap = false)
    @Final
    private int width;

    @Shadow(remap = false)
    @Final
    private PatternResolver.ResolvedCraftingPattern pattern;

    @Shadow(remap = false)
    private int currentCycle;

    @Inject(method = "renderInputSlot", at = @At("TAIL"))
    private void rfs$renderFluidSubstitutionBadge(final int x,
                                                  final int y,
                                                  final GuiGraphics graphics,
                                                  final int sx,
                                                  final int sy,
                                                  final CallbackInfo ci) {
        final int slot = sy * this.width + sx;
        final FluidSubstitutionPatternResolver.Substitution substitution =
            FluidSubstitutionPatternTooltipCache.getSubstitution(this.pattern.pattern().id(), slot);
        if (substitution == null) {
            return;
        }

        final List<ResourceKey> inputs = this.pattern.inputs().get(slot);
        if (inputs.isEmpty()) {
            return;
        }
        final ResourceKey displayedResource = inputs.get(this.currentCycle % inputs.size());
        if (!displayedResource.equals(substitution.container())) {
            return;
        }
        final ResourceKey fluid = substitution.fluid();

        final int badgeX = x + sx * 18 + FLUID_BADGE_OFFSET;
        final int badgeY = y + sy * 18 + FLUID_BADGE_OFFSET;

        graphics.pose().pushPose();
        try {
            graphics.pose().translate(0.0F, 0.0F, 200.0F);
            graphics.fill(
                badgeX - 1,
                badgeY - 1,
                badgeX + FLUID_BADGE_SIZE + 1,
                badgeY + FLUID_BADGE_SIZE + 1,
                FLUID_BADGE_BACKGROUND
            );

            graphics.pose().translate(badgeX, badgeY, 1.0F);
            graphics.pose().scale(FLUID_BADGE_SCALE, FLUID_BADGE_SCALE, 1.0F);

            RefinedStorageClientApi.INSTANCE.getResourceRendering(fluid.getClass()).render(fluid, graphics, 0, 0);
        } finally {
            graphics.pose().popPose();
        }
    }
}
