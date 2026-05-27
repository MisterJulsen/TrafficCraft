package de.mrjulsen.trafficcraft.util;

import java.util.Comparator;

public class NaturalOrderComparator implements Comparator<String> {

    @Override
    public int compare(String a, String b) {
        int ia = 0, ib = 0;
        int nza, nzb;
        char ca, cb;

        while (true) {
            nza = nzb = 0;

            ca = charAt(a, ia);
            cb = charAt(b, ib);

            while (Character.isDigit(ca) && ca == '0') {
                nza++;
                ia++;
                ca = charAt(a, ia);
            }
            while (Character.isDigit(cb) && cb == '0') {
                nzb++;
                ib++;
                cb = charAt(b, ib);
            }

            if (Character.isDigit(ca) && Character.isDigit(cb)) {
                int result = compareRight(a.substring(ia), b.substring(ib));
                if (result != 0) {
                    return result;
                }
            }

            if (ca == 0 && cb == 0) {
                return nza - nzb;
            }
            if (ca < cb) {
                return -1;
            }
            if (ca > cb) {
                return +1;
            }

            ia++;
            ib++;
        }
    }

    private static int compareRight(String a, String b) {
        int ia = 0, ib = 0;
        int bias = 0;

        while (true) {
            char ca = charAt(a, ia);
            char cb = charAt(b, ib);

            if (!Character.isDigit(ca) && !Character.isDigit(cb)) {
                return bias;
            }
            if (!Character.isDigit(ca)) {
                return -1;
            }
            if (!Character.isDigit(cb)) {
                return +1;
            }
            if (ca < cb) {
                if (bias == 0) bias = -1;
            } else if (ca > cb) {
                if (bias == 0) bias = +1;
            } else if (ca == 0 && cb == 0) {
                return bias;
            }

            ia++;
            ib++;
        }
    }

    private static char charAt(String s, int i) {
        return i >= s.length() ? 0 : s.charAt(i);
    }
}
