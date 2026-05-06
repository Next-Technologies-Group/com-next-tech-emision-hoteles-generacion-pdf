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
package com.cfdi40.pdfgen.util;

public class ConstantesFacto {

    //RFC SAT
    public static final String RFC_EXTRANJERO_SAT = "XEXX010101000";
    public static final String RFC_GENERICO_SAT = "XAXX010101000";
    //RFC RegEx del SAT
    public static final String RFC_PATTERN = "[A-Z,Ñ,&]{3,4}[0-9]{2}[0-1][0-9][0-3][0-9][A-Z,0-9]?[A-Z,0-9]?[0-9,A-Z]?";
    public static final String UUID_PATTERN = "[a-f0-9A-F]{8}-[a-f0-9A-F]{4}-[a-f0-9A-F]{4}-[a-f0-9A-F]{4}-[a-f0-9A-F]{12}";
    //Metdos de pago
    public static final String METODO_PAGO_OTROS = "99-Otros";

    //Abreviatura Tipo de Moneda Facto
    public static final String MONEDA_MXN = "MXN";
    public static final String MONEDA_USD = "USD";
    public static final String MONEDA_XXX = "XXX";

    //Abreviatura de Idioma en los procesos de Facto
    public static final String IDIOMA_ESANOL = "SP";
    public static final String IDIOMA_INGLES = "EN";

    //VERSIONES CFDI
    public static final String V_32 = "3.2";
    public static final String V_33 = "3.3";

    //IMPUESTOS FEDERALES
    public static final String IVA = "IVA";
    public static final String IEPS = "IEPS";
    public static final String ISR = "ISR";
    public static final String IVA_CLAVE = "002";
    public static final String IEPS_CLAVE = "003";
    public static final String ISR_CLAVE = "001";

    //IMPUESTOS LOCALES
    public static final String ISH = "ISH";

    public static final String IMPUESTO_LOCAL = "LOCAL";
    public static final String IMPUESTO_FEDERAL = "FEDERAL";

    public static final String IMPUESTO_FACTOR_TASA = "Tasa";
    public static final String IMPUESTO_FACTOR_CUOTA = "Cuota";

    //TASA IVA 16
    public static final String TASA_16_IVA = "0.160000";

    //TIPO DE COMPROBANTE
    public static final String INGRESO = "ingreso";
    public static final String EGRESO = "egreso";

    //RegEx para validacion de email
    public static final String EMAIL_PATTERN = "^[_A-Za-z0-9-]+(\\."
            + "[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*" + "(\\.[A-Za-z]{2,})$";

    //Separador de rutas 
    public static final String PATH_SEPARATOR = "/";

    //Codificacion 
    public static final String CHARSET_UTF8 = "UTF-8";
    public static final String CHARSET_ISO = "ISO-8859-1";

    //Cargos no facturables
    public static final String PROPINA = "Propina";
    public static final String PAIDOUT = "PaidOut";

    //Unidad default de un concepto para una factura
    public static final String UNIDAD_CONCEPTO = "N/A";

    public static final String FORMA_PAGO_POR_DEFINIR = "99";

    //Constantes leyenda para PDF
    public static final String LEYENDA_PDF_SIN_SELLAR = "S i n   S e l l a r";
    public static final String LEYENDA_PDF_CANCELADO = "C a n c e l a d a";

    public static final int TIMBRADO_ERRONEO = 500;
    public static final int TIMBRADO_EXITOSO = 200;

    public static final String ARCHIVO_XML = "xml";
    public static final String ARCHIVO_PDF = "pdf";
    
    public static final String EXTENSION_XML = ".xml";
    public static final String EXTENSION_PDF = ".pdf";

    //Nombre Archivo de Descarga
    public static final String ZIP_NAME = "Cfdi.zip";

    //CAT PROD/SERVICIO
    public final static String CLAVE_PROD_SERV_84111506 = "84111506";

    //CAT UNIDAD
    public final static String CLAVE_UNIDAD_ACT = "ACT";

    //CAT METODO PAGO
    public static final String METODO_PAGO_PPD = "PPD";
    
    //CONCEPTO PARA COMPLEMENTOS
    public final static String DESCRIPCION_PAGO = "Pago";

    //CAT TIPO DE RELACION
    public final static String TIPO_RELACION_01 = "01";
    public final static String TIPO_RELACION_04 = "04";

    //CAT FORMA PAGO
    public static final String FORMA_PAGO_99 = "99";

    //CAT USO DE CFDI
    public final static String USO_CFDI_P01 = "P01";
    public final static String USO_CFDI_GASTOS_EN_GENERAL = "G03";
    
    //LIMITES IMPORTE
    public final static String LIMITE_INFERIOR = "LI";    
    public final static String LIMITE_SUPERIOR = "LS";
    
    //RegEx Complemento de Pago
    
    public static final String NUM_OPERACION_PATTERN = "([A-Z]|[a-z]|"
            + "[0-9]| |Ñ|ñ|!|\"|%|&|'|´|-|:|;|>|=|<|@|_|,|\\{|\\}|`|~|á|é|í|ó|ú|Á|É|Í|Ó|Ú|ü|Ü){1,100}";
    
    public static final String RFC_EMISOR_CTA_ORD_PATTERN = "XEXX010101000|[A-Z&Ñ]{3}[0-9]{2}"
            + "(0[1-9]|1[012])(0[1-9]|[12][0-9]|3[01])[A-Z0-9]{2}[0-9A]";
    
    public static final String NOM_BANCO_ORD_EXT_PATTERN = "([A-Z]|[a-z]|"
             + "[0-9]| |Ñ|ñ|!|\"|%|&|'|´|-|:|;|>|=|<|@|_|,|\\{|\\}|`|~|á|é|í|ó|ú|Á|É|Í|Ó|Ú|ü|Ü){1,300}";
     
    //Obtenida de http://www.sat.gob.mx/sitio_internet/cfd/tipoDatos/tdCFDI/tdCFDI.xsd seccion t_RFC_PM
    public static final String RFC_EMISOR_CTA_BEN_PATTERN = "[A-Z&Ñ]{3}[0-9]{2}(0[1-9]|1[012])(0[1-9]|[12][0-9]|3[01])[A-Z0-9]{2}[0-9A]";
     

    
    //Constantes del Servicio de Bitacora de Timbrado
    public final static String PAC_VALUE = "PAC_VALUE";
    public final static String TIPO_SOLICITUD_PAC = "TIPO_REQUEST";
    public final static String CLAVE_ID = "id";    
    public final static String CENTRO_CONSUMO_ID = "centroConsumoId";
    
    
    public static final String AUTHORIZATION_HEADER = "Authorization";
    
    public static final String CURP_PATTERN
            = "[A-Z]{1}[AEIOU]{1}[A-Z]{2}[0-9]{2}"
            + "(0[1-9]|1[0-2])(0[1-9]|1[0-9]|2[0-9]|3[0-1])"
            + "[HM]{1}"
            + "(AS|BC|BS|CC|CS|CH|CL|CM|DF|DG|GT|GR|HG|JC|MC|MN|MS|NT|NL|OC|PL|QT|QR|SP|SL|SR|TC|TS|TL|VZ|YN|ZS|NE)"
            + "[B-DF-HJ-NP-TV-Z]{3}"
            + "[0-9A-Z]{1}[0-9]{1}$";
    
}
