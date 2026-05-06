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
import com.itextpdf.text.pdf.*;
import com.cfdi40.exceptionhandlerstarter.exception.BusinessException;
import com.cfdi40.pdfgen.dto.CatImpuestos;
import com.cfdi40.pdfgen.dto.CatTipoFactor;
import com.cfdi40.pdfgen.model.entity.CatIdiomapdf;
import com.cfdi40.pdfgen.model.entity.CatTagxidioma;
import com.cfdi40.pdfgen.model.entity.IdentificadorSucursal;
import com.cfdi40.pdfgen.model.repository.CatIdiomapdfRepository;
import com.cfdi40.pdfgen.model.repository.CatTagxidiomaRepository;
import com.cfdi40.pdfgen.util.Utilities;
import com.cfdi40.pdfgen.util.*;
import com.cfdi40.pdfgen.tenants.tenanta.cfdi.dto.CFDI;
import com.cfdi40.pdfgen.tenants.tenanta.cfdi.dto.CFDI.Complementos.ComplementoPago20;
import com.cfdi40.pdfgen.tenants.tenanta.cfdi.dto.CFDI.Complementos.ComplementoPago20.Pago.DoctoRelacionado;
import com.cfdi40.pdfgen.tenants.tenanta.cfdi.dto.CFDI.Complementos.ComplementoPago20.Pago.DoctoRelacionado.ImpuestosDR.RetencionesDR.RetencionDR;
import com.cfdi40.pdfgen.tenants.tenanta.cfdi.dto.CFDI.Complementos.ComplementoPago20.Pago.DoctoRelacionado.ImpuestosDR.TrasladosDR.TrasladoDR;
import com.cfdi40.pdfgen.tenants.tenanta.cfdi.dto.CFDI.Complementos.ComplementoPago20.Pago.ImpuestosP.RetencionesP.RetencionP;
import com.cfdi40.pdfgen.tenants.tenanta.cfdi.dto.CFDI.Complementos.ComplementoPago20.Pago.ImpuestosP.TrasladosP.TrasladoP;
import com.cfdi40.pdfgen.tenants.tenanta.cfdi.dto.CFDI.Conceptos;
import com.cfdi40.pdfgen.tenants.tenanta.cfdi.dto.CFDI.Conceptos.CuentaPredial;
import com.cfdi40.pdfgen.tenants.tenanta.cfdi.dto.CFDI.Conceptos.InformacionAduanera;
import com.cfdi40.pdfgen.tenants.tenanta.cfdi.dto.CFDI.Impuestos.Retencion;
import com.cfdi40.pdfgen.tenants.tenanta.cfdi.dto.CFDI.Impuestos.Traslado;
import com.cfdi40.pdfgen.tenants.tenanta.cfdi.dto.CFDI.ImpuestosLocales.TrasladoLocal;
import com.cfdi40.pdfgen.tenants.tenanta.cfdi.dto.CFDI.TenantAData;
import com.cfdi40.pdfgen.tenants.tenanta.cfdi.dto.CFDI.Relacionados;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.*;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import java.math.RoundingMode;

@Service

public class PdfFacturaTenantAServiceImpl extends PdfPageEventHelper implements PdfFacturaTenantAService {

    @Autowired
    CatTagxidiomaRepository catTagxidiomaRepository; 
    @Autowired
    CatIdiomapdfRepository catIdiomapdfRepository;
    @org.springframework.beans.factory.annotation.Autowired
    CatalogStaticFallbackService catalogFallback;
    @Autowired
    CatalogosSATService catalogosSatService;
    
