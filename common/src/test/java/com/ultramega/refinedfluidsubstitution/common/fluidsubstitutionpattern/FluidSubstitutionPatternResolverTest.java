package com.ultramega.refinedfluidsubstitution.common.fluidsubstitutionpattern;

import com.refinedmods.refinedstorage.api.autocrafting.Ingredient;
import com.refinedmods.refinedstorage.api.autocrafting.Pattern;
import com.refinedmods.refinedstorage.api.autocrafting.PatternLayout;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.common.api.support.resource.FluidOperationResult;
import com.refinedmods.refinedstorage.common.support.resource.FluidResource;
import com.refinedmods.refinedstorage.common.support.resource.ItemResource;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static com.ultramega.refinedfluidsubstitution.common.fluidsubstitutionpattern.FluidSubstitutionPatternResolver.createHelperPatternUUID;
import static org.assertj.core.api.Assertions.assertThat;

@MinecraftRegistriesTest
class FluidSubstitutionPatternResolverTest {
    private static final UUID PATTERN_ID = UUID.fromString("41ca7d2d-2cec-427a-aa84-ba2f4e4aa425");
    private static final long BUCKET_AMOUNT = 1000;

    @Test
    void substitutesFilledContainerAndCreatesFallbackHelperPattern() {
        final ItemResource waterBucket = item(Items.WATER_BUCKET);
        final ItemResource bucket = item(Items.BUCKET);
        final FluidResource water = new FluidResource(Fluids.WATER);
        final Pattern pattern = pattern(
            List.of(new Ingredient(1, List.of(waterBucket))),
            List.of(new ResourceAmount(bucket, 1))
        );

        final FluidSubstitutionPatternResolver.ResolvedPattern resolved = FluidSubstitutionPatternResolver.resolve(
            pattern,
            List.of(new ItemStack(Items.WATER_BUCKET)),
            BUCKET_AMOUNT,
            ignored -> Optional.of(drainResult(Items.BUCKET, water, BUCKET_AMOUNT))
        );

        assertThat(resolved.pattern().layout().ingredients()).containsExactly(new Ingredient(BUCKET_AMOUNT, List.of(water)));
        assertThat(resolved.pattern().layout().byproducts()).isEmpty();
        assertThat(resolved.substitutions()).containsExactly(new FluidSubstitutionPatternResolver.Substitution(0, waterBucket, water));

        assertThat(resolved.helperPatterns()).hasSize(1);
        final Pattern helper = resolved.helperPatterns().getFirst();
        assertThat(helper.id()).isEqualTo(createHelperPatternUUID(PATTERN_ID, 0));
        assertThat(helper.layout().ingredients()).containsExactly(new Ingredient(1, List.of(waterBucket)));
        assertThat(helper.layout().outputs()).containsExactly(new ResourceAmount(water, BUCKET_AMOUNT));
        assertThat(helper.layout().byproducts()).containsExactly(new ResourceAmount(bucket, 1));
    }

    @Test
    void substitutesRepeatedContainersAndRemovesAllMatchingRemainders() {
        final ItemResource waterBucket = item(Items.WATER_BUCKET);
        final ItemResource bucket = item(Items.BUCKET);
        final FluidResource water = new FluidResource(Fluids.WATER);
        final Pattern pattern = pattern(
            List.of(
                new Ingredient(1, List.of(waterBucket)),
                new Ingredient(1, List.of(waterBucket)),
                new Ingredient(1, List.of(waterBucket))
            ),
            List.of(new ResourceAmount(bucket, 3))
        );

        final FluidSubstitutionPatternResolver.ResolvedPattern resolved = FluidSubstitutionPatternResolver.resolve(
            pattern,
            List.of(
                new ItemStack(Items.WATER_BUCKET),
                new ItemStack(Items.WATER_BUCKET),
                new ItemStack(Items.WATER_BUCKET)
            ),
            BUCKET_AMOUNT,
            ignored -> Optional.of(drainResult(Items.BUCKET, water, BUCKET_AMOUNT))
        );

        assertThat(resolved.pattern().layout().ingredients()).containsExactly(
            new Ingredient(BUCKET_AMOUNT, List.of(water)),
            new Ingredient(BUCKET_AMOUNT, List.of(water)),
            new Ingredient(BUCKET_AMOUNT, List.of(water))
        );
        assertThat(resolved.pattern().layout().byproducts()).isEmpty();
        assertThat(resolved.helperPatterns()).hasSize(3);
        assertThat(resolved.substitutions()).extracting(FluidSubstitutionPatternResolver.Substitution::slot).containsExactly(0, 1, 2);
    }

