package de.mrjulsen.trafficcraft.block.properties;

import de.mrjulsen.trafficcraft.block.colors.IPaintableBlock;
import de.mrjulsen.trafficcraft.block.entity.ColoredBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public abstract class ColorableBlock extends BaseEntityBlock implements IPaintableBlock {

    public ColorableBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void attack(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer) {
        onRemoveColor(pState, pLevel, pPos, pPlayer);
    }

    @Override
    public RenderShape getRenderShape(BlockState pState) {        
        return RenderShape.MODEL;
    }
    
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new ColoredBlockEntity(pPos, pState);
    }

}
