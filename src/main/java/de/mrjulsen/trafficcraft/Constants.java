package de.mrjulsen.trafficcraft;

import java.util.HashMap;

import de.mrjulsen.trafficcraft.block.properties.TrafficSignShape;

public class Constants {
    public static final HashMap<TrafficSignShape, Integer> SIGN_PATTERNS = new HashMap<>() {{
        put(TrafficSignShape.CIRCLE, 113);
        put(TrafficSignShape.SQUARE, 106);
        put(TrafficSignShape.TRIANGLE, 97);
        put(TrafficSignShape.DIAMOND, 116);
        put(TrafficSignShape.RECTANGLE, 85);
        put(TrafficSignShape.MISC, 76);
    }};

    public static final int MAX_TRAFFIC_SIGN_STATES = 116;
    public static final int MAX_ASPHALT_PATTERNS = 323;
    public static final int MAX_PAINT = 64;

    public static final int METAL_COLOR = 0xFF828282;
    public static final int TRAFFIC_CONE_BASE_COLOR = 0xFFD12725;
}
