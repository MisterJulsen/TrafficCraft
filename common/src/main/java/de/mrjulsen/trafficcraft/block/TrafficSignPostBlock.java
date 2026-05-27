package de.mrjulsen.trafficcraft.block;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import de.mrjulsen.mcdragonlib.util.MapCache;
import de.mrjulsen.mcdragonlib.util.Pair;
import de.mrjulsen.trafficcraft.block.data.ITrafficPostLike;
import de.mrjulsen.trafficcraft.block.data.attachments.*;
import de.mrjulsen.trafficcraft.block.entity.PostBlockEntity;
import de.mrjulsen.trafficcraft.registry.ModBlockEntities;
import de.mrjulsen.trafficcraft.registry.ModBlockTags;
import de.mrjulsen.trafficcraft.registry.builtin.PostAttachmentRegistry;
import de.mrjulsen.trafficcraft.util.VoxelShapeRotator;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;

public class TrafficSignPostBlock extends BaseEntityBlock implements SimpleWaterloggedBlock, ITrafficPostLike {

    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.AXIS;

    public static final BooleanProperty NORTH = PipeBlock.NORTH;
    public static final BooleanProperty EAST = PipeBlock.EAST;
    public static final BooleanProperty SOUTH = PipeBlock.SOUTH;
    public static final BooleanProperty WEST = PipeBlock.WEST;
    public static final BooleanProperty UP = PipeBlock.UP;
    public static final BooleanProperty DOWN = PipeBlock.DOWN;
    public static final BooleanProperty EXTEND_BOTTOM = BooleanProperty.create("bottom_extension");

    protected static final Map<Direction, BooleanProperty> PROPERTY_BY_DIRECTION = PipeBlock.PROPERTY_BY_DIRECTION.entrySet().stream().collect(Util.toMap());

    public static final VoxelShape SHAPE_BASE = Block.box(7, 7, 7, 9, 9, 9);
    public static final VoxelShape SHAPE_NORTH = Block.box(7, 7, 0, 9, 9, 7);
    public static final VoxelShape SHAPE_EAST = Block.box(9, 7, 7, 16, 9, 9);
    public static final VoxelShape SHAPE_SOUTH = Block.box(7, 7, 9, 9, 9, 16);
    public static final VoxelShape SHAPE_WEST = Block.box(0, 7, 7, 7, 9, 9);
    public static final VoxelShape SHAPE_UP = Block.box(7, 9, 7, 9, 16, 9);
    public static final VoxelShape SHAPE_DOWN = Block.box(7, 0, 7, 9, 7, 9);
    private static final VoxelShape SHAPE_EXTEND_DOWN = Block.box(7, -16, 7, 9, 0, 9);

    private static final MapCache<VoxelShape, BlockState, BlockState> SHAPES = new MapCache<>(state -> {
        VoxelShape shape = SHAPE_BASE;
        Axis axis = state.getValue(AXIS);

        boolean hasNorth = state.getValue(NORTH);
        boolean hasEast = state.getValue(EAST);
        boolean hasSouth = state.getValue(SOUTH);
        boolean hasWest = state.getValue(WEST);
        boolean hasUp = state.getValue(UP);
        boolean hasDown = state.getValue(DOWN);
        if (axis == Axis.X && !hasNorth && !hasSouth && !hasUp && !hasDown) {
            shape = Shapes.or(shape, SHAPE_EAST, SHAPE_WEST);
        } else if (axis == Axis.Z && !hasEast && !hasWest && !hasUp && !hasDown) {
            shape = Shapes.or(shape, SHAPE_NORTH, SHAPE_SOUTH);
        } else if (axis == Axis.Y && !hasEast && !hasWest && !hasNorth && !hasSouth) {
            shape = Shapes.or(shape, SHAPE_UP, SHAPE_DOWN);
        } else {
            if (hasNorth) shape = Shapes.or(shape, SHAPE_NORTH);
            if (hasEast) shape = Shapes.or(shape, SHAPE_EAST);
            if (hasSouth) shape = Shapes.or(shape, SHAPE_SOUTH);
            if (hasWest) shape = Shapes.or(shape, SHAPE_WEST);
            if (hasUp) shape = Shapes.or(shape, SHAPE_UP);
            if (hasDown) shape = Shapes.or(shape, SHAPE_DOWN);
        }

        if (state.getValue(EXTEND_BOTTOM)) {
            shape = Shapes.or(shape, SHAPE_EXTEND_DOWN);
        }

        return shape;
    }, BlockState::hashCode);


