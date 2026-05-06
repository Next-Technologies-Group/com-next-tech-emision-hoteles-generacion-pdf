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
package com.cfdi40.pdfgen.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

/**
 * Cumplimiento del artículo 13 de la AGPL v3 ("Remote Network Interaction"):
 * cualquier usuario que interactúe con esta API a través de la red puede obtener
 * la URL del código fuente correspondiente a esta versión vía GET /source.
 */
@RestController
@RequestMapping("${prefix_server_path}")
public class SourceCodeController {

    @Value("${app.source.url:https://github.com/Next-Technologies-Group/com-next-tech-emision-hoteles-generacion-pdf}")
    private String sourceUrl;

    @GetMapping("/source")
    public ResponseEntity<Void> source() {
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(sourceUrl)).build();
    }
}
