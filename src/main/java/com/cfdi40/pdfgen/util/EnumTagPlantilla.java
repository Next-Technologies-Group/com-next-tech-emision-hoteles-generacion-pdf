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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cfdi40.pdfgen.util; 


public enum EnumTagPlantilla {

    //SECCION ENCABEZADO
    E_FECHAEMISION,//0
    E_FOLIOFISCAL,//1
    E_FECHATIMBRADO,//2
    E_SERIECERT,//3
    E_NOCERTSAT,//4
    E_LUGAREXPEDICION,//5


    //GENERALES
    COLONIA,//6
    CODIGOPOSTAL,//7
    NOMBRE,//8
    RFC,//9

    //SUCURSAL
    INFORMACION_SUCURSAL_TITTLE,//10

    //DATOS DEL CLIENTE
    DC_TITULO,//11
    DC_CALLE,//12
    DC_COLONIA,//13
    DC_DELEGACION,//14
    DC_ESTADO,//15
    DC_PAIS,//16

    //TABLAS OPCIONALES SUPERIORES
    //MICROS
    MIC_REFERENCIA,//17
    MIC_FECHACONSUMO,//18
    MIC_NOCHEQUE,//19
    //OPERA
    OPE_NOMBREHUESPED,//20
    OPE_FECHALLEGADA,//21
    OPE_NOHABITACION,//22
    OPE_FECHASALIDA,//23
    OPE_FOLIOID,//24

    //ENCABEZADO CONCEPTOS
    CON_CANTIDAD,//25
    CON_UNIDAD,//26
    CON_DESCRIPCION,//27
    CON_PUNITARIO,//28
    CON_IMPORTE,//29

    //TABLAS OPCIONALES INFERIORES
    NOTA_TITTLE,//30
    PE_TITTLE,//31
    PE_NOPASAPORTE,//32
    PE_FORMATOPAGO,//33
    PE_DIGTARJETA,//34

    //TABLA TOTALES
    DESCRIPCION_PAGO_TITTLE,//35
    T_FORMAPAGO,//36
    T_METODOPAGO,//37
    T_SUBTOTAL,//38
    T_TOTALFACT,//39
    T_TOTALPAGAR,//40
    T_DESCUENTO,//41
    T_SUBTOTALSDESC,//42
    T_SERVICIO,//43

    //TABLA SELLOS
    S_SDCFDI,//44
    S_SD,//45
    S_CADENACDSAT,//46

    //LETRAS PEQUENIAS
    MENSAJE1,//47
    MENSAJE2,//48

    //PAGINACION
    PAGINA,//49
    DE,//50

    //PAIDOUT
    T_PAIDOUT,//51

    T_NUMEROCTA,//52

    //Opera
    OPE_NUMERO_RESERVA,//53
    OPE_NUMERO_REFERENCIA,//54

    //COMPLEMENTO INE
    INE_TIPO_PROCESO,//55
    INE_TIPO_COMITE,//56
    INE_ID_CONTABILIDAD,//57
    INE_ENTIDAD_FEDERATIVA,//58
    INE_AMBITO,//59
    INE_ENCABEZADO,//60
    INE_CLAVE_ENTIDAD,//61

    //Datos Sucursal
    SUCURSAL_TELEFONO,//62

    INFORMACION_HUESPED_TITTLE,//63

    TOTAL_PAGO_TITTLE,//64

    //DATOS DEL CLIENTE
    DOMICILIO_ENTREGA_TITTLE,//65

    //Datos Addenda Audi
    ORDEN_COMPRA,//66
    NUMERO_PROVEEDOR,//67
    ADDENDA_TITLE,// 68
    AUDI_TITLE,// 69
    CANCELACIONES,//70
    REFERENCIA_1,//71
    REFERENCIA_2,//72
    CODIGO,//73
    PLAN_EMTREGA,//74
    MABE_TITLE, //75
    UNIDAD_NEGOCIO,//76

    //NUMERO INTERIOR Y EXTERIOR DE LA DIRECCION DEL CLIENTE
    //    NOTA: Estan en Hardcode no se usan
    //    DC_NUMEROINT,
    //    DC_NUMERO;

    T_PROPINA,//77

    //OPERA HARD ROCK
    OPE_LOCALIZADOR,//78

    //Tipo De Cambio
    T_TIPOCAMBIO,//79

    FACTURA, //80
    HONORARIOS, //81
    NOTA_CREDITO, //82

    //TAGS DE ADDENDA HOTELERA
    P_LUGAREXPEDICION, //83 Expedido en
    P_RESERVACION, //84 Reservaciones 
    P_CONTACTO, //85 Contacto hotel
    P_EMAIL, //86 Email Hotel
    P_TIPOREGIMEN, //87 Tipo regimen
    P_NOCERTIFICADO, //88 Certificado
    P_NOCERTIFICADOSAT, //89 No. Certificado SAT
    P_FOLIOUUID, //90 Folio (UUID)
    P_FECHACERTIFICACIONCFDI, //91 Fecha de Certificación del CFDI
    P_FOLIOXML, //92 Folio (xml)
    P_DELEGACION, //93 DELG. 
    P_COMPLEMENTOINE, //94 Complemento INE
    P_DATOSFACTURACION, //95 DATOS DE FACTURACION
    P_DATOSDELCLIENTE, //96 DATOS DEL CLIENTE
    P_FOLIO, //97 Folio
    P_CAJERO, //98 Cajero
    P_FORMATOFACTURA, //99 Formato de Factura
    P_LEYENDA, //100 Leyenda Leyenda

