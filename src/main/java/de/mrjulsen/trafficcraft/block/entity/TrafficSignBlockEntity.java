package de.mrjulsen.trafficcraft.block.entity;

import javax.annotation.Nullable;

import de.mrjulsen.trafficcraft.client.ClientWrapper;
import de.mrjulsen.trafficcraft.network.NetworkManager;
import de.mrjulsen.trafficcraft.network.packets.TrafficSignTextureResetPacket;
import de.mrjulsen.trafficcraft.registry.ModBlockEntities;
import de.mrjulsen.mcdragonlib.common.BlockEntityUtil;
import de.mrjulsen.mcdragonlib.common.IIdentifiable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkDirection;

public class TrafficSignBlockEntity extends BlockEntity implements IIdentifiable, AutoCloseable {

    private static final String TEXTURE_TAG = "texture";
    private final String ID;

    private String textureData;

    protected TrafficSignBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        ID = String.format("%s,%s,%s", pos.getX(), pos.getY(), pos.getZ());
    }

    public TrafficSignBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TRAFFIC_SIGN_BLOCK_ENTITY.get(), pos, state);
        ID = String.format("%s,%s,%s", pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public void load(CompoundTag compound) {
        super.load(compound);

        if (compound.contains(TEXTURE_TAG))
            setBase64Texture(compound.getString(TEXTURE_TAG));
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        if (textureData != null) {
            tag.putString(TEXTURE_TAG, getTexture());
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

    public String getTexture() {
        return textureData;
    }

    public void setAndResetTexture(String base64) {
        setBase64Texture(base64);
        if (!this.level.isClientSide) {
            for (ServerPlayer player : level.players().stream().filter(p -> p instanceof ServerPlayer).toArray(ServerPlayer[]::new)) {
                NetworkManager.MOD_CHANNEL.sendTo(new TrafficSignTextureResetPacket(ID), player.connection.getConnection(), NetworkDirection.PLAY_TO_CLIENT);
            }
        }
    }

    public void setBase64Texture(String base64) {        
        textureData = base64;
        BlockEntityUtil.sendUpdatePacket(this);
        this.setChanged();
    }

    @Override
    public void onChunkUnloaded() {
        clear();
        super.onChunkUnloaded();
    }

    private void clear() {
        if (!this.level.isClientSide) {
            for (ServerPlayer player : level.players().stream().filter(p -> p instanceof ServerPlayer).toArray(ServerPlayer[]::new)) {
                NetworkManager.MOD_CHANNEL.sendTo(new TrafficSignTextureResetPacket(ID), player.connection.getConnection(), NetworkDirection.PLAY_TO_CLIENT);
            }
        }
    }

    @Override
    public void close() {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientWrapper.clearTexture(this));
    }

    @Override
    protected void finalize() {
        this.close();
    }
}
