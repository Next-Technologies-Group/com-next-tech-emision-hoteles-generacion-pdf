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
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cfdi40.pdfgen.service;
import java.io.ByteArrayOutputStream;

import com.cfdi40.exceptionhandlerstarter.exception.BusinessException;
import com.cfdi40.pdfgen.model.entity.IdentificadorSucursal;
import com.cfdi40.pdfgen.tenants.tenanta.cfdi.dto.CFDI;


public interface PdfFacturaService{
    
    /**
     * Metodo para la generacion del PDF de la factura
     * @param facturaDto, datos de la factura con la cual será generado el PDF
     * @param identificadorSucursal
     * @throws BusinessException 
     */
 public ByteArrayOutputStream generarPDfFactura(CFDI facturaDto, IdentificadorSucursal identificadorSucursal ) throws BusinessException;
 
        
}
