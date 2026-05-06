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

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;


public class NumberToLetterConverter {

    private static final String[] UNIDADES = {"", "UN ", "DOS ", "TRES ",
        "CUATRO ", "CINCO ", "SEIS ", "SIETE ", "OCHO ", "NUEVE ", "DIEZ ",
        "ONCE ", "DOCE ", "TRECE ", "CATORCE ", "QUINCE ", "DIECISEIS ",
        "DIECISIETE ", "DIECIOCHO ", "DIECINUEVE ", "VEINTE "};
    private static final String[] DECENAS = {"VEINTI", "TREINTA ", "CUARENTA ",
        "CINCUENTA ", "SESENTA ", "SETENTA ", "OCHENTA ", "NOVENTA ",
        "CIEN "};
    private static final String[] CENTENAS = {"CIENTO ", "DOSCIENTOS ",
        "TRESCIENTOS ", "CUATROCIENTOS ", "QUINIENTOS ", "SEISCIENTOS ",
        "SETECIENTOS ", "OCHOCIENTOS ", "NOVECIENTOS "};
    private static final String[] UNITS = {"", "ONE ", "TWO ", "THREE ",
        "FOUR ", "FIVE ", "SIX ", "SEVEN ", "EIGHT ", "NINE ", "TEN ",
        "ELEVEN ", "TWELVE ", "THIRTEEN ", "FOURTEEN ", "FIFTEEN ", "SIXTEEN ",
        "SEVENTEEN ", "EIGHTEEN ", "NINETEEN ", "TWENTY "};
    private static final String[] TENS = {"TWENTY", "THIRTY ", "FORTY ",
        "FIFTY ", "SIXTY ", "SEVENTY ", "EIGHTY ", "NINETY ",
        "ONE HUNDRED "};
    private static final String[] HUNDREDS = {"ONE HUNDRED ", "TWO HUNDRED ",
        "THREE HUNDRED ", "FOUR HUNDRED ", "FIVE HUNDRED ", "SIX HUNDRED ",
        "SEVEN HUNDRED ", "EIGHT HUNDRED ", "NINE HUNDRED "};

    /**
     * Convierte un numero en representacion numerica a uno en representacion de
     * texto. El numero es valido si esta entre 0 y 999'999.999
     * <p>
     * Creation date 3/05/2006 - 05:37:47 PM
     *
     * @param number Numero a convertir
     * @return Numero convertido a texto
     * @throws NumberFormatException Si el numero esta fuera del rango
     * @since 1.0
     */
    public static String convertNumberToLetter(double number, String moneda) {
        String letra = "";
        String centavos = "";
        if (moneda.equals("MXN")) {
            letra = convertNumberToLetter(number);
            centavos = convertCentavosToLetter(number);
        } else if (moneda.equals("USD")) {
            letra = convertNumberToLetter_EN(number);
            centavos = convertCentavosToLetter_EN(number);
        } else if (moneda.equals("EUR")) {     
            letra = convertNumberToLetter_EN(number);
            centavos = convertCentavosToLetter_EU(number);            
        }
        return letra + centavos;
    }

