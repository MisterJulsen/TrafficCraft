package de.mrjulsen.trafficcraft.block;

import de.mrjulsen.trafficcraft.block.data.ITrafficPostLike;
import de.mrjulsen.trafficcraft.block.data.TownSignVariant;
import de.mrjulsen.trafficcraft.block.entity.TownSignBlockEntity;
import de.mrjulsen.trafficcraft.client.ClientWrapper;
import de.mrjulsen.trafficcraft.item.BrushItem;
import de.mrjulsen.trafficcraft.item.WrenchItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class TownSignBlock extends WritableTrafficSign implements ITrafficPostLike {

    private static final VoxelShape SHAPE_COMMON = Block.box(7, 0, 7, 9, 16, 9);
    private static final VoxelShape SHAPE_SOUTH = Shapes.or(SHAPE_COMMON, Block.box(0, 4, 9, 16, 16, 9.5D));    
    private static final VoxelShape SHAPE_NORTH = Shapes.or(SHAPE_COMMON, Block.box(0, 4, 6.5D, 16, 16, 7));
    private static final VoxelShape SHAPE_EAST = Shapes.or(SHAPE_COMMON, Block.box(9, 4, 0, 9.5D, 16, 16));
    private static final VoxelShape SHAPE_WEST = Shapes.or(SHAPE_COMMON, Block.box(6.5D, 4, 0, 7, 16, 16));

    public static final EnumProperty<TownSignVariant> VARIANT = EnumProperty.create("variant", TownSignVariant.class);

    public TownSignBlock() {
        super(BlockBehaviour.Properties.of(Material.METAL)
            .strength(1.0f)
            .requiresCorrectToolForDrops()
            .sound(SoundType.LANTERN)
        );

        this.registerDefaultState(this.stateDefinition.any().setValue(VARIANT, TownSignVariant.FRONT));
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        switch (pState.getValue(FACING)) {
            case NORTH:
                return pState.getValue(VARIANT) == TownSignVariant.BOTH ? Shapes.or(SHAPE_NORTH, SHAPE_SOUTH) : SHAPE_NORTH;
            case SOUTH:
                return pState.getValue(VARIANT) == TownSignVariant.BOTH ? Shapes.or(SHAPE_NORTH, SHAPE_SOUTH) : SHAPE_SOUTH;
            case EAST:
                return pState.getValue(VARIANT) == TownSignVariant.BOTH ? Shapes.or(SHAPE_EAST, SHAPE_WEST) : SHAPE_EAST;
            case WEST:
                return pState.getValue(VARIANT) == TownSignVariant.BOTH ? Shapes.or(SHAPE_EAST, SHAPE_WEST) : SHAPE_WEST;
            default:
                return SHAPE_COMMON;
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        super.createBlockStateDefinition(pBuilder);
        pBuilder.add(VARIANT);
    }

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {        
        Item item = pPlayer.getInventory().getSelected().getItem();

        if (item instanceof BrushItem) {
            return InteractionResult.FAIL;
        }

        ETownSignSide editSide;
        switch (pState.getValue(VARIANT)) {
            case BACK:
                editSide = ETownSignSide.BACK;
                break;
            case BOTH:
                if (pHit.getDirection() == pState.getValue(FACING))
                    editSide = ETownSignSide.FRONT;
                else if (pHit.getDirection() == pState.getValue(FACING).getOpposite())
                    editSide = ETownSignSide.BACK;
                else
                    return InteractionResult.FAIL;
                break;
            case FRONT:
            default:
                editSide = ETownSignSide.FRONT;
                break;
        }

        if(pLevel.isClientSide) {
            if (item instanceof WrenchItem && pLevel.getBlockEntity(pPos) instanceof TownSignBlockEntity blockEntity) {
                if(!pPlayer.isShiftKeyDown()) {                
                    ClientWrapper.showTownSignScreen(blockEntity, editSide);
                }
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.FAIL;

    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new TownSignBlockEntity(pPos, pState);
    }

    public enum ETownSignSide implements StringRepresentable {
        FRONT("front", 0),
        BACK("back", 1);
        
        private String side;
        private int index;
        
        private ETownSignSide(String variant, int index) {
            this.side = variant;
            this.index = index;
        }
        
        public String getSide() {
            return this.side;
        }
    
        public int getIndex() {
            return this.index;
        }
    
        public String getTranslationKey() {
            return String.format("gui.trafficcraft.town_sign.variant.%s", side);
        }
    
        public static ETownSignSide getSideByIndex(int index) {
            for (ETownSignSide shape : ETownSignSide.values()) {
                if (shape.getIndex() == index) {
                    return shape;
                }
            }
            return ETownSignSide.FRONT;
        }
    
        @Override
        public String getSerializedName() {
            return side;
        }
    }

    @Override
    public boolean canAttach(BlockState pState, BlockPos pPos, Direction pDirection) {
        return pState.getValue(VARIANT) != TownSignVariant.BOTH && pDirection == pState.getValue(FACING).getOpposite();
    }
}
