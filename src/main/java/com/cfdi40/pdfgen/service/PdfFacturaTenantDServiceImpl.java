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

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.ExceptionConverter;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BarcodeQRCode;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfGState;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfTemplate;
import com.itextpdf.text.pdf.PdfWriter;
import com.cfdi40.exceptionhandlerstarter.exception.BusinessException;
import com.cfdi40.pdfgen.dto.CatImpuestos;
import com.cfdi40.pdfgen.dto.CatTipoFactor;
import com.cfdi40.pdfgen.model.entity.CatIdiomapdf;
import com.cfdi40.pdfgen.model.entity.CatTagxidioma;
import com.cfdi40.pdfgen.model.entity.IdentificadorSucursal;
import com.cfdi40.pdfgen.model.repository.CatIdiomapdfRepository;
import com.cfdi40.pdfgen.model.repository.CatTagxidiomaRepository;
import com.cfdi40.pdfgen.util.ConstantesFacto;
import com.cfdi40.pdfgen.util.EnumTagPlantilla;
import com.cfdi40.pdfgen.util.FechasUtil;
import com.cfdi40.pdfgen.util.Utilities;
import com.cfdi40.pdfgen.tenants.tenanta.cfdi.dto.CFDI;
import com.cfdi40.pdfgen.tenants.tenanta.cfdi.dto.CFDI.Conceptos;
import com.cfdi40.pdfgen.tenants.tenanta.cfdi.dto.CFDI.Relacionados;
import com.cfdi40.pdfgen.tenants.tenanta.cfdi.dto.CFDI.Complementos.ComplementoPago20;
import com.cfdi40.pdfgen.tenants.tenanta.cfdi.dto.CFDI.Complementos.Donatarias;
import com.cfdi40.pdfgen.tenants.tenanta.cfdi.dto.CFDI.Complementos.ComplementoPago20.Pago;
import com.cfdi40.pdfgen.tenants.tenanta.cfdi.dto.CFDI.Complementos.ComplementoPago20.Pago.DoctoRelacionado;
import com.cfdi40.pdfgen.tenants.tenanta.cfdi.dto.CFDI.Complementos.ComplementoPago20.Pago.DoctoRelacionado.ImpuestosDR.RetencionesDR.RetencionDR;
import com.cfdi40.pdfgen.tenants.tenanta.cfdi.dto.CFDI.Complementos.ComplementoPago20.Pago.DoctoRelacionado.ImpuestosDR.TrasladosDR.TrasladoDR;
import com.cfdi40.pdfgen.tenants.tenanta.cfdi.dto.CFDI.Impuestos.Traslado;
import com.cfdi40.pdfgen.tenants.tenanta.cfdi.dto.CFDI.ImpuestosLocales.TrasladoLocal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import javax.persistence.Id;

@Service

public class PdfFacturaTenantDServiceImpl  implements PdfFacturaTenantDService {

    @Autowired
    CatTagxidiomaRepository catTagxidiomaRepository; 
    @Autowired
    CatIdiomapdfRepository catIdiomapdfRepository;
    @org.springframework.beans.factory.annotation.Autowired
    CatalogStaticFallbackService catalogFallback;
    @Autowired
    CatalogosSATService catalogosSatService;
    
