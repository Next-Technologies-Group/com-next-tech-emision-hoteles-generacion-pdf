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
import com.cfdi40.pdfgen.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.List;

/**
 * Catálogos SAT con fallback defensivo: si la API externa no está
 * configurada o falla, devuelve un DTO con descripcion = clave (eco)
 * para que la generación del PDF nunca aborte.
 */
@Component
@Slf4j
public class CatalogosSATService {

    private static final String MONEDA_RESOURCE = "/moneda";
    private static final String CLAVE_PRODUCTO_SERVICIO_RESOURCE = "/clave_producto_servicio_by_clave/";
    private static final String IMPUESTOS_RESOURCE = "/impuesto/";
    private static final String TIPO_FACTOR_RESOURCE = "/tipo_factor/";
    private static final String TIPO_CONTRATO_RESOURCE = "/nomina_tipo_contrato/";
    private static final String TIPO_JORNADA_RESOURCE = "/nomina_tipo_jornada/";
    private static final String RIESGO_PUESTO_RESOURCE = "/nomina_riesgo_puesto/";
    private static final String TIPO_OTRO_PAGO_RESOURCE = "/nomina_tipo_otro_pago/";
    private static final String TIPO_INCAPACIDAD_RESOURCE = "/nomina_tipo_incapacidad/";
    private static final String REGIMEN_CONTRATACION_RESOURCE = "/nomina_regimen_contratatcion/";

    @Value("${api.catalogos.url:}")
    private String catalogosURL;
    private RestTemplate restTemplate;
    private List<CatMoneda> catMonedaList = Collections.emptyList();

    @PostConstruct
    public void init() {
        restTemplate = new RestTemplate();
        if (isApiDisabled()) {
            log.info("api.catalogos.url no configurada; usando catálogos vacíos.");
            return;
        }
        try {
            log.info("Cargando catálogo de moneda SAT desde {}", catalogosURL);
            ResponseEntity<CatMoneda[]> response = restTemplate.getForEntity(
                    catalogosURL.concat(MONEDA_RESOURCE), CatMoneda[].class);
            CatMoneda[] body = response.getBody();
            catMonedaList = body != null ? java.util.Arrays.asList(body) : Collections.emptyList();
        } catch (Exception e) {
            log.warn("No fue posible cargar el catálogo SAT de moneda ({}); continuando con lista vacía.", e.getMessage());
            catMonedaList = Collections.emptyList();
        }
    }

    private boolean isApiDisabled() {
        return catalogosURL == null || catalogosURL.isBlank();
    }

    public List<CatMoneda> getCatMonedaList() {
        return catMonedaList;
    }

    public CatClaveProducto getCatClaveProductoSat(String claveProducto) {
        if (isApiDisabled()) return fallbackClaveProducto(claveProducto);
        try {
            ResponseEntity<CatClaveProducto> r = restTemplate.getForEntity(
                    catalogosURL.concat(CLAVE_PRODUCTO_SERVICIO_RESOURCE).concat(claveProducto), CatClaveProducto.class);
            return r.getBody() != null ? r.getBody() : fallbackClaveProducto(claveProducto);
        } catch (Exception e) {
            log.warn("Fallback CatClaveProducto[{}]: {}", claveProducto, e.getMessage());
            return fallbackClaveProducto(claveProducto);
        }
    }

    public CatImpuestos obtenerCatImpuestoSatByClave(String clave) {
        if (isApiDisabled()) return fallbackImpuesto(clave);
        try {
            ResponseEntity<CatImpuestos> r = restTemplate.getForEntity(
                    catalogosURL.concat(IMPUESTOS_RESOURCE).concat(clave), CatImpuestos.class);
            return r.getBody() != null ? r.getBody() : fallbackImpuesto(clave);
        } catch (Exception e) {
            log.warn("Fallback CatImpuestos[{}]: {}", clave, e.getMessage());
            return fallbackImpuesto(clave);
        }
    }

    public CatTipoFactor obtenerCatTipofactorSatByClave(String clave) {
        if (isApiDisabled()) return fallbackTipoFactor(clave);
        try {
            ResponseEntity<CatTipoFactor> r = restTemplate.getForEntity(
                    catalogosURL.concat(TIPO_FACTOR_RESOURCE).concat(clave), CatTipoFactor.class);
            return r.getBody() != null ? r.getBody() : fallbackTipoFactor(clave);
        } catch (Exception e) {
            log.warn("Fallback CatTipoFactor[{}]: {}", clave, e.getMessage());
            return fallbackTipoFactor(clave);
        }
    }

    public CatTipoContrato getCatTipoContratoByClave(String clave) {
        if (isApiDisabled()) return fallbackTipoContrato(clave);
        try {
            ResponseEntity<CatTipoContrato> r = restTemplate.getForEntity(
                    catalogosURL.concat(TIPO_CONTRATO_RESOURCE).concat(clave), CatTipoContrato.class);
            return r.getBody() != null ? r.getBody() : fallbackTipoContrato(clave);
        } catch (Exception e) {
            log.warn("Fallback CatTipoContrato[{}]: {}", clave, e.getMessage());
            return fallbackTipoContrato(clave);
        }
    }

