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
public class Hinta implements Comparable<Hinta> {

    private final Kauppa kauppa;
    private final long hinta;
    private final long postikulut;
    private final String toimitusaika;

    public Hinta(Kauppa kauppa, long hinta, long postikulut, String toimitusaika) {
        this.kauppa = kauppa;
        this.hinta = hinta;
        this.postikulut = postikulut;
        this.toimitusaika = toimitusaika;
    }

    public Kauppa getKauppa() {
        return kauppa;
    }

    public long getHinta() {
        return hinta;
    }

    public long getPostikulut() {
        return postikulut;
    }

    public String getToimitusaika() {
        return toimitusaika;
    }

    public long getSuodatettuHinta() {
        return isSuodataPois() ? -1 : isVoiNoutaa() ? hinta : getHintaKuluineen();
    }

    public long getSuodatetutPostikulut() {
        return isSuodataPois() ? -1 : isVoiNoutaa() ? 0 : postikulut;
    }

    public long getHintaKuluineen() {
        return hinta == -1 || postikulut == -1 ? -1 : hinta + postikulut;
    }

    public String getKaupanNimi() {
        return kauppa.getKaupanNimi();
    }

    public boolean isVoiNoutaa() {
        return kauppa.isVoiNoutaa();
    }

    public void setVoiNoutaa(boolean voiNoutaa) {
        kauppa.setVoiNoutaa(voiNoutaa);
    }

    public boolean isSuodataPois() {
        return kauppa.isSuodataPois();
    }

    public void setSuodataPois(boolean suodataPois) {
        kauppa.setSuodataPois(suodataPois);
    }

    @Override
    public int compareTo(Hinta t) {
        long suodatettu1 = t.getSuodatettuHinta();
        long suodatettu2 = getSuodatettuHinta();

        //(suodatettu1 ^ suodatettu2) < 0 onko samalla puolella nollaa?
        int ekataso = (suodatettu1 ^ suodatettu2) < 0 ? 0 : suodatettu1 > suodatettu2 ? -1 : 1;
        return ekataso == 0 ? Long.compare(suodatettu1, suodatettu2) : ekataso;
    }
}
