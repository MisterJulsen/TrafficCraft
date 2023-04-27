package de.mrjulsen.trafficcraft.screen;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import de.mrjulsen.trafficcraft.Constants;
import de.mrjulsen.trafficcraft.ModMain;
import de.mrjulsen.trafficcraft.block.properties.TrafficSignShape;
import de.mrjulsen.trafficcraft.network.NetworkManager;
import de.mrjulsen.trafficcraft.network.packets.SignPacket;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TrafficSignEditScreen extends Screen
{
    public static final Component title = new TextComponent("signpicker");

    private static final int WIDTH = 196;
    private static final int HEIGHT = 205;
      
    private int MAX_PATTERNS = 113;
    private int ROWS = (MAX_PATTERNS / 9) - 10 + 1;
    
    
    private int pattern;

    private int guiLeft;
    private int guiTop;
    private int selectX;
    private int selectY;

    private int posX;
    private int posY;

    /*TABS */
    private static final int TAB_X = 196;
    private static final int TAB_WIDTH = 32;
    private static final int TAB_HEIGHT = 28;
    private static final int UNSELECTED_TAB_Y = 0;
    private static final int SELECTED_TAB_Y = UNSELECTED_TAB_Y + TAB_HEIGHT;
    private static final int SHAPE_COUNT = TrafficSignShape.values().length;
    private static final int TAB_ICON_OFFSET_X = 9;
    private static final int TAB_ICON_OFFSET_Y = 6;
    private static final int TAB_START_Y = 14;

    private int scroll = 0;
    private float currentScroll = 0.0f;
    private TrafficSignShape shape = TrafficSignShape.CIRCLE;
    private boolean isScrolling;

    private BlockPos blockPos;
    private Level level;
    private Player player;

    private TranslatableComponent textTitle = new TranslatableComponent("gui.trafficcraft.signpicker.title");
    private TranslatableComponent btnDoneTxt = new TranslatableComponent("gui.done");
    private TranslatableComponent btnCancelTxt = new TranslatableComponent("gui.cancel");

    private static final ResourceLocation gui = new ResourceLocation(ModMain.MOD_ID, "textures/gui/sign_picker.png");

    public TrafficSignEditScreen(int pattern, float scroll, TrafficSignShape shape, BlockPos pos, Level level, Player player)
    {
        super(title);
        this.pattern = pattern;
        this.currentScroll = scroll;
        this.shape = shape;

        this.blockPos = pos;
        this.level = level;
        this.player = player;


        recalculateMaxPatterns();
    }

    private void recalculateMaxPatterns() {
        MAX_PATTERNS = Constants.SIGN_PATTERNS.get(shape) + 1;
        ROWS = Math.max((MAX_PATTERNS / 9) - 10 + 1, 0);
    }

    @Override
    public boolean isPauseScreen()
    {
        return false;
    }

    @Override
    public void init()
    {
        super.init();
        guiLeft = this.width / 2 - WIDTH / 2;
        guiTop = this.height / 2 - (HEIGHT + 24) / 2;

        scroll = (int)(currentScroll * ROWS);
        if(scroll < 0)
            scroll = 0;
        else if(scroll > ROWS)
            scroll = ROWS;

        int pattern_temp = pattern;
        if(pattern > 8)
            pattern_temp = pattern - 9 * (pattern / 9);

        selectX = guiLeft + 6 + pattern_temp * 18;
        selectY = guiTop + 15 + (pattern / 9) * 18;

        this.addRenderableWidget(new Button(guiLeft, guiTop + HEIGHT + 3, (WIDTH - 4) / 2, 20, btnDoneTxt, (p) -> {
            NetworkManager.MOD_CHANNEL.sendToServer(new SignPacket(pattern, shape.getIndex(), scroll, blockPos));            
            level.playSound(player, blockPos, SoundEvents.VILLAGER_WORK_CARTOGRAPHER, SoundSource.BLOCKS, 1F, 0.5f);
            level.playSound(player, blockPos, SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 0.5F, 2f);
            this.onClose();
        }));

        this.addRenderableWidget(new Button(guiLeft + WIDTH / 2 + 2, guiTop + HEIGHT + 3, (WIDTH - 4) / 2, 20, btnCancelTxt, (p) -> {
            this.onClose();
        }));
    }

    @Override
    public void render(PoseStack stack, int mouseX, int mouseY, float partialTicks)
    {
        renderBackground(stack, 0);
        RenderSystem.setShaderTexture(0, gui);
        blit(stack, guiLeft, guiTop, 0, 0, WIDTH, HEIGHT);
        blit(stack, guiLeft + 193, guiTop + 0, 0, 205, 46, 50);

        // Draws patterns
        int j = 0;
        int row = 0;
        for(int i = 0; i < 90; i++)
        {
            if(i + (scroll * 9) < MAX_PATTERNS)
            {
                RenderSystem.setShaderTexture(0, new ResourceLocation(ModMain.MOD_ID, "textures/block/sign/" + shape.getShape() + "/" + shape.getShape() + (i + (9 * scroll)) + ".png"));
                blit(stack, guiLeft + 16 * j + 9 + j * 2, guiTop + 18 + row, 0, 0, 16, 16, 16, 16);

                j++;
                if(j >= 9)
                {
                    j = 0;
                    row += 18;
                }
            }
        }


        // Draws current selected pattern
        RenderSystem.setShaderTexture(0, new ResourceLocation(ModMain.MOD_ID, "textures/block/sign/" + shape.getShape() + "/" + shape.getShape() + pattern + ".png"));
        blit(stack, guiLeft + 198, guiTop + 9, 0, 0, 32, 32, 32, 32);

        // Draws hover square above slots
        if(mouseX > guiLeft + 7 && mouseX < guiLeft + 171 && mouseY > guiTop + 16 && mouseY < guiTop + 196)
        {
            posX = Math.toIntExact(Math.round((mouseX - guiLeft - 9) / 18) * 18) + guiLeft + 9;
            posY = Math.toIntExact(Math.round((mouseY - guiTop - 17) / 18) * 18) + guiTop + 18;
            fill(stack, posX, posY, posX + 16, posY + 16, new Color(255, 255, 255, 128).getRGB());
        }

        // Draws selection box around the selected pattern
        int boxY = selectY - (scroll * 18);

        if(boxY > guiTop && boxY < guiTop + HEIGHT - 27)
        {
            RenderSystem.setShaderTexture(0, gui);
            blit(stack, selectX, boxY, 256 - 22, 256 - 22, 22, 22);
        }

        String title = textTitle.getString();
        this.font.draw(stack, title, guiLeft + WIDTH / 2 - font.width(title) / 2, guiTop + 6, 4210752);

        // Scrollbar
        RenderSystem.setShaderTexture(0, gui);
        if (ROWS > 0) {
            blit(stack, guiLeft + WIDTH - 21, (int)(guiTop + 18 + 164 * this.currentScroll), 256 - 24, 0, 12, 15);
        } else {
            blit(stack, guiLeft + WIDTH - 21, (int)(guiTop + 18 + 164 * this.currentScroll), 256 - 24 + 12, 0, 12, 15);
        }

        // Render tabs
        for (int i = 0; i < SHAPE_COUNT; i++) {
            blit(stack, guiLeft - TAB_WIDTH + 4, guiTop + TAB_START_Y + i * (TAB_HEIGHT + 2), TAB_X, shape.getIndex() == i ? SELECTED_TAB_Y : UNSELECTED_TAB_Y, TAB_WIDTH, TAB_HEIGHT);
        }

        // Render tab icons
        for (int i = 0; i < SHAPE_COUNT; i++) {
            RenderSystem.setShaderTexture(0, new ResourceLocation(ModMain.MOD_ID, "textures/block/sign/icons/" + TrafficSignShape.getShapeByIndex(i).getShape() + ".png"));
            blit(stack, guiLeft - TAB_WIDTH + 4 + TAB_ICON_OFFSET_X, guiTop + TAB_START_Y + i * (TAB_HEIGHT + 2) + TAB_ICON_OFFSET_Y, 0, 0, 16, 16, 16, 16);
        }


        super.render(stack, mouseX, mouseY, partialTicks);

        // Draw favorite tooltip
        if(mouseX > guiLeft - TAB_WIDTH && mouseX < guiLeft && mouseY > guiTop + TAB_START_Y && mouseY < guiTop + TAB_START_Y + SHAPE_COUNT * (TAB_HEIGHT + 2))
        {
            int choice = ((int)(mouseY) - (guiTop + TAB_START_Y)) / (TAB_HEIGHT + 2);

            List<Component> patternTooltipFinal = new ArrayList<Component>();
            Component patternTooltip;

            patternTooltip = new TranslatableComponent(TrafficSignShape.getShapeByIndex(choice).getTranslationKey());
            patternTooltipFinal.add(patternTooltip);

            renderTooltip(stack, patternTooltipFinal, Optional.empty(), mouseX, mouseY);
        }

    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button)
    {

        if(mouseX > guiLeft + 7 && mouseX < guiLeft + WIDTH - 21 && mouseY > guiTop + 14 && mouseY < guiTop + 196)
        {
            if(button == 0)
            {
                int choice = (posX - guiLeft - 9) / 18 + ((posY - guiTop - 9) / 18) * 9 + scroll * 9;
                if(choice < MAX_PATTERNS)
                {
                    pattern = choice;
                    selectX = posX - 3;
                    selectY = posY + scroll * 18 - 3;
                }

                return true;
            }
        }
        else if(mouseX > guiLeft - TAB_WIDTH && mouseX < guiLeft && mouseY > guiTop + TAB_START_Y && mouseY < guiTop + TAB_START_Y + SHAPE_COUNT * (TAB_HEIGHT + 2))
        {
            if(button == 0)
            {
                int choice = ((int)(mouseY) - (guiTop + TAB_START_Y)) / (TAB_HEIGHT + 2);
                pattern = 0;
                scroll = 0;
                selectX = posX - 3;
                selectY = posY + scroll * 18 - 3;
                shape = TrafficSignShape.getShapeByIndex(choice);
                recalculateMaxPatterns();
                return true;
            }
        }

        if(button == 0 && scrollbarClamp(mouseX, mouseY) && ROWS > 0)
            isScrolling = true;
        
            
        return super.mouseClicked(mouseX, mouseY, button);
    }

    public boolean keyPressed(int p_keyPressed_1_, int p_keyPressed_2_, int p_keyPressed_3_)
    {
        if(this.shouldCloseOnEsc() && p_keyPressed_1_ == 256 || this.minecraft.options.keyInventory.isActiveAndMatches(InputConstants.getKey(p_keyPressed_1_, p_keyPressed_2_)))
        {
            this.onClose();
            return true;
        }
        else
        {
            return super.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_);
        }
    }


    @Override
    public boolean mouseScrolled(double p_mouseScrolled_1_, double p_mouseScrolled_3_, double p_mouseScrolled_5_)
    {
        if (ROWS > 0) {
            scroll -= p_mouseScrolled_5_;
            if(scroll < 0)
                scroll = 0;
            else if(scroll > ROWS)
                scroll = ROWS;

            int i = ROWS;
            this.currentScroll = (float)((double)this.currentScroll - p_mouseScrolled_5_ / (double)i);
            this.currentScroll = Mth.clamp(this.currentScroll, 0.0F, 1.0F);

            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean mouseDragged(double p_mouseDragged_1_, double p_mouseDragged_3_, int p_mouseDragged_5_, double p_mouseDragged_6_, double p_mouseDragged_8_)
    {
        if(this.isScrolling)
        {
            int i = this.guiTop + 18;
            int j = i + 179;
            this.currentScroll = ((float)p_mouseDragged_3_ - (float)i - 7.5F) / ((float)(j - i) - 15.0F);
            this.currentScroll = Mth.clamp(this.currentScroll, 0.0F, 1.0F);
            scroll = (int)((currentScroll + 0.01) * ROWS);
            if(scroll < 0)
                scroll = 0;
            return true;
        }
        else
        {
            return super.mouseDragged(p_mouseDragged_1_, p_mouseDragged_3_, p_mouseDragged_5_, p_mouseDragged_6_, p_mouseDragged_8_);
        }
    }

    @Override
    public boolean mouseReleased(double p_mouseReleased_1_, double p_mouseReleased_3_, int p_mouseReleased_5_)
    {
        if(p_mouseReleased_5_ == 0)
        {
            this.isScrolling = false;
        }

        return super.mouseReleased(p_mouseReleased_1_, p_mouseReleased_3_, p_mouseReleased_5_);
    }

    protected boolean scrollbarClamp(double mouseX, double mouseY)
    {
        int i = this.guiLeft;
        int j = this.guiTop;
        int k = i + 175;
        int l = j + 18;
        int i1 = k + 13;
        int j1 = l + 179;
        return mouseX >= (double)k && mouseY >= (double)l && mouseX < (double)i1 && mouseY < (double)j1;
    }
}
