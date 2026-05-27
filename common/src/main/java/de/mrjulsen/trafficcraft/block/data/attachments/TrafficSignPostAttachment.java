package de.mrjulsen.trafficcraft.block.data.attachments;

import com.mojang.math.Axis;
import de.mrjulsen.mcdragonlib.block.DLSyncedBlockEntity;
import de.mrjulsen.mcdragonlib.client.ber.BERGraphics;
import de.mrjulsen.mcdragonlib.client.model.ModelContext;
import de.mrjulsen.mcdragonlib.client.model.mesh.BasicMesh;
import de.mrjulsen.mcdragonlib.client.model.mesh.Mesh;
import de.mrjulsen.mcdragonlib.client.util.RenderUtils;
import de.mrjulsen.mcdragonlib.network.NetworkDirection;
import de.mrjulsen.mcdragonlib.util.DLColor;
import de.mrjulsen.mcdragonlib.util.DLUtils;
import de.mrjulsen.trafficcraft.TrafficCraft;
import de.mrjulsen.trafficcraft.block.data.TrafficSignShape;
import de.mrjulsen.trafficcraft.block.entity.PostBlockEntity;
import de.mrjulsen.trafficcraft.data.NamedTextureKey;
import de.mrjulsen.trafficcraft.data.textures.ClientTextureCache;
import de.mrjulsen.trafficcraft.data.textures.TextureHandle;
import de.mrjulsen.trafficcraft.data.textures.TextureIdentifier;
import de.mrjulsen.trafficcraft.data.textures.TextureRepository;
import de.mrjulsen.trafficcraft.data.textures.data.ITextureData;
import de.mrjulsen.trafficcraft.data.textures.data.TrafficSignData;
import de.mrjulsen.trafficcraft.data.textures.decoder.context.IDecoderContext;
import de.mrjulsen.trafficcraft.item.CreativePatternCatalogueItem;
import de.mrjulsen.trafficcraft.item.PatternCatalogueItem;
import de.mrjulsen.trafficcraft.network.packets.cts.UpdateTrafficSignShapePacket;
import de.mrjulsen.trafficcraft.network.packets.stc.TrafficSignTextureResetPacket;
import de.mrjulsen.trafficcraft.registry.ModNetworkManager;
import de.mrjulsen.trafficcraft.registry.ModRegistries;
import de.mrjulsen.trafficcraft.registry.builtin.PostAttachmentRegistry;
import de.mrjulsen.trafficcraft.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Vector3f;

import java.util.Objects;

public class TrafficSignPostAttachment extends AbstractPostAttachment<TrafficSignPostAttachment> {

    private static final String NBT_SHAPE = "Shape";
    private static final String NBT_TEXTURE = "SignTexture";

    private static final VoxelShape SHAPE = Block.box(0, 0, 6, 16, 16, 7);

    private ResourceLocation modelLocation = TrafficSignData.EMPTY_MODEL;
    private TextureIdentifier textureKey;
    private TextureHandle texture;

    public TrafficSignPostAttachment(PostAttachmentRegistry.PostAttachmentContext<?> context) {
        super(context);
    }

    @Override
    public PostAttachmentRegistry.PostAttachmentRegistryObject<TrafficSignPostAttachment> getRegistryType() {
        return ModRegistries.TRAFFIC_SIGN_POST_ATTACHMENT;
    }

    @Override
    public Mesh getModel(BlockState state, RandomSource random, ModelContext context) {
        if (modelLocation.equals(TrafficSignData.EMPTY_MODEL)) {
            return new BasicMesh();
        }
        return BasicMesh.fromLocation(modelLocation, random);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos) {
        return SHAPE;
    }

