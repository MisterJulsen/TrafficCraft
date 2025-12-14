package de.mrjulsen.trafficcraft.block.data;

import de.mrjulsen.mcdragonlib.client.util.DLSprite;
import de.mrjulsen.mcdragonlib.client.util.DLTexture;
import de.mrjulsen.mcdragonlib.util.DLUtils;
import de.mrjulsen.trafficcraft.TrafficCraft;

public interface IIconEnum {

	public static final DLTexture ICON_TEXTURE = new DLTexture(DLUtils.resourceLocation(TrafficCraft.MOD_ID, "textures/gui/icons.png"), 256, 256);
	public static final int DEFAULT_SPRITE_SIZE = 16;

    int getUMultiplier();
    int getVMultiplier();

    default DLSprite getSprite() {
        return new DLSprite(ICON_TEXTURE, DEFAULT_SPRITE_SIZE, DEFAULT_SPRITE_SIZE, DEFAULT_SPRITE_SIZE * getUMultiplier(), DEFAULT_SPRITE_SIZE * getVMultiplier(), DEFAULT_SPRITE_SIZE, DEFAULT_SPRITE_SIZE);
    }
}
