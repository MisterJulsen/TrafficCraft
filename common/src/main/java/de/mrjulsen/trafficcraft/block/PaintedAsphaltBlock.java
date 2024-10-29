package de.mrjulsen.trafficcraft.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import de.mrjulsen.trafficcraft.block.data.RoadBlock;
import de.mrjulsen.trafficcraft.block.data.RoadType;
import de.mrjulsen.trafficcraft.item.BrushItem;
import de.mrjulsen.trafficcraft.registry.ModBlocks;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;

public class PaintedAsphaltBlock extends RoadBlock {
        
    public static final MapCodec<PaintedAsphaltBlock> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(
            propertiesCodec(),
            RoadType.CODEC.fieldOf("road_type").forGetter(PaintedAsphaltBlock::getDefaultRoadType)
        ).apply(instance, PaintedAsphaltBlock::new);
    });

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    private RegistrySupplier<Block> pickupBlock;

    public PaintedAsphaltBlock(BlockBehaviour.Properties properties, RoadType type) {
        super(properties
            .mapColor(MapColor.STONE)
            .strength(1.5f)
            .requiresCorrectToolForDrops()
        , type);

        this.pickupBlock = type.getPickupBlock();
    }
    
    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
        if (pickupBlock == null || pickupBlock == this) {
            ItemStack stack = super.getCloneItemStack(level, pos, state);
            stack.setTag(null);
            return stack;
        }

        ItemStack stack = this.pickupBlock.get().getCloneItemStack(level, pos, state);
        stack.setTag(null);
        return stack;
    }

    @Override
    public void attack(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer) {
        onRemoveColor(pState, pLevel, pPos, pPlayer);
    }

    @Override
    public void onRemoveColor(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer) {
        ItemStack stack = pPlayer.getInventory().getSelected();
        Item item = stack.getItem();

        if (!(item instanceof BrushItem)) {
            return;
        }

        if (this == ModBlocks.ASPHALT.get() || this == ModBlocks.CONCRETE.get()) {
            return;
        }

        switch (this.getDefaultRoadType()) {
            case ASPHALT:
                pLevel.setBlockAndUpdate(pPos, ModBlocks.ASPHALT.get().defaultBlockState());
                pLevel.playSound(null, pPos, SoundEvents.SLIME_BLOCK_PLACE, SoundSource.BLOCKS, 0.8F, 2.0F);
                break;
            case CONCRETE:
                pLevel.setBlockAndUpdate(pPos, ModBlocks.CONCRETE.get().defaultBlockState());
                pLevel.playSound(null, pPos, SoundEvents.SLIME_BLOCK_PLACE, SoundSource.BLOCKS, 0.8F, 2.0F);
                break;
            default:
                break;
        }
    }

    
}
