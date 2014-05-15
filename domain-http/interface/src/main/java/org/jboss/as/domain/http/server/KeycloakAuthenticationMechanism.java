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

import io.undertow.security.api.AuthenticationMechanism;
import io.undertow.security.api.SecurityContext;
import io.undertow.server.HttpServerExchange;
import org.keycloak.adapters.AuthChallenge;
import org.keycloak.adapters.AuthOutcome;
import org.keycloak.adapters.KeycloakDeployment;
import static org.keycloak.adapters.undertow.ServletKeycloakAuthMech.KEYCLOAK_CHALLENGE_ATTACHMENT_KEY;
import org.keycloak.adapters.undertow.UndertowHttpFacade;
import org.keycloak.adapters.undertow.UndertowRequestAuthenticator;

/**
 *
 * @author Stan Silvert ssilvert@redhat.com (C) 2014 Red Hat Inc.
 */
public class KeycloakAuthenticationMechanism implements AuthenticationMechanism {

    private final KeycloakDeployment deployment;

    public KeycloakAuthenticationMechanism(KeycloakDeployment deployment) {
        System.out.println("**** KeycloakAuthenticationMechanism created for " + deployment.getResourceName());
        this.deployment = deployment;
    }

    @Override
    public ChallengeResult sendChallenge(HttpServerExchange exchange, SecurityContext securityContext) {
        System.out.println("**** KeycloakAuthenticationMechanism.sendChallenge() for " + deployment.getResourceName());
        System.out.println("exchange=" + exchange);
        System.out.println("securityContext=" + securityContext);
        System.out.println("exchange.getAttachment(KEYCLOAK_CHALLENGE_ATTACHMENT_KEY)=" + exchange.getAttachment(KEYCLOAK_CHALLENGE_ATTACHMENT_KEY));

        AuthChallenge challenge = exchange.getAttachment(KEYCLOAK_CHALLENGE_ATTACHMENT_KEY);
        if (challenge != null) {
            UndertowHttpFacade facade = new UndertowHttpFacade(exchange);
            if (challenge.challenge(facade)) {
                return new ChallengeResult(true, exchange.getResponseCode());
            }
        }
        return new ChallengeResult(false);
    }

    @Override
    public AuthenticationMechanismOutcome authenticate(HttpServerExchange exchange, SecurityContext securityContext) {
        System.out.println("**** KeycloakAuthenticationMechanism.authenticate() for " + deployment.getResourceName());
        System.out.println("exchange=" + exchange);
        System.out.println("securityContext from exchange=" + exchange.getSecurityContext());
        System.out.println("securityContext=" + securityContext);
        System.out.println("identity manager=" + securityContext.getIdentityManager());
        System.out.println("***** May be authenticated now.  securityContext.isAuthenticated()=" + securityContext.isAuthenticated());
        if (securityContext.getAuthenticatedAccount() != null) {
            System.out.println("***** May be authenticated now.  securityContext.getAuthenticatedAccount().getPrincipal()=" + securityContext.getAuthenticatedAccount().getPrincipal());
        }

        UndertowHttpFacade facade = new UndertowHttpFacade(exchange);
        UndertowRequestAuthenticator authenticator = createRequestAuthenticator(exchange, securityContext, facade);
        AuthOutcome outcome = authenticator.authenticate();
        System.out.println("*** outcome=" + outcome);
        if (outcome == AuthOutcome.AUTHENTICATED) {
            //authenticated = true;
            System.out.println("***** Should be authenticated now.  securityContext.isAuthenticated()=" + securityContext.isAuthenticated());
            System.out.println("***** Should be authenticated now.  securityContext.getAuthenticatedAccount().getPrincipal().getName()=" + securityContext.getAuthenticatedAccount().getPrincipal().getName());
            return AuthenticationMechanismOutcome.AUTHENTICATED;
        }
        AuthChallenge challenge = authenticator.getChallenge();
        System.out.println("*** challenge =" + challenge);
        if (challenge != null) {
            exchange.putAttachment(KEYCLOAK_CHALLENGE_ATTACHMENT_KEY, challenge);
        }

        if (outcome == AuthOutcome.FAILED) {
            return AuthenticationMechanismOutcome.NOT_AUTHENTICATED;
        }

        return AuthenticationMechanismOutcome.NOT_ATTEMPTED;
    }

    private UndertowRequestAuthenticator createRequestAuthenticator(HttpServerExchange exchange, SecurityContext securityContext, UndertowHttpFacade facade) {
        int sslRedirectPort = 9990;
        return new KeycloakRequestAuthenticator(facade, deployment, sslRedirectPort, securityContext, exchange);
    }

}