    @Test
    void onlySubstitutesInputsWithAvailableContainerRemainders() {
        final ItemResource waterBucket = item(Items.WATER_BUCKET);
        final ItemResource bucket = item(Items.BUCKET);
        final FluidResource water = new FluidResource(Fluids.WATER);
        final Pattern pattern = pattern(
            List.of(
                new Ingredient(1, List.of(waterBucket)),
                new Ingredient(1, List.of(waterBucket)),
                new Ingredient(1, List.of(waterBucket))
            ),
            List.of(new ResourceAmount(bucket, 2))
        );

        final FluidSubstitutionPatternResolver.ResolvedPattern resolved = FluidSubstitutionPatternResolver.resolve(
            pattern,
            List.of(
                new ItemStack(Items.WATER_BUCKET),
                new ItemStack(Items.WATER_BUCKET),
                new ItemStack(Items.WATER_BUCKET)
            ),
            BUCKET_AMOUNT,
            ignored -> Optional.of(drainResult(Items.BUCKET, water, BUCKET_AMOUNT))
        );

        assertThat(resolved.pattern().layout().ingredients()).containsExactly(
            new Ingredient(BUCKET_AMOUNT, List.of(water)),
            new Ingredient(BUCKET_AMOUNT, List.of(water)),
            new Ingredient(1, List.of(waterBucket))
        );
        assertThat(resolved.pattern().layout().byproducts()).isEmpty();
        assertThat(resolved.helperPatterns()).hasSize(2);
        assertThat(resolved.substitutions()).extracting(FluidSubstitutionPatternResolver.Substitution::slot).containsExactly(0, 1);
    }

    @Test
    void preservesUnrelatedByproducts() {
        final ItemResource waterBucket = item(Items.WATER_BUCKET);
        final ItemResource bucket = item(Items.BUCKET);
        final ItemResource diamond = item(Items.DIAMOND);
        final FluidResource water = new FluidResource(Fluids.WATER);
        final Pattern pattern = pattern(
            List.of(new Ingredient(1, List.of(waterBucket))),
            List.of(
                new ResourceAmount(bucket, 1),
                new ResourceAmount(diamond, 2)
            )
        );

        final FluidSubstitutionPatternResolver.ResolvedPattern resolved = FluidSubstitutionPatternResolver.resolve(
            pattern,
            List.of(new ItemStack(Items.WATER_BUCKET)),
            BUCKET_AMOUNT,
            ignored -> Optional.of(drainResult(Items.BUCKET, water, BUCKET_AMOUNT))
        );

        assertThat(resolved.pattern().layout().byproducts()).containsExactly(new ResourceAmount(diamond, 2));
    }

