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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import org.keycloak.adapters.AdapterDeploymentContext;
import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.adapters.KeycloakDeploymentBuilder;

/**
 * Static factory for generating Keycloak deployments.
 * @author Stan Silvert ssilvert@redhat.com (C) 2014 Red Hat Inc.
 */
public enum KeycloakConfig {
    HTTP_ENDPOINT("keycloak-http-endpoint.json", httpEndpointJSON()),
    WEB_CONSOLE("keycloak-web-console.json", webConsoleJSON());

    private final String configDir = System.getProperty("jboss.server.config.dir");

    private final String fileName;
    private final String inlinedJSON;
    private final KeycloakDeployment deployment;
    private final AdapterDeploymentContext context;


    KeycloakConfig(String fileName, String inlinedJSON) {
        this.fileName = fileName;
        this.inlinedJSON = inlinedJSON;
        try {
            this.deployment = KeycloakDeploymentBuilder.build(configStream());
            this.context = new AdapterDeploymentContext(deployment);
        } catch (RuntimeException e) {
            System.err.println("Unable to read realm info from " + fileName);
            throw e;
        }
    }

    public KeycloakDeployment deployment() {
        return this.deployment;
    }

    public AdapterDeploymentContext context() {
        return this.context;
    }

    private InputStream configStream() {
        if (configDir == null) return makeStream(inlinedJSON);
        File jsonFile = new File(configDir, fileName);

        try {
            FileInputStream fileStream = new FileInputStream(jsonFile);
            System.out.println("@@@@@@@@@ using json file=" + jsonFile);
            return fileStream;
        } catch (FileNotFoundException e) {
            System.out.println("@@@@@@@@@@ using inlined json for " + toString());
            return makeStream(inlinedJSON);
        }
    }

    private static InputStream makeStream(String json) {
        return new ByteArrayInputStream(json.getBytes());
    }

    private static String httpEndpointJSON() {
        return "{\n" +
"  \"realm\": \"ManagementRealm\",\n" +
"  \"realm-public-key\": \"MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCiTN5MrxKlhbRpsiic3HlWOjZDCZ87nkSm39y5E7vlBpBj5Q3VvXFxnME9KfeHpjg3cIVLdvALa5UNsNVx6N+qDY56YcjY+Zxq4lYk60eWuSk3y26dubV1WUHqcSOofB++/+rcmQevtYc1+fbr0uINHJ+kbZ7D5tYNU2qvjrPSWwIDAQAB\",\n" +
"  \"auth-server-url\": \"http://10.16.23.117:8080/auth\",\n" +
"  \"ssl-not-required\": true,\n" +
"  \"resource\": \"http-endpoint\",\n" +
"  \"credentials\": {\n" +
"    \"secret\": \"3d37d499-5cc3-4f8f-aac4-51dc192db592\"\n" +
"  },\n" +
"  \"use-resource-role-mappings\": true\n" +
"}";
    }

    private static String webConsoleJSON() {
        return "{\n" +
"  \"realm\": \"ManagementRealm\",\n" +
"  \"realm-public-key\": \"MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCiTN5MrxKlhbRpsiic3HlWOjZDCZ87nkSm39y5E7vlBpBj5Q3VvXFxnME9KfeHpjg3cIVLdvALa5UNsNVx6N+qDY56YcjY+Zxq4lYk60eWuSk3y26dubV1WUHqcSOofB++/+rcmQevtYc1+fbr0uINHJ+kbZ7D5tYNU2qvjrPSWwIDAQAB\",\n" +
"  \"auth-server-url\": \"http://10.16.23.117:8080/auth\",\n" +
"  \"ssl-not-required\": true,\n" +
"  \"resource\": \"web-console\",\n" +
"  \"credentials\": {\n" +
"    \"secret\": \"afe23ede-2fee-48e3-b8ae-bb67a37553cb\"\n" +
"  },\n" +
"  \"use-resource-role-mappings\": true\n" +
"}";
    }
}