    public static String convertNumberToLetter(double number) throws NumberFormatException {
        String converted = new String();

        // Validamos que sea un numero legal
        //double doubleNumber = Math.round(number);
        double doubleNumber = number;
        if (doubleNumber > 999999999) {
            throw new NumberFormatException("El numero es mayor de 999,999'999.999, " + "no es posible convertirlo");
        }

        //String splitNumber[] = String.valueOf(doubleNumber).replace('.', '#').split("#");
        DecimalFormat dc = new DecimalFormat("#.##", DecimalFormatSymbols.getInstance(Locale.US));
        dc.format(number);

        //String splitNumber[]= String.format("%14.2f", bdoubleNumber.floatValue()).trim().replace('.', '#').split("#");
        String splitNumber[] = dc.format(number).replace("-", "").replace('.', '#').split("#");
//                System.out.println("Cadena convertida:"+String.valueOf(doubleNumber).replace('.', '#'));
        // Descompone el trio de millones - ¡SGT!
        int millon = Integer.parseInt(
                String.valueOf(getDigitAt(splitNumber[0], 8))
                + String.valueOf(getDigitAt(splitNumber[0], 7))
                + String.valueOf(getDigitAt(splitNumber[0], 6)));
        if (millon == 1) {
            converted = "UN MILLON ";
        }
        if (millon > 1) {
            converted = convertNumber(String.valueOf(millon)) + "MILLONES ";
        }

        // Descompone el trio de miles - ¡SGT!
        int miles = Integer.parseInt(
                String.valueOf(getDigitAt(splitNumber[0], 5))
                + String.valueOf(getDigitAt(splitNumber[0], 4))
                + String.valueOf(getDigitAt(splitNumber[0], 3)));
        if (miles == 1) {
            converted += "UN MIL ";
        }
        if (miles > 1) {
            converted += convertNumber(String.valueOf(miles)) + "MIL ";
        }

        // Descompone el ultimo trio de unidades - ¡SGT!
        int cientos = Integer.parseInt(
                String.valueOf(getDigitAt(splitNumber[0], 2))
                + String.valueOf(getDigitAt(splitNumber[0], 1))
                + String.valueOf(getDigitAt(splitNumber[0], 0)));
        if (cientos == 1) {
            converted += "UN ";
        }

        if (millon + miles + cientos == 0) {
            converted += "CERO ";
        }
        if (cientos > 1) {
            converted += convertNumber(String.valueOf(cientos));
        }
        return converted;
    }

    public static String convertNumberToLetter_EN(double number)
            throws NumberFormatException {
        String converted = new String();

        // Validamos que sea un numero legal
        //double doubleNumber = Math.round(number);
        double doubleNumber = number;
        if (doubleNumber > 999999999) {
            throw new NumberFormatException("El numero es mayor de 999'999.999, " + "no es posible convertirlo");
        }

        DecimalFormat dc = new DecimalFormat("#.##", DecimalFormatSymbols.getInstance(Locale.US));
        dc.format(number);

        //String splitNumber[] = String.valueOf(doubleNumber).replace('.', '#').split("#");
        String splitNumber[] = dc.format(number).replace('.', '#').split("#");
//                System.out.println("Cadena convertida:"+String.valueOf(doubleNumber).replace('.', '#'));
        // Descompone el trio de millones - ¡SGT!
        int millon = Integer.parseInt(
                String.valueOf(getDigitAt_EN(splitNumber[0], 8))
                + String.valueOf(getDigitAt_EN(splitNumber[0], 7))
                + String.valueOf(getDigitAt_EN(splitNumber[0], 6)));
        if (millon == 1) {
            converted = "ONE MILLION ";
        }
        if (millon > 1) {
            converted = convertNumber_EN(String.valueOf(millon)) + "MILLION ";
        }

        // Descompone el trio de miles - ¡SGT!
        int miles = Integer.parseInt(
                String.valueOf(getDigitAt_EN(splitNumber[0], 5))
                + String.valueOf(getDigitAt_EN(splitNumber[0], 4))
                + String.valueOf(getDigitAt_EN(splitNumber[0], 3)));
        if (miles == 1) {
            converted += "ONE THOUSAND ";
        }
        if (miles > 1) {
            converted += convertNumber_EN(String.valueOf(miles)) + "THOUSAND ";
        }

        // Descompone el ultimo trio de unidades - ¡SGT!
        int cientos = Integer.parseInt(
                String.valueOf(getDigitAt_EN(splitNumber[0], 2))
                + String.valueOf(getDigitAt_EN(splitNumber[0], 1))
                + String.valueOf(getDigitAt_EN(splitNumber[0], 0)));
        if (cientos == 1) {
            converted += "ONE ";
        }

        if (millon + miles + cientos == 0) {
            converted += "ZERO ";
        }
        if (cientos > 1) {
            converted += convertNumber_EN(String.valueOf(cientos));
        }

        return converted;
    }

