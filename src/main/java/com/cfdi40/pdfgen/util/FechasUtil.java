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
package com.cfdi40.pdfgen.util; 

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;


public class FechasUtil {

    private static final DateFormat formatoHoraMinuto = new SimpleDateFormat("HH:mm");
    private static final DateFormat formatoHoraMinutoSegundo = new SimpleDateFormat("HH:mm:ss");
    private static final DateFormat formatoFechaDiaMesAnio = new SimpleDateFormat("dd/MM/yyyy");
    private static final DateFormat formatoFechaAnioMesDia = new SimpleDateFormat("yyyy/MM/dd");
    private static final DateFormat formatoFechaDiaMesAnioHoraMinuto = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    private static final DateFormat formatoFechaDiaMesAnioHoraMinutoSegundo = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final DateFormat formatoFechaMesDiaAñoHoraMinSeg = new SimpleDateFormat("MMMM dd, yyyy HH:mm:ss");
    private static final DateFormat formatoFechaMesAnio = new SimpleDateFormat("MM/yyyy");
    private static final DateFormat formatoFechaDiaMes = new SimpleDateFormat("dd/MM");
    private static final DateFormat formatoFechaAnioMesDiaFactura = new SimpleDateFormat("yyyy-MM-dd");
    private static final DateFormat formatoFechaDiaMesAnioFactura = new SimpleDateFormat("dd-MM-yyyy");
    private static final DateFormat formatoFechaHHmmssSSS = new SimpleDateFormat("HHmmssSSS");
    private static final DateFormat formattFechaTimbrado = new SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy", Locale.US);
    public static final int CURRENT_YEAR = LocalDate.now().getYear();

