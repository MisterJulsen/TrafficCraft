package de.mrjulsen.legacydragonlib.client.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlConst;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import de.mrjulsen.legacydragonlib.client.CustomRenderTarget;
import de.mrjulsen.legacydragonlib.client.gui.widgets.ModEditBox;
import de.mrjulsen.legacydragonlib.client.gui.widgets.ModSlider;
import de.mrjulsen.legacydragonlib.client.gui.widgets.ResizableButton;
import de.mrjulsen.legacydragonlib.client.gui.widgets.ResizableCycleButton;
import de.mrjulsen.legacydragonlib.common.ITranslatableEnum;
import de.mrjulsen.legacydragonlib.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.gui.ScreenUtils;

public final class GuiUtils {

	public static final int DEFAULT_BUTTON_HEIGHT = 20;
	
	protected static CustomRenderTarget framebuffer;
    
    /**
	 * @see https://github.com/Creators-of-Create/Create/blob/mc1.18/dev/src/main/java/com/simibubi/create/foundation/gui/UIRenderHelper.java
	 */
	public static void init() {
		RenderSystem.recordRenderCall(() -> {
			Window mainWindow = Minecraft.getInstance().getWindow();
			framebuffer = CustomRenderTarget.create(mainWindow);
		});
	}

	public static CustomRenderTarget getFramebuffer() {
		return framebuffer;
	}

    public static void updateWindowSize(Window window) {
        if (getFramebuffer() != null)
            getFramebuffer().resize(window.getWidth(), window.getHeight(), Minecraft.ON_OSX);
    }
    
