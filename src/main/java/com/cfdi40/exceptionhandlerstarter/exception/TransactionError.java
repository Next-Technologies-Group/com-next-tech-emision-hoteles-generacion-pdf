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

import java.util.Objects;

/**
 * Clase que representa un error de transacción.
 * Implementada con características modernas de Java.
 */
public final class TransactionError {

    private final String mensaje;

    /**
     * Constructor que crea un TransactionError con el mensaje especificado.
     * 
     * @param mensaje El mensaje de error, no puede ser nulo o vacío
     */
    public TransactionError(String mensaje) {
        this.mensaje = (mensaje == null || mensaje.trim().isEmpty()) ? "Error desconocido" : mensaje;
    }

    /**
     * @return El mensaje de error
     */
    public String getMensaje() {
        return mensaje;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        TransactionError that = (TransactionError) obj;
        return Objects.equals(mensaje, that.mensaje);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mensaje);
    }

    @Override
    public String toString() {
        return String.format("TransactionError[mensaje=%s]", mensaje);
    }
}
