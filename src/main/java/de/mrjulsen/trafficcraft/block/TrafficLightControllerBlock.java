package de.mrjulsen.trafficcraft.block;

import javax.annotation.Nullable;

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
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
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
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.block.state.StateDefinition.Builder;

public class TrafficLightControllerBlock extends BaseEntityBlock {
    
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    public TrafficLightControllerBlock() {
        super(BlockBehaviour.Properties.of(Material.METAL)
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
    @SuppressWarnings("deprecation")
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

        if (!pEntity.fireImmune() && pEntity instanceof LivingEntity && !EnchantmentHelper.hasFrostWalker((LivingEntity)pEntity) && pLevel.getBlockEntity(pPos) instanceof TrafficLightControllerBlockEntity blockEntity && blockEntity.isRunning()) {
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
    @SuppressWarnings("deprecation")
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {

        Item item = pPlayer.getInventory().getSelected().getItem();
        if (item != null && (item instanceof WrenchItem && !(item instanceof ILinkerItem && ((ILinkerItem)item).isSourceBlockAccepted(this)))) {
            if(pLevel.isClientSide) {
                if (!pPlayer.isShiftKeyDown())
                    ClientWrapper.showTrafficLightControllerScreen(pPos, pLevel);
            }

            return InteractionResult.SUCCESS;
        }

        return super.use(pState, pLevel, pPos, pPlayer, pHand, pHit);
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

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
        return createTickerHelper(pBlockEntityType, ModBlockEntities.TRAFFIC_LIGHT_CONTROLLER_BLOCK_ENTITY.get(), TrafficLightControllerBlockEntity::tick);
    }
}
