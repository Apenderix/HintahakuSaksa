/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hintahakuSaksa;

/**
 *
 * @author Tuupertunut
 */
public class Muunnin {

    public static long stringToLong(String hinta) {
        try {
            return Long.parseLong(hinta.replace(" ", "").replace(",", "").replace("€", ""));
        } catch (NumberFormatException ex) {
            return -1;
        }
    }

    public static String longToString(long hinta) {
        if (hinta == -1) {
            return "-";
        }
        StringBuilder numero = new StringBuilder(Long.toString(hinta));
        while (numero.length() < 3) {
            numero.insert(0, "0");
        }
        return numero.insert(numero.length() - 2, ",").append(" €").toString();
    }
}
