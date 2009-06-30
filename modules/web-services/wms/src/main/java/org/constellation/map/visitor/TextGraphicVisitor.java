/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2005, Institut de Recherche pour le Développement
 *    (C) 2007 - 2008, Geomatys
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
package org.constellation.map.visitor;

import org.constellation.query.wms.GetFeatureInfo;
import org.geotoolkit.display2d.canvas.AbstractGraphicVisitor;


/**
 * Abstract graphic visitor designed to handle text output format.
 *
 * @author Johann Sorel (Geomatys)
 * @author Cédric Briançon (Geomatys)
 *
 * @see AbstractGraphicVisitor
 */
public abstract class TextGraphicVisitor extends AbstractGraphicVisitor {
    /**
     * The GetFeatureInfo WMS request.
     */
    protected final GetFeatureInfo gfi;

    /**
     * Instanciates this abstract graphic visitor with the GetFeatureInfo request specified.
     *
     * @param gfi A GetFeatureInfo request.
     */
    protected TextGraphicVisitor(final GetFeatureInfo gfi) {
        if (gfi == null) {
            throw new NullPointerException("GetFeatureInfo Object can not be null");
        }
        this.gfi = gfi;
    }

    /**
     * Method that have to be called in order to get the output result of the GetFeatureInfo request.
     *
     * @return A text representing the result, depending on the output format chosen.
     */
    public abstract String getResult();
}
