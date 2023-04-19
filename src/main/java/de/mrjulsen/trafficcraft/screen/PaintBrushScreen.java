package de.mrjulsen.trafficcraft.screen;

import java.awt.Color;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import de.mrjulsen.trafficcraft.Constants;
import de.mrjulsen.trafficcraft.ModMain;
import de.mrjulsen.trafficcraft.network.NetworkManager;
import de.mrjulsen.trafficcraft.network.packets.PaintBrushPacket;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.DyeColor;

public class PaintBrushScreen extends Screen
{
    public static final Component title = new TextComponent("paintbrush");

    
    private static final int WIDTH = 234;
    private static final int HEIGHT = 205;
      
    private int MAX_PATTERNS = Constants.MAX_ASPHALT_PATTERNS;
    private int ROWS = (MAX_PATTERNS / 9) - 10 + 1;
    
    
    private int pattern;

    private int guiLeft;
    private int guiTop;
    private int selectX;
    private int selectY;

    private int posX;
    private int posY;

    private int scroll = 0;
    private float currentScroll = 0.0f;
    private boolean isScrolling;
    private int paint;
    private DyeColor color;

    private TranslatableComponent textTitle = new TranslatableComponent("gui.trafficcraft.signpicker.title");

    private static final ResourceLocation gui = new ResourceLocation(ModMain.MOD_ID, "textures/gui/paint_brush.png");

    public PaintBrushScreen(int pattern, int paint, int color, float scroll)
    {
        super(title);
        this.pattern = pattern;
        this.currentScroll = scroll;
        this.color = DyeColor.byId(color);
        this.paint = paint;
    }

    private float calcPaintPercentage() {
        return 100.0f / Constants.MAX_PAINT * paint;
    }

    @Override
    public boolean isPauseScreen() {
        return true;
    }

    @Override
    public void init() {
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
    }

    @Override
    public void render(PoseStack stack, int mouseX, int mouseY, float partialTicks) {
        renderBackground(stack, 0);
        RenderSystem.setShaderTexture(0, gui);
        blit(stack, guiLeft, guiTop, 0, 0, WIDTH, HEIGHT);

        // Draws patterns
        int j = 0;
        int row = 0;
        float[] diffuseColor = this.color.getTextureDiffuseColors();
        RenderSystem.setShaderColor(diffuseColor[0], diffuseColor[1], diffuseColor[2], 1);

        for(int i = 0; i < 90; i++)
        {
            if(i + (scroll * 9) < MAX_PATTERNS)
            {
                int index = (i + (9 * scroll));
                RenderSystem.setShaderTexture(0, new ResourceLocation(ModMain.MOD_ID, "textures/block/patterns/" + (index == 0 ? "eraser" : index) + ".png"));
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
        RenderSystem.setShaderTexture(0, new ResourceLocation(ModMain.MOD_ID, "textures/block/patterns/" + (pattern == 0 ? "eraser" : pattern) + ".png"));
        blit(stack, guiLeft + 193, guiTop + 18, 0, 0, 32, 32, 32, 32);

        // Paint amount
        RenderSystem.setShaderTexture(0, gui);
        int p = (int)(100.0f * (this.calcPaintPercentage() / 100.0f));
        blit(stack, guiLeft + WIDTH - 41, guiTop + 196 - p, WIDTH, 200 - p, 12, p + 1);
        RenderSystem.setShaderColor(1, 1, 1, 1);
        blit(stack, guiLeft + WIDTH - 41, guiTop + 96, WIDTH, 0, 12, 100);

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
            blit(stack, selectX, boxY, 256 - 22, 256 - 22, 22, 22);
        }

        // Scrollbar
        if (ROWS > 0) {
            blit(stack, guiLeft + WIDTH - 59, (int)(guiTop + 18 + 163 * this.currentScroll), 0, HEIGHT, 12, 15);
        } else {
            blit(stack, guiLeft + WIDTH - 59, (int)(guiTop + 18 + 163 * this.currentScroll), 12, HEIGHT, 12, 15);
        }

        String title = textTitle.getString();
        this.font.draw(stack, title, guiLeft + WIDTH / 2 - font.width(title) / 2, guiTop + 6, 4210752);
       
        super.render(stack, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button)
    {

        if(mouseX > guiLeft + 7 && mouseX < guiLeft + WIDTH - 64 && mouseY > guiTop + 14 && mouseY < guiTop + 196)
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

        if(button == 0 && scrollbarClamp(mouseX, mouseY) && ROWS > 0)
            isScrolling = true;
        
            
        return super.mouseClicked(mouseX, mouseY, button);
    }

    public boolean keyPressed(int p_keyPressed_1_, int p_keyPressed_2_, int p_keyPressed_3_)
    {
        if (this.shouldCloseOnEsc() && p_keyPressed_1_ == 256 || this.minecraft.options.keyInventory.isActiveAndMatches(InputConstants.getKey(p_keyPressed_1_, p_keyPressed_2_))) {
            NetworkManager.MOD_CHANNEL.sendToServer(new PaintBrushPacket(pattern, currentScroll));
            
            this.onClose();
            return true;
        } else {
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
            int j = i + 178;
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
        int j1 = l + 178;
        return mouseX >= (double)k && mouseY >= (double)l && mouseX < (double)i1 && mouseY < (double)j1;
    }
}
