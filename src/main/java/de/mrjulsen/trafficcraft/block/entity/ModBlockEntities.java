package de.mrjulsen.trafficcraft.block.entity;

import de.mrjulsen.trafficcraft.ModMain;
import de.mrjulsen.trafficcraft.block.ModBlocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlockEntities {
    
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITIES, ModMain.MOD_ID);


    public static final RegistryObject<BlockEntityType<TrafficLightBlockEntity>> TRAFFIC_LIGHT_BLOCK_ENTITY = BLOCK_ENTITIES.register("traffic_light_block_entity", () -> BlockEntityType.Builder.of(TrafficLightBlockEntity::new, ModBlocks.TRAFFIC_LIGHT.get()).build(null));
    public static final RegistryObject<BlockEntityType<TrafficLightControllerBlockEntity>> TRAFFIC_LIGHT_CONTROLLER_BLOCK_ENTITY = BLOCK_ENTITIES.register("traffic_light_controller_block_entity", () -> BlockEntityType.Builder.of(TrafficLightControllerBlockEntity::new, ModBlocks.TRAFFIC_LIGHT_CONTROLLER.get()).build(null));
    public static final RegistryObject<BlockEntityType<TrafficLightRequestButtonBlockEntity>> TRAFFIC_LIGHT_REQUEST_BUTTON_BLOCK_ENTITY = BLOCK_ENTITIES.register("traffic_light_request_button_block_entity", () -> BlockEntityType.Builder.of(TrafficLightRequestButtonBlockEntity::new, ModBlocks.TRAFFIC_LIGHT_REQUEST_BUTTON.get()).build(null));
    

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