    public CatTipoJornada getCatTipoJornadaByClave(String clave) {
        if (isApiDisabled()) return fallbackTipoJornada(clave);
        try {
            ResponseEntity<CatTipoJornada> r = restTemplate.getForEntity(
                    catalogosURL.concat(TIPO_JORNADA_RESOURCE).concat(clave), CatTipoJornada.class);
            return r.getBody() != null ? r.getBody() : fallbackTipoJornada(clave);
        } catch (Exception e) {
            log.warn("Fallback CatTipoJornada[{}]: {}", clave, e.getMessage());
            return fallbackTipoJornada(clave);
        }
    }

    public CatRiesgoPuesto getCatRiesgoPuestoByClave(String clave) {
        if (isApiDisabled()) return fallbackRiesgoPuesto(clave);
        try {
            ResponseEntity<CatRiesgoPuesto> r = restTemplate.getForEntity(
                    catalogosURL.concat(RIESGO_PUESTO_RESOURCE).concat(clave), CatRiesgoPuesto.class);
            return r.getBody() != null ? r.getBody() : fallbackRiesgoPuesto(clave);
        } catch (Exception e) {
            log.warn("Fallback CatRiesgoPuesto[{}]: {}", clave, e.getMessage());
            return fallbackRiesgoPuesto(clave);
        }
    }

    public CatTipoOtroPago getCatTipoOtroPagooByClave(String clave) {
        if (isApiDisabled()) return fallbackTipoOtroPago(clave);
        try {
            ResponseEntity<CatTipoOtroPago> r = restTemplate.getForEntity(
                    catalogosURL.concat(TIPO_OTRO_PAGO_RESOURCE).concat(clave), CatTipoOtroPago.class);
            return r.getBody() != null ? r.getBody() : fallbackTipoOtroPago(clave);
        } catch (Exception e) {
            log.warn("Fallback CatTipoOtroPago[{}]: {}", clave, e.getMessage());
            return fallbackTipoOtroPago(clave);
        }
    }

    public CatTipoIncapacidad getCatTipoIncapacidadByClave(String clave) {
        if (isApiDisabled()) return fallbackTipoIncapacidad(clave);
        try {
            ResponseEntity<CatTipoIncapacidad> r = restTemplate.getForEntity(
                    catalogosURL.concat(TIPO_INCAPACIDAD_RESOURCE).concat(clave), CatTipoIncapacidad.class);
            return r.getBody() != null ? r.getBody() : fallbackTipoIncapacidad(clave);
        } catch (Exception e) {
            log.warn("Fallback CatTipoIncapacidad[{}]: {}", clave, e.getMessage());
            return fallbackTipoIncapacidad(clave);
        }
    }

    public CatRegimenContratacion getCatRegimenContratacionByClave(String clave) {
        if (isApiDisabled()) return fallbackRegimenContratacion(clave);
        try {
            ResponseEntity<CatRegimenContratacion> r = restTemplate.getForEntity(
                    catalogosURL.concat(REGIMEN_CONTRATACION_RESOURCE).concat(clave), CatRegimenContratacion.class);
            return r.getBody() != null ? r.getBody() : fallbackRegimenContratacion(clave);
        } catch (Exception e) {
            log.warn("Fallback CatRegimenContratacion[{}]: {}", clave, e.getMessage());
            return fallbackRegimenContratacion(clave);
        }
    }

    // ---------- fallbacks (eco clave -> descripcion) ----------
    private CatClaveProducto fallbackClaveProducto(String clave) {
        CatClaveProducto d = new CatClaveProducto();
        d.setClave(clave);
        d.setDescripcion(clave);
        return d;
    }
    private CatImpuestos fallbackImpuesto(String clave) {
        CatImpuestos d = new CatImpuestos();
        d.setImpuesto(clave);
        d.setDescripcion(clave);
        return d;
    }
    private CatTipoFactor fallbackTipoFactor(String clave) {
        // Lombok: el setter de un campo `cTipoFactor` puede ser setcTipoFactor o setCTipoFactor
        // según la versión; usamos reflexión simple via @Data dejando el DTO vacío.
        return new CatTipoFactor();
    }
    private CatTipoContrato fallbackTipoContrato(String clave) {
        CatTipoContrato d = new CatTipoContrato();
        d.setClave(clave);
        d.setConcepto(clave);
        return d;
    }
    private CatTipoJornada fallbackTipoJornada(String clave) {
        CatTipoJornada d = new CatTipoJornada();
        d.setClave(clave);
        d.setConcepto(clave);
        return d;
    }
    private CatRiesgoPuesto fallbackRiesgoPuesto(String clave) {
        CatRiesgoPuesto d = new CatRiesgoPuesto();
        d.setClave(clave);
        d.setDescripcion(clave);
        return d;
    }
    private CatTipoOtroPago fallbackTipoOtroPago(String clave) {
        CatTipoOtroPago d = new CatTipoOtroPago();
        d.setClave(clave);
        d.setDescripcion(clave);
        return d;
    }
    private CatTipoIncapacidad fallbackTipoIncapacidad(String clave) {
        CatTipoIncapacidad d = new CatTipoIncapacidad();
        d.setClave(clave);
        d.setDescripcion(clave);
        return d;
    }
    private CatRegimenContratacion fallbackRegimenContratacion(String clave) {
        CatRegimenContratacion d = new CatRegimenContratacion();
        d.setClave(clave);
        d.setDescripcion(clave);
        return d;
    }
}
