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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.adapters.KeycloakDeploymentBuilder;

/**
 * Static factory for generating Keycloak deployments.
 * @author Stan Silvert ssilvert@redhat.com (C) 2014 Red Hat Inc.
 */
public class KeycloakDeploymentFactory {

    private static final KeycloakDeployment httpEndpointDeployment = KeycloakDeploymentBuilder.build(makeStream(httpEndpointJSON()));
    private static final KeycloakDeployment webConsoleDeployment = KeycloakDeploymentBuilder.build(makeStream(webConsoleJSON()));

    // Don't allow instances
    private KeycloakDeploymentFactory() {}

    public static KeycloakDeployment getHttpEndpointDeployment() {
        return httpEndpointDeployment;
    }

    public static KeycloakDeployment getWebConsoleDeployment() {
        return webConsoleDeployment;
    }

    private static InputStream makeStream(String json) {
        return new ByteArrayInputStream(json.getBytes());
    }

    private static String httpEndpointJSON() {
        return "{\n" +
"  \"realm\": \"ManagementRealm\",\n" +
"  \"realm-public-key\": \"MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAqlYfaNBZ/voh9T4T7RGGsLd5/xquLBnCzSNxR8P2+hZtn1cDcvSbIg/SxjoH1FUCyQYpPOV4PHs7SgB+YqiMn10IHk/ThylJcttHTmG1odlLs+73JT43NqEuRbpnaAlqA3FZBr8nKGits2I155tIbEf0zfbjK03MmyLAs9WH0eHmVyEb9fuXYtJVs+x/3HOWFSiEpVg4hbC6Ca5K2M3TV1DFSwxGLA5KcISBot2QU+le7/0wGQiwl5pv/rcsyhXrq0SbYPCJG26T88xjOI4jEqPy/zs5UvjvrjTF8TBu/y4nSQiR7D0flF1pqEqH3HZ21RVN+3crwEisp4iQQfVHuwIDAQAB\",\n" +
"  \"auth-server-url\": \"http://localhost:8080/auth\",\n" +
"  \"ssl-not-required\": true,\n" +
"  \"resource\": \"http-endpoint\",\n" +
"  \"credentials\": {\n" +
"    \"secret\": \"17724e6d-8f91-4ecf-97ff-83d911fdeedb\"\n" +
"  },\n" +
"  \"use-resource-role-mappings\": true\n" +
"}";
    }

    private static String webConsoleJSON() {
        return "{\n" +
"  \"realm\": \"ManagementRealm\",\n" +
"  \"realm-public-key\": \"MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAqlYfaNBZ/voh9T4T7RGGsLd5/xquLBnCzSNxR8P2+hZtn1cDcvSbIg/SxjoH1FUCyQYpPOV4PHs7SgB+YqiMn10IHk/ThylJcttHTmG1odlLs+73JT43NqEuRbpnaAlqA3FZBr8nKGits2I155tIbEf0zfbjK03MmyLAs9WH0eHmVyEb9fuXYtJVs+x/3HOWFSiEpVg4hbC6Ca5K2M3TV1DFSwxGLA5KcISBot2QU+le7/0wGQiwl5pv/rcsyhXrq0SbYPCJG26T88xjOI4jEqPy/zs5UvjvrjTF8TBu/y4nSQiR7D0flF1pqEqH3HZ21RVN+3crwEisp4iQQfVHuwIDAQAB\",\n" +
"  \"auth-server-url\": \"http://localhost:8080/auth\",\n" +
"  \"ssl-not-required\": true,\n" +
"  \"resource\": \"web-console\",\n" +
"  \"credentials\": {\n" +
"    \"secret\": \"fe3c851c-0bb1-458d-99e6-a43a0e17061e\"\n" +
"  },\n" +
"  \"use-resource-role-mappings\": true\n" +
"}";
    }
}
