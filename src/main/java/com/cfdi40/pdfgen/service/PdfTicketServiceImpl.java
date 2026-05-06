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
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BarcodeQRCode;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
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
import com.cfdi40.pdfgen.tenants.tenanta.cfdi.dto.CFDI.Complementos.Ine;
import com.cfdi40.pdfgen.tenants.tenanta.cfdi.dto.CFDI.Impuestos.Retencion;
import com.cfdi40.pdfgen.tenants.tenanta.cfdi.dto.CFDI.Impuestos.Traslado;
import com.cfdi40.pdfgen.tenants.tenanta.cfdi.dto.CFDI.ImpuestosLocales.TrasladoLocal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Service
public class PdfTicketServiceImpl implements PdfTicketService {

    @Autowired
    CatTagxidiomaRepository catTagxidiomaRepository; 
    @Autowired
    CatIdiomapdfRepository catIdiomapdfRepository;
    @org.springframework.beans.factory.annotation.Autowired
    CatalogStaticFallbackService catalogFallback;
    @Autowired
    CatalogosSATService catalogosSatService;
    
    Font arial = FontFactory.getFont("/Fuentes/arial.ttf",
            BaseFont.IDENTITY_H, BaseFont.EMBEDDED, 8.4f, Font.BOLD, BaseColor.BLACK);
    BaseFont baseFont = arial.getBaseFont();
    Font arialLeyenda = FontFactory.getFont("/Fuentes/arial.ttf",
            BaseFont.IDENTITY_H, BaseFont.EMBEDDED, 8.4f, Font.NORMAL, BaseColor.BLACK);
    BaseFont baseFontTotales = arialLeyenda.getBaseFont();
    Font arialEncabezadoConceptos = FontFactory.getFont("/Fuentes/arial.ttf",
            BaseFont.IDENTITY_H, BaseFont.EMBEDDED, 7.5f, Font.BOLD, BaseColor.BLACK);
    BaseFont baseFontEncabezadoConceptos = arialEncabezadoConceptos.getBaseFont();
    Font arialConceptos = FontFactory.getFont("/Fuentes/arial.ttf",
            BaseFont.IDENTITY_H, BaseFont.EMBEDDED, 7.8f, Font.BOLD, BaseColor.BLACK);
    BaseFont baseFontConceptos = arialConceptos.getBaseFont();
    Font arialSellos = FontFactory.getFont("/Fuentes/arial.ttf",
            BaseFont.IDENTITY_H, BaseFont.EMBEDDED, 7.4f, Font.NORMAL, BaseColor.BLACK);
    BaseFont baseFontSellos = arialSellos.getBaseFont();
    private List<CatTagxidioma> catTagxidioma ;
    private HashMap<EnumTagPlantilla, String> hmapTagIdioma;

