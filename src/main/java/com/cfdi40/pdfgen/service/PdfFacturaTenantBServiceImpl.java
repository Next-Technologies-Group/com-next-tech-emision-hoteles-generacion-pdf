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
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BarcodeQRCode;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPCellEvent;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPTableEvent;
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
import com.cfdi40.pdfgen.tenants.tenanta.cfdi.dto.CFDI.Relacionados;
import com.cfdi40.pdfgen.tenants.tenanta.cfdi.dto.CFDI.Complementos.ComplementoPago20;
import com.cfdi40.pdfgen.tenants.tenanta.cfdi.dto.CFDI.Complementos.ComplementoPago20.Pago;
import com.cfdi40.pdfgen.tenants.tenanta.cfdi.dto.CFDI.Complementos.ComplementoPago20.Pago.DoctoRelacionado;
import com.cfdi40.pdfgen.tenants.tenanta.cfdi.dto.CFDI.Complementos.ComplementoPago20.Pago.DoctoRelacionado.ImpuestosDR.RetencionesDR.RetencionDR;
import com.cfdi40.pdfgen.tenants.tenanta.cfdi.dto.CFDI.Complementos.ComplementoPago20.Pago.DoctoRelacionado.ImpuestosDR.TrasladosDR.TrasladoDR;
import com.cfdi40.pdfgen.tenants.tenanta.cfdi.dto.CFDI.Impuestos.Traslado;
import com.cfdi40.pdfgen.tenants.tenanta.cfdi.dto.CFDI.ImpuestosLocales.TrasladoLocal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class PdfFacturaTenantBServiceImpl  implements PdfFacturaTenantBService {


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
    
    Font fontContenido = new Font(Font.FontFamily.HELVETICA, 7, Font.NORMAL, new BaseColor(128, 128, 128));
    Font fontEtiquetas = new Font(Font.FontFamily.HELVETICA, 7, Font.BOLD, new BaseColor(128, 128, 128));
    Font fontLeyendas = new Font(Font.FontFamily.HELVETICA, 6, Font.NORMAL, new BaseColor(128, 128, 128));
    Font titulo = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, new BaseColor(128, 128, 128));
    Font secondTitulo = new Font(Font.FontFamily.HELVETICA, 8, Font.BOLD, new BaseColor(128, 128, 128));
    private HashMap<EnumTagPlantilla, String> hmapTagIdioma;
    private List<CatTagxidioma> catTagxidioma;
    BaseColor colorBorderCell;
    private static final int ANCHO_TOTAL_TABLA = 560;
    private static final int PORCENTAJE_ANCHO_TABLA = 100;
    private static final float ALTURA_FIJA_TABLA = 80;

    @Override
    public ByteArrayOutputStream generarPDfFactura(CFDI facturaDto, IdentificadorSucursal identificadorSucursal) throws BusinessException {
        try {
            colorBorderCell = new BaseColor(230, 231, 233);
            CatIdiomapdf catIdioma = null;
            ByteArrayOutputStream pdfResponse = null;

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
            agregarDatosYLogo(document,identificadorSucursal, facturaDto);
            agregarDatosEmision(document, facturaDto);
            agregarDatosEmisor(document, facturaDto);
            agregarConceptos(document, facturaDto);
            agregarSubtotales(document, facturaDto);
            agregarInformacionGlobal(document, facturaDto);
            agregarTableDatosCfdiRelacionados(document, facturaDto);
            agregarComplementoPagos(document, facturaDto);
            agregarTotales(document, facturaDto);
            agregarSellos(document, facturaDto);
            
            document.close();
            writer.flush();
            
            return pdfResponse;
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException(HttpStatus.BAD_REQUEST,"Ocurrio un error al tratar de generar el PDF de Globo Go de la factura con UUID "
                    + (facturaDto.getTimbrado() != null ? facturaDto.getTimbrado().getUuid() : "") + " Error: " + e.getStackTrace()[0].getLineNumber());
        }
    }

    public void agregarDatosYLogo(Document document, IdentificadorSucursal identificadorSucursal, CFDI facturaDto) throws DocumentException {
        PdfPTable tableDatosYLogo = new PdfPTable(2);
        tableDatosYLogo.setWidths(new int[]{200, 360});
        tableDatosYLogo.setTotalWidth(ANCHO_TOTAL_TABLA);
        tableDatosYLogo.setLockedWidth(true);
        tableDatosYLogo.setWidthPercentage(PORCENTAJE_ANCHO_TABLA);
        tableDatosYLogo.getDefaultCell().setBorder(Rectangle.NO_BORDER);

        PdfPTable tableLogo = new PdfPTable(1);
        tableLogo.setWidths(new int[]{200});
        tableLogo.setTotalWidth(85);
        tableLogo.setLockedWidth(true);
        tableLogo.setWidthPercentage(40);
        tableLogo.getDefaultCell().setBorder(Rectangle.NO_BORDER);

        try {
            PdfPCell celdaLogo = new PdfPCell();
            byte[] bytesLogo = getLogo(identificadorSucursal.getEmisorId().getRfc(),identificadorSucursal.getSucursalId().getNombreLogo());
            if (bytesLogo!=null) {
                Image logo = Image.getInstance(bytesLogo);
                logo.scaleToFit(80,80);
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

        PdfPTable tableDatos = new PdfPTable(1);
        tableDatos.setWidths(new int[]{360});
        tableDatos.setTotalWidth(360);
        tableDatos.setLockedWidth(true);
        tableDatos.setWidthPercentage(PORCENTAJE_ANCHO_TABLA);
        tableDatos.getDefaultCell().setBorder(Rectangle.NO_BORDER);

        PdfPTable tableEncabezado = new PdfPTable(2);
        tableEncabezado.setWidths(new int[]{240, 120});
        tableEncabezado.setTotalWidth(360);
        tableEncabezado.setLockedWidth(true);
        tableEncabezado.setWidthPercentage(PORCENTAJE_ANCHO_TABLA);
        tableEncabezado.getDefaultCell().setBorder(Rectangle.NO_BORDER);

        PdfPCell nombreDatos = new PdfPCell(new Paragraph(facturaDto.getEmisor().getNombre() != null ? facturaDto.getEmisor().getNombre() : "", titulo));
        nombreDatos.setHorizontalAlignment(Element.ALIGN_LEFT);
        nombreDatos.setBorder(Rectangle.NO_BORDER);

        StringBuilder tipoDocumento = new StringBuilder();
        if (facturaDto.getTipoCfdi().equals(CFDI.EnumTipoFactura.NOTA_CREDITO)) {
            tipoDocumento.append(hmapTagIdioma.get(EnumTagPlantilla.NOTA_CREDITO)).append("  ");
        } else if (facturaDto.getTipoCfdi().equals(CFDI.EnumTipoFactura.FACTURA)) {
            tipoDocumento.append(hmapTagIdioma.get(EnumTagPlantilla.FACTURA)).append("  ");
        } else if (facturaDto.getTipoCfdi().equals(CFDI.EnumTipoFactura.PAGO)) {
            tipoDocumento.append(hmapTagIdioma.get(EnumTagPlantilla.C_COMPLEMENTO_RECEPCION_PAGO)).append("  ");
        }
        PdfPCell tipoDocumentoDatos = new PdfPCell(new Paragraph(tipoDocumento.toString(), secondTitulo));
        tipoDocumentoDatos.setHorizontalAlignment(Element.ALIGN_RIGHT);
        tipoDocumentoDatos.setBorder(Rectangle.NO_BORDER);

        tableEncabezado.addCell(nombreDatos);
        tableEncabezado.addCell(tipoDocumentoDatos);

//        StringBuilder direccionDatosString = new StringBuilder();
//        direccionDatosString = direccionDatosString.append((facturaDto.getEmisor().getCalle() != null ? facturaDto.getEmisor().getCalle() : "")
//                .concat("   |   ").concat(hmapTagIdioma.get(EnumTagPlantilla.CODIGOPOSTAL))
//                .concat(facturaDto.getEmisor().getCodigoPostal() != null ? facturaDto.getEmisor().getCodigoPostal() : "")
//                .concat("  ,  ").concat(facturaDto.getEmisor().getMunicipio() != null ? facturaDto.getEmisor().getMunicipio() : "")
//                .concat(" , ").concat(facturaDto.getEmisor().getEstado() != null ? facturaDto.getEmisor().getEstado() : ""));
        StringBuilder direccionDatosString = new StringBuilder();
        direccionDatosString = direccionDatosString.append((hmapTagIdioma.get(EnumTagPlantilla.CODIGOPOSTAL))
                .concat(facturaDto.getLugarExpedicion() != null ? facturaDto.getLugarExpedicion() : ""));
        PdfPCell direccionDatos = new PdfPCell(new Paragraph(direccionDatosString.toString(), secondTitulo));
        direccionDatos.setHorizontalAlignment(Element.ALIGN_LEFT);
        direccionDatos.setBorder(Rectangle.NO_BORDER);

        StringBuilder direccionDatosString2 = new StringBuilder();
        direccionDatosString2 = direccionDatosString2.append(hmapTagIdioma.get(EnumTagPlantilla.G_TELEFONO).concat(".")
                .concat(identificadorSucursal.getSucursalId().getTelefono() != null ? identificadorSucursal.getSucursalId().getTelefono() : "").concat("   |   ").concat(hmapTagIdioma.get(EnumTagPlantilla.RFC))
                .concat(identificadorSucursal.getEmisorId().getRfc() != null ? identificadorSucursal.getEmisorId().getRfc() : "").concat("   |   ")
                .concat(hmapTagIdioma.get(EnumTagPlantilla.N_REGIMENFISCAL)).concat(" : ")
                .concat(identificadorSucursal.getEmisorId().getRegimenFiscal() != null ? identificadorSucursal.getEmisorId().getRegimenFiscal() : ""));

        PdfPCell direccionDatos2 = new PdfPCell(new Paragraph(direccionDatosString2.toString(), secondTitulo));
        direccionDatos2.setHorizontalAlignment(Element.ALIGN_LEFT);
        direccionDatos2.setBorder(Rectangle.NO_BORDER);

        tableDatos.addCell(tableEncabezado);
        tableDatos.addCell(direccionDatos);
        tableDatos.addCell(direccionDatos2);

        tableDatosYLogo.addCell(tableLogo);
        tableDatosYLogo.addCell(tableDatos);

        document.add(tableDatosYLogo);
    }

    public void agregarDatosEmision(Document document, CFDI facturaDto) throws DocumentException {
        PdfPTable tableDatosEmision = new PdfPTable(1);
        tableDatosEmision.setWidths(new int[]{560});
        tableDatosEmision.setTotalWidth(ANCHO_TOTAL_TABLA);
        tableDatosEmision.setLockedWidth(true);
        tableDatosEmision.setWidthPercentage(PORCENTAJE_ANCHO_TABLA);
        tableDatosEmision.setSpacingBefore(5);
        tableDatosEmision.getDefaultCell().setBorder(Rectangle.NO_BORDER);

        PdfPTable tableDatos = new PdfPTable(8);
        tableDatos.setWidths(new int[]{120, 20, 120, 20, 120, 20, 120, 20});
        tableDatos.setTotalWidth(ANCHO_TOTAL_TABLA);
        tableDatos.setWidthPercentage(50);
        tableDatos.getDefaultCell().setBorder(Rectangle.NO_BORDER);

        PdfPCell serieDatosC = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.G_SERIENUMERO), fontContenido));
        serieDatosC.setHorizontalAlignment(Element.ALIGN_LEFT);
        serieDatosC.setBorder(Rectangle.NO_BORDER);

        PdfPCell fechaDatosC = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.G_FECHAHORAEMISION), fontContenido));
        fechaDatosC.setHorizontalAlignment(Element.ALIGN_LEFT);
        fechaDatosC.setBorder(Rectangle.NO_BORDER);

        PdfPCell numeroCertificadoEmisorDatosC = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.G_NOCERTIFICADOEMISOR), fontContenido));
        numeroCertificadoEmisorDatosC.setHorizontalAlignment(Element.ALIGN_LEFT);
        numeroCertificadoEmisorDatosC.setBorder(Rectangle.NO_BORDER);

        PdfPCell numeroCertificadoSATDatosC = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.E_NOCERTSAT), fontContenido));
        numeroCertificadoSATDatosC.setHorizontalAlignment(Element.ALIGN_LEFT);
        numeroCertificadoSATDatosC.setBorder(Rectangle.NO_BORDER);

        StringBuilder serieFolio = new StringBuilder();
        serieFolio = serieFolio.append((facturaDto.getSerie() != null ? facturaDto.getSerie() : "").concat((facturaDto.getFolio() != null ? facturaDto.getFolio() : "")));
        PdfPCell serieDatosV = new PdfPCell(new Paragraph(serieFolio.toString(), fontContenido));
        serieDatosV.setCellEvent(new RoundRectangle());
        serieDatosV.setBorder(Rectangle.NO_BORDER);
        serieDatosV.setFixedHeight(15);

        String lugarFechaCertificacion = "";
        if (facturaDto.getFecha() != null) {
            Date dateCheckIn = facturaDto.getFecha();
            DateFormat dateFormatCheckIn = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            lugarFechaCertificacion = dateFormatCheckIn.format(dateCheckIn);
        }

        PdfPCell fechaDatosV = new PdfPCell(new Paragraph(lugarFechaCertificacion, fontContenido));
        fechaDatosV.setCellEvent(new RoundRectangle());
        fechaDatosV.setBorder(Rectangle.NO_BORDER);

        PdfPCell numeroCertificadoEmisorDatosV = new PdfPCell(new Paragraph(facturaDto.getNumeroCertificado() != null ? facturaDto.getNumeroCertificado() : "", fontContenido));
        numeroCertificadoEmisorDatosV.setCellEvent(new RoundRectangle());
        numeroCertificadoEmisorDatosV.setBorder(Rectangle.NO_BORDER);

        PdfPCell numeroCertificadoSATDatosV = new PdfPCell(new Paragraph(facturaDto.getTimbrado().getNumCertificadoSAT(), fontContenido));
        numeroCertificadoSATDatosV.setCellEvent(new RoundRectangle());
        numeroCertificadoSATDatosV.setBorder(Rectangle.NO_BORDER);

        PdfPCell vacia = new PdfPCell();
        vacia.setHorizontalAlignment(Element.ALIGN_LEFT);
        vacia.setBorder(Rectangle.NO_BORDER);

        tableDatos.addCell(serieDatosC);
        tableDatos.addCell(vacia);
        tableDatos.addCell(fechaDatosC);
        tableDatos.addCell(vacia);
        tableDatos.addCell(numeroCertificadoEmisorDatosC);
        tableDatos.addCell(vacia);
        tableDatos.addCell(numeroCertificadoSATDatosC);
        tableDatos.addCell(vacia);
        tableDatos.addCell(serieDatosV);
        tableDatos.addCell(vacia);
        tableDatos.addCell(fechaDatosV);
        tableDatos.addCell(vacia);
        tableDatos.addCell(numeroCertificadoEmisorDatosV);
        tableDatos.addCell(vacia);
        tableDatos.addCell(numeroCertificadoSATDatosV);
        tableDatos.addCell(vacia);

        PdfPTable tableDatos2 = new PdfPTable(6);
        tableDatos2.setWidths(new int[]{166, 20, 166, 20, 166, 20});
        tableDatos2.setTotalWidth(ANCHO_TOTAL_TABLA);
        tableDatos2.setWidthPercentage(50);
        tableDatos2.setSpacingBefore(6);
        tableDatos2.getDefaultCell().setBorder(Rectangle.NO_BORDER);

        PdfPCell folioDatosC = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.G_FOLIOUUID), fontContenido));
        folioDatosC.setHorizontalAlignment(Element.ALIGN_LEFT);
        folioDatosC.setBorder(Rectangle.NO_BORDER);

        PdfPCell fechaCertificacionDatosC = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.G_FECHACERTIFICACION), fontContenido));
        fechaCertificacionDatosC.setHorizontalAlignment(Element.ALIGN_LEFT);
        fechaCertificacionDatosC.setBorder(Rectangle.NO_BORDER);

        PdfPCell compraDatosC = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.G_COMPRAREALIZADA), fontContenido));
        compraDatosC.setHorizontalAlignment(Element.ALIGN_LEFT);
        compraDatosC.setBorder(Rectangle.NO_BORDER);

        PdfPCell folioDatosV = new PdfPCell(new Paragraph(facturaDto.getTimbrado().getUuid(), fontContenido));
        folioDatosV.setCellEvent(new RoundRectangle());
        folioDatosV.setBorder(Rectangle.NO_BORDER);
        folioDatosV.setFixedHeight(15);

        String lugarFechaEmision = "";
        if (facturaDto.getTimbrado().getFechaTimbrado() != null) {
            Date dateCheckIn = facturaDto.getTimbrado().getFechaTimbrado();
            DateFormat dateFormatCheckIn = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            lugarFechaEmision = dateFormatCheckIn.format(dateCheckIn);
        }

        PdfPCell fechaCertificacionDatosV = new PdfPCell(new Paragraph(lugarFechaEmision, fontContenido));
        fechaCertificacionDatosV.setCellEvent(new RoundRectangle());
        fechaCertificacionDatosV.setBorder(Rectangle.NO_BORDER);

        PdfPCell compraDatosV = new PdfPCell(new Paragraph(facturaDto.getTenantB() != null && facturaDto.getTenantB().getPortal() != null ? facturaDto.getTenantB().getPortal() : "", fontContenido));
        compraDatosV.setCellEvent(new RoundRectangle());
        compraDatosV.setBorder(Rectangle.NO_BORDER);

        tableDatos2.addCell(folioDatosC);
        tableDatos2.addCell(vacia);
        tableDatos2.addCell(fechaCertificacionDatosC);
        tableDatos2.addCell(vacia);
        tableDatos2.addCell(compraDatosC);
        tableDatos2.addCell(vacia);
        tableDatos2.addCell(folioDatosV);
        tableDatos2.addCell(vacia);
        tableDatos2.addCell(fechaCertificacionDatosV);
        tableDatos2.addCell(vacia);
        tableDatos2.addCell(compraDatosV);
        tableDatos2.addCell(vacia);

        tableDatosEmision.addCell(tableDatos);
        tableDatosEmision.addCell(tableDatos2);

        document.add(tableDatosEmision);

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
        PdfPCell space = new PdfPCell();
        space.setBackgroundColor(BaseColor.BLACK);
        space.setFixedHeight(1.5f);
        space.setBorder(Rectangle.NO_BORDER);
      
        PdfPCell datosClienteC = new PdfPCell(new Phrase(hmapTagIdioma.get(EnumTagPlantilla.CFDI_RELACIONADOS), fontEtiquetas));
        datosClienteC.setColspan(2);
        //datosClienteC.setBackgroundColor(BaseColor.BLACK);
        datosClienteC.setHorizontalAlignment(Element.ALIGN_LEFT);
        datosClienteC.setBorder(Rectangle.NO_BORDER);

        tableDatosH.addCell(space);
        tableDatosH.addCell(datosClienteC);
        tableDatosH.addCell(tableCfdiRelacionados);    
       

        document.add(tableDatosH);
    }

    
    public void agregarInformacionGlobal(Document document, CFDI facturaDto) throws DocumentException {
        
        PdfPTable tableHeader = new PdfPTable(1);
        tableHeader.setWidths(new int[]{560});
        tableHeader.setTotalWidth(ANCHO_TOTAL_TABLA);
        tableHeader.setLockedWidth(true);
        tableHeader.setWidthPercentage(PORCENTAJE_ANCHO_TABLA);
        tableHeader.getDefaultCell().setBorder(Rectangle.NO_BORDER);

        if (facturaDto.getInformacionGlobal()!= null) {

        PdfPCell informacionGlobal = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.INFORMACION_GLOBAL), fontEtiquetas));
        informacionGlobal.setBorder(Rectangle.NO_BORDER);
        tableHeader.addCell(informacionGlobal);


        PdfPTable tableInformacionGlobal = new PdfPTable(5);
        tableInformacionGlobal.setWidths(new int[]{20, 20, 20, 20, 20});
        tableInformacionGlobal.setTotalWidth(ANCHO_TOTAL_TABLA);
        tableInformacionGlobal.setLockedWidth(true);
        tableInformacionGlobal.setWidthPercentage(PORCENTAJE_ANCHO_TABLA);
        tableInformacionGlobal.setSpacingBefore(10);
        tableInformacionGlobal.getDefaultCell().setBorder(Rectangle.NO_BORDER);
        tableInformacionGlobal.getDefaultCell().setBorderColor(BaseColor.WHITE);

        PdfPCell periodo = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.PERIODICIDAD), fontContenido));
        periodo.setHorizontalAlignment(Element.ALIGN_LEFT);
        periodo.setBorderColor(BaseColor.WHITE);
        periodo.setColspan(1);
        tableInformacionGlobal.addCell(periodo);

        PdfPCell meses = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.MESES), fontContenido));
        meses.setBorderColor(BaseColor.WHITE);
        meses.setHorizontalAlignment(Element.ALIGN_LEFT);
        meses.setColspan(1);
        tableInformacionGlobal.addCell(meses);

        PdfPCell anio = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.ANIO), fontContenido));
        anio.setBorderColor(BaseColor.WHITE);
        anio.setHorizontalAlignment(Element.ALIGN_LEFT);
        anio.setColspan(1);
        tableInformacionGlobal.addCell(anio);

        PdfPCell cell4 = new PdfPCell(new Paragraph("", fontContenido));
        cell4.setHorizontalAlignment(Element.ALIGN_LEFT);
        cell4.setBorderColor(BaseColor.WHITE);
        cell4.setColspan(1);
        tableInformacionGlobal.addCell(cell4);

        PdfPCell cell5 = new PdfPCell(new Paragraph("", fontContenido));
        cell5.setHorizontalAlignment(Element.ALIGN_LEFT);
        cell5.setBorderColor(BaseColor.WHITE);
        cell5.setColspan(1);
        tableInformacionGlobal.addCell(cell5);
        
        PdfPCell periodicidadV = new PdfPCell(new Paragraph(
            facturaDto.getInformacionGlobal() != null && facturaDto.getInformacionGlobal().getPeriodicidad() != null
                ? facturaDto.getInformacionGlobal().getPeriodicidad()
                : ""
            , fontContenido)
        );
        periodicidadV.setCellEvent(new RoundRectangle());
        periodicidadV.setBorder(Rectangle.NO_BORDER);
        periodicidadV.setFixedHeight(15);
        periodicidadV.setColspan(1);
        tableInformacionGlobal.addCell(periodicidadV);
        
        PdfPCell mesesV = new PdfPCell(new Paragraph(
            facturaDto.getInformacionGlobal() != null && facturaDto.getInformacionGlobal().getMeses() != null
                ? facturaDto.getInformacionGlobal().getMeses()
                : ""
            , fontContenido)
        );
        mesesV.setCellEvent(new RoundRectangle());
        mesesV.setBorder(Rectangle.NO_BORDER);
        mesesV.setFixedHeight(15);
        mesesV.setColspan(1);
        tableInformacionGlobal.addCell(mesesV);
        
        PdfPCell anioV = new PdfPCell(new Paragraph(
            facturaDto.getInformacionGlobal() != null ? facturaDto.getInformacionGlobal().getMeses() : ""
            , fontContenido)
        );
        anioV.setCellEvent(new RoundRectangle());
        anioV.setBorder(Rectangle.NO_BORDER);
        anioV.setFixedHeight(15);
        anioV.setColspan(1);
        tableInformacionGlobal.addCell(anioV);        

        PdfPCell cell6 = new PdfPCell(new Paragraph("", fontContenido));
        cell6.setHorizontalAlignment(Element.ALIGN_LEFT);
        cell6.setBorderColor(BaseColor.WHITE);
        cell6.setColspan(1);
        tableInformacionGlobal.addCell(cell6);

        PdfPCell cell7 = new PdfPCell(new Paragraph("", fontContenido));
        cell7.setHorizontalAlignment(Element.ALIGN_LEFT);
        cell7.setBorderColor(BaseColor.WHITE);
        cell7.setColspan(1);
        tableInformacionGlobal.addCell(cell7);

        PdfPTable tableFooter = new PdfPTable(1);
        tableFooter.setWidths(new int[]{ANCHO_TOTAL_TABLA});
        tableFooter.setTotalWidth(ANCHO_TOTAL_TABLA);
        tableFooter.setLockedWidth(true);
        tableFooter.setWidthPercentage(PORCENTAJE_ANCHO_TABLA);
        tableFooter.getDefaultCell().setBorder(Rectangle.NO_BORDER);

        PdfPCell space = new PdfPCell();
        space.setBackgroundColor(BaseColor.WHITE);
        space.setFixedHeight(5f);
        space.setBorder(Rectangle.NO_BORDER);
        tableFooter.addCell(space);
        document.add(tableHeader);
        document.add(tableInformacionGlobal);
        document.add(tableFooter);
        }
       
    }
    public void agregarDatosEmisor(Document document, CFDI facturaDto) throws DocumentException {
        PdfPTable tableDatosEmisor = new PdfPTable(1);
        tableDatosEmisor.setWidths(new int[]{560});
        tableDatosEmisor.setTotalWidth(ANCHO_TOTAL_TABLA);
        tableDatosEmisor.setLockedWidth(true);
        tableDatosEmisor.setWidthPercentage(PORCENTAJE_ANCHO_TABLA);
        tableDatosEmisor.setSpacingBefore(10);
        tableDatosEmisor.getDefaultCell().setBorder(Rectangle.NO_BORDER);

        StringBuilder rfcReceptorString = new StringBuilder();
        rfcReceptorString = rfcReceptorString.append(facturaDto.getReceptor().getRfc() != null ? facturaDto.getReceptor().getRfc() : "");
        PdfPCell rfcReceptor = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.G_RFCCLIENTE).concat(": ").concat(rfcReceptorString.toString()), fontContenido));
        rfcReceptor.setHorizontalAlignment(Element.ALIGN_LEFT);
        rfcReceptor.setBorderWidthLeft(0.3f);
        rfcReceptor.setBorderWidthTop(0.3f);
        rfcReceptor.setBorderWidthRight(0.3f);
        rfcReceptor.setBorderWidthBottom(0f);
        rfcReceptor.setBorderColor(BaseColor.LIGHT_GRAY);

        StringBuilder razonSocialReceptorString = new StringBuilder();
        razonSocialReceptorString = razonSocialReceptorString.append(facturaDto.getReceptor().getNombre() != null ? facturaDto.getReceptor().getNombre() : "");
        PdfPCell razonSocialReceptor = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.G_RAZONCLIENTE).concat(": ").concat(razonSocialReceptorString.toString()), fontContenido));
        razonSocialReceptor.setHorizontalAlignment(Element.ALIGN_LEFT);
        razonSocialReceptor.setBorderWidthLeft(0.3f);
        razonSocialReceptor.setBorderWidthTop(0f);
        razonSocialReceptor.setBorderWidthRight(0.3f);
        razonSocialReceptor.setBorderWidthBottom(0f);
        razonSocialReceptor.setBorderColor(BaseColor.LIGHT_GRAY);

        StringBuilder domicilioFiscalReceptorString = new StringBuilder();
        domicilioFiscalReceptorString = domicilioFiscalReceptorString.append(facturaDto.getReceptor().getDomicilioFiscal() != null ? facturaDto.getReceptor().getDomicilioFiscal() : "");
        PdfPCell domicilioFiscalReceptor = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.R_DOMFISCRECEP).concat(": ").concat(domicilioFiscalReceptorString.toString()), fontContenido));
        domicilioFiscalReceptor.setHorizontalAlignment(Element.ALIGN_LEFT);
        domicilioFiscalReceptor.setBorderWidthLeft(0.3f);
        domicilioFiscalReceptor.setBorderWidthTop(0f);
        domicilioFiscalReceptor.setBorderWidthRight(0.3f);
        domicilioFiscalReceptor.setBorderWidthBottom(0f);
        domicilioFiscalReceptor.setBorderColor(BaseColor.LIGHT_GRAY);

        StringBuilder usoCfdiReceptorString = new StringBuilder();
        usoCfdiReceptorString = usoCfdiReceptorString.append(facturaDto.getReceptor().getUsoCFDI() != null ? facturaDto.getReceptor().getUsoCFDI() : "");
        PdfPCell usoCFDIReceptor = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.G_USOCFDI).concat(": ").concat(usoCfdiReceptorString.toString()), fontContenido));
        usoCFDIReceptor.setHorizontalAlignment(Element.ALIGN_LEFT);
        usoCFDIReceptor.setBorderWidthLeft(0.3f);
        usoCFDIReceptor.setBorderWidthTop(0f);
        usoCFDIReceptor.setBorderWidthRight(0.3f);
        usoCFDIReceptor.setBorderWidthBottom(0.3f);
        usoCFDIReceptor.setBorderColor(BaseColor.LIGHT_GRAY);

        StringBuilder regimeFiscReceptorString = new StringBuilder();
        regimeFiscReceptorString.append(facturaDto.getReceptor().getRegimenFiscal() != null ? facturaDto.getReceptor().getRegimenFiscal() : "");
        PdfPCell regFiscReceptor = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.REG_FISCAL_RECEP).concat(": ").concat(regimeFiscReceptorString.toString()), fontContenido));
        regFiscReceptor.setHorizontalAlignment(Element.ALIGN_LEFT);
        regFiscReceptor.setBorderWidthLeft(0.3f);
        regFiscReceptor.setBorderWidthTop(0f);
        regFiscReceptor.setBorderWidthRight(0.3f);
        regFiscReceptor.setBorderWidthBottom(0.3f);
        regFiscReceptor.setBorderColor(BaseColor.LIGHT_GRAY);

        PdfPTable tableEncabezadoProductos = new PdfPTable(6);
        tableEncabezadoProductos.setWidths(new int[]{30, 35, 60, 180, 80, 80});
        tableEncabezadoProductos.setTotalWidth(ANCHO_TOTAL_TABLA);
        tableEncabezadoProductos.setLockedWidth(true);
        tableEncabezadoProductos.setWidthPercentage(PORCENTAJE_ANCHO_TABLA);
        tableEncabezadoProductos.setSpacingAfter(-0.000004f);
        tableEncabezadoProductos.getDefaultCell().setBorder(Rectangle.NO_BORDER);

        PdfPCell claveProductos = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.N_CLAVE), fontContenido));
        claveProductos.setHorizontalAlignment(Element.ALIGN_CENTER);
        claveProductos.setBorderWidthLeft(0.3f);
        claveProductos.setBorderWidthTop(0f);
        claveProductos.setBorderWidthRight(0.3f);
        claveProductos.setBorderWidthBottom(0.3f);
        claveProductos.setBorderColor(BaseColor.LIGHT_GRAY);

        PdfPCell cantidadProductos = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.P_CANTIDAD), fontContenido));
        cantidadProductos.setHorizontalAlignment(Element.ALIGN_CENTER);
        cantidadProductos.setBorderWidthLeft(0f);
        cantidadProductos.setBorderWidthTop(0f);
        cantidadProductos.setBorderWidthRight(0.3f);
        cantidadProductos.setBorderWidthBottom(0.3f);
        cantidadProductos.setBorderColor(BaseColor.LIGHT_GRAY);

        PdfPCell unidadProductos = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.P_CLAVEUNIDAD), fontContenido));
        unidadProductos.setHorizontalAlignment(Element.ALIGN_CENTER);
        unidadProductos.setBorderWidthLeft(0f);
        unidadProductos.setBorderWidthTop(0f);
        unidadProductos.setBorderWidthRight(0.3f);
        unidadProductos.setBorderWidthBottom(0.3f);
        unidadProductos.setBorderColor(BaseColor.LIGHT_GRAY);

        PdfPCell descripcionProducto = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.CON_DESCRIPCION), fontContenido));
        descripcionProducto.setHorizontalAlignment(Element.ALIGN_CENTER);
        descripcionProducto.setBorderWidthLeft(0f);
        descripcionProducto.setBorderWidthTop(0f);
        descripcionProducto.setBorderWidthRight(0.3f);
        descripcionProducto.setBorderWidthBottom(0.3f);
        descripcionProducto.setBorderColor(BaseColor.LIGHT_GRAY);

        PdfPCell precioUnitarioProductos = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.CON_PUNITARIO), fontContenido));
        precioUnitarioProductos.setHorizontalAlignment(Element.ALIGN_CENTER);
        precioUnitarioProductos.setBorderWidthLeft(0f);
        precioUnitarioProductos.setBorderWidthTop(0f);
        precioUnitarioProductos.setBorderWidthRight(0.3f);
        precioUnitarioProductos.setBorderWidthBottom(0.3f);
        precioUnitarioProductos.setBorderColor(BaseColor.LIGHT_GRAY);

        PdfPCell importeProductos = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.CON_IMPORTE), fontContenido));
        importeProductos.setHorizontalAlignment(Element.ALIGN_CENTER);
        importeProductos.setBorderWidthLeft(0f);
        importeProductos.setBorderWidthTop(0f);
        importeProductos.setBorderWidthRight(0.3f);
        importeProductos.setBorderWidthBottom(0.3f);
        importeProductos.setBorderColor(BaseColor.LIGHT_GRAY);

        tableEncabezadoProductos.addCell(claveProductos);
        tableEncabezadoProductos.addCell(cantidadProductos);
        tableEncabezadoProductos.addCell(unidadProductos);
        tableEncabezadoProductos.addCell(descripcionProducto);
        tableEncabezadoProductos.addCell(precioUnitarioProductos);
        tableEncabezadoProductos.addCell(importeProductos);

        tableDatosEmisor.addCell(rfcReceptor);
        tableDatosEmisor.addCell(razonSocialReceptor);
        tableDatosEmisor.addCell(domicilioFiscalReceptor);
        tableDatosEmisor.addCell(usoCFDIReceptor);
        tableDatosEmisor.addCell(regFiscReceptor);
        tableDatosEmisor.addCell(tableEncabezadoProductos);

        document.add(tableDatosEmisor);

    }

    public void agregarConceptos(Document document, CFDI facturaDto) throws DocumentException {
        PdfPTable tableConceptos = new PdfPTable(6);
        tableConceptos.setWidths(new int[]{30, 35, 60, 180, 80, 80});
        tableConceptos.setTotalWidth(ANCHO_TOTAL_TABLA);
        tableConceptos.setWidthPercentage(PORCENTAJE_ANCHO_TABLA);
        tableConceptos.getDefaultCell().setBorder(Rectangle.NO_BORDER);
        tableConceptos.setLockedWidth(true);

        PdfPCell encabezadoConceptos = new PdfPCell();
        encabezadoConceptos.setColspan(6);
        encabezadoConceptos.setBorderColor(BaseColor.LIGHT_GRAY);
        encabezadoConceptos.setBorderWidthLeft(0.3f);
        encabezadoConceptos.setBorderWidthTop(0.3f);
        encabezadoConceptos.setBorderWidthRight(0.3f);
        encabezadoConceptos.setBorderWidthBottom(0f);

        for (Conceptos conceptosCargados : facturaDto.getConceptos()) {

            StringBuilder descConcepto = new StringBuilder();

            descConcepto.append("\n");
            descConcepto.append(hmapTagIdioma.get(EnumTagPlantilla.S_IMPOBJECT)).append(":  ").append(conceptosCargados.getObjetoImp());

            if (conceptosCargados.getImpuestos() != null && conceptosCargados.getImpuestos().getTraslados() != null && !conceptosCargados.getImpuestos().getTraslados().isEmpty()) {
                descConcepto.append(armarConceptoImpuestoTraslados(conceptosCargados));
            }
            if (conceptosCargados.getImpuestos() != null && conceptosCargados.getImpuestos().getRetenciones() != null && !conceptosCargados.getImpuestos().getRetenciones().isEmpty()) {
                descConcepto.append(armarConceptoImpuestoRetenciones(conceptosCargados));
            }

            if (conceptosCargados.getCuentaPredial() != null) {
                for(CFDI.Conceptos.CuentaPredial cuentaPredialCagados : conceptosCargados.getCuentaPredial()){
                    descConcepto.append(armarConceptoCuentaPredial(cuentaPredialCagados));
                }               
            }

            if (conceptosCargados.getInformacionAduanera() != null) {
                for(CFDI.Conceptos.InformacionAduanera informacionAduanera : conceptosCargados.getInformacionAduanera()){
                descConcepto.append(armarConceptoPedimentoAduana(informacionAduanera));
                }
            }

            if (conceptosCargados.getDescuento() != null && !conceptosCargados.getDescuento().equals(BigDecimal.ZERO)) {
                descConcepto.append(armarConceptoDescuento(conceptosCargados));
            }

            if (conceptosCargados.getACuentaTerceros()!= null) {
                descConcepto.append(armarConceptoCuentaTerceros(conceptosCargados));
            }

            PdfPCell claveProductosV = new PdfPCell(new Phrase(conceptosCargados.getClaveProdServ(), fontContenido));
            claveProductosV.setHorizontalAlignment(Element.ALIGN_CENTER);
            claveProductosV.setBorderWidthLeft(0.3f);
            claveProductosV.setBorderWidthTop(0f);
            claveProductosV.setBorderWidthRight(0.3f);
            claveProductosV.setBorderWidthBottom(0f);
            claveProductosV.setBorderColor(BaseColor.LIGHT_GRAY);

            PdfPCell cantidadProductosV = new PdfPCell(new Paragraph(new Phrase(String.valueOf(conceptosCargados.getCantidad() != null ? conceptosCargados.getCantidad() : 0), fontContenido)));
            cantidadProductosV.setHorizontalAlignment(Element.ALIGN_CENTER);
            cantidadProductosV.setBorderWidthLeft(0f);
            cantidadProductosV.setBorderWidthTop(0f);
            cantidadProductosV.setBorderWidthRight(0.3f);
            cantidadProductosV.setBorderWidthBottom(0f);
            cantidadProductosV.setBorderColor(BaseColor.LIGHT_GRAY);

            PdfPCell unidadProductosV = new PdfPCell(new Paragraph(new Phrase(conceptosCargados.getClaveUnidad(), fontContenido)));
            unidadProductosV.setHorizontalAlignment(Element.ALIGN_CENTER);
            unidadProductosV.setBorderWidthLeft(0f);
            unidadProductosV.setBorderWidthTop(0f);
            unidadProductosV.setBorderWidthRight(0.3f);
            unidadProductosV.setBorderWidthBottom(0f);
            unidadProductosV.setBorderColor(BaseColor.LIGHT_GRAY);

            PdfPCell descripcionProductoV = new PdfPCell(new Paragraph(new Phrase(conceptosCargados.getDescripcion().concat(descConcepto.toString()), fontContenido)));
            descripcionProductoV.setHorizontalAlignment(Element.ALIGN_CENTER);
            descripcionProductoV.setBorderWidthLeft(0f);
            descripcionProductoV.setBorderWidthTop(0f);
            descripcionProductoV.setBorderWidthRight(0.3f);
            descripcionProductoV.setBorderWidthBottom(0f);
            descripcionProductoV.setBorderColor(BaseColor.LIGHT_GRAY);

            PdfPCell precioUnitarioProductosV = new PdfPCell(new Paragraph(new Phrase(String.valueOf(conceptosCargados.getValorUnitario() != null ? conceptosCargados.getValorUnitario() : BigDecimal.ZERO), fontContenido)));
            precioUnitarioProductosV.setHorizontalAlignment(Element.ALIGN_CENTER);
            precioUnitarioProductosV.setBorderWidthLeft(0f);
            precioUnitarioProductosV.setBorderWidthTop(0f);
            precioUnitarioProductosV.setBorderWidthRight(0.3f);
            precioUnitarioProductosV.setBorderWidthBottom(0f);
            precioUnitarioProductosV.setBorderColor(BaseColor.LIGHT_GRAY);

            PdfPCell importeProductosV = new PdfPCell(new Paragraph(new Phrase(String.valueOf(conceptosCargados.getImporte() != null ? conceptosCargados.getImporte() : BigDecimal.ZERO), fontContenido)));
            importeProductosV.setHorizontalAlignment(Element.ALIGN_CENTER);
            importeProductosV.setBorderWidthLeft(0f);
            importeProductosV.setBorderWidthTop(0f);
            importeProductosV.setBorderWidthRight(0.3f);
            importeProductosV.setBorderWidthBottom(0f);
            importeProductosV.setBorderColor(BaseColor.LIGHT_GRAY);

            tableConceptos.addCell(claveProductosV);
            tableConceptos.addCell(cantidadProductosV);
            tableConceptos.addCell(unidadProductosV);
            tableConceptos.addCell(descripcionProductoV);
            tableConceptos.addCell(precioUnitarioProductosV);
            tableConceptos.addCell(importeProductosV);
        }

        PdfPCell encabezadoConceptosFinal = new PdfPCell();
        encabezadoConceptosFinal.setColspan(6);
        encabezadoConceptosFinal.setBorderColor(BaseColor.LIGHT_GRAY);
        encabezadoConceptosFinal.setBorderWidthLeft(0.3f);
        encabezadoConceptosFinal.setBorderWidthTop(0f);
        encabezadoConceptosFinal.setBorderWidthRight(0.3f);
        encabezadoConceptosFinal.setBorderWidthBottom(0.3f);
        tableConceptos.addCell(encabezadoConceptosFinal);

        document.add(tableConceptos);
    }

    public StringBuilder armarConceptoImpuestoTraslados(Conceptos conceptos) {
        StringBuilder descConcepto = new StringBuilder();
        try {
            for (Conceptos.Traslados impuestosCargados : conceptos.getImpuestos().getTraslados()) {
                descConcepto.append("\n");
                CatImpuestos obtenerImpuestoCatalogo = catalogosSatService.obtenerCatImpuestoSatByClave(impuestosCargados.getImpuesto());
                descConcepto.append("Impuesto: ").append((obtenerImpuestoCatalogo.getDescripcion() != null ? obtenerImpuestoCatalogo.getDescripcion() : ""))
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
    
    public void agregarComplementoPagos(Document document, CFDI facturaDto) throws DocumentException {
        PdfPTable tableDocumentoRelacionado = new PdfPTable(1);
        tableDocumentoRelacionado.setWidths(new int[]{ANCHO_TOTAL_TABLA});
        tableDocumentoRelacionado.setTotalWidth(ANCHO_TOTAL_TABLA);
        tableDocumentoRelacionado.setLockedWidth(true);
        tableDocumentoRelacionado.setWidthPercentage(PORCENTAJE_ANCHO_TABLA);
        tableDocumentoRelacionado.getDefaultCell().setBorder(Rectangle.NO_BORDER);

        if (facturaDto.getComplementos() != null && facturaDto.getComplementos().getComplementoPago20() != null) {
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

                            PdfPCell serieDocumentoRelacionadoC = new PdfPCell(new Paragraph("Serie : ".concat(documentosRelacionCargados.getSerie() != null ? documentosRelacionCargados.getSerie() : ""), fontContenido));
                            serieDocumentoRelacionadoC.setHorizontalAlignment(Element.ALIGN_LEFT);
                            serieDocumentoRelacionadoC.setBorder(Rectangle.NO_BORDER);

                            PdfPCell folioDocumentoRelacionadoC = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.C_FOLIO).concat(" : ").concat(documentosRelacionCargados.getFolio() != null ? documentosRelacionCargados.getFolio() : ""), fontContenido));
                            folioDocumentoRelacionadoC.setHorizontalAlignment(Element.ALIGN_LEFT);
                            folioDocumentoRelacionadoC.setBorder(Rectangle.NO_BORDER);

                            PdfPCell monedaDocumentoRelacionadoC = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.N_MONEDA).concat(" : ").concat(documentosRelacionCargados.getMonedaDR() != null ? documentosRelacionCargados.getMonedaDR() : ""), fontContenido));
                            monedaDocumentoRelacionadoC.setHorizontalAlignment(Element.ALIGN_LEFT);
                            monedaDocumentoRelacionadoC.setBorder(Rectangle.NO_BORDER);

                            PdfPCell tipoDeCambioDocumentoRelacionadoC = new PdfPCell(new Paragraph("Tipo de Cambio : ".concat(String.valueOf(documentosRelacionCargados.getEquivalenciaDR() != null ? documentosRelacionCargados.getEquivalenciaDR()  : "")), fontContenido));
                            tipoDeCambioDocumentoRelacionadoC.setHorizontalAlignment(Element.ALIGN_LEFT);
                            tipoDeCambioDocumentoRelacionadoC.setBorder(Rectangle.NO_BORDER);

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
                            tableDocumentoRelacionadoPago.addCell(numeroParcialidaDocumentoRelacionadoC);
                            tableDocumentoRelacionadoPago.addCell(importeSaldoAntDocumentoRelacionadoC);
                            tableDocumentoRelacionadoPago.addCell(importeSaldoPagadoDocumentoRelacionadoC);
                            tableDocumentoRelacionadoPago.addCell(importeSaldoInsoDocumentoRelacionadoC);
                           
                            StringBuilder objetoImpDR = new StringBuilder();
                            objetoImpDR.append(hmapTagIdioma.get(EnumTagPlantilla.S_IMPOBJECT).concat(": ").concat((documentosRelacionCargados.getObjetoImpDR() != null ? documentosRelacionCargados.getObjetoImpDR():  "")));
                            PdfPCell objetoImpDRV = new PdfPCell(new Paragraph( "" +  objetoImpDR, fontContenido));
                            objetoImpDRV.setHorizontalAlignment(Element.ALIGN_LEFT);
                            objetoImpDRV.setColspan(3);
                            objetoImpDRV.setBorder(Rectangle.NO_BORDER);
                            tableDocumentoRelacionadoPago.addCell(objetoImpDRV);

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
        }
        document.add(tableDocumentoRelacionado);
    }
    public void agregarTotales(Document document, CFDI facturaDto) throws DocumentException
    {
        if (facturaDto.getComplementos() != null && facturaDto.getComplementos().getComplementoPago20()!= null && facturaDto.getComplementos().getComplementoPago20().getTotales()  != null)
        {
                
            PdfPTable tableTotalesHeader = new PdfPTable(1);
            tableTotalesHeader.setWidths(new int[]{ANCHO_TOTAL_TABLA});
            tableTotalesHeader.setTotalWidth(ANCHO_TOTAL_TABLA);
            tableTotalesHeader.setLockedWidth(true);
            tableTotalesHeader.setWidthPercentage(PORCENTAJE_ANCHO_TABLA);
            tableTotalesHeader.getDefaultCell().setBorder(Rectangle.NO_BORDER);

            PdfPCell headerNodoTotales = new PdfPCell(new Paragraph("Totales Pagos ", fontEtiquetas));
            headerNodoTotales.setHorizontalAlignment(Element.ALIGN_RIGHT);
            headerNodoTotales.setBorder(Rectangle.NO_BORDER);

            tableTotalesHeader.addCell(headerNodoTotales);

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

            if (!Optional.ofNullable( facturaDto.getComplementos().getComplementoPago20().getTotales().getTotalTrasladosBaseIVAExento()).isEmpty())
            {
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
            

            if (!Optional.ofNullable( facturaDto.getComplementos().getComplementoPago20().getTotales().getTotalRetencionesIVA()).isEmpty())
            {
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
            document.add(tableTotalesHeader);
            document.add(tableTotales);
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
            for (Pago.ImpuestosP.RetencionesP.RetencionP   impuestosCargados :  conceptos.getImpuestosP().getRetencionesP().getRetencionP() ) { 
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

    public void agregarSubtotales(Document document, CFDI facturaDto) throws DocumentException {
        PdfPTable tableDatosSubtotales = new PdfPTable(3);
        tableDatosSubtotales.setWidths(new int[]{400, 70, 99});
        tableDatosSubtotales.setTotalWidth(ANCHO_TOTAL_TABLA);
        tableDatosSubtotales.setLockedWidth(true);
        tableDatosSubtotales.setWidthPercentage(PORCENTAJE_ANCHO_TABLA);
        tableDatosSubtotales.getDefaultCell().setBorder(Rectangle.NO_BORDER);

        PdfPCell importeConLetra = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.N_IMPORTELETRA).concat(" : ").concat(Utilities.crearTotalLetra(facturaDto)), fontContenido));
        importeConLetra.setHorizontalAlignment(Element.ALIGN_LEFT);
        importeConLetra.setBorder(Rectangle.NO_BORDER);
        tableDatosSubtotales.addCell(importeConLetra);

        PdfPCell subtotalC = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.T_SUBTOTAL), fontContenido));
        subtotalC.setHorizontalAlignment(Element.ALIGN_CENTER);
        subtotalC.setBorderWidthLeft(0.3f);
        subtotalC.setBorderWidthTop(0f);
        subtotalC.setBorderWidthRight(0.3f);
        subtotalC.setBorderWidthBottom(0.3f);
        subtotalC.setBorderColor(BaseColor.LIGHT_GRAY);
        tableDatosSubtotales.addCell(subtotalC);

        PdfPCell subtotalV = new PdfPCell(new Paragraph(String.valueOf(facturaDto.getSubTotal() != null ? facturaDto.getSubTotal() : BigDecimal.ZERO), fontContenido));
        subtotalV.setHorizontalAlignment(Element.ALIGN_CENTER);
        subtotalV.setBorderWidthLeft(0f);
        subtotalV.setBorderWidthTop(0f);
        subtotalV.setBorderWidthRight(0.3f);
        subtotalV.setBorderWidthBottom(0.3f);
        subtotalV.setBorderColor(BaseColor.LIGHT_GRAY);
        tableDatosSubtotales.addCell(subtotalV);

        PdfPCell vacia = new PdfPCell();
        vacia.setBorder(Rectangle.NO_BORDER);
        if (facturaDto.getDescuento() != null && !facturaDto.getDescuento().equals(BigDecimal.ZERO)) {
            tableDatosSubtotales.addCell(vacia);

            PdfPCell descuentoC = new PdfPCell(new Phrase(hmapTagIdioma.get(EnumTagPlantilla.T_DESCUENTO), fontContenido));
            descuentoC.setHorizontalAlignment(Element.ALIGN_CENTER);
            descuentoC.setBorderWidthLeft(0.3f);
            descuentoC.setBorderWidthTop(0f);
            descuentoC.setBorderWidthRight(0.3f);
            descuentoC.setBorderWidthBottom(0.3f);
            descuentoC.setBorderColor(BaseColor.LIGHT_GRAY);
            tableDatosSubtotales.addCell(descuentoC);

            PdfPCell descuentoV = new PdfPCell(new Phrase(String.valueOf(facturaDto.getDescuento()), fontContenido));
            descuentoV.setHorizontalAlignment(Element.ALIGN_CENTER);
            descuentoV.setBorderWidthLeft(0f);
            descuentoV.setBorderWidthTop(0f);
            descuentoV.setBorderWidthRight(0.3f);
            descuentoV.setBorderWidthBottom(0.3f);
            descuentoV.setBorderColor(BaseColor.LIGHT_GRAY);
            tableDatosSubtotales.addCell(descuentoV);
        }

        String sImpuesto = "";
        if (facturaDto.getImpuestos() != null && facturaDto.getImpuestos().getTraslado() != null && !facturaDto.getImpuestos().getTraslado().isEmpty()) {
            for (Traslado impuestosTrasladados : facturaDto.getImpuestos().getTraslado()) {
                CatImpuestos obtenerImpuestoCatalogo = catalogosSatService.obtenerCatImpuestoSatByClave(impuestosTrasladados.getImpuesto());
                sImpuesto = obtenerImpuestoCatalogo.getDescripcion() + "(" + String.valueOf(impuestosTrasladados.getTasaoCuota() != null ? impuestosTrasladados.getTasaoCuota().doubleValue() : BigDecimal.ZERO) + ")";

                PdfPCell celdaImpuestoT = new PdfPCell(new Paragraph(new Phrase(sImpuesto, fontContenido)));
                celdaImpuestoT.setHorizontalAlignment(Element.ALIGN_CENTER);
                celdaImpuestoT.setBorderWidthLeft(0.3f);
                celdaImpuestoT.setBorderWidthTop(0f);
                celdaImpuestoT.setBorderWidthRight(0.3f);
                celdaImpuestoT.setBorderWidthBottom(0.3f);
                celdaImpuestoT.setBorderColor(BaseColor.LIGHT_GRAY);

                PdfPCell celdaImpuesto = new PdfPCell(new Paragraph(new Phrase(String.valueOf(impuestosTrasladados.getImporte() != null ? impuestosTrasladados.getImporte() : BigDecimal.ZERO), fontContenido)));
                celdaImpuesto.setHorizontalAlignment(Element.ALIGN_CENTER);
                celdaImpuesto.setBorderWidthLeft(0f);
                celdaImpuesto.setBorderWidthTop(0f);
                celdaImpuesto.setBorderWidthRight(0.3f);
                celdaImpuesto.setBorderWidthBottom(0.3f);
                celdaImpuesto.setBorderColor(BaseColor.LIGHT_GRAY);

                tableDatosSubtotales.addCell(vacia);
                tableDatosSubtotales.addCell(celdaImpuestoT);
                tableDatosSubtotales.addCell(celdaImpuesto);
            }
        }
        String sImpuestoLocales = "";
        if (facturaDto.getImpuestosLocales() != null && facturaDto.getImpuestosLocales().getTrasladoLocales() != null && !facturaDto.getImpuestosLocales().getTrasladoLocales().isEmpty()) {
            for (TrasladoLocal imp : facturaDto.getImpuestosLocales().getTrasladoLocales()) {
                sImpuestoLocales = imp.getImpuesto() + "(" + imp.getTasa().doubleValue() + ")";

                PdfPCell celdaImpuestoT = new PdfPCell(new Paragraph(new Phrase(sImpuestoLocales, fontContenido)));
                celdaImpuestoT.setHorizontalAlignment(Element.ALIGN_CENTER);
                celdaImpuestoT.setBorderWidthLeft(0.3f);
                celdaImpuestoT.setBorderWidthTop(0f);
                celdaImpuestoT.setBorderWidthRight(0.3f);
                celdaImpuestoT.setBorderWidthBottom(0.3f);
                celdaImpuestoT.setBorderColor(BaseColor.LIGHT_GRAY);

                PdfPCell celdaImpuesto = new PdfPCell(new Paragraph(new Phrase(String.valueOf(imp.getImporte() != null ? imp.getImporte() : BigDecimal.ZERO), fontContenido)));
                celdaImpuesto.setHorizontalAlignment(Element.ALIGN_CENTER);
                celdaImpuesto.setBorderWidthLeft(0f);
                celdaImpuesto.setBorderWidthTop(0f);
                celdaImpuesto.setBorderWidthRight(0.3f);
                celdaImpuesto.setBorderWidthBottom(0.3f);
                celdaImpuesto.setBorderColor(BaseColor.LIGHT_GRAY);

                tableDatosSubtotales.addCell(vacia);
                tableDatosSubtotales.addCell(celdaImpuestoT);
                tableDatosSubtotales.addCell(celdaImpuesto);
            }
        }

        String tipoCambio = facturaDto.getTipoCambio() != null ? "Tipo de Cambio :"+String.valueOf(facturaDto.getTipoCambio()) : "";

        PdfPCell monedaTipoCambio = new PdfPCell(new Paragraph("Moneda : " + facturaDto.getMoneda() + " " + tipoCambio, fontContenido));
        monedaTipoCambio.setHorizontalAlignment(Element.ALIGN_LEFT);
        monedaTipoCambio.setBorder(Rectangle.NO_BORDER);

        PdfPCell totalFacturadoC = new PdfPCell(new Paragraph(new Phrase(hmapTagIdioma.get(EnumTagPlantilla.T_TOTALPAGAR), fontContenido)));
        totalFacturadoC.setHorizontalAlignment(Element.ALIGN_CENTER);
        totalFacturadoC.setBorderWidthLeft(0.3f);
        totalFacturadoC.setBorderWidthTop(0f);
        totalFacturadoC.setBorderWidthRight(0.3f);
        totalFacturadoC.setBorderWidthBottom(0.3f);
        totalFacturadoC.setBorderColor(BaseColor.LIGHT_GRAY);

        PdfPCell totalFacturadoV = new PdfPCell(new Paragraph(new Phrase(String.valueOf(facturaDto.getTotal() != null ? facturaDto.getTotal() : BigDecimal.ZERO), fontContenido)));
        totalFacturadoV.setHorizontalAlignment(Element.ALIGN_CENTER);
        totalFacturadoV.setBorderWidthLeft(0f);
        totalFacturadoV.setBorderWidthTop(0f);
        totalFacturadoV.setBorderWidthRight(0.3f);
        totalFacturadoV.setBorderWidthBottom(0.3f);
        totalFacturadoV.setBorderColor(BaseColor.LIGHT_GRAY);

        tableDatosSubtotales.addCell(monedaTipoCambio);
        tableDatosSubtotales.addCell(totalFacturadoC);
        tableDatosSubtotales.addCell(totalFacturadoV);

        document.add(tableDatosSubtotales);
    }

    public void agregarSellos(Document document, CFDI facturaDto) throws DocumentException {
        PdfPTable tableSellos = new PdfPTable(1);
        tableSellos.setWidths(new int[]{480});
        tableSellos.setTotalWidth(480);
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

        PdfPTable tableTimbrado = new PdfPTable(2);
        tableTimbrado.setWidths(new int[]{480, 80});//560
        tableTimbrado.setTotalWidth(560);
        tableTimbrado.setLockedWidth(true);
        tableTimbrado.setWidthPercentage(PORCENTAJE_ANCHO_TABLA);
        tableTimbrado.getDefaultCell().setBorder(Rectangle.NO_BORDER);

        PdfPCell condicionesPago = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.G_CONDICIONESPAGO).concat(": ").concat(facturaDto.getCondicionesDePago() != null ? facturaDto.getCondicionesDePago() : ""), fontEtiquetas));
        condicionesPago.setHorizontalAlignment(Element.ALIGN_LEFT);
        condicionesPago.setBorder(Rectangle.NO_BORDER);
        tableSellos.addCell(condicionesPago);

        PdfPCell metodoDePago = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.T_METODOPAGO).concat(": ").concat(facturaDto.getMetodoPago() != null ? facturaDto.getMetodoPago() : ""), fontEtiquetas));
        metodoDePago.setHorizontalAlignment(Element.ALIGN_LEFT);
        metodoDePago.setBorder(Rectangle.NO_BORDER);
        tableSellos.addCell(metodoDePago);

        String formaPagoDescripcion = "";
        if (facturaDto.getFormaPago() != null && !facturaDto.getFormaPago().isEmpty()) {
            formaPagoDescripcion = facturaDto.getFormaPago().trim();
        }
        PdfPCell formaDePago = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.T_FORMAPAGO).concat(": ").concat(String.valueOf(formaPagoDescripcion != null ? formaPagoDescripcion : "")), fontEtiquetas));
        formaDePago.setHorizontalAlignment(Element.ALIGN_LEFT);
        formaDePago.setBorder(Rectangle.NO_BORDER);
        tableSellos.addCell(formaDePago);

        PdfPCell exportacionC = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.E_EXPORTACION).concat(": ").concat(facturaDto.getExportacion() != null ? facturaDto.getExportacion() : ""), fontEtiquetas));
        exportacionC.setHorizontalAlignment(Element.ALIGN_LEFT);
        exportacionC.setBorder(Rectangle.NO_BORDER);
        tableSellos.addCell(exportacionC);

        PdfPTable tableCodigoQR = new PdfPTable(1);
        tableCodigoQR.setWidths(new int[]{80});
        tableCodigoQR.setTotalWidth(80);
        tableCodigoQR.setLockedWidth(true);
        tableCodigoQR.setWidthPercentage(PORCENTAJE_ANCHO_TABLA);
        tableCodigoQR.getDefaultCell().setBorder(Rectangle.NO_BORDER);
        tableCodigoQR.getDefaultCell().setBackgroundColor(colorBorderCell);

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
            tableCodigoQR.addCell(codeQrImage);
        } catch (Exception ex) {
            tableCodigoQR.addCell("");
            System.out.println("Error al generar el codigo QR " + ex.getMessage());
        }

        PdfPCell leyenda = new PdfPCell(new Paragraph("Este documento es una representación impresa de un CFDI. Efectos fiscales al pago.", fontContenido));
        leyenda.setHorizontalAlignment(Element.ALIGN_CENTER);
        leyenda.setBorder(Rectangle.NO_BORDER);

        tableTimbrado.addCell(tableSellos);
        tableTimbrado.addCell(tableCodigoQR);
        
        document.add(tableTimbrado);

        PdfPTable tableLeyenda = new PdfPTable(1);
        tableLeyenda.setWidths(new int[]{170});
        tableLeyenda.setTotalWidth(420);
        tableLeyenda.setLockedWidth(true);
        tableLeyenda.setWidthPercentage(PORCENTAJE_ANCHO_TABLA);
        tableLeyenda.getDefaultCell().setFixedHeight(ALTURA_FIJA_TABLA);
        tableLeyenda.getDefaultCell().setBorder(Rectangle.NO_BORDER);
        tableLeyenda.addCell(leyenda);

        document.add(tableLeyenda);
    }
   
    class RoundRectangle implements PdfPCellEvent {

        /**
         * Metodo que redondea los contornos de una celda
         *
         * @param cell
         * @param rect
         * @param canvas
         */
        @Override
        public void cellLayout(PdfPCell cell, Rectangle rect, PdfContentByte[] canvas) {

            PdfContentByte contentByte = canvas[PdfPTable.LINECANVAS];
            contentByte.setColorFill(colorBorderCell);
            contentByte.roundRectangle(rect.getLeft(), rect.getBottom(), rect.getWidth(), rect.getHeight(), 4);
            contentByte.fill();
        }
    }

    class WithoutRoundRectangle implements PdfPCellEvent {

        /**
         * Metodo que redondea los contornos de una celda
         *
         * @param cell
         * @param rect
         * @param canvas
         */
        @Override
        public void cellLayout(PdfPCell cell, Rectangle rect, PdfContentByte[] canvas) {

            PdfContentByte contentByte = canvas[PdfPTable.LINECANVAS];
            contentByte.setColorFill(colorBorderCell);
            contentByte.roundRectangle(rect.getLeft(), rect.getBottom() - 0.2f, rect.getWidth(), rect.getHeight(), 0);
            contentByte.fill();

        }
    }

    class RoundRectangleTable implements PdfPTableEvent {

        /**
         * Metodo que redondea los contornos de una celda
         *
         * @param cell
         * @param rect
         * @param canvas
         */
        @Override
        public void tableLayout(PdfPTable table, float[][] widths, float[] heights, int headerRows, int rowStart, PdfContentByte[] canvas) {
            int columns;
            Rectangle rect;
            int footer = widths.length - table.getFooterRows();
            int header = table.getHeaderRows() - table.getFooterRows() + 1;
            for (int row = header; row < footer; row += 2) {
                columns = widths[row].length - 1;
                rect = new Rectangle(widths[row][0], heights[row],
                        widths[row][columns], heights[row + 1]);
                rect.setBackgroundColor(colorBorderCell);
                rect.setBorder(Rectangle.NO_BORDER);
                canvas[PdfPTable.BASECANVAS].rectangle(rect);
            }
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
