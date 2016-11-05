/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hintahakuSaksa;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Tuupertunut
 */
public class Kokoonpano {

    private static Path tiedosto;
    private static final EventList<KokoonpanoTuote> tuotteetEventList = new BasicEventList<>();
    private static final Map<String, KokoonpanoTuote> tuotteetMap = new HashMap<>();
    private static long kokonaisHinta;
    private static int kokonaisMaara;
    private static final EventList<KaupoittainRivi> kaupoittainLista = new BasicEventList<>();

    public static Path getTiedosto() {
        return tiedosto;
    }

    public static void setTiedosto(Path tiedosto) {
        Kokoonpano.tiedosto = tiedosto;
    }

    public static long getKokonaisHinta() {
        return kokonaisHinta;
    }

    public static int getKokonaisMaara() {
        return kokonaisMaara;
    }

    public static EventList<KaupoittainRivi> getKaupoittainLista() {
        return kaupoittainLista;
    }

    public static EventList<KokoonpanoTuote> getTuotteetEventList() {
        return tuotteetEventList;
    }

    public static List<KokoonpanoTuote> getTuotteet() {
        return new ArrayList<>(tuotteetEventList);
    }

    public static KokoonpanoTuote haeTuote(String url) {
        return tuotteetMap.get(url);
    }

    public static void lisaaTuote(KokoonpanoTuote tuote) {
        tuotteetMap.put(tuote.getUrl(), tuote);
        tuotteetEventList.add(tuote);
        tuote.addPropertyChangeListener("maara", (pce) -> valitseKaupat());
        valitseKaupat();
    }

    public static void poistaTuote(KokoonpanoTuote tuote) {
        tuotteetEventList.remove(tuote);
        tuotteetMap.remove(tuote.getUrl());
        valitseKaupat();
    }

    public static void tyhjenna() {
        tuotteetEventList.clear();
        tuotteetMap.clear();
        valitseKaupat();
    }

    public static void tyhjennaJaLisaaMonta(Collection<KokoonpanoTuote> tuotteet) {
        tuotteetEventList.clear();
        tuotteetMap.clear();
        for (KokoonpanoTuote tuote : tuotteet) {
            tuotteetMap.put(tuote.getUrl(), tuote);
            tuote.addPropertyChangeListener("maara", (pce) -> valitseKaupat());
        }
        tuotteetEventList.addAll(tuotteet);
        valitseKaupat();
    }

    public static void lisaaMonta(Collection<KokoonpanoTuote> tuotteet) {
        for (KokoonpanoTuote tuote : tuotteet) {
            tuotteetMap.put(tuote.getUrl(), tuote);
            tuote.addPropertyChangeListener("maara", (pce) -> valitseKaupat());
        }
        tuotteetEventList.addAll(tuotteet);
        valitseKaupat();
    }

    public static void poistaMonta(Collection<KokoonpanoTuote> tuotteet) {
        tuotteetEventList.removeAll(tuotteet);
        for (KokoonpanoTuote tuote : tuotteet) {
            tuotteetMap.remove(tuote.getUrl());
        }
        valitseKaupat();
    }

