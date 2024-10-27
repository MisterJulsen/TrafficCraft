package de.mrjulsen.trafficcraft.block;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import de.mrjulsen.mcdragonlib.core.IIterableEnum;
import de.mrjulsen.trafficcraft.block.entity.EmptyBlockEntity;
import de.mrjulsen.trafficcraft.config.ModServerConfig;
import de.mrjulsen.trafficcraft.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class RoadSaltBlock extends BaseEntityBlock {

    public static final EnumProperty<RoadSaltQuality> QUALITY = EnumProperty.create("quality", RoadSaltQuality.class);

    public RoadSaltBlock() {
        super(BlockBehaviour.Properties.of(ModBlocks.ROAD_SALT_MATERIAL)
            .instabreak()
            .noOcclusion()
            .noCollission()
            .noDrops()
            .sound(SoundType.GRAVEL)
        );

        this.registerDefaultState(this.stateDefinition.any()
            .setValue(QUALITY, RoadSaltQuality.FRESH)
        );
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Block.box(0, 0, 0, 16, 1, 16);
    }

    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> pBuilder) {
        super.createBlockStateDefinition(pBuilder);
        pBuilder.add(QUALITY);
    }

    public BlockState updateShape(BlockState pState, Direction pFacing, BlockState pFacingState, LevelAccessor pLevel, BlockPos pCurrentPos, BlockPos pFacingPos) {
        return pFacing == Direction.DOWN && !this.canSurvive(pState, pLevel, pCurrentPos) ? Blocks.AIR.defaultBlockState() : pState;
    }

    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos blockpos = pos.below();
        BlockState blockstate = level.getBlockState(blockpos);
        return this.canSurviveOn(level, blockpos, blockstate);
    }

    private boolean canSurviveOn(BlockGetter reader, BlockPos pos, BlockState state) {
        return state.isFaceSturdy(reader, pos, Direction.UP);
    }

    @Override
    public boolean canBeReplaced(BlockState state, Fluid fluid) {
        return true;
    }

    @Override
    public boolean canBeReplaced(BlockState state, BlockPlaceContext useContext) {
        return true;
    }
    
    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new EmptyBlockEntity(pPos, pState);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
        if (!pLevel.isClientSide) {
            return (level, pos, state, blockEntity) -> {
                int randTicks = level.getGameRules().getRule(GameRules.RULE_RANDOMTICKING).get();
                if (randTicks <= 0 || level.getRandom().nextInt(ModServerConfig.ROAD_SALT_SPEED.get() * GameRules.DEFAULT_RANDOM_TICK_SPEED * (pState.getValue(QUALITY).getIndex() + 1) / randTicks) != 0) {
                    return;
                }                
        
                List<BlockPos> blocks = new ArrayList<>();
                BlockPos.betweenClosed(pos.offset(-ModServerConfig.ROAD_SALT_RANGE.get(), -ModServerConfig.ROAD_SALT_RANGE.get(), -ModServerConfig.ROAD_SALT_RANGE.get()), pos.offset(ModServerConfig.ROAD_SALT_RANGE.get(), 1, ModServerConfig.ROAD_SALT_RANGE.get())).iterator().forEachRemaining(a -> {
                    blocks.add(new BlockPos(a));
                });
                Collections.shuffle(blocks);
                Iterator<BlockPos> iterator = blocks.iterator();
            
                do {
                    BlockPos blockpos = iterator.next();

                    if (blockpos.distManhattan(pos) > ModServerConfig.ROAD_SALT_RANGE.get()) {
                        continue;
                    }

                    if (level.getBlockState(blockpos).is(Blocks.SNOW) || level.getBlockState(blockpos).is(Blocks.ICE)) {                        
                        Block.dropResources(state, level, blockpos);
                        level.removeBlock(blockpos, false);
                        if (ModServerConfig.ROAD_SALT_PRESERVATION.get() >= 0 && level.getRandom().nextInt(ModServerConfig.ROAD_SALT_PRESERVATION.get() * (pState.getValue(QUALITY).getIndex() + 1)) == 0) {
                            RoadSaltQuality quality = level.getBlockState(pos).getValue(QUALITY).next();
                            if (quality.getIndex() == 0) {
                                level.removeBlock(pos, false);
                            } else {
                                level.setBlockAndUpdate(pos, pState.setValue(QUALITY, quality));
                            }
                        } 
                        return;
                    } else if (level.getBlockState(blockpos).is(BlockTags.DIRT) && !level.getBlockState(blockpos).is(Blocks.COARSE_DIRT)) {
                        level.setBlockAndUpdate(blockpos, Blocks.COARSE_DIRT.defaultBlockState());
                        return;
                    } else if (
                            level.getBlockState(blockpos).is(BlockTags.SMALL_FLOWERS) ||
                            level.getBlockState(blockpos).is(BlockTags.CROPS) ||
                            level.getBlockState(blockpos).is(BlockTags.REPLACEABLE_PLANTS)
                    ) {
                        level.removeBlock(blockpos, false);
                        return;
                    } else if (!level.getBlockState(blockpos).is(Blocks.DEAD_BUSH) && level.getBlockState(blockpos).is(BlockTags.SAPLINGS)) {
                        level.setBlockAndUpdate(blockpos, Blocks.DEAD_BUSH.defaultBlockState());
                        return;
                    }
                } while (iterator.hasNext());                
            };
        }
        return null;
    }

    public static enum RoadSaltQuality implements StringRepresentable, IIterableEnum<RoadSaltQuality> {
        FRESH(0, "fresh"),
        MUDDY(1, "muddy"),
        DILUTED(2, "diluted");

        private final int index;
        private final String name;

        private RoadSaltQuality(int index, String name) {
            this.index = index;
            this.name = name;
        }

        public int getIndex() {
            return index;
        }

        public String getName() {
            return name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }

        @Override
        public RoadSaltQuality[] getValues() {
            return values();
        }       
        
    }
}
