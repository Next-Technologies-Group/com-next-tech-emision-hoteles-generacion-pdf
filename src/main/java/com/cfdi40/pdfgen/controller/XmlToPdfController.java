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

import com.itextpdf.text.DocumentException;
import com.cfdi40.exceptionhandlerstarter.exception.BusinessException;
import com.cfdi40.pdfgen.dto.Response;
import com.cfdi40.pdfgen.dto.XmlToPdfRequest;
import com.cfdi40.pdfgen.service.GenerarPdfService;
import com.cfdi40.pdfgen.service.SatXmlParser;
import com.cfdi40.pdfgen.tenants.tenanta.cfdi.dto.CFDI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoint que convierte un XML CFDI 4.0 emitido por el SAT en un PDF
 * usando las plantillas internas. Disponible en dos modalidades:
 *   - POST application/xml | text/xml: cuerpo es el XML directo.
 *   - POST application/json: cuerpo es {@link XmlToPdfRequest}.
 */
@RestController
@RequestMapping("${prefix_server_path}")
public class XmlToPdfController {

    @Autowired
    private SatXmlParser satXmlParser;

    @Autowired
    private GenerarPdfService generarPdfService;

    @PostMapping(value = "/xml-to-pdf",
            consumes = {MediaType.APPLICATION_XML_VALUE, MediaType.TEXT_XML_VALUE},
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Response> fromXml(@RequestBody String xml,
                                            @RequestParam(value = "puntoVenta", defaultValue = "FRONT") CFDI.PuntoVenta puntoVenta,
                                            @RequestParam(value = "identificadorSucursalId", defaultValue = "0") Integer identificadorSucursalId)
            throws BusinessException, DocumentException {
        return generate(xml, puntoVenta, identificadorSucursalId);
    }

    @PostMapping(value = "/xml-to-pdf",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Response> fromJson(@RequestBody XmlToPdfRequest req)
            throws BusinessException, DocumentException {
        if (req == null) throw new BusinessException(HttpStatus.BAD_REQUEST, "Request vacío");
        CFDI.PuntoVenta pv = req.getPuntoVenta() != null ? req.getPuntoVenta() : CFDI.PuntoVenta.FRONT;
        Integer id = req.getIdentificadorSucursalId() != null ? req.getIdentificadorSucursalId() : 0;
        return generate(req.getXml(), pv, id);
    }

    private ResponseEntity<Response> generate(String xml, CFDI.PuntoVenta pv, Integer id)
            throws BusinessException, DocumentException {
        CFDI cfdi = satXmlParser.parse(xml);
        cfdi.setPuntoVenta(pv);
        Response r = generarPdfService.generarPdfFactura(cfdi, id);
        return new ResponseEntity<>(r, HttpStatus.CREATED);
    }
}
