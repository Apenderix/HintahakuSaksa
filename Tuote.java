/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hintahakuSaksa;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import hintahaku.Muunnin;

/**
 *
 * @authors Tuupertunut & japo2101
 */
public class Tuote {

	private final List<Hinta> hinnat = new ArrayList<>();
	private Hinta parasHinta;
	private final String nimi;
	private final String url;

	public Tuote(String url, Document dok) {

		this.url = url;

		Elements tuotenimiEl = dok.select(".arthdr");
		Elements tuoteEl = tuotenimiEl.select("[itemprop=\"name\"]");
		nimi = tuoteEl.hasText() ? tuoteEl.text() : tuoteEl.attr("content");

		boolean tallennus = false;

		for (Element hintaEl : dok.select(".offer")) {
			
			Elements valiEl = hintaEl.select("[class=\"merchant__logo-image\"]");
			Elements imgEl = valiEl.select("img");
			String kaupanNimi = imgEl.attr("title");
			
			Elements valiEl2 = hintaEl.select("[class=\"merchant__logo-caption\"]");
			Elements tekstiEl = valiEl2.select("[class=\"notrans\"]");
			kaupanNimi = tekstiEl.text();
			
			long hinta = Muunnin.stringToLong(hintaEl.select("[class=\"gh_price\"]").text().replace("-", "0"));

			long postikulut = 2000;

			String toimitusaika = "taikomisnopeutesi";

			Kauppa kauppa = Suodattimet.haeKauppa(kaupanNimi);
			if (kauppa == null) {
				kauppa = new Kauppa(kaupanNimi, false, false);
				Suodattimet.lisaaKauppa(kauppa);
				tallennus = true;
			}

			Hinta luotuHinta = new Hinta(kauppa, hinta, postikulut, toimitusaika);
			hinnat.add(luotuHinta);
		}
		laskeParasHinta();

		if (tallennus) {
			Suodattimet.tallenna();

		}
	}

	public List<Hinta> getHinnat() {
		return hinnat;
	}

	public Hinta getParasHinta() {
		return parasHinta;
	}

	public String getNimi() {
		return nimi;
	}

	public String getUrl() {
		return url;
	}

	public void laskeParasHinta() {
		try {
			Hinta hinta = Collections.min(hinnat);
			parasHinta = hinta.getSuodatettuHinta() == -1 ? null : hinta;
		} catch (NoSuchElementException ex) {
			parasHinta = null;
		}
	}
}
