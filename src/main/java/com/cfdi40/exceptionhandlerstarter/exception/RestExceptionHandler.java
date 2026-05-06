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

import org.apache.catalina.connector.ClientAbortException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Clase para el manejo de las excepciones generales de la aplicación.
 * Actualizada para Spring Boot 3.x y Java 21.
 *
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(RestExceptionHandler.class);

    /**
     * Maneja excepciones de negocio.
     * 
     * @param ex La excepción de negocio
     * @return ResponseEntity con el error formateado
     */
    @ExceptionHandler(BusinessException.class)
    protected ResponseEntity<Object> handleBusinessException(BusinessException ex) {
        LOG.error("Ocurrió un error al tratar de procesar la solicitud HttpStatus [{}], Mensaje [{}], Detalle [{}]", 
                ex.getHttpStatus(), ex.getMensaje(), ex.getDetalle());
        return new ResponseEntity<>(new TransactionError(ex.getMensaje()), ex.getHttpStatus());
    }

    /**
     * Maneja excepciones internas del sistema.
     * 
     * @param ex La excepción interna
     * @return ResponseEntity con el error formateado
     */
    @ExceptionHandler(InternalErrorException.class)
    protected ResponseEntity<Object> handleInternalErrorException(InternalErrorException ex) {
        LOG.error("Ocurrió un error interno al tratar de procesar la solicitud HttpStatus [{}], Detalle [{}]", 
                ex.getHttpStatus(), ex.getMensaje(), ex.getEx());
        return new ResponseEntity<>(new TransactionError("Ocurrió un error interno al tratar de procesar la solicitud"), 
                ex.getHttpStatus());
    }

    /**
     * Maneja errores de validación de argumentos de método.
     * Método actualizado para Spring Boot 3.x.
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            @NonNull MethodArgumentNotValidException ex, 
            @NonNull HttpHeaders headers, 
            @NonNull HttpStatus status, 
            @NonNull WebRequest request) {

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        String errorDetails = errors.entrySet().stream()
                .map(entry -> entry.getKey() + " = " + entry.getValue())
                .collect(Collectors.joining(", ", "[", "]"));

        LOG.error("Ocurrió un error al tratar de procesar la solicitud HttpStatus [{}], La solicitud contiene los siguientes errores [{}]", 
                HttpStatus.BAD_REQUEST, errorDetails);
        
        return new ResponseEntity<>(
                new TransactionError("La solicitud contiene los siguientes errores: " + errorDetails), 
                HttpStatus.BAD_REQUEST);
    }

    /**
     * Maneja todas las excepciones no controladas específicamente.
     * 
     * @param ex La excepción general
     * @param request La solicitud web
     * @return ResponseEntity con el error formateado
     */
    @ExceptionHandler({Exception.class})
    public ResponseEntity<Object> handleAll(Exception ex, WebRequest request) {
        
        if (ex instanceof ClientAbortException) {
            LOG.error("Ocurrió un error {}, parece que el cliente cerró el navegador", ClientAbortException.class.getSimpleName());
        } else {
            LOG.error("Ocurrió un error desconocido", ex);
        }
        
        return new ResponseEntity<>(
                new TransactionError("Ocurrió un error desconocido"), 
                HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
