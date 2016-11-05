/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hintahakuSaksa;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 *
 * @author Tuupertunut
 */
public class Kauppa {

    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private final String kaupanNimi;
    private boolean voiNoutaa;
    private boolean suodataPois;

    public Kauppa(String kaupanNimi, boolean voiNoutaa, boolean suodataPois) {
        this.kaupanNimi = kaupanNimi;
        this.voiNoutaa = voiNoutaa;
        this.suodataPois = suodataPois;
    }

    public String getKaupanNimi() {
        return kaupanNimi;
    }

    public boolean isVoiNoutaa() {
        return voiNoutaa;
    }

    public void setVoiNoutaa(boolean voiNoutaa) {
        boolean oldVoiNoutaa = this.voiNoutaa;
        this.voiNoutaa = voiNoutaa;
        pcs.firePropertyChange("voiNoutaa", oldVoiNoutaa, voiNoutaa);
    }

    public boolean isSuodataPois() {
        return suodataPois;
    }

    public void setSuodataPois(boolean suodataPois) {
        boolean oldSuodataPois = this.suodataPois;
        this.suodataPois = suodataPois;
        pcs.firePropertyChange("suodataPois", oldSuodataPois, suodataPois);
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
