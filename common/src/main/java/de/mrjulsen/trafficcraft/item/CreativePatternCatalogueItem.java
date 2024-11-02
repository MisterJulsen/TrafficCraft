package de.mrjulsen.trafficcraft.item;

import java.util.List;
import java.util.Optional;

import de.mrjulsen.trafficcraft.Constants;
import de.mrjulsen.trafficcraft.client.ClientWrapper;
import de.mrjulsen.trafficcraft.components.CreativePatternCatalogueComponent;
import de.mrjulsen.trafficcraft.data.NamedTrafficSignTextureReference;
import de.mrjulsen.trafficcraft.registry.ModDataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public class CreativePatternCatalogueItem extends PatternCatalogueItem {

    public CreativePatternCatalogueItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public int getMaxPatterns() {
        return Short.MAX_VALUE;
    }


    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        tooltipComponents.add(Constants.CREATIVE_MODE_ONLY_TOOLTIP);
    }

    @Override
    public boolean isFoil(ItemStack pStack) {
        return true;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level.isClientSide) {
            ClientWrapper.showSignPatternSelectionScreen(stack);
        }

        return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
    }

    @Override
    public NamedTrafficSignTextureReference getSelectedImageData(ItemStack stack) {
        return shouldUseCustomPattern(stack) ? getCustomImage(stack) : super.getSelectedImageData(stack);
    }

    public boolean hasCreativeComponent(ItemStack stack) {
        return stack.has(ModDataComponents.CREATIVE_PATTERN_CATALOGUE_COMPONENT.get());
    }

    public CreativePatternCatalogueComponent getCreativeComponent(ItemStack stack) {
        if (!hasCreativeComponent(stack)) {
            return stack.set(ModDataComponents.CREATIVE_PATTERN_CATALOGUE_COMPONENT.get(), CreativePatternCatalogueComponent.empty());
        }
        return stack.get(ModDataComponents.CREATIVE_PATTERN_CATALOGUE_COMPONENT.get());
    }

    public CreativePatternCatalogueComponent setCreativeComponent(ItemStack stack, CreativePatternCatalogueComponent data) {
        return stack.set(ModDataComponents.CREATIVE_PATTERN_CATALOGUE_COMPONENT.get(), data);
    }

    public void setCustomImage(ItemStack stack, NamedTrafficSignTextureReference data) {        
        setCreativeComponent(stack, new CreativePatternCatalogueComponent(Optional.of(data)));
    }

    public void clearCustomImage(ItemStack stack) { 
        stack.remove(ModDataComponents.CREATIVE_PATTERN_CATALOGUE_COMPONENT.get());
    }

    public NamedTrafficSignTextureReference getCustomImage(ItemStack stack) {
        if (hasCustomPattern(stack)) {
            return getCreativeComponent(stack).customTexture().get();
        } else {
            return null;
        }
    }

    public boolean hasCustomPattern(ItemStack stack) {
        return hasCreativeComponent(stack);
    }

    public boolean shouldUseCustomPattern(ItemStack stack) {
        return hasCustomPattern(stack) && getCreativeComponent(stack).customTexture().isPresent() && !indexInBounds(stack, getSelectedIndex(stack));
    }
}