    P_HUESPED_OPE, //101 Huesped
    P_VOUCHER_OPE, //102 Voucher
    P_CUPON_OPE, //103 Cupon
    P_ESTANCIA_OPE, //104 Estancia
    P_HABITACION_OPE, //106 HAB
    P_RESERVACION_OPE, //107 Reservacion

    P_PRODUCTOS, //107 PRODUCTOS Y SERVICIOS
    P_CANTIDAD, //108 Cantidad
    P_CONCEPTO, //109 Concepto
    P_PRECIOUNITARIO, //110 Precio Unitario
    P_IMPORTE, //111 Importe
    P_UNIDADMEDIDA, //112 Unidad de medida

    P_MONEDA, //113 MONEDA
    MENSAJE3, //114 Si su factura presenta algún error, podrá solicitar la modificación dentro del mismo mes que se
    MENSAJE4, //115 emitió este documento, contactando a Servicio a Huéspedes del Hotel. Tome nota que la nueva
    MENSAJE5, //116 factura saldrá con la fecha de reemisión. La fecha de consumo no cambia.
    MENSAJE6, //117 Leyenda legal del emisor (texto definido en plantilla del emisor)
    MENSAJE7, //118 Leyenda legal del emisor en inglés (texto definido en plantilla del emisor)

    //TenantAData Nomina
    N_REGISTROPATRONAL, //119 Registro Patronal
    N_REGIMENFISCAL, //120 Regimen Fiscal
    N_NOSEGURIDADSOCIAL, // 121 No de seguridad social
    N_NOEMPLEADO, //122 No. empleado
    N_CURP, //123 CURP
    N_SINDICALIZADO, //124 Sindicalizado
    N_PERIODOPAGO, //125 Periodo de pago
    N_DIASPAGADOS, //126 Dias Pagados
    N_FECHAPAGO, //127 Fecha Pagado
    N_PERIOCIDADPAGO, //128 Periocidad Pago
    N_REGIMENTRABAJADOR, //129 Régimen del trabajador
    N_TIPONOMINA, //130 Tipo de nómina
    N_DEPARTAMENTO, //131 Departamento
    N_PUESTO, //132 Puesto
    N_TIPOCONTRATO, //133 Tipo de contrato
    N_CLAVEENTIDAD,//134 Clave Entidad Federativa
    N_SALARIOBASE,//135 Salario base de cotización
    N_TIPOJORNADA, //136 Tipo de jornada
    N_SALARIODIARIO, //137 Salario diario integrado
    N_RIESGOPUESTO, //138 Riesgo de puesto
    N_FECHAINICIOLABORAL, //139 Fecha de inicio de relación laboral
    N_ANTIGUEDAD, //140 Antiguedad
    N_PERCEPCIONES, //141 PERCEPCIONES
    N_TIPO, //142 Tipo
    N_CLAVE, //143 Clave
    N_CONCEPTO, //144 Concepto
    N_IMPORTEGRAVADO, //145 Importe gravado
    N_IMPORTEEXCENTO, //146 Importe exento
    N_TOTALPERCEPCIONES, //147 Total percepciones
    N_TOTALDEDUCCIONES, //148 Total deducciones
    N_TOTALIMPUESTOSRET, //149 Total Impuestos retenidos
    N_DESCRIPCION, //150 Descripcion 
    N_VALORUNITARIO, //151 Valor Unitario
    N_IMPORTENETO, //152 Importe Neto
    N_MONEDA, //153 Moneda
    N_IMPORTELETRA, //154 Importe con letra
    N_LUGARFECHAEMISION, //155 Lugar, fecha y hora de emisión
    N_FECHAHORACERTIFICACION, //156 Fecha y hora de certificación
    N_NOSERIEEMISOR, //157 No. de serie del certificado del emisor
    N_BANCO, //158 Banco
    N_CLABE, //159 Clabe
   

    //TenantAData TenantB
    G_SERIENUMERO,//160
    G_FECHAHORAEMISION,//161
    G_NOCERTIFICADOEMISOR,//162
    G_FOLIOUUID,//163
    G_FECHACERTIFICACION,//164
    G_COMPRAREALIZADA,//165
    G_RFCCLIENTE,//166
    G_RAZONCLIENTE,//167
    G_USOCFDI,//168
    G_CONDICIONESPAGO,//169
    G_TIPORELACION,//170
    G_UUIDRELACIONADO, //171

    //ComplementoPago
    G_CFDIRELACIONADO,//172
    P_CLAVEUNIDAD,//173
    P_CLAVEPRODUCTO,//174

