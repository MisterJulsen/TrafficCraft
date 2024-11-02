package de.mrjulsen.trafficcraft.item;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import de.mrjulsen.trafficcraft.client.ClientWrapper;
import de.mrjulsen.trafficcraft.client.tooltip.TrafficSignTooltip;
import de.mrjulsen.trafficcraft.components.PatternCatalogueComponent;
import de.mrjulsen.trafficcraft.data.NamedTrafficSignTextureReference;
import de.mrjulsen.trafficcraft.registry.ModDataComponents;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class PatternCatalogueItem extends Item implements IUseDataComponent<PatternCatalogueComponent> {

    private static final int MAX_SIGN_PATTERNS = 36;

    private static final Map<ItemStack, Optional<TooltipComponent>> tooltips = new HashMap<>();

    public PatternCatalogueItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level.isClientSide) {
            ClientWrapper.showSignPatternSelectionScreen(stack);
        }

        return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
    }

    public int getMaxPatterns() {
        return MAX_SIGN_PATTERNS;
    }

    public Optional<TooltipComponent> getTooltipImage(ItemStack pStack) {
        Optional<TooltipComponent> tooltip = tooltips.computeIfAbsent(pStack, x -> createTooltip(x));
        tooltip.ifPresent(x -> ((TrafficSignTooltip)x).initAgeable());
        return tooltip;
    }

    private Optional<TooltipComponent> createTooltip(ItemStack stck) {
        final ItemStack stack = stck;

        NonNullList<NamedTrafficSignTextureReference> nonnulllist = NonNullList.create();
        if (hasComponent(stack)) {
            Arrays.stream(getStoredPatterns(stack)).forEach(nonnulllist::add);
        }
        return Optional.of(new TrafficSignTooltip(nonnulllist, getSelectedImageData(stack), getSelectedIndex(stack), () -> {
            if (tooltips.containsKey(stack)) {
                tooltips.remove(stack);
            }
        }));
    }

    public NamedTrafficSignTextureReference getSelectedImageData(ItemStack stack) {
        return getSelectedPattern(stack);
    }

    protected boolean indexInBounds(ItemStack stack, int index) {
        return index >= 0 && index < getStoredPatternCount(stack);
    }

    public int getSelectedIndex(ItemStack stack) {
        return getComponent(stack).selectedIndex();
    }

    public short getStoredPatternCount(ItemStack stack) {
        return (short)(getComponent(stack).textures().size());
    }

    public NamedTrafficSignTextureReference getPatternAt(ItemStack stack, int index) {
        if (!indexInBounds(stack, index))
            return null;

        return getComponent(stack).textures().get(index);
    }

    public NamedTrafficSignTextureReference getSelectedPattern(ItemStack stack) {
        return getPatternAt(stack, getComponent(stack).selectedIndex());
    }

    public NamedTrafficSignTextureReference[] getStoredPatterns(ItemStack stack) {
        return getComponent(stack).textures().toArray(NamedTrafficSignTextureReference[]::new);
    }

    public boolean setPattern(ItemStack stack, NamedTrafficSignTextureReference pattern) {
        if (getStoredPatternCount(stack) >= ((PatternCatalogueItem)stack.getItem()).getMaxPatterns())
            return false;

        PatternCatalogueComponent comp = getComponent(stack);
        List<NamedTrafficSignTextureReference> textures = comp.textures();
        textures.add(pattern);
        setComponent(stack, new PatternCatalogueComponent(textures, textures.size() - 1));
        return true;
    }

    public boolean replacePattern(ItemStack stack, NamedTrafficSignTextureReference pattern, int index) {
        if (getStoredPatternCount(stack) >= ((PatternCatalogueItem)stack.getItem()).getMaxPatterns())
            return false;

        PatternCatalogueComponent comp = getComponent(stack);
        List<NamedTrafficSignTextureReference> textures = comp.textures();
        textures.set(index, pattern);
        setComponent(stack, new PatternCatalogueComponent(textures, index));
        return true;
    }

    public boolean removePatternAt(ItemStack stack, int index) {
        if (!indexInBounds(stack, index))
            return false;

            
        PatternCatalogueComponent comp = getComponent(stack);
        List<NamedTrafficSignTextureReference> textures = comp.textures();
        textures.remove(index);
        int newIndex = index;
        if (newIndex >= textures.size()) {
            newIndex = Math.max(0, textures.size() - 1);
        }
        
        setComponent(stack, new PatternCatalogueComponent(textures, textures.size() - 1));
        return true;
    }

    public void clearPatterns(ItemStack stack) {
        setComponent(stack, new PatternCatalogueComponent(new ArrayList<>(), 0));
    }

    public void setSelectedIndex(ItemStack stack, int index) {
        
        PatternCatalogueComponent comp = getComponent(stack);
        setComponent(stack, new PatternCatalogueComponent(comp.textures(), Mth.clamp(index, -1, Math.max(0, getStoredPatternCount(stack) - 1))));
    }

    @Override
    public DataComponentType<PatternCatalogueComponent> getComponentType() {
        return ModDataComponents.PATTERN_CATALOGUE_COMPONENT.get();
    }

    @Override
    public PatternCatalogueComponent emptyComponent() {
        return PatternCatalogueComponent.empty();
    }
}