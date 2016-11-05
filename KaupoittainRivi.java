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
public class KaupoittainRivi {

    private final String kaupanNimi;
    private long hinta;
    private long postikulut;
    private int maara;

    public KaupoittainRivi(String kaupanNimi, long hinta, long postikulut, int maara) {
        this.kaupanNimi = kaupanNimi;
        this.hinta = hinta;
        this.postikulut = postikulut;
        this.maara = maara;
    }

    public long getHinta() {
        return hinta;
    }

    public void setHinta(long hinta) {
        this.hinta = hinta;
    }

    public int getMaara() {
        return maara;
    }

    public void setMaara(int maara) {
        this.maara = maara;
    }

    public String getKaupanNimi() {
        return kaupanNimi;
    }

    public long getPostikulut() {
        return postikulut;
    }

    public void setPostikulut(long postikulut) {
        this.postikulut = postikulut;
    }
}
