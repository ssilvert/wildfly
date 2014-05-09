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
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import org.keycloak.adapters.AuthChallenge;
import org.keycloak.adapters.AuthOutcome;
import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.adapters.KeycloakDeploymentBuilder;
import static org.keycloak.adapters.undertow.ServletKeycloakAuthMech.KEYCLOAK_CHALLENGE_ATTACHMENT_KEY;
import org.keycloak.adapters.undertow.UndertowHttpFacade;
import org.keycloak.adapters.undertow.UndertowRequestAuthenticator;

/**
 *
 * @author Stan Silvert ssilvert@redhat.com (C) 2014 Red Hat Inc.
 */
public class KeycloakAuthenticationMechanism implements AuthenticationMechanism {

    private KeycloakDeployment deployment = KeycloakDeploymentBuilder.build(makeStream(getJSON()));

    public KeycloakAuthenticationMechanism() {
        //super(deployment, new UndertowUserSessionManagement(deployment), new ManagementConfidentialPortManager());
        System.out.println("**** KeycloakAuthenticationMechanism created");
    }

    @Override
    public ChallengeResult sendChallenge(HttpServerExchange exchange, SecurityContext securityContext) {
        System.out.println("**** KeycloakAuthenticationMechanism.sendChallenge()");
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

    //boolean authenticated = false;

    @Override
    public AuthenticationMechanismOutcome authenticate(HttpServerExchange exchange, SecurityContext securityContext) {
        System.out.println("**** KeycloakAuthenticationMechanism.authenticate()");
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

        //if (!authenticated) {
            return AuthenticationMechanismOutcome.NOT_ATTEMPTED;
        //} else {
          //  return AuthenticationMechanismOutcome.AUTHENTICATED;
        //}
    }

    private UndertowRequestAuthenticator createRequestAuthenticator(HttpServerExchange exchange, SecurityContext securityContext, UndertowHttpFacade facade) {
        //public UndertowRequestAuthenticator(HttpFacade facade, KeycloakDeployment deployment, int sslRedirectPort, SecurityContext securityContext, HttpServerExchange exchange) {

        int sslRedirectPort = 9990;
        return new KeycloakRequestAuthenticator(facade, deployment, sslRedirectPort, securityContext, exchange);
    }

    private InputStream makeStream(String json) {
        return new ByteArrayInputStream(json.getBytes());
    }

    private String getJSON() {
        return "{\n" +
"  \"realm\": \"ManagementRealm\",\n" +
"  \"realm-public-key\": \"MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAqlYfaNBZ/voh9T4T7RGGsLd5/xquLBnCzSNxR8P2+hZtn1cDcvSbIg/SxjoH1FUCyQYpPOV4PHs7SgB+YqiMn10IHk/ThylJcttHTmG1odlLs+73JT43NqEuRbpnaAlqA3FZBr8nKGits2I155tIbEf0zfbjK03MmyLAs9WH0eHmVyEb9fuXYtJVs+x/3HOWFSiEpVg4hbC6Ca5K2M3TV1DFSwxGLA5KcISBot2QU+le7/0wGQiwl5pv/rcsyhXrq0SbYPCJG26T88xjOI4jEqPy/zs5UvjvrjTF8TBu/y4nSQiR7D0flF1pqEqH3HZ21RVN+3crwEisp4iQQfVHuwIDAQAB\",\n" +
"  \"auth-server-url\": \"http://localhost:8080/auth\",\n" +
"  \"ssl-not-required\": true,\n" +
"  \"resource\": \"console\",\n" +
"  \"credentials\": {\n" +
"    \"secret\": \"17724e6d-8f91-4ecf-97ff-83d911fdeedb\"\n" +
"  },\n" +
"  \"use-resource-role-mappings\": true\n" +
"}";
    }

}
