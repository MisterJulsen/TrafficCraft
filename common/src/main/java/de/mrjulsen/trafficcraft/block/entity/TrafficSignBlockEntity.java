package de.mrjulsen.trafficcraft.block.entity;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import de.mrjulsen.mcdragonlib.block.DLSyncedBlockEntity;
import de.mrjulsen.mcdragonlib.block.IBlockEntityExtension;
import de.mrjulsen.mcdragonlib.network.NetworkDirection;
import de.mrjulsen.mcdragonlib.util.DLUtils;
import de.mrjulsen.trafficcraft.block.TrafficSignBlock;
import de.mrjulsen.trafficcraft.data.NamedTrafficSignTextureReference;
import de.mrjulsen.trafficcraft.data.TrafficSignClientTexture;
import de.mrjulsen.trafficcraft.data.TrafficSignTextureData;
import de.mrjulsen.trafficcraft.network.packets.stc.TrafficSignTextureResetPacket;
import de.mrjulsen.trafficcraft.registry.ModBlockEntities;
import de.mrjulsen.trafficcraft.registry.ModNetworkManager;
import dev.architectury.utils.GameInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class TrafficSignBlockEntity extends DLSyncedBlockEntity implements IBlockEntityExtension {

    private static final String NBT_LEGACY_TEXTURE = "texture";
    private static final String NBT_TEXTURE = "SignTexture";

    private String textureId;
    private TrafficSignClientTexture texture;
    

    protected TrafficSignBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);        
    }

    public TrafficSignBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TRAFFIC_SIGN_BLOCK_ENTITY.get(), pos, state);
    }

    @Override
    public void load(CompoundTag compound) {
        super.load(compound);

        if (compound.contains(NBT_LEGACY_TEXTURE)) {
            migrate(compound.getString(NBT_LEGACY_TEXTURE));
        } else if (compound.contains(NBT_TEXTURE)) {
            setTextureId(compound.getString(NBT_TEXTURE));
        }
    }

    private void migrate(String base64) {
        new Thread(() -> {
            while (getLevel() == null) {
                try { TimeUnit.SECONDS.sleep(1); } catch (InterruptedException e) { }
            }
            if (getLevel().isClientSide) return;
            
            GameInstance.getServer().execute(() -> {
                BlockState state = getLevel().getBlockState(getBlockPos());
                TrafficSignTextureData data = new TrafficSignTextureData(state.getValue(TrafficSignBlock.SHAPE), java.util.Base64.getDecoder().decode(base64), (short)32, (short)32, System.currentTimeMillis(), new UUID(0, 0));
                data.save();
                setTextureId(data.getHash().toString());
            });
        }, "Traffic Sign Migration").start();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        if (textureId != null) {
            tag.putString(NBT_TEXTURE, getTextureId());
        }
        super.saveAdditional(tag);
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        resetTexture();
    }

    public String getTextureId() {
        return textureId;
    }

    public TrafficSignClientTexture getClientTexture() {
        if (texture == null) {
            if (getTextureId() == null || getTextureId().equals("empty")) {
                return TrafficSignClientTexture.EMPTY;
            }
            texture = TrafficSignClientTexture.load(getTextureId(), true, null);
        }
        return texture;
    }

    public void resetTexture() {
        if (level.isClientSide) {
            DLUtils.doIfNotNull(texture, x -> x.close());
            texture = null;
        }
    }

    public void setAndResetTexture(NamedTrafficSignTextureReference texture) {
        setTextureId(texture.getTextureId());
        if (!this.level.isClientSide) {
            for (ServerPlayer player : level.players().stream().filter(p -> p instanceof ServerPlayer).toArray(ServerPlayer[]::new)) {
                ModNetworkManager.RESET_TRAFFIC_DIGN_TEXTURE.send(NetworkDirection.toPlayer(player), new TrafficSignTextureResetPacket(getBlockPos()));
            }
        }
    }

    public void setTextureId(String id) {
        this.textureId = id;
        notifyUpdate();
    }
}
