/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2012, Geomatys
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
package org.constellation.process.style.update;

import java.io.File;
import java.net.MalformedURLException;
import org.constellation.process.ConstellationProcessFactory;
import org.constellation.process.style.AbstractMapStyleTest;
import org.constellation.provider.*;
import org.geotoolkit.process.ProcessDescriptor;
import org.geotoolkit.process.ProcessException;
import org.geotoolkit.process.ProcessFinder;
import org.geotoolkit.style.*;
import org.geotoolkit.util.FileUtilities;
import static org.junit.Assert.*;
import org.junit.Test;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.util.NoSuchIdentifierException;

/**
 *
 * @author Quentin Boileau (Geomatys).
 */
public class UpdateMapStyleTest extends AbstractMapStyleTest {

    public UpdateMapStyleTest() {
        super(UpdateMapStyleDescriptor.NAME);
    }

    @Test
    public void testCreateStyle() throws ProcessException, NoSuchIdentifierException, MalformedURLException {

        addProvider(buildProvider("updateStyleProvider1", true));

        final ProcessDescriptor desc = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, UpdateMapStyleDescriptor.NAME);

        final MutableStyleFactory msf = new DefaultStyleFactory();
        final MutableStyle style1 = msf.style(StyleConstants.DEFAULT_LINE_SYMBOLIZER);

         for (StyleProvider p : StyleProviderProxy.getInstance().getProviders()) {
            if (p.getId().equals("updateStyleProvider1")) {
                p.set("styleToUpdate", style1);
            }
        }

        final MutableStyle newStyle = msf.style(StyleConstants.DEFAULT_POLYGON_SYMBOLIZER);

        final ParameterValueGroup in = desc.getInputDescriptor().createValue();
        in.parameter(UpdateMapStyleDescriptor.PROVIDER_ID_NAME).setValue("updateStyleProvider1");
        in.parameter(UpdateMapStyleDescriptor.STYLE_ID_NAME).setValue("styleToUpdate");
        in.parameter(UpdateMapStyleDescriptor.STYLE_NAME).setValue(newStyle);

        desc.createProcess(in).call();

        Provider provider = null;
        for (StyleProvider p : StyleProviderProxy.getInstance().getProviders()) {
            if ("updateStyleProvider1".equals(p.getId())){
                provider = p;
            }
        }
        assertNotNull(provider);
        final File newFile = new File(configDirectory.getAbsolutePath()+"/sldDir/", "styleToUpdate.xml");
        assertTrue(newFile.exists());
        assertTrue(provider.contains("styleToUpdate"));

        removeProvider("updateStyleProvider1");
    }

    /**
     * Provider doesn't exist.
     */
    @Test
    public void testFailCreateStyle1() throws ProcessException, NoSuchIdentifierException, MalformedURLException {

        final ProcessDescriptor desc = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, UpdateMapStyleDescriptor.NAME);

        final MutableStyleFactory msf = new DefaultStyleFactory();
        final MutableStyle style = msf.style(StyleConstants.DEFAULT_LINE_SYMBOLIZER);

        final ParameterValueGroup in = desc.getInputDescriptor().createValue();
        in.parameter(UpdateMapStyleDescriptor.PROVIDER_ID_NAME).setValue("updateStyleProvider2");
        in.parameter(UpdateMapStyleDescriptor.STYLE_ID_NAME).setValue("myStyle");
        in.parameter(UpdateMapStyleDescriptor.STYLE_NAME).setValue(style);

        try {
            desc.createProcess(in).call();
            fail();
        } catch (ProcessException ex) {
            //do nothing
        }
    }

    /**
     * Empty provider name
     */
    @Test
    public void testFailCreateStyle2() throws ProcessException, NoSuchIdentifierException, MalformedURLException {

        final ProcessDescriptor desc = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, UpdateMapStyleDescriptor.NAME);

        final MutableStyleFactory msf = new DefaultStyleFactory();
        final MutableStyle style = msf.style(StyleConstants.DEFAULT_LINE_SYMBOLIZER);

        final ParameterValueGroup in = desc.getInputDescriptor().createValue();
        in.parameter(UpdateMapStyleDescriptor.PROVIDER_ID_NAME).setValue("");
        in.parameter(UpdateMapStyleDescriptor.STYLE_ID_NAME).setValue("myStyle");
        in.parameter(UpdateMapStyleDescriptor.STYLE_NAME).setValue(style);

        try {
            desc.createProcess(in).call();
            fail();
        } catch (ProcessException ex) {
            //do nothing
        }
    }

    /**
     * Empty style name
     */
    @Test
    public void testFailCreateStyle3() throws ProcessException, NoSuchIdentifierException, MalformedURLException {

        addProvider(buildProvider("updateStyleProvider3", true));

        final ProcessDescriptor desc = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, UpdateMapStyleDescriptor.NAME);

        final MutableStyleFactory msf = new DefaultStyleFactory();
        final MutableStyle style = msf.style(StyleConstants.DEFAULT_LINE_SYMBOLIZER);

        final ParameterValueGroup in = desc.getInputDescriptor().createValue();
        in.parameter(UpdateMapStyleDescriptor.PROVIDER_ID_NAME).setValue("updateStyleProvider3");
        in.parameter(UpdateMapStyleDescriptor.STYLE_ID_NAME).setValue("");
        in.parameter(UpdateMapStyleDescriptor.STYLE_NAME).setValue(style);

        try {
            desc.createProcess(in).call();
            fail();
        } catch (ProcessException ex) {
            //do nothing
        }

        removeProvider("updateStyleProvider3");
    }
}
