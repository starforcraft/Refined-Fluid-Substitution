package com.ultramega.refinedfluidsubstitution.fabric;

import com.ultramega.refinedfluidsubstitution.common.registry.Items;

import com.refinedmods.refinedstorage.common.content.DataComponents;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.renderer.item.ItemProperties;

import static com.ultramega.refinedfluidsubstitution.common.FluidSubstitutionIdentifierUtil.createFluidSubstitutionIdentifier;

public class ClientModInitializerImpl implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ItemProperties.register(
            Items.INSTANCE.getFluidSubstitutionPattern(),
            createFluidSubstitutionIdentifier("encoded"),
            (stack, level, entity, seed) -> stack.has(DataComponents.INSTANCE.getPatternState()) ? 1.0F : 0.0F
        );
    }
}
