package de.mrjulsen.trafficcraft.screen.widgets;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.TooltipAccessor;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ResizableCycleButton<T> extends AbstractButton implements TooltipAccessor {
    static final BooleanSupplier DEFAULT_ALT_LIST_SELECTOR = Screen::hasAltDown;
    private static final List<Boolean> BOOLEAN_OPTIONS = ImmutableList.of(Boolean.TRUE, Boolean.FALSE);
    private final Component name;
    private int index;
    private T value;
    private final ResizableCycleButton.ValueListSupplier<T> values;
    private final Function<T, Component> valueStringifier;
    private final Function<ResizableCycleButton<T>, MutableComponent> narrationProvider;
    private final ResizableCycleButton.OnValueChange<T> onValueChange;
    private final ResizableCycleButton.TooltipSupplier<T> tooltipSupplier;
    private final boolean displayOnlyValue;

    ResizableCycleButton(int pX, int pY, int pWidth, int pHeight, Component pMessage, Component pName, int pIndex,
            T pValue, ResizableCycleButton.ValueListSupplier<T> pValues, Function<T, Component> pValueStringifier,
            Function<ResizableCycleButton<T>, MutableComponent> pNarrationProvider,
            ResizableCycleButton.OnValueChange<T> pOnValueChange,
            ResizableCycleButton.TooltipSupplier<T> pTooltipSupplier, boolean pDisplayOnlyValue) {
        super(pX, pY, pWidth, pHeight, pMessage);
        this.name = pName;
        this.index = pIndex;
        this.value = pValue;
        this.values = pValues;
        this.valueStringifier = pValueStringifier;
        this.narrationProvider = pNarrationProvider;
        this.onValueChange = pOnValueChange;
        this.tooltipSupplier = pTooltipSupplier;
        this.displayOnlyValue = pDisplayOnlyValue;
    }

    @Override
    public void renderButton(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        Minecraft minecraft = Minecraft.getInstance();
        Font font = minecraft.font;
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, WIDGETS_LOCATION);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
        int i = this.getYImage(this.isHoveredOrFocused());
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        this.blit(pPoseStack, this.x, this.y, 0, 46 + i * 20, this.width / 2, this.height / 2);
        this.blit(pPoseStack, this.x + this.width / 2, this.y, 200 - this.width / 2, 46 + i * 20, this.width / 2, this.height / 2);        

        this.blit(pPoseStack, this.x, this.y + this.height / 2, 0, 46 + (i + 1) * 20 - this.height / 2, this.width / 2, this.height / 2);
        this.blit(pPoseStack, this.x + this.width / 2, this.y + this.height / 2, 200 - this.width / 2, 46 + (i + 1) * 20 - this.height / 2, this.width / 2, this.height / 2);

        this.renderBg(pPoseStack, minecraft, pMouseX, pMouseY);
        int j = getFGColor();
        drawCenteredString(pPoseStack, font, this.getMessage(), this.x + this.width / 2, this.y + (this.height - 8) / 2, j | Mth.ceil(this.alpha * 255.0F) << 24);
    }

    public void onPress() {
        if (Screen.hasShiftDown()) {
            this.cycleValue(-1);
        } else {
            this.cycleValue(1);
        }

    }

    private void cycleValue(int pDelta) {
        List<T> list = this.values.getSelectedList();
        this.index = Mth.positiveModulo(this.index + pDelta, list.size());
        T t = list.get(this.index);
        this.updateValue(t);
        this.onValueChange.onValueChange(this, t);
    }

    private T getCycledValue(int pDelta) {
        List<T> list = this.values.getSelectedList();
        return list.get(Mth.positiveModulo(this.index + pDelta, list.size()));
    }

    public boolean mouseScrolled(double pMouseX, double pMouseY, double pDelta) {
        if (pDelta > 0.0D) {
            this.cycleValue(-1);
        } else if (pDelta < 0.0D) {
            this.cycleValue(1);
        }

        return true;
    }

    public void setValue(T pValue) {
        List<T> list = this.values.getSelectedList();
        int i = list.indexOf(pValue);
        if (i != -1) {
            this.index = i;
        }

        this.updateValue(pValue);
    }

    private void updateValue(T pValue) {
        Component component = this.createLabelForValue(pValue);
        this.setMessage(component);
        this.value = pValue;
    }

    private Component createLabelForValue(T pValue) {
        return (Component) (this.displayOnlyValue ? this.valueStringifier.apply(pValue) : this.createFullName(pValue));
    }

    private MutableComponent createFullName(T pValue) {
        return CommonComponents.optionNameValue(this.name, this.valueStringifier.apply(pValue));
    }

    public T getValue() {
        return this.value;
    }

    protected MutableComponent createNarrationMessage() {
        return this.narrationProvider.apply(this);
    }

    public void updateNarration(NarrationElementOutput pNarrationElementOutput) {
        pNarrationElementOutput.add(NarratedElementType.TITLE, this.createNarrationMessage());
        if (this.active) {
            T t = this.getCycledValue(1);
            Component component = this.createLabelForValue(t);
            if (this.isFocused()) {
                pNarrationElementOutput.add(NarratedElementType.USAGE,
                        new TranslatableComponent("narration.cycle_button.usage.focused", component));
            } else {
                pNarrationElementOutput.add(NarratedElementType.USAGE,
                        new TranslatableComponent("narration.cycle_button.usage.hovered", component));
            }
        }

    }

    public MutableComponent createDefaultNarrationMessage() {
        return wrapDefaultNarrationMessage(
                (Component) (this.displayOnlyValue ? this.createFullName(this.value) : this.getMessage()));
    }

    public List<FormattedCharSequence> getTooltip() {
        return this.tooltipSupplier.apply(this.value);
    }

    public static <T> ResizableCycleButton.Builder<T> builder(Function<T, Component> pValueStringifier) {
        return new ResizableCycleButton.Builder<>(pValueStringifier);
    }

    public static ResizableCycleButton.Builder<Boolean> booleanBuilder(Component pComponentOn,
            Component pComponentOff) {
        return (new ResizableCycleButton.Builder<Boolean>((p_168902_) -> {
            return p_168902_ ? pComponentOn : pComponentOff;
        })).withValues(BOOLEAN_OPTIONS);
    }

    public static ResizableCycleButton.Builder<Boolean> onOffBuilder() {
        return (new ResizableCycleButton.Builder<Boolean>((p_168891_) -> {
            return p_168891_ ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF;
        })).withValues(BOOLEAN_OPTIONS);
    }

    public static ResizableCycleButton.Builder<Boolean> onOffBuilder(boolean pInitialValue) {
        return onOffBuilder().withInitialValue(pInitialValue);
    }

    @OnlyIn(Dist.CLIENT)
    public static class Builder<T> {
        private int initialIndex;
        @Nullable
        private T initialValue;
        private final Function<T, Component> valueStringifier;
        private ResizableCycleButton.TooltipSupplier<T> tooltipSupplier = (p_168964_) -> {
            return ImmutableList.of();
        };
        private Function<ResizableCycleButton<T>, MutableComponent> narrationProvider = ResizableCycleButton::createDefaultNarrationMessage;
        private ResizableCycleButton.ValueListSupplier<T> values = ResizableCycleButton.ValueListSupplier
                .create(ImmutableList.of());
        private boolean displayOnlyValue;

        public Builder(Function<T, Component> pValueStringifier) {
            this.valueStringifier = pValueStringifier;
        }

        public ResizableCycleButton.Builder<T> withValues(List<T> pValues) {
            this.values = ResizableCycleButton.ValueListSupplier.create(pValues);
            return this;
        }

        @SafeVarargs
        public final ResizableCycleButton.Builder<T> withValues(T... pValues) {
            return this.withValues(ImmutableList.copyOf(pValues));
        }

        public ResizableCycleButton.Builder<T> withValues(List<T> pDefaultList, List<T> pSelectedList) {
            this.values = ResizableCycleButton.ValueListSupplier.create(ResizableCycleButton.DEFAULT_ALT_LIST_SELECTOR,
                    pDefaultList, pSelectedList);
            return this;
        }

        public ResizableCycleButton.Builder<T> withValues(BooleanSupplier pAltListSelector, List<T> pDefaultList,
                List<T> pSelectedList) {
            this.values = ResizableCycleButton.ValueListSupplier.create(pAltListSelector, pDefaultList, pSelectedList);
            return this;
        }

        public ResizableCycleButton.Builder<T> withTooltip(ResizableCycleButton.TooltipSupplier<T> pTooltipSupplier) {
            this.tooltipSupplier = pTooltipSupplier;
            return this;
        }

        public ResizableCycleButton.Builder<T> withInitialValue(T pInitialValue) {
            this.initialValue = pInitialValue;
            int i = this.values.getDefaultList().indexOf(pInitialValue);
            if (i != -1) {
                this.initialIndex = i;
            }

            return this;
        }

        public ResizableCycleButton.Builder<T> withCustomNarration(
                Function<ResizableCycleButton<T>, MutableComponent> pNarrationProvider) {
            this.narrationProvider = pNarrationProvider;
            return this;
        }

        public ResizableCycleButton.Builder<T> displayOnlyValue() {
            this.displayOnlyValue = true;
            return this;
        }

        public ResizableCycleButton<T> create(int pX, int pY, int pWidth, int pHeight, Component pName) {
            return this.create(pX, pY, pWidth, pHeight, pName, (p_168946_, p_168947_) -> {
            });
        }

        public ResizableCycleButton<T> create(int pX, int pY, int pWidth, int pHeight, Component pName,
                ResizableCycleButton.OnValueChange<T> pOnValueChange) {
            List<T> list = this.values.getDefaultList();
            if (list.isEmpty()) {
                throw new IllegalStateException("No values for cycle button");
            } else {
                T t = (T) (this.initialValue != null ? this.initialValue : list.get(this.initialIndex));
                Component component = this.valueStringifier.apply(t);
                Component component1 = (Component) (this.displayOnlyValue ? component
                        : CommonComponents.optionNameValue(pName, component));
                return new ResizableCycleButton<>(pX, pY, pWidth, pHeight, component1, pName, this.initialIndex, t,
                        this.values, this.valueStringifier, this.narrationProvider, pOnValueChange,
                        this.tooltipSupplier, this.displayOnlyValue);
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    public interface OnValueChange<T> {
        void onValueChange(ResizableCycleButton pCycleButton, T pValue);
    }

    @FunctionalInterface
    @OnlyIn(Dist.CLIENT)
    public interface TooltipSupplier<T> extends Function<T, List<FormattedCharSequence>> {
    }

    @OnlyIn(Dist.CLIENT)
    interface ValueListSupplier<T> {
        List<T> getSelectedList();

        List<T> getDefaultList();

        static <T> ResizableCycleButton.ValueListSupplier<T> create(List<T> pValues) {
            final List<T> list = ImmutableList.copyOf(pValues);
            return new ResizableCycleButton.ValueListSupplier<T>() {
                public List<T> getSelectedList() {
                    return list;
                }

                public List<T> getDefaultList() {
                    return list;
                }
            };
        }

        static <T> ResizableCycleButton.ValueListSupplier<T> create(final BooleanSupplier pAltListSelector,
                List<T> pDefaultList, List<T> pSelectedList) {
            final List<T> list = ImmutableList.copyOf(pDefaultList);
            final List<T> list1 = ImmutableList.copyOf(pSelectedList);
            return new ResizableCycleButton.ValueListSupplier<T>() {
                public List<T> getSelectedList() {
                    return pAltListSelector.getAsBoolean() ? list1 : list;
                }

                public List<T> getDefaultList() {
                    return list;
                }
            };
        }
    }
}