    private static void valitseKaupat() {
        Map<Kauppa, Joukkotilaus> joukkotilaukset = new HashMap<>();
        Map<KokoonpanoTuote, List<HintaValinta>> kaikkienValinnat = new HashMap<>();
        for (KokoonpanoTuote tuote : getTuotteet()) {
            if (tuote.getParasHinta() == null) {
                tuote.setValittuHinta(null);
                continue;
            }
            long parasHinta = tuote.getParasHinta().getSuodatettuHinta();
            int maara = tuote.getMaara();

            Map<Kauppa, Hinta> hinnat = new HashMap<>();
            for (Hinta hinta : tuote.getHinnat()) {
                Kauppa kauppa = hinta.getKauppa();
                long hintaLuku = hinta.getHinta();

                if (hinta.getSuodatettuHinta() != -1 && hintaLuku < parasHinta) { //Poistetaan hinnat, joissa ei s��st�mahdollisuutta
                    Hinta haettuHinta = hinnat.get(kauppa);
                    if (haettuHinta == null || hintaLuku < haettuHinta.getHinta()) { //J�tet��n useasti samassa tuotteessa esiintyvist� kaupoista halvimmat
                        hinnat.put(kauppa, hinta);
                    }
                }
            }
            for (Hinta hinta : hinnat.values()) {
                HintaValinta valinta = new HintaValinta(tuote, hinta);

                Joukkotilaus tilaus = joukkotilaukset.get(hinta.getKauppa());
                if (tilaus == null) {
                    List<HintaValinta> valinnat = new ArrayList<>();
                    valinnat.add(valinta);
                    joukkotilaukset.put(hinta.getKauppa(), new Joukkotilaus(maara, valinnat));
                } else {
                    tilaus.kokonaisMaara += maara;
                    tilaus.valinnat.add(valinta);
                }
            }

            List<HintaValinta> valinnat = new ArrayList<>();
            valinnat.add(new HintaValinta(tuote, tuote.getParasHinta()));
            kaikkienValinnat.put(tuote, valinnat);
        }
        for (Joukkotilaus tilaus : joukkotilaukset.values()) {
            if (tilaus.kokonaisMaara > 1) { //Poistetaan joukkotilaukset, joissa on vain yksi tuote
                for (HintaValinta valinta : tilaus.valinnat) {
                    if (!valinta.hinta.equals(valinta.tuote.getParasHinta())) {
                        kaikkienValinnat.get(valinta.tuote).add(valinta);
                    }
                }
            }
        }
        if (kaikkienValinnat.isEmpty()) {
            kokonaisHinta = 0;
            kokonaisMaara = 0;
            kaupoittainLista.clear();
            return;
        }
        kaikkienValinnatList = new ArrayList<>(kaikkienValinnat.values());
        valiaikainen = new HintaValinta[kaikkienValinnatList.size()];
        parasKokonaisHinta = -1;
        kayLapiValinnat(0);

        for (HintaValinta valinta : parasYhdistelma) {
            valinta.tuote.setValittuHinta(valinta.hinta);
        }
        kokonaisHinta = parasKokonaisHinta;
        kokonaisMaara = parasYhdistelma.length;
        Map<String, KaupoittainRivi> kaupoittainMap = new HashMap<>();
        for (HintaValinta valinta : parasYhdistelma) {
            String nimi = valinta.hinta.getKaupanNimi();
            int maara = valinta.tuote.getMaara();
            long hinta = valinta.hinta.getHinta() * maara;
            long postikulut = valinta.hinta.getSuodatetutPostikulut();

            KaupoittainRivi rivi = kaupoittainMap.get(nimi);
            if (rivi == null) {
                kaupoittainMap.put(nimi, new KaupoittainRivi(nimi, hinta, postikulut, maara));
            } else {
                rivi.setHinta(rivi.getHinta() + hinta);
                rivi.setMaara(rivi.getMaara() + maara);
                if (postikulut > rivi.getPostikulut()) {
                    rivi.setPostikulut(postikulut);
                }
            }
        }
        kaupoittainLista.clear();
        kaupoittainLista.addAll(kaupoittainMap.values());
    }

    private static List<List<HintaValinta>> kaikkienValinnatList;
    private static HintaValinta[] valiaikainen;
    private static HintaValinta[] parasYhdistelma;
    private static long parasKokonaisHinta;

    private static void kayLapiValinnat(int tuoteRivinumero) {
        for (HintaValinta valinta : kaikkienValinnatList.get(tuoteRivinumero)) {
            valiaikainen[tuoteRivinumero] = valinta;
            if (tuoteRivinumero < kaikkienValinnatList.size() - 1) {
                kayLapiValinnat(tuoteRivinumero + 1);
            } else {
                long kokonaisHinta = laskeKokonaisHinta(valiaikainen);
                if (parasKokonaisHinta == -1 || kokonaisHinta < parasKokonaisHinta) {
                    parasKokonaisHinta = kokonaisHinta;
                    parasYhdistelma = Arrays.copyOf(valiaikainen, valiaikainen.length);
                }
            }
        }
    }

    private static long laskeKokonaisHinta(HintaValinta[] valinnat) {
        long kokonaisHinta = 0;
        Map<Kauppa, Long> suurimmatPostikulut = new HashMap<>();
        for (HintaValinta valinta : valinnat) {
            long hinta = valinta.tuote.getMaara() * valinta.hinta.getHinta();
            kokonaisHinta += hinta;

            Kauppa kauppa = valinta.hinta.getKauppa();
            Long haetutPostikulut = suurimmatPostikulut.get(kauppa);
            long postikulut = valinta.hinta.getSuodatetutPostikulut();
            if (haetutPostikulut == null || postikulut > haetutPostikulut) {
                suurimmatPostikulut.put(kauppa, postikulut);
            }
        }
        for (long postikulut : suurimmatPostikulut.values()) {
            kokonaisHinta += postikulut;
        }
        return kokonaisHinta;
    }

    private static class Joukkotilaus {

        public int kokonaisMaara;
        public final List<HintaValinta> valinnat;

        public Joukkotilaus(int kokonaisMaara, List<HintaValinta> saastot) {
            this.kokonaisMaara = kokonaisMaara;
            this.valinnat = saastot;
        }
    }

    private static class HintaValinta {

        public final KokoonpanoTuote tuote;
        public final Hinta hinta;

        public HintaValinta(KokoonpanoTuote tuote, Hinta hinta) {
            this.tuote = tuote;
            this.hinta = hinta;
        }
    }
}
