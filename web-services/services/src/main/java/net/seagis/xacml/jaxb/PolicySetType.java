//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.1-b02-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2008.03.19 at 05:35:22 PM BRT 
//

package net.seagis.xacml.jaxb;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>Java class for PolicySetType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="PolicySetType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Location" type="{http://www.w3.org/2001/XMLSchema}anyURI" minOccurs="0"/>
 *         &lt;element name="Policy" type="{urn:jboss:xacml:2.0}PolicyType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="PolicySet" type="{urn:jboss:xacml:2.0}PolicySetType" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PolicySetType", propOrder =
{"location", "policy", "policySet"})
public class PolicySetType
{

   @XmlElement(name = "Location")
   @XmlSchemaType(name = "anyURI")
   protected String location;

   @XmlElement(name = "Policy")
   protected List<PolicyType> policy;

   @XmlElement(name = "PolicySet")
   protected List<PolicySetType> policySet;

   /**
    * Gets the value of the location property.
    * 
    * @return
    *     possible object is
    *     {@link String }
    *     
    */
   public String getLocation()
   {
      return location;
   }

   /**
    * Sets the value of the location property.
    * 
    * @param value
    *     allowed object is
    *     {@link String }
    *     
    */
   public void setLocation(String value)
   {
      this.location = value;
   }

   /**
    * Gets the value of the policy property.
    * 
    * <p>
    * This accessor method returns a reference to the live list,
    * not a snapshot. Therefore any modification you make to the
    * returned list will be present inside the JAXB object.
    * This is why there is not a <CODE>set</CODE> method for the policy property.
    * 
    * <p>
    * For example, to add a new item, do as follows:
    * <pre>
    *    getPolicy().add(newItem);
    * </pre>
    * 
    * 
    * <p>
    * Objects of the following type(s) are allowed in the list
    * {@link PolicyType }
    * 
    * 
    */
   public List<PolicyType> getPolicy()
   {
      if (policy == null)
      {
         policy = new ArrayList<PolicyType>();
      }
      return this.policy;
   }

   /**
    * Gets the value of the policySet property.
    * 
    * <p>
    * This accessor method returns a reference to the live list,
    * not a snapshot. Therefore any modification you make to the
    * returned list will be present inside the JAXB object.
    * This is why there is not a <CODE>set</CODE> method for the policySet property.
    * 
    * <p>
    * For example, to add a new item, do as follows:
    * <pre>
    *    getPolicySet().add(newItem);
    * </pre>
    * 
    * 
    * <p>
    * Objects of the following type(s) are allowed in the list
    * {@link PolicySetType }
    * 
    * 
    */
   public List<PolicySetType> getPolicySet()
   {
      if (policySet == null)
      {
         policySet = new ArrayList<PolicySetType>();
      }
      return this.policySet;
   }

}
