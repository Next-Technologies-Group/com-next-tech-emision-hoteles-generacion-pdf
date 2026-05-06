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
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;

import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "sucursal")
@Data
@NoArgsConstructor
public class Sucursal implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "ID")
    private Integer id;
    //@Size(max = 255)
    @Column(name = "NOMBRE")
    private String nombre;
    //@Size(max = 255)
    @Column(name = "PAIS")
    private String pais;
    //@Size(max = 255)
    @Column(name = "ESTADO")
    private String estado;
    //@Size(max = 255)
    @Column(name = "CODIGO_POSTAL")
    private String codigoPostal;
    //@Size(max = 244)
    @Column(name = "NUMERO_INTERIOR")
    private String numeroInterior;
    //@Size(max = 255)
    @Column(name = "NUMERO_EXTERIOR")
    private String numeroExterior;
    //@Size(max = 255)
    @Column(name = "CIUDAD_DELEGACION")
    private String ciudadDelegacion;
    //@Size(max = 255)
    @Column(name = "CALLE")
    private String calle;
    //@Size(max = 255)
    @Column(name = "COLONIA")
    private String colonia;
    //@Size(max = 255)
    @Column(name = "TELEFONO")
    private String telefono;
    //@Size(max = 255)
    @Column(name = "EMAIL")
    private String email;
    //@Size(max = 255)
    @Column(name = "NOMBRE_LOGO")
    private String nombreLogo;
    @Column(name = "ULTIMA_MODIFICACION")
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date ultimaModificacion;
    @Column(name = "USUARIO_ID")
    private Integer usuarioId;

    @Column(name = "EMAIL_CONTRALOR")
    private String emailContralor;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "sucursalId")
    private List<IdentificadorSucursal> listIdentificadorSucursal;
    
}
