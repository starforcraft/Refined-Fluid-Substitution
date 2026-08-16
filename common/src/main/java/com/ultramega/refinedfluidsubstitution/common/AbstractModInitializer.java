package com.ultramega.refinedfluidsubstitution.common;

import com.ultramega.refinedfluidsubstitution.common.fluidsubstitutionpattern.FluidSubstitutionPatternItem;
import com.ultramega.refinedfluidsubstitution.common.registry.Items;

import com.refinedmods.refinedstorage.common.content.RegistryCallback;

import net.minecraft.world.item.Item;

public class AbstractModInitializer {
    protected void registerItems(final RegistryCallback<Item> callback) {
        Items.INSTANCE.setFluidSubstitutionPattern(callback.register(ContentIds.FLUID_SUBSTITUTION_PATTERN, FluidSubstitutionPatternItem::new));
    }
}
