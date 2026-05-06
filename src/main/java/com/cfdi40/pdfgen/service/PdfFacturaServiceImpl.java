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

import com.itextpdf.text.*;
import com.itextpdf.text.Font.FontFamily;
import com.itextpdf.text.pdf.BarcodeQRCode;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.cfdi40.exceptionhandlerstarter.exception.BusinessException;
import com.cfdi40.pdfgen.dto.CatClaveProducto;
import com.cfdi40.pdfgen.dto.CatImpuestos;
import com.cfdi40.pdfgen.model.entity.CatIdiomapdf;
import com.cfdi40.pdfgen.model.entity.CatTagxidioma;
import com.cfdi40.pdfgen.model.entity.IdentificadorSucursal;
import com.cfdi40.pdfgen.model.repository.CatIdiomapdfRepository;
import com.cfdi40.pdfgen.model.repository.CatTagxidiomaRepository;
import com.cfdi40.pdfgen.util.Utilities;
import com.cfdi40.pdfgen.util.*;
import com.cfdi40.pdfgen.tenants.tenanta.cfdi.dto.CFDI;
import com.cfdi40.pdfgen.tenants.tenanta.cfdi.dto.CFDI.Complementos.ComplementoPago20;
import com.cfdi40.pdfgen.tenants.tenanta.cfdi.dto.CFDI.Complementos.ComplementoPago20.Pago;
import com.cfdi40.pdfgen.tenants.tenanta.cfdi.dto.CFDI.Complementos.ComplementoPago20.Pago.DoctoRelacionado;
import com.cfdi40.pdfgen.tenants.tenanta.cfdi.dto.CFDI.Complementos.ComplementoPago20.Pago.DoctoRelacionado.ImpuestosDR.RetencionesDR.RetencionDR;
import com.cfdi40.pdfgen.tenants.tenanta.cfdi.dto.CFDI.Complementos.ComplementoPago20.Pago.DoctoRelacionado.ImpuestosDR.TrasladosDR.TrasladoDR;
import com.cfdi40.pdfgen.tenants.tenanta.cfdi.dto.CFDI.Conceptos;
import com.cfdi40.pdfgen.tenants.tenanta.cfdi.dto.CFDI.Conceptos.CuentaPredial;
import com.cfdi40.pdfgen.tenants.tenanta.cfdi.dto.CFDI.Impuestos.Retencion;
import com.cfdi40.pdfgen.tenants.tenanta.cfdi.dto.CFDI.Impuestos.Traslado;
import com.cfdi40.pdfgen.tenants.tenanta.cfdi.dto.CFDI.ImpuestosLocales.TrasladoLocal;
import com.cfdi40.pdfgen.tenants.tenanta.cfdi.dto.CFDI.Relacionados;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;


@Service
@Slf4j
public class PdfFacturaServiceImpl implements PdfFacturaService {

    @Value("${url.servicio.logos}")
    private String urlGetLogo;
    @Value("${clave.producto}")
    private String claveProducto;

    @Autowired
    CatTagxidiomaRepository catTagxidiomaRepository; 
    @Autowired
    CatIdiomapdfRepository catIdiomapdfRepository;
    @org.springframework.beans.factory.annotation.Autowired
    CatalogStaticFallbackService catalogFallback;
    @Autowired
    CatalogosSATService catalogosSatService;
    
    Font fontContenido = new Font(FontFamily.HELVETICA, 7, Font.NORMAL, BaseColor.BLACK);
    Font fontEtiquetas = new Font(FontFamily.HELVETICA, 7, Font.BOLD, BaseColor.BLACK);
    Font fontLeyendas = new Font(FontFamily.HELVETICA, 6, Font.NORMAL, BaseColor.BLACK);
    private HashMap<EnumTagPlantilla, String> hmapTagIdioma;
    private List<CatTagxidioma> catTagxidioma ;
    private static final int ANCHO_TOTAL_TABLA = 560;
    private static final int PORCENTAJE_ANCHO_TABLA = 100;
    private final String EMPTY_VALUE = Strings.EMPTY;
    private static final float SPACING_AFTER_GENERIC_TABLE = 5F;

    @Override
    public ByteArrayOutputStream generarPDfFactura(CFDI facturaDto, IdentificadorSucursal identificadorSucursal) throws BusinessException {
        try {       
            ByteArrayOutputStream pdfResponse = null;
            CatIdiomapdf catIdioma = null;
                    
            switch (facturaDto.getIdioma() != null && !facturaDto.getIdioma().trim().isEmpty() ? facturaDto.getIdioma() : ConstantesFacto.IDIOMA_ESANOL) {
                case ConstantesFacto.IDIOMA_ESANOL:
                    catIdioma = catalogFallback.findIdiomaSafe(1);
                    catTagxidioma = catalogFallback.findTagsSafe(catIdioma);
                    hmapTagIdioma = Utilities.obtenerTagByIdioma(catTagxidioma);
                    break;

                case ConstantesFacto.IDIOMA_INGLES:
                    catIdioma = catalogFallback.findIdiomaSafe(2);
                    catTagxidioma = catalogFallback.findTagsSafe(catIdioma);
                    hmapTagIdioma = Utilities.obtenerTagByIdioma(catTagxidioma);
                    break;
                default:
                    break;
            }
            Document document = new Document();
            document = new Document(PageSize.LETTER, 10.0f, 10.0f, 36.0f, 36.0f);
            pdfResponse = new ByteArrayOutputStream();
            PdfWriter writer = PdfWriter.getInstance(document, pdfResponse);
            document.open();
            facturaDto.setFormato(
        Optional.ofNullable(facturaDto.getFormato()).orElse("00"));
            if (facturaDto.getFormato() != null) {
                switch (facturaDto.getFormato()) {
                    case "50":
                    case "51":
                    case "81":
                        agregarTableEncabezados(document, identificadorSucursal, facturaDto);
                        agregarTableDatosFactura(document, identificadorSucursal, facturaDto);
                        agregarTableInformacionGlobal(document, facturaDto);
                        agregarTableDatosCliente(document, facturaDto);
                        agregarTableDatosCfdiRelacionados(document, facturaDto);
                        agregarTableDatosClienteOpera(document, facturaDto);
                        agregarTableEncabezadoProducto(document);
                        agregarTableAgregaProducto(document, facturaDto);
                        agregarComplementoCartaPorte(document,facturaDto);
                        agregarTableSubtotales(document, facturaDto);
                        agregarTableMetodosDePago(document, facturaDto);
                        agregarComplementoPagos(document, facturaDto);
                        agregarTableSellos(document, facturaDto);
                        agregarTableLeyendas(document, facturaDto);

                        break;
                    case "00":
                    case "01":
                    case "02":
                    case "03":
                    case "04":
                    case "06":
                    case "21":
                    case "23":
                    case "80":
                        agregarTableEncabezados(document, identificadorSucursal, facturaDto);
                        agregarTableDatosFactura(document, identificadorSucursal, facturaDto);
                        agregarTableInformacionGlobal(document, facturaDto);
                        agregarTableDatosClienteEspecial(document, facturaDto);
                        agregarTableDatosClienteOperaSinVoucher(document, facturaDto);
                        agregarTableDatosCfdiRelacionados(document, facturaDto);
                        agregarTableEncabezadoProductoEspecial(document);
                        agregarTableAgregaProducto(document, facturaDto);
                        agregarComplementoCartaPorte(document,facturaDto);
                        agregarTableSubtotales(document, facturaDto);
                        agregarTableMetodosDePago(document, facturaDto);
                        agregarComplementoPagos(document, facturaDto);
                        agregarTableSellos(document, facturaDto);
                        agregarTableLeyendas(document, facturaDto);

                        break;
                    case "05":
                    case "20":
                        agregarTableEncabezados(document, identificadorSucursal, facturaDto);
                        agregarTableDatosFactura(document, identificadorSucursal, facturaDto);
                        agregarTableInformacionGlobal(document, facturaDto);
                        agregarTableDatosCliente(document, facturaDto);
                        agregarTableDatosCfdiRelacionados(document, facturaDto);
                        agregarTableDatosClienteOperaSinVoucher(document, facturaDto);
                        agregarTableEncabezadoProducto(document);
                        agregarTableAgregaProducto(document,facturaDto);
                        agregarComplementoCartaPorte(document,facturaDto);
                        agregarTableSubtotales(document, facturaDto);
                        agregarTableMetodosDePago(document, facturaDto);
                        agregarComplementoPagos(document,facturaDto);
                        agregarTableSellos(document,facturaDto);
                        agregarTableLeyendas(document, facturaDto);

                        break;
                    case "90":
                    case "91":
                        agregarTableEncabezados(document, identificadorSucursal, facturaDto);
                        agregarTableDatosFactura(document, identificadorSucursal, facturaDto);
                        agregarTableInformacionGlobal(document, facturaDto);
                        agregarTableDatosClienteEspecial(document,facturaDto);
                        agregarTableDatosClienteOperaEspecial(document, facturaDto);
                        agregarTableDatosCfdiRelacionados(document, facturaDto);
                        agregarTableEncabezadoProductoEspecial(document);
                        agregarTableAgregaProducto(document, facturaDto);
                        agregarComplementoCartaPorte(document,facturaDto);
                        agregarTableSubtotales(document, facturaDto);
                        agregarTableMetodosDePago(document, facturaDto);
                        agregarComplementoPagos(document, facturaDto);
                        agregarTableSellos(document,facturaDto);
                        agregarTableLeyendas(document, facturaDto);

                        break;
                    case "10":
                        agregarTableEncabezados(document, identificadorSucursal, facturaDto);
                        agregarTableDatosFactura(document, identificadorSucursal, facturaDto);
                        agregarTableInformacionGlobal(document,facturaDto);
                        agregarTableDatosCliente(document, facturaDto);
                        agregarTableDatosClienteOpera(document,facturaDto);
                        agregarTableDatosCfdiRelacionados(document,facturaDto);
                        agregarTableEncabezadoProducto(document);
                        agregarTableAgregaProductoSAT(document, facturaDto);
                        agregarComplementoCartaPorte(document,facturaDto);
                        agregarTableSubtotales(document, facturaDto);
                        agregarTableMetodosDePago(document, facturaDto);
                        agregarComplementoPagos(document, facturaDto);
                        agregarTableSellos(document, facturaDto);
                        agregarTableLeyendas(document, facturaDto);

                        break;
                    default:
                        agregarTableEncabezados(document, identificadorSucursal, facturaDto);
                        agregarTableDatosFactura(document, identificadorSucursal, facturaDto);
                        agregarTableInformacionGlobal(document, facturaDto);
                        agregarTableDatosCliente(document,facturaDto);
                        agregarTableDatosClienteOpera(document,facturaDto);
                        agregarTableDatosCfdiRelacionados(document,facturaDto);
                        agregarTableEncabezadoProducto(document);
                        agregarTableAgregaProducto(document,facturaDto);
                        agregarComplementoCartaPorte(document,facturaDto);
                        agregarTableSubtotales(document,facturaDto);
                        agregarTableMetodosDePago(document,facturaDto);
                        agregarComplementoPagos(document,facturaDto);
                        agregarTableSellos(document,facturaDto);
                        agregarTableLeyendas(document,facturaDto);

                        break;
                }
            } else {
                throw new Exception("Ocurrio un error al tratar de generar el PDF de Hoteleria, falta el campo formato");
            }
            document.close();
            writer.flush();
            return pdfResponse;
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException(HttpStatus.CONFLICT,"Ocurrio un error al tratar de generar el PDF de Hoteleria de la factura con UUID "
                    + (facturaDto.getTimbrado() != null ? facturaDto.getTimbrado().getUuid() : "") + " Error: " + e.getMessage());
        }
    }

