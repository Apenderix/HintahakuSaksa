/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hintahakuSaksa;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.swing.JOptionPane;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

/**
 *
 * @author Tuupertunut
 */
public class Suodattimet {

    private static final PropertyChangeSupport pcs = new PropertyChangeSupport(Suodattimet.class);
    private static boolean tapahtumatPaalla = true;
    private static Path tiedosto;
    private static final Map<String, Kauppa> kaupatMap = new HashMap<>();
    private static final EventList<Kauppa> kaupatEventList = new BasicEventList<>();

    public static Path getTiedosto() {
        return tiedosto;
    }

    public static void setTiedosto(Path tiedosto) {
        Suodattimet.tiedosto = tiedosto;
    }

    public static EventList<Kauppa> getKaupatEventList() {
        return kaupatEventList;
    }

    public static Kauppa haeKauppa(String nimi) {
        return kaupatMap.get(nimi);
    }

    public static void lisaaKauppa(Kauppa kauppa) {
        kaupatMap.put(kauppa.getKaupanNimi(), kauppa);
        kaupatEventList.add(kauppa);
        kauppa.addPropertyChangeListener((pce) -> {
            if (tapahtumatPaalla) {
                pcs.firePropertyChange("suodattimet", null, null);
            }
        });
    }

    public static void vaihda(Path tiedosto) throws IOException, JDOMException, SuodatinTiedostoException {
        Document xml;
        try (BufferedReader reader = Files.newBufferedReader(tiedosto)) {
            xml = new SAXBuilder().build(reader);
        }
        Element juuri = xml.getRootElement();

        if (!"Hintahaku-suodattimet".equals(juuri.getAttributeValue("info"))) {
            throw new SuodatinTiedostoException();
        }

        tapahtumatPaalla = false;

        Set<Kauppa> tiedostossa = new HashSet<>();

        for (Element kauppaxml : juuri.getChildren()) {
            String kaupanNimi = kauppaxml.getChildText("nimi");
            if (kaupanNimi != null) {
                boolean voiNoutaa = Boolean.parseBoolean(kauppaxml.getChildText("voiNoutaa"));
                boolean suodataPois = Boolean.parseBoolean(kauppaxml.getChildText("suodataPois"));

                Kauppa haettuKauppa = haeKauppa(kaupanNimi);
                if (haettuKauppa == null) {
                    Kauppa uusiKauppa = new Kauppa(kaupanNimi, voiNoutaa, suodataPois);
                    lisaaKauppa(uusiKauppa);
                    tiedostossa.add(uusiKauppa);
                } else {
                    haettuKauppa.setVoiNoutaa(voiNoutaa);
                    haettuKauppa.setSuodataPois(suodataPois);
                    tiedostossa.add(haettuKauppa);
                }
            }
        }

        for (Kauppa kauppa : kaupatEventList) {
            if (!tiedostossa.contains(kauppa)) {
                kauppa.setVoiNoutaa(false);
                kauppa.setSuodataPois(false);
            }
        }

        tapahtumatPaalla = true;
        pcs.firePropertyChange("suodattimet", null, null);

        Suodattimet.tiedosto = tiedosto;
    }

    public static void tallennaNimella(Path tiedosto) throws IOException {
        Element juuri = new Element("suodattimet");
        juuri.setAttribute("info", "Hintahaku-suodattimet");
        juuri.setAttribute("pvm", new SimpleDateFormat("d.M.y H:mm:ss").format(Calendar.getInstance().getTime()));

        for (Kauppa kauppa : kaupatEventList) {
            Element kauppaxml = new Element("kauppa");
            kauppaxml.addContent(new Element("nimi").setText(kauppa.getKaupanNimi()));
            kauppaxml.addContent(new Element("voiNoutaa").setText(Boolean.toString(kauppa.isVoiNoutaa())));
            kauppaxml.addContent(new Element("suodataPois").setText(Boolean.toString(kauppa.isSuodataPois())));
            juuri.addContent(kauppaxml);
        }

        Document xml = new Document(juuri);
        try (BufferedWriter writer = Files.newBufferedWriter(tiedosto)) {
            new XMLOutputter(Format.getPrettyFormat()).output(xml, writer);
        }

        Suodattimet.tiedosto = tiedosto;
    }

    public static void tallenna() {
        if (tiedosto != null) {
            try {
                tallennaNimella(tiedosto);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(null, "Suodattimien tallentaminen epäonnistui!", "Virhe!", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    public static void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }

    public static void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(propertyName, listener);
    }

    public static void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(propertyName, listener);
    }

    public static class SuodatinTiedostoException extends Exception {
    }
}