    //Plantilla tenantD
    F_RECIBODONATIVOS,//175
    F_MENSAJEDONATIVOS1, //176
    F_MENSAJEDONATIVOS2, //177
    F_EFECTODONATIVOS, //178

    //Plantilla TenantA
    C_CLIENTE, //179
    C_NOCLIENTE, //180
    C_NOORDENFACTURACION, //181

    //Plantilla Ticket
    T_RESTAURANTE,//182
    T_NUMCERTIFICADO,//183
    T_FECHACHEQUE,//184
    T_CHEQUE,//185
    T_REFENCIA, //186
    T_CANT, //187
    T_PRE,//188
    T_MENSAJE, //189

    //Etiquetas Faltantes 
    //TenantA 
    C_TIPOCAMBIO, //190 Tipo de cambio
    C_COMPLEMENTO_RECEPCION_PAGO, //191 COMPLEMENTO PARA RECEPCION DE PAGO
    C_RAZONSOCIAL, //192 Razon Social
    C_PRODUCTOS_SERVICIO, //193 Productos/Servicios
    C_TOTAL, //194 Total

    //Complemento para recepcion de pago
    C_PAGO, //195 Pago
    C_FECHAPAGO, //196 Fecha Pago
    C_MONTO, //197 Monto
    C_NUMEROOPERACION, //198 Numero Operación
    C_RFCEMISORCTAORDENANTE, //199 RFC Emisor Cuenta Ordenante
    C_NOMBREBANCOORDENANTE, //200 Nombre Banco Ordenante
    C_CTAORDENANTE, //201 Cuenta Ordenante
    C_EMISORCTABENEFICIARIO, //202 RFC Emisor Cuenta Beneficiario
    C_CTABENEFICIARIO, //203 Cuenta Beneficiario
    C_TIPOCADENAPAGO, //204 Tipo de Cadena de Pago
    C_CERTIFICADOPAGO, //205 Certificado pago
    C_CADUCIDADPAGO, //206 Caducidad pago
    C_SELLOPAGO, //207 Sello pago
    C_DTOSRELACIONADOS, //208 Documentos Relacionados
    C_FOLIOUUID, //209 Folio UUID
    C_FOLIO, //210 Folio
    C_NUMEROPARCIALIDADES, //211 Número de Parcialidade
    C_IMPORTESALDOANT, //212 Importe Saldo Anterior
    C_IMPORTESALDOINS, //213 Importe Saldo Insoluto

    G_TELEFONO, //214 Tel
    C_IMPORTEPAGADO,// 215 ImpPagado
    C_NUM_REG_TRIBUTARIO, //216 NUMERO REGIMEN TRIBUTARIOS

    P_CONTRA_CODE,//217 ETIQUETA CONTACODE DE ADDENDA HOTELERA
    P_COMPANIA,//218 ETIQUETA COMPANIA DE ADDENDA HOTELERA
    
    //Plantilla tenantD
    LEYENDA_GOBIERNO,//219

    //-----------------------------------------------------------------------------
    // ---------------------------Campos version 4.0 ------------------------------
    //-----------------------------------------------------------------------------

    E_EXPORTACION, //220
    R_DOMFISCRECEP, //221 Domicilio Fiscal Receptor
    //cfdi:InformacionGlobal
    PERIODICIDAD,//222
    MESES,//223
    ANIO, //224
    E_NOMBRE, // 225 Nombre Emisor
    FAC_ATR_ADQUIRIENTE, // 226 Fac Atr Adquirente
    REG_FISCAL_RECEP, // 227
    NUM_REGID_TRIB, //228
    TOTAL_RETENCIONES_IVA, //229
    TOTAL_RETENCIONES_ISR, //230
    TOTAL_RETENCIONES_IEPS, //231
    TOTAL_TRASLADOS_BASE_IVA_16, //232
    TOTAL_TRASLADOS_IMPUESTO_IVA_16, //233
    TOTAL_TRASLADOS_BASE_IVA8, //234
    TOTAL_TRASLADOS_IMPUESTO_IVA_8, //235
    TOTAL_TRASLADOS_BASE_IVA_0, //236
    TOTAL_TRASLADOS_IMPUESTO_IVA_0, //237
    TOTAL_TRASLADOS_BASE_IVA_EXENTO, //238
    MONTO_TOTAL_PAGOS, //239
    NOMBRE_A_CUENTA_TERCEROS, //240
    RFC_A_CUENTA_TERCEROS, //241
    DOMICILIO_A_CUENTA_TERCEROS, //242
    IMP_TRASLADO, //243
    TASA_O_CUOTA, //244
    TIPO_FACTOR, //245
    IMPORTE, //246
    BASE, //247
    IMP_RETENIDO, //248
    CFDI_RELACIONADOS, //249
    UUID_RELACIONADO, //250
    TIPO_RELACION, //251
    INFORMACION_GLOBAL, //252
    IMPUESTO_L, //253
    S_CADENADTSAT,//254
    S_IMPOBJECT,//255
    EQUIVALENCIA_DR,//256
    SERIE ,//257
    P_AGENCIA,//258 ETIQUETA AGENCIA DE ADDENDA HOTELERA


}
