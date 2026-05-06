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

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.ExceptionConverter;
import com.itextpdf.text.Font;
import com.itextpdf.text.Font.FontFamily;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.cfdi40.pdfgen.util.Utilities;
import com.itextpdf.text.pdf.BarcodeQRCode;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfTemplate;
import com.itextpdf.text.pdf.PdfWriter;
import com.cfdi40.exceptionhandlerstarter.exception.BusinessException;
import com.cfdi40.pdfgen.dto.CatRegimenContratacion;
import com.cfdi40.pdfgen.dto.CatRiesgoPuesto;
import com.cfdi40.pdfgen.dto.CatTipoContrato;
import com.cfdi40.pdfgen.dto.CatTipoIncapacidad;
import com.cfdi40.pdfgen.dto.CatTipoJornada;
import com.cfdi40.pdfgen.dto.CatTipoOtroPago;
import com.cfdi40.pdfgen.model.entity.CatIdiomapdf;
import com.cfdi40.pdfgen.model.entity.CatTagxidioma;
import com.cfdi40.pdfgen.model.entity.TipoPeriodicidad;
import com.cfdi40.pdfgen.model.repository.CatIdiomapdfRepository;
import com.cfdi40.pdfgen.model.repository.CatTagxidiomaRepository;
import com.cfdi40.pdfgen.model.repository.TipoPeriodicidadRepository;
import com.cfdi40.pdfgen.util.EnumTagPlantilla;
import com.cfdi40.pdfgen.util.FechasUtil;
import com.cfdi40.pdfgen.tenants.tenanta.cfdi.dto.CFDI;
import com.cfdi40.pdfgen.tenants.tenanta.cfdi.dto.CFDI.Conceptos;
import com.cfdi40.pdfgen.tenants.tenanta.cfdi.dto.CFDI.Complementos.Nomina.Deducciones.Deduccion;
import com.cfdi40.pdfgen.tenants.tenanta.cfdi.dto.CFDI.Complementos.Nomina.Incapacidades.Incapacidad;
import com.cfdi40.pdfgen.tenants.tenanta.cfdi.dto.CFDI.Complementos.Nomina.OtrosPagos.OtroPago;
import com.cfdi40.pdfgen.tenants.tenanta.cfdi.dto.CFDI.Complementos.Nomina.Percepciones.Percepcion;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.w3c.dom.NodeList;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

@Service

public class PdfNominaServiceImpl extends PdfPageEventHelper implements PdfNominaService {

    @Autowired
    CatTagxidiomaRepository catTagxidiomaRepository; 
    @Autowired
    CatIdiomapdfRepository catIdiomapdfRepository;
    @org.springframework.beans.factory.annotation.Autowired
    CatalogStaticFallbackService catalogFallback;
    @Autowired
    TipoPeriodicidadRepository tipoPeriodicidadRepository; 
    @Autowired
    CatalogosSATService catalogosSatService;
    Font fontEtiqueta = new Font(FontFamily.HELVETICA, 5, Font.BOLD, BaseColor.BLACK);
    Font fontContenido = new Font(FontFamily.HELVETICA, 5, Font.NORMAL, BaseColor.BLACK);
    private HashMap<EnumTagPlantilla, String> hmapTagIdioma;
    private List<CatTagxidioma> catTagxidioma ;

    @Override
    public ByteArrayOutputStream generarPDfFactura(CFDI facturaDto) throws BusinessException {
        try {
            ByteArrayOutputStream pdfResponse = null;
            CFDI.Complementos.Nomina nomina = facturaDto.getComplementos().getNomina();

            CatIdiomapdf catIdioma = null;
    
            catIdioma = catalogFallback.findIdiomaSafe(1);
            catTagxidioma = catalogFallback.findTagsSafe(catIdioma);
            hmapTagIdioma = Utilities.obtenerTagByIdioma(catTagxidioma);
            
         

            // Document document = new Document(PageSize.LETTER, 8.0f, 8.0f, 10.0f, 10.0f);//establece margenes y tamaÃƒÂ±o de pagina
            // PdfWriter writer = PdfWriter.getInstance(document, pdfResponse);
            // TableHeader event = new TableHeader();
            // writer.setPageEvent(event);

            Document document = new Document();
            document = new Document(PageSize.LETTER, 8.0f, 8.0f, 10.0f, 10.0f);
            pdfResponse = new ByteArrayOutputStream();
            PdfWriter writer = PdfWriter.getInstance(document, pdfResponse);  

        //  PdfWriter writer2 = PdfWriter.getInstance(document, new FileOutputStream(new File(rutaPdf)));
                       
            
            document.open();
            agregarEncabezadoSucursal(document, facturaDto, nomina);
            agregarDatosEmpleado(document, facturaDto, nomina);
            agregarDatosNomina(document,nomina,facturaDto);
            agregarDatosComplementarios(document, nomina);
            agregarEncabezadosPercepcionesYDeducciones(document, facturaDto);
            agregarPercepcionesYDeducciones(document, facturaDto);
            agregarEncabezadosOtrosPagos(document, facturaDto);
            agregarOtrosPagos(document, facturaDto);
            agregarConceptos(document, facturaDto);
            agregarSubtotales(document, facturaDto);
            agregarSellos(document, facturaDto);
            agregarDatosEmisionYQr(document, facturaDto, nomina);
            agregarFirmaEmpleado(document);
            

            document.close();
            writer.flush();
            return pdfResponse;
            
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException(HttpStatus.BAD_REQUEST,"Ocurrio un error al tratar de generar el PDF de Nómina de la factura con UUID "
                    + (facturaDto.getTimbrado() != null ? facturaDto.getTimbrado().getUuid() : "") + " Error: " + e.getMessage() + e.getStackTrace()[0].getLineNumber() + "Metodo " + e.getStackTrace()[0].getMethodName());
        }
    }

