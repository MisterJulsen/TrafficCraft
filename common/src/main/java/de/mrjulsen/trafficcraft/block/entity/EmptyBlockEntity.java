package de.mrjulsen.trafficcraft.block.entity;

import de.mrjulsen.mcdragonlib.block.SyncedBlockEntity;
import de.mrjulsen.trafficcraft.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class EmptyBlockEntity extends SyncedBlockEntity {

    protected EmptyBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public EmptyBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.EMPTY_BLOCK_ENTITY.get(), pos, state);
    }
}
