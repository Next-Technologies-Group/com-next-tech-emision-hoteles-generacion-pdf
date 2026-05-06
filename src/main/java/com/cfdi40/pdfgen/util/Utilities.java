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
import java.io.File;
import java.io.FileInputStream;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import com.cfdi40.pdfgen.model.entity.CatTagxidioma;
import com.cfdi40.pdfgen.tenants.tenanta.cfdi.dto.CFDI;

public class Utilities {

   
    public static HashMap<EnumTagPlantilla, String> obtenerTagByIdioma(List<CatTagxidioma> lstTagIdioma) {
        // Pre-cargar todos los enums con un valor por defecto (nombre legible)
        // para garantizar que map.get(enum) nunca retorne null aún sin filas en BD.
        HashMap<EnumTagPlantilla, String> hmapTagIdioma = new HashMap<>();
        HashMap<Integer, EnumTagPlantilla> mapEnumsLocales = new HashMap<>();
        try {
            EnumTagPlantilla[] lstEnumLocales = EnumTagPlantilla.values();
            for (EnumTagPlantilla enumTagPlantilla : lstEnumLocales) {
                mapEnumsLocales.put(enumTagPlantilla.ordinal(), enumTagPlantilla);
                // Default: nombre del enum sin prefijo P_/E_/G_/R_/etc.
                String name = enumTagPlantilla.name();
                int us = name.indexOf('_');
                String pretty = us >= 0 && us < name.length() - 1 ? name.substring(us + 1) : name;
                hmapTagIdioma.put(enumTagPlantilla, pretty);
            }
            if (lstTagIdioma != null) {
                for (CatTagxidioma vo : lstTagIdioma) {
                    EnumTagPlantilla enumLocal = mapEnumsLocales.get(vo.getEnumTag());
                    if (enumLocal != null) {
                        hmapTagIdioma.put(enumLocal, vo.getValor());
                    }
                }
            }
        } catch (Exception e)  {
            e.printStackTrace();
        }
        return hmapTagIdioma;
    }
    /**
     * Metodo para obtener el total letra correspondiente al monto generado del
     * total * Todos los formatos
     *
     * @return String totalLetra
     */
    public static String crearTotalLetra(CFDI facturaDto ) {
        try {

            double totalDouble = facturaDto.getTotal() != null ? facturaDto.getTotal().doubleValue() : 0;
            switch (facturaDto.getIdioma() != null && !facturaDto.getIdioma().trim().isEmpty() ? facturaDto.getIdioma() : ConstantesFacto.IDIOMA_ESANOL) {
                case ConstantesFacto.IDIOMA_ESANOL:

                    return NumberToLetterConverter.convertNumberToLetterByIdioma(totalDouble, ConstantesFacto.IDIOMA_ESANOL, facturaDto.getMoneda());
                case ConstantesFacto.IDIOMA_INGLES:

                    return NumberToLetterConverter.convertNumberToLetterByIdioma(totalDouble, ConstantesFacto.IDIOMA_INGLES, facturaDto.getMoneda());
                default:
                    return NumberToLetterConverter.convertNumberToLetterByIdioma(totalDouble, ConstantesFacto.IDIOMA_ESANOL, facturaDto.getMoneda());
            }

        } catch (Exception e) {
            System.err.println("No se pudo crear el Total con letra del PDF, Error: " + e);
            return "";
        }
    }

    public static String bigDecimalToStr(BigDecimal bigDecimal) {
        try {
            return "$" + String.format(Locale.US, "%,.2f", bigDecimal);
        } catch (NumberFormatException ex) {
            return bigDecimal.toPlainString();
        }
    }

    /**
     * Metodo para obtener un arreglo de bytes con el contenido de un archivo
     *
     * @param file
     * @return
     * @throws Exception
     */
    public static byte[] getBytesFile(File file) throws Exception {

        FileInputStream inputStream = null;
        try {
            byte[] bytesFile = null;
            inputStream = new FileInputStream(file);
            bytesFile = new byte[(int) file.length()];
            inputStream.read(bytesFile);
            return bytesFile;

        } catch (Exception e) {
            System.err.println("Ocurrio un error al tratar de obtener lo bytes de un archivo");
            throw e;
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (Exception e) {
                System.err.println("Ocurrio un error al tratar de cerra el stream de un file input");
            }
        }
    }

    public static String toFormatMoneda(Object numer) {
        try {
            String format = String.format(Locale.US, "%,.2f", numer);
            return "$ " + format;
        } catch (Exception e) {
            return "";
        }
    }
     
}
