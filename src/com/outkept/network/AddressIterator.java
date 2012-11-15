package com.outkept.network;

import java.util.Arrays;

/**
 *
 * @author pedrodias
 */
public class AddressIterator {

    private int[] start = new int[4];
    private int[] end = new int[4];

    public AddressIterator(String range) {
        String rs[] = range.trim().split("-");
        this.start = convertAddress(rs[0]);
        this.end = convertAddress(rs[1]);
    }

    public AddressIterator(String start, String end) {
        this.start = convertAddress(start);
        this.end = convertAddress(end);
    }

    private int[] convertAddress(String address) {
        String ee[] = address.trim().split("\\.");
        int[] rr = new int[4];
        for (int i = 0; i < 4; i++) {
            rr[i] = Integer.parseInt(ee[i]);
        }
        return rr;
    }

    public boolean hasNext() {
        if (Arrays.equals(this.start, this.end)) {
            return false;
        } else {
            return true;
        }
    }

    public String next() {
        if (this.hasNext()) {
            start[3] = (++start[3] % 256);
            if (start[3] == 0) {
                start[2] = (++start[2] % 256);
            }
        }
        return start[0] + "." + start[1] + "." + start[2] + "." + start[3];
    }
}