    private void agregarComplementoCartaPorte(Document document, CFDI cfdi) throws DocumentException {

        if (cfdi.getComplementos() != null && cfdi.getComplementos().getCartaPorte() != null) {


            CFDI.Complementos.CartaPorte cartaPorte = cfdi.getComplementos().getCartaPorte();
            PdfPTable cartaPorteTable = getGenericTable(4, new int[]{145, 145, 145, 145}, ANCHO_TOTAL_TABLA, PORCENTAJE_ANCHO_TABLA, SPACING_AFTER_GENERIC_TABLE);

            PdfPCell cartaPorteHeader = getGenericHeaderCellLabel(LabelConstants.COMPL_CARTAP_HEADER.toUpperCase(), 4);
            cartaPorteHeader.setHorizontalAlignment(Element.ALIGN_CENTER);
            cartaPorteTable.addCell(cartaPorteHeader);

            addCellToTable(LabelConstants.COMPL_CARTAP_TRANSPORTE, cartaPorte.getTranspInternac(), cartaPorteTable);
            addCellToTable(LabelConstants.COMPL_CARTAP_ES_MERCANCIA, cartaPorte.getEntradaSalidaMerc(), cartaPorteTable);
            addCellToTable(LabelConstants.COMPL_CARTAP_PAIS, cartaPorte.getPaisOrigenDestino(), cartaPorteTable);
            addCellToTable(LabelConstants.COMPL_CARTAP_VIA_ES, cartaPorte.getViaEntradaSalida(), cartaPorteTable);
            addCellToTable(LabelConstants.COMPL_CARTAP_TOTAL_DISTANCIA, cartaPorte.getTotalDistRec() != null ? cartaPorte.getTotalDistRec().toPlainString() : null, cartaPorteTable);
            cartaPorteTable.addCell(getGenericEmptyCell(2));
            document.add(cartaPorteTable);

            if (cartaPorte.getUbicaciones() != null) {


                List<CFDI.Complementos.CartaPorte.Ubicaciones.Ubicacion> ubicaciones = Optional.ofNullable(cartaPorte.getUbicaciones().getUbicacion()).orElse(new ArrayList<>()).stream().filter(f -> f != null).collect(Collectors.toList());

                if (!ubicaciones.isEmpty()) {


                    PdfPTable ubicacionesTable = getGenericTable(8, new int[]{73, 73, 73, 73, 73, 73, 73, 73}, ANCHO_TOTAL_TABLA, PORCENTAJE_ANCHO_TABLA, SPACING_AFTER_GENERIC_TABLE);

                    PdfPCell ubicacionesHeader = getGenericHeaderCellLabel(LabelConstants.COMPL_CARTAP_UBICACIONES_HEADER, 8);
                    ubicacionesHeader.setHorizontalAlignment(Element.ALIGN_CENTER);
                    ubicacionesTable.addCell(ubicacionesHeader);

                    ListIterator<CFDI.Complementos.CartaPorte.Ubicaciones.Ubicacion> iterator = ubicaciones.listIterator();
                    while (iterator.hasNext()) {

                        CFDI.Complementos.CartaPorte.Ubicaciones.Ubicacion ubicacion = iterator.next();

                        addCellToTable(LabelConstants.COMPL_CARTAP_UBICACIONES_TIPO, ubicacion.getTipoUbicacion(), ubicacionesTable);
                        addCellToTable(LabelConstants.COMPL_CARTAP_UBICACIONES_ID, ubicacion.getIdUbicacion(), ubicacionesTable);
                        addCellToTable(LabelConstants.COMPL_CARTAP_UBICACIONES_RFC_REMI_DESTI, ubicacion.getRfcRemitenteDestinatario(), ubicacionesTable);
                        addCellToTable(LabelConstants.COMPL_CARTAP_UBICACIONES_NOM_REMI_DESTI, ubicacion.getNombreRemitenteDestinatario(), ubicacionesTable);
                        addCellToTable(LabelConstants.COMPL_CARTAP_UBICACIONES_REGIMEN, ubicacion.getNumRegIdTrib(), ubicacionesTable);
                        addCellToTable(LabelConstants.COMPL_CARTAP_UBICACIONES_RESIDENCIA, ubicacion.getResidenciaFiscal(), ubicacionesTable);
                        addCellToTable(LabelConstants.COMPL_CARTAP_UBICACIONES_NUM_ESTACION, ubicacion.getNumEstacion(), ubicacionesTable);
                        addCellToTable(LabelConstants.COMPL_CARTAP_UBICACIONES_NOMB_ESTACION, ubicacion.getNombreRemitenteDestinatario(), ubicacionesTable);
                        addCellToTable(LabelConstants.COMPL_CARTAP_UBICACIONES_NAVEGACION, ubicacion.getNavegacionTrafico(), ubicacionesTable);
                        addCellToTable(LabelConstants.COMPL_CARTAP_UBICACIONES_FECHA_SALIDA_LLEGADA, ubicacion.getFechaHoraSalidaLlegada() != null ? getStringAnioMesDiaHoraMinSeg(ubicacion.getFechaHoraSalidaLlegada()) : null, ubicacionesTable);
                        addCellToTable(LabelConstants.COMPL_CARTAP_UBICACIONES_TIPO_ESTACION, ubicacion.getTipoUbicacion(), ubicacionesTable);
                        addCellToTable(LabelConstants.COMPL_CARTAP_UBICACIONES_DISTANCIA, ubicacion.getDistanciaRecorrida() != null ? ubicacion.getDistanciaRecorrida().toPlainString() : null, ubicacionesTable);

                        if (ubicacion.getDomicilio() != null) {

                            PdfPTable direccionTable = getGenericTable(10, new int[]{50, 95, 50, 50, 50, 59, 59, 59, 59, 59}, ANCHO_TOTAL_TABLA, PORCENTAJE_ANCHO_TABLA, SPACING_AFTER_GENERIC_TABLE);

                            PdfPCell direccionHeader = new PdfPCell(new Phrase(LabelConstants.COMPL_CARTAP_DOMICILIO_HEADER, fontEtiquetas));
                            direccionHeader.setColspan(10);
                            direccionHeader.setBorder(Rectangle.BOTTOM);
                            direccionHeader.setHorizontalAlignment(Element.ALIGN_CENTER);
                            direccionTable.addCell(direccionHeader);

                            addCellToTable(LabelConstants.COMPL_CARTAP_DOMICILIO_CALLE, ubicacion.getDomicilio().getCalle(), direccionTable);
                            addCellToTable(LabelConstants.COMPL_CARTAP_DOMICILIO_NUM_EXT, ubicacion.getDomicilio().getNumeroExterior(), direccionTable);
                            addCellToTable(LabelConstants.COMPL_CARTAP_DOMICILIO_NUM_INT, ubicacion.getDomicilio().getNumeroInterior(), direccionTable);

                            addCellToTable(LabelConstants.COMPL_CARTAP_DOMICILIO_COLONIA, ubicacion.getDomicilio().getColonia(), direccionTable);

                            addCellToTable(LabelConstants.COMPL_CARTAP_DOMICILIO_LOCALIDAD, ubicacion.getDomicilio().getLocalidad(), direccionTable);
                            addCellToTable(LabelConstants.COMPL_CARTAP_DOMICILIO_REFERENCIA, ubicacion.getDomicilio().getReferencia(), direccionTable);

                            addCellToTable(LabelConstants.COMPL_CARTAP_DOMICILIO_MUNICIPIO, ubicacion.getDomicilio().getMunicipio(), direccionTable);

                            addCellToTable(LabelConstants.COMPL_CARTAP_DOMICILIO_ESTADO, ubicacion.getDomicilio().getEstado(), direccionTable);
                            addCellToTable(LabelConstants.COMPL_CARTAP_DOMICILIO_PAIS, ubicacion.getDomicilio().getPais(), direccionTable);
                            addCellToTable(LabelConstants.COMPL_CARTAP_DOMICILIO_CP, ubicacion.getDomicilio().getCodigoPostal(), direccionTable);


                            PdfPCell direccionCell = new PdfPCell(direccionTable);
                            direccionCell.setColspan(8);
                            direccionCell.setBorder(Rectangle.NO_BORDER);
                            ubicacionesTable.addCell(direccionCell);
                        }

                        if (iterator.hasNext()) {
                            PdfPCell breakLineCell = getGenericEmptyCell(8);

                            breakLineCell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                            breakLineCell.setBorder(Rectangle.NO_BORDER);
                            breakLineCell.setFixedHeight(4f);
                            ubicacionesTable.addCell(breakLineCell);
                        }
                    }
                    document.add(ubicacionesTable);
                }
            }


            if (cartaPorte.getMercancias() != null) {

                PdfPTable mercanciasTable = getGenericTable(10, new int[]{65, 20, 50, 40, 65, 20, 90, 30, 90, 20}, ANCHO_TOTAL_TABLA, PORCENTAJE_ANCHO_TABLA, SPACING_AFTER_GENERIC_TABLE);

                PdfPCell mercanciasHeader = getGenericHeaderCellLabel(LabelConstants.COMPL_CARTAP_MERCANCIAS_HEADER, 10);
                mercanciasHeader.setHorizontalAlignment(Element.ALIGN_CENTER);
                mercanciasTable.addCell(mercanciasHeader);

                addCellToTable(LabelConstants.COMPL_CARTAP_MERCANCIAS_PESO_BRUTO, cartaPorte.getMercancias().getPesoBrutoTotal() != null ? cartaPorte.getMercancias().getPesoBrutoTotal().toPlainString() : null, mercanciasTable);

                addCellToTable(LabelConstants.COMPL_CARTAP_MERCANCIAS_UNIDAD_PESO, cartaPorte.getMercancias().getUnidadPeso(), mercanciasTable);
                addCellToTable(LabelConstants.COMPL_CARTAP_MERCANCIAS_PESO_NETO, cartaPorte.getMercancias().getPesoNetoTotal() != null ? cartaPorte.getMercancias().getPesoNetoTotal().toPlainString() : null, mercanciasTable);
                addCellToTable(LabelConstants.COMPL_CARTAP_MERCANCIAS_NUM_TOTAL, cartaPorte.getMercancias().getNumTotalMercancias() > 0 ? String.valueOf(cartaPorte.getMercancias().getNumTotalMercancias()) : null, mercanciasTable);
                addCellToTable(LabelConstants.COMPL_CARTAP_MERCANCIAS_CARGO_TRANSACCION, cartaPorte.getMercancias().getCargoPorTasacion() != null ? cartaPorte.getMercancias().getCargoPorTasacion().toPlainString() : null, mercanciasTable);

                if (cartaPorte.getMercancias().getMercancia() != null && !cartaPorte.getMercancias().getMercancia().isEmpty()) {

                    PdfPTable mercanciaTable = getGenericTable(10, new int[]{95, 50, 50, 30, 70, 80, 70, 30, 59, 30}, ANCHO_TOTAL_TABLA, PORCENTAJE_ANCHO_TABLA, SPACING_AFTER_GENERIC_TABLE);

                    PdfPCell mercanciaHeader = getGenericHeaderCellLabel(LabelConstants.COMPL_CARTAP_MERCANCIAS_MER_HEADER, 10);
                    mercanciaHeader.setHorizontalAlignment(Element.ALIGN_CENTER);
                    mercanciaTable.addCell(mercanciaHeader);

                    ListIterator<CFDI.Complementos.CartaPorte.Mercancias.Mercancia> iterator = cartaPorte.getMercancias().getMercancia().listIterator();
                    while (iterator.hasNext()) {

                        CFDI.Complementos.CartaPorte.Mercancias.Mercancia mercancia = iterator.next();

                        addCellToTable(LabelConstants.COMPL_CARTAP_MERCANCIAS_MER_BIENES, mercancia.getBienesTransp(), mercanciaTable);
                        addCellToTable(LabelConstants.COMPL_CARTAP_MERCANCIAS_MER_CVE_STCC, mercancia.getClaveSTCC(), mercanciaTable);
                        addCellToTable(LabelConstants.COMPL_CARTAP_MERCANCIAS_MER_DESC, mercancia.getDescripcion(), mercanciaTable);
                        addCellToTable(LabelConstants.COMPL_CARTAP_MERCANCIAS_MER_CANTIDAD, mercancia.getCantidad() != null ? mercancia.getCantidad().toPlainString() : null, mercanciaTable);
                        addCellToTable(LabelConstants.COMPL_CARTAP_MERCANCIAS_MER_CVE_UNIDAD, mercancia.getClaveUnidad(), mercanciaTable);
                        addCellToTable(LabelConstants.COMPL_CARTAP_MERCANCIAS_MER_UNIDAD, mercancia.getUnidad(), mercanciaTable);
                        addCellToTable(LabelConstants.COMPL_CARTAP_MERCANCIAS_MER_DIMENSIONES, mercancia.getDimensiones(), mercanciaTable);
                        addCellToTable(LabelConstants.COMPL_CARTAP_MERCANCIAS_MER_MATERIAL, mercancia.getMaterialPeligroso(), mercanciaTable);
                        addCellToTable(LabelConstants.COMPL_CARTAP_MERCANCIAS_MER_CVE_MATERIAL, mercancia.getCveMaterialPeligroso(), mercanciaTable);
                        addCellToTable(LabelConstants.COMPL_CARTAP_MERCANCIAS_MER_EMBALAJE, mercancia.getEmbalaje(), mercanciaTable);
                        addCellToTable(LabelConstants.COMPL_CARTAP_MERCANCIAS_MER_DESC_EMBALAJE, mercancia.getDescripEmbalaje(), mercanciaTable);
                        addCellToTable(LabelConstants.COMPL_CARTAP_MERCANCIAS_MER_PESO, mercancia.getPesoEnKg() != null ? mercancia.getPesoEnKg().toPlainString() : null, mercanciaTable);
                        addCellToTable(LabelConstants.COMPL_CARTAP_MERCANCIAS_MER_VALOR, mercancia.getValorMercancia() != null ? mercancia.getValorMercancia().toPlainString() : null, mercanciaTable);

                        addCellToTable(LabelConstants.COMPL_CARTAP_MERCANCIAS_MER_MONEDA, mercancia.getMoneda(), mercanciaTable);
                        addCellToTable(LabelConstants.COMPL_CARTAP_MERCANCIAS_MER_FRACCION, mercancia.getFraccionArancelaria(), mercanciaTable);
                        addCellToTable(LabelConstants.COMPL_CARTAP_MERCANCIAS_MER_UUID, mercancia.getUuidComercioExt(), mercanciaTable);
                        mercanciaTable.addCell(getGenericEmptyCell(8));


                        if (mercancia.getPedimentos() != null && !mercancia.getPedimentos().isEmpty()) {

                            List<CFDI.Complementos.CartaPorte.Mercancias.Mercancia.Pedimentos> pedimentos = mercancia.getPedimentos().stream().filter(f -> f != null).collect(Collectors.toList());
                            if (!pedimentos.isEmpty()) {
                                PdfPTable pedimentosTable = getGenericTable(2, new int[]{50, 500}, ANCHO_TOTAL_TABLA, PORCENTAJE_ANCHO_TABLA, SPACING_AFTER_GENERIC_TABLE);

                                PdfPCell pedimentosHeader = new PdfPCell(new Phrase(LabelConstants.COMPL_CARTAP_MERCANCIAS_MER_PEDI_HEADER, fontEtiquetas));
                                pedimentosHeader.setColspan(2);
                                pedimentosHeader.setBorder(Rectangle.BOTTOM);
                                pedimentosHeader.setHorizontalAlignment(Element.ALIGN_CENTER);
                                pedimentosTable.addCell(pedimentosHeader);

                                addCellToTable(LabelConstants.COMPL_CARTAP_MERCANCIAS_MER_PEDI_PEDIMENTO, pedimentos.stream().map(CFDI.Complementos.CartaPorte.Mercancias.Mercancia.Pedimentos::getPedimento).collect(Collectors.joining(",")), pedimentosTable);

                                PdfPCell pedimentosCell = new PdfPCell(pedimentosTable);
                                pedimentosCell.setColspan(10);
                                pedimentosCell.setBorder(Rectangle.NO_BORDER);
                                mercanciaTable.addCell(pedimentosCell);

                            }
                        }

                        if (mercancia.getGuiasIdentificacion() != null && !mercancia.getGuiasIdentificacion().isEmpty()) {

                            List<CFDI.Complementos.CartaPorte.Mercancias.Mercancia.GuiasIdentificacion> listGuiasIdentificacion = mercancia.getGuiasIdentificacion().stream().filter(f -> f != null).collect(Collectors.toList());
                            if (!listGuiasIdentificacion.isEmpty()) {
                                PdfPTable guiasTable = getGenericTable(6, new int[]{98, 98, 98, 98, 98, 98}, ANCHO_TOTAL_TABLA, PORCENTAJE_ANCHO_TABLA, SPACING_AFTER_GENERIC_TABLE);

                                PdfPCell guiasHeader = new PdfPCell(new Phrase(LabelConstants.COMPL_CARTAP_MERCANCIAS_MER_GUIA_HEADER, fontEtiquetas));
                                guiasHeader.setColspan(6);
                                guiasHeader.setBorder(Rectangle.BOTTOM);
                                guiasHeader.setHorizontalAlignment(Element.ALIGN_CENTER);
                                guiasTable.addCell(guiasHeader);

                                for (CFDI.Complementos.CartaPorte.Mercancias.Mercancia.GuiasIdentificacion guiasIdentificacion : listGuiasIdentificacion) {

                                    addCellToTable(LabelConstants.COMPL_CARTAP_MERCANCIAS_MER_GUIA_NUMERO, guiasIdentificacion.getNumeroGuiaIdentificacion(), guiasTable);
                                    addCellToTable(LabelConstants.COMPL_CARTAP_MERCANCIAS_MER_GUIA_DESC, guiasIdentificacion.getDescripGuiaIdentificacion(), guiasTable);
                                    addCellToTable(LabelConstants.COMPL_CARTAP_MERCANCIAS_MER_GUIA_PESO, guiasIdentificacion.getPesoGuiaIdentificacion() != null ? guiasIdentificacion.getPesoGuiaIdentificacion().toPlainString() : null, guiasTable);

                                }


                                PdfPCell guiasCell = new PdfPCell(guiasTable);
                                guiasCell.setColspan(10);
                                guiasCell.setBorder(Rectangle.NO_BORDER);
                                mercanciaTable.addCell(guiasCell);

                            }
                        }

                        if (mercancia.getCantidadTransporta() != null && !mercancia.getCantidadTransporta().isEmpty()) {

                            List<CFDI.Complementos.CartaPorte.Mercancias.Mercancia.CantidadTransporta> listCantidadTransporta = mercancia.getCantidadTransporta().stream().filter(f -> f != null).collect(Collectors.toList());
                            if (!listCantidadTransporta.isEmpty()) {
                                PdfPTable table = getGenericTable(8, new int[]{73, 73, 73, 73, 73, 73, 73, 73}, ANCHO_TOTAL_TABLA, PORCENTAJE_ANCHO_TABLA, SPACING_AFTER_GENERIC_TABLE);

                                PdfPCell header = new PdfPCell(new Phrase(LabelConstants.COMPL_CARTAP_MERCANCIAS_MER_TRANSPORTA_HEADER, fontEtiquetas));
                                header.setColspan(8);
                                header.setBorder(Rectangle.BOTTOM);
                                header.setHorizontalAlignment(Element.ALIGN_CENTER);
                                table.addCell(header);

                                for (CFDI.Complementos.CartaPorte.Mercancias.Mercancia.CantidadTransporta cantidadTransporta : listCantidadTransporta) {

                                    addCellToTable(LabelConstants.COMPL_CARTAP_MERCANCIAS_MER_TRANSPORTA_CANTIDAD, cantidadTransporta.getCantidad() != null ? cantidadTransporta.getCantidad().toPlainString() : null, table);
                                    addCellToTable(LabelConstants.COMPL_CARTAP_MERCANCIAS_MER_TRANSPORTA_ID_ORIG, cantidadTransporta.getIdOrigen(), table);
                                    addCellToTable(LabelConstants.COMPL_CARTAP_MERCANCIAS_MER_TRANSPORTA_ID_DEST, cantidadTransporta.getIdDestino(), table);
                                    addCellToTable(LabelConstants.COMPL_CARTAP_MERCANCIAS_MER_TRANSPORTA_CVE_TRANSPORTE, cantidadTransporta.getCvesTransporte(), table);
                                }


                                PdfPCell cell = new PdfPCell(table);
                                cell.setColspan(10);
                                cell.setBorder(Rectangle.NO_BORDER);
                                mercanciaTable.addCell(cell);


                            }
                        }

                        if (mercancia.getDetalleMercancia() != null) {

                            PdfPTable table = getGenericTable(10, new int[]{59, 59, 59, 59, 59, 59, 59, 59, 59, 59}, ANCHO_TOTAL_TABLA, PORCENTAJE_ANCHO_TABLA, SPACING_AFTER_GENERIC_TABLE);

                            PdfPCell header = new PdfPCell(new Phrase(LabelConstants.COMPL_CARTAP_MERCANCIAS_MER_DETALLE_HEADER, fontEtiquetas));
                            header.setColspan(10);
                            header.setBorder(Rectangle.BOTTOM);
                            header.setHorizontalAlignment(Element.ALIGN_CENTER);
                            table.addCell(header);

                            addCellToTable(LabelConstants.COMPL_CARTAP_MERCANCIAS_MER_DETALLE_CANTIDAD, mercancia.getDetalleMercancia().getUnidadPesoMerc(), table);
                            addCellToTable(LabelConstants.COMPL_CARTAP_MERCANCIAS_MER_DETALLE_PESO_BRUTO, mercancia.getDetalleMercancia().getPesoBruto() != null ? mercancia.getDetalleMercancia().getPesoBruto().toPlainString() : null, table);
                            addCellToTable(LabelConstants.COMPL_CARTAP_MERCANCIAS_MER_DETALLE_PESO_NETO, mercancia.getDetalleMercancia().getPesoNeto() != null ? mercancia.getDetalleMercancia().getPesoNeto().toPlainString() : null, table);
                            addCellToTable(LabelConstants.COMPL_CARTAP_MERCANCIAS_MER_DETALLE_PESO_TARA, mercancia.getDetalleMercancia().getPesoTara() != null ? mercancia.getDetalleMercancia().getPesoTara().toPlainString() : null, table);
                            addCellToTable(LabelConstants.COMPL_CARTAP_MERCANCIAS_MER_DETALLE_PIEZAS, mercancia.getDetalleMercancia().getNumPiezas() != null ? mercancia.getDetalleMercancia().getNumPiezas().toString() : null, table);

                            PdfPCell cell = new PdfPCell(table);
                            cell.setColspan(10);
                            cell.setBorder(Rectangle.NO_BORDER);
                            mercanciaTable.addCell(cell);

                        }

                        if (iterator.hasNext()) {
                            PdfPCell breakLineCell = getGenericEmptyCell(10);
                            breakLineCell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                            breakLineCell.setBorder(Rectangle.NO_BORDER);
                            breakLineCell.setFixedHeight(4f);
                            mercanciaTable.addCell(breakLineCell);
                        }
                    }


                    PdfPCell mercanciaCell = new PdfPCell(mercanciaTable);
                    mercanciaCell.setColspan(10);
                    mercanciaCell.setBorder(Rectangle.NO_BORDER);
                    mercanciasTable.addCell(mercanciaCell);
                }


                if (cartaPorte.getMercancias().getAutotransporte() != null) {

                    PdfPTable autotransporteTable = getGenericTable(4, new int[]{59, 59, 59, 59}, ANCHO_TOTAL_TABLA, PORCENTAJE_ANCHO_TABLA, SPACING_AFTER_GENERIC_TABLE);

                    PdfPCell autotransporteHeader = getGenericHeaderCellLabel(LabelConstants.COMPL_CARTAP_MERCANCIAS_AUTO_HEADER, 4);
                    autotransporteHeader.setHorizontalAlignment(Element.ALIGN_CENTER);
                    autotransporteTable.addCell(autotransporteHeader);

                    addCellToTable(LabelConstants.COMPL_CARTAP_MERCANCIAS_AUTO_PERMISO_SCT, cartaPorte.getMercancias().getAutotransporte().getPermSCT(), autotransporteTable);
                    addCellToTable(LabelConstants.COMPL_CARTAP_MERCANCIAS_AUTO_NUM_SCT, cartaPorte.getMercancias().getAutotransporte().getNumPermisoSCT(), autotransporteTable);

                    if (cartaPorte.getMercancias().getAutotransporte().getIdentificacionVehicular() != null) {

                        PdfPTable table = getGenericTable(6, new int[]{98, 98, 98, 98, 98, 98}, ANCHO_TOTAL_TABLA, PORCENTAJE_ANCHO_TABLA, SPACING_AFTER_GENERIC_TABLE);

                        PdfPCell header = new PdfPCell(new Phrase(LabelConstants.COMPL_CARTAP_MERCANCIAS_AUTO_IDV_HEADER, fontEtiquetas));
                        header.setColspan(6);
                        header.setBorder(Rectangle.BOTTOM);
                        header.setHorizontalAlignment(Element.ALIGN_CENTER);
                        table.addCell(header);

                        addCellToTable(LabelConstants.COMPL_CARTAP_MERCANCIAS_AUTO_IDV_CONFIG, cartaPorte.getMercancias().getAutotransporte().getIdentificacionVehicular().getConfigVehicular(), table);
                        addCellToTable(LabelConstants.COMPL_CARTAP_MERCANCIAS_AUTO_IDV_PLACA, cartaPorte.getMercancias().getAutotransporte().getIdentificacionVehicular().getPlacaVM(), table);
                        addCellToTable(LabelConstants.COMPL_CARTAP_MERCANCIAS_AUTO_IDV_MODELO, String.valueOf(cartaPorte.getMercancias().getAutotransporte().getIdentificacionVehicular().getAnioModeloVM()), table);

                        PdfPCell cell = new PdfPCell(table);
                        cell.setColspan(4);
                        cell.setBorder(Rectangle.NO_BORDER);
                        autotransporteTable.addCell(cell);

                    }

                    if (cartaPorte.getMercancias().getAutotransporte().getSeguros() != null) {

                        PdfPTable table = getGenericTable(4, new int[]{147, 147, 194, 100}, ANCHO_TOTAL_TABLA, PORCENTAJE_ANCHO_TABLA, SPACING_AFTER_GENERIC_TABLE);

                        PdfPCell header = new PdfPCell(new Phrase(LabelConstants.COMPL_CARTAP_MERCANCIAS_AUTO_SEGURO_HEADER, fontEtiquetas));
                        header.setColspan(4);
                        header.setBorder(Rectangle.BOTTOM);
                        header.setHorizontalAlignment(Element.ALIGN_CENTER);
                        table.addCell(header);

                        addCellToTable(LabelConstants.COMPL_CARTAP_MERCANCIAS_AUTO_SEGURO_CIVIL, cartaPorte.getMercancias().getAutotransporte().getSeguros().getAseguraRespCivil(), table);
                        addCellToTable(LabelConstants.COMPL_CARTAP_MERCANCIAS_AUTO_SEGURO_POLIZA_CIVIL, cartaPorte.getMercancias().getAutotransporte().getSeguros().getPolizaRespCivil(), table);
                        addCellToTable(LabelConstants.COMPL_CARTAP_MERCANCIAS_AUTO_SEGURO_AMBIENTE, cartaPorte.getMercancias().getAutotransporte().getSeguros().getAseguraMedAmbiente(), table);
                        addCellToTable(LabelConstants.COMPL_CARTAP_MERCANCIAS_AUTO_SEGURO_POLIZA_AMBIENTE, cartaPorte.getMercancias().getAutotransporte().getSeguros().getPolizaMedAmbiente(), table);
                        addCellToTable(LabelConstants.COMPL_CARTAP_MERCANCIAS_AUTO_SEGURO_CARGA, cartaPorte.getMercancias().getAutotransporte().getSeguros().getAseguraCarga(), table);
                        addCellToTable(LabelConstants.COMPL_CARTAP_MERCANCIAS_AUTO_SEGURO_POLIZA_CARGA, cartaPorte.getMercancias().getAutotransporte().getSeguros().getPolizaCarga(), table);
                        addCellToTable(LabelConstants.COMPL_CARTAP_MERCANCIAS_AUTO_SEGURO_PRIMA, cartaPorte.getMercancias().getAutotransporte().getSeguros().getPrimaSeguro() != null ? cartaPorte.getMercancias().getAutotransporte().getSeguros().getPrimaSeguro().toPlainString() : null, table);
                        table.addCell(getGenericEmptyCell(2));
                        PdfPCell cell = new PdfPCell(table);
                        cell.setColspan(4);
                        cell.setBorder(Rectangle.NO_BORDER);
                        autotransporteTable.addCell(cell);

                    }

                    if (cartaPorte.getMercancias().getAutotransporte().getRemolques() != null && cartaPorte.getMercancias().getAutotransporte().getRemolques().getRemolque() != null && !cartaPorte.getMercancias().getAutotransporte().getRemolques().getRemolque().isEmpty()) {

                        List<CFDI.Complementos.CartaPorte.Mercancias.Autotransporte.Remolques.Remolque> listRemolques = cartaPorte.getMercancias().getAutotransporte().getRemolques().getRemolque().stream().filter(f -> f != null).collect(Collectors.toList());
                        if (!listRemolques.isEmpty()) {


                            PdfPTable table = getGenericTable(4, new int[]{147, 147, 147, 147}, ANCHO_TOTAL_TABLA, PORCENTAJE_ANCHO_TABLA, SPACING_AFTER_GENERIC_TABLE);

                            PdfPCell header = new PdfPCell(new Phrase(LabelConstants.COMPL_CARTAP_MERCANCIAS_AUTO_REMOLQUE_HEADER, fontEtiquetas));
                            header.setColspan(4);
                            header.setBorder(Rectangle.BOTTOM);
                            header.setHorizontalAlignment(Element.ALIGN_CENTER);
                            table.addCell(header);

                            for (CFDI.Complementos.CartaPorte.Mercancias.Autotransporte.Remolques.Remolque remolque : listRemolques) {

                                addCellToTable(LabelConstants.COMPL_CARTAP_MERCANCIAS_AUTO_REMOLQUE_SUBTIPO, remolque.getSubTipoRem(), table);
                                addCellToTable(LabelConstants.COMPL_CARTAP_MERCANCIAS_AUTO_REMOLQUE_PLACA, remolque.getPlaca(), table);

                            }


                            PdfPCell cell = new PdfPCell(table);
                            cell.setColspan(10);
                            cell.setBorder(Rectangle.NO_BORDER);
                            autotransporteTable.addCell(cell);
                        }
                    }

                    PdfPCell autotransporteCell = new PdfPCell(autotransporteTable);
                    autotransporteCell.setColspan(10);
                    autotransporteCell.setBorder(Rectangle.NO_BORDER);
                    mercanciasTable.addCell(autotransporteCell);

                }

                if (cartaPorte.getMercancias().getTransporteMaritimo() != null) {

                    PdfPTable transporteMaritimoTable = getGenericTable(10, new int[]{59, 59, 59, 59, 59, 59, 59, 59, 59, 59}, ANCHO_TOTAL_TABLA, PORCENTAJE_ANCHO_TABLA, SPACING_AFTER_GENERIC_TABLE);

                    PdfPCell transporteMaritimoHeader = getGenericHeaderCellLabel(LabelConstants.COMPL_CARTAP_MERCANCIAS_MARITIMO_HEADER, 10);
                    transporteMaritimoHeader.setHorizontalAlignment(Element.ALIGN_CENTER);
                    transporteMaritimoTable.addCell(transporteMaritimoHeader);

                    addCellToTable(LabelConstants.COMPL_CARTAP_MERCANCIAS_MARITIMO_PERMISO_SCT, cartaPorte.getMercancias().getTransporteMaritimo().getPermSCT(), transporteMaritimoTable);
                    addCellToTable(LabelConstants.COMPL_CARTAP_MERCANCIAS_MARITIMO_NUM_SCT, cartaPorte.getMercancias().getTransporteMaritimo().getNumPermisoSCT(), transporteMaritimoTable);
                    addCellToTable(LabelConstants.COMPL_CARTAP_MERCANCIAS_MARITIMO_ASEGURADORA, cartaPorte.getMercancias().getTransporteMaritimo().getNombreAseg(), transporteMaritimoTable);
                    addCellToTable(LabelConstants.COMPL_CARTAP_MERCANCIAS_MARITIMO_POLIZA, cartaPorte.getMercancias().getTransporteMaritimo().getNumPolizaSeguro(), transporteMaritimoTable);

                    addCellToTable(LabelConstants.COMPL_CARTAP_MERCANCIAS_MARITIMO_TIPO_EMBARCA, cartaPorte.getMercancias().getTransporteMaritimo().getTipoEmbarcacion(), transporteMaritimoTable);
                    addCellToTable(LabelConstants.COMPL_CARTAP_MERCANCIAS_MARITIMO_MATRICULA, cartaPorte.getMercancias().getTransporteMaritimo().getMatricula(), transporteMaritimoTable);
                    addCellToTable(LabelConstants.COMPL_CARTAP_MERCANCIAS_MARITIMO_NUM_OMI, cartaPorte.getMercancias().getTransporteMaritimo().getNumeroOMI(), transporteMaritimoTable);
                    addCellToTable(LabelConstants.COMPL_CARTAP_MERCANCIAS_MARITIMO_ANIO_EMBARCA, cartaPorte.getMercancias().getTransporteMaritimo().getAnioEmbarcacion() != null ? cartaPorte.getMercancias().getTransporteMaritimo().getAnioEmbarcacion().toString() : null, transporteMaritimoTable);
                    addCellToTable(LabelConstants.COMPL_CARTAP_MERCANCIAS_MARITIMO_NOMB_EMBARCA, cartaPorte.getMercancias().getTransporteMaritimo().getNombreEmbarc(), transporteMaritimoTable);

                    addCellToTable(LabelConstants.COMPL_CARTAP_MERCANCIAS_MARITIMO_NACIO_EMBARCA, cartaPorte.getMercancias().getTransporteMaritimo().getNacionalidadEmbarc(), transporteMaritimoTable);
                    addCellToTable(LabelConstants.COMPL_CARTAP_MERCANCIAS_MARITIMO_UNIDAD_ARQUEO, cartaPorte.getMercancias().getTransporteMaritimo().getUnidadesDeArqBruto() != null ? cartaPorte.getMercancias().getTransporteMaritimo().getUnidadesDeArqBruto().toPlainString() : null, transporteMaritimoTable);

                    addCellToTable(LabelConstants.COMPL_CARTAP_MERCANCIAS_MARITIMO_TIPO_CARGA, cartaPorte.getMercancias().getTransporteMaritimo().getTipoCarga(), transporteMaritimoTable);
                    addCellToTable(LabelConstants.COMPL_CARTAP_MERCANCIAS_MARITIMO_NUM_CER_ITC, cartaPorte.getMercancias().getTransporteMaritimo().getNumCertITC(), transporteMaritimoTable);
                    addCellToTable(LabelConstants.COMPL_CARTAP_MERCANCIAS_MARITIMO_ESLORA, cartaPorte.getMercancias().getTransporteMaritimo().getEslora() != null ? cartaPorte.getMercancias().getTransporteMaritimo().getEslora().toPlainString() : null, transporteMaritimoTable);
                    addCellToTable(LabelConstants.COMPL_CARTAP_MERCANCIAS_MARITIMO_MANGA, cartaPorte.getMercancias().getTransporteMaritimo().getManga() != null ? cartaPorte.getMercancias().getTransporteMaritimo().getManga().toPlainString() : null, transporteMaritimoTable);
                    addCellToTable(LabelConstants.COMPL_CARTAP_MERCANCIAS_MARITIMO_CALADO, cartaPorte.getMercancias().getTransporteMaritimo().getCalado() != null ? cartaPorte.getMercancias().getTransporteMaritimo().getCalado().toPlainString() : null, transporteMaritimoTable);
                    addCellToTable(LabelConstants.COMPL_CARTAP_MERCANCIAS_MARITIMO_LINEA_NAV, cartaPorte.getMercancias().getTransporteMaritimo().getLineaNaviera(), transporteMaritimoTable);
                    addCellToTable(LabelConstants.COMPL_CARTAP_MERCANCIAS_MARITIMO_AGENTE, cartaPorte.getMercancias().getTransporteMaritimo().getNombreAgenteNaviero(), transporteMaritimoTable);

                    addCellToTable(LabelConstants.COMPL_CARTAP_MERCANCIAS_MARITIMO_NUM_AUTO, cartaPorte.getMercancias().getTransporteMaritimo().getNumAutorizacionNaviero(), transporteMaritimoTable);
                    addCellToTable(LabelConstants.COMPL_CARTAP_MERCANCIAS_MARITIMO_NUM_VIAJE, cartaPorte.getMercancias().getTransporteMaritimo().getNumViaje(), transporteMaritimoTable);
                    addCellToTable(LabelConstants.COMPL_CARTAP_MERCANCIAS_MARITIMO_NUM_EMBARQUE, cartaPorte.getMercancias().getTransporteMaritimo().getNumConocEmbarc(), transporteMaritimoTable);
                    transporteMaritimoTable.addCell(getGenericEmptyCell(8));

                    if (cartaPorte.getMercancias().getTransporteMaritimo().getContenedor() != null && !cartaPorte.getMercancias().getTransporteMaritimo().getContenedor().isEmpty()) {


                        PdfPTable table = getGenericTable(6, new int[]{98, 98, 98, 98, 98, 98}, ANCHO_TOTAL_TABLA, PORCENTAJE_ANCHO_TABLA, SPACING_AFTER_GENERIC_TABLE);

                        PdfPCell header = new PdfPCell(new Phrase(LabelConstants.COMPL_CARTAP_MERCANCIAS_MARITIMO_CONT_HEADER, fontEtiquetas));
                        header.setColspan(6);
                        header.setBorder(Rectangle.BOTTOM);
                        header.setHorizontalAlignment(Element.ALIGN_CENTER);
                        table.addCell(header);

                        for (CFDI.Complementos.CartaPorte.Mercancias.TransporteMaritimo.Contenedor contenedor : cartaPorte.getMercancias().getTransporteMaritimo().getContenedor()) {

                            addCellToTable(LabelConstants.COMPL_CARTAP_MERCANCIAS_MARITIMO_CONT_MATRICULA, contenedor.getMatriculaContenedor(), table);

                            addCellToTable(LabelConstants.COMPL_CARTAP_MERCANCIAS_MARITIMO_CONT_TIPO, contenedor.getTipoContenedor(), table);
                            addCellToTable(LabelConstants.COMPL_CARTAP_MERCANCIAS_MARITIMO_CONT_PRECINTO, contenedor.getNumPrecinto(), table);
                        }


                        PdfPCell cell = new PdfPCell(table);
                        cell.setColspan(10);
                        cell.setBorder(Rectangle.NO_BORDER);
                        transporteMaritimoTable.addCell(cell);
                    }

                    PdfPCell transporteMaritimoCell = new PdfPCell(transporteMaritimoTable);
                    transporteMaritimoCell.setColspan(10);
                    transporteMaritimoCell.setBorder(Rectangle.NO_BORDER);
                    mercanciasTable.addCell(transporteMaritimoCell);

                }

                if (cartaPorte.getMercancias().getTransporteAereo() != null) {

                    PdfPTable transporteAereoTable = getGenericTable(10, new int[]{59, 59, 59, 59, 59, 59, 59, 59, 59, 59}, ANCHO_TOTAL_TABLA, PORCENTAJE_ANCHO_TABLA, SPACING_AFTER_GENERIC_TABLE);

                    PdfPCell transporteAereoHeader = getGenericHeaderCellLabel(LabelConstants.COMPL_CARTAP_MERCANCIAS_AEREO_HEADER, 10);
                    transporteAereoHeader.setHorizontalAlignment(Element.ALIGN_CENTER);
                    transporteAereoTable.addCell(transporteAereoHeader);
                    addCellToTable(LabelConstants.COMPL_CARTAP_MERCANCIAS_AEREO_PERMISO_SCT, cartaPorte.getMercancias().getTransporteAereo().getPermSCT(), transporteAereoTable);
                    addCellToTable(LabelConstants.COMPL_CARTAP_MERCANCIAS_AEREO_NUM_SCT, cartaPorte.getMercancias().getTransporteAereo().getNumPermisoSCT(), transporteAereoTable);
                    addCellToTable(LabelConstants.COMPL_CARTAP_MERCANCIAS_AEREO_MATRICULA, cartaPorte.getMercancias().getTransporteAereo().getMatriculaAeronave(), transporteAereoTable);
                    addCellToTable(LabelConstants.COMPL_CARTAP_MERCANCIAS_AEREO_ASEGURADORA, cartaPorte.getMercancias().getTransporteAereo().getNombreAseg(), transporteAereoTable);
                    addCellToTable(LabelConstants.COMPL_CARTAP_MERCANCIAS_AEREO_POLIZA, cartaPorte.getMercancias().getTransporteAereo().getNumPolizaSeguro(), transporteAereoTable);
                    addCellToTable(LabelConstants.COMPL_CARTAP_MERCANCIAS_AEREO_GUIA, cartaPorte.getMercancias().getTransporteAereo().getNumeroGuia(), transporteAereoTable);
                    addCellToTable(LabelConstants.COMPL_CARTAP_MERCANCIAS_AEREO_CONTRATO, cartaPorte.getMercancias().getTransporteAereo().getLugarContrato(), transporteAereoTable);

                    addCellToTable(LabelConstants.COMPL_CARTAP_MERCANCIAS_AEREO_TRANSPORTISTA, cartaPorte.getMercancias().getTransporteAereo().getCodigoTransportista(), transporteAereoTable);
                    addCellToTable(LabelConstants.COMPL_CARTAP_MERCANCIAS_AEREO_EMBARCADOR, cartaPorte.getMercancias().getTransporteAereo().getRfcEmbarcador(), transporteAereoTable);
                    addCellToTable(LabelConstants.COMPL_CARTAP_MERCANCIAS_AEREO_REGIMEN, cartaPorte.getMercancias().getTransporteAereo().getNumRegIdTribEmbarc(), transporteAereoTable);

                    addCellToTable(LabelConstants.COMPL_CARTAP_MERCANCIAS_AEREO_RESIDENCIA, cartaPorte.getMercancias().getTransporteAereo().getResidenciaFiscalEmbarc(), transporteAereoTable);
                    addCellToTable(LabelConstants.COMPL_CARTAP_MERCANCIAS_AEREO_NOMBRE_EMBARCADOR, cartaPorte.getMercancias().getTransporteAereo().getNombreEmbarcador(), transporteAereoTable);
                    transporteAereoTable.addCell(getGenericEmptyCell(6));
                    PdfPCell transporteAereoCell = new PdfPCell(transporteAereoTable);
                    transporteAereoCell.setColspan(10);
                    transporteAereoCell.setBorder(Rectangle.NO_BORDER);
                    mercanciasTable.addCell(transporteAereoCell);

                }

                if (cartaPorte.getMercancias().getTransporteFerroviario() != null) {

                    PdfPTable transporteFerroTable = getGenericTable(8, new int[]{73, 73, 73, 73, 73, 73, 73, 73}, ANCHO_TOTAL_TABLA, PORCENTAJE_ANCHO_TABLA, SPACING_AFTER_GENERIC_TABLE);

                    PdfPCell transporteFerroHeader = getGenericHeaderCellLabel(LabelConstants.COMPL_CARTAP_MERCANCIAS_FERRO_HEADER, 8);
                    transporteFerroHeader.setHorizontalAlignment(Element.ALIGN_CENTER);
                    transporteFerroTable.addCell(transporteFerroHeader);
                    addCellToTable(LabelConstants.COMPL_CARTAP_MERCANCIAS_FERRO_TIPO_SERV, cartaPorte.getMercancias().getTransporteFerroviario().getTipoDeServicio(), transporteFerroTable);

                    addCellToTable(LabelConstants.COMPL_CARTAP_MERCANCIAS_FERRO_TIPO_TRAF, cartaPorte.getMercancias().getTransporteFerroviario().getTipoDeTrafico(), transporteFerroTable);
                    addCellToTable(LabelConstants.COMPL_CARTAP_MERCANCIAS_FERRO_ASEGURADORA, cartaPorte.getMercancias().getTransporteFerroviario().getNombreAseg(), transporteFerroTable);
                    addCellToTable(LabelConstants.COMPL_CARTAP_MERCANCIAS_FERRO_POLIZA, cartaPorte.getMercancias().getTransporteFerroviario().getNumPolizaSeguro(), transporteFerroTable);


                    if (cartaPorte.getMercancias().getTransporteFerroviario().getDerechosDePaso() != null && !cartaPorte.getMercancias().getTransporteFerroviario().getDerechosDePaso().isEmpty()) {


                        PdfPTable table = getGenericTable(4, new int[]{147, 147, 147, 147}, ANCHO_TOTAL_TABLA, PORCENTAJE_ANCHO_TABLA, SPACING_AFTER_GENERIC_TABLE);

                        PdfPCell header = new PdfPCell(new Phrase(LabelConstants.COMPL_CARTAP_MERCANCIAS_FERRO_PASO_HEADER, fontEtiquetas));
                        header.setColspan(4);
                        header.setBorder(Rectangle.BOTTOM);
                        header.setHorizontalAlignment(Element.ALIGN_CENTER);
                        table.addCell(header);

                        for (CFDI.Complementos.CartaPorte.Mercancias.TransporteFerroviario.DerechosDePaso derechosDePaso : cartaPorte.getMercancias().getTransporteFerroviario().getDerechosDePaso()) {

                            addCellToTable(LabelConstants.COMPL_CARTAP_MERCANCIAS_FERRO_PASO_TIPO, derechosDePaso.getTipoDerechoDePaso(), table);
                            addCellToTable(LabelConstants.COMPL_CARTAP_MERCANCIAS_FERRO_PASO_KM, derechosDePaso.getKilometrajePagado() != null ? derechosDePaso.getKilometrajePagado().toPlainString() : null, table);
                        }


                        PdfPCell cell = new PdfPCell(table);
                        cell.setColspan(8);
                        cell.setBorder(Rectangle.NO_BORDER);
                        transporteFerroTable.addCell(cell);
                    }

                    if (cartaPorte.getMercancias().getTransporteFerroviario().getCarro() != null && !cartaPorte.getMercancias().getTransporteFerroviario().getCarro().isEmpty()) {


                        PdfPTable table = getGenericTable(8, new int[]{73, 73, 73, 73, 73, 73, 73, 73}, ANCHO_TOTAL_TABLA, PORCENTAJE_ANCHO_TABLA, SPACING_AFTER_GENERIC_TABLE);

                        PdfPCell header = new PdfPCell(new Phrase(LabelConstants.COMPL_CARTAP_MERCANCIAS_FERRO_CARRO_HEADER, fontEtiquetas));
                        header.setColspan(8);
                        header.setBorder(Rectangle.BOTTOM);
                        header.setHorizontalAlignment(Element.ALIGN_CENTER);
                        table.addCell(header);

                        for (CFDI.Complementos.CartaPorte.Mercancias.TransporteFerroviario.Carro carro : cartaPorte.getMercancias().getTransporteFerroviario().getCarro()) {

                            addCellToTable(LabelConstants.COMPL_CARTAP_MERCANCIAS_FERRO_CARRO_TIPO, carro.getTipoCarro(), table);
                            addCellToTable(LabelConstants.COMPL_CARTAP_MERCANCIAS_FERRO_CARRO_MATRICULA, carro.getMatriculaCarro(), table);
                            addCellToTable(LabelConstants.COMPL_CARTAP_MERCANCIAS_FERRO_CARRO_GUIA, carro.getGuiaCarro(), table);
                            addCellToTable(LabelConstants.COMPL_CARTAP_MERCANCIAS_FERRO_CARRO_TONELADAS, carro.getToneladasNetasCarro() != null ? carro.getToneladasNetasCarro().toPlainString() : null, table);
                        }


                        PdfPCell cell = new PdfPCell(table);
                        cell.setColspan(8);
                        cell.setBorder(Rectangle.NO_BORDER);
                        transporteFerroTable.addCell(cell);
                    }

                    PdfPCell transporteFerroCell = new PdfPCell(transporteFerroTable);
                    transporteFerroCell.setColspan(10);
                    transporteFerroCell.setBorder(Rectangle.NO_BORDER);
                    mercanciasTable.addCell(transporteFerroCell);
                }


                document.add(mercanciasTable);

            }


            if (cartaPorte.getFiguraTransporte() != null && cartaPorte.getFiguraTransporte().getTiposFigura() != null && !cartaPorte.getFiguraTransporte().getTiposFigura().isEmpty()) {

                List<CFDI.Complementos.CartaPorte.FiguraTransporte.TiposFigura> listTiposFigura = cartaPorte.getFiguraTransporte().getTiposFigura().stream().filter(f -> f != null).collect(Collectors.toList());
                if (!listTiposFigura.isEmpty()) {

                    PdfPTable tiposFiguraTable = getGenericTable(10, new int[]{35, 59, 30, 74, 68, 68, 59, 59, 98, 40}, ANCHO_TOTAL_TABLA, PORCENTAJE_ANCHO_TABLA, SPACING_AFTER_GENERIC_TABLE);

                    PdfPCell tiposFiguraHeader = getGenericHeaderCellLabel(LabelConstants.COMPL_CARTAP_FIGURA_TRANSPORTE_HEADER, 10);
                    tiposFiguraHeader.setHorizontalAlignment(Element.ALIGN_CENTER);
                    tiposFiguraTable.addCell(tiposFiguraHeader);
                    ListIterator<CFDI.Complementos.CartaPorte.FiguraTransporte.TiposFigura> iterator = listTiposFigura.listIterator();
                    while (iterator.hasNext()) {

                        CFDI.Complementos.CartaPorte.FiguraTransporte.TiposFigura tiposFigura = iterator.next();
                        addCellToTable(LabelConstants.COMPL_CARTAP_FIGURA_TRANSPORTE_TIPO, tiposFigura.getTipoFigura(), tiposFiguraTable);
                        addCellToTable(LabelConstants.COMPL_CARTAP_FIGURA_TRANSPORTE_RFC, tiposFigura.getRfcFigura(), tiposFiguraTable);
                        addCellToTable(LabelConstants.COMPL_CARTAP_FIGURA_TRANSPORTE_LICENCIA, tiposFigura.getNumLicencia(), tiposFiguraTable);

                        addCellToTable(LabelConstants.COMPL_CARTAP_FIGURA_TRANSPORTE_RESIDENCIA, tiposFigura.getResidenciaFiscalFigura(), tiposFiguraTable);

                        addCellToTable(LabelConstants.COMPL_CARTAP_FIGURA_TRANSPORTE_REGIMEN, tiposFigura.getNumRegIdTribFigura(), tiposFiguraTable);
                        addCellToTable(LabelConstants.COMPL_CARTAP_FIGURA_TRANSPORTE_NOMBRE, tiposFigura.getNombreFigura(), tiposFiguraTable);
                        tiposFiguraTable.addCell(getGenericEmptyCell(8));

                        if (tiposFigura.getPartesTransporte() != null && !tiposFigura.getPartesTransporte().isEmpty()) {

                            List<CFDI.Complementos.CartaPorte.FiguraTransporte.PartesTransporte> listPartesTransporte = tiposFigura.getPartesTransporte().stream().filter(f -> f != null).collect(Collectors.toList());
                            if (!listPartesTransporte.isEmpty()) {


                                PdfPTable partesTransporteTable = getGenericTable(2, new int[]{95, 400}, ANCHO_TOTAL_TABLA, PORCENTAJE_ANCHO_TABLA, SPACING_AFTER_GENERIC_TABLE);

                                PdfPCell partesTransporteHeader = new PdfPCell(new Phrase(LabelConstants.COMPL_CARTAP_FIGURA_TRANSPORTE_PARTE_HEADER, fontEtiquetas));
                                partesTransporteHeader.setColspan(2);
                                partesTransporteHeader.setBorder(Rectangle.BOTTOM);
                                partesTransporteHeader.setHorizontalAlignment(Element.ALIGN_CENTER);
                                partesTransporteTable.addCell(partesTransporteHeader);

                                addCellToTable(LabelConstants.COMPL_CARTAP_FIGURA_TRANSPORTE_PARTE_PARTE, listPartesTransporte.stream().map(CFDI.Complementos.CartaPorte.FiguraTransporte.PartesTransporte::getParteTransporte).collect(Collectors.joining(",")), partesTransporteTable);

                                PdfPCell parteTransporteCell = new PdfPCell(partesTransporteTable);
                                parteTransporteCell.setColspan(10);
                                parteTransporteCell.setBorder(Rectangle.NO_BORDER);
                                tiposFiguraTable.addCell(parteTransporteCell);
                            }

                            if (tiposFigura.getDomicilio() != null) {

                                PdfPTable direccionTable = getGenericTable(10, new int[]{50, 95, 50, 50, 50, 59, 59, 59, 59, 59}, ANCHO_TOTAL_TABLA, PORCENTAJE_ANCHO_TABLA, SPACING_AFTER_GENERIC_TABLE);

                                PdfPCell direccionHeader = new PdfPCell(new Phrase(LabelConstants.COMPL_CARTAP_DOMICILIO_HEADER, fontEtiquetas));
                                direccionHeader.setColspan(10);
                                direccionHeader.setBorder(Rectangle.BOTTOM);
                                direccionHeader.setHorizontalAlignment(Element.ALIGN_CENTER);
                                direccionTable.addCell(direccionHeader);

                                addCellToTable(LabelConstants.COMPL_CARTAP_DOMICILIO_CALLE, tiposFigura.getDomicilio().getCalle(), direccionTable);
                                addCellToTable(LabelConstants.COMPL_CARTAP_DOMICILIO_NUM_EXT, tiposFigura.getDomicilio().getNumeroExterior(), direccionTable);
                                addCellToTable(LabelConstants.COMPL_CARTAP_DOMICILIO_NUM_INT, tiposFigura.getDomicilio().getNumeroInterior(), direccionTable);

                                addCellToTable(LabelConstants.COMPL_CARTAP_DOMICILIO_COLONIA, tiposFigura.getDomicilio().getColonia(), direccionTable);

                                addCellToTable(LabelConstants.COMPL_CARTAP_DOMICILIO_LOCALIDAD, tiposFigura.getDomicilio().getLocalidad(), direccionTable);
                                addCellToTable(LabelConstants.COMPL_CARTAP_DOMICILIO_REFERENCIA, tiposFigura.getDomicilio().getReferencia(), direccionTable);

                                addCellToTable(LabelConstants.COMPL_CARTAP_DOMICILIO_MUNICIPIO, tiposFigura.getDomicilio().getMunicipio(), direccionTable);

                                addCellToTable(LabelConstants.COMPL_CARTAP_DOMICILIO_ESTADO, tiposFigura.getDomicilio().getEstado(), direccionTable);
                                addCellToTable(LabelConstants.COMPL_CARTAP_DOMICILIO_PAIS, tiposFigura.getDomicilio().getPais(), direccionTable);
                                addCellToTable(LabelConstants.COMPL_CARTAP_DOMICILIO_CP, tiposFigura.getDomicilio().getCodigoPostal(), direccionTable);

                                PdfPCell direccionCell = new PdfPCell(direccionTable);
                                direccionCell.setColspan(10);
                                direccionCell.setBorder(Rectangle.NO_BORDER);
                                tiposFiguraTable.addCell(direccionCell);
                            }


                            if (iterator.hasNext()) {
                                PdfPCell breakLineCell = getGenericEmptyCell(10);
                                breakLineCell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                                breakLineCell.setBorder(Rectangle.NO_BORDER);
                                breakLineCell.setFixedHeight(4f);
                                tiposFiguraTable.addCell(breakLineCell);
                            }
                        }
                    }

                    document.add(tiposFiguraTable);

                }
            }
        }


    }

