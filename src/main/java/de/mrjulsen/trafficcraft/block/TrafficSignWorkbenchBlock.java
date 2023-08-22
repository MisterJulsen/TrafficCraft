package de.mrjulsen.trafficcraft.block;

import de.mrjulsen.trafficcraft.screen.menu.TrafficSignWorkbenchMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;

public class TrafficSignWorkbenchBlock extends Block {

    public TrafficSignWorkbenchBlock() {
        super(BlockBehaviour.Properties.of(Material.STONE)
                .strength(1.0f)
                .requiresCorrectToolForDrops());
    }

    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        if (pLevel.isClientSide) {
            return InteractionResult.SUCCESS;
        } else {
            pPlayer.openMenu(pState.getMenuProvider(pLevel, pPos));
            return InteractionResult.CONSUME;
        }
    }

    public MenuProvider getMenuProvider(BlockState pState, Level pLevel, BlockPos pPos) {
        return new SimpleMenuProvider((containerId, inv, player) -> {
            return new TrafficSignWorkbenchMenu(containerId, inv, ContainerLevelAccess.create(pLevel, pPos));
        }, new TextComponent(""));
    }
}
