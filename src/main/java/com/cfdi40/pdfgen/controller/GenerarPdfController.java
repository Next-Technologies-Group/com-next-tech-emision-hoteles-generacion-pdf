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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.cfdi40.pdfgen.dto.Request;
import com.cfdi40.pdfgen.service.GenerarPdfService;
import com.itextpdf.text.DocumentException;
import com.cfdi40.exceptionhandlerstarter.exception.BusinessException;

@RestController
@RequestMapping("${prefix_server_path}")

public class GenerarPdfController {
    
    @Autowired
    GenerarPdfService generarPdfService;

    @RequestMapping(method = RequestMethod.GET)
    public String test() {

        return "OK";
    }
    @PostMapping(consumes = {MediaType.APPLICATION_JSON_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?>  generarPdfFactura(@RequestBody Request request) throws BusinessException, DocumentException {
        return new ResponseEntity<>(generarPdfService.generarPdfFactura(request.getCfdiDto(), request.getIdentificador_sucursal_id()), HttpStatus.CREATED);
    }
    
}
