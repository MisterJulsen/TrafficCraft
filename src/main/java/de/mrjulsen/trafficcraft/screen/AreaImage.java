package de.mrjulsen.trafficcraft.screen;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class AreaImage {

    private final Level world;
    private final BlockPos pos;
    private final int yLevel;

    private final int areaWidth, areaHeight;

    private DynamicTexture texture;

    public AreaImage(Level world, BlockPos pos, int yLevel, int width, int height){
        this.world = world;
        this.pos = pos;
        this.yLevel = yLevel;
        this.areaWidth = width;
        this.areaHeight = height;
    }

    public void bindTexture(){
        if(this.texture == null)
            this.texture = new DynamicTexture(this.createImage());
        RenderSystem.setShaderTexture(0, this.texture.getId());
    }

    public void dispose(){
        if(this.texture != null){
            this.texture.close();
            this.texture = null;
        }
    }

    private NativeImage createImage(){
        NativeImage image = new NativeImage(NativeImage.Format.RGBA, areaWidth, areaHeight, false);

        int rWidth = areaWidth / 2;
        int rHeight = areaHeight / 2;
        int minX = pos.getX() - rWidth;
        int minZ = pos.getZ() - rHeight;

        for(int x = 0; x < areaWidth; x++){
            for(int z = 0; z < areaHeight; z++){
                BlockPos pos;
                int northY, westY;
                if(this.shouldDrawAtSameLayer()){
                    pos = this.getFirstBlockGoingDown(minX + x, this.yLevel + 1, minZ + z, 5);
                    northY = this.getFirstBlockGoingDown(minX + x, this.yLevel + 1, minZ + z - 1, 6).getY();
                    westY = this.getFirstBlockGoingDown(minX + x - 1, this.yLevel + 1, minZ + z, 6).getY();
                }else{
                    pos = this.world.getHeightmapPos(Heightmap.Types.WORLD_SURFACE, new BlockPos(minX + x, 0, minZ + z)).below();
                    northY = this.world.getHeight(Heightmap.Types.WORLD_SURFACE, pos.getX(), pos.getZ() - 1) - 1;
                    westY = this.world.getHeight(Heightmap.Types.WORLD_SURFACE, pos.getX() - 1, pos.getZ()) - 1;
                }

                BlockState state = this.world.getBlockState(pos);
                MaterialColor color = state.getMapColor(this.world, pos);
                // Apparently blocks can return null map color #66
                int rgb = color == null ? MaterialColor.NONE.col : color.col;

                int red = ((rgb >> 16) & 255);
                int green = ((rgb >> 8) & 255);
                int blue = (rgb & 255);

                if((pos.getY() > northY && northY >= 0) || (pos.getY() > westY && westY >= 0)){
                    if(red == 0 && green == 0 && blue == 0){
                        red = 3;
                        green = 3;
                        blue = 3;
                    }else{
                        if(red > 0 && red < 3) red = 3;
                        if(green > 0 && green < 3) green = 3;
                        if(blue > 0 && blue < 3) blue = 3;
                        red = Math.min((int)(red / 0.7), 255);
                        green = Math.min((int)(green / 0.7), 255);
                        blue = Math.min((int)(blue / 0.7), 255);
                    }
                }
                if((pos.getY() < northY && northY >= 0) || (pos.getY() < westY && westY >= 0)){
                    red = Math.max((int)(red * 0.7), 0);
                    green = Math.max((int)(green * 0.7), 0);
                    blue = Math.max((int)(blue * 0.7), 0);
                }

                image.setPixelRGBA(x, z, (255 << 24) | (blue << 16) | (green << 8) | red);
            }
        }

        return image;
    }

    private BlockPos getFirstBlockGoingDown(int x, int y, int z, int maxTries){
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(x, y, z);
        int tries = 0;
        while(this.world.isEmptyBlock(pos) && ++tries < maxTries)
            pos.setY(pos.getY() - 1);

        return pos;
    }

    private boolean shouldDrawAtSameLayer(){
        return this.world.dimensionType().hasCeiling();
    }
}