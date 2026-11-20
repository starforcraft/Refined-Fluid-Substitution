package com.ultramega.refinedfluidsubstitution.common.mixin;

import com.ultramega.refinedfluidsubstitution.common.fluidsubstitutionpattern.FluidSubstitutionPatternItem;
import com.ultramega.refinedfluidsubstitution.common.fluidsubstitutionpattern.FluidSubstitutionPatternResolver;

import com.refinedmods.refinedstorage.api.autocrafting.Pattern;
import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;

import java.util.Optional;

import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Desc;
import org.spongepowered.asm.mixin.injection.Redirect;

@Pseudo
@Mixin(targets = "com.ultramega.stepcrafter.common.stepcrafter.StepCrafterBlockEntity", remap = false)
public abstract class StepCrafterBlockEntityMixin {
    @Unique
    private boolean rfs$resolvingFluidSubstitution;

    @Shadow
    public abstract void patternChanged(int slot);

    @Redirect(method = "patternChanged",
        at = @At(value = "INVOKE", desc = @Desc(owner = RefinedStorageApi.class, value = "getPattern", args = {ItemStack.class, Level.class}, ret = Optional.class)))
    private Optional<Pattern> rfs$resolveFluidSubstitution(final RefinedStorageApi api,
                                                           final ItemStack stack,
                                                           final Level level,
                                                           final int slot) {
        if (!(stack.getItem() instanceof FluidSubstitutionPatternItem) || !(level instanceof ServerLevel serverLevel)) {
            return api.getPattern(stack, level);
        }
        if (this.rfs$resolvingFluidSubstitution) {
            return FluidSubstitutionPatternResolver.resolve(stack, level)
                .map(FluidSubstitutionPatternResolver.ResolvedPattern::pattern);
        }

        final BlockEntity crafter = (BlockEntity) (Object) this;
        final var server = serverLevel.getServer();
        server.tell(new TickTask(server.getTickCount(), () -> {
            if (crafter.isRemoved() || crafter.getLevel() != serverLevel) {
                return;
            }
            this.rfs$resolvingFluidSubstitution = true;
            try {
                this.patternChanged(slot);
            } finally {
                this.rfs$resolvingFluidSubstitution = false;
            }
        }));
        return Optional.empty();
    }
}
