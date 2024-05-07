package de.mrjulsen.legacydragonlib.client.gui.wrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

import de.mrjulsen.legacydragonlib.client.gui.DragonLibTooltip;
import de.mrjulsen.legacydragonlib.client.gui.GuiUtils;
import de.mrjulsen.legacydragonlib.client.gui.widgets.IExtendedAreaWidget;
import de.mrjulsen.legacydragonlib.client.gui.widgets.ItemButton;
import de.mrjulsen.legacydragonlib.client.gui.widgets.ModSlider;
import de.mrjulsen.legacydragonlib.client.gui.widgets.ResizableButton;
import de.mrjulsen.legacydragonlib.client.gui.widgets.ResizableCycleButton;
import de.mrjulsen.legacydragonlib.common.ITranslatableEnum;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.gui.widget.ForgeSlider;

public abstract class CommonScreen extends net.minecraft.client.gui.screens.Screen {

    protected List<DragonLibTooltip> tooltips = new ArrayList<>();

    public final Consumer<Button> NO_BUTTON_CLICK_ACTION = (a) -> {};
    public final BiConsumer<CycleButton<?>, ?> NO_CYCLE_BUTTON_VALUE_CHANGE_ACTION = (a, b) -> {};
    public final BiConsumer<EditBox, Boolean> NO_EDIT_BOX_FOCUS_CHANGE_ACTION = (a, b) -> {};
    public final BiConsumer<ForgeSlider, Double> NO_SLIDER_CHANGE_VALUE_ACTION = (a, b) -> {};

    protected CommonScreen(Component title) {
        super(title);
    }

    @Override
    protected void init() {
        super.init();
        tooltips.clear();
    }

    @Override
    public void tick() {
        super.tick();
        this.renderables.stream().filter(x -> x instanceof EditBox).forEach(x -> ((EditBox)x).tick());
    }

    protected void onDone() {}

    public void renderBg(GuiGraphics graphics, int pMouseX, int pMouseY, float pPartialTick) {}

    public void renderFg(GuiGraphics graphics, int pMouseX, int pMouseY, float pPartialTick) {
        tooltips.forEach(x -> x.render(this, graphics, pMouseX, pMouseY));
        ItemButton.renderAllItemButtonTooltips(this, graphics, pMouseX, pMouseY);
    }
    
    @Override
    public void render(GuiGraphics graphics, int pMouseX, int pMouseY, float pPartialTick) {
        renderBg(graphics, pMouseX, pMouseY, pPartialTick);
        super.render(graphics, pMouseX, pMouseY, pPartialTick);
        renderFg(graphics, pMouseX, pMouseY, pPartialTick);
    }

    protected DragonLibTooltip addTooltip(DragonLibTooltip tooltip) {
        this.tooltips.add(tooltip);
        return tooltip;
    }

    protected boolean removeTooltips(Predicate<? super DragonLibTooltip> condition) {
        return tooltips.removeIf(condition);
    }

    protected ResizableButton addButton(int x, int y, int width, int height, Component text, Consumer<Button> onClick, DragonLibTooltip tooltip) {
        ResizableButton btn = GuiUtils.createButton(x, y, width, height, text, onClick);
        return addRenderableWidget(btn, x, y, width, height, tooltip);
	}

    protected <T extends Enum<T> & ITranslatableEnum> ResizableCycleButton<T> addCycleButton(String modid, Class<T> clazz, int x, int y, int width, int height, Component text, T initialValue, BiConsumer<ResizableCycleButton<?>, T> onValueChanged, DragonLibTooltip tooltip) {
        ResizableCycleButton<T> btn = GuiUtils.createCycleButton(modid, clazz, x, y, width, height, text, initialValue, onValueChanged);
        return addRenderableWidget(btn, x, y, width, height, tooltip);
	}

    protected ResizableCycleButton<Boolean> addOnOffButton(int x, int y, int width, int height, Component text, boolean initialValue, BiConsumer<ResizableCycleButton<?>, Boolean> onValueChanged, DragonLibTooltip tooltip) {
        ResizableCycleButton<Boolean> btn = GuiUtils.createOnOffButton(x, y, width, height, text, initialValue, onValueChanged);
        return addRenderableWidget(btn, x, y, width, height, tooltip);
	}

    protected EditBox addEditBox(int x, int y, int width, int height, String text, boolean drawBg, Consumer<String> onValueChanged, BiConsumer<EditBox, Boolean> onFocusChanged, DragonLibTooltip tooltip) {
        EditBox box = GuiUtils.createEditBox(x, y, width, height, font, text, drawBg, onValueChanged, onFocusChanged);
        return this.addRenderableWidget(box, x, y, width, height, tooltip);
    }

    protected ModSlider addSlider(int x, int y, int width, int height, Component prefix, Component suffix, double min, double max, double step, double initialValue, boolean drawLabel, BiConsumer<ModSlider, Double> onValueChanged, Consumer<ModSlider> onUpdateMessage, DragonLibTooltip tooltip) {
        ModSlider slider = GuiUtils.createSlider(x, y, width, height, prefix, suffix, min, max, step, initialValue, drawLabel, onValueChanged, onUpdateMessage);        
        return this.addRenderableWidget(slider, x, y, width, height, tooltip);
    }

    protected <W extends AbstractWidget> W addRenderableWidget(W widget, int x, int y, int width, int height, DragonLibTooltip tooltip) {
        if (tooltip != null && !tooltip.equals(DragonLibTooltip.empty())) {
            addTooltip(tooltip.assignedTo(widget));
        }

        widget.setX(x);
        widget.setY(y);
        widget.setWidth(width);
        widget.setHeight(height);
        
		return addRenderableWidget(widget);
    }

    @Override
    public boolean mouseScrolled(double pMouseX, double pMouseY, double pDelta) {
        if (super.mouseScrolled(pMouseX, pMouseY, pDelta)) {
            return true;
        }
        boolean[] b = new boolean[] { false };
        this.renderables.stream().filter(x -> x instanceof IExtendedAreaWidget && x instanceof GuiEventListener g).forEach(x -> {
            if (((x instanceof IExtendedAreaWidget exw && exw.isInArea(pMouseX, pMouseY))) && !b[0]) {
                b[0] = ((GuiEventListener)x).mouseScrolled(pMouseX, pMouseY, pDelta);
                if (b[0]) {
                    return;
                }
            }
        });
        return b[0];
    }
}
