package de.mrjulsen.trafficcraft.block;

import de.mrjulsen.trafficcraft.block.colors.IPaintableBlock;
import de.mrjulsen.trafficcraft.block.properties.RoadType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Material;

public class AsphaltBlock extends Block implements IPaintableBlock {

    private RoadType defaultRoadType;

    public AsphaltBlock(RoadType type) {
        super(Properties.of(Material.STONE)
            .strength(1.5f)
            .requiresCorrectToolForDrops()
        );

        this.defaultRoadType = type;
    }

    public RoadType getDefaultRoadType() {
        return this.defaultRoadType;
    }    
}