    private static final ThreadLocal<DateFormat> formatoFechaDiaMesAnioHoraMinutoSegundoThread = new ThreadLocal<DateFormat>() {
        @Override
        protected DateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        }
    };

    private static final ThreadLocal<DateFormat> formatoFechaTimbradoThread = new ThreadLocal<DateFormat>() {
        @Override
        protected DateFormat initialValue() {
            return new SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy", Locale.US);
        }
    };

    private static final ThreadLocal<DateFormat> formatoFechaDiaTMesAnioHoraMinutoSegundoThread = new ThreadLocal<DateFormat>() {
        @Override
        protected DateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        }
    };

    /**
     * Metodo para obtener una fecha en formato String yyyy/MM/dd
     *
     * @param fechaDate, fecha que se requiere a dar formato
     * @return String de la fecha indicada como parametro o null si ocurrio un
     * error
     * @throws java.lang.Exception
     */
    public static String getStringFechaAnioMesDia(Date fechaDate) throws Exception {

        try {
            String fechaString = formatoFechaAnioMesDia.format(fechaDate);
            return fechaString;
        } catch (Exception e) {
            System.out.println("Ocurrio un error al tratar de formater la fecha " + fechaDate + " error " + e);
            throw e;
        }
    }

    /**
     * Metodo para obtener un fecha en formato Date yyyy/MM/dd
     *
     * @param fechaString, fecha que se requiere a transformar
     * @return Date con la fecha indicada como parametro o null si ocurrio un
     * error
     * @throws java.lang.Exception
     */
    public static Date getDateFechaAnioMesDia(String fechaString) throws Exception {

        try {

            Date fechaDate = formatoFechaAnioMesDia.parse(fechaString);
            return fechaDate;
        } catch (Exception e) {
            System.out.println("Ocurrio un error al tratar de formater la fecha " + fechaString + " error " + e);
            throw e;
        }
    }

    public static Date formatDateFechaAnioMesDia(Date fecha) throws Exception {

        try {

            return fecha = formatoFechaAnioMesDia.parse(getStringFechaAnioMesDia(fecha));

        } catch (Exception e) {
            System.out.println("Ocurrio un error al tratar de formater la fecha " + fecha + " error " + e);
            throw e;
        }
    }

    /**
     * Metodo que convierte un date con la fecha en formato yyyy-MM-dd
     *
     * @param fecha
     * @return String con la fecha con el formato yyyy-MM-dd o null si ocurrio
     * un error
     * @throws java.lang.Exception
     */
    public static String formatDateFechaAnioMesDiaFactura(Date fecha) throws Exception {

        try {
            String fechaString = formatoFechaAnioMesDiaFactura.format(fecha);
            return fechaString;
        } catch (Exception e) {
            System.out.println("Ocurrio un error al tratar de formater la fecha " + fecha + " error " + e);
            throw e;
        }
    }

    /**
     * Metodo que convierte un date con la fecha en formato dd-MM-yyyy
     *
     * @param fecha, Date a convertir
     * @return, String con la fecha en el formato dd-MM-yyyy o null si ocurrio
     * un error
     * @throws java.lang.Exception
     */
    public static String formatDateDiaMesAnio(Date fecha) throws Exception {

        try {
            String fechaString = formatoFechaDiaMesAnioFactura.format(fecha);
            return fechaString;
        } catch (Exception e) {
            System.out.println("Ocurrio un error al tratar de formater la fecha " + fecha + " error " + e);
            throw e;
        }

    }

    /**
     * Metodo para darle formato MM/yyyy a un Date
     *
     * @param fecha, fecha en formato MM/yyyy
     * @return
     * @throws java.lang.Exception
     */
    public static Date formatDateMesAnio(Date fecha) throws Exception {

        try {
            String fechaString = formatoFechaMesAnio.format(fecha);
            fecha = formatoFechaMesAnio.parse((fechaString));
            return fecha;

        } catch (Exception e) {
            System.out.println("Ocurrio un error al tratar de formater la fecha " + fecha + " error " + e);
            throw e;
        }
    }

    /**
     * Metodo para obtener los datos de hora, minuto , segundo y milisegundo de
     * un date en formado HHmmssSS
     *
     * @param date
     * @return
     * @throws Exception
     */
    public static String getStringHHmmssSS(Date date) throws Exception {

        try {
            String hora = formatoFechaHHmmssSSS.format(date);
            return hora;

        } catch (Exception e) {
            System.out.println("Ocurrio un error al tratar de formater la fecha " + date + " error " + e);
            throw e;
        }
    }

    /**
     * Metodo que convertir un String a Date en formato yyyy-MM-dd HH:mm:ss
     *
     * @param fecha, fecha en formato String que será convertida
     * @return Date con la fecha en el formato yyyy-MM-dd HH:mm:ss
     * @throws Exception, si ocurre un errro al tratar de transformar la fecha
     */
    public static Date getDateAnioMesDiaHoraMinSeg(String fecha) throws Exception {
        try {
            return formatoFechaDiaMesAnioHoraMinutoSegundoThread.get().parse(fecha);
        } catch (Exception e) {
            throw e;
        }
    }

    public static Date getDateFechaTimbrado(String fecha) throws ParseException {

        return formattFechaTimbrado.parse(fecha);
    }

    /**
     * Metodo para obtener una fecha en String con formato MM/yyyy a partir de
     * un Date
     *
     * @param fecha
     * @return
     * @throws Exception
     */
    public static String getStringMesAnio(Date fecha) throws Exception {

        try {
            return formatoFechaMesAnio.format(fecha);
        } catch (Exception e) {
            System.out.println("Ocurrio un error al tratar de formater la fecha " + fecha + " error " + e);
            throw e;
        }
    }

    /**
     * Metodo que convertir un Date a String en formato yyyy-MM-dd HH:mm:ss
     *
     * @param date
     * @return String con la fecha en el formato yyyy-MM-dd HH:mm:ss
     * @throws Exception, si ocurre un errro al tratar de transformar la fecha
     */
    public static String getStringAnioMesDiaHoraMinSeg(Date date) throws Exception {
        try {
            return formatoFechaDiaMesAnioHoraMinutoSegundo.format(date);
        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * Metodo que convertir un Date a String en formato yyyy-MM-dd HH:mm:ss
     *
     * @param date
     * @return String con la fecha en el formato yyyy-MM-dd HH:mm:ss
     * @throws Exception, si ocurre un errro al tratar de transformar la fecha
     */
    public static String getStringMesDiaAnioHoraMinSeg(Date date) throws Exception {
        try {
            return formatoFechaMesDiaAñoHoraMinSeg.format(date);
        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * Metodo que formate una fecha string a YYYY-MM-DD
     *
     * @param fecha, string con la fecha
     * @return string con la fecha con el formato
     * @throws Exception
     */
    public static String getStringAnioMesDia(Date fecha) throws Exception {

        try {
            return formatoFechaAnioMesDiaFactura.format(fecha);
        } catch (Exception e) {
            System.out.println("Ocurrio un error al tratar de formater la fecha " + fecha + " error " + e);
            throw e;
        }
    }

    public static XMLGregorianCalendar gregorianCalendarAnioMesDia(Date date) throws DatatypeConfigurationException {
        return DatatypeFactory.newInstance().newXMLGregorianCalendar(formatoFechaAnioMesDiaFactura.format(date));
    }

    /**
     * Método que resta los meses especificados a una fecha.
     *
     *
     * @param fecha Date fecha.
     * @param dias int dias a restar en negativo
     * @return nueva fecha con los meses restados.
     */
    public static Date restaDiasFecha(Date fecha, int dias) {

        Calendar calendar = Calendar.getInstance();

        try {

            calendar.setTime(fecha);
            calendar.add(Calendar.DAY_OF_MONTH, -dias);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return calendar.getTime();
    }

    public static String getStringFechaTimbrado(Date fecha) {
        return formatoFechaDiaMesAnioHoraMinutoSegundoThread.get().format(fecha);
    }
    
    public static String getDateAnioMesDiaHoraMinSeg(Date fecha) {
        return formatoFechaDiaTMesAnioHoraMinutoSegundoThread.get().format(fecha);
    }
    
     public static Date getDateFechaAnioMesDiaYYYYMMdd(String fecha) throws Exception {
        try {
            return formatoFechaAnioMesDiaFactura.parse(fecha);
        } catch (Exception e) {
            System.out.println("Ocurrio un error al tratar de formatear la fecha " + fecha + " error " + e);
            throw e;
        }
    }
     
     public static int getYearFromDate(Date date) {

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.YEAR);
    }
     
     
}
