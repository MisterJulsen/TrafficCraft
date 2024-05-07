package de.mrjulsen.legacydragonlib.client.gui;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;

import de.mrjulsen.legacydragonlib.common.ITranslatableEnum;
import de.mrjulsen.legacydragonlib.utils.Utils;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.FormattedText;

public class Tooltip {
    public static final int WIDTH_UNDEFINED = -1;

    private final List<FormattedText> lines;
    protected int maxWidth = WIDTH_UNDEFINED;
    protected GuiAreaDefinition assignedArea = null;
    protected AbstractWidget assignedWidget = null;
    protected boolean visible = true;

    protected Tooltip(List<FormattedText> lines) {
        this.lines = lines;
    }

    public static Tooltip empty() {
        return Tooltip.of((FormattedText)null);
    }

    public static Tooltip of(String text) {
        return new Tooltip(text == null ? null : List.of(Utils.text(text)));
    }

    public static Tooltip of(Collection<String> text) {
        return new Tooltip(text.stream().map(x -> (FormattedText)Utils.text(x)).toList());
    }

    public static Tooltip of(String... texts) {
        return new Tooltip(Arrays.stream(texts).map(x -> (FormattedText)Utils.text(x)).toList());
    }

    public static Tooltip of(FormattedText formattedText) {
        return new Tooltip(formattedText == null ? null : List.of(formattedText));
    }

    public static Tooltip of(List<FormattedText> formattedTexts) {
        return new Tooltip(formattedTexts);
    }

    public static Tooltip of(FormattedText... formattedTexts) {
        return new Tooltip(Arrays.stream(formattedTexts).toList());
    }

    public static <E extends Enum<E> & ITranslatableEnum> Tooltip of(String modid, Class<E> enumClass) {
        return new Tooltip(GuiUtils.getEnumTooltipData(modid, enumClass));
    }

    public Tooltip withMaxWidth(int maxWidth) {
        this.maxWidth = maxWidth;
        return this;
    }

    public Tooltip assignedTo(AbstractWidget widget) {
        assignedWidget = widget;
        return this;
    }

    public Tooltip assignedTo(GuiAreaDefinition area) {
        assignedArea = area;
        return this;
    }

    public Tooltip withVisibility(boolean b) {
        visible = b;
        return this;
    }


    public GuiAreaDefinition getAssignedArea() {
        return assignedArea;
    }

    public AbstractWidget getAssignedWidget() {
        return assignedWidget;
    }

    public List<FormattedText> getLines() {
        return lines;
    }

    public boolean isVisible() {
        return visible;
    }

    public int getMaxWidth() {
        return maxWidth;
    }

    public void render(Screen screen, PoseStack poseStack, int mouseX, int mouseY) {
        render(screen, poseStack, mouseX, mouseY, 0, 0);
    }

    public void render(Screen screen, PoseStack poseStack, int mouseX, int mouseY, int xOffset, int yOffset) {
        if (lines.size() <= 0) {
            return;
        }

        if (assignedWidget != null) {
            GuiUtils.renderTooltipWithScrollOffset(screen, assignedWidget, getLines(), getMaxWidth() > 0 ? getMaxWidth() : screen.width, poseStack, mouseX, mouseY, xOffset, yOffset);            
        } else if (assignedArea != null) {
            GuiUtils.renderTooltipWithScrollOffset(screen, assignedArea, getLines(), getMaxWidth() > 0 ? getMaxWidth() : screen.width, poseStack, mouseX, mouseY, xOffset, yOffset);  
        } else {
            GuiUtils.renderTooltipWithScrollOffset(screen, GuiAreaDefinition.of(screen), getLines(), getMaxWidth() > 0 ? getMaxWidth() : screen.width, poseStack, mouseX, mouseY, xOffset, yOffset); 
        }        
    }
}