    @Override
    public ByteArrayOutputStream generarPDfFactura(CFDI facturaDto, IdentificadorSucursal identificadorSucursal ) throws BusinessException {
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
            Rectangle ticket = new Rectangle((float) 267.87, (float) 859.9);
            Document document = new Document(ticket, 2.2f, 2.2f, 6.0f, 6.0f);
            pdfResponse = new ByteArrayOutputStream();
            PdfWriter writer = PdfWriter.getInstance(document, pdfResponse);
            document.open();

            agregarSucursal(document, identificadorSucursal,facturaDto);
            agregarDatosFactura(document, facturaDto, identificadorSucursal);
            agregarDatosCliente(document,facturaDto);
            agregarTableInformacionGlobal(document,facturaDto);
            agregarDatosAlimentos(document,facturaDto);
            agregarEncabezadosProductos(document);
            agregarProducto(document,facturaDto);
            agregarSubtotales(document,facturaDto);
            agregarSubtotales2(document,facturaDto);
            agregarMetodosDePago(document,facturaDto);
            agregaComplementoINE(document,facturaDto);
            agregarSellos(document,facturaDto);

            document.close();
            writer.flush();
            return pdfResponse;

        } catch (Exception e) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Ocurrio un error al tratar de generar el PDF de la plantilla Ticket de la factura con UUID "
                    + (facturaDto.getTimbrado() != null ? facturaDto.getTimbrado().getUuid() : "" ));
        }
    }

    /**
     * *
     * Metodo para agregar los datos de la sucursal nombre, calle, etc
     *
     * @param document
     * @throws DocumentException
     */
    public void agregarSucursal(Document document, IdentificadorSucursal identificadorSucursal, CFDI facturaDto) throws DocumentException {
        PdfPTable tableEncabezado = new PdfPTable(2);
        tableEncabezado.setWidths(new int[]{80, 80});
        tableEncabezado.setTotalWidth(215);
        tableEncabezado.setLockedWidth(true);
        tableEncabezado.getDefaultCell().setBorder(Rectangle.NO_BORDER);

        PdfPCell cellNombreSucursal = new PdfPCell(new Paragraph(facturaDto.getNombreHotel() != null ? facturaDto.getNombreHotel() : "", arial));
        cellNombreSucursal.setColspan(2);
        cellNombreSucursal.setHorizontalAlignment(Element.ALIGN_CENTER);
        cellNombreSucursal.setBorder(Rectangle.NO_BORDER);

        PdfPCell cellSucursal = new PdfPCell(new Paragraph((identificadorSucursal.getEmisorId().getNombre() != null ? identificadorSucursal.getEmisorId().getNombre() : ""), arial));
        cellSucursal.setPaddingTop(-2f);
        cellSucursal.setColspan(2);
        cellSucursal.setHorizontalAlignment(Element.ALIGN_LEFT);
        cellSucursal.setBorder(Rectangle.NO_BORDER);

        PdfPCell cellCalleSucursal = new PdfPCell(new Paragraph((identificadorSucursal.getEmisorId().getCalle() != null ? identificadorSucursal.getEmisorId().getCalle() : "").concat(" ")
                .concat(identificadorSucursal.getEmisorId().getNumeroInterior() != null ? identificadorSucursal.getEmisorId().getNumeroInterior() : "").concat(" ")
                .concat(identificadorSucursal.getEmisorId().getNumeroExterior() != null ? identificadorSucursal.getEmisorId().getNumeroExterior() : ""), arial));
        cellCalleSucursal.setPaddingTop(-2f);
        cellCalleSucursal.setColspan(2);
        cellCalleSucursal.setHorizontalAlignment(Element.ALIGN_LEFT);
        cellCalleSucursal.setBorder(Rectangle.NO_BORDER);

        PdfPCell cellEstado = new PdfPCell(new Paragraph((identificadorSucursal.getEmisorId().getColonia() != null ? identificadorSucursal.getEmisorId().getColonia() : "").concat(" ")
                .concat(identificadorSucursal.getEmisorId().getCiudadDelegacion() != null ? identificadorSucursal.getEmisorId().getCiudadDelegacion() : ""), arial));
        cellEstado.setPaddingTop(-2f);
        cellEstado.setColspan(2);
        cellEstado.setHorizontalAlignment(Element.ALIGN_LEFT);
        cellEstado.setBorder(Rectangle.NO_BORDER);

        PdfPCell cellPaisCodigoPostal = new PdfPCell(new Paragraph((identificadorSucursal.getEmisorId().getEstado() != null ? identificadorSucursal.getEmisorId().getEstado() : "")
                .concat(" ").concat(identificadorSucursal.getEmisorId().getPais() != null ? identificadorSucursal.getEmisorId().getPais() : "")
                .concat(" C.P ").concat(identificadorSucursal.getEmisorId().getCodigoPostal() != null ? identificadorSucursal.getEmisorId().getCodigoPostal() : ""), arial));
        cellPaisCodigoPostal.setPaddingTop(-2f);
        cellPaisCodigoPostal.setColspan(2);
        cellPaisCodigoPostal.setHorizontalAlignment(Element.ALIGN_LEFT);
        cellPaisCodigoPostal.setBorder(Rectangle.NO_BORDER);

        PdfPCell cellRFC = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.RFC).concat(identificadorSucursal.getEmisorId().getRfc() != null ? identificadorSucursal.getEmisorId().getRfc() : ""), arial));
        cellRFC.setPaddingTop(-2f);
        cellRFC.setHorizontalAlignment(Element.ALIGN_LEFT);
        cellRFC.setBorder(Rectangle.NO_BORDER);

        PdfPCell cellTelefono = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.SUCURSAL_TELEFONO).concat(" : ").concat((identificadorSucursal.getSucursalId().getTelefono() != null ? identificadorSucursal.getSucursalId().getTelefono() : "")), arial));
        cellTelefono.setPaddingTop(-2f);
        cellTelefono.setHorizontalAlignment(Element.ALIGN_LEFT);
        cellTelefono.setBorder(Rectangle.NO_BORDER);

        PdfPCell cellEmail = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.P_EMAIL).concat(" : ").concat((identificadorSucursal.getSucursalId() != null && identificadorSucursal.getSucursalId().getEmail() != null ? identificadorSucursal.getSucursalId().getEmail() : "")), arial));
        cellEmail.setPaddingTop(-2f);
        cellEmail.setColspan(2);
        cellEmail.setHorizontalAlignment(Element.ALIGN_LEFT);
        cellEmail.setBorder(Rectangle.NO_BORDER);

        PdfPCell cellExpedidoC = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.P_LUGAREXPEDICION).concat(" : "), arial));
        cellExpedidoC.setPaddingTop(2f);
        cellExpedidoC.setColspan(2);
        cellExpedidoC.setHorizontalAlignment(Element.ALIGN_LEFT);
        cellExpedidoC.setBorder(Rectangle.NO_BORDER);

        PdfPCell expedidoEnV = new PdfPCell(new Paragraph((identificadorSucursal.getSucursalId().getCalle() != null ? identificadorSucursal.getSucursalId().getCalle() : "").concat(" ")
                .concat(identificadorSucursal.getSucursalId().getNumeroInterior() != null ? identificadorSucursal.getSucursalId().getNumeroInterior() : "").concat(" ")
                .concat(identificadorSucursal.getSucursalId().getNumeroExterior() != null ? identificadorSucursal.getSucursalId().getNumeroExterior() : ""), arial));
        expedidoEnV.setHorizontalAlignment(Element.ALIGN_LEFT);
        expedidoEnV.setPaddingTop(-2f);
        expedidoEnV.setColspan(2);
        expedidoEnV.setBorder(Rectangle.NO_BORDER);

        PdfPCell estadoV = new PdfPCell(new Paragraph((identificadorSucursal.getSucursalId().getColonia() != null ? identificadorSucursal.getSucursalId().getColonia() : "").concat(" ")
                .concat(identificadorSucursal.getSucursalId().getCiudadDelegacion() != null ? identificadorSucursal.getSucursalId().getCiudadDelegacion() : ""), arial));
        estadoV.setHorizontalAlignment(Element.ALIGN_LEFT);
        estadoV.setPaddingTop(-2f);
        estadoV.setColspan(2);
        estadoV.setBorder(Rectangle.NO_BORDER);

        StringBuilder paisEmisor = new StringBuilder();
        paisEmisor.append(identificadorSucursal.getSucursalId().getEstado() != null ? identificadorSucursal.getSucursalId().getEstado() : "")
                .append(" ").append(identificadorSucursal.getSucursalId().getPais() != null ? identificadorSucursal.getSucursalId().getPais() : "")
                .append(" ").append(facturaDto.getLugarExpedicion() != null ? facturaDto.getLugarExpedicion() : "");
        PdfPCell paisV = new PdfPCell(new Paragraph(paisEmisor.toString(), arial));
        paisV.setHorizontalAlignment(Element.ALIGN_LEFT);
        paisV.setPaddingTop(-2f);
        paisV.setColspan(2);
        paisV.setBorder(Rectangle.NO_BORDER);

        PdfPCell residenciaFiscal = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.REG_FISCAL_RECEP).concat(facturaDto.getReceptor().getRegimenFiscal() != null ? facturaDto.getReceptor().getRegimenFiscal() : ""), arial));
        residenciaFiscal.setPaddingTop(-2f);
        residenciaFiscal.setHorizontalAlignment(Element.ALIGN_LEFT);
        residenciaFiscal.setBorder(Rectangle.NO_BORDER);

        tableEncabezado.addCell(cellNombreSucursal);
        tableEncabezado.addCell(cellSucursal);
        tableEncabezado.addCell(cellCalleSucursal);
        tableEncabezado.addCell(cellEstado);
        tableEncabezado.addCell(cellPaisCodigoPostal);
        tableEncabezado.addCell(cellRFC);
        tableEncabezado.addCell(cellTelefono);
        tableEncabezado.addCell(cellEmail);
        tableEncabezado.addCell(cellExpedidoC);
        tableEncabezado.addCell(expedidoEnV);
        tableEncabezado.addCell(estadoV);
        tableEncabezado.addCell(paisV);
        tableEncabezado.addCell(residenciaFiscal);
        document.add(tableEncabezado);
    }

    /**
     * *
     * Metodo para agregar los datos de la factura No.CertificadoSat, folio, etc
     *
     * @param document
     * @throws DocumentException
     */
    public void agregarDatosFactura(Document document, CFDI facturaDto, IdentificadorSucursal identificadorSucursal) throws DocumentException {
        PdfPTable tableHeaderFactura = new PdfPTable(2);
        tableHeaderFactura.setWidths(new int[]{85, 100});
        tableHeaderFactura.setTotalWidth(215);
        tableHeaderFactura.setLockedWidth(true);
        tableHeaderFactura.getDefaultCell().setBorder(Rectangle.NO_BORDER);

        PdfPCell cellHeader = new PdfPCell(new Paragraph(facturaDto.getNombreCentroConsumo() != null ? facturaDto.getNombreCentroConsumo() : "", arial));
        cellHeader.setColspan(2);
        cellHeader.setBackgroundColor(BaseColor.LIGHT_GRAY);
        cellHeader.setHorizontalAlignment(Element.ALIGN_CENTER);
        cellHeader.setBorder(Rectangle.NO_BORDER);

        String tipoDoc = "";
        if (facturaDto.getTipoCfdi().equals(CFDI.EnumTipoFactura.FACTURA)) {
            tipoDoc = hmapTagIdioma.get(EnumTagPlantilla.FACTURA);
        }
        if (facturaDto.getTipoCfdi().equals(CFDI.EnumTipoFactura.NOTA_CREDITO)) {
            tipoDoc = hmapTagIdioma.get(EnumTagPlantilla.NOTA_CREDITO);
        }

        PdfPCell cellTipoDocumento = new PdfPCell(new Paragraph(tipoDoc, arial));
        cellTipoDocumento.setColspan(2);
        cellTipoDocumento.setHorizontalAlignment(Element.ALIGN_CENTER);
        cellTipoDocumento.setBorder(Rectangle.NO_BORDER);

        PdfPCell cellNumeroCertificado = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.T_NUMCERTIFICADO).concat(" : "), arial));
        cellNumeroCertificado.setPaddingTop(-2f);
        cellNumeroCertificado.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cellNumeroCertificado.setBorder(Rectangle.NO_BORDER);

        PdfPCell numeroCertificado = new PdfPCell(new Paragraph(facturaDto.getNumeroCertificado() != null ? facturaDto.getNumeroCertificado() : "", arial));
        numeroCertificado.setPaddingTop(-2f);
        numeroCertificado.setHorizontalAlignment(Element.ALIGN_LEFT);
        numeroCertificado.setBorder(Rectangle.NO_BORDER);

        PdfPCell cellFechaEmision = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.E_FECHAEMISION) + ":", arial));
        cellFechaEmision.setPaddingTop(-2f);
        cellFechaEmision.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cellFechaEmision.setBorder(Rectangle.NO_BORDER);

        String lugarFechaEmision = "";
        if (facturaDto.getFecha() != null) {
            Date dateCheckIn = facturaDto.getFecha();
            DateFormat dateFormatCheckIn = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            lugarFechaEmision = dateFormatCheckIn.format(dateCheckIn);
        }

        PdfPCell fechaEmision = new PdfPCell(new Paragraph(lugarFechaEmision, arial));
        fechaEmision.setPaddingTop(-2f);
        fechaEmision.setColspan(2);
        fechaEmision.setHorizontalAlignment(Element.ALIGN_LEFT);
        fechaEmision.setBorder(Rectangle.NO_BORDER);

        PdfPCell cellNumeroDeCertificadoSAT = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.P_NOCERTIFICADOSAT).concat(" : "), arial));
        cellNumeroDeCertificadoSAT.setPaddingTop(-2f);
        cellNumeroDeCertificadoSAT.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cellNumeroDeCertificadoSAT.setBorder(Rectangle.NO_BORDER);

        PdfPCell numeroDeCertificadoSAT = new PdfPCell(new Paragraph(facturaDto.getTimbrado().getNumCertificadoSAT() != null ? facturaDto.getTimbrado().getNumCertificadoSAT() : "", arial));
        numeroDeCertificadoSAT.setPaddingTop(-2f);
        numeroDeCertificadoSAT.setHorizontalAlignment(Element.ALIGN_LEFT);
        numeroDeCertificadoSAT.setBorder(Rectangle.NO_BORDER);

        PdfPCell cellFolioUUID = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.P_FOLIOUUID).concat(" : "), arial));
        cellFolioUUID.setPaddingTop(-2f);
        cellFolioUUID.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cellFolioUUID.setBorder(Rectangle.NO_BORDER);

        PdfPCell folioUUID = new PdfPCell(new Paragraph(facturaDto.getTimbrado().getUuid() != null ? facturaDto.getTimbrado().getUuid().toLowerCase() : "", arial));
        folioUUID.setPaddingTop(-2f);
        folioUUID.setHorizontalAlignment(Element.ALIGN_LEFT);
        folioUUID.setBorder(Rectangle.NO_BORDER);

        PdfPCell cellFechaDeCertificacion = new PdfPCell(new Paragraph("Fecha de Certificación : ", arial));
        cellFechaDeCertificacion.setPaddingTop(-2f);
        cellFechaDeCertificacion.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cellFechaDeCertificacion.setBorder(Rectangle.NO_BORDER);

        String lugarFechaTimbrado = "";
        if (facturaDto.getTimbrado().getFechaTimbrado() != null) {
            Date dateCheckIn = facturaDto.getTimbrado().getFechaTimbrado();
            DateFormat dateFormatCheckIn = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            lugarFechaTimbrado = dateFormatCheckIn.format(dateCheckIn);
        }

        PdfPCell fechaDeCertificacion = new PdfPCell(new Paragraph(lugarFechaTimbrado, arial));
        fechaDeCertificacion.setPaddingTop(-2f);
        fechaDeCertificacion.setHorizontalAlignment(Element.ALIGN_LEFT);
        fechaDeCertificacion.setBorder(Rectangle.NO_BORDER);

        PdfPCell folioXML = new PdfPCell(new Paragraph((identificadorSucursal.getEmisorId().getRegimenFiscal() != null ? identificadorSucursal.getEmisorId().getRegimenFiscal() : "").concat("                             ").concat("Folio (XML)").concat((facturaDto.getFolio() != null ? facturaDto.getFolio() : "")), arial));
        folioXML.setColspan(2);
        folioXML.setPaddingTop(-2f);
        folioXML.setHorizontalAlignment(Element.ALIGN_CENTER);
        folioXML.setBorder(Rectangle.NO_BORDER);


        PdfPCell cellExportacion = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.E_EXPORTACION).concat(" : "), arial));
        cellExportacion.setPaddingTop(-2f);
        cellExportacion.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cellExportacion.setBorder(Rectangle.NO_BORDER);

        PdfPCell exportacion = new PdfPCell(new Paragraph(facturaDto.getExportacion() != null ? facturaDto.getExportacion() : "", arial));
        exportacion.setColspan(2);
        exportacion.setPaddingTop(-2f);
        exportacion.setHorizontalAlignment(Element.ALIGN_LEFT);
        exportacion.setBorder(Rectangle.NO_BORDER);

        tableHeaderFactura.addCell(cellHeader);
        tableHeaderFactura.addCell(cellTipoDocumento);
        tableHeaderFactura.addCell(cellNumeroCertificado);
        tableHeaderFactura.addCell(numeroCertificado);
        tableHeaderFactura.addCell(cellFechaEmision);
        tableHeaderFactura.addCell(fechaEmision);
        tableHeaderFactura.addCell(cellNumeroDeCertificadoSAT);
        tableHeaderFactura.addCell(numeroDeCertificadoSAT);
        tableHeaderFactura.addCell(cellFolioUUID);
        tableHeaderFactura.addCell(folioUUID);
        tableHeaderFactura.addCell(cellFechaDeCertificacion);
        tableHeaderFactura.addCell(fechaDeCertificacion);
        tableHeaderFactura.addCell(cellExportacion);
        tableHeaderFactura.addCell(exportacion);

        if (facturaDto.getCfdiRelacionados() != null) {

            for (Relacionados cfdiRelacionadosCargados : facturaDto.getCfdiRelacionados()) {
                     
                PdfPCell cellTipoRelacion = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.G_TIPORELACION), arial));
                cellTipoRelacion.setPaddingTop(-2f);
                cellTipoRelacion.setHorizontalAlignment(Element.ALIGN_RIGHT);
                cellTipoRelacion.setBorder(Rectangle.NO_BORDER);

                PdfPCell tipoCfdiRelacion = new PdfPCell(new Paragraph(cfdiRelacionadosCargados.getTipoRelacion(), arial));
                tipoCfdiRelacion.setPaddingTop(-2f);
                tipoCfdiRelacion.setHorizontalAlignment(Element.ALIGN_LEFT);
                tipoCfdiRelacion.setBorder(Rectangle.NO_BORDER);

                tableHeaderFactura.addCell(cellTipoRelacion);
                tableHeaderFactura.addCell(tipoCfdiRelacion);

                PdfPCell cellRelacion = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.G_UUIDRELACIONADO), arial));
                cellRelacion.setPaddingTop(-2f);
                cellRelacion.setHorizontalAlignment(Element.ALIGN_RIGHT);
                cellRelacion.setBorder(Rectangle.NO_BORDER);

                PdfPCell cfdiRelacion = new PdfPCell(new Paragraph(! cfdiRelacionadosCargados.getUuid().isEmpty()
                        ? String.join(",", cfdiRelacionadosCargados.getUuid()) : "", arial));
                cfdiRelacion.setPaddingTop(-2f);
                cfdiRelacion.setHorizontalAlignment(Element.ALIGN_LEFT);
                cfdiRelacion.setBorder(Rectangle.NO_BORDER);

                tableHeaderFactura.addCell(cellRelacion);
                tableHeaderFactura.addCell(cfdiRelacion);
            }
        }

        tableHeaderFactura.addCell(folioXML);

        document.add(tableHeaderFactura);
    }

    /**
     * *
     * Metodo para agregar los datos del cliente RFC, Nombre, colonia,
     * municipio, etc
     *
     * @param document
     * @throws DocumentException
     */
    public void agregarDatosCliente(Document document, CFDI facturaDto) throws DocumentException {
        PdfPTable tableDatosCliente = new PdfPTable(1);
        tableDatosCliente.setWidths(new int[]{100});
        tableDatosCliente.setTotalWidth(215);
        tableDatosCliente.setLockedWidth(true);
        tableDatosCliente.getDefaultCell().setBorder(Rectangle.NO_BORDER);

        PdfPCell cellHeader = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.P_DATOSDELCLIENTE), arial));
        cellHeader.setBackgroundColor(BaseColor.LIGHT_GRAY);
        cellHeader.setHorizontalAlignment(Element.ALIGN_CENTER);
        cellHeader.setBorder(Rectangle.NO_BORDER);

        PdfPCell cellRFC = new PdfPCell(new Paragraph(facturaDto.getReceptor().getRfc() != null ? facturaDto.getReceptor().getRfc() : "", arial));
        cellRFC.setHorizontalAlignment(Element.ALIGN_LEFT);
        cellRFC.setBorder(Rectangle.NO_BORDER);

        PdfPCell cellNombre = new PdfPCell(new Paragraph(facturaDto.getReceptor().getNombre() != null ? facturaDto.getReceptor().getNombre() : "", arial));
        cellNombre.setHorizontalAlignment(Element.ALIGN_LEFT);
        cellNombre.setPaddingTop(-1f);
        cellNombre.setBorder(Rectangle.NO_BORDER);

        PdfPCell cellUsoCfdi = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.G_USOCFDI).concat(" : ").concat(facturaDto.getReceptor().getUsoCFDI() != null ? facturaDto.getReceptor().getUsoCFDI() : ""), arial));
        cellUsoCfdi.setHorizontalAlignment(Element.ALIGN_LEFT);
        cellUsoCfdi.setPaddingTop(-1f);
        cellUsoCfdi.setBorder(Rectangle.NO_BORDER);

        PdfPCell cellRegFiscReceptor = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.REG_FISCAL_RECEP).concat(" : ").concat(facturaDto.getReceptor().getRegimenFiscal() != null ? facturaDto.getReceptor().getRegimenFiscal() : ""), arial));
        cellRegFiscReceptor.setHorizontalAlignment(Element.ALIGN_LEFT);
        cellRegFiscReceptor.setPaddingTop(-1f);
        cellRegFiscReceptor.setBorder(Rectangle.NO_BORDER);

        PdfPCell cellDomicReceptor = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.R_DOMFISCRECEP).concat(" : ").concat(facturaDto.getReceptor().getDomicilioFiscal() != null ? facturaDto.getReceptor().getDomicilioFiscal() : ""), arial));
        cellDomicReceptor.setHorizontalAlignment(Element.ALIGN_LEFT);
        cellDomicReceptor.setPaddingTop(-1f);
        cellDomicReceptor.setBorder(Rectangle.NO_BORDER);

        tableDatosCliente.addCell(cellHeader);
        tableDatosCliente.addCell(cellRFC);
        tableDatosCliente.addCell(cellNombre);
        tableDatosCliente.addCell(cellUsoCfdi);
        tableDatosCliente.addCell(cellRegFiscReceptor);
        tableDatosCliente.addCell(cellDomicReceptor);
        document.add(tableDatosCliente);
    }

    /**
     * *
     * Metodo para agregar los datos del cliente tipo opera referencia, fecha,
     * folio
     *
     * @param document
     * @throws DocumentException
     */
    public void agregarDatosAlimentos(Document document, CFDI facturaDto) throws DocumentException {
        PdfPTable tableDatosClienteOpera = new PdfPTable(3);
        tableDatosClienteOpera.setWidths(new int[]{180, 180, 140});
        tableDatosClienteOpera.setTotalWidth(215);
        tableDatosClienteOpera.setLockedWidth(true);
        tableDatosClienteOpera.setWidthPercentage(100);
        tableDatosClienteOpera.getDefaultCell().setFixedHeight(90f);
        tableDatosClienteOpera.getDefaultCell().setBorder(Rectangle.NO_BORDER);

        PdfPCell cellEspace = new PdfPCell();
        cellEspace.setColspan(3);
        cellEspace.setFixedHeight(1.5f);
        cellEspace.setBackgroundColor(BaseColor.LIGHT_GRAY);
        cellEspace.setHorizontalAlignment(Element.ALIGN_LEFT);
        cellEspace.setBorder(Rectangle.NO_BORDER);

        PdfPCell fechaChequeLabel = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.T_FECHACHEQUE),arial));
        fechaChequeLabel.setHorizontalAlignment(Element.ALIGN_LEFT);
        fechaChequeLabel.setBorder(Rectangle.NO_BORDER);

        PdfPCell chequeLabel = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.T_CHEQUE),arial));
        chequeLabel.setHorizontalAlignment(Element.ALIGN_LEFT);
        chequeLabel.setBorder(Rectangle.NO_BORDER);

        PdfPCell referenciaLabel = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.T_REFENCIA),arial));
        referenciaLabel.setHorizontalAlignment(Element.ALIGN_LEFT);
        referenciaLabel.setBorder(Rectangle.NO_BORDER);

        tableDatosClienteOpera.addCell(fechaChequeLabel);
        tableDatosClienteOpera.addCell(chequeLabel);
        tableDatosClienteOpera.addCell(referenciaLabel);


        String fechaCheque = "";
        if (facturaDto.getAlimentos() != null && facturaDto.getAlimentos().getFechaCheque() != null) {
            Date dateCheckIn = facturaDto.getAlimentos().getFechaCheque();
            DateFormat dateFormatCheckIn = new java.text.SimpleDateFormat("yyyy-MM-dd");
            fechaCheque = dateFormatCheckIn.format(dateCheckIn);
        }

        PdfPCell cellFechaChequeo = new PdfPCell(new Paragraph(fechaCheque, arial));
        cellFechaChequeo.setHorizontalAlignment(Element.ALIGN_LEFT);
        cellFechaChequeo.setBorder(Rectangle.NO_BORDER);

        PdfPCell cellCheque = new PdfPCell(new Paragraph(facturaDto.getAlimentos() != null && facturaDto.getAlimentos().getCheque() != null ? facturaDto.getAlimentos().getCheque() : "", arial));
        cellCheque.setHorizontalAlignment(Element.ALIGN_LEFT);
        cellCheque.setBorder(Rectangle.NO_BORDER);

        PdfPCell cellReferencia = new PdfPCell(new Paragraph(facturaDto.getAlimentos() != null && facturaDto.getAlimentos().getReferencia() != null ? facturaDto.getAlimentos().getReferencia() : "", arial));
        cellReferencia.setHorizontalAlignment(Element.ALIGN_LEFT);
        cellReferencia.setBorder(Rectangle.NO_BORDER);

