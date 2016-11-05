package hintahakuSaksa;

import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.ObservableElementList;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.gui.AdvancedTableFormat;
import ca.odell.glazedlists.gui.WritableTableFormat;
import ca.odell.glazedlists.swing.AdvancedTableModel;
import ca.odell.glazedlists.swing.GlazedListsSwing;
import ca.odell.glazedlists.swing.TableComparatorChooser;
import java.awt.Desktop;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.plaf.basic.BasicSplitPaneUI;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @authors Tuupertunut & japo2101
 */
public class HintahakuFrame extends javax.swing.JFrame {

	private double splitPaneSuhde1;
	private double splitPaneSuhde2 = 0.5;
	private final String USERAGENT = "Hintahaku-ohjelma/1.0";
	private Tuote nykyinenTuote;
	private final Comparator<String> hintaComp = (s, s1) -> Long.compare(Muunnin.stringToLong(s),
			Muunnin.stringToLong(s1));

	/**
	 * Creates new form NewJFrame
	 */
	public HintahakuFrame() {
		initComponents();
		haeSuhde1(jSplitPane1.getDividerLocation());
		rootPane.setDefaultButton(jButton1);
		jScrollPane1.getVerticalScrollBar().setUnitIncrement(16);

		Asetukset.avaa();

		Path suodatintiedosto = Asetukset.getSuodatintiedosto();
		if (suodatintiedosto == null) {
			suodatintiedosto = Paths.get("suodattimet.xml");
			if (Files.isRegularFile(suodatintiedosto)) {
				try {
					Suodattimet.vaihda(suodatintiedosto);
					Asetukset.setSuodatintiedosto(suodatintiedosto);
					JOptionPane.showMessageDialog(null,
							"Suodatintiedostoa ei ole asetettu, joten käytetään ohjelman kansiosta läytyvää suodattimet.xml-tiedostoa.",
							"Tiedotus", JOptionPane.INFORMATION_MESSAGE);
				} catch (IOException | JDOMException | Suodattimet.SuodatinTiedostoException ex) {
					JOptionPane.showMessageDialog(null,
							"Suodatintiedostoa ei ole asetettu, eikä ohjelman kansiosta läytyvää suodattimet.xml-tiedostoa voi käyttää.",
							"Virhe!", JOptionPane.ERROR_MESSAGE);
				}
			} else {
				Suodattimet.setTiedosto(suodatintiedosto);
				Asetukset.setSuodatintiedosto(suodatintiedosto);
				JOptionPane.showMessageDialog(null,
						"Suodatintiedostoa ei ole asetettu, joten luodaan uusi suodattimet.xml-tiedosto ohjelman kansioon.",
						"Tiedotus", JOptionPane.INFORMATION_MESSAGE);
			}
		} else {
			try {
				Suodattimet.vaihda(suodatintiedosto);
			} catch (IOException ex) {
				JOptionPane.showMessageDialog(null, "Nykyisen suodatintiedoston avaaminen epäonnistui!", "Virhe!",
						JOptionPane.ERROR_MESSAGE);
			} catch (JDOMException | Suodattimet.SuodatinTiedostoException ex) {
				JOptionPane.showMessageDialog(null, "Nykyinen suodatintiedosto on virheellinen!", "Virhe!",
						JOptionPane.ERROR_MESSAGE);
			}
		}
		Path haettuTiedosto = Suodattimet.getTiedosto();
		if (haettuTiedosto == null) {
			jLabel19.setText("<html><font color='red'>Ei tiedostoa! Suodattimet eivät tallennu.</font>");
		} else {
			jLabel19.setText(suodatintiedosto.toAbsolutePath().toString());
		}

		SortedList<KokoonpanoTuote> jarjestetty1 = new SortedList<>(new ObservableElementList<KokoonpanoTuote>(
				Kokoonpano.getTuotteetEventList(), GlazedLists.beanConnector(KokoonpanoTuote.class)), (a, b) -> 0);
		AdvancedTableModel<KokoonpanoTuote> eventModel1 = GlazedListsSwing.eventTableModel(jarjestetty1,
				new JTable1Format());
		jTable1.setModel(eventModel1);
		TableComparatorChooser.install(jTable1, jarjestetty1, TableComparatorChooser.SINGLE_COLUMN);
		new TableColumnAdjuster(jTable1).setDynamicAdjustment(true);

		SortedList<KaupoittainRivi> jarjestetty2 = new SortedList<>(Kokoonpano.getKaupoittainLista(), (a, b) -> 0);
		jTable2.setModel(GlazedListsSwing.eventTableModel(jarjestetty2, new JTable2Format()));
		TableComparatorChooser.install(jTable2, jarjestetty2, TableComparatorChooser.SINGLE_COLUMN);
		new TableColumnAdjuster(jTable2).setDynamicAdjustment(true);

		SortedList<Kauppa> jarjestetty3 = new SortedList<>(new ObservableElementList<Kauppa>(
				Suodattimet.getKaupatEventList(), GlazedLists.beanConnector(Kauppa.class)), (a, b) -> 0);
		jTable3.setModel(GlazedListsSwing.eventTableModel(jarjestetty3, new JTable3Format()));
		TableComparatorChooser.install(jTable3, jarjestetty3, TableComparatorChooser.SINGLE_COLUMN);
		new TableColumnAdjuster(jTable3).setDynamicAdjustment(true);

		TableColumnModel columnModel1 = jTable1.getColumnModel();
		TableColumn column = columnModel1.getColumn(5);
		column.setCellEditor(new SpinnerEditor());
		column.setCellRenderer(new SpinnerRenderer());
		columnModel1.getColumn(2).setCellRenderer(new HuutomerkkiRenderer());

		jTable1.addMouseListener(new MouseAdapter() {

			@Override
			public void mousePressed(MouseEvent e) {
				Point kohta = e.getPoint();
				if (jTable1.columnAtPoint(kohta) == 1 && e.getClickCount() == 2) {
					try {
						Desktop.getDesktop()
								.browse(URI.create(eventModel1.getElementAt(jTable1.rowAtPoint(kohta)).getUrl()));
					} catch (IOException | UnsupportedOperationException ex) {
					}
				}
			}
		});

		Suodattimet.addPropertyChangeListener((pce) -> {
			List<KokoonpanoTuote> poistettavat = new ArrayList<>();
			for (KokoonpanoTuote tuote : Kokoonpano.getTuotteet()) {
				Hinta oldParasHinta = tuote.getParasHinta();
				tuote.laskeParasHinta();
				if (oldParasHinta != null && tuote.getParasHinta() == null) {
					Object[] napit = { "Poista tuote", "Jätä kokoonpanoon" };
					int valinta = JOptionPane.showOptionDialog(null,
							"<html>Tuotetta ei ole saatavilla enää yhdessäkään kaupassa:<br><b>" + tuote.getNimi()
									+ "</b><br><br>Haluatko poistaa tuotteen kokoonpanosta?",
							"Varmistus", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, napit, napit[1]);
					if (valinta == JOptionPane.YES_OPTION) {
						poistettavat.add(tuote);
					}
				}
			}
			Kokoonpano.poistaMonta(poistettavat); // Vaikkei poistettavia olisi,
													// valitsee kaupat uudelleen
			if (nykyinenTuote != null) {
				nykyinenTuote.laskeParasHinta();
				paivitaParasHinta();
			}
			paivitaKokoonpanonTulos();
			if (!poistettavat.isEmpty()) {
				paivitaLisattyMaara();
				jButton7.setEnabled(Kokoonpano.getTiedosto() != null);
			}
		});
	}

