package de.mrjulsen.trafficcraft.screen;

import java.util.List;
import java.util.regex.Pattern;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import de.mrjulsen.trafficcraft.ModMain;
import de.mrjulsen.trafficcraft.item.PatternCatalogueItem;
import de.mrjulsen.trafficcraft.network.NetworkManager;
import de.mrjulsen.trafficcraft.network.packets.PatternCatalogueIndexPacket;
import de.mrjulsen.trafficcraft.screen.widgets.AreaRenderer;
import de.mrjulsen.trafficcraft.screen.widgets.AreaRenderer.AreaStyle;
import de.mrjulsen.trafficcraft.screen.widgets.AreaRenderer.ColorStyle;
import de.mrjulsen.trafficcraft.screen.widgets.ControlCollection;
import de.mrjulsen.trafficcraft.screen.widgets.GuiAreaDefinition;
import de.mrjulsen.trafficcraft.screen.widgets.HScrollBar;
import de.mrjulsen.trafficcraft.screen.widgets.ICustomAreaControl;
import de.mrjulsen.trafficcraft.screen.widgets.IconButton;
import de.mrjulsen.trafficcraft.screen.widgets.IconButton.ButtonType;
import de.mrjulsen.trafficcraft.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TrafficSignPatternSelectionScreen extends Screen
{
    public static final Component title = new TranslatableComponent("gui.trafficcraft.patternselection.title");
    
    private static final int WIDTH = 158;
    private static final int HEIGHT = 200;
    
    private static final int TEXTURE_WIDTH = 158;
    private static final int TEXTURE_HEIGHT = 174;

    private static final int MAX_ENTRIES_IN_ROW = 6;
    private static final int MAX_ROWS = 6;

    private static final int ICON_BUTTON_WIDTH = 18;
    private static final int ICON_BUTTON_HEIGHT = 18;
    
    private final ControlCollection groupPatterns = new ControlCollection();
    private HScrollBar scrollbar;

    private int guiTop;
    private int guiLeft;

    private final ItemStack stack;
    
    private static final ResourceLocation OVERLAY = new ResourceLocation(ModMain.MOD_ID, "textures/gui/traffic_sign_workbench_overlay.png");

    public TrafficSignPatternSelectionScreen(ItemStack stack) {
        super(title);
        this.stack = stack;
    }

    @Override
    public boolean isPauseScreen() {
        return true;
    }

    @Override
    public void onClose() {
        NetworkManager.MOD_CHANNEL.sendToServer(new PatternCatalogueIndexPacket(PatternCatalogueItem.getSelectedIndex(stack)));
        super.onClose();
    }

    @Override
    public void init() {
        super.init();
        
        guiLeft = this.width / 2 - WIDTH / 2;
        guiTop = this.height / 2 - HEIGHT / 2;

        groupPatterns.components.clear();

        this.scrollbar = this.addRenderableWidget(new HScrollBar(guiLeft + WIDTH / 2 + ICON_BUTTON_WIDTH * MAX_ENTRIES_IN_ROW / 2, guiTop + 45 - 1, 8, MAX_ROWS * ICON_BUTTON_HEIGHT + 2,
            new GuiAreaDefinition(
                guiLeft + WIDTH / 2 - ICON_BUTTON_WIDTH * MAX_ENTRIES_IN_ROW / 2,
                guiTop + 45,
                MAX_ENTRIES_IN_ROW * ICON_BUTTON_WIDTH,
                MAX_ROWS * ICON_BUTTON_HEIGHT
            )
        ));

        final int count = PatternCatalogueItem.getStoredPatternCount(stack);
        for (int i = 0; i < count; i++) {
            final int j = i;
            IconButton btn = new IconButton(ButtonType.RADIO_BUTTON, groupPatterns, guiLeft + 9, guiTop + 36 + j * ICON_BUTTON_HEIGHT, ICON_BUTTON_WIDTH, ICON_BUTTON_HEIGHT, title, (button) -> {
                PatternCatalogueItem.setSelectedIndex(stack, j);
            }, (button, poseStack, mouseX, mouseY) -> {
                Utils.renderTooltip(this, button, () -> List.of(new TextComponent(PatternCatalogueItem.getPatternAt(stack, j).getName()).getVisualOrderText()), poseStack, mouseX, mouseY);
            }) {
                @Override
                protected void renderBg(PoseStack pPoseStack, Minecraft pMinecraft, int pMouseX, int pMouseY) {
                    super.renderBg(pPoseStack, pMinecraft, pMouseX, pMouseY);
                    RenderSystem.setShaderTexture(0, PatternCatalogueItem.getPatternAt(stack, j).getDynamicTexture().getId());
                    blit(pPoseStack, x + 1, y + 1, ICON_BUTTON_WIDTH - 2, ICON_BUTTON_HEIGHT - 2, 0, 0, 32, 32, 32, 32);
                }
            };
            this.addRenderableWidget(btn);
        }

        fillButtons(groupPatterns.components.toArray(IconButton[]::new), guiLeft + WIDTH / 2 - ICON_BUTTON_WIDTH * MAX_ENTRIES_IN_ROW / 2 - 1, guiTop + 45);
    }

    private void fillButtons(IconButton[] buttons, int defX, int defY) {
        int currentRow = -1;
        for (int i = 0; i < buttons.length; i++) {
            if (i % MAX_ENTRIES_IN_ROW == 0)
                currentRow++;

            buttons[i].x = defX + (i % MAX_ENTRIES_IN_ROW) * ICON_BUTTON_WIDTH;
            buttons[i].y = defY + (currentRow) * ICON_BUTTON_HEIGHT;
        }
    }

    @Override
    public void render(PoseStack pPoseStack, int mouseX, int mouseY, float partialTicks) {        
        renderBackground(pPoseStack, 0);
        
        RenderSystem.setShaderTexture(0, OVERLAY);
        blit(pPoseStack, guiLeft, guiTop + 26, 0, 0, TEXTURE_WIDTH, TEXTURE_HEIGHT, 256, 256);
        AreaRenderer.renderArea(pPoseStack, guiLeft + WIDTH / 2 - ICON_BUTTON_WIDTH * MAX_ENTRIES_IN_ROW / 2 - 2, guiTop + 45 - 1, MAX_ENTRIES_IN_ROW * ICON_BUTTON_WIDTH + 2, MAX_ROWS * ICON_BUTTON_HEIGHT + 2, ColorStyle.BROWN, AreaStyle.SUNKEN);

        RenderSystem.setShaderTexture(0, PatternCatalogueItem.getSelectedPattern(stack).getDynamicTexture().getId());
        blit(pPoseStack, guiLeft + 15, guiTop + HEIGHT - 15 - 24, 24, 24, 0, 0, 32, 32, 32, 32);

        float scale = 0.75f;
        pPoseStack.scale(scale, scale, scale);
        this.font.draw(pPoseStack, PatternCatalogueItem.getSelectedPattern(stack).getName(), (guiLeft + 15 + 30) / scale, (guiTop + HEIGHT - 15 - 24 / 2 - this.font.lineHeight / 2) / scale, 4210752);
        pPoseStack.setIdentity();

        drawCenteredString(pPoseStack, this.font, title, this.width / 2, guiTop, 16777215);
        
        super.render(pPoseStack, mouseX, mouseY, partialTicks);
        groupPatterns.performForEach(x -> x.renderToolTip(pPoseStack, mouseX, mouseY));
    }

    @Override
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        if (this.shouldCloseOnEsc() && pKeyCode == 256 || this.minecraft.options.keyInventory.isActiveAndMatches(InputConstants.getKey(pKeyCode, pScanCode))) {
            this.onClose();
            return true;
        } else {
            return super.keyPressed(pKeyCode, pScanCode, pModifiers);
        }
    }

    @Override
    public boolean mouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY) {
        boolean[] b = new boolean[] { false };
        this.renderables.stream().filter(x -> x instanceof GuiEventListener).forEach(x -> {
            if (((GuiEventListener)x).mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY) && !b[0])
                b[0] = true;
        });
        super.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY);
        return b[0];
    }

    @Override
    public boolean mouseScrolled(double pMouseX, double pMouseY, double pDelta) {
        boolean[] b = new boolean[] { false };
        this.renderables.stream().filter(x -> x instanceof ICustomAreaControl && x instanceof GuiEventListener).forEach(x -> {
            if (((ICustomAreaControl)x).isInArea(pMouseX, pMouseY) && ((GuiEventListener)x).mouseScrolled(pMouseX, pMouseY, pDelta) && !b[0]) {
                b[0] = true;
            }
        });
        super.mouseScrolled(pMouseX, pMouseY, pDelta);
        return b[0];
    }

    @Override
    public boolean mouseReleased(double pMouseX, double pMouseY, int pButton) {
        boolean[] b = new boolean[] { false };
        this.renderables.stream().filter(x -> x instanceof GuiEventListener).forEach(x -> {
            if (((GuiEventListener)x).mouseReleased(pMouseX, pMouseY, pButton) && !b[0]) {
                b[0] = true;
            }
        });
        super.mouseReleased(pMouseX, pMouseY, pButton);
        return b[0];
    }
}
