package de.mrjulsen.trafficcraft.block;

import de.mrjulsen.trafficcraft.Constants;
import de.mrjulsen.trafficcraft.block.properties.TrafficSignShape;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public class RectangleTrafficSignBlock extends TrafficSignBlock {

    private static final TrafficSignShape SIGN_SHAPE = TrafficSignShape.RECTANGLE;
    public static final IntegerProperty TYPE = IntegerProperty.create("type", 0, Constants.SIGN_PATTERNS.get(SIGN_SHAPE));

    public RectangleTrafficSignBlock() {        
        this.registerDefaultState(this.stateDefinition.any()
            .setValue(FACING, Direction.NORTH)
            .setValue(TYPE, 0)
            .setValue(WATERLOGGED, false)
        );
    }

    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> pBuilder) {
        super.createBlockStateDefinition(pBuilder);
        pBuilder.add(TYPE);
    }

    @Override
    protected int getType(BlockState state) {
        return state.getValue(TYPE);
    }

    @Override
    protected TrafficSignShape getSignShape() {
        return SIGN_SHAPE;
    }  
}
