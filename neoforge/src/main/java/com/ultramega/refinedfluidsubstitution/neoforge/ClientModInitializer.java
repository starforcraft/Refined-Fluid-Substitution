package com.ultramega.refinedfluidsubstitution.neoforge;

import com.ultramega.refinedfluidsubstitution.common.registry.Items;

import com.refinedmods.refinedstorage.common.content.DataComponents;

import net.minecraft.client.renderer.item.ItemProperties;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

import static com.ultramega.refinedfluidsubstitution.common.FluidSubstitutionIdentifierUtil.createFluidSubstitutionIdentifier;

public final class ClientModInitializer {
    private ClientModInitializer() {
    }

    public static void onClientSetup(final FMLClientSetupEvent event) {
        event.enqueueWork(() -> ItemProperties.register(
            Items.INSTANCE.getFluidSubstitutionPattern(),
            createFluidSubstitutionIdentifier("encoded"),
            (stack, level, entity, seed) -> stack.has(DataComponents.INSTANCE.getPatternState()) ? 1.0F : 0.0F
        ));
    }
}
