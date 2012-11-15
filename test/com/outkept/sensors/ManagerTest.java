/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.outkept.sensors;

import com.outkept.TemplateManager;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author pedrodias
 */
public class ManagerTest {

    public ManagerTest() {
    }

    @Test
    public void testGetSensor() throws Exception {
        TemplateManager mng = new TemplateManager();
        LoadedSensor aux = mng.getSensor("load");
        System.out.println(aux);

        assertEquals(aux.getName(), "load");
    }
}