    /**
	 * @see https://github.com/Creators-of-Create/Create/blob/mc1.18/dev/src/main/java/com/simibubi/create/foundation/gui/UIRenderHelper.java
	 */
	public static void swapAndBlitColor(RenderTarget src, RenderTarget dst) {
		GlStateManager._glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, src.frameBufferId);
		GlStateManager._glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, dst.frameBufferId);
		GlStateManager._glBlitFrameBuffer(0, 0, src.viewWidth, src.viewHeight, 0, 0, dst.viewWidth, dst.viewHeight, GL30.GL_COLOR_BUFFER_BIT, GL20.GL_LINEAR);

		GlStateManager._glBindFramebuffer(GlConst.GL_FRAMEBUFFER, dst.frameBufferId);
	}

    /**
	 * @see https://github.com/Creators-of-Create/Create/blob/mc1.18/dev/src/main/java/com/simibubi/create/foundation/gui/UIRenderHelper.java
	 */
    public static void startStencil(PoseStack matrixStack, float x, float y, float w, float h) {
		RenderSystem.clear(GL30.GL_STENCIL_BUFFER_BIT | GL30.GL_DEPTH_BUFFER_BIT, Minecraft.ON_OSX);

		GL11.glDisable(GL11.GL_STENCIL_TEST);
		RenderSystem.stencilMask(~0);
		RenderSystem.clear(GL11.GL_STENCIL_BUFFER_BIT, Minecraft.ON_OSX);
		GL11.glEnable(GL11.GL_STENCIL_TEST);
		RenderSystem.stencilOp(GL11.GL_REPLACE, GL11.GL_KEEP, GL11.GL_KEEP);
		RenderSystem.stencilMask(0xFF);
		RenderSystem.stencilFunc(GL11.GL_NEVER, 1, 0xFF);

		matrixStack.pushPose();
		matrixStack.translate(x, y, 0);
		matrixStack.scale(w, h, 1);
		ScreenUtils.drawGradientRect(matrixStack.last()
			.pose(), -100, 0, 0, 1, 1, 0xff000000, 0xff000000);
		matrixStack.popPose();

		GL11.glEnable(GL11.GL_STENCIL_TEST);
		RenderSystem.stencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);
		RenderSystem.stencilFunc(GL11.GL_EQUAL, 1, 0xFF);
	}

    /**
	 * @see https://github.com/Creators-of-Create/Create/blob/mc1.18/dev/src/main/java/com/simibubi/create/foundation/gui/UIRenderHelper.java
	 */
	public static void endStencil() {
		GL11.glDisable(GL11.GL_STENCIL_TEST);
	}

    public static <W extends AbstractWidget, T extends FormattedText> boolean renderTooltip(Screen screen, W widget, List<T> lines, int maxWidth, PoseStack stack, int mouseX, int mouseY) {
        return renderTooltipWithScrollOffset(screen, widget, lines, maxWidth, stack, mouseX, mouseY, 0, 0);
    }

    public static <T extends FormattedText> boolean renderTooltip(Screen screen, GuiAreaDefinition area, List<T> lines, int maxWidth, PoseStack stack, int mouseX, int mouseY) {
        return renderTooltipWithScrollOffset(screen, area, lines, maxWidth, stack, mouseX, mouseY, 0, 0);
    }

    @SuppressWarnings("resource")
    public static <W extends AbstractWidget, T extends FormattedText> boolean renderTooltipWithScrollOffset(Screen screen, W widget, List<T> lines, int maxWidth, PoseStack stack, int mouseX, int mouseY, int xOffset, int yOffset) {
        if (widget.isMouseOver(mouseX + xOffset, mouseY + yOffset)) {
            screen.renderComponentTooltip(stack, getTooltipData(screen, lines, maxWidth), mouseX, mouseY, screen.getMinecraft().font);
			return true;
        }
		return false;
    }

	@SuppressWarnings("resource")
    public static <T extends FormattedText> boolean renderTooltipWithScrollOffset(Screen screen, GuiAreaDefinition area, List<T> lines, int maxWidth, PoseStack stack, int mouseX, int mouseY, int xOffset, int yOffset) {
        if (area.isInBounds(mouseX + xOffset, mouseY + yOffset)) {
            screen.renderComponentTooltip(stack, getTooltipData(screen, lines, maxWidth), mouseX, mouseY, screen.getMinecraft().font);
			return true;
        }
		return false;
    }

    @SuppressWarnings("resource")
    public static <T extends Enum<T> & ITranslatableEnum> List<FormattedText> getEnumTooltipData(String modid, Screen screen, Class<T> enumClass, int maxWidth) {
        List<FormattedText> c = new ArrayList<>();
        T enumValue = enumClass.getEnumConstants()[0];
        c.addAll(screen.getMinecraft().font.splitter.splitLines(Utils.translate(enumValue.getEnumDescriptionTranslationKey(modid)), maxWidth, Style.EMPTY).stream().toList());
        c.add(Utils.text(" "));
        c.addAll(Arrays.stream(enumClass.getEnumConstants()).map((tr) -> {
            return Utils.text(String.format("§l> %s§r§7\n%s", Utils.translate(tr.getValueTranslationKey(modid)).getString(), Utils.translate(tr.getValueInfoTranslationKey(modid)).getString()));
        }).map((x) -> screen.getMinecraft().font.splitter.splitLines(x, maxWidth, Style.EMPTY).stream().toList()).flatMap(List::stream).collect(Collectors.toList()));
        
        return c;
    }

    public static <T extends Enum<T> & ITranslatableEnum> List<FormattedText> getEnumTooltipData(String modid, Class<T> enumClass) {
        List<FormattedText> c = new ArrayList<>();
        T enumValue = enumClass.getEnumConstants()[0];
        c.add(Utils.translate(enumValue.getEnumDescriptionTranslationKey(modid)));
        c.add(Utils.text(" "));
        c.addAll(Arrays.stream(enumClass.getEnumConstants()).map((tr) -> {
            return Utils.text(String.format("§l> %s§r§7\n%s", Utils.translate(tr.getValueTranslationKey(modid)).getString(), Utils.translate(tr.getValueInfoTranslationKey(modid)).getString()));
        }).toList());
        return c;
    }

    public static <T extends FormattedText> List<FormattedText> getTooltipData(Screen screen, T component, int maxWidth) {
        return getTooltipData(screen, List.of(component), maxWidth);
    }

    public static <T extends FormattedText> List<FormattedText> getTooltipData(Screen screen, Collection<T> components, int maxWidth) {
        return components.stream().flatMap(a -> screen.getMinecraft().font.splitter.splitLines(a, maxWidth <= 0 ? screen.width : maxWidth, Style.EMPTY).stream()).toList();
    }


	public static boolean editBoxNumberFilter(String input) {
        if (input.isEmpty())
            return true;

        String i = input;
        if (input.equals("-"))
            i = "-0";

        try {
            Integer.parseInt(i);
			return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean editBoxPositiveNumberFilter(String input) {
        if (input.isEmpty()) {
            return true;
        } else {
            try {
                int i = Integer.parseInt(input);
                return i > 0;
            } catch (NumberFormatException var3) {
                return false;
            }
        }
    }

    public static boolean editBoxNonNegativeNumberFilter(String input) {
        if (input.isEmpty()) {
            return true;
        } else {
            try {
                int i = Integer.parseInt(input);
                return i >= 0;
            } catch (NumberFormatException var3) {
                return false;
            }
        }
    }

	public static void setTexture(ResourceLocation texture) {
        RenderSystem.setShaderTexture(0, texture);
	}

    public static void setTexture(int textureId) {
        RenderSystem.setShaderTexture(0, textureId);
	}

	public static void setShaderColor(float r, float g, float b, float a) {		
        RenderSystem.setShaderColor(r, g, b, a);
	}

	private static void internalBlit(PoseStack pPoseStack, int pX, int pY, int pUOffset, int pVOffset, int pUWidth, int pVHeight) {
		GuiComponent.blit(pPoseStack, pX, pY, (float)pUOffset, (float)pVOffset, pUWidth, pVHeight, 256, 256);
	}

	private static void internalBlit(PoseStack pPoseStack, int pX, int pY, int pWidth, int pHeight, float pUOffset, float pVOffset, int pUWidth, int pVHeight, int pTextureWidth, int pTextureHeight) {
		GuiComponent.blit(pPoseStack, pX, pY, pWidth, pHeight, pUOffset, pVOffset, pUWidth, pVHeight, pTextureWidth, pTextureHeight);
	}

	private static void internalBlit(PoseStack pPoseStack, int pX, int pY, float pUOffset, float pVOffset, int pWidth, int pHeight, int pTextureWidth, int pTextureHeight) {
		GuiComponent.blit(pPoseStack, pX, pY, pUOffset, pVOffset, pWidth, pHeight, pTextureHeight, pTextureWidth);
	}

    public static void blit(ResourceLocation texture, PoseStack pPoseStack, int pX, int pY, int pUOffset, int pVOffset, int pUWidth, int pVHeight) {
		setTexture(texture);
		internalBlit(pPoseStack, pX, pY, pUOffset, pVOffset, pUWidth, pVHeight);
	}

	public static void blit(ResourceLocation texture, PoseStack pPoseStack, int pX, int pY, int pWidth, int pHeight, float pUOffset, float pVOffset, int pUWidth, int pVHeight, int pTextureWidth, int pTextureHeight) {
		setTexture(texture);
		internalBlit(pPoseStack, pX, pY, pWidth, pHeight, pUOffset, pVOffset, pUWidth, pVHeight, pTextureWidth, pTextureHeight);
	}

	public static void blit(ResourceLocation texture, PoseStack pPoseStack, int pX, int pY, float pUOffset, float pVOffset, int pWidth, int pHeight, int pTextureWidth, int pTextureHeight) {
		setTexture(texture);
		internalBlit(pPoseStack, pX, pY, pUOffset, pVOffset, pWidth, pHeight, pTextureHeight, pTextureWidth);
	}

    public static void blit(int textureId, PoseStack pPoseStack, int pX, int pY, int pUOffset, int pVOffset, int pUWidth, int pVHeight) {
		setTexture(textureId);
		internalBlit(pPoseStack, pX, pY, pUOffset, pVOffset, pUWidth, pVHeight);
	}

	public static void blit(int textureId, PoseStack pPoseStack, int pX, int pY, int pWidth, int pHeight, float pUOffset, float pVOffset, int pUWidth, int pVHeight, int pTextureWidth, int pTextureHeight) {
		setTexture(textureId);
		internalBlit(pPoseStack, pX, pY, pWidth, pHeight, pUOffset, pVOffset, pUWidth, pVHeight, pTextureWidth, pTextureHeight);
	}

	public static void blit(int textureId, PoseStack pPoseStack, int pX, int pY, float pUOffset, float pVOffset, int pWidth, int pHeight, int pTextureWidth, int pTextureHeight) {
		setTexture(textureId);
		internalBlit(pPoseStack, pX, pY, pUOffset, pVOffset, pWidth, pHeight, pTextureHeight, pTextureWidth);
	}
	

	public static ResizableButton createButton(int x, int y, int width, int height, Component text, Consumer<Button> onClick) {
		return new ResizableButton(x, y, width, height, text, (btn) -> onClick.accept(btn));
	}

	public static <T extends Enum<T> & ITranslatableEnum> ResizableCycleButton<T> createCycleButton(String modid, Class<T> clazz, int x, int y, int width, int height, Component text, T initialValue, BiConsumer<ResizableCycleButton<?>, T> onValueChanged) {
        ResizableCycleButton<T> btn = ResizableCycleButton.<T>builder((p) -> {            
            return Utils.translate(clazz.cast(p).getValueTranslationKey(modid));
        })
            .withValues(clazz.getEnumConstants()).withInitialValue(initialValue)
            .create(x, y, width, height, text, (b, v) -> onValueChanged.accept(b, v))
        ;
		return btn;
	}

    public static ResizableCycleButton<Boolean> createOnOffButton(int x, int y, int width, int height, Component text, boolean initialValue, BiConsumer<ResizableCycleButton<?>, Boolean> onValueChanged) {
        ResizableCycleButton<Boolean> btn = ResizableCycleButton.onOffBuilder(initialValue)
            .create(x, y, width, height, text, (b, v) -> onValueChanged.accept(b, v))
        ;
		
		return btn;
	}

    public static EditBox createEditBox(int x, int h, int width, int height, Font font, String text, boolean drawBg, Consumer<String> onValueChanged, BiConsumer<EditBox, Boolean> onFocusChanged) {
        ModEditBox box = new ModEditBox(font, x, h, width, height, Utils.text(text));
        box.setOnFocusChanged(onFocusChanged);
		box.setResponder(onValueChanged);
        box.setValue(text);
        box.setBordered(drawBg);
		
		return box;
    }

    public static ModSlider createSlider(int x, int y, int width, int height, Component prefix, Component suffix, double min, double max, double step, double initialValue, boolean drawLabel, BiConsumer<ModSlider, Double> onValueChanged, Consumer<ModSlider> onUpdateMessage) {
        
        ModSlider slider = new ModSlider(x, y, width, height, prefix, suffix, min, max, initialValue, step, 1, drawLabel, null) {
            @Override
            protected void updateMessage() {
                if (onUpdateMessage == null) {
                    if (this.drawString) {
                        this.setMessage(Utils.text("").append(prefix).append(": ").append(this.getValueString()).append(suffix));
                    } else {
                        this.setMessage(Utils.emptyText());
                    } 
                    return;
                }                
                onUpdateMessage.accept(this);                       
            }

            @Override
            protected void applyValue() {
                super.applyValue();
                onValueChanged.accept(this, this.getValue());
            }
        };
        
		return slider;
    }

    public static void renderBoundingBox(PoseStack poseStack, GuiAreaDefinition area, int fillColor, int borderColor) {
        GuiComponent.fill(poseStack, area.getLeft(), area.getTop(), area.getRight(), area.getBottom(), fillColor);
        GuiComponent.fill(poseStack, area.getLeft(), area.getTop() - 1, area.getRight(), area.getTop(), borderColor);
        GuiComponent.fill(poseStack, area.getLeft(), area.getBottom(), area.getRight(), area.getBottom() + 1, borderColor);
        GuiComponent.fill(poseStack, area.getLeft() - 1, area.getTop() - 1, area.getLeft(), area.getBottom() + 1, borderColor);
        GuiComponent.fill(poseStack, area.getRight(), area.getTop() - 1, area.getRight() + 1, area.getBottom() + 1, borderColor);
    }	
}

