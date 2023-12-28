package de.mrjulsen.trafficcraft.block.data;

import de.mrjulsen.mcdragonlib.client.gui.Sprite;
import de.mrjulsen.trafficcraft.ModMain;
import net.minecraft.resources.ResourceLocation;

public interface IIconEnum {

	public static final ResourceLocation ICON_TEXTURE_LOCATION = new ResourceLocation(ModMain.MOD_ID, "textures/gui/icons.png");
	public static final int TEXTURE_SIZE = 128;
	public static final int DEFAULT_SPRITE_SIZE = 16;

    int getUMultiplier();
    int getVMultiplier();

    default Sprite getSprite() {
        return new Sprite(ICON_TEXTURE_LOCATION, TEXTURE_SIZE, TEXTURE_SIZE, DEFAULT_SPRITE_SIZE * getUMultiplier(), DEFAULT_SPRITE_SIZE * getVMultiplier(), DEFAULT_SPRITE_SIZE, DEFAULT_SPRITE_SIZE);
    }
}
