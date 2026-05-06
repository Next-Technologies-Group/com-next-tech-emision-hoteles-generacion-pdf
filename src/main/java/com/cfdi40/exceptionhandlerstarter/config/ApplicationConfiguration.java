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
package com.cfdi40.exceptionhandlerstarter.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración principal de la aplicación.
 * Define el escaneo de componentes para el manejo de excepciones.
 *
 */
@Configuration
@ComponentScan(basePackages = "com.cfdi40.exceptionhandlerstarter.exception")
public class ApplicationConfiguration {
    
    /**
     * Esta clase habilita automáticamente el manejo de excepciones
     * mediante el escaneo de componentes en el paquete de excepciones.
     */
}
