package de.mrjulsen.trafficcraft.block;

import de.mrjulsen.trafficcraft.block.colors.IPaintableBlock;
import de.mrjulsen.trafficcraft.block.properties.RoadType;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.Material;

public class AsphaltCurb extends Block implements IPaintableBlock {

    private RoadType defaultRoadType;
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    
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
    public BlockState mirror(BlockState pState, Mirror pMirror) {
        return pState.rotate(pMirror.getRotation(pState.getValue(FACING)));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        return this.defaultBlockState().setValue(FACING, pContext.getHorizontalDirection().getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        super.createBlockStateDefinition(pBuilder);
        pBuilder.add(FACING);
    }
}
