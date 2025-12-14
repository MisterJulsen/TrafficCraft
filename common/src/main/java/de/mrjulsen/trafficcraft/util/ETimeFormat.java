package de.mrjulsen.trafficcraft.util;

import java.util.Arrays;

import de.mrjulsen.mcdragonlib.util.time.format.ITimeFormatter;
import de.mrjulsen.mcdragonlib.util.time.format.TimeFormat12Hours;
import de.mrjulsen.mcdragonlib.util.time.format.TimeFormat24Hours;
import de.mrjulsen.mcdragonlib.util.time.format.TimeFormatTicks;

public enum ETimeFormat {
    HOURS_24(1, "hours_24", new TimeFormat24Hours()),
    HOURS_12(2, "hours_12", new TimeFormat12Hours()),
    TICKS(0, "ticks", new TimeFormatTicks());

    private final int index;
    private final String name;
    private final ITimeFormatter format;

    private ETimeFormat(int index, String name, ITimeFormatter format) {
        this.index = index;
        this.name = name;
        this.format = format;
    }

    public int getIndex() {
        return index;
    }

    public String getName() {
        return name;
    }

    public ITimeFormatter getFormat() {
        return format;
    }

    public static final ETimeFormat getByIndex(int index) {
        return Arrays.stream(values()).filter(x -> x.getIndex() == index).findFirst().orElse(HOURS_24);
    }
}
