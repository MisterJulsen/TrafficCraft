package de.mrjulsen.trafficcraft;

import java.util.HashMap;

import de.mrjulsen.trafficcraft.block.properties.TrafficSignShape;

public class Constants {
    public static final HashMap<TrafficSignShape, Integer> SIGN_PATTERNS = new HashMap<>() {{
        put(TrafficSignShape.CIRCLE, 133);
        put(TrafficSignShape.SQUARE, 105);
        put(TrafficSignShape.TRIANGLE, 96);
        put(TrafficSignShape.DIAMOND, 115);
        put(TrafficSignShape.RECTANGLE, 84);
        put(TrafficSignShape.MISC, 78);
    }};

    public static final int MAX_TRAFFIC_SIGN_STATES = 134;
    public static final int MAX_ASPHALT_PATTERNS = 323;
    public static final int MAX_PAINT = 100;
    public static final int TICKS_PER_DAY = 24000;

    public static final int METAL_COLOR = 0xFF828282;
    public static final int TRAFFIC_CONE_BASE_COLOR = 0xFFD12725;
}
