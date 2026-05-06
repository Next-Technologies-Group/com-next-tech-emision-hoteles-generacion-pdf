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
 * Excepción para errores internos del sistema.
 * 
 */
public class InternalErrorException extends Exception {

    private static final long serialVersionUID = 1L;
    
    private final HttpStatus httpStatus;
    private final String mensaje;
    private final Throwable cause;

    /**
     * Constructor por defecto.
     */
    public InternalErrorException() {
        this(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del sistema", null);
    }

    /**
     * Constructor completo con estado HTTP, mensaje y causa.
     * 
     * @param httpStatus El estado HTTP asociado al error
     * @param mensaje El mensaje del error
     * @param cause La excepción que causó este error
     */
    public InternalErrorException(HttpStatus httpStatus, String mensaje, Throwable cause) {
        super(mensaje, cause);
        this.httpStatus = httpStatus;
        this.mensaje = mensaje;
        this.cause = cause;
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
     * @return La excepción que causó este error
     */
    public Throwable getEx() {
        return cause;
    }
}
