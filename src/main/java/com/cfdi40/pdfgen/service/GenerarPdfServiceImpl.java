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
package com.cfdi40.pdfgen.service;

import com.itextpdf.text.DocumentException;
import com.cfdi40.exceptionhandlerstarter.exception.BusinessException;
import com.cfdi40.pdfgen.dto.Response;
import com.cfdi40.pdfgen.model.entity.IdentificadorSucursal;
import com.cfdi40.pdfgen.model.repository.IdentificadorSucursalRepository;
import com.cfdi40.pdfgen.tenants.tenanta.cfdi.dto.CFDI;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;

import java.io.ByteArrayOutputStream;

@Service

public class GenerarPdfServiceImpl implements GenerarPdfService {

    @Autowired
    IdentificadorSucursalRepository identificadorSucursalRespository;
    @Autowired
    CatalogStaticFallbackService catalogFallback;
    @Autowired
    PdfFacturaService pdfFacturaService;
    @Autowired
    PdfTicketService pdfTicketService;
    @Autowired
    PdfFacturaTenantAService pdfFacturaTenantAService;
    @Autowired
    PdfFacturaTenantBService pdfFacturaTenantBService;
    @Autowired
    PdfFacturaTenantCService pdfFacturaTenantCService;
    @Autowired
    PdfFacturaTenantDService pdfFacturaTenantDService;
    @Autowired
    PdfNominaService pdfNominaService;
    

    @Override
    public Response generarPdfFactura(CFDI facturaDto, Integer identificadorSucursalId) throws BusinessException, DocumentException {

        ByteArrayOutputStream pdfResponse = null;
        try {           
            pdfResponse = null;
            IdentificadorSucursal identificadorSucursal = catalogFallback.findIdentificadorSucursalSafe(identificadorSucursalId);
            
            if (facturaDto.getTipoCfdi() != null) {
            pdfResponse = new ByteArrayOutputStream();

                switch (facturaDto.getTipoCfdi()) {
                    case FACTURA:
                    case NOTA_CREDITO:
                    case TRASLADO:

                       if (facturaDto.getPuntoVenta() != null) {
                           switch (facturaDto.getPuntoVenta()) {
                               case FRONT:
                               case BCXC:
                                    pdfResponse = pdfFacturaService.generarPDfFactura(facturaDto, identificadorSucursal);                                    
                                   break;
                               case POS:
                                    pdfResponse =  pdfTicketService.generarPDfFactura(facturaDto, identificadorSucursal);
                                   break;
                               case TENANT_A:
                               case PROGRMAS_LEALTAD:
                                    pdfResponse =  pdfFacturaTenantAService.generarPDfFactura(facturaDto, identificadorSucursal);
                                   break;
                               case TENANT_B:
                                    pdfResponse = pdfFacturaTenantBService.generarPDfFactura(facturaDto, identificadorSucursal);
                                   break;
                               case TENANT_C:
                                    pdfResponse = pdfFacturaTenantCService.generarPDfFactura(facturaDto, identificadorSucursal);
                                   break;
                               default:
                                   throw new BusinessException(HttpStatus.BAD_REQUEST, "El Punto de Venta no es valido para la creación del PDF de la Factura con UUID " + (facturaDto.getTimbrado() != null ? facturaDto.getTimbrado().getUuid() : ""));
                           }
                       } else {
                            throw new BusinessException(HttpStatus.BAD_REQUEST, "El Punto de Venta es requerido para la creación del PDF de la Factura con UUID " + (facturaDto.getTimbrado() != null ? facturaDto.getTimbrado().getUuid() : ""));
                       }
                       break;
                   case PAGO:

                       if (facturaDto.getPuntoVenta() != null) {
                           switch (facturaDto.getPuntoVenta()) {
                               case TENANT_A:
                               case PROGRMAS_LEALTAD:
                                    pdfResponse = pdfFacturaTenantAService.generarPDfFactura(facturaDto, identificadorSucursal);
                                   break;
                               case BCXC:
                               case FRONT:
                               case POS:
                                    pdfResponse = pdfFacturaService.generarPDfFactura(facturaDto, identificadorSucursal);
                                   break;
                               case TENANT_C:
                                    pdfResponse = pdfFacturaTenantCService.generarPDfFactura(facturaDto, identificadorSucursal);
                                   break;
                               default:
                               throw new BusinessException(HttpStatus.BAD_REQUEST,"El tipo de POS no es valido para la creación del PDF Complemento de Pago, Factura con UUID " + (facturaDto.getTimbrado() != null ? facturaDto.getTimbrado().getUuid() : ""));
                           }
                       } else {
                           throw new BusinessException(HttpStatus.BAD_REQUEST,"El Punto de Venta es requerido para la creación del PDF de la Factura con UUID " + (facturaDto.getTimbrado() != null ? facturaDto.getTimbrado().getUuid() : ""));
                       }
                       break;
                 case NOMINA:
                     pdfResponse = pdfNominaService.generarPDfFactura(facturaDto);
                     break;
                 case DONATIVO:
                     pdfResponse = pdfFacturaTenantDService.generarPDfFactura(facturaDto, identificadorSucursal);
                     break;

                 default:
                     throw new BusinessException(HttpStatus.BAD_REQUEST,"El tipo de CFDI no es valido para la creación del PDF de la Factura con UUID " + (facturaDto.getTimbrado() != null ? facturaDto.getTimbrado().getUuid() : ""));
               }

            } else {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "El tipo de CFDI es requerido para generar el PDF de la Factura con UUID " + (facturaDto.getTimbrado() != null ? facturaDto.getTimbrado().getUuid() : ""));
            }

            Response response = new Response();
            String base64String = Base64Utils.encodeToString(pdfResponse.toByteArray());
            response.setPdf(base64String);

            return response;
        } catch (JSONException ex) {

            throw new BusinessException(HttpStatus.BAD_REQUEST, "Ocurrio un error al generar el pdf en una cadena en base64" + ex.toString());
        }
    }

  
}