//        tableDatosClienteOpera.addCell(cellEspace);
        tableDatosClienteOpera.addCell(cellFechaChequeo);
        tableDatosClienteOpera.addCell(cellCheque);
        tableDatosClienteOpera.addCell(cellReferencia);
        document.add(tableDatosClienteOpera);
    }

    /**
     * *
     * Metodo para agregar los encabezados de la tabla productos
     *
     * @param document
     * @throws DocumentException
     */
    public void agregarEncabezadosProductos(Document document) throws DocumentException {
        PdfPTable tableHeaderProducto = new PdfPTable(5);
        tableHeaderProducto.setWidths(new int[]{15, 20, 34, 20, 20});
        tableHeaderProducto.setTotalWidth(215);
        tableHeaderProducto.setLockedWidth(true);
        tableHeaderProducto.getDefaultCell().setBorder(Rectangle.NO_BORDER);

        PdfPCell cellDatosCliente = new PdfPCell(new Phrase(hmapTagIdioma.get(EnumTagPlantilla.P_PRODUCTOS), arial));
        cellDatosCliente.setColspan(5);
        cellDatosCliente.setBackgroundColor(BaseColor.LIGHT_GRAY);
        cellDatosCliente.setHorizontalAlignment(Element.ALIGN_CENTER);
        cellDatosCliente.setBorder(Rectangle.NO_BORDER);

        PdfPCell cellCantidad = new PdfPCell(new Phrase(hmapTagIdioma.get(EnumTagPlantilla.T_CANT), arialEncabezadoConceptos));
        cellCantidad.setHorizontalAlignment(Element.ALIGN_LEFT);
        cellCantidad.setBorder(Rectangle.NO_BORDER);

        PdfPCell cellUnidad = new PdfPCell(new Phrase(hmapTagIdioma.get(EnumTagPlantilla.CON_UNIDAD), arialEncabezadoConceptos));
        cellUnidad.setHorizontalAlignment(Element.ALIGN_LEFT);
        cellUnidad.setBorder(Rectangle.NO_BORDER);

        PdfPCell cellDescripcion = new PdfPCell(new Phrase(hmapTagIdioma.get(EnumTagPlantilla.CON_DESCRIPCION), arialEncabezadoConceptos));
        cellDescripcion.setHorizontalAlignment(Element.ALIGN_LEFT);
        cellDescripcion.setBorder(Rectangle.NO_BORDER);

        PdfPCell cellPrecioUnitario = new PdfPCell(new Phrase(hmapTagIdioma.get(EnumTagPlantilla.T_PRE), arialEncabezadoConceptos));
        cellPrecioUnitario.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cellPrecioUnitario.setBorder(Rectangle.NO_BORDER);

        PdfPCell cellImporte = new PdfPCell(new Phrase(hmapTagIdioma.get(EnumTagPlantilla.CON_IMPORTE), arialEncabezadoConceptos));
        cellImporte.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cellImporte.setBorder(Rectangle.NO_BORDER);

        tableHeaderProducto.addCell(cellDatosCliente);
        tableHeaderProducto.addCell(cellCantidad);
        tableHeaderProducto.addCell(cellUnidad);
        tableHeaderProducto.addCell(cellDescripcion);
        tableHeaderProducto.addCell(cellPrecioUnitario);
        tableHeaderProducto.addCell(cellImporte);
        document.add(tableHeaderProducto);
    }

    /**
     * *
     * Metodo para agregar los productos al ticket
     *
     * @param document
     * @throws DocumentException
     */
    public void agregarProducto(Document document, CFDI facturaDto) throws DocumentException {
        StringBuilder descConcepto;

        PdfPTable tableLlenadoConceptos = new PdfPTable(5);
        tableLlenadoConceptos.setWidths(new int[]{15, 20, 34, 20, 20});
        tableLlenadoConceptos.setTotalWidth(215);
        tableLlenadoConceptos.setLockedWidth(true);
        tableLlenadoConceptos.getDefaultCell().setBorder(Rectangle.NO_BORDER);

        for (Conceptos conceptos : facturaDto.getConceptos()) {

            descConcepto = new StringBuilder();

            descConcepto.append("\n");
            descConcepto.append("Objeto Imp:  ").append(conceptos.getObjetoImp());

            if (conceptos.getImpuestos() != null && !conceptos.getImpuestos().getTraslados().isEmpty()) {
                descConcepto.append(armarConceptoImpuestoTraslados(conceptos));
            }

            PdfPCell celdaDescripcion = new PdfPCell(new Paragraph(new Phrase(conceptos.getDescripcion().concat(descConcepto.toString()), arialConceptos)));
            celdaDescripcion.setHorizontalAlignment(Element.ALIGN_LEFT);
            celdaDescripcion.setBorder(Rectangle.NO_BORDER);

            PdfPCell celdaUnidad = new PdfPCell(new Paragraph(new Phrase(conceptos.getClaveUnidad(), arialConceptos)));
            celdaUnidad.setHorizontalAlignment(Element.ALIGN_LEFT);
            celdaUnidad.setBorder(Rectangle.NO_BORDER);

            PdfPCell celdaCantidad = new PdfPCell(new Paragraph(new Phrase(conceptos.getCantidad().toString(), arialConceptos)));
            celdaCantidad.setHorizontalAlignment(Element.ALIGN_LEFT);
            celdaCantidad.setBorder(Rectangle.NO_BORDER);

            PdfPCell celdaPUnitario = new PdfPCell(new Paragraph(new Phrase(String.valueOf(conceptos.getValorUnitario() != null ? conceptos.getValorUnitario() : ""), arialConceptos)));
            celdaPUnitario.setHorizontalAlignment(Element.ALIGN_RIGHT);
            celdaPUnitario.setBorder(Rectangle.NO_BORDER);

            PdfPCell celdaImporte = new PdfPCell(new Paragraph(new Phrase(String.valueOf(conceptos.getImporte() != null ? conceptos.getImporte() : ""), arialConceptos)));
            celdaImporte.setHorizontalAlignment(Element.ALIGN_RIGHT);
            celdaImporte.setBorder(Rectangle.NO_BORDER);

            tableLlenadoConceptos.addCell(celdaCantidad);
            tableLlenadoConceptos.addCell(celdaUnidad);
            tableLlenadoConceptos.addCell(celdaDescripcion);
            tableLlenadoConceptos.addCell(celdaPUnitario);
            tableLlenadoConceptos.addCell(celdaImporte);
        }

        document.add(tableLlenadoConceptos);
    }

    public StringBuilder armarConceptoImpuestoTraslados(Conceptos conceptos) {
        StringBuilder descConcepto = new StringBuilder();
        try {
            for (Conceptos.Traslados impuestosCargados : conceptos.getImpuestos().getTraslados()) {
                descConcepto.append("\n");
                CatImpuestos obtenerCatImpuestosSatByClave = catalogosSatService.obtenerCatImpuestoSatByClave(impuestosCargados.getImpuesto());
                descConcepto.append("Impuesto:  ").append((obtenerCatImpuestosSatByClave.getDescripcion() != null ? obtenerCatImpuestosSatByClave.getDescripcion() : ""))
                        .append("  Tasa/Cuota: ").append((String.valueOf(impuestosCargados.getTasaoCuota() != null ? impuestosCargados.getTasaoCuota() : "")))
                        .append("  Tipo Factor: ").append((impuestosCargados.getTipoFactor() != null ? impuestosCargados.getTipoFactor() : ""))
                        .append("  Importe $").append((String.valueOf(impuestosCargados.getImporte() != null ? impuestosCargados.getImporte() : ""))).append("\n");

            }
            if (conceptos.getImpuestos().getRetenciones() != null) {
                for (Conceptos.Retenciones retenidos : conceptos.getImpuestos().getRetenciones()) {
                    descConcepto.append("Retenciones\n");
                    CatImpuestos obtenerCatImpuestosSatByClave = catalogosSatService.obtenerCatImpuestoSatByClave(retenidos.getImpuesto());
                    descConcepto.append("Impuesto:  ").append((obtenerCatImpuestosSatByClave.getDescripcion() != null ? obtenerCatImpuestosSatByClave.getDescripcion() : ""))
                            .append("  Tasa/Cuota: ").append((String.valueOf(retenidos.getTasaoCuota() != null ? retenidos.getTasaoCuota() : "")))
                            .append("  Tipo Factor: ").append((retenidos.getTipoFactor() != null ? retenidos.getTipoFactor() : ""))
                            .append("  Importe $").append((String.valueOf(retenidos.getImporte() != null ? retenidos.getImporte() : ""))).append("\n");

                }
            }
            descConcepto.append("Clave Producto: ").append(conceptos.getClaveProdServ() != null ? conceptos.getClaveProdServ() : "");
            return descConcepto;
        } catch (Exception e) {
            System.out.println("Ocurrio un error al tratar de armar el concepto con impuestos trasladado" + e.getMessage());
            return new StringBuilder("");
        }
    }

    /**
     * *
     * Metodo para agregar los impuestos, totales y leyenda de formas de pago
     *
     * @param document
     * @throws DocumentException
     */
    public void agregarSubtotales(Document document, CFDI facturaDto) throws DocumentException {
        PdfPTable tableSubtotales = new PdfPTable(3);
        tableSubtotales.setWidths(new int[]{140, 200, 100});
        tableSubtotales.setTotalWidth(215);
        tableSubtotales.setLockedWidth(true);
        tableSubtotales.getDefaultCell().setBorder(Rectangle.NO_BORDER);

        PdfPCell cellTotalLetra = new PdfPCell(new Phrase("*** " + (Utilities.crearTotalLetra(facturaDto)) + " ***", arial));
        cellTotalLetra.setColspan(3);
        cellTotalLetra.setHorizontalAlignment(Element.ALIGN_LEFT);
        cellTotalLetra.setBorder(Rectangle.NO_BORDER);

        PdfPCell cellSubtotal = new PdfPCell(new Phrase(hmapTagIdioma.get(EnumTagPlantilla.T_SUBTOTAL), arial));
        cellSubtotal.setColspan(2);
        cellSubtotal.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cellSubtotal.setBorder(Rectangle.NO_BORDER);

        PdfPCell subtotal = new PdfPCell(new Phrase(String.valueOf(facturaDto.getSubTotal() != null ? facturaDto.getSubTotal() : ""), arial));
        subtotal.setHorizontalAlignment(Element.ALIGN_RIGHT);
        subtotal.setBorder(Rectangle.NO_BORDER);

        tableSubtotales.addCell(cellTotalLetra);
        tableSubtotales.addCell(cellSubtotal);
        tableSubtotales.addCell(subtotal);

        String sImpuesto = "";
        if (facturaDto.getImpuestos() != null && !facturaDto.getImpuestos().getTraslado().isEmpty()) {
            for (Traslado impuestosTrasladados : facturaDto.getImpuestos().getTraslado()) {
                CatImpuestos obtenerImpuestoCatalogo = catalogosSatService.obtenerCatImpuestoSatByClave(impuestosTrasladados.getImpuesto());
                sImpuesto = obtenerImpuestoCatalogo.getDescripcion() + "(" + impuestosTrasladados.getTasaoCuota().doubleValue() + ")";

                PdfPCell celdaImpuestoT = new PdfPCell(new Paragraph(new Phrase(sImpuesto, arial)));
                celdaImpuestoT.setColspan(2);
                celdaImpuestoT.setHorizontalAlignment(Element.ALIGN_RIGHT);
                celdaImpuestoT.setBorder(Rectangle.NO_BORDER);

                PdfPCell celdaImpuesto = new PdfPCell(new Paragraph(new Phrase(String.valueOf(impuestosTrasladados.getImporte() != null ? impuestosTrasladados.getImporte() : ""), arial)));
                celdaImpuesto.setHorizontalAlignment(Element.ALIGN_RIGHT);
                celdaImpuesto.setBorder(Rectangle.NO_BORDER);

                tableSubtotales.addCell(celdaImpuestoT);
                tableSubtotales.addCell(celdaImpuesto);
            }
        }
        String sImpuestoRetenido = "";
        if (facturaDto.getImpuestos() != null && facturaDto.getImpuestos().getRetenciones() != null && !facturaDto.getImpuestos().getRetenciones().isEmpty()) {
            for (Retencion impuestoRetenido : facturaDto.getImpuestos().getRetenciones()) {
                CatImpuestos obtenerImpuestoCatalogo = catalogosSatService.obtenerCatImpuestoSatByClave(impuestoRetenido.getImpuesto());
                sImpuestoRetenido = obtenerImpuestoCatalogo.getDescripcion() + "(Retenido)";

                PdfPCell celdaImpuestoT = new PdfPCell(new Paragraph(new Phrase(sImpuestoRetenido, arial)));
                celdaImpuestoT.setColspan(2);
                celdaImpuestoT.setHorizontalAlignment(Element.ALIGN_RIGHT);
                celdaImpuestoT.setBorder(Rectangle.NO_BORDER);

                PdfPCell celdaImpuesto = new PdfPCell(new Paragraph(new Phrase(String.valueOf(impuestoRetenido.getImporte() != null ? impuestoRetenido.getImporte() : ""), arial)));
                celdaImpuesto.setHorizontalAlignment(Element.ALIGN_RIGHT);
                celdaImpuesto.setBorder(Rectangle.NO_BORDER);

                tableSubtotales.addCell(celdaImpuestoT);
                tableSubtotales.addCell(celdaImpuesto);
            }
        }

        String sImpuestoLocales = "";
        if (facturaDto.getImpuestosLocales() != null && facturaDto.getImpuestosLocales().getTrasladoLocales() != null && !facturaDto.getImpuestosLocales().getTrasladoLocales().isEmpty()) {
            for (TrasladoLocal imp : facturaDto.getImpuestosLocales().getTrasladoLocales()) {
                sImpuestoLocales = imp.getImpuesto() + "(" + imp.getTasa().doubleValue() + ")";

                PdfPCell celdaImpuestoT = new PdfPCell(new Paragraph(new Phrase(sImpuestoLocales, arial)));
                celdaImpuestoT.setColspan(2);
                celdaImpuestoT.setHorizontalAlignment(Element.ALIGN_RIGHT);
                celdaImpuestoT.setBorder(Rectangle.NO_BORDER);

                PdfPCell celdaImpuesto = new PdfPCell(new Paragraph(new Phrase(String.valueOf(imp.getImporte() != null ? imp.getImporte() : ""), arial)));
                celdaImpuesto.setHorizontalAlignment(Element.ALIGN_RIGHT);
                celdaImpuesto.setBorder(Rectangle.NO_BORDER);

                tableSubtotales.addCell(celdaImpuestoT);
                tableSubtotales.addCell(celdaImpuesto);
            }
        }

        if (!facturaDto.getTotal().equals(BigDecimal.ZERO)) {
            PdfPCell celdaTotalFactTittle = new PdfPCell(new Paragraph(new Phrase(hmapTagIdioma.get(EnumTagPlantilla.T_TOTALFACT), arial)));
            celdaTotalFactTittle.setColspan(2);
            celdaTotalFactTittle.setPaddingTop(-2f);
            celdaTotalFactTittle.setHorizontalAlignment(Element.ALIGN_RIGHT);
            celdaTotalFactTittle.setBorder(Rectangle.NO_BORDER);

            PdfPCell celdaTotalFact = new PdfPCell(new Paragraph(new Phrase(String.valueOf(facturaDto.getTotal() != null ? facturaDto.getTotal() : ""), arial)));
            celdaTotalFact.setPaddingTop(-2f);
            celdaTotalFact.setHorizontalAlignment(Element.ALIGN_RIGHT);
            celdaTotalFact.setBorder(Rectangle.NO_BORDER);

            tableSubtotales.addCell(celdaTotalFactTittle);
            tableSubtotales.addCell(celdaTotalFact);
        }

        PdfPCell cellExhibicion = new PdfPCell(new Phrase(hmapTagIdioma.get(EnumTagPlantilla.T_METODOPAGO).concat(" : ").concat(facturaDto.getMetodoPago() != null ? facturaDto.getMetodoPago() : ""), arial));
        cellExhibicion.setColspan(3);
        cellExhibicion.setPaddingTop(-2f);
        cellExhibicion.setHorizontalAlignment(Element.ALIGN_CENTER);
        cellExhibicion.setBorder(Rectangle.NO_BORDER);

        tableSubtotales.addCell(cellExhibicion);
        document.add(tableSubtotales);
    }

  

    /**
     * *
     * Metodo para agregar la propina y el importe a pagar
     *
     * @param document
     * @throws DocumentException
     */
    public void agregarSubtotales2(Document document, CFDI facturaDto) throws DocumentException {
        PdfPTable tableSubtotales = new PdfPTable(3);
        tableSubtotales.setWidths(new int[]{140, 200, 100});
        tableSubtotales.setTotalWidth(215);
        tableSubtotales.setLockedWidth(true);
        tableSubtotales.getDefaultCell().setBorder(Rectangle.NO_BORDER);

        PdfPCell cellPropina = new PdfPCell();
        cellPropina.setColspan(3);
        cellPropina.setFixedHeight(1.5f);
        cellPropina.setBackgroundColor(BaseColor.LIGHT_GRAY);
        cellPropina.setHorizontalAlignment(Element.ALIGN_LEFT);
        cellPropina.setBorder(Rectangle.NO_BORDER);
        tableSubtotales.addCell(cellPropina);

        if (facturaDto.getPropina() == null) {
            PdfPCell servicioC = new PdfPCell(new Phrase(hmapTagIdioma.get(EnumTagPlantilla.T_PROPINA) + " : ", arial));
            servicioC.setHorizontalAlignment(Element.ALIGN_RIGHT);
            servicioC.setColspan(2);
            servicioC.setBorder(Rectangle.NO_BORDER);

            PdfPCell servicioV = new PdfPCell(new Phrase("0.00", arial));
            servicioV.setHorizontalAlignment(Element.ALIGN_RIGHT);
            servicioV.setBorder(Rectangle.NO_BORDER);

            tableSubtotales.addCell(servicioC);
            tableSubtotales.addCell(servicioV);
        } else {
            PdfPCell servicioC = new PdfPCell(new Phrase(hmapTagIdioma.get(EnumTagPlantilla.T_PROPINA) + " : ", arial));
            servicioC.setHorizontalAlignment(Element.ALIGN_RIGHT);
            servicioC.setColspan(2);
            servicioC.setBorder(Rectangle.NO_BORDER);

            PdfPCell servicioV = new PdfPCell(new Phrase(String.valueOf(facturaDto.getPropina() != null ? facturaDto.getPropina() : ""), arial));
            servicioV.setHorizontalAlignment(Element.ALIGN_RIGHT);
            servicioV.setBorder(Rectangle.NO_BORDER);

            tableSubtotales.addCell(servicioC);
            tableSubtotales.addCell(servicioV);
        }

        PdfPCell cellImporteAPagar = new PdfPCell(new Paragraph(new Phrase(hmapTagIdioma.get(EnumTagPlantilla.T_TOTALPAGAR), arial)));
        cellImporteAPagar.setColspan(2);
        cellImporteAPagar.setPaddingTop(-1f);
        cellImporteAPagar.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cellImporteAPagar.setBorder(Rectangle.NO_BORDER);

        PdfPCell importeAPagar = new PdfPCell(new Paragraph(new Phrase(String.valueOf(facturaDto.getTotalPagar() != null ? facturaDto.getTotalPagar() : ""), arial)));
        importeAPagar.setHorizontalAlignment(Element.ALIGN_RIGHT);
        importeAPagar.setPaddingTop(-1f);
        importeAPagar.setBorder(Rectangle.NO_BORDER);

        PdfPCell cellLeyenda = new PdfPCell(new Paragraph(new Phrase(hmapTagIdioma.get(EnumTagPlantilla.MENSAJE2), arial)));
        cellLeyenda.setColspan(3);
        cellLeyenda.setPaddingTop(-1f);
        cellLeyenda.setHorizontalAlignment(Element.ALIGN_CENTER);
        cellLeyenda.setBorder(Rectangle.NO_BORDER);

        tableSubtotales.addCell(cellImporteAPagar);
        tableSubtotales.addCell(importeAPagar);
        tableSubtotales.addCell(cellLeyenda);
        document.add(tableSubtotales);
    }

    /**
     * *
     * Metodo para agregar los metodos de pago correspondientes del ticket
     *
     * @param document
     * @throws DocumentException
     */
    public void agregarMetodosDePago(Document document, CFDI facturaDto) throws DocumentException {
        PdfPTable tableMetodosPago = new PdfPTable(1);
        tableMetodosPago.setWidths(new int[]{140});
        tableMetodosPago.setTotalWidth(215);
        tableMetodosPago.setLockedWidth(true);
        tableMetodosPago.getDefaultCell().setBorder(Rectangle.NO_BORDER);

        PdfPCell cellMetodoPago = new PdfPCell();
        cellMetodoPago.setColspan(1);
        cellMetodoPago.setFixedHeight(1.5f);
        cellMetodoPago.setBackgroundColor(BaseColor.LIGHT_GRAY);
        cellMetodoPago.setHorizontalAlignment(Element.ALIGN_LEFT);
        cellMetodoPago.setBorder(Rectangle.NO_BORDER);

        String formaPagoDescripcion = "";
        if (facturaDto.getFormaPago() != null && !facturaDto.getFormaPago().isEmpty()) {
            formaPagoDescripcion = facturaDto.getFormaPago().trim();
            //List<FormaPago> ordenarFormasPagoByMonto = Utilities.ordenarFormasPagoByMonto(facturaDto.getFormaPago());
            //formaPagoDescripcion = ordenarFormasPagoByMonto.get(0).getFormaPago().trim();
        }

        PdfPCell cellMetodoDePago = new PdfPCell(new Paragraph(new Phrase(hmapTagIdioma.get(EnumTagPlantilla.T_FORMAPAGO) + ": " + formaPagoDescripcion, arial)));
        cellMetodoDePago.setColspan(2);
        cellMetodoDePago.setHorizontalAlignment(Element.ALIGN_LEFT);
        cellMetodoDePago.setBorder(Rectangle.NO_BORDER);

        PdfPCell cellTipoMoneda = new PdfPCell(new Phrase("Tipo de Moneda:  " + facturaDto.getMoneda(), arial));
        cellTipoMoneda.setPaddingTop(-1f);
        cellTipoMoneda.setHorizontalAlignment(Element.ALIGN_LEFT);
        cellTipoMoneda.setBorder(Rectangle.NO_BORDER);

        PdfPCell celdaTipoDeCambio = new PdfPCell(new Paragraph(new Phrase(hmapTagIdioma.get(EnumTagPlantilla.T_TIPOCAMBIO) + " : " + String.valueOf(facturaDto.getTipoCambio() != null ? facturaDto.getTipoCambio() : ""), arial)));
        celdaTipoDeCambio.setPaddingTop(-1f);
        celdaTipoDeCambio.setBorder(Rectangle.NO_BORDER);
        celdaTipoDeCambio.setHorizontalAlignment(Element.ALIGN_LEFT);

        tableMetodosPago.addCell(cellMetodoPago);
        tableMetodosPago.addCell(cellMetodoDePago);

        tableMetodosPago.addCell(cellTipoMoneda);
        tableMetodosPago.addCell(celdaTipoDeCambio);

        document.add(tableMetodosPago);
    }

    public void agregaComplementoINE(Document document,CFDI facturaDto) throws DocumentException {

       if (facturaDto.getComplementos() != null) {
        if (facturaDto.getComplementos().getIne() != null) {
            PdfPTable tableHeaderINE = new PdfPTable(3);
            tableHeaderINE.setWidths(new int[]{30, 30, 30});
            tableHeaderINE.setTotalWidth(215);
            tableHeaderINE.setLockedWidth(true);
            tableHeaderINE.getDefaultCell().setBorder(Rectangle.NO_BORDER);

            PdfPCell cellTitulo = new PdfPCell(new Phrase(hmapTagIdioma.get(EnumTagPlantilla.P_COMPLEMENTOINE), arial));
            cellTitulo.setColspan(3);
            cellTitulo.setBackgroundColor(BaseColor.LIGHT_GRAY);
            cellTitulo.setHorizontalAlignment(Element.ALIGN_CENTER);
            cellTitulo.setBorder(Rectangle.NO_BORDER);

            PdfPCell tagTipoProceso = new PdfPCell(new Phrase(hmapTagIdioma.get(EnumTagPlantilla.INE_TIPO_PROCESO), arialEncabezadoConceptos));
            tagTipoProceso.setHorizontalAlignment(Element.ALIGN_LEFT);
            tagTipoProceso.setBorder(Rectangle.NO_BORDER);

            PdfPCell tagComite = new PdfPCell(new Phrase(hmapTagIdioma.get(EnumTagPlantilla.INE_TIPO_COMITE), arialEncabezadoConceptos));
            tagComite.setHorizontalAlignment(Element.ALIGN_LEFT);
            tagComite.setBorder(Rectangle.NO_BORDER);

            PdfPCell tagContabilidad = new PdfPCell(new Phrase(("Id Contabilidad: "), arialEncabezadoConceptos));
            tagContabilidad.setHorizontalAlignment(Element.ALIGN_LEFT);
            tagContabilidad.setBorder(Rectangle.NO_BORDER);

            tableHeaderINE.addCell(cellTitulo);
            tableHeaderINE.addCell(tagTipoProceso);
            tableHeaderINE.addCell(tagComite);
            tableHeaderINE.addCell(tagContabilidad);
            document.add(tableHeaderINE);

            PdfPTable tableValueINE = new PdfPTable(3);
            tableValueINE.setWidths(new int[]{30, 30, 30});
            tableValueINE.setTotalWidth(215);
            tableValueINE.setLockedWidth(true);
            tableValueINE.getDefaultCell().setBorder(Rectangle.NO_BORDER);

            PdfPCell tagTipoProcesoValue = new PdfPCell(new Phrase(facturaDto.getComplementos().getIne().getTipoProceso(), arialConceptos));
            tagTipoProcesoValue.setHorizontalAlignment(Element.ALIGN_LEFT);
            tagTipoProcesoValue.setBorder(Rectangle.NO_BORDER);

            PdfPCell tagComiteValue = new PdfPCell(new Phrase(facturaDto.getComplementos().getIne().getTipoComite(), arialConceptos));
            tagComiteValue.setHorizontalAlignment(Element.ALIGN_LEFT);
            tagComiteValue.setBorder(Rectangle.NO_BORDER);

            PdfPCell tagContabilidadValue = new PdfPCell(new Phrase(
                    facturaDto.getComplementos().getIne().getIdContabilidad() != null
                    ? facturaDto.getComplementos().getIne().getIdContabilidad().toString() : "", arialConceptos));
            tagContabilidadValue.setHorizontalAlignment(Element.ALIGN_LEFT);
            tagContabilidadValue.setBorder(Rectangle.NO_BORDER);

            tableValueINE.addCell(tagTipoProcesoValue);
            tableValueINE.addCell(tagComiteValue);
            tableValueINE.addCell(tagContabilidadValue);
            document.add(tableValueINE);

            if (facturaDto.getComplementos().getIne().getEntidad() != null && !facturaDto.getComplementos().getIne().getEntidad().isEmpty()) {
                PdfPTable tableHeaderEntidad = new PdfPTable(3);
                tableHeaderEntidad.setWidths(new int[]{30, 30, 30});
                tableHeaderEntidad.setTotalWidth(215);
                tableHeaderEntidad.setLockedWidth(true);
                tableHeaderEntidad.getDefaultCell().setBorder(Rectangle.NO_BORDER);

                PdfPCell tagClaveentidad = new PdfPCell(new Phrase(hmapTagIdioma.get(EnumTagPlantilla.INE_CLAVE_ENTIDAD), arialEncabezadoConceptos));
                tagClaveentidad.setHorizontalAlignment(Element.ALIGN_LEFT);
                tagClaveentidad.setBorder(Rectangle.NO_BORDER);

                PdfPCell tagAmbito = new PdfPCell(new Phrase(hmapTagIdioma.get(EnumTagPlantilla.INE_AMBITO), arialEncabezadoConceptos));
                tagAmbito.setHorizontalAlignment(Element.ALIGN_LEFT);
                tagAmbito.setBorder(Rectangle.NO_BORDER);

                PdfPCell tagIdContabilidad = new PdfPCell(new Phrase(hmapTagIdioma.get(EnumTagPlantilla.INE_ID_CONTABILIDAD), arialEncabezadoConceptos));
                tagIdContabilidad.setHorizontalAlignment(Element.ALIGN_LEFT);
                tagIdContabilidad.setBorder(Rectangle.NO_BORDER);

                tableHeaderEntidad.addCell(tagClaveentidad);
                tableHeaderEntidad.addCell(tagAmbito);
                tableHeaderEntidad.addCell(tagIdContabilidad);
                document.add(tableHeaderEntidad);

                for (Ine.Entidad entidad : facturaDto.getComplementos().getIne().getEntidad()) {
                    PdfPTable tablValueEntidad = new PdfPTable(3);
                    tablValueEntidad.setWidths(new int[]{30, 30, 30});
                    tablValueEntidad.setTotalWidth(215);
                    tablValueEntidad.setLockedWidth(true);
                    tablValueEntidad.getDefaultCell().setBorder(Rectangle.NO_BORDER);

                    PdfPCell valueClaveentidad = new PdfPCell(new Phrase(entidad.getClaveEntidad(), arialEncabezadoConceptos));
                    valueClaveentidad.setHorizontalAlignment(Element.ALIGN_LEFT);
                    valueClaveentidad.setBorder(Rectangle.NO_BORDER);

                    PdfPCell valueAmbito = new PdfPCell(new Phrase(entidad.getAmbito(), arialEncabezadoConceptos));
                    valueAmbito.setHorizontalAlignment(Element.ALIGN_LEFT);
                    valueAmbito.setBorder(Rectangle.NO_BORDER);

                    String idcontabilidad = "";
                    List<Ine.Entidad.Contabilidad> contabilidad = entidad.getContabilidad();
                    for (Ine.Entidad.Contabilidad c : contabilidad) {
                        idcontabilidad += String.valueOf(c.getIdContabilidad());
                    }
                    PdfPCell valueIdContabilidad = new PdfPCell(new Phrase(idcontabilidad, arialEncabezadoConceptos));
                    valueIdContabilidad.setHorizontalAlignment(Element.ALIGN_LEFT);
                    valueIdContabilidad.setBorder(Rectangle.NO_BORDER);

                    tablValueEntidad.addCell(valueClaveentidad);
                    tablValueEntidad.addCell(valueAmbito);
                    tablValueEntidad.addCell(valueIdContabilidad);
                    document.add(tablValueEntidad);
                }
            }
        }
       }
    }

    /**
     * *
     * Metodo para agregar los sellos del SAT
     *
     * @param document
     * @throws DocumentException
     */
    public void agregarSellos(Document document, CFDI facturaDto) throws DocumentException {
        PdfPTable tableSellos = new PdfPTable(1);
        tableSellos.setWidths(new int[]{140});
        tableSellos.setTotalWidth(215);
        tableSellos.setLockedWidth(true);
        tableSellos.getDefaultCell().setBorder(Rectangle.NO_BORDER);

        PdfPCell cellSpace = new PdfPCell();
        cellSpace.setFixedHeight(1.5f);
        cellSpace.setHorizontalAlignment(Element.ALIGN_LEFT);
        cellSpace.setBackgroundColor(BaseColor.LIGHT_GRAY);
        cellSpace.setBorder(Rectangle.NO_BORDER);

        PdfPCell cellLeyenda = new PdfPCell(new Phrase(hmapTagIdioma.get(EnumTagPlantilla.T_MENSAJE), arialSellos));
        cellLeyenda.setHorizontalAlignment(Element.ALIGN_LEFT);
        cellLeyenda.setBorder(Rectangle.NO_BORDER);

        PdfPCell cellSelloDigital = new PdfPCell(new Phrase(hmapTagIdioma.get(EnumTagPlantilla.S_SDCFDI), arial));
        cellSelloDigital.setPaddingTop(-1f);
        cellSelloDigital.setHorizontalAlignment(Element.ALIGN_LEFT);
        cellSelloDigital.setBorder(Rectangle.NO_BORDER);

        PdfPCell selloDigital = new PdfPCell(new Phrase(facturaDto.getTimbrado().getSelloCFDI(), arialSellos));
        selloDigital.setHorizontalAlignment(Element.ALIGN_LEFT);
        selloDigital.setBorder(Rectangle.NO_BORDER);

        PdfPCell cellCadenaOriginal = new PdfPCell(new Phrase(hmapTagIdioma.get(EnumTagPlantilla.S_CADENACDSAT), arial));
        cellCadenaOriginal.setHorizontalAlignment(Element.ALIGN_LEFT);
        cellCadenaOriginal.setPaddingTop(-1f);
        cellCadenaOriginal.setBorder(Rectangle.NO_BORDER);

        PdfPCell cadenaOriginal = new PdfPCell(new Phrase(facturaDto.getTimbrado().getCadenaDatosTimbrado(), arialSellos));
        cadenaOriginal.setHorizontalAlignment(Element.ALIGN_LEFT);
        cadenaOriginal.setBorder(Rectangle.NO_BORDER);

        PdfPCell cellSelloDigitalSAT = new PdfPCell(new Phrase(hmapTagIdioma.get(EnumTagPlantilla.S_SD), arial));
        cellSelloDigitalSAT.setPaddingTop(-1f);
        cellSelloDigitalSAT.setHorizontalAlignment(Element.ALIGN_LEFT);
        cellSelloDigitalSAT.setBorder(Rectangle.NO_BORDER);

        PdfPCell selloDigitalSAT = new PdfPCell(new Phrase(facturaDto.getTimbrado().getSelloSAT(), arialSellos));
        selloDigitalSAT.setHorizontalAlignment(Element.ALIGN_LEFT);
        selloDigitalSAT.setBorder(Rectangle.NO_BORDER);

        tableSellos.addCell(cellSpace);
        tableSellos.addCell(cellLeyenda);
        tableSellos.addCell(cellSelloDigital);
        tableSellos.addCell(selloDigital);
        tableSellos.addCell(cellCadenaOriginal);
        tableSellos.addCell(cadenaOriginal);
        tableSellos.addCell(cellSelloDigitalSAT);
        tableSellos.addCell(selloDigitalSAT);

        PdfPTable tableQRCode = new PdfPTable(1);
        tableQRCode.setWidths(new int[]{120});
        tableQRCode.setTotalWidth(50);
        tableQRCode.getDefaultCell().setBorder(Rectangle.NO_BORDER);
        tableQRCode.getDefaultCell().setFixedHeight(75f);
        try {
            String dato = "https://verificacfdi.facturaelectronica.sat.gob.mx/default.aspx?"
                    + "id=" + facturaDto.getTimbrado().getUuid()
                    + "&re=" + facturaDto.getEmisor().getRfc()
                    + "&rr=" + facturaDto.getReceptor().getRfc()
                    + "&tt=" + facturaDto.getTotal()
                    + "&fe=" + facturaDto.getTimbrado().getSelloCFDI().substring(
                            facturaDto.getTimbrado().getSelloCFDI().length() - 8);
            BarcodeQRCode barcodeQRCode = new BarcodeQRCode(dato, 600, 600, null);
            Image codeQrImage = barcodeQRCode.getImage();
            codeQrImage.scaleAbsolute(30, 30);
            tableQRCode.addCell(codeQrImage);
        } catch (Exception ex) {
            tableQRCode.addCell("");
            System.out.println("Error al generar el codigo QR " + ex.getMessage());
        }
        tableSellos.addCell(tableQRCode);

        document.add(tableSellos);
    }


    private void agregarTableInformacionGlobal (Document document, CFDI facturaDto) throws  DocumentException {
        PdfPTable tableInformacionGlobalH = new PdfPTable(1);
        tableInformacionGlobalH.setWidths(new int[]{ 100});
        tableInformacionGlobalH.setTotalWidth(215);
        tableInformacionGlobalH.setLockedWidth(true);
        tableInformacionGlobalH.getDefaultCell().setBorder(Rectangle.NO_BORDER);


        if (facturaDto.getInformacionGlobal()!= null) {

            PdfPCell encabezado = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.INFORMACION_GLOBAL), arial));     
            encabezado.setBackgroundColor(BaseColor.LIGHT_GRAY);
            encabezado.setHorizontalAlignment(Element.ALIGN_CENTER);
            encabezado.setBorder(Rectangle.NO_BORDER);

            PdfPCell peridodicidad = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.PERIODICIDAD).concat(" : ").concat(facturaDto.getInformacionGlobal().getPeriodicidad() != null ? facturaDto.getInformacionGlobal().getPeriodicidad() : ""), arial));
            peridodicidad.setHorizontalAlignment(Element.ALIGN_LEFT);
            peridodicidad.setBorder(Rectangle.NO_BORDER);

            PdfPCell meses = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.MESES).concat(" : ").concat(facturaDto.getInformacionGlobal().getMeses() != null ? facturaDto.getInformacionGlobal().getMeses() : ""), arial));
            meses.setHorizontalAlignment(Element.ALIGN_LEFT);
            meses.setPaddingTop(-1f);
            meses.setBorder(Rectangle.NO_BORDER);

            PdfPCell anio = new PdfPCell(new Paragraph(hmapTagIdioma.get(EnumTagPlantilla.ANIO).concat(" : ").concat( String.valueOf(facturaDto.getInformacionGlobal().getAnio())  != null  ? String.valueOf(facturaDto.getInformacionGlobal().getAnio()) : ""), arial));
            anio.setHorizontalAlignment(Element.ALIGN_LEFT);
            anio.setPaddingTop(-1f);
            anio.setBorder(Rectangle.NO_BORDER);


            tableInformacionGlobalH.addCell(encabezado);
            tableInformacionGlobalH.addCell(peridodicidad);
            tableInformacionGlobalH.addCell(meses);
            tableInformacionGlobalH.addCell(anio);
           

        }
      

        document.add(tableInformacionGlobalH);
    }


}
