package com.ultramega.refinedfluidsubstitution.neoforge;

import com.ultramega.refinedfluidsubstitution.common.AbstractModInitializer;
import com.ultramega.refinedfluidsubstitution.common.registry.CreativeModeTabItems;

import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;
import com.refinedmods.refinedstorage.common.content.RegistryCallback;

import java.util.function.Supplier;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

import static com.ultramega.refinedfluidsubstitution.common.FluidSubstitutionIdentifierUtil.MOD_ID;

@Mod(MOD_ID)
public class ModInitializer extends AbstractModInitializer {
    private final DeferredRegister<Item> itemRegistry = DeferredRegister.create(BuiltInRegistries.ITEM, MOD_ID);

    public ModInitializer(final IEventBus eventBus) {
        this.registerContent(eventBus);
        eventBus.addListener(this::registerCreativeModeTabListener);
    }

    private void registerContent(final IEventBus eventBus) {
        this.registerItems(eventBus);
    }

    private void registerItems(final IEventBus eventBus) {
        final RegistryCallback<Item> callback = new ForgeRegistryCallback<>(this.itemRegistry);
        this.registerItems(callback);
        this.itemRegistry.register(eventBus);
    }

    private void registerCreativeModeTabListener(final BuildCreativeModeTabContentsEvent e) {
        final ResourceKey<CreativeModeTab> creativeModeTab = ResourceKey.create(
            Registries.CREATIVE_MODE_TAB,
            RefinedStorageApi.INSTANCE.getCreativeModeTabId()
        );

        if (e.getTabKey().equals(creativeModeTab)) {
            CreativeModeTabItems.appendItems(e::accept);
        }
    }

    private record ForgeRegistryCallback<T>(DeferredRegister<T> registry) implements RegistryCallback<T> {
        @Override
        public <R extends T> Supplier<R> register(final Identifier id, final Supplier<R> value) {
            return this.registry.register(id.getPath(), value);
        }
    }
}
