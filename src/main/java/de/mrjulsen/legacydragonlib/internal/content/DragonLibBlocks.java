package de.mrjulsen.legacydragonlib.internal.content;

import java.lang.reflect.InvocationTargetException;
import java.util.function.Supplier;

import de.mrjulsen.legacydragonlib.DragonLibConstants;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class DragonLibBlocks {
    
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, DragonLibConstants.DRAGONLIB_MODID);
    
    public static final RegistryObject<Block> DRAGON = registerBlock("dragon", () -> { return new DragonLibSampleBlock(BlockBehaviour.Properties.of(Material.STONE).strength(1.5f)); }, DragonLibSampleBlock.DragonItem.class);
    

    private static <T extends Block, I extends BlockItem>RegistryObject<T> registerBlock(String name, Supplier<T> block, Class<I> blockItemClass) {
        RegistryObject<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn, blockItemClass);
        return toReturn;
    }

    private static <T extends Block, I extends BlockItem>RegistryObject<Item> registerBlockItem(String name, RegistryObject<T> block, Class<I> blockItemClass) {
        return DragonLibItems.ITEMS.register(name, () -> {
            try {
                return blockItemClass.getDeclaredConstructor(Block.class, Item.Properties.class).newInstance(block.get(), new Item.Properties());
            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
                e.printStackTrace();
                return new BlockItem(block.get(), new Item.Properties());
            }
        });
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }

}

