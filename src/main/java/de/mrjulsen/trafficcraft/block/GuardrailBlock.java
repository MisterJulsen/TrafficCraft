package de.mrjulsen.trafficcraft.block;

import de.mrjulsen.trafficcraft.Constants;
import de.mrjulsen.trafficcraft.block.properties.ColorableBlock;
import de.mrjulsen.trafficcraft.block.properties.ITrafficPostLike;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.StairsShape;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.block.state.StateDefinition.Builder;

public class GuardrailBlock extends ColorableBlock implements SimpleWaterloggedBlock, ITrafficPostLike {

    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final EnumProperty<StairsShape> SHAPE = BlockStateProperties.STAIRS_SHAPE;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    private static final VoxelShape SHAPE_POST = Block.box(7, 0, 7, 9, 16, 9);    
    private static final VoxelShape SHAPE_XP_SOUTH = Block.box(9D, 8.5D, 5.5D, 16, 15.5D, 7D);
    private static final VoxelShape SHAPE_XN_SOUTH = Block.box(0, 8.5D, 5.5D, 7, 15.5D, 7D);
    private static final VoxelShape SHAPE_XP_NORTH = Block.box(9D, 8.5D, 9, 16, 15.5D, 10.5D);
    private static final VoxelShape SHAPE_XN_NORTH = Block.box(0, 8.5D, 9, 7, 15.5D, 10.5D);    
    private static final VoxelShape SHAPE_ZP_EAST = Block.box(5.5D, 8.5D, 9, 7, 15.5D, 16);
    private static final VoxelShape SHAPE_ZN_EAST = Block.box(5.5D, 8.5D, 0, 7, 15.5D, 7);
    private static final VoxelShape SHAPE_ZP_WEST = Block.box(9, 8.5D, 9, 10.5D, 15.5D, 16);
    private static final VoxelShape SHAPE_ZN_WEST = Block.box(9, 8.5D, 0, 10.5D, 15.5D, 7D);    
    private static final VoxelShape SHAPE_SOUTH = Block.box(5.5D, 8.5D, 5.5D, 10.5D, 15.5D, 7D);
    private static final VoxelShape SHAPE_NORTH = Block.box(5.5D, 8.5D, 9, 10.5D, 15.5D, 10.5D);    
    private static final VoxelShape SHAPE_EAST = Block.box(5.5D, 8.5D, 5.5D, 7, 15.5D, 10.5D);
    private static final VoxelShape SHAPE_WEST = Block.box(9, 8.5D, 5.5D, 10.5D, 15.5D, 10.5D);

    private static final VoxelShape COLLISION_POST = Block.box(7, 0, 7, 9, 24, 9);
    private static final VoxelShape COLLISION_XP_SOUTH = Block.box(9D, 8.5D, 5.5D, 16, 24, 7D);
    private static final VoxelShape COLLISION_XN_SOUTH = Block.box(0, 8.5D, 5.5D, 7, 24, 7D);
    private static final VoxelShape COLLISION_XP_NORTH = Block.box(9D, 8.5D, 9, 16, 24, 10.5D);
    private static final VoxelShape COLLISION_XN_NORTH = Block.box(0, 8.5D, 9, 7, 24, 10.5D);    
    private static final VoxelShape COLLISION_ZP_EAST = Block.box(5.5D, 8.5D, 9, 7, 24, 16);
    private static final VoxelShape COLLISION_ZN_EAST = Block.box(5.5D, 8.5D, 0, 7, 24, 7);
    private static final VoxelShape COLLISION_ZP_WEST = Block.box(9, 8.5D, 9, 10.5D, 24, 16);
    private static final VoxelShape COLLISION_ZN_WEST = Block.box(9, 8.5D, 0, 10.5D, 15.5D, 7D);    
    private static final VoxelShape COLLISION_SOUTH = Block.box(5.5D, 8.5D, 5.5D, 10.5D, 24, 7D);
    private static final VoxelShape COLLISION_NORTH = Block.box(5.5D, 8.5D, 9, 10.5D, 24, 10.5D);    
    private static final VoxelShape COLLISION_EAST = Block.box(5.5D, 8.5D, 5.5D, 7, 24, 10.5D);
    private static final VoxelShape COLLISION_WEST = Block.box(9, 8.5D, 5.5D, 10.5D, 24, 10.5D);


