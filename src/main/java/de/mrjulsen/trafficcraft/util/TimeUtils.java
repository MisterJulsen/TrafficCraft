package de.mrjulsen.trafficcraft.util;

import de.mrjulsen.trafficcraft.Constants;
import de.mrjulsen.trafficcraft.block.properties.TimeFormat;

public class TimeUtils {

    public static int shiftTimeToMinecraftTicks(int time) {
        time = (time - 6000) % Constants.TICKS_PER_DAY;
        if (time < 0) {
            time += Constants.TICKS_PER_DAY;
        }
        return time;
    }

    public static String parseTime(int time, TimeFormat format) {
        if (format == TimeFormat.TICKS) {
            return TimeUtils.shiftTimeToMinecraftTicks(time) + "t";
        }

        time = time % Constants.TICKS_PER_DAY;
        int hours = time / 1000;
        int minutes = time % 1000;
        minutes = (int)Math.round(minutes / (1000.0D / 60.0D));
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

    public static boolean isInRange(int time, int start, int end) {
        time = time % Constants.TICKS_PER_DAY;
        start = start % Constants.TICKS_PER_DAY;
        end = end % Constants.TICKS_PER_DAY;
        if (start <= end) {
            return time >= start && time <= end;
        } else {
            return time >= start || time <= end;
        }
    }
    
}
