package com.outkept.network;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 *
 * @author pedrodias
 */
public class AddressRange {

    InetAddress begin;
    InetAddress end;

    public AddressRange(String begin, String end) throws UnknownHostException {
        this.begin = InetAddress.getByName(begin);
        this.end = InetAddress.getByName(end);
    }

    @Override
    public String toString() {
        return begin.getHostAddress() + "-" + end.getHostAddress();
    }

    public AddressIterator getIterator() {
        return new AddressIterator(begin.getHostAddress(), end.getHostAddress());
    }

    public boolean contains(InetAddress ip) {
        long c = this.ip2long(ip);
        long b = this.ip2long(this.begin);
        long e = this.ip2long(this.end);

        if (c >= b && c <= e) {
            return true;
        } else {
            return false;
        }
    }

    private long ip2long(InetAddress ip) {
        long l = 0;
        byte[] addr = ip.getAddress();

        if (addr.length == 4) {
            for (int i = 0; i < 4; ++i) {
                l += (((long) addr[i] & 0xFF) << 8 * (3 - i));
            }
        } else { //IPV6 todo
            return 0;
        }
        return l;
    }
}