    private record AttachmentShapeKey(Class<?> clazz, VoxelShape rawShape, Quaternionf rotation) {}
    private final Map<AttachmentShapeKey, VoxelShape> transformedAttachmentShapes = new ConcurrentHashMap<>();


    public TrafficSignPostBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(1.0f)
                .requiresCorrectToolForDrops()
                .sound(SoundType.LANTERN)
        );

        this.registerDefaultState(this.stateDefinition.any()
                .setValue(WATERLOGGED, false)
                .setValue(AXIS, Axis.Y)
                .setValue(NORTH, false)
                .setValue(SOUTH, false)
                .setValue(WEST, false)
                .setValue(EAST, false)
                .setValue(UP, false)
                .setValue(DOWN, false)
                .setValue(EXTEND_BOTTOM, false)
        );
    }


    private VoxelShape getAttachmentShape(BlockState state, BlockGetter level, BlockPos pos, PostBlockEntity blockEntity, IPostAttachment<?> attachment) {
        final Quaternionf rot = blockEntity.getAttachmentRotation(attachment, state.getValue(AXIS));
        final VoxelShape shape = attachment.getShape(state, level, pos);
        final AttachmentShapeKey key = new AttachmentShapeKey(attachment.getClass(), shape, rot);
        return transformedAttachmentShapes.computeIfAbsent(key, k -> VoxelShapeRotator.rotateByQuaternion(shape, rot).optimize());
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        VoxelShape shape = SHAPES.get(pState, pState);
        if (pLevel.getBlockEntity(pPos) instanceof PostBlockEntity be) {
            for (IPostAttachment<?> attachment : be.getAttachments().values()) {
                shape = Shapes.or(shape, getAttachmentShape(pState, pLevel, pPos, be, attachment));
            }
        }
        return shape;
    }

    @Override
    public boolean isPathfindable(BlockState pState, BlockGetter pLevel, BlockPos pPos, PathComputationType pType) {
        return false;
    }

    @Override
    public BlockState rotate(BlockState pState, Rotation pRot) {
        return switch (pRot) {
            case COUNTERCLOCKWISE_90, CLOCKWISE_90 -> switch (pState.getValue(AXIS)) {
                case X -> pState.setValue(AXIS, Axis.Z);
                case Z -> pState.setValue(AXIS, Axis.X);
                default -> pState;
            };
            default -> pState;
        };
    }

    private static boolean needsBottomExtension(BlockState pState, BlockState belowBlock) {
        if (pState.getValue(AXIS) != Axis.Y) return false;

        return belowBlock.hasProperty(BlockStateProperties.LAYERS)
                || belowBlock.is(ModBlockTags.POST_EXTENSION)
                || (belowBlock.getBlock() instanceof SlabBlock
                && belowBlock.getValue(BlockStateProperties.SLAB_TYPE) == SlabType.BOTTOM);
    }

    @Override
    public BlockState updateShape(BlockState pState, Direction pFacing, BlockState pFacingState, LevelAccessor pLevel, BlockPos pCurrentPos, BlockPos pFacingPos) {
        if (pState.getValue(WATERLOGGED)) {
            pLevel.scheduleTick(pCurrentPos, Fluids.WATER, Fluids.WATER.getTickDelay(pLevel));
        }

        BlockState belowBlock = pLevel.getBlockState(pCurrentPos.below());

        return pState
                .setValue(PROPERTY_BY_DIRECTION.get(pFacing),
                        connectsTo(pLevel, pCurrentPos, pState, pFacingState,
                                pFacingState.isFaceSturdy(pLevel, pFacingPos, pFacing.getOpposite()),
                                pFacing))
                .setValue(EXTEND_BOTTOM, needsBottomExtension(pState, belowBlock));
    }

    @Override
    @SuppressWarnings("deprecation")
    public FluidState getFluidState(BlockState pState) {
        return pState.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(pState);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        FluidState fluidstate = pContext.getLevel().getFluidState(pContext.getClickedPos());
        BlockGetter world = pContext.getLevel();
        BlockPos pos = pContext.getClickedPos();

        BlockState newState = this.defaultBlockState()
                .setValue(WATERLOGGED, fluidstate.getType() == Fluids.WATER)
                .setValue(AXIS, pContext.getClickedFace().getAxis());

        for (Direction dir : Direction.values()) {
            BlockPos neighborPos = pos.relative(dir);
            BlockState neighborState = world.getBlockState(neighborPos);
            boolean isSturdy = neighborState.isFaceSturdy(world, neighborPos, dir.getOpposite());

            newState = newState.setValue(PROPERTY_BY_DIRECTION.get(dir), connectsTo(pContext.getLevel(), pos, newState, neighborState, isSturdy, dir));
        }

        return newState.setValue(EXTEND_BOTTOM, needsBottomExtension(newState, world.getBlockState(pos.below())));
    }
    public boolean connectsTo(LevelAccessor level, BlockPos pos, BlockState pState, BlockState pTargetState, boolean pIsSideSolid, Direction pDirection) {
        if (pTargetState.getBlock() instanceof ITrafficPostLike postLike) {
            return canConnect(pState, pDirection) && postLike.canConnect(pTargetState, pDirection.getOpposite());
        }
        return pState.getValue(AXIS).test(pDirection) && pIsSideSolid;
    }

    @Override
    public boolean canConnect(BlockState pState, Direction pDirection) {
        return true;
    }

    @Override
    public boolean canAttach(BlockState pState, BlockPos pPos, Direction pDirection) {
        if (pDirection.getAxis() == Axis.Y) {
            return false;
        }

        boolean isVerticalFree =
                pState.getValue(AXIS) == Axis.Y
                        && !pState.getValue(EAST)
                        && !pState.getValue(WEST)
                        && !pState.getValue(NORTH)
                        && !pState.getValue(SOUTH);

        return isVerticalFree || pState.getValue(UP);
    }

    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> pBuilder) {
        super.createBlockStateDefinition(pBuilder);
        pBuilder.add(WATERLOGGED, AXIS, NORTH, SOUTH, WEST, EAST, UP, DOWN, EXTEND_BOTTOM);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PostBlockEntity(ModBlockEntities.POST.get(), pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        Vec3 localPos = hit.getLocation().subtract(pos.getX(), pos.getY(), pos.getZ());

        if (level.getBlockEntity(pos) instanceof PostBlockEntity be) {
            for (Map.Entry<Direction, IPostAttachment<?>> entry : be.getAttachments().entrySet()) {
                IPostAttachment<?> attachment = entry.getValue();
                VoxelShape shape = getAttachmentShape(state, level, pos, be, attachment);

                boolean isHit = shape.toAabbs().stream()
                        .map(bb -> bb.inflate(0.002))
                        .anyMatch(bb -> bb.contains(localPos));

                if (isHit) {
                    return attachment.use(state, level, pos, player, hand, hit);
                }
            }

            if (player.getItemInHand(hand).getItem() instanceof BlockItem bi && bi.getBlock() instanceof TrafficLightBlock t) {
                be.getAttachments().put(hit.getDirection(), new TrafficLightPostAttachment(new PostAttachmentRegistry.PostAttachmentContext<>(be, hit.getDirection())));
                level.setBlock(pos, state, Block.UPDATE_ALL);
                level.sendBlockUpdated(pos, state, state, Block.UPDATE_ALL);
                return InteractionResult.SUCCESS;
            } else if (player.getItemInHand(hand).getItem() instanceof BlockItem bi && bi.getBlock() instanceof TrafficSignBlock t) {
                be.getAttachments().put(hit.getDirection(), new TrafficSignPostAttachment(new PostAttachmentRegistry.PostAttachmentContext<>(be, hit.getDirection())));
                level.setBlock(pos, state, Block.UPDATE_ALL);
                level.sendBlockUpdated(pos, state, state, Block.UPDATE_ALL);
                return InteractionResult.SUCCESS;
            }
        }
        return super.use(state, level, pos, player, hand, hit);
    }
}