    public static String convertCentavosToLetter(double number)
            throws NumberFormatException {
        String converted = new String();
        double doubleNumber = number;
        if (doubleNumber > 999999999) {
            throw new NumberFormatException("El numero es mayor de 999'999.999, " + "no es posible convertirlo");
        }

        DecimalFormat dc = new DecimalFormat("#.##", DecimalFormatSymbols.getInstance(Locale.US));
        dc.format(number);
        String splitNumber[] = dc.format(number).replace('.', '#').split("#");
        DecimalFormat df = new DecimalFormat("00");
        if (splitNumber.length == 1) {
            converted += " PESOS 00/100";
        } else {
            if (splitNumber[1].length() == 1) {
                converted += " PESOS " + splitNumber[1] + "0/100";
            } else {
                converted += " PESOS  " + splitNumber[1] + "/100";
            }
        }
        return converted;
    }

    public static String convertCentavosToLetter_EN(double number)
            throws NumberFormatException {
        String converted = new String();
        double doubleNumber = number;
        if (doubleNumber > 999999999) {
            throw new NumberFormatException("El numero es mayor de 999'999.999, " + "no es posible convertirlo");
        }
        DecimalFormat dc = new DecimalFormat("#.##", DecimalFormatSymbols.getInstance(Locale.US));
        dc.format(number);
        //String splitNumber[] = String.valueOf(doubleNumber).replace('.', '#').split("#");
        String splitNumber[] = dc.format(number).replace('.', '#').split("#");
        DecimalFormat df = new DecimalFormat("00");
        if (splitNumber.length == 1) {            
            converted += " 00/100";
        } else {
            if (splitNumber[1].length() == 1) {
                converted += " " + splitNumber[1] + "0/100";
            } else {
                converted += " " + splitNumber[1] + "/100";
            }
        }
        return converted;
    }
    
     public static String convertCentavosToLetter_EU(double number)
            throws NumberFormatException {
        String converted = new String();
        double doubleNumber = number;
        if (doubleNumber > 999999999) {
            throw new NumberFormatException("El numero es mayor de 999'999.999, " + "no es posible convertirlo");
        }
        DecimalFormat dc = new DecimalFormat("#.##", DecimalFormatSymbols.getInstance(Locale.US));
        dc.format(number);
        //String splitNumber[] = String.valueOf(doubleNumber).replace('.', '#').split("#");
        String splitNumber[] = dc.format(number).replace('.', '#').split("#");
        DecimalFormat df = new DecimalFormat("00");
        if (splitNumber.length == 1) {            
            converted += " EURO 00/100";
        } else {
            if (splitNumber[1].length() == 1) {
                converted += " EUROS " + splitNumber[1] + "0/100";
            } else {
                converted += " EUROS " + splitNumber[1] + "/100";
            }
        }
        return converted;
    }

    /**
     * Convierte los trios de numeros que componen las unidades, las decenas y
     * las centenas del numero.
     * <p>
     * Creation date 3/05/2006 - 05:33:40 PM
     *
     * @param number Numero a convetir en digitos
     * @return Numero convertido en letras
     * @since 1.0
     */
    private static String convertNumber(String number) {
        if (number.length() > 3) {
            throw new NumberFormatException(
                    "La longitud maxima debe ser 3 digitos");
        }

        String output = new String();
        if (getDigitAt(number, 2) != 0) {
            output = CENTENAS[getDigitAt(number, 2) - 1];
        }

        int k = Integer.parseInt(String.valueOf(getDigitAt(number, 1))
                + String.valueOf(getDigitAt(number, 0)));

        if (k <= 20) {
            output += UNIDADES[k];
        } else {
            if (k > 30 && getDigitAt(number, 0) != 0) {
                output += DECENAS[getDigitAt(number, 1) - 2] + "Y "
                        + UNIDADES[getDigitAt(number, 0)];
            } else {
                output += DECENAS[getDigitAt(number, 1) - 2]
                        + UNIDADES[getDigitAt(number, 0)];
            }
        }

        // Caso especial con el 100
        if (getDigitAt(number, 2) == 1 && k == 0) {
            output = "CIEN ";
        }

        return output;
    }

