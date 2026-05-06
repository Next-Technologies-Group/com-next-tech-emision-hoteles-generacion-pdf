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
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cfdi40.pdfgen.model.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.springframework.lang.NonNull;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "emisor")
public class Emisor implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "ID")
    private Integer id;
    // @Size(max = 255)
    @Column(name = "RFC")
    private String rfc;
    // @Size(max = 500)
    @Column(name = "NOMBRE")
    private String nombre;
    // @Size(max = 500)
    @Column(name = "RAZON_SOCIAL")
    private String razonSocial;
    // @Size(max = 255)
    @Column(name = "AP_PATERNO")
    private String apPaterno;
    // @Size(max = 255)
    @Column(name = "AP_MATERNO")
    private String apMaterno;
    // @Size(max = 255)
    @Column(name = "REGIMEN_FISCAL")
    private String regimenFiscal;
    @Column(name = "ES_PERSONA_FISICA")
    private Boolean esPersonaFisica;
    // @Size(max = 255)
    @Column(name = "PAIS")
    private String pais;
    // @Size(max = 255)
    @Column(name = "ESTADO")
    private String estado;
    // @Size(max = 255)
    @Column(name = "COLONIA")
    private String colonia;
    // @Size(max = 255)
    @Column(name = "CALLE")
    private String calle;
    // @Size(max = 255)
    @Column(name = "NUMERO_INTERIOR")
    private String numeroInterior;
    // @Size(max = 255)
    @Column(name = "NUMERO_EXTERIOR")
    private String numeroExterior;
    // @Size(max = 255)
    @Column(name = "CIUDAD_DELEGACION")
    private String ciudadDelegacion;
    // @Size(max = 255)
    @Column(name = "CODIGO_POSTAL")
    private String codigoPostal;
    // @Size(max = 255)
    @Column(name = "TIPO_SELLADO")
    private String tipoSellado;
    // @Size(max = 25)
    @Column(name = "VERSION")
    private String version;
    // @Size(max = 255)
    @Column(name = "LLAVE_CERTIFICADO")
    private String llaveCertificado;
    // @Size(max = 255)
    @Column(name = "NUM_CERTIFICADO")
    private String numCertificado;
    // @Size(max = 50)
    @Column(name = "PASSWORD_CERTIFICADO")
    private String passwordCertificado;
    // @Size(max = 255)
    @Column(name = "FILE_CERTIFICADO")
    private String fileCertificado;
    @Column(name = "FEC_FINAL_CER")
    @Temporal(TemporalType.DATE)
    private Date fecFinalCer;
    @Basic(optional = false)
    @NonNull
    // @Size(min = 1, max = 255)
    @Column(name = "RUTA_CERTIFICADOS")
    private String rutaCertificados;
    @Basic(optional = false)
    @NonNull
    // @Size(min = 1, max = 255)
    @Column(name = "RUTA_XMLS")
    private String rutaXmls;
    @Basic(optional = false)
    @NonNull
    // @Size(min = 1, max = 255)
    @Column(name = "RUTA_PDFS")
    private String rutaPdfs;
    // @Size(max = 255)
    @Column(name = "RUTA_TRANSACCION")
    private String rutaTransaccion;
    @Basic(optional = false)
    @NonNull
    // @Size(min = 1, max = 45)
    @Column(name = "RUTA_PLANTILLA")
    private String rutaPlantilla;
    @Column(name = "RUTA_DESCARGAS")
    private String rutaDescargas;
    // @Size(max = 255)
    @Column(name = "LOGO_EMISOR")
    private String logoEmisor;
    @Column(name = "ACTIVO")
    private Boolean activo;
    @Column (name = "ULTIMA_MODIFICACION")
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date ultimaModificacion;
    @Column (name = "USUARIO_ID")
    private Integer usuarioId;
    
    // @JoinColumn(name = "CORPORATIVO_ID", referencedColumnName = "ID")
    // @ManyToOne(optional = false)
    // private Corporativo corporativoId;
    
    @OneToMany(cascade = CascadeType.ALL,mappedBy = "emisorId")
    private List<IdentificadorSucursal> listIdentificadorSucursal;

    // @OneToMany(cascade = CascadeType.ALL, mappedBy = "emisorId")
    // private List<MapeoAddendaMabeEntrega> mapeoAddendaMabeEntrega;
    
    @Basic(optional = false)
    @Column(name = "CERTIFICADO_ID")
    private Integer certificadoId;


}
