package de.mrjulsen.legacydragonlib.utils;

import de.mrjulsen.legacydragonlib.DragonLibConstants;
import de.mrjulsen.legacydragonlib.common.ITranslatableEnum;
import net.minecraft.util.StringRepresentable;

public final class TimeUtils {

    public static int shiftDayTimeToMinecraftTicks(int time) {
        time = (time - 6000) % DragonLibConstants.TICKS_PER_DAY;
        if (time < 0) {
            time += DragonLibConstants.TICKS_PER_DAY;
        }
        return time;
    }

    public static String parseTime(int time, TimeFormat format) {
        if (format == TimeFormat.TICKS) {
            return TimeUtils.shiftDayTimeToMinecraftTicks(time) + "t";
        }

        time = time % DragonLibConstants.TICKS_PER_DAY;
        int hours = time / 1000;
        int minutes = time % 1000;
        minutes = (int)java.lang.Math.round(minutes / (1000.0D / 60.0D));
        if (format == TimeFormat.HOURS_24) {
            return String.format("%02d:%02d", hours, minutes);

        } else if (format == TimeFormat.HOURS_12) {
            String suffix = "AM";
            if (hours >= 12) {
                suffix = "PM";
                hours -= 12;
            }
            if (hours == 0) {
                hours = 12;
            }
        
            return String.format("%02d:%02d %s", hours, minutes, suffix);
        }
        return "";
    }

    public static String parseDuration(int time) {
        if (time < 0) {
            return "-";
        }

        time = time % DragonLibConstants.TICKS_PER_DAY;
        int days = time / DragonLibConstants.TICKS_PER_DAY;
        int hours = time / 1000;
        int minutes = time % 1000;
        minutes = (int)(minutes / (1000.0D / 60.0D));
        if (hours <= 0 && days <= 0) { 
            return Utils.translate(DragonLibConstants.DRAGONLIB_MODID + ".time_format.m", minutes).getString();
        } else if (days <= 0) { 
            return Utils.translate(DragonLibConstants.DRAGONLIB_MODID + ".time_format.hm", hours, minutes).getString();
        } else { 
            return Utils.translate(DragonLibConstants.DRAGONLIB_MODID + ".time_format.dhm", days, hours, minutes).getString();
        }
    }

    public static String parseDurationShort(int time) {        
        if (time < 0) {
            return "-";
        }

        int days = time / DragonLibConstants.TICKS_PER_DAY;
        time = time % DragonLibConstants.TICKS_PER_DAY;
        int hours = time / 1000;
        int minutes = time % 1000;
        minutes = (int)(minutes / (1000.0D / 60.0D));
        if (hours <= 0 && days <= 0) { 
            return String.format("%sm", minutes);
        } else if (days <= 0) { 
            return String.format("%sh %sm", hours, minutes);
        } else { 
            return String.format("%sd %sh %sm", days, hours, minutes);
        }
    }

    public static boolean isInRange(int time, int start, int end) {
        time = time % DragonLibConstants.TICKS_PER_DAY;
        start = start % DragonLibConstants.TICKS_PER_DAY;
        end = end % DragonLibConstants.TICKS_PER_DAY;
        if (start <= end) {
            return time >= start && time <= end;
        } else {
            return time >= start || time <= end;
        }
    }

    public static enum TimeFormat implements StringRepresentable, ITranslatableEnum {
        TICKS(0, "ticks"),
        HOURS_24(1, "hours_24"),
        HOURS_12(2, "hours_12");
        
        private String format;
        private int index;
        
        private TimeFormat(int index, String format) {
            this.format = format;
            this.index = index;
        }
        
        public String getFormat() {
            return this.format;
        }

        public int getIndex() {
            return this.index;
        }

        public String getTranslationKey() {
            return String.format("%s.time_format.%s", DragonLibConstants.DRAGONLIB_MODID, format);
        }

        public static TimeFormat getFormatByIndex(int index) {
            for (TimeFormat shape : TimeFormat.values()) {
                if (shape.getIndex() == index) {
                    return shape;
                }
            }
            return TimeFormat.TICKS;
        }

        @Override
        public String getSerializedName() {
            return this.format;
        }

        @Override
        public String getEnumName() {
            return "time_format";
        }

        @Override
        public String getEnumValueName() {
            return getFormat();
        }
    }    
}

