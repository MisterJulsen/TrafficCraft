package de.mrjulsen.trafficcraft.block;

import javax.annotation.Nullable;

import de.mrjulsen.trafficcraft.block.data.IPaintableBlock;
import de.mrjulsen.trafficcraft.block.data.ITrafficPostLike;
import de.mrjulsen.trafficcraft.block.entity.StreetSignBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class StreetSignBlock extends WritableTrafficSign implements ITrafficPostLike, IPaintableBlock {

   public static final VoxelShape SHAPE_NORTH = Block.box(7.5, 10.5, 6, 8.5, 14.5, 23);
   public static final VoxelShape SHAPE_SOUTH = Block.box(7.5, 10.5, -7, 8.5, 14.5, 10);
   public static final VoxelShape SHAPE_WEST = Block.box(6, 10.5, 7.5, 23, 14.5, 8.5);
   public static final VoxelShape SHAPE_EAST = Block.box(-7, 10.5, 7.5, 10, 14.5, 8.5);

   public StreetSignBlock() {
      super(BlockBehaviour.Properties.of(Material.BAMBOO)
            .strength(0.2f)
            .sound(SoundType.BAMBOO));
   }

   @Override
   public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
      switch (pState.getValue(FACING)) {
         default:
         case NORTH:
            return SHAPE_NORTH;
         case EAST:
            return SHAPE_EAST;
         case SOUTH:
            return SHAPE_SOUTH;
         case WEST:
            return SHAPE_WEST;
      }
   }

   @Override
   public VoxelShape getCollisionShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
      switch (pState.getValue(FACING)) {
         default:
         case NORTH:
            return SHAPE_NORTH;
         case EAST:
            return SHAPE_EAST;
         case SOUTH:
            return SHAPE_SOUTH;
         case WEST:
            return SHAPE_WEST;
      }
   }

   @Override
   public void attack(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer) {
      onRemoveColor(pState, pLevel, pPos, pPlayer);
   }

   @Override
   public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
      return new StreetSignBlockEntity(pPos, pState);
   }

   @Override
   public boolean canSurvive(BlockState pState, LevelReader pLevel, BlockPos pPos) {
      Direction direction = pState.getValue(FACING);
      return this.canAttachTo(pLevel, pPos.relative(direction.getOpposite()), direction);
   }

   private boolean canAttachTo(BlockGetter pBlockReader, BlockPos pPos, Direction pDirection) {
      BlockState blockstate = pBlockReader.getBlockState(pPos);

      if (blockstate.getBlock() instanceof ITrafficPostLike postLike) {
         return postLike.canAttach(blockstate, pPos, pDirection);
      }

      return false;
   }

   public BlockState updateShape(BlockState pState, Direction pFacing, BlockState pFacingState, LevelAccessor pLevel, BlockPos pCurrentPos, BlockPos pFacingPos) {
      if (pFacing.getOpposite() == pState.getValue(FACING) && !pState.canSurvive(pLevel, pCurrentPos)) {
         return Blocks.AIR.defaultBlockState();
      } else {
         if (pState.getValue(WATERLOGGED)) {
            pLevel.scheduleTick(pCurrentPos, Fluids.WATER, Fluids.WATER.getTickDelay(pLevel));
         }

         return super.updateShape(pState, pFacing, pFacingState, pLevel, pCurrentPos, pFacingPos);
      }
   }

   @Nullable
   public BlockState getStateForPlacement(BlockPlaceContext pContext) {
      if (!pContext.replacingClickedOnBlock()) {
         BlockState blockstate = pContext.getLevel().getBlockState(pContext.getClickedPos().relative(pContext.getClickedFace().getOpposite()));
         if (blockstate.is(this) && blockstate.getValue(FACING) == pContext.getClickedFace()) {
            return null;
         }
      }

      BlockState blockstate1 = this.defaultBlockState();
      LevelReader levelreader = pContext.getLevel();
      BlockPos blockpos = pContext.getClickedPos();
      FluidState fluidstate = pContext.getLevel().getFluidState(pContext.getClickedPos());

      for (Direction direction : pContext.getNearestLookingDirections()) {
         if (direction.getAxis().isHorizontal()) {
            blockstate1 = blockstate1.setValue(FACING, direction.getOpposite());
            if (blockstate1.canSurvive(levelreader, blockpos)) {
               return blockstate1.setValue(WATERLOGGED, Boolean.valueOf(fluidstate.getType() == Fluids.WATER));
            }
         }
      }

      return null;
   }

   @Override
   public boolean canAttach(BlockState pState, BlockPos pPos, Direction pDirection) {
      return false;
   }

   @Override
   public boolean canConnect(BlockState pState, Direction pDirection) {
      return false;
   }
}
