package de.mrjulsen.trafficcraft.block;

import de.mrjulsen.trafficcraft.block.data.IPaintableBlock;
import de.mrjulsen.trafficcraft.block.data.RoadBlock;
import de.mrjulsen.trafficcraft.block.data.RoadType;
import de.mrjulsen.trafficcraft.item.BrushItem;
import de.mrjulsen.trafficcraft.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;

public class AsphaltBlock extends Block implements IPaintableBlock {

    private RoadType defaultRoadType;

    public AsphaltBlock(RoadType type) {
        super(properties(type));

        this.defaultRoadType = type;
    }

    public static Properties properties(RoadType type) {
        Properties props = Properties.of()
            .mapColor(MapColor.STONE)
            .strength(1.5f)
            .requiresCorrectToolForDrops();

        switch (type) {
            case ASPHALT:
                props.mapColor(MapColor.COLOR_GRAY);
                break;
            case CONCRETE:
                props.mapColor(MapColor.CLAY);
                break;
            default:
                break;
        }

        return props;
    }

    public RoadType getDefaultRoadType() {
        return this.defaultRoadType;
    }

    @Override
    public InteractionResult onSetColor(UseOnContext pContext) {
        
        String id = "";
        Level level = pContext.getLevel();
        BlockPos pos = pContext.getClickedPos();
        BlockState state = pContext.getLevel().getBlockState(pos);
        ItemStack stack = pContext.getItemInHand();
        Player player = pContext.getPlayer();

        if (!(stack.getItem() instanceof BrushItem item)) {
            return InteractionResult.FAIL;
        }

        if (state.getBlock() instanceof AsphaltSlope)
            id = this.getDefaultRoadType().getRoadType() + "_slope_pattern_" + item.getComponent(stack).patternId();
        else if (state.getBlock() instanceof AsphaltBlock)
            id = this.getDefaultRoadType().getRoadType() + "_pattern_" + item.getComponent(stack).patternId();

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
