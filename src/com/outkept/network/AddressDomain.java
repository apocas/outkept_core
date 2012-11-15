package com.outkept.network;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author pedrodias
 */
public class AddressDomain {

    public ArrayList<AddressRange> ranges = new ArrayList<AddressRange>();

    public AddressDomain() {
    }

    public void addRange(String ar) {
        String[] aux = ar.split("-");
        try {
            ranges.add(new AddressRange(aux[0], aux[1]));
        } catch (UnknownHostException ex) {
            Logger.getLogger(AddressDomain.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public boolean contains(String ip) throws UnknownHostException {
        for (AddressRange addressRange : ranges) {
            if (addressRange.contains(InetAddress.getByName(ip))) {
                return true;
            }
        }
        return false;
    }
}
