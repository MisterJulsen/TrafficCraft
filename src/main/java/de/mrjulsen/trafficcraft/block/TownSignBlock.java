package de.mrjulsen.trafficcraft.block;

import de.mrjulsen.trafficcraft.block.client.WritableTrafficSignClient;
import de.mrjulsen.trafficcraft.block.entity.TownSignBlockEntity;
import de.mrjulsen.trafficcraft.block.properties.TownSignVariant;
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

public class TownSignBlock extends WritableTrafficSign {

    private static final VoxelShape BASE_SHAPE = Block.box(6, 0, 6, 10, 16, 10);
    
    protected static final VoxelShape SHAPE_SN = Shapes.or(BASE_SHAPE, Block.box(0, 4, 6, 16, 16, 10));
    protected static final VoxelShape SHAPE_EW = Shapes.or(BASE_SHAPE, Block.box(6, 4, 0, 10, 16, 16));

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
        return pState.getValue(FACING) == Direction.NORTH || pState.getValue(FACING) == Direction.SOUTH ? SHAPE_SN : SHAPE_EW;
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
                    WritableTrafficSignClient.showTownSignGui(blockEntity, editSide);
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
}