	private void paivitaParasHinta() {
		Hinta paras = nykyinenTuote.getParasHinta();
		if (paras == null) {
			jLabel8.setText("0,00 €");
			jLabel9.setText("Ei saatavilla");
			jButton6.setEnabled(false);
		} else {
			jLabel8.setText(Muunnin.longToString(paras.getSuodatettuHinta()));
			jLabel9.setText(paras.getKaupanNimi());
			jButton6.setEnabled(true);
		}
	}

	private void paivitaKokoonpanonTulos() {
		jLabel12.setText(Muunnin.longToString(Kokoonpano.getKokonaisHinta()));
		jLabel14.setText(Integer.toString(Kokoonpano.getKokonaisMaara()));
	}

	private void paivitaLisattyMaara() {
		if (nykyinenTuote == null) {
			jLabel17.setText("");
			return;
		}
		KokoonpanoTuote tuote = Kokoonpano.haeTuote(nykyinenTuote.getUrl());
		if (tuote == null) {
			jLabel17.setText("");
		} else {
			int maara = tuote.getMaara();
			jLabel17.setText(
					maara == 1 ? "1 kappale lisätty kokoonpanoon." : maara + " kappaletta lisätty kokoonpanoon.");
		}
	}

	private void tallenna(Path tallennettava) {
		Element juuri = new Element("kokoonpano");
		juuri.setAttribute("info", "Tallennettu Hintahaku-kokoonpano");
		juuri.setAttribute("pvm", new SimpleDateFormat("d.M.y H:mm:ss").format(Calendar.getInstance().getTime()));

		for (KokoonpanoTuote tuote : Kokoonpano.getTuotteet()) {
			Element tuoteEl = new Element("tuote");
			tuoteEl.addContent(new Element("url").setText(tuote.getUrl()));
			tuoteEl.addContent(new Element("määrä").setText(Integer.toString(tuote.getMaara())));
			juuri.addContent(tuoteEl);
		}

		org.jdom2.Document xml = new org.jdom2.Document(juuri);
		try (BufferedWriter writer = Files.newBufferedWriter(tallennettava)) {
			new XMLOutputter(Format.getPrettyFormat()).output(xml, writer);
		} catch (IOException ex) {
			JOptionPane.showMessageDialog(null, "Tiedoston tallentaminen epäonnistui!", "Virhe!",
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		Kokoonpano.setTiedosto(tallennettava);
		jLabel11.setText("Avattu: " + tallennettava.getFileName().toString());
		jButton7.setEnabled(false);
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
	// <editor-fold defaultstate="collapsed" desc="Generated
	// Code">//GEN-BEGIN:initComponents
	private void initComponents() {

		jTabbedPane1 = new javax.swing.JTabbedPane();
		jPanel3 = new javax.swing.JPanel();
		jLabel9 = new javax.swing.JLabel();
		jLabel1 = new javax.swing.JLabel();
		jLabel2 = new javax.swing.JLabel();
		jLabel8 = new javax.swing.JLabel();
		jLabel4 = new javax.swing.JLabel();
		jTextField1 = new javax.swing.JTextField();
		jButton1 = new javax.swing.JButton();
		jLabel3 = new javax.swing.JLabel();
		jLabel10 = new javax.swing.JLabel("Anna Geishalz.de tuote");
		jLabel7 = new javax.swing.JLabel();
		jLabel5 = new javax.swing.JLabel();
		jLabel6 = new javax.swing.JLabel();
		jScrollPane1 = new javax.swing.JScrollPane();
		jPanel1 = new javax.swing.JPanel();
		jPanel2 = new javax.swing.JPanel();
		jButton6 = new javax.swing.JButton();
		jLabel17 = new javax.swing.JLabel();
		jSplitPane1 = new javax.swing.JSplitPane();
		jPanel4 = new javax.swing.JPanel();
		jScrollPane3 = new javax.swing.JScrollPane();
		jTable2 = new javax.swing.JTable();
		jButton2 = new javax.swing.JButton();
		jButton3 = new javax.swing.JButton();
		jLabel11 = new javax.swing.JLabel();
		jSeparator1 = new javax.swing.JSeparator();
		jLabel12 = new javax.swing.JLabel();
		jLabel13 = new javax.swing.JLabel();
		jLabel14 = new javax.swing.JLabel();
		jLabel15 = new javax.swing.JLabel();
		jButton4 = new javax.swing.JButton();
		jLabel16 = new javax.swing.JLabel();
		jButton7 = new javax.swing.JButton();
		jPanel5 = new javax.swing.JPanel();
		jScrollPane2 = new javax.swing.JScrollPane();
		jTable1 = new javax.swing.JTable();
		jButton5 = new javax.swing.JButton();
		jLabel18 = new javax.swing.JLabel();
		jSplitPane2 = new javax.swing.JSplitPane();
		jPanel6 = new javax.swing.JPanel();
		jButton8 = new javax.swing.JButton();
		jButton9 = new javax.swing.JButton();
		jLabel19 = new javax.swing.JLabel();
		jLabel20 = new javax.swing.JLabel();
		jPanel7 = new javax.swing.JPanel();
		jScrollPane4 = new javax.swing.JScrollPane();
		jTable3 = new javax.swing.JTable();

		setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
		setTitle("Hintahaku");

		jTabbedPane1.addChangeListener(new javax.swing.event.ChangeListener() {
			public void stateChanged(javax.swing.event.ChangeEvent evt) {
				jTabbedPane1StateChanged(evt);
			}
		});

		jLabel9.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N

		jLabel1.setText("URL:");

		jLabel2.setText("<html><br>Toimituksella");

		jLabel8.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N

		jLabel4.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N

		jButton1.setText("Hae");
		jButton1.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButton1ActionPerformed(evt);
			}
		});

		jLabel3.setText("<html><br>Hinta noudolla");

		jLabel7.setText("Paras hinta:");

		jLabel5.setText("<html>Suodata<br>pois");

		jLabel6.setText("<html>Voi<br>noutaa");

		jScrollPane1.setBorder(javax.swing.BorderFactory.createEtchedBorder());

		jPanel2.setLayout(new java.awt.GridLayout(0, 1, 0, 10));

		javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
		jPanel1.setLayout(jPanel1Layout);
		jPanel1Layout.setHorizontalGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(jPanel1Layout.createSequentialGroup().addContainerGap().addComponent(jPanel2,
						javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addContainerGap()));
		jPanel1Layout.setVerticalGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(jPanel1Layout.createSequentialGroup().addContainerGap()
						.addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE,
								javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
						.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

		jScrollPane1.setViewportView(jPanel1);

		jButton6.setText("<html><font color=\"#04B404\" size=\"4\">+</font> Lisää kokoonpanoon");
		jButton6.setEnabled(false);
		jButton6.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButton6ActionPerformed(evt);
			}
		});

		javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
		jPanel3.setLayout(jPanel3Layout);
		jPanel3Layout.setHorizontalGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addComponent(jScrollPane1)
				.addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
						.addContainerGap()
						.addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(
								javax.swing.GroupLayout.Alignment.TRAILING,
								jPanel3Layout.createSequentialGroup().addComponent(jLabel1)
										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addGroup(jPanel3Layout
												.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
												.addGroup(jPanel3Layout.createSequentialGroup().addComponent(jLabel10)
														.addPreferredGap(
																javax.swing.LayoutStyle.ComponentPlacement.RELATED,
																javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
														.addComponent(jLabel7)
														.addPreferredGap(
																javax.swing.LayoutStyle.ComponentPlacement.RELATED)
														.addComponent(jLabel8)
														.addPreferredGap(
																javax.swing.LayoutStyle.ComponentPlacement.RELATED)
														.addComponent(jLabel9))
												.addGroup(
														jPanel3Layout.createSequentialGroup().addComponent(jTextField1)
																.addPreferredGap(
																		javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																.addComponent(jButton1))))
								.addGroup(javax.swing.GroupLayout.Alignment.TRAILING,
										jPanel3Layout.createSequentialGroup().addGap(0, 0, Short.MAX_VALUE)
												.addComponent(jLabel17).addGap(18, 18, 18).addComponent(jButton6,
														javax.swing.GroupLayout.PREFERRED_SIZE,
														javax.swing.GroupLayout.DEFAULT_SIZE,
														javax.swing.GroupLayout.PREFERRED_SIZE))
								.addGroup(javax.swing.GroupLayout.Alignment.TRAILING,
										jPanel3Layout.createSequentialGroup()
												.addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE,
														javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
												.addGap(18, 18, 18)
												.addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE,
														javax.swing.GroupLayout.DEFAULT_SIZE,
														javax.swing.GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
										.addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE,
												javax.swing.GroupLayout.DEFAULT_SIZE,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
										.addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE,
												javax.swing.GroupLayout.DEFAULT_SIZE,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
										.addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE,
												javax.swing.GroupLayout.DEFAULT_SIZE,
												javax.swing.GroupLayout.PREFERRED_SIZE)))
						.addContainerGap()));
		jPanel3Layout.setVerticalGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
						.addContainerGap()
						.addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
								.addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE,
										javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
								.addComponent(jLabel1).addComponent(jButton1))
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
								.addComponent(jLabel10)
								.addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
										.addComponent(jLabel8).addComponent(jLabel7).addComponent(jLabel9)))
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
						.addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
								.addComponent(jLabel4, javax.swing.GroupLayout.Alignment.TRAILING)
								.addGroup(javax.swing.GroupLayout.Alignment.TRAILING,
										jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
												.addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE,
														javax.swing.GroupLayout.DEFAULT_SIZE,
														javax.swing.GroupLayout.PREFERRED_SIZE)
										.addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE,
												javax.swing.GroupLayout.DEFAULT_SIZE,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE,
												javax.swing.GroupLayout.DEFAULT_SIZE,
												javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(jLabel3,
														javax.swing.GroupLayout.PREFERRED_SIZE,
														javax.swing.GroupLayout.DEFAULT_SIZE,
														javax.swing.GroupLayout.PREFERRED_SIZE)))
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 446, Short.MAX_VALUE)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
						.addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
								.addComponent(jButton6, javax.swing.GroupLayout.PREFERRED_SIZE,
										javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
								.addComponent(jLabel17))
						.addContainerGap()));

		jTabbedPane1.addTab("Haku", jPanel3);

		jSplitPane1.setDividerLocation(280);
		jSplitPane1.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
		jSplitPane1.setUI(new ListenerSplitPaneUI1());
		jSplitPane1.setContinuousLayout(true);
		jSplitPane1.addComponentListener(new java.awt.event.ComponentAdapter() {
			public void componentResized(java.awt.event.ComponentEvent evt) {
				jSplitPane1ComponentResized(evt);
			}
		});

		jScrollPane3.setBorder(javax.swing.BorderFactory.createEtchedBorder());

		jTable2.setBackground(getBackground());
		jTable2.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS);
		jTable2.setRowSelectionAllowed(false);
		jTable2.getTableHeader().setResizingAllowed(false);
		jScrollPane3.setViewportView(jTable2);

		jButton2.setText("Tallenna nimellä...");
		jButton2.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButton2ActionPerformed(evt);
			}
		});

		jButton3.setText("Avaa...");
		jButton3.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButton3ActionPerformed(evt);
			}
		});

		jLabel12.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
		jLabel12.setText("0,00 €");

		jLabel13.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
		jLabel13.setText("Kokonaishinta:");

		jLabel14.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
		jLabel14.setText("0");

		jLabel15.setText("Tuotteiden määrä:");

		jButton4.setText("Uusi");
		jButton4.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButton4ActionPerformed(evt);
			}
		});

		jLabel16.setText("Kaupoittain:");

		jButton7.setText("Tallenna");
		jButton7.setEnabled(false);
		jButton7.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButton7ActionPerformed(evt);
			}
		});

		javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
		jPanel4.setLayout(jPanel4Layout);
		jPanel4Layout.setHorizontalGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addComponent(jScrollPane3, javax.swing.GroupLayout.Alignment.TRAILING,
						javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
				.addComponent(jSeparator1, javax.swing.GroupLayout.Alignment.TRAILING)
				.addGroup(javax.swing.GroupLayout.Alignment.TRAILING,
						jPanel4Layout.createSequentialGroup()
								.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addGroup(jPanel4Layout
										.createParallelGroup(
												javax.swing.GroupLayout.Alignment.LEADING)
										.addGroup(javax.swing.GroupLayout.Alignment.TRAILING,
												jPanel4Layout.createSequentialGroup().addComponent(jLabel11)
														.addPreferredGap(
																javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
												.addComponent(jButton4)
												.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
												.addComponent(jButton3)
												.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
												.addComponent(jButton2)
												.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
												.addComponent(jButton7))
								.addGroup(javax.swing.GroupLayout.Alignment.TRAILING,
										jPanel4Layout.createSequentialGroup().addComponent(jLabel13)
												.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
												.addComponent(jLabel12))
										.addGroup(javax.swing.GroupLayout.Alignment.TRAILING,
												jPanel4Layout.createSequentialGroup().addComponent(jLabel15)
														.addPreferredGap(
																javax.swing.LayoutStyle.ComponentPlacement.RELATED)
												.addComponent(jLabel14)))
								.addContainerGap())
				.addGroup(jPanel4Layout.createSequentialGroup().addContainerGap().addComponent(jLabel16)
						.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
		jPanel4Layout
				.setVerticalGroup(
						jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
								.addGroup(jPanel4Layout.createSequentialGroup().addContainerGap().addComponent(jLabel16)
										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 165,
												Short.MAX_VALUE)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
						.addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
								.addComponent(jLabel14).addComponent(jLabel15))
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
								.addComponent(jLabel12).addComponent(jLabel13))
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
						.addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 1,
								javax.swing.GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
						.addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
								.addComponent(jButton2).addComponent(jButton3).addComponent(jLabel11)
								.addComponent(jButton4).addComponent(jButton7)).addContainerGap()));

		jSplitPane1.setBottomComponent(jPanel4);

		jScrollPane2.setBorder(javax.swing.BorderFactory.createEtchedBorder());

		jTable1.setBackground(getBackground());
		jTable1.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS);
		jTable1.setRowSelectionAllowed(false);
		jTable1.getTableHeader().setResizingAllowed(false);
		jScrollPane2.setViewportView(jTable1);

		jButton5.setText("Poista valitut");
		jButton5.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButton5ActionPerformed(evt);
			}
		});

		jLabel18.setText("Avaa selaimessa tuplaklikkaamalla tuotteen nimeä.");

		javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
		jPanel5.setLayout(jPanel5Layout);
		jPanel5Layout.setHorizontalGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(jPanel5Layout.createSequentialGroup().addComponent(jButton5)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED,
								javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addComponent(jLabel18).addContainerGap())
				.addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE));
		jPanel5Layout
				.setVerticalGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(javax.swing.GroupLayout.Alignment.TRAILING,
								jPanel5Layout.createSequentialGroup()
										.addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 250,
												Short.MAX_VALUE)
										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addGroup(jPanel5Layout
												.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
												.addComponent(jButton5).addComponent(jLabel18))));

		jSplitPane1.setLeftComponent(jPanel5);

		jTabbedPane1.addTab("Kokoonpanot", jSplitPane1);

		jSplitPane2.setDividerLocation(400);
		jSplitPane2.setUI(new ListenerSplitPaneUI2());
		jSplitPane2.setContinuousLayout(true);
		jSplitPane2.addComponentListener(new java.awt.event.ComponentAdapter() {
			public void componentResized(java.awt.event.ComponentEvent evt) {
				jSplitPane2ComponentResized(evt);
			}
		});

		jButton8.setText("Tallenna nimellä...");
		jButton8.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButton8ActionPerformed(evt);
			}
		});

		jButton9.setText("Vaihda...");
		jButton9.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButton9ActionPerformed(evt);
			}
		});

		jLabel20.setText("Nykyinen suodatintiedosto:");

		javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
		jPanel6.setLayout(jPanel6Layout);
		jPanel6Layout
				.setHorizontalGroup(
						jPanel6Layout
								.createParallelGroup(
										javax.swing.GroupLayout.Alignment.LEADING)
								.addGroup(
										jPanel6Layout.createSequentialGroup().addContainerGap()
												.addGroup(
														jPanel6Layout
																.createParallelGroup(
																		javax.swing.GroupLayout.Alignment.LEADING)
																.addComponent(jButton8).addComponent(jButton9)
																.addComponent(jLabel19).addComponent(jLabel20))
												.addContainerGap(248, Short.MAX_VALUE)));
		jPanel6Layout
				.setVerticalGroup(
						jPanel6Layout
								.createParallelGroup(
										javax.swing.GroupLayout.Alignment.LEADING)
								.addGroup(javax.swing.GroupLayout.Alignment.TRAILING,
										jPanel6Layout.createSequentialGroup().addContainerGap(491, Short.MAX_VALUE)
												.addComponent(jLabel20)
												.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
												.addComponent(jLabel19)
												.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
												.addComponent(jButton9)
												.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
												.addComponent(jButton8).addContainerGap()));

		jSplitPane2.setRightComponent(jPanel6);

		jTable3.setBackground(getBackground());
		jTable3.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS);
		jTable3.setRowSelectionAllowed(false);
		jTable3.getTableHeader().setResizingAllowed(false);
		jScrollPane4.setViewportView(jTable3);

		javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
		jPanel7.setLayout(jPanel7Layout);
		jPanel7Layout.setHorizontalGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(jPanel7Layout.createSequentialGroup().addContainerGap().addComponent(jScrollPane4,
						javax.swing.GroupLayout.DEFAULT_SIZE, 389, Short.MAX_VALUE)));
		jPanel7Layout.setVerticalGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(jPanel7Layout.createSequentialGroup().addContainerGap()
						.addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 568, Short.MAX_VALUE)
						.addContainerGap()));

		jSplitPane2.setLeftComponent(jPanel7);

		jTabbedPane1.addTab("Suodattimet", jSplitPane2);

		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
		layout.setHorizontalGroup(
				layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(jTabbedPane1));
		layout.setVerticalGroup(
				layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(jTabbedPane1));

		pack();
	}// </editor-fold>//GEN-END:initComponents

	private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jButton1ActionPerformed

		// Hae

		String url = jTextField1.getText();
		if (!url.startsWith("https://")) {
			url = "http://" + url;
		}

		url = url.replace("www.", "").replaceFirst("(\\d+)(?:\\?|/).*", "$1");
		if (!url.matches("http://hinta\\.fi/\\d+")) {

		}

		// http://hinta.fi/506139/intel-core-i7-4770k
		// http://geizhals.de/intel-core-i7-6700k-bx80662i76700k-a1290371.html?hloc=at&hloc=de

		final String finalUrl = url;
		jLabel10.setText("<html><font color='black'>Haetaan hintatietoja...</font>");
		new SwingWorker<Document, Void>() {

			@Override
			protected Document doInBackground() throws Exception {
				return Jsoup.connect(finalUrl).userAgent(USERAGENT).get();
			}

			@Override
			protected void done() {
				Document dok;
				try {
					dok = get();
				} catch (InterruptedException ex) {
					jLabel10.setText("Tuntematon virhe!");
					return;
				} catch (ExecutionException ex) {
					Throwable cause = ex.getCause();
					if (cause instanceof HttpStatusException) {
						jLabel10.setText("Haettua tuotetta ei ole olemassa!");
					} else if (cause instanceof IOException) {
						jLabel10.setText("Ohjelma ei saa yhteyttä geizhals.de-palvelimeen!");
					} else {
						jLabel10.setText("Tuntematon virhe!");
					}
					return;
				}

				jPanel2.removeAll();

				nykyinenTuote = new Tuote(finalUrl, dok);
				List<Hinta> hinnat = nykyinenTuote.getHinnat();
				for (Hinta hinta : hinnat) {
					jPanel2.add(new Hintapaneeli(hinta));
				}
				jPanel2.revalidate();
				jLabel4.setText("<html>" + nykyinenTuote.getNimi());
				paivitaParasHinta();
				paivitaLisattyMaara();

				jLabel10.setText("");
			}
		}.execute();
	}// GEN-LAST:event_jButton1ActionPerformed

	private void setDefi(boolean b) {
		// TODO Auto-generated method stub

	}

	private void jTabbedPane1StateChanged(javax.swing.event.ChangeEvent evt) {// GEN-FIRST:event_jTabbedPane1StateChanged
		if (jTabbedPane1.getSelectedIndex() == 0) {
			rootPane.setDefaultButton(jButton1);
		} else {
			rootPane.setDefaultButton(null);
		}
	}// GEN-LAST:event_jTabbedPane1StateChanged

	private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jButton6ActionPerformed
		// Lisää kokoonpanoon
		KokoonpanoTuote tuote = Kokoonpano.haeTuote(nykyinenTuote.getUrl());
		if (tuote == null) {
			Kokoonpano.lisaaTuote(new KokoonpanoTuote(nykyinenTuote, 1));
		} else {
			tuote.setMaara(tuote.getMaara() + 1);
		}
		paivitaLisattyMaara();
		paivitaKokoonpanonTulos();
		jButton7.setEnabled(Kokoonpano.getTiedosto() != null);
	}// GEN-LAST:event_jButton6ActionPerformed

	private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jButton5ActionPerformed
		// Poista valitut
		List<KokoonpanoTuote> poistettavat = new ArrayList<>();
		for (KokoonpanoTuote tuote : Kokoonpano.getTuotteet()) {
			if (tuote.isPoistettava()) {
				poistettavat.add(tuote);
			}
		}
		Kokoonpano.poistaMonta(poistettavat);
		paivitaKokoonpanonTulos();
		paivitaLisattyMaara();
		jButton7.setEnabled(Kokoonpano.getTiedosto() != null);
	}// GEN-LAST:event_jButton5ActionPerformed

	private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jButton4ActionPerformed
		// Uusi
		Object[] napit = { "Kyllä", "Peruuta" };
		int valinta = JOptionPane.showOptionDialog(null,
				"Haluatko varmasti sulkea nykyisen kokoonpanon ja aloittaa uuden?", "Varmistus",
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE, null, napit, napit[1]);
		if (valinta != JOptionPane.OK_OPTION) {
			return;
		}

		Kokoonpano.tyhjenna();
		Kokoonpano.setTiedosto(null);
		paivitaKokoonpanonTulos();
		paivitaLisattyMaara();
		jLabel11.setText("");
		jButton7.setEnabled(false);
	}// GEN-LAST:event_jButton4ActionPerformed

	private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jButton3ActionPerformed
		// Avaa
		Path viimeisin = Asetukset.getViimeisinKokoonpanoKansio();
		JFileChooser valitsin = new JFileChooser(viimeisin == null ? null : viimeisin.toFile());
		valitsin.setFileFilter(new FileNameExtensionFilter("Kokoonpanot (xml)", "xml"));
		int valinta = valitsin.showOpenDialog(rootPane);
		if (valinta != JFileChooser.APPROVE_OPTION) {
			return;
		}
		Path avattava = valitsin.getSelectedFile().toPath();
		Asetukset.setViimeisinKokoonpanoKansio(avattava.getParent());

		org.jdom2.Document xml;
		try (BufferedReader reader = Files.newBufferedReader(avattava)) {
			xml = new SAXBuilder().build(reader);
		} catch (IOException ex) {
			JOptionPane.showMessageDialog(null, "Tiedoston avaaminen epäonnistui!", "Virhe!",
					JOptionPane.ERROR_MESSAGE);
			return;
		} catch (JDOMException ex) {
			JOptionPane.showMessageDialog(null, "Avattu tiedosto ei ole kokoonpano!", "Virhe!",
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		Element juuri = xml.getRootElement();
		if (!"Tallennettu Hintahaku-kokoonpano".equals(juuri.getAttributeValue("info"))) {
			JOptionPane.showMessageDialog(null, "Avattu tiedosto ei ole kokoonpano!", "Virhe!",
					JOptionPane.ERROR_MESSAGE);
			return;
		}

		String vanhaTeksti = jLabel11.getText();
		jLabel11.setText("Haetaan kokoonpanon hintatietoja...");
		new SwingWorker<List<RaakaTuote>, Void>() {

			@Override
			protected List<RaakaTuote> doInBackground() throws Exception {
				List<RaakaTuote> lista = new ArrayList<>();
				Pattern urlPattern = Pattern.compile("http://hinta\\.fi/\\d+");
				for (Element tuoteEl : juuri.getChildren()) {
					String url = tuoteEl.getChildText("url");
					int maara = Integer.parseInt(tuoteEl.getChildText("määrä"));
					if (!urlPattern.matcher(url).matches() || maara <= 0) {
						throw new KokoonpanoTiedostoException();
					}

					Document dok;
					try {
						dok = Jsoup.connect(url).userAgent(USERAGENT).get();
					} catch (HttpStatusException ex) { // Tuotetta ei ole enää
														// olemassa
						dok = null;
					}
					lista.add(new RaakaTuote(url, dok, maara));
				}
				return lista;
			}

			@Override
			protected void done() {
				List<RaakaTuote> lista;
				try {
					lista = get();
				} catch (InterruptedException ex) {
					JOptionPane.showMessageDialog(null, "Tuntematon virhe!", "Virhe!", JOptionPane.ERROR_MESSAGE);
					jLabel11.setText(vanhaTeksti);
					return;
				} catch (ExecutionException ex) {
					Throwable cause = ex.getCause();
					if (cause instanceof IOException) {
						JOptionPane.showMessageDialog(null, "Ohjelma ei saa yhteyttä hinta.fi-palvelimeen!", "Virhe!",
								JOptionPane.ERROR_MESSAGE);
					} else if (cause instanceof NumberFormatException || cause instanceof KokoonpanoTiedostoException) {
						JOptionPane.showMessageDialog(null, "Avattu tiedosto on virheellinen!", "Virhe!",
								JOptionPane.ERROR_MESSAGE);
					} else {
						JOptionPane.showMessageDialog(null, "Tuntematon virhe!", "Virhe!", JOptionPane.ERROR_MESSAGE);
					}
					jLabel11.setText(vanhaTeksti);
					return;
				}

				List<KokoonpanoTuote> lisattavat = new ArrayList<>();
				for (RaakaTuote raakaTuote : lista) {
					if (raakaTuote.dok == null) {
						Object[] napit = { "Poista tuote", "Älä näytä" };
						int valinta = JOptionPane.showOptionDialog(null,
								"<html>Tuotetta ei löydy enää hinta.fi:stä:<br><b>" + raakaTuote.url
										+ "</b><br><br>Haluatko poistaa tuotteen tiedostosta, vai jättää sen vain näyttämättä kokoonpanossa? (Tallentaminen poistaa silti.)",
								"Varmistus", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, napit,
								napit[1]);
						if (valinta == JOptionPane.YES_OPTION) {
							Iterator<Element> xmlIter = juuri.getChildren().iterator();
							while (xmlIter.hasNext()) {
								Element tuoteEl = xmlIter.next();
								if (raakaTuote.url.equals(tuoteEl.getChildText("url"))) {
									xmlIter.remove();
									break;
								}
							}
							try (BufferedWriter writer = Files.newBufferedWriter(avattava)) {
								new XMLOutputter(Format.getPrettyFormat()).output(xml, writer);
							} catch (IOException ex) {
								JOptionPane.showMessageDialog(null,
										"Tuotteen poistaminen epäonnistui, joten se jätetään tiedostoon!", "Virhe!",
										JOptionPane.ERROR_MESSAGE);
							}
						}
					} else {
						Tuote tuote = new Tuote(raakaTuote.url, raakaTuote.dok);
						if (tuote.getParasHinta() == null) {
							Object[] napit = { "Poista tuote", "Jätä kokoonpanoon" };
							int valinta = JOptionPane.showOptionDialog(null,
									"<html>Tuotetta ei ole saatavilla enää yhdessäkään kaupassa:<br><b>"
											+ tuote.getNimi() + "</b><br><br>Haluatko poistaa tuotteen tiedostosta?",
									"Varmistus", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, napit,
									napit[1]);
							if (valinta == JOptionPane.YES_OPTION) {
								Iterator<Element> xmlIter = juuri.getChildren().iterator();
								while (xmlIter.hasNext()) {
									Element tuoteEl = xmlIter.next();
									if (tuote.getUrl().equals(tuoteEl.getChildText("url"))) {
										xmlIter.remove();
										break;
									}
								}
								try (BufferedWriter writer = Files.newBufferedWriter(avattava)) {
									new XMLOutputter(Format.getPrettyFormat()).output(xml, writer);
									continue; // Keskeytetään tuotteen
												// lisääminen
								} catch (IOException ex) {
									JOptionPane.showMessageDialog(null,
											"Tuotteen poistaminen epäonnistui, joten se jätetään tiedostoon!", "Virhe!",
											JOptionPane.ERROR_MESSAGE);
								}
							}
						}

						lisattavat.add(new KokoonpanoTuote(tuote, raakaTuote.maara));
					}
				}

				Kokoonpano.tyhjennaJaLisaaMonta(lisattavat);
				Kokoonpano.setTiedosto(avattava);
				paivitaKokoonpanonTulos();
				paivitaLisattyMaara();
				jLabel11.setText("Avattu: " + avattava.getFileName().toString());
				jButton7.setEnabled(false);
			}
		}.execute();
	}// GEN-LAST:event_jButton3ActionPerformed

	private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jButton2ActionPerformed
		// Tallenna nimellä
		Path viimeisin = Asetukset.getViimeisinKokoonpanoKansio();
		JFileChooser valitsin = new JFileChooser(viimeisin == null ? null : viimeisin.toFile());
		valitsin.setFileFilter(new FileNameExtensionFilter("Kokoonpanot (xml)", "xml"));
		int valinta = valitsin.showSaveDialog(rootPane);
		if (valinta != JFileChooser.APPROVE_OPTION) {
			return;
		}
		Path tallennettava = valitsin.getSelectedFile().toPath();
		Asetukset.setViimeisinKokoonpanoKansio(tallennettava.getParent());

		if (!tallennettava.toString().endsWith(".xml")) {
			tallennettava = Paths.get(tallennettava + ".xml");
		}

		if (Files.isRegularFile(tallennettava)) {
			Object[] napit = { "Kyllä", "Peruuta" };
			int korvaaValinta = JOptionPane.showOptionDialog(null,
					"Kyseinen tiedosto on jo olemassa, haluatko korvata sen?", "Varmistus",
					JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE, null, napit, napit[1]);
			if (korvaaValinta != JOptionPane.OK_OPTION) {
				return;
			}
		}

		tallenna(tallennettava);
	}// GEN-LAST:event_jButton2ActionPerformed

	private void jButton7ActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jButton7ActionPerformed
		// Tallenna
		tallenna(Kokoonpano.getTiedosto());
	}// GEN-LAST:event_jButton7ActionPerformed

	private void jSplitPane1ComponentResized(java.awt.event.ComponentEvent evt) {// GEN-FIRST:event_jSplitPane1ComponentResized
		jSplitPane1.setDividerLocation(splitPaneSuhde1);
	}// GEN-LAST:event_jSplitPane1ComponentResized

	private void jSplitPane2ComponentResized(java.awt.event.ComponentEvent evt) {// GEN-FIRST:event_jSplitPane2ComponentResized
		jSplitPane2.setDividerLocation(splitPaneSuhde2);
	}// GEN-LAST:event_jSplitPane2ComponentResized

	private void jButton9ActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jButton9ActionPerformed
		// Vaihda (Suodattimet)
		Path viimeisin = Asetukset.getViimeisinSuodatinKansio();
		JFileChooser valitsin = new JFileChooser(viimeisin == null ? null : viimeisin.toFile());
		valitsin.setFileFilter(new FileNameExtensionFilter("Suodattimet (xml)", "xml"));
		int valinta = valitsin.showOpenDialog(rootPane);
		if (valinta != JFileChooser.APPROVE_OPTION) {
			return;
		}
		Path avattava = valitsin.getSelectedFile().toPath();
		Asetukset.setViimeisinSuodatinKansio(avattava.getParent());

		try {
			Suodattimet.vaihda(avattava);
			Asetukset.setSuodatintiedosto(avattava);
			jLabel19.setText(avattava.toAbsolutePath().toString());
		} catch (IOException ex) {
			JOptionPane.showMessageDialog(null, "Tiedoston avaaminen epäonnistui!", "Virhe!",
					JOptionPane.ERROR_MESSAGE);
		} catch (JDOMException | Suodattimet.SuodatinTiedostoException ex) {
			JOptionPane.showMessageDialog(null, "Avattu tiedosto ei ole suodatintiedosto!", "Virhe!",
					JOptionPane.ERROR_MESSAGE);
		}
	}// GEN-LAST:event_jButton9ActionPerformed

	private void jButton8ActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jButton8ActionPerformed
		// Tallenna nimellä (Suodattimet)
		Path viimeisin = Asetukset.getViimeisinSuodatinKansio();
		JFileChooser valitsin = new JFileChooser(viimeisin == null ? null : viimeisin.toFile());
		valitsin.setFileFilter(new FileNameExtensionFilter("Suodattimet (xml)", "xml"));
		int valinta = valitsin.showSaveDialog(rootPane);
		if (valinta != JFileChooser.APPROVE_OPTION) {
			return;
		}
		Path tallennettava = valitsin.getSelectedFile().toPath();
		Asetukset.setViimeisinSuodatinKansio(tallennettava.getParent());

		if (!tallennettava.toString().endsWith(".xml")) {
			tallennettava = Paths.get(tallennettava + ".xml");
		}

		if (Files.isRegularFile(tallennettava)) {
			Object[] napit = { "Kyllä", "Peruuta" };
			int korvaaValinta = JOptionPane.showOptionDialog(null,
					"Kyseinen tiedosto on jo olemassa, haluatko korvata sen?", "Varmistus",
					JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE, null, napit, napit[1]);
			if (korvaaValinta != JOptionPane.OK_OPTION) {
				return;
			}
		}

		try {
			Suodattimet.tallennaNimella(tallennettava);
			Asetukset.setSuodatintiedosto(tallennettava);
			jLabel19.setText(tallennettava.toAbsolutePath().toString());
		} catch (IOException ex) {
			JOptionPane.showMessageDialog(null, "Tiedoston tallentaminen epäonnistui!", "Virhe!",
					JOptionPane.ERROR_MESSAGE);
		}
	}// GEN-LAST:event_jButton8ActionPerformed

	private class RaakaTuote {

		public final String url;
		public final Document dok;
		public final int maara;

		public RaakaTuote(String url, Document dok, int maara) {
			this.url = url;
			this.dok = dok;
			this.maara = maara;
		}
	}

	private class KokoonpanoTiedostoException extends Exception {
	}

	private class ListenerSplitPaneUI1 extends BasicSplitPaneUI {

		@Override
		protected void finishDraggingTo(int location) {
			super.finishDraggingTo(location);
			haeSuhde1(location);
		}
	}

	private class ListenerSplitPaneUI2 extends BasicSplitPaneUI {

		@Override
		protected void finishDraggingTo(int location) {
			super.finishDraggingTo(location);
			haeSuhde2(location);
		}
	}

	private void haeSuhde1(int location) {
		int korkeus = jSplitPane1.getHeight();
		int jakajanKorkeus = jSplitPane1.getDividerSize();
		if (jakajanKorkeus >= korkeus) {
			return;
		}
		splitPaneSuhde1 = (double) location / (korkeus - jakajanKorkeus);
	}

	private void haeSuhde2(int location) {
		int leveys = jSplitPane2.getWidth();
		int jakajanLeveys = jSplitPane2.getDividerSize();
		if (jakajanLeveys >= leveys) {
			return;
		}
		splitPaneSuhde2 = (double) location / (leveys - jakajanLeveys);
	}

	private class JTable1Format implements WritableTableFormat<KokoonpanoTuote>, AdvancedTableFormat<KokoonpanoTuote> {

		@Override
		public boolean isEditable(KokoonpanoTuote baseObject, int column) {
			switch (column) {
			case 0:
			case 5:
				return true;
			case 1:
			case 2:
			case 3:
			case 4:
				return false;
			default:
				throw new IllegalArgumentException();
			}
		}

		@Override
		public KokoonpanoTuote setColumnValue(KokoonpanoTuote baseObject, Object editedValue, int column) {
			switch (column) {
			case 0:
				baseObject.setPoistettava((boolean) editedValue);
				return null;
			case 5:
				baseObject.setMaara((int) editedValue);

				paivitaKokoonpanonTulos();
				paivitaLisattyMaara();
				jButton7.setEnabled(Kokoonpano.getTiedosto() != null);

				return null;
			default:
				throw new IllegalArgumentException();
			}
		}

		@Override
		public int getColumnCount() {
			return 6;
		}

		@Override
		public String getColumnName(int column) {
			switch (column) {
			case 0:
				return "";
			case 1:
				return "Tuote";
			case 2:
				return "Kauppa";
			case 3:
				return "Toimitusaika";
			case 4:
				return "Kappalehinta";
			case 5:
				return "Määrä";
			default:
				throw new IllegalArgumentException();
			}
		}

		@Override
		public Object getColumnValue(KokoonpanoTuote baseObject, int column) {
			Hinta valittu;
			switch (column) {
			case 0:
				return baseObject.isPoistettava();
			case 1:
				return baseObject.getNimi();
			case 2:
				valittu = baseObject.getValittuHinta();
				return valittu == null ? "Ei saatavilla" : valittu.getKaupanNimi();
			case 3:
				valittu = baseObject.getValittuHinta();
				return valittu == null ? "" : valittu.getToimitusaika();
			case 4:
				valittu = baseObject.getValittuHinta();
				return valittu == null ? "" : Muunnin.longToString(valittu.getHinta());
			case 5:
				return baseObject.getMaara();
			default:
				throw new IllegalArgumentException();
			}
		}

		@Override
		public Class getColumnClass(int column) {
			switch (column) {
			case 0:
				return Boolean.class;
			case 1:
			case 2:
			case 3:
			case 4:
				return String.class;
			case 5:
				return Integer.class;
			default:
				throw new IllegalArgumentException();
			}
		}

		@Override
		public Comparator getColumnComparator(int column) {
			switch (column) {
			case 0:
			case 1:
			case 2:
			case 3:
			case 5:
				return GlazedLists.comparableComparator();
			case 4:
				return hintaComp;
			default:
				throw new IllegalArgumentException();
			}
		}
	}

	private class JTable2Format implements AdvancedTableFormat<KaupoittainRivi> {

		@Override
		public Class getColumnClass(int column) {
			return String.class;
		}

		@Override
		public Comparator getColumnComparator(int column) {
			switch (column) {
			case 0:
			case 3:
				return GlazedLists.comparableComparator();
			case 1:
			case 2:
				return hintaComp;
			default:
				throw new IllegalArgumentException();
			}
		}

		@Override
		public int getColumnCount() {
			return 4;
		}

		@Override
		public String getColumnName(int column) {
			switch (column) {
			case 0:
				return "Kauppa";
			case 1:
				return "Hinta";
			case 2:
				return "Toimituskulut";
			case 3:
				return "Määrä";
			default:
				throw new IllegalArgumentException();
			}
		}

		@Override
		public Object getColumnValue(KaupoittainRivi baseObject, int column) {
			switch (column) {
			case 0:
				return baseObject.getKaupanNimi();
			case 1:
				return Muunnin.longToString(baseObject.getHinta());
			case 2:
				return Muunnin.longToString(baseObject.getPostikulut());
			case 3:
				return baseObject.getMaara();
			default:
				throw new IllegalArgumentException();
			}
		}
	}

	private class JTable3Format implements WritableTableFormat<Kauppa>, AdvancedTableFormat<Kauppa> {

		@Override
		public boolean isEditable(Kauppa baseObject, int column) {
			switch (column) {
			case 0:
				return false;
			case 1:
			case 2:
				return true;
			default:
				throw new IllegalArgumentException();
			}
		}

		@Override
		public Kauppa setColumnValue(Kauppa baseObject, Object editedValue, int column) {
			switch (column) {
			case 1:
				baseObject.setVoiNoutaa((boolean) editedValue);
				Suodattimet.tallenna();
				return null;
			case 2:
				baseObject.setSuodataPois((boolean) editedValue);
				Suodattimet.tallenna();
				return null;
			default:
				throw new IllegalArgumentException();
			}
		}

		@Override
		public int getColumnCount() {
			return 3;
		}

		@Override
		public String getColumnName(int column) {
			switch (column) {
			case 0:
				return "Kauppa";
			case 1:
				return "Voi noutaa";
			case 2:
				return "Suodata pois";
			default:
				throw new IllegalArgumentException();
			}
		}

		@Override
		public Object getColumnValue(Kauppa baseObject, int column) {
			switch (column) {
			case 0:
				return baseObject.getKaupanNimi();
			case 1:
				return baseObject.isVoiNoutaa();
			case 2:
				return baseObject.isSuodataPois();
			default:
				throw new IllegalArgumentException();
			}
		}

		@Override
		public Class getColumnClass(int column) {
			switch (column) {
			case 0:
				return String.class;
			case 1:
			case 2:
				return Boolean.class;
			default:
				throw new IllegalArgumentException();
			}
		}

		@Override
		public Comparator getColumnComparator(int column) {
			return GlazedLists.comparableComparator();
		}

	}

	/**
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String args[]) {
		/* Set the Nimbus look and feel */
		// <editor-fold defaultstate="collapsed" desc=" Look and feel setting
		// code (optional) ">
		/*
		 * If Nimbus (introduced in Java SE 6) is not available, stay with the
		 * default look and feel. For details see
		 * http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.
		 * html
		 */
		try {
			javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| javax.swing.UnsupportedLookAndFeelException ex) {
			java.util.logging.Logger.getLogger(HintahakuFrame.class.getName()).log(java.util.logging.Level.SEVERE, null,
					ex);
		}
		// </editor-fold>

		/* Create and display the form */
		java.awt.EventQueue.invokeLater(() -> {
			new HintahakuFrame().setVisible(true);
		});
	}

	// Variables declaration - do not modify//GEN-BEGIN:variables
	private javax.swing.JButton jButton1;
	private javax.swing.JButton jButton2;
	private javax.swing.JButton jButton3;
	private javax.swing.JButton jButton4;
	private javax.swing.JButton jButton5;
	private javax.swing.JButton jButton6;
	private javax.swing.JButton jButton7;
	private javax.swing.JButton jButton8;
	private javax.swing.JButton jButton9;
	private javax.swing.JLabel jLabel1;
	private javax.swing.JLabel jLabel10;
	private javax.swing.JLabel jLabel11;
	private javax.swing.JLabel jLabel12;
	private javax.swing.JLabel jLabel13;
	private javax.swing.JLabel jLabel14;
	private javax.swing.JLabel jLabel15;
	private javax.swing.JLabel jLabel16;
	private javax.swing.JLabel jLabel17;
	private javax.swing.JLabel jLabel18;
	private javax.swing.JLabel jLabel19;
	private javax.swing.JLabel jLabel2;
	private javax.swing.JLabel jLabel20;
	private javax.swing.JLabel jLabel3;
	private javax.swing.JLabel jLabel4;
	private javax.swing.JLabel jLabel5;
	private javax.swing.JLabel jLabel6;
	private javax.swing.JLabel jLabel7;
	private javax.swing.JLabel jLabel8;
	private javax.swing.JLabel jLabel9;
	private javax.swing.JPanel jPanel1;
	private javax.swing.JPanel jPanel2;
	private javax.swing.JPanel jPanel3;
	private javax.swing.JPanel jPanel4;
	private javax.swing.JPanel jPanel5;
	private javax.swing.JPanel jPanel6;
	private javax.swing.JPanel jPanel7;
	private javax.swing.JScrollPane jScrollPane1;
	private javax.swing.JScrollPane jScrollPane2;
	private javax.swing.JScrollPane jScrollPane3;
	private javax.swing.JScrollPane jScrollPane4;
	private javax.swing.JSeparator jSeparator1;
	private javax.swing.JSplitPane jSplitPane1;
	private javax.swing.JSplitPane jSplitPane2;
	private javax.swing.JTabbedPane jTabbedPane1;
	private javax.swing.JTable jTable1;
	private javax.swing.JTable jTable2;
	private javax.swing.JTable jTable3;
	private javax.swing.JTextField jTextField1;
	// End of variables declaration//GEN-END:variables

}