    public GuardrailBlock() {
        super(BlockBehaviour.Properties.of(Material.METAL)
            .strength(2f)
            .sound(SoundType.LANTERN)  
        );
        this.registerDefaultState(this.stateDefinition.any()
            .setValue(FACING, Direction.NORTH)  
            .setValue(WATERLOGGED, false)        
        );
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        Direction direction = pState.getValue(FACING);
        StairsShape shape = pState.getValue(SHAPE);

        if (direction == Direction.NORTH && shape == StairsShape.STRAIGHT) {
            return Shapes.or(SHAPE_POST, SHAPE_XP_NORTH, SHAPE_XN_NORTH, SHAPE_NORTH);
        } else if (direction == Direction.SOUTH && shape == StairsShape.STRAIGHT) {
            return Shapes.or(SHAPE_POST, SHAPE_XP_SOUTH, SHAPE_XN_SOUTH, SHAPE_SOUTH);
        } else if (direction == Direction.EAST && shape == StairsShape.STRAIGHT) {
            return Shapes.or(SHAPE_POST, SHAPE_ZP_EAST, SHAPE_ZN_EAST, SHAPE_EAST);
        } else if (direction == Direction.WEST && shape == StairsShape.STRAIGHT) {
            return Shapes.or(SHAPE_POST, SHAPE_ZP_WEST, SHAPE_ZN_WEST, SHAPE_WEST);
        }


        else if (shape == StairsShape.INNER_RIGHT && (direction == Direction.NORTH)) {
            return Shapes.or(SHAPE_POST, SHAPE_ZP_EAST, SHAPE_XN_NORTH);           
        } else if (shape == StairsShape.INNER_LEFT && (direction == Direction.NORTH)) { 
            return Shapes.or(SHAPE_POST, SHAPE_ZP_WEST, SHAPE_XP_NORTH);
        }

        else if (shape == StairsShape.INNER_RIGHT && (direction == Direction.EAST)) {
            return Shapes.or(SHAPE_POST, SHAPE_XN_SOUTH, SHAPE_ZN_EAST);
        } else if (shape == StairsShape.INNER_LEFT && (direction == Direction.EAST)) {
            return Shapes.or(SHAPE_POST, SHAPE_XN_NORTH, SHAPE_ZP_EAST);            
        }

        else if (shape == StairsShape.INNER_RIGHT && (direction == Direction.SOUTH)) {
            return Shapes.or(SHAPE_POST, SHAPE_ZN_WEST, SHAPE_XP_SOUTH);
        } else if (shape == StairsShape.INNER_LEFT && (direction == Direction.SOUTH)) {
            return Shapes.or(SHAPE_POST, SHAPE_ZN_EAST, SHAPE_XN_SOUTH);            
        }

        else if (shape == StairsShape.INNER_RIGHT && (direction == Direction.WEST)) {
            return Shapes.or(SHAPE_POST, SHAPE_XP_NORTH, SHAPE_ZP_WEST);
        } else if (shape == StairsShape.INNER_LEFT && (direction == Direction.WEST)) {
            return Shapes.or(SHAPE_POST, SHAPE_XP_SOUTH, SHAPE_ZN_WEST);            
        }

        

        else if (shape == StairsShape.OUTER_RIGHT && (direction == Direction.NORTH)) {
            return Shapes.or(SHAPE_POST, SHAPE_ZN_EAST, SHAPE_XP_NORTH, SHAPE_EAST, SHAPE_NORTH);
        } else if (shape == StairsShape.OUTER_LEFT && (direction == Direction.NORTH)) {
            return Shapes.or(SHAPE_POST, SHAPE_ZN_WEST, SHAPE_XN_NORTH, SHAPE_WEST, SHAPE_NORTH);            
        }

        else if (shape == StairsShape.OUTER_RIGHT && (direction == Direction.EAST)) {
            return Shapes.or(SHAPE_POST, SHAPE_XP_SOUTH, SHAPE_ZP_EAST, SHAPE_SOUTH, SHAPE_EAST);
        } else if (shape == StairsShape.OUTER_LEFT && (direction == Direction.EAST)) {
            return Shapes.or(SHAPE_POST, SHAPE_XP_NORTH, SHAPE_ZN_EAST, SHAPE_NORTH, SHAPE_EAST);            
        }

        else if (shape == StairsShape.OUTER_RIGHT && (direction == Direction.SOUTH)) {
            return Shapes.or(SHAPE_POST, SHAPE_ZP_WEST, SHAPE_XN_SOUTH, SHAPE_WEST, SHAPE_SOUTH);
        } else if (shape == StairsShape.OUTER_LEFT && (direction == Direction.SOUTH)) {
            return Shapes.or(SHAPE_POST, SHAPE_ZP_EAST, SHAPE_XP_SOUTH, SHAPE_EAST, SHAPE_SOUTH);            
        }

        else if (shape == StairsShape.OUTER_RIGHT && (direction == Direction.WEST)) {
            return Shapes.or(SHAPE_POST, SHAPE_XN_NORTH, SHAPE_ZN_WEST, SHAPE_WEST, SHAPE_NORTH);
        } else if (shape == StairsShape.OUTER_LEFT && (direction == Direction.WEST)) {
            return Shapes.or(SHAPE_POST, SHAPE_XN_SOUTH, SHAPE_ZP_WEST, SHAPE_WEST, SHAPE_SOUTH);            
        }

        return SHAPE_POST;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        Direction direction = pState.getValue(FACING);
        StairsShape shape = pState.getValue(SHAPE);

        if (direction == Direction.NORTH && shape == StairsShape.STRAIGHT) {
            return Shapes.or(COLLISION_POST, COLLISION_XP_NORTH, COLLISION_XN_NORTH, COLLISION_NORTH);
        } else if (direction == Direction.SOUTH && shape == StairsShape.STRAIGHT) {
            return Shapes.or(COLLISION_POST, COLLISION_XP_SOUTH, COLLISION_XN_SOUTH, COLLISION_SOUTH);
        } else if (direction == Direction.EAST && shape == StairsShape.STRAIGHT) {
            return Shapes.or(COLLISION_POST, COLLISION_ZP_EAST, COLLISION_ZN_EAST, COLLISION_EAST);
        } else if (direction == Direction.WEST && shape == StairsShape.STRAIGHT) {
            return Shapes.or(COLLISION_POST, COLLISION_ZP_WEST, COLLISION_ZN_WEST, COLLISION_WEST);
        }


        else if (shape == StairsShape.INNER_RIGHT && (direction == Direction.NORTH)) {
            return Shapes.or(COLLISION_POST, COLLISION_ZP_EAST, COLLISION_XN_NORTH);           
        } else if (shape == StairsShape.INNER_LEFT && (direction == Direction.NORTH)) { 
            return Shapes.or(COLLISION_POST, COLLISION_ZP_WEST, COLLISION_XP_NORTH);
        }

        else if (shape == StairsShape.INNER_RIGHT && (direction == Direction.EAST)) {
            return Shapes.or(COLLISION_POST, COLLISION_XN_SOUTH, COLLISION_ZN_EAST);
        } else if (shape == StairsShape.INNER_LEFT && (direction == Direction.EAST)) {
            return Shapes.or(COLLISION_POST, COLLISION_XN_NORTH, COLLISION_ZP_EAST);            
        }

        else if (shape == StairsShape.INNER_RIGHT && (direction == Direction.SOUTH)) {
            return Shapes.or(COLLISION_POST, COLLISION_ZN_WEST, COLLISION_XP_SOUTH);
        } else if (shape == StairsShape.INNER_LEFT && (direction == Direction.SOUTH)) {
            return Shapes.or(COLLISION_POST, COLLISION_ZN_EAST, COLLISION_XN_SOUTH);            
        }

        else if (shape == StairsShape.INNER_RIGHT && (direction == Direction.WEST)) {
            return Shapes.or(COLLISION_POST, COLLISION_XP_NORTH, COLLISION_ZP_WEST);
        } else if (shape == StairsShape.INNER_LEFT && (direction == Direction.WEST)) {
            return Shapes.or(COLLISION_POST, COLLISION_XP_SOUTH, COLLISION_ZN_WEST);            
        }

        

        else if (shape == StairsShape.OUTER_RIGHT && (direction == Direction.NORTH)) {
            return Shapes.or(COLLISION_POST, COLLISION_ZN_EAST, COLLISION_XP_NORTH, COLLISION_EAST, COLLISION_NORTH);
        } else if (shape == StairsShape.OUTER_LEFT && (direction == Direction.NORTH)) {
            return Shapes.or(COLLISION_POST, COLLISION_ZN_WEST, COLLISION_XN_NORTH, COLLISION_WEST, COLLISION_NORTH);            
        }

        else if (shape == StairsShape.OUTER_RIGHT && (direction == Direction.EAST)) {
            return Shapes.or(COLLISION_POST, COLLISION_XP_SOUTH, COLLISION_ZP_EAST, COLLISION_SOUTH, COLLISION_EAST);
        } else if (shape == StairsShape.OUTER_LEFT && (direction == Direction.EAST)) {
            return Shapes.or(COLLISION_POST, COLLISION_XP_NORTH, COLLISION_ZN_EAST, COLLISION_NORTH, COLLISION_EAST);            
        }

        else if (shape == StairsShape.OUTER_RIGHT && (direction == Direction.SOUTH)) {
            return Shapes.or(COLLISION_POST, COLLISION_ZP_WEST, COLLISION_XN_SOUTH, COLLISION_WEST, COLLISION_SOUTH);
        } else if (shape == StairsShape.OUTER_LEFT && (direction == Direction.SOUTH)) {
            return Shapes.or(COLLISION_POST, COLLISION_ZP_EAST, COLLISION_XP_SOUTH, COLLISION_EAST, COLLISION_SOUTH);            
        }

        else if (shape == StairsShape.OUTER_RIGHT && (direction == Direction.WEST)) {
            return Shapes.or(COLLISION_POST, COLLISION_XN_NORTH, COLLISION_ZN_WEST, COLLISION_WEST, COLLISION_NORTH);
        } else if (shape == StairsShape.OUTER_LEFT && (direction == Direction.WEST)) {
            return Shapes.or(COLLISION_POST, COLLISION_XN_SOUTH, COLLISION_ZP_WEST, COLLISION_WEST, COLLISION_SOUTH);            
        }

        return COLLISION_POST;
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
        if (pState.getValue(WATERLOGGED)) {
           pLevel.scheduleTick(pCurrentPos, Fluids.WATER, Fluids.WATER.getTickDelay(pLevel));
        }
  
        return pFacing.getAxis().isHorizontal() ? pState.setValue(SHAPE, getGuardrailShape(pState, pLevel, pCurrentPos)) : super.updateShape(pState, pFacing, pFacingState, pLevel, pCurrentPos, pFacingPos);
    
    }

