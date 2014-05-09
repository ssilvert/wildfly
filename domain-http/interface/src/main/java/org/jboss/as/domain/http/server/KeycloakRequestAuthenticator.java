/*
 * JBoss, Home of Professional Open Source
 * Copyright 2014 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @author tags. All rights reserved.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 */

package org.jboss.as.domain.http.server;

import io.undertow.security.api.SecurityContext;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.Cookie;
import io.undertow.server.handlers.CookieImpl;
import java.util.HashMap;
import java.util.Map;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.adapters.HttpFacade;
import org.keycloak.adapters.KeycloakAccount;
import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.adapters.RefreshableKeycloakSecurityContext;
import org.keycloak.adapters.undertow.KeycloakUndertowAccount;
import org.keycloak.adapters.undertow.UndertowRequestAuthenticator;

/**
 *
 * @author Stan Silvert ssilvert@redhat.com (C) 2014 Red Hat Inc.
 */
public class KeycloakRequestAuthenticator extends UndertowRequestAuthenticator {
    private static final String COOKIE_NAME = "keycloak-user";
    private static final Map<String, KeycloakUndertowAccount> accountsLoggedIn = new HashMap<String, KeycloakUndertowAccount>();

    public KeycloakRequestAuthenticator(HttpFacade facade, KeycloakDeployment deployment, int sslRedirectPort,
                                       SecurityContext securityContext, HttpServerExchange exchange) {
        super(facade, deployment, sslRedirectPort, securityContext, exchange);
    }

    @Override
    protected boolean isCached() {
        Cookie userCookie = exchange.getRequestCookies().get(COOKIE_NAME);

        if (userCookie == null) {
            System.out.println("## did not find userCookie");
            return false;
        }

        String userId = userCookie.getValue();
        System.out.println("## userId=" + userId);

        KeycloakUndertowAccount account = accountsLoggedIn.get(userId);
        if (account == null) {
            System.out.println("## account is null");
            unsetCookie();
            return false;
        }

        account.setDeployment(deployment);
        if (account.isActive()) {
            log.info("## Cached account found");
            securityContext.authenticationComplete(account, "KEYCLOAK", false);
            propagateKeycloakContext( account);
            return true;
        }

        System.out.println("## removing inactive account");
        accountsLoggedIn.remove(userId);
        unsetCookie();

        return false;
    }

    //TODO: Is this how to unset an undertow cookie?
    private void unsetCookie() {
        exchange.setResponseCookie(new CookieImpl(COOKIE_NAME, null));
    }

    @Override
    protected void completeOAuthAuthentication(KeycloakPrincipal principal, RefreshableKeycloakSecurityContext session) {
        KeycloakUndertowAccount account = new KeycloakUndertowSubjectAccount(principal, session, deployment);
        securityContext.authenticationComplete(account, "KEYCLOAK", false);
        propagateKeycloakContext(account);
        login(account);
    }

    @Override
    protected void login(KeycloakAccount account) {
        String userId = account.getPrincipal().getName();
        System.out.println("## login() userId=" + userId);
        accountsLoggedIn.put(userId, (KeycloakUndertowAccount)account);
        exchange.setResponseCookie(new CookieImpl(COOKIE_NAME, userId));
    }

    @Override
    protected void propagateKeycloakContext(KeycloakUndertowAccount account) {
        super.propagateKeycloakContext(account);
        //System.out.println(">>>>>>>> Called propagateSessionInfo ");
        //SecurityInfoHelper.propagateSessionInfo(account);
    }

}
