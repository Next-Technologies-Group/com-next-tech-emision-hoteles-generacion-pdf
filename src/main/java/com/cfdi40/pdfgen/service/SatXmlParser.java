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

import com.cfdi40.exceptionhandlerstarter.exception.BusinessException;
import com.cfdi40.pdfgen.tenants.tenanta.cfdi.dto.CFDI;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Convierte un XML CFDI 4.0 emitido por el SAT en un objeto {@link CFDI} interno
 * compatible con los servicios de generación de PDF.
 *
 * Implementación DOM endurecida contra XXE (deshabilita DOCTYPE, entidades
 * externas y procesamiento seguro habilitado). No descarga XSDs externos.
 *
 * Esta es una conversión de campos clave del estándar CFDI 4.0 publicado por el
 * SAT (cfdv40.xsd) y el complemento TimbreFiscalDigital v1.1. Para producción
 * se recomienda validar contra los XSDs oficiales antes del parseo.
 */
@Service
@Slf4j
public class SatXmlParser {

    private static final String NS_CFDI = "http://www.sat.gob.mx/cfd/4";
    private static final String NS_TFD = "http://www.sat.gob.mx/TimbreFiscalDigital";

    @Value("${sat.xml.strict-validation:false}")
    private boolean strictValidation;

    public CFDI parse(String xml) throws BusinessException {
        if (xml == null || xml.isBlank()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "XML vacío");
        }
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            dbf.setFeature("http://xml.org/sax/features/external-general-entities", false);
            dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            dbf.setXIncludeAware(false);
            dbf.setExpandEntityReferences(false);

            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new InputSource(new StringReader(xml)));
            Element comp = doc.getDocumentElement();
            if (!"Comprobante".equals(localName(comp)) || !NS_CFDI.equals(comp.getNamespaceURI())) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "Raíz no es cfdi:Comprobante v4.0");
            }
            return mapComprobante(comp);
        } catch (BusinessException be) {
            throw be;
        } catch (Exception e) {
            log.warn("Error parseando XML CFDI: {}", e.getMessage());
            throw new BusinessException(HttpStatus.BAD_REQUEST, "XML CFDI inválido: " + e.getMessage());
        }
    }

    private CFDI mapComprobante(Element comp) throws BusinessException {
        CFDI cfdi = new CFDI();
        cfdi.setSerie(attr(comp, "Serie"));
        cfdi.setFolio(attr(comp, "Folio"));
        cfdi.setFecha(parseDate(attr(comp, "Fecha")));
        cfdi.setFormaPago(attr(comp, "FormaPago"));
        cfdi.setCondicionesDePago(attr(comp, "CondicionesDePago"));
        cfdi.setSubTotal(decimal(attr(comp, "SubTotal")));
        cfdi.setDescuento(decimal(attr(comp, "Descuento")));
        cfdi.setMoneda(attr(comp, "Moneda"));
        cfdi.setTipoCambio(decimal(attr(comp, "TipoCambio")));
        cfdi.setTotal(decimal(attr(comp, "Total")));
        cfdi.setLugarExpedicion(attr(comp, "LugarExpedicion"));
        cfdi.setExportacion(attr(comp, "Exportacion"));
        cfdi.setMetodoPago(attr(comp, "MetodoPago"));
        cfdi.setNumeroCertificado(attr(comp, "NoCertificado"));
        cfdi.setTipoCfdi(mapTipoComprobante(attr(comp, "TipoDeComprobante")));

        Element infoGlobal = childCfdi(comp, "InformacionGlobal");
        if (infoGlobal != null) {
            CFDI.InformacionGlobal ig = new CFDI.InformacionGlobal();
            ig.setPeriodicidad(attr(infoGlobal, "Periodicidad"));
            ig.setMeses(attr(infoGlobal, "Meses"));
            try { ig.setAnio(Integer.parseInt(Optional.ofNullable(attr(infoGlobal, "Año")).orElse("0"))); } catch (NumberFormatException ignored) { }
            cfdi.setInformacionGlobal(ig);
        }

        cfdi.setEmisor(mapEmisor(childCfdi(comp, "Emisor")));
        cfdi.setReceptor(mapReceptor(childCfdi(comp, "Receptor")));
        cfdi.setConceptos(mapConceptos(childCfdi(comp, "Conceptos")));
        cfdi.setImpuestos(mapImpuestos(childCfdi(comp, "Impuestos")));
        cfdi.setCfdiRelacionados(mapRelacionados(childCfdi(comp, "CfdiRelacionados")));
        cfdi.setTimbrado(mapTimbrado(childCfdi(comp, "Complemento")));

        if (cfdi.getTimbrado() == null && strictValidation) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "XML no timbrado (TFD ausente)");
        }
        return cfdi;
    }

    private CFDI.EnumTipoFactura mapTipoComprobante(String t) {
        if (t == null) return CFDI.EnumTipoFactura.FACTURA;
        switch (t) {
            case "I": return CFDI.EnumTipoFactura.FACTURA;
            case "E": return CFDI.EnumTipoFactura.NOTA_CREDITO;
            case "T": return CFDI.EnumTipoFactura.TRASLADO;
            case "N": return CFDI.EnumTipoFactura.NOMINA;
            case "P": return CFDI.EnumTipoFactura.PAGO;
            default:  return CFDI.EnumTipoFactura.FACTURA;
        }
    }

    private CFDI.Emisor mapEmisor(Element e) {
        if (e == null) return null;
        CFDI.Emisor em = new CFDI.Emisor();
        em.setRfc(attr(e, "Rfc"));
        em.setNombre(attr(e, "Nombre"));
        em.setRegimenFiscal(attr(e, "RegimenFiscal"));
        em.setFacAtrAdquirente(attr(e, "FacAtrAdquirente"));
        return em;
    }

    private CFDI.Receptor mapReceptor(Element e) {
        if (e == null) return null;
        CFDI.Receptor r = new CFDI.Receptor();
        r.setRfc(attr(e, "Rfc"));
        r.setNombre(attr(e, "Nombre"));
        r.setNumRegIdTrib(attr(e, "NumRegIdTrib"));
        r.setUsoCFDI(attr(e, "UsoCFDI"));
        r.setResidenciaFiscal(attr(e, "ResidenciaFiscal"));
        r.setRegimenFiscal(attr(e, "RegimenFiscalReceptor"));
        r.setDomicilioFiscal(attr(e, "DomicilioFiscalReceptor"));
        r.setCodigoPostal(attr(e, "DomicilioFiscalReceptor"));
        return r;
    }

    private List<CFDI.Conceptos> mapConceptos(Element conceptosEl) {
        if (conceptosEl == null) return Collections.emptyList();
        List<CFDI.Conceptos> out = new ArrayList<>();
        for (Element ce : childrenCfdi(conceptosEl, "Concepto")) {
            CFDI.Conceptos c = new CFDI.Conceptos();
            c.setCantidad(decimal(attr(ce, "Cantidad")));
            c.setUnidad(attr(ce, "Unidad"));
            c.setNoIdentificacion(attr(ce, "NoIdentificacion"));
            c.setDescripcion(attr(ce, "Descripcion"));
            c.setValorUnitario(decimal(attr(ce, "ValorUnitario")));
            c.setImporte(decimal(attr(ce, "Importe")));
            c.setDescuento(decimal(attr(ce, "Descuento")));
            c.setClaveProdServ(attr(ce, "ClaveProdServ"));
            c.setClaveUnidad(attr(ce, "ClaveUnidad"));
            c.setObjetoImp(attr(ce, "ObjetoImp"));

            Element imps = childCfdi(ce, "Impuestos");
            if (imps != null) {
                CFDI.Conceptos.ImpuestosConcepto ic = new CFDI.Conceptos.ImpuestosConcepto();
                Element trasEl = childCfdi(imps, "Traslados");
                if (trasEl != null) {
                    List<CFDI.Conceptos.Traslados> ts = new ArrayList<>();
                    for (Element t : childrenCfdi(trasEl, "Traslado")) {
                        CFDI.Conceptos.Traslados tr = new CFDI.Conceptos.Traslados();
                        tr.setBase(decimal(attr(t, "Base")));
                        tr.setImpuesto(attr(t, "Impuesto"));
                        tr.setTipoFactor(attr(t, "TipoFactor"));
                        tr.setTasaoCuota(decimal(attr(t, "TasaOCuota")));
                        tr.setImporte(decimal(attr(t, "Importe")));
                        ts.add(tr);
                    }
                    ic.setTraslados(ts);
                }
                Element retEl = childCfdi(imps, "Retenciones");
                if (retEl != null) {
                    List<CFDI.Conceptos.Retenciones> rs = new ArrayList<>();
                    for (Element t : childrenCfdi(retEl, "Retencion")) {
                        CFDI.Conceptos.Retenciones rr = new CFDI.Conceptos.Retenciones();
                        rr.setBase(decimal(attr(t, "Base")));
                        rr.setImpuesto(attr(t, "Impuesto"));
                        rr.setTipoFactor(attr(t, "TipoFactor"));
                        rr.setTasaoCuota(decimal(attr(t, "TasaOCuota")));
                        rr.setImporte(decimal(attr(t, "Importe")));
                        rs.add(rr);
                    }
                    ic.setRetenciones(rs);
                }
                c.setImpuestos(ic);
            }
            out.add(c);
        }
        return out;
    }

    private CFDI.Impuestos mapImpuestos(Element impEl) {
        if (impEl == null) return null;
        CFDI.Impuestos imp = new CFDI.Impuestos();
        imp.setTotalImpuestosRetenidos(decimal(attr(impEl, "TotalImpuestosRetenidos")));
        imp.setTotalImpuestosTrasladados(decimal(attr(impEl, "TotalImpuestosTrasladados")));
        Element trasEl = childCfdi(impEl, "Traslados");
        if (trasEl != null) {
            List<CFDI.Impuestos.Traslado> ts = new ArrayList<>();
            for (Element t : childrenCfdi(trasEl, "Traslado")) {
                CFDI.Impuestos.Traslado tr = new CFDI.Impuestos.Traslado();
                tr.setBase(decimal(attr(t, "Base")));
                tr.setImpuesto(attr(t, "Impuesto"));
                tr.setTipoFactor(attr(t, "TipoFactor"));
                tr.setTasaoCuota(decimal(attr(t, "TasaOCuota")));
                tr.setImporte(decimal(attr(t, "Importe")));
                ts.add(tr);
            }
            imp.setTraslado(ts);
        }
        Element retEl = childCfdi(impEl, "Retenciones");
        if (retEl != null) {
            List<CFDI.Impuestos.Retencion> rs = new ArrayList<>();
            for (Element t : childrenCfdi(retEl, "Retencion")) {
                CFDI.Impuestos.Retencion rr = new CFDI.Impuestos.Retencion();
                rr.setImpuesto(attr(t, "Impuesto"));
                rr.setImporte(decimal(attr(t, "Importe")));
                rs.add(rr);
            }
            imp.setRetenciones(rs);
        }
        return imp;
    }

    private List<CFDI.Relacionados> mapRelacionados(Element rel) {
        if (rel == null) return null;
        List<CFDI.Relacionados> out = new ArrayList<>();
        CFDI.Relacionados r = new CFDI.Relacionados();
        r.setTipoRelacion(attr(rel, "TipoRelacion"));
        List<String> uuids = new ArrayList<>();
        for (Element c : childrenCfdi(rel, "CfdiRelacionado")) {
            String u = attr(c, "UUID");
            if (u != null) uuids.add(u);
        }
        r.setUuid(uuids);
        out.add(r);
        return out;
    }

    private CFDI.Timbrado mapTimbrado(Element complemento) {
        if (complemento == null) return null;
        NodeList kids = complemento.getChildNodes();
        for (int i = 0; i < kids.getLength(); i++) {
            Node n = kids.item(i);
            if (n.getNodeType() != Node.ELEMENT_NODE) continue;
            if (!NS_TFD.equals(n.getNamespaceURI()) || !"TimbreFiscalDigital".equals(localName((Element) n))) continue;
            Element tfd = (Element) n;
            CFDI.Timbrado t = new CFDI.Timbrado();
            t.setVersion(attr(tfd, "Version"));
            t.setUuid(attr(tfd, "UUID"));
            t.setSelloCFDI(attr(tfd, "SelloCFD"));
            t.setSelloSAT(attr(tfd, "SelloSAT"));
            t.setNumCertificadoSAT(attr(tfd, "NoCertificadoSAT"));
            t.setRfcProvCertif(attr(tfd, "RfcProvCertif"));
            t.setLeyenda(attr(tfd, "Leyenda"));
            t.setFechaTimbrado(parseDate(attr(tfd, "FechaTimbrado")));
            return t;
        }
        return null;
    }

    // --- helpers ---
    private static String attr(Element e, String name) {
        if (e == null) return null;
        String v = e.getAttribute(name);
        return (v == null || v.isEmpty()) ? null : v;
    }

    private static String localName(Element e) {
        return e.getLocalName() != null ? e.getLocalName() : e.getTagName();
    }

    private static Element childCfdi(Element parent, String name) {
        if (parent == null) return null;
        NodeList kids = parent.getChildNodes();
        for (int i = 0; i < kids.getLength(); i++) {
            Node n = kids.item(i);
            if (n.getNodeType() != Node.ELEMENT_NODE) continue;
            Element el = (Element) n;
            if (NS_CFDI.equals(el.getNamespaceURI()) && name.equals(localName(el))) return el;
        }
        return null;
    }

    private static List<Element> childrenCfdi(Element parent, String name) {
        List<Element> out = new ArrayList<>();
        if (parent == null) return out;
        NodeList kids = parent.getChildNodes();
        for (int i = 0; i < kids.getLength(); i++) {
            Node n = kids.item(i);
            if (n.getNodeType() != Node.ELEMENT_NODE) continue;
            Element el = (Element) n;
            if (NS_CFDI.equals(el.getNamespaceURI()) && name.equals(localName(el))) out.add(el);
        }
        return out;
    }

    private static BigDecimal decimal(String s) {
        if (s == null || s.isBlank()) return null;
        try { return new BigDecimal(s); } catch (NumberFormatException e) { return null; }
    }

    private static java.util.Date parseDate(String s) {
        if (s == null || s.isBlank()) return null;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            return sdf.parse(s);
        } catch (Exception e) {
            return null;
        }
    }
}