    Font fontLeyendas = new Font(Font.FontFamily.HELVETICA, 5, Font.NORMAL, BaseColor.BLACK);
    Font fontSellos = new Font(Font.FontFamily.HELVETICA, 6, Font.NORMAL, BaseColor.BLACK);
    Font fontEtiquetasFinalesBold = new Font(Font.FontFamily.HELVETICA, 6, Font.BOLDITALIC, BaseColor.BLACK);
    Font fontEtiquetasFinales = new Font(Font.FontFamily.HELVETICA, 7, Font.NORMAL, BaseColor.BLACK);
    Font fontContenidoComplemento = new Font(Font.FontFamily.HELVETICA, 4.6f, Font.NORMAL, BaseColor.BLACK);
    Font fontEtiquetasComplementoPago = new Font(Font.FontFamily.HELVETICA, 5, Font.BOLD, BaseColor.BLACK);
    Font fontEtiquetasComplemento = new Font(Font.FontFamily.HELVETICA, 7, Font.BOLD, BaseColor.BLACK);
    Font fontContenido = new Font(Font.FontFamily.HELVETICA, 8, Font.NORMAL, BaseColor.BLACK);
    Font fontEtiquetas = new Font(Font.FontFamily.HELVETICA, 8, Font.BOLDITALIC, BaseColor.BLACK);
    Font fontEncabezados = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLDITALIC, BaseColor.WHITE);
    private HashMap<EnumTagPlantilla, String> hmapTagIdioma;
    private List<CatTagxidioma> catTagxidioma;
    private static final int ANCHO_TOTAL__TABLA = 520;
    private static final int ANCHO_TOTLA_SUBTABLA = 530;
    private static final int PORCENTAJE_ANCHO_TABLA = 100;
    private static final float ALTURA_FIJA_TABLA = 80;
    private static final float ALTURA_FIJA_TABLA_SUBTOTAL = 15;
    private static final float ALTURA_FIJA_TABLA_COMPLEMENTO_PAGO = 1.5f;
    private static final float ALTURA_FIJA_TABLA_QR = 90;
    private static final float ALTURA_FIJA_FINAL_PAGE = 2;

    @Override
    public ByteArrayOutputStream generarPDfFactura(CFDI facturaDto, IdentificadorSucursal identificadorSucursal) throws BusinessException {
        try {
            ByteArrayOutputStream pdfResponse = null;
            TenantAData datosTenantAData = facturaDto.getTenantAData() != null ? facturaDto.getTenantAData() : new TenantAData();
            CatIdiomapdf catIdioma = null;
            
            switch (facturaDto.getIdioma() != null && !facturaDto.getIdioma().trim().isEmpty()  ? facturaDto.getIdioma() : ConstantesFacto.IDIOMA_ESANOL) {
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
            document = new Document(PageSize.LETTER, 6.0f, 10.0f, 12.0f, 12.0f);
            pdfResponse = new ByteArrayOutputStream();

            TableHeader event = new TableHeader(identificadorSucursal.getTipoElaboracion());
            PdfWriter writer = PdfWriter.getInstance(document, pdfResponse);
            writer.setPageEvent(event);

            document.open();

            agregarDatosEmisor(document,facturaDto);
            agregarDatosSucursal(document,facturaDto,datosTenantAData);
            agregarTableInformacionGlobal(document,facturaDto);
            agregarTableDatosCfdiRelacionados(document,facturaDto);
            agregarConceptos(document,facturaDto);
            agregarSubtotales(document,facturaDto);
            agregarComplementoPagos(document,facturaDto);
            agregarSellos(document,facturaDto);
            agregarDatosFinales(document,facturaDto);
            agregarCodigoQR(document,facturaDto);
            agregarLeyenda(document);

            document.close();
            writer.flush();
            return pdfResponse;

        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException(HttpStatus.BAD_REQUEST,"Ocurrio un error al tratar de generar el PDF de la plantilla TenantA de la factura con UUID "
                    + (facturaDto.getTimbrado() != null ? facturaDto.getTimbrado().getUuid() : "") + " Error: " + e.getMessage() + e.getStackTrace()[0].getLineNumber() + "Metodo " + e.getStackTrace()[0].getMethodName());
        }
    }

    /**
     * Metodo para agregar los datos del Emisor.
     *
     * @param document
     * @return Tabla Datos Emisor
     * @throws DocumentException
     */
    public void agregarDatosEmisor(Document document, CFDI facturaDto) throws DocumentException {
        try {
            PdfPTable tableDatosEmisor = new PdfPTable(2);
            tableDatosEmisor.setWidths(new int[]{60, 130});
            tableDatosEmisor.setTotalWidth(ANCHO_TOTAL__TABLA);
            tableDatosEmisor.setLockedWidth(true);
            tableDatosEmisor.setWidthPercentage(PORCENTAJE_ANCHO_TABLA);
            tableDatosEmisor.getDefaultCell().setBorder(Rectangle.NO_BORDER);
            tableDatosEmisor.setSpacingAfter(8f);

            PdfPTable tableTitulo = new PdfPTable(2);
            tableTitulo.setWidths(new int[]{300, 200});
            tableTitulo.setTotalWidth(ANCHO_TOTAL__TABLA);
            tableTitulo.setLockedWidth(true);
            tableTitulo.setWidthPercentage(PORCENTAJE_ANCHO_TABLA);
            tableTitulo.getDefaultCell().setBorder(Rectangle.NO_BORDER);
            tableTitulo.setSpacingAfter(8f);

            PdfPCell nombreEmisor = new PdfPCell(new Paragraph(facturaDto.getEmisor().getNombre() != null ? facturaDto.getEmisor().getNombre() : "", fontEtiquetas));
            nombreEmisor.setHorizontalAlignment(Element.ALIGN_LEFT);
            nombreEmisor.setColspan(2);
            nombreEmisor.setBorder(Rectangle.NO_BORDER);

            PdfPCell rfcEmisorC = new PdfPCell(new Paragraph(facturaDto.getEmisor().getRfc() != null ? facturaDto.getEmisor().getRfc() : "", fontEtiquetas));
            rfcEmisorC.setHorizontalAlignment(Element.ALIGN_LEFT);
            rfcEmisorC.setColspan(2);
            rfcEmisorC.setPaddingBottom(10f);
            rfcEmisorC.setBorder(Rectangle.NO_BORDER);

            PdfPCell regimenEmisorC = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.N_REGIMENFISCAL).concat(" : "), fontEtiquetas));
            regimenEmisorC.setHorizontalAlignment(Element.ALIGN_LEFT);
            regimenEmisorC.setBorder(Rectangle.NO_BORDER);

            PdfPCell regimenEmisorV = new PdfPCell(new Paragraph(facturaDto.getEmisor().getRegimenFiscal() != null ? facturaDto.getEmisor().getRegimenFiscal() : "", fontContenido));
            regimenEmisorV.setHorizontalAlignment(Element.ALIGN_LEFT);
            regimenEmisorV.setBorder(Rectangle.NO_BORDER);

            PdfPCell cellEmisionLugarFecha = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.N_LUGARFECHAEMISION).concat(" : "), fontEtiquetas));
            cellEmisionLugarFecha.setHorizontalAlignment(Element.ALIGN_LEFT);
            cellEmisionLugarFecha.setBorder(Rectangle.NO_BORDER);

            PdfPCell cellEmisionLugarFechaV = new PdfPCell(new Paragraph((facturaDto.getLugarExpedicion() != null ? facturaDto.getLugarExpedicion() : "").concat(", ").concat(" a ".concat(FechasUtil.getStringAnioMesDiaHoraMinSeg(facturaDto.getFecha() != null ? facturaDto.getFecha() : new Date()))), fontContenido));
            cellEmisionLugarFechaV.setHorizontalAlignment(Element.ALIGN_LEFT);
            cellEmisionLugarFechaV.setBorder(Rectangle.NO_BORDER);

            PdfPCell monedaC = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.N_MONEDA).concat(" : "), fontEtiquetas));
            monedaC.setHorizontalAlignment(Element.ALIGN_LEFT);
            monedaC.setBorder(Rectangle.NO_BORDER);

            PdfPCell monedaV = new PdfPCell(new Paragraph(facturaDto.getMoneda() != null ? facturaDto.getMoneda() : "", fontContenido));
            monedaV.setHorizontalAlignment(Element.ALIGN_LEFT);
            monedaV.setBorder(Rectangle.NO_BORDER);

            PdfPCell tipoCambioC = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.C_TIPOCAMBIO).concat(" : "), fontEtiquetas));
            tipoCambioC.setHorizontalAlignment(Element.ALIGN_LEFT);
            tipoCambioC.setBorder(Rectangle.NO_BORDER);

            PdfPCell tipoCambioV = new PdfPCell(new Paragraph(String.valueOf(facturaDto.getTipoCambio() != null ? facturaDto.getTipoCambio() : ""), fontContenido));
            tipoCambioV.setHorizontalAlignment(Element.ALIGN_LEFT);
            tipoCambioV.setBorder(Rectangle.NO_BORDER);

            if (!facturaDto.getTipoCfdi().equals(CFDI.EnumTipoFactura.PAGO)) {
                if (facturaDto.getTipoCfdi().equals(CFDI.EnumTipoFactura.NOTA_CREDITO)) {

                    PdfPCell tipoDocumento = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.NOTA_CREDITO), fontEncabezados));
                    tipoDocumento.setBackgroundColor(BaseColor.BLACK);
                    tipoDocumento.setBorder(Rectangle.NO_BORDER);
                    tipoDocumento.setHorizontalAlignment(Element.ALIGN_LEFT);

                    PdfPCell tipoDocumentoFolio = new PdfPCell(new Paragraph((facturaDto.getTenantAData() != null && facturaDto.getTenantAData().getTrxType() != null ? facturaDto.getTenantAData().getTrxType() : "").concat((facturaDto.getFolio() != null ? facturaDto.getFolio() : "")), fontEncabezados));
                    tipoDocumentoFolio.setBackgroundColor(BaseColor.BLACK);
                    tipoDocumentoFolio.setBorder(Rectangle.NO_BORDER);
                    tipoDocumentoFolio.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    tableTitulo.addCell(tipoDocumento);
                    tableTitulo.addCell(tipoDocumentoFolio);

                } else if (facturaDto.getTipoCfdi().equals(CFDI.EnumTipoFactura.FACTURA)) {
                    PdfPCell tipoDocumento = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.FACTURA), fontEncabezados));
                    tipoDocumento.setBackgroundColor(BaseColor.BLACK);
                    tipoDocumento.setBorder(Rectangle.NO_BORDER);
                    tipoDocumento.setHorizontalAlignment(Element.ALIGN_LEFT);

                    PdfPCell tipoDocumentoFolio = new PdfPCell(new Paragraph((facturaDto.getTenantAData() != null && facturaDto.getTenantAData().getTrxType() != null ? facturaDto.getTenantAData().getTrxType() : "").concat((facturaDto.getFolio() != null ? facturaDto.getFolio() : "")), fontEncabezados));
                    tipoDocumentoFolio.setBackgroundColor(BaseColor.BLACK);
                    tipoDocumentoFolio.setBorder(Rectangle.NO_BORDER);
                    tipoDocumentoFolio.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    tableTitulo.addCell(tipoDocumento);
                    tableTitulo.addCell(tipoDocumentoFolio);
                }
            } else {
                PdfPCell tipoDocumento = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.C_COMPLEMENTO_RECEPCION_PAGO), fontEncabezados));
                tipoDocumento.setBackgroundColor(BaseColor.BLACK);
                tipoDocumento.setBorder(Rectangle.NO_BORDER);
                tipoDocumento.setHorizontalAlignment(Element.ALIGN_LEFT);

                PdfPCell tipoDocumentoFolio = new PdfPCell(new Paragraph((facturaDto.getFolio() != null ? facturaDto.getFolio() : ""), fontEncabezados));
                tipoDocumentoFolio.setBackgroundColor(BaseColor.BLACK);
                tipoDocumentoFolio.setBorder(Rectangle.NO_BORDER);
                tipoDocumentoFolio.setHorizontalAlignment(Element.ALIGN_RIGHT);
                tableTitulo.addCell(tipoDocumento);
                tableTitulo.addCell(tipoDocumentoFolio);
            }

            PdfPCell titulo = new PdfPCell(tableTitulo);
            titulo.setColspan(2);
            tableDatosEmisor.addCell(titulo);
            tableDatosEmisor.addCell(nombreEmisor);
            tableDatosEmisor.addCell(rfcEmisorC);
            tableDatosEmisor.addCell(regimenEmisorC);
            tableDatosEmisor.addCell(regimenEmisorV);
            tableDatosEmisor.addCell(cellEmisionLugarFecha);
            tableDatosEmisor.addCell(cellEmisionLugarFechaV);
            tableDatosEmisor.addCell(monedaC);
            tableDatosEmisor.addCell(monedaV);
            tableDatosEmisor.addCell(tipoCambioC);
            tableDatosEmisor.addCell(tipoCambioV);

            document.add(tableDatosEmisor);
        } catch (Exception ex) {
           
            Logger.getLogger(PdfFacturaTenantAServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Metodo para agregar los datos de la sucursal.
     *
     * @param document
     * @return Tabla DatosSucursal
     * @throws DocumentException
     */
    public void agregarDatosSucursal(Document document, CFDI facturaDto, TenantAData datosTenantAData) throws DocumentException {
        PdfPTable tableDatosSucursal = new PdfPTable(4);
        tableDatosSucursal.setWidths(new int[]{64, 100, 65, 70});
        tableDatosSucursal.setTotalWidth(ANCHO_TOTAL__TABLA);
        tableDatosSucursal.setLockedWidth(true);
        tableDatosSucursal.setWidthPercentage(PORCENTAJE_ANCHO_TABLA);
        tableDatosSucursal.getDefaultCell().setBorder(Rectangle.NO_BORDER);
        tableDatosSucursal.setSpacingAfter(8f);

        PdfPCell encabezadoCliente = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.C_CLIENTE), fontEncabezados));
        encabezadoCliente.setHorizontalAlignment(Element.ALIGN_LEFT);
        encabezadoCliente.setColspan(4);
        encabezadoCliente.setBackgroundColor(BaseColor.BLACK);
        encabezadoCliente.setBorder(Rectangle.NO_BORDER);

        PdfPCell nombreClienteC = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.C_RAZONSOCIAL).concat(" : "), fontEtiquetas));
        nombreClienteC.setHorizontalAlignment(Element.ALIGN_LEFT);
        nombreClienteC.setBorder(Rectangle.NO_BORDER);

        PdfPCell nombreCliente = new PdfPCell(new Paragraph(facturaDto.getReceptor() != null && facturaDto.getReceptor().getNombre() != null ? facturaDto.getReceptor().getNombre() : "", fontContenido));
        nombreCliente.setHorizontalAlignment(Element.ALIGN_LEFT);
        nombreCliente.setBorder(Rectangle.NO_BORDER);

        PdfPCell rfcCliente = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.RFC).concat(" : "), fontEtiquetas));
        rfcCliente.setHorizontalAlignment(Element.ALIGN_LEFT);
        rfcCliente.setBorder(Rectangle.NO_BORDER);

        PdfPCell rfcClienteV = new PdfPCell(new Paragraph(facturaDto.getReceptor() != null && facturaDto.getReceptor().getRfc() != null ? facturaDto.getReceptor().getRfc() : "", fontContenido));
        rfcClienteV.setHorizontalAlignment(Element.ALIGN_LEFT);
        rfcClienteV.setBorder(Rectangle.NO_BORDER);

        PdfPCell referenciaClienteC = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.MIC_REFERENCIA), fontEtiquetas));
        referenciaClienteC.setHorizontalAlignment(Element.ALIGN_LEFT);
        referenciaClienteC.setBorder(Rectangle.NO_BORDER);

        PdfPCell referenciaClienteV = new PdfPCell(new Paragraph(datosTenantAData != null && datosTenantAData.getNumeroReferencia() != null ? datosTenantAData.getNumeroReferencia() : "", fontContenido));
        referenciaClienteV.setHorizontalAlignment(Element.ALIGN_LEFT);
        referenciaClienteV.setBorder(Rectangle.NO_BORDER);

        PdfPCell numClienteC = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.C_NOCLIENTE).concat(" : "), fontEtiquetas));
        numClienteC.setHorizontalAlignment(Element.ALIGN_LEFT);
        numClienteC.setBorder(Rectangle.NO_BORDER);

        PdfPCell numClienteV = new PdfPCell(new Paragraph(datosTenantAData != null && datosTenantAData.getNumeroCliente() != null ? datosTenantAData.getNumeroCliente() : "", fontContenido));
        numClienteV.setHorizontalAlignment(Element.ALIGN_LEFT);
        numClienteV.setBorder(Rectangle.NO_BORDER);

        PdfPCell numOrdenFacturacionClienteC = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.C_NOORDENFACTURACION).concat(" : "), fontEtiquetas));
        numOrdenFacturacionClienteC.setHorizontalAlignment(Element.ALIGN_LEFT);
        numOrdenFacturacionClienteC.setBorder(Rectangle.NO_BORDER);

        PdfPCell numOrdenFacturacionClientev = new PdfPCell(new Paragraph(datosTenantAData != null && datosTenantAData.getNumeroOrdenFacturacion() != null ? datosTenantAData.getNumeroOrdenFacturacion() : "", fontContenido));
        numOrdenFacturacionClientev.setHorizontalAlignment(Element.ALIGN_LEFT);
        numOrdenFacturacionClientev.setBorder(Rectangle.NO_BORDER);

        PdfPCell usoCfdiReceptor = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.G_USOCFDI).concat(" : "), fontEtiquetas));
        usoCfdiReceptor.setHorizontalAlignment(Element.ALIGN_LEFT);
        usoCfdiReceptor.setBorder(Rectangle.NO_BORDER);

        PdfPCell usoCfdiReceptorV = new PdfPCell(new Paragraph(facturaDto.getReceptor().getUsoCFDI() != null ? facturaDto.getReceptor().getUsoCFDI() : "", fontContenido));
        usoCfdiReceptorV.setHorizontalAlignment(Element.ALIGN_LEFT);
        usoCfdiReceptorV.setBorder(Rectangle.NO_BORDER);

     
        PdfPCell regimenFiscalC = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.REG_FISCAL_RECEP).concat(" : "), fontEtiquetas));
        regimenFiscalC.setHorizontalAlignment(Element.ALIGN_LEFT);
        regimenFiscalC.setBorder(Rectangle.NO_BORDER);

    
        PdfPCell regimenFiscalV = new PdfPCell(new Paragraph(facturaDto.getReceptor().getRegimenFiscal() != null ? facturaDto.getReceptor().getRegimenFiscal() : "", fontContenido));
        regimenFiscalV.setHorizontalAlignment(Element.ALIGN_LEFT);
        regimenFiscalV.setBorder(Rectangle.NO_BORDER);

        PdfPCell domFiscR = new PdfPCell(new Paragraph(facturaDto.getReceptor() != null && facturaDto.getReceptor().getDomicilioFiscal() != null ? hmapTagIdioma.get(EnumTagPlantilla.R_DOMFISCRECEP).concat(" : ") : "", fontEtiquetas));
        domFiscR.setHorizontalAlignment(Element.ALIGN_LEFT);
        domFiscR.setBorder(Rectangle.NO_BORDER);

        PdfPCell domFiscRV = new PdfPCell(new Paragraph(facturaDto.getReceptor() != null && facturaDto.getReceptor().getDomicilioFiscal() != null ? facturaDto.getReceptor().getDomicilioFiscal() : "", fontContenido));
        domFiscRV.setHorizontalAlignment(Element.ALIGN_LEFT);
        domFiscRV.setBorder(Rectangle.NO_BORDER);


        tableDatosSucursal.addCell(encabezadoCliente);
        tableDatosSucursal.addCell(nombreClienteC);
        tableDatosSucursal.addCell(nombreCliente);
        tableDatosSucursal.addCell(referenciaClienteC);
        tableDatosSucursal.addCell(referenciaClienteV);
        tableDatosSucursal.addCell(rfcCliente);
        tableDatosSucursal.addCell(rfcClienteV);
        tableDatosSucursal.addCell(numClienteC);
        tableDatosSucursal.addCell(numClienteV);
        tableDatosSucursal.addCell(regimenFiscalC);
        tableDatosSucursal.addCell(regimenFiscalV);
        tableDatosSucursal.addCell(domFiscR);
        tableDatosSucursal.addCell(domFiscRV);
        tableDatosSucursal.addCell(usoCfdiReceptor);
        tableDatosSucursal.addCell(usoCfdiReceptorV);
        if(!facturaDto.getTipoCfdi().equals(CFDI.EnumTipoFactura.PAGO)){
        tableDatosSucursal.addCell(numOrdenFacturacionClienteC);
        tableDatosSucursal.addCell(numOrdenFacturacionClientev);
        }else{
            PdfPCell celdaVacia = new PdfPCell();
            celdaVacia.setColspan(2);
            celdaVacia.setBorder(Rectangle.NO_BORDER);
            tableDatosSucursal.addCell(celdaVacia);
        }

        document.add(tableDatosSucursal);
    }

    /**
     * Metodo para agregar los conceptos
     *
     * @param document
     * @return Tabla Conceptos
     * @throws DocumentException
     */
    public void agregarConceptos(Document document, CFDI facturaDto) throws DocumentException {
        PdfPTable tableConceptos = new PdfPTable(6);
        tableConceptos.setWidths(new int[]{30, 39, 200, 30, 50, 50});
        tableConceptos.setTotalWidth(ANCHO_TOTAL__TABLA);
        tableConceptos.setLockedWidth(true);
        tableConceptos.setWidthPercentage(PORCENTAJE_ANCHO_TABLA);
        tableConceptos.getDefaultCell().setBorder(Rectangle.NO_BORDER);

        PdfPCell encabezadoConceptos = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.C_PRODUCTOS_SERVICIO), fontEncabezados));
        encabezadoConceptos.setHorizontalAlignment(Element.ALIGN_LEFT);
        encabezadoConceptos.setColspan(6);
        encabezadoConceptos.setBackgroundColor(BaseColor.BLACK);
        encabezadoConceptos.setBorder(Rectangle.NO_BORDER);

        PdfPCell cantidadConceptosC = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.P_CANTIDAD), fontEtiquetas));
        cantidadConceptosC.setHorizontalAlignment(Element.ALIGN_CENTER);
        cantidadConceptosC.setBorder(Rectangle.NO_BORDER);

        PdfPCell claveConceptoC = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.P_CLAVEPRODUCTO), fontEtiquetas));
        claveConceptoC.setHorizontalAlignment(Element.ALIGN_CENTER);
        claveConceptoC.setBorder(Rectangle.NO_BORDER);

        PdfPCell conceptoConceptoC = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.N_CONCEPTO), fontEtiquetas));
        conceptoConceptoC.setHorizontalAlignment(Element.ALIGN_CENTER);
        conceptoConceptoC.setBorder(Rectangle.NO_BORDER);

        PdfPCell unidadConceptoC = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.P_CLAVEUNIDAD), fontEtiquetas));
        unidadConceptoC.setHorizontalAlignment(Element.ALIGN_CENTER);
        unidadConceptoC.setBorder(Rectangle.NO_BORDER);

        PdfPCell precioUniConceptoC = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.P_PRECIOUNITARIO), fontEtiquetas));
        precioUniConceptoC.setHorizontalAlignment(Element.ALIGN_RIGHT);
        precioUniConceptoC.setBorder(Rectangle.NO_BORDER);

        PdfPCell importeConceptoC = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.P_IMPORTE), fontEtiquetas));
        importeConceptoC.setHorizontalAlignment(Element.ALIGN_RIGHT);
        importeConceptoC.setBorder(Rectangle.NO_BORDER);

        tableConceptos.addCell(encabezadoConceptos);
        tableConceptos.addCell(cantidadConceptosC);
        tableConceptos.addCell(claveConceptoC);
        tableConceptos.addCell(conceptoConceptoC);
        tableConceptos.addCell(unidadConceptoC);
        tableConceptos.addCell(precioUniConceptoC);
        tableConceptos.addCell(importeConceptoC);

        for (Conceptos conceptos : facturaDto.getConceptos()) {

            StringBuilder descConcepto = new StringBuilder();

            descConcepto.append("\n");
            descConcepto.append(hmapTagIdioma.get(EnumTagPlantilla.S_IMPOBJECT)).append(":  ").append(conceptos.getObjetoImp());

            if (conceptos.getImpuestos() != null && conceptos.getImpuestos().getTraslados() != null && !conceptos.getImpuestos().getTraslados().isEmpty()) {
                descConcepto.append(armarConceptoImpuestoTraslados(conceptos));
            }
            if (conceptos.getImpuestos() != null && conceptos.getImpuestos().getRetenciones() != null && !conceptos.getImpuestos().getRetenciones().isEmpty()) {
                descConcepto.append(armarConceptoImpuestoRetenciones(conceptos));
            }
            if (conceptos.getCuentaPredial() != null) {
                for(CuentaPredial cuentaPredialCagados : conceptos.getCuentaPredial()){
                    descConcepto.append(armarConceptoCuentaPredial(cuentaPredialCagados));
                }               
            }
            if (conceptos.getInformacionAduanera() != null) {
                for(InformacionAduanera informacionAduanera : conceptos.getInformacionAduanera()){
                descConcepto.append(armarConceptoPedimentoAduana(informacionAduanera));
                }
            }

            if (conceptos.getDescuento() != null && !conceptos.getDescuento().equals(BigDecimal.ZERO)) {
                descConcepto.append(armarConceptoDescuento(conceptos));
            }

            if (conceptos.getACuentaTerceros()!= null) {
                descConcepto.append(armarConceptoCuentaTerceros(conceptos));
            }


            PdfPCell celdaCantidad = new PdfPCell(new Paragraph(new Phrase(conceptos.getCantidad().toString(), fontEtiquetasFinales)));
            celdaCantidad.setHorizontalAlignment(Element.ALIGN_CENTER);
            celdaCantidad.setBorder(Rectangle.NO_BORDER);

            PdfPCell celdaClaveProducto = new PdfPCell(new Paragraph(new Phrase(conceptos.getClaveProdServ(), fontEtiquetasFinales)));
            celdaClaveProducto.setHorizontalAlignment(Element.ALIGN_CENTER);
            celdaClaveProducto.setBorder(Rectangle.NO_BORDER);

            PdfPCell celdaDescripcion = new PdfPCell(new Paragraph(new Phrase(conceptos.getDescripcion().concat(descConcepto.toString()), fontEtiquetasFinales)));
            celdaDescripcion.setHorizontalAlignment(Element.ALIGN_JUSTIFIED);
            celdaDescripcion.setBorder(Rectangle.NO_BORDER);

            PdfPCell celdaUnidad = new PdfPCell(new Paragraph(new Phrase(conceptos.getClaveUnidad(), fontEtiquetasFinales)));
            celdaUnidad.setHorizontalAlignment(Element.ALIGN_CENTER);
            celdaUnidad.setBorder(Rectangle.NO_BORDER);

            PdfPCell celdaPUnitario = new PdfPCell(new Paragraph(new Phrase(Utilities.bigDecimalToStr(conceptos.getValorUnitario()), fontEtiquetasFinales)));
            celdaPUnitario.setHorizontalAlignment(Element.ALIGN_RIGHT);
            celdaPUnitario.setBorder(Rectangle.NO_BORDER);

            PdfPCell celdaImporte = new PdfPCell(new Paragraph(new Phrase(Utilities.bigDecimalToStr(conceptos.getImporte()), fontEtiquetasFinales)));
            celdaImporte.setHorizontalAlignment(Element.ALIGN_RIGHT);
            celdaImporte.setBorder(Rectangle.NO_BORDER);

            tableConceptos.addCell(celdaCantidad);
            tableConceptos.addCell(celdaClaveProducto);
            tableConceptos.addCell(celdaDescripcion);
            tableConceptos.addCell(celdaUnidad);
            tableConceptos.addCell(celdaPUnitario);
            tableConceptos.addCell(celdaImporte);
        }

        document.add(tableConceptos);
    }

    public StringBuilder armarConceptoImpuestoTraslados(Conceptos conceptos) {
        StringBuilder descConcepto = new StringBuilder();
        try {
            for (Conceptos.Traslados impuestosCargados : conceptos.getImpuestos().getTraslados()) {
                descConcepto.append("\n");
                CatImpuestos obtenerImpuestoCatalogo = catalogosSatService.obtenerCatImpuestoSatByClave(impuestosCargados.getImpuesto());
                descConcepto.append("Impuesto Trasladado: ").append((obtenerImpuestoCatalogo.getDescripcion() != null ? obtenerImpuestoCatalogo.getDescripcion() : ""))
                        .append(" Tasa/Cuota: ").append((String.valueOf(impuestosCargados.getTasaoCuota() != null ? impuestosCargados.getTasaoCuota() : "")))
                        .append(" Tipo Factor: ").append((impuestosCargados.getTipoFactor() != null ? impuestosCargados.getTipoFactor() : ""))
                        .append(" Importe $").append((String.valueOf(impuestosCargados.getImporte() != null ? impuestosCargados.getImporte() : "")))
                        .append(" Base: ").append(impuestosCargados.getBase() != null ? impuestosCargados.getBase() : "");
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
            for (Conceptos.Retenciones impuestosCargados : conceptos.getImpuestos().getRetenciones()) {
                descConcepto.append("\n");
                CatImpuestos obtenerImpuestoCatalogo = catalogosSatService.obtenerCatImpuestoSatByClave(impuestosCargados.getImpuesto());
                descConcepto.append("Impuesto Retenido: ").append((obtenerImpuestoCatalogo.getDescripcion() != null ? obtenerImpuestoCatalogo.getDescripcion() : ""))
                        .append(" Tasa/Cuota: ").append((String.valueOf(impuestosCargados.getTasaoCuota() != null ? impuestosCargados.getTasaoCuota() : "")))
                        .append(" Tipo Factor: ").append((impuestosCargados.getTipoFactor() != null ? impuestosCargados.getTipoFactor() : ""))
                        .append(" Importe $").append((String.valueOf(impuestosCargados.getImporte() != null ? impuestosCargados.getImporte() : "")))
                        .append(" Base: ").append(impuestosCargados.getBase() != null ? impuestosCargados.getBase() : "");
            }
            return descConcepto;
        } catch (Exception e) {
            System.out.println("Ocurrio un error al tratar de armar el concepto con impuestos trasladado" + e.getMessage());
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

    public StringBuilder armarConceptoPedimentoAduana(Conceptos.InformacionAduanera conceptos) {
        StringBuilder descConcepto = new StringBuilder();
        try {
            descConcepto.append("\n");
            descConcepto.append("  No. Pedimento Aduanal: ").append(conceptos.getNumeroPedimento());
            return descConcepto;
        } catch (Exception e) {
            System.out.println("Ocurrio un error al tratar de armar el concepto con Número pedimento aduana" + e.getMessage());
            return new StringBuilder("");
        }
    }

    /**
     * Metodo para agregar los subtotales
     *
     * @param document
     * @return Tabla Subtotales
     * @throws DocumentException
     */
    public void agregarSubtotales(Document document, CFDI facturaDto) throws DocumentException {
        PdfPTable tableConceptos = new PdfPTable(4);
        tableConceptos.setWidths(new int[]{60, 145, 45, 60});
        tableConceptos.setTotalWidth(ANCHO_TOTAL__TABLA);
        tableConceptos.setLockedWidth(true);
        tableConceptos.setWidthPercentage(PORCENTAJE_ANCHO_TABLA);
        tableConceptos.getDefaultCell().setFixedHeight(ALTURA_FIJA_TABLA);
        tableConceptos.getDefaultCell().setBorder(Rectangle.NO_BORDER);
        tableConceptos.setSpacingAfter(8f);

        PdfPCell encabezadosIdFactura = new PdfPCell();
        encabezadosIdFactura.setHorizontalAlignment(Element.ALIGN_RIGHT);
        encabezadosIdFactura.setColspan(4);;
        encabezadosIdFactura.setFixedHeight(ALTURA_FIJA_TABLA_SUBTOTAL);
        encabezadosIdFactura.setBackgroundColor(BaseColor.BLACK);
        encabezadosIdFactura.setBorder(Rectangle.NO_BORDER);
        tableConceptos.addCell(encabezadosIdFactura);

        PdfPCell subtotalC = new PdfPCell(new Phrase(hmapTagIdioma.get(EnumTagPlantilla.T_SUBTOTAL), fontEtiquetas));
        subtotalC.setColspan(3);
        subtotalC.setHorizontalAlignment(Element.ALIGN_RIGHT);
        subtotalC.setBorder(Rectangle.NO_BORDER);
        tableConceptos.addCell(subtotalC);

        PdfPCell subtotalV = new PdfPCell(new Phrase(Utilities.bigDecimalToStr(facturaDto.getSubTotal()), fontContenido));
        subtotalV.setHorizontalAlignment(Element.ALIGN_RIGHT);
        subtotalV.setBorder(Rectangle.NO_BORDER);
        tableConceptos.addCell(subtotalV);

        if (facturaDto.getDescuento() != null && !facturaDto.getDescuento().equals(BigDecimal.ZERO)) {
            PdfPCell descuentoC = new PdfPCell(new Phrase(hmapTagIdioma.get(EnumTagPlantilla.T_DESCUENTO), fontContenido));
            descuentoC.setColspan(3);
            descuentoC.setHorizontalAlignment(Element.ALIGN_RIGHT);
            descuentoC.setBorder(Rectangle.NO_BORDER);
            tableConceptos.addCell(descuentoC);

            PdfPCell descuentoV = new PdfPCell(new Phrase(String.valueOf(facturaDto.getDescuento()), fontContenido));
            descuentoV.setHorizontalAlignment(Element.ALIGN_RIGHT);
            descuentoV.setBorder(Rectangle.NO_BORDER);
            tableConceptos.addCell(descuentoV);
        }

        String sImpuesto = "";
        if (facturaDto.getImpuestos() != null) {
            if (facturaDto.getImpuestos().getTraslado() != null && !facturaDto.getImpuestos().getTraslado().isEmpty()) {
                for (Traslado impuestosTrasladados : facturaDto.getImpuestos().getTraslado()) {
                    BigDecimal impuestoTrasladado = BigDecimal.ZERO;
                    CatImpuestos impuestoSat = catalogosSatService.obtenerCatImpuestoSatByClave(impuestosTrasladados.getImpuesto());
                    impuestoTrasladado = impuestoTrasladado.add(impuestosTrasladados.getImporte());

                    CatTipoFactor tipoFactor;
                    tipoFactor = catalogosSatService.obtenerCatTipofactorSatByClave(impuestosTrasladados.getTipoFactor());
                    if (!tipoFactor.equals("EXENTO")) {
                        sImpuesto = impuestoSat.getDescripcion() + "(" + impuestosTrasladados.getTasaoCuota().setScale(2) + ")";
                    } else {
                        sImpuesto = impuestoSat.getDescripcion() + "(EXENTO)";
                    }

                    PdfPCell celdaImpuestoT = new PdfPCell(new Paragraph(new Phrase(sImpuesto, fontEtiquetas)));
                    celdaImpuestoT.setColspan(3);
                    celdaImpuestoT.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    celdaImpuestoT.setBorder(Rectangle.NO_BORDER);
                    tableConceptos.addCell(celdaImpuestoT);

                    PdfPCell celdaImpuesto = new PdfPCell(new Paragraph(new Phrase(Utilities.bigDecimalToStr(impuestosTrasladados.getImporte()), fontContenido)));
                    celdaImpuesto.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    celdaImpuesto.setBorder(Rectangle.NO_BORDER);
                    tableConceptos.addCell(celdaImpuesto);
                }
            }

            if (facturaDto.getImpuestos().getRetenciones() != null && !facturaDto.getImpuestos().getRetenciones().isEmpty()) {
                for (Retencion impuestosRetenidos : facturaDto.getImpuestos().getRetenciones()) {
                    BigDecimal impuestoRetenido = BigDecimal.ZERO;
                    CatImpuestos impuestoSat = catalogosSatService.obtenerCatImpuestoSatByClave(impuestosRetenidos.getImpuesto());
                    impuestoRetenido = impuestoRetenido.add(impuestosRetenidos.getImporte());
                    sImpuesto = impuestoSat.getDescripcion() + " Retenido";

                    PdfPCell celdaImpuestoR = new PdfPCell(new Paragraph(new Phrase(sImpuesto, fontEtiquetas)));
                    celdaImpuestoR.setColspan(3);
                    celdaImpuestoR.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    celdaImpuestoR.setBorder(Rectangle.NO_BORDER);
                    tableConceptos.addCell(celdaImpuestoR);

                    PdfPCell celdaImpuesto = new PdfPCell(new Paragraph(new Phrase(Utilities.bigDecimalToStr(impuestosRetenidos.getImporte()), fontContenido)));
                    celdaImpuesto.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    celdaImpuesto.setBorder(Rectangle.NO_BORDER);
                    tableConceptos.addCell(celdaImpuesto);

                }
            }

        }

        String sImpuestoLocales = "";
        if (facturaDto.getImpuestosLocales() != null && facturaDto.getImpuestosLocales().getTrasladoLocales() != null && !facturaDto.getImpuestosLocales().getTrasladoLocales().isEmpty()) {
            for (TrasladoLocal imp : facturaDto.getImpuestosLocales().getTrasladoLocales()) {
                sImpuestoLocales = imp.getImpuesto() + "(" + imp.getTasa().setScale(4, RoundingMode.DOWN) + ")"; // 4 decimales por solicitud de tenantA para proyecto PSS-5676

                PdfPCell celdaImpuestoT = new PdfPCell(new Paragraph(new Phrase(sImpuestoLocales, fontEtiquetas)));
                celdaImpuestoT.setColspan(3);
                celdaImpuestoT.setHorizontalAlignment(Element.ALIGN_RIGHT);
                celdaImpuestoT.setBorder(Rectangle.NO_BORDER);

                PdfPCell celdaImpuesto = new PdfPCell(new Paragraph(new Phrase(Utilities.bigDecimalToStr(imp.getImporte()), fontContenido)));
                celdaImpuesto.setHorizontalAlignment(Element.ALIGN_RIGHT);
                celdaImpuesto.setBorder(Rectangle.NO_BORDER);

                tableConceptos.addCell(celdaImpuestoT);
                tableConceptos.addCell(celdaImpuesto);
            }
        }

        sImpuestoLocales = "";
        if (facturaDto.getImpuestosLocales() != null && facturaDto.getImpuestosLocales().getRetencionesLocales() != null && !facturaDto.getImpuestosLocales().getRetencionesLocales().isEmpty()) {
            for (CFDI.ImpuestosLocales.RetencionLocal imp : facturaDto.getImpuestosLocales().getRetencionesLocales()) {
                sImpuestoLocales = imp.getImpuesto() + "(" + imp.getTasa().setScale(4, RoundingMode.DOWN) + ")"; // 4 decimales por solicitud de tenantA para proyecto PSS-5676

                PdfPCell celdaImpuestoT = new PdfPCell(new Paragraph(new Phrase(sImpuestoLocales, fontEtiquetas)));
                celdaImpuestoT.setColspan(3);
                celdaImpuestoT.setHorizontalAlignment(Element.ALIGN_RIGHT);
                celdaImpuestoT.setBorder(Rectangle.NO_BORDER);

                PdfPCell celdaImpuesto = new PdfPCell(new Paragraph(new Phrase(Utilities.bigDecimalToStr(imp.getImporte()), fontContenido)));
                celdaImpuesto.setHorizontalAlignment(Element.ALIGN_RIGHT);
                celdaImpuesto.setBorder(Rectangle.NO_BORDER);

                tableConceptos.addCell(celdaImpuestoT);
                tableConceptos.addCell(celdaImpuesto);
            }
        }

        PdfPCell totalLetraC = new PdfPCell(new Phrase(hmapTagIdioma.get(EnumTagPlantilla.N_IMPORTELETRA).concat(" : "), fontEtiquetas));
        totalLetraC.setHorizontalAlignment(Element.ALIGN_LEFT);
        totalLetraC.setBorder(Rectangle.NO_BORDER);

        PdfPCell totalLetraV = new PdfPCell(new Phrase(Utilities.crearTotalLetra(facturaDto), fontEtiquetasFinales));
        totalLetraV.setHorizontalAlignment(Element.ALIGN_JUSTIFIED);
        totalLetraV.setBorder(Rectangle.NO_BORDER);

        tableConceptos.addCell(totalLetraC);
        tableConceptos.addCell(totalLetraV);

        PdfPCell totalPagarC = new PdfPCell(new Paragraph(new Phrase(hmapTagIdioma.get(EnumTagPlantilla.C_TOTAL), fontEtiquetas)));
        totalPagarC.setHorizontalAlignment(Element.ALIGN_RIGHT);
        totalPagarC.setBorder(Rectangle.NO_BORDER);

        PdfPCell totalPagarV = new PdfPCell(new Paragraph(new Phrase(Utilities.bigDecimalToStr(facturaDto.getTotal()), fontContenido)));
        totalPagarV.setHorizontalAlignment(Element.ALIGN_RIGHT);
        totalPagarV.setBorder(Rectangle.NO_BORDER);

        tableConceptos.addCell(totalPagarC);
        tableConceptos.addCell(totalPagarV);

        PdfPCell finTableSeparator = new PdfPCell(new Phrase("", fontEtiquetas));
        finTableSeparator.setHorizontalAlignment(Element.ALIGN_RIGHT);
        finTableSeparator.setColspan(4);
        finTableSeparator.setBackgroundColor(BaseColor.BLACK);
        finTableSeparator.setBorder(Rectangle.NO_BORDER);
        tableConceptos.addCell(finTableSeparator);

        document.add(tableConceptos);

    }

    /**
     * *
     * Metodo para agregar complemento de recepcion de pago.
     *
     * @param document
     * @throws DocumentException
     */
    public void agregarComplementoPagos(Document document, CFDI facturaDto) throws DocumentException {
        PdfPTable tableDocumentoRelacionado = new PdfPTable(1);
        tableDocumentoRelacionado.setWidths(new int[]{ANCHO_TOTAL__TABLA});
        tableDocumentoRelacionado.setTotalWidth(ANCHO_TOTAL__TABLA);
        tableDocumentoRelacionado.setLockedWidth(true);
        tableDocumentoRelacionado.setWidthPercentage(PORCENTAJE_ANCHO_TABLA);
        tableDocumentoRelacionado.getDefaultCell().setBorder(Rectangle.NO_BORDER);
        tableDocumentoRelacionado.setSpacingAfter(8f);

        if (facturaDto.getComplementos()!= null && facturaDto.getComplementos().getComplementoPago20() != null) {

            PdfPCell spaceComplementoPagos = new PdfPCell();
            spaceComplementoPagos.setBackgroundColor(BaseColor.BLACK);
            spaceComplementoPagos.setFixedHeight(ALTURA_FIJA_TABLA_COMPLEMENTO_PAGO);
            spaceComplementoPagos.setBorder(Rectangle.NO_BORDER);

            if (facturaDto.getComplementos().getComplementoPago20().getPago() != null && !facturaDto.getComplementos().getComplementoPago20().getPago().isEmpty()) {
                for (ComplementoPago20.Pago complementoPagoCargados : facturaDto.getComplementos().getComplementoPago20().getPago()) {

                    try {
                        PdfPCell headerNodoPago = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.C_PAGO).concat(" : "), fontEtiquetas));
                        headerNodoPago.setHorizontalAlignment(Element.ALIGN_LEFT);
                        headerNodoPago.setBorder(Rectangle.NO_BORDER);
                        tableDocumentoRelacionado.addCell(headerNodoPago);

                        PdfPTable tablePago = new PdfPTable(6);
                        tablePago.setWidths(new int[]{35, 25, 35, 25, 35, 25});
                        tablePago.setTotalWidth(ANCHO_TOTLA_SUBTABLA);
                        tablePago.setWidthPercentage(PORCENTAJE_ANCHO_TABLA);
                        tablePago.setLockedWidth(true);
                        tablePago.getDefaultCell().setBorder(Rectangle.NO_BORDER);

                        PdfPCell fechaPagoC = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.C_FECHAPAGO).concat(" : "), fontEtiquetasComplemento));
                        fechaPagoC.setHorizontalAlignment(Element.ALIGN_LEFT);
                        fechaPagoC.setBorder(Rectangle.NO_BORDER);

                        PdfPCell fechaPagoV = new PdfPCell(new Paragraph(FechasUtil.getStringAnioMesDiaHoraMinSeg(complementoPagoCargados.getFechaPago() != null ? complementoPagoCargados.getFechaPago() : new Date()), fontEtiquetasFinales));
                        fechaPagoV.setHorizontalAlignment(Element.ALIGN_LEFT);
                        fechaPagoV.setBorder(Rectangle.NO_BORDER);

                        PdfPCell formaDePagoC = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.T_FORMAPAGO).concat(" : "), fontEtiquetasComplemento));
                        formaDePagoC.setHorizontalAlignment(Element.ALIGN_LEFT);
                        formaDePagoC.setBorder(Rectangle.NO_BORDER);

                        PdfPCell formaDePagoV = new PdfPCell(new Paragraph(complementoPagoCargados.getFormaDePagoP() != null ? complementoPagoCargados.getFormaDePagoP() : "", fontEtiquetasFinales));
                        formaDePagoV.setHorizontalAlignment(Element.ALIGN_LEFT);
                        formaDePagoV.setBorder(Rectangle.NO_BORDER);

                        PdfPCell monedaPagoC = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.N_MONEDA).concat(" : "), fontEtiquetasComplemento));
                        monedaPagoC.setHorizontalAlignment(Element.ALIGN_LEFT);
                        monedaPagoC.setBorder(Rectangle.NO_BORDER);

                        PdfPCell monedaPagoV = new PdfPCell(new Paragraph(complementoPagoCargados.getMonedaP() != null ? complementoPagoCargados.getMonedaP() : "", fontEtiquetasFinales));
                        monedaPagoV.setHorizontalAlignment(Element.ALIGN_LEFT);
                        monedaPagoV.setBorder(Rectangle.NO_BORDER);

                        PdfPCell tipoCambioPagoC = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.C_TIPOCAMBIO).concat(" : "), fontEtiquetasComplemento));
                        tipoCambioPagoC.setHorizontalAlignment(Element.ALIGN_LEFT);
                        tipoCambioPagoC.setBorder(Rectangle.NO_BORDER);

                        PdfPCell tipoCambioPagoV = new PdfPCell(new Paragraph(String.valueOf(complementoPagoCargados.getTipoCambioP() != null ? complementoPagoCargados.getTipoCambioP() : ""), fontEtiquetasFinales));
                        tipoCambioPagoV.setHorizontalAlignment(Element.ALIGN_LEFT);
                        tipoCambioPagoV.setBorder(Rectangle.NO_BORDER);

                        PdfPCell montoPagoC = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.C_MONTO).concat(" : "), fontEtiquetasComplemento));
                        montoPagoC.setHorizontalAlignment(Element.ALIGN_LEFT);
                        montoPagoC.setBorder(Rectangle.NO_BORDER);

                        PdfPCell montoPagoV = new PdfPCell(new Paragraph(Utilities.bigDecimalToStr(complementoPagoCargados.getMonto() != null ? complementoPagoCargados.getMonto() : BigDecimal.ZERO), fontEtiquetasFinales));
                        montoPagoV.setHorizontalAlignment(Element.ALIGN_LEFT);
                        montoPagoV.setBorder(Rectangle.NO_BORDER);

                        PdfPCell nombreBancoOrdenantePagoC = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.C_NOMBREBANCOORDENANTE).concat(" : "), fontEtiquetasComplemento));
                        nombreBancoOrdenantePagoC.setHorizontalAlignment(Element.ALIGN_LEFT);
                        nombreBancoOrdenantePagoC.setBorder(Rectangle.NO_BORDER);

                        PdfPCell nombreBancoOrdenantePagoV = new PdfPCell(new Paragraph(complementoPagoCargados.getNomBancoOrdExt() != null ? complementoPagoCargados.getNomBancoOrdExt() : "", fontEtiquetasFinales));
                        nombreBancoOrdenantePagoV.setHorizontalAlignment(Element.ALIGN_LEFT);
                        nombreBancoOrdenantePagoV.setBorder(Rectangle.NO_BORDER);

                        PdfPCell cuentaOrdenantePagoC = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.C_CTAORDENANTE).concat(" : "), fontEtiquetasComplemento));
                        cuentaOrdenantePagoC.setHorizontalAlignment(Element.ALIGN_LEFT);
                        cuentaOrdenantePagoC.setBorder(Rectangle.NO_BORDER);

                        PdfPCell cuentaOrdenantePagoV = new PdfPCell(new Paragraph(complementoPagoCargados.getCtaOrdenante() != null ? complementoPagoCargados.getCtaOrdenante() : "", fontEtiquetasFinales));
                        cuentaOrdenantePagoV.setHorizontalAlignment(Element.ALIGN_LEFT);
                        cuentaOrdenantePagoV.setColspan(5);
                        cuentaOrdenantePagoV.setBorder(Rectangle.NO_BORDER);

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
   

                        PdfPCell impPago = new PdfPCell(new Paragraph( "" + descImpuestoP , fontEtiquetasFinales));
                        impPago.setHorizontalAlignment(Element.ALIGN_LEFT);
                        impPago.setColspan(6);
                        impPago.setBorder(Rectangle.NO_BORDER);
                      

                        tablePago.addCell(fechaPagoC);
                        tablePago.addCell(fechaPagoV);
                        tablePago.addCell(formaDePagoC);
                        tablePago.addCell(formaDePagoV);
                        tablePago.addCell(monedaPagoC);
                        tablePago.addCell(monedaPagoV);
                        tablePago.addCell(tipoCambioPagoC);
                        tablePago.addCell(tipoCambioPagoV);
                        tablePago.addCell(montoPagoC);
                        tablePago.addCell(montoPagoV);
                        tablePago.addCell(nombreBancoOrdenantePagoC);
                        tablePago.addCell(nombreBancoOrdenantePagoV);
                        tablePago.addCell(cuentaOrdenantePagoC);
                        tablePago.addCell(cuentaOrdenantePagoV);
                        tablePago.addCell(impPago);
                        tableDocumentoRelacionado.addCell(tablePago);

                        if (complementoPagoCargados.getDocumentoRelacionado() != null && !complementoPagoCargados.getDocumentoRelacionado().isEmpty()) {
                            int vueltaEncabezado = 0;
                            PdfPCell encabezadoDocRelacionado = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.C_DTOSRELACIONADOS), fontEtiquetas));
                            encabezadoDocRelacionado.setHorizontalAlignment(Element.ALIGN_LEFT);
                            encabezadoDocRelacionado.setBorder(Rectangle.NO_BORDER);
                            tableDocumentoRelacionado.addCell(encabezadoDocRelacionado);
                            for (DoctoRelacionado documentosRelacionCargados : complementoPagoCargados.getDocumentoRelacionado()) {

                                PdfPTable tableDocumentoRelacionadoPago = new PdfPTable(10);
                                                                                //uuid serie, folio, moneda
                                tableDocumentoRelacionadoPago.setWidths(new int[]{107, 46, 35, 25, 40, 40, 50, 60, 60, 60});
                                tableDocumentoRelacionadoPago.setTotalWidth(ANCHO_TOTLA_SUBTABLA);
                                tableDocumentoRelacionadoPago.setWidthPercentage(PORCENTAJE_ANCHO_TABLA);
                                tableDocumentoRelacionadoPago.setLockedWidth(true);
                                tableDocumentoRelacionadoPago.getDefaultCell().setBorder(Rectangle.NO_BORDER);

                                if (vueltaEncabezado == 0) {
                                    PdfPCell idDocumentoRelacionadoC = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.C_FOLIOUUID), fontEtiquetasComplementoPago));
                                    idDocumentoRelacionadoC.setHorizontalAlignment(Element.ALIGN_CENTER);
                                    idDocumentoRelacionadoC.setBorderColor(BaseColor.LIGHT_GRAY);
                                    idDocumentoRelacionadoC.setBorderWidthBottom(0.3f);
                                    idDocumentoRelacionadoC.setBorderWidthTop(0.3f);
                                    idDocumentoRelacionadoC.setBorderWidthRight(0.3f);
                                    idDocumentoRelacionadoC.setBorderWidthLeft(0.3f);

                                    PdfPCell serieDocumentoRelacionadoC = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.SERIE), fontEtiquetasComplementoPago));
                                    serieDocumentoRelacionadoC.setHorizontalAlignment(Element.ALIGN_CENTER);
                                    serieDocumentoRelacionadoC.setBorderColor(BaseColor.LIGHT_GRAY);
                                    serieDocumentoRelacionadoC.setBorderWidthBottom(0.3f);
                                    serieDocumentoRelacionadoC.setBorderWidthTop(0.3f);
                                    serieDocumentoRelacionadoC.setBorderWidthRight(0.3f);
                                    serieDocumentoRelacionadoC.setBorderWidthLeft(0.3f);

                                    PdfPCell folioDocumentoRelacionadoC = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.C_FOLIO), fontEtiquetasComplementoPago));
                                    folioDocumentoRelacionadoC.setHorizontalAlignment(Element.ALIGN_CENTER);
                                    folioDocumentoRelacionadoC.setBorderColor(BaseColor.LIGHT_GRAY);
                                    folioDocumentoRelacionadoC.setBorderWidthBottom(0.3f);
                                    folioDocumentoRelacionadoC.setBorderWidthTop(0.3f);
                                    folioDocumentoRelacionadoC.setBorderWidthRight(0.3f);
                                    folioDocumentoRelacionadoC.setBorderWidthLeft(0.3f);

                                    PdfPCell monedaDocumentoRelacionadoC = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.N_MONEDA), fontEtiquetasComplementoPago));
                                    monedaDocumentoRelacionadoC.setHorizontalAlignment(Element.ALIGN_CENTER);
                                    monedaDocumentoRelacionadoC.setBorderColor(BaseColor.LIGHT_GRAY);
                                    monedaDocumentoRelacionadoC.setBorderWidthBottom(0.3f);
                                    monedaDocumentoRelacionadoC.setBorderWidthTop(0.3f);
                                    monedaDocumentoRelacionadoC.setBorderWidthRight(0.3f);
                                    monedaDocumentoRelacionadoC.setBorderWidthLeft(0.3f);

                                    PdfPCell tipoDeCambioDocumentoRelacionadoC = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.EQUIVALENCIA_DR), fontEtiquetasComplementoPago));
                                    tipoDeCambioDocumentoRelacionadoC.setHorizontalAlignment(Element.ALIGN_CENTER);
                                    tipoDeCambioDocumentoRelacionadoC.setBorderColor(BaseColor.LIGHT_GRAY);
                                    tipoDeCambioDocumentoRelacionadoC.setBorderWidthBottom(0.3f);
                                    tipoDeCambioDocumentoRelacionadoC.setBorderWidthTop(0.3f);
                                    tipoDeCambioDocumentoRelacionadoC.setBorderWidthRight(0.3f);
                                    tipoDeCambioDocumentoRelacionadoC.setBorderWidthLeft(0.3f);

                                    PdfPCell metodoPagoDocumentoRelacionadoC = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.T_METODOPAGO), fontEtiquetasComplementoPago));
                                    metodoPagoDocumentoRelacionadoC.setHorizontalAlignment(Element.ALIGN_CENTER);
                                    metodoPagoDocumentoRelacionadoC.setBorderColor(BaseColor.LIGHT_GRAY);
                                    metodoPagoDocumentoRelacionadoC.setBorderWidthBottom(0.3f);
                                    metodoPagoDocumentoRelacionadoC.setBorderWidthTop(0.3f);
                                    metodoPagoDocumentoRelacionadoC.setBorderWidthRight(0.3f);
                                    metodoPagoDocumentoRelacionadoC.setBorderWidthLeft(0.3f);

                                    PdfPCell numeroParcialidaDocumentoRelacionadoC = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.C_NUMEROPARCIALIDADES), fontEtiquetasComplementoPago));
                                    numeroParcialidaDocumentoRelacionadoC.setHorizontalAlignment(Element.ALIGN_CENTER);
                                    numeroParcialidaDocumentoRelacionadoC.setBorderColor(BaseColor.LIGHT_GRAY);
                                    numeroParcialidaDocumentoRelacionadoC.setBorderWidthBottom(0.3f);
                                    numeroParcialidaDocumentoRelacionadoC.setBorderWidthTop(0.3f);
                                    numeroParcialidaDocumentoRelacionadoC.setBorderWidthRight(0.3f);
                                    numeroParcialidaDocumentoRelacionadoC.setBorderWidthLeft(0.3f);

                                    PdfPCell impPagadoDocumentoRelacionadoC = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.C_IMPORTEPAGADO), fontEtiquetasComplementoPago));
                                    impPagadoDocumentoRelacionadoC.setHorizontalAlignment(Element.ALIGN_CENTER);
                                    impPagadoDocumentoRelacionadoC.setBorderColor(BaseColor.LIGHT_GRAY);
                                    impPagadoDocumentoRelacionadoC.setBorderWidthBottom(0.3f);
                                    impPagadoDocumentoRelacionadoC.setBorderWidthTop(0.3f);
                                    impPagadoDocumentoRelacionadoC.setBorderWidthRight(0.3f);
                                    impPagadoDocumentoRelacionadoC.setBorderWidthLeft(0.3f);

                                    PdfPCell importeSaldoAntDocumentoRelacionadoC = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.C_IMPORTESALDOANT), fontEtiquetasComplementoPago));
                                    importeSaldoAntDocumentoRelacionadoC.setHorizontalAlignment(Element.ALIGN_CENTER);
                                    importeSaldoAntDocumentoRelacionadoC.setBorderColor(BaseColor.LIGHT_GRAY);
                                    importeSaldoAntDocumentoRelacionadoC.setBorderWidthBottom(0.3f);
                                    importeSaldoAntDocumentoRelacionadoC.setBorderWidthTop(0.3f);
                                    importeSaldoAntDocumentoRelacionadoC.setBorderWidthRight(0.3f);
                                    importeSaldoAntDocumentoRelacionadoC.setBorderWidthLeft(0.3f);

                                    PdfPCell importeSaldoInsoDocumentoRelacionadoC = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.C_IMPORTESALDOINS), fontEtiquetasComplementoPago));
                                    importeSaldoInsoDocumentoRelacionadoC.setHorizontalAlignment(Element.ALIGN_CENTER);
                                    importeSaldoInsoDocumentoRelacionadoC.setBorderColor(BaseColor.LIGHT_GRAY);
                                    importeSaldoInsoDocumentoRelacionadoC.setBorderWidthBottom(0.3f);
                                    importeSaldoInsoDocumentoRelacionadoC.setBorderWidthTop(0.3f);
                                    importeSaldoInsoDocumentoRelacionadoC.setBorderWidthRight(0.3f);
                                    importeSaldoInsoDocumentoRelacionadoC.setBorderWidthLeft(0.3f);

                                    PdfPCell impDocumentoRelacionadoC = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.IMPUESTO_L), fontEtiquetasComplementoPago));
                                    impDocumentoRelacionadoC.setHorizontalAlignment(Element.ALIGN_CENTER);
                                    impDocumentoRelacionadoC.setBorderColor(BaseColor.LIGHT_GRAY);
                                    impDocumentoRelacionadoC.setBorderWidthBottom(0.3f);
                                    impDocumentoRelacionadoC.setBorderWidthTop(0.3f);
                                    impDocumentoRelacionadoC.setBorderWidthRight(0.3f);
                                    impDocumentoRelacionadoC.setBorderWidthLeft(0.3f);

                                    tableDocumentoRelacionadoPago.addCell(idDocumentoRelacionadoC);
                                    tableDocumentoRelacionadoPago.addCell(serieDocumentoRelacionadoC);
                                    tableDocumentoRelacionadoPago.addCell(folioDocumentoRelacionadoC);
                                    tableDocumentoRelacionadoPago.addCell(monedaDocumentoRelacionadoC);
                                    tableDocumentoRelacionadoPago.addCell(tipoDeCambioDocumentoRelacionadoC);
                                    // tableDocumentoRelacionadoPago.addCell(metodoPagoDocumentoRelacionadoC);
                                    tableDocumentoRelacionadoPago.addCell(numeroParcialidaDocumentoRelacionadoC);
                                    tableDocumentoRelacionadoPago.addCell(importeSaldoAntDocumentoRelacionadoC);
                                    tableDocumentoRelacionadoPago.addCell(impPagadoDocumentoRelacionadoC);
                                    tableDocumentoRelacionadoPago.addCell(importeSaldoInsoDocumentoRelacionadoC);
                                    tableDocumentoRelacionadoPago.addCell(impDocumentoRelacionadoC);
                                }

                                PdfPCell idDocumentoRelacionadoV = new PdfPCell(new Paragraph(documentosRelacionCargados.getIdDocumento() != null ? documentosRelacionCargados.getIdDocumento() : "", fontContenidoComplemento));
                                idDocumentoRelacionadoV.setHorizontalAlignment(Element.ALIGN_CENTER);
                                idDocumentoRelacionadoV.setBorderColor(BaseColor.LIGHT_GRAY);
                                idDocumentoRelacionadoV.setBorderWidthBottom(0.3f);
                                idDocumentoRelacionadoV.setBorderWidthTop(0.3f);
                                idDocumentoRelacionadoV.setBorderWidthRight(0.3f);
                                idDocumentoRelacionadoV.setBorderWidthLeft(0.3f);

                                PdfPCell serieDocumentoRelacionadoV = new PdfPCell(new Paragraph(documentosRelacionCargados.getSerie() != null ? documentosRelacionCargados.getSerie() : "", fontContenidoComplemento));
                                serieDocumentoRelacionadoV.setHorizontalAlignment(Element.ALIGN_CENTER);
                                serieDocumentoRelacionadoV.setBorderColor(BaseColor.LIGHT_GRAY);
                                serieDocumentoRelacionadoV.setBorderWidthBottom(0.3f);
                                serieDocumentoRelacionadoV.setBorderWidthTop(0.3f);
                                serieDocumentoRelacionadoV.setBorderWidthRight(0.3f);
                                serieDocumentoRelacionadoV.setBorderWidthLeft(0.3f);

                                PdfPCell folioDocumentoRelacionadoV = new PdfPCell(new Paragraph(documentosRelacionCargados.getFolio() != null ? documentosRelacionCargados.getFolio() : "", fontContenidoComplemento));
                                folioDocumentoRelacionadoV.setHorizontalAlignment(Element.ALIGN_CENTER);
                                folioDocumentoRelacionadoV.setBorderColor(BaseColor.LIGHT_GRAY);
                                folioDocumentoRelacionadoV.setBorderWidthBottom(0.3f);
                                folioDocumentoRelacionadoV.setBorderWidthTop(0.3f);
                                folioDocumentoRelacionadoV.setBorderWidthRight(0.3f);
                                folioDocumentoRelacionadoV.setBorderWidthLeft(0.3f);

                                PdfPCell monedaDocumentoRelacionadoV = new PdfPCell(new Paragraph(documentosRelacionCargados.getMonedaDR() != null ? documentosRelacionCargados.getMonedaDR() : "", fontContenidoComplemento));
                                monedaDocumentoRelacionadoV.setHorizontalAlignment(Element.ALIGN_CENTER);
                                monedaDocumentoRelacionadoV.setBorderColor(BaseColor.LIGHT_GRAY);
                                monedaDocumentoRelacionadoV.setBorderWidthBottom(0.3f);
                                monedaDocumentoRelacionadoV.setBorderWidthTop(0.3f);
                                monedaDocumentoRelacionadoV.setBorderWidthRight(0.3f);
                                monedaDocumentoRelacionadoV.setBorderWidthLeft(0.3f);

                                PdfPCell tipoDeCambioDocumentoRelacionadoV = new PdfPCell(new Paragraph(Optional.ofNullable(documentosRelacionCargados.getEquivalenciaDR()).orElse(BigDecimal.ZERO).toPlainString(), fontContenidoComplemento));
                                tipoDeCambioDocumentoRelacionadoV.setHorizontalAlignment(Element.ALIGN_CENTER);
                                tipoDeCambioDocumentoRelacionadoV.setBorderColor(BaseColor.LIGHT_GRAY);
                                tipoDeCambioDocumentoRelacionadoV.setBorderWidthBottom(0.3f);
                                tipoDeCambioDocumentoRelacionadoV.setBorderWidthTop(0.3f);
                                tipoDeCambioDocumentoRelacionadoV.setBorderWidthRight(0.3f);
                                tipoDeCambioDocumentoRelacionadoV.setBorderWidthLeft(0.3f);


                                PdfPCell numeroParcialidaDocumentoRelacionadoV = new PdfPCell(new Paragraph(String.valueOf(documentosRelacionCargados.getNumParcialidad() != null ? documentosRelacionCargados.getNumParcialidad() : ""), fontContenidoComplemento));
                                numeroParcialidaDocumentoRelacionadoV.setHorizontalAlignment(Element.ALIGN_CENTER);
                                numeroParcialidaDocumentoRelacionadoV.setBorderColor(BaseColor.LIGHT_GRAY);
                                numeroParcialidaDocumentoRelacionadoV.setBorderWidthBottom(0.3f);
                                numeroParcialidaDocumentoRelacionadoV.setBorderWidthTop(0.3f);
                                numeroParcialidaDocumentoRelacionadoV.setBorderWidthRight(0.3f);
                                numeroParcialidaDocumentoRelacionadoV.setBorderWidthLeft(0.3f);

                                PdfPCell impPagadoDocumentoRelacionadoV = new PdfPCell(new Paragraph(Utilities.bigDecimalToStr(documentosRelacionCargados.getImpPagado() != null ? documentosRelacionCargados.getImpPagado() : BigDecimal.ZERO), fontContenidoComplemento));
                                impPagadoDocumentoRelacionadoV.setHorizontalAlignment(Element.ALIGN_CENTER);
                                impPagadoDocumentoRelacionadoV.setBorderColor(BaseColor.LIGHT_GRAY);
                                impPagadoDocumentoRelacionadoV.setBorderWidthBottom(0.3f);
                                impPagadoDocumentoRelacionadoV.setBorderWidthTop(0.3f);
                                impPagadoDocumentoRelacionadoV.setBorderWidthRight(0.3f);
                                impPagadoDocumentoRelacionadoV.setBorderWidthLeft(0.3f);

                                PdfPCell importeSaldoAntDocumentoRelacionadoV = new PdfPCell(new Paragraph(Utilities.bigDecimalToStr(documentosRelacionCargados.getImpSaldoAnt() != null ? documentosRelacionCargados.getImpSaldoAnt() : BigDecimal.ZERO), fontContenidoComplemento));
                                importeSaldoAntDocumentoRelacionadoV.setHorizontalAlignment(Element.ALIGN_CENTER);
                                importeSaldoAntDocumentoRelacionadoV.setBorderColor(BaseColor.LIGHT_GRAY);
                                importeSaldoAntDocumentoRelacionadoV.setBorderWidthBottom(0.3f);
                                importeSaldoAntDocumentoRelacionadoV.setBorderWidthTop(0.3f);
                                importeSaldoAntDocumentoRelacionadoV.setBorderWidthRight(0.3f);
                                importeSaldoAntDocumentoRelacionadoV.setBorderWidthLeft(0.3f);

                                PdfPCell importeSaldoInsoDocumentoRelacionadoV = new PdfPCell(new Paragraph(Utilities.bigDecimalToStr(documentosRelacionCargados.getImpSaldoInsoluto() != null ? documentosRelacionCargados.getImpSaldoInsoluto() : BigDecimal.ZERO), fontContenidoComplemento));
                                importeSaldoInsoDocumentoRelacionadoV.setHorizontalAlignment(Element.ALIGN_CENTER);
                                importeSaldoInsoDocumentoRelacionadoV.setBorderColor(BaseColor.LIGHT_GRAY);
                                importeSaldoInsoDocumentoRelacionadoV.setBorderWidthBottom(0.3f);
                                importeSaldoInsoDocumentoRelacionadoV.setBorderWidthTop(0.3f);
                                importeSaldoInsoDocumentoRelacionadoV.setBorderWidthRight(0.3f);
                                importeSaldoInsoDocumentoRelacionadoV.setBorderWidthLeft(0.3f);

                                tableDocumentoRelacionadoPago.addCell(idDocumentoRelacionadoV);
                                tableDocumentoRelacionadoPago.addCell(serieDocumentoRelacionadoV);
                                tableDocumentoRelacionadoPago.addCell(folioDocumentoRelacionadoV);
                                tableDocumentoRelacionadoPago.addCell(monedaDocumentoRelacionadoV);
                                tableDocumentoRelacionadoPago.addCell(tipoDeCambioDocumentoRelacionadoV);
                                // tableDocumentoRelacionadoPago.addCell(metodoPagoDocumentoRelacionadoV);
                                tableDocumentoRelacionadoPago.addCell(numeroParcialidaDocumentoRelacionadoV);
                                tableDocumentoRelacionadoPago.addCell(importeSaldoAntDocumentoRelacionadoV);
                                tableDocumentoRelacionadoPago.addCell(impPagadoDocumentoRelacionadoV);
                                tableDocumentoRelacionadoPago.addCell(importeSaldoInsoDocumentoRelacionadoV);

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
                                PdfPCell impPagoDR = new PdfPCell(new Paragraph( "" + descImpuestoDR , fontContenidoComplemento));
                                impPagoDR.setHorizontalAlignment(Element.ALIGN_CENTER);
                                impPagoDR.setBorderColor(BaseColor.LIGHT_GRAY);
                                impPagoDR.setBorderWidthBottom(0.3f);
                                impPagoDR.setBorderWidthTop(0.3f);
                                impPagoDR.setBorderWidthRight(0.3f);
                                impPagoDR.setBorderWidthLeft(0.3f);
                                
                                tableDocumentoRelacionadoPago.addCell(impPagoDR);

                                tableDocumentoRelacionado.addCell(tableDocumentoRelacionadoPago);
                                vueltaEncabezado += 1;
                            }
                        }
                    } catch (Exception ex) {
                        Logger.getLogger(PdfFacturaTenantAServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
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
                tableTotales.setTotalWidth(ANCHO_TOTAL__TABLA);
                tableTotales.setWidthPercentage(PORCENTAJE_ANCHO_TABLA);
                tableTotales.setLockedWidth(true);
                tableTotales.getDefaultCell().setFixedHeight(80f);
                tableTotales.getDefaultCell().setBorder(Rectangle.NO_BORDER);

                if (facturaDto.getComplementos().getComplementoPago20().getTotales().getTotalRetencionesIVA() != null
                        && facturaDto.getComplementos().getComplementoPago20().getTotales().getTotalRetencionesIVA().compareTo(BigDecimal.ZERO) > 0 ) {

                    PdfPCell totalRetencionesIVA = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.TOTAL_RETENCIONES_IVA).concat(" : ").
                            concat(Utilities.bigDecimalToStr(Optional.ofNullable(facturaDto.getComplementos().getComplementoPago20().getTotales().getTotalRetencionesIVA()).orElse(BigDecimal.ZERO))), fontContenido));
                    totalRetencionesIVA.setColspan(3);
                    totalRetencionesIVA.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    totalRetencionesIVA.setBorder(Rectangle.NO_BORDER);
                    tableTotales.addCell(totalRetencionesIVA);
                }

                if (facturaDto.getComplementos().getComplementoPago20().getTotales().getTotalRetencionesISR() != null
                        && facturaDto.getComplementos().getComplementoPago20().getTotales().getTotalRetencionesISR().compareTo(BigDecimal.ZERO) > 0 ) {
                    PdfPCell totalRetencionesISR = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.TOTAL_RETENCIONES_ISR).concat(" : ")
                            .concat(Utilities.bigDecimalToStr(Optional.ofNullable(facturaDto.getComplementos().getComplementoPago20().getTotales().getTotalRetencionesISR()).orElse(BigDecimal.ZERO))), fontContenido
                    )
                    );
                    totalRetencionesISR.setColspan(3);
                    totalRetencionesISR.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    totalRetencionesISR.setBorder(Rectangle.NO_BORDER);
                    tableTotales.addCell(totalRetencionesISR);
                }


                if (facturaDto.getComplementos().getComplementoPago20().getTotales().getTotalRetencionesIEPS() != null
                        && facturaDto.getComplementos().getComplementoPago20().getTotales().getTotalRetencionesIEPS().compareTo(BigDecimal.ZERO) > 0 ) {
                    PdfPCell totalRetencionesIEPS = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.TOTAL_RETENCIONES_IEPS).concat(" : ")
                            .concat(Utilities.bigDecimalToStr(Optional.ofNullable(facturaDto.getComplementos().getComplementoPago20().getTotales().getTotalRetencionesIEPS()).orElse(BigDecimal.ZERO))), fontContenido
                    )
                    );
                    totalRetencionesIEPS.setColspan(3);
                    totalRetencionesIEPS.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    totalRetencionesIEPS.setBorder(Rectangle.NO_BORDER);
                    tableTotales.addCell(totalRetencionesIEPS);
                }


                if (facturaDto.getComplementos()
                        .getComplementoPago20().getTotales().getTotalTrasladosBaseIVA16() != null
                        && facturaDto.getComplementos().getComplementoPago20().getTotales().getTotalTrasladosBaseIVA16().compareTo(BigDecimal.ZERO) > 0 ) {
                    PdfPCell totalTrasladosBaseIVA16 = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.TOTAL_TRASLADOS_BASE_IVA_16).concat(" : ").concat(Utilities.bigDecimalToStr(Optional.ofNullable(facturaDto.getComplementos()
                            .getComplementoPago20().getTotales().getTotalTrasladosBaseIVA16()).orElse(BigDecimal.ZERO))), fontContenido
                    )
                    );
                    totalTrasladosBaseIVA16.setColspan(3);
                    totalTrasladosBaseIVA16.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    totalTrasladosBaseIVA16.setBorder(Rectangle.NO_BORDER);
                    tableTotales.addCell(totalTrasladosBaseIVA16);
                }

                if (facturaDto.getComplementos()
                        .getComplementoPago20().getTotales().getTotalTrasladosImpuestoIVA16() != null
                        && facturaDto.getComplementos().getComplementoPago20().getTotales().getTotalTrasladosImpuestoIVA16().compareTo(BigDecimal.ZERO) > 0 ) {
                    PdfPCell totalTrasladosImpuestoIVA16 = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.TOTAL_TRASLADOS_IMPUESTO_IVA_16).concat(" : ").concat(Utilities.bigDecimalToStr(Optional.ofNullable(facturaDto.getComplementos()
                            .getComplementoPago20().getTotales().getTotalTrasladosImpuestoIVA16()).orElse(BigDecimal.ZERO))), fontContenido
                    )
                    );
                    totalTrasladosImpuestoIVA16.setColspan(3);
                    totalTrasladosImpuestoIVA16.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    totalTrasladosImpuestoIVA16.setBorder(Rectangle.NO_BORDER);
                    tableTotales.addCell(totalTrasladosImpuestoIVA16);
                }


                if (facturaDto.getComplementos().getComplementoPago20().getTotales().getTotalTrasladosBaseIVA8() != null
                        && facturaDto.getComplementos().getComplementoPago20().getTotales().getTotalTrasladosBaseIVA8().compareTo(BigDecimal.ZERO) > 0 ) {
                    PdfPCell totalTrasladosBaseIVA8 = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.TOTAL_TRASLADOS_BASE_IVA8).concat(" : ")
                            .concat(Utilities.bigDecimalToStr(Optional.ofNullable(facturaDto.getComplementos().getComplementoPago20().getTotales().getTotalTrasladosBaseIVA8()).orElse(BigDecimal.ZERO))), fontContenido
                    )
                    );
                    totalTrasladosBaseIVA8.setColspan(3);
                    totalTrasladosBaseIVA8.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    totalTrasladosBaseIVA8.setBorder(Rectangle.NO_BORDER);
                    tableTotales.addCell(totalTrasladosBaseIVA8);
                }


                if (facturaDto.getComplementos().getComplementoPago20().getTotales().getTotalTrasladosImpuestoIVA8() != null
                        && facturaDto.getComplementos().getComplementoPago20().getTotales().getTotalTrasladosImpuestoIVA8().compareTo(BigDecimal.ZERO) > 0 ) {
                    PdfPCell totalTrasladosImpuestoIVA8 = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.TOTAL_TRASLADOS_IMPUESTO_IVA_8).concat(" : ")
                            .concat(Utilities.bigDecimalToStr(Optional.ofNullable(facturaDto.getComplementos().getComplementoPago20().getTotales().getTotalTrasladosImpuestoIVA8()).orElse(BigDecimal.ZERO))), fontContenido
                    )
                    );
                    totalTrasladosImpuestoIVA8.setColspan(3);
                    totalTrasladosImpuestoIVA8.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    totalTrasladosImpuestoIVA8.setBorder(Rectangle.NO_BORDER);
                    tableTotales.addCell(totalTrasladosImpuestoIVA8);
                }

                if (facturaDto.getComplementos().getComplementoPago20().getTotales().getTotalTrasladosBaseIVA0() != null
                        && facturaDto.getComplementos().getComplementoPago20().getTotales().getTotalTrasladosBaseIVA0().compareTo(BigDecimal.ZERO) > 0 ) {
                    PdfPCell totalTrasladosBaseIVA0 = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.TOTAL_TRASLADOS_BASE_IVA_0).concat(" : ")
                            .concat(Utilities.bigDecimalToStr(Optional.ofNullable(facturaDto.getComplementos().getComplementoPago20().getTotales().getTotalTrasladosBaseIVA0()).orElse(BigDecimal.ZERO))), fontContenido
                    )
                    );
                    totalTrasladosBaseIVA0.setColspan(3);
                    totalTrasladosBaseIVA0.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    totalTrasladosBaseIVA0.setBorder(Rectangle.NO_BORDER);
                    tableTotales.addCell(totalTrasladosBaseIVA0);
                }


                if (facturaDto.getComplementos().getComplementoPago20().getTotales().getTotalTrasladosImpuestoIVA0() != null
                        && facturaDto.getComplementos().getComplementoPago20().getTotales().getTotalTrasladosImpuestoIVA0().compareTo(BigDecimal.ZERO) > 0 ) {


                    PdfPCell totalTrasladosImpuestoIVA0 = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.TOTAL_TRASLADOS_IMPUESTO_IVA_0).concat(" : ")
                            .concat(Utilities.bigDecimalToStr(Optional.ofNullable(facturaDto.getComplementos().getComplementoPago20().getTotales().getTotalTrasladosImpuestoIVA0()).orElse(BigDecimal.ZERO))), fontContenido
                    )
                    );
                    totalTrasladosImpuestoIVA0.setColspan(3);
                    totalTrasladosImpuestoIVA0.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    totalTrasladosImpuestoIVA0.setBorder(Rectangle.NO_BORDER);
                    tableTotales.addCell(totalTrasladosImpuestoIVA0);
                }


                if (facturaDto.getComplementos().getComplementoPago20().getTotales().getTotalTrasladosBaseIVAExento() != null
                        && facturaDto.getComplementos().getComplementoPago20().getTotales().getTotalTrasladosBaseIVAExento().compareTo(BigDecimal.ZERO) > 0 ) {
                    PdfPCell totalTrasladosBaseIVAExento = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.TOTAL_TRASLADOS_BASE_IVA_EXENTO).concat(" : ")
                            .concat(Utilities.bigDecimalToStr(Optional.ofNullable(facturaDto.getComplementos().getComplementoPago20().getTotales().getTotalTrasladosBaseIVAExento()).orElse(BigDecimal.ZERO))), fontContenido
                    )
                    );
                    totalTrasladosBaseIVAExento.setColspan(3);
                    totalTrasladosBaseIVAExento.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    totalTrasladosBaseIVAExento.setBorder(Rectangle.NO_BORDER);
                    tableTotales.addCell(totalTrasladosBaseIVAExento);
                }


                if (facturaDto.getComplementos().getComplementoPago20().getTotales().getMontoTotalPagos() != null
                        && facturaDto.getComplementos().getComplementoPago20().getTotales().getMontoTotalPagos().compareTo(BigDecimal.ZERO) > 0 ) {
                    PdfPCell montoTotalPagos = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.MONTO_TOTAL_PAGOS).concat(" : ")
                            .concat(Utilities.bigDecimalToStr(Optional.ofNullable(facturaDto.getComplementos().getComplementoPago20().getTotales().getMontoTotalPagos()).orElse(BigDecimal.ZERO))), fontContenido
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
     * Metodo para agregar los sellos
     *
     * @param document
     * @return Tabla Sellos
     * @throws DocumentException
     */
    public void agregarSellos(Document document, CFDI facturaDto) throws DocumentException {
        PdfPTable tableSellos = new PdfPTable(1);
        tableSellos.setWidths(new int[]{170});
        tableSellos.setTotalWidth(420);
        tableSellos.setLockedWidth(true);
        tableSellos.setWidthPercentage(PORCENTAJE_ANCHO_TABLA);
        tableSellos.getDefaultCell().setFixedHeight(ALTURA_FIJA_TABLA);
        tableSellos.getDefaultCell().setBorder(Rectangle.NO_BORDER);
        tableSellos.setSpacingAfter(8f);

        PdfPCell selloEmisorC = new PdfPCell(new Phrase(hmapTagIdioma.get(EnumTagPlantilla.S_SD).concat(" : "), fontEtiquetas));
        selloEmisorC.setHorizontalAlignment(Element.ALIGN_LEFT);
        selloEmisorC.setBorder(Rectangle.NO_BORDER);

        PdfPCell selloEmisorV = new PdfPCell(new Phrase(facturaDto.getTimbrado().getSelloCFDI(), fontSellos));
        selloEmisorV.setHorizontalAlignment(Element.ALIGN_LEFT);
        selloEmisorV.setBorder(Rectangle.NO_BORDER);

        PdfPCell cadenaOriginalC = new PdfPCell(new Phrase(hmapTagIdioma.get(EnumTagPlantilla.S_CADENACDSAT).concat(" : "), fontEtiquetas));
        cadenaOriginalC.setHorizontalAlignment(Element.ALIGN_LEFT);
        cadenaOriginalC.setBorder(Rectangle.NO_BORDER);

        PdfPCell cadenaOriginalV = new PdfPCell(new Phrase(facturaDto.getTimbrado().getCadenaDatosTimbrado(), fontSellos));
        cadenaOriginalV.setHorizontalAlignment(Element.ALIGN_LEFT);
        cadenaOriginalV.setBorder(Rectangle.NO_BORDER);

        PdfPCell selloSATC = new PdfPCell(new Phrase(hmapTagIdioma.get(EnumTagPlantilla.S_SDCFDI).concat(" : "), fontEtiquetas));
        selloSATC.setHorizontalAlignment(Element.ALIGN_LEFT);
        selloSATC.setBorder(Rectangle.NO_BORDER);

        PdfPCell selloSATV = new PdfPCell(new Phrase(facturaDto.getTimbrado().getSelloSAT(), fontSellos));
        selloSATV.setHorizontalAlignment(Element.ALIGN_LEFT);
        selloSATV.setBorder(Rectangle.NO_BORDER);

        tableSellos.addCell(selloEmisorC);
        tableSellos.addCell(selloEmisorV);
        tableSellos.addCell(cadenaOriginalC);
        tableSellos.addCell(cadenaOriginalV);
        tableSellos.addCell(selloSATC);
        tableSellos.addCell(selloSATV);

        document.add(tableSellos);

    }

    /**
     * Metodo para agregar los datos de timbrado, No de Certificado SAT.
     *
     * @param document
     * @return Tabla DatosFinales
     * @throws DocumentException
     */
    public void agregarDatosFinales(Document document, CFDI facturaDto) throws DocumentException {
        try {
            PdfPTable tableDatosFinales = new PdfPTable(2);
            tableDatosFinales.setWidths(new int[]{85, 85});
            tableDatosFinales.setTotalWidth(420);
            tableDatosFinales.setLockedWidth(true);
            tableDatosFinales.setWidthPercentage(PORCENTAJE_ANCHO_TABLA);
            tableDatosFinales.getDefaultCell().setFixedHeight(ALTURA_FIJA_TABLA);
            tableDatosFinales.getDefaultCell().setBorder(Rectangle.NO_BORDER);
            tableDatosFinales.setSpacingAfter(8f);

            PdfPCell folioFiscalV = new PdfPCell(new Phrase(facturaDto.getTimbrado().getUuid(), fontEtiquetasFinales));
            folioFiscalV.setHorizontalAlignment(Element.ALIGN_LEFT);
            folioFiscalV.setBorder(Rectangle.NO_BORDER);

            PdfPCell cellEmisionFechaCertificacion = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.N_FECHAHORACERTIFICACION).concat(" : "), fontEtiquetasFinalesBold));
            cellEmisionFechaCertificacion.setHorizontalAlignment(Element.ALIGN_LEFT);
            cellEmisionFechaCertificacion.setBorder(Rectangle.NO_BORDER);

            PdfPCell cellEmisionFechaCertificacionV = new PdfPCell(new Paragraph(FechasUtil.getStringMesDiaAnioHoraMinSeg(facturaDto.getTimbrado() != null && facturaDto.getTimbrado().getFechaTimbrado() != null ? facturaDto.getTimbrado().getFechaTimbrado() : new Date()), fontEtiquetasFinales));
            cellEmisionFechaCertificacionV.setHorizontalAlignment(Element.ALIGN_LEFT);
            cellEmisionFechaCertificacionV.setBorder(Rectangle.NO_BORDER);

            PdfPCell cellEmisionCertificadoEmisor = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.N_NOSERIEEMISOR).concat(" : "), fontEtiquetasFinalesBold));
            cellEmisionCertificadoEmisor.setHorizontalAlignment(Element.ALIGN_LEFT);
            cellEmisionCertificadoEmisor.setBorder(Rectangle.NO_BORDER);

            PdfPCell cellEmisionCertificadoEmisorV = new PdfPCell(new Paragraph(facturaDto.getNumeroCertificado()  != null ? facturaDto.getNumeroCertificado()  : "", fontEtiquetasFinales));
            cellEmisionCertificadoEmisorV.setHorizontalAlignment(Element.ALIGN_LEFT);
            cellEmisionCertificadoEmisorV.setBorder(Rectangle.NO_BORDER);

            PdfPCell cellEmisionCertificadoSAT = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.E_NOCERTSAT).concat(" : "), fontEtiquetasFinalesBold));
            cellEmisionCertificadoSAT.setHorizontalAlignment(Element.ALIGN_LEFT);
            cellEmisionCertificadoSAT.setBorder(Rectangle.NO_BORDER);

            PdfPCell cellEmisionCertificadoSATV = new PdfPCell(new Paragraph(facturaDto.getTimbrado().getNumCertificadoSAT(), fontEtiquetasFinales));
            cellEmisionCertificadoSATV.setHorizontalAlignment(Element.ALIGN_LEFT);
            cellEmisionCertificadoSATV.setBorder(Rectangle.NO_BORDER);

            PdfPCell cellEmisionCondiciones = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.G_CONDICIONESPAGO).concat(" : "), fontEtiquetasFinalesBold));
            cellEmisionCondiciones.setHorizontalAlignment(Element.ALIGN_LEFT);
            cellEmisionCondiciones.setBorder(Rectangle.NO_BORDER);

            PdfPCell cellEmisionCondicionesV = new PdfPCell(new Paragraph(facturaDto.getCondicionesDePago() != null ? facturaDto.getCondicionesDePago() : "", fontEtiquetasFinales));
            cellEmisionCondicionesV.setHorizontalAlignment(Element.ALIGN_LEFT);
            cellEmisionCondicionesV.setBorder(Rectangle.NO_BORDER);

            PdfPCell cellEmisionMetodoPago = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.T_METODOPAGO).concat(" : "), fontEtiquetasFinalesBold));
            cellEmisionMetodoPago.setHorizontalAlignment(Element.ALIGN_LEFT);
            cellEmisionMetodoPago.setBorder(Rectangle.NO_BORDER);

            PdfPCell cellEmisionMetodoPagoV = new PdfPCell(new Paragraph(facturaDto.getMetodoPago() != null ? facturaDto.getMetodoPago() : "", fontEtiquetasFinales));
            cellEmisionMetodoPagoV.setHorizontalAlignment(Element.ALIGN_LEFT);
            cellEmisionMetodoPagoV.setBorder(Rectangle.NO_BORDER);

            PdfPCell cellEmisionFormaPago = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.T_FORMAPAGO).concat(" : "), fontEtiquetasFinalesBold));
            cellEmisionFormaPago.setHorizontalAlignment(Element.ALIGN_LEFT);
            cellEmisionFormaPago.setBorder(Rectangle.NO_BORDER);

            if (hmapTagIdioma.get(EnumTagPlantilla.E_FOLIOFISCAL) != null) {
                PdfPCell folioFiscalC = new PdfPCell(new Phrase(hmapTagIdioma.get(EnumTagPlantilla.E_FOLIOFISCAL).concat(" : "), fontEtiquetasFinalesBold));
                folioFiscalC.setHorizontalAlignment(Element.ALIGN_LEFT);
                folioFiscalC.setBorder(Rectangle.NO_BORDER);
                tableDatosFinales.addCell(folioFiscalC);
            } else {
                PdfPCell folioFiscalC = new PdfPCell(new Phrase("Folio Fiscal".concat(" : "), fontEtiquetasFinalesBold));
                folioFiscalC.setHorizontalAlignment(Element.ALIGN_LEFT);
                folioFiscalC.setBorder(Rectangle.NO_BORDER);
                tableDatosFinales.addCell(folioFiscalC);
            }


            PdfPCell cellEmisionCertificadoEmisor2 = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.N_NOSERIEEMISOR).concat(" : "), fontEtiquetasFinalesBold));
            cellEmisionCertificadoEmisor2.setHorizontalAlignment(Element.ALIGN_LEFT);
            cellEmisionCertificadoEmisor2.setBorder(Rectangle.NO_BORDER);

            PdfPCell cellEmisionCertificadoEmisorV2 = new PdfPCell(new Paragraph(facturaDto.getNumeroCertificado()  != null ? facturaDto.getNumeroCertificado()  : "", fontEtiquetasFinales));
            cellEmisionCertificadoEmisorV2.setHorizontalAlignment(Element.ALIGN_LEFT);
            cellEmisionCertificadoEmisorV2.setBorder(Rectangle.NO_BORDER);

            PdfPCell exportacionC = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.E_EXPORTACION).concat(" : "), fontEtiquetasFinalesBold));
            exportacionC.setHorizontalAlignment(Element.ALIGN_LEFT);
            exportacionC.setBorder(Rectangle.NO_BORDER);

            PdfPCell exportacion = new PdfPCell(new Paragraph(facturaDto.getExportacion() != null ? facturaDto.getExportacion() : "", fontEtiquetasFinales));
            exportacion.setHorizontalAlignment(Element.ALIGN_LEFT);
            exportacion.setBorder(Rectangle.NO_BORDER);

            tableDatosFinales.addCell(folioFiscalV);
            tableDatosFinales.addCell(cellEmisionFechaCertificacion);
            tableDatosFinales.addCell(cellEmisionFechaCertificacionV);
            tableDatosFinales.addCell(cellEmisionCertificadoEmisor);
            tableDatosFinales.addCell(cellEmisionCertificadoEmisorV);
            tableDatosFinales.addCell(cellEmisionCertificadoSAT);
            tableDatosFinales.addCell(cellEmisionCertificadoSATV);
            tableDatosFinales.addCell(cellEmisionCondiciones);
            tableDatosFinales.addCell(cellEmisionCondicionesV);
            tableDatosFinales.addCell(cellEmisionMetodoPago);
            tableDatosFinales.addCell(cellEmisionMetodoPagoV);
            tableDatosFinales.addCell(cellEmisionFormaPago);
           

            if (facturaDto.getFormaPago() != null && !facturaDto.getFormaPago().isEmpty()) {
                String formaPagoDescripcion = "";
                // for (FormaPago formaPago : facturaDto.getFormaPago()) {
                //     formaPagoDescripcion = formaPago.getFormaPago();
                // }
                formaPagoDescripcion = facturaDto.getFormaPago().trim();
                PdfPCell cellEmisionFormaPagoV = new PdfPCell(new Paragraph(formaPagoDescripcion, fontEtiquetasFinales));
                cellEmisionFormaPagoV.setHorizontalAlignment(Rectangle.ALIGN_LEFT);
                cellEmisionFormaPagoV.setBorder(Rectangle.NO_BORDER);
                tableDatosFinales.addCell(cellEmisionFormaPagoV);
            } else {
                PdfPCell cellEmisionFormaPagoV = new PdfPCell();
                cellEmisionFormaPagoV.setHorizontalAlignment(Element.ALIGN_LEFT);
                cellEmisionFormaPagoV.setBorder(Rectangle.NO_BORDER);
                tableDatosFinales.addCell(cellEmisionFormaPagoV);
            }

            tableDatosFinales.addCell(exportacionC);
            tableDatosFinales.addCell(exportacion);

            document.add(tableDatosFinales);
        } catch (Exception ex) {
            Logger.getLogger(PdfFacturaTenantAServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    /**
     * Metodo para agregar el codigo QR.
     *
     * @param document
     * @return Tabla CodigoQR
     * @throws DocumentException
     */
    public void agregarCodigoQR(Document document, CFDI facturaDto) throws DocumentException {
        PdfPTable tableCodigoQR = new PdfPTable(1);
        tableCodigoQR.setWidths(new int[]{380});
        tableCodigoQR.setTotalWidth(ANCHO_TOTAL__TABLA);
        tableCodigoQR.setLockedWidth(true);
        tableCodigoQR.setWidthPercentage(PORCENTAJE_ANCHO_TABLA);
        tableCodigoQR.getDefaultCell().setBorder(Rectangle.NO_BORDER);
        tableCodigoQR.getDefaultCell().setFixedHeight(ALTURA_FIJA_TABLA_QR);
        tableCodigoQR.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);

        PdfPCell cell = new PdfPCell();
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        cell.setBorder(Rectangle.NO_BORDER);
        tableCodigoQR.addCell(cell);

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
            tableCodigoQR.addCell(codeQrImage);
        } catch (Exception ex) {
            tableCodigoQR.addCell("");
            System.out.println("Error al generar el codigo QR " + ex.getMessage());
        }

        document.add(tableCodigoQR);
    }

    /**
     * Metodo para agregar la leyenda este documento es una representacion
     * imrpresa.
     *
     * @param document
     * @return Tabla Leyendas
     * @throws DocumentException
     */
    public void agregarLeyenda(Document document) throws DocumentException {
        PdfPTable tableLeyenda = new PdfPTable(2);
        tableLeyenda.setWidths(new int[]{85, 85});
        tableLeyenda.setTotalWidth(420);
        tableLeyenda.setLockedWidth(true);
        tableLeyenda.setWidthPercentage(PORCENTAJE_ANCHO_TABLA);
        tableLeyenda.getDefaultCell().setFixedHeight(ALTURA_FIJA_TABLA);
        tableLeyenda.getDefaultCell().setBorder(Rectangle.NO_BORDER);

        PdfPCell mensaje1 = new PdfPCell(new Phrase(hmapTagIdioma.get(EnumTagPlantilla.MENSAJE2).toUpperCase(), fontEtiquetas));
        mensaje1.setHorizontalAlignment(Element.ALIGN_CENTER);
        mensaje1.setColspan(2);
        mensaje1.setBorder(Rectangle.NO_BORDER);

        PdfPCell mensaje2 = new PdfPCell(new Phrase(hmapTagIdioma.get(EnumTagPlantilla.F_EFECTODONATIVOS), fontEtiquetas));
        mensaje2.setHorizontalAlignment(Element.ALIGN_CENTER);
        mensaje2.setColspan(2);
        mensaje2.setBorder(Rectangle.NO_BORDER);

        tableLeyenda.addCell(mensaje1);
        tableLeyenda.addCell(mensaje2);

        document.add(tableLeyenda);
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
    private StringBuilder armarConceptoImpuestoPTraslados(ComplementoPago20.Pago  pago) {
        StringBuilder descripcion = new StringBuilder();
        try {
            for (TrasladoP impuestosCargados : pago.getImpuestosP().getTrasladosP().getTrasladoP() ) {
                descripcion.append("\n");
                CatImpuestos obtenerCatImpuestosSatByClave = catalogosSatService.obtenerCatImpuestoSatByClave(impuestosCargados.getImpuestoP());
                descripcion.append(hmapTagIdioma.get(EnumTagPlantilla.IMP_TRASLADO).concat(" : ").concat(obtenerCatImpuestosSatByClave.getDescripcion() != null ? obtenerCatImpuestosSatByClave.getDescripcion().concat("     ") : " "))
                            .append(hmapTagIdioma.get(EnumTagPlantilla.TASA_O_CUOTA).concat(" : ").concat(String.valueOf(impuestosCargados.getTasaOCuotaP()) != null ? String.valueOf(impuestosCargados.getTasaOCuotaP()).concat("    ")  : ""))
                            .append(hmapTagIdioma.get(EnumTagPlantilla.TIPO_FACTOR).concat(" : ").concat(impuestosCargados.getTipoFactorP() != null ? impuestosCargados.getTipoFactorP().concat("     ") : ""))
                            .append(hmapTagIdioma.get(EnumTagPlantilla.IMPORTE).concat(" : ").concat(Utilities.bigDecimalToStr(Optional.ofNullable(impuestosCargados.getImporteP()).orElse(BigDecimal.ZERO)).concat("     ")))
                            .append(hmapTagIdioma.get(EnumTagPlantilla.BASE).concat(" : ").concat(Utilities.bigDecimalToStr(Optional.ofNullable(impuestosCargados.getBaseP()).orElse(BigDecimal.ZERO))));
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
            for (RetencionP   impuestosCargados :  conceptos.getImpuestosP().getRetencionesP().getRetencionP() ) { 
                descripcion.append("\n");
                CatImpuestos obtenerCatImpuestosSatByClave = catalogosSatService.obtenerCatImpuestoSatByClave(impuestosCargados.getImpuestoP());
                descripcion.append(hmapTagIdioma.get(EnumTagPlantilla.IMP_RETENIDO).concat(" : ").concat(obtenerCatImpuestosSatByClave.getDescripcion() != null ? obtenerCatImpuestosSatByClave.getDescripcion().concat("    ") : ""))
                        .append(hmapTagIdioma.get(EnumTagPlantilla.IMPORTE).concat(" : $").concat(String.valueOf(impuestosCargados.getImporteP()) != null ? String.valueOf(impuestosCargados.getImporteP()).concat("    ") : ""));
            }
            return descripcion;
        } catch (Exception e) {
            System.out.println("Ocurrio un error al tratar de armar el concepto con impuestos retenido" + e.getMessage());
            return new StringBuilder("");
        }
    }

    private StringBuilder armarConceptoImpuestoDRTraslados(ComplementoPago20.Pago.DoctoRelacionado  doctoRelacionado) {
        StringBuilder descripcion = new StringBuilder();
        try {
            for (TrasladoDR  impuestosCargados : doctoRelacionado.getImpuestosDR().getTrasladosDR().getTrasladoDR()) {
                descripcion.append("\n");
                CatImpuestos obtenerCatImpuestosSatByClave = catalogosSatService.obtenerCatImpuestoSatByClave(impuestosCargados.getImpuestoDR());
                descripcion.append(hmapTagIdioma.get(EnumTagPlantilla.IMP_TRASLADO).concat(" : ").concat((obtenerCatImpuestosSatByClave.getDescripcion() != null ? obtenerCatImpuestosSatByClave.getDescripcion().concat("    ") : ""))) 
                        .append(hmapTagIdioma.get(EnumTagPlantilla.TASA_O_CUOTA).concat(" : ").concat(String.valueOf(impuestosCargados.getTasaOCuotaDR()) != null ? String.valueOf(impuestosCargados.getTasaOCuotaDR()).concat("    ") : "")) 
                        .append(hmapTagIdioma.get(EnumTagPlantilla.TIPO_FACTOR).concat(" : ").concat(impuestosCargados.getTipoFactorDR() != null ? impuestosCargados.getTipoFactorDR().concat("    ") : ""))
                        .append(hmapTagIdioma.get(EnumTagPlantilla.IMPORTE).concat(" : ").concat(Utilities.bigDecimalToStr(Optional.ofNullable(impuestosCargados.getImporteDR()).orElse(BigDecimal.ZERO)).concat("     ")))
                        .append(hmapTagIdioma.get(EnumTagPlantilla.BASE).concat(" : ").concat(Utilities.bigDecimalToStr(Optional.ofNullable(impuestosCargados.getBaseDR()).orElse(BigDecimal.ZERO))));
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
            for (RetencionDR  impuestosCargados : conceptos.getImpuestosDR().getRetencionesDR().getRetencionDR()) { 
                descripcion.append("\n");
                CatImpuestos obtenerCatImpuestosSatByClave = catalogosSatService.obtenerCatImpuestoSatByClave(impuestosCargados.getImpuestoDR());
                descripcion.append(hmapTagIdioma.get(EnumTagPlantilla.IMP_RETENIDO).concat(" : ").concat(obtenerCatImpuestosSatByClave.getDescripcion() != null ? obtenerCatImpuestosSatByClave.getDescripcion().concat("     ") : ""))
                        .append(hmapTagIdioma.get(EnumTagPlantilla.TASA_O_CUOTA).concat(" : ").concat(String.valueOf(impuestosCargados.getTasaOCuotaDR()) != null ? String.valueOf(impuestosCargados.getTasaOCuotaDR()).concat("     ") : ""))
                        .append(hmapTagIdioma.get(EnumTagPlantilla.TIPO_FACTOR).concat(" : ").concat(impuestosCargados.getTipoFactorDR() != null ? impuestosCargados.getTipoFactorDR().concat("     ") : ""))
                        .append(hmapTagIdioma.get(EnumTagPlantilla.IMPORTE).concat(" : $").concat(String.valueOf(impuestosCargados.getImporteDR()) != null ? String.valueOf(impuestosCargados.getImporteDR()).concat("     ") : ""))
                        .append(hmapTagIdioma.get(EnumTagPlantilla.BASE).concat(" : ").concat(String.valueOf(impuestosCargados.getBaseDR()) != null ? String.valueOf(impuestosCargados.getBaseDR()).concat("    ") : ""));
            }
            return descripcion;
        } catch (Exception e) {
            System.out.println("Ocurrio un error al tratar de armar el concepto con impuestos retenido" + e.getMessage());
            return new StringBuilder("");
        }
    }

    private void agregarTableInformacionGlobal (Document document, CFDI facturaDto) throws  DocumentException {
        PdfPTable tableInformacionGlobalH = new PdfPTable(1);
        tableInformacionGlobalH.setWidths(new int[]{ 560});
        tableInformacionGlobalH.setTotalWidth(ANCHO_TOTAL__TABLA);
        tableInformacionGlobalH.setLockedWidth(true);
        tableInformacionGlobalH.setWidthPercentage(PORCENTAJE_ANCHO_TABLA);
        tableInformacionGlobalH.getDefaultCell().setBorder(Rectangle.NO_BORDER);


        if (facturaDto.getInformacionGlobal()!= null) {

            PdfPCell space = new PdfPCell();     
            space.setBackgroundColor(BaseColor.BLACK);
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
            tableDatosH.setWidths(new int[]{ANCHO_TOTAL__TABLA});
            tableDatosH.setTotalWidth(ANCHO_TOTAL__TABLA);
            tableDatosH.setLockedWidth(true);
            tableDatosH.setWidthPercentage(PORCENTAJE_ANCHO_TABLA);
            tableDatosH.getDefaultCell().setBorder(Rectangle.NO_BORDER);

            PdfPTable tableCfdiRelacionados = new PdfPTable(2);
            tableCfdiRelacionados.setWidths(new int[]{400, 200});
            tableCfdiRelacionados.setTotalWidth(ANCHO_TOTAL__TABLA);
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


            PdfPCell datosClienteC = new PdfPCell(new Phrase(hmapTagIdioma.get(EnumTagPlantilla.CFDI_RELACIONADOS), fontEncabezados));
            datosClienteC.setColspan(2);
            datosClienteC.setBackgroundColor(BaseColor.BLACK);
            datosClienteC.setHorizontalAlignment(Element.ALIGN_LEFT);
            datosClienteC.setBorder(Rectangle.NO_BORDER);


            tableDatosH.addCell(datosClienteC);
            tableDatosH.addCell(tableCfdiRelacionados);


            document.add(tableDatosH);
        }
    }

   
    @Override
    public void insertarLeyenda(String rutaPdf, String leyenda) {

        PdfContentByte pdfContentByte = null;
        PdfStamper pdfStamper = null;
        OutputStream outputStream = null;
        PdfReader pdf = null;

        try {

            pdf = new PdfReader(Utilities.getBytesFile(new File(rutaPdf)));
            outputStream = new FileOutputStream(rutaPdf);
            pdfStamper = new PdfStamper(pdf, outputStream);
            BaseFont font = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED);

            for (int numPagina = 1; numPagina <= pdf.getNumberOfPages(); numPagina++) {

                pdfContentByte = pdfStamper.getOverContent(numPagina);
                pdfContentByte.beginText();
                pdfContentByte.setFontAndSize(font, 80);
                pdfContentByte.showTextAligned(Element.ALIGN_CENTER, leyenda, 300, 380, 30);
                pdfContentByte.endText();
            }

            outputStream.flush();

        } catch (Exception e) {
            System.err.println("Ocurrio un error al tratar de insertar la Leyenda " + leyenda + " al PDF con Ruta " + rutaPdf + " Error " + e);

        } finally {
            try {
                if (pdfStamper != null) {
                    pdfStamper.close();
                }
                if (outputStream != null) {
                    outputStream.close();
                }
                if (pdf != null) {
                    pdf.close();
                }
            } catch (Exception e) {
                System.err.println("Error al tratar de cerrar streams de pdf");
            }

        }
    }

    class TableHeader extends PdfPageEventHelper {

        /**
         * The header text.
         */
        String header;
        /**
         * The template with the total number of pages.
         */
        PdfTemplate totalPagina;

        private EnumTipoProcesoTimbrado ambiente;

        public TableHeader(EnumTipoProcesoTimbrado ambiente) {
            this.ambiente = ambiente;
        }

        /**
         * Allows us to change the content of the header.
         *
         * @param header The new header String
         */
        public void setHeader(String header) {
            this.header = header;
        }

        /**
         * Creates the PdfTemplate that will hold the total number of pages.
         *
         * @see com.itextpdf.text.pdf.PdfPageEventHelper#onOpenDocument(
         * com.itextpdf.text.pdf.PdfWriter, com.itextpdf.text.Document)
         */
        @Override
        public void onOpenDocument(PdfWriter writer, Document document) {
            totalPagina = writer.getDirectContent().createTemplate(30, 16);
        }

        /**
         * Increase the page number.
         *
         * @see com.itextpdf.text.pdf.PdfPageEventHelper#onStartPage(
         * com.itextpdf.text.pdf.PdfWriter, com.itextpdf.text.Document)
         */
        @Override
        public void onStartPage(PdfWriter writer, Document document) {
        }

        /**
         * Adds a header to every page
         *
         * @see com.itextpdf.text.pdf.PdfPageEventHelper#onEndPage(
         * com.itextpdf.text.pdf.PdfWriter, com.itextpdf.text.Document)
         */
        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            PdfPTable table = new PdfPTable(3);
            try {
                Font fontEtiqueta = new Font(Font.FontFamily.HELVETICA, 2, Font.NORMAL, BaseColor.BLACK);
                table.setWidths(new int[]{24, 24, 2});
                table.setTotalWidth(527);
                table.setLockedWidth(true);
                table.getDefaultCell().setFixedHeight(ALTURA_FIJA_FINAL_PAGE);
                table.getDefaultCell().setBorder(Rectangle.NO_BORDER);
                Paragraph paragraph1 = new Paragraph("", fontEtiqueta);
                table.addCell(paragraph1);
                table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
                PdfPCell cellPaginado = new PdfPCell(new Paragraph(new Phrase(String.format(hmapTagIdioma.get(EnumTagPlantilla.PAGINA) + " %d " + hmapTagIdioma.get(EnumTagPlantilla.DE), writer.getPageNumber()), FontFactory.getFont(FontFactory.HELVETICA, 7, Font.NORMAL))));
                cellPaginado.setBorder(Rectangle.NO_BORDER);
                cellPaginado.setHorizontalAlignment(Element.ALIGN_RIGHT);
                table.addCell(cellPaginado);
                Image instance = Image.getInstance(totalPagina);
                instance.scalePercent(60);
                PdfPCell cell = new PdfPCell(Image.getInstance(instance));
                cell.setBorder(Rectangle.NO_BORDER);
                table.addCell(cell);
                table.writeSelectedRows(0, -1, 34, 20, writer.getDirectContent());

                if (ambiente!=null && ambiente == EnumTipoProcesoTimbrado.TEST) {
                    PdfContentByte contentByte = writer.getDirectContent();
                    PdfTemplate template = contentByte.createTemplate(700, 300);
                    template.beginText();
                    BaseFont baseFont = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
                    template.setFontAndSize(baseFont, 50);

                    for (int page = 0; page <= writer.getPageNumber(); page++) {
                        template.setTextMatrix(0, 0);
                        template.showText("SIN VALIDEZ FISCAL");
                    }

                    template.endText();
                    PdfGState pdfGState = new PdfGState();
                    pdfGState.setFillOpacity(0.3f);
                    contentByte.setGState(pdfGState);
                    contentByte.addTemplate(template, 1, 1, -1, 1, 100, 110);
                }

            } catch (IOException | DocumentException de) {
                throw new ExceptionConverter(de);
            }
        }

        /**
         * Fills out the total number of pages before the document is closed.
         *
         * @see com.itextpdf.text.pdf.PdfPageEventHelper#onCloseDocument(
         * com.itextpdf.text.pdf.PdfWriter, com.itextpdf.text.Document)
         */
        @Override
        public void onCloseDocument(PdfWriter writer, Document document) {
            ColumnText.showTextAligned(totalPagina, Element.ALIGN_LEFT,
                    new Phrase(String.valueOf(writer.getPageNumber() - 1)), 2, 2, 0);
        }


  
    }
}