    private static StairsShape getGuardrailShape(BlockState pState, BlockGetter pLevel, BlockPos pPos) {
        Direction direction = pState.getValue(FACING);
        BlockState blockstate = pLevel.getBlockState(pPos.relative(direction));

        if (isGuardrail(blockstate)) {
           Direction direction1 = blockstate.getValue(FACING);
           if (direction1.getAxis() != pState.getValue(FACING).getAxis() && canTakeShape(pState, pLevel, pPos, direction1.getOpposite())) {
              if (direction1 == direction.getCounterClockWise()) {
                 return StairsShape.OUTER_LEFT;
              }
  
              return StairsShape.OUTER_RIGHT;
           }
        }
  
        BlockState blockstate1 = pLevel.getBlockState(pPos.relative(direction.getOpposite()));
        if (isGuardrail(blockstate1)) {
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
        return !isGuardrail(blockstate) || blockstate.getValue(FACING) != pState.getValue(FACING);
    }

    public static boolean isGuardrail(BlockState pState) {
        return pState.getBlock() instanceof GuardrailBlock;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        BlockPos blockpos = pContext.getClickedPos();
        FluidState fluidstate = pContext.getLevel().getFluidState(blockpos);
        BlockState blockstate = this.defaultBlockState()
            .setValue(FACING, pContext.getHorizontalDirection())
            .setValue(WATERLOGGED, Boolean.valueOf(fluidstate.getType() == Fluids.WATER))        
        ;

        return blockstate.setValue(SHAPE, getGuardrailShape(blockstate, pContext.getLevel(), blockpos));
    }

    @Override
    @SuppressWarnings("deprecation")
    public FluidState getFluidState(BlockState pState) {
        return pState.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(pState);
    }

    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> pBuilder) {
        super.createBlockStateDefinition(pBuilder);
        pBuilder.add(FACING, SHAPE, WATERLOGGED);
    }

    public boolean isPathfindable(BlockState pState, BlockGetter pLevel, BlockPos pPos, PathComputationType pType) {
        return false;
    }

    @Override
    public int getDefaultColor() {
        return Constants.METAL_COLOR;
    }

    @Override
    public boolean canAttach(BlockState pState, BlockPos pPos, Direction pDirection) {
        return pDirection == pState.getValue(FACING).getOpposite();
    }
}