    private static String convertNumber_EN(String number) {
        if (number.length() > 3) {
            throw new NumberFormatException(
                    "La longitud maxima debe ser 3 digitos");
        }

        String output = new String();
        if (getDigitAt_EN(number, 2) != 0) {
            output = HUNDREDS[getDigitAt_EN(number, 2) - 1];
        }

        int k = Integer.parseInt(String.valueOf(getDigitAt_EN(number, 1))
                + String.valueOf(getDigitAt_EN(number, 0)));

        if (k <= 20) {
            output += UNITS[k];
        } else {
            if (k > 30 && getDigitAt_EN(number, 0) != 0) {
                output += TENS[getDigitAt_EN(number, 1) - 2] + "  "
                        + UNITS[getDigitAt_EN(number, 0)];
            } else {
                output += TENS[getDigitAt_EN(number, 1) - 2]
                        + UNITS[getDigitAt_EN(number, 0)];
            }
        }

        // Caso especial con el 100
        if (getDigitAt_EN(number, 2) == 1 && k == 0) {
            output = "ONE HUNDRED ";
        }

        return output;
    }

    /**
     * Retorna el digito numerico en la posicion indicada de derecha a izquierda
     * <p>
     * Creation date 3/05/2006 - 05:26:03 PM
     *
     * @param origin Cadena en la cual se busca el digito
     * @param position Posicion de derecha a izquierda a retornar
     * @return Digito ubicado en la posicion indicada
     * @since 1.0
     */
    private static int getDigitAt(String origin, int position) {
        if (origin.length() > position && position >= 0) {
            return origin.charAt(origin.length() - position - 1) - 48;
        }
        return 0;
    }

    private static int getDigitAt_EN(String origin, int position) {
        if (origin.length() > position && position >= 0) {
            return origin.charAt(origin.length() - position - 1) - 48;
        }

        return 0;
    }

    public static String convertNumberToLetterByIdioma(double number, String idioma, String moneda) {

        String letra = "";
        String centavos = "";
        StringBuilder totalTexto = new StringBuilder();

        try {
            //Total en letra
            if (idioma.equals("SP")) {
                letra = convertNumberToLetter(number);
            } else if (idioma.equals("EN")) {
                letra = convertNumberToLetter_EN(number);
            }

            //centavos y tipo cambio
            if (moneda.equals("MXN") || moneda.equals("MXP")) {
                centavos = convertCentavosToLetter(number);
                centavos = centavos.replace("moneda", "PESOS");
            } else if (moneda.equals("USD") || moneda.equals("DLS")) {
                centavos = convertCentavosToLetter_EN(number);
                centavos = centavos.replace("moneda", "DLLS");
            } else if(moneda.equals("EUR")){
                centavos = convertCentavosToLetter_EN(number);
                centavos = centavos.replace("moneda", "EUROS");
            } else {
                centavos = convertCentavosToLetter(number);
                centavos = centavos.replace("moneda", "PESOS");
            }

            totalTexto.append(letra).append(centavos);

            //asignamos la moneda
            if (moneda.equals("MXN")) {
                totalTexto.append(" M.N.");
            } else if (moneda.equals("USD")) {
                totalTexto.append(" U.S.D.");
            } else if (moneda.equals("DLS")) {
                totalTexto.append(" DLS");
            } else if (moneda.equals("EUR")) {
                totalTexto.append(" EUR");
            } else {
                totalTexto.append(" M.N.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return totalTexto.toString();
    }
}
