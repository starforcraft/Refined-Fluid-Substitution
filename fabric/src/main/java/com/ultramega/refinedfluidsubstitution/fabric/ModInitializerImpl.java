package com.ultramega.refinedfluidsubstitution.fabric;

import com.ultramega.refinedfluidsubstitution.common.AbstractModInitializer;
import com.ultramega.refinedfluidsubstitution.common.Platform;
import com.ultramega.refinedfluidsubstitution.common.registry.CreativeModeTabItems;

import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;
import com.refinedmods.refinedstorage.common.content.DirectRegistryCallback;
import com.refinedmods.refinedstorage.fabric.api.RefinedStoragePlugin;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;

public class ModInitializerImpl extends AbstractModInitializer implements RefinedStoragePlugin, ModInitializer {
    @Override
    public void onApiAvailable(final RefinedStorageApi refinedStorageApi) {
        Platform.setConfigProvider(ConfigImpl::get);
        this.registerContent();
        this.registerCreativeModeTabListener(refinedStorageApi);
    }

    private void registerContent() {
        this.registerItems(new DirectRegistryCallback<>(BuiltInRegistries.ITEM));
    }

    private void registerCreativeModeTabListener(final RefinedStorageApi refinedStorageApi) {
        final ResourceKey<CreativeModeTab> creativeModeTab = ResourceKey.create(
            Registries.CREATIVE_MODE_TAB,
            refinedStorageApi.getCreativeModeTabId()
        );
        CreativeModeTabEvents.modifyOutputEvent(creativeModeTab).register(
            entries -> CreativeModeTabItems.appendItems(entries::accept)
        );
    }

    @Override
    public void onInitialize() {
        AutoConfig.register(ConfigImpl.class, Toml4jConfigSerializer::new);
    }
}
