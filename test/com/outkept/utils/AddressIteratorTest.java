/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.outkept.utils;

import com.outkept.network.AddressIterator;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author pedrodias
 */
public class AddressIteratorTest {

    @Test
    public void testNext() {
        String end = "192.168.20.200";
        String run = null;
        AddressIterator ai = new AddressIterator("192.168.0.2", end);
        while (ai.hasNext()) {
            run = ai.next();
            System.out.println(run);
        }
        assertEquals(run, end);
    }
}
