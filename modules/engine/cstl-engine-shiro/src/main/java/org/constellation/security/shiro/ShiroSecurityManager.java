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
package org.constellation.security.shiro;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.constellation.security.SecurityManager;

/**
 * 
 * @author Guilhem Legal (Geomatys)
 * @author Olivier NOUGUIER (Geomatys)
 */
public class ShiroSecurityManager implements SecurityManager {

    public String getCurrentUserLogin() {
        final Subject currentUser = SecurityUtils.getSubject();
        return (String) currentUser.getPrincipal();
    }

    public boolean isAuthenticated() {
        final Subject currentUser = SecurityUtils.getSubject();
        return currentUser.isAuthenticated();
    }

    public boolean isAllowed(final String action) {
        final Subject currentUser = SecurityUtils.getSubject();
        return currentUser.isPermitted(action);
    }

    public boolean hasRole(final String role) {
        final Subject currentUser = SecurityUtils.getSubject();
        return currentUser.hasRole(role);
    }

    public void login(final String login, final String pass)
            throws UnknownAccountException, IncorrectCredentialsException {
        final UsernamePasswordToken token = new UsernamePasswordToken(login,
                pass);
        SecurityUtils.getSubject().login(token);
    }

    public void logout() {
        SecurityUtils.getSubject().logout();
    }
}