    @Override
    public void renderAdditional(BERGraphics<?> graphics, float partialTick) {
        TextureHandle tex = getClientTexture();

        if (tex == null || tex.isClosed()) {
            return;
        }

        tex.getTextureData(TrafficSignData.class).ifPresent(data -> {
            double p = 1 / 16f;
            double z = 1.5d * p;//this.shape == TrafficSignShape.MISC ? p : 1.5d * p;
            graphics.poseStack().pushPose();
            graphics.poseStack().scale(16, 16, 16);
            graphics.poseStack().translate(0.5f, 0.5f, 0.5f);
            graphics.poseStack().translate(-0.5d, -0.5d, z + 0.002d);
            RenderUtils.renderTexture(tex.getLocation(), graphics, new Vector3f(0), 1, 1, 0, 0, 1, 1, getDirection(), DLColor.WHITE, graphics.packedLight(), true);
            graphics.poseStack().popPose();

            if (data.requiresBackground()) {
                tex.getExtension(TrafficSignData.TrafficSignExtension.class).flatMap(TrafficSignData.TrafficSignExtension::getBackLocation).ifPresent(bg -> {
                    double bz = 9.0d * p - 0.5d;
                    graphics.poseStack().pushPose();
                    graphics.poseStack().scale(16, 16, 16);
                    graphics.poseStack().translate(0.5f, 0.5f, 0.5f);
                    graphics.poseStack().mulPose(Axis.YP.rotationDegrees(180));
                    graphics.poseStack().translate(-0.5d, -0.5d, -(p * 2) + bz - 0.002d);
                    RenderUtils.renderTexture(bg, graphics, new Vector3f(0), 1, 1, 0, 0, 1, 1, getDirection().getOpposite(), DLColor.WHITE, graphics.packedLight(), true);
                    graphics.poseStack().popPose();
                });
            }
        });
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack stack = player.getInventory().getSelected();
        Item item = stack.getItem();

        if (item instanceof PatternCatalogueItem && ((item instanceof CreativePatternCatalogueItem && CreativePatternCatalogueItem.shouldUseCustomPattern(stack)) || PatternCatalogueItem.getSelectedPattern(stack) != null)) {
            if (item instanceof CreativePatternCatalogueItem && CreativePatternCatalogueItem.shouldUseCustomPattern(stack)) {
                setAndResetTexture(CreativePatternCatalogueItem.getCustomImage(stack));
            } else {
                setAndResetTexture(PatternCatalogueItem.getSelectedPattern(stack));
            }

            if (level.isClientSide()) {
                level.playSound(player, pos, SoundEvents.SLIME_BLOCK_PLACE, SoundSource.BLOCKS, 0.3F, 1.5f);
                ClientTextureCache.INSTANCE.getTextureDataAsync(textureKey, data -> {
                    data.ifPresent(d -> {
                        ITextureData.ifType(TrafficSignData.class, d).ifPresent(signData -> {
                            setModelLocation(signData.modelLocation());
                            modelLocation = signData.modelLocation();
                            ModNetworkManager.UPDATE_SIGN_SAPE.send(NetworkDirection.toServer(), new UpdateTrafficSignShapePacket(new AttachmentIdentifier(getBlockEntity().getBlockPos(), getDirection(), getRegistryType().id()), signData.modelLocation()));
                        });
                    });
                });
            }
            return InteractionResult.SUCCESS;
        }

        return super.use(state, level, pos, player, hand, hit);
    }

    @Override
    public void onRemoved(Level level, BlockPos pos) {
        resetTexture();
    }

    public TextureIdentifier getTextureKey() {
        return textureKey;
    }

    public TextureHandle getClientTexture() {
        if (textureKey == null) {
            return TextureHandle.EMPTY;
        }
        if (texture == null) {
            texture = ClientTextureCache.INSTANCE.getTexture(getTextureKey(), IDecoderContext.EMPTY);
        }
        return texture;
    }

    public void resetTexture() {
        if (Objects.requireNonNull(getBlockEntity().getLevel()).isClientSide()) {
            ClientTextureCache.INSTANCE.release(textureKey);
            texture = null;
        }
    }

    public void setAndResetTexture(NamedTextureKey texture) {
        setTextureKey(texture.textureKey());
        if (!Objects.requireNonNull(getBlockEntity().getLevel()).isClientSide()) {
            for (ServerPlayer player : getBlockEntity().getLevel().players().stream().filter(p -> p instanceof ServerPlayer).toArray(ServerPlayer[]::new)) {
                ModNetworkManager.RESET_TRAFFIC_SIGN_TEXTURE.send(NetworkDirection.toPlayer(player), new TrafficSignTextureResetPacket(getBlockEntity().getBlockPos()));
            }
        }
    }

    public void setTextureKey(TextureIdentifier key) {
        this.textureKey = key;
        Utils.doIfType(getBlockEntity(), PostBlockEntity.class, DLSyncedBlockEntity::notifyUpdate);
    }

    @Override
    protected CompoundTag saveAdditional() {
        CompoundTag nbt = super.saveAdditional();
        nbt.putString(NBT_SHAPE, modelLocation.toString());
        if (textureKey != null) {
            nbt.put(NBT_TEXTURE, textureKey.toNbt());
        }
        return nbt;
    }

    @Override
    protected void loadAdditional(CompoundTag nbt) {
        this.modelLocation = ResourceLocation.tryParse(nbt.getString(NBT_SHAPE));
        setTextureKey(TextureIdentifier.fromNbt(nbt.getCompound(NBT_TEXTURE)));
        updateModel();
        super.loadAdditional(nbt);
    }

    public void setModelLocation(ResourceLocation modelLocation) {
        this.modelLocation = modelLocation;
        Utils.doIfType(getBlockEntity(), PostBlockEntity.class, DLSyncedBlockEntity::notifyUpdate);
        updateModel();
    }
}
