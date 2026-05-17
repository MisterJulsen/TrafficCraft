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
import de.mrjulsen.trafficcraft.block.TrafficSignBlock;
import de.mrjulsen.trafficcraft.block.data.TrafficSignShape;
import de.mrjulsen.trafficcraft.block.entity.PostBlockEntity;
import de.mrjulsen.trafficcraft.block.entity.TrafficSignBlockEntity;
import de.mrjulsen.trafficcraft.data.NamedTrafficSignTextureReference;
import de.mrjulsen.trafficcraft.data.TrafficSignClientTexture;
import de.mrjulsen.trafficcraft.data.TrafficSignTextureData;
import de.mrjulsen.trafficcraft.data.TrafficSignTextureManager;
import de.mrjulsen.trafficcraft.item.CreativePatternCatalogueItem;
import de.mrjulsen.trafficcraft.item.PatternCatalogueItem;
import de.mrjulsen.trafficcraft.network.packets.stc.TrafficSignTextureResetPacket;
import de.mrjulsen.trafficcraft.registry.ModNetworkManager;
import de.mrjulsen.trafficcraft.registry.ModRegistries;
import de.mrjulsen.trafficcraft.registry.builtin.PostAttachmentRegistry;
import de.mrjulsen.trafficcraft.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
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

    private TrafficSignShape shape = TrafficSignShape.CIRCLE;
    private String textureId;
    private TrafficSignClientTexture texture;

    public TrafficSignPostAttachment(PostAttachmentRegistry.PostAttachmentContext<?> context) {
        super(context);
    }

    @Override
    public PostAttachmentRegistry.PostAttachmentRegistryObject<TrafficSignPostAttachment> getRegistryType() {
        return ModRegistries.TRAFFIC_SIGN_POST_ATTACHMENT;
    }

    @Override
    public Mesh getModel(BlockState state, RandomSource random, ModelContext context) {
        return BasicMesh.fromLocation(DLUtils.resourceLocation(TrafficCraft.MOD_ID, String.format("block/sign/%s", shape.getSerializedName())), random);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos) {
        return SHAPE;
    }

    @Override
    public void renderAdditional(BERGraphics<?> graphics, float partialTick) {
        TrafficSignClientTexture tex = getClientTexture();

        if (tex.isDisposed()) {
            return;
        }

        double p = 1 / 16f;
        double z = this.shape == TrafficSignShape.MISC ? p : 1.5d * p;
        graphics.poseStack().pushPose();
        graphics.poseStack().scale(16, 16, 16);
        graphics.poseStack().translate(0.5f, 0.5f, 0.5f);
        graphics.poseStack().translate(-0.5d, -0.5d, z + 0.002d);

        RenderUtils.renderTexture(tex.getTextureLocation(), graphics, new Vector3f(0), 1, 1, 0, 0, 1, 1, getDirection(), DLColor.WHITE, graphics.packedLight(), true);

        graphics.poseStack().popPose();

        if (tex.hasBackground()) {
            z = 9.0d * p - 0.5d;
            graphics.poseStack().pushPose();
            graphics.poseStack().scale(16, 16, 16);
            graphics.poseStack().translate(0.5f, 0.5f, 0.5f);
            graphics.poseStack().mulPose(Axis.YP.rotationDegrees(180));
            graphics.poseStack().translate(-0.5d, -0.5d, -(p * 2) + z - 0.002d);

            RenderUtils.renderTexture(tex.getBackgroundTextureLocation(), graphics, new Vector3f(0), 1, 1, 0, 0, 1, 1, getDirection().getOpposite(), DLColor.WHITE, graphics.packedLight(), true);

            graphics.poseStack().popPose();
        }
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
            }
            TrafficSignTextureData data = TrafficSignTextureManager.load(item instanceof CreativePatternCatalogueItem && CreativePatternCatalogueItem.shouldUseCustomPattern(stack) ? CreativePatternCatalogueItem.getCustomImage(stack).getTextureId() : PatternCatalogueItem.getSelectedPattern(stack).getTextureId());
            this.shape = data.getShape();

            Utils.doIfType(getBlockEntity(), PostBlockEntity.class, DLSyncedBlockEntity::notifyUpdate);
            updateModel();

            return InteractionResult.SUCCESS;
        }

        return super.use(state, level, pos, player, hand, hit);
    }

    @Override
    public void onRemoved(Level level, BlockPos pos) {
        resetTexture();
    }

    public String getTextureId() {
        return textureId;
    }

    public TrafficSignClientTexture getClientTexture() {
        if (texture == null) {
            if (getTextureId() == null || getTextureId().isBlank() || getTextureId().equals("empty")) {
                return TrafficSignClientTexture.EMPTY;
            }
            texture = TrafficSignClientTexture.load(getTextureId(), true, null);
        }
        return texture;
    }

    public void resetTexture() {
        if (Objects.requireNonNull(getBlockEntity().getLevel()).isClientSide()) {
            TrafficSignClientTexture oldTexture = texture;
            texture = null;
            DLUtils.doIfNotNull(oldTexture, TrafficSignClientTexture::close);
        }
    }

    public void setAndResetTexture(NamedTrafficSignTextureReference texture) {
        setTextureId(texture.getTextureId());
        if (!Objects.requireNonNull(getBlockEntity().getLevel()).isClientSide()) {
            for (ServerPlayer player : getBlockEntity().getLevel().players().stream().filter(p -> p instanceof ServerPlayer).toArray(ServerPlayer[]::new)) {
                ModNetworkManager.RESET_TRAFFIC_SIGN_TEXTURE.send(NetworkDirection.toPlayer(player), new TrafficSignTextureResetPacket(getBlockEntity().getBlockPos()));
            }
        }
    }

    public void setTextureId(String id) {
        this.textureId = id;
        Utils.doIfType(getBlockEntity(), PostBlockEntity.class, DLSyncedBlockEntity::notifyUpdate);
    }

    @Override
    protected CompoundTag saveAdditional() {
        CompoundTag nbt = super.saveAdditional();
        nbt.putInt(NBT_SHAPE, shape.ordinal());
        if (textureId != null) {
            nbt.putString(NBT_TEXTURE, getTextureId());
        }
        return nbt;
    }

    @Override
    protected void loadAdditional(CompoundTag nbt) {
        this.shape = TrafficSignShape.getShapeByIndex(nbt.getInt(NBT_SHAPE));
        setTextureId(nbt.getString(NBT_TEXTURE));
        updateModel();
        super.loadAdditional(nbt);
    }
}
