package de.mrjulsen.trafficcraft.block.entity;

import java.io.IOException;

import javax.annotation.Nullable;

import com.mojang.blaze3d.platform.NativeImage;

import de.mrjulsen.trafficcraft.ModMain;
import de.mrjulsen.trafficcraft.block.TrafficSignBlock;
import de.mrjulsen.trafficcraft.block.properties.TrafficSignShape;
import de.mrjulsen.trafficcraft.util.BlockEntityUtil;
import de.mrjulsen.trafficcraft.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class TrafficSignBlockEntity extends BlockEntity implements AutoCloseable {

    private static final String TEXTURE_TAG = "texture";

    // Properties
    private DynamicTexture texture = null;
    private DynamicTexture background = null;

    protected TrafficSignBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public TrafficSignBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TRAFFIC_SIGN_BLOCK_ENTITY.get(), pos, state);
    }

    @Override
    public void load(CompoundTag compound) {
        super.load(compound);

        if (compound.contains(TEXTURE_TAG))
            setTexture(compound.getString(TEXTURE_TAG));
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        if (texture != null) {
            tag.putString(TEXTURE_TAG, Utils.textureToBase64(this.texture.getPixels()));
        }
        super.saveAdditional(tag);
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this, BlockEntity::getUpdateTag);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return this.saveWithFullMetadata();
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        this.load(pkt.getTag());
    }

    public DynamicTexture getDynamicTexture() {
        return this.texture;
    }

    public DynamicTexture getBackground() {
        return this.background;
    }

    public boolean hasBackground() {
        return this.background != null;
    }

    public DynamicTexture setTexture(DynamicTexture texture) {
        close();
        this.texture = texture;
    
        if (this.texture != null && this.getBlockState().getValue(TrafficSignBlock.SHAPE) == TrafficSignShape.MISC) {
            try {
                NativeImage bg = NativeImage.read(Minecraft.getInstance().getResourceManager().getResource(new ResourceLocation(ModMain.MOD_ID, "textures/block/sign/blank.png")).getInputStream());
                for (int x = 0; x < 32; x++) {
                    for (int y = 0; y < 32; y++) {
                        if (texture.getPixels().getPixelRGBA(x, y) != 0)
                            continue;

                        bg.setPixelRGBA(31 - x, y, 0);
                    }
                }
                this.background = new DynamicTexture(bg);
            } catch (IOException e) {
                e.printStackTrace();
            }            
        } else if (hasBackground()) {
            this.background.close();
            this.background = null;
        }

        BlockEntityUtil.sendUpdatePacket(this);
        this.setChanged();
        return this.texture;
    }

    public DynamicTexture setTexture(String base64) {
        try {
            return this.setTexture(new DynamicTexture(NativeImage.fromBase64(base64)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return this.texture;
    }

    @Override
    public void close() {
        if (texture != null) {
            texture.close();
            texture = null;
        }
    }
}
