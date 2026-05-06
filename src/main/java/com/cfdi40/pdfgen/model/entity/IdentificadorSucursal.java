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
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.cfdi40.pdfgen.util.EnumTipoProcesoTimbrado;

import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "identificador_sucursal")
@Data
@NoArgsConstructor
public class IdentificadorSucursal implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "ID")
    private Integer id;

    @Basic(optional = false)
    @Column(name = "IDENTIFICADOR")
    private String identificador;
    
    @JoinColumn(name = "SUCURSAL_ID", referencedColumnName = "ID")
    @ManyToOne(optional = false)
    private Sucursal sucursalId;
    
    @JoinColumn(name = "EMISOR_ID", referencedColumnName = "ID")
    @ManyToOne
    private Emisor emisorId;
    
    @Basic(optional = false)
    @Column(name = "TIPO_ELABORACION")
    private EnumTipoProcesoTimbrado tipoElaboracion;

   
 
}
