/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hintahakuSaksa;

import java.awt.Color;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.table.DefaultTableCellRenderer;

/**
 *
 * @author Tuupertunut
 */
public class HuutomerkkiRenderer extends DefaultTableCellRenderer {

    private static final Icon ERRORICON = new ImageIcon(HuutomerkkiRenderer.class.getResource("exclaim.gif"));
    private static final String EISAAT = "Ei saatavilla";
    private final Color original = getForeground();

    @Override
    protected void setValue(Object o) {
        if (EISAAT.equals(o)) {
            setForeground(Color.RED);
            setIcon(ERRORICON);
            setText(EISAAT);
        } else {
            setForeground(original);
            setIcon(null);
            setText(o == null ? "" : o.toString());
        }
    }

}
