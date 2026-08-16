package com.ultramega.refinedfluidsubstitution.common.fluidsubstitutionpattern;

import com.refinedmods.refinedstorage.api.autocrafting.Ingredient;
import com.refinedmods.refinedstorage.api.autocrafting.Pattern;
import com.refinedmods.refinedstorage.api.autocrafting.PatternLayout;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.common.Platform;
import com.refinedmods.refinedstorage.common.api.support.resource.FluidOperationResult;
import com.refinedmods.refinedstorage.common.autocrafting.CraftingPatternState;
import com.refinedmods.refinedstorage.common.content.DataComponents;
import com.refinedmods.refinedstorage.common.content.Items;
import com.refinedmods.refinedstorage.common.support.resource.ItemResource;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.level.Level;

public final class FluidSubstitutionPatternResolver {
    private FluidSubstitutionPatternResolver() {
    }

    public static Optional<ResolvedPattern> resolve(final ItemStack stack, final Level level) {
        final Optional<Pattern> basePattern = Items.INSTANCE.getPattern().getPattern(stack, level);
        if (basePattern.isEmpty()) {
            return Optional.empty();
        }

        final Pattern pattern = basePattern.get();
        final CraftingPatternState craftingState = stack.get(DataComponents.INSTANCE.getCraftingPatternState());
        if (craftingState == null) {
            return Optional.of(new ResolvedPattern(pattern, List.of(), List.of()));
        }
        final CraftingInput craftingInput = craftingState.input().input();
        final List<ItemStack> encodedInputs = new ArrayList<>(craftingInput.size());
        for (int slot = 0; slot < craftingInput.size(); ++slot) {
            encodedInputs.add(craftingInput.getItem(slot));
        }
        return Optional.of(resolve(
            pattern,
            encodedInputs,
            Platform.INSTANCE.getBucketAmount(),
            Platform.INSTANCE::drainContainer
        ));
    }

    static ResolvedPattern resolve(final Pattern pattern,
                                   final List<ItemStack> encodedInputs,
                                   final long bucketAmount,
                                   final Function<ItemStack, Optional<FluidOperationResult>> containerDrainer) {
        final List<Ingredient> ingredients = new ArrayList<>(pattern.layout().ingredients());
        final List<ResourceAmount> byproducts = new ArrayList<>(pattern.layout().byproducts());
        final List<Pattern> helperPatterns = new ArrayList<>();
        final List<Substitution> substitutions = new ArrayList<>();

        int ingredientIndex = 0;
        for (int slot = 0; slot < encodedInputs.size(); ++slot) {
            final ItemStack encodedInput = encodedInputs.get(slot);
            if (encodedInput.isEmpty()) {
                continue;
            }
            if (ingredientIndex >= ingredients.size()) {
                return new ResolvedPattern(pattern, List.of(), List.of());
            }

            final int currentIngredientIndex = ingredientIndex++;
            final Optional<FluidOperationResult> drained = containerDrainer.apply(encodedInput.copyWithCount(1));
            if (drained.isEmpty() || drained.get().amount() != bucketAmount) {
                continue;
            }

            final FluidOperationResult result = drained.get();
            if (!canRemoveContainerRemainder(byproducts, result.container())) {
                continue;
            }

            removeContainerRemainder(byproducts, result.container());
            final Ingredient originalIngredient = ingredients.get(currentIngredientIndex);
            ingredients.set(currentIngredientIndex, new Ingredient(
                Math.multiplyExact(originalIngredient.amount(), bucketAmount),
                List.of(result.fluid())
            ));
            helperPatterns.add(createHelperPattern(pattern.id(), slot, encodedInput, result, bucketAmount));
            substitutions.add(new Substitution(
                slot,
                ItemResource.ofItemStack(encodedInput.copyWithCount(1)),
                result.fluid()
            ));
        }

        if (helperPatterns.isEmpty()) {
            return new ResolvedPattern(pattern, List.of(), List.of());
        }

        final Pattern substituted = new Pattern(
            pattern.id(),
            new PatternLayout(
                ingredients,
                pattern.layout().outputs(),
                byproducts,
                pattern.layout().type()
            )
        );
        return new ResolvedPattern(substituted, helperPatterns, substitutions);
    }

    private static Pattern createHelperPattern(final UUID parentPatternId,
                                               final int slot,
                                               final ItemStack encodedInput,
                                               final FluidOperationResult drained,
                                               final long bucketAmount) {
        final List<ResourceAmount> helperByproducts = drained.container().isEmpty()
            ? List.of()
            : List.of(new ResourceAmount(
                ItemResource.ofItemStack(drained.container()),
                drained.container().getCount()
            ));

        return new Pattern(createHelperPatternUUID(parentPatternId, slot), PatternLayout.internal(
            List.of(new Ingredient(1, List.of(ItemResource.ofItemStack(encodedInput.copyWithCount(1))))),
            List.of(new ResourceAmount(drained.fluid(), bucketAmount)),
            helperByproducts
        ));
    }

    static UUID createHelperPatternUUID(final UUID parentPatternId, final int slot) {
        return UUID.nameUUIDFromBytes((parentPatternId + ":fluid-substitution:" + slot).getBytes(StandardCharsets.UTF_8));
    }

    private static boolean canRemoveContainerRemainder(final List<ResourceAmount> byproducts,
                                                       final ItemStack drainedContainer) {
        if (drainedContainer.isEmpty()) {
            return true;
        }
        final ResourceKey remainder = ItemResource.ofItemStack(drainedContainer);
        long available = 0;
        for (final ResourceAmount byproduct : byproducts) {
            if (byproduct.resource().equals(remainder)) {
                available += byproduct.amount();
            }
        }
        return available >= drainedContainer.getCount();
    }

    private static void removeContainerRemainder(final List<ResourceAmount> byproducts,
                                                 final ItemStack drainedContainer) {
        if (drainedContainer.isEmpty()) {
            return;
        }
        final ResourceKey remainder = ItemResource.ofItemStack(drainedContainer);
        long toRemove = drainedContainer.getCount();
        for (int i = 0; i < byproducts.size() && toRemove > 0; ++i) {
            final ResourceAmount byproduct = byproducts.get(i);
            if (!byproduct.resource().equals(remainder)) {
                continue;
            }
            final long removed = Math.min(toRemove, byproduct.amount());
            final long remaining = byproduct.amount() - removed;
            toRemove -= removed;
            if (remaining == 0) {
                byproducts.remove(i--);
            } else {
                byproducts.set(i, new ResourceAmount(byproduct.resource(), remaining));
            }
        }
    }

    public record ResolvedPattern(Pattern pattern, List<Pattern> helperPatterns, List<Substitution> substitutions) {
        public ResolvedPattern {
            helperPatterns = List.copyOf(helperPatterns);
            substitutions = List.copyOf(substitutions);
        }
    }

    public record Substitution(int slot, ItemResource container, ResourceKey fluid) {
    }
}
