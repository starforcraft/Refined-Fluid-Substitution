package com.ultramega.refinedfluidsubstitution.common.fluidsubstitutionpattern;

import com.ultramega.refinedfluidsubstitution.common.ContentIds;

import com.refinedmods.refinedstorage.api.autocrafting.Pattern;
import com.refinedmods.refinedstorage.common.api.autocrafting.PatternProviderItem;
import com.refinedmods.refinedstorage.common.api.support.HelpTooltipComponent;
import com.refinedmods.refinedstorage.common.autocrafting.PatternItem;
import com.refinedmods.refinedstorage.common.autocrafting.PatternState;
import com.refinedmods.refinedstorage.common.content.DataComponents;
import com.refinedmods.refinedstorage.common.content.Items;
import com.refinedmods.refinedstorage.common.util.ClientPlatformUtil;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createTranslation;
import static com.ultramega.refinedfluidsubstitution.common.FluidSubstitutionIdentifierUtil.createFluidSubstitutionTranslation;

public class FluidSubstitutionPatternItem extends Item implements PatternProviderItem {
    private static final Component VANILLA_PATTERN_HELP = createTranslation("item", "pattern.help");
    private static final Component FLUID_SUBSTITUTION_HELP = createFluidSubstitutionTranslation("item", "fluid_substitution_pattern.help");

    public FluidSubstitutionPatternItem() {
        super(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, ContentIds.FLUID_SUBSTITUTION_PATTERN)));
    }

    @Override
    @Nullable
    public UUID getId(final ItemStack stack) {
        return getVanillaPatternItem().getId(stack);
    }

    @Override
    public Optional<Pattern> getPattern(final ItemStack stack, final Level level) {
        return getVanillaPatternItem().getPattern(stack, level);
    }

    @Override
    public Optional<ItemStack> getOutput(final ItemStack stack, final Level level) {
        return getVanillaPatternItem().getOutput(stack, level);
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(final ItemStack stack) {
        final PatternState state = stack.get(DataComponents.INSTANCE.getPatternState());
        if (state == null) {
            return Optional.of(new HelpTooltipComponent(VANILLA_PATTERN_HELP.copy().append(" ").append(FLUID_SUBSTITUTION_HELP)));
        }

        if (!FluidSubstitutionPatternTooltipCache.contains(state.id())) {
            final Level level = ClientPlatformUtil.getClientLevel();
            if (level != null) {
                FluidSubstitutionPatternResolver.resolve(stack, level).ifPresent(resolved ->
                    FluidSubstitutionPatternTooltipCache.put(state.id(), resolved.substitutions())
                );
            }
        }

        return getVanillaPatternItem().getTooltipImage(stack);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void appendHoverText(final ItemStack stack,
                                final TooltipContext context,
                                final TooltipDisplay display,
                                final Consumer<Component> builder,
                                final TooltipFlag tooltipFlag) {
        getVanillaPatternItem().appendHoverText(stack, context, display, builder, tooltipFlag);
    }

    @Override
    public InteractionResult use(final Level level, final Player player, final InteractionHand hand) {
        final ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide() && player.isCrouching()) {
            return InteractionResult.CONSUME.heldItemTransformedTo(new ItemStack(this, stack.getCount()));
        }
        return InteractionResult.PASS;
    }

    private static PatternItem getVanillaPatternItem() {
        return Items.INSTANCE.getPattern();
    }
}
