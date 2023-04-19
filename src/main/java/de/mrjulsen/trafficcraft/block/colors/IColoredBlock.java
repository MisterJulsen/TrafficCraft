package de.mrjulsen.trafficcraft.block.colors;

import de.mrjulsen.trafficcraft.util.PaintColor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.EnumProperty;

public interface IColoredBlock {    
    EnumProperty<PaintColor> COLOR = EnumProperty.create("color", PaintColor.class);
    int getDefaultColor();

    void onRemoveColor(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer);
}
