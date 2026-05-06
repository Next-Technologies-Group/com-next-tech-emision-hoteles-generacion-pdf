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

import com.cfdi40.pdfgen.model.entity.CatIdiomapdf;
import com.cfdi40.pdfgen.model.entity.CatTagxidioma;
import com.cfdi40.pdfgen.model.entity.Emisor;
import com.cfdi40.pdfgen.model.entity.IdentificadorSucursal;
import com.cfdi40.pdfgen.model.entity.Sucursal;
import com.cfdi40.pdfgen.model.entity.TipoPeriodicidad;
import com.cfdi40.pdfgen.model.repository.CatIdiomapdfRepository;
import com.cfdi40.pdfgen.model.repository.CatTagxidiomaRepository;
import com.cfdi40.pdfgen.model.repository.IdentificadorSucursalRepository;
import com.cfdi40.pdfgen.model.repository.TipoPeriodicidadRepository;
import com.cfdi40.pdfgen.util.EnumTipoProcesoTimbrado;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Provee fallbacks en memoria cuando la base de datos no está disponible
 * o no contiene los registros esperados. Garantiza que la generación de
 * PDF nunca aborte por un error de persistencia.
 */
@Service
@Slf4j
public class CatalogStaticFallbackService {

    @Autowired(required = false)
    private IdentificadorSucursalRepository identificadorSucursalRepository;
    @Autowired(required = false)
    private CatIdiomapdfRepository catIdiomapdfRepository;
    @Autowired(required = false)
    private CatTagxidiomaRepository catTagxidiomaRepository;
    @Autowired(required = false)
    private TipoPeriodicidadRepository tipoPeriodicidadRepository;

    public IdentificadorSucursal findIdentificadorSucursalSafe(Integer id) {
        if (identificadorSucursalRepository != null && id != null && id > 0) {
            try {
                return identificadorSucursalRepository.findById(id).orElseGet(this::defaultIdentificadorSucursal);
            } catch (Exception e) {
                log.warn("Fallback IdentificadorSucursal[{}]: {}", id, e.getMessage());
            }
        }
        return defaultIdentificadorSucursal();
    }

    public CatIdiomapdf findIdiomaSafe(Integer id) {
        if (catIdiomapdfRepository != null) {
            try {
                return catIdiomapdfRepository.findById(id).orElseGet(() -> defaultIdioma(id));
            } catch (Exception e) {
                log.warn("Fallback CatIdiomapdf[{}]: {}", id, e.getMessage());
            }
        }
        return defaultIdioma(id);
    }

    public List<CatTagxidioma> findTagsSafe(CatIdiomapdf idioma) {
        if (catTagxidiomaRepository != null && idioma != null && idioma.getId() != null) {
            try {
                List<CatTagxidioma> r = catTagxidiomaRepository.findByCatIdiomaPdfId(idioma);
                return r != null ? r : Collections.emptyList();
            } catch (Exception e) {
                log.warn("Fallback tags idioma[{}]: {}", idioma.getId(), e.getMessage());
            }
        }
        return Collections.emptyList();
    }

    public TipoPeriodicidad findPeriodicidadSafe(String clave) {
        if (tipoPeriodicidadRepository != null && clave != null && !clave.isBlank()) {
            try {
                TipoPeriodicidad r = tipoPeriodicidadRepository.findByClave(clave);
                if (r != null) return r;
            } catch (Exception e) {
                log.warn("Fallback TipoPeriodicidad[{}]: {}", clave, e.getMessage());
            }
        }
        TipoPeriodicidad t = new TipoPeriodicidad();
        t.setClave(clave != null ? clave : "");
        t.setDescripcion(clave != null ? clave : "");
        return t;
    }

    private IdentificadorSucursal defaultIdentificadorSucursal() {
        IdentificadorSucursal i = new IdentificadorSucursal();
        i.setId(0);
        i.setIdentificador("DEFAULT");
        i.setTipoElaboracion(EnumTipoProcesoTimbrado.PRODUCTIVO);
        Sucursal s = new Sucursal();
        s.setId(0);
        s.setNombre("");
        s.setPais("");
        s.setEstado("");
        s.setCalle("");
        s.setColonia("");
        s.setCiudadDelegacion("");
        s.setCodigoPostal("");
        s.setNombreLogo("");
        i.setSucursalId(s);
        Emisor e = new Emisor();
        e.setId(0);
        e.setRfc("");
        e.setNombre("");
        e.setRazonSocial("");
        e.setRegimenFiscal("");
        e.setRutaCertificados("");
        e.setRutaXmls("");
        e.setRutaPdfs("");
        e.setRutaPlantilla("");
        e.setCertificadoId(0);
        i.setEmisorId(e);
        return i;
    }

    private CatIdiomapdf defaultIdioma(Integer id) {
        CatIdiomapdf c = new CatIdiomapdf();
        c.setId(id != null ? id : 1);
        c.setDescripcion(id != null && id == 2 ? "EN" : "ES");
        Date now = new Date();
        c.setFechacreacion(now);
        c.setUltimamodificacion(now);
        c.setUsuario("system");
        return c;
    }
}
