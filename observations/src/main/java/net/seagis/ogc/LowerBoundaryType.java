/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
 * (C) 2005, Institut de Recherche pour le Développement
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */


package net.seagis.ogc;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import net.seagis.coverage.web.ExpressionType;


/**
 * <p>Java class for LowerBoundaryType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="LowerBoundaryType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice>
 *         &lt;element ref="{http://www.opengis.net/ogc}expression"/>
 *         &lt;element ref="{http://www.opengis.net/ogc}Literal"/>
 *         &lt;element ref="{http://www.opengis.net/ogc}PropertyName"/>
 *       &lt;/choice>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * @author Guilhem Legal
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "LowerBoundaryType", propOrder = {
    "expression",
    "literal",
    "propertyName"
})
public class LowerBoundaryType {

    @XmlElement(nillable = true)
    private ExpressionType expression;
    @XmlElement(name = "Literal", nillable = true)
    private LiteralType literal;
    @XmlElement(name = "PropertyName", nillable = true)
    private String propertyName;

    /**
     * Gets the value of the expression property.
     */
    public ExpressionType getExpression() {
        return expression;
    }

    /**
     * Gets the value of the literal property.
     */
    public LiteralType getLiteral() {
        return literal;
    }

    /**
     * Gets the value of the propertyName property.
     */
    public String getPropertyName() {
        return propertyName;
    }
}
