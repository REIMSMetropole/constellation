/*
  * JBoss, Home of Professional Open Source
  * Copyright 2007, JBoss Inc., and individual contributors as indicated
  * by the @authors tag. See the copyright.txt in the distribution for a
  * full listing of individual contributors.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
package net.seagis.xacml.locators;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import net.seagis.xacml.bridge.WrapperPolicyFinderModule;
import net.seagis.xacml.XACMLConstants;
import net.seagis.xacml.api.XACMLPolicy;
import com.sun.xacml.Policy;
import com.sun.xacml.finder.PolicyFinderModule;

/**
 *  Policy Locator for plain XACML Policy instances
 *  @author Anil.Saldhana@redhat.com
 *  @since  Jul 6, 2007 
 *  @version $Revision$
 */
public class JBossPolicyLocator extends AbstractJBossPolicyLocator {
   
    private List<PolicyFinderModule> pfml = new ArrayList<PolicyFinderModule>();

    public JBossPolicyLocator() {
    }

    public JBossPolicyLocator(Set<XACMLPolicy> policies) {
        setPolicies(policies);
    }

    @Override
    public void setPolicies(Set<XACMLPolicy> policies) {
        for (XACMLPolicy xp : policies) {
            if (xp.getType() == XACMLPolicy.POLICY) {
                Policy p = (Policy) xp.get(XACMLConstants.UNDERLYING_POLICY);
                WrapperPolicyFinderModule wpfm = new WrapperPolicyFinderModule(p);
                pfml.add(wpfm);
            }
        }
        this.map.put(XACMLConstants.POLICY_FINDER_MODULE.key, pfml);
    }

    public Object get(XACMLConstants key) {
        return map.get(key.key);
    }

    public void set(XACMLConstants key, Object obj) {
        map.put(key.key, obj);
    }
}
