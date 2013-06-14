/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2012, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 3 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */

package org.constellation.gui;

import junit.framework.AssertionFailedError;
import juzu.arquillian.Helper;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.io.File;
import java.net.URL;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

/**
 * Main junit test case with arquillian and selenium
 *
 * @author Benjamin Garcia (Geomatys)
 * @version 0.9
 * @since 0.9
 *
 */
@RunWith(Arquillian.class)
public class CstlClientTestCase {

    /**
     * Initialise war deployment for test case
     * @return a war file object
     */
    @Deployment
    public static WebArchive createDeployment() {
        WebArchive war = Helper.createBaseServletDeployment("spring");
        war.addAsWebInfResource(new File("src/test/resources/spring.xml"));
        war.addPackages(true, "org.constellation.gui");
        return war;
    }

    /**
     * driver to find object on web page
     */
    @Drone
    WebDriver driver;

    /**
     * root url deployment
     */
    @ArquillianResource
    URL deploymentURL;


    /**
     * Method to find application {@link URL}
     * @param application application name
     * @return application {@link URL}
     */
    public URL getApplicationURL(String application) {
        try {
            return deploymentURL.toURI().resolve(application).toURL();
        } catch (Exception e) {
            AssertionFailedError afe = new AssertionFailedError("Could not build URL");
            afe.initCause(e);
            throw afe;
        }
    }


    /**
     * Main menu bar test case. Use to verify all links in menu
     */
    @Test
    @RunAsClient
    public void testNavBar() {
        driver.get(deploymentURL.toString());
        WebElement nav = driver.findElement(By.className("nav"));
        assertEquals("Homepage Web services Datas Styles Map context Geoviewer Administration", nav.getText());
        List<WebElement> navLinks = nav.findElements(By.tagName("a"));
        for (WebElement navLink : navLinks) {
            String href = navLink.getAttribute("href");

            if (navLink.getText().equals("HomePage")) {
                assertEquals(href, deploymentURL.toString());
            }
            if (navLink.getText().equals("Web services")) {
                assertEquals(href, deploymentURL.toString() + "webservices");
            }
//            TODO : to continue with all pages
        }
    }

    /**
     * Test Homepage links with found a {@link WebElement} an next navigation page.
     */
    @Test
    @RunAsClient
    public void testHomePageLinks(){
        driver.get(deploymentURL.toString());
        WebElement serviceBtn = driver.findElement(By.id("servicebtn"));
        serviceBtn.click();
        WebElement createService = driver.findElement(By.id("createservice"));

        //if not null => button exist we are go on an other page...
        assertNotNull(createService);
    }

    /**
     * test wms creation page navigation
     */
    @Test
    @RunAsClient
    public void testCreateWMS(){
        driver.get(deploymentURL.toString()+"webservices");
        WebElement createService = driver.findElement(By.id("createservice"));
        assertNotNull(createService);

        //wms button test visibility
        WebElement wmschoice = driver.findElement(By.id("wmschoice"));
        assertFalse(wmschoice.isDisplayed());
        createService.click();
        assertTrue(wmschoice.isDisplayed());

        //go to form first page
        wmschoice.click();

//        TODO continue test
    }
}