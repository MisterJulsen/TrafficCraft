package de.mrjulsen.trafficcraft.client;

import java.util.Arrays;

import de.mrjulsen.mcdragonlib.client.atlas.DLTextureSheetData;
import de.mrjulsen.mcdragonlib.client.atlas.DLTextureSheetData.StretchedSprite;
import de.mrjulsen.mcdragonlib.client.render.DLTextureSheet;
import de.mrjulsen.mcdragonlib.client.util.DLGuiGraphics;
import de.mrjulsen.mcdragonlib.client.util.DLSprite;
import de.mrjulsen.mcdragonlib.client.util.DLTexture;
import de.mrjulsen.mcdragonlib.util.DLUtils;
import de.mrjulsen.trafficcraft.TrafficCraft;
import net.minecraft.resources.ResourceLocation;

public enum ModGuiIcons {
    TRAFFIC_LIGHT("traffic_light"),
    TRAM_TRAFFIC_LIGHT("tram_traffic_light"),
    TRAFFIC_LIGHT_1_LIGHT("traffic_light_one_light"),
    TRAFFIC_LIGHT_2_LIGHTS("traffic_light_two_lights"),
    TRAFFIC_LIGHT_3_LIGHTS("traffic_light_three_lights"),
    TRAFFIC_LIGHT_4_LIGHTS("traffic_light_four_lights"),
    TRAFFIC_LIGHT_NO_ICON("traffic_light_no_icon"),
    TRAFFIC_LIGHT_RIGHT("traffic_light_right"),
    TRAFFIC_LIGHT_LEFT("traffic_light_left"),
    TRAFFIC_LIGHT_UP("traffic_light_up"),
    TRAFFIC_LIGHT_UP_RIGHT("traffic_light_up_right"),
    TRAFFIC_LIGHT_UP_LEFT("traffic_light_up_left"),
    TRAFFIC_LIGHT_PEDESTRIAN("traffic_light_pedestrian"),
    TRAFFIC_LIGHT_BIKE("traffic_light_bike"),
    TRAFFIC_LIGHT_TRAM_ICON("traffic_light_tram_icon"),
    TRAFFIC_LIGHT_TRAM_RIGHT("traffic_light_tram_right"),
    TRAFFIC_LIGHT_TRAM_LEFT("traffic_light_tram_left"),
    COPY("copy"),
    PASTE("paste"),
    HELP("help"),
    CHECK("check"),
    CANCEL("cancel"),
    EDIT("edit"),
    ERASE("erase"),
    PICK("pick"),
    TEXT("text"),
    FILL("fill"),
    DELETE("delete"),
    PATTERN("pattern"),
    ADD("add"),
    ADD_BULLET("add_bullet"),
    SAVE("save"),
    OPEN("open"),
    WRITE_TO_FILE("write_to_file"),
    DISCARD_FILE("discard_file"),
    DELETE_WHITE("delete_white"),
    MOVE_DOWN("move_down"),
    MOVE_UP("move_up");

    private String id;

    public static final int ICON_SIZE = 16;
    public static final DLTextureSheet ICONS = new DLTextureSheet(DLUtils.resourceLocation(TrafficCraft.MOD_ID, "textures/gui/icons.png"));

    ModGuiIcons(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public static ModGuiIcons getByStringId(String id) {
        return Arrays.stream(values()).filter(x -> x.getId().equals(id)).findFirst().orElse(ModGuiIcons.TRAFFIC_LIGHT_NO_ICON);
    }

    public void render(DLGuiGraphics graphics, int x, int y) {
        if (ICONS.getSprite(id) instanceof StretchedSprite sprite) {
            sprite.render(graphics, x, y, sprite.width(), sprite.height());
        }
    }
    
    public DLSprite getAsSprite(int renderWidth, int renderHeight) {
        DLTextureSheetData metadata = ICONS.getSprite(id).metadata();
        if (!(ICONS.getSprite(id) instanceof StretchedSprite sprite)) {
            return DLSprite.empty();
        }
        DLTexture texture = new DLTexture(metadata.location(), metadata.width(), metadata.height());
        return new DLSprite(texture, renderWidth, renderHeight, sprite.u(), sprite.v(), sprite.width(), sprite.height());
    }
}
