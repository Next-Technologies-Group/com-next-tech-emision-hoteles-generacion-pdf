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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

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
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfTemplate;
import com.itextpdf.text.pdf.PdfWriter;
import com.cfdi40.exceptionhandlerstarter.exception.BusinessException;
import com.cfdi40.pdfgen.dto.CatImpuestos;
import com.cfdi40.pdfgen.model.entity.CatIdiomapdf;
import com.cfdi40.pdfgen.model.entity.CatTagxidioma;
import com.cfdi40.pdfgen.model.entity.IdentificadorSucursal;
import com.cfdi40.pdfgen.model.repository.CatIdiomapdfRepository;
import com.cfdi40.pdfgen.model.repository.CatTagxidiomaRepository;
import com.cfdi40.pdfgen.util.ConstantesFacto;
import com.cfdi40.pdfgen.util.EnumTagPlantilla;
import com.cfdi40.pdfgen.util.Utilities;
import com.cfdi40.pdfgen.tenants.tenanta.cfdi.dto.CFDI;
import com.cfdi40.pdfgen.tenants.tenanta.cfdi.dto.CFDI.Conceptos;
import com.cfdi40.pdfgen.tenants.tenanta.cfdi.dto.CFDI.TenantAData;
import com.cfdi40.pdfgen.tenants.tenanta.cfdi.dto.CFDI.Relacionados;
import com.cfdi40.pdfgen.tenants.tenanta.cfdi.dto.CFDI.Complementos.ComplementoPago20;
import com.cfdi40.pdfgen.tenants.tenanta.cfdi.dto.CFDI.Complementos.ComplementoPago20.Pago;
import com.cfdi40.pdfgen.tenants.tenanta.cfdi.dto.CFDI.Complementos.ComplementoPago20.Pago.DoctoRelacionado;
import com.cfdi40.pdfgen.tenants.tenanta.cfdi.dto.CFDI.Complementos.ComplementoPago20.Pago.DoctoRelacionado.ImpuestosDR.RetencionesDR.RetencionDR;
import com.cfdi40.pdfgen.tenants.tenanta.cfdi.dto.CFDI.Complementos.ComplementoPago20.Pago.DoctoRelacionado.ImpuestosDR.TrasladosDR.TrasladoDR;
import com.cfdi40.pdfgen.tenants.tenanta.cfdi.dto.CFDI.Complementos.ComplementoPago20.Pago.ImpuestosP.RetencionesP.RetencionP;
import com.cfdi40.pdfgen.tenants.tenanta.cfdi.dto.CFDI.Complementos.ComplementoPago20.Pago.ImpuestosP.TrasladosP.TrasladoP;
import com.cfdi40.pdfgen.tenants.tenanta.cfdi.dto.CFDI.Conceptos.CuentaPredial;
import com.cfdi40.pdfgen.tenants.tenanta.cfdi.dto.CFDI.Conceptos.InformacionAduanera;
import com.cfdi40.pdfgen.tenants.tenanta.cfdi.dto.CFDI.Impuestos.Retencion;
import com.cfdi40.pdfgen.tenants.tenanta.cfdi.dto.CFDI.Impuestos.Traslado;
import com.cfdi40.pdfgen.tenants.tenanta.cfdi.dto.CFDI.ImpuestosLocales.TrasladoLocal;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class PdfFacturaTenantCServiceImpl extends PdfPageEventHelper implements PdfFacturaTenantCService {

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
    
    Font fontLeyendas = new Font(Font.FontFamily.HELVETICA, 7, Font.NORMAL, BaseColor.BLACK);
    Font fontContenido = new Font(Font.FontFamily.HELVETICA, 8, Font.NORMAL, BaseColor.BLACK);
    Font fontEtiquetas = new Font(Font.FontFamily.HELVETICA, 8, Font.BOLDITALIC, BaseColor.BLACK);
    Font fontEncabezados = new Font(Font.FontFamily.HELVETICA, 8, Font.BOLDITALIC, BaseColor.WHITE);
    private HashMap<EnumTagPlantilla, String> hmapTagIdioma;
    private List<CatTagxidioma> catTagxidioma;
    private static final int ANCHO_TOTAL_TABLA = 560;
    private static final int PORCENTAJE_ANCHO_TABLA = 100;
    private static final float ALTURA_FIJA_TABLA = 80;
    private static final float ALTURA_FIJA_TABLA_COMPLEMENTO_PAGO = 1.5f;

    @Override
    public ByteArrayOutputStream generarPDfFactura(CFDI facturaDto, IdentificadorSucursal identificadorSucursal) throws BusinessException {
        try {
            ByteArrayOutputStream pdfResponse = null;

            TenantAData datosTenantAData = facturaDto.getTenantAData() != null ? facturaDto.getTenantAData() : new TenantAData();

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

            agregarDatosEmisor(document,identificadorSucursal,facturaDto);
            agregarDatosSucursal(document,datosTenantAData, facturaDto);
            agregarTableInformacionGlobal(document,facturaDto);
            agregarTableDatosCfdiRelacionados(document,facturaDto);
            agregarConceptos(document,facturaDto);
            agregarSubtotales(document, facturaDto);
            agregarComplementoPagos(document,facturaDto);
            agregarSellos(document, facturaDto);
            agregarDatosFinales(document, facturaDto);
            agregarCodigoQR(document,facturaDto);
            agregarLeyenda(document);

            document.close();
            writer.flush();
            return pdfResponse;
        } catch (Exception e) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, " Ocurrio un error al tratar de generar el PDF de la plantilla TenantC de la factura con UUID "
                    + (facturaDto.getTimbrado() != null ? facturaDto.getTimbrado().getUuid() : "") + " Error: " + e.getStackTrace()[0].getLineNumber());
        }
    }

    /**
     * Metodo para agregar los datos del Emisor.
     *
     * @param document
     * @return Tabla Datos Emisor
     * @throws DocumentException
     */
    public void agregarDatosEmisor(Document document, IdentificadorSucursal identificadorSucursal, CFDI facturaDto) throws DocumentException {
        PdfPTable tableDatosEmisor = new PdfPTable(2);
        tableDatosEmisor.setWidths(new int[]{80, 270});
        tableDatosEmisor.setTotalWidth(ANCHO_TOTAL_TABLA);
        tableDatosEmisor.setLockedWidth(true);
        tableDatosEmisor.setWidthPercentage(PORCENTAJE_ANCHO_TABLA);
        tableDatosEmisor.getDefaultCell().setBorder(Rectangle.NO_BORDER);

        PdfPTable tableLogo = new PdfPTable(1);
        tableLogo.setWidths(new int[]{80});
        tableLogo.setTotalWidth(ALTURA_FIJA_TABLA);
        tableLogo.setLockedWidth(true);
        tableLogo.setWidthPercentage(PORCENTAJE_ANCHO_TABLA);

        try {
            PdfPCell celdaLogo = new PdfPCell();
            byte[] bytesLogo = getLogo(identificadorSucursal.getEmisorId().getRfc(),identificadorSucursal.getSucursalId().getNombreLogo());

            if (bytesLogo!=null) {
                Image logo = Image.getInstance(bytesLogo);
                logo.scaleToFit(75,80);
                celdaLogo.addElement(logo);
                celdaLogo.setHorizontalAlignment(Element.ALIGN_CENTER);
                celdaLogo.setBorder(Rectangle.NO_BORDER);
                tableLogo.addCell(celdaLogo);
            }
        } catch (Exception e) {
            PdfPCell celdaLogo = new PdfPCell();
            celdaLogo.setBorder(Rectangle.NO_BORDER);
            tableLogo.addCell(celdaLogo);
        }

        PdfPTable datosEmisor = new PdfPTable(2);
        datosEmisor.setWidths(new int[]{60, 130});
        datosEmisor.setTotalWidth(435);
        datosEmisor.setLockedWidth(true);
        datosEmisor.setWidthPercentage(PORCENTAJE_ANCHO_TABLA);
        datosEmisor.getDefaultCell().setBorder(Rectangle.NO_BORDER);

        PdfPCell encabezadosIdFactura = new PdfPCell(new Paragraph((facturaDto.getTenantAData() != null && facturaDto.getTenantAData().getTrxType() != null ? facturaDto.getTenantAData().getTrxType() : "").concat(facturaDto.getFolio() != null ? facturaDto.getFolio() : ""), fontEncabezados));
        encabezadosIdFactura.setHorizontalAlignment(Element.ALIGN_RIGHT);
        encabezadosIdFactura.setColspan(2);
        encabezadosIdFactura.setFixedHeight(20f);
        encabezadosIdFactura.setBackgroundColor(BaseColor.BLACK);
        encabezadosIdFactura.setBorder(Rectangle.NO_BORDER);

        PdfPCell nombreEmisor = new PdfPCell(new Paragraph(facturaDto.getEmisor().getNombre() != null ? facturaDto.getEmisor().getNombre() : "", fontEtiquetas));
        nombreEmisor.setHorizontalAlignment(Element.ALIGN_LEFT);
        nombreEmisor.setColspan(2);
        nombreEmisor.setBorder(Rectangle.NO_BORDER);

        PdfPCell rfcEmisorC = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.RFC).concat(" : ").concat(facturaDto.getEmisor().getRfc() != null ? facturaDto.getEmisor().getRfc() : ""), fontEtiquetas));
        rfcEmisorC.setHorizontalAlignment(Element.ALIGN_LEFT);
        rfcEmisorC.setBorder(Rectangle.NO_BORDER);

        PdfPCell rfcEmisorV = new PdfPCell();
        rfcEmisorV.setHorizontalAlignment(Element.ALIGN_LEFT);
        rfcEmisorV.setBorder(Rectangle.NO_BORDER);

        PdfPCell paisEmisor = new PdfPCell(new Paragraph(facturaDto.getEmisor().getPais() != null ? facturaDto.getEmisor().getPais() : "", fontContenido));
        paisEmisor.setHorizontalAlignment(Element.ALIGN_LEFT);
        paisEmisor.setColspan(2);
        paisEmisor.setPaddingBottom(10f);
        paisEmisor.setBorder(Rectangle.NO_BORDER);

        PdfPCell regimenEmisorC = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.N_REGIMENFISCAL).concat(" : "), fontEtiquetas));
        regimenEmisorC.setHorizontalAlignment(Element.ALIGN_LEFT);
        regimenEmisorC.setBorder(Rectangle.NO_BORDER);

        PdfPCell regimenEmisorV = new PdfPCell(new Paragraph(facturaDto.getEmisor().getRegimenFiscal() != null ? facturaDto.getEmisor().getRegimenFiscal() : "", fontContenido));
        regimenEmisorV.setHorizontalAlignment(Element.ALIGN_LEFT);
        regimenEmisorV.setBorder(Rectangle.NO_BORDER);

        PdfPCell cellEmisionLugarFecha = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.N_LUGARFECHAEMISION).concat(" : "), fontEtiquetas));
        cellEmisionLugarFecha.setHorizontalAlignment(Element.ALIGN_LEFT);
        cellEmisionLugarFecha.setBorder(Rectangle.NO_BORDER);

        String lugarFechaEmision = "";
        if (facturaDto.getFecha() != null) {
            Date dateCheckIn = facturaDto.getFecha();
            DateFormat dateFormatCheckIn = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            lugarFechaEmision = dateFormatCheckIn.format(dateCheckIn);
        }
        PdfPCell cellEmisionLugarFechaV = new PdfPCell(new Paragraph((facturaDto.getLugarExpedicion() != null ? facturaDto.getLugarExpedicion() : "").concat(", ").concat(" a ".concat(lugarFechaEmision)), fontContenido));
        cellEmisionLugarFechaV.setHorizontalAlignment(Element.ALIGN_LEFT);
        cellEmisionLugarFechaV.setBorder(Rectangle.NO_BORDER);

        PdfPCell monedaC = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.N_MONEDA).concat(" : "), fontEtiquetas));
        monedaC.setHorizontalAlignment(Element.ALIGN_LEFT);
        monedaC.setBorder(Rectangle.NO_BORDER);

        PdfPCell monedaV = new PdfPCell(new Paragraph(facturaDto.getMoneda() != null ? facturaDto.getMoneda() : "", fontContenido));
        monedaV.setHorizontalAlignment(Element.ALIGN_LEFT);
        monedaV.setBorder(Rectangle.NO_BORDER);

        datosEmisor.addCell(encabezadosIdFactura);
        if (facturaDto.getTipoCfdi().equals(CFDI.EnumTipoFactura.NOTA_CREDITO)) {
            PdfPCell cellFactura = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.NOTA_CREDITO), fontEtiquetas));
            cellFactura.setHorizontalAlignment(Element.ALIGN_LEFT);
            cellFactura.setColspan(2);
            cellFactura.setBorder(Rectangle.NO_BORDER);
            datosEmisor.addCell(cellFactura);
        } else if (facturaDto.getTipoCfdi().equals(CFDI.EnumTipoFactura.FACTURA)) {
            PdfPCell cellFactura = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.FACTURA), fontEtiquetas));
            cellFactura.setHorizontalAlignment(Element.ALIGN_LEFT);
            cellFactura.setColspan(2);
            cellFactura.setBorder(Rectangle.NO_BORDER);
            datosEmisor.addCell(cellFactura);
        } else if (facturaDto.getTipoCfdi().equals(CFDI.EnumTipoFactura.PAGO)) {
            PdfPCell cellFactura = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.C_COMPLEMENTO_RECEPCION_PAGO), fontEtiquetas));
            cellFactura.setHorizontalAlignment(Element.ALIGN_LEFT);
            cellFactura.setColspan(2);
            cellFactura.setBorder(Rectangle.NO_BORDER);
            datosEmisor.addCell(cellFactura);
        }

        datosEmisor.addCell(nombreEmisor);
        datosEmisor.addCell(rfcEmisorC);
        datosEmisor.addCell(rfcEmisorV);
        datosEmisor.addCell(regimenEmisorC);
        datosEmisor.addCell(regimenEmisorV);
        datosEmisor.addCell(cellEmisionLugarFecha);
        datosEmisor.addCell(cellEmisionLugarFechaV);
        datosEmisor.addCell(monedaC);
        datosEmisor.addCell(monedaV);
        if (facturaDto.getTipoCambio() != null) {
            PdfPCell tipoCambioC = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.C_TIPOCAMBIO).concat(" : "), fontEtiquetas));
            tipoCambioC.setHorizontalAlignment(Element.ALIGN_LEFT);
            tipoCambioC.setBorder(Rectangle.NO_BORDER);

            PdfPCell tipoCambioV = new PdfPCell(new Paragraph(String.valueOf(facturaDto.getTipoCambio() != null ? facturaDto.getTipoCambio() : ""), fontContenido));
            tipoCambioV.setHorizontalAlignment(Element.ALIGN_LEFT);
            tipoCambioV.setBorder(Rectangle.NO_BORDER);
            datosEmisor.addCell(tipoCambioC);
            datosEmisor.addCell(tipoCambioV);

        }
        tableDatosEmisor.addCell(tableLogo);
        tableDatosEmisor.addCell(datosEmisor);

        document.add(tableDatosEmisor);
    }

    /**
     * Metodo para agregar los datos de la sucursal.
     *
     * @param document
     * @return Tabla DatosSucursal
     * @throws DocumentException
     */
    public void agregarDatosSucursal(Document document, TenantAData  datosTenantAData, CFDI facturaDto) throws DocumentException {
        PdfPTable tableDatosSucursal = new PdfPTable(4);
        tableDatosSucursal.setWidths(new int[]{50, 130, 50, 50});
        tableDatosSucursal.setTotalWidth(ANCHO_TOTAL_TABLA);
        tableDatosSucursal.setLockedWidth(true);
        tableDatosSucursal.setWidthPercentage(PORCENTAJE_ANCHO_TABLA);
        tableDatosSucursal.getDefaultCell().setBorder(Rectangle.NO_BORDER);

        PdfPCell encabezadoCliente = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.C_CLIENTE), fontEncabezados));
        encabezadoCliente.setHorizontalAlignment(Element.ALIGN_LEFT);
        encabezadoCliente.setColspan(4);
        encabezadoCliente.setFixedHeight(20f);
        encabezadoCliente.setBackgroundColor(BaseColor.BLACK);
        encabezadoCliente.setBorder(Rectangle.NO_BORDER);

        PdfPCell nombreCliente = new PdfPCell(new Paragraph(facturaDto.getReceptor() != null && facturaDto.getReceptor().getNombre() != null ? facturaDto.getReceptor().getNombre() : "", fontEtiquetas));
        nombreCliente.setColspan(2);
        nombreCliente.setHorizontalAlignment(Element.ALIGN_LEFT);
        nombreCliente.setBorder(Rectangle.NO_BORDER);

        PdfPCell rfcCliente = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.RFC).concat(" : ").concat(facturaDto.getReceptor() != null && facturaDto.getReceptor().getRfc() != null ? facturaDto.getReceptor().getRfc() : ""), fontEtiquetas));
        rfcCliente.setColspan(2);
        rfcCliente.setHorizontalAlignment(Element.ALIGN_LEFT);
        rfcCliente.setBorder(Rectangle.NO_BORDER);

        PdfPCell referenciaClienteC = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.MIC_REFERENCIA).concat(" : "), fontEtiquetas));
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

        // PdfPCell numOrdenFacturacionClienteC = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.C_NOORDENFACTURACION), fontEtiquetas));
        // numOrdenFacturacionClienteC.setHorizontalAlignment(Element.ALIGN_LEFT);
        // numOrdenFacturacionClienteC.setBorder(Rectangle.NO_BORDER);

        // PdfPCell numOrdenFacturacionClientev = new PdfPCell(new Paragraph(datosTenantAData != null && datosTenantAData.getNumeroOrdenFacturacion() != null ? datosTenantAData.getNumeroOrdenFacturacion() : "", fontContenido));
        // numOrdenFacturacionClientev.setHorizontalAlignment(Element.ALIGN_LEFT);
        // numOrdenFacturacionClientev.setBorder(Rectangle.NO_BORDER);

        PdfPCell paisReceptorC = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.DC_PAIS).concat(" : ").concat(facturaDto.getReceptor() != null && facturaDto.getReceptor().getPais() != null ? facturaDto.getReceptor().getPais() : ""), fontEtiquetas));
        paisReceptorC.setColspan(2);    
        paisReceptorC.setHorizontalAlignment(Element.ALIGN_LEFT);
        paisReceptorC.setBorder(Rectangle.NO_BORDER);

        // PdfPCell paisReceptor = new PdfPCell(new Paragraph(facturaDto.getReceptor() != null && facturaDto.getReceptor().getPais() != null ? facturaDto.getReceptor().getPais() : "", fontContenido));
        // paisReceptor.setHorizontalAlignment(Element.ALIGN_LEFT);
        // paisReceptor.setBorder(Rectangle.NO_BORDER);

        PdfPCell usoCfdiReceptorL = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.G_USOCFDI).concat(" : ").concat(facturaDto.getReceptor() != null && facturaDto.getReceptor().getUsoCFDI() != null ? facturaDto.getReceptor().getUsoCFDI() : ""), fontEtiquetas));
        usoCfdiReceptorL.setColspan(2);
        usoCfdiReceptorL.setHorizontalAlignment(Element.ALIGN_LEFT);
        usoCfdiReceptorL.setBorder(Rectangle.NO_BORDER);

        PdfPCell numRegTributarioC = new PdfPCell(new Paragraph(facturaDto.getReceptor() != null && facturaDto.getReceptor().getNumRegIdTrib() != null ? hmapTagIdioma.get(EnumTagPlantilla.REG_FISCAL_RECEP).concat(" : ") : "", fontEtiquetas));
        numRegTributarioC.setColspan(2);
        numRegTributarioC.setHorizontalAlignment(Element.ALIGN_LEFT);
        numRegTributarioC.setBorder(Rectangle.NO_BORDER);
        
        PdfPCell residenciaFiscal = new PdfPCell(new Paragraph((hmapTagIdioma.get(EnumTagPlantilla.REG_FISCAL_RECEP)).concat(" : ").concat(facturaDto.getReceptor().getRegimenFiscal() != null ? facturaDto.getReceptor().getRegimenFiscal() : ""), fontEtiquetas));
        residenciaFiscal.setColspan(2);        
        residenciaFiscal.setHorizontalAlignment(Element.ALIGN_LEFT);
        residenciaFiscal.setBorder(Rectangle.NO_BORDER);

        PdfPCell domFiscR = new PdfPCell(new Paragraph(facturaDto.getReceptor() != null && facturaDto.getReceptor().getDomicilioFiscal() != null ? hmapTagIdioma.get(EnumTagPlantilla.R_DOMFISCRECEP).concat(" : ") : "", fontEtiquetas));
        domFiscR.setHorizontalAlignment(Element.ALIGN_LEFT);
        domFiscR.setBorder(Rectangle.NO_BORDER);

        PdfPCell domFiscRV = new PdfPCell(new Paragraph(facturaDto.getReceptor() != null && facturaDto.getReceptor().getDomicilioFiscal() != null ? facturaDto.getReceptor().getDomicilioFiscal() : "", fontContenido));
        domFiscRV.setHorizontalAlignment(Element.ALIGN_LEFT);
        domFiscRV.setBorder(Rectangle.NO_BORDER);
           
        tableDatosSucursal.addCell(encabezadoCliente);
        tableDatosSucursal.addCell(nombreCliente);
        tableDatosSucursal.addCell(referenciaClienteC);
        tableDatosSucursal.addCell(referenciaClienteV);
        tableDatosSucursal.addCell(rfcCliente);
        tableDatosSucursal.addCell(numClienteC);
        tableDatosSucursal.addCell(numClienteV);
        tableDatosSucursal.addCell(usoCfdiReceptorL);
        // tableDatosSucursal.addCell(numOrdenFacturacionClienteC);
        // tableDatosSucursal.addCell(numOrdenFacturacionClientev);
        tableDatosSucursal.addCell(paisReceptorC);
        tableDatosSucursal.addCell(residenciaFiscal);
        tableDatosSucursal.addCell(numRegTributarioC);
        tableDatosSucursal.addCell(domFiscR);
        tableDatosSucursal.addCell(domFiscRV);
        PdfPCell cellVacia = new PdfPCell();
        cellVacia.setColspan(2);
        cellVacia.setBorder(Rectangle.NO_BORDER);
        tableDatosSucursal.addCell(cellVacia);

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
        tableConceptos.setTotalWidth(ANCHO_TOTAL_TABLA);
        tableConceptos.setLockedWidth(true);
        tableConceptos.setWidthPercentage(PORCENTAJE_ANCHO_TABLA);
        tableConceptos.getDefaultCell().setBorder(Rectangle.NO_BORDER);

        PdfPCell encabezadoConceptos = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.C_PRODUCTOS_SERVICIO), fontEncabezados));
        encabezadoConceptos.setHorizontalAlignment(Element.ALIGN_LEFT);
        encabezadoConceptos.setColspan(6);
        encabezadoConceptos.setFixedHeight(20f);
        encabezadoConceptos.setBackgroundColor(BaseColor.BLACK);
        encabezadoConceptos.setBorder(Rectangle.NO_BORDER);

        PdfPCell cantidadConceptosC = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.P_CANTIDAD), fontEtiquetas));
        cantidadConceptosC.setHorizontalAlignment(Element.ALIGN_LEFT);
        cantidadConceptosC.setBorder(Rectangle.NO_BORDER);

        PdfPCell claveConceptoC = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.P_CLAVEPRODUCTO), fontEtiquetas));
        claveConceptoC.setHorizontalAlignment(Element.ALIGN_LEFT);
        claveConceptoC.setBorder(Rectangle.NO_BORDER);

        PdfPCell conceptoConceptoC = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.N_CONCEPTO), fontEtiquetas));
        conceptoConceptoC.setHorizontalAlignment(Element.ALIGN_LEFT);
        conceptoConceptoC.setBorder(Rectangle.NO_BORDER);

        PdfPCell unidadConceptoC = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.P_CLAVEUNIDAD), fontEtiquetas));
        unidadConceptoC.setHorizontalAlignment(Element.ALIGN_LEFT);
        unidadConceptoC.setBorder(Rectangle.NO_BORDER);

        PdfPCell precioUniConceptoC = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.P_PRECIOUNITARIO), fontEtiquetas));
        precioUniConceptoC.setHorizontalAlignment(Element.ALIGN_CENTER);
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

            PdfPCell celdaCantidad = new PdfPCell(new Paragraph(new Phrase(conceptos.getCantidad().toString(), fontContenido)));
            celdaCantidad.setHorizontalAlignment(Element.ALIGN_LEFT);
            celdaCantidad.setBorder(Rectangle.NO_BORDER);

            PdfPCell celdaClaveProducto = new PdfPCell(new Paragraph(new Phrase(conceptos.getClaveProdServ(), fontContenido)));
            celdaClaveProducto.setHorizontalAlignment(Element.ALIGN_LEFT);
            celdaClaveProducto.setBorder(Rectangle.NO_BORDER);

            PdfPCell celdaDescripcion = new PdfPCell(new Paragraph(new Phrase(conceptos.getDescripcion().concat(descConcepto.toString()), fontContenido)));
            celdaDescripcion.setHorizontalAlignment(Element.ALIGN_LEFT);
            celdaDescripcion.setBorder(Rectangle.NO_BORDER);

            PdfPCell celdaUnidad = new PdfPCell(new Paragraph(new Phrase(conceptos.getClaveUnidad(), fontContenido)));
            celdaUnidad.setHorizontalAlignment(Element.ALIGN_LEFT);
            celdaUnidad.setBorder(Rectangle.NO_BORDER);

            PdfPCell celdaPUnitario = new PdfPCell(new Paragraph(new Phrase(Utilities.bigDecimalToStr(conceptos.getValorUnitario()), fontContenido)));
            celdaPUnitario.setHorizontalAlignment(Element.ALIGN_RIGHT);
            celdaPUnitario.setBorder(Rectangle.NO_BORDER);

            PdfPCell celdaImporte = new PdfPCell(new Paragraph(new Phrase(Utilities.bigDecimalToStr(conceptos.getImporte()), fontContenido))
            );
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
                descConcepto.append("Impuesto Trasladado:  ").append((obtenerImpuestoCatalogo.getDescripcion() != null ? obtenerImpuestoCatalogo.getDescripcion() : ""))
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
            for (Conceptos.Retenciones impuestosCargados : conceptos.getImpuestos().getRetenciones()) {
                descConcepto.append("\n");
                CatImpuestos obtenerImpuestoCatalogo = catalogosSatService.obtenerCatImpuestoSatByClave(impuestosCargados.getImpuesto());
                descConcepto.append("Impuesto Retenido:  ").append((obtenerImpuestoCatalogo.getDescripcion() != null ? obtenerImpuestoCatalogo.getDescripcion() : ""))
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
     * Metodo para agregar los subtotales
     *
     * @param document
     * @return Tabla Subtotales
     * @throws DocumentException
     */
    public void agregarSubtotales(Document document, CFDI facturaDto) throws DocumentException {
        PdfPTable tableConceptos = new PdfPTable(3);
        tableConceptos.setWidths(new int[]{140, 100, 20});
        tableConceptos.setTotalWidth(560);
        tableConceptos.setLockedWidth(true);
        tableConceptos.setWidthPercentage(100);
        tableConceptos.getDefaultCell().setFixedHeight(80f);
        tableConceptos.getDefaultCell().setBorder(Rectangle.NO_BORDER);

        PdfPCell encabezadosIdFactura = new PdfPCell();
        encabezadosIdFactura.setHorizontalAlignment(Element.ALIGN_RIGHT);
        encabezadosIdFactura.setColspan(3);;
        encabezadosIdFactura.setFixedHeight(15f);
        encabezadosIdFactura.setBackgroundColor(BaseColor.BLACK);
        encabezadosIdFactura.setBorder(Rectangle.NO_BORDER);
        tableConceptos.addCell(encabezadosIdFactura);

        PdfPCell subtotalC = new PdfPCell(new Phrase(hmapTagIdioma.get(EnumTagPlantilla.T_SUBTOTAL), fontEtiquetas));
        subtotalC.setColspan(2);
        subtotalC.setHorizontalAlignment(Element.ALIGN_RIGHT);
        subtotalC.setBorder(Rectangle.NO_BORDER);
        tableConceptos.addCell(subtotalC);

        PdfPCell subtotalV = new PdfPCell(new Phrase(String.valueOf(facturaDto.getSubTotal() != null ? facturaDto.getSubTotal() : ""), fontContenido));
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
                    sImpuesto = impuestoSat.getDescripcion() + "(" + impuestosTrasladados.getTasaoCuota().doubleValue() + ")";

                    PdfPCell celdaImpuestoT = new PdfPCell(new Paragraph(new Phrase(sImpuesto, fontEtiquetas)));
                    celdaImpuestoT.setColspan(2);
                    celdaImpuestoT.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    celdaImpuestoT.setBorder(Rectangle.NO_BORDER);

                    PdfPCell celdaImpuesto = new PdfPCell(new Paragraph(new Phrase(String.valueOf(impuestosTrasladados.getImporte() != null ? impuestosTrasladados.getImporte() : ""), fontContenido)));
                    celdaImpuesto.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    celdaImpuesto.setBorder(Rectangle.NO_BORDER);

                    tableConceptos.addCell(celdaImpuestoT);
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
                    celdaImpuestoR.setColspan(2);
                    celdaImpuestoR.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    celdaImpuestoR.setBorder(Rectangle.NO_BORDER);

                    PdfPCell celdaImpuesto = new PdfPCell(new Paragraph(new Phrase(String.valueOf(impuestosRetenidos.getImporte() != null ? impuestosRetenidos.getImporte() : ""), fontContenido)));
                    celdaImpuesto.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    celdaImpuesto.setBorder(Rectangle.NO_BORDER);

                    tableConceptos.addCell(celdaImpuestoR);
                    tableConceptos.addCell(celdaImpuesto);
                }
            }
        }

        String sImpuestoLocales = "";
        if (facturaDto.getImpuestosLocales() != null && facturaDto.getImpuestosLocales().getTrasladoLocales() != null && !facturaDto.getImpuestosLocales().getTrasladoLocales().isEmpty()) {
            for (TrasladoLocal imp : facturaDto.getImpuestosLocales().getTrasladoLocales()) {
                sImpuestoLocales = imp.getImpuesto() + "(" + imp.getTasa().doubleValue() + ")";

                PdfPCell celdaImpuestoT = new PdfPCell(new Paragraph(new Phrase(sImpuestoLocales, fontContenido)));
                celdaImpuestoT.setColspan(2);
                celdaImpuestoT.setHorizontalAlignment(Element.ALIGN_RIGHT);
                celdaImpuestoT.setBorder(Rectangle.NO_BORDER);

                PdfPCell celdaImpuesto = new PdfPCell(new Paragraph(new Phrase(String.valueOf(imp.getImporte() != null ? imp.getImporte() : ""), fontContenido)));
                celdaImpuesto.setHorizontalAlignment(Element.ALIGN_RIGHT);
                celdaImpuesto.setBorder(Rectangle.NO_BORDER);

                tableConceptos.addCell(celdaImpuestoT);
                tableConceptos.addCell(celdaImpuesto);
            }
        }

        if (facturaDto.getTotalPagar() != null) {
            PdfPCell totalC = new PdfPCell(new Phrase(hmapTagIdioma.get(EnumTagPlantilla.C_TOTAL), fontEtiquetas));
            totalC.setHorizontalAlignment(Element.ALIGN_RIGHT);
            totalC.setColspan(2);
            totalC.setBorder(Rectangle.NO_BORDER);
            tableConceptos.addCell(totalC);

            PdfPCell totalV = new PdfPCell(new Phrase(String.valueOf(facturaDto.getTotal()), fontContenido));
            totalV.setHorizontalAlignment(Element.ALIGN_RIGHT);
            totalV.setBorder(Rectangle.NO_BORDER);
            tableConceptos.addCell(totalV);

            PdfPCell totalLetraC = new PdfPCell(new Phrase(hmapTagIdioma.get(EnumTagPlantilla.N_IMPORTELETRA).concat(" : ").concat(Utilities.crearTotalLetra(facturaDto)), fontContenido));
            totalLetraC.setHorizontalAlignment(Element.ALIGN_LEFT);
            totalLetraC.setBorder(Rectangle.NO_BORDER);
            tableConceptos.addCell(totalLetraC);

            PdfPCell totalPagarC = new PdfPCell(new Phrase(hmapTagIdioma.get(EnumTagPlantilla.T_TOTALPAGAR), fontEtiquetas));
            totalPagarC.setHorizontalAlignment(Element.ALIGN_RIGHT);
            totalPagarC.setBorder(Rectangle.NO_BORDER);
            tableConceptos.addCell(totalPagarC);

            PdfPCell totalPagarV = new PdfPCell(new Phrase(String.valueOf(facturaDto.getTotalPagar()), fontContenido));
            totalPagarV.setHorizontalAlignment(Element.ALIGN_RIGHT);
            totalPagarV.setBorder(Rectangle.NO_BORDER);
            tableConceptos.addCell(totalPagarV);
        } else if (facturaDto.getTotal() != null) {
            PdfPCell totalLetraC = new PdfPCell(new Phrase(hmapTagIdioma.get(EnumTagPlantilla.N_IMPORTELETRA).concat(" : ").concat(Utilities.crearTotalLetra(facturaDto)), fontContenido));
            totalLetraC.setHorizontalAlignment(Element.ALIGN_LEFT);
            totalLetraC.setBorder(Rectangle.NO_BORDER);
            tableConceptos.addCell(totalLetraC);

            PdfPCell totalPagarC = new PdfPCell(new Phrase(hmapTagIdioma.get(EnumTagPlantilla.C_TOTAL), fontEtiquetas));
            totalPagarC.setHorizontalAlignment(Element.ALIGN_RIGHT);
            totalPagarC.setBorder(Rectangle.NO_BORDER);
            tableConceptos.addCell(totalPagarC);

            PdfPCell totalPagarV = new PdfPCell(new Phrase(String.valueOf(facturaDto.getTotal()), fontContenido));
            totalPagarV.setHorizontalAlignment(Element.ALIGN_RIGHT);
            totalPagarV.setBorder(Rectangle.NO_BORDER);
            tableConceptos.addCell(totalPagarV);
        }

        PdfPCell finTableSeparator = new PdfPCell(new Phrase("", fontEtiquetas));
        finTableSeparator.setHorizontalAlignment(Element.ALIGN_RIGHT);
        finTableSeparator.setColspan(3);
        finTableSeparator.setBackgroundColor(BaseColor.BLACK);
        finTableSeparator.setBorder(Rectangle.NO_BORDER);
        tableConceptos.addCell(finTableSeparator);

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
            spaceComplementoPagos.setFixedHeight(ALTURA_FIJA_TABLA_COMPLEMENTO_PAGO);
            spaceComplementoPagos.setBorder(Rectangle.NO_BORDER);

            tableDocumentoRelacionado.addCell(spaceComplementoPagos);
            if (facturaDto.getComplementos().getComplementoPago20().getPago() != null && !facturaDto.getComplementos().getComplementoPago20().getPago().isEmpty()) {

                for (Pago complementoPagoCargados : facturaDto.getComplementos().getComplementoPago20().getPago()) {

                    PdfPCell headerNodoPago = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.C_PAGO).concat(" : "), fontEtiquetas));
                    headerNodoPago.setHorizontalAlignment(Element.ALIGN_LEFT);
                    headerNodoPago.setBorder(Rectangle.NO_BORDER);
                    tableDocumentoRelacionado.addCell(headerNodoPago);

                    PdfPTable tablePago = new PdfPTable(3);
                    tablePago.setWidths(new int[]{80, 30, 30});
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

                    PdfPCell fechaPagoC = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.C_FECHAPAGO).concat(" : ").concat(fechaPago), fontContenido));
                    fechaPagoC.setHorizontalAlignment(Element.ALIGN_LEFT);
                    fechaPagoC.setBorder(Rectangle.NO_BORDER);

                    PdfPCell formaDePago = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.T_FORMAPAGO).concat(" : ").concat(complementoPagoCargados.getFormaDePagoP() != null ? complementoPagoCargados.getFormaDePagoP() : ""), fontContenido));
                    formaDePago.setHorizontalAlignment(Element.ALIGN_LEFT);
                    formaDePago.setBorder(Rectangle.NO_BORDER);

                    PdfPCell monedaPago = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.N_MONEDA).concat(" : ").concat(complementoPagoCargados.getMonedaP() != null ? complementoPagoCargados.getMonedaP() : ""), fontContenido));
                    monedaPago.setHorizontalAlignment(Element.ALIGN_LEFT);
                    monedaPago.setBorder(Rectangle.NO_BORDER);

                    PdfPCell tipoCambioPago = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.C_TIPOCAMBIO).concat(" : ").concat(String.valueOf(complementoPagoCargados.getTipoCambioP() != null ? complementoPagoCargados.getTipoCambioP() : "")), fontContenido));
                    tipoCambioPago.setHorizontalAlignment(Element.ALIGN_LEFT);
                    tipoCambioPago.setBorder(Rectangle.NO_BORDER);

                    PdfPCell montoPago = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.C_MONTO).concat(" : ").concat(String.valueOf(complementoPagoCargados.getMonto() != null ? complementoPagoCargados.getMonto() : "")), fontContenido));
                    montoPago.setHorizontalAlignment(Element.ALIGN_LEFT);
                    montoPago.setBorder(Rectangle.NO_BORDER);

                    PdfPCell vacia = new PdfPCell();
                    vacia.setHorizontalAlignment(Element.ALIGN_LEFT);
                    vacia.setBorder(Rectangle.NO_BORDER);

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
                    tablePago.addCell(vacia);
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

                            PdfPCell tipoDeCambioDocumentoRelacionadoC = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.EQUIVALENCIA_DR).concat(" : ").concat(String.valueOf(documentosRelacionCargados.getEquivalenciaDR() != null ? documentosRelacionCargados.getEquivalenciaDR() : "")), fontContenido));
                            tipoDeCambioDocumentoRelacionadoC.setHorizontalAlignment(Element.ALIGN_LEFT);
                            tipoDeCambioDocumentoRelacionadoC.setBorder(Rectangle.NO_BORDER);

                            // PdfPCell metodoPagoDocumentoRelacionadoC = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.T_METODOPAGO).concat(" : ").concat(documentosRelacionCargados.getMetodoDePagoDR() != null ? documentosRelacionCargados.getMetodoDePagoDR() : ""), fontContenido));
                            // metodoPagoDocumentoRelacionadoC.setHorizontalAlignment(Element.ALIGN_LEFT);
                            // metodoPagoDocumentoRelacionadoC.setBorder(Rectangle.NO_BORDER);

                            PdfPCell numeroParcialidaDocumentoRelacionadoC = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.C_NUMEROPARCIALIDADES).concat(" : ").concat(String.valueOf(documentosRelacionCargados.getNumParcialidad() != null ? documentosRelacionCargados.getNumParcialidad() : "")), fontContenido));
                            numeroParcialidaDocumentoRelacionadoC.setHorizontalAlignment(Element.ALIGN_LEFT);
                            numeroParcialidaDocumentoRelacionadoC.setBorder(Rectangle.NO_BORDER);

                            PdfPCell importeSaldoAntDocumentoRelacionadoC = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.C_IMPORTESALDOANT).concat(" : ").concat(String.valueOf(documentosRelacionCargados.getImpSaldoAnt() != null ? documentosRelacionCargados.getImpSaldoAnt() : "")), fontContenido));
                            importeSaldoAntDocumentoRelacionadoC.setHorizontalAlignment(Element.ALIGN_LEFT);
                            importeSaldoAntDocumentoRelacionadoC.setBorder(Rectangle.NO_BORDER);

                            PdfPCell importeSaldoInsoDocumentoRelacionadoC = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.C_IMPORTESALDOINS).concat(" : ").concat(String.valueOf(documentosRelacionCargados.getImpSaldoInsoluto() != null ? documentosRelacionCargados.getImpSaldoInsoluto() : "")), fontContenido));
                            importeSaldoInsoDocumentoRelacionadoC.setHorizontalAlignment(Element.ALIGN_LEFT);
                            importeSaldoInsoDocumentoRelacionadoC.setColspan(3);
                            importeSaldoInsoDocumentoRelacionadoC.setBorder(Rectangle.NO_BORDER);

                            PdfPCell importeSaldoPagadoDocumentoRelacionadoC = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.C_IMPORTEPAGADO).concat(" : ").concat(String.valueOf(documentosRelacionCargados.getImpPagado() != null ? documentosRelacionCargados.getImpPagado() : "")), fontContenido));
                            importeSaldoPagadoDocumentoRelacionadoC.setHorizontalAlignment(Element.ALIGN_LEFT);
                            importeSaldoPagadoDocumentoRelacionadoC.setBorder(Rectangle.NO_BORDER);

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

        PdfPCell selloEmisorC = new PdfPCell(new Phrase(hmapTagIdioma.get(EnumTagPlantilla.S_SD).concat(" : "), fontEtiquetas));
        selloEmisorC.setHorizontalAlignment(Element.ALIGN_LEFT);
        selloEmisorC.setBorder(Rectangle.NO_BORDER);

        PdfPCell selloEmisorV = new PdfPCell(new Phrase(facturaDto.getTimbrado().getSelloCFDI(), fontContenido));
        selloEmisorV.setHorizontalAlignment(Element.ALIGN_LEFT);
        selloEmisorV.setBorder(Rectangle.NO_BORDER);

        PdfPCell cadenaOriginalC = new PdfPCell(new Phrase(hmapTagIdioma.get(EnumTagPlantilla.S_CADENACDSAT).concat(" : "), fontEtiquetas));
        cadenaOriginalC.setHorizontalAlignment(Element.ALIGN_LEFT);
        cadenaOriginalC.setBorder(Rectangle.NO_BORDER);

        PdfPCell cadenaOriginalV = new PdfPCell(new Phrase(facturaDto.getTimbrado().getCadenaDatosTimbrado(), fontContenido));
        cadenaOriginalV.setHorizontalAlignment(Element.ALIGN_LEFT);
        cadenaOriginalV.setBorder(Rectangle.NO_BORDER);

        PdfPCell selloSATC = new PdfPCell(new Phrase(hmapTagIdioma.get(EnumTagPlantilla.S_SDCFDI).concat(" : "), fontEtiquetas));
        selloSATC.setHorizontalAlignment(Element.ALIGN_LEFT);
        selloSATC.setBorder(Rectangle.NO_BORDER);

        PdfPCell selloSATV = new PdfPCell(new Phrase(facturaDto.getTimbrado().getSelloSAT(), fontContenido));
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

            PdfPCell folioFiscalV = new PdfPCell(new Phrase(facturaDto.getTimbrado().getUuid(), fontContenido));
            folioFiscalV.setHorizontalAlignment(Element.ALIGN_LEFT);
            folioFiscalV.setBorder(Rectangle.NO_BORDER);

            PdfPCell cellEmisionFechaCertificacion = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.N_FECHAHORACERTIFICACION).concat(" : "), fontEtiquetas));
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

            PdfPCell cellEmisionCertificadoEmisor = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.N_NOSERIEEMISOR).concat(" : "), fontEtiquetas));
            cellEmisionCertificadoEmisor.setHorizontalAlignment(Element.ALIGN_LEFT);
            cellEmisionCertificadoEmisor.setBorder(Rectangle.NO_BORDER);

            PdfPCell cellEmisionCertificadoEmisorV = new PdfPCell(new Paragraph(facturaDto.getNumeroCertificado()  != null ? facturaDto.getNumeroCertificado()  : "", fontContenido));
            cellEmisionCertificadoEmisorV.setHorizontalAlignment(Element.ALIGN_LEFT);
            cellEmisionCertificadoEmisorV.setBorder(Rectangle.NO_BORDER);

            PdfPCell cellEmisionCertificadoSAT = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.E_NOCERTSAT).concat(" : "), fontEtiquetas));
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

            PdfPCell cellEmisionMetodoPago = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.T_METODOPAGO).concat(" : "), fontEtiquetas));
            cellEmisionMetodoPago.setHorizontalAlignment(Element.ALIGN_LEFT);
            cellEmisionMetodoPago.setBorder(Rectangle.NO_BORDER);

            PdfPCell cellEmisionMetodoPagoV = new PdfPCell(new Paragraph(facturaDto.getMetodoPago() != null ? facturaDto.getMetodoPago() : "", fontContenido));
            cellEmisionMetodoPagoV.setHorizontalAlignment(Element.ALIGN_LEFT);
            cellEmisionMetodoPagoV.setBorder(Rectangle.NO_BORDER);

            PdfPCell cellEmisionFormaPago = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.T_FORMAPAGO).concat(" : "), fontEtiquetas));
            cellEmisionFormaPago.setHorizontalAlignment(Element.ALIGN_LEFT);
            cellEmisionFormaPago.setBorder(Rectangle.NO_BORDER);

            if (hmapTagIdioma.get(EnumTagPlantilla.E_FOLIOFISCAL) != null) {
                PdfPCell folioFiscalC = new PdfPCell(new Phrase(hmapTagIdioma.get(EnumTagPlantilla.E_FOLIOFISCAL), fontEtiquetas));
                folioFiscalC.setHorizontalAlignment(Element.ALIGN_LEFT);
                folioFiscalC.setBorder(Rectangle.NO_BORDER);
                tableDatosFinales.addCell(folioFiscalC);
            } else {
                PdfPCell folioFiscalC = new PdfPCell(new Phrase("Folio Fiscal", fontEtiquetas));
                folioFiscalC.setHorizontalAlignment(Element.ALIGN_LEFT);
                folioFiscalC.setBorder(Rectangle.NO_BORDER);
                tableDatosFinales.addCell(folioFiscalC);
            }

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
                formaPagoDescripcion = facturaDto.getFormaPago().trim();

                PdfPCell cellEmisionFormaPagoV = new PdfPCell(new Paragraph(formaPagoDescripcion, fontContenido));
                cellEmisionFormaPagoV.setHorizontalAlignment(Rectangle.ALIGN_LEFT);
                cellEmisionFormaPagoV.setBorder(Rectangle.NO_BORDER);
                tableDatosFinales.addCell(cellEmisionFormaPagoV);
            } else {
                PdfPCell cellEmisionFormaPagoV = new PdfPCell();
                cellEmisionFormaPagoV.setHorizontalAlignment(Element.ALIGN_LEFT);
                cellEmisionFormaPagoV.setBorder(Rectangle.NO_BORDER);
                tableDatosFinales.addCell(cellEmisionFormaPagoV);
            }

            PdfPCell exportacionC = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.E_EXPORTACION).concat(" : "), fontEtiquetas));
            exportacionC.setHorizontalAlignment(Element.ALIGN_LEFT);
            exportacionC.setBorder(Rectangle.NO_BORDER);

            PdfPCell exportacion = new PdfPCell(new Paragraph(facturaDto.getExportacion() != null ? facturaDto.getExportacion() : "", fontContenido));
            exportacion.setHorizontalAlignment(Element.ALIGN_LEFT);
            exportacion.setBorder(Rectangle.NO_BORDER);

            tableDatosFinales.addCell(exportacionC);
            tableDatosFinales.addCell(exportacion);

            document.add(tableDatosFinales);

            } catch (Exception ex) {
                System.out.println("Error al agregar Datos Finales " + ex.getMessage());
            }
           // Logger.getLogger(PdfFacturaTenantAServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
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
        tableCodigoQR.setWidths(new int[]{300});
        tableCodigoQR.setTotalWidth(ANCHO_TOTAL_TABLA);
        tableCodigoQR.setLockedWidth(true);
        tableCodigoQR.setWidthPercentage(PORCENTAJE_ANCHO_TABLA);
        tableCodigoQR.getDefaultCell().setBorder(Rectangle.NO_BORDER);
        tableCodigoQR.getDefaultCell().setFixedHeight(ALTURA_FIJA_TABLA);
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
            BarcodeQRCode barcodeQRCode = new BarcodeQRCode(dato, 800, 800, null);
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


    private void agregarTableInformacionGlobal (Document document, CFDI facturaDto) throws  DocumentException {
        PdfPTable tableInformacionGlobalH = new PdfPTable(1);
        tableInformacionGlobalH.setWidths(new int[]{ 560});
        tableInformacionGlobalH.setTotalWidth(ANCHO_TOTAL_TABLA);
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


    private void agregarTableDatosCfdiRelacionados(Document document, CFDI facturaDto) throws  DocumentException {
        PdfPTable tableDatosH = new PdfPTable(1);
        tableDatosH.setWidths(new int[]{ANCHO_TOTAL_TABLA});
        tableDatosH.setTotalWidth(ANCHO_TOTAL_TABLA);
        tableDatosH.setLockedWidth(true);
        tableDatosH.setWidthPercentage(PORCENTAJE_ANCHO_TABLA);
        tableDatosH.getDefaultCell().setBorder(Rectangle.NO_BORDER);

        PdfPTable tableCfdiRelacionados = new PdfPTable(2);
        tableCfdiRelacionados.setWidths(new int[]{400 ,200});
        tableCfdiRelacionados.setTotalWidth(ANCHO_TOTAL_TABLA);
        tableCfdiRelacionados.setLockedWidth(true);
        tableCfdiRelacionados.setWidthPercentage(PORCENTAJE_ANCHO_TABLA);
        tableCfdiRelacionados.getDefaultCell().setBorder(Rectangle.NO_BORDER);


        if (facturaDto.getCfdiRelacionados()  != null) {
     

            for (Relacionados cfdiRelacionadosCargados : facturaDto.getCfdiRelacionados()) {             

                StringBuilder cfdiRelacionado = new StringBuilder();       
                if (facturaDto.getCfdiRelacionados() != null ) {
                    cfdiRelacionado.append(cfdiRelacionadosCargados.getUuid()).append(" ");
                }

                PdfPCell uuidRelacionado = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.UUID_RELACIONADO).concat(" : ") + cfdiRelacionado, fontContenido));
                uuidRelacionado.setHorizontalAlignment(Element.ALIGN_LEFT);
                uuidRelacionado.setBorder(Rectangle.NO_BORDER);
                

                PdfPCell tipoRelacion = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.TIPO_RELACION).concat(" : ").concat(cfdiRelacionadosCargados.getTipoRelacion() != null ? cfdiRelacionadosCargados.getTipoRelacion()  : ""), fontContenido));
                tipoRelacion.setHorizontalAlignment(Element.ALIGN_LEFT);
                tipoRelacion.setBorder(Rectangle.NO_BORDER);

                tableCfdiRelacionados.addCell(uuidRelacionado);
                tableCfdiRelacionados.addCell(tipoRelacion);
            }
       
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
            for (TrasladoP impuestosCargados : conceptos.getImpuestosP().getTrasladosP().getTrasladoP() ) {
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
}
