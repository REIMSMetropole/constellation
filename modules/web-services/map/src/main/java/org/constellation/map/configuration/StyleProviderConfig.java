/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2013, Geomatys
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

package org.constellation.map.configuration;

import org.apache.sis.util.Static;
import org.constellation.configuration.ConfigProcessException;
import org.constellation.configuration.ConfigurationException;
import org.constellation.configuration.TargetNotFoundException;
import org.constellation.dto.StyleBean;
import org.constellation.process.ConstellationProcessFactory;
import org.constellation.process.provider.style.DeleteStyleToStyleProviderDescriptor;
import org.constellation.process.provider.style.SetStyleToStyleProviderDescriptor;
import org.constellation.provider.StyleProvider;
import org.constellation.provider.StyleProviderProxy;
import org.geotoolkit.process.ProcessDescriptor;
import org.geotoolkit.process.ProcessException;
import org.geotoolkit.process.ProcessFinder;
import org.geotoolkit.style.MutableStyle;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.util.NoSuchIdentifierException;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Bernard Fabien (Geomatys)
 * @version 0.9
 * @since 0.9
 */
public final class StyleProviderConfig extends Static {

    /**
     * Ensures that a style provider with the specified identifier really exists.
     *
     * @param providerId the style provider identifier
     * @throws TargetNotFoundException if the style provider instance can't be found
     */
    private static void ensureExistingProvider(final String providerId) throws TargetNotFoundException {
        if (StyleProviderProxy.getInstance().getProvider(providerId) == null) {
            throw new TargetNotFoundException("Style provider with identifier \"" + providerId + "\" does not exist.");
        }
    }

    /**
     * Ensures that a style with the specified identifier really exists from the style
     * provider with the specified identifier.
     *
     * @param providerId the style provider identifier
     * @param styleId    the style identifier
     * @throws TargetNotFoundException if the style instance can't be found
     */
    private static void ensureExistingStyle(final String providerId, final String styleId) throws TargetNotFoundException {
        ensureExistingProvider(providerId);
        if (!StyleProviderProxy.getInstance().getProvider(providerId).contains(styleId)) {
            throw new TargetNotFoundException("Style provider with identifier \"" + providerId + "\" does not contain style named \"" + styleId + "\".");
        }
    }

    /**
     * Creates a new style into a style provider instance.
     *
     * @param providerId the style provider identifier
     * @param style      the style body
     * @throws TargetNotFoundException if the style with the specified identifier can't be found
     * @throws ConfigurationException if the operation has failed for any reason
     */
    public static void createStyle(final String providerId, final MutableStyle style) throws ConfigurationException {
        ensureExistingProvider(providerId);
        setStyle(providerId, style.getName(), style);
    }

    /**
     * Returns the list of available styles as {@link StyleBean} object.
     *
     * @return a {@link List} of {@link StyleBean} instances
     */
    public static List<StyleBean> getAvailableStyles() {
        final List<StyleBean> beans = new ArrayList<>();
        for (final StyleProvider provider : StyleProviderProxy.getInstance().getProviders()) {
            if ("GO2".equals(provider.getId())) {
                continue; // skip "GO2" provider
            }
            try {
                beans.addAll(getAvailableStyles(provider.getId()));
            } catch (TargetNotFoundException ignore) { // couldn't happen
            }
        }
        return beans;
    }

    /**
     * Returns the list of available styles as {@link StyleBean} object for the style
     * provider with the specified identifier.
     *
     * @throws TargetNotFoundException if the style provider does not exist
     * @return a {@link List} of {@link StyleBean} instances
     */
    public static List<StyleBean> getAvailableStyles(final String providerId) throws TargetNotFoundException {
        ensureExistingProvider(providerId);
        final StyleProvider provider = StyleProviderProxy.getInstance().getProvider(providerId);
        final String folder = provider.getSource().groups("sldFolder").get(0).parameter("path").stringValue();

        final List<StyleBean> beans = new ArrayList<>();
        for (final String key : provider.getKeys()) {
            final StyleBean bean = new StyleBean();
            bean.setName(key);
            bean.setProviderId(providerId);
            if (new File(folder, key + ".xml").exists()) {
                bean.setDate(new Date(new File(folder, key + ".xml").lastModified()));
            } else if (new File(folder, key + ".sld").exists()) {
                bean.setDate(new Date(new File(folder, key + ".sld").lastModified()));
            }
            beans.add(bean);
        }
        return beans;
    }

