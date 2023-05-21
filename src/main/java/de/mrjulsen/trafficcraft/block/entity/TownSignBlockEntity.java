package de.mrjulsen.trafficcraft.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class TownSignBlockEntity extends WritableTrafficSignBlockEntity {
    
    protected TownSignBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public TownSignBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TOWN_SIGN_BLOCK_ENTITY.get(), pos, state);
    }

    @Override
    public int lineCount() {
        return 4;
    }
}
