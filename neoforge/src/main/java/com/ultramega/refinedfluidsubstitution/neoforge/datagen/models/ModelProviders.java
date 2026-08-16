package com.ultramega.refinedfluidsubstitution.neoforge.datagen.models;

import com.ultramega.refinedfluidsubstitution.common.registry.Items;

import com.refinedmods.refinedstorage.common.content.DataComponents;

import java.util.stream.Stream;

import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.properties.conditional.HasComponent;
import net.minecraft.core.Holder;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
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
        final Item pattern = Items.INSTANCE.getFluidSubstitutionPattern();
        final Identifier emptyModel = itemModels.createFlatItemModel(pattern, "/empty", ModelTemplates.FLAT_ITEM);
        final Identifier encodedModel = itemModels.createFlatItemModel(pattern, "/pattern", ModelTemplates.FLAT_ITEM);
        final ItemModel.Unbaked empty = ItemModelUtils.plainModel(emptyModel);
        final ItemModel.Unbaked encoded = ItemModelUtils.plainModel(encodedModel);

        itemModels.itemModelOutput.accept(
            pattern,
            ItemModelUtils.conditional(
                new HasComponent(DataComponents.INSTANCE.getPatternState(), false),
                encoded,
                empty
            )
        );
    }
}
