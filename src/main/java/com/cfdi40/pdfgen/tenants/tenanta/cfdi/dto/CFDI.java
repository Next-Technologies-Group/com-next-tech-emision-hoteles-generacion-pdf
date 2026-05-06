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
package com.cfdi40.pdfgen.tenants.tenanta.cfdi.dto;

import lombok.Data;
import lombok.Getter;

import javax.xml.bind.annotation.*;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Cfdi")
public class CFDI {

    @XmlElement(name = "serie", required = false, nillable = true)
    private String serie;
    @XmlElement(name = "folio", required = false, nillable = true)
    private String folio;
    @XmlElement(name = "fecha", required = true, nillable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "America/Mexico_City")
    private Date fecha;
    @XmlElement(name = "exportacion", required = true, nillable = false)
    private String exportacion;
    @XmlElement(name = "informacionGlobal", required = false, nillable = true)
    private InformacionGlobal informacionGlobal;
    @XmlElement(name = "formaPago", required = false, nillable = true)
    private String formaPago;
    @XmlElement(name = "condicionesDePago", required = false, nillable = true)
    private String condicionesDePago;
    @XmlElement(name = "subTotal", required = true, nillable = false)
    private BigDecimal subTotal;
    @XmlElement(name = "descuento", required = false, nillable = true)
    private BigDecimal descuento;
    @XmlElement(name = "moneda", required = true, nillable = false)
    private String moneda;
    @XmlElement(name = "tipoCambio", required = false, nillable = true)
    private BigDecimal tipoCambio;
    @XmlElement(name = "total", required = true, nillable = false)
    private BigDecimal total;
    @XmlElement(name = "lugarExpedicion", required = false, nillable = true)
    private String lugarExpedicion;
    @XmlElement(name = "confirmacion", required = false, nillable = true)
    private String confirmacion;
    @XmlElement(name = "tipo", required = true, nillable = false)
    private EnumTipoFactura tipoCfdi;
    @XmlElement(name = "puntoVenta", required = false, nillable = true)
    private PuntoVenta puntoVenta;
    @XmlElement(name = "formato", required = false, nillable = true)
    private String formato;
    private List<String> leyenda;
    private String numeroReferencia;
    @XmlElement(name = "propina", required = false, nillable = true)
    private BigDecimal propina;
    @XmlElement(name = "totalPagar", required = false, nillable = true)
    private BigDecimal totalPagar;
    @XmlElement(name = "email", required = false, nillable = true)
    private List<String> email;
    @XmlElement(name = "metodoPago", required = false, nillable = true)
    private String metodoPago;
    private String numCtaPago;
    private String idioma;
    private String nombreHotel;
    private String telefonoHotel;
    private String numeroCertificado;
    private String nombreCentroConsumo;
    private boolean foliosFacto;
    @XmlElement(name = "cfdiRelacionados", required = false, nillable = true)
    private List<Relacionados> cfdiRelacionados;
    @XmlElement(name = "emisor", required = true, nillable = false)
    private Emisor emisor;
    @XmlElement(name = "receptor", required = true, nillable = false)
    private Receptor receptor;
    @XmlElement(name = "conceptos", required = true, nillable = false)
    private List<Conceptos> conceptos;
    @XmlElement(name = "impuestos", required = false, nillable = true)
    private Impuestos impuestos;

    private TenantBDto tenantB;

    private Hospedaje hospedaje;
    private Alimentos alimentos;

    private Addendas addendas;
    //    private AdendaDto adenda; parece que no se usa
    private TenantAData tenantAData;

    private String xmlAddenda;

    @XmlTransient
   private Timbrado timbrado;

    private ImpuestosLocales impuestosLocales;
    private Complementos complementos;

    @Data
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class InformacionGlobal {

        private String periodicidad;
        private String meses;
        private int anio;

    }

    @Data
    @XmlTransient
    public static class Timbrado{

