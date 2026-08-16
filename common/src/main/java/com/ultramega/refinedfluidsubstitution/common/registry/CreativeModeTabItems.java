package com.ultramega.refinedfluidsubstitution.common.registry;

import java.util.function.Consumer;

import net.minecraft.world.item.ItemStack;

public final class CreativeModeTabItems {
    private CreativeModeTabItems() {
    }

    public static void appendItems(final Consumer<ItemStack> consumer) {
        consumer.accept(Items.INSTANCE.getFluidSubstitutionPattern().getDefaultInstance());
    }
}
