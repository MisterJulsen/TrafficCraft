package de.mrjulsen.trafficcraft.block.data.attachments;

import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.client.ber.BERGraphics;
import de.mrjulsen.mcdragonlib.client.model.ModelContext;
import de.mrjulsen.mcdragonlib.client.model.mesh.BasicMesh;
import de.mrjulsen.mcdragonlib.client.model.mesh.CornerType;
import de.mrjulsen.mcdragonlib.client.model.mesh.Face;
import de.mrjulsen.mcdragonlib.client.model.mesh.Mesh;
import de.mrjulsen.mcdragonlib.util.DLColor;
import de.mrjulsen.mcdragonlib.util.DLUtils;
import de.mrjulsen.mcdragonlib.util.math.MathUtils;
import de.mrjulsen.trafficcraft.TrafficCraft;
import de.mrjulsen.trafficcraft.block.data.TrafficLightColor;
import de.mrjulsen.trafficcraft.block.data.TrafficLightIcon;
import de.mrjulsen.trafficcraft.block.data.TrafficLightModel;
import de.mrjulsen.trafficcraft.block.data.TrafficLightType;
import de.mrjulsen.trafficcraft.client.TrafficLightTextureManager;
import de.mrjulsen.trafficcraft.registry.ModRegistries;
import de.mrjulsen.trafficcraft.registry.builtin.PostAttachmentRegistry;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
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
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Vector3f;
import org.lwjgl.system.MathUtil;

public class TrafficLightPostAttachment extends AbstractPostAttachment<TrafficLightPostAttachment> {

    private static final String NBT_MODEL = "Model";

    private static final VoxelShape SHAPE = Block.box(4, 0, 1, 12, 16, 6);

    private TrafficLightModel model = TrafficLightModel.THREE_LIGHTS;

    public TrafficLightPostAttachment(PostAttachmentRegistry.PostAttachmentContext<?> context) {
        super(context);
    }

    @Override
    public PostAttachmentRegistry.PostAttachmentRegistryObject<TrafficLightPostAttachment> getRegistryType() {
        return ModRegistries.TRAFFIC_LIGHT_POST_ATTACHMENT;
    }

    @Override
    public Mesh getModel(BlockState state, RandomSource random, ModelContext context) {
        return BasicMesh.fromLocation(DLUtils.resourceLocation(TrafficCraft.MOD_ID, String.format("block/traffic_light/%s", model.getName())), random);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos) {
        return SHAPE;
    }

    @Override
    public void renderAdditional(BERGraphics<?> graphics, float partialTick) {
        graphics.poseStack().pushPose();
        graphics.poseStack().translate(6f, 5.5f, 13);

        DLColor[] colors = new  DLColor[]{DLColor.RED, DLColor.YELLOW, DLColor.GREEN};
        for (int i = 0; i < 3; i++) {
            new TrafficLightTextureManager.TrafficLightTextureKey(TrafficLightIcon.NONE, TrafficLightColor.getAllowedForType(TrafficLightType.CAR, false)[i]).render(graphics, graphics.blockEntity(), graphics.packedLight());
            float pixel = DragonLib.BLOCK_PIXEL;
            BasicMesh cube = new BasicMesh();
            Face frontFace = Face.createFace(Direction.SOUTH, new Vector3f(0, 0, pixel * 1), pixel * 4, pixel * 4);
            frontFace.setTexture(DLUtils.resourceLocation(TrafficCraft.MOD_ID, "textures/block/traffic_light/light.png"));
            frontFace.setColor(colors[i]);
            cube.addFace(frontFace);

            Face rightSide = Face.createFace(Direction.EAST, new Vector3f(pixel * 4, 0, 0), pixel * 1, pixel * 4);
            rightSide.getCorner(CornerType.TOP_RIGHT).setU(DragonLib.BLOCK_PIXEL);
            rightSide.getCorner(CornerType.BOTTOM_RIGHT).setU(DragonLib.BLOCK_PIXEL);
            rightSide.setTexture(DLUtils.resourceLocation(TrafficCraft.MOD_ID, "textures/block/traffic_light/light.png"));
            rightSide.setColor(colors[i]);
            cube.addFace(rightSide);

            Face leftSide = Face.createFace(Direction.WEST, new Vector3f(), pixel * 1, pixel * 4);
            leftSide.setTexture(DLUtils.resourceLocation(TrafficCraft.MOD_ID, "textures/block/traffic_light/light.png"));
            leftSide.setColor(colors[i]);
            leftSide.getCorner(CornerType.TOP_RIGHT).setU(DragonLib.BLOCK_PIXEL);
            leftSide.getCorner(CornerType.BOTTOM_RIGHT).setU(DragonLib.BLOCK_PIXEL);
            cube.addFace(leftSide);

            Face bottomFace = Face.createFace(Direction.DOWN, new Vector3f(), pixel * 4, pixel * 1);
            bottomFace.setTexture(DLUtils.resourceLocation(TrafficCraft.MOD_ID, "textures/block/traffic_light/light.png"));
            bottomFace.setColor(colors[i]);
            bottomFace.getCorner(CornerType.BOTTOM_LEFT).setV(DragonLib.BLOCK_PIXEL);
            bottomFace.getCorner(CornerType.BOTTOM_RIGHT).setV(DragonLib.BLOCK_PIXEL);
            cube.addFace(bottomFace);

            cube.cleanUp();
            cube.render(graphics, LightTexture.FULL_BRIGHT, false);
            graphics.poseStack().translate(0, 5, 0);
        }
        graphics.poseStack().popPose();
    }

    @Override
    protected CompoundTag saveAdditional() {
        CompoundTag nbt = super.saveAdditional();
        nbt.putByte(NBT_MODEL, model.getLightsCount());
        return nbt;
    }

    @Override
    protected void loadAdditional(CompoundTag nbt) {
        this.model = TrafficLightModel.getModelByLightsCount(nbt.getByte(NBT_MODEL));
        updateModel();
        super.loadAdditional(nbt);
    }
}
