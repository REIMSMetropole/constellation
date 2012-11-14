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
package org.constellation.configuration;

import java.awt.Dimension;
import java.awt.Font;
import java.io.Serializable;
import javax.xml.bind.annotation.*;
import org.geotoolkit.display2d.ext.BackgroundTemplate;
import org.geotoolkit.display2d.ext.legend.DefaultLegendTemplate;

/**
 * WMS Portrayal legend template configuration.
 * 
 * @author Quentin Boileau (Geomatys).
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class LegendTemplate implements Serializable {
    
    @XmlElement(name="Background")
    private Background background;
    
    @XmlElement(name="Gap")
    private Float gap;
    
    @XmlElement(name="MainFont")
    private String mainFont;
    
    @XmlElement(name="SecondFont")
    private String secondFont;
    
    @XmlElement(name="LayerVisible")
    private Boolean layerVisible;

    @XmlElement(name="GlyphWidth")
    private Integer glyphWidth;
    
    @XmlElement(name="GlyphHeight")
    private Integer glyphHeight;

    /**
     * Default LegendTemplate.
     */
    public LegendTemplate() {
        this(new Background(), 2.0f, "Arial-BOLD-14", "Arial-ITALIC-11", true, 20, 30);
    }

    public LegendTemplate(final Background background, final Float gap, final String mainFont, final String secondFont, 
            final Boolean layerName, final Integer glyphWidth, final Integer glyphHeight) {
        this.background = background;
        this.gap = gap;
        this.mainFont = mainFont;
        this.secondFont = secondFont;
        this.layerVisible = layerName;
        this.glyphWidth = glyphWidth;
        this.glyphHeight = glyphHeight;
    }

    public LegendTemplate (final org.geotoolkit.display2d.ext.legend.LegendTemplate displayLegendTemplate) {
        this.gap = displayLegendTemplate.getGapSize();
        this.mainFont = displayLegendTemplate.getLayerFont().toString();
        this.secondFont = displayLegendTemplate.getRuleFont().toString();
        this.layerVisible = displayLegendTemplate.isLayerVisible();
        this.glyphWidth = displayLegendTemplate.getGlyphSize().width;
        this.glyphHeight = displayLegendTemplate.getGlyphSize().height;
        this.background = new Background(displayLegendTemplate.getBackground());
        
    }
    
    public Background getBackground() {
        return background;
    }

    public void setBackground(Background background) {
        this.background = background;
    }

    public Float getGap() {
        return gap;
    }

    public void setGap(Float gap) {
        this.gap = gap;
    }

    public String getMainFont() {
        return mainFont;
    }

    public void setMainFont(String mainFont) {
        this.mainFont = mainFont;
    }

    public String getSecondFont() {
        return secondFont;
    }

    public void setSecondFont(String secondFont) {
        this.secondFont = secondFont;
    }

    public Boolean getLayerVisible() {
        return layerVisible;
    }

    public void setLayerVisible(Boolean layerName) {
        this.layerVisible = layerName;
    }

    public Integer getGlyphWidth() {
        return glyphWidth;
    }

    public void setGlyphWidth(Integer glyphWidth) {
        this.glyphWidth = glyphWidth;
    }

    public Integer getGlyphHeight() {
        return glyphHeight;
    }

    public void setGlyphHeight(Integer glyphHeight) {
        this.glyphHeight = glyphHeight;
    }
    
    /**
     * Convert to a displayable geotoolkit LegendTemplate .
     * @return org.geotoolkit.display2d.ext.legend.LegendTemplate
     */
    public org.geotoolkit.display2d.ext.legend.LegendTemplate toDisplayLegendTemplate() {
        final BackgroundTemplate dispBackground = background.toBackgroundTemplate();
        final Font mainF = Font.decode(mainFont);
        final Font secondF = Font.decode(secondFont);
        
        final Dimension glyphSize;
        if(glyphWidth != null && glyphHeight != null){
            glyphSize = new Dimension( glyphWidth , glyphHeight ) ;
        }else{
            glyphSize = null;
        }
        
        return new DefaultLegendTemplate(dispBackground, gap, glyphSize, mainF, layerVisible, secondF);
    }
    
}