        // private String numeroCertificado;
        private String numCertificadoSAT;
        private String selloCFDI;
        private String selloSAT;
        private String rfcProvCertif;
        private String leyenda;
        private String uuid;
        private String version;
        private String cadenaOriginal;
        private String cadenaDatosTimbrado;
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "America/Mexico_City")
        private Date fechaTimbrado;
        private String nombreArchivo;
        private String rutaXml;
        private String rutaPdf;
        // private EnumPacs pacTimbrado;
        private String urlDescargaXml;
        private String urlDescargaPdf;
    }


    @Data
    @XmlAccessorType(XmlAccessType.FIELD)

    public static class Alimentos{
        private String cheque;
        private Date fechaCheque;
        private String referencia;
    }

    @Data
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class ImpuestosLocales {

        private BigDecimal totalImpuestosRetenidos;
        private BigDecimal totalImpuestosTrasladados;
        private List<RetencionLocal> retencionesLocales;
        private List<TrasladoLocal> trasladoLocales;

        @Data
        @XmlAccessorType(XmlAccessType.FIELD)
        public static class RetencionLocal {

            private String impuesto;
            private BigDecimal importe;
            private BigDecimal tasa;
        }

        @Data
        @XmlAccessorType(XmlAccessType.FIELD)
        public static class TrasladoLocal {

            private String impuesto;
            private BigDecimal tasa;
            private BigDecimal importe;
        }


    }

    @Data
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Complementos {

        private Ine ine;
        private CartaPorte cartaPorte;
        @XmlElement(name = "complementoPago20", required = false, nillable = true)
        private ComplementoPago20 complementoPago20;
        private Donatarias donatarias;
        private Nomina nomina;

        @Data
        public static class CartaPorte {
            private String version; // requerido
            private String idCCP; // requerido
            private String transpInternac;// requerido
            private EnumRegimenAduanero regimenAduanero; // condicional
            private String entradaSalidaMerc;
            private String paisOrigenDestino;
            private String viaEntradaSalida;
            private BigDecimal totalDistRec;
            private String registroISTMO;
            private String ubicacionPoloOrigen;
             private String ubicacionPoloDestino;
            private Ubicaciones ubicaciones; // requerido
            private Mercancias mercancias; // requerido
            private FiguraTransporte figuraTransporte;
    
            @Data
            public static class Ubicaciones {
    
                private List<Ubicacion> ubicacion;
    
                @Data
                public static class Ubicacion {
    
                    private Domicilio domicilio;
                    private String tipoUbicacion;
                    private String idUbicacion;
                    private String rfcRemitenteDestinatario;
                    private String nombreRemitenteDestinatario;
                    private String numRegIdTrib;
                    private String residenciaFiscal;
                    private String numEstacion;
                    private String nombreEstacion;
                    private String navegacionTrafico;
                    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "America/Mexico_City")
                    private Date fechaHoraSalidaLlegada;
                    private String tipoEstacion;
                    private BigDecimal distanciaRecorrida;
    
                    @Data
                    @XmlType(
                        name = "DomicilioUbicacionCartaPorte"
                    )
                    public static class Domicilio {
    
                        private String calle;
                        private String numeroExterior;
                        private String numeroInterior;
                        private String colonia;
                        private String localidad;
                        private String referencia;
                        private String municipio;
                        private String estado;
                        private String pais;
                        private String codigoPostal;
    
                    }
    
                }
            }
    
            @Data
            @XmlType(name = "MercanciasCartaPorte")
            public static class Mercancias {
    
                private List<Mercancia> mercancia;
                private Autotransporte autotransporte;
                private TransporteMaritimo transporteMaritimo;
                private TransporteAereo transporteAereo;
                private TransporteFerroviario transporteFerroviario;
                private BigDecimal pesoBrutoTotal;
                private String unidadPeso;
                private BigDecimal pesoNetoTotal;
                private int numTotalMercancias;
                private BigDecimal cargoPorTasacion;
                private String logisticaInversaRecoleccionDevolucion;
    
                @Data
                @XmlType(name = "MercanciaCartaPorte")
                public static class Mercancia {
    
                    private List<DocumentacionAduanera> documentacionAduanera;
                    private List<GuiasIdentificacion> guiasIdentificacion;
                    private List<CantidadTransporta> cantidadTransporta;
                    private DetalleMercancia detalleMercancia;
                    private String bienesTransp;
                    private String claveSTCC;
                    private String descripcion;
                    private BigDecimal cantidad;
                    private String claveUnidad;
                    private String unidad;
                    private String dimensiones;
                    private String materialPeligroso;
                    private String cveMaterialPeligroso;
                    private String embalaje;
                    private String descripEmbalaje;
                    private String sectorCOFEPRIS;
                    private String nombreIngredienteActivo;
                    private String nomQuimico;
                    private String denominacionGenericaProd;
                    private String denominacionDistintivaProd;
                    private String fabricante;
                    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "America/Mexico_City")
                    private Date fechaCaducidad;
                    private String loteMedicamento;
                    private String formaFarmaceutica;
                    private String condicionesEspTransp;
                    private String registroSanitarioFolioAutorizacion;
                    private String permisoImportacion;
                    private String folioImpoVUCEM;
                    private String numCAS;
                    private String razonSocialEmpImp;
                    private String numRegSanPlagCOFEPRIS;
                    private String datosFabricante;
                    private String datosFormulador;
                    private String datosMaquilador;
                    private String usoAutorizado;
                    private BigDecimal pesoEnKg;
                    private BigDecimal valorMercancia;
                    private String moneda;
                    private String fraccionArancelaria;
                    private String uuidComercioExt;
                    private String tipoMateria;
                    private String descripcionMateria;
                    private List<Pedimentos> pedimentos;

                    @Data
                    public static class Pedimentos {
                        private String pedimento;
                    }

                    @Data
                    public static class DetalleMercancia {
    
                        private String unidadPesoMerc;
                        private BigDecimal pesoBruto;
                        private BigDecimal pesoNeto;
                        private BigDecimal pesoTara;
                        private Integer numPiezas;
    
                    }
                    @Data 
                    public static class DocumentacionAduanera {
    
                        private String tipoDocumento;
                        private String numPedimento;
                        private String identDocAduanero;
                        private String rfcImpo;
                    }
    
    
                    @Data
                    public static class CantidadTransporta {
    
                        private BigDecimal cantidad;
                        private String idOrigen;
                        private String idDestino;
                        private String cvesTransporte;
    
                    }
    
                    @Data
                    public static class GuiasIdentificacion {
    
                        private String numeroGuiaIdentificacion;
                        private String descripGuiaIdentificacion;
                        private BigDecimal pesoGuiaIdentificacion;
                    }
    
                   
                }
    
                @Data
                public static class Autotransporte {
    
                    private IdentificacionVehicular identificacionVehicular;
                    private Seguros seguros;
                    private Remolques remolques;
                    private String permSCT;
                    private String numPermisoSCT;
    
                    @Data
                    public static class Remolques {
    
                        private List<Remolque> remolque;
    
                        @Data
                        public static class Remolque {
    
                            private String subTipoRem;
                            private String placa;
    
                        }
    
                    }
    
                    @Data
                    public static class Seguros {
    
                        private String aseguraRespCivil;
                        private String polizaRespCivil;
                        private String aseguraMedAmbiente;
                        private String polizaMedAmbiente;
                        private String aseguraCarga;
                        private String polizaCarga;
                        private BigDecimal primaSeguro;
    
                    }
    
                    @Data
                    public static class IdentificacionVehicular {
    
                        private String configVehicular;
                        private String placaVM;
                        private int anioModeloVM;
                        private BigDecimal pesoBrutoVehicular;
    
                    }
    
                }
    
                @Data
                public static class TransporteMaritimo {
    
                    private List<Contenedor> contenedor;
                    private RemolquesCCP remolquesCCP;
                    private String permSCT;
                    private String numPermisoSCT;
                    private String nombreAseg;
                    private String numPolizaSeguro;
                    private String tipoEmbarcacion;
                    private String matricula;
                    private String numeroOMI;
                    private Integer anioEmbarcacion;
                    private String nombreEmbarc;
                    private String nacionalidadEmbarc;
                    private BigDecimal unidadesDeArqBruto;
                    private String tipoCarga;
                    private String numCertITC;
                    private BigDecimal eslora;
                    private BigDecimal manga;
                    private BigDecimal calado;
                    private BigDecimal puntal;
                    private String lineaNaviera;
                    private String nombreAgenteNaviero;
                    private String numAutorizacionNaviero;
                    private String numViaje;
                    private String numConocEmbarc;
                    private String permisoTempNavegacion;
    
                    @Data
                    @XmlType(
                        name = "ContenedorTrasnporteMaritimo"
                    )
                    public static class Contenedor {

                        private String matriculaContenedor;
                        private String tipoContenedor;
                        private String numPrecinto;
                        private String idCCPRelacionado;
                        private String placaVMCCP;
                        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "America/Mexico_City")
                        private Date fechaCertificacionCCP;
                       
    
                    }
                    @Data
                    public static class RemolquesCCP {
    
                        private List<RemolqueCCP> remolqueCCP;
    
                        @Data
                        public static class RemolqueCCP {
    
                            private String subTipoRemCCP;
                            private String placaCCP;
    
                        }
    
                    }
    
    
                }
    
                @Data
                public static class TransporteAereo {
    
                    private String permSCT;
                    private String numPermisoSCT;
                    private String matriculaAeronave;
                    private String nombreAseg;
                    private String numPolizaSeguro;
                    private String numeroGuia;
                    private String lugarContrato;
                    private String codigoTransportista;
                    private String rfcEmbarcador;
                    private String numRegIdTribEmbarc;
                    private String residenciaFiscalEmbarc;
                    private String nombreEmbarcador;
    
                }
    
                @Data
                public static class TransporteFerroviario {
    
                    private List<DerechosDePaso> derechosDePaso;
                    private List<Carro> carro;
                    private String tipoDeServicio;
                    private String tipoDeTrafico;
                    private String nombreAseg;
                    private String numPolizaSeguro;
    
                    @Data
                    public static class Carro {
    
                        private List<Contenedor> contenedor;
                        private String tipoCarro;
                        private String matriculaCarro;
                        private String guiaCarro;
                        private BigDecimal toneladasNetasCarro;
    
                    }
    
                    @Data
                    @XmlType(
                        name = "ContenedorTransporteFerroviario"
                    )
                    public static class Contenedor {
    
                        private String tipoContenedor;
                        private BigDecimal pesoContenedorVacio;
                        private BigDecimal pesoNetoMercancia;
    
                    }
    
                    @Data
                    public static class DerechosDePaso {
    
                        private String tipoDerechoDePaso;
                        private BigDecimal kilometrajePagado;
    
                    }
    
                }
    
            }
    
            @Data
            public static class FiguraTransporte {
    
                private List<TiposFigura> tiposFigura;
    
                @Data
                public static class TiposFigura {
    
                    private List<PartesTransporte> partesTransporte;
                    private Domicilio domicilio;
                    private String tipoFigura;
                    private String rfcFigura;
                    private String numLicencia;
                    private String nombreFigura;
                    private String numRegIdTribFigura;
                    private String residenciaFiscalFigura;
                }
    
                @Data
                @XmlType(
                    name = "DomicilioFiguraTransporte"
                )
                public static class Domicilio {
    
                    private String calle;
                    private String numeroExterior;
                    private String numeroInterior;
                    private String colonia;
                    private String localidad;
                    private String referencia;
                    private String municipio;
                    private String estado;
                    private String pais;
                    private String codigoPostal;
    
                }
    
                @Data
                public static class PartesTransporte {
    
                    private String parteTransporte;
    
                }
    
            }
            @Getter
            public static enum EnumRegimenAduanero {
                IMD,
                EXD,
                ITR,
                ITE,
                ETR,
                ETE,
                DFI,
                RFE,
                RFS,
                TRA
            }
        }
    

        @Data
        @XmlAccessorType(XmlAccessType.FIELD)
        public static class ComplementoPago20 {
            private String version;
            private List<ComplementoPago20.Pago> pago;
            @XmlElement(name = "totales", required = true, nillable = false)
            private Totales totales;

            @Data
            @XmlAccessorType(XmlAccessType.FIELD)
            public static class Totales {

                @XmlElement(name = "totalRetencionesIVA")
                private BigDecimal totalRetencionesIVA;
                @XmlElement(name = "totalRetencionesISR")
                private BigDecimal totalRetencionesISR;
                @XmlElement(name = "totalRetencionesIEPS")
                private BigDecimal totalRetencionesIEPS;
                @XmlElement(name = "totalTrasladosBaseIVA16")
                private BigDecimal totalTrasladosBaseIVA16;
                @XmlElement(name = "totalTrasladosImpuestoIVA16")
                private BigDecimal totalTrasladosImpuestoIVA16;
                @XmlElement(name = "totalTrasladosBaseIVA8")
                private BigDecimal totalTrasladosBaseIVA8;
                @XmlElement(name = "totalTrasladosImpuestoIVA8")
                private BigDecimal totalTrasladosImpuestoIVA8;
                @XmlElement(name = "totalTrasladosBaseIVA0")
                private BigDecimal totalTrasladosBaseIVA0;
                @XmlElement(name = "totalTrasladosImpuestoIVA0")
                private BigDecimal totalTrasladosImpuestoIVA0;
                @XmlElement(name = "totalTrasladosBaseIVAExento")
                private BigDecimal totalTrasladosBaseIVAExento;
                @XmlElement(name = "montoTotalPagos", required = true)
                private BigDecimal montoTotalPagos;

            }

            @Data
            @XmlAccessorType(XmlAccessType.FIELD)
            public static class Pago {
                //Del tipo tdCFDI:t_FechaH, formato aaaa-mm-ddThh:mm:ss de acuerdo con estándar del SAT
                @XmlElement(name = "fechaPago", required = true)
                @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "America/Mexico_City")
                private Date fechaPago;
                @XmlElement(name = "formaDePagoP", required = true)
                private String formaDePagoP;
                @XmlElement(name = "monedaP", required = true)
                private String monedaP;
                @XmlElement(name = "tipoCambioP", required = false)
                private BigDecimal tipoCambioP;
                @XmlElement(name = "monto", required = false)
                private BigDecimal monto;
                @XmlElement(name = "numOperacion", required = false)
                private String numOperacion;
                @XmlElement(name = "rfcEmisorCtaOrd", required = false)
                private String rfcEmisorCtaOrd;
                @XmlElement(name = "nomBancoOrdExt", required = false)
                private String nomBancoOrdExt;
                @XmlElement(name = "ctaOrdenante", required = false)
                private String ctaOrdenante;
                @XmlElement(name = "rfcEmisorCtaBen", required = false)
                private String rfcEmisorCtaBen;
                @XmlElement(name = "ctaBeneficiario", required = false)
                private String ctaBeneficiario;
                @XmlElement(name = "tipoCadPago", required = false)
                private String tipoCadPago;
                @XmlElement(name = "certPago", required = false)
                private String certPago;
                @XmlElement(name = "cadPago", required = false)
                private String cadPago;
                @XmlElement(name = "selloPago", required = false)
                private String selloPago;
                @XmlElement(name = "documentoRelacionado", required = true)
                private List<DoctoRelacionado> documentoRelacionado;
                @XmlElement(name = "impuestosP", required = false)
                private ImpuestosP impuestosP;

                @Data
                @XmlAccessorType(XmlAccessType.FIELD)
                public static class ImpuestosP {

                    @XmlElement(name = "retencionesP")
                    private RetencionesP retencionesP;
                    @XmlElement(name = "trasladosP")
                    private TrasladosP trasladosP;

                    @Data
                    @XmlAccessorType(XmlAccessType.FIELD)
                    public static class TrasladosP {

                        @XmlElement(name = "trasladoP", required = true)
                        private List<TrasladoP> trasladoP;

                        @Data
                        @XmlAccessorType(XmlAccessType.FIELD)
                        public static class TrasladoP {

                            @XmlElement(name = "baseP", required = true)
                            private BigDecimal baseP;
                            @XmlElement(name = "impuestoP", required = true)
                            private String impuestoP;
                            @XmlElement(name = "tipoFactorP", required = true)
                            private String tipoFactorP;
                            @XmlElement(name = "tasaOCuotaP")
                            private BigDecimal tasaOCuotaP;
                            @XmlElement(name = "importeP")
                            private BigDecimal importeP;
                        }
                    }

                    @Data
                    @XmlAccessorType(XmlAccessType.FIELD)
                    public static class RetencionesP {

                        @XmlElement(name = "retencionP", required = true)
                        private List<RetencionP> retencionP;

                        @Data
                        @XmlAccessorType(XmlAccessType.FIELD)
                        public static class RetencionP {

                            @XmlElement(name = "impuestoP", required = true)
                            private String impuestoP;
                            @XmlElement(name = "importeP", required = true)
                            private BigDecimal importeP;


                        }


                    }
                }

                @Data
                @XmlAccessorType(XmlAccessType.FIELD)
                public static class DoctoRelacionado {


                    @XmlElement(name = "impuestosDR", required = false)
                    private ImpuestosDR impuestosDR;
                    @XmlElement(name = "idDocumento", required = true)
                    private String idDocumento;
                    @XmlElement(name = "serie", required = false)
                    private String serie;
                    @XmlElement(name = "folio", required = false)
                    private String folio;
                    @XmlElement(name = "monedaDR", required = true)
                    private String monedaDR;
                    @XmlElement(name = "equivalenciaDR", required = false)
                    private BigDecimal equivalenciaDR;
                    //private BigDecimal tipoCambioDR;
//                    private String metodoDePagoDR;
                    @XmlElement(name = "numParcialidad", required = true)
                    private Integer numParcialidad;
                    @XmlElement(name = "impSaldoAnt", required = true)
                    private BigDecimal impSaldoAnt;
                    @XmlElement(name = "impPagado", required = true)
                    private BigDecimal impPagado;
                    @XmlElement(name = "impSaldoInsoluto", required = true)
                    private BigDecimal impSaldoInsoluto;
                    @XmlElement(name = "objetoImpDR", required = true)
                    private String objetoImpDR;

                    @Data
                    @XmlAccessorType(XmlAccessType.FIELD)
                    public static class ImpuestosDR {

                        @XmlElement(name = "retencionesDR")
                        private RetencionesDR retencionesDR;
                        @XmlElement(name = "trasladosDR")
                        private TrasladosDR trasladosDR;

                        @Data
                        @XmlAccessorType(XmlAccessType.FIELD)
                        public static class TrasladosDR {

                            @XmlElement(name = "trasladoDR", required = true)
                            private List<TrasladoDR> trasladoDR;

                            @Data
                            @XmlAccessorType(XmlAccessType.FIELD)
                            public static class TrasladoDR {

                                @XmlElement(name = "baseDR", required = true)
                                private BigDecimal baseDR;
                                @XmlElement(name = "impuestoDR", required = true)
                                private String impuestoDR;
                                @XmlElement(name = "tipoFactorDR", required = true)
                                private String tipoFactorDR;
                                @XmlElement(name = "tasaOCuotaDR")
                                private BigDecimal tasaOCuotaDR;
                                @XmlElement(name = "importeDR")
                                private BigDecimal importeDR;
                            }


                        }

                        @Data
                        @XmlAccessorType(XmlAccessType.FIELD)
                        public static class RetencionesDR {

                            @XmlElement(name = "retencionDR", required = true)
                            private List<RetencionDR> retencionDR;

                            @Data
                            @XmlAccessorType(XmlAccessType.FIELD)
                            public static class RetencionDR {

                                @XmlElement(name = "baseDR", required = true)
                                private BigDecimal baseDR;
                                @XmlElement(name = "impuestoDR", required = true)
                                private String impuestoDR;
                                @XmlElement(name = "tipoFactorDR", required = true)
                                private String tipoFactorDR;
                                @XmlElement(name = "tasaOCuotaDR", required = true)
                                private BigDecimal tasaOCuotaDR;
                                @XmlElement(name = "importeDR", required = true)
                                private BigDecimal importeDR;
                            }
                        }
                    }
                }
            }
        }

        @Data
        @XmlAccessorType(XmlAccessType.FIELD)
        public static class Donatarias {

            private String version;
            private String noAutorizacion;
            private Date fechaAutorizacion;
            private String leyenda;

        }

        @Data
        @XmlAccessorType(XmlAccessType.FIELD)
        public static class Ine {

            private List<Ine.Entidad> entidad;
            private String version;
            private String tipoProceso;
            private String tipoComite;
            private Integer idContabilidad;

            @Data
            @XmlAccessorType(XmlAccessType.FIELD)
            public static class Entidad {

                private List<Ine.Entidad.Contabilidad> contabilidad;
                private String claveEntidad;
                private String ambito;

                @Data
                @XmlAccessorType(XmlAccessType.FIELD)
                public static class Contabilidad {
                    private int idContabilidad;
                }
            }
        }

        @Data
        @XmlAccessorType(XmlAccessType.FIELD)
        public static class Nomina {
            private Nomina.Emisor emisor;
            private Nomina.Receptor receptor;
            private Percepciones percepciones;
            private Deducciones deducciones;
            private OtrosPagos otrosPagos;
            private Incapacidades incapacidades;
            // private String version;
            private EnumTipoNomina tipoNomina;
            private Date fechaPago;
            private Date fechaInicialPago;
            private Date fechaFinalPago;
            private BigDecimal numDiasPagados;
            private BigDecimal totalPercepciones;
            private BigDecimal totalDeducciones;
            private BigDecimal totalOtrosPagos;

            @Getter
            public static enum EnumTipoNomina {

                O,
                E

            }

            @Data
            @XmlType(name = "EmisorNomina")
            public static class Emisor {
                private EntidadSNCF entidadSNCF;
                private String curp;
                private String registroPatronal;
                private String rfcPatronOrigen;
                @Data
                public static class EntidadSNCF {
                    private EnumCOrigenRecurso origenRecurso;
                    private BigDecimal montoRecursoPropio;

                    @Getter
                    public static enum EnumCOrigenRecurso {

                        IP,
                        IF,
                        IM

                    }

                }

            }

            @Data
            @XmlType(name = "ReceptorNomina")
            public static class Receptor {
                private List<Nomina.Receptor.SubContratacion> subContratacion;
                private String curp;
                private String numSeguridadSocial;
                private Date fechaInicioRelLaboral;
                private String antiguedad;
                private String tipoContrato;
                private String sindicalizado;
                private String tipoJornada;
                private String tipoRegimen;
                private String numEmpleado;
                private String departamento;
                private String puesto;
                private String riesgoPuesto;
                private String periodicidadPago;
                private String banco;
                private Integer cuentaBancaria;
                private BigDecimal salarioBaseCotApor;
                private BigDecimal salarioDiarioIntegrado;
                private String claveEntFed;
                @Data
                public static class SubContratacion {
                    private BigDecimal porcentajeTiempo;
                    private String rfcLabora;
                }

            }
            @Data
            public static class Percepciones {
                private List<Nomina.Percepciones.Percepcion> percepcion;
                private Nomina.Percepciones.JubilacionPensionRetiro jubilacionPensionRetiro;
                private Nomina.Percepciones.SeparacionIndemnizacion separacionIndemnizacion;
                private BigDecimal totalSueldos;
                private BigDecimal totalSeparacionIndemnizacion;
                private BigDecimal totalJubilacionPensionRetiro;
                private BigDecimal totalGravado;
                private BigDecimal totalExento;

                @Data
                public static class Percepcion {
                    private Nomina.Percepciones.Percepcion.AccionesOTitulos accionesOTitulos;
                    private List<Nomina.Percepciones.Percepcion.HorasExtra> horasExtra;
                    private String tipoPercepcion;
                    private String clave;
                    private String concepto;
                    private BigDecimal importeGravado;
                    private BigDecimal importeExento;
                    @Data
                    public static class AccionesOTitulos {
                        private BigDecimal valorMercado;
                        private BigDecimal precioAlOtorgarse;

                    }
                    @Data
                    public static class HorasExtra {
                        private int dias;
                        private String tipoHoras;
                        private int horasExtra;
                        private BigDecimal importePagado;
                    }
                }
                @Data
                public static class JubilacionPensionRetiro {
                    private BigDecimal totalUnaExhibicion;
                    private BigDecimal totalParcialidad;
                    private BigDecimal montoDiario;
                    private BigDecimal ingresoAcumulable;
                    private BigDecimal ingresoNoAcumulable;

                }
                @Data
                public static class SeparacionIndemnizacion {
                    private BigDecimal totalPagado;
                    private int numAniosServicio;
                    private BigDecimal ultimoSueldoMensOrd;
                    private BigDecimal ingresoAcumulable;
                    private BigDecimal ingresoNoAcumulable;
                }



            }
            @Data
            public static class Deducciones {
                private List<Nomina.Deducciones.Deduccion> deduccion;
                private BigDecimal totalOtrasDeducciones;
                private BigDecimal totalImpuestosRetenidos;
                @Data
                public static class Deduccion {
                    private String tipoDeduccion;
                    private String clave;
                    private String concepto;
                    private BigDecimal importe;
                }

            }
            @Data
            public static class OtrosPagos {
                private List<Nomina.OtrosPagos.OtroPago> otroPago;
                @Data
                public static class OtroPago {
                    private Nomina.OtrosPagos.OtroPago.SubsidioAlEmpleo subsidioAlEmpleo;
                    private Nomina.OtrosPagos.OtroPago.CompensacionSaldosAFavor compensacionSaldosAFavor;
                    private String tipoOtroPago;
                    private String clave;
                    private String concepto;
                    private BigDecimal importe;

                    @Data
                    public static class SubsidioAlEmpleo {
                        private BigDecimal subsidioCausado;
                    }
                    @Data
                    public static class CompensacionSaldosAFavor {
                        private BigDecimal saldoAFavor;
                        private short anio;
                        private BigDecimal remanenteSalFav;
                    }

                }

            }
            @Data
            public static class Incapacidades {
                private List<Nomina.Incapacidades.Incapacidad> incapacidad;
                @Data
                public static class Incapacidad {
                    private int diasIncapacidad;
                    private String tipoIncapacidad;
                    private BigDecimal importeMonetario;

                }


            }
        }

    }

    @Data
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class TenantAData {

        private boolean intercompania;
        private String siglasEmisor;
        private String siglasReceptor;
        private String cuentaGasto;
        private String cuentaIngreso;
        //PDF TenantA datos extras
        private String numeroReferencia;
        private String numeroCliente;
        private String numeroOrdenFacturacion;
        private String trxType;
        private String loteOracle;//Datos de TenantA para validar las facturas duplicadas o con previo timbre
        private Long trxIdOracle;//Datos de TenantA para validar las facturas duplicadas o con previo timbre
        private String contraCode;
        private String compania;
        private String agencia;
//        private EnumTipoInstitucion tipoInstitucionDonativo; //Fixme este dejara de usarse, validar si se puede eliminar
    }

    @Data
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Addendas {

        private AddendaSantander addendaSantander;
        private AddendaMabe addendaMabe;

        @Data
        @XmlAccessorType(XmlAccessType.FIELD)
        public static class AddendaMabe {

            private String version;
            private String tipoDocumento;
            private String folio;
            private Date fecha;
            private String ordenCompra;
            private String referencia1;
            private String referencia2;
            private MonedaMabe moneda;
            private ProveedorMabe proveedor;
            private Entrega entrega;
            private DetallesMabe detalles;
            private DescuentosMabe descuentos;
            private SubtotalMabe subtotal;
            private TrasladosMabe traslados;
            private RetencionesMabe retenciones;
            private TotalMabe total;

            @Data
            @XmlAccessorType(XmlAccessType.FIELD)
            public static class TotalMabe {

                private String importe;
            }

            @Data
            @XmlAccessorType(XmlAccessType.FIELD)
            public static class RetencionesMabe {
                private List<RetencionMabe> retenciones;

                @Data
                @XmlAccessorType(XmlAccessType.FIELD)
                public static class RetencionMabe {
                    private String tipo;

                    private String tasa;
                    private String importe;
                }
            }

            @Data
            @XmlAccessorType(XmlAccessType.FIELD)
            public static class TrasladosMabe {

                private List<TrasladoMabe> traslados;

                @Data
                @XmlAccessorType(XmlAccessType.FIELD)
                public static class TrasladoMabe {

                    private String tipo;
                    private String tasa;
                    private String importe;

                }

            }

            @Data
            @XmlAccessorType(XmlAccessType.FIELD)
            public static class SubtotalMabe {

                private String importe;
            }

            @Data
            @XmlAccessorType(XmlAccessType.FIELD)
            public static class DescuentosMabe {

                private TipoDescuento tipo;
                private String descripcion;
                private String importe;

                @Getter
                public static enum TipoDescuento {
                    CARGO("Cargo"),
                    DESCUENTO("Descuento");

                    private final String tipoDescuento;

                    TipoDescuento(String tipoDescuento) {
                        this.tipoDescuento = tipoDescuento;
                    }

                    public String getTipoDescuento() {
                        return tipoDescuento;
                    }
                }


            }

            @Data
            @XmlAccessorType(XmlAccessType.FIELD)
            public static class DetallesMabe {

                private List<DetalleMabe> detalles;
            }

            @Data
            @XmlAccessorType(XmlAccessType.FIELD)
            public static class DetalleMabe {

                private String noLineaArticulo;
                private String codigoArticulo;
                private String descripcion;
                private String unidad;
                private String cantidad;
                private String precioSinIva;
                private String precioConIva;
                private String importeSinIva;
                private String importeConIva;

            }

            @Data
            @XmlAccessorType(XmlAccessType.FIELD)
            public static class Entrega {

                private String plantaEntrega;
                private String calle;
                private String noExterior;
                private String noInterior;
                private String codigoPostal;

            }

            @Data
            @XmlAccessorType(XmlAccessType.FIELD)
            public static class ProveedorMabe {
                private String codigo;
            }

            @Data
            @XmlAccessorType(XmlAccessType.FIELD)
            public static class MonedaMabe {

                private TipoMoneda tipoMoneda;
                private String tipoCambio;
                private String importeConLetra;

                @Getter
                public static enum TipoMoneda {
                    MXN("MXN"),
                    USD("USD"),
                    YEN("YEN"),
                    VEF("VEF");

                    private final String tipoMoneda;

                    TipoMoneda(String tipoMoneda) {
                        this.tipoMoneda = tipoMoneda;
                    }

                    public String getTipoMoneda() {
                        return tipoMoneda;
                    }
                }

            }


        }

        @Data
        @XmlAccessorType(XmlAccessType.FIELD)
        public static class AddendaSantander {

            private List<InformacionPago> informacionPago;
            private InformacionEmision informacionEmision;
            private Inmuebles inmuebles;
            private Basilea basilea;
            private List<CampoAdicional> campoAdicional;

            @Data
            @XmlAccessorType(XmlAccessType.FIELD)
            public static class CampoAdicional {

                private String campo;
                private String valor;

            }

            @Data
            @XmlAccessorType(XmlAccessType.FIELD)
            public static class Basilea {

                private String numContrato;
                private String origenGasto;
                private String tipoGasto;
            }

            @Data
            @XmlAccessorType(XmlAccessType.FIELD)
            public static class Inmuebles {

                private Date fechaVencimiento;
                private String numContrato;
            }

            @Data
            @XmlAccessorType(XmlAccessType.FIELD)
            public static class InformacionPago {

                private String numeroProveedor;
                private String ordenCompra;
                private String posCompra;
                private String nombreBeneficiario;
                private String institucionReceptora;
                private String numeroCuenta;
                private String cuentaContable;
                private String claveDeposito;
                private String email;
                private String codigoISOMoneda;
                private String concepto;
            }

            @Data
            @XmlAccessorType(XmlAccessType.FIELD)
            public static class InformacionEmision {

                private String codigoCliente;
                private String contrato;
                private Date periodo;
                private String centroCostos;
                private String folioInterno;
                private String claveSantander;
                private List<InformacionFactoraje> informacionFactoraje;
            }

            @Data
            @XmlAccessorType(XmlAccessType.FIELD)
            public static class InformacionFactoraje {

                private String deudorProveedor;
                private String tipoDocumento;
                private String numeroDocumento;
                private Date fechaVencimiento;
                private BigDecimal plazo;
                private BigDecimal valorNominal;
                private BigDecimal aforo;
                private BigDecimal precioBase;
                private BigDecimal tasaDescuento;
                private BigDecimal precioFactoraje;
                private BigDecimal importeDescuento;
            }


        }


    }

    @Data
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Hospedaje {
        private String huesped;
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "America/Mexico_City")
        private Date chekIn;
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "America/Mexico_City")
        private Date chekOut;
        private String reserva;
        private String terminal;
        private String habitacion;
        private String folio;
        private BigDecimal paidout;
        private String voucher;
        private String cupon;
        private String extencion;
    }

    @Data
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class TenantBDto {

        private String portal;
    }

    @Data
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Impuestos {

        @XmlElement(name = "totalImpuestosRetenidos", required = false, nillable = true)
        private BigDecimal totalImpuestosRetenidos;
        @XmlElement(name = "totalImpuestosTrasladados", required = false, nillable = true)
        private BigDecimal totalImpuestosTrasladados;
        @XmlElement(name = "retenciones", required = false, nillable = true)
        private List<Retencion> retenciones;
        @XmlElement(name = "traslado", required = false, nillable = true)
        private List<Traslado> traslado;

        @Data
        @XmlAccessorType(XmlAccessType.FIELD)
        public static class Retencion {
            @XmlElement(name = "impuesto", required = true, nillable = false)
            private String impuesto;
            @XmlElement(name = "importe", required = true, nillable = false)
            private BigDecimal importe;
        }

        @Data
        @XmlAccessorType(XmlAccessType.FIELD)
        public static class Traslado {
            @XmlElement(name = "base", required = true, nillable = false)
            private BigDecimal base;
            @XmlElement(name = "impuesto", required = true, nillable = false)
            private String impuesto;
            @XmlElement(name = "tipoFactor", required = true, nillable = false)
            private String tipoFactor;
            private BigDecimal tasaoCuota;
            private BigDecimal importe;


        }
    }

    @Data
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Conceptos {

        @XmlElement(name = "cantidad", required = true, nillable = false)
        private BigDecimal cantidad;
        @XmlElement(name = "unidad", required = false, nillable = true)
        private String unidad;
        @XmlElement(name = "noIdentificacion", required = false, nillable = true)
        private String noIdentificacion;
        @XmlElement(name = "descripcion", required = true, nillable = false)
        private String descripcion;
        @XmlElement(name = "valorUnitario", required = true, nillable = false)
        private BigDecimal valorUnitario;
        @XmlElement(name = "importe", required = true, nillable = false)
        private BigDecimal importe;
        @XmlElement(name = "descuento", required = false, nillable = true)
        private BigDecimal descuento;
        @XmlElement(name = "claveProdServ", required = true, nillable = false)
        private String claveProdServ;
        @XmlElement(name = "cuentaPredial", required = false, nillable = true)
        private List<CuentaPredial> cuentaPredial;
        @XmlElement(name = "informacionAduanera", required = false, nillable = true)
        private List<InformacionAduanera> informacionAduanera;
        @XmlElement(name = "claveUnidad", required = true, nillable = false)
        private String claveUnidad;
        @XmlElement(name = "objetoImp", required = true, nillable = false)
        private String objetoImp;

        @XmlElement(name = "impuestos", required = false, nillable = true)
        //Impuestos Federales
        private ImpuestosConcepto impuestos;
        //Impuestos Locales
        private ImpuestosConceptoLocales impuestosConceptoLocales;

        @XmlElement(name = "aCuentaTerceros", required = false, nillable = true)
        private ACuentaTerceros aCuentaTerceros;

        @Data
        @XmlAccessorType(XmlAccessType.FIELD)
        public static class ACuentaTerceros {

            @XmlElement(name = "rfcACuentaTerceros", required = true)
            private String rfcACuentaTerceros;
            @XmlElement(name = "nombreACuentaTerceros", required = true)
            private String nombreACuentaTerceros;
            @XmlElement(name = "regimenFiscalACuentaTerceros", required = true)
            private String regimenFiscalACuentaTerceros;
            @XmlElement(name = "domicilioFiscalACuentaTerceros", required = true)
            private String domicilioFiscalACuentaTerceros;
        }

        @Data
        @XmlAccessorType(XmlAccessType.FIELD)
        public static class InformacionAduanera {
            @XmlElement(name = "numeroPedimento", required = true, nillable = false)
            private String numeroPedimento;
        }

        @Data
        @XmlAccessorType(XmlAccessType.FIELD)
        public static class CuentaPredial {
            @XmlElement(name = "numero", required = true, nillable = false)
            private String numero;
        }


        @Data
        @XmlAccessorType(XmlAccessType.FIELD)
        public static class ImpuestosConceptoLocales {

            @XmlElement(name = "traslados", required = false, nillable = true)
            private List<Traslados> traslados;
            @XmlElement(name = "retenciones", required = false, nillable = true)
            private List<Retenciones> retenciones;

        }

        @Data
        @XmlAccessorType(XmlAccessType.FIELD)
        public static class ImpuestosConcepto {

            private List<Traslados> traslados;
            private List<Retenciones> retenciones;
        }

        @Data
        @XmlAccessorType(XmlAccessType.FIELD)
        public static class Traslados {

            private BigDecimal base;
            private String impuesto;
            private String tipoFactor;
            private BigDecimal tasaoCuota;
            private BigDecimal importe;
        }

        @Data
        @XmlAccessorType(XmlAccessType.FIELD)
        public static class Retenciones {

            private BigDecimal base;
            private String impuesto;
            private String tipoFactor;
            private BigDecimal tasaoCuota;
            private BigDecimal importe;

        }
    }

    @Data
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Receptor {

        @XmlElement(name = "rfc", required = true, nillable = false)
        private String rfc;
        @XmlElement(name = "nombre", required = true, nillable = false)
        private String nombre;
        @XmlElement(name = "numRegIdTrib", required = false, nillable = true)
        private String numRegIdTrib;
        @XmlElement(name = "usoCFDI", required = true, nillable = false)
        private String usoCFDI;
        @XmlElement(name = "residenciaFiscal", required = false, nillable = true)
        private String residenciaFiscal;
        @XmlElement(name = "regimenFiscal", required = true, nillable = false)
        private String regimenFiscal;
        @XmlElement(name = "domicilioFiscal", required = true, nillable = false)
        private String domicilioFiscal;
        // @XmlTransient
        private String calle;
        // @XmlTransient
        private String noExterior;
        // @XmlTransient
        private String noInterior;
        // @XmlTransient
        private String colonia;
        // @XmlTransient
        private String municipio;
        // @XmlTransient
        private String estado;
        // @XmlTransient
        private String pais;
        @XmlTransient
        private String codigoPostal;
    }

    @Data
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Emisor {

        @XmlElement(name = "rfc", required = true, nillable = false)
        private String rfc;
        @XmlElement(name = "facAtrAdquirente", required = false, nillable = true)
        private String facAtrAdquirente;
        @XmlElement(name = "nombre", required = false, nillable = true)
        private String nombre;
        @XmlElement(name = "regimenFiscal", required = false, nillable = true)
        private String regimenFiscal;
        @XmlTransient
        private String calle;
        @XmlTransient
        private String noExterior;
        @XmlTransient
        private String noInterior;
        @XmlTransient
        private String colonia;
        @XmlTransient
        private String municipio;
        @XmlTransient
        private String estado;
        @XmlTransient
        private String pais;
        @XmlTransient
        private String codigoPostal;

    }

    @Data
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Relacionados {

        @XmlElement(name = "uuid", required = true, nillable = false)
        private List<String> uuid;
        @XmlElement(name = "tipoRelacion", required = true, nillable = false)
        private String tipoRelacion;

    }

    @Getter
    public static enum PuntoVenta {
        FRONT,//0
        POS,  //1
        TENANT_A,//2
        TENANT_B, //3
        TENANT_C,//4
        BCXC,//5
        PROGRMAS_LEALTAD;

        /**
         * Backwards-compatible deserialization. Older API clients may still send
         * the historical (now anonymized) tenant tokens; this preserves the
         * external JSON contract while keeping the public source code free of
         * any proprietary brand identifiers.
         *
         * Accepted aliases (case-insensitive): the original values plus the
         * legacy tokens that were renamed during the open-sourcing.
         */
        @com.fasterxml.jackson.annotation.JsonCreator
        public static PuntoVenta fromString(String value) {
            if (value == null) {
                return null;
            }
            String v = value.trim().toUpperCase(java.util.Locale.ROOT);
            switch (v) {
                case "TENANT_A":
                case "CONECTUM":
                    return TENANT_A;
                case "TENANT_B":
                case "GLOBOGO":
                    return TENANT_B;
                case "TENANT_C":
                case "PROVAC":
                    return TENANT_C;
                case "FRONT":
                    return FRONT;
                case "POS":
                    return POS;
                case "BCXC":
                    return BCXC;
                case "PROGRMAS_LEALTAD":
                case "PROGRAMAS_LEALTAD":
                    return PROGRMAS_LEALTAD;
                default:
                    throw new IllegalArgumentException("Unknown PuntoVenta value: " + value);
            }
        }
    }

    @Getter
    public static enum EnumTipoFactura {
        FACTURA("Factura"),
        NOTA_CREDITO("Nota de Crédito"),
        FACTURA_GLOBAL("Factura Global"),
        RETENCIONES("Retención"),
        NOMINA("Nómina"),
        PAGO("Pago"),
        TRASLADO("Traslado"),
        DONATIVO("Donativo");


        private final String tipoFactura;

        EnumTipoFactura(String tipoFactura) {
            this.tipoFactura = tipoFactura;
        }

        public String getTipoFactura() {
            return tipoFactura;
        }


    }


}
