package de.mrjulsen.legacydragonlib.client.gui;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import de.mrjulsen.legacydragonlib.common.ITranslatableEnum;
import de.mrjulsen.legacydragonlib.utils.Utils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.FormattedText;

public class DragonLibTooltip {
    public static final int WIDTH_UNDEFINED = -1;

    private final List<FormattedText> lines;
    protected int maxWidth = WIDTH_UNDEFINED;
    protected GuiAreaDefinition assignedArea = null;
    protected AbstractWidget assignedWidget = null;
    protected boolean visible = true;

    protected DragonLibTooltip(List<FormattedText> lines) {
        this.lines = lines;
    }

    public static DragonLibTooltip empty() {
        return DragonLibTooltip.of((FormattedText)null);
    }

    public static DragonLibTooltip of(String text) {
        return new DragonLibTooltip(text == null ? null : List.of(Utils.text(text)));
    }

    public static DragonLibTooltip of(Collection<String> text) {
        return new DragonLibTooltip(text.stream().map(x -> (FormattedText)Utils.text(x)).toList());
    }

    public static DragonLibTooltip of(String... texts) {
        return new DragonLibTooltip(Arrays.stream(texts).map(x -> (FormattedText)Utils.text(x)).toList());
    }

    public static DragonLibTooltip of(FormattedText formattedText) {
        return new DragonLibTooltip(formattedText == null ? null : List.of(formattedText));
    }

    public static DragonLibTooltip of(List<FormattedText> formattedTexts) {
        return new DragonLibTooltip(formattedTexts);
    }

    public static DragonLibTooltip of(FormattedText... formattedTexts) {
        return new DragonLibTooltip(Arrays.stream(formattedTexts).toList());
    }

    public static <E extends Enum<E> & ITranslatableEnum> DragonLibTooltip of(String modid, Class<E> enumClass) {
        return new DragonLibTooltip(GuiUtils.getEnumTooltipData(modid, enumClass));
    }

    public DragonLibTooltip withMaxWidth(int maxWidth) {
        this.maxWidth = maxWidth;
        return this;
    }

    public DragonLibTooltip assignedTo(AbstractWidget widget) {
        assignedWidget = widget;
        return this;
    }

    public DragonLibTooltip assignedTo(GuiAreaDefinition area) {
        assignedArea = area;
        return this;
    }

    public DragonLibTooltip withVisibility(boolean b) {
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

    public void render(Screen screen, GuiGraphics graphics, int mouseX, int mouseY) {
        render(screen, graphics, mouseX, mouseY, 0, 0);
    }

    public void render(Screen screen, GuiGraphics graphics, int mouseX, int mouseY, int xOffset, int yOffset) {
        if (lines.size() <= 0) {
            return;
        }

        if (assignedWidget != null) {
            GuiUtils.renderTooltipWithScrollOffset(screen, assignedWidget, getLines(), getMaxWidth() > 0 ? getMaxWidth() : screen.width, graphics, mouseX, mouseY, xOffset, yOffset);            
        } else if (assignedArea != null) {
            GuiUtils.renderTooltipWithScrollOffset(screen, assignedArea, getLines(), getMaxWidth() > 0 ? getMaxWidth() : screen.width, graphics, mouseX, mouseY, xOffset, yOffset);  
        } else {
            GuiUtils.renderTooltipWithScrollOffset(screen, GuiAreaDefinition.of(screen), getLines(), getMaxWidth() > 0 ? getMaxWidth() : screen.width, graphics, mouseX, mouseY, xOffset, yOffset); 
        }        
    }
}
