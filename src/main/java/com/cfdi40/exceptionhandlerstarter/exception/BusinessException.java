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
package com.cfdi40.exceptionhandlerstarter.exception;

import org.springframework.http.HttpStatus;

/**
 * Excepción de negocio que encapsula errores específicos de la lógica de aplicación.
 * 
 */
public class BusinessException extends Exception {

    private final HttpStatus httpStatus;
    private final String mensaje;
    private final String detalle;

    /**
     * Constructor completo con estado HTTP, mensaje y detalle.
     * 
     * @param httpStatus El estado HTTP asociado al error
     * @param mensaje El mensaje principal del error
     * @param detalle Información adicional sobre el error
     */
    public BusinessException(HttpStatus httpStatus, String mensaje, String detalle) {
        super(mensaje);
        this.httpStatus = httpStatus;
        this.mensaje = mensaje;
        this.detalle = detalle;
    }
    
    /**
     * Constructor con estado HTTP y mensaje.
     * 
     * @param httpStatus El estado HTTP asociado al error
     * @param mensaje El mensaje del error
     */
    public BusinessException(HttpStatus httpStatus, String mensaje) {
        this(httpStatus, mensaje, null);
    }

    /**
     * Constructor por defecto.
     */
    public BusinessException() {
        this(HttpStatus.INTERNAL_SERVER_ERROR, "Error de negocio", null);
    }

    /**
     * @return El estado HTTP asociado
     */
    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    /**
     * @return El mensaje del error
     */
    public String getMensaje() {
        return mensaje;
    }

    /**
     * @return El detalle del error
     */
    public String getDetalle() {
        return detalle;
    }
}
