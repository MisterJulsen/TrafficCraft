package de.mrjulsen.trafficcraft.screen;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import de.mrjulsen.trafficcraft.ModMain;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GuiTrafficLightMap extends Screen
{
    public static final Component title = new TextComponent("trafficlightcontroller");

    private AreaImage image;
    
    private static final int WIDTH = 213;
    private static final int HEIGHT = 222; 
    
    private static final int MAP_X = 9;
    private static final int MAP_Y = 18;

    private static final int MAX_RADIUS = 32;
    
    private int guiLeft;
    private int guiTop;


    private BlockPos blockPos;
    private Level level;

    private TranslatableComponent textTitle = new TranslatableComponent("gui.trafficcraft.trafficlightcontroller.title");
    private TranslatableComponent btnDoneTxt = new TranslatableComponent("gui.done");
    private TranslatableComponent btnCancelTxt = new TranslatableComponent("gui.cancel");

    private static final ResourceLocation gui = new ResourceLocation(ModMain.MOD_ID, "textures/gui/traffic_light_controller.png");

    public GuiTrafficLightMap(BlockPos pos, Level level)
    {
        super(title);
        this.level = level;
        this.blockPos = pos;

        image = new AreaImage(level, pos, pos.getY(), MAX_RADIUS * 2 + 1, MAX_RADIUS * 2 + 1);
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
        guiTop = this.height / 2 - HEIGHT / 2;      
    }

    @Override
    public void render(PoseStack stack, int mouseX, int mouseY, float partialTicks)
    {
        renderBackground(stack, 0);
        RenderSystem.setShaderTexture(0, gui);
        blit(stack, guiLeft, guiTop, 0, 0, WIDTH, HEIGHT);

        String title = textTitle.getString();
        this.font.draw(stack, title, guiLeft + WIDTH / 2 - font.width(title) / 2, guiTop + 6, 4210752);

        image.bindTexture();
        blit(stack, guiLeft + MAP_X, guiTop + MAP_Y, 0, 0, 192, 192, 192, 192);
        
        super.render(stack, mouseX, mouseY, partialTicks);
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
    public void onClose() {
        image.dispose();
        super.onClose();
    }
}
