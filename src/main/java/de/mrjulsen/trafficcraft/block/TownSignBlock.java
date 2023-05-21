package de.mrjulsen.trafficcraft.block;

import de.mrjulsen.trafficcraft.block.entity.TownSignBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;

public class TownSignBlock extends WritableTrafficSign {

    public TownSignBlock() {
        super(BlockBehaviour.Properties.of(Material.METAL)
            .strength(1.0f)
            .requiresCorrectToolForDrops()
            .sound(SoundType.LANTERN)
        );
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new TownSignBlockEntity(pPos, pState);
    }
}