    /**
     * Metodo para agregar los encabezados de la Sucursal Nombre, RFC, etc.
     *
     * @param document
     * @return Tabla con el encabezado de la sucursal
     * @throws DocumentException
     */
    public void agregarEncabezadoSucursal(Document document, CFDI facturaDto, CFDI.Complementos.Nomina nomina) throws DocumentException {
        PdfPTable tableEncabezadoSucursal = new PdfPTable(1);
        tableEncabezadoSucursal.setWidths(new int[]{15});
        tableEncabezadoSucursal.setTotalWidth(560);
        tableEncabezadoSucursal.setLockedWidth(true);
        tableEncabezadoSucursal.setWidthPercentage(100);
        tableEncabezadoSucursal.getDefaultCell().setBorder(Rectangle.NO_BORDER);

        PdfPCell cellNombreSucursal = new PdfPCell(new Paragraph(facturaDto.getEmisor().getNombre() != null ? facturaDto.getEmisor().getNombre() : "", fontEtiqueta));
        cellNombreSucursal.setHorizontalAlignment(Element.ALIGN_LEFT);
        cellNombreSucursal.setBorder(Rectangle.NO_BORDER);

        PdfPCell cellRFCSucursal = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.RFC).concat(" : ").concat(facturaDto.getEmisor().getRfc() != null ? facturaDto.getEmisor().getRfc() : ""), fontContenido));
        cellRFCSucursal.setPaddingTop(2f);
        cellRFCSucursal.setHorizontalAlignment(Element.ALIGN_LEFT);
        cellRFCSucursal.setBorder(Rectangle.NO_BORDER);

        PdfPCell cellRegistroPatronal = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.N_REGISTROPATRONAL).concat(" : ").concat(nomina.getEmisor().getRegistroPatronal() != null ? nomina.getEmisor().getRegistroPatronal() : ""), fontContenido));
        cellRegistroPatronal.setPaddingTop(2f);
        cellRegistroPatronal.setHorizontalAlignment(Element.ALIGN_LEFT);
        cellRegistroPatronal.setBorder(Rectangle.NO_BORDER);

        PdfPCell cellRegimenFiscal = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.N_REGIMENFISCAL).concat(" : ").concat(facturaDto.getEmisor().getRegimenFiscal() != null ? facturaDto.getEmisor().getRegimenFiscal() : ""), fontContenido));
        cellRegimenFiscal.setPaddingTop(2f);
        cellRegimenFiscal.setHorizontalAlignment(Element.ALIGN_LEFT);
        cellRegimenFiscal.setBorder(Rectangle.NO_BORDER);

        tableEncabezadoSucursal.addCell(cellNombreSucursal);
        tableEncabezadoSucursal.addCell(cellRFCSucursal);
        tableEncabezadoSucursal.addCell(cellRegistroPatronal);
        tableEncabezadoSucursal.addCell(cellRegimenFiscal);

        document.add(tableEncabezadoSucursal); 
    }

    /**
     * Metodo para agregar los datos del empleado Nombre empleado, RFC empleado,
     * N° Seguridad Social, etc.
     *
     * @param document
     * @return Tabla para agregar los datos del empleado
     * @throws DocumentException
     */
    public void agregarDatosEmpleado(Document document, CFDI facturaDto, CFDI.Complementos.Nomina nomina) throws DocumentException {
        PdfPTable tableDatosEmpleado = new PdfPTable(4);
        tableDatosEmpleado.setWidths(new int[]{20, 40, 20, 40});
        tableDatosEmpleado.setTotalWidth(560);
        tableDatosEmpleado.setLockedWidth(true);
        tableDatosEmpleado.setWidthPercentage(100);
        tableDatosEmpleado.getDefaultCell().setBorder(Rectangle.NO_BORDER);

        PdfPCell borderEncabezadoSucursal = new PdfPCell();
        borderEncabezadoSucursal.setColspan(4);
        borderEncabezadoSucursal.setBackgroundColor(BaseColor.LIGHT_GRAY);
        borderEncabezadoSucursal.setHorizontalAlignment(Element.ALIGN_LEFT);
        borderEncabezadoSucursal.setBorder(Rectangle.NO_BORDER);

        PdfPCell cellNombreEmpleado = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.NOMBRE).concat(" : "), fontEtiqueta));
        cellNombreEmpleado.setHorizontalAlignment(Element.ALIGN_LEFT);
        cellNombreEmpleado.setBorder(Rectangle.NO_BORDER);

        PdfPCell cellNombreEmpleadoV = new PdfPCell(new Paragraph(facturaDto.getReceptor().getNombre() != null ? facturaDto.getReceptor().getNombre() : "", fontContenido));
        cellNombreEmpleadoV.setHorizontalAlignment(Element.ALIGN_LEFT);
        cellNombreEmpleadoV.setBorder(Rectangle.NO_BORDER);

        PdfPCell cellNoSeguroSocial = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.N_NOSEGURIDADSOCIAL).concat(" : "), fontEtiqueta));
        cellNoSeguroSocial.setHorizontalAlignment(Element.ALIGN_LEFT);
        cellNoSeguroSocial.setBorder(Rectangle.NO_BORDER);

        PdfPCell cellNoSeguroSocialV = new PdfPCell(new Paragraph(nomina.getReceptor() != null ? nomina.getReceptor().getNumSeguridadSocial() : "", fontContenido));
        cellNoSeguroSocialV.setHorizontalAlignment(Element.ALIGN_LEFT);
        cellNoSeguroSocialV.setBorder(Rectangle.NO_BORDER);

        PdfPCell cellNoEmpleado = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.N_NOEMPLEADO).concat(" : "), fontEtiqueta));
        cellNoEmpleado.setHorizontalAlignment(Element.ALIGN_LEFT);
        cellNoEmpleado.setBorder(Rectangle.NO_BORDER);

        PdfPCell cellNoEmpleadoV = new PdfPCell(new Paragraph(nomina.getReceptor() != null ? nomina.getReceptor().getNumEmpleado() : "", fontContenido));
        cellNoEmpleadoV.setHorizontalAlignment(Element.ALIGN_LEFT);
        cellNoEmpleadoV.setBorder(Rectangle.NO_BORDER);

        PdfPCell cellCurpEmpleado = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.N_CURP).concat(" : "), fontEtiqueta));
        cellCurpEmpleado.setHorizontalAlignment(Element.ALIGN_LEFT);
        cellCurpEmpleado.setBorder(Rectangle.NO_BORDER);

        PdfPCell cellCurpEmpleadoV = new PdfPCell(new Paragraph(nomina.getReceptor() != null ? nomina.getReceptor().getCurp() : "", fontContenido));
        cellCurpEmpleadoV.setHorizontalAlignment(Element.ALIGN_LEFT);
        cellCurpEmpleadoV.setBorder(Rectangle.NO_BORDER);

        PdfPCell cellRFCEmpleado = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.RFC).concat(" : "), fontEtiqueta));
        cellRFCEmpleado.setHorizontalAlignment(Element.ALIGN_LEFT);
        cellRFCEmpleado.setBorder(Rectangle.NO_BORDER);

        PdfPCell cellRFCEmpleadoV = new PdfPCell(new Paragraph(facturaDto.getReceptor() != null ? facturaDto.getReceptor().getRfc() : "", fontContenido));
        cellRFCEmpleadoV.setHorizontalAlignment(Element.ALIGN_LEFT);
        cellRFCEmpleadoV.setBorder(Rectangle.NO_BORDER);

        PdfPCell cellSindicalizadoEmpleado = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.N_SINDICALIZADO).concat(" : "), fontEtiqueta));
        cellSindicalizadoEmpleado.setHorizontalAlignment(Element.ALIGN_LEFT);
        cellSindicalizadoEmpleado.setBorder(Rectangle.NO_BORDER);

        PdfPCell cellSindicalizadoEmpleadoV = new PdfPCell(new Paragraph(nomina.getReceptor().getSindicalizado(), fontContenido));
        cellSindicalizadoEmpleadoV.setHorizontalAlignment(Element.ALIGN_LEFT);
        cellSindicalizadoEmpleadoV.setBorder(Rectangle.NO_BORDER);

        PdfPCell cellUsoCFDI = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.G_USOCFDI).concat(" : "), fontEtiqueta));
        cellUsoCFDI.setHorizontalAlignment(Element.ALIGN_LEFT);
        cellUsoCFDI.setBorder(Rectangle.NO_BORDER);

        PdfPCell cellUsoCFDIV = new PdfPCell(new Paragraph(facturaDto.getReceptor().getUsoCFDI(), fontContenido));
        cellUsoCFDIV.setHorizontalAlignment(Element.ALIGN_LEFT);
        cellUsoCFDIV.setBorder(Rectangle.NO_BORDER);

        PdfPCell celdaVacia = new PdfPCell();
        celdaVacia.setBorder(Rectangle.NO_BORDER);

        tableDatosEmpleado.addCell(borderEncabezadoSucursal);
        tableDatosEmpleado.addCell(cellNombreEmpleado);
        tableDatosEmpleado.addCell(cellNombreEmpleadoV);
        tableDatosEmpleado.addCell(cellNoSeguroSocial);
        tableDatosEmpleado.addCell(cellNoSeguroSocialV);
        tableDatosEmpleado.addCell(cellNoEmpleado);
        tableDatosEmpleado.addCell(cellNoEmpleadoV);
        tableDatosEmpleado.addCell(cellCurpEmpleado);
        tableDatosEmpleado.addCell(cellCurpEmpleadoV);
        tableDatosEmpleado.addCell(cellRFCEmpleado);
        tableDatosEmpleado.addCell(cellRFCEmpleadoV);
        tableDatosEmpleado.addCell(cellSindicalizadoEmpleado);
        tableDatosEmpleado.addCell(cellSindicalizadoEmpleadoV);
        tableDatosEmpleado.addCell(cellUsoCFDI);
        tableDatosEmpleado.addCell(cellUsoCFDIV);
        tableDatosEmpleado.addCell(celdaVacia);
        tableDatosEmpleado.addCell(celdaVacia);
        document.add(tableDatosEmpleado);
    }

    /**
     * Metodo para agregar datos de la nomina del trabajador Dias pagados,
     * periodo de pago, folio, etc
     *
     * @param document
     * @return Tabla para agregar los datos de la nomina
     * @throws DocumentException
     */
    public void agregarDatosNomina(Document document, CFDI.Complementos.Nomina nomina, CFDI facturaDto) throws DocumentException, ParseException {
        PdfPTable tableDatosNomina = new PdfPTable(4);
        tableDatosNomina.setWidths(new int[]{20, 40, 20, 40});
        tableDatosNomina.setTotalWidth(560);
        tableDatosNomina.setLockedWidth(true);
        tableDatosNomina.setWidthPercentage(100);
        tableDatosNomina.getDefaultCell().setBorder(Rectangle.NO_BORDER);

        PdfPCell borderEncabezadoSucursal = new PdfPCell();
        borderEncabezadoSucursal.setColspan(4);
        borderEncabezadoSucursal.setBackgroundColor(BaseColor.LIGHT_GRAY);
        borderEncabezadoSucursal.setHorizontalAlignment(Element.ALIGN_LEFT);
        borderEncabezadoSucursal.setBorder(Rectangle.NO_BORDER);

        PdfPCell cellNominaPeriodoPago = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.N_PERIODOPAGO).concat(" : "), fontEtiqueta));
        cellNominaPeriodoPago.setHorizontalAlignment(Element.ALIGN_LEFT);
        cellNominaPeriodoPago.setBorder(Rectangle.NO_BORDER);

        String fechaInicialPago = "";
        if (nomina.getFechaInicialPago() != null) {
            Date dateFechaInicial = nomina.getFechaInicialPago();
            SimpleDateFormat dateFormatFechaInicial = new SimpleDateFormat("dd/MM/yyyy");
            fechaInicialPago = dateFormatFechaInicial.format(dateFechaInicial);
        }
        String fechaFinalPago = "";
        if (nomina.getFechaFinalPago() != null) {
            Date dateFechaFinalPago = nomina.getFechaFinalPago();
            SimpleDateFormat dateFormatFechaFinalPago = new SimpleDateFormat("dd/MM/yyyy");
            fechaFinalPago = dateFormatFechaFinalPago.format(dateFechaFinalPago);
        }
        PdfPCell cellNominaPeriodoPagoV = new PdfPCell(new Paragraph(fechaInicialPago.concat(" al ").concat(fechaFinalPago), fontContenido));
        cellNominaPeriodoPagoV.setHorizontalAlignment(Element.ALIGN_LEFT);
        cellNominaPeriodoPagoV.setBorder(Rectangle.NO_BORDER);

        PdfPCell cellNominaFolio = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.P_FOLIO).concat(" : "), fontEtiqueta));
        cellNominaFolio.setHorizontalAlignment(Element.ALIGN_LEFT);
        cellNominaFolio.setBorder(Rectangle.NO_BORDER);

        PdfPCell cellNominaFolioV = null;
        // if (facturaDto.getEnumOrigen().equals(EnumOrigenFactura.OUTNOM)) {
        //     cellNominaFolioV = new PdfPCell(new Paragraph(facturaDto.getSerie(), fontContenido));
        //     cellNominaFolioV.setHorizontalAlignment(Element.ALIGN_LEFT);
        //     cellNominaFolioV.setBorder(Rectangle.NO_BORDER);
        // } else {
            cellNominaFolioV = new PdfPCell(new Paragraph((facturaDto.getSerie() != null ? facturaDto.getSerie() : "") + " " + facturaDto.getFolio(), fontContenido));
            cellNominaFolioV.setHorizontalAlignment(Element.ALIGN_LEFT);
            cellNominaFolioV.setBorder(Rectangle.NO_BORDER);
        // }

        PdfPCell cellNominaDiasPagados = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.N_DIASPAGADOS).concat(" : "), fontEtiqueta));
        cellNominaDiasPagados.setHorizontalAlignment(Element.ALIGN_LEFT);
        cellNominaDiasPagados.setBorder(Rectangle.NO_BORDER);

        PdfPCell cellNominaDiasPagadosV = new PdfPCell(new Paragraph(nomina.getNumDiasPagados().toString(), fontContenido));
        cellNominaDiasPagadosV.setHorizontalAlignment(Element.ALIGN_LEFT);
        cellNominaDiasPagadosV.setBorder(Rectangle.NO_BORDER);

        PdfPCell cellNominaFechaPago = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.N_FECHAPAGO).concat(" : "), fontEtiqueta));
        cellNominaFechaPago.setHorizontalAlignment(Element.ALIGN_LEFT);
        cellNominaFechaPago.setBorder(Rectangle.NO_BORDER);

        String fechaPagados = "";
        if (nomina.getFechaPago() != null) {
            Date datefechaPagados = nomina.getFechaPago();
            DateFormat dateFormatfechaPagados = new SimpleDateFormat("yyyy-MM-dd");
            fechaPagados = dateFormatfechaPagados.format(datefechaPagados);
        }

        PdfPCell cellNominaFechaPagoV = new PdfPCell(new Paragraph(fechaPagados, fontContenido));
        cellNominaFechaPagoV.setHorizontalAlignment(Element.ALIGN_LEFT);
        cellNominaFechaPagoV.setBorder(Rectangle.NO_BORDER);

        PdfPCell cellSpace = new PdfPCell();
        cellSpace.setColspan(2);
        cellSpace.setHorizontalAlignment(Element.ALIGN_LEFT);
        cellSpace.setBorder(Rectangle.NO_BORDER);


        PdfPCell exportacionC = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.E_EXPORTACION).concat(" : "), fontEtiqueta));
        exportacionC.setHorizontalAlignment(Element.ALIGN_LEFT);
        exportacionC.setBorder(Rectangle.NO_BORDER);

        PdfPCell exportacion = new PdfPCell(new Paragraph(facturaDto.getExportacion() != null ? facturaDto.getExportacion() : "", fontContenido));
        exportacion.setHorizontalAlignment(Element.ALIGN_LEFT);
        exportacion.setBorder(Rectangle.NO_BORDER);

        tableDatosNomina.addCell(borderEncabezadoSucursal);
        tableDatosNomina.addCell(cellNominaPeriodoPago);
        tableDatosNomina.addCell(cellNominaPeriodoPagoV);
        tableDatosNomina.addCell(cellNominaFolio);
        tableDatosNomina.addCell(cellNominaFolioV);
        tableDatosNomina.addCell(cellNominaDiasPagados);
        tableDatosNomina.addCell(cellNominaDiasPagadosV);
        tableDatosNomina.addCell(cellNominaFechaPago);
        tableDatosNomina.addCell(cellNominaFechaPagoV);
        tableDatosNomina.addCell(exportacionC);
        tableDatosNomina.addCell(exportacion);
        tableDatosNomina.addCell(cellSpace);
        if (nomina.getReceptor().getPeriodicidadPago() != null) {
            //System.out.println("clave " + nomina.getPeriodicidadPago());
            TipoPeriodicidad tipoPeriodicidad = catalogFallback.findPeriodicidadSafe(nomina.getReceptor().getPeriodicidadPago());
            if (tipoPeriodicidad != null) {
                PdfPCell cellNominaPeriocidadPago = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.N_PERIOCIDADPAGO).concat(" : "), fontEtiqueta));
                cellNominaPeriocidadPago.setHorizontalAlignment(Element.ALIGN_LEFT);
                cellNominaPeriocidadPago.setBorder(Rectangle.NO_BORDER);

                PdfPCell cellNominaPeriocidadPagoV = new PdfPCell(new Paragraph(tipoPeriodicidad.getDescripcion(), fontContenido));
                cellNominaPeriocidadPagoV.setHorizontalAlignment(Element.ALIGN_LEFT);
                cellNominaPeriocidadPagoV.setBorder(Rectangle.NO_BORDER);
                tableDatosNomina.addCell(cellNominaPeriocidadPago);
                tableDatosNomina.addCell(cellNominaPeriocidadPagoV);
            } else {
                PdfPCell celdaVacia = new PdfPCell();
                celdaVacia.setBorder(Rectangle.NO_BORDER);
                tableDatosNomina.addCell(celdaVacia);
                tableDatosNomina.addCell(celdaVacia);
            }
            PdfPCell celdaVacia = new PdfPCell();
            celdaVacia.setBorder(Rectangle.NO_BORDER);
            tableDatosNomina.addCell(celdaVacia);
            tableDatosNomina.addCell(celdaVacia);
        }
        document.add(tableDatosNomina);
    }

    /**
     * Metodo para agregar datos de nomina complementarios Tipo Jornada,
     * antiguedad, puesto, salario, etc. Validar el salario base, validar
     * salario diario
     *
     * @param document
     * @return Tabla para agregar los datos complementarios de la nomina
     * @throws DocumentException
     */
    public void agregarDatosComplementarios(Document document, CFDI.Complementos.Nomina nomina) throws DocumentException {
        PdfPTable tableDatosComplementarios = new PdfPTable(4);
        tableDatosComplementarios.setWidths(new int[]{35, 40, 33, 40});
        tableDatosComplementarios.setTotalWidth(560);
        tableDatosComplementarios.setLockedWidth(true);
        tableDatosComplementarios.setWidthPercentage(100);
        tableDatosComplementarios.getDefaultCell().setBorder(Rectangle.NO_BORDER);

        PdfPCell borderDatosComplementarios = new PdfPCell();
        borderDatosComplementarios.setColspan(4);
        borderDatosComplementarios.setBackgroundColor(BaseColor.LIGHT_GRAY);
        borderDatosComplementarios.setHorizontalAlignment(Element.ALIGN_LEFT);
        borderDatosComplementarios.setBorder(Rectangle.NO_BORDER);

        PdfPCell cellNominaRegimenTrabajor = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.N_REGIMENTRABAJADOR).concat(" : "), fontEtiqueta));
        cellNominaRegimenTrabajor.setHorizontalAlignment(Element.ALIGN_LEFT);
        cellNominaRegimenTrabajor.setBorder(Rectangle.NO_BORDER);

        CatRegimenContratacion tiporegimen = catalogosSatService.getCatRegimenContratacionByClave(nomina.getReceptor().getTipoRegimen());
        PdfPCell cellNominaRegimenTrabajorV = new PdfPCell(new Paragraph(nomina.getReceptor().getTipoRegimen() != null ? tiporegimen.getDescripcion() : "", fontContenido));
        cellNominaRegimenTrabajorV.setHorizontalAlignment(Element.ALIGN_LEFT);
        cellNominaRegimenTrabajorV.setBorder(Rectangle.NO_BORDER);

        PdfPCell cellNominaTipoNomina = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.N_TIPONOMINA).concat(" : "), fontEtiqueta));
        cellNominaTipoNomina.setHorizontalAlignment(Element.ALIGN_LEFT);
        cellNominaTipoNomina.setBorder(Rectangle.NO_BORDER);

        String tipoNomina = "";

        switch (nomina.getTipoNomina()) {
            case O:
            tipoNomina = "0 - Nómina ordinaria";
            case E:
            tipoNomina = "E - Nómina extraordinaria"; 
        }
       
       
        PdfPCell cellNominaTipoNominaV = new PdfPCell(new Paragraph(nomina.getTipoNomina() != null ?  tipoNomina :  "" , fontContenido));
        cellNominaTipoNominaV.setHorizontalAlignment(Element.ALIGN_LEFT);
        cellNominaTipoNominaV.setBorder(Rectangle.NO_BORDER);

        PdfPCell cellNominaDepartamento = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.N_DEPARTAMENTO).concat(" : "), fontEtiqueta));
        cellNominaDepartamento.setHorizontalAlignment(Element.ALIGN_LEFT);
        cellNominaDepartamento.setBorder(Rectangle.NO_BORDER);

        PdfPCell cellNominaDepartamentoV = new PdfPCell(new Paragraph(nomina.getReceptor().getDepartamento() != null ? nomina.getReceptor().getDepartamento() : "", fontContenido));
        cellNominaDepartamentoV.setColspan(3);
        cellNominaDepartamentoV.setHorizontalAlignment(Element.ALIGN_LEFT);
        cellNominaDepartamentoV.setBorder(Rectangle.NO_BORDER);

        PdfPCell cellNominaPuesto = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.N_PUESTO).concat(" : "), fontEtiqueta));
        cellNominaPuesto.setHorizontalAlignment(Element.ALIGN_LEFT);
        cellNominaPuesto.setBorder(Rectangle.NO_BORDER);

        PdfPCell cellNominaPuestoV = new PdfPCell(new Paragraph(nomina.getReceptor().getPuesto() != null ? nomina.getReceptor().getPuesto() : "", fontContenido));
        cellNominaPuestoV.setColspan(3);
        cellNominaPuestoV.setHorizontalAlignment(Element.ALIGN_LEFT);
        cellNominaPuestoV.setBorder(Rectangle.NO_BORDER);

        PdfPCell cellNominaTipodeContrato = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.N_TIPOCONTRATO).concat(" : "), fontEtiqueta));
        cellNominaTipodeContrato.setHorizontalAlignment(Element.ALIGN_LEFT);
        cellNominaTipodeContrato.setBorder(Rectangle.NO_BORDER);

        PdfPCell cellNominaTipodeContratoV = new PdfPCell(new Paragraph(""));
        if (nomina.getReceptor().getTipoContrato() != null) {

            CatTipoContrato tipoContrato;
            tipoContrato = catalogosSatService.getCatTipoContratoByClave(nomina.getReceptor().getTipoContrato());
            cellNominaTipodeContratoV = new PdfPCell(new Paragraph(tipoContrato != null ? tipoContrato.getConcepto() : "", fontContenido));
            cellNominaTipodeContratoV.setHorizontalAlignment(Element.ALIGN_LEFT);
            cellNominaTipodeContratoV.setBorder(Rectangle.NO_BORDER);
        } else {
            cellNominaTipodeContratoV.setHorizontalAlignment(Element.ALIGN_LEFT);
            cellNominaTipodeContratoV.setBorder(Rectangle.NO_BORDER);
        }

        PdfPCell cellNominaClaveEntidad = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.N_CLAVEENTIDAD).concat(" : "), fontEtiqueta));
        cellNominaClaveEntidad.setHorizontalAlignment(Element.ALIGN_LEFT);
        cellNominaClaveEntidad.setBorder(Rectangle.NO_BORDER);

        PdfPCell cellNominaClaveEntidadV = new PdfPCell(new Paragraph(nomina.getReceptor().getClaveEntFed() != null ? nomina.getReceptor().getClaveEntFed() : "", fontContenido));
        cellNominaClaveEntidadV.setHorizontalAlignment(Element.ALIGN_LEFT);
        cellNominaClaveEntidadV.setBorder(Rectangle.NO_BORDER);

        PdfPCell cellNominaSalarioBase = new PdfPCell(new Paragraph(("Salario Base de Cotización").concat(" : "), fontEtiqueta));
        cellNominaSalarioBase.setHorizontalAlignment(Element.ALIGN_LEFT);
        cellNominaSalarioBase.setBorder(Rectangle.NO_BORDER);

        PdfPCell cellNominaSalarioBaseV = new PdfPCell(new Paragraph((nomina.getReceptor().getSalarioBaseCotApor() != null ? Utilities.toFormatMoneda(nomina.getReceptor().getSalarioBaseCotApor()) : ""), fontContenido));
        cellNominaSalarioBaseV.setHorizontalAlignment(Element.ALIGN_LEFT);
        cellNominaSalarioBaseV.setBorder(Rectangle.NO_BORDER);

        PdfPCell cellNominaJornada = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.N_TIPOJORNADA).concat(" : "), fontEtiqueta));
        cellNominaJornada.setHorizontalAlignment(Element.ALIGN_LEFT);
        cellNominaJornada.setBorder(Rectangle.NO_BORDER);

        PdfPCell cellNominaJornadaV = new PdfPCell(new Paragraph("", fontContenido));
        if (nomina.getReceptor().getTipoJornada() != null) {
            CatTipoJornada tipoJornada = catalogosSatService.getCatTipoJornadaByClave(nomina.getReceptor().getTipoJornada());
            cellNominaJornadaV = new PdfPCell(new Paragraph(tipoJornada != null ? tipoJornada.getConcepto() : "", fontContenido));
            cellNominaJornadaV.setHorizontalAlignment(Element.ALIGN_LEFT);
            cellNominaJornadaV.setBorder(Rectangle.NO_BORDER);
        } else {
            cellNominaJornadaV.setHorizontalAlignment(Element.ALIGN_LEFT);
            cellNominaJornadaV.setBorder(Rectangle.NO_BORDER);
        }

        PdfPCell cellNominaSalarioIntegrado = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.N_SALARIODIARIO).concat(" : "), fontEtiqueta));
        cellNominaSalarioIntegrado.setHorizontalAlignment(Element.ALIGN_LEFT);
        cellNominaSalarioIntegrado.setBorder(Rectangle.NO_BORDER);

        PdfPCell cellNominaSalarioIntegradoV = new PdfPCell(new Paragraph((nomina.getReceptor().getSalarioDiarioIntegrado() != null ? Utilities.toFormatMoneda(nomina.getReceptor().getSalarioDiarioIntegrado()) : ""), fontContenido));
        cellNominaSalarioIntegradoV.setHorizontalAlignment(Element.ALIGN_LEFT);
        cellNominaSalarioIntegradoV.setBorder(Rectangle.NO_BORDER);

        PdfPCell cellNominaRiesgoPuesto = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.N_RIESGOPUESTO).concat(" : "), fontEtiqueta));
        cellNominaRiesgoPuesto.setHorizontalAlignment(Element.ALIGN_LEFT);
        cellNominaRiesgoPuesto.setBorder(Rectangle.NO_BORDER);

        CatRiesgoPuesto catRiesgoPuesto = catalogosSatService.getCatRiesgoPuestoByClave(nomina.getReceptor().getRiesgoPuesto());
        PdfPCell cellNominaRiesgoPuestoV = new PdfPCell(new Paragraph(catRiesgoPuesto != null ? (nomina.getReceptor().getRiesgoPuesto() + "-" + catRiesgoPuesto.getDescripcion()) : "", fontContenido));
        cellNominaRiesgoPuestoV.setHorizontalAlignment(Element.ALIGN_LEFT);
        cellNominaRiesgoPuestoV.setBorder(Rectangle.NO_BORDER);

        PdfPCell cellNominaFechaIngreso = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.N_FECHAINICIOLABORAL).concat(" : "), fontEtiqueta));
        cellNominaFechaIngreso.setHorizontalAlignment(Element.ALIGN_LEFT);
        cellNominaFechaIngreso.setBorder(Rectangle.NO_BORDER);

        String inicioLaboral = "";
        if (nomina.getReceptor().getFechaInicioRelLaboral()  != null) {
            Date dateCheckOut = nomina.getReceptor().getFechaInicioRelLaboral() ;
            DateFormat dateFormatCheckOut = new SimpleDateFormat("dd/MM/yyyy");
            inicioLaboral = dateFormatCheckOut.format(dateCheckOut);
        }

        PdfPCell cellNominaFechaIngresoV = new PdfPCell(new Paragraph(inicioLaboral, fontContenido));
        cellNominaFechaIngresoV.setHorizontalAlignment(Element.ALIGN_LEFT);
        cellNominaFechaIngresoV.setBorder(Rectangle.NO_BORDER);

        PdfPCell cellNominaAntiguedad = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.N_ANTIGUEDAD).concat(" : "), fontEtiqueta));
        cellNominaAntiguedad.setHorizontalAlignment(Element.ALIGN_LEFT);
        cellNominaAntiguedad.setBorder(Rectangle.NO_BORDER);

        PdfPCell cellNominaAntiguedadV = new PdfPCell(new Paragraph(nomina.getReceptor().getAntiguedad() != null ? nomina.getReceptor().getAntiguedad() : "", fontContenido));
        cellNominaAntiguedadV.setHorizontalAlignment(Element.ALIGN_LEFT);
        cellNominaAntiguedadV.setBorder(Rectangle.NO_BORDER);

        tableDatosComplementarios.addCell(borderDatosComplementarios);
        tableDatosComplementarios.addCell(cellNominaRegimenTrabajor);
        tableDatosComplementarios.addCell(cellNominaRegimenTrabajorV);
        tableDatosComplementarios.addCell(cellNominaTipoNomina);
        tableDatosComplementarios.addCell(cellNominaTipoNominaV);
        tableDatosComplementarios.addCell(cellNominaDepartamento);
        tableDatosComplementarios.addCell(cellNominaDepartamentoV);
        tableDatosComplementarios.addCell(cellNominaPuesto);
        tableDatosComplementarios.addCell(cellNominaPuestoV);
        tableDatosComplementarios.addCell(cellNominaTipodeContrato);
        tableDatosComplementarios.addCell(cellNominaTipodeContratoV);
        tableDatosComplementarios.addCell(cellNominaClaveEntidad);
        tableDatosComplementarios.addCell(cellNominaClaveEntidadV);
        tableDatosComplementarios.addCell(cellNominaSalarioBase);
        tableDatosComplementarios.addCell(cellNominaSalarioBaseV);
        tableDatosComplementarios.addCell(cellNominaJornada);
        tableDatosComplementarios.addCell(cellNominaJornadaV);
        tableDatosComplementarios.addCell(cellNominaSalarioIntegrado);
        tableDatosComplementarios.addCell(cellNominaSalarioIntegradoV);
        tableDatosComplementarios.addCell(cellNominaRiesgoPuesto);
        tableDatosComplementarios.addCell(cellNominaRiesgoPuestoV);
        tableDatosComplementarios.addCell(cellNominaFechaIngreso);
        tableDatosComplementarios.addCell(cellNominaFechaIngresoV);
        tableDatosComplementarios.addCell(cellNominaAntiguedad);
        tableDatosComplementarios.addCell(cellNominaAntiguedadV);
        document.add(tableDatosComplementarios);

    }

    /**
     * Metodo para agregar unicamente los encabezados de la tabla Tipo, clave,
     * conceptos, etc
     *
     * @param document
     * @return Tabla para agregar los encabezados de la tabla Percepciones y
     * deducciones
     * @throws DocumentException
     */
    public void agregarEncabezadosPercepcionesYDeducciones(Document document, CFDI facturaDto) throws DocumentException {
        PdfPTable tablePercepcionesYDeducciones = new PdfPTable(2);
        tablePercepcionesYDeducciones.setWidths(new int[]{80, 80});
        tablePercepcionesYDeducciones.setTotalWidth(560);
        tablePercepcionesYDeducciones.setLockedWidth(true);
        tablePercepcionesYDeducciones.setWidthPercentage(100);
        tablePercepcionesYDeducciones.getDefaultCell().setBorder(Rectangle.NO_BORDER);

        PdfPTable tablePercepciones = new PdfPTable(5);
        tablePercepciones.setWidths(new int[]{25, 25, 86, 56, 56});
        tablePercepciones.setTotalWidth(280);
        tablePercepciones.setLockedWidth(true);
        tablePercepciones.setWidthPercentage(100);
        tablePercepciones.getDefaultCell().setBorder(Rectangle.NO_BORDER);
        if (facturaDto.getComplementos().getNomina().getPercepciones() != null && !facturaDto.getComplementos().getNomina().getPercepciones().getPercepcion().isEmpty()) {

            PdfPCell cellHeaderPercepciones = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.N_PERCEPCIONES), fontEtiqueta));
            cellHeaderPercepciones.setColspan(5);
            cellHeaderPercepciones.setHorizontalAlignment(Element.ALIGN_CENTER);
            dibujarContornoEncabezadosArriba(cellHeaderPercepciones);

            PdfPCell cellPercepcionesTipo = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.N_TIPO), fontEtiqueta));
            cellPercepcionesTipo.setHorizontalAlignment(Element.ALIGN_LEFT);
            cellPercepcionesTipo.setBorder(Rectangle.NO_BORDER);
            dibujarContornoIzquierdo(cellPercepcionesTipo);

            PdfPCell cellPercepcionesClave = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.N_CLAVE), fontEtiqueta));
            cellPercepcionesClave.setHorizontalAlignment(Element.ALIGN_LEFT);
            cellPercepcionesClave.setBorder(Rectangle.NO_BORDER);

            PdfPCell cellPercepcionesConcepto = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.N_CONCEPTO), fontEtiqueta));
            cellPercepcionesConcepto.setHorizontalAlignment(Element.ALIGN_LEFT);
            cellPercepcionesConcepto.setBorder(Rectangle.NO_BORDER);

            PdfPCell cellPercepcionesImporteGravado = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.N_IMPORTEGRAVADO), fontEtiqueta));
            cellPercepcionesImporteGravado.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cellPercepcionesImporteGravado.setBorder(Rectangle.NO_BORDER);

            PdfPCell cellIPercepcionesImporteExento = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.N_IMPORTEEXCENTO), fontEtiqueta));
            cellIPercepcionesImporteExento.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cellIPercepcionesImporteExento.setBorder(Rectangle.NO_BORDER);
            dibujarContornoDerecho(cellIPercepcionesImporteExento);

            PdfPCell cellPercepcionesCierreTabla = new PdfPCell(new Paragraph("", fontEtiqueta));
            cellPercepcionesCierreTabla.setColspan(5);
            cellPercepcionesCierreTabla.setHorizontalAlignment(Element.ALIGN_CENTER);
            dibujarContornoEncabezadosAbajo(cellPercepcionesCierreTabla);

            tablePercepciones.addCell(cellHeaderPercepciones);
            tablePercepciones.addCell(cellPercepcionesTipo);
            tablePercepciones.addCell(cellPercepcionesClave);
            tablePercepciones.addCell(cellPercepcionesConcepto);
            tablePercepciones.addCell(cellPercepcionesImporteGravado);
            tablePercepciones.addCell(cellIPercepcionesImporteExento);
            tablePercepciones.addCell(cellPercepcionesCierreTabla);
        }
        PdfPTable tableDeducciones = new PdfPTable(4);
        tableDeducciones.setWidths(new int[]{30, 30, 110, 70});
        tableDeducciones.setTotalWidth(280);
        tableDeducciones.setLockedWidth(true);
        tableDeducciones.setWidthPercentage(100);
        tableDeducciones.getDefaultCell().setBorder(Rectangle.NO_BORDER);
        if (facturaDto.getComplementos().getNomina().getDeducciones() != null && !facturaDto.getComplementos().getNomina().getDeducciones().getDeduccion().isEmpty()) {

            PdfPCell cellHeaderDeducciones = new PdfPCell(new Paragraph("DEDUCCIONES", fontEtiqueta));
            cellHeaderDeducciones.setColspan(4);
            cellHeaderDeducciones.setHorizontalAlignment(Element.ALIGN_CENTER);
            dibujarContornoEncabezadosArriba(cellHeaderDeducciones);

            PdfPCell cellDeduccioneTipo = new PdfPCell(new Paragraph("Tipo", fontEtiqueta));
            cellDeduccioneTipo.setHorizontalAlignment(Element.ALIGN_LEFT);
            cellDeduccioneTipo.setBorder(Rectangle.NO_BORDER);
            dibujarContornoIzquierdo(cellDeduccioneTipo);

            PdfPCell cellDeduccionesClave = new PdfPCell(new Paragraph("Clave", fontEtiqueta));
            cellDeduccionesClave.setHorizontalAlignment(Element.ALIGN_LEFT);
            cellDeduccionesClave.setBorder(Rectangle.NO_BORDER);

            PdfPCell cellDeduccionesConcepto = new PdfPCell(new Paragraph("Concepto", fontEtiqueta));
            cellDeduccionesConcepto.setHorizontalAlignment(Element.ALIGN_LEFT);
            cellDeduccionesConcepto.setBorder(Rectangle.NO_BORDER);

            PdfPCell cellDeduccionesImporte = new PdfPCell(new Paragraph("Importe", fontEtiqueta));
            cellDeduccionesImporte.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cellDeduccionesImporte.setBorder(Rectangle.NO_BORDER);
            dibujarContornoDerecho(cellDeduccionesImporte);

            PdfPCell cellDeduccionesCierreTabla = new PdfPCell(new Paragraph("", fontEtiqueta));
            cellDeduccionesCierreTabla.setColspan(4);
            cellDeduccionesCierreTabla.setHorizontalAlignment(Element.ALIGN_CENTER);
            dibujarContornoEncabezadosAbajo(cellDeduccionesCierreTabla);

            tableDeducciones.addCell(cellHeaderDeducciones);
            tableDeducciones.addCell(cellDeduccioneTipo);
            tableDeducciones.addCell(cellDeduccionesClave);
            tableDeducciones.addCell(cellDeduccionesConcepto);
            tableDeducciones.addCell(cellDeduccionesImporte);
            tableDeducciones.addCell(cellDeduccionesCierreTabla);
        }
        tablePercepcionesYDeducciones.addCell(tablePercepciones);
        tablePercepcionesYDeducciones.addCell(tableDeducciones);
        document.add(tablePercepcionesYDeducciones);

    }

    /**
     * Metodo para realizar el llenado correspondiente de las tablas
     * percepciones y deduciones. Tambien llena los subtotales de las
     * percepciones y deducciones
     *
     * @param document
     * @return Tabla para agregar los datos correspondientes a percepciones y
     * deducciones
     * @throws DocumentException
     */
    public void agregarPercepcionesYDeducciones(Document document, CFDI facturaDto) throws DocumentException {
        PdfPTable tablePercepcionesYDeducciones = new PdfPTable(2);
        tablePercepcionesYDeducciones.setWidths(new int[]{80, 80});
        tablePercepcionesYDeducciones.setTotalWidth(560);
        tablePercepcionesYDeducciones.setLockedWidth(true);
        tablePercepcionesYDeducciones.setWidthPercentage(100);
        tablePercepcionesYDeducciones.getDefaultCell().setBorder(Rectangle.NO_BORDER);
        tablePercepcionesYDeducciones.getDefaultCell().setPaddingTop(-2f);

        PdfPTable tablePercepciones = new PdfPTable(5);
        tablePercepciones.setWidths(new int[]{25, 28, 86, 56, 56});
        tablePercepciones.setTotalWidth(280);
        tablePercepciones.setLockedWidth(true);
        tablePercepciones.setWidthPercentage(100);
        tablePercepciones.getDefaultCell().setBorder(Rectangle.NO_BORDER);

        if (facturaDto.getComplementos().getNomina().getPercepciones() != null && !facturaDto.getComplementos().getNomina().getPercepciones().getPercepcion().isEmpty()) {
            for (Percepcion percepcionesCargadas : facturaDto.getComplementos().getNomina().getPercepciones().getPercepcion()) {
                PdfPCell cellPercepcionesTipo = new PdfPCell(new Paragraph(percepcionesCargadas.getTipoPercepcion().trim(), fontContenido));
                cellPercepcionesTipo.setHorizontalAlignment(Element.ALIGN_LEFT);
                dibujarContornoIzquierdo(cellPercepcionesTipo);

                PdfPCell cellPercepcionesClave = new PdfPCell(new Paragraph(percepcionesCargadas.getClave().trim(), fontContenido));
                cellPercepcionesClave.setHorizontalAlignment(Element.ALIGN_LEFT);
                cellPercepcionesClave.setBorder(Rectangle.NO_BORDER);

                PdfPCell cellPercepcionesConcepto = new PdfPCell(new Paragraph(percepcionesCargadas.getConcepto(), fontContenido));
                cellPercepcionesConcepto.setHorizontalAlignment(Element.ALIGN_LEFT);
                cellPercepcionesConcepto.setBorder(Rectangle.NO_BORDER);

                PdfPCell cellPercepcionesImporteGravado = new PdfPCell(new Paragraph(Utilities.toFormatMoneda(percepcionesCargadas.getImporteGravado()), fontContenido));
                cellPercepcionesImporteGravado.setHorizontalAlignment(Element.ALIGN_RIGHT);
                cellPercepcionesImporteGravado.setBorder(Rectangle.NO_BORDER);

                PdfPCell cellIPercepcionesImporteExento = new PdfPCell(new Paragraph(Utilities.toFormatMoneda(percepcionesCargadas.getImporteExento()) , fontContenido));
                cellIPercepcionesImporteExento.setHorizontalAlignment(Element.ALIGN_RIGHT);
                dibujarContornoDerecho(cellIPercepcionesImporteExento);

                tablePercepciones.addCell(cellPercepcionesTipo);
                tablePercepciones.addCell(cellPercepcionesClave);
                tablePercepciones.addCell(cellPercepcionesConcepto);
                tablePercepciones.addCell(cellPercepcionesImporteGravado);
                tablePercepciones.addCell(cellIPercepcionesImporteExento);
            }

            if (facturaDto.getComplementos().getNomina().getPercepciones().getSeparacionIndemnizacion() !=  null) {

                PdfPCell cellHeaderSeparacionIndemnizacion = new PdfPCell(new Paragraph("SEPARACIÓN / INDEMNIZACIÓN", fontEtiqueta));
                cellHeaderSeparacionIndemnizacion.setColspan(5);
                cellHeaderSeparacionIndemnizacion.setHorizontalAlignment(Element.ALIGN_LEFT);
                dibujarContornoEncabezadosAbajo(cellHeaderSeparacionIndemnizacion);

                PdfPCell cellIngresoAcumulableC = new PdfPCell(new Paragraph("Ingreso Acumulable", fontContenido));
                cellIngresoAcumulableC.setHorizontalAlignment(Element.ALIGN_LEFT);
                cellIngresoAcumulableC.setColspan(2);
                dibujarContornoIzquierdo(cellIngresoAcumulableC);

                PdfPCell cellIngresoAcumulableV = new PdfPCell(new Paragraph((facturaDto.getComplementos().getNomina().getPercepciones().getSeparacionIndemnizacion() != null && facturaDto.getComplementos().getNomina().getPercepciones().getSeparacionIndemnizacion().getIngresoAcumulable() != null) ? Utilities.toFormatMoneda(facturaDto.getComplementos().getNomina().getPercepciones().getSeparacionIndemnizacion().getIngresoAcumulable()) : "", fontContenido));
                cellIngresoAcumulableV.setHorizontalAlignment(Element.ALIGN_RIGHT);
                cellIngresoAcumulableV.setBorder(Rectangle.NO_BORDER);

                PdfPCell cellIngresoNoAcumulableC = new PdfPCell(new Paragraph("Ingreso No Acumulable", fontContenido));
                cellIngresoNoAcumulableC.setHorizontalAlignment(Element.ALIGN_LEFT);
                cellIngresoNoAcumulableC.setColspan(2);
                dibujarContornoIzquierdo(cellIngresoNoAcumulableC);

                PdfPCell cellIngresoNoAcumulableV = new PdfPCell(new Paragraph((facturaDto.getComplementos().getNomina().getPercepciones().getSeparacionIndemnizacion() != null && facturaDto.getComplementos().getNomina().getPercepciones().getSeparacionIndemnizacion().getIngresoNoAcumulable() != null) ? Utilities.toFormatMoneda(facturaDto.getComplementos().getNomina().getPercepciones().getSeparacionIndemnizacion().getIngresoNoAcumulable()) : "", fontContenido));
                cellIngresoNoAcumulableV.setHorizontalAlignment(Element.ALIGN_RIGHT);
                cellIngresoNoAcumulableV.setBorder(Rectangle.NO_BORDER);

                PdfPCell cellAniosServicioC = new PdfPCell(new Paragraph("Años de servicio", fontContenido));
                cellAniosServicioC.setHorizontalAlignment(Element.ALIGN_LEFT);
                cellAniosServicioC.setColspan(2);
                dibujarContornoIzquierdo(cellAniosServicioC);

                PdfPCell cellAniosServicioV = new PdfPCell(new Paragraph(String.valueOf(facturaDto.getComplementos().getNomina().getPercepciones().getSeparacionIndemnizacion() != null && String.valueOf(facturaDto.getComplementos().getNomina().getPercepciones().getSeparacionIndemnizacion().getNumAniosServicio()) != null ? facturaDto.getComplementos().getNomina().getPercepciones().getSeparacionIndemnizacion().getNumAniosServicio() : ""), fontContenido));
                cellAniosServicioV.setHorizontalAlignment(Element.ALIGN_RIGHT);
                cellAniosServicioV.setBorder(Rectangle.NO_BORDER);

                PdfPCell cellUltimoSueldoC = new PdfPCell(new Paragraph("Ultimo sueldo", fontContenido));
                cellUltimoSueldoC.setHorizontalAlignment(Element.ALIGN_LEFT);
                cellUltimoSueldoC.setColspan(2);
                dibujarContornoIzquierdo(cellUltimoSueldoC);

                PdfPCell cellUltimoSueldoV = new PdfPCell(new Paragraph(((facturaDto.getComplementos().getNomina().getPercepciones().getSeparacionIndemnizacion() != null && facturaDto.getComplementos().getNomina().getPercepciones().getSeparacionIndemnizacion().getUltimoSueldoMensOrd() != null) ? Utilities.toFormatMoneda(facturaDto.getComplementos().getNomina().getPercepciones().getSeparacionIndemnizacion().getUltimoSueldoMensOrd()) : ""), fontContenido));
                cellUltimoSueldoV.setHorizontalAlignment(Element.ALIGN_RIGHT);
                cellUltimoSueldoV.setBorder(Rectangle.NO_BORDER);

                PdfPCell cellTotalPagadoC = new PdfPCell(new Paragraph("Total Pagado", fontContenido));
                cellTotalPagadoC.setHorizontalAlignment(Element.ALIGN_LEFT);
                cellTotalPagadoC.setColspan(2);
                dibujarContornoIzquierdo(cellTotalPagadoC);

                PdfPCell cellTotalPagadoV = new PdfPCell(new Paragraph(((facturaDto.getComplementos().getNomina().getPercepciones().getSeparacionIndemnizacion() != null && facturaDto.getComplementos().getNomina().getPercepciones().getSeparacionIndemnizacion().getTotalPagado() != null) ? Utilities.toFormatMoneda(facturaDto.getComplementos().getNomina().getPercepciones().getSeparacionIndemnizacion().getTotalPagado()) : ""), fontContenido));
                cellTotalPagadoV.setHorizontalAlignment(Element.ALIGN_RIGHT);
                cellTotalPagadoV.setBorder(Rectangle.NO_BORDER);

                PdfPCell celdaVacia = new PdfPCell();
                celdaVacia.setBorder(Rectangle.NO_BORDER);
                celdaVacia.setColspan(2);
                dibujarContornoDerecho(celdaVacia);

                tablePercepciones.addCell(cellHeaderSeparacionIndemnizacion);
                tablePercepciones.addCell(cellIngresoAcumulableC);
                tablePercepciones.addCell(cellIngresoAcumulableV);
                tablePercepciones.addCell(celdaVacia);
                tablePercepciones.addCell(cellIngresoNoAcumulableC);
                tablePercepciones.addCell(cellIngresoNoAcumulableV);
                tablePercepciones.addCell(celdaVacia);
                tablePercepciones.addCell(cellAniosServicioC);
                tablePercepciones.addCell(cellAniosServicioV);
                tablePercepciones.addCell(celdaVacia);
                tablePercepciones.addCell(cellUltimoSueldoC);
                tablePercepciones.addCell(cellUltimoSueldoV);
                tablePercepciones.addCell(celdaVacia);
                tablePercepciones.addCell(cellTotalPagadoC);
                tablePercepciones.addCell(cellTotalPagadoV);
                tablePercepciones.addCell(celdaVacia);
            }

            if (facturaDto.getComplementos().getNomina().getPercepciones().getJubilacionPensionRetiro() != null && facturaDto.getComplementos().getNomina().getPercepciones().getJubilacionPensionRetiro().getIngresoAcumulable() != null) {
                PdfPCell cellHeaderSeparacionIndemnizacion = new PdfPCell(new Paragraph("JUBILACIÓN, PENSION O RETIRO", fontEtiqueta));
                cellHeaderSeparacionIndemnizacion.setColspan(5);
                cellHeaderSeparacionIndemnizacion.setHorizontalAlignment(Element.ALIGN_LEFT);
                dibujarContornoEncabezadosAbajo(cellHeaderSeparacionIndemnizacion);

                PdfPCell cellMontoDiarioC = new PdfPCell(new Paragraph("Monto diario", fontContenido));
                cellMontoDiarioC.setHorizontalAlignment(Element.ALIGN_LEFT);
                cellMontoDiarioC.setColspan(2);
                dibujarContornoIzquierdo(cellMontoDiarioC);

                PdfPCell cellMontoDiarioV = new PdfPCell(new Paragraph((facturaDto.getComplementos().getNomina().getPercepciones().getJubilacionPensionRetiro()  != null && facturaDto.getComplementos().getNomina().getPercepciones().getJubilacionPensionRetiro().getMontoDiario() != null) ? Utilities.toFormatMoneda(facturaDto.getComplementos().getNomina().getPercepciones().getJubilacionPensionRetiro().getMontoDiario()) : "", fontContenido));
                cellMontoDiarioV.setHorizontalAlignment(Element.ALIGN_RIGHT);
                cellMontoDiarioV.setBorder(Rectangle.NO_BORDER);

                PdfPCell cellIngresoAcumulableC = new PdfPCell(new Paragraph("Ingreso Acumulable", fontContenido));
                cellIngresoAcumulableC.setHorizontalAlignment(Element.ALIGN_LEFT);
                cellIngresoAcumulableC.setColspan(2);
                dibujarContornoIzquierdo(cellIngresoAcumulableC);

                PdfPCell cellIngresoAcumulableV = new PdfPCell(new Paragraph((facturaDto.getComplementos().getNomina().getPercepciones().getJubilacionPensionRetiro()  != null && facturaDto.getComplementos().getNomina().getPercepciones().getJubilacionPensionRetiro().getIngresoAcumulable() != null) ? Utilities.toFormatMoneda(facturaDto.getComplementos().getNomina().getPercepciones().getJubilacionPensionRetiro().getIngresoAcumulable()) : "", fontContenido));
                cellIngresoAcumulableV.setHorizontalAlignment(Element.ALIGN_RIGHT);
                cellIngresoAcumulableV.setBorder(Rectangle.NO_BORDER);

                PdfPCell cellIngresoNoAcumulableC = new PdfPCell(new Paragraph("Ingreso No Acumulable", fontContenido));
                cellIngresoNoAcumulableC.setHorizontalAlignment(Element.ALIGN_LEFT);
                cellIngresoNoAcumulableC.setColspan(2);
                dibujarContornoIzquierdo(cellIngresoNoAcumulableC);

                PdfPCell cellIngresoNoAcumulableV = new PdfPCell(new Paragraph((facturaDto.getComplementos().getNomina().getPercepciones().getJubilacionPensionRetiro()  != null && facturaDto.getComplementos().getNomina().getPercepciones().getJubilacionPensionRetiro().getIngresoNoAcumulable() != null) ? Utilities.toFormatMoneda(facturaDto.getComplementos().getNomina().getPercepciones().getJubilacionPensionRetiro().getIngresoNoAcumulable()) : "", fontContenido));
                cellIngresoNoAcumulableV.setHorizontalAlignment(Element.ALIGN_RIGHT);
                cellIngresoNoAcumulableV.setBorder(Rectangle.NO_BORDER);

                PdfPCell cellTotalParcialidadC = new PdfPCell(new Paragraph("Total Parcialidad", fontContenido));
                cellTotalParcialidadC.setHorizontalAlignment(Element.ALIGN_LEFT);
                cellTotalParcialidadC.setColspan(2);
                dibujarContornoIzquierdo(cellTotalParcialidadC);

                PdfPCell cellTotalParcialidadV = new PdfPCell(new Paragraph(((facturaDto.getComplementos().getNomina().getPercepciones().getJubilacionPensionRetiro()  != null && facturaDto.getComplementos().getNomina().getPercepciones().getJubilacionPensionRetiro().getTotalParcialidad() != null) ? Utilities.toFormatMoneda(facturaDto.getComplementos().getNomina().getPercepciones().getJubilacionPensionRetiro().getTotalParcialidad()) : ""), fontContenido));
                cellTotalParcialidadV.setHorizontalAlignment(Element.ALIGN_RIGHT);
                cellTotalParcialidadV.setBorder(Rectangle.NO_BORDER);

                PdfPCell cellTotalExhibicionC = new PdfPCell(new Paragraph("Total exhibición", fontContenido));
                cellTotalExhibicionC.setHorizontalAlignment(Element.ALIGN_LEFT);
                cellTotalExhibicionC.setColspan(2);
                dibujarContornoIzquierdo(cellTotalExhibicionC);

                PdfPCell cellTotalExhibicionV = new PdfPCell(new Paragraph(((facturaDto.getComplementos().getNomina().getPercepciones().getJubilacionPensionRetiro()  != null && facturaDto.getComplementos().getNomina().getPercepciones().getJubilacionPensionRetiro().getTotalUnaExhibicion() != null) ? Utilities.toFormatMoneda(facturaDto.getComplementos().getNomina().getPercepciones().getJubilacionPensionRetiro().getTotalUnaExhibicion()) : ""), fontContenido));
                cellTotalExhibicionV.setHorizontalAlignment(Element.ALIGN_RIGHT);
                cellTotalExhibicionV.setBorder(Rectangle.NO_BORDER);

                PdfPCell celdaVacia = new PdfPCell();
                celdaVacia.setBorder(Rectangle.NO_BORDER);
                celdaVacia.setColspan(2);
                dibujarContornoDerecho(celdaVacia);

                tablePercepciones.addCell(cellHeaderSeparacionIndemnizacion);
                tablePercepciones.addCell(cellMontoDiarioC);
                tablePercepciones.addCell(cellMontoDiarioV);
                tablePercepciones.addCell(celdaVacia);
                tablePercepciones.addCell(cellIngresoAcumulableC);
                tablePercepciones.addCell(cellIngresoAcumulableV);
                tablePercepciones.addCell(celdaVacia);
                tablePercepciones.addCell(cellIngresoNoAcumulableC);
                tablePercepciones.addCell(cellIngresoNoAcumulableV);
                tablePercepciones.addCell(celdaVacia);
                tablePercepciones.addCell(cellTotalParcialidadC);
                tablePercepciones.addCell(cellTotalParcialidadV);
                tablePercepciones.addCell(celdaVacia);
                tablePercepciones.addCell(cellTotalExhibicionC);
                tablePercepciones.addCell(cellTotalExhibicionV);
                tablePercepciones.addCell(celdaVacia);
            }

            PdfPCell cellSeparacionTotales = new PdfPCell();
            cellSeparacionTotales.setColspan(5);
            cellSeparacionTotales.setBorderWidthTop(0f);
            cellSeparacionTotales.setBorderWidthBottom(0.3f);
            cellSeparacionTotales.setBorderWidthRight(0.3f);
            cellSeparacionTotales.setBorderWidthLeft(0.3f);
            tablePercepciones.addCell(cellSeparacionTotales);

            PdfPCell cellPercecpionesTotal = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.N_TOTALPERCEPCIONES).concat(" : "), fontEtiqueta));
            cellPercecpionesTotal.setHorizontalAlignment(Element.ALIGN_LEFT);
            cellPercecpionesTotal.setColspan(3);
            dibujarContornoIzquierdo(cellPercecpionesTotal);

            PdfPCell cellPercepcionesSubtotal = new PdfPCell(new Paragraph(Utilities.toFormatMoneda(facturaDto.getComplementos().getNomina().getTotalPercepciones()), fontContenido));
            cellPercepcionesSubtotal.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cellPercepcionesSubtotal.setBorder(Rectangle.NO_BORDER);

            PdfPCell cellPercepcionesTotal = new PdfPCell(new Paragraph(Utilities.toFormatMoneda(facturaDto.getComplementos().getNomina().getPercepciones().getTotalExento()), fontContenido));
            cellPercepcionesTotal.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cellPercepcionesTotal.setBorder(Rectangle.NO_BORDER);
            dibujarContornoDerecho(cellPercepcionesTotal);

            PdfPCell cellHeaderPercepciones = new PdfPCell(new Paragraph("", fontEtiqueta));
            cellHeaderPercepciones.setColspan(5);
            cellHeaderPercepciones.setPaddingTop(-2f);
            cellHeaderPercepciones.setHorizontalAlignment(Element.ALIGN_CENTER);
            dibujarContornoEncabezadosAbajo(cellHeaderPercepciones);

            tablePercepciones.addCell(cellPercecpionesTotal);
            tablePercepciones.addCell(cellPercepcionesSubtotal);
            tablePercepciones.addCell(cellPercepcionesTotal);
            tablePercepciones.addCell(cellHeaderPercepciones);
            tablePercepcionesYDeducciones.addCell(tablePercepciones);
        }

        if (facturaDto.getComplementos().getNomina().getDeducciones().getDeduccion() != null && !facturaDto.getComplementos().getNomina().getDeducciones().getDeduccion().isEmpty()) {

            PdfPTable tableDeduccionesTotales = new PdfPTable(4);
            tableDeduccionesTotales.setWidths(new int[]{30, 30, 110, 70});
            tableDeduccionesTotales.setTotalWidth(280);
            tableDeduccionesTotales.setLockedWidth(true);
            tableDeduccionesTotales.setWidthPercentage(100);
            tableDeduccionesTotales.getDefaultCell().setBorder(Rectangle.NO_BORDER);

            for (Deduccion deduccionesCargadas : facturaDto.getComplementos().getNomina().getDeducciones().getDeduccion()) {
                PdfPCell cellDeduccioneTipo = new PdfPCell(new Paragraph(deduccionesCargadas.getTipoDeduccion(), fontContenido));
                cellDeduccioneTipo.setHorizontalAlignment(Element.ALIGN_LEFT);
                dibujarContornoIzquierdo(cellDeduccioneTipo);

                PdfPCell cellDeduccionesClave = new PdfPCell(new Paragraph(deduccionesCargadas.getClave(), fontContenido));
                cellDeduccionesClave.setHorizontalAlignment(Element.ALIGN_LEFT);
                cellDeduccionesClave.setBorder(Rectangle.NO_BORDER);

                PdfPCell cellDeduccionesConcepto = new PdfPCell(new Paragraph(deduccionesCargadas.getConcepto(), fontContenido));
                cellDeduccionesConcepto.setHorizontalAlignment(Element.ALIGN_LEFT);
                cellDeduccionesConcepto.setBorder(Rectangle.NO_BORDER);

                PdfPCell cellDeduccionesImporte = new PdfPCell(new Paragraph(Utilities.toFormatMoneda(deduccionesCargadas.getImporte()), fontContenido));
                cellDeduccionesImporte.setHorizontalAlignment(Element.ALIGN_RIGHT);
                dibujarContornoDerecho(cellDeduccionesImporte);

                tableDeduccionesTotales.addCell(cellDeduccioneTipo);
                tableDeduccionesTotales.addCell(cellDeduccionesClave);
                tableDeduccionesTotales.addCell(cellDeduccionesConcepto);
                tableDeduccionesTotales.addCell(cellDeduccionesImporte);
            }

            PdfPCell cellSeparacionTotales = new PdfPCell();
            cellSeparacionTotales.setColspan(4);
            cellSeparacionTotales.setBorderWidthTop(0f);
            cellSeparacionTotales.setBorderWidthBottom(0.3f);
            cellSeparacionTotales.setBorderWidthRight(0.3f);
            cellSeparacionTotales.setBorderWidthLeft(0.3f);
            tableDeduccionesTotales.addCell(cellSeparacionTotales);

            PdfPCell cellDeduccionesTotal = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.N_TOTALDEDUCCIONES).concat(" : "), fontEtiqueta));
            cellDeduccionesTotal.setColspan(3);
            cellDeduccionesTotal.setHorizontalAlignment(Element.ALIGN_LEFT);
            dibujarContornoIzquierdo(cellDeduccionesTotal);

            PdfPCell cellDeducionesSubtotal = new PdfPCell(new Paragraph(Utilities.toFormatMoneda(facturaDto.getComplementos().getNomina().getDeducciones().getTotalOtrasDeducciones()), fontContenido));
            cellDeducionesSubtotal.setHorizontalAlignment(Element.ALIGN_RIGHT);
            dibujarContornoDerecho(cellDeducionesSubtotal);

            PdfPCell cellDeduccionesImpuestos = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.N_TOTALIMPUESTOSRET), fontEtiqueta));
            cellDeduccionesImpuestos.setColspan(3);
            cellDeduccionesImpuestos.setHorizontalAlignment(Element.ALIGN_LEFT);
            dibujarContornoIzquierdo(cellDeduccionesImpuestos);

            PdfPCell cellDeduccionesImpuestosSubtotal = new PdfPCell(new Paragraph(Utilities.toFormatMoneda(facturaDto.getComplementos().getNomina().getDeducciones().getTotalImpuestosRetenidos()), fontContenido));
            cellDeduccionesImpuestosSubtotal.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cellDeduccionesImpuestosSubtotal.setPaddingBottom(1.5f);
            dibujarContornoDerecho(cellDeduccionesImpuestosSubtotal);

            PdfPCell cellHeaderPercepciones = new PdfPCell(new Paragraph("", fontEtiqueta));
            cellHeaderPercepciones.setColspan(4);
            cellHeaderPercepciones.setPaddingTop(-2f);
            cellHeaderPercepciones.setHorizontalAlignment(Element.ALIGN_CENTER);
            dibujarContornoEncabezadosAbajo(cellHeaderPercepciones);

            tableDeduccionesTotales.addCell(cellDeduccionesTotal);
            tableDeduccionesTotales.addCell(cellDeducionesSubtotal);
            tableDeduccionesTotales.addCell(cellDeduccionesImpuestos);
            tableDeduccionesTotales.addCell(cellDeduccionesImpuestosSubtotal);
            tableDeduccionesTotales.addCell(cellHeaderPercepciones);
            tablePercepcionesYDeducciones.addCell(tableDeduccionesTotales);
        } else {
            PdfPCell celdaVacia = new PdfPCell();
            celdaVacia.setBorder(Rectangle.NO_BORDER);
            tablePercepcionesYDeducciones.addCell(celdaVacia);
        }
        document.add(tablePercepcionesYDeducciones);
    }

    /**
     * Metodo para agregar otros pagos e incapacidad.
     *
     * @param document
     * @return Tabla para agregar los encabezados de la tabla Percepciones y
     * deducciones
     * @throws DocumentException
     */
    public void agregarEncabezadosOtrosPagos(Document document, CFDI facturaDto) throws DocumentException {
        PdfPTable tableEncabezadosOtrosPagos = new PdfPTable(2);
        tableEncabezadosOtrosPagos.setWidths(new int[]{80, 80});
        tableEncabezadosOtrosPagos.setTotalWidth(560);
        tableEncabezadosOtrosPagos.setLockedWidth(true);
        tableEncabezadosOtrosPagos.setWidthPercentage(100);
        tableEncabezadosOtrosPagos.getDefaultCell().setBorder(Rectangle.NO_BORDER);

        PdfPTable tableOtrosPagos = new PdfPTable(4);
        tableOtrosPagos.setWidths(new int[]{56, 56, 56, 56});
        tableOtrosPagos.setTotalWidth(280);
        tableOtrosPagos.setLockedWidth(true);
        tableOtrosPagos.setWidthPercentage(100);
        tableOtrosPagos.getDefaultCell().setBorder(Rectangle.NO_BORDER);

        if (facturaDto.getComplementos().getNomina().getOtrosPagos().getOtroPago() != null && !facturaDto.getComplementos().getNomina().getOtrosPagos().getOtroPago().isEmpty()) {

            PdfPCell cellHeaderOtrosPagos = new PdfPCell(new Paragraph("OTROS PAGOS", fontEtiqueta));
            cellHeaderOtrosPagos.setColspan(4);
            cellHeaderOtrosPagos.setHorizontalAlignment(Element.ALIGN_CENTER);
            dibujarContornoEncabezadosArriba(cellHeaderOtrosPagos);

            PdfPCell cellOtrosPagosTipo = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.N_TIPO), fontEtiqueta));
            cellOtrosPagosTipo.setHorizontalAlignment(Element.ALIGN_LEFT);
            cellOtrosPagosTipo.setBorder(Rectangle.NO_BORDER);
            dibujarContornoIzquierdo(cellOtrosPagosTipo);

            PdfPCell cellOtrosPagosClave = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.N_CLAVE), fontEtiqueta));
            cellOtrosPagosClave.setHorizontalAlignment(Element.ALIGN_LEFT);
            cellOtrosPagosClave.setBorder(Rectangle.NO_BORDER);

            PdfPCell cellOtrosPagosConcepto = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.N_CONCEPTO), fontEtiqueta));
            cellOtrosPagosConcepto.setHorizontalAlignment(Element.ALIGN_LEFT);
            cellOtrosPagosConcepto.setBorder(Rectangle.NO_BORDER);

            PdfPCell cellOtrosPagosImporteGravado = new PdfPCell(new Paragraph("Importe", fontEtiqueta));
            cellOtrosPagosImporteGravado.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cellOtrosPagosImporteGravado.setBorder(Rectangle.NO_BORDER);
            dibujarContornoDerecho(cellOtrosPagosImporteGravado);

            PdfPCell cellOtrosPagosCierreTabla = new PdfPCell(new Paragraph("", fontEtiqueta));
            cellOtrosPagosCierreTabla.setColspan(4);
            cellOtrosPagosCierreTabla.setHorizontalAlignment(Element.ALIGN_CENTER);
            dibujarContornoEncabezadosAbajo(cellOtrosPagosCierreTabla);

            tableOtrosPagos.addCell(cellHeaderOtrosPagos);
            tableOtrosPagos.addCell(cellOtrosPagosTipo);
            tableOtrosPagos.addCell(cellOtrosPagosClave);
            tableOtrosPagos.addCell(cellOtrosPagosConcepto);
            tableOtrosPagos.addCell(cellOtrosPagosImporteGravado);
            tableOtrosPagos.addCell(cellOtrosPagosCierreTabla);
        } else {

            PdfPCell cellHeaderDeducciones = new PdfPCell();
            cellHeaderDeducciones.setColspan(4);
            cellHeaderDeducciones.setHorizontalAlignment(Element.ALIGN_CENTER);
            cellHeaderDeducciones.setBorder(Rectangle.NO_BORDER);

            PdfPCell cellDeduccioneTipo = new PdfPCell();
            cellDeduccioneTipo.setHorizontalAlignment(Element.ALIGN_LEFT);
            cellDeduccioneTipo.setBorder(Rectangle.NO_BORDER);

            PdfPCell cellDeduccionesClave = new PdfPCell();
            cellDeduccionesClave.setHorizontalAlignment(Element.ALIGN_LEFT);
            cellDeduccionesClave.setBorder(Rectangle.NO_BORDER);

            PdfPCell cellDeduccionesImporte = new PdfPCell();
            cellDeduccionesImporte.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cellDeduccionesImporte.setBorder(Rectangle.NO_BORDER);

            PdfPCell cellDeduccionesCierreTabla = new PdfPCell(new Paragraph("", fontEtiqueta));
            cellDeduccionesCierreTabla.setColspan(4);
            cellDeduccionesCierreTabla.setHorizontalAlignment(Element.ALIGN_CENTER);
            cellDeduccionesCierreTabla.setBorder(Rectangle.NO_BORDER);

            tableOtrosPagos.addCell(cellHeaderDeducciones);
            tableOtrosPagos.addCell(cellDeduccioneTipo);
            tableOtrosPagos.addCell(cellDeduccionesClave);
            tableOtrosPagos.addCell(cellDeduccionesClave);
            tableOtrosPagos.addCell(cellDeduccionesImporte);
            tableOtrosPagos.addCell(cellDeduccionesCierreTabla);
        }

        PdfPTable tableIncapacidad = new PdfPTable(3);
        tableIncapacidad.setWidths(new int[]{56, 56, 56});
        tableIncapacidad.setTotalWidth(280);
        tableIncapacidad.setLockedWidth(true);
        tableIncapacidad.setWidthPercentage(100);
        tableIncapacidad.getDefaultCell().setBorder(Rectangle.NO_BORDER);
        if (facturaDto.getComplementos().getNomina().getIncapacidades() != null && !facturaDto.getComplementos().getNomina().getIncapacidades().getIncapacidad().isEmpty()) {

            PdfPTable tablePercepciones = new PdfPTable(3);
            tablePercepciones.setWidths(new int[]{56, 56, 56});
            tablePercepciones.setTotalWidth(280);
            tablePercepciones.setLockedWidth(true);
            tablePercepciones.setWidthPercentage(100);
            tablePercepciones.getDefaultCell().setBorder(Rectangle.NO_BORDER);

            PdfPCell cellHeaderPercepciones = new PdfPCell(new Paragraph("INCAPACIDAD", fontEtiqueta));
            cellHeaderPercepciones.setColspan(3);
            cellHeaderPercepciones.setHorizontalAlignment(Element.ALIGN_CENTER);
            dibujarContornoEncabezadosArriba(cellHeaderPercepciones);

            PdfPCell cellPercepcionesTipo = new PdfPCell(new Paragraph("Dias", fontEtiqueta));
            cellPercepcionesTipo.setHorizontalAlignment(Element.ALIGN_LEFT);
            cellPercepcionesTipo.setBorder(Rectangle.NO_BORDER);
            dibujarContornoIzquierdo(cellPercepcionesTipo);

            PdfPCell cellPercepcionesClave = new PdfPCell(new Paragraph("Tipo de incapacidad", fontEtiqueta));
            cellPercepcionesClave.setHorizontalAlignment(Element.ALIGN_LEFT);
            cellPercepcionesClave.setBorder(Rectangle.NO_BORDER);

            PdfPCell cellPercepcionesImporteGravado = new PdfPCell(new Paragraph("Importe", fontEtiqueta));
            cellPercepcionesImporteGravado.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cellPercepcionesImporteGravado.setBorder(Rectangle.NO_BORDER);
            dibujarContornoDerecho(cellPercepcionesImporteGravado);

            PdfPCell cellPercepcionesCierreTabla = new PdfPCell(new Paragraph("", fontEtiqueta));
            cellPercepcionesCierreTabla.setColspan(3);
            cellPercepcionesCierreTabla.setHorizontalAlignment(Element.ALIGN_CENTER);
            dibujarContornoEncabezadosAbajo(cellPercepcionesCierreTabla);

            tableIncapacidad.addCell(cellHeaderPercepciones);
            tableIncapacidad.addCell(cellPercepcionesTipo);
            tableIncapacidad.addCell(cellPercepcionesClave);
            tableIncapacidad.addCell(cellPercepcionesImporteGravado);
            tableIncapacidad.addCell(cellPercepcionesCierreTabla);
        } else {

            PdfPCell cellHeaderDeducciones = new PdfPCell();
            cellHeaderDeducciones.setColspan(3);
            cellHeaderDeducciones.setHorizontalAlignment(Element.ALIGN_CENTER);
            cellHeaderDeducciones.setBorder(Rectangle.NO_BORDER);

            PdfPCell cellDeduccioneTipo = new PdfPCell();
            cellDeduccioneTipo.setHorizontalAlignment(Element.ALIGN_LEFT);
            cellDeduccioneTipo.setBorder(Rectangle.NO_BORDER);

            PdfPCell cellDeduccionesClave = new PdfPCell();
            cellDeduccionesClave.setHorizontalAlignment(Element.ALIGN_LEFT);
            cellDeduccionesClave.setBorder(Rectangle.NO_BORDER);

            PdfPCell cellDeduccionesImporte = new PdfPCell();
            cellDeduccionesImporte.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cellDeduccionesImporte.setBorder(Rectangle.NO_BORDER);

            PdfPCell cellDeduccionesCierreTabla = new PdfPCell(new Paragraph("", fontEtiqueta));
            cellDeduccionesCierreTabla.setColspan(3);
            cellDeduccionesCierreTabla.setHorizontalAlignment(Element.ALIGN_CENTER);
            cellDeduccionesCierreTabla.setBorder(Rectangle.NO_BORDER);

            tableIncapacidad.addCell(cellHeaderDeducciones);
            tableIncapacidad.addCell(cellDeduccioneTipo);
            tableIncapacidad.addCell(cellDeduccionesClave);
            tableIncapacidad.addCell(cellDeduccionesImporte);
            tableIncapacidad.addCell(cellDeduccionesCierreTabla);
        }

        tableEncabezadosOtrosPagos.addCell(tableOtrosPagos);
        tableEncabezadosOtrosPagos.addCell(tableIncapacidad);
        document.add(tableEncabezadosOtrosPagos);

    }

    public void agregarOtrosPagos(Document document, CFDI facturaDto) throws DocumentException {
        PdfPTable tableOtrosPagos = new PdfPTable(2);
        tableOtrosPagos.setWidths(new int[]{80, 80});
        tableOtrosPagos.setTotalWidth(560);
        tableOtrosPagos.setLockedWidth(true);
        tableOtrosPagos.setWidthPercentage(100);
        tableOtrosPagos.getDefaultCell().setBorder(Rectangle.NO_BORDER);
        tableOtrosPagos.getDefaultCell().setPaddingTop(-2f);

        PdfPTable tableOtrosPago = new PdfPTable(4);
        tableOtrosPago.setWidths(new int[]{56, 56, 56, 56});
        tableOtrosPago.setTotalWidth(280);
        tableOtrosPago.setLockedWidth(true);
        tableOtrosPago.setWidthPercentage(100);
        tableOtrosPago.getDefaultCell().setBorder(Rectangle.NO_BORDER);

        if (facturaDto.getComplementos().getNomina().getOtrosPagos().getOtroPago() != null && !facturaDto.getComplementos().getNomina().getOtrosPagos().getOtroPago().isEmpty()) {

            Iterator iteratorOtrosPagos = facturaDto.getComplementos().getNomina().getOtrosPagos().getOtroPago().iterator();
            while (iteratorOtrosPagos.hasNext()) {
                OtroPago otrosPagos = (OtroPago) iteratorOtrosPagos.next();

                CatTipoOtroPago tipoOtroPago = catalogosSatService.getCatTipoOtroPagooByClave (otrosPagos.getTipoOtroPago());
                PdfPCell cellOtrosPagosTipo = new PdfPCell(new Paragraph(tipoOtroPago.getDescripcion(), fontContenido));
                cellOtrosPagosTipo.setHorizontalAlignment(Element.ALIGN_LEFT);
                dibujarContornoIzquierdo(cellOtrosPagosTipo);

                PdfPCell cellOtrosPagosClave = new PdfPCell(new Paragraph(otrosPagos.getClave(), fontContenido));
                cellOtrosPagosClave.setHorizontalAlignment(Element.ALIGN_LEFT);
                cellOtrosPagosClave.setBorder(Rectangle.NO_BORDER);

                PdfPCell cellOtrosPagosConcepto = new PdfPCell(new Paragraph(otrosPagos.getConcepto(), fontContenido));
                cellOtrosPagosConcepto.setHorizontalAlignment(Element.ALIGN_LEFT);
                cellOtrosPagosConcepto.setBorder(Rectangle.NO_BORDER);

                PdfPCell cellOtrosPagosImporteGravado = new PdfPCell(new Paragraph(Utilities.toFormatMoneda(otrosPagos.getImporte()), fontContenido));
                cellOtrosPagosImporteGravado.setHorizontalAlignment(Element.ALIGN_RIGHT);
                dibujarContornoDerecho(cellOtrosPagosImporteGravado);

                tableOtrosPago.addCell(cellOtrosPagosTipo);
                tableOtrosPago.addCell(cellOtrosPagosClave);
                tableOtrosPago.addCell(cellOtrosPagosConcepto);
                tableOtrosPago.addCell(cellOtrosPagosImporteGravado);

                if (otrosPagos.getSubsidioAlEmpleo() != null) {
                    PdfPCell cellSubsidioCausado = new PdfPCell(new Paragraph("Subsidio Causado:  " + Utilities.toFormatMoneda(otrosPagos.getSubsidioAlEmpleo()), fontContenido));
                    cellSubsidioCausado.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    cellSubsidioCausado.setColspan(4);
                    dibujarContornoEncabezadosAbajo(cellSubsidioCausado);
                    tableOtrosPago.addCell(cellSubsidioCausado);
                } else {
                    PdfPCell cellSubsidioCausado = new PdfPCell();
                    cellSubsidioCausado.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    cellSubsidioCausado.setColspan(4);
                    dibujarContornoEncabezadosAbajo(cellSubsidioCausado);
                    tableOtrosPago.addCell(cellSubsidioCausado);
                }

                if (otrosPagos.getCompensacionSaldosAFavor() != null) {
                    PdfPCell cellCompensacionAFavor = new PdfPCell(new Paragraph("Compensación o saldo a favor", fontEtiqueta));
                    cellCompensacionAFavor.setHorizontalAlignment(Element.ALIGN_LEFT);
                    cellCompensacionAFavor.setColspan(4);
                    dibujarEsquinaDerechaIzquierda(cellCompensacionAFavor);

                    PdfPCell cellAnioCompensacion = new PdfPCell(new Paragraph("Año: " + otrosPagos.getCompensacionSaldosAFavor().getAnio(), fontContenido));
                    cellAnioCompensacion.setHorizontalAlignment(Element.ALIGN_LEFT);
                    cellAnioCompensacion.setColspan(4);
                    dibujarEsquinaDerechaIzquierda(cellAnioCompensacion);

                    PdfPCell cellRemanente = new PdfPCell(new Paragraph("Remanente: " + Utilities.toFormatMoneda(otrosPagos.getCompensacionSaldosAFavor().getRemanenteSalFav()), fontContenido));
                    cellRemanente.setHorizontalAlignment(Element.ALIGN_LEFT);
                    cellRemanente.setColspan(4);
                    dibujarEsquinaDerechaIzquierda(cellRemanente);

                    PdfPCell cellRemanenteSaldoFavor = new PdfPCell(new Paragraph("Saldo a favor: " + Utilities.toFormatMoneda(otrosPagos.getCompensacionSaldosAFavor().getSaldoAFavor()), fontContenido));
                    cellRemanenteSaldoFavor.setHorizontalAlignment(Element.ALIGN_LEFT);
                    cellRemanenteSaldoFavor.setColspan(4);
                    dibujarEsquinaDerechaIzquierda(cellRemanenteSaldoFavor);

                    PdfPCell cellCierreTabla = new PdfPCell(new Paragraph("", fontEtiqueta));
                    cellCierreTabla.setColspan(4);
                    cellCierreTabla.setHorizontalAlignment(Element.ALIGN_CENTER);
                    dibujarContornoEncabezadosAbajo(cellCierreTabla);

                    tableOtrosPago.addCell(cellCompensacionAFavor);
                    tableOtrosPago.addCell(cellAnioCompensacion);
                    tableOtrosPago.addCell(cellRemanente);
                    tableOtrosPago.addCell(cellRemanenteSaldoFavor);
                    tableOtrosPago.addCell(cellCierreTabla);
                }
            }
        }
        PdfPTable tableIncapacidad = new PdfPTable(3);
        tableIncapacidad.setWidths(new int[]{56, 56, 56});
        tableIncapacidad.setTotalWidth(280);
        tableIncapacidad.setLockedWidth(true);
        tableIncapacidad.setWidthPercentage(100);
        tableIncapacidad.getDefaultCell().setBorder(Rectangle.NO_BORDER);

        if (facturaDto.getComplementos().getNomina().getIncapacidades().getIncapacidad() != null && !facturaDto.getComplementos().getNomina().getIncapacidades().getIncapacidad().isEmpty()) {
            Iterator iteratorTipoIncapacidad = facturaDto.getComplementos().getNomina().getIncapacidades().getIncapacidad().iterator();
            while (iteratorTipoIncapacidad.hasNext()) {
                Incapacidad tipoIncapacidad = (Incapacidad) iteratorTipoIncapacidad.next();

                PdfPCell cellIncapacidadTipo = new PdfPCell(new Paragraph(String.valueOf(tipoIncapacidad.getDiasIncapacidad()), fontContenido));
                cellIncapacidadTipo.setHorizontalAlignment(Element.ALIGN_LEFT);
                dibujarContornoIzquierdo(cellIncapacidadTipo);
                CatTipoIncapacidad tipoIncapacidadCatalogo = catalogosSatService.getCatTipoIncapacidadByClave(tipoIncapacidad.getTipoIncapacidad());
                PdfPCell cellIncapacidadConcepto = new PdfPCell(new Paragraph(tipoIncapacidad.getTipoIncapacidad().concat(" - ").concat(tipoIncapacidadCatalogo != null ? tipoIncapacidadCatalogo.getDescripcion() : ""), fontContenido));
                cellIncapacidadConcepto.setHorizontalAlignment(Element.ALIGN_LEFT);
                cellIncapacidadConcepto.setBorder(Rectangle.NO_BORDER);

                PdfPCell cellIncapacidadImporte = new PdfPCell(new Paragraph(Utilities.toFormatMoneda(tipoIncapacidad.getImporteMonetario()), fontContenido));
                cellIncapacidadImporte.setHorizontalAlignment(Element.ALIGN_RIGHT);
                dibujarContornoDerecho(cellIncapacidadImporte);

                PdfPCell cellIncapacidadCierreTabla = new PdfPCell(new Paragraph("", fontEtiqueta));
                cellIncapacidadCierreTabla.setColspan(3);
                cellIncapacidadCierreTabla.setHorizontalAlignment(Element.ALIGN_CENTER);
                dibujarContornoEncabezadosAbajo(cellIncapacidadCierreTabla);

                tableIncapacidad.addCell(cellIncapacidadTipo);
                tableIncapacidad.addCell(cellIncapacidadConcepto);
                tableIncapacidad.addCell(cellIncapacidadImporte);
                tableIncapacidad.addCell(cellIncapacidadCierreTabla);
            }
        }

        tableOtrosPagos.addCell(tableOtrosPago);
        tableOtrosPagos.addCell(tableIncapacidad);
        document.add(tableOtrosPagos);
    }

    /**
     * Metodo para agregar los conceptos para la nomina cantidad, descripcion de
     * la factura, valor unitario, importe, etc con sus correspondientes montos
     *
     * @param document
     * @return Tabla para agregar los conceptos de la nomina con sus montos
     * @throws DocumentException
     */
    public void agregarConceptos(Document document, CFDI facturaDto) throws DocumentException {
        PdfPTable tableConceptos = new PdfPTable(6);
        tableConceptos.setWidths(new int[]{112, 112, 112, 112, 112, 112});
        tableConceptos.setTotalWidth(560);
        tableConceptos.setLockedWidth(true);
        tableConceptos.setWidthPercentage(100);
        tableConceptos.getDefaultCell().setBorder(Rectangle.NO_BORDER);

        PdfPCell cellConceptos = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.P_CONCEPTO), fontEtiqueta));
        cellConceptos.setColspan(6);
        cellConceptos.setHorizontalAlignment(Element.ALIGN_CENTER);
        cellConceptos.setBorder(Rectangle.NO_BORDER);

        PdfPCell cellConceptosCantidad = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.P_CANTIDAD), fontEtiqueta));
        cellConceptosCantidad.setHorizontalAlignment(Element.ALIGN_LEFT);
        cellConceptosCantidad.setBorder(Rectangle.NO_BORDER);

        PdfPCell cellConceptosUnidadMedida = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.P_UNIDADMEDIDA), fontEtiqueta));
        cellConceptosUnidadMedida.setHorizontalAlignment(Element.ALIGN_LEFT);
        cellConceptosUnidadMedida.setBorder(Rectangle.NO_BORDER);

        PdfPCell cellClaveProducto = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.P_CLAVEPRODUCTO), fontEtiqueta));
        cellClaveProducto.setHorizontalAlignment(Element.ALIGN_LEFT);
        cellClaveProducto.setBorder(Rectangle.NO_BORDER);

        PdfPCell cellConceptosDescripcion = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.N_DESCRIPCION), fontEtiqueta));
        cellConceptosDescripcion.setHorizontalAlignment(Element.ALIGN_LEFT);
        cellConceptosDescripcion.setBorder(Rectangle.NO_BORDER);

        PdfPCell cellConceptosValorUnitario = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.N_VALORUNITARIO), fontEtiqueta));
        cellConceptosValorUnitario.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cellConceptosValorUnitario.setBorder(Rectangle.NO_BORDER);

        PdfPCell cellConceptosImporte = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.P_IMPORTE), fontEtiqueta));
        cellConceptosImporte.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cellConceptosImporte.setBorder(Rectangle.NO_BORDER);

        tableConceptos.addCell(cellConceptos);
        tableConceptos.addCell(cellConceptosCantidad);
        tableConceptos.addCell(cellConceptosUnidadMedida);
        tableConceptos.addCell(cellClaveProducto);
        tableConceptos.addCell(cellConceptosDescripcion);
        tableConceptos.addCell(cellConceptosValorUnitario);
        tableConceptos.addCell(cellConceptosImporte);

        if (facturaDto.getConceptos() != null && !facturaDto.getConceptos().isEmpty()) {
            Iterator iterator = facturaDto.getConceptos().iterator();
            while (iterator.hasNext()) {
                Conceptos conceptos = (Conceptos) iterator.next();
                BigDecimal pUnitario = BigDecimal.ZERO;
                BigDecimal importe = BigDecimal.ZERO;

                PdfPCell celdaCantidad = new PdfPCell(new Paragraph(new Phrase(conceptos.getCantidad().toString(), fontContenido)));
                celdaCantidad.setHorizontalAlignment(Element.ALIGN_LEFT);
                celdaCantidad.setBorder(Rectangle.NO_BORDER);

                PdfPCell celdaUnidad = new PdfPCell(new Paragraph(new Phrase(conceptos.getUnidad(), fontContenido)));
                celdaUnidad.setHorizontalAlignment(Element.ALIGN_LEFT);
                celdaUnidad.setBorder(Rectangle.NO_BORDER);

                PdfPCell celdaclaveProducto = new PdfPCell(new Paragraph(new Phrase(conceptos.getClaveProdServ(), fontContenido)));
                celdaclaveProducto.setHorizontalAlignment(Element.ALIGN_LEFT);
                celdaclaveProducto.setBorder(Rectangle.NO_BORDER);

                PdfPCell celdaDescripcion = new PdfPCell(new Paragraph(new Phrase(conceptos.getDescripcion(), fontContenido)));
                celdaDescripcion.setHorizontalAlignment(Element.ALIGN_LEFT);
                celdaDescripcion.setBorder(Rectangle.NO_BORDER);

                pUnitario = pUnitario.add(conceptos.getValorUnitario().setScale(2, RoundingMode.HALF_EVEN));
                PdfPCell celdaPUnitario = new PdfPCell(new Paragraph(new Phrase(Utilities.toFormatMoneda(pUnitario), fontContenido)));
                celdaPUnitario.setHorizontalAlignment(Element.ALIGN_RIGHT);
                celdaPUnitario.setBorder(Rectangle.NO_BORDER);

                importe = importe.add(conceptos.getImporte().setScale(2, RoundingMode.HALF_EVEN));
                PdfPCell celdaImporte = new PdfPCell(new Paragraph(new Phrase(Utilities.toFormatMoneda(importe), fontContenido)));
                celdaImporte.setHorizontalAlignment(Element.ALIGN_RIGHT);
                celdaImporte.setBorder(Rectangle.NO_BORDER);

                tableConceptos.addCell(celdaCantidad);
                tableConceptos.addCell(celdaUnidad);
                tableConceptos.addCell(celdaclaveProducto);
                tableConceptos.addCell(celdaDescripcion);
                tableConceptos.addCell(celdaPUnitario);
                tableConceptos.addCell(celdaImporte);
            }
        }
        document.add(tableConceptos);
    }

    /**
     * Metodo para agregar los subtotales de la nomina subtotal, importe con
     * letra, moneda, etc
     *
     * @param document
     * @return Tabla para agregar los subtotales de la nomina
     * @throws DocumentException
     */
    public void agregarSubtotales(Document document, CFDI facturaDto) throws DocumentException {
        PdfPTable tableSubtotales = new PdfPTable(5);
        //tableSubtotales.setWidths(new int[]{300, 200});
        tableSubtotales.setWidths(new int[]{112, 112, 112, 112, 112});
        tableSubtotales.setTotalWidth(560);
        tableSubtotales.setLockedWidth(true);
        tableSubtotales.setWidthPercentage(100);
        tableSubtotales.getDefaultCell().setBorder(Rectangle.NO_BORDER);

        PdfPCell borderSubtotales = new PdfPCell();
        borderSubtotales.setColspan(5);
        borderSubtotales.setBackgroundColor(BaseColor.LIGHT_GRAY);
        borderSubtotales.setHorizontalAlignment(Element.ALIGN_LEFT);
        borderSubtotales.setBorder(Rectangle.NO_BORDER);

        PdfPCell cellBlanca = new PdfPCell(new Paragraph("", fontEtiqueta));
        cellBlanca.setHorizontalAlignment(Element.ALIGN_LEFT);
        cellBlanca.setBorder(Rectangle.NO_BORDER);

        PdfPCell cellSubtotales = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.T_SUBTOTAL), fontEtiqueta));
        cellSubtotales.setHorizontalAlignment(Element.ALIGN_LEFT);
        cellSubtotales.setBorder(Rectangle.NO_BORDER);

        PdfPCell cellSubtotalesV = new PdfPCell(new Paragraph(Utilities.toFormatMoneda(facturaDto.getSubTotal()), fontContenido));
        cellSubtotalesV.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cellSubtotalesV.setBorder(Rectangle.NO_BORDER);

        PdfPCell cellSubtotalesDescuento = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.T_DESCUENTO), fontEtiqueta));
        cellSubtotalesDescuento.setHorizontalAlignment(Element.ALIGN_LEFT);
        cellSubtotalesDescuento.setBorder(Rectangle.NO_BORDER);

        PdfPCell cellSubtotalesDescuentoV = new PdfPCell(new Paragraph(Utilities.toFormatMoneda(facturaDto.getDescuento()), fontContenido));
        cellSubtotalesDescuentoV.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cellSubtotalesDescuentoV.setBorder(Rectangle.NO_BORDER);

        PdfPCell cellSubtotalesImporteNeto = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.N_IMPORTENETO), fontEtiqueta));
        cellSubtotalesImporteNeto.setHorizontalAlignment(Element.ALIGN_LEFT);
        cellSubtotalesImporteNeto.setBorder(Rectangle.NO_BORDER);

        PdfPCell cellSubtotalesImporteNetoV = new PdfPCell(new Paragraph(Utilities.toFormatMoneda(facturaDto.getTotal()), fontContenido));
        cellSubtotalesImporteNetoV.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cellSubtotalesImporteNetoV.setBorder(Rectangle.NO_BORDER);

        PdfPCell cellSubtotalesMoneda = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.N_MONEDA), fontEtiqueta));
        cellSubtotalesMoneda.setHorizontalAlignment(Element.ALIGN_LEFT);
        cellSubtotalesMoneda.setBorder(Rectangle.NO_BORDER);

        PdfPCell cellSubtotalesMonedaV = new PdfPCell(new Paragraph(facturaDto.getMoneda() != null ? facturaDto.getMoneda() : "", fontContenido));
        cellSubtotalesMonedaV.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cellSubtotalesMonedaV.setBorder(Rectangle.NO_BORDER);

        PdfPCell cellSubtotalesTotalLetra = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.N_IMPORTELETRA).concat(" : "), fontEtiqueta));
        cellSubtotalesTotalLetra.setHorizontalAlignment(Element.ALIGN_LEFT);
        cellSubtotalesTotalLetra.setBorder(Rectangle.NO_BORDER);

        PdfPCell cellSubtotalesTotalLetraV = new PdfPCell(new Paragraph(Utilities.crearTotalLetra(facturaDto), fontContenido));
        cellSubtotalesTotalLetraV.setHorizontalAlignment(Element.ALIGN_LEFT);
        cellSubtotalesTotalLetraV.setBorder(Rectangle.NO_BORDER);

        tableSubtotales.addCell(borderSubtotales);

        tableSubtotales.addCell(cellBlanca);
        tableSubtotales.addCell(cellBlanca);
        tableSubtotales.addCell(cellBlanca);
        tableSubtotales.addCell(cellSubtotales);
        tableSubtotales.addCell(cellSubtotalesV);

        tableSubtotales.addCell(cellBlanca);
        tableSubtotales.addCell(cellBlanca);
        tableSubtotales.addCell(cellBlanca);
        tableSubtotales.addCell(cellSubtotalesDescuento);
        tableSubtotales.addCell(cellSubtotalesDescuentoV);

        tableSubtotales.addCell(cellBlanca);
        tableSubtotales.addCell(cellBlanca);
        tableSubtotales.addCell(cellBlanca);
        tableSubtotales.addCell(cellSubtotalesImporteNeto);
        tableSubtotales.addCell(cellSubtotalesImporteNetoV);

        tableSubtotales.addCell(cellBlanca);
        tableSubtotales.addCell(cellBlanca);
        tableSubtotales.addCell(cellBlanca);
        tableSubtotales.addCell(cellSubtotalesMoneda);
        tableSubtotales.addCell(cellSubtotalesMonedaV);

        tableSubtotales.addCell(cellBlanca);
        tableSubtotales.addCell(cellBlanca);
        tableSubtotales.addCell(cellBlanca);
        tableSubtotales.addCell(cellSubtotalesTotalLetra);
        tableSubtotales.addCell(cellSubtotalesTotalLetraV);

        document.add(tableSubtotales);
    }

    /**
     * Metodo para agregar los sellos del SAT
     *
     * @param document
     * @return Tabla para agregar los sellos de la nomina
     * @throws DocumentException
     */
    public void agregarSellos(Document document, CFDI facturaDto) throws DocumentException {
        PdfPTable tableSellos = new PdfPTable(1);
        tableSellos.setWidths(new int[]{560});
        tableSellos.setTotalWidth(560);
        tableSellos.setLockedWidth(true);
        tableSellos.setWidthPercentage(100);
        tableSellos.getDefaultCell().setBorder(Rectangle.NO_BORDER);

        PdfPCell borderSellos = new PdfPCell();
        borderSellos.setBackgroundColor(BaseColor.LIGHT_GRAY);
        borderSellos.setHorizontalAlignment(Element.ALIGN_LEFT);
        borderSellos.setBorder(Rectangle.NO_BORDER);

        PdfPCell cellCadenaOriginal = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.S_CADENACDSAT).concat(" : "), fontEtiqueta));
        cellCadenaOriginal.setHorizontalAlignment(Element.ALIGN_LEFT);
        cellCadenaOriginal.setBorder(Rectangle.NO_BORDER);

        PdfPCell cellCadenaOriginalV = new PdfPCell(new Paragraph(facturaDto.getTimbrado().getCadenaOriginal() , fontContenido));
        cellCadenaOriginalV.setHorizontalAlignment(Element.ALIGN_LEFT);
        cellCadenaOriginalV.setBorder(Rectangle.NO_BORDER);

        PdfPCell cellSelloDigitalEmisor = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.S_SDCFDI).concat(" : "), fontEtiqueta));
        cellSelloDigitalEmisor.setHorizontalAlignment(Element.ALIGN_LEFT);
        cellSelloDigitalEmisor.setBorder(Rectangle.NO_BORDER);

        PdfPCell cellSelloDigitalEmisorV = new PdfPCell(new Paragraph(facturaDto.getTimbrado().getSelloCFDI(), fontContenido));
        cellSelloDigitalEmisorV.setHorizontalAlignment(Element.ALIGN_LEFT);
        cellSelloDigitalEmisorV.setBorder(Rectangle.NO_BORDER);

        PdfPCell cellSelloDigitalSAT = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.S_SD).concat(" : "), fontEtiqueta));
        cellSelloDigitalSAT.setHorizontalAlignment(Element.ALIGN_LEFT);
        cellSelloDigitalSAT.setBorder(Rectangle.NO_BORDER);

        PdfPCell cellSelloDigitalSATV = new PdfPCell(new Paragraph(facturaDto.getTimbrado().getSelloSAT(), fontContenido));
        cellSelloDigitalSATV.setHorizontalAlignment(Element.ALIGN_LEFT);
        cellSelloDigitalSATV.setBorder(Rectangle.NO_BORDER);

        tableSellos.addCell(borderSellos);
        tableSellos.addCell(cellCadenaOriginal);
        tableSellos.addCell(cellCadenaOriginalV);
        tableSellos.addCell(cellSelloDigitalEmisor);
        tableSellos.addCell(cellSelloDigitalEmisorV);
        tableSellos.addCell(cellSelloDigitalSAT);
        tableSellos.addCell(cellSelloDigitalSATV);
        document.add(tableSellos);
    }

    /**
     * Metodo para agregar datos del emisor y el codigo qr
     *
     * @param document
     * @return Tabla para agregar con los datos del emisor y el codigo qr
     * generado
     * @throws Exception
     */
    public void agregarDatosEmisionYQr(Document document, CFDI facturaDto, CFDI.Complementos.Nomina nomina) throws Exception {
        PdfPTable tableDatosEmisionQR = new PdfPTable(2);
        tableDatosEmisionQR.setWidths(new int[]{460, 100});
        tableDatosEmisionQR.setTotalWidth(560);
        tableDatosEmisionQR.setLockedWidth(true);
        tableDatosEmisionQR.setWidthPercentage(100);
        tableDatosEmisionQR.getDefaultCell().setBorder(Rectangle.NO_BORDER);
        tableDatosEmisionQR.getDefaultCell().setFixedHeight(140f);

        PdfPTable tableDatosEmision = new PdfPTable(2);
        tableDatosEmision.setWidths(new int[]{150, 300});
        tableDatosEmision.setTotalWidth(460);
        tableDatosEmision.setLockedWidth(true);
        tableDatosEmision.setWidthPercentage(100);
        tableDatosEmision.getDefaultCell().setBorder(Rectangle.NO_BORDER);

        PdfPCell cellEmisionLugarFecha = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.N_LUGARFECHAEMISION).concat(" : "), fontEtiqueta));
        cellEmisionLugarFecha.setHorizontalAlignment(Element.ALIGN_LEFT);
        cellEmisionLugarFecha.setBorder(Rectangle.NO_BORDER);

        String lugarFechaEmision = "";
        if (facturaDto.getFecha() != null) {
            Date dateCheckIn = facturaDto.getFecha();
            DateFormat dateFormatCheckIn = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            lugarFechaEmision = dateFormatCheckIn.format(dateCheckIn);
        }
        PdfPCell cellEmisionLugarFechaV = new PdfPCell(new Paragraph(facturaDto.getLugarExpedicion().concat(" a ".concat(lugarFechaEmision)), fontContenido));
        cellEmisionLugarFechaV.setHorizontalAlignment(Element.ALIGN_LEFT);
        cellEmisionLugarFechaV.setBorder(Rectangle.NO_BORDER);

        PdfPCell cellEmisionFolioFiscal = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.E_FOLIOFISCAL).concat(" : "), fontEtiqueta));
        cellEmisionFolioFiscal.setHorizontalAlignment(Element.ALIGN_LEFT);
        cellEmisionFolioFiscal.setBorder(Rectangle.NO_BORDER);

        PdfPCell cellEmisionFolioFiscalV = new PdfPCell(new Paragraph(facturaDto.getTimbrado().getUuid(), fontContenido));
        cellEmisionFolioFiscalV.setHorizontalAlignment(Element.ALIGN_LEFT);
        cellEmisionFolioFiscalV.setBorder(Rectangle.NO_BORDER);

        PdfPCell cellEmisionCertificadoSAT = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.E_NOCERTSAT).concat(" : "), fontEtiqueta));
        cellEmisionCertificadoSAT.setHorizontalAlignment(Element.ALIGN_LEFT);
        cellEmisionCertificadoSAT.setBorder(Rectangle.NO_BORDER);

        PdfPCell cellEmisionCertificadoSATV = new PdfPCell(new Paragraph(facturaDto.getTimbrado().getNumCertificadoSAT(), fontContenido));
        cellEmisionCertificadoSATV.setHorizontalAlignment(Element.ALIGN_LEFT);
        cellEmisionCertificadoSATV.setBorder(Rectangle.NO_BORDER);

        PdfPCell cellEmisionFechaCertificacion = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.N_FECHAHORACERTIFICACION).concat(" : "), fontEtiqueta));
        cellEmisionFechaCertificacion.setHorizontalAlignment(Element.ALIGN_LEFT);
        cellEmisionFechaCertificacion.setBorder(Rectangle.NO_BORDER);

        PdfPCell cellEmisionFechaCertificacionV = new PdfPCell(new Paragraph(FechasUtil.getStringAnioMesDiaHoraMinSeg(facturaDto.getTimbrado() != null && facturaDto.getTimbrado().getFechaTimbrado() != null ? facturaDto.getTimbrado().getFechaTimbrado() : new Date()), fontContenido));
        cellEmisionFechaCertificacionV.setHorizontalAlignment(Element.ALIGN_LEFT);
        cellEmisionFechaCertificacionV.setBorder(Rectangle.NO_BORDER);

        // PdfPCell cellEmisionCertificadoEmisor = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.N_NOSERIEEMISOR).concat(" : "), fontEtiqueta));
        // cellEmisionCertificadoEmisor.setHorizontalAlignment(Element.ALIGN_LEFT);
        // cellEmisionCertificadoEmisor.setBorder(Rectangle.NO_BORDER);

        // PdfPCell cellEmisionCertificadoEmisorV = new PdfPCell(new Paragraph(facturaDto.getTimbrado().get(), fontContenido));
        // cellEmisionCertificadoEmisorV.setHorizontalAlignment(Element.ALIGN_LEFT);
        // cellEmisionCertificadoEmisorV.setBorder(Rectangle.NO_BORDER);

        PdfPCell cellRfcProveedor = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.RFC).concat(" : "), fontEtiqueta));
        cellRfcProveedor.setHorizontalAlignment(Element.ALIGN_LEFT);
        cellRfcProveedor.setBorder(Rectangle.NO_BORDER);

        PdfPCell cellRfcProveedorV = new PdfPCell(new Paragraph(facturaDto.getTimbrado().getRfcProvCertif(), fontContenido));
        cellRfcProveedorV.setHorizontalAlignment(Element.ALIGN_LEFT);
        cellRfcProveedorV.setBorder(Rectangle.NO_BORDER);

        PdfPCell cellEmisionFormaPago = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.T_FORMAPAGO).concat(" : "), fontEtiqueta));
        cellEmisionFormaPago.setHorizontalAlignment(Element.ALIGN_LEFT);
        cellEmisionFormaPago.setBorder(Rectangle.NO_BORDER);

        PdfPCell cellEmisionFormaPagoV = new PdfPCell(new Paragraph(facturaDto.getFormaPago(), fontContenido));
        cellEmisionFormaPagoV.setHorizontalAlignment(Element.ALIGN_LEFT);
        cellEmisionFormaPagoV.setBorder(Rectangle.NO_BORDER);

        PdfPCell cellEmisionMetodoPago = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.T_METODOPAGO).concat(" : "), fontEtiqueta));
        cellEmisionMetodoPago.setHorizontalAlignment(Element.ALIGN_LEFT);
        cellEmisionMetodoPago.setBorder(Rectangle.NO_BORDER);

        PdfPCell cellEmisionMetodoPagoV = new PdfPCell(new Paragraph(facturaDto.getMetodoPago(), fontContenido));
        cellEmisionMetodoPagoV.setHorizontalAlignment(Element.ALIGN_LEFT);
        cellEmisionMetodoPagoV.setBorder(Rectangle.NO_BORDER);

        PdfPCell cellEmisionBanco = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.N_BANCO).concat(" : "), fontEtiqueta));
        cellEmisionBanco.setHorizontalAlignment(Element.ALIGN_LEFT);
        cellEmisionBanco.setBorder(Rectangle.NO_BORDER);

        PdfPCell cellEmisionBancoV = new PdfPCell(new Paragraph(nomina.getReceptor().getBanco()  != null ? nomina.getReceptor().getBanco() : "", fontContenido));
        cellEmisionBancoV.setHorizontalAlignment(Element.ALIGN_LEFT);
        cellEmisionBancoV.setBorder(Rectangle.NO_BORDER);

        PdfPCell cellEmisionClabe = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.N_CLABE).concat(" : "), fontEtiqueta));
        cellEmisionClabe.setHorizontalAlignment(Element.ALIGN_LEFT);
        cellEmisionClabe.setBorder(Rectangle.NO_BORDER);

        PdfPCell cellEmisionClabeV = new PdfPCell(new Paragraph(nomina.getReceptor().getCuentaBancaria() != null ? nomina.getReceptor().getCuentaBancaria().toString() : "", fontContenido));
        cellEmisionClabeV.setHorizontalAlignment(Element.ALIGN_LEFT);
        cellEmisionClabeV.setBorder(Rectangle.NO_BORDER);

        PdfPCell cellEmisionTipoCambio = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.T_TIPOCAMBIO).concat(" : "), fontEtiqueta));
        cellEmisionTipoCambio.setHorizontalAlignment(Element.ALIGN_LEFT);
        cellEmisionTipoCambio.setBorder(Rectangle.NO_BORDER);

        PdfPCell cellEmisionTipoCambioV = null;
        if (String.valueOf(facturaDto.getTipoCambio()) != "1") {
            cellEmisionTipoCambioV = new PdfPCell(new Paragraph(Utilities.toFormatMoneda(facturaDto.getTipoCambio()), fontContenido));
            cellEmisionTipoCambioV.setHorizontalAlignment(Element.ALIGN_LEFT);
            cellEmisionTipoCambioV.setBorder(Rectangle.NO_BORDER);
        } else {
            cellEmisionTipoCambioV = new PdfPCell(new Paragraph("", fontContenido));
            cellEmisionTipoCambioV.setHorizontalAlignment(Element.ALIGN_LEFT);
            cellEmisionTipoCambioV.setBorder(Rectangle.NO_BORDER);
        }

        tableDatosEmision.addCell(cellEmisionLugarFecha);
        tableDatosEmision.addCell(cellEmisionLugarFechaV);
        tableDatosEmision.addCell(cellEmisionFolioFiscal);
        tableDatosEmision.addCell(cellEmisionFolioFiscalV);
        tableDatosEmision.addCell(cellEmisionCertificadoSAT);
        tableDatosEmision.addCell(cellEmisionCertificadoSATV);
        // tableDatosEmision.addCell(cellEmisionCertificadoEmisor);
        // tableDatosEmision.addCell(cellEmisionCertificadoEmisorV);
        tableDatosEmision.addCell(cellEmisionFechaCertificacion);
        tableDatosEmision.addCell(cellEmisionFechaCertificacionV);
        tableDatosEmision.addCell(cellRfcProveedor);
        tableDatosEmision.addCell(cellRfcProveedorV);
        tableDatosEmision.addCell(cellEmisionFormaPago);
        tableDatosEmision.addCell(cellEmisionFormaPagoV);
        tableDatosEmision.addCell(cellEmisionMetodoPago);
        tableDatosEmision.addCell(cellEmisionMetodoPagoV);
        tableDatosEmision.addCell(cellEmisionBanco);
        tableDatosEmision.addCell(cellEmisionBancoV);
        tableDatosEmision.addCell(cellEmisionClabe);
        tableDatosEmision.addCell(cellEmisionClabeV);
        tableDatosEmision.addCell(cellEmisionTipoCambio);
        tableDatosEmision.addCell(cellEmisionTipoCambioV);

        PdfPTable tableCodigoQR = new PdfPTable(1);
        tableCodigoQR.setWidths(new int[]{100});
        tableCodigoQR.setTotalWidth(100);
        tableCodigoQR.setLockedWidth(true);
        tableCodigoQR.setWidthPercentage(100);
        tableCodigoQR.getDefaultCell().setBorder(Rectangle.NO_BORDER);

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

        tableDatosEmisionQR.addCell(tableDatosEmision);
        tableDatosEmisionQR.addCell(tableCodigoQR);
        document.add(tableDatosEmisionQR);
    }

    /**
     * Metodo para agregar la firma del trabajador
     *
     * @param document
     * @return Tabla para agregar la firma del trabajador
     * @throws DocumentException
     */
    public void agregarFirmaEmpleado(Document document) throws DocumentException {
        PdfPTable tableFirmaEmpleado = new PdfPTable(1);
        tableFirmaEmpleado.setWidths(new int[]{560});
        tableFirmaEmpleado.setTotalWidth(560);
        tableFirmaEmpleado.setLockedWidth(true);
        tableFirmaEmpleado.setWidthPercentage(100);
        tableFirmaEmpleado.getDefaultCell().setBorder(Rectangle.NO_BORDER);

        PdfPCell cellEmpleadoLeyenda = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.MENSAJE2), fontEtiqueta));
        cellEmpleadoLeyenda.setHorizontalAlignment(Element.ALIGN_CENTER);
        cellEmpleadoLeyenda.setBorder(Rectangle.NO_BORDER);

        PdfPCell cellEmpleadoLinea = new PdfPCell(new Paragraph("___________________________________________________________", fontEtiqueta));
        cellEmpleadoLinea.setPaddingTop(10f);
        cellEmpleadoLinea.setHorizontalAlignment(Element.ALIGN_CENTER);
        cellEmpleadoLinea.setBorder(Rectangle.NO_BORDER);

        PdfPCell cellEmpleadoFirma = new PdfPCell(new Paragraph("Firma del trabajador", fontEtiqueta));
        cellEmpleadoFirma.setHorizontalAlignment(Element.ALIGN_CENTER);
        cellEmpleadoFirma.setBorder(Rectangle.NO_BORDER);

        tableFirmaEmpleado.addCell(cellEmpleadoLeyenda);
        tableFirmaEmpleado.addCell(cellEmpleadoLinea);
        tableFirmaEmpleado.addCell(cellEmpleadoFirma);

       document.add(tableFirmaEmpleado);
    }


    /**
     * Metodo para agregar a la celda un contorno de lado izquierdo | celda
     *
     * @param celda
     */
    public void dibujarContornoIzquierdo(PdfPCell celda) {
        celda.setBorderWidthBottom(0f);
        celda.setBorderWidthRight(0f);
        celda.setBorderWidthTop(0f);
        celda.setBorderWidthLeft(0.2f);
    }

    /**
     * Metodo para agregar a la celda un contorno de lado derecho celda |
     *
     * @param celda
     */
    public void dibujarContornoDerecho(PdfPCell celda) {
        celda.setBorderWidthBottom(0f);
        celda.setBorderWidthRight(0.2f);
        celda.setBorderWidthTop(0f);
        celda.setBorderWidthLeft(0f);
    }

    /**
     * Metodo para agregar a la celda un contorno debajo celda --------
     *
     * @param celda
     */
    public void dibujarContornoAbajo(PdfPCell celda) {
        celda.setBorderWidthBottom(0.2f);
        celda.setBorderWidthRight(0f);
        celda.setBorderWidthTop(0f);
        celda.setBorderWidthLeft(0f);
    }

    /**
     * Metodo para agregar a la celda un contorno debajo -------- celda
     *
     * @param celda
     */
    public void dibujarContornoArriba(PdfPCell celda) {
        celda.setBorderWidthBottom(0f);
        celda.setBorderWidthRight(0f);
        celda.setBorderWidthTop(0.2f);
        celda.setBorderWidthLeft(0f);
    }

    /**
     * Metodo para agregar a la celda un contorno debajo -------- | celda |
     *
     * @param celda
     */
    public void dibujarContornoEncabezadosArriba(PdfPCell celda) {
        celda.setBorderWidthBottom(0f);
        celda.setBorderWidthRight(0.2f);
        celda.setBorderWidthTop(0.2f);
        celda.setBorderWidthLeft(0.2f);
    }

    public void dibujarEsquinaDerechaIzquierda(PdfPCell celda) {
        celda.setBorderWidthBottom(0f);
        celda.setBorderWidthRight(0.2f);
        celda.setBorderWidthTop(0f);
        celda.setBorderWidthLeft(0.2f);
    }

    /**
     * Metodo para agregar a la celda un contorno debajo | celda | --------
     *
     * @param celda
     */
    public void dibujarContornoEncabezadosAbajo(PdfPCell celda) {
        celda.setBorderWidthBottom(0.2f);
        celda.setBorderWidthRight(0.2f);
        celda.setBorderWidthTop(0f);
        celda.setBorderWidthLeft(0.2f);
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
                pdfContentByte.setFontAndSize(font, 40);
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
                Font fontEtiqueta = new Font(FontFamily.HELVETICA, 5, Font.NORMAL, BaseColor.BLACK);
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
