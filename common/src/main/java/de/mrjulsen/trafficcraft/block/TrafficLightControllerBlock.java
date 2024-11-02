package de.mrjulsen.trafficcraft.block;

import com.mojang.serialization.MapCodec;

import de.mrjulsen.trafficcraft.block.data.TrafficLightTrigger;
import de.mrjulsen.trafficcraft.block.entity.TrafficLightControllerBlockEntity;
import de.mrjulsen.trafficcraft.client.ClientWrapper;
import de.mrjulsen.trafficcraft.item.ILinkerItem;
import de.mrjulsen.trafficcraft.item.WrenchItem;
import de.mrjulsen.trafficcraft.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.block.state.StateDefinition.Builder;

public class TrafficLightControllerBlock extends BaseEntityBlock {
        
    public static final MapCodec<TrafficLightControllerBlock> CODEC = simpleCodec(TrafficLightControllerBlock::new);

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }
    
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    public TrafficLightControllerBlock(BlockBehaviour.Properties properties) {
        super(properties
            .mapColor(MapColor.METAL)
            .strength(1.5f)
            .requiresCorrectToolForDrops()
            .sound(SoundType.METAL)
        );

        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    public BlockState rotate(BlockState pState, Rotation pRotation) {
        return pState.setValue(FACING, pRotation.rotate(pState.getValue(FACING)));
    }    

    @Override
    public BlockState mirror(BlockState pState, Mirror pMirror) {
        return pState.rotate(pMirror.getRotation(pState.getValue(FACING)));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        return this.defaultBlockState().setValue(FACING, pContext.getHorizontalDirection().getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> pBuilder) {
        super.createBlockStateDefinition(pBuilder);
        pBuilder.add(FACING);
    }

    public void stepOn(Level pLevel, BlockPos pPos, BlockState pState, Entity pEntity) {
        if (!pLevel.dimensionType().ultraWarm()) {
            return;
        } 

        if (!pEntity.isSteppingCarefully() && pEntity instanceof LivingEntity) {
            pEntity.hurt(pLevel.damageSources().hotFloor(), 1.0F);
        }

        super.stepOn(pLevel, pPos, pState, pEntity);
    }

    @Override
    public void animateTick(BlockState pState, Level pLevel, BlockPos pPos, RandomSource pRandom) {
        if (pLevel.dimensionType().ultraWarm() && pLevel.getBlockEntity(pPos) instanceof TrafficLightControllerBlockEntity blockEntity && blockEntity.isRunning()) {
            pLevel.addParticle(ParticleTypes.SMOKE, pPos.getX() + 0.5D, pPos.getY() + 0.5D, pPos.getZ() + 0.5D, pRandom.triangle(-0.05D, 0.05D), pRandom.triangle(-0.05D, 0.05D), pRandom.triangle(-0.05D, 0.05D));
        }

        super.animateTick(pState, pLevel, pPos, pRandom);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        Item item = stack.getItem();
        if (item != null && (item instanceof WrenchItem && !(item instanceof ILinkerItem && ((ILinkerItem)item).isSourceBlockAccepted(this)))) {
            if(level.isClientSide) {
                if (!player.isShiftKeyDown())
                    ClientWrapper.showTrafficLightControllerScreen(pos, level);
            }

            return ItemInteractionResult.SUCCESS;
        }

        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    public void neighborChanged(BlockState pState, Level pLevel, BlockPos pPos, Block pBlock, BlockPos pFromPos, boolean pIsMoving) {
        if (pLevel.getBlockEntity(pPos) instanceof TrafficLightControllerBlockEntity blockEntity) {
            if (pLevel.hasNeighborSignal(pPos)) {
                if (blockEntity.getFirstOrMainSchedule().getTrigger() == TrafficLightTrigger.REDSTONE && !blockEntity.isPowered()) {
                    blockEntity.setPowered(true);
                    blockEntity.startSchedule(true);
                }
            } else {
                blockEntity.setPowered(false);
                if (blockEntity.getFirstOrMainSchedule().getTrigger() == TrafficLightTrigger.REDSTONE) {
                    blockEntity.stopSchedule();
                }
            }                    
        }
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState pState) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState pState, Level pLevel, BlockPos pPos) {
        return pLevel.getBlockEntity(pPos) instanceof TrafficLightControllerBlockEntity blockEntity && blockEntity.isRunning() ? 15 : 0;
    }

    /* BLOCK ENTITY */
    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new TrafficLightControllerBlockEntity(pPos, pState);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
        return createTickerHelper(pBlockEntityType, ModBlockEntities.TRAFFIC_LIGHT_CONTROLLER_BLOCK_ENTITY.get(), TrafficLightControllerBlockEntity::tick);
    }
}
