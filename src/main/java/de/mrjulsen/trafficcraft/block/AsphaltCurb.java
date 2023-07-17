package de.mrjulsen.trafficcraft.block;

import de.mrjulsen.trafficcraft.block.properties.RoadType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.StairsShape;
import net.minecraft.world.level.material.Material;

public class AsphaltCurb extends Block {

    private RoadType defaultRoadType;
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final EnumProperty<StairsShape> SHAPE = BlockStateProperties.STAIRS_SHAPE;
    
    public AsphaltCurb(RoadType type) {
        super(Properties.of(Material.STONE)
            .strength(1.5f)
            .requiresCorrectToolForDrops()
        );

        this.registerDefaultState(this.stateDefinition.any()
            .setValue(FACING, Direction.NORTH)
        );
        
        this.defaultRoadType = type;
    }

    public RoadType getDefaultRoadType() {
        return this.defaultRoadType;
    }   
    
    @Override
    public BlockState rotate(BlockState pState, Rotation pRotation) {
        return pState.setValue(FACING, pRotation.rotate(pState.getValue(FACING)));
    }

    @Override
    @SuppressWarnings("deprecation")
    public BlockState mirror(BlockState pState, Mirror pMirror) {
        Direction direction = pState.getValue(FACING);
        StairsShape stairsshape = pState.getValue(SHAPE);

        switch(pMirror) {
        case LEFT_RIGHT:
           if (direction.getAxis() == Direction.Axis.Z) {
              switch(stairsshape) {
              case INNER_LEFT:
                 return pState.rotate(Rotation.CLOCKWISE_180).setValue(SHAPE, StairsShape.INNER_RIGHT);
              case INNER_RIGHT:
                 return pState.rotate(Rotation.CLOCKWISE_180).setValue(SHAPE, StairsShape.INNER_LEFT);
              case OUTER_LEFT:
                 return pState.rotate(Rotation.CLOCKWISE_180).setValue(SHAPE, StairsShape.OUTER_RIGHT);
              case OUTER_RIGHT:
                 return pState.rotate(Rotation.CLOCKWISE_180).setValue(SHAPE, StairsShape.OUTER_LEFT);
              default:
                 return pState.rotate(Rotation.CLOCKWISE_180);
              }
           }
           break;
        case FRONT_BACK:
           if (direction.getAxis() == Direction.Axis.X) {
              switch(stairsshape) {
              case INNER_LEFT:
                 return pState.rotate(Rotation.CLOCKWISE_180).setValue(SHAPE, StairsShape.INNER_LEFT);
              case INNER_RIGHT:
                 return pState.rotate(Rotation.CLOCKWISE_180).setValue(SHAPE, StairsShape.INNER_RIGHT);
              case OUTER_LEFT:
                 return pState.rotate(Rotation.CLOCKWISE_180).setValue(SHAPE, StairsShape.OUTER_RIGHT);
              case OUTER_RIGHT:
                 return pState.rotate(Rotation.CLOCKWISE_180).setValue(SHAPE, StairsShape.OUTER_LEFT);
              case STRAIGHT:
                 return pState.rotate(Rotation.CLOCKWISE_180);
              }
           }
         case NONE:
         default:
            break;
        }
  
        return super.mirror(pState, pMirror);
    }
    

    @Override
    @SuppressWarnings("deprecation")
    public BlockState updateShape(BlockState pState, Direction pFacing, BlockState pFacingState, LevelAccessor pLevel, BlockPos pCurrentPos, BlockPos pFacingPos) {        
        return pFacing.getAxis().isHorizontal() ? pState.setValue(SHAPE, getBlockShape(pState, pLevel, pCurrentPos)) : super.updateShape(pState, pFacing, pFacingState, pLevel, pCurrentPos, pFacingPos);
    }

    private static StairsShape getBlockShape(BlockState pState, BlockGetter pLevel, BlockPos pPos) {
        Direction direction = pState.getValue(FACING);
        BlockState blockstate = pLevel.getBlockState(pPos.relative(direction));

        if (isLikeThis(blockstate)) {
           Direction direction1 = blockstate.getValue(FACING);
           if (direction1.getAxis() != pState.getValue(FACING).getAxis() && canTakeShape(pState, pLevel, pPos, direction1.getOpposite())) {
              if (direction1 == direction.getCounterClockWise()) {
                 return StairsShape.OUTER_LEFT;
              }
  
              return StairsShape.OUTER_RIGHT;
           }
        }
  
        BlockState blockstate1 = pLevel.getBlockState(pPos.relative(direction.getOpposite()));
        if (isLikeThis(blockstate1)) {
           Direction direction2 = blockstate1.getValue(FACING);
           if (direction2.getAxis() != pState.getValue(FACING).getAxis() && canTakeShape(pState, pLevel, pPos, direction2)) {
              if (direction2 == direction.getCounterClockWise()) {
                 return StairsShape.INNER_LEFT;
              }
  
              return StairsShape.INNER_RIGHT;
           }
        }
  
        return StairsShape.STRAIGHT;
    }

    private static boolean canTakeShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, Direction pFace) {
        BlockState blockstate = pLevel.getBlockState(pPos.relative(pFace));
        return !isLikeThis(blockstate) || blockstate.getValue(FACING) != pState.getValue(FACING);
    }

    public static boolean isLikeThis(BlockState pState) {
        return pState.getBlock() instanceof AsphaltCurb || pState.getBlock() instanceof AsphaltCurbSlope;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        BlockPos blockpos = pContext.getClickedPos();
        BlockState blockstate = this.defaultBlockState()
            .setValue(FACING, pContext.getHorizontalDirection())
        ;

        return blockstate.setValue(SHAPE, getBlockShape(blockstate, pContext.getLevel(), blockpos));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        super.createBlockStateDefinition(pBuilder);
        pBuilder.add(FACING, SHAPE);
    }
}
