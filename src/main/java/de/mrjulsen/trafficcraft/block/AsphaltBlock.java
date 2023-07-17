package de.mrjulsen.trafficcraft.block;

import de.mrjulsen.trafficcraft.block.colors.IPaintableBlock;
import de.mrjulsen.trafficcraft.block.properties.RoadBlock;
import de.mrjulsen.trafficcraft.block.properties.RoadType;
import de.mrjulsen.trafficcraft.item.BrushItem;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;

public class AsphaltBlock extends Block implements IPaintableBlock {

    private RoadType defaultRoadType;

    public AsphaltBlock(RoadType type) {
        super(Properties.of(Material.STONE)
                .strength(1.5f)
                .requiresCorrectToolForDrops());

        this.defaultRoadType = type;
    }

    public RoadType getDefaultRoadType() {
        return this.defaultRoadType;
    }

    @Override
    public InteractionResult onSetColor(UseOnContext pContext) {
        if (this instanceof AsphaltCurbSlope)
            return InteractionResult.FAIL;

        String id = "";
        Level level = pContext.getLevel();
        BlockPos pos = pContext.getClickedPos();
        BlockState state = pContext.getLevel().getBlockState(pos);
        ItemStack stack = pContext.getItemInHand();
        Player player = pContext.getPlayer();

        if (state.getBlock() instanceof AsphaltSlope)
            id = this.getDefaultRoadType().getRoadType() + "_slope_pattern_" + BrushItem.getPatternId(stack);
        else if (state.getBlock() instanceof AsphaltBlock)
            id = this.getDefaultRoadType().getRoadType() + "_pattern_" + BrushItem.getPatternId(stack);

        if (!ModBlocks.ROAD_BLOCKS.containsKey(id) || !(ModBlocks.ROAD_BLOCKS.get(id).get() instanceof RoadBlock)) {
            return InteractionResult.FAIL;
        }

        RoadBlock road = (RoadBlock) ModBlocks.ROAD_BLOCKS.get(id).get();
        BlockState newState = road.defaultBlockState().setValue(RoadBlock.FACING, player.getDirection());

        if (state.getBlock() instanceof AsphaltSlope) {
            newState = newState.setValue(PaintedAsphaltSlope.LAYERS, state.getValue(AsphaltSlope.LAYERS));
        }

        level.setBlockAndUpdate(pos, newState);
        road.onSetColor(pContext);

        return InteractionResult.CONSUME;
    }
}
