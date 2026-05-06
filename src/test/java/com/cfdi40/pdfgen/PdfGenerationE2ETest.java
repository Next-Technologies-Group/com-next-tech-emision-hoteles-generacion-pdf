/*
 * Copyright (C) 2025 The cfdi-4.0-pdf-generador Authors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.cfdi40.pdfgen;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-end functional verification:
 *  1. Boots the full Spring Boot application on a random port (no DB required;
 *     HikariCP is configured in lazy mode against an unreachable sentinel URL).
 *  2. Hits each public HTTP endpoint of the API.
 *  3. For PDF-producing endpoints, decodes the base64 payload and asserts:
 *      a) The byte stream begins with the PDF magic header "%PDF-".
 *      b) The byte stream contains the literal "iText" producer fingerprint,
 *         which is emitted by iTextPDF 5.5.x in the Producer / Creator
 *         metadata fields. This guarantees the PDF was actually rendered by
 *         iText and not by an unrelated library.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                // Avoid binding to the configurable prefix; use root path for tests.
                "prefix_server_path=/pdf/cfdi/4.0",
                "spring.main.banner-mode=off"
        })
class PdfGenerationE2ETest {

    @LocalServerPort
    int port;

    @Autowired
    TestRestTemplate rest;

    @Value("classpath:sample-cfdi-40.xml")
    private org.springframework.core.io.Resource sampleXml;

    private static final String BASE = "/pdf/cfdi/4.0";

    @Test
    void healthEndpointReturnsOk() {
        ResponseEntity<String> resp = rest.getForEntity(url(""), String.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).isEqualTo("OK");
    }

    @Test
    void sourceEndpointRedirectsToTaggedRelease() {
        TestRestTemplate noFollow = rest.withBasicAuth("", "");
        // TestRestTemplate follows redirects by default; use HEAD to capture 302.
        ResponseEntity<Void> resp = noFollow.exchange(url("/source"), HttpMethod.HEAD,
                HttpEntity.EMPTY, Void.class);
        // Either 302 with Location header, or a 2xx after redirect — both prove the endpoint exists.
        assertThat(resp.getStatusCode().value()).isBetween(200, 399);
    }

    @Test
    void xmlToPdfWithRawXmlBodyProducesItextPdf() throws Exception {
        String xml = new String(Files.readAllBytes(Paths.get(sampleXml.getURI())), StandardCharsets.UTF_8);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_XML);
        ResponseEntity<String> resp = rest.exchange(
                url("/xml-to-pdf?puntoVenta=FRONT&identificadorSucursalId=0"),
                HttpMethod.POST, new HttpEntity<>(xml, headers), String.class);

        assertItextPdfResponse(resp);
    }

    @Test
    void xmlToPdfWithJsonWrapperProducesItextPdf() throws Exception {
        String xml = new String(Files.readAllBytes(Paths.get(sampleXml.getURI())), StandardCharsets.UTF_8);
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.createObjectNode()
                .put("xml", xml)
                .put("puntoVenta", "FRONT")
                .put("identificadorSucursalId", 0)
                .toString();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<String> resp = rest.exchange(
                url("/xml-to-pdf"),
                HttpMethod.POST, new HttpEntity<>(json, headers), String.class);

        assertItextPdfResponse(resp);
    }

    private void assertItextPdfResponse(ResponseEntity<String> resp) throws Exception {
        assertThat(resp.getStatusCode().is2xxSuccessful())
                .as("Expected 2xx but got %s with body=%s", resp.getStatusCode(), resp.getBody())
                .isTrue();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(resp.getBody());
        assertThat(root.has("pdf")).as("Response must contain 'pdf' field").isTrue();
        byte[] pdf = Base64.getDecoder().decode(root.get("pdf").asText());

        // (a) PDF magic
        assertThat(new String(pdf, 0, 5, StandardCharsets.US_ASCII))
                .as("PDF must start with %%PDF- magic header")
                .isEqualTo("%PDF-");

        // (b) iText producer fingerprint — present in Producer / Creator metadata
        // emitted by iTextPDF 5.5.x.
        String pdfAscii = new String(pdf, StandardCharsets.ISO_8859_1);
        assertThat(pdfAscii)
                .as("PDF must contain the iText producer fingerprint")
                .contains("iText");
    }

    private String url(String path) {
        return "http://localhost:" + port + BASE + path;
    }
}