    @Test
    void doesNotSubstituteWhenContainerRemainderIsMissing() {
        final ItemResource waterBucket = item(Items.WATER_BUCKET);
        final FluidResource water = new FluidResource(Fluids.WATER);
        final Pattern pattern = pattern(
            List.of(new Ingredient(1, List.of(waterBucket))),
            List.of()
        );

        final FluidSubstitutionPatternResolver.ResolvedPattern resolved = FluidSubstitutionPatternResolver.resolve(
            pattern,
            List.of(new ItemStack(Items.WATER_BUCKET)),
            BUCKET_AMOUNT,
            ignored -> Optional.of(drainResult(Items.BUCKET, water, BUCKET_AMOUNT))
        );

        assertThat(resolved.pattern()).isSameAs(pattern);
        assertThat(resolved.helperPatterns()).isEmpty();
        assertThat(resolved.substitutions()).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(longs = {1, 999, 1001, 2000})
    void doesNotSubstituteWhenDrainedAmountIsNotExactlyOneBucket(final long drainedAmount) {
        final ItemResource waterBucket = item(Items.WATER_BUCKET);
        final ItemResource bucket = item(Items.BUCKET);
        final FluidResource water = new FluidResource(Fluids.WATER);
        final Pattern pattern = pattern(
            List.of(new Ingredient(1, List.of(waterBucket))),
            List.of(new ResourceAmount(bucket, 1))
        );

        final FluidSubstitutionPatternResolver.ResolvedPattern resolved = FluidSubstitutionPatternResolver.resolve(
            pattern,
            List.of(new ItemStack(Items.WATER_BUCKET)),
            BUCKET_AMOUNT,
            ignored -> Optional.of(drainResult(Items.BUCKET, water, drainedAmount))
        );

        assertThat(resolved.pattern()).isSameAs(pattern);
        assertThat(resolved.helperPatterns()).isEmpty();
        assertThat(resolved.substitutions()).isEmpty();
    }

    @Test
    void substitutesContainerWithoutRemainder() {
        final ItemResource waterBucket = item(Items.WATER_BUCKET);
        final FluidResource water = new FluidResource(Fluids.WATER);
        final Pattern pattern = pattern(
            List.of(new Ingredient(1, List.of(waterBucket))),
            List.of()
        );

        final FluidSubstitutionPatternResolver.ResolvedPattern resolved = FluidSubstitutionPatternResolver.resolve(
            pattern,
            List.of(new ItemStack(Items.WATER_BUCKET)),
            BUCKET_AMOUNT,
            ignored -> Optional.of(new FluidOperationResult(ItemStack.EMPTY, water, BUCKET_AMOUNT))
        );

        assertThat(resolved.pattern().layout().ingredients()).containsExactly(new Ingredient(BUCKET_AMOUNT, List.of(water)));
        assertThat(resolved.helperPatterns()).hasSize(1);
        assertThat(resolved.helperPatterns().getFirst().layout().byproducts()).isEmpty();
    }

    @Test
    void keepsGridSlotIndexWhenCraftingInputContainsEmptySlots() {
        final ItemResource waterBucket = item(Items.WATER_BUCKET);
        final ItemResource sugar = item(Items.SUGAR);
        final ItemResource bucket = item(Items.BUCKET);
        final FluidResource water = new FluidResource(Fluids.WATER);
        final Pattern pattern = pattern(
            List.of(
                new Ingredient(1, List.of(waterBucket)),
                new Ingredient(1, List.of(sugar))
            ),
            List.of(new ResourceAmount(bucket, 1))
        );

        final FluidSubstitutionPatternResolver.ResolvedPattern resolved = FluidSubstitutionPatternResolver.resolve(
            pattern,
            List.of(
                ItemStack.EMPTY,
                new ItemStack(Items.WATER_BUCKET),
                ItemStack.EMPTY,
                new ItemStack(Items.SUGAR)
            ),
            BUCKET_AMOUNT,
            stack -> stack.is(Items.WATER_BUCKET)
                ? Optional.of(drainResult(Items.BUCKET, water, BUCKET_AMOUNT))
                : Optional.empty()
        );

        assertThat(resolved.pattern().layout().ingredients()).containsExactly(
            new Ingredient(BUCKET_AMOUNT, List.of(water)),
            new Ingredient(1, List.of(sugar))
        );
        assertThat(resolved.substitutions()).containsExactly(
            new FluidSubstitutionPatternResolver.Substitution(1, waterBucket, water)
        );
        assertThat(resolved.helperPatterns()).hasSize(1);
        assertThat(resolved.helperPatterns().getFirst().id()).isEqualTo(createHelperPatternUUID(PATTERN_ID, 1));
    }

    @Test
    void supportsDifferentFluidsInTheSamePattern() {
        final ItemResource waterBucket = item(Items.WATER_BUCKET);
        final ItemResource lavaBucket = item(Items.LAVA_BUCKET);
        final ItemResource bucket = item(Items.BUCKET);
        final FluidResource water = new FluidResource(Fluids.WATER);
        final FluidResource lava = new FluidResource(Fluids.LAVA);
        final Pattern pattern = pattern(
            List.of(
                new Ingredient(1, List.of(waterBucket)),
                new Ingredient(1, List.of(lavaBucket))
            ),
            List.of(new ResourceAmount(bucket, 2))
        );

        final FluidSubstitutionPatternResolver.ResolvedPattern resolved = FluidSubstitutionPatternResolver.resolve(
            pattern,
            List.of(
                new ItemStack(Items.WATER_BUCKET),
                new ItemStack(Items.LAVA_BUCKET)
            ),
            BUCKET_AMOUNT,
            stack -> {
                if (stack.is(Items.WATER_BUCKET)) {
                    return Optional.of(drainResult(Items.BUCKET, water, BUCKET_AMOUNT));
                }
                if (stack.is(Items.LAVA_BUCKET)) {
                    return Optional.of(drainResult(Items.BUCKET, lava, BUCKET_AMOUNT));
                }
                return Optional.empty();
            }
        );

        assertThat(resolved.pattern().layout().ingredients()).containsExactly(
            new Ingredient(BUCKET_AMOUNT, List.of(water)),
            new Ingredient(BUCKET_AMOUNT, List.of(lava))
        );
        assertThat(resolved.pattern().layout().byproducts()).isEmpty();
        assertThat(resolved.helperPatterns()).extracting(helper -> helper.layout().outputs().getFirst())
            .containsExactly(
                new ResourceAmount(water, BUCKET_AMOUNT),
                new ResourceAmount(lava, BUCKET_AMOUNT)
            );
    }

    @Test
    void leavesPatternUnchangedWhenNoEncodedInputCanBeDrained() {
        final ItemResource sugar = item(Items.SUGAR);
        final Pattern pattern = pattern(
            List.of(new Ingredient(1, List.of(sugar))),
            List.of()
        );

        final FluidSubstitutionPatternResolver.ResolvedPattern resolved = FluidSubstitutionPatternResolver.resolve(
            pattern,
            List.of(new ItemStack(Items.SUGAR)),
            BUCKET_AMOUNT,
            ignored -> Optional.empty()
        );

        assertThat(resolved.pattern()).isSameAs(pattern);
        assertThat(resolved.helperPatterns()).isEmpty();
        assertThat(resolved.substitutions()).isEmpty();
    }

    private static Pattern pattern(final List<Ingredient> ingredients, final List<ResourceAmount> byproducts) {
        return new Pattern(PATTERN_ID, PatternLayout.internal(
            ingredients,
            List.of(new ResourceAmount(item(Items.DIAMOND), 1)),
            byproducts
        ));
    }

    private static ItemResource item(final Item item) {
        return ItemResource.ofItemStack(new ItemStack(item));
    }

    private static FluidOperationResult drainResult(final Item container,
                                                    final FluidResource fluid,
                                                    final long amount) {
        return new FluidOperationResult(new ItemStack(container), fluid, amount);
    }
}
