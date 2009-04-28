/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
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
package org.constellation.xacml;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.Principal;
import java.security.acl.Group;
import java.util.Enumeration;
import org.constellation.xacml.api.PolicyDecisionPoint;
import org.constellation.xacml.api.RequestContext;
import org.constellation.xacml.api.ResponseContext;
import org.geotoolkit.xacml.xml.context.ActionType;
import org.geotoolkit.xacml.xml.context.AttributeType;
import org.geotoolkit.xacml.xml.context.EnvironmentType;
import org.geotoolkit.xacml.xml.context.RequestType;
import org.geotoolkit.xacml.xml.context.ResourceType;
import org.geotoolkit.xacml.xml.context.SubjectType;
import org.constellation.xacml.factory.RequestAttributeFactory;

/**
 *
 * @author Guilhem Legal
 */
public class PEP {
    
    private String issuer = "constellation.org";
    
    private PolicyDecisionPoint PDP;
    
    /**
     * Build a new Policy Enforcement Point.
     * 
     * @param PDP
     */
    public PEP(PolicyDecisionPoint PDP) {
        this.PDP = PDP;
    }
    
    /**
     * Create A XACML request for the request resource containing the userName (principal), the role group and the action.
     * 
     * @param resourceURI The request resource URI.
     * @param principal   The user.
     * @param roleGroup   The user group.
     * 
     * @return An XACML request.
     * @throws java.lang.Exception
     */
    public RequestContext createXACMLRequest(String resourceURI, Principal principal, Group roleGroup, String action) throws URISyntaxException, IOException {
        RequestContext requestCtx = new CstlRequestContext();

        //Create a subject type
        SubjectType subject = createSubject(principal, roleGroup);

        //Create a resource type
        ResourceType resourceType = createResource(resourceURI);

        //Create an action type
        ActionType actionType = createAction(action);

        //Create an Environment Type (Optional)
        EnvironmentType environmentType = createTimeEnvironement();

        //Create a Request Type
        RequestType requestType = new RequestType();
        requestType.getSubject().add(subject);
        requestType.getResource().add(resourceType);
        requestType.setAction(actionType);
        requestType.setEnvironment(environmentType);

        requestCtx.setRequest(requestType);

        return requestCtx;
    }
    
    /**
     * Create A XACML request for the request resource containing the userName (principal), the role group and the action.
     * 
     * @param resourceURI The request resource URI.
     * @param principal   The user.
     * @param roleGroup   The user group.
     * 
     * @return An XACML request.
     * @throws java.lang.Exception
     */
    public RequestContext createXACMLRequest(URI resourceURI, Principal principal, Group roleGroup, String action) throws IOException {
        RequestContext requestCtx = new CstlRequestContext();

        //Create a subject type
        SubjectType subject = createSubject(principal, roleGroup);

        //Create a resource type
        ResourceType resourceType = createResource(resourceURI);

        //Create an action type
        ActionType actionType = createAction(action);

        //Create an Environment Type (Optional)
        EnvironmentType environmentType = createTimeEnvironement();

        //Create a Request Type
        RequestType requestType = new RequestType();
        requestType.getSubject().add(subject);
        requestType.getResource().add(resourceType);
        requestType.setAction(actionType);
        requestType.setEnvironment(environmentType);

        requestCtx.setRequest(requestType);

        return requestCtx;
    }
    
    /**
     * Create a part of XACML request about the user and group.
     * 
     * @param user      The authentified user.
     * @param roleGroup The user group.
     * 
     * @return a subject Type whitch is a part of XACML request.
     */
    protected SubjectType createSubject(Principal user, Group roleGroup) {
    
        //Create a subject type
        SubjectType subject = new SubjectType();
        subject.getAttribute().add(
                RequestAttributeFactory.createStringAttributeType(XACMLConstants.ATTRIBUTEID_SUBJECT_SUBJECTID.key, 
                                                                  issuer, 
                                                                  user.getName()));
        
        Enumeration<Principal> roles = (Enumeration<Principal>) roleGroup.members();
        while (roles.hasMoreElements()) {
            Principal rolePrincipal = roles.nextElement();
            AttributeType attSubjectID = RequestAttributeFactory.createStringAttributeType(
                    XACMLConstants.ATTRIBUTEID_SUBJECT_ROLE.key, issuer, rolePrincipal.getName());
            subject.getAttribute().add(attSubjectID);
        }
        return subject;
    }

    /**
     * Create a  part of XACML request about the requested resource.
     * 
     * @param URI the requested resource URI.
     * 
     * @return a resource Type whitch is a part of XACML request.
     */
    protected ResourceType createResource(String URI) throws URISyntaxException {
    
        //Create a resource type
        ResourceType resourceType = new ResourceType();
        resourceType.getAttribute().add(
                RequestAttributeFactory.createAnyURIAttributeType(XACMLConstants.ATTRIBUTEID_RESOURCE_RESOURCEID.key, null, new URI(URI)));
        return resourceType;
    }
    
    /**
     * Create a  part of XACML request about the requested resource.
     * 
     * @param URI the requested resource URI.
     * 
     * @return a resource Type whitch is a part of XACML request.
     */
    protected ResourceType createResource(URI URI) {
    
        //Create a resource type
        ResourceType resourceType = new ResourceType();
        resourceType.getAttribute().add(
                RequestAttributeFactory.createAnyURIAttributeType(XACMLConstants.ATTRIBUTEID_RESOURCE_RESOURCEID.key, null, URI));
        return resourceType;
    }
    
    /**
     * Create a  part of XACML request about the action to execute.
     * 
     * @param URI the requested resource URI.
     * 
     * @return a action Type whitch is a part of XACML request.
     */
    protected ActionType createAction(String action)  {
        
        //Create an action type
        ActionType actionType = new ActionType();
        actionType.getAttribute().add(
                RequestAttributeFactory.createStringAttributeType(XACMLConstants.ATTRIBUTEID_ACTION_ACTIONID.key, issuer, action));
        
        return actionType;
    
    }
    
    /**
     * Create a part of XACML request about the time.
     */
    protected EnvironmentType createTimeEnvironement() {
            
        //Create an Environment Type
        EnvironmentType environmentType = new EnvironmentType();
        environmentType.getAttribute().add(
                RequestAttributeFactory.createDateTimeAttributeType(XACMLConstants.ATTRIBUTEID_ENVIRONMENT_CURRENTTIME.key, issuer));
        return environmentType;
        
    }
    
   /**
    * Get the response for a request from the PDP
    * @param pdp
    * @param request
    * @return
    * @throws Exception
    */
   public ResponseContext getResponse(RequestContext request) {
      return PDP.evaluate(request);
   }

   /**
    * Get the decision from the PDP
    * @param pdp
    * @param request RequestContext containing the request
    * @return
    * @throws Exception
    */
   public int getDecision(RequestContext request) {
      ResponseContext response = PDP.evaluate(request);
      return response.getDecision();
   }
}
