package com.ultramega.refinedfluidsubstitution.neoforge.datagen.models;

import com.ultramega.refinedfluidsubstitution.common.registry.Items;

import java.util.stream.Stream;

import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.core.Holder;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import static com.ultramega.refinedfluidsubstitution.common.FluidSubstitutionIdentifierUtil.MOD_ID;

public class ModelProviders extends ModelProvider {
    public ModelProviders(final PackOutput output) {
        super(output, MOD_ID);
    }

    @Override
    protected Stream<? extends Holder<Block>> getKnownBlocks() {
        return Stream.of();
    }

    @Override
    protected Stream<? extends Holder<Item>> getKnownItems() {
        return Stream.of();
    }

    @Override
    protected void registerModels(final BlockModelGenerators blockModels, final ItemModelGenerators itemModels) {
        itemModels.generateFlatItem(Items.INSTANCE.getFluidSubstitutionPattern(), ModelTemplates.FLAT_ITEM);
    }
}