    /**
     * Metodo para agregar los encabezados al PDF logo, datos de la sucursal *
     * Todos los formatos
     *
     * @param document
     * @throws BusinessException
     * @throws DocumentException
     */
    private void agregarTableEncabezados(Document document, IdentificadorSucursal identificadorSucursal, CFDI facturaDto) throws  DocumentException {
        PdfPTable tableEncabezado = new PdfPTable(3);
        tableEncabezado.setWidths(new int[]{150, 190, 200});
        tableEncabezado.setTotalWidth(ANCHO_TOTAL_TABLA);
        tableEncabezado.setLockedWidth(true);
        tableEncabezado.setWidthPercentage(PORCENTAJE_ANCHO_TABLA);
        tableEncabezado.getDefaultCell().setFixedHeight(90f);
        tableEncabezado.getDefaultCell().setBorder(Rectangle.NO_BORDER);

        PdfPTable tableLogo = new PdfPTable(1);
        tableLogo.setWidths(new int[]{140});
        tableLogo.setTotalWidth(100);
        tableLogo.setLockedWidth(true);
        tableLogo.setWidthPercentage(PORCENTAJE_ANCHO_TABLA);

        try {
            PdfPCell celdaLogo = new PdfPCell();

            byte[] bytesLogo = getLogo(identificadorSucursal.getEmisorId().getRfc(),identificadorSucursal.getSucursalId().getNombreLogo());

            //if (!StringUtils.isNotBlank(rutaLogo)) {
            if (bytesLogo!=null) {
                Image logo = Image.getInstance(bytesLogo);
                logo.scaleToFit(95,80);
                celdaLogo.addElement(logo);
                celdaLogo.setHorizontalAlignment(Element.ALIGN_RIGHT);
                celdaLogo.setBorder(Rectangle.NO_BORDER);
                tableLogo.addCell(celdaLogo);
            }
        } catch (Exception e) {
            e.printStackTrace();
            PdfPCell celdaLogo = new PdfPCell();
            celdaLogo.setBorder(Rectangle.NO_BORDER);
            tableLogo.addCell(celdaLogo);
        }

        PdfPTable tableDatosReceptor = new PdfPTable(1);
        tableDatosReceptor.setWidths(new int[]{150});
        tableDatosReceptor.setTotalWidth(150);
        tableDatosReceptor.setLockedWidth(true);
        tableDatosReceptor.setWidthPercentage(PORCENTAJE_ANCHO_TABLA);
        tableDatosReceptor.getDefaultCell().setBorder(Rectangle.NO_BORDER);

        PdfPCell expedidoEnC = new PdfPCell(new Paragraph(new Phrase(hmapTagIdioma.get(EnumTagPlantilla.P_LUGAREXPEDICION), fontContenido)));
        expedidoEnC.setHorizontalAlignment(Element.ALIGN_LEFT);
        expedidoEnC.setPaddingBottom(1f);
        expedidoEnC.setBorder(Rectangle.NO_BORDER);

        PdfPCell expedidoEnV = new PdfPCell(new Paragraph((identificadorSucursal.getSucursalId().getCalle() != null ? identificadorSucursal.getSucursalId().getCalle() : "").concat(" ")
                .concat(identificadorSucursal.getSucursalId().getNumeroInterior() != null ? identificadorSucursal.getSucursalId().getNumeroInterior() : "").concat(" ")
                .concat(identificadorSucursal.getSucursalId().getNumeroExterior() != null ? identificadorSucursal.getSucursalId().getNumeroExterior() : ""), fontContenido));
        expedidoEnV.setHorizontalAlignment(Element.ALIGN_LEFT);
        expedidoEnV.setPaddingBottom(10f);
        expedidoEnV.setBorder(Rectangle.NO_BORDER);

        PdfPCell estadoV = new PdfPCell(new Paragraph((identificadorSucursal.getSucursalId().getColonia() != null ? identificadorSucursal.getSucursalId().getColonia() : "").concat(" ")
                .concat(identificadorSucursal.getSucursalId().getCiudadDelegacion() != null ? identificadorSucursal.getSucursalId().getCiudadDelegacion() : ""), fontContenido));
        estadoV.setHorizontalAlignment(Element.ALIGN_LEFT);
        estadoV.setPaddingBottom(1f);
        estadoV.setBorder(Rectangle.NO_BORDER);

        StringBuilder paisEmisor = new StringBuilder();
        paisEmisor.append(identificadorSucursal.getSucursalId().getEstado() != null ? identificadorSucursal.getSucursalId().getEstado() : "")
                .append(" ").append(identificadorSucursal.getSucursalId().getPais() != null ? identificadorSucursal.getSucursalId().getPais() : "")
                .append(" ").append(facturaDto.getLugarExpedicion() != null ? facturaDto.getLugarExpedicion() : "");
        PdfPCell paisV = new PdfPCell(new Paragraph(paisEmisor.toString(), fontContenido));
        paisV.setHorizontalAlignment(Element.ALIGN_LEFT);
        paisV.setPaddingBottom(1f);
        paisV.setBorder(Rectangle.NO_BORDER);

        tableDatosReceptor.addCell(expedidoEnC);
        tableDatosReceptor.addCell(expedidoEnV);
        tableDatosReceptor.addCell(estadoV);
        tableDatosReceptor.addCell(paisV);

        PdfPTable tableDatosEmisor = new PdfPTable(1);
        tableDatosEmisor.setWidths(new int[]{200});
        tableDatosEmisor.setTotalWidth(200);
        tableDatosEmisor.setLockedWidth(true);
        tableDatosEmisor.setWidthPercentage(PORCENTAJE_ANCHO_TABLA);
        tableDatosEmisor.getDefaultCell().setBorder(Rectangle.NO_BORDER);

        PdfPCell nombreEmisorV = new PdfPCell(new Paragraph(facturaDto.getEmisor().getNombre() != null ? facturaDto.getEmisor().getNombre() : "", fontContenido));
        nombreEmisorV.setHorizontalAlignment(Element.ALIGN_LEFT);
        nombreEmisorV.setBorder(Rectangle.NO_BORDER);

        PdfPCell direccionEmisorV = new PdfPCell(new Paragraph((facturaDto.getEmisor().getCalle() != null ? facturaDto.getEmisor().getCalle() : "").concat(" ")
                .concat(facturaDto.getEmisor().getNoExterior() != null ? facturaDto.getEmisor().getNoExterior() : "").concat(" ")
                .concat(facturaDto.getEmisor().getNoInterior() != null ? facturaDto.getEmisor().getNoInterior() : ""), fontContenido));
        direccionEmisorV.setHorizontalAlignment(Element.ALIGN_LEFT);
        direccionEmisorV.setPaddingBottom(10f);
        direccionEmisorV.setBorder(Rectangle.NO_BORDER);

        StringBuilder municipioEmisor = new StringBuilder();
        municipioEmisor = municipioEmisor.append(facturaDto.getEmisor().getColonia() != null ? facturaDto.getEmisor().getColonia() : "").append("   ").append(facturaDto.getEmisor().getMunicipio() != null ? facturaDto.getEmisor().getMunicipio() : "");
        PdfPCell municipioEmisorV = new PdfPCell(new Paragraph(municipioEmisor.toString(), fontContenido));
        municipioEmisorV.setHorizontalAlignment(Element.ALIGN_LEFT);
        municipioEmisorV.setBorder(Rectangle.NO_BORDER);

        PdfPCell paisEmisorV = new PdfPCell(new Paragraph((facturaDto.getEmisor().getEstado() != null ? facturaDto.getEmisor().getEstado() : "").concat(" ").concat(facturaDto.getEmisor().getPais() != null ? facturaDto.getEmisor().getPais() : "").concat("    ").concat(facturaDto.getEmisor().getCodigoPostal() != null ? facturaDto.getEmisor().getCodigoPostal() : ""), fontContenido));
        paisEmisorV.setHorizontalAlignment(Element.ALIGN_LEFT);
        paisEmisorV.setBorder(Rectangle.NO_BORDER);

        PdfPCell rfcEmisorV = new PdfPCell(new Paragraph(facturaDto.getEmisor().getRfc() != null ? facturaDto.getEmisor().getRfc() : "", fontContenido));
        rfcEmisorV.setHorizontalAlignment(Element.ALIGN_LEFT);
        rfcEmisorV.setBorder(Rectangle.NO_BORDER);

        tableDatosEmisor.addCell(nombreEmisorV);
        tableDatosEmisor.addCell(direccionEmisorV);
        tableDatosEmisor.addCell(municipioEmisorV);
        tableDatosEmisor.addCell(paisEmisorV);
        tableDatosEmisor.addCell(rfcEmisorV);

        tableEncabezado.addCell(tableLogo);
        tableEncabezado.addCell(tableDatosReceptor);
        tableEncabezado.addCell(tableDatosEmisor);

        document.add(tableEncabezado);
    }

