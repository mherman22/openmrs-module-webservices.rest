/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.GlobalProperty;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.docs.swagger.SwaggerSpecificationCreator;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.api.RestService;
import org.openmrs.web.test.BaseModuleWebContextSensitiveTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class OpenApiGeneratorTest extends BaseModuleWebContextSensitiveTest {

    private static final Logger log = LoggerFactory.getLogger(OpenApiGeneratorTest.class);

    @Before
    public void setUp() throws Exception {
        System.out.println("*******************************************");
        RestService restService = Context.getService(RestService.class);
        System.out.println("RestService available: " + (restService != null));

        System.out.println("Initializing REST service...");
        assert restService != null;
        restService.initialize();
        System.out.println("REST service initialized");
        System.out.println("Resource handlers after init: " + restService.getResourceHandlers().size());
        Context.getAdministrationService().saveGlobalProperty(
            new GlobalProperty(RestConstants.SWAGGER_QUIET_DOCS_GLOBAL_PROPERTY_NAME, "true"));
        
        Context.flushSession();
        System.out.println("*******************************************");
    }

    @Test
    public void generateOpenApiSpec() throws IOException {
        assertTrue("OpenMRS session should be open", Context.isSessionOpen());
        
        RestService restService = Context.getService(RestService.class);
        assertNotNull("RestService should be available", restService);
        assertNotNull("Resource handlers should be available", restService.getResourceHandlers());
        
        int handlerCount = restService.getResourceHandlers().size();
        System.out.println("Resource handlers found: " + handlerCount);
        
        if (handlerCount == 0) {
            log.warn("No resource handlers found. This may indicate a configuration issue.");
            log.warn("Available services: {}", Context.getRegisteredComponents(Object.class).size());
        }
        
        assertFalse("Should have at least one resource handler", restService.getResourceHandlers().isEmpty());
        String openApiJson = new SwaggerSpecificationCreator().getJSON();
        
        assertNotNull("OpenAPI specification should not be null", openApiJson);
        assertFalse("OpenAPI specification should not be empty", openApiJson.trim().isEmpty());
        
        boolean isSwagger2 = openApiJson.contains("\"swagger\"");
        boolean isOpenApi3 = openApiJson.contains("\"openapi\"");
        assertTrue("OpenAPI specification should contain either 'swagger' (2.0) or 'openapi' (3.0) field", 
                  isSwagger2 || isOpenApi3);
        
        assertTrue("OpenAPI specification should contain 'paths' field", openApiJson.contains("\"paths\""));
        assertTrue("OpenAPI specification should contain actual API paths",
                  openApiJson.contains("\"/") && openApiJson.contains("\"get\""));
        
        File outputDir = new File("target/openapi");
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }
        
        File outputFile = new File(outputDir, "openapi.json");
        try (FileWriter writer = new FileWriter(outputFile)) {
            writer.write(openApiJson);
        }
        
        System.out.println("=== OpenAPI Specification Generated ===");
        System.out.println("OpenMRS Version: " + Context.getAdministrationService().getSystemInformation().get("SystemInfo.OpenMRSInstallation.openmrsVersion"));
        System.out.println("Specification Format: " + (isOpenApi3 ? "OpenAPI 3.0" : "Swagger 2.0"));
        System.out.println("Resource Handlers Found: " + restService.getResourceHandlers().size());
        System.out.println("Output Location: " + outputFile.getAbsolutePath());
        System.out.println("File Size: " + Files.size(Paths.get(outputFile.getAbsolutePath())) + " bytes");
        System.out.println("========================================");
        
        assertTrue("Output file should exist", outputFile.exists());
        assertTrue("Output file should not be empty", outputFile.length() > 0);
    }
} 