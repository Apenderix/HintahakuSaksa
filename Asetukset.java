/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hintahakuSaksa;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Calendar;
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
public class Asetukset {

    private static final Path tiedosto = Paths.get("asetukset.xml");
    private static Path suodatintiedosto;
    private static Path viimeisinSuodatinKansio;
    private static Path viimeisinKokoonpanoKansio;

    public static Path getSuodatintiedosto() {
        return suodatintiedosto;
    }

    public static void setSuodatintiedosto(Path suodatintiedosto) {
        Asetukset.suodatintiedosto = suodatintiedosto;
        tallenna();
    }

    public static Path getViimeisinSuodatinKansio() {
        return viimeisinSuodatinKansio;
    }

    public static void setViimeisinSuodatinKansio(Path viimeisinSuodatinKansio) {
        Asetukset.viimeisinSuodatinKansio = viimeisinSuodatinKansio;
        tallenna();
    }

    public static Path getViimeisinKokoonpanoKansio() {
        return viimeisinKokoonpanoKansio;
    }

    public static void setViimeisinKokoonpanoKansio(Path viimeisinKokoonpanoKansio) {
        Asetukset.viimeisinKokoonpanoKansio = viimeisinKokoonpanoKansio;
        tallenna();
    }

    public static void avaa() {
        Document xml;
        try (BufferedReader reader = Files.newBufferedReader(tiedosto)) {
            xml = new SAXBuilder().build(reader);
        } catch (JDOMException | IOException ex) {
            return;
        }
        Element juuri = xml.getRootElement();

        if (!"Hintahaku-asetukset".equals(juuri.getAttributeValue("info"))) {
            return;
        }

        try {
            suodatintiedosto = Paths.get(juuri.getChildText("suodatintiedosto"));
        } catch (InvalidPathException | NullPointerException ex) {
        }
        try {
            viimeisinSuodatinKansio = Paths.get(juuri.getChildText("viimeisinSuodatinKansio"));
        } catch (InvalidPathException | NullPointerException ex) {
        }
        try {
            viimeisinKokoonpanoKansio = Paths.get(juuri.getChildText("viimeisinKokoonpanoKansio"));
        } catch (InvalidPathException | NullPointerException ex) {
        }
    }

    private static void tallenna() {
        Element juuri = new Element("asetukset");
        juuri.setAttribute("info", "Hintahaku-asetukset");
        juuri.setAttribute("pvm", new SimpleDateFormat("d.M.y H:mm:ss").format(Calendar.getInstance().getTime()));

        if (suodatintiedosto != null) {
            juuri.addContent(new Element("suodatintiedosto").setText(suodatintiedosto.toString()));
        }
        if (viimeisinSuodatinKansio != null) {
            juuri.addContent(new Element("viimeisinSuodatinKansio").setText(viimeisinSuodatinKansio.toString()));
        }
        if (viimeisinKokoonpanoKansio != null) {
            juuri.addContent(new Element("viimeisinKokoonpanoKansio").setText(viimeisinKokoonpanoKansio.toString()));
        }

        Document xml = new Document(juuri);
        try (BufferedWriter writer = Files.newBufferedWriter(tiedosto)) {
            new XMLOutputter(Format.getPrettyFormat()).output(xml, writer);
        } catch (IOException ex) {
        }
    }
}