    /**
     * Metodo para agregar los datos de la factura Receptor, Datos Timbrado, etc
     * * Todos los Formatos
     *
     * @param document
     * @throws BusinessException
     */
    private void agregarTableDatosFactura(Document document, IdentificadorSucursal identificadorSucursal, CFDI facturaDto) throws BusinessException, Exception {
        try {
            PdfPTable tableDatosFactura = new PdfPTable(2);
            tableDatosFactura.setWidths(new int[]{200, 200});
            tableDatosFactura.setTotalWidth(ANCHO_TOTAL_TABLA);
            tableDatosFactura.setLockedWidth(true);
            tableDatosFactura.setWidthPercentage(PORCENTAJE_ANCHO_TABLA);
            tableDatosFactura.getDefaultCell().setFixedHeight(90f);
            tableDatosFactura.getDefaultCell().setBorder(Rectangle.NO_BORDER);

            PdfPTable tableDatosReceptor = new PdfPTable(2);
            tableDatosReceptor.setWidths(new int[]{30, 120});
            tableDatosReceptor.setTotalWidth(280);
            tableDatosReceptor.setLockedWidth(true);
            tableDatosReceptor.setWidthPercentage(PORCENTAJE_ANCHO_TABLA);
            tableDatosReceptor.getDefaultCell().setBorder(Rectangle.NO_BORDER);
            

            PdfPCell nombreSucursalV = new PdfPCell(new Paragraph(identificadorSucursal.getSucursalId().getNombre() != null ? identificadorSucursal.getSucursalId().getNombre() : "", fontContenido));
            nombreSucursalV.setHorizontalAlignment(Element.ALIGN_LEFT);
            nombreSucursalV.setColspan(2);
            nombreSucursalV.setBorder(Rectangle.NO_BORDER);

            PdfPCell reservacionV = new PdfPCell(new Paragraph(new Phrase(hmapTagIdioma.get(EnumTagPlantilla.P_RESERVACION).concat(" : ").concat("(01-800) 504 5000 / 53 26 69 00"), fontContenido)));
            reservacionV.setHorizontalAlignment(Element.ALIGN_LEFT);
            reservacionV.setColspan(2);
            reservacionV.setBorder(Rectangle.NO_BORDER);

            PdfPCell contactoV = new PdfPCell(new Paragraph(new Phrase(hmapTagIdioma.get(EnumTagPlantilla.P_CONTACTO).concat(" : ").concat(identificadorSucursal.getSucursalId().getTelefono() != null ? identificadorSucursal.getSucursalId().getTelefono() : ""), fontContenido)));
            contactoV.setHorizontalAlignment(Element.ALIGN_LEFT);
            contactoV.setColspan(2);
            contactoV.setBorder(Rectangle.NO_BORDER);

            PdfPCell emailV = new PdfPCell(new Paragraph(new Phrase(hmapTagIdioma.get(EnumTagPlantilla.P_EMAIL) + " : " + (Optional.ofNullable(identificadorSucursal.getSucursalId().getEmail()).orElse("")), fontContenido)));
            emailV.setHorizontalAlignment(Element.ALIGN_LEFT);
            emailV.setPaddingBottom(14f);
            emailV.setColspan(2);
            emailV.setBorder(Rectangle.NO_BORDER);

            PdfPCell regimenC = new PdfPCell(new Paragraph(new Phrase(hmapTagIdioma.get(EnumTagPlantilla.P_TIPOREGIMEN).concat(" : "), fontEtiquetas)));
            regimenC.setHorizontalAlignment(Element.ALIGN_RIGHT);
            regimenC.setBorder(Rectangle.NO_BORDER);

            PdfPCell regimenV = new PdfPCell(new Paragraph(new Phrase(facturaDto.getEmisor().getRegimenFiscal() != null ? facturaDto.getEmisor().getRegimenFiscal() : "", fontContenido)));
            regimenV.setHorizontalAlignment(Element.ALIGN_LEFT);
            regimenV.setBorder(Rectangle.NO_BORDER);

           
            tableDatosReceptor.addCell(nombreSucursalV);
            tableDatosReceptor.addCell(reservacionV);
            tableDatosReceptor.addCell(contactoV);
            tableDatosReceptor.addCell(emailV);
            tableDatosReceptor.addCell(regimenC);
            tableDatosReceptor.addCell(regimenV);

            PdfPTable tableCamposFactura = new PdfPTable(3);
            tableCamposFactura.setWidths(new int[]{110, 90, 80});
            tableCamposFactura.setTotalWidth(300);
            tableCamposFactura.setLockedWidth(true);
            tableCamposFactura.setWidthPercentage(PORCENTAJE_ANCHO_TABLA);
            tableCamposFactura.getDefaultCell().setBorder(Rectangle.NO_BORDER);

            String tipoDoc = "";
            if (facturaDto.getTipoCfdi().equals(CFDI.EnumTipoFactura.FACTURA)) {
                tipoDoc = hmapTagIdioma.get(EnumTagPlantilla.FACTURA);
            }
            if (facturaDto.getTipoCfdi().equals(CFDI.EnumTipoFactura.NOTA_CREDITO)) {
                tipoDoc = hmapTagIdioma.get(EnumTagPlantilla.NOTA_CREDITO);
            }

            PdfPCell tipoDocumentoV = new PdfPCell(new Paragraph(tipoDoc, fontEtiquetas));
            tipoDocumentoV.setHorizontalAlignment(Element.ALIGN_CENTER);
            tipoDocumentoV.setColspan(3);
            tipoDocumentoV.setBorder(Rectangle.NO_BORDER);

            PdfPCell noCertificadoC = new PdfPCell(new Paragraph(new Phrase(hmapTagIdioma.get(EnumTagPlantilla.P_NOCERTIFICADO), fontEtiquetas)));
            noCertificadoC.setHorizontalAlignment(Element.ALIGN_RIGHT);
            noCertificadoC.setBorder(Rectangle.NO_BORDER);

            PdfPCell noCertificadoV = new PdfPCell(new Paragraph(facturaDto.getNumeroCertificado() != null ? facturaDto.getNumeroCertificado()  : "", fontContenido));
            noCertificadoV.setHorizontalAlignment(Element.ALIGN_LEFT);
            noCertificadoV.setColspan(2);
            noCertificadoV.setBorder(Rectangle.NO_BORDER);

            PdfPCell fechaEmisionC = new PdfPCell((new Phrase(hmapTagIdioma.get(EnumTagPlantilla.E_FECHAEMISION), fontEtiquetas)));
            fechaEmisionC.setHorizontalAlignment(Element.ALIGN_RIGHT);
            fechaEmisionC.setBorder(Rectangle.NO_BORDER);

            PdfPCell fechaEmisionV = new PdfPCell(new Paragraph(FechasUtil.getStringAnioMesDiaHoraMinSeg(facturaDto.getFecha() != null ? facturaDto.getFecha() : new Date()), fontContenido));
            fechaEmisionV.setHorizontalAlignment(Element.ALIGN_LEFT);
            fechaEmisionV.setColspan(2);
            fechaEmisionV.setBorder(Rectangle.NO_BORDER);

            PdfPCell noCertificadoSATC = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.P_NOCERTIFICADOSAT), fontEtiquetas));
            noCertificadoSATC.setHorizontalAlignment(Element.ALIGN_RIGHT);
            noCertificadoSATC.setBorder(Rectangle.NO_BORDER);

            PdfPCell noCertificadoSATV = new PdfPCell(new Paragraph(facturaDto.getTimbrado().getNumCertificadoSAT() != null ? facturaDto.getTimbrado().getNumCertificadoSAT() : "", fontContenido));
            noCertificadoSATV.setHorizontalAlignment(Element.ALIGN_LEFT);
            noCertificadoSATV.setColspan(2);
            noCertificadoSATV.setBorder(Rectangle.NO_BORDER);

            PdfPCell folioUUIDC = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.P_FOLIOUUID), fontEtiquetas));
            folioUUIDC.setHorizontalAlignment(Element.ALIGN_RIGHT);
            folioUUIDC.setBorder(Rectangle.NO_BORDER);

            PdfPCell folioUUIDV = new PdfPCell(new Paragraph(facturaDto.getTimbrado().getUuid() != null ? facturaDto.getTimbrado().getUuid() : "", fontContenido));
            folioUUIDV.setHorizontalAlignment(Element.ALIGN_LEFT);
            folioUUIDV.setColspan(2);
            folioUUIDV.setBorder(Rectangle.NO_BORDER);

            PdfPCell fechaCertificacionC = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.P_FECHACERTIFICACIONCFDI), fontEtiquetas));
            fechaCertificacionC.setHorizontalAlignment(Element.ALIGN_RIGHT);
            fechaCertificacionC.setBorder(Rectangle.NO_BORDER);

            PdfPCell fechaCertificacionV = new PdfPCell(new Paragraph(FechasUtil.getStringAnioMesDiaHoraMinSeg(facturaDto.getTimbrado() != null && facturaDto.getTimbrado().getFechaTimbrado() != null ? facturaDto.getTimbrado().getFechaTimbrado() : new Date()), fontContenido));
            fechaCertificacionV.setHorizontalAlignment(Element.ALIGN_LEFT);
            fechaCertificacionV.setBorder(Rectangle.NO_BORDER);

            PdfPCell folioXMLC = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.P_FOLIOXML) + ":" + (facturaDto.getFolio() != null ? facturaDto.getFolio() : ""), fontEtiquetas));
            folioXMLC.setHorizontalAlignment(Element.ALIGN_LEFT);
            folioXMLC.setBorder(Rectangle.NO_BORDER);

            PdfPCell exportacionC = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.E_EXPORTACION), fontEtiquetas));
            exportacionC.setHorizontalAlignment(Element.ALIGN_RIGHT);
            exportacionC.setBorder(Rectangle.NO_BORDER);

            PdfPCell exportacion = new PdfPCell(new Paragraph(facturaDto.getExportacion() != null ? facturaDto.getExportacion() : "", fontContenido));
            exportacion.setHorizontalAlignment(Element.ALIGN_LEFT);
            exportacion.setColspan(2);
            exportacion.setBorder(Rectangle.NO_BORDER);


            tableCamposFactura.addCell(tipoDocumentoV);//3
            tableCamposFactura.addCell(noCertificadoC);//1
            tableCamposFactura.addCell(noCertificadoV);//2
            tableCamposFactura.addCell(fechaEmisionC);//1
            tableCamposFactura.addCell(fechaEmisionV);//2
            tableCamposFactura.addCell(noCertificadoSATC);//1
            tableCamposFactura.addCell(noCertificadoSATV);//2
            tableCamposFactura.addCell(folioUUIDC);//1
            tableCamposFactura.addCell(folioUUIDV);//2
            tableCamposFactura.addCell(fechaCertificacionC);//1
            tableCamposFactura.addCell(fechaCertificacionV);//1
            tableCamposFactura.addCell(folioXMLC);//1
            tableCamposFactura.addCell(exportacionC);
            tableCamposFactura.addCell(exportacion);

            tableDatosFactura.addCell(tableDatosReceptor);
            tableDatosFactura.addCell(tableCamposFactura);

            document.add(tableDatosFactura);
        } catch (Exception ex) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Ocurrio un error al tratar de generar los datos de la factura, Error: " + ex.toString());
        }
    }

    /**
     * Metodo para agregar los Datos del Cliente * Formatos: 03, 05, 06, 20, 21,
     * 23, 51.
     *
     * @param document
     * @throws DocumentException
     */
    private void agregarTableDatosCliente(Document document, CFDI facturaDto) throws  DocumentException {
        PdfPTable tableDatosCliente = new PdfPTable(2);
        tableDatosCliente.setWidths(new int[]{680, 280});
        tableDatosCliente.setTotalWidth(ANCHO_TOTAL_TABLA);
        tableDatosCliente.setLockedWidth(true);
        tableDatosCliente.setWidthPercentage(PORCENTAJE_ANCHO_TABLA);
        tableDatosCliente.getDefaultCell().setBorder(Rectangle.NO_BORDER);

        PdfPTable tableDatosClientes = new PdfPTable(3);
        tableDatosClientes.setWidths(new int[]{200, 180, 200});
        tableDatosClientes.setTotalWidth(400);
        tableDatosClientes.setLockedWidth(true);
        tableDatosClientes.setWidthPercentage(PORCENTAJE_ANCHO_TABLA);
        tableDatosClientes.getDefaultCell().setBorder(Rectangle.NO_BORDER);

        PdfPCell nombreReceptorV = new PdfPCell(new Phrase(facturaDto.getReceptor().getNombre() != null ? facturaDto.getReceptor().getNombre() : "", fontContenido));
        nombreReceptorV.setColspan(3);
        nombreReceptorV.setHorizontalAlignment(Element.ALIGN_LEFT);
        nombreReceptorV.setBorder(Rectangle.NO_BORDER);

        PdfPCell rfcReceptorV = new PdfPCell(new Phrase(hmapTagIdioma.get(EnumTagPlantilla.RFC) + ":" + facturaDto.getReceptor().getRfc() != null ? facturaDto.getReceptor().getRfc() : "", fontContenido));
        rfcReceptorV.setColspan(3);
        rfcReceptorV.setHorizontalAlignment(Element.ALIGN_LEFT);
        rfcReceptorV.setBorder(Rectangle.NO_BORDER);

        PdfPCell usoCfdiReceptor = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.G_USOCFDI).concat(" : ").concat(facturaDto.getReceptor().getUsoCFDI() != null ? facturaDto.getReceptor().getUsoCFDI() : ""), fontContenido));
        usoCfdiReceptor.setHorizontalAlignment(Element.ALIGN_LEFT);
        usoCfdiReceptor.setColspan(3);
        usoCfdiReceptor.setBorder(Rectangle.NO_BORDER);

        PdfPCell domicilioFiscReceptor = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.R_DOMFISCRECEP).concat(" : ").concat(facturaDto.getReceptor().getDomicilioFiscal() != null ? facturaDto.getReceptor().getDomicilioFiscal() : ""), fontContenido));
        domicilioFiscReceptor.setHorizontalAlignment(Element.ALIGN_LEFT);
        domicilioFiscReceptor.setColspan(3);
        domicilioFiscReceptor.setBorder(Rectangle.NO_BORDER);

        PdfPCell regFiscR = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.REG_FISCAL_RECEP).concat(" : ").concat(facturaDto.getReceptor().getRegimenFiscal() != null ? facturaDto.getReceptor().getRegimenFiscal() : ""), fontContenido));
        regFiscR.setHorizontalAlignment(Element.ALIGN_LEFT);
        regFiscR.setColspan(3);
        regFiscR.setBorder(Rectangle.NO_BORDER);

        tableDatosClientes.addCell(nombreReceptorV);
        tableDatosClientes.addCell(rfcReceptorV);
        tableDatosClientes.addCell(usoCfdiReceptor);
        tableDatosClientes.addCell(domicilioFiscReceptor);
        tableDatosClientes.addCell(regFiscR);

        if (facturaDto.getReceptor().getResidenciaFiscal() != null && !facturaDto.getReceptor().getResidenciaFiscal().trim().equals("")) {
            PdfPCell residenciaFiscal = new PdfPCell(new Paragraph(("Residencía Fiscal").concat(" : ").concat(facturaDto.getReceptor().getResidenciaFiscal() != null ? facturaDto.getReceptor().getResidenciaFiscal() : ""), fontContenido));
            residenciaFiscal.setHorizontalAlignment(Element.ALIGN_LEFT);
            residenciaFiscal.setColspan(3);
            residenciaFiscal.setBorder(Rectangle.NO_BORDER);
            tableDatosClientes.addCell(residenciaFiscal);
        }

        if (facturaDto.getReceptor().getNumRegIdTrib() != null && !facturaDto.getReceptor().getNumRegIdTrib().trim().equals("")) {
            PdfPCell numRegIdTrib = new PdfPCell(new Paragraph((hmapTagIdioma.get(EnumTagPlantilla.NUM_REGID_TRIB)).concat(" : ").concat(facturaDto.getReceptor().getNumRegIdTrib() != null ? facturaDto.getReceptor().getNumRegIdTrib() : ""), fontContenido));
            numRegIdTrib.setHorizontalAlignment(Element.ALIGN_LEFT);
            numRegIdTrib.setColspan(3);
            numRegIdTrib.setBorder(Rectangle.NO_BORDER);
            tableDatosClientes.addCell(numRegIdTrib);
        }

        PdfPTable tableComplementoINE = new PdfPTable(2);
        tableComplementoINE.setWidths(new int[]{200, 120});
        tableComplementoINE.setTotalWidth(PORCENTAJE_ANCHO_TABLA);
        tableComplementoINE.setLockedWidth(true);
        tableComplementoINE.setWidthPercentage(PORCENTAJE_ANCHO_TABLA);
        tableComplementoINE.getDefaultCell().setBorder(Rectangle.NO_BORDER);

        if (facturaDto.getComplementos() != null) {

         if (facturaDto.getComplementos().getIne() != null) {
            PdfPCell cellComplementoINE = new PdfPCell(new Phrase(hmapTagIdioma.get(EnumTagPlantilla.P_COMPLEMENTOINE), fontEtiquetas));
            cellComplementoINE.setHorizontalAlignment(Element.ALIGN_CENTER);
            cellComplementoINE.setColspan(2);
            cellComplementoINE.setBorder(Rectangle.NO_BORDER);

            PdfPCell cellProceso = new PdfPCell(new Phrase((hmapTagIdioma.get(EnumTagPlantilla.INE_TIPO_PROCESO)), fontEtiquetas));
            cellProceso.setHorizontalAlignment(Element.ALIGN_LEFT);
            cellProceso.setBorder(Rectangle.NO_BORDER);

            PdfPCell proceso = new PdfPCell(new Phrase(facturaDto.getComplementos().getIne().getTipoProceso(), fontContenido));
            proceso.setHorizontalAlignment(Element.ALIGN_LEFT);
            proceso.setBorder(Rectangle.NO_BORDER);

            PdfPCell cellComite = new PdfPCell(new Phrase(((hmapTagIdioma.get(EnumTagPlantilla.INE_TIPO_COMITE))), fontEtiquetas));
            cellComite.setHorizontalAlignment(Element.ALIGN_LEFT);
            cellComite.setBorder(Rectangle.NO_BORDER);

            PdfPCell comite = new PdfPCell(new Phrase(facturaDto.getComplementos().getIne().getTipoComite() != null ? facturaDto.getComplementos().getIne().getTipoComite() : "", fontContenido));
            comite.setHorizontalAlignment(Element.ALIGN_LEFT);
            comite.setBorder(Rectangle.NO_BORDER);

            PdfPCell cellIdContabilida = new PdfPCell(new Phrase((("Id Contabilidad: ")), fontEtiquetas));
            cellIdContabilida.setHorizontalAlignment(Element.ALIGN_LEFT);
            cellIdContabilida.setBorder(Rectangle.NO_BORDER);

            PdfPCell cellIdContabilidadVS = new PdfPCell(new Phrase(facturaDto.getComplementos().getIne().getIdContabilidad() != null ? String.valueOf(facturaDto.getComplementos().getIne().getIdContabilidad()) : "", fontContenido));
            cellIdContabilidadVS.setHorizontalAlignment(Element.ALIGN_LEFT);
            cellIdContabilidadVS.setBorder(Rectangle.NO_BORDER);

            tableComplementoINE.addCell(cellComplementoINE);
            tableComplementoINE.addCell(cellProceso);
            tableComplementoINE.addCell(proceso);
            tableComplementoINE.addCell(cellComite);
            tableComplementoINE.addCell(comite);
            tableComplementoINE.addCell(cellIdContabilida);
            tableComplementoINE.addCell(cellIdContabilidadVS);

            String idContabilidadINE = "";
            if (facturaDto.getComplementos().getIne().getEntidad() != null && !facturaDto.getComplementos().getIne().getEntidad().isEmpty()) {
                for (CFDI.Complementos.Ine.Entidad entidadIne : facturaDto.getComplementos().getIne().getEntidad()) {
                    PdfPCell cellEntidad = new PdfPCell(new Phrase(((hmapTagIdioma.get(EnumTagPlantilla.INE_CLAVE_ENTIDAD))), fontEtiquetas));
                    cellEntidad.setHorizontalAlignment(Element.ALIGN_LEFT);
                    cellEntidad.setBorder(Rectangle.NO_BORDER);

                    PdfPCell entidad = new PdfPCell(new Phrase(entidadIne.getClaveEntidad(), fontContenido));
                    entidad.setHorizontalAlignment(Element.ALIGN_LEFT);
                    entidad.setBorder(Rectangle.NO_BORDER);

                    PdfPCell cellAmbito = new PdfPCell(new Phrase(((hmapTagIdioma.get(EnumTagPlantilla.INE_AMBITO))), fontEtiquetas));
                    cellAmbito.setHorizontalAlignment(Element.ALIGN_LEFT);
                    cellAmbito.setBorder(Rectangle.NO_BORDER);

                    PdfPCell cellIdContabilidad = new PdfPCell(new Phrase(((hmapTagIdioma.get(EnumTagPlantilla.INE_ID_CONTABILIDAD))), fontEtiquetas));
                    cellIdContabilidad.setHorizontalAlignment(Element.ALIGN_LEFT);
                    cellIdContabilidad.setBorder(Rectangle.NO_BORDER);

                    PdfPCell cellClaveEntida = new PdfPCell(new Paragraph(new Phrase(entidadIne.getClaveEntidad(), fontContenido)));
                    cellClaveEntida.setBorder(Rectangle.NO_BORDER);
                    cellClaveEntida.setHorizontalAlignment(Element.ALIGN_LEFT);

                    PdfPCell ambito = new PdfPCell(new Paragraph(new Phrase(entidadIne.getAmbito() != null ? entidadIne.getAmbito() : "", fontContenido)));
                    ambito.setBorder(Rectangle.NO_BORDER);
                    ambito.setHorizontalAlignment(Element.ALIGN_LEFT);

                    if (entidadIne.getContabilidad() != null && !entidadIne.getContabilidad().isEmpty()) {
                        if (entidadIne.getContabilidad().size() > 1) {
                            for (CFDI.Complementos.Ine.Entidad.Contabilidad contabilidad : entidadIne.getContabilidad()) {
                                idContabilidadINE = idContabilidadINE + String.valueOf(contabilidad.getIdContabilidad() + ",");
                            }
                            idContabilidadINE = idContabilidadINE.substring(0, idContabilidadINE.length() - 1);
                        } else {
                            idContabilidadINE = String.valueOf(entidadIne.getContabilidad().get(0).getIdContabilidad());
                        }
                    }
                    PdfPCell idContabilidad = new PdfPCell(new Phrase(idContabilidadINE, fontContenido));
                    idContabilidad.setHorizontalAlignment(Element.ALIGN_LEFT);
                    idContabilidad.setBorder(Rectangle.NO_BORDER);

                    tableComplementoINE.addCell(cellEntidad);
                    tableComplementoINE.addCell(entidad);
                    tableComplementoINE.addCell(cellAmbito);
                    tableComplementoINE.addCell(ambito);
                    tableComplementoINE.addCell(cellIdContabilidad);
                    tableComplementoINE.addCell(idContabilidad);
                }
            }
        }
        }
        PdfPCell datosClienteC = new PdfPCell(new Phrase(hmapTagIdioma.get(EnumTagPlantilla.P_DATOSDELCLIENTE), fontEtiquetas));
        datosClienteC.setColspan(2);
        datosClienteC.setBackgroundColor(BaseColor.LIGHT_GRAY);
        datosClienteC.setHorizontalAlignment(Element.ALIGN_LEFT);
        datosClienteC.setBorder(Rectangle.NO_BORDER);

        tableDatosCliente.addCell(datosClienteC);
        tableDatosCliente.addCell(tableDatosClientes);       
        tableDatosCliente.addCell(tableComplementoINE);
       

        document.add(tableDatosCliente);
    }

    /**
     * Metodo para agregar los datos del cliente (Formato Especial) * Formatos
     * Especiales: 00, 01, 02, 04, 50, 80, 81, 90, 91.
     *
     * @param document
     * @throws DocumentException
     */
    private void agregarTableDatosClienteEspecial(Document document, CFDI facturaDto) throws DocumentException {
        PdfPTable tableDatosCliente = new PdfPTable(2);
        tableDatosCliente.setWidths(new int[]{680, 280});
        tableDatosCliente.setTotalWidth(ANCHO_TOTAL_TABLA);
        tableDatosCliente.setLockedWidth(true);
        tableDatosCliente.setWidthPercentage(PORCENTAJE_ANCHO_TABLA);
        tableDatosCliente.getDefaultCell().setBorder(Rectangle.NO_BORDER);

        PdfPTable tableDatosClientes = new PdfPTable(3);
        tableDatosClientes.setWidths(new int[]{200, 180, 200});
        tableDatosClientes.setTotalWidth(400);
        tableDatosClientes.setLockedWidth(true);
        tableDatosClientes.setWidthPercentage(PORCENTAJE_ANCHO_TABLA);
        tableDatosClientes.getDefaultCell().setBorder(Rectangle.NO_BORDER);

        PdfPCell nombreReceptorV = new PdfPCell(new Phrase(facturaDto.getReceptor().getNombre() != null ? facturaDto.getReceptor().getNombre() : "", fontContenido));
        nombreReceptorV.setColspan(3);
        nombreReceptorV.setHorizontalAlignment(Element.ALIGN_LEFT);
        nombreReceptorV.setBorder(Rectangle.NO_BORDER);

        PdfPCell rfcReceptorV = new PdfPCell(new Phrase(hmapTagIdioma.get(EnumTagPlantilla.RFC) + ":" + facturaDto.getReceptor().getRfc() != null ? facturaDto.getReceptor().getRfc() : "", fontContenido));
        rfcReceptorV.setColspan(3);
        rfcReceptorV.setHorizontalAlignment(Element.ALIGN_LEFT);
        rfcReceptorV.setBorder(Rectangle.NO_BORDER);

        PdfPCell usoCfdiReceptor = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.G_USOCFDI).concat(" : ").concat(facturaDto.getReceptor().getUsoCFDI() != null ? facturaDto.getReceptor().getUsoCFDI() : ""), fontContenido));
        usoCfdiReceptor.setHorizontalAlignment(Element.ALIGN_LEFT);
        usoCfdiReceptor.setColspan(3);
        usoCfdiReceptor.setBorder(Rectangle.NO_BORDER);

        PdfPCell domFiscR = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.R_DOMFISCRECEP).concat(" : ").concat(facturaDto.getReceptor().getDomicilioFiscal() != null ? facturaDto.getReceptor().getDomicilioFiscal() : ""), fontContenido));
        domFiscR.setHorizontalAlignment(Element.ALIGN_LEFT);
        domFiscR.setColspan(3);
        domFiscR.setBorder(Rectangle.NO_BORDER);

        PdfPCell regFiscR = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.REG_FISCAL_RECEP).concat(" : ").concat(facturaDto.getReceptor().getRegimenFiscal() != null ? facturaDto.getReceptor().getRegimenFiscal() : ""), fontContenido));
        regFiscR.setHorizontalAlignment(Element.ALIGN_LEFT);
        regFiscR.setColspan(3);
        regFiscR.setBorder(Rectangle.NO_BORDER);

        tableDatosClientes.addCell(nombreReceptorV);
        tableDatosClientes.addCell(rfcReceptorV);
        tableDatosClientes.addCell(usoCfdiReceptor);
        tableDatosClientes.addCell(domFiscR);
        tableDatosClientes.addCell(regFiscR);

        if (facturaDto.getReceptor().getResidenciaFiscal() != null && !facturaDto.getReceptor().getResidenciaFiscal().trim().equals("")) {
            PdfPCell residenciaFiscal = new PdfPCell(new Paragraph(("Residencía Fiscal").concat(" : ").concat(facturaDto.getReceptor().getResidenciaFiscal() != null ? facturaDto.getReceptor().getResidenciaFiscal() : ""), fontContenido));
            residenciaFiscal.setHorizontalAlignment(Element.ALIGN_LEFT);
            residenciaFiscal.setColspan(3);
            residenciaFiscal.setBorder(Rectangle.NO_BORDER);
            tableDatosClientes.addCell(residenciaFiscal);
        }

        if (facturaDto.getReceptor().getNumRegIdTrib() != null && !facturaDto.getReceptor().getNumRegIdTrib().trim().equals("")) {
            PdfPCell numRegIdTrib = new PdfPCell(new Paragraph(("NumRegIdTrib").concat(" : ").concat(facturaDto.getReceptor().getNumRegIdTrib() != null ? facturaDto.getReceptor().getNumRegIdTrib() : ""), fontContenido));
            numRegIdTrib.setHorizontalAlignment(Element.ALIGN_LEFT);
            numRegIdTrib.setColspan(3);
            numRegIdTrib.setBorder(Rectangle.NO_BORDER);
            tableDatosClientes.addCell(numRegIdTrib);
        }

       
        PdfPTable tableComplementoINE = new PdfPTable(2);
        tableComplementoINE.setWidths(new int[]{200, 120});
        tableComplementoINE.setTotalWidth(PORCENTAJE_ANCHO_TABLA);
        tableComplementoINE.setLockedWidth(true);
        tableComplementoINE.setWidthPercentage(PORCENTAJE_ANCHO_TABLA);
        tableComplementoINE.getDefaultCell().setBorder(Rectangle.NO_BORDER);

        if (facturaDto.getComplementos()!= null && facturaDto.getComplementos().getIne()!= null) {
            PdfPCell cellComplementoINE = new PdfPCell(new Phrase(hmapTagIdioma.get(EnumTagPlantilla.P_COMPLEMENTOINE), fontEtiquetas));
            cellComplementoINE.setHorizontalAlignment(Element.ALIGN_CENTER);
            cellComplementoINE.setColspan(2);
            cellComplementoINE.setBorder(Rectangle.NO_BORDER);

            PdfPCell cellProceso = new PdfPCell(new Phrase((hmapTagIdioma.get(EnumTagPlantilla.INE_TIPO_PROCESO)), fontEtiquetas));
            cellProceso.setHorizontalAlignment(Element.ALIGN_LEFT);
            cellProceso.setBorder(Rectangle.NO_BORDER);

            PdfPCell proceso = new PdfPCell(new Phrase(facturaDto.getComplementos().getIne().getTipoProceso(), fontContenido));
            proceso.setHorizontalAlignment(Element.ALIGN_LEFT);
            proceso.setBorder(Rectangle.NO_BORDER);

            PdfPCell cellComite = new PdfPCell(new Phrase(((hmapTagIdioma.get(EnumTagPlantilla.INE_TIPO_COMITE))), fontEtiquetas));
            cellComite.setHorizontalAlignment(Element.ALIGN_LEFT);
            cellComite.setBorder(Rectangle.NO_BORDER);

            PdfPCell comite = new PdfPCell(new Phrase(facturaDto.getComplementos().getIne().getTipoComite() != null ? facturaDto.getComplementos().getIne().getTipoComite() : "", fontContenido));
            comite.setHorizontalAlignment(Element.ALIGN_LEFT);
            comite.setBorder(Rectangle.NO_BORDER);

            PdfPCell cellIdContabilida = new PdfPCell(new Phrase((("Id Contabilidad")), fontEtiquetas));
            cellIdContabilida.setHorizontalAlignment(Element.ALIGN_LEFT);
            cellIdContabilida.setBorder(Rectangle.NO_BORDER);

            PdfPCell cellIdContabilidadVS = new PdfPCell(new Phrase(facturaDto.getComplementos().getIne().getIdContabilidad() != null ? String.valueOf(facturaDto.getComplementos().getIne().getIdContabilidad()) : "", fontContenido));
            cellIdContabilidadVS.setHorizontalAlignment(Element.ALIGN_LEFT);
            cellIdContabilidadVS.setBorder(Rectangle.NO_BORDER);

            tableComplementoINE.addCell(cellComplementoINE);
            tableComplementoINE.addCell(cellProceso);
            tableComplementoINE.addCell(proceso);
            tableComplementoINE.addCell(cellComite);
            tableComplementoINE.addCell(comite);
            tableComplementoINE.addCell(cellIdContabilida);
            tableComplementoINE.addCell(cellIdContabilidadVS);

            String idContabilidadINE = "";
            if (facturaDto.getComplementos().getIne().getEntidad() != null && !facturaDto.getComplementos().getIne().getEntidad().isEmpty()) {
                for (CFDI.Complementos.Ine.Entidad entidadIne : facturaDto.getComplementos().getIne().getEntidad()) {
                    PdfPCell cellEntidad = new PdfPCell(new Phrase(((hmapTagIdioma.get(EnumTagPlantilla.INE_CLAVE_ENTIDAD))), fontEtiquetas));
                    cellEntidad.setHorizontalAlignment(Element.ALIGN_LEFT);
                    cellEntidad.setBorder(Rectangle.NO_BORDER);

                    PdfPCell entidad = new PdfPCell(new Phrase(entidadIne.getClaveEntidad(), fontContenido));
                    entidad.setHorizontalAlignment(Element.ALIGN_LEFT);
                    entidad.setBorder(Rectangle.NO_BORDER);

                    PdfPCell cellAmbito = new PdfPCell(new Phrase(((hmapTagIdioma.get(EnumTagPlantilla.INE_AMBITO))), fontEtiquetas));
                    cellAmbito.setHorizontalAlignment(Element.ALIGN_LEFT);
                    cellAmbito.setBorder(Rectangle.NO_BORDER);

                    PdfPCell cellIdContabilidad = new PdfPCell(new Phrase(((hmapTagIdioma.get(EnumTagPlantilla.INE_ID_CONTABILIDAD))), fontEtiquetas));
                    cellIdContabilidad.setHorizontalAlignment(Element.ALIGN_LEFT);
                    cellIdContabilidad.setBorder(Rectangle.NO_BORDER);

                    PdfPCell cellClaveEntida = new PdfPCell(new Paragraph(new Phrase(entidadIne.getClaveEntidad(), fontContenido)));
                    cellClaveEntida.setBorder(Rectangle.NO_BORDER);
                    cellClaveEntida.setHorizontalAlignment(Element.ALIGN_CENTER);

                    PdfPCell ambito = new PdfPCell(new Paragraph(new Phrase(entidadIne.getAmbito() != null ? entidadIne.getAmbito() : "", fontContenido)));
                    ambito.setBorder(Rectangle.NO_BORDER);
                    ambito.setHorizontalAlignment(Element.ALIGN_CENTER);

                    if (entidadIne.getContabilidad() != null && !entidadIne.getContabilidad().isEmpty()) {
                        if (entidadIne.getContabilidad().size() > 1) {
                            for (CFDI.Complementos.Ine.Entidad.Contabilidad contabilidad : entidadIne.getContabilidad()) {
                                idContabilidadINE = idContabilidadINE + String.valueOf(contabilidad.getIdContabilidad() + ",");
                            }
                            idContabilidadINE = idContabilidadINE.substring(0, idContabilidadINE.length() - 1);
                        } else {
                            idContabilidadINE = String.valueOf(entidadIne.getContabilidad().get(0).getIdContabilidad());
                        }
                    }
                    PdfPCell idContabilidad = new PdfPCell(new Phrase(idContabilidadINE, fontContenido));
                    idContabilidad.setHorizontalAlignment(Element.ALIGN_LEFT);
                    idContabilidad.setBorder(Rectangle.NO_BORDER);

                    tableComplementoINE.addCell(cellEntidad);
                    tableComplementoINE.addCell(entidad);
                    tableComplementoINE.addCell(cellAmbito);
                    tableComplementoINE.addCell(ambito);
                    tableComplementoINE.addCell(cellIdContabilidad);
                    tableComplementoINE.addCell(idContabilidad);
                }
            }
        }

        PdfPCell datosClienteC = new PdfPCell(new Phrase(hmapTagIdioma.get(EnumTagPlantilla.P_DATOSFACTURACION), fontEtiquetas));
        datosClienteC.setColspan(2);
        datosClienteC.setBackgroundColor(BaseColor.LIGHT_GRAY);
        datosClienteC.setHorizontalAlignment(Element.ALIGN_LEFT);
        datosClienteC.setBorder(Rectangle.NO_BORDER);

        tableDatosCliente.addCell(datosClienteC);
        tableDatosCliente.addCell(tableDatosClientes);
        tableDatosCliente.addCell(tableComplementoINE);

        document.add(tableDatosCliente);

    }

    /**
     * Metodo para agregar los datos del cliente nombre huesped, habitacion, etc
     * * Todos los formatos a excepcion del 90, 91.
     *
     * @param document
     * @throws BusinessException
     * @throws ParseException
     */
    private void agregarTableDatosClienteOpera(Document document, CFDI facturaDto) throws  DocumentException, ParseException {
        PdfPTable tableDatosClienteOpera = new PdfPTable(4);
        tableDatosClienteOpera.setWidths(new int[]{100, 100, 100, 100});
        tableDatosClienteOpera.setTotalWidth(ANCHO_TOTAL_TABLA);
        tableDatosClienteOpera.setLockedWidth(true);
        tableDatosClienteOpera.setWidthPercentage(PORCENTAJE_ANCHO_TABLA);
        tableDatosClienteOpera.getDefaultCell().setFixedHeight(100f);
        tableDatosClienteOpera.getDefaultCell().setBorder(Rectangle.NO_BORDER);

        PdfPCell cellDatosCliente = new PdfPCell(new Phrase("", fontContenido));
        cellDatosCliente.setColspan(4);
        cellDatosCliente.setBackgroundColor(BaseColor.LIGHT_GRAY);
        cellDatosCliente.setHorizontalAlignment(Element.ALIGN_LEFT);
        cellDatosCliente.setBorder(Rectangle.NO_BORDER);
        cellDatosCliente.setFixedHeight(5f);

        PdfPCell rfcHuespedV = new PdfPCell(new Phrase(hmapTagIdioma.get(EnumTagPlantilla.P_HUESPED_OPE).concat(" : ").concat(facturaDto.getHospedaje() != null && facturaDto.getHospedaje().getHuesped() != null ? facturaDto.getHospedaje().getHuesped() : ""), fontContenido));
        rfcHuespedV.setColspan(2);
        rfcHuespedV.setHorizontalAlignment(Element.ALIGN_LEFT);
        rfcHuespedV.setBorder(Rectangle.NO_BORDER);

        PdfPCell voucherHuespedV = new PdfPCell(new Phrase(hmapTagIdioma.get(EnumTagPlantilla.P_VOUCHER_OPE).concat(" : ").concat(facturaDto.getHospedaje() != null && facturaDto.getHospedaje().getVoucher() != null ? facturaDto.getHospedaje().getVoucher() : ""), fontContenido));
        voucherHuespedV.setHorizontalAlignment(Element.ALIGN_LEFT);
        voucherHuespedV.setBorder(Rectangle.NO_BORDER);

        PdfPCell cuponHuespedV = new PdfPCell(new Phrase(hmapTagIdioma.get(EnumTagPlantilla.P_CUPON_OPE).concat(" : ").concat(facturaDto.getHospedaje() != null && facturaDto.getHospedaje().getCupon() != null ? facturaDto.getHospedaje().getCupon() : ""), fontContenido));
        cuponHuespedV.setHorizontalAlignment(Element.ALIGN_LEFT);
        cuponHuespedV.setBorder(Rectangle.NO_BORDER);

        String chekIn = "";
        if (facturaDto.getHospedaje() != null && facturaDto.getHospedaje().getChekIn() != null) {
            Date dateCheckIn = facturaDto.getHospedaje().getChekIn();
            DateFormat dateFormatCheckIn = new SimpleDateFormat("yyyyMMdd");
            chekIn = dateFormatCheckIn.format(dateCheckIn);
        }
        String chekOut = "";
        if (facturaDto.getHospedaje() != null && facturaDto.getHospedaje().getChekOut() != null) {
            Date dateCheckOut = facturaDto.getHospedaje().getChekOut();
            DateFormat dateFormatCheckOut = new SimpleDateFormat("yyyyMMdd");
            chekOut = dateFormatCheckOut.format(dateCheckOut);
        }
        PdfPCell ciudadHuespedV = new PdfPCell(new Phrase(hmapTagIdioma.get(EnumTagPlantilla.P_ESTANCIA_OPE) + " : " + chekIn + "      " + chekOut, fontContenido));
        ciudadHuespedV.setHorizontalAlignment(Element.ALIGN_LEFT);
        ciudadHuespedV.setBorder(Rectangle.NO_BORDER);

        PdfPCell folioHuespedV = new PdfPCell(new Phrase(hmapTagIdioma.get(EnumTagPlantilla.P_FOLIO).concat(" : ").concat(facturaDto.getHospedaje() != null && facturaDto.getHospedaje().getFolio() != null ? facturaDto.getHospedaje().getFolio() : "").concat(" - ").concat(facturaDto.getHospedaje() != null && facturaDto.getHospedaje().getExtencion() != null ? facturaDto.getHospedaje().getExtencion().trim() : ""), fontContenido));
        folioHuespedV.setHorizontalAlignment(Element.ALIGN_LEFT);
        folioHuespedV.setBorder(Rectangle.NO_BORDER);

        PdfPCell habitacionHuespedV = new PdfPCell(new Phrase(hmapTagIdioma.get(EnumTagPlantilla.P_HABITACION_OPE).concat(" : ").concat(facturaDto.getHospedaje() != null && facturaDto.getHospedaje().getHabitacion() != null ? facturaDto.getHospedaje().getHabitacion() : ""), fontContenido));
        habitacionHuespedV.setHorizontalAlignment(Element.ALIGN_LEFT);
        habitacionHuespedV.setBorder(Rectangle.NO_BORDER);

        PdfPCell reservacionHuespedV = new PdfPCell(new Phrase(hmapTagIdioma.get(EnumTagPlantilla.P_RESERVACION_OPE).concat(" : ").concat(facturaDto.getHospedaje() != null && facturaDto.getHospedaje().getReserva() != null ? facturaDto.getHospedaje().getReserva() : ""), fontContenido));
        reservacionHuespedV.setHorizontalAlignment(Element.ALIGN_LEFT);
        reservacionHuespedV.setBorder(Rectangle.NO_BORDER);

        PdfPCell cajeroHuespedV = new PdfPCell(new Phrase(hmapTagIdioma.get(EnumTagPlantilla.P_CAJERO).concat(" : ").concat(facturaDto.getHospedaje() != null && facturaDto.getHospedaje().getTerminal() != null ? facturaDto.getHospedaje().getTerminal() : ""), fontContenido));
        cajeroHuespedV.setHorizontalAlignment(Element.ALIGN_LEFT);
        cajeroHuespedV.setBorder(Rectangle.NO_BORDER);

        PdfPCell formatoFacturaHuespedV = new PdfPCell(new Phrase(hmapTagIdioma.get(EnumTagPlantilla.P_FORMATOFACTURA).concat(" : ").concat(facturaDto.getFormato() != null ? facturaDto.getFormato() : ""), fontContenido));
        formatoFacturaHuespedV.setHorizontalAlignment(Element.ALIGN_LEFT);
        formatoFacturaHuespedV.setBorder(Rectangle.NO_BORDER);

        PdfPCell leyendaHuespedV = new PdfPCell(new Phrase(hmapTagIdioma.get(EnumTagPlantilla.MIC_REFERENCIA).concat(" : ").concat((facturaDto.getNumeroReferencia() != null ? facturaDto.getNumeroReferencia() : "")), fontContenido));
        leyendaHuespedV.setHorizontalAlignment(Element.ALIGN_LEFT);
        leyendaHuespedV.setColspan(2);
        leyendaHuespedV.setBorder(Rectangle.NO_BORDER);

        tableDatosClienteOpera.addCell(cellDatosCliente);
        tableDatosClienteOpera.addCell(rfcHuespedV);
        tableDatosClienteOpera.addCell(voucherHuespedV);
        tableDatosClienteOpera.addCell(cuponHuespedV);
        tableDatosClienteOpera.addCell(ciudadHuespedV);
        tableDatosClienteOpera.addCell(folioHuespedV);
        tableDatosClienteOpera.addCell(habitacionHuespedV);
        tableDatosClienteOpera.addCell(reservacionHuespedV);
        tableDatosClienteOpera.addCell(cajeroHuespedV);
        tableDatosClienteOpera.addCell(formatoFacturaHuespedV);
        tableDatosClienteOpera.addCell(leyendaHuespedV);

        if(facturaDto.getTenantAData()!=null){

            PdfPCell contraCodeV = new PdfPCell(new Phrase(hmapTagIdioma.get(EnumTagPlantilla.P_CONTRA_CODE).concat(" : ").concat(Optional.ofNullable(facturaDto.getTenantAData().getContraCode()).orElse("")), fontContenido));
            contraCodeV.setHorizontalAlignment(Element.ALIGN_LEFT);
            contraCodeV.setBorder(Rectangle.NO_BORDER);
            tableDatosClienteOpera.addCell(contraCodeV);

            PdfPCell companiaV = new PdfPCell(new Phrase(hmapTagIdioma.get(EnumTagPlantilla.P_COMPANIA).concat(" : ").concat(Optional.ofNullable(facturaDto.getTenantAData().getCompania()).orElse("")), fontContenido));
            companiaV.setHorizontalAlignment(Element.ALIGN_LEFT);
            companiaV.setBorder(Rectangle.NO_BORDER);
            tableDatosClienteOpera.addCell(companiaV);

            PdfPCell empty = new PdfPCell();
            empty.setBorder(Rectangle.NO_BORDER);
            tableDatosClienteOpera.addCell(empty);

            PdfPCell agenciaV = new PdfPCell(new Phrase(hmapTagIdioma.get(EnumTagPlantilla.P_AGENCIA).concat(" : ").concat(Optional.ofNullable(facturaDto.getTenantAData().getAgencia()).orElse("")), fontContenido));
            agenciaV.setHorizontalAlignment(Element.ALIGN_LEFT);
            agenciaV.setBorder(Rectangle.NO_BORDER);
            tableDatosClienteOpera.addCell(agenciaV);
        }






        document.add(tableDatosClienteOpera);
    }

    /**
     * Metodo para agregar los datos del cliente (Formato Especial) cupon,
     * voucher, etc * Formatos: 90, 91
     *
     * @param document
     * @throws BusinessException
     * @throws ParseException
     */
    private void agregarTableDatosClienteOperaSinVoucher(Document document, CFDI facturaDto) throws DocumentException, ParseException {
        PdfPTable tableDatosClienteOpera = new PdfPTable(4);
        tableDatosClienteOpera.setWidths(new int[]{100, 100, 100, 100});
        tableDatosClienteOpera.setTotalWidth(ANCHO_TOTAL_TABLA);
        tableDatosClienteOpera.setLockedWidth(true);
        tableDatosClienteOpera.setWidthPercentage(PORCENTAJE_ANCHO_TABLA);
        tableDatosClienteOpera.getDefaultCell().setFixedHeight(100f);
        tableDatosClienteOpera.getDefaultCell().setBorder(Rectangle.NO_BORDER);

        PdfPCell cellDatosCliente = new PdfPCell(new Phrase("", fontContenido));
        cellDatosCliente.setColspan(4);
        cellDatosCliente.setBackgroundColor(BaseColor.LIGHT_GRAY);
        cellDatosCliente.setHorizontalAlignment(Element.ALIGN_LEFT);
        cellDatosCliente.setBorder(Rectangle.NO_BORDER);
        cellDatosCliente.setFixedHeight(5f);

        PdfPCell rfcHuespedV = new PdfPCell(new Phrase(hmapTagIdioma.get(EnumTagPlantilla.P_HUESPED_OPE).concat(" : ").concat((facturaDto.getHospedaje() != null && facturaDto.getHospedaje().getHuesped() != null ? facturaDto.getHospedaje().getHuesped() : "")), fontContenido));
        rfcHuespedV.setColspan(4);
        rfcHuespedV.setHorizontalAlignment(Element.ALIGN_LEFT);
        rfcHuespedV.setBorder(Rectangle.NO_BORDER);

        String chekIn = "";
        if (facturaDto.getHospedaje() != null && facturaDto.getHospedaje().getChekIn() != null) {
            Date dateCheckIn = facturaDto.getHospedaje().getChekIn();
            DateFormat dateFormatCheckIn = new SimpleDateFormat("yyyyMMdd");
            chekIn = dateFormatCheckIn.format(dateCheckIn);
        }
        String chekOut = "";
        if (facturaDto.getHospedaje() != null && facturaDto.getHospedaje().getChekOut() != null) {
            Date dateCheckOut = facturaDto.getHospedaje().getChekOut();
            DateFormat dateFormatCheckOut = new SimpleDateFormat("yyyyMMdd");
            chekOut = dateFormatCheckOut.format(dateCheckOut);
        }
        PdfPCell ciudadHuespedV = new PdfPCell(new Phrase(hmapTagIdioma.get(EnumTagPlantilla.P_ESTANCIA_OPE) + " : " + chekIn + "      " + chekOut, fontContenido));
        ciudadHuespedV.setHorizontalAlignment(Element.ALIGN_LEFT);
        ciudadHuespedV.setBorder(Rectangle.NO_BORDER);

        PdfPCell folioHuespedV = new PdfPCell(new Phrase(hmapTagIdioma.get(EnumTagPlantilla.P_FOLIO).concat(" : ").concat(facturaDto.getHospedaje() != null && facturaDto.getHospedaje().getFolio() != null ? facturaDto.getHospedaje().getFolio() : "").concat(" - ").concat(facturaDto.getHospedaje() != null && facturaDto.getHospedaje().getExtencion() != null ? facturaDto.getHospedaje().getExtencion().trim() : ""), fontContenido));
        folioHuespedV.setHorizontalAlignment(Element.ALIGN_LEFT);
        folioHuespedV.setBorder(Rectangle.NO_BORDER);

        PdfPCell habitacionHuespedV = new PdfPCell(new Phrase(hmapTagIdioma.get(EnumTagPlantilla.P_HABITACION_OPE).concat(" : ").concat((facturaDto.getHospedaje() != null && facturaDto.getHospedaje().getHabitacion() != null ? facturaDto.getHospedaje().getHabitacion() : "")), fontContenido));
        habitacionHuespedV.setHorizontalAlignment(Element.ALIGN_LEFT);
        habitacionHuespedV.setBorder(Rectangle.NO_BORDER);

        PdfPCell reservacionHuespedV = new PdfPCell(new Phrase(hmapTagIdioma.get(EnumTagPlantilla.P_RESERVACION_OPE).concat(" : ").concat((facturaDto.getHospedaje() != null && facturaDto.getHospedaje().getReserva() != null ? facturaDto.getHospedaje().getReserva() : "")), fontContenido));
        reservacionHuespedV.setHorizontalAlignment(Element.ALIGN_LEFT);
        reservacionHuespedV.setBorder(Rectangle.NO_BORDER);

        PdfPCell cajeroHuespedV = new PdfPCell(new Phrase(hmapTagIdioma.get(EnumTagPlantilla.P_CAJERO).concat(" : ").concat((facturaDto.getHospedaje() != null && facturaDto.getHospedaje().getTerminal() != null ? facturaDto.getHospedaje().getTerminal() : "")), fontContenido));
        cajeroHuespedV.setHorizontalAlignment(Element.ALIGN_LEFT);
        cajeroHuespedV.setBorder(Rectangle.NO_BORDER);

        PdfPCell formatoFacturaHuespedV = new PdfPCell(new Phrase(hmapTagIdioma.get(EnumTagPlantilla.P_FORMATOFACTURA).concat(" : ").concat(facturaDto.getFormato() != null ? facturaDto.getFormato() : ""), fontContenido));
        formatoFacturaHuespedV.setHorizontalAlignment(Element.ALIGN_LEFT);
        formatoFacturaHuespedV.setBorder(Rectangle.NO_BORDER);

        PdfPCell leyendaHuespedV = new PdfPCell(new Phrase(hmapTagIdioma.get(EnumTagPlantilla.MIC_REFERENCIA).concat(" : ").concat((facturaDto.getNumeroReferencia() != null ? facturaDto.getNumeroReferencia() : "")), fontContenido));
        leyendaHuespedV.setHorizontalAlignment(Element.ALIGN_LEFT);
        leyendaHuespedV.setColspan(2);
        leyendaHuespedV.setBorder(Rectangle.NO_BORDER);

        tableDatosClienteOpera.addCell(cellDatosCliente);
        tableDatosClienteOpera.addCell(rfcHuespedV);
        tableDatosClienteOpera.addCell(ciudadHuespedV);
        tableDatosClienteOpera.addCell(folioHuespedV);
        tableDatosClienteOpera.addCell(habitacionHuespedV);
        tableDatosClienteOpera.addCell(reservacionHuespedV);
        tableDatosClienteOpera.addCell(cajeroHuespedV);
        tableDatosClienteOpera.addCell(formatoFacturaHuespedV);
        tableDatosClienteOpera.addCell(leyendaHuespedV);

        if (facturaDto.getTenantAData() != null) {

            PdfPCell contraCodeV = new PdfPCell(new Phrase(hmapTagIdioma.get(EnumTagPlantilla.P_CONTRA_CODE).concat(" : ").concat(Optional.ofNullable(facturaDto.getTenantAData().getContraCode()).orElse(Strings.EMPTY)), fontContenido));
            contraCodeV.setHorizontalAlignment(Element.ALIGN_LEFT);
            contraCodeV.setBorder(Rectangle.NO_BORDER);
            tableDatosClienteOpera.addCell(contraCodeV);

            PdfPCell companiaV = new PdfPCell(new Phrase(hmapTagIdioma.get(EnumTagPlantilla.P_COMPANIA).concat(" : ").concat(Optional.ofNullable(facturaDto.getTenantAData().getCompania()).orElse(Strings.EMPTY)), fontContenido));
            companiaV.setHorizontalAlignment(Element.ALIGN_LEFT);
//            companiaV.setColspan(3);
            companiaV.setBorder(Rectangle.NO_BORDER);
            tableDatosClienteOpera.addCell(companiaV);

            PdfPCell agenciaV = new PdfPCell(new Phrase(hmapTagIdioma.get(EnumTagPlantilla.P_AGENCIA).concat(" : ").concat(Optional.ofNullable(facturaDto.getTenantAData().getAgencia()).orElse(Strings.EMPTY)), fontContenido));
            agenciaV.setHorizontalAlignment(Element.ALIGN_LEFT);
            agenciaV.setBorder(Rectangle.NO_BORDER);
            agenciaV.setColspan(2);
            tableDatosClienteOpera.addCell(agenciaV);

        }



        document.add(tableDatosClienteOpera);
    }

    /**
     * Metodo para agregar los datos del cliente (Formato Especial) cupon,
     * voucher, etc * Formatos: 90, 91
     *
     * @param document
     * @throws DocumentException
     */
    private void agregarTableDatosClienteOperaEspecial(Document document, CFDI facturaDto) throws DocumentException {
        PdfPTable tableDatosClienteOperaEspecial = new PdfPTable(4);
        tableDatosClienteOperaEspecial.setWidths(new int[]{100, 100, 100, 100});
        tableDatosClienteOperaEspecial.setTotalWidth(ANCHO_TOTAL_TABLA);
        tableDatosClienteOperaEspecial.setLockedWidth(true);
        tableDatosClienteOperaEspecial.setWidthPercentage(PORCENTAJE_ANCHO_TABLA);
        tableDatosClienteOperaEspecial.getDefaultCell().setFixedHeight(100f);
        tableDatosClienteOperaEspecial.getDefaultCell().setBorder(Rectangle.NO_BORDER);

        PdfPCell cellDatosCliente = new PdfPCell(new Phrase("", fontContenido));
        cellDatosCliente.setColspan(4);
        cellDatosCliente.setBackgroundColor(BaseColor.LIGHT_GRAY);
        cellDatosCliente.setHorizontalAlignment(Element.ALIGN_LEFT);
        cellDatosCliente.setBorder(Rectangle.NO_BORDER);
        cellDatosCliente.setFixedHeight(5f);

        PdfPCell folioHuespedV = new PdfPCell(new Phrase(hmapTagIdioma.get(EnumTagPlantilla.P_FOLIO).concat(" : ").concat(facturaDto.getHospedaje() != null && facturaDto.getHospedaje().getFolio() != null ? facturaDto.getHospedaje().getFolio() : "").concat(" - ").concat(facturaDto.getHospedaje() != null && facturaDto.getHospedaje().getExtencion() != null ? facturaDto.getHospedaje().getExtencion().trim() : ""), fontContenido));
        folioHuespedV.setHorizontalAlignment(Element.ALIGN_LEFT);
        folioHuespedV.setBorder(Rectangle.NO_BORDER);

        PdfPCell cajeroHuespedV = new PdfPCell(new Phrase(hmapTagIdioma.get(EnumTagPlantilla.P_CAJERO).concat(" : ").concat((facturaDto.getHospedaje() != null && facturaDto.getHospedaje().getTerminal() != null ? facturaDto.getHospedaje().getTerminal() : "")), fontContenido));
//        cajeroHuespedV.setColspan(3);
        cajeroHuespedV.setHorizontalAlignment(Element.ALIGN_LEFT);
        cajeroHuespedV.setBorder(Rectangle.NO_BORDER);

        PdfPCell formatoFacturaHuespedV = new PdfPCell(new Phrase(hmapTagIdioma.get(EnumTagPlantilla.P_FORMATOFACTURA).concat(" : ").concat((facturaDto.getHospedaje() != null && facturaDto.getFormato() != null ? facturaDto.getFormato() : "")), fontContenido));
        formatoFacturaHuespedV.setHorizontalAlignment(Element.ALIGN_LEFT);
        formatoFacturaHuespedV.setBorder(Rectangle.NO_BORDER);

        PdfPCell leyendaHuespedV = new PdfPCell(new Phrase(hmapTagIdioma.get(EnumTagPlantilla.MIC_REFERENCIA).concat(" : ").concat((facturaDto.getNumeroReferencia() != null ? facturaDto.getNumeroReferencia() : "")), fontContenido));
//        leyendaHuespedV.setColspan(3);
        leyendaHuespedV.setHorizontalAlignment(Element.ALIGN_LEFT);
        leyendaHuespedV.setBorder(Rectangle.NO_BORDER);

        tableDatosClienteOperaEspecial.addCell(cellDatosCliente);
        tableDatosClienteOperaEspecial.addCell(folioHuespedV);
        tableDatosClienteOperaEspecial.addCell(cajeroHuespedV);
        tableDatosClienteOperaEspecial.addCell(formatoFacturaHuespedV);
        tableDatosClienteOperaEspecial.addCell(leyendaHuespedV);

        if (facturaDto.getTenantAData() != null) {

            if (facturaDto.getTenantAData().getAgencia() != null && !facturaDto.getTenantAData().getAgencia().trim().isEmpty()) {

                PdfPCell agenciaV = new PdfPCell(new Phrase(hmapTagIdioma.get(EnumTagPlantilla.P_AGENCIA).concat(" : ").concat(facturaDto.getTenantAData().getAgencia()), fontContenido));
                agenciaV.setColspan(4);
                agenciaV.setHorizontalAlignment(Element.ALIGN_LEFT);
                agenciaV.setBorder(Rectangle.NO_BORDER);
                tableDatosClienteOperaEspecial.addCell(agenciaV);
            }
        }

        document.add(tableDatosClienteOperaEspecial);
    }

    /**
     * Metodo para agregar los encabezados del producto * Formatos: 03, 05, 06,
     * 20, 21, 23, 51.
     *
     * @param document
     * @throws DocumentException
     */
    private void agregarTableEncabezadoProducto(Document document) throws DocumentException {
        PdfPTable tableHeaderProducto = new PdfPTable(6);
        tableHeaderProducto.setWidths(new int[]{50, 65, 65, 150, 100, 100});
        tableHeaderProducto.setTotalWidth(ANCHO_TOTAL_TABLA);
        tableHeaderProducto.setLockedWidth(true);
        tableHeaderProducto.setWidthPercentage(PORCENTAJE_ANCHO_TABLA);
        tableHeaderProducto.getDefaultCell().setFixedHeight(100f);
        tableHeaderProducto.getDefaultCell().setBorder(Rectangle.NO_BORDER);

        PdfPCell productosYServiciosC = new PdfPCell(new Phrase(hmapTagIdioma.get(EnumTagPlantilla.P_PRODUCTOS), fontEtiquetas));
        productosYServiciosC.setColspan(6);
        productosYServiciosC.setBackgroundColor(BaseColor.LIGHT_GRAY);
        productosYServiciosC.setHorizontalAlignment(Element.ALIGN_LEFT);
        productosYServiciosC.setBorder(Rectangle.NO_BORDER);

        PdfPCell cantidadC = new PdfPCell(new Phrase(hmapTagIdioma.get(EnumTagPlantilla.P_CANTIDAD), fontContenido));
        cantidadC.setHorizontalAlignment(Element.ALIGN_LEFT);
        cantidadC.setBorder(Rectangle.NO_BORDER);

        PdfPCell conceptosC = new PdfPCell(new Phrase(hmapTagIdioma.get(EnumTagPlantilla.P_CONCEPTO), fontContenido));
        conceptosC.setHorizontalAlignment(Element.ALIGN_LEFT);
        conceptosC.setBorder(Rectangle.NO_BORDER);

        PdfPCell precioUnitarioC = new PdfPCell(new Phrase(hmapTagIdioma.get(EnumTagPlantilla.P_PRECIOUNITARIO), fontContenido));
        precioUnitarioC.setHorizontalAlignment(Element.ALIGN_RIGHT);
        precioUnitarioC.setBorder(Rectangle.NO_BORDER);

        PdfPCell importeC = new PdfPCell(new Phrase(hmapTagIdioma.get(EnumTagPlantilla.P_IMPORTE), fontContenido));
        importeC.setHorizontalAlignment(Element.ALIGN_RIGHT);
        importeC.setBorder(Rectangle.NO_BORDER);

        tableHeaderProducto.addCell(productosYServiciosC);
        tableHeaderProducto.addCell(cantidadC);

        PdfPCell unidadC = new PdfPCell(new Phrase(hmapTagIdioma.get(EnumTagPlantilla.P_CLAVEUNIDAD), fontContenido));
        unidadC.setHorizontalAlignment(Element.ALIGN_LEFT);
        unidadC.setBorder(Rectangle.NO_BORDER);

        PdfPCell claveProductoC = new PdfPCell(new Phrase(hmapTagIdioma.get(EnumTagPlantilla.P_CLAVEPRODUCTO), fontContenido));
        claveProductoC.setHorizontalAlignment(Element.ALIGN_LEFT);
        claveProductoC.setBorder(Rectangle.NO_BORDER);

        tableHeaderProducto.addCell(unidadC);
        tableHeaderProducto.addCell(claveProductoC);
        tableHeaderProducto.addCell(conceptosC);
        tableHeaderProducto.addCell(precioUnitarioC);
        tableHeaderProducto.addCell(importeC);

        document.add(tableHeaderProducto);
    }

    /**
     * Metodo para agregar los encabezados de los productos * Formatos
     * Especiales: 00, 01, 02, 04, 50, 80, 81, 90, 91.
     *
     * @param document
     * @throws DocumentException
     */
    private void agregarTableEncabezadoProductoEspecial(Document document) throws DocumentException {
        PdfPTable tableHeaderProducto = new PdfPTable(6);
        tableHeaderProducto.setWidths(new int[]{50, 65, 65, 150, 100, 100});
        tableHeaderProducto.setTotalWidth(ANCHO_TOTAL_TABLA);
        tableHeaderProducto.setLockedWidth(true);
        tableHeaderProducto.setWidthPercentage(PORCENTAJE_ANCHO_TABLA);
        tableHeaderProducto.getDefaultCell().setFixedHeight(100f);
        tableHeaderProducto.getDefaultCell().setBorder(Rectangle.NO_BORDER);

        PdfPCell productosYServiciosC = new PdfPCell(new Phrase(hmapTagIdioma.get(EnumTagPlantilla.P_PRODUCTOS), fontEtiquetas));
        productosYServiciosC.setColspan(6);
        productosYServiciosC.setBackgroundColor(BaseColor.LIGHT_GRAY);
        productosYServiciosC.setHorizontalAlignment(Element.ALIGN_LEFT);
        productosYServiciosC.setBorder(Rectangle.NO_BORDER);

        PdfPCell cantidadC = new PdfPCell(new Phrase(hmapTagIdioma.get(EnumTagPlantilla.P_CANTIDAD), fontContenido));
        cantidadC.setHorizontalAlignment(Element.ALIGN_LEFT);
        cantidadC.setBorder(Rectangle.NO_BORDER);

        PdfPCell conceptosC = new PdfPCell(new Phrase(hmapTagIdioma.get(EnumTagPlantilla.P_CONCEPTO), fontContenido));
        conceptosC.setHorizontalAlignment(Element.ALIGN_LEFT);
        conceptosC.setBorder(Rectangle.NO_BORDER);

        PdfPCell precioUnitarioC = new PdfPCell(new Phrase(hmapTagIdioma.get(EnumTagPlantilla.P_PRECIOUNITARIO), fontContenido));
        precioUnitarioC.setHorizontalAlignment(Element.ALIGN_RIGHT);
        precioUnitarioC.setBorder(Rectangle.NO_BORDER);

        PdfPCell importeC = new PdfPCell(new Phrase(hmapTagIdioma.get(EnumTagPlantilla.P_IMPORTE), fontContenido));
        importeC.setHorizontalAlignment(Element.ALIGN_RIGHT);
        importeC.setBorder(Rectangle.NO_BORDER);

        tableHeaderProducto.addCell(productosYServiciosC);
        tableHeaderProducto.addCell(cantidadC);

        PdfPCell unidadC = new PdfPCell(new Phrase(hmapTagIdioma.get(EnumTagPlantilla.P_UNIDADMEDIDA), fontContenido));
        unidadC.setHorizontalAlignment(Element.ALIGN_LEFT);
        unidadC.setBorder(Rectangle.NO_BORDER);

        PdfPCell claveProductoC = new PdfPCell(new Phrase(hmapTagIdioma.get(EnumTagPlantilla.P_CLAVEPRODUCTO), fontContenido));
        claveProductoC.setHorizontalAlignment(Element.ALIGN_LEFT);
        claveProductoC.setBorder(Rectangle.NO_BORDER);

        tableHeaderProducto.addCell(unidadC);
        tableHeaderProducto.addCell(claveProductoC);
        tableHeaderProducto.addCell(conceptosC);
        tableHeaderProducto.addCell(precioUnitarioC);
        tableHeaderProducto.addCell(importeC);

        document.add(tableHeaderProducto);
    }

    /**
     * Metodo para agregar los productos en el PDF * Todos los formatos.
     *
     * @param document
     * @throws DocumentException
     */
    private void agregarTableAgregaProducto(Document document, CFDI facturaDto) throws DocumentException {
        StringBuilder descConcepto;

        PdfPTable tableLlenadoConceptos = new PdfPTable(6);
        tableLlenadoConceptos.setWidths(new int[]{50, 65, 65, 150, 100, 100});
        tableLlenadoConceptos.setTotalWidth(ANCHO_TOTAL_TABLA);
        tableLlenadoConceptos.setLockedWidth(true);
        tableLlenadoConceptos.setWidthPercentage(PORCENTAJE_ANCHO_TABLA);
        tableLlenadoConceptos.getDefaultCell().setFixedHeight(400f);

        for (Conceptos conceptosCargados : facturaDto.getConceptos()) {
            descConcepto = new StringBuilder();
            descConcepto.append("\n");
            descConcepto.append(hmapTagIdioma.get(EnumTagPlantilla.S_IMPOBJECT)).append(":  ").append(conceptosCargados.getObjetoImp());
            if (conceptosCargados.getImpuestos() != null && conceptosCargados.getImpuestos().getTraslados() != null && !conceptosCargados.getImpuestos().getTraslados().isEmpty()) {
                descConcepto.append(armarConceptoImpuestoTraslados(conceptosCargados));
            }
            
            if (conceptosCargados.getImpuestos() != null && conceptosCargados.getImpuestos().getRetenciones() != null && !conceptosCargados.getImpuestos().getRetenciones().isEmpty()) {
                descConcepto.append(armarConceptoImpuestoRetenciones(conceptosCargados));
            }

            if (conceptosCargados.getDescuento() != null && !conceptosCargados.getDescuento().equals(BigDecimal.ZERO)) {
                descConcepto.append(armarConceptoDescuento(conceptosCargados));
            }

            if (conceptosCargados.getCuentaPredial() != null) {
                for(CuentaPredial cuentaPredialCagados : conceptosCargados.getCuentaPredial()){
                    descConcepto.append(armarConceptoCuentaPredial(cuentaPredialCagados));

                }
               
            }
            if (conceptosCargados.getACuentaTerceros()!= null) {
                descConcepto.append(armarConceptoCuentaTerceros(conceptosCargados));
            }

            PdfPCell celdaDescripcion = new PdfPCell(new Paragraph(new Phrase(conceptosCargados.getDescripcion().concat(descConcepto.toString()), fontContenido)));
            celdaDescripcion.setHorizontalAlignment(Element.ALIGN_LEFT);
            celdaDescripcion.setBorder(Rectangle.NO_BORDER);
            PdfPCell celdaUnidad = new PdfPCell(new Paragraph(new Phrase(conceptosCargados.getClaveUnidad(), fontContenido)));
            celdaUnidad.setHorizontalAlignment(Element.ALIGN_LEFT);
            celdaUnidad.setBorder(Rectangle.NO_BORDER);
            PdfPCell celdaClaveProducto = new PdfPCell(new Paragraph(new Phrase(conceptosCargados.getClaveProdServ(), fontContenido)));
            celdaClaveProducto.setHorizontalAlignment(Element.ALIGN_LEFT);
            celdaClaveProducto.setBorder(Rectangle.NO_BORDER);
            PdfPCell celdaCantidad = new PdfPCell(new Paragraph(new Phrase(conceptosCargados.getCantidad().toString(), fontContenido)));
            celdaCantidad.setHorizontalAlignment(Element.ALIGN_LEFT);
            celdaCantidad.setBorder(Rectangle.NO_BORDER);
            PdfPCell celdaPUnitario = new PdfPCell(new Paragraph(new Phrase(String.valueOf(conceptosCargados.getValorUnitario() != null ? conceptosCargados.getValorUnitario() : "0.0"), fontContenido)));
            celdaPUnitario.setHorizontalAlignment(Element.ALIGN_RIGHT);
            celdaPUnitario.setBorder(Rectangle.NO_BORDER);
            PdfPCell celdaImporte = new PdfPCell(new Paragraph(new Phrase(String.valueOf(conceptosCargados.getImporte() != null ? conceptosCargados.getImporte() : "0.0"), fontContenido)));
            celdaImporte.setHorizontalAlignment(Element.ALIGN_RIGHT);
            celdaImporte.setBorder(Rectangle.NO_BORDER);

            tableLlenadoConceptos.addCell(celdaCantidad);
            tableLlenadoConceptos.addCell(celdaUnidad);
            tableLlenadoConceptos.addCell(celdaClaveProducto);
            tableLlenadoConceptos.addCell(celdaDescripcion);
            tableLlenadoConceptos.addCell(celdaPUnitario);
            tableLlenadoConceptos.addCell(celdaImporte);
        }

        document.add(tableLlenadoConceptos);
    }

    /**
     * Metodo para agregar los productos en el PDF * Todos los formatos.
     *
     * @param document
     * @throws DocumentException
     */
    private void agregarTableAgregaProductoSAT(Document document, CFDI facturaDto) throws DocumentException {
        StringBuilder descConcepto;

        PdfPTable tableLlenadoConceptos = new PdfPTable(6);
        tableLlenadoConceptos.setWidths(new int[]{50, 65, 70, 150, 100, 100});
        tableLlenadoConceptos.setTotalWidth(ANCHO_TOTAL_TABLA);
        tableLlenadoConceptos.setLockedWidth(true);
        tableLlenadoConceptos.setWidthPercentage(PORCENTAJE_ANCHO_TABLA);
        tableLlenadoConceptos.getDefaultCell().setFixedHeight(400f);

        for (Conceptos conceptosCargados : facturaDto.getConceptos()) {
            descConcepto = new StringBuilder();
            if (conceptosCargados.getImpuestos() != null && !conceptosCargados.getImpuestos().getTraslados().isEmpty()) {
                descConcepto.append(armarConceptoImpuestoTraslados(conceptosCargados));
            }
            
            if (conceptosCargados.getImpuestos() != null && !conceptosCargados.getImpuestos().getTraslados().isEmpty()) {
                descConcepto.append(armarConceptoImpuestoRetenciones(conceptosCargados));
            }

            if (conceptosCargados.getDescuento() != null && !conceptosCargados.getDescuento().equals(BigDecimal.ZERO)) {
                descConcepto.append(armarConceptoDescuento(conceptosCargados));
            }

            if (conceptosCargados.getCuentaPredial() != null) {
                for (CuentaPredial cuentaPredialCargados : conceptosCargados.getCuentaPredial()) {
                    descConcepto.append(armarConceptoCuentaPredial(cuentaPredialCargados));
                }
                
            }

            PdfPCell celdaDescripcion = new PdfPCell(new Paragraph(new Phrase(conceptosCargados.getDescripcion().concat(descConcepto.toString()), fontContenido)));
            celdaDescripcion.setHorizontalAlignment(Element.ALIGN_LEFT);
            celdaDescripcion.setBorder(Rectangle.NO_BORDER);

            PdfPCell celdaUnidad = new PdfPCell(new Paragraph(new Phrase(conceptosCargados.getClaveUnidad(), fontContenido)));
            celdaUnidad.setHorizontalAlignment(Element.ALIGN_LEFT);
            celdaUnidad.setBorder(Rectangle.NO_BORDER);

            CatClaveProducto obtenerCatClaveProductoSatDto = catalogosSatService.getCatClaveProductoSat(conceptosCargados.getClaveProdServ());
            StringBuilder descConceptoClave = new StringBuilder();
            descConceptoClave.append(conceptosCargados.getClaveProdServ()).append(" ").append(obtenerCatClaveProductoSatDto.getDescripcion());
            
            PdfPCell celdaClaveProducto = new PdfPCell(new Paragraph(new Phrase(descConceptoClave.toString(), fontContenido)));
            celdaClaveProducto.setHorizontalAlignment(Element.ALIGN_LEFT);
            celdaClaveProducto.setBorder(Rectangle.NO_BORDER);
            PdfPCell celdaCantidad = new PdfPCell(new Paragraph(new Phrase(conceptosCargados.getCantidad().toString(), fontContenido)));
            celdaCantidad.setHorizontalAlignment(Element.ALIGN_LEFT);
            celdaCantidad.setBorder(Rectangle.NO_BORDER);
            PdfPCell celdaPUnitario = new PdfPCell(new Paragraph(new Phrase(String.valueOf(conceptosCargados.getValorUnitario() != null ? conceptosCargados.getValorUnitario() : "0.0"), fontContenido)));
            celdaPUnitario.setHorizontalAlignment(Element.ALIGN_RIGHT);
            celdaPUnitario.setBorder(Rectangle.NO_BORDER);
            PdfPCell celdaImporte = new PdfPCell(new Paragraph(new Phrase(String.valueOf(conceptosCargados.getImporte() != null ? conceptosCargados.getImporte() : "0.0"), fontContenido)));
            celdaImporte.setHorizontalAlignment(Element.ALIGN_RIGHT);
            celdaImporte.setBorder(Rectangle.NO_BORDER);

            tableLlenadoConceptos.addCell(celdaCantidad);
            tableLlenadoConceptos.addCell(celdaUnidad);
            tableLlenadoConceptos.addCell(celdaClaveProducto);
            tableLlenadoConceptos.addCell(celdaDescripcion);
            tableLlenadoConceptos.addCell(celdaPUnitario);
            tableLlenadoConceptos.addCell(celdaImporte);
        }

        document.add(tableLlenadoConceptos);
    }

    public StringBuilder armarConceptoCuentaPredial(Conceptos.CuentaPredial cuentaPredial) {
        StringBuilder descConcepto = new StringBuilder();
        try {
            descConcepto.append("\n");
            descConcepto.append("  No. Cuenta Predial: ").append(cuentaPredial.getNumero());
            return descConcepto;
        } catch (Exception e) {
            System.out.println("Ocurrio un error al tratar de armar el concepto con cuenta predial" + e.getMessage());
            return new StringBuilder("");
        }
    }

    public StringBuilder armarConceptoImpuestoTraslados(Conceptos conceptos) {
        StringBuilder descConcepto = new StringBuilder();
        try {
            for (Conceptos.Traslados impuestosCargados : conceptos.getImpuestos().getTraslados()) {
                descConcepto.append("\n");
                CatImpuestos obtenerCatImpuestosSatByClave = catalogosSatService.obtenerCatImpuestoSatByClave(impuestosCargados.getImpuesto());
                descConcepto.append("Impuesto Trasladado:  ").append((obtenerCatImpuestosSatByClave.getDescripcion() != null ? obtenerCatImpuestosSatByClave.getDescripcion() : ""))
                        .append("  Tasa/Cuota: ").append((String.valueOf(impuestosCargados.getTasaoCuota() != null ? impuestosCargados.getTasaoCuota() : "")))
                        .append("  Tipo Factor: ").append((impuestosCargados.getTipoFactor() != null ? impuestosCargados.getTipoFactor() : ""))
                        .append("  Importe $").append((String.valueOf(impuestosCargados.getImporte() != null ? impuestosCargados.getImporte() : "")))
                        .append("  Base: ").append(impuestosCargados.getBase() != null ? impuestosCargados.getBase() : "");
            }
            return descConcepto;
        } catch (Exception e) {
            System.out.println("Ocurrio un error al tratar de armar el concepto con impuestos trasladado" + e.getMessage());
            return new StringBuilder("");
        }
    }
    
    public StringBuilder armarConceptoImpuestoRetenciones(Conceptos conceptos) {
        StringBuilder descConcepto = new StringBuilder();
        try {
            if (conceptos.getImpuestos() != null && conceptos.getImpuestos().getRetenciones() != null) {


                for (Conceptos.Retenciones impuestosCargados : conceptos.getImpuestos().getRetenciones()) {
                    descConcepto.append("\n");
                    CatImpuestos obtenerCatImpuestosSatByClave = catalogosSatService.obtenerCatImpuestoSatByClave(impuestosCargados.getImpuesto());
                    descConcepto.append("Impuesto Retenido:  ").append((obtenerCatImpuestosSatByClave.getDescripcion() != null ? obtenerCatImpuestosSatByClave.getDescripcion() : ""))
                            .append("  Tasa/Cuota: ").append((String.valueOf(impuestosCargados.getTasaoCuota() != null ? impuestosCargados.getTasaoCuota() : "")))
                            .append("  Tipo Factor: ").append((impuestosCargados.getTipoFactor() != null ? impuestosCargados.getTipoFactor() : ""))
                            .append("  Importe $").append((String.valueOf(impuestosCargados.getImporte() != null ? impuestosCargados.getImporte() : "")))
                            .append("  Base: ").append(impuestosCargados.getBase() != null ? impuestosCargados.getBase() : "");
                }
            }
            return descConcepto;
        } catch (Exception e) {

            System.out.println("Ocurrio un error al tratar de armar el concepto con impuestos retenido " + e.getMessage());
            return new StringBuilder("");
        }
    }

    public StringBuilder armarConceptoDescuento(Conceptos conceptos) {
        StringBuilder descConcepto = new StringBuilder();
        try {
            descConcepto.append("\n");
            descConcepto.append("Descuento:  ").append((conceptos.getDescuento() != null && conceptos.getDescuento() != BigDecimal.ZERO ? conceptos.getDescuento() : BigDecimal.ZERO));
            return descConcepto;
        } catch (Exception e) {
            System.out.println("Ocurrio un error al tratar de armar el concepto con impuestos trasladado" + e.getMessage());
            return new StringBuilder("");
        }
    }

    /**
     * Metodo para agregar los subtotales, totales, etc * Todos los formatos.
     *
     * @param document
     * @throws DocumentException
     */
    private void agregarTableSubtotales(Document document, CFDI facturaDto) throws DocumentException {
        PdfPTable tableSubtotales = new PdfPTable(3);
        tableSubtotales.setWidths(new int[]{140, 100, 50});
        tableSubtotales.setTotalWidth(ANCHO_TOTAL_TABLA);
        tableSubtotales.setLockedWidth(true);
        tableSubtotales.setWidthPercentage(PORCENTAJE_ANCHO_TABLA);
        tableSubtotales.getDefaultCell().setFixedHeight(80f);
        tableSubtotales.getDefaultCell().setBorder(Rectangle.NO_BORDER);

        PdfPCell subtotalC = new PdfPCell(new Phrase(hmapTagIdioma.get(EnumTagPlantilla.T_SUBTOTAL), fontContenido));
        subtotalC.setColspan(2);
        subtotalC.setHorizontalAlignment(Element.ALIGN_RIGHT);
        subtotalC.setBorder(Rectangle.NO_BORDER);
        tableSubtotales.addCell(subtotalC);

        PdfPCell subtotalV = new PdfPCell(new Phrase(String.valueOf(facturaDto.getSubTotal() != null ? facturaDto.getSubTotal() : ""), fontContenido));
        subtotalV.setHorizontalAlignment(Element.ALIGN_RIGHT);
        subtotalV.setBorder(Rectangle.NO_BORDER);
        tableSubtotales.addCell(subtotalV);

        if (facturaDto.getDescuento() != null && !facturaDto.getDescuento().equals(BigDecimal.ZERO)) {
            PdfPCell descuentoC = new PdfPCell(new Phrase(hmapTagIdioma.get(EnumTagPlantilla.T_DESCUENTO), fontContenido));
            descuentoC.setColspan(2);
            descuentoC.setHorizontalAlignment(Element.ALIGN_RIGHT);
            descuentoC.setBorder(Rectangle.NO_BORDER);
            tableSubtotales.addCell(descuentoC);

            PdfPCell descuentoV = new PdfPCell(new Phrase(String.valueOf(facturaDto.getDescuento()), fontContenido));
            descuentoV.setHorizontalAlignment(Element.ALIGN_RIGHT);
            descuentoV.setBorder(Rectangle.NO_BORDER);
            tableSubtotales.addCell(descuentoV);
        }

        String sImpuesto = "";
        if (facturaDto.getImpuestos() != null) {
        	if (facturaDto.getImpuestos().getTraslado() != null && !facturaDto.getImpuestos().getTraslado().isEmpty()) {
	            for (Traslado impuestosTrasladados : facturaDto.getImpuestos().getTraslado()) {
	                CatImpuestos obtenerImpuestoCatalogo = catalogosSatService.obtenerCatImpuestoSatByClave(impuestosTrasladados.getImpuesto());
	                sImpuesto = obtenerImpuestoCatalogo.getDescripcion() + "(" + impuestosTrasladados.getTasaoCuota().doubleValue() + ")";
	
	                PdfPCell celdaImpuestoT = new PdfPCell(new Paragraph(new Phrase(sImpuesto, fontContenido)));
	                celdaImpuestoT.setColspan(2);
	                celdaImpuestoT.setHorizontalAlignment(Element.ALIGN_RIGHT);
	                celdaImpuestoT.setBorder(Rectangle.NO_BORDER);
	
	                PdfPCell celdaImpuesto = new PdfPCell(new Paragraph(new Phrase(String.valueOf(impuestosTrasladados.getImporte() != null ? impuestosTrasladados.getImporte() : ""), fontContenido)));
	                celdaImpuesto.setHorizontalAlignment(Element.ALIGN_RIGHT);
	                celdaImpuesto.setBorder(Rectangle.NO_BORDER);
	
	                tableSubtotales.addCell(celdaImpuestoT);
	                tableSubtotales.addCell(celdaImpuesto);
	            }
        	}
        	
        	if (facturaDto.getImpuestos().getRetenciones() != null && !facturaDto.getImpuestos().getRetenciones().isEmpty()) {
	            for (Retencion impuestosRetenidos : facturaDto.getImpuestos().getRetenciones()) {
	                CatImpuestos obtenerImpuestoCatalogo = catalogosSatService.obtenerCatImpuestoSatByClave(impuestosRetenidos.getImpuesto());
	                sImpuesto = obtenerImpuestoCatalogo.getDescripcion() + " Retenido";
	
	                PdfPCell celdaImpuestoR = new PdfPCell(new Paragraph(new Phrase(sImpuesto, fontContenido)));
	                celdaImpuestoR.setColspan(2);
	                celdaImpuestoR.setHorizontalAlignment(Element.ALIGN_RIGHT);
	                celdaImpuestoR.setBorder(Rectangle.NO_BORDER);
	
	                PdfPCell celdaImpuesto = new PdfPCell(new Paragraph(new Phrase(String.valueOf(impuestosRetenidos.getImporte() != null ? impuestosRetenidos.getImporte() : ""), fontContenido)));
	                celdaImpuesto.setHorizontalAlignment(Element.ALIGN_RIGHT);
	                celdaImpuesto.setBorder(Rectangle.NO_BORDER);
	
	                tableSubtotales.addCell(celdaImpuestoR);
	                tableSubtotales.addCell(celdaImpuesto);
	            }
        	}
        }
        String sImpuestoLocales = "";
        if (facturaDto.getImpuestosLocales() != null && facturaDto.getImpuestosLocales().getTrasladoLocales() != null && !facturaDto.getImpuestosLocales().getTrasladoLocales().isEmpty()) {
            for (TrasladoLocal imp : facturaDto.getImpuestosLocales().getTrasladoLocales()) {

                sImpuestoLocales = imp.getImpuesto();

                if (!imp.getImpuesto().toUpperCase().trim().equals(ConstantesFacto.ISH)) {

                    sImpuestoLocales = sImpuestoLocales.concat("(" + imp.getTasa().doubleValue() + ")");
                }

                PdfPCell celdaImpuestoT = new PdfPCell(new Paragraph(new Phrase(sImpuestoLocales, fontContenido)));
                celdaImpuestoT.setColspan(2);
                celdaImpuestoT.setHorizontalAlignment(Element.ALIGN_RIGHT);
                celdaImpuestoT.setBorder(Rectangle.NO_BORDER);

                PdfPCell celdaImpuesto = new PdfPCell(new Paragraph(new Phrase(String.valueOf(imp.getImporte() != null ? imp.getImporte() : ""), fontContenido)));
                celdaImpuesto.setHorizontalAlignment(Element.ALIGN_RIGHT);
                celdaImpuesto.setBorder(Rectangle.NO_BORDER);

                tableSubtotales.addCell(celdaImpuestoT);
                tableSubtotales.addCell(celdaImpuesto);
            }
        }
        //Utilities.crearTotalLetra(facturaDto);

        PdfPCell totalLetraV = new PdfPCell(new Phrase("*** " + Utilities.crearTotalLetra(facturaDto) + " ***", fontContenido));
        totalLetraV.setHorizontalAlignment(Element.ALIGN_LEFT);
        totalLetraV.setBorder(Rectangle.NO_BORDER);
        tableSubtotales.addCell(totalLetraV);

        if (!facturaDto.getTotal().equals(BigDecimal.ZERO)) {
            PdfPCell totalFacturadoC = new PdfPCell(new Paragraph(new Phrase(hmapTagIdioma.get(EnumTagPlantilla.T_TOTALFACT), fontContenido)));
            totalFacturadoC.setHorizontalAlignment(Element.ALIGN_RIGHT);
            totalFacturadoC.setBorder(Rectangle.NO_BORDER);

            PdfPCell totalFacturadoV = new PdfPCell(new Paragraph(new Phrase(String.valueOf(facturaDto.getTotal() != null ? facturaDto.getTotal() : ""), fontContenido)));
            totalFacturadoV.setHorizontalAlignment(Element.ALIGN_RIGHT);
            totalFacturadoV.setBorder(Rectangle.NO_BORDER);
            tableSubtotales.addCell(totalFacturadoC);
            tableSubtotales.addCell(totalFacturadoV);
        }

        StringBuilder notas = new StringBuilder();
        if (facturaDto.getLeyenda() != null && !facturaDto.getLeyenda().isEmpty()) {
            for (String leyendas : facturaDto.getLeyenda()) {
                notas = notas.append(leyendas).append("\n");
            }
        }
        PdfPCell notasC = new PdfPCell(new Phrase(notas.toString(), fontEtiquetas));
        notasC.setColspan(3);
        notasC.setHorizontalAlignment(Element.ALIGN_CENTER);
        notasC.setBorder(Rectangle.NO_BORDER);
        tableSubtotales.addCell(notasC);

        if (facturaDto.getMetodoPago() != null) {
            PdfPCell formasPagoC = new PdfPCell(new Phrase(hmapTagIdioma.get(EnumTagPlantilla.T_METODOPAGO).concat(" : ").concat(facturaDto.getMetodoPago()), fontEtiquetas));
            formasPagoC.setColspan(3);
            formasPagoC.setHorizontalAlignment(Element.ALIGN_LEFT);
            formasPagoC.setBorder(Rectangle.NO_BORDER);
            tableSubtotales.addCell(formasPagoC);
        } else {
            PdfPCell formasPagoC = new PdfPCell();
            formasPagoC.setColspan(3);
            formasPagoC.setHorizontalAlignment(Element.ALIGN_LEFT);
            formasPagoC.setBorder(Rectangle.NO_BORDER);
            tableSubtotales.addCell(formasPagoC);
        }

        document.add(tableSubtotales);
    }

    /**
     * Metodo para agregar los metodos de pago, numero de cuenta, tipo de
     * cambio, etc * Todos los formatos
     *
     * @param document
     * @throws BusinessException
     * @throws DocumentException
     */
    private void agregarTableMetodosDePago(Document document, CFDI facturaDto) throws BusinessException, DocumentException {
        PdfPTable tableMetodoDepago = new PdfPTable(6);
        tableMetodoDepago.setWidths(new int[]{70, 70, 110, 110, 50, 50});
        tableMetodoDepago.setTotalWidth(ANCHO_TOTAL_TABLA);
        tableMetodoDepago.setLockedWidth(true);
        tableMetodoDepago.setWidthPercentage(PORCENTAJE_ANCHO_TABLA);
        tableMetodoDepago.getDefaultCell().setFixedHeight(80f);
        tableMetodoDepago.getDefaultCell().setBorder(Rectangle.NO_BORDER);

        String formaPagoDescripcion = "";
        if (facturaDto.getFormaPago() != null && !facturaDto.getFormaPago().isEmpty()) {
             formaPagoDescripcion = facturaDto.getFormaPago().trim();
        }

        PdfPCell borderEncabezadoSucursal = new PdfPCell();
        borderEncabezadoSucursal.setColspan(6);
        borderEncabezadoSucursal.setBackgroundColor(BaseColor.LIGHT_GRAY);
        borderEncabezadoSucursal.setHorizontalAlignment(Element.ALIGN_LEFT);
        borderEncabezadoSucursal.setBorder(Rectangle.NO_BORDER);
        borderEncabezadoSucursal.setFixedHeight(5f);
        tableMetodoDepago.addCell(borderEncabezadoSucursal);

        PdfPCell metodoDePagoC = new PdfPCell(new Paragraph(new Phrase(hmapTagIdioma.get(EnumTagPlantilla.T_FORMAPAGO) + " : ", fontEtiquetas)));
        metodoDePagoC.setHorizontalAlignment(Element.ALIGN_LEFT);
        metodoDePagoC.setBorder(Rectangle.NO_BORDER);

        PdfPCell metodoDePagoV = new PdfPCell(new Paragraph(new Phrase(formaPagoDescripcion, fontContenido)));
        metodoDePagoV.setHorizontalAlignment(Element.ALIGN_LEFT);
        metodoDePagoV.setColspan(3);
        metodoDePagoV.setBorder(Rectangle.NO_BORDER);

        tableMetodoDepago.addCell(metodoDePagoC);
        tableMetodoDepago.addCell(metodoDePagoV);

        if (facturaDto.getHospedaje() != null && facturaDto.getHospedaje().getPaidout() == null) {
            PdfPCell paidOutC = new PdfPCell(new Phrase(hmapTagIdioma.get(EnumTagPlantilla.T_PAIDOUT).concat(" : "), fontEtiquetas));
            paidOutC.setHorizontalAlignment(Element.ALIGN_LEFT);
            paidOutC.setBorder(Rectangle.NO_BORDER);

            PdfPCell paidOutV = new PdfPCell(new Phrase("0.00", fontContenido));
            paidOutV.setHorizontalAlignment(Element.ALIGN_RIGHT);
            paidOutV.setBorder(Rectangle.NO_BORDER);

            tableMetodoDepago.addCell(paidOutC);
            tableMetodoDepago.addCell(paidOutV);
        } else {
            PdfPCell paidOutC = new PdfPCell(new Phrase(hmapTagIdioma.get(EnumTagPlantilla.T_PAIDOUT).concat(" : "), fontEtiquetas));
            paidOutC.setHorizontalAlignment(Element.ALIGN_LEFT);
            paidOutC.setBorder(Rectangle.NO_BORDER);

            PdfPCell paidOutV = new PdfPCell(new Phrase(String.valueOf(facturaDto.getHospedaje() != null && facturaDto.getHospedaje().getPaidout() != null ? facturaDto.getHospedaje().getPaidout() : "0.0"), fontContenido));
            paidOutV.setHorizontalAlignment(Element.ALIGN_RIGHT);
            paidOutV.setBorder(Rectangle.NO_BORDER);

            tableMetodoDepago.addCell(paidOutC);
            tableMetodoDepago.addCell(paidOutV);
        }

        PdfPCell tipoMonedaC = new PdfPCell(new Phrase(hmapTagIdioma.get(EnumTagPlantilla.P_MONEDA).concat(" : "), fontEtiquetas));
        tipoMonedaC.setHorizontalAlignment(Element.ALIGN_LEFT);
        tipoMonedaC.setBorder(Rectangle.NO_BORDER);

        PdfPCell tipoMonedaV = new PdfPCell(new Phrase(facturaDto.getMoneda() != null ? facturaDto.getMoneda() : "", fontContenido));
        tipoMonedaV.setHorizontalAlignment(Element.ALIGN_LEFT);
        tipoMonedaV.setColspan(3);
        tipoMonedaV.setBorder(Rectangle.NO_BORDER);

        tableMetodoDepago.addCell(tipoMonedaC);
        tableMetodoDepago.addCell(tipoMonedaV);

        if (facturaDto.getPropina() == null) {
            PdfPCell servicioC = new PdfPCell(new Phrase(hmapTagIdioma.get(EnumTagPlantilla.T_PROPINA) + " : ", fontEtiquetas));
            servicioC.setHorizontalAlignment(Element.ALIGN_LEFT);
            servicioC.setBorder(Rectangle.NO_BORDER);

            PdfPCell servicioV = new PdfPCell(new Phrase("0.00", fontContenido));
            servicioV.setHorizontalAlignment(Element.ALIGN_RIGHT);
            servicioV.setBorder(Rectangle.NO_BORDER);

            tableMetodoDepago.addCell(servicioC);
            tableMetodoDepago.addCell(servicioV);
        } else {
            PdfPCell servicioC = new PdfPCell(new Phrase(hmapTagIdioma.get(EnumTagPlantilla.T_PROPINA) + " : ", fontEtiquetas));
            servicioC.setHorizontalAlignment(Element.ALIGN_LEFT);
            servicioC.setBorder(Rectangle.NO_BORDER);

            PdfPCell servicioV = new PdfPCell(new Phrase(String.valueOf(facturaDto.getPropina() != null ? facturaDto.getPropina() : "0.0"), fontContenido));
            servicioV.setHorizontalAlignment(Element.ALIGN_RIGHT);
            servicioV.setBorder(Rectangle.NO_BORDER);

            tableMetodoDepago.addCell(servicioC);
            tableMetodoDepago.addCell(servicioV);
        }

        PdfPCell tipoCambioC = new PdfPCell(new Paragraph(new Phrase(hmapTagIdioma.get(EnumTagPlantilla.T_TIPOCAMBIO) + " : ", fontEtiquetas)));
        tipoCambioC.setHorizontalAlignment(Element.ALIGN_LEFT);
        tipoCambioC.setBorder(Rectangle.NO_BORDER);

        PdfPCell tipoCambioV = new PdfPCell(new Paragraph(new Phrase(String.valueOf(facturaDto.getTipoCambio() != null ? facturaDto.getTipoCambio() : ""), fontContenido)));
        tipoCambioV.setHorizontalAlignment(Element.ALIGN_LEFT);
        tipoCambioV.setColspan(3);
        tipoCambioV.setBorder(Rectangle.NO_BORDER);

        PdfPCell totalPagoC = new PdfPCell(new Paragraph(new Phrase(hmapTagIdioma.get(EnumTagPlantilla.T_TOTALPAGAR) + " : ", fontEtiquetas)));
        totalPagoC.setHorizontalAlignment(Element.ALIGN_LEFT);
        totalPagoC.setBorder(Rectangle.NO_BORDER);

        PdfPCell totalPagoV = new PdfPCell(new Paragraph(new Phrase(String.valueOf(facturaDto.getTotalPagar() != null ? facturaDto.getTotalPagar() : ""), fontContenido)));
        totalPagoV.setHorizontalAlignment(Element.ALIGN_RIGHT);
        totalPagoV.setBorder(Rectangle.NO_BORDER);

        tableMetodoDepago.addCell(tipoCambioC);
        tableMetodoDepago.addCell(tipoCambioV);
        tableMetodoDepago.addCell(totalPagoC);
        tableMetodoDepago.addCell(totalPagoV);

        document.add(tableMetodoDepago);
    }

    public void agregarComplementoPagos(Document document, CFDI facturaDto) throws DocumentException {
        PdfPTable tableDocumentoRelacionado = new PdfPTable(1);
        tableDocumentoRelacionado.setWidths(new int[]{ANCHO_TOTAL_TABLA});
        tableDocumentoRelacionado.setTotalWidth(ANCHO_TOTAL_TABLA);
        tableDocumentoRelacionado.setLockedWidth(true);
        tableDocumentoRelacionado.setWidthPercentage(PORCENTAJE_ANCHO_TABLA);
        tableDocumentoRelacionado.getDefaultCell().setBorder(Rectangle.NO_BORDER);

        if (facturaDto.getComplementos()!= null && facturaDto.getComplementos().getComplementoPago20() != null) {
            PdfPCell spaceComplementoPagos = new PdfPCell();
            spaceComplementoPagos.setBackgroundColor(BaseColor.BLACK);
            spaceComplementoPagos.setFixedHeight(1.5f);
            spaceComplementoPagos.setBorder(Rectangle.NO_BORDER);

            PdfPCell encabezadoPago = new PdfPCell(new Paragraph("COMPLEMENTO PARA RECEPCIÓN DE PAGOS", fontEtiquetas));
            encabezadoPago.setColspan(1);
            encabezadoPago.setHorizontalAlignment(Element.ALIGN_LEFT);
            encabezadoPago.setBorder(Rectangle.NO_BORDER);
            tableDocumentoRelacionado.addCell(spaceComplementoPagos);
            tableDocumentoRelacionado.addCell(encabezadoPago);



            if (facturaDto.getComplementos().getComplementoPago20().getPago() != null && !facturaDto.getComplementos().getComplementoPago20().getPago().isEmpty()) {
                for (Pago complementoPagoCargados : facturaDto.getComplementos().getComplementoPago20().getPago()) {

                    PdfPCell headerNodoPago = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.C_PAGO).concat(" : "), fontEtiquetas));
                    headerNodoPago.setHorizontalAlignment(Element.ALIGN_LEFT);
                    headerNodoPago.setBorder(Rectangle.NO_BORDER);
                    tableDocumentoRelacionado.addCell(headerNodoPago);

                    PdfPTable tablePago = new PdfPTable(3);
                    tablePago.setWidths(new int[]{50, 50, 50});
                    tablePago.setTotalWidth(480);
                    tablePago.setWidthPercentage(PORCENTAJE_ANCHO_TABLA);
                    tablePago.setLockedWidth(true);
                    tablePago.getDefaultCell().setBorder(Rectangle.NO_BORDER);

                    String fechaPago = "";
                    if (complementoPagoCargados.getFechaPago() != null) {
                        Date dateCheckIn = complementoPagoCargados.getFechaPago();
                        DateFormat dateFormatCheckIn = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                        fechaPago = dateFormatCheckIn.format(dateCheckIn);
                    }

                    PdfPCell fechaPagoC = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.C_FECHAPAGO).concat(" : ") + fechaPago, fontContenido));
                    fechaPagoC.setHorizontalAlignment(Element.ALIGN_LEFT);
                    fechaPagoC.setBorder(Rectangle.NO_BORDER);
                    

                    PdfPCell formaDePago = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.T_FORMAPAGO).concat(" : ").concat(complementoPagoCargados.getFormaDePagoP() != null ? complementoPagoCargados.getFormaDePagoP() : ""), fontContenido));
                    formaDePago.setHorizontalAlignment(Element.ALIGN_LEFT);
                    formaDePago.setBorder(Rectangle.NO_BORDER);

                    PdfPCell monedaPago = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.N_MONEDA).concat(" : ").concat(complementoPagoCargados.getMonedaP() != null ? complementoPagoCargados.getMonedaP() : ""), fontContenido));
                    monedaPago.setHorizontalAlignment(Element.ALIGN_LEFT);
                    monedaPago.setBorder(Rectangle.NO_BORDER);

                    PdfPCell tipoCambioPago = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.C_TIPOCAMBIO).concat(" : ").concat(String.valueOf(complementoPagoCargados.getTipoCambioP() != null && !complementoPagoCargados.getTipoCambioP().equals(BigDecimal.ZERO) ? complementoPagoCargados.getTipoCambioP() : "")), fontContenido));
                    tipoCambioPago.setHorizontalAlignment(Element.ALIGN_LEFT);
                    tipoCambioPago.setBorder(Rectangle.NO_BORDER);

                    PdfPCell montoPago = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.C_MONTO).concat(" : ").concat(String.valueOf(complementoPagoCargados.getMonto() != null && !complementoPagoCargados.getMonto().equals(BigDecimal.ZERO) ? complementoPagoCargados.getMonto() : "")), fontContenido));
                    montoPago.setHorizontalAlignment(Element.ALIGN_LEFT);
                    montoPago.setBorder(Rectangle.NO_BORDER);

                    PdfPCell numeroOperacionPago = new PdfPCell(new Paragraph("Numero Operación: ".concat(complementoPagoCargados.getNumOperacion() != null ? complementoPagoCargados.getNumOperacion() : ""), fontContenido));
                    numeroOperacionPago.setHorizontalAlignment(Element.ALIGN_LEFT);
                    numeroOperacionPago.setBorder(Rectangle.NO_BORDER);

                    PdfPCell rfcEmisorCtaOrdenantePago = new PdfPCell(new Paragraph("RFC Emisor Cuenta Ordenante: ".concat(complementoPagoCargados.getRfcEmisorCtaOrd() != null ? complementoPagoCargados.getRfcEmisorCtaOrd() : ""), fontContenido));
                    rfcEmisorCtaOrdenantePago.setHorizontalAlignment(Element.ALIGN_LEFT);
                    rfcEmisorCtaOrdenantePago.setBorder(Rectangle.NO_BORDER);

                    PdfPCell nombreBancoOrdenantePago = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.C_NOMBREBANCOORDENANTE).concat(" : ").concat(complementoPagoCargados.getNomBancoOrdExt() != null ? complementoPagoCargados.getNomBancoOrdExt() : ""), fontContenido));
                    nombreBancoOrdenantePago.setHorizontalAlignment(Element.ALIGN_LEFT);
                    nombreBancoOrdenantePago.setBorder(Rectangle.NO_BORDER);

                    PdfPCell cuentaOrdenantePago = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.C_CTAORDENANTE).concat(" : ").concat(complementoPagoCargados.getCtaOrdenante() != null ? complementoPagoCargados.getCtaOrdenante() : ""), fontContenido));
                    cuentaOrdenantePago.setHorizontalAlignment(Element.ALIGN_LEFT);
                    cuentaOrdenantePago.setBorder(Rectangle.NO_BORDER);

                    PdfPCell rfcEmisorCtaBeneficiarioPago = new PdfPCell(new Paragraph("RFC Emisor Cuenta Beneficiario: ".concat(complementoPagoCargados.getRfcEmisorCtaBen() != null ? complementoPagoCargados.getRfcEmisorCtaBen() : ""), fontContenido));
                    rfcEmisorCtaBeneficiarioPago.setHorizontalAlignment(Element.ALIGN_LEFT);
                    rfcEmisorCtaBeneficiarioPago.setBorder(Rectangle.NO_BORDER);

                    PdfPCell cuentaBenefiarioPago = new PdfPCell(new Paragraph("Cuenta Beneficiario: ".concat(complementoPagoCargados.getCtaBeneficiario() != null ? complementoPagoCargados.getCtaBeneficiario() : ""), fontContenido));
                    cuentaBenefiarioPago.setHorizontalAlignment(Element.ALIGN_LEFT);
                    cuentaBenefiarioPago.setBorder(Rectangle.NO_BORDER);

                    PdfPCell tipoCaducidadPago = new PdfPCell(new Paragraph("Tipo de Cadena de Pago : ".concat(complementoPagoCargados.getTipoCadPago() != null ? complementoPagoCargados.getTipoCadPago() : ""), fontContenido));
                    tipoCaducidadPago.setHorizontalAlignment(Element.ALIGN_LEFT);
                    tipoCaducidadPago.setBorder(Rectangle.NO_BORDER);

                    PdfPCell certificadoPago = new PdfPCell(new Paragraph("Certificado pago : ".concat(complementoPagoCargados.getCertPago() != null ? complementoPagoCargados.getCertPago() : ""), fontContenido));
                    certificadoPago.setHorizontalAlignment(Element.ALIGN_LEFT);
                    certificadoPago.setBorder(Rectangle.NO_BORDER);

                    PdfPCell caducidadPago = new PdfPCell(new Paragraph("Caducidad pago : ".concat(complementoPagoCargados.getCadPago() != null ? complementoPagoCargados.getCadPago() : ""), fontContenido));
                    caducidadPago.setHorizontalAlignment(Element.ALIGN_LEFT);
                    caducidadPago.setBorder(Rectangle.NO_BORDER);

                    PdfPCell selloPago = new PdfPCell(new Paragraph("Sello pago : ".concat(complementoPagoCargados.getSelloPago() != null ? complementoPagoCargados.getSelloPago() : ""), fontContenido));
                    selloPago.setHorizontalAlignment(Element.ALIGN_LEFT);
                    selloPago.setBorder(Rectangle.NO_BORDER);

                    StringBuilder descImpuestoP = new StringBuilder();
                    if (complementoPagoCargados.getImpuestosP() != null ){

                        descImpuestoP = new StringBuilder();
                        if (complementoPagoCargados.getImpuestosP().getTrasladosP() != null ) {
                            descImpuestoP.append(armarConceptoImpuestoPTraslados(complementoPagoCargados));                        
                        }
                    
                        if (complementoPagoCargados.getImpuestosP().getRetencionesP() != null ) {
                            descImpuestoP.append(armarConceptoImpuestoPRetenciones(complementoPagoCargados));
                        }

                       
                    }

                    PdfPCell impPago = new PdfPCell(new Paragraph( "" + descImpuestoP , fontContenido));
                    impPago.setHorizontalAlignment(Element.ALIGN_LEFT);
                    impPago.setColspan(3);
                    impPago.setBorder(Rectangle.NO_BORDER);
                    



                    tablePago.addCell(fechaPagoC);
                    tablePago.addCell(formaDePago);
                    tablePago.addCell(monedaPago);
                    tablePago.addCell(tipoCambioPago);
                    tablePago.addCell(montoPago);
                    tablePago.addCell(numeroOperacionPago);
                    tablePago.addCell(rfcEmisorCtaOrdenantePago);
                    tablePago.addCell(nombreBancoOrdenantePago);
                    tablePago.addCell(cuentaOrdenantePago);
                    tablePago.addCell(rfcEmisorCtaBeneficiarioPago);
                    tablePago.addCell(cuentaBenefiarioPago);
                    tablePago.addCell(tipoCaducidadPago);
                    tablePago.addCell(certificadoPago);
                    tablePago.addCell(caducidadPago);
                    tablePago.addCell(selloPago);
                    tablePago.addCell(impPago);

                  
       
                    
                    tableDocumentoRelacionado.addCell(tablePago);

                    if (complementoPagoCargados.getDocumentoRelacionado() != null && !complementoPagoCargados.getDocumentoRelacionado().isEmpty()) {
                        for (DoctoRelacionado documentosRelacionCargados : complementoPagoCargados.getDocumentoRelacionado()) {

                            PdfPCell encabezadoDocRelacionado = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.C_DTOSRELACIONADOS), fontEtiquetas));
                            encabezadoDocRelacionado.setHorizontalAlignment(Element.ALIGN_LEFT);
                            encabezadoDocRelacionado.setBorder(Rectangle.NO_BORDER);
                            tableDocumentoRelacionado.addCell(encabezadoDocRelacionado);

                            PdfPTable tableDocumentoRelacionadoPago = new PdfPTable(3);
                            tableDocumentoRelacionadoPago.setWidths(new int[]{50, 50, 50});
                            tableDocumentoRelacionadoPago.setTotalWidth(480);
                            tableDocumentoRelacionadoPago.setWidthPercentage(PORCENTAJE_ANCHO_TABLA);
                            tableDocumentoRelacionadoPago.setLockedWidth(true);
                            tableDocumentoRelacionadoPago.getDefaultCell().setBorder(Rectangle.NO_BORDER);

                            PdfPCell idDocumentoRelacionadoC = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.C_FOLIOUUID).concat(" : ").concat(documentosRelacionCargados.getIdDocumento() != null ? documentosRelacionCargados.getIdDocumento() : ""), fontContenido));
                            idDocumentoRelacionadoC.setHorizontalAlignment(Element.ALIGN_LEFT);
                            idDocumentoRelacionadoC.setBorder(Rectangle.NO_BORDER);

                            PdfPCell serieDocumentoRelacionadoC = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.SERIE).concat(" : ").concat(documentosRelacionCargados.getSerie() != null ? documentosRelacionCargados.getSerie() : ""), fontContenido));
                            serieDocumentoRelacionadoC.setHorizontalAlignment(Element.ALIGN_LEFT);
                            serieDocumentoRelacionadoC.setBorder(Rectangle.NO_BORDER);

                            PdfPCell folioDocumentoRelacionadoC = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.C_FOLIO).concat(" : ").concat(documentosRelacionCargados.getFolio() != null ? documentosRelacionCargados.getFolio() : ""), fontContenido));
                            folioDocumentoRelacionadoC.setHorizontalAlignment(Element.ALIGN_LEFT);
                            folioDocumentoRelacionadoC.setBorder(Rectangle.NO_BORDER);

                            PdfPCell monedaDocumentoRelacionadoC = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.N_MONEDA).concat(" : ").concat(documentosRelacionCargados.getMonedaDR() != null ? documentosRelacionCargados.getMonedaDR() : ""), fontContenido));
                            monedaDocumentoRelacionadoC.setHorizontalAlignment(Element.ALIGN_LEFT);
                            monedaDocumentoRelacionadoC.setBorder(Rectangle.NO_BORDER);

                            PdfPCell tipoDeCambioDocumentoRelacionadoC = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.EQUIVALENCIA_DR).concat(" : ").concat(String.valueOf(documentosRelacionCargados.getEquivalenciaDR() != null ? documentosRelacionCargados.getEquivalenciaDR()  : "")), fontContenido));
                            tipoDeCambioDocumentoRelacionadoC.setHorizontalAlignment(Element.ALIGN_LEFT);
                            tipoDeCambioDocumentoRelacionadoC.setBorder(Rectangle.NO_BORDER);

                            // PdfPCell metodoPagoDocumentoRelacionadoC = new PdfPCell(new Paragraph("Método de Pago : ".concat(documentosRelacionCargados.getMetodoDePagoDR() != null ? documentosRelacionCargados.getMetodoDePagoDR() : ""), fontContenido));
                            // metodoPagoDocumentoRelacionadoC.setHorizontalAlignment(Element.ALIGN_LEFT);
                            // metodoPagoDocumentoRelacionadoC.setBorder(Rectangle.NO_BORDER);

                            PdfPCell numeroParcialidaDocumentoRelacionadoC = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.C_NUMEROPARCIALIDADES).concat(" : ").concat(String.valueOf(documentosRelacionCargados.getNumParcialidad() != null ? documentosRelacionCargados.getNumParcialidad() : "")), fontContenido));
                            numeroParcialidaDocumentoRelacionadoC.setHorizontalAlignment(Element.ALIGN_LEFT);
                            numeroParcialidaDocumentoRelacionadoC.setBorder(Rectangle.NO_BORDER);

                            PdfPCell importeSaldoAntDocumentoRelacionadoC = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.C_IMPORTESALDOANT).concat(" : ").concat(String.valueOf(documentosRelacionCargados.getImpSaldoAnt() != null ? documentosRelacionCargados.getImpSaldoAnt() : "")), fontContenido));
                            importeSaldoAntDocumentoRelacionadoC.setHorizontalAlignment(Element.ALIGN_LEFT);
                            importeSaldoAntDocumentoRelacionadoC.setBorder(Rectangle.NO_BORDER);

                            PdfPCell importeSaldoPagadoDocumentoRelacionadoC = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.C_IMPORTEPAGADO).concat(" : ").concat(String.valueOf(documentosRelacionCargados.getImpPagado() != null ? documentosRelacionCargados.getImpPagado() : "")), fontContenido));
                            importeSaldoPagadoDocumentoRelacionadoC.setHorizontalAlignment(Element.ALIGN_LEFT);
                            importeSaldoPagadoDocumentoRelacionadoC.setBorder(Rectangle.NO_BORDER);

                            PdfPCell importeSaldoInsoDocumentoRelacionadoC = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.C_IMPORTESALDOINS).concat(" : ").concat(String.valueOf(documentosRelacionCargados.getImpSaldoInsoluto() != null ? documentosRelacionCargados.getImpSaldoInsoluto() : "")), fontContenido));
                            importeSaldoInsoDocumentoRelacionadoC.setHorizontalAlignment(Element.ALIGN_LEFT);
                            importeSaldoInsoDocumentoRelacionadoC.setColspan(3);
                            importeSaldoInsoDocumentoRelacionadoC.setBorder(Rectangle.NO_BORDER);     


                            PdfPCell importeSaldoInsoDocumentoRelacionadoCC = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.C_IMPORTESALDOINS).concat(" :  ").concat(String.valueOf(documentosRelacionCargados.getImpSaldoInsoluto() != null ? documentosRelacionCargados.getImpSaldoInsoluto() : "")), fontContenido));
                            importeSaldoInsoDocumentoRelacionadoCC.setHorizontalAlignment(Element.ALIGN_LEFT);
                            importeSaldoInsoDocumentoRelacionadoCC.setColspan(3);
                            importeSaldoInsoDocumentoRelacionadoCC.setBorder(Rectangle.NO_BORDER);                                   

                            tableDocumentoRelacionadoPago.addCell(idDocumentoRelacionadoC);
                            tableDocumentoRelacionadoPago.addCell(serieDocumentoRelacionadoC);
                            tableDocumentoRelacionadoPago.addCell(folioDocumentoRelacionadoC);
                            tableDocumentoRelacionadoPago.addCell(monedaDocumentoRelacionadoC);
                            tableDocumentoRelacionadoPago.addCell(tipoDeCambioDocumentoRelacionadoC);
                            // tableDocumentoRelacionadoPago.addCell(metodoPagoDocumentoRelacionadoC);
                            tableDocumentoRelacionadoPago.addCell(numeroParcialidaDocumentoRelacionadoC);
                            tableDocumentoRelacionadoPago.addCell(importeSaldoAntDocumentoRelacionadoC);
                            tableDocumentoRelacionadoPago.addCell(importeSaldoPagadoDocumentoRelacionadoC);
                            tableDocumentoRelacionadoPago.addCell(importeSaldoInsoDocumentoRelacionadoC);
                           
                            StringBuilder descImpuestoDR = new StringBuilder();
                            if (documentosRelacionCargados.getImpuestosDR() != null ){
                               
                                
                                descImpuestoDR = new StringBuilder();
                                if (documentosRelacionCargados.getImpuestosDR().getTrasladosDR() != null ) {
                                    descImpuestoDR.append(armarConceptoImpuestoDRTraslados(documentosRelacionCargados));                        
                                }
                            
                                if (documentosRelacionCargados.getImpuestosDR().getRetencionesDR() != null ) {
                                    descImpuestoDR.append(armarConceptoImpuestoDRRetenciones(documentosRelacionCargados));
                                }
                               
                            }
                            PdfPCell impPagoDR = new PdfPCell(new Paragraph( "" + descImpuestoDR , fontContenido));
                            impPagoDR.setHorizontalAlignment(Element.ALIGN_LEFT);
                            impPagoDR.setColspan(3);
                            impPagoDR.setBorder(Rectangle.NO_BORDER);
                            tableDocumentoRelacionadoPago.addCell(impPagoDR);

                            tableDocumentoRelacionado.addCell(tableDocumentoRelacionadoPago);
                        }
                    }
                }
            }
            if (facturaDto.getComplementos().getComplementoPago20().getTotales() != null) {
                PdfPCell headerNodoTotales = new PdfPCell(new Paragraph("Totales Pagos ", fontEtiquetas));
                headerNodoTotales.setHorizontalAlignment(Element.ALIGN_RIGHT);
                headerNodoTotales.setBorder(Rectangle.NO_BORDER);

                tableDocumentoRelacionado.addCell(headerNodoTotales);

                PdfPTable tableTotales = new PdfPTable(3);
                tableTotales.setWidths(new int[]{140, 100, 50});
                tableTotales.setTotalWidth(ANCHO_TOTAL_TABLA);
                tableTotales.setWidthPercentage(PORCENTAJE_ANCHO_TABLA);
                tableTotales.setLockedWidth(true);
                tableTotales.getDefaultCell().setFixedHeight(80f);
                tableTotales.getDefaultCell().setBorder(Rectangle.NO_BORDER);


                if (!Optional.ofNullable( facturaDto.getComplementos().getComplementoPago20().getTotales().getTotalRetencionesIVA()).isEmpty()) {
                    PdfPCell totalRetencionesIVA = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.TOTAL_RETENCIONES_IVA).concat(" : ").concat( 
                        facturaDto.getComplementos().getComplementoPago20().getTotales().getTotalRetencionesIVA() != null
                        ? String.valueOf(facturaDto.getComplementos().getComplementoPago20().getTotales().getTotalRetencionesIVA())
                        : ""), fontContenido
                    ));
                    totalRetencionesIVA.setColspan(3);
                    totalRetencionesIVA.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    totalRetencionesIVA.setBorder(Rectangle.NO_BORDER);
                    tableTotales.addCell(totalRetencionesIVA);
                }

                if (!Optional.ofNullable( facturaDto.getComplementos().getComplementoPago20().getTotales().getTotalRetencionesISR()).isEmpty()) {
                        PdfPCell totalRetencionesISR = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.TOTAL_RETENCIONES_ISR).concat(" : ").concat(
                            facturaDto.getComplementos().getComplementoPago20().getTotales().getTotalRetencionesISR() != null
                            ? String.valueOf(facturaDto.getComplementos().getComplementoPago20().getTotales().getTotalRetencionesISR())
                            : ""), fontContenido
                        )
                    );
                    totalRetencionesISR.setColspan(3);
                    totalRetencionesISR.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    totalRetencionesISR.setBorder(Rectangle.NO_BORDER);
                    tableTotales.addCell(totalRetencionesISR);
                }
               
                if (!Optional.ofNullable( facturaDto.getComplementos().getComplementoPago20().getTotales().getTotalRetencionesIEPS()).isEmpty()) {
                    PdfPCell totalRetencionesIEPS = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.TOTAL_RETENCIONES_IEPS).concat(" : ").concat(
                        facturaDto.getComplementos().getComplementoPago20().getTotales().getTotalRetencionesIEPS() != null
                        ? String.valueOf(facturaDto.getComplementos().getComplementoPago20().getTotales().getTotalRetencionesIEPS())
                        : ""), fontContenido
                        )
                    );
                    totalRetencionesIEPS.setColspan(3);
                    totalRetencionesIEPS.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    totalRetencionesIEPS.setBorder(Rectangle.NO_BORDER);
                    tableTotales.addCell(totalRetencionesIEPS);
                }


                if (!Optional.ofNullable( facturaDto.getComplementos().getComplementoPago20().getTotales().getTotalTrasladosBaseIVA16()).isEmpty()) {
                    PdfPCell totalTrasladosBaseIVA16 = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.TOTAL_TRASLADOS_BASE_IVA_16).concat(" : ").concat(
                        facturaDto.getComplementos().getComplementoPago20().getTotales().getTotalTrasladosBaseIVA16() != null
                        ? String.valueOf(facturaDto.getComplementos().getComplementoPago20().getTotales().getTotalTrasladosBaseIVA16())
                        : ""), fontContenido
                        )
                    );
                    totalTrasladosBaseIVA16.setColspan(3);
                    totalTrasladosBaseIVA16.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    totalTrasladosBaseIVA16.setBorder(Rectangle.NO_BORDER);
                    tableTotales.addCell(totalTrasladosBaseIVA16);

                }

              

                if (!Optional.ofNullable( facturaDto.getComplementos().getComplementoPago20().getTotales().getTotalTrasladosImpuestoIVA16()).isEmpty()) {
                    PdfPCell totalTrasladosImpuestoIVA16 = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.TOTAL_TRASLADOS_IMPUESTO_IVA_16).concat(" : ").concat(
                        facturaDto.getComplementos().getComplementoPago20().getTotales().getTotalTrasladosImpuestoIVA16() != null
                        ? String.valueOf(facturaDto.getComplementos().getComplementoPago20().getTotales().getTotalTrasladosImpuestoIVA16())
                        : ""), fontContenido
                    )
                );
                totalTrasladosImpuestoIVA16.setColspan(3);
                totalTrasladosImpuestoIVA16.setHorizontalAlignment(Element.ALIGN_RIGHT);
                totalTrasladosImpuestoIVA16.setBorder(Rectangle.NO_BORDER);
                tableTotales.addCell(totalTrasladosImpuestoIVA16);

                }
              

                if (!Optional.ofNullable( facturaDto.getComplementos().getComplementoPago20().getTotales().getTotalTrasladosBaseIVA8()).isEmpty()) {
                    PdfPCell totalTrasladosBaseIVA8 = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.TOTAL_TRASLADOS_BASE_IVA8).concat(" : ").concat(
                        facturaDto.getComplementos().getComplementoPago20().getTotales().getTotalTrasladosBaseIVA8() != null
                        ? String.valueOf(facturaDto.getComplementos().getComplementoPago20().getTotales().getTotalTrasladosBaseIVA8())
                        : ""), fontContenido
                    )
                );
                totalTrasladosBaseIVA8.setColspan(3);
                totalTrasladosBaseIVA8.setHorizontalAlignment(Element.ALIGN_RIGHT);
                totalTrasladosBaseIVA8.setBorder(Rectangle.NO_BORDER);
                tableTotales.addCell(totalTrasladosBaseIVA8);


                }
               

                if (!Optional.ofNullable( facturaDto.getComplementos().getComplementoPago20().getTotales().getTotalTrasladosImpuestoIVA8()).isEmpty()) {
                    PdfPCell totalTrasladosImpuestoIVA8 = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.TOTAL_TRASLADOS_IMPUESTO_IVA_8).concat(" : ").concat(
                        facturaDto.getComplementos().getComplementoPago20().getTotales().getTotalTrasladosImpuestoIVA8() != null
                        ? String.valueOf(facturaDto.getComplementos().getComplementoPago20().getTotales().getTotalTrasladosImpuestoIVA8())
                        : ""), fontContenido
                    )
                    );
                    totalTrasladosImpuestoIVA8.setColspan(3);
                    totalTrasladosImpuestoIVA8.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    totalTrasladosImpuestoIVA8.setBorder(Rectangle.NO_BORDER);
                    tableTotales.addCell(totalTrasladosImpuestoIVA8);

                    }
               

                if (!Optional.ofNullable( facturaDto.getComplementos().getComplementoPago20().getTotales().getTotalTrasladosBaseIVA0()).isEmpty()) {
                    PdfPCell totalTrasladosBaseIVA0 = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.TOTAL_TRASLADOS_BASE_IVA_0).concat(" : ").concat(
                        facturaDto.getComplementos().getComplementoPago20().getTotales().getTotalTrasladosBaseIVA0() != null
                        ? String.valueOf(facturaDto.getComplementos().getComplementoPago20().getTotales().getTotalTrasladosBaseIVA0())
                        : ""), fontContenido
                    )
                );
                totalTrasladosBaseIVA0.setColspan(3);
                totalTrasladosBaseIVA0.setHorizontalAlignment(Element.ALIGN_RIGHT);
                totalTrasladosBaseIVA0.setBorder(Rectangle.NO_BORDER);
                tableTotales.addCell(totalTrasladosBaseIVA0);

                }
               

                if (!Optional.ofNullable( facturaDto.getComplementos().getComplementoPago20().getTotales().getTotalTrasladosImpuestoIVA0()).isEmpty()) {
                    PdfPCell totalTrasladosImpuestoIVA0 = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.TOTAL_TRASLADOS_IMPUESTO_IVA_0).concat(" : ").concat(
                        facturaDto.getComplementos().getComplementoPago20().getTotales().getTotalTrasladosImpuestoIVA0() != null
                        ? String.valueOf(facturaDto.getComplementos().getComplementoPago20().getTotales().getTotalTrasladosImpuestoIVA0())
                        : ""), fontContenido
                    )
                );
                totalTrasladosImpuestoIVA0.setColspan(3);
                totalTrasladosImpuestoIVA0.setHorizontalAlignment(Element.ALIGN_RIGHT);
                totalTrasladosImpuestoIVA0.setBorder(Rectangle.NO_BORDER);
                tableTotales.addCell(totalTrasladosImpuestoIVA0);
                }
               


                if (!Optional.ofNullable( facturaDto.getComplementos().getComplementoPago20().getTotales().getTotalTrasladosBaseIVAExento()).isEmpty()) {
                    PdfPCell totalTrasladosBaseIVAExento = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.TOTAL_TRASLADOS_BASE_IVA_EXENTO).concat(" : ").concat(
                        facturaDto.getComplementos().getComplementoPago20().getTotales().getTotalTrasladosBaseIVAExento() != null
                        ? String.valueOf(facturaDto.getComplementos().getComplementoPago20().getTotales().getTotalTrasladosBaseIVAExento())
                        : ""), fontContenido
                    )
                );
                totalTrasladosBaseIVAExento.setColspan(3);
                totalTrasladosBaseIVAExento.setHorizontalAlignment(Element.ALIGN_RIGHT);
                totalTrasladosBaseIVAExento.setBorder(Rectangle.NO_BORDER);
           
                tableTotales.addCell(totalTrasladosBaseIVAExento);
                }
              

                if (!Optional.ofNullable( facturaDto.getComplementos().getComplementoPago20().getTotales().getTotalRetencionesIVA()).isEmpty()) {
                    PdfPCell montoTotalPagos = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.MONTO_TOTAL_PAGOS).concat(" : ").concat(
                        facturaDto.getComplementos().getComplementoPago20().getTotales().getMontoTotalPagos() != null
                        ? String.valueOf(facturaDto.getComplementos().getComplementoPago20().getTotales().getMontoTotalPagos())
                        : ""), fontContenido
                    )
                );
                montoTotalPagos.setColspan(3);
                montoTotalPagos.setHorizontalAlignment(Element.ALIGN_RIGHT);
                montoTotalPagos.setBorder(Rectangle.NO_BORDER);
                tableTotales.addCell(montoTotalPagos);
                }
              

                tableDocumentoRelacionado.addCell(tableTotales);
            }
        }
        document.add(tableDocumentoRelacionado);
    }

    /**
     * Metodo para agregar los sellos, sello sat, cadena original, etc * Todos
     * los formatos.
     *
     * @param document
     * @throws DocumentException
     */
    private void agregarTableSellos(Document document, CFDI facturaDto) throws DocumentException {
        PdfPTable tableFinalDoc = new PdfPTable(2);
        tableFinalDoc.setWidths(new int[]{440, 120});
        tableFinalDoc.setTotalWidth(ANCHO_TOTAL_TABLA);
        tableFinalDoc.setLockedWidth(true);
        tableFinalDoc.setWidthPercentage(PORCENTAJE_ANCHO_TABLA);
//        tableFinalDoc.getDefaultCell().setFixedHeight(115f);
        tableFinalDoc.getDefaultCell().setBorder(Rectangle.NO_BORDER);

        PdfPTable tableSellos = new PdfPTable(1);
        tableSellos.setWidths(new int[]{380});
        tableSellos.setTotalWidth(380);
        tableSellos.getDefaultCell().setBorder(Rectangle.NO_BORDER);

        PdfPCell selloCFDITitleC = new PdfPCell(new Paragraph(new Phrase(hmapTagIdioma.get(EnumTagPlantilla.S_SDCFDI), fontEtiquetas)));
        selloCFDITitleC.setHorizontalAlignment(Element.ALIGN_LEFT);
        selloCFDITitleC.setBackgroundColor(BaseColor.LIGHT_GRAY);
        selloCFDITitleC.setBorder(Rectangle.NO_BORDER);

        PdfPCell selloCFDITitleV = new PdfPCell(new Paragraph(new Phrase(facturaDto.getTimbrado().getSelloCFDI(), FontFactory.getFont(FontFactory.HELVETICA, 6))));
        selloCFDITitleV.setHorizontalAlignment(Element.ALIGN_LEFT);
        selloCFDITitleV.setBorder(Rectangle.NO_BORDER);

        PdfPCell selloSatTitleC = new PdfPCell(new Paragraph(new Phrase(hmapTagIdioma.get(EnumTagPlantilla.S_SD), fontEtiquetas)));
        selloSatTitleC.setHorizontalAlignment(Element.ALIGN_LEFT);
        selloSatTitleC.setBackgroundColor(BaseColor.LIGHT_GRAY);
        selloSatTitleC.setBorder(Rectangle.NO_BORDER);

        PdfPCell cellSelloSat = new PdfPCell(new Paragraph(new Phrase(facturaDto.getTimbrado().getSelloSAT(), FontFactory.getFont(FontFactory.HELVETICA, 6))));
        cellSelloSat.setHorizontalAlignment(Element.ALIGN_LEFT);
        cellSelloSat.setBorder(Rectangle.NO_BORDER);

        PdfPCell cellCadenaCCDSATTitle = new PdfPCell(new Paragraph(new Phrase(hmapTagIdioma.get(EnumTagPlantilla.S_CADENACDSAT), fontEtiquetas)));
        cellCadenaCCDSATTitle.setHorizontalAlignment(Element.ALIGN_LEFT);
        cellCadenaCCDSATTitle.setBackgroundColor(BaseColor.LIGHT_GRAY);
        cellCadenaCCDSATTitle.setBorder(Rectangle.NO_BORDER);

        PdfPCell cellCadenaCCDSAT = new PdfPCell(new Paragraph(new Phrase(facturaDto.getTimbrado().getCadenaDatosTimbrado(), FontFactory.getFont(FontFactory.HELVETICA, 6))));
        cellCadenaCCDSAT.setHorizontalAlignment(Element.ALIGN_LEFT);
        cellCadenaCCDSAT.setBorder(Rectangle.NO_BORDER);

        tableSellos.addCell(selloCFDITitleC);
        tableSellos.addCell(selloCFDITitleV);
        tableSellos.addCell(selloSatTitleC);
        tableSellos.addCell(cellSelloSat);
        tableSellos.addCell(cellCadenaCCDSATTitle);
        tableSellos.addCell(cellCadenaCCDSAT);

        tableFinalDoc.addCell(tableSellos);

        PdfPTable tableQRCode = new PdfPTable(1);
        tableQRCode.setWidths(new int[]{120});
        tableQRCode.setTotalWidth(120);
        tableQRCode.getDefaultCell().setBorder(Rectangle.NO_BORDER);
        tableQRCode.getDefaultCell().setFixedHeight(110f);

        try {
            String dato = "https://verificacfdi.facturaelectronica.sat.gob.mx/default.aspx?" 
                    + "id=" + facturaDto.getTimbrado().getUuid()
                    + "&re=" + facturaDto.getEmisor().getRfc() 
                    + "&rr=" + facturaDto.getReceptor().getRfc()
                    + "&tt=" + facturaDto.getTotal() 
                    + "&fe=" + facturaDto.getTimbrado().getSelloCFDI()
                            .substring(facturaDto.getTimbrado().getSelloCFDI().length() - 8);
            BarcodeQRCode barcodeQRCode = new BarcodeQRCode(dato, 600, 600, null);
            Image codeQrImage = barcodeQRCode.getImage();
            codeQrImage.scaleAbsolute(30, 30);
            tableQRCode.addCell(codeQrImage);
        } catch (Exception ex) {
            tableQRCode.addCell("");
            System.out.println("Error al generar el codigo QR " + ex.getMessage());
        }
        tableFinalDoc.addCell(tableQRCode);
        document.add(tableFinalDoc);
    }

    /**
     * Metodo para agregar las leyendas al final del PDF * Todos los formatos.
     *
     * @param document
     * @throws DocumentException
     */
    private void agregarTableLeyendas(Document document, CFDI facturaDto) throws DocumentException {
        PdfPTable tableLeyenda = new PdfPTable(2);
        tableLeyenda.setWidths(new int[]{235, 220});
        tableLeyenda.setTotalWidth(ANCHO_TOTAL_TABLA);
        tableLeyenda.setLockedWidth(true);
        tableLeyenda.setWidthPercentage(PORCENTAJE_ANCHO_TABLA);
        tableLeyenda.getDefaultCell().setFixedHeight(80f);
        tableLeyenda.getDefaultCell().setBorder(Rectangle.NO_BORDER);

        PdfPCell leyendaC = new PdfPCell(new Phrase(hmapTagIdioma.get(EnumTagPlantilla.MENSAJE3), fontLeyendas));
        leyendaC.setHorizontalAlignment(Element.ALIGN_LEFT);
        leyendaC.setBorderWidthRight(2f);
        leyendaC.setBorderWidthBottom(0f);
        leyendaC.setBorderWidthLeft(0f);
        leyendaC.setBorderWidthTop(0f);

        PdfPCell leyenda2C = new PdfPCell(new Phrase(hmapTagIdioma.get(EnumTagPlantilla.MENSAJE4), fontLeyendas));
        leyenda2C.setHorizontalAlignment(Element.ALIGN_LEFT);
        leyenda2C.setBorderWidthRight(2f);
        leyenda2C.setBorderWidthBottom(0f);
        leyenda2C.setBorderWidthLeft(0f);
        leyenda2C.setBorderWidthTop(0f);

        PdfPCell leyenda3C = new PdfPCell(new Phrase(hmapTagIdioma.get(EnumTagPlantilla.MENSAJE5), fontLeyendas));
        leyenda3C.setHorizontalAlignment(Element.ALIGN_LEFT);
        leyenda3C.setBorderWidthRight(2f);
        leyenda3C.setBorderWidthBottom(0f);
        leyenda3C.setBorderWidthLeft(0f);
        leyenda3C.setBorderWidthTop(0f);

        PdfPCell leyendaDocumentoC = new PdfPCell(new Phrase(hmapTagIdioma.get(EnumTagPlantilla.MENSAJE2).toUpperCase(), fontEtiquetas));
        leyendaDocumentoC.setHorizontalAlignment(Element.ALIGN_LEFT);
        leyendaDocumentoC.setBorderWidthRight(0f);
        leyendaDocumentoC.setBorderWidthBottom(0f);
        leyendaDocumentoC.setBorderWidthLeft(0f);
        leyendaDocumentoC.setBorderWidthTop(0f);

        PdfPCell leyendaDocumento2C = new PdfPCell(new Phrase(hmapTagIdioma.get(EnumTagPlantilla.MENSAJE6).concat(facturaDto.getEmisor().getNombre() != null ? facturaDto.getEmisor().getNombre() : ""), fontLeyendas));
        leyendaDocumento2C.setHorizontalAlignment(Element.ALIGN_LEFT);
        leyendaDocumento2C.setBorderWidthRight(0f);
        leyendaDocumento2C.setBorderWidthBottom(0f);
        leyendaDocumento2C.setBorderWidthLeft(0f);
        leyendaDocumento2C.setBorderWidthTop(0f);

        PdfPCell leyendaDocumento3C = new PdfPCell(new Phrase(hmapTagIdioma.get(EnumTagPlantilla.MENSAJE7).concat(facturaDto.getEmisor().getNombre() != null ? facturaDto.getEmisor().getNombre() : ""), fontLeyendas));
        leyendaDocumento3C.setHorizontalAlignment(Element.ALIGN_LEFT);
        leyendaDocumento3C.setBorderWidthRight(0f);
        leyendaDocumento3C.setBorderWidthBottom(0f);
        leyendaDocumento3C.setBorderWidthLeft(0f);
        leyendaDocumento3C.setBorderWidthTop(0f);

        tableLeyenda.addCell(leyendaC);
        tableLeyenda.addCell(leyendaDocumentoC);
        tableLeyenda.addCell(leyenda2C);
        tableLeyenda.addCell(leyendaDocumento2C);
        tableLeyenda.addCell(leyenda3C);
        tableLeyenda.addCell(leyendaDocumento3C);

        document.add(tableLeyenda);
    }

  
    private void agregarTableInformacionGlobal (Document document, CFDI facturaDto) throws  DocumentException {
        PdfPTable tableInformacionGlobalH = new PdfPTable(1);
        tableInformacionGlobalH.setWidths(new int[]{ 560});
        tableInformacionGlobalH.setTotalWidth(ANCHO_TOTAL_TABLA);
        tableInformacionGlobalH.setLockedWidth(true);
        tableInformacionGlobalH.setWidthPercentage(PORCENTAJE_ANCHO_TABLA);
        tableInformacionGlobalH.getDefaultCell().setBorder(Rectangle.NO_BORDER);


        if (facturaDto.getInformacionGlobal()!= null) {

            PdfPCell space = new PdfPCell();     
            space.setBackgroundColor(BaseColor.LIGHT_GRAY);
            space.setFixedHeight(5f);
            space.setBorder(Rectangle.NO_BORDER);
            
            PdfPCell encabezado = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.INFORMACION_GLOBAL), fontEtiquetas));
            encabezado.setHorizontalAlignment(Element.ALIGN_LEFT);
            encabezado.setBorder(Rectangle.NO_BORDER);
            tableInformacionGlobalH.addCell(space);
            tableInformacionGlobalH.addCell(encabezado);

            PdfPTable tableInformacionGlobal = new PdfPTable(3);
            tableInformacionGlobal.setWidths(new int[]{50, 50, 50});
            tableInformacionGlobal.setTotalWidth(480);
            tableInformacionGlobal.setLockedWidth(true);
            tableInformacionGlobal.setWidthPercentage(PORCENTAJE_ANCHO_TABLA);
            tableInformacionGlobal.getDefaultCell().setBorder(Rectangle.NO_BORDER);

            PdfPCell peridodicidad = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.PERIODICIDAD).concat(" : ").concat(facturaDto.getInformacionGlobal().getPeriodicidad() != null ? facturaDto.getInformacionGlobal().getPeriodicidad() : ""), fontContenido));
            peridodicidad.setHorizontalAlignment(Element.ALIGN_LEFT);
            peridodicidad.setBorder(Rectangle.NO_BORDER);

            PdfPCell meses = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.MESES).concat(" : ").concat(facturaDto.getInformacionGlobal().getMeses() != null ? facturaDto.getInformacionGlobal().getMeses() : ""), fontContenido));
            meses.setHorizontalAlignment(Element.ALIGN_LEFT);
            meses.setBorder(Rectangle.NO_BORDER);

            PdfPCell anio = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.ANIO).concat(" : ").concat( String.valueOf(facturaDto.getInformacionGlobal().getAnio())  != null  ? String.valueOf(facturaDto.getInformacionGlobal().getAnio()) : ""), fontContenido));
            anio.setHorizontalAlignment(Element.ALIGN_LEFT);
            anio.setBorder(Rectangle.NO_BORDER);

            tableInformacionGlobal.addCell(peridodicidad);
            tableInformacionGlobal.addCell(meses);
            tableInformacionGlobal.addCell(anio);
            tableInformacionGlobalH.addCell(tableInformacionGlobal);

        }
      

        document.add(tableInformacionGlobalH);
    }


    private void agregarTableDatosCfdiRelacionados(Document document, CFDI facturaDto) throws DocumentException {
        if (facturaDto.getCfdiRelacionados() != null) {
            PdfPTable tableDatosH = new PdfPTable(1);
            tableDatosH.setWidths(new int[]{ANCHO_TOTAL_TABLA});
            tableDatosH.setTotalWidth(ANCHO_TOTAL_TABLA);
            tableDatosH.setLockedWidth(true);
            tableDatosH.setWidthPercentage(PORCENTAJE_ANCHO_TABLA);
            tableDatosH.getDefaultCell().setBorder(Rectangle.NO_BORDER);

            PdfPTable tableCfdiRelacionados = new PdfPTable(2);
            tableCfdiRelacionados.setWidths(new int[]{400, 200});
            tableCfdiRelacionados.setTotalWidth(ANCHO_TOTAL_TABLA);
            tableCfdiRelacionados.setLockedWidth(true);
            tableCfdiRelacionados.setWidthPercentage(PORCENTAJE_ANCHO_TABLA);
            tableCfdiRelacionados.getDefaultCell().setBorder(Rectangle.NO_BORDER);

            for (Relacionados cfdiRelacionadosCargados : facturaDto.getCfdiRelacionados()) {

                StringBuilder cfdiRelacionado = new StringBuilder();
                if (facturaDto.getCfdiRelacionados() != null) {
                    cfdiRelacionado.append(cfdiRelacionadosCargados.getUuid()).append(" ");
                }

                PdfPCell uuidRelacionado = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.UUID_RELACIONADO).concat(" : ") + cfdiRelacionado, fontContenido));
                uuidRelacionado.setHorizontalAlignment(Element.ALIGN_LEFT);
                uuidRelacionado.setBorder(Rectangle.NO_BORDER);


                PdfPCell tipoRelacion = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.TIPO_RELACION).concat(" : ").concat(cfdiRelacionadosCargados.getTipoRelacion() != null ? cfdiRelacionadosCargados.getTipoRelacion() : ""), fontContenido));
                tipoRelacion.setHorizontalAlignment(Element.ALIGN_LEFT);
                tipoRelacion.setBorder(Rectangle.NO_BORDER);

                tableCfdiRelacionados.addCell(uuidRelacionado);
                tableCfdiRelacionados.addCell(tipoRelacion);
            }

            PdfPCell datosClienteC = new PdfPCell(new Phrase(hmapTagIdioma.get(EnumTagPlantilla.CFDI_RELACIONADOS), fontEtiquetas));
            datosClienteC.setColspan(2);
            datosClienteC.setBackgroundColor(BaseColor.LIGHT_GRAY);
            datosClienteC.setHorizontalAlignment(Element.ALIGN_LEFT);
            datosClienteC.setBorder(Rectangle.NO_BORDER);

            tableDatosH.addCell(datosClienteC);
            tableDatosH.addCell(tableCfdiRelacionados);

            document.add(tableDatosH);
        }
    }


    private StringBuilder armarConceptoCuentaTerceros (Conceptos conceptos) {
        StringBuilder descConcepto = new StringBuilder();
        try {
            descConcepto.append("\n");
            descConcepto.append(hmapTagIdioma.get(EnumTagPlantilla.NOMBRE_A_CUENTA_TERCEROS).concat(" : ").concat(conceptos.getACuentaTerceros().getNombreACuentaTerceros()));
            descConcepto.append("\n");
            descConcepto.append(hmapTagIdioma.get(EnumTagPlantilla.RFC_A_CUENTA_TERCEROS).concat(" : ").concat(conceptos.getACuentaTerceros().getRfcACuentaTerceros()));
            descConcepto.append("\n");
            descConcepto.append(hmapTagIdioma.get(EnumTagPlantilla.DOMICILIO_A_CUENTA_TERCEROS).concat(" : ").concat(conceptos.getACuentaTerceros().getDomicilioFiscalACuentaTerceros()));
            return descConcepto;
        } catch (Exception e) {
            System.out.println("Ocurrio un error al tratar de armar el concepto con cuenta de terceros" + e.getMessage());
            return new StringBuilder("");
        }
    }

    private StringBuilder armarConceptoImpuestoPTraslados(ComplementoPago20.Pago  conceptos) {
        StringBuilder descripcion = new StringBuilder();
        try {
            for (Pago.ImpuestosP.TrasladosP.TrasladoP impuestosCargados : conceptos.getImpuestosP().getTrasladosP().getTrasladoP() ) {
                descripcion.append("\n");
                CatImpuestos obtenerCatImpuestosSatByClave = catalogosSatService.obtenerCatImpuestoSatByClave(impuestosCargados.getImpuestoP());
                descripcion.append(hmapTagIdioma.get(EnumTagPlantilla.IMP_TRASLADO).concat(" : ").concat(obtenerCatImpuestosSatByClave.getDescripcion() != null ? obtenerCatImpuestosSatByClave.getDescripcion().concat("     ") : " "))
                            .append(hmapTagIdioma.get(EnumTagPlantilla.TASA_O_CUOTA).concat(" : ").concat(String.valueOf(impuestosCargados.getTasaOCuotaP()) != null ? String.valueOf(impuestosCargados.getTasaOCuotaP()).concat("    ")  : ""))
                            .append(hmapTagIdioma.get(EnumTagPlantilla.TIPO_FACTOR).concat(" : ").concat(impuestosCargados.getTipoFactorP() != null ? impuestosCargados.getTipoFactorP().concat("     ") : ""))
                            .append(hmapTagIdioma.get(EnumTagPlantilla.IMPORTE).concat(" : $").concat(String.valueOf(impuestosCargados.getImporteP()) != null ? String.valueOf(impuestosCargados.getImporteP()).concat("     ") : ""))
                            .append(hmapTagIdioma.get(EnumTagPlantilla.BASE).concat(" : ").concat(String.valueOf(impuestosCargados.getBaseP()) != null ? String.valueOf(impuestosCargados.getBaseP()).concat("    ") : ""));
            }
            return descripcion;
        } catch (Exception e) {
            System.out.println("Ocurrio un error al tratar de armar el concepto con impuestos trasladado" + e.getMessage());
            return new StringBuilder("");
        }
    }
    
    private StringBuilder armarConceptoImpuestoPRetenciones(ComplementoPago20.Pago  conceptos) {
        StringBuilder descripcion = new StringBuilder();
        try {
            if (conceptos.getImpuestosP() != null && conceptos.getImpuestosP().getRetencionesP() != null) {
                for (Pago.ImpuestosP.RetencionesP.RetencionP impuestosCargados : conceptos.getImpuestosP().getRetencionesP().getRetencionP()) {
                    descripcion.append("\n");
                    CatImpuestos obtenerCatImpuestosSatByClave = catalogosSatService.obtenerCatImpuestoSatByClave(impuestosCargados.getImpuestoP());
                    descripcion.append(hmapTagIdioma.get(EnumTagPlantilla.IMP_RETENIDO).concat(" : ").concat(obtenerCatImpuestosSatByClave.getDescripcion() != null ? obtenerCatImpuestosSatByClave.getDescripcion().concat("    ") : ""))
                            .append(hmapTagIdioma.get(EnumTagPlantilla.IMPORTE).concat(" : $").concat(String.valueOf(impuestosCargados.getImporteP()) != null ? String.valueOf(impuestosCargados.getImporteP()).concat("    ") : ""));
                }
            }
            return descripcion;
        } catch (Exception e) {

            System.out.println("Ocurrio un error al tratar de armar el concepto con impuestos retenido " + e.getMessage());
            return new StringBuilder("");
        }
    }

    private StringBuilder armarConceptoImpuestoDRTraslados(ComplementoPago20.Pago.DoctoRelacionado  conceptos) {
        StringBuilder descripcion = new StringBuilder();
        try {
            for (TrasladoDR  impuestosCargados : conceptos.getImpuestosDR().getTrasladosDR().getTrasladoDR()) {
                descripcion.append("\n");
                CatImpuestos obtenerCatImpuestosSatByClave = catalogosSatService.obtenerCatImpuestoSatByClave(impuestosCargados.getImpuestoDR());
                descripcion.append(hmapTagIdioma.get(EnumTagPlantilla.IMP_TRASLADO).concat(" : ").concat((obtenerCatImpuestosSatByClave.getDescripcion() != null ? obtenerCatImpuestosSatByClave.getDescripcion().concat("    ") : ""))) 
                        .append(hmapTagIdioma.get(EnumTagPlantilla.TASA_O_CUOTA).concat(" : ").concat(String.valueOf(impuestosCargados.getTasaOCuotaDR()) != null ? String.valueOf(impuestosCargados.getTasaOCuotaDR()).concat("    ") : "")) 
                        .append(hmapTagIdioma.get(EnumTagPlantilla.TIPO_FACTOR).concat(" : ").concat(impuestosCargados.getTipoFactorDR() != null ? impuestosCargados.getTipoFactorDR().concat("    ") : "")) 
                        .append(hmapTagIdioma.get(EnumTagPlantilla.IMPORTE).concat(" : $").concat(String.valueOf(impuestosCargados.getImporteDR()) != null ? String.valueOf(impuestosCargados.getImporteDR()).concat("    ") : ""))
                        .append(hmapTagIdioma.get(EnumTagPlantilla.BASE).concat(" : ").concat(String.valueOf(impuestosCargados.getBaseDR()) != null ? String.valueOf(impuestosCargados.getBaseDR()).concat("    ") : ""));
            }
            return descripcion;
        } catch (Exception e) {
            System.out.println("Ocurrio un error al tratar de armar el concepto con impuestos trasladado" + e.getMessage());
            return new StringBuilder("");
        }
    }
    
    private StringBuilder armarConceptoImpuestoDRRetenciones(ComplementoPago20.Pago.DoctoRelacionado  conceptos) {
        StringBuilder descripcion = new StringBuilder();
        try {
            if (conceptos.getImpuestosDR() != null && conceptos.getImpuestosDR().getRetencionesDR() != null && conceptos.getImpuestosDR().getRetencionesDR().getRetencionDR() != null) {
                for (RetencionDR impuestosCargados : conceptos.getImpuestosDR().getRetencionesDR().getRetencionDR()) {
                    descripcion.append("\n");
                    CatImpuestos obtenerCatImpuestosSatByClave = catalogosSatService.obtenerCatImpuestoSatByClave(impuestosCargados.getImpuestoDR());
                    descripcion.append(hmapTagIdioma.get(EnumTagPlantilla.IMP_RETENIDO).concat(" : ").concat(obtenerCatImpuestosSatByClave.getDescripcion() != null ? obtenerCatImpuestosSatByClave.getDescripcion().concat("     ") : ""))
                            .append(hmapTagIdioma.get(EnumTagPlantilla.TASA_O_CUOTA).concat(" : ").concat(String.valueOf(impuestosCargados.getTasaOCuotaDR()) != null ? String.valueOf(impuestosCargados.getTasaOCuotaDR()).concat("     ") : ""))
                            .append(hmapTagIdioma.get(EnumTagPlantilla.TIPO_FACTOR).concat(" : ").concat(impuestosCargados.getTipoFactorDR() != null ? impuestosCargados.getTipoFactorDR().concat("     ") : ""))
                            .append(hmapTagIdioma.get(EnumTagPlantilla.IMPORTE).concat(" : $").concat(String.valueOf(impuestosCargados.getImporteDR()) != null ? String.valueOf(impuestosCargados.getImporteDR()).concat("     ") : ""))
                            .append(hmapTagIdioma.get(EnumTagPlantilla.BASE).concat(" : ").concat(String.valueOf(impuestosCargados.getBaseDR()) != null ? String.valueOf(impuestosCargados.getBaseDR()).concat("    ") : ""));
                }
            }
            return descripcion;
        } catch (Exception e) {

            System.out.println("Ocurrio un error al tratar de armar el concepto con impuestos retenido " + e.getMessage());
            return new StringBuilder("");
        }
    }

    private byte[] getLogo(String rfcEmisor, String nombreLogo) {
        if (urlGetLogo == null || urlGetLogo.isBlank()) {
            log.info("API de logo no configurada (api.get.logo vacío); se omite descarga de logo");
            return null;
        }
        try {
            RestTemplate restTemplate = new RestTemplate();
            String urlLogo = urlGetLogo + claveProducto + "/" + rfcEmisor + "/PLANTILLA/" + Optional.ofNullable(nombreLogo).orElse("");
            log.info("Descargando logo de PDF desde URL [{}]", urlLogo);
            ResponseEntity<byte[]> response = restTemplate.getForEntity(urlLogo, byte[].class);
            if (response.getBody() == null) {
                log.info("No se encontró el logo para el PDF en la URL [{}]", urlLogo);
            }
            return response.getBody();
        } catch (Exception e) {
            log.warn("No se pudo descargar el logo: {}", e.getMessage());
            return null;
        }
    }

    private PdfPTable getGenericTable(int colums, int[] widthColums, float totalWidth, float widthPercentage, float spacingAfter) throws DocumentException {

        PdfPTable table = new PdfPTable(colums);
        table.setWidths(widthColums);
        table.setTotalWidth(totalWidth);
        table.setLockedWidth(true);
        table.setWidthPercentage(widthPercentage);
        table.getDefaultCell().setBorder(Rectangle.NO_BORDER);
        table.setSpacingAfter(spacingAfter);
        return table;
    }

    private void addCellToTable(String label, String value, PdfPTable table) {
        PdfPCell cellLabel = getGenericCellLabel(label);
        table.addCell(cellLabel);
        PdfPCell cellValue = getGenericCellValue(Optional.ofNullable(value).orElse(EMPTY_VALUE));
        table.addCell(cellValue);
    }

    private PdfPCell getGenericCellLabel(String phrase) {
        PdfPCell cell = new PdfPCell(new Paragraph(phrase, fontEtiquetas));
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        cell.setBorder(Rectangle.NO_BORDER);
        return cell;
    }


    private PdfPCell getGenericCellValue(String phrase) {
        PdfPCell cell = new PdfPCell(new Paragraph(phrase, fontContenido));
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        cell.setBorder(Rectangle.NO_BORDER);
        return cell;
    }

    private PdfPCell getGenericEmptyCell(int colspan) {

        PdfPCell cell = new PdfPCell(new Paragraph(Strings.EMPTY));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setColspan(colspan);
        return cell;
    }

    private PdfPCell getGenericHeaderCellLabel(String phrase, int colSpan) {
        PdfPCell cell = new PdfPCell(new Paragraph(phrase, fontEtiquetas));
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        cell.setColspan(colSpan);
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
//        cell.setMinimumHeight(15F);
        return cell;
    }

    public String getStringAnioMesDiaHoraMinSeg(Date date) {
        try {
            DateFormat formatoFechaDiaMesAnioHoraMinutoSegundo = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return formatoFechaDiaMesAnioHoraMinutoSegundo.format(date);
        } catch (Exception e) {
            return EMPTY_VALUE;
        }
    }
}