    Font fontContenido = new Font(Font.FontFamily.HELVETICA, 7, Font.NORMAL, BaseColor.BLACK);
    Font fontEtiquetas = new Font(Font.FontFamily.HELVETICA, 7, Font.BOLD, BaseColor.BLACK);
    Font fontCabecera = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, BaseColor.BLACK);
    Font fontLeyendas = new Font(Font.FontFamily.HELVETICA, 6, Font.NORMAL, BaseColor.BLACK);
    private HashMap<EnumTagPlantilla, String> hmapTagIdioma;
    private List<CatTagxidioma> catTagxidioma;
    private static final int ANCHO_TOTAL_TABLA = 560;
    private static final int PORCENTAJE_ANCHO_TABLA = 100;

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
            agregarDatosEmisor(document, facturaDto);
            agregarDatosDonativos(document,facturaDto);
            agregarConceptos(document,facturaDto);
            agregarComplementoPagos(document,facturaDto);
            agregarTotales(document,facturaDto);
            agregarLeyenda(document, facturaDto.getComplementos().getDonatarias());
            agregarSellos(document,facturaDto);
            agregarDatosFinales(document,facturaDto,identificadorSucursal);
            agregarCodigoQR(document,facturaDto);
            agregarLeyendFinal(document);
            document.close();
            writer.flush();

            return pdfResponse;
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException(HttpStatus.BAD_REQUEST,"Ocurrio un error al tratar de generar el PDF de la plantilla TenantD de la factura con UUID "
                    + (facturaDto.getTimbrado() != null ? facturaDto.getTimbrado().getUuid() : "") + " Error: " + e.getStackTrace()[0].getLineNumber());
        }
    }

    /**
     * Metodo para agregar los datos del emisor
     *
     * @param document
     * @return Tabla Emisor
     * @throws DocumentException
     */
    public void agregarDatosEmisor(Document document, CFDI facturaDto) throws DocumentException {
        PdfPTable tableDatosEmisor = new PdfPTable(2);
        tableDatosEmisor.setWidths(new int[]{480, 80});
        tableDatosEmisor.setTotalWidth(ANCHO_TOTAL_TABLA);
        tableDatosEmisor.setLockedWidth(true);
        tableDatosEmisor.setWidthPercentage(PORCENTAJE_ANCHO_TABLA);
        tableDatosEmisor.getDefaultCell().setBorder(Rectangle.NO_BORDER);

        PdfPCell nombreEmisor = new PdfPCell(new Paragraph(facturaDto.getEmisor().getNombre() != null ? facturaDto.getEmisor().getNombre() : "", fontCabecera));
        nombreEmisor.setHorizontalAlignment(Element.ALIGN_CENTER);
        nombreEmisor.setBorder(Rectangle.NO_BORDER);

        PdfPCell cellReciboDonativos = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.F_RECIBODONATIVOS), fontEtiquetas));
        cellReciboDonativos.setHorizontalAlignment(Element.ALIGN_CENTER);
        cellReciboDonativos.setBorder(Rectangle.BOX);

        PdfPCell direccionEmisor = new PdfPCell();
        direccionEmisor.setHorizontalAlignment(Element.ALIGN_CENTER);
        direccionEmisor.setPaddingTop(-2f);
        direccionEmisor.setBorder(Rectangle.NO_BORDER);

        PdfPCell cellReciboDonativosV = new PdfPCell(new Paragraph((facturaDto.getTenantAData() != null && facturaDto.getTenantAData().getTrxType() != null ? facturaDto.getTenantAData().getTrxType() : "").concat(facturaDto.getFolio() != null ? facturaDto.getFolio() : ""), fontEtiquetas));
        cellReciboDonativosV.setHorizontalAlignment(Element.ALIGN_CENTER);
        cellReciboDonativosV.setBorder(Rectangle.BOX);

        tableDatosEmisor.addCell(nombreEmisor);
        tableDatosEmisor.addCell(cellReciboDonativos);
        tableDatosEmisor.addCell(direccionEmisor);
        tableDatosEmisor.addCell(cellReciboDonativosV);

        document.add(tableDatosEmisor);
    }

    /**
     * *
     * Metodo para agregar los datos de la donacion
     *
     * @param document
     * @return Tabla Donativos
     * @throws DocumentException
     */
    public void agregarDatosDonativos(Document document, CFDI facturaDto) throws DocumentException, Exception {
        PdfPTable tableDonativos = new PdfPTable(2);
        tableDonativos.setWidths(new int[]{480, 80});
        tableDonativos.setTotalWidth(ANCHO_TOTAL_TABLA);
        tableDonativos.setLockedWidth(true);
        tableDonativos.setWidthPercentage(PORCENTAJE_ANCHO_TABLA);
        tableDonativos.getDefaultCell().setBorder(Rectangle.NO_BORDER);

        PdfPCell rfcEmisor = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.RFC).concat(": ").concat(facturaDto.getEmisor().getRfc() != null ? facturaDto.getEmisor().getRfc() : ""), fontContenido));
        rfcEmisor.setHorizontalAlignment(Element.ALIGN_CENTER);
        rfcEmisor.setBorder(Rectangle.NO_BORDER);
        tableDonativos.addCell(rfcEmisor);

        PdfPCell celdaVacia = new PdfPCell();
        celdaVacia.setBorder(Rectangle.NO_BORDER);;
        celdaVacia.setHorizontalAlignment(Element.ALIGN_LEFT);
        tableDonativos.addCell(celdaVacia);

        
        if (facturaDto.getComplementos()!= null  ) {
            StringBuilder sbOficio = new StringBuilder("Autorizado para recibir donativos deducibles según oficio No.")
            .append(facturaDto.getComplementos().getDonatarias().getNoAutorizacion()).append(" del ").append(FechasUtil.formatDateDiaMesAnio(facturaDto.getComplementos().getDonatarias().getFechaAutorizacion()));

            PdfPCell oficioDonativo = new PdfPCell(new Paragraph(sbOficio.toString(), fontContenido));
            oficioDonativo.setHorizontalAlignment(Element.ALIGN_CENTER);
            oficioDonativo.setBorder(Rectangle.NO_BORDER);
            tableDonativos.addCell(oficioDonativo);
        }
    
        
      

        PdfPCell direccionEmisor = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.CODIGOPOSTAL).concat(facturaDto.getLugarExpedicion() != null ? facturaDto.getLugarExpedicion() : "").concat(" A ").concat(new Date().toLocaleString()), fontContenido));
        direccionEmisor.setHorizontalAlignment(Element.ALIGN_CENTER);
        direccionEmisor.setBorder(Rectangle.NO_BORDER);

        PdfPCell regimenFiscalEmisor = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.N_REGIMENFISCAL).concat(": ").concat(facturaDto.getEmisor().getRegimenFiscal() != null ? facturaDto.getEmisor().getRegimenFiscal() : ""), fontContenido));
        regimenFiscalEmisor.setHorizontalAlignment(Element.ALIGN_CENTER);
        regimenFiscalEmisor.setBorder(Rectangle.NO_BORDER);

        PdfPCell nombreDonativo = new PdfPCell(new Paragraph("RECIBIMOS DE: ".concat(facturaDto.getReceptor().getNombre() != null ? facturaDto.getReceptor().getNombre() : ""), fontContenido));
        nombreDonativo.setHorizontalAlignment(Element.ALIGN_CENTER);
        nombreDonativo.setBorder(Rectangle.NO_BORDER);

        PdfPCell rfcReceptor = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.RFC).concat(": ").concat(facturaDto.getReceptor().getRfc() != null ? facturaDto.getReceptor().getRfc() : ""), fontContenido));
        rfcReceptor.setHorizontalAlignment(Element.ALIGN_CENTER);
        rfcReceptor.setBorder(Rectangle.NO_BORDER);

        PdfPCell usoCfdiReceptor = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.G_USOCFDI).concat(": ").concat(facturaDto.getReceptor().getUsoCFDI() != null ? facturaDto.getReceptor().getUsoCFDI() : ""), fontContenido));
        usoCfdiReceptor.setHorizontalAlignment(Element.ALIGN_CENTER);
        usoCfdiReceptor.setBorder(Rectangle.NO_BORDER);

        PdfPCell regFiscReceptor = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.REG_FISCAL_RECEP).concat(": ").concat(facturaDto.getReceptor().getRegimenFiscal() != null ? facturaDto.getReceptor().getRegimenFiscal() : ""), fontContenido));
        regFiscReceptor.setHorizontalAlignment(Element.ALIGN_CENTER);
        regFiscReceptor.setBorder(Rectangle.NO_BORDER);

        PdfPCell domicilioFiscReceptor = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.R_DOMFISCRECEP).concat(": ").concat(facturaDto.getReceptor().getDomicilioFiscal() != null ? facturaDto.getReceptor().getDomicilioFiscal() : ""), fontContenido));
        domicilioFiscReceptor.setHorizontalAlignment(Element.ALIGN_CENTER);
        domicilioFiscReceptor.setBorder(Rectangle.NO_BORDER);

       
        tableDonativos.addCell(celdaVacia);
        tableDonativos.addCell(direccionEmisor);
        tableDonativos.addCell(celdaVacia);
        tableDonativos.addCell(regimenFiscalEmisor);
        tableDonativos.addCell(celdaVacia);
        tableDonativos.addCell(nombreDonativo);
        tableDonativos.addCell(celdaVacia);
        tableDonativos.addCell(rfcReceptor);
        tableDonativos.addCell(celdaVacia);
        tableDonativos.addCell(usoCfdiReceptor);
        tableDonativos.addCell(celdaVacia);
        tableDonativos.addCell(regFiscReceptor);
        tableDonativos.addCell(celdaVacia);
        tableDonativos.addCell(domicilioFiscReceptor);
        tableDonativos.addCell(celdaVacia);
        tableDonativos.setSpacingAfter(10f);

        StringBuilder cfdiRelacionado = new StringBuilder();
        if (facturaDto.getCfdiRelacionados() != null ) {
            for (Relacionados cfdiRelacionadosCargados : facturaDto.getCfdiRelacionados()) {
                cfdiRelacionado.append(cfdiRelacionadosCargados.getUuid()).append(",");
            }
        }

        PdfPTable tableRelacionado = new PdfPTable(2);
        tableRelacionado.setWidths(new int[]{80, 480});
        tableRelacionado.setTotalWidth(ANCHO_TOTAL_TABLA);
        tableRelacionado.setLockedWidth(true);
        tableRelacionado.setWidthPercentage(PORCENTAJE_ANCHO_TABLA);
        tableRelacionado.getDefaultCell().setBorder(Rectangle.NO_BORDER);

        PdfPCell spaceCfdiRelacionado = new PdfPCell();
        spaceCfdiRelacionado.setBackgroundColor(BaseColor.BLACK);
        spaceCfdiRelacionado.setColspan(6);
        spaceCfdiRelacionado.setFixedHeight(1.5f);
        spaceCfdiRelacionado.setBorder(Rectangle.NO_BORDER);
        tableRelacionado.addCell(spaceCfdiRelacionado);

        if (cfdiRelacionado.length() > 0) {
            PdfPCell cfdiRelacionadoReceptor = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.G_CFDIRELACIONADO).concat(": "), fontEtiquetas));
            cfdiRelacionadoReceptor.setHorizontalAlignment(Element.ALIGN_LEFT);
            cfdiRelacionadoReceptor.setBorder(Rectangle.NO_BORDER);


            PdfPCell cfdiRelacionadoReceptorV = new PdfPCell(new Paragraph((cfdiRelacionado.substring(0, cfdiRelacionado.length() - 1)), fontContenido));
            cfdiRelacionadoReceptorV.setHorizontalAlignment(Element.ALIGN_LEFT);
            
            cfdiRelacionadoReceptorV.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cfdiRelacionadoReceptorV.setBackgroundColor(BaseColor.WHITE);
            cfdiRelacionadoReceptorV.setBorder(Rectangle.NO_BORDER);

            tableRelacionado.addCell(cfdiRelacionadoReceptor);
            tableRelacionado.addCell(cfdiRelacionadoReceptorV);
        } else {
            PdfPCell cfdiRelacionadoReceptor = new PdfPCell();
            cfdiRelacionadoReceptor.setBorder(Rectangle.NO_BORDER);
            tableRelacionado.addCell(cfdiRelacionadoReceptor);
        }
        //tableDonativos.addCell(tableRelacionado);

        PdfPTable tableTipoRelacion = new PdfPTable(2);
        tableTipoRelacion.setWidths(new int[]{80, 480});
        tableTipoRelacion.setTotalWidth(ANCHO_TOTAL_TABLA);
        tableTipoRelacion.setLockedWidth(true);
        tableTipoRelacion.setWidthPercentage(PORCENTAJE_ANCHO_TABLA);
        tableTipoRelacion.getDefaultCell().setBorder(Rectangle.NO_BORDER);

        if (facturaDto.getCfdiRelacionados() != null) {
            for (Relacionados cfdiRelacionadosCargados : facturaDto.getCfdiRelacionados()) {
                PdfPCell tipoRelacion = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.G_TIPORELACION).concat(": "), fontEtiquetas));
                tipoRelacion.setHorizontalAlignment(Element.ALIGN_LEFT);
                tipoRelacion.setBorder(Rectangle.NO_BORDER);

                PdfPCell tipoRelacionV = new PdfPCell(new Paragraph(cfdiRelacionadosCargados.getTipoRelacion(), fontContenido));
                tipoRelacionV.setHorizontalAlignment(Element.ALIGN_LEFT);
                tipoRelacionV.setBorder(Rectangle.NO_BORDER);
                tableTipoRelacion.addCell(tipoRelacion);
                tableTipoRelacion.addCell(tipoRelacionV);
            }
        } else {
            PdfPCell tipoRelacion = new PdfPCell();
            tipoRelacion.setHorizontalAlignment(Element.ALIGN_LEFT);
            tipoRelacion.setBorder(Rectangle.NO_BORDER);
            tableTipoRelacion.addCell(tipoRelacion);
        }
        document.add(tableDonativos);
        document.add(tableRelacionado);
        document.add(tableTipoRelacion);
    }

    /**
     * *
     * Metodo para agregar los conceptos
     *
     * @param document
     * @return Tabla Conceptos
     * @throws DocumentException
     */
    public void agregarConceptos(Document document, CFDI facturaDto) throws DocumentException {
        PdfPTable tableConceptos = new PdfPTable(6);
        tableConceptos.setWidths(new int[]{30, 30, 200, 30, 50, 50});
        tableConceptos.setTotalWidth(ANCHO_TOTAL_TABLA);
        tableConceptos.setLockedWidth(true);
        tableConceptos.setWidthPercentage(PORCENTAJE_ANCHO_TABLA);
        tableConceptos.getDefaultCell().setBorder(Rectangle.NO_BORDER);

        PdfPCell spaceConceptos = new PdfPCell();
        spaceConceptos.setBackgroundColor(BaseColor.BLACK);
        spaceConceptos.setColspan(6);
        spaceConceptos.setFixedHeight(1.5f);
        spaceConceptos.setBorder(Rectangle.NO_BORDER);

        PdfPCell cantidadConceptosC = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.P_CANTIDAD), fontEtiquetas));
        cantidadConceptosC.setHorizontalAlignment(Element.ALIGN_CENTER);
        cantidadConceptosC.setBorder(Rectangle.NO_BORDER);

        PdfPCell conceptoConceptoC = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.N_CONCEPTO), fontEtiquetas));
        conceptoConceptoC.setHorizontalAlignment(Element.ALIGN_CENTER);
        conceptoConceptoC.setBorder(Rectangle.NO_BORDER);

        PdfPCell claveConcepto = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.P_CLAVEPRODUCTO), fontEtiquetas));
        claveConcepto.setHorizontalAlignment(Element.ALIGN_CENTER);
        claveConcepto.setBorder(Rectangle.NO_BORDER);

        PdfPCell unidadConceptoC = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.P_CLAVEUNIDAD), fontEtiquetas));
        unidadConceptoC.setHorizontalAlignment(Element.ALIGN_CENTER);
        unidadConceptoC.setBorder(Rectangle.NO_BORDER);

        PdfPCell precioUniConceptoC = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.P_PRECIOUNITARIO), fontEtiquetas));
        precioUniConceptoC.setHorizontalAlignment(Element.ALIGN_CENTER);
        precioUniConceptoC.setBorder(Rectangle.NO_BORDER);

        PdfPCell importeConceptoC = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.P_IMPORTE), fontEtiquetas));
        importeConceptoC.setHorizontalAlignment(Element.ALIGN_CENTER);
        importeConceptoC.setBorder(Rectangle.NO_BORDER);

        tableConceptos.addCell(spaceConceptos);
        tableConceptos.addCell(cantidadConceptosC);
        tableConceptos.addCell(claveConcepto);
        tableConceptos.addCell(conceptoConceptoC);
        tableConceptos.addCell(unidadConceptoC);
        tableConceptos.addCell(precioUniConceptoC);
        tableConceptos.addCell(importeConceptoC);

        for (Conceptos conceptosCargados : facturaDto.getConceptos()) {
            StringBuilder descConcepto = new StringBuilder();
            if (conceptosCargados.getImpuestos() != null && conceptosCargados.getImpuestos().getTraslados() != null && !conceptosCargados.getImpuestos().getTraslados().isEmpty()) {
                descConcepto.append(armarConceptoImpuestoTraslados(conceptosCargados));
            }
            PdfPCell celdaCantidad = new PdfPCell(new Paragraph(new Phrase(conceptosCargados.getCantidad().toString(), fontContenido)));
            celdaCantidad.setHorizontalAlignment(Element.ALIGN_CENTER);
            celdaCantidad.setBorder(Rectangle.NO_BORDER);

            PdfPCell celdaClaveProducto = new PdfPCell(new Paragraph(new Phrase(conceptosCargados.getClaveProdServ(), fontContenido)));
            celdaClaveProducto.setHorizontalAlignment(Element.ALIGN_CENTER);
            celdaClaveProducto.setBorder(Rectangle.NO_BORDER);

            PdfPCell celdaDescripcion = new PdfPCell(new Paragraph(new Phrase(conceptosCargados.getDescripcion().concat(descConcepto.toString()), fontContenido)));
            celdaDescripcion.setHorizontalAlignment(Element.ALIGN_LEFT);
            celdaDescripcion.setBorder(Rectangle.NO_BORDER);

            PdfPCell celdaUnidad = new PdfPCell(new Paragraph(new Phrase(conceptosCargados.getClaveUnidad(), fontContenido)));
            celdaUnidad.setHorizontalAlignment(Element.ALIGN_CENTER);
            celdaUnidad.setBorder(Rectangle.NO_BORDER);

            PdfPCell celdaPUnitario = new PdfPCell(new Paragraph(new Phrase(Utilities.bigDecimalToStr(conceptosCargados.getValorUnitario()), fontContenido)));
            celdaPUnitario.setHorizontalAlignment(Element.ALIGN_RIGHT);
            celdaPUnitario.setBorder(Rectangle.NO_BORDER);

            PdfPCell celdaImporte = new PdfPCell(new Paragraph(new Phrase(Utilities.bigDecimalToStr(conceptosCargados.getImporte()), fontContenido)));
            celdaImporte.setHorizontalAlignment(Element.ALIGN_RIGHT);
            celdaImporte.setBorder(Rectangle.NO_BORDER);

            tableConceptos.addCell(celdaCantidad);
            tableConceptos.addCell(celdaClaveProducto);
            tableConceptos.addCell(celdaDescripcion);
            tableConceptos.addCell(celdaUnidad);
            tableConceptos.addCell(celdaPUnitario);
            tableConceptos.addCell(celdaImporte);
        }

        PdfPCell subtotalC = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.T_SUBTOTAL), fontEtiquetas));
        subtotalC.setHorizontalAlignment(Element.ALIGN_RIGHT);
        subtotalC.setColspan(5);
        subtotalC.setBorder(Rectangle.NO_BORDER);

        PdfPCell subtotalV = new PdfPCell(new Phrase(Utilities.bigDecimalToStr(facturaDto.getSubTotal()), fontContenido));
        subtotalV.setHorizontalAlignment(Element.ALIGN_RIGHT);
        subtotalV.setBorder(Rectangle.NO_BORDER);

        tableConceptos.addCell(subtotalC);
        tableConceptos.addCell(subtotalV);
        String sImpuesto = "";
        if (facturaDto.getImpuestos() != null && facturaDto.getImpuestos().getTraslado() != null && !facturaDto.getImpuestos().getTraslado().isEmpty()) {
            for (Traslado impuestosTrasladados : facturaDto.getImpuestos().getTraslado()) {
                                
                CatImpuestos impuestoSat = catalogosSatService.obtenerCatImpuestoSatByClave(impuestosTrasladados.getImpuesto());                

                CatTipoFactor tipoFactor;
                tipoFactor = catalogosSatService.obtenerCatTipofactorSatByClave(impuestosTrasladados.getTipoFactor());
                if (!tipoFactor.equals("EXENTO")) {
                    sImpuesto = impuestoSat.getDescripcion() + "(" + impuestosTrasladados.getTasaoCuota().setScale(2) + ")";
                } else {
                    sImpuesto = impuestoSat.getDescripcion() +  "(EXENTO)";
                }
                                                
                PdfPCell celdaImpuestoT = new PdfPCell(new Paragraph(new Phrase(sImpuesto, fontEtiquetas)));
                celdaImpuestoT.setColspan(5);
                celdaImpuestoT.setHorizontalAlignment(Element.ALIGN_RIGHT);
                celdaImpuestoT.setBorder(Rectangle.NO_BORDER);

                PdfPCell celdaImpuesto = new PdfPCell(new Paragraph(new Phrase(Utilities.bigDecimalToStr(impuestosTrasladados.getImporte()), fontContenido)));
                celdaImpuesto.setHorizontalAlignment(Element.ALIGN_RIGHT);
                celdaImpuesto.setBorder(Rectangle.NO_BORDER);

                tableConceptos.addCell(celdaImpuestoT);
                tableConceptos.addCell(celdaImpuesto);
            }
        }
        String sImpuestoLocales = "";
        if (facturaDto.getImpuestosLocales() != null && facturaDto.getImpuestosLocales().getTrasladoLocales() != null && !facturaDto.getImpuestosLocales().getTrasladoLocales().isEmpty()) {
            for (TrasladoLocal imp : facturaDto.getImpuestosLocales().getTrasladoLocales()) {
                sImpuestoLocales = imp.getImpuesto() + "(" + imp.getTasa().doubleValue() + ")";

                PdfPCell celdaImpuestoT = new PdfPCell(new Paragraph(new Phrase(sImpuestoLocales, fontEtiquetas)));
                celdaImpuestoT.setColspan(5);
                celdaImpuestoT.setHorizontalAlignment(Element.ALIGN_RIGHT);
                celdaImpuestoT.setBorder(Rectangle.NO_BORDER);

                PdfPCell celdaImpuesto = new PdfPCell(new Paragraph(new Phrase(Utilities.bigDecimalToStr(imp.getImporte()), fontContenido)));
                celdaImpuesto.setHorizontalAlignment(Element.ALIGN_RIGHT);
                celdaImpuesto.setBorder(Rectangle.NO_BORDER);

                tableConceptos.addCell(celdaImpuestoT);
                tableConceptos.addCell(celdaImpuesto);
            }
        }

        PdfPCell totalLetraC = new PdfPCell(new Phrase(hmapTagIdioma.get(EnumTagPlantilla.N_IMPORTELETRA).concat(": ").concat(Utilities.crearTotalLetra(facturaDto)), fontContenido));
        totalLetraC.setHorizontalAlignment(Element.ALIGN_LEFT);
        totalLetraC.setColspan(4);
        totalLetraC.setBorder(Rectangle.NO_BORDER);

        PdfPCell totalC = new PdfPCell(new Phrase(hmapTagIdioma.get(EnumTagPlantilla.C_TOTAL), fontEtiquetas));
        totalC.setHorizontalAlignment(Element.ALIGN_RIGHT);
        totalC.setBorder(Rectangle.NO_BORDER);

        PdfPCell totalV = new PdfPCell(new Phrase(Utilities.bigDecimalToStr(facturaDto.getTotal()), fontContenido));
        totalV.setHorizontalAlignment(Element.ALIGN_RIGHT);
        totalV.setBorder(Rectangle.NO_BORDER);

        tableConceptos.addCell(totalLetraC);
        tableConceptos.addCell(totalC);
        tableConceptos.addCell(totalV);
        tableConceptos.addCell(spaceConceptos);

        document.add(tableConceptos);
    }

    public void agregarComplementoPagos(Document document, CFDI facturaDto) throws DocumentException {
        PdfPTable tableDocumentoRelacionado = new PdfPTable(1);
        tableDocumentoRelacionado.setWidths(new int[]{560});
        tableDocumentoRelacionado.setTotalWidth(ANCHO_TOTAL_TABLA);
        tableDocumentoRelacionado.setLockedWidth(true);
        tableDocumentoRelacionado.setWidthPercentage(PORCENTAJE_ANCHO_TABLA);
        tableDocumentoRelacionado.getDefaultCell().setBorder(Rectangle.NO_BORDER);

        if (facturaDto.getComplementos() != null && facturaDto.getComplementos().getComplementoPago20() != null) {
            PdfPCell spaceComplementoPagos = new PdfPCell();
            spaceComplementoPagos.setBackgroundColor(BaseColor.BLACK);
            spaceComplementoPagos.setFixedHeight(1.5f);
            spaceComplementoPagos.setBorder(Rectangle.NO_BORDER);

            // PdfPCell encabezadoPago = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.C_COMPLEMENTO_RECEPCION_PAGO), fontEtiquetas));
            // encabezadoPago.setHorizontalAlignment(Element.ALIGN_LEFT);
            // encabezadoPago.setBorder(Rectangle.NO_BORDER);
            tableDocumentoRelacionado.addCell(spaceComplementoPagos);

            //tableDocumentoRelacionado.addCell(encabezadoPago);
            for (Pago complementoPagoCargados : facturaDto.getComplementos().getComplementoPago20().getPago()) {

                PdfPCell headerNodoPago = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.C_COMPLEMENTO_RECEPCION_PAGO), fontEtiquetas));
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

                PdfPCell fechaPagoC = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.C_PAGO).concat(": ").concat(fechaPago), fontContenido));
                fechaPagoC.setHorizontalAlignment(Element.ALIGN_LEFT);
                fechaPagoC.setBorder(Rectangle.NO_BORDER);

                PdfPCell formaDePago = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.T_FORMAPAGO).concat(": ").concat(complementoPagoCargados.getFormaDePagoP() != null ? complementoPagoCargados.getFormaDePagoP() : ""), fontContenido));
                formaDePago.setHorizontalAlignment(Element.ALIGN_LEFT);
                formaDePago.setBorder(Rectangle.NO_BORDER);

                PdfPCell monedaPago = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.N_MONEDA).concat(": ").concat(complementoPagoCargados.getMonedaP() != null ? complementoPagoCargados.getMonedaP() : ""), fontContenido));
                monedaPago.setHorizontalAlignment(Element.ALIGN_LEFT);
                monedaPago.setBorder(Rectangle.NO_BORDER);

                PdfPCell tipoCambioPago = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.C_TIPOCAMBIO).concat(": ").concat(String.valueOf(complementoPagoCargados.getTipoCambioP() != null ? complementoPagoCargados.getTipoCambioP() : "")), fontContenido));
                tipoCambioPago.setHorizontalAlignment(Element.ALIGN_LEFT);
                tipoCambioPago.setBorder(Rectangle.NO_BORDER);

                PdfPCell montoPago = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.C_MONTO).concat(": ").concat(String.valueOf(complementoPagoCargados.getMonto() != null ? complementoPagoCargados.getMonto() : "")), fontContenido));
                montoPago.setHorizontalAlignment(Element.ALIGN_LEFT);
                montoPago.setBorder(Rectangle.NO_BORDER);

                PdfPCell numeroOperacionPago = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.C_NUMEROOPERACION).concat(": ").concat(complementoPagoCargados.getNumOperacion() != null ? complementoPagoCargados.getNumOperacion() : ""), fontContenido));
                numeroOperacionPago.setHorizontalAlignment(Element.ALIGN_LEFT);
                numeroOperacionPago.setBorder(Rectangle.NO_BORDER);

                PdfPCell rfcEmisorCtaOrdenantePago = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.C_RFCEMISORCTAORDENANTE).concat(": ").concat(complementoPagoCargados.getRfcEmisorCtaOrd() != null ? complementoPagoCargados.getRfcEmisorCtaOrd() : ""), fontContenido));
                rfcEmisorCtaOrdenantePago.setHorizontalAlignment(Element.ALIGN_LEFT);
                rfcEmisorCtaOrdenantePago.setBorder(Rectangle.NO_BORDER);

                PdfPCell nombreBancoOrdenantePago = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.C_NOMBREBANCOORDENANTE).concat(": ").concat(complementoPagoCargados.getNomBancoOrdExt() != null ? complementoPagoCargados.getNomBancoOrdExt() : ""), fontContenido));
                nombreBancoOrdenantePago.setHorizontalAlignment(Element.ALIGN_LEFT);
                nombreBancoOrdenantePago.setBorder(Rectangle.NO_BORDER);

                PdfPCell cuentaOrdenantePago = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.C_CTAORDENANTE).concat(": ").concat(complementoPagoCargados.getCtaOrdenante() != null ? complementoPagoCargados.getCtaOrdenante() : ""), fontContenido));
                cuentaOrdenantePago.setHorizontalAlignment(Element.ALIGN_LEFT);
                cuentaOrdenantePago.setBorder(Rectangle.NO_BORDER);

                PdfPCell rfcEmisorCtaBeneficiarioPago = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.C_EMISORCTABENEFICIARIO).concat(": ").concat(complementoPagoCargados.getRfcEmisorCtaBen() != null ? complementoPagoCargados.getRfcEmisorCtaBen() : ""), fontContenido));
                rfcEmisorCtaBeneficiarioPago.setHorizontalAlignment(Element.ALIGN_LEFT);
                rfcEmisorCtaBeneficiarioPago.setBorder(Rectangle.NO_BORDER);

                PdfPCell cuentaBenefiarioPago = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.C_CTABENEFICIARIO).concat(": ").concat(complementoPagoCargados.getCtaBeneficiario() != null ? complementoPagoCargados.getCtaBeneficiario() : ""), fontContenido));
                cuentaBenefiarioPago.setHorizontalAlignment(Element.ALIGN_LEFT);
                cuentaBenefiarioPago.setBorder(Rectangle.NO_BORDER);

                PdfPCell tipoCaducidadPago = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.C_TIPOCADENAPAGO).concat(": ").concat(complementoPagoCargados.getTipoCadPago() != null ? complementoPagoCargados.getTipoCadPago() : ""), fontContenido));
                tipoCaducidadPago.setHorizontalAlignment(Element.ALIGN_LEFT);
                tipoCaducidadPago.setBorder(Rectangle.NO_BORDER);

                PdfPCell certificadoPago = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.C_CERTIFICADOPAGO).concat(": ").concat(complementoPagoCargados.getCertPago() != null ? complementoPagoCargados.getCertPago() : ""), fontContenido));
                certificadoPago.setHorizontalAlignment(Element.ALIGN_LEFT);
                certificadoPago.setBorder(Rectangle.NO_BORDER);

                PdfPCell caducidadPago = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.C_CADUCIDADPAGO).concat(": ").concat(complementoPagoCargados.getCadPago() != null ? complementoPagoCargados.getCadPago() : ""), fontContenido));
                caducidadPago.setHorizontalAlignment(Element.ALIGN_LEFT);
                caducidadPago.setBorder(Rectangle.NO_BORDER);

                PdfPCell selloPago = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.C_SELLOPAGO).concat(": ").concat(complementoPagoCargados.getSelloPago() != null ? complementoPagoCargados.getSelloPago() : ""), fontContenido));
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

                PdfPTable tableImpPago = new PdfPTable(3);
                tableImpPago.setWidths(new int[]{50, 50, 50});
                tableImpPago.setTotalWidth(480);
                tableImpPago.setWidthPercentage(PORCENTAJE_ANCHO_TABLA);
                tableImpPago.setLockedWidth(true);
                tableImpPago.getDefaultCell().setBorder(Rectangle.NO_BORDER);
                tableImpPago.addCell(impPago);


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
                tableDocumentoRelacionado.addCell(tablePago);
                tableDocumentoRelacionado.addCell(tableImpPago);

                if (complementoPagoCargados.getDocumentoRelacionado() != null && !complementoPagoCargados.getDocumentoRelacionado().isEmpty()) {
                    for (DoctoRelacionado documentosRelacionCargados : complementoPagoCargados.getDocumentoRelacionado())
                    {

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

                        PdfPCell idDocumentoRelacionadoC = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.C_FOLIOUUID).concat(": ").concat(documentosRelacionCargados.getIdDocumento() != null ? documentosRelacionCargados.getIdDocumento() : ""), fontContenido));
                        idDocumentoRelacionadoC.setHorizontalAlignment(Element.ALIGN_LEFT);
                        idDocumentoRelacionadoC.setBorder(Rectangle.NO_BORDER);

                        PdfPCell serieDocumentoRelacionadoC = new PdfPCell(new Paragraph("Serie".concat(": ").concat(documentosRelacionCargados.getSerie() != null ? documentosRelacionCargados.getSerie() : ""), fontContenido));
                        serieDocumentoRelacionadoC.setHorizontalAlignment(Element.ALIGN_LEFT);
                        serieDocumentoRelacionadoC.setBorder(Rectangle.NO_BORDER);

                        PdfPCell folioDocumentoRelacionadoC = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.C_FOLIO).concat(": ").concat(documentosRelacionCargados.getFolio() != null ? documentosRelacionCargados.getFolio() : ""), fontContenido));
                        folioDocumentoRelacionadoC.setHorizontalAlignment(Element.ALIGN_LEFT);
                        folioDocumentoRelacionadoC.setBorder(Rectangle.NO_BORDER);

                        PdfPCell monedaDocumentoRelacionadoC = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.N_MONEDA).concat(": ").concat(documentosRelacionCargados.getMonedaDR() != null ? documentosRelacionCargados.getMonedaDR() : ""), fontContenido));
                        monedaDocumentoRelacionadoC.setHorizontalAlignment(Element.ALIGN_LEFT);
                        monedaDocumentoRelacionadoC.setBorder(Rectangle.NO_BORDER);

                        // PdfPCell tipoDeCambioDocumentoRelacionadoC = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.C_TIPOCAMBIO).concat(": ").concat(String.valueOf(documentosRelacionCargados.getTipoCambioDR() != null ? documentosRelacionCargados.getTipoCambioDR() : "")), fontContenido));
                        // tipoDeCambioDocumentoRelacionadoC.setHorizontalAlignment(Element.ALIGN_LEFT);
                        // tipoDeCambioDocumentoRelacionadoC.setBorder(Rectangle.NO_BORDER);

                        // PdfPCell metodoPagoDocumentoRelacionadoC = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.T_METODOPAGO).concat(": ").concat(documentosRelacionCargados.getMetodoDePagoDR() != null ? documentosRelacionCargados.getMetodoDePagoDR() : ""), fontContenido));
                        // metodoPagoDocumentoRelacionadoC.setHorizontalAlignment(Element.ALIGN_LEFT);
                        // metodoPagoDocumentoRelacionadoC.setBorder(Rectangle.NO_BORDER);

                        PdfPCell numeroParcialidaDocumentoRelacionadoC = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.C_NUMEROPARCIALIDADES).concat(": ").concat(String.valueOf(documentosRelacionCargados.getNumParcialidad() != null ? documentosRelacionCargados.getNumParcialidad() : "")), fontContenido));
                        numeroParcialidaDocumentoRelacionadoC.setHorizontalAlignment(Element.ALIGN_LEFT);
                        numeroParcialidaDocumentoRelacionadoC.setBorder(Rectangle.NO_BORDER);

                        PdfPCell importeSaldoAntDocumentoRelacionadoC = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.C_IMPORTESALDOANT).concat(": ").concat(String.valueOf(documentosRelacionCargados.getImpSaldoAnt() != null ? documentosRelacionCargados.getImpSaldoAnt() : "")), fontContenido));
                        importeSaldoAntDocumentoRelacionadoC.setHorizontalAlignment(Element.ALIGN_LEFT);
                        importeSaldoAntDocumentoRelacionadoC.setBorder(Rectangle.NO_BORDER);

                        PdfPCell importeSaldoInsoDocumentoRelacionadoC = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.C_IMPORTESALDOINS).concat(": ").concat(String.valueOf(documentosRelacionCargados.getImpSaldoInsoluto() != null ? documentosRelacionCargados.getImpSaldoInsoluto() : "")), fontContenido));
                        importeSaldoInsoDocumentoRelacionadoC.setHorizontalAlignment(Element.ALIGN_LEFT);
                        importeSaldoInsoDocumentoRelacionadoC.setBorder(Rectangle.NO_BORDER);

                        tableDocumentoRelacionadoPago.addCell(idDocumentoRelacionadoC);
                        tableDocumentoRelacionadoPago.addCell(serieDocumentoRelacionadoC);
                        tableDocumentoRelacionadoPago.addCell(folioDocumentoRelacionadoC);
                        tableDocumentoRelacionadoPago.addCell(monedaDocumentoRelacionadoC);
                        // tableDocumentoRelacionadoPago.addCell(tipoDeCambioDocumentoRelacionadoC);
                        // tableDocumentoRelacionadoPago.addCell(metodoPagoDocumentoRelacionadoC);
                        // tableDocumentoRelacionadoPago.addCell(numeroParcialidaDocumentoRelacionadoC);
                        tableDocumentoRelacionadoPago.addCell(importeSaldoAntDocumentoRelacionadoC);
                        tableDocumentoRelacionadoPago.addCell(importeSaldoInsoDocumentoRelacionadoC);
                        tableDocumentoRelacionado.addCell(tableDocumentoRelacionadoPago);

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
                        PdfPTable tableImpPagoDR = new PdfPTable(3);
                        tableImpPagoDR.setWidths(new int[]{50, 50, 50});
                        tableImpPagoDR.setTotalWidth(480);
                        tableImpPagoDR.setWidthPercentage(PORCENTAJE_ANCHO_TABLA);
                        tableImpPagoDR.setLockedWidth(true);
                        tableImpPagoDR.getDefaultCell().setBorder(Rectangle.NO_BORDER);

                        PdfPCell impPagoDR = new PdfPCell(new Paragraph( "" + descImpuestoDR , fontContenido));
                        impPagoDR.setHorizontalAlignment(Element.ALIGN_LEFT);
                        impPagoDR.setColspan(3);
                        impPagoDR.setBorder(Rectangle.NO_BORDER);
                        tableImpPagoDR.addCell(impPagoDR);
                        tableDocumentoRelacionado.addCell(tableImpPagoDR);

                    }
                }
            }
        }
        document.add(tableDocumentoRelacionado);
    }
    
    public void agregarTotales(Document document, CFDI facturaDto) throws DocumentException
    {
        PdfPTable tableTotalesHeader = new PdfPTable(1);
        tableTotalesHeader.setWidths(new int[]{560});
        tableTotalesHeader.setTotalWidth(ANCHO_TOTAL_TABLA);
        tableTotalesHeader.setLockedWidth(true);
        tableTotalesHeader.setWidthPercentage(PORCENTAJE_ANCHO_TABLA);
        tableTotalesHeader.getDefaultCell().setBorder(Rectangle.NO_BORDER);

        PdfPCell headerNodoTotales = new PdfPCell(new Paragraph("Totales Pagos", fontEtiquetas));
        headerNodoTotales.setHorizontalAlignment(Element.ALIGN_RIGHT);
        headerNodoTotales.setBorder(Rectangle.NO_BORDER);
        tableTotalesHeader.addCell(headerNodoTotales);

        if(facturaDto.getComplementos() != null && facturaDto.getComplementos().getComplementoPago20() != null) {

        if (!Optional.ofNullable( facturaDto.getComplementos().getComplementoPago20().getTotales().getTotalRetencionesIVA()).isEmpty()) {
            PdfPCell totalRetencionesIVA = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.TOTAL_RETENCIONES_IVA).concat(": ").concat( 
                facturaDto.getComplementos().getComplementoPago20().getTotales().getTotalRetencionesIVA() != null
                ? String.valueOf(facturaDto.getComplementos().getComplementoPago20().getTotales().getTotalRetencionesIVA())
                : ""), fontContenido
            ));
            totalRetencionesIVA.setColspan(3);
            totalRetencionesIVA.setHorizontalAlignment(Element.ALIGN_RIGHT);
            totalRetencionesIVA.setBorder(Rectangle.NO_BORDER);
            tableTotalesHeader.addCell(totalRetencionesIVA);
        }

        if (!Optional.ofNullable( facturaDto.getComplementos().getComplementoPago20().getTotales().getTotalRetencionesISR()).isEmpty()) {
                PdfPCell totalRetencionesISR = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.TOTAL_RETENCIONES_ISR).concat(": ").concat(
                    facturaDto.getComplementos().getComplementoPago20().getTotales().getTotalRetencionesISR() != null
                    ? String.valueOf(facturaDto.getComplementos().getComplementoPago20().getTotales().getTotalRetencionesISR())
                    : ""), fontContenido
                )
            );
            totalRetencionesISR.setColspan(3);
            totalRetencionesISR.setHorizontalAlignment(Element.ALIGN_RIGHT);
            totalRetencionesISR.setBorder(Rectangle.NO_BORDER);
            tableTotalesHeader.addCell(totalRetencionesISR);
        }
        
        if (!Optional.ofNullable( facturaDto.getComplementos().getComplementoPago20().getTotales().getTotalRetencionesIEPS()).isEmpty()) {
            PdfPCell totalRetencionesIEPS = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.TOTAL_RETENCIONES_IEPS).concat(": ").concat(
                facturaDto.getComplementos().getComplementoPago20().getTotales().getTotalRetencionesIEPS() != null
                ? String.valueOf(facturaDto.getComplementos().getComplementoPago20().getTotales().getTotalRetencionesIEPS())
                : ""), fontContenido
                )
            );
            totalRetencionesIEPS.setColspan(3);
            totalRetencionesIEPS.setHorizontalAlignment(Element.ALIGN_RIGHT);
            totalRetencionesIEPS.setBorder(Rectangle.NO_BORDER);
            tableTotalesHeader.addCell(totalRetencionesIEPS);
        }


        if (!Optional.ofNullable( facturaDto.getComplementos().getComplementoPago20().getTotales().getTotalTrasladosBaseIVA16()).isEmpty()) {
            PdfPCell totalTrasladosBaseIVA16 = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.TOTAL_TRASLADOS_BASE_IVA_16).concat(": ").concat(
                facturaDto.getComplementos().getComplementoPago20().getTotales().getTotalTrasladosBaseIVA16() != null
                ? String.valueOf(facturaDto.getComplementos().getComplementoPago20().getTotales().getTotalTrasladosBaseIVA16())
                : ""), fontContenido
                )
            );
            totalTrasladosBaseIVA16.setColspan(3);
            totalTrasladosBaseIVA16.setHorizontalAlignment(Element.ALIGN_RIGHT);
            totalTrasladosBaseIVA16.setBorder(Rectangle.NO_BORDER);
            tableTotalesHeader.addCell(totalTrasladosBaseIVA16);
        }          

        if (!Optional.ofNullable( facturaDto.getComplementos().getComplementoPago20().getTotales().getTotalTrasladosImpuestoIVA16()).isEmpty()) {
            PdfPCell totalTrasladosImpuestoIVA16 = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.TOTAL_TRASLADOS_IMPUESTO_IVA_16).concat(": ").concat(
                    facturaDto.getComplementos().getComplementoPago20().getTotales().getTotalTrasladosImpuestoIVA16() != null
                    ? String.valueOf(facturaDto.getComplementos().getComplementoPago20().getTotales().getTotalTrasladosImpuestoIVA16())
                    : ""), fontContenido
                )
            );
            totalTrasladosImpuestoIVA16.setColspan(3);
            totalTrasladosImpuestoIVA16.setHorizontalAlignment(Element.ALIGN_RIGHT);
            totalTrasladosImpuestoIVA16.setBorder(Rectangle.NO_BORDER);
            tableTotalesHeader.addCell(totalTrasladosImpuestoIVA16);
        }
        

        if (!Optional.ofNullable( facturaDto.getComplementos().getComplementoPago20().getTotales().getTotalTrasladosBaseIVA8()).isEmpty()) {
                PdfPCell totalTrasladosBaseIVA8 = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.TOTAL_TRASLADOS_BASE_IVA8).concat(": ").concat(
                    facturaDto.getComplementos().getComplementoPago20().getTotales().getTotalTrasladosBaseIVA8() != null
                    ? String.valueOf(facturaDto.getComplementos().getComplementoPago20().getTotales().getTotalTrasladosBaseIVA8())
                    : ""), fontContenido
                )
            );
            totalTrasladosBaseIVA8.setColspan(3);
            totalTrasladosBaseIVA8.setHorizontalAlignment(Element.ALIGN_RIGHT);
            totalTrasladosBaseIVA8.setBorder(Rectangle.NO_BORDER);
            tableTotalesHeader.addCell(totalTrasladosBaseIVA8);
        }
        

        if (!Optional.ofNullable( facturaDto.getComplementos().getComplementoPago20().getTotales().getTotalTrasladosImpuestoIVA8()).isEmpty()) {
            PdfPCell totalTrasladosImpuestoIVA8 = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.TOTAL_TRASLADOS_IMPUESTO_IVA_8).concat(": ").concat(
                facturaDto.getComplementos().getComplementoPago20().getTotales().getTotalTrasladosImpuestoIVA8() != null
                ? String.valueOf(facturaDto.getComplementos().getComplementoPago20().getTotales().getTotalTrasladosImpuestoIVA8())
                : ""), fontContenido
            )
            );
            totalTrasladosImpuestoIVA8.setColspan(3);
            totalTrasladosImpuestoIVA8.setHorizontalAlignment(Element.ALIGN_RIGHT);
            totalTrasladosImpuestoIVA8.setBorder(Rectangle.NO_BORDER);
            tableTotalesHeader.addCell(totalTrasladosImpuestoIVA8);
        }
        

        if (!Optional.ofNullable( facturaDto.getComplementos().getComplementoPago20().getTotales().getTotalTrasladosBaseIVA0()).isEmpty()) {
                PdfPCell totalTrasladosBaseIVA0 = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.TOTAL_TRASLADOS_BASE_IVA_0).concat(": ").concat(
                    facturaDto.getComplementos().getComplementoPago20().getTotales().getTotalTrasladosBaseIVA0() != null
                    ? String.valueOf(facturaDto.getComplementos().getComplementoPago20().getTotales().getTotalTrasladosBaseIVA0())
                    : ""), fontContenido
                )
            );
            totalTrasladosBaseIVA0.setColspan(3);
            totalTrasladosBaseIVA0.setHorizontalAlignment(Element.ALIGN_RIGHT);
            totalTrasladosBaseIVA0.setBorder(Rectangle.NO_BORDER);
            tableTotalesHeader.addCell(totalTrasladosBaseIVA0);
        }
        

        if (!Optional.ofNullable( facturaDto.getComplementos().getComplementoPago20().getTotales().getTotalTrasladosImpuestoIVA0()).isEmpty()) {
                PdfPCell totalTrasladosImpuestoIVA0 = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.TOTAL_TRASLADOS_IMPUESTO_IVA_0).concat(": ").concat(
                    facturaDto.getComplementos().getComplementoPago20().getTotales().getTotalTrasladosImpuestoIVA0() != null
                    ? String.valueOf(facturaDto.getComplementos().getComplementoPago20().getTotales().getTotalTrasladosImpuestoIVA0())
                    : ""), fontContenido
                )
            );
            totalTrasladosImpuestoIVA0.setColspan(3);
            totalTrasladosImpuestoIVA0.setHorizontalAlignment(Element.ALIGN_RIGHT);
            totalTrasladosImpuestoIVA0.setBorder(Rectangle.NO_BORDER);
            tableTotalesHeader.addCell(totalTrasladosImpuestoIVA0);
        }

        if (!Optional.ofNullable( facturaDto.getComplementos().getComplementoPago20().getTotales().getTotalTrasladosBaseIVAExento()).isEmpty()) {
                PdfPCell totalTrasladosBaseIVAExento = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.TOTAL_TRASLADOS_BASE_IVA_EXENTO).concat(": ").concat(
                    facturaDto.getComplementos().getComplementoPago20().getTotales().getTotalTrasladosBaseIVAExento() != null
                    ? String.valueOf(facturaDto.getComplementos().getComplementoPago20().getTotales().getTotalTrasladosBaseIVAExento())
                    : ""), fontContenido
                )
            );
            totalTrasladosBaseIVAExento.setColspan(3);
            totalTrasladosBaseIVAExento.setHorizontalAlignment(Element.ALIGN_RIGHT);
            totalTrasladosBaseIVAExento.setBorder(Rectangle.NO_BORDER);
            tableTotalesHeader.addCell(totalTrasladosBaseIVAExento);
        }          

        if (!Optional.ofNullable( facturaDto.getComplementos().getComplementoPago20().getTotales().getTotalRetencionesIVA()).isEmpty()) {
                PdfPCell montoTotalPagos = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.MONTO_TOTAL_PAGOS).concat(": ").concat(
                    facturaDto.getComplementos().getComplementoPago20().getTotales().getMontoTotalPagos() != null
                    ? String.valueOf(facturaDto.getComplementos().getComplementoPago20().getTotales().getMontoTotalPagos())
                    : ""), fontContenido
                )
            );
            montoTotalPagos.setColspan(3);
            montoTotalPagos.setHorizontalAlignment(Element.ALIGN_RIGHT);
            montoTotalPagos.setBorder(Rectangle.NO_BORDER);
            tableTotalesHeader.addCell(montoTotalPagos);
        }
    }
        document.add(tableTotalesHeader);
    }

    private StringBuilder armarConceptoImpuestoPTraslados(ComplementoPago20.Pago  conceptos) {
        StringBuilder descripcion = new StringBuilder();
        try {
            for (Pago.ImpuestosP.TrasladosP.TrasladoP impuestosCargados : conceptos.getImpuestosP().getTrasladosP().getTrasladoP() ) {
                descripcion.append("\n");
                CatImpuestos obtenerCatImpuestosSatByClave = catalogosSatService.obtenerCatImpuestoSatByClave(impuestosCargados.getImpuestoP());
                descripcion.append(hmapTagIdioma.get(EnumTagPlantilla.IMP_TRASLADO).concat(": ").concat(obtenerCatImpuestosSatByClave.getDescripcion() != null ? obtenerCatImpuestosSatByClave.getDescripcion().concat("     ") : " "))
                            .append(hmapTagIdioma.get(EnumTagPlantilla.TASA_O_CUOTA).concat(": ").concat(String.valueOf(impuestosCargados.getTasaOCuotaP()) != null ? String.valueOf(impuestosCargados.getTasaOCuotaP()).concat("    ")  : ""))
                            .append(hmapTagIdioma.get(EnumTagPlantilla.TIPO_FACTOR).concat(": ").concat(impuestosCargados.getTipoFactorP() != null ? impuestosCargados.getTipoFactorP().concat("     ") : ""))
                            .append(hmapTagIdioma.get(EnumTagPlantilla.IMPORTE).concat(": $").concat(String.valueOf(impuestosCargados.getImporteP()) != null ? String.valueOf(impuestosCargados.getImporteP()).concat("     ") : ""))
                            .append(hmapTagIdioma.get(EnumTagPlantilla.BASE).concat(": ").concat(String.valueOf(impuestosCargados.getBaseP()) != null ? String.valueOf(impuestosCargados.getBaseP()).concat("    ") : ""));
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
            for (Pago.ImpuestosP.RetencionesP.RetencionP   impuestosCargados :  conceptos.getImpuestosP().getRetencionesP().getRetencionP() ) { 
                descripcion.append("\n");
                CatImpuestos obtenerCatImpuestosSatByClave = catalogosSatService.obtenerCatImpuestoSatByClave(impuestosCargados.getImpuestoP());
                descripcion.append(hmapTagIdioma.get(EnumTagPlantilla.IMP_RETENIDO).concat(": ").concat(obtenerCatImpuestosSatByClave.getDescripcion() != null ? obtenerCatImpuestosSatByClave.getDescripcion().concat("    ") : ""))
                        .append(hmapTagIdioma.get(EnumTagPlantilla.IMPORTE).concat(": $").concat(String.valueOf(impuestosCargados.getImporteP()) != null ? String.valueOf(impuestosCargados.getImporteP()).concat("    ") : ""));
            }
            return descripcion;
        } catch (Exception e) {
            System.out.println("Ocurrio un error al tratar de armar el concepto con impuestos retenido" + e.getMessage());
            return new StringBuilder("");
        }
    }

    private StringBuilder armarConceptoImpuestoDRTraslados(ComplementoPago20.Pago.DoctoRelacionado  conceptos) {
        StringBuilder descripcion = new StringBuilder();
        try {
            for (TrasladoDR  impuestosCargados : conceptos.getImpuestosDR().getTrasladosDR().getTrasladoDR()) {
                descripcion.append("\n");
                CatImpuestos obtenerCatImpuestosSatByClave = catalogosSatService.obtenerCatImpuestoSatByClave(impuestosCargados.getImpuestoDR());
                descripcion.append(hmapTagIdioma.get(EnumTagPlantilla.IMP_TRASLADO).concat(": ").concat((obtenerCatImpuestosSatByClave.getDescripcion() != null ? obtenerCatImpuestosSatByClave.getDescripcion().concat("    ") : ""))) 
                        .append(hmapTagIdioma.get(EnumTagPlantilla.TASA_O_CUOTA).concat(": ").concat(String.valueOf(impuestosCargados.getTasaOCuotaDR()) != null ? String.valueOf(impuestosCargados.getTasaOCuotaDR()).concat("    ") : "")) 
                        .append(hmapTagIdioma.get(EnumTagPlantilla.TIPO_FACTOR).concat(": ").concat(impuestosCargados.getTipoFactorDR() != null ? impuestosCargados.getTipoFactorDR().concat("    ") : "")) 
                        .append(hmapTagIdioma.get(EnumTagPlantilla.IMPORTE).concat(": $").concat(String.valueOf(impuestosCargados.getImporteDR()) != null ? String.valueOf(impuestosCargados.getImporteDR()).concat("    ") : ""))
                        .append(hmapTagIdioma.get(EnumTagPlantilla.BASE).concat(": ").concat(String.valueOf(impuestosCargados.getBaseDR()) != null ? String.valueOf(impuestosCargados.getBaseDR()).concat("    ") : ""));
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
                descripcion.append(hmapTagIdioma.get(EnumTagPlantilla.IMP_RETENIDO).concat(": ").concat(obtenerCatImpuestosSatByClave.getDescripcion() != null ? obtenerCatImpuestosSatByClave.getDescripcion().concat("     ") : ""))
                        .append(hmapTagIdioma.get(EnumTagPlantilla.TASA_O_CUOTA).concat(": ").concat(String.valueOf(impuestosCargados.getTasaOCuotaDR()) != null ? String.valueOf(impuestosCargados.getTasaOCuotaDR()).concat("     ") : ""))
                        .append(hmapTagIdioma.get(EnumTagPlantilla.TIPO_FACTOR).concat(": ").concat(impuestosCargados.getTipoFactorDR() != null ? impuestosCargados.getTipoFactorDR().concat("     ") : ""))
                        .append(hmapTagIdioma.get(EnumTagPlantilla.IMPORTE).concat(": $").concat(String.valueOf(impuestosCargados.getImporteDR()) != null ? String.valueOf(impuestosCargados.getImporteDR()).concat("     ") : ""))
                        .append(hmapTagIdioma.get(EnumTagPlantilla.BASE).concat(": ").concat(String.valueOf(impuestosCargados.getBaseDR()) != null ? String.valueOf(impuestosCargados.getBaseDR()).concat("    ") : ""));
            }
            return descripcion;
        } catch (Exception e) {
            System.out.println("Ocurrio un error al tratar de armar el concepto con impuestos retenido" + e.getMessage());
            return new StringBuilder("");
        }
    }

    public StringBuilder armarConceptoImpuestoTraslados(Conceptos conceptos) {
        StringBuilder descConcepto = new StringBuilder();
        try {
            for (Conceptos.Traslados impuestosCargados : conceptos.getImpuestos().getTraslados()) {
                descConcepto.append("\n");
                CatImpuestos obtenerImpuestoCatalogo = catalogosSatService.obtenerCatImpuestoSatByClave(impuestosCargados.getImpuesto());
                descConcepto.append("Impuesto:  ").append((obtenerImpuestoCatalogo.getDescripcion() != null && !obtenerImpuestoCatalogo.getDescripcion().trim().equals("") ? obtenerImpuestoCatalogo.getDescripcion() : ""))
                        .append("  Tasa/Cuota: ").append((String.valueOf(impuestosCargados.getTasaoCuota() != null && !impuestosCargados.getTasaoCuota().equals(BigDecimal.ZERO) ? impuestosCargados.getTasaoCuota() : "")))
                        .append("  Tipo Factor: ").append((impuestosCargados.getTipoFactor() != null && !impuestosCargados.getTipoFactor().trim().equals("") ? impuestosCargados.getTipoFactor() : ""))
                        .append("  Importe $").append((String.valueOf(impuestosCargados.getImporte() != null && !impuestosCargados.getImporte().equals(BigDecimal.ZERO) ? impuestosCargados.getImporte() : "")))
                        .append("  Base: ").append(impuestosCargados.getBase() != null ? impuestosCargados.getBase() : "");;
            }
            return descConcepto;
        } catch (Exception e) {
            System.out.println("Ocurrio un error al tratar de armar el concepto con impuestos trasladado" + e.getMessage());
            return new StringBuilder("");
        }
    }

    /**
     * *
     * Metodo para agregar la leyenda
     *
     * @param document
     * @throws DocumentException
     */
    public void agregarLeyenda(Document document, Donatarias donatarias) throws DocumentException {
        PdfPTable tableLeyenda = new PdfPTable(1);
        tableLeyenda.setWidths(new int[]{560});
        tableLeyenda.setTotalWidth(ANCHO_TOTAL_TABLA);
        tableLeyenda.setLockedWidth(true);
        tableLeyenda.setWidthPercentage(PORCENTAJE_ANCHO_TABLA);
        tableLeyenda.getDefaultCell().setBorder(Rectangle.NO_BORDER);

        PdfPCell leyenda = new PdfPCell(new Phrase(donatarias.getLeyenda(), fontContenido));
//        if (facturaDto.getTenantAData() != null && facturaDto.getTenantAData().getTipoInstitucionDonativo() != null) {
//
//            switch (facturaDto.getTenantAData().getTipoInstitucionDonativo()) {
//                case GOBIERNO:
//                    
//                    leyenda = new PdfPCell(new Phrase(hmapTagIdioma.get(EnumTagPlantilla.LEYENDA_GOBIERNO), fontContenido));
//                    break;
//
//                case PARTICULAR:
//
//                    leyenda = new PdfPCell(new Phrase(hmapTagIdioma.get(EnumTagPlantilla.F_MENSAJEDONATIVOS1), fontContenido));
//                    break;
//
//                default:
//                    leyenda = new PdfPCell(new Phrase(hmapTagIdioma.get(EnumTagPlantilla.F_MENSAJEDONATIVOS1), fontContenido));
//                    break;
//            }
//
//        } else {
//
//            leyenda = new PdfPCell(new Phrase(hmapTagIdioma.get(EnumTagPlantilla.F_MENSAJEDONATIVOS1), fontContenido));
//        }
        
        leyenda.setHorizontalAlignment(Element.ALIGN_LEFT);
        leyenda.setFixedHeight(28f);
        leyenda.setBorder(Rectangle.NO_BORDER);

        PdfPCell spaceLeyenda = new PdfPCell();
        spaceLeyenda.setBackgroundColor(BaseColor.BLACK);
        spaceLeyenda.setFixedHeight(1.5f);
        spaceLeyenda.setBorder(Rectangle.NO_BORDER);

        tableLeyenda.addCell(leyenda);
        tableLeyenda.addCell(spaceLeyenda);

        document.add(tableLeyenda);
    }

    /**
     * *
     * Metodo para agregar los sellos
     *
     * @param document
     * @return Tabla Sellos
     * @throws DocumentException
     */
    public void agregarSellos(Document document, CFDI facturaDto) throws DocumentException {
        PdfPTable tableSello = new PdfPTable(1);
        tableSello.setWidths(new int[]{480});
        tableSello.setTotalWidth(ANCHO_TOTAL_TABLA);
        tableSello.setLockedWidth(true);
        tableSello.setWidthPercentage(PORCENTAJE_ANCHO_TABLA);
        tableSello.getDefaultCell().setBorder(Rectangle.NO_BORDER);

        PdfPCell selloEmisorC = new PdfPCell(new Phrase(hmapTagIdioma.get(EnumTagPlantilla.S_SD).concat(": "), fontEtiquetas));
        selloEmisorC.setHorizontalAlignment(Element.ALIGN_LEFT);
        selloEmisorC.setBorder(Rectangle.NO_BORDER);

        PdfPCell selloEmisorV = new PdfPCell(new Phrase(facturaDto.getTimbrado().getSelloCFDI(), fontLeyendas));
        selloEmisorV.setHorizontalAlignment(Element.ALIGN_LEFT);
        selloEmisorV.setBorder(Rectangle.NO_BORDER);

        PdfPCell cadenaOriginalC = new PdfPCell(new Phrase(hmapTagIdioma.get(EnumTagPlantilla.S_CADENACDSAT).concat(": "), fontEtiquetas));
        cadenaOriginalC.setHorizontalAlignment(Element.ALIGN_LEFT);
        cadenaOriginalC.setBorder(Rectangle.NO_BORDER);

        PdfPCell cadenaOriginalV = new PdfPCell(new Phrase(facturaDto.getTimbrado().getCadenaDatosTimbrado(), fontLeyendas));
        cadenaOriginalV.setHorizontalAlignment(Element.ALIGN_LEFT);
        cadenaOriginalV.setBorder(Rectangle.NO_BORDER);

        PdfPCell selloSATC = new PdfPCell(new Phrase(hmapTagIdioma.get(EnumTagPlantilla.S_SDCFDI).concat(": "), fontEtiquetas));
        selloSATC.setHorizontalAlignment(Element.ALIGN_LEFT);
        selloSATC.setBorder(Rectangle.NO_BORDER);

        PdfPCell selloSATV = new PdfPCell(new Phrase(facturaDto.getTimbrado().getSelloSAT(), fontLeyendas));
        selloSATV.setHorizontalAlignment(Element.ALIGN_LEFT);
        selloSATV.setBorder(Rectangle.NO_BORDER);

        PdfPCell cellVacia = new PdfPCell();
        cellVacia.setHorizontalAlignment(Element.ALIGN_LEFT);
        cellVacia.setBorder(Rectangle.NO_BORDER);

        PdfPCell exportacionC = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.E_EXPORTACION).concat(": ").concat(facturaDto.getExportacion() != null ? facturaDto.getExportacion() : ""), fontEtiquetas));
        exportacionC.setHorizontalAlignment(Element.ALIGN_LEFT);
        exportacionC.setBorder(Rectangle.NO_BORDER);

        tableSello.addCell(selloEmisorC);
        tableSello.addCell(selloEmisorV);
        tableSello.addCell(cadenaOriginalC);
        tableSello.addCell(cadenaOriginalV);
        tableSello.addCell(selloSATC);
        tableSello.addCell(selloSATV);
        tableSello.addCell(exportacionC);

        document.add(tableSello);
    }

    /**
     * *
     * Metodo para agregar los datos finales
     *
     * @param document
     * @return Tabla DatosFinales
     * @throws DocumentException
     */
    public void agregarDatosFinales(Document document, CFDI facturaDto, IdentificadorSucursal identificadorSucursal) throws DocumentException {
        PdfPTable tableDatosFinales = new PdfPTable(2);
        tableDatosFinales.setWidths(new int[]{85, 85});
        tableDatosFinales.setTotalWidth(ANCHO_TOTAL_TABLA);
        tableDatosFinales.setLockedWidth(true);
        tableDatosFinales.setWidthPercentage(PORCENTAJE_ANCHO_TABLA);
        tableDatosFinales.getDefaultCell().setFixedHeight(80f);
        tableDatosFinales.getDefaultCell().setBorder(Rectangle.NO_BORDER);

        PdfPCell folioFiscalC = new PdfPCell(new Phrase(hmapTagIdioma.get(EnumTagPlantilla.E_FOLIOFISCAL), fontEtiquetas));
        folioFiscalC.setHorizontalAlignment(Element.ALIGN_LEFT);
        folioFiscalC.setBorder(Rectangle.NO_BORDER);

        PdfPCell folioFiscalV = new PdfPCell(new Phrase(facturaDto.getTimbrado().getUuid(), fontContenido));
        folioFiscalV.setHorizontalAlignment(Element.ALIGN_LEFT);
        folioFiscalV.setBorder(Rectangle.NO_BORDER);

        PdfPCell cellEmisionFechaCertificacion = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.N_FECHAHORACERTIFICACION).concat(": "), fontEtiquetas));
        cellEmisionFechaCertificacion.setHorizontalAlignment(Element.ALIGN_LEFT);
        cellEmisionFechaCertificacion.setBorder(Rectangle.NO_BORDER);

        String lugarFechaEmision = "";
        if (facturaDto.getTimbrado().getFechaTimbrado() != null) {
            Date dateCheckIn = facturaDto.getTimbrado().getFechaTimbrado();
            DateFormat dateFormatCheckIn = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            lugarFechaEmision = dateFormatCheckIn.format(dateCheckIn);
        }

        PdfPCell cellEmisionFechaCertificacionV = new PdfPCell(new Paragraph(lugarFechaEmision, fontContenido));
        cellEmisionFechaCertificacionV.setHorizontalAlignment(Element.ALIGN_LEFT);
        cellEmisionFechaCertificacionV.setBorder(Rectangle.NO_BORDER);

        PdfPCell cellEmisionCertificadoEmisor = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.N_NOSERIEEMISOR).concat(": "), fontEtiquetas));
        cellEmisionCertificadoEmisor.setHorizontalAlignment(Element.ALIGN_LEFT);
        cellEmisionCertificadoEmisor.setBorder(Rectangle.NO_BORDER);

        PdfPCell cellEmisionCertificadoEmisorV = new PdfPCell(new Paragraph(identificadorSucursal.getEmisorId().getNumCertificado(), fontContenido));
        cellEmisionCertificadoEmisorV.setHorizontalAlignment(Element.ALIGN_LEFT);
        cellEmisionCertificadoEmisorV.setBorder(Rectangle.NO_BORDER);

        PdfPCell cellEmisionCertificadoSAT = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.E_NOCERTSAT).concat(": "), fontEtiquetas));
        cellEmisionCertificadoSAT.setHorizontalAlignment(Element.ALIGN_LEFT);
        cellEmisionCertificadoSAT.setBorder(Rectangle.NO_BORDER);

        PdfPCell cellEmisionCertificadoSATV = new PdfPCell(new Paragraph(facturaDto.getTimbrado().getNumCertificadoSAT(), fontContenido));
        cellEmisionCertificadoSATV.setHorizontalAlignment(Element.ALIGN_LEFT);
        cellEmisionCertificadoSATV.setBorder(Rectangle.NO_BORDER);

        PdfPCell cellEmisionCondiciones = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.G_CONDICIONESPAGO), fontEtiquetas));
        cellEmisionCondiciones.setHorizontalAlignment(Element.ALIGN_LEFT);
        cellEmisionCondiciones.setBorder(Rectangle.NO_BORDER);

        PdfPCell cellEmisionCondicionesV = new PdfPCell(new Paragraph(facturaDto.getCondicionesDePago() != null ? facturaDto.getCondicionesDePago() : "", fontContenido));
        cellEmisionCondicionesV.setHorizontalAlignment(Element.ALIGN_LEFT);
        cellEmisionCondicionesV.setBorder(Rectangle.NO_BORDER);

        PdfPCell cellEmisionMetodoPago = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.T_METODOPAGO).concat(": "), fontEtiquetas));
        cellEmisionMetodoPago.setHorizontalAlignment(Element.ALIGN_LEFT);
        cellEmisionMetodoPago.setBorder(Rectangle.NO_BORDER);

        PdfPCell cellEmisionMetodoPagoV = new PdfPCell(new Paragraph(facturaDto.getMetodoPago() != null ? facturaDto.getMetodoPago() : "", fontContenido));
        cellEmisionMetodoPagoV.setHorizontalAlignment(Element.ALIGN_LEFT);
        cellEmisionMetodoPagoV.setBorder(Rectangle.NO_BORDER);

        PdfPCell cellEmisionFormaPago = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.T_FORMAPAGO).concat(": "), fontEtiquetas));
        cellEmisionFormaPago.setHorizontalAlignment(Element.ALIGN_LEFT);
        cellEmisionFormaPago.setBorder(Rectangle.NO_BORDER);

        String formaPagoDescripcion = "";
        if (facturaDto.getFormaPago() != null && !facturaDto.getFormaPago().isEmpty()) {
            formaPagoDescripcion = facturaDto.getFormaPago().trim();
        }
        PdfPCell cellEmisionFormaPagoV = new PdfPCell(new Paragraph(formaPagoDescripcion.toString(), fontContenido));
        cellEmisionFormaPagoV.setHorizontalAlignment(Element.ALIGN_LEFT);
        cellEmisionFormaPagoV.setBorder(Rectangle.NO_BORDER);

        PdfPCell cellEmisionMonedaC = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.N_MONEDA) + ": ", fontEtiquetas));
        cellEmisionMonedaC.setHorizontalAlignment(Element.ALIGN_LEFT);
        cellEmisionMonedaC.setBorder(Rectangle.NO_BORDER);

        PdfPCell cellEmisionMonedaV = new PdfPCell(new Paragraph(facturaDto.getMoneda() != null ? facturaDto.getMoneda() : "", fontContenido));
        cellEmisionMonedaV.setHorizontalAlignment(Element.ALIGN_LEFT);
        cellEmisionMonedaV.setBorder(Rectangle.NO_BORDER);

        PdfPCell tipoCambioC = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.C_TIPOCAMBIO).concat(": "), fontEtiquetas));
        tipoCambioC.setHorizontalAlignment(Element.ALIGN_LEFT);
        tipoCambioC.setBorder(Rectangle.NO_BORDER);

        PdfPCell tipoCambioV = new PdfPCell(new Paragraph(String.valueOf(facturaDto.getTipoCambio() != null ? facturaDto.getTipoCambio() : ""), fontContenido));
        tipoCambioV.setHorizontalAlignment(Element.ALIGN_LEFT);
        tipoCambioV.setBorder(Rectangle.NO_BORDER);

        tableDatosFinales.addCell(folioFiscalC);
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
        tableDatosFinales.addCell(cellEmisionFormaPagoV);
        tableDatosFinales.addCell(cellEmisionMonedaC);
        tableDatosFinales.addCell(cellEmisionMonedaV);
        tableDatosFinales.addCell(tipoCambioC);
        tableDatosFinales.addCell(tipoCambioV);

        document.add(tableDatosFinales);

    }

    /**
     * *
     * Metodo para agregar el codigo QR
     *
     * @param document
     * @return Tabla CodigoQR
     * @throws DocumentException
     */
    public void agregarCodigoQR(Document document, CFDI facturaDto) throws DocumentException {
        PdfPTable tableCodigoQR = new PdfPTable(2);
        tableCodigoQR.setWidths(new int[]{100, 100});
        tableCodigoQR.setTotalWidth(ANCHO_TOTAL_TABLA);
        tableCodigoQR.setLockedWidth(true);
        tableCodigoQR.setWidthPercentage(PORCENTAJE_ANCHO_TABLA);
        tableCodigoQR.getDefaultCell().setBorder(Rectangle.NO_BORDER);
        tableCodigoQR.getDefaultCell().setFixedHeight(100f);
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
     * *
     * Metodo para agregar la leyenda final
     *
     * @param document
     * @return Tabla leyenda
     * @throws DocumentException
     */
    public void agregarLeyendFinal(Document document) throws DocumentException {
        PdfPTable tableLeyenda = new PdfPTable(2);
        tableLeyenda.setWidths(new int[]{85, 85});
        tableLeyenda.setTotalWidth(420);
        tableLeyenda.setLockedWidth(true);
        tableLeyenda.setWidthPercentage(PORCENTAJE_ANCHO_TABLA);
        tableLeyenda.getDefaultCell().setFixedHeight(80f);
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

  
    class tableHeader extends PdfPageEventHelper {

        /**
         * The header text.
         */
        String header;
        /**
         * The template with the total number of pages.
         */
        PdfTemplate totalPagina;

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
                Font fontEtiqueta = new Font(Font.FontFamily.HELVETICA, 5, Font.NORMAL, BaseColor.BLACK);
                table.setWidths(new int[]{24, 24, 2});
                table.setTotalWidth(527);
                table.setLockedWidth(true);
                table.getDefaultCell().setFixedHeight(2);
                table.getDefaultCell().setBorder(Rectangle.NO_BORDER);
                Paragraph paragraph1 = new Paragraph("", fontEtiqueta);
                table.addCell(paragraph1);
                table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);

                Paragraph paragraph = new Paragraph(String.format("Página %d de", writer.getPageNumber()), fontEtiqueta);
                table.addCell(paragraph);
                Image instance = Image.getInstance(totalPagina);
                instance.scalePercent(60);
                PdfPCell cell = new PdfPCell(Image.getInstance(instance));
                cell.setBorder(Rectangle.NO_BORDER);
                table.addCell(cell);
                table.writeSelectedRows(0, -1, 34, 20, writer.getDirectContent());
                
                
//                switch (identificadorSucursal.getTipoElaboracion()) {
//                    case TEST:
//
//                        PdfContentByte contentByte = writer.getDirectContent();
//                        PdfTemplate template = contentByte.createTemplate(700, 300);
//                        template.beginText();
//                        BaseFont baseFont = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
//                        template.setFontAndSize(baseFont, 50);
//
//                        for (int page = 0; page <= writer.getPageNumber(); page++) {
//                            template.setTextMatrix(0, 0);
//                            template.showText("SIN VALIDEZ FISCAL");
//                        }
//
//                        template.endText();
//                        PdfGState pdfGState = new PdfGState();
//                        pdfGState.setFillOpacity(0.3f);
//                        contentByte.setGState(pdfGState);
//                        contentByte.addTemplate(template, 1, 1, -1, 1, 100, 110);
//
//                        break;
//                    default:
//                        break;
//                }
                
                
                
            } catch (DocumentException de) {
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