    /**
     * Returns a style metadata.
     *
     * TODO: find a way to attach metadata to created style.
     *
     * @throws TargetNotFoundException if the style with the specified identifier can't be found
     * @return a {@link StyleBean} instance
     */
    public static Object getStyleMetadata(final String providerId, final String styleId) throws TargetNotFoundException {
        ensureExistingStyle(providerId, styleId);
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    /**
     * Gets and returns the {@link MutableStyle} that matches with the specified identifier.
     *
     * @param providerId the style provider identifier
     * @param styleId    the style identifier
     * @return the {@link MutableStyle} instance
     * @throws TargetNotFoundException if the style with the specified identifier can't be found
     */
    public static MutableStyle getStyle(final String providerId, final String styleId) throws TargetNotFoundException {
        ensureExistingStyle(providerId, styleId);
        return StyleProviderProxy.getInstance().getProvider(providerId).get(styleId);
    }

    /**
     * Updates an existing from a style provider instance.
     *
     * @param providerId the style provider identifier
     * @param styleId    the style identifier
     * @param style      the new style body
     * @throws TargetNotFoundException if the style with the specified identifier can't be found
     * @throws ConfigurationException if the operation has failed for any reason
     */
    public static void setStyle(final String providerId, final String styleId, final MutableStyle style) throws ConfigurationException {
        ensureExistingStyle(providerId, styleId);
        final ProcessDescriptor desc = getProcessDescriptor(SetStyleToStyleProviderDescriptor.NAME);
        final ParameterValueGroup inputs = desc.getInputDescriptor().createValue();
        inputs.parameter(SetStyleToStyleProviderDescriptor.PROVIDER_ID_NAME).setValue(providerId);
        inputs.parameter(SetStyleToStyleProviderDescriptor.STYLE_ID_NAME).setValue(styleId);
        inputs.parameter(SetStyleToStyleProviderDescriptor.STYLE_NAME).setValue(style);
        try {
            desc.createProcess(inputs).call();
        } catch (ProcessException ex) {
            throw new ConfigProcessException("Process to add/set style has reported an error.", ex);
        }
    }

    /**
     * Removes a style from a style provider instance.
     *
     * @param providerId the style provider identifier
     * @param styleId    the style identifier
     * @throws TargetNotFoundException if the style with the specified identifier can't be found
     * @throws ConfigurationException if the operation has failed for any reason
     */
    public static void deleteStyle(final String providerId, final String styleId) throws ConfigurationException {
        ensureExistingStyle(providerId, styleId);
        final ProcessDescriptor desc = getProcessDescriptor(DeleteStyleToStyleProviderDescriptor.NAME);
        final ParameterValueGroup inputs = desc.getInputDescriptor().createValue();
        inputs.parameter(DeleteStyleToStyleProviderDescriptor.PROVIDER_ID_NAME).setValue(providerId);
        inputs.parameter(DeleteStyleToStyleProviderDescriptor.STYLE_ID_NAME).setValue(styleId);
        try {
            desc.createProcess(inputs).call();
        } catch (ProcessException ex) {
            throw new ConfigProcessException("Process to delete a style has reported an error.", ex);
        }
    }

    /**
     * Returns a Constellation {@link ProcessDescriptor} from its name.
     *
     * @param name the process descriptor name
     * @return a {@link ProcessDescriptor} instance
     */
    private static ProcessDescriptor getProcessDescriptor(final String name) {
        try {
            return ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, name);
        } catch (NoSuchIdentifierException ex) { // unexpected
            throw new IllegalStateException("Unexpected error has occurred", ex);
        }
    }
}
