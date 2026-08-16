package com.ultramega.refinedfluidsubstitution.neoforge.datagen.recipe;

import com.ultramega.refinedfluidsubstitution.common.registry.Items;

import java.util.concurrent.CompletableFuture;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.level.ItemLike;

public class MainRecipeProvider extends RecipeProvider {
    public MainRecipeProvider(final HolderLookup.Provider registries, final RecipeOutput output) {
        super(registries, output);
    }

    @Override
    protected void buildRecipes() {
        this.fluidSubstitutionPattern();
    }

    private void fluidSubstitutionPattern() {
        final ItemLike pattern = com.refinedmods.refinedstorage.common.content.Items.INSTANCE.getPattern();
        ShapedRecipeBuilder.shaped(this.items, RecipeCategory.MISC, Items.INSTANCE.getFluidSubstitutionPattern())
            .pattern("BPB")
            .define('B', net.minecraft.world.item.Items.BUCKET)
            .define('P', pattern)
            .unlockedBy("has_pattern", this.has(pattern))
            .save(this.output);
    }

    public static final class Runner extends RecipeProvider.Runner {
        public Runner(final PackOutput packOutput, final CompletableFuture<HolderLookup.Provider> registries) {
            super(packOutput, registries);
        }

        @Override
        protected RecipeProvider createRecipeProvider(final HolderLookup.Provider registries,
                                                      final RecipeOutput output) {
            return new MainRecipeProvider(registries, output);
        }

        @Override
        public String getName() {
            return "Refined Fluid Substitution recipes";
        }
    }
}
