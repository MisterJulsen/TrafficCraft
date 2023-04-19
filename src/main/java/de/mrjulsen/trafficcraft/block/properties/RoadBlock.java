package de.mrjulsen.trafficcraft.block.properties;

import de.mrjulsen.trafficcraft.block.properties.RoadBlock;
import de.mrjulsen.trafficcraft.util.PaintColor;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.DirectionProperty;

public abstract class RoadBlock extends ColorableBlock {

    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    private RoadType defaultRoadType;

    public RoadBlock(Properties properties, RoadType type) {
        super(properties);
        this.defaultRoadType = type;
        this.registerDefaultState(this.stateDefinition.any()
            .setValue(FACING, Direction.NORTH)
        );
    }

    public RoadType getDefaultRoadType() {
        return this.defaultRoadType;
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
        return this.defaultBlockState()
            .setValue(FACING, pContext.getHorizontalDirection().getOpposite())
            .setValue(COLOR, PaintColor.NONE)
        ;
    }

    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> pBuilder) {
        super.createBlockStateDefinition(pBuilder);
        pBuilder.add(FACING);
    }

    @Override
    public int getDefaultColor() {
        return 0xFFFFFFFF;
    }
}
