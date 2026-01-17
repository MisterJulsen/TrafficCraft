package de.mrjulsen.trafficcraft.util;

import java.util.Arrays;

import de.mrjulsen.mcdragonlib.data.ITranslatableEnum;
import de.mrjulsen.mcdragonlib.util.time.format.ITimeFormatter;
import de.mrjulsen.mcdragonlib.util.time.format.TimeFormat12Hours;
import de.mrjulsen.mcdragonlib.util.time.format.TimeFormat24Hours;
import de.mrjulsen.mcdragonlib.util.time.format.TimeFormatTicks;
import de.mrjulsen.trafficcraft.TrafficCraft;

public enum ETimeFormat implements ITranslatableEnum {
    HOURS_24(1, "hours_24", TimeFormat24Hours.INSTANCE),
    HOURS_12(2, "hours_12", TimeFormat12Hours.INSTANCE),
    TICKS(0, "ticks", TimeFormatTicks.INSTANCE);

    private final int index;
    private final String name;
    private final ITimeFormatter format;

    ETimeFormat(int index, String name, ITimeFormatter format) {
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

    public static ETimeFormat getByIndex(int index) {
        return Arrays.stream(values()).filter(x -> x.getIndex() == index).findFirst().orElse(HOURS_24);
    }

    @Override
    public Data getTranslationData() {
        return new Data(TrafficCraft.MOD_ID, "time_format", name);
    }
}
