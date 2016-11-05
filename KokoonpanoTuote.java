/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hintahakuSaksa;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.List;

/**
 *
 * @author Tuupertunut
 */
public class KokoonpanoTuote {

    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private final Tuote tuote;
    private int maara;
    private boolean poistettava = false;
    private Hinta valittuHinta;

    public KokoonpanoTuote(Tuote tuote, int maara) {
        this.tuote = tuote;
        this.maara = maara;
    }

    public Tuote getTuote() {
        return tuote;
    }

    public int getMaara() {
        return maara;
    }

    public void setMaara(int maara) {
        int oldMaara = this.maara;
        this.maara = maara;
        pcs.firePropertyChange("maara", oldMaara, maara);
    }

    public boolean isPoistettava() {
        return poistettava;
    }

    public void setPoistettava(boolean poistettava) {
        boolean oldPoistettava = this.poistettava;
        this.poistettava = poistettava;
        pcs.firePropertyChange("poistettava", oldPoistettava, poistettava);
    }

    public Hinta getValittuHinta() {
        return valittuHinta;
    }

    public void setValittuHinta(Hinta valittuHinta) {
        Hinta oldValittuHinta = this.valittuHinta;
        this.valittuHinta = valittuHinta;
        pcs.firePropertyChange("valittuHinta", oldValittuHinta, valittuHinta);
    }

    public List<Hinta> getHinnat() {
        return tuote.getHinnat();
    }

    public Hinta getParasHinta() {
        return tuote.getParasHinta();
    }

    public String getNimi() {
        return tuote.getNimi();
    }

    public String getUrl() {
        return tuote.getUrl();
    }

    public void laskeParasHinta() {
        tuote.laskeParasHinta();
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }

    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(propertyName, listener);
    }

    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(propertyName, listener);
    }
}
