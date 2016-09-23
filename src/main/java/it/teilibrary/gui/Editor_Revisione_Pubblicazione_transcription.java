package it.teilibrary.gui;

import chrriis.dj.nativeswing.swtimpl.NativeInterface;
import chrriis.dj.nativeswing.swtimpl.components.JHTMLEditor;
import it.teilibrary.Login;
import it.teilibrary.entity.ManuscriptScan;
import it.teilibrary.entity.ManuscriptTranscription;
import it.teilibrary.entity.ManuscriptTranscriptionId;
import it.teilibrary.entity.User;
import it.teilibrary.util.HibernateUtil;
import it.teilibrary.util.imagePanel.NavigableImagePanel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

/**
 * Maschera di editing immagini acquistite, revisione e pubblicazione.
 * L'abilitazione delle funzioni viene assicurata tramite la gestione del ruolo
 * utente autenticato
 */
@SuppressWarnings("serial")
public class Editor_Revisione_Pubblicazione_transcription extends JFrame {

    private final JFrame frame;
    private int current_id = -1;
    User transcription_user = Login.session_user;

    public Editor_Revisione_Pubblicazione_transcription() {
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        frame = (JFrame) SwingUtilities.getRoot(this);

        final NavigableImagePanel imagePanel = new NavigableImagePanel();

        if (NativeInterface.isOpen()) {
            NativeInterface.close();
        }
        NativeInterface.open();

        Map<String, String> optionMap = new HashMap<>();
        optionMap.put("theme_advanced_buttons1", "'bold,italic,underline,strikethrough,sub,sup,|,charmap,|,justifyleft,justifycenter,justifyright,justifyfull,|,hr,removeformat'");
        optionMap.put("theme_advanced_buttons2", "'undo,redo,|,cut,copy,paste,pastetext,pasteword,|,search,replace,|,forecolor,backcolor,bullist,numlist,|,outdent,indent,blockquote,|,table'");
        optionMap.put("theme_advanced_buttons3", "''");
        optionMap.put("theme_advanced_toolbar_location", "'top'");
        optionMap.put("theme_advanced_toolbar_align", "'left'");
        optionMap.put("language", "'it'");
        optionMap.put("plugins", "'table,paste,contextmenu'");
        final JHTMLEditor htmlEditor = new JHTMLEditor(JHTMLEditor.HTMLEditorImplementation.TinyMCE,
                JHTMLEditor.TinyMCEOptions.setOptions(optionMap)
        );

        Object[][] rowData = tableData();
        Object columnNames[] = {"ID", "Titolo", "Autore", "Descrizione Scansione", "Data Acquisizione", "Pubblicazione"};
        final DefaultTableModel model = new DefaultTableModel(rowData, columnNames);
        final JTable table = new JTable(model) {

            @Override
            public Class getColumnClass(int column) {
                switch (column) {
                    case 0:
                        return Integer.class;
                    case 1:
                        return String.class;
                    case 2:
                        return String.class;
                    case 3:
                        return String.class;
                    case 4:
                        return Date.class;
                    default:
                        return Boolean.class;
                }
            }
        };
        table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (table.getSelectedRow() > -1) {
                    if (table.getSelectedColumn() == 5) {
                        if ((boolean) table.getValueAt(table.getSelectedRow(), 5)) {
                            int n = JOptionPane.showConfirmDialog(frame, "Vuoi pubblicare " + table.getValueAt(table.getSelectedRow(), 3) + "?", "Pubblicazione", JOptionPane.YES_NO_OPTION);
                            if (Login.session_user.getRole().getAdministrator() || Login.session_user.getRole().getTranscriberRevisor()) {
                                if (n == JOptionPane.YES_OPTION) {
                                    Session session = HibernateUtil.getSessionFactory().openSession();
                                    session.beginTransaction();
                                    current_id = (Integer) table.getValueAt(table.getSelectedRow(), 0);
                                    ManuscriptScan manuscriptScan = (ManuscriptScan) session.load(ManuscriptScan.class, current_id);

                                    Criteria criteria = session.createCriteria(ManuscriptTranscription.class);
                                    criteria.add(Restrictions.eq("user", transcription_user));
                                    criteria.add(Restrictions.eq("manuscriptScan", manuscriptScan));
                                    ManuscriptTranscription manuscriptTranscription = (ManuscriptTranscription) criteria.uniqueResult();

                                    if (manuscriptTranscription != null) {
                                        manuscriptTranscription.setPubblication(true);
                                        session.save(manuscriptTranscription);
                                        session.getTransaction().commit();
                                    } else {
                                        JOptionPane.showMessageDialog(frame, "Caricare il dettaglio trascrizione per pubblicare la revisione dell'utente da pubblicare", "Attenzione", JOptionPane.WARNING_MESSAGE);
                                    }

                                    model.setRowCount(0);
                                    for (Object[] row : tableData()) {
                                        model.addRow(row);
                                    }
                                } else {
                                    table.setValueAt(false, table.getSelectedRow(), 5);
                                }
                            }
                        } else {
                            JOptionPane.showMessageDialog(rootPane, "Non si dispone dei diritti di revisione", "Attenzione", JOptionPane.WARNING_MESSAGE);
                            table.setValueAt(false, table.getSelectedRow(), 5);
                        }
                    } else {
                        Session session = HibernateUtil.getSessionFactory().openSession();
                        session.beginTransaction();

                        //se l'utente è di revisione, può caricare le trascrizioni di altri utenti
                        try {
                            if (Login.session_user.getRole().getAdministrator() || Login.session_user.getRole().getTranscriberRevisor()) {
                                ManuscriptScan manuscriptScan = (ManuscriptScan) session.load(ManuscriptScan.class, (Integer) table.getValueAt(table.getSelectedRow(), 0));
                                DefaultComboBoxModel<User> DLM = new DefaultComboBoxModel<>();
                                if (manuscriptScan.getManuscriptTranscriptions().size() > 0) {
                                    for (ManuscriptTranscription m : manuscriptScan.getManuscriptTranscriptions()) {
                                        DLM.addElement(m.getUser());
                                    }

                                    JComboBox comboUser = new JComboBox();
                                    comboUser.setModel(DLM);

                                    JOptionPane.showMessageDialog(frame, comboUser, "Seleziona utente da revisionare", JOptionPane.QUESTION_MESSAGE);
                                    transcription_user = (User) comboUser.getSelectedItem();
                                } else {
                                    transcription_user = Login.session_user;
                                }
                            }

                            List<Object[]> l = HibernateUtil.getHQLQueryResult("select scan.imageScan, transcription.manuscriptXmlTei from ManuscriptScan scan left join scan.manuscriptTranscriptions transcription where scan.idmanuscriptScan=" + table.getValueAt(table.getSelectedRow(), 0).toString() + (transcription_user != null ? " and coalesce(transcription.user.id.username,'" + transcription_user.getId().getUsername() + "') = '" + transcription_user.getId().getUsername() + "'" : ""));

                            InputStream in = new ByteArrayInputStream(((byte[]) l.get(0)[0]));
                            BufferedImage bImageFromConvert;
                            bImageFromConvert = ImageIO.read(in);

                            imagePanel.setImage(bImageFromConvert);
                            imagePanel.repaint();

                            System.out.println((String) l.get(0)[1]);

                            if (l.get(0)[1] != null) {
                                htmlEditor.setHTMLContent((String) l.get(0)[1]);
                            } else {
                                htmlEditor.setHTMLContent("");
                            }
                        } catch (IOException ex) {
                            Logger.getLogger(Revisione_Pubblicazione_paper.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            }
        });
        final JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem deleteItem = new JMenuItem("Delete");
        deleteItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(frame, "Right-click performed on table and choose DELETE");
            }
        });
        popupMenu.add(deleteItem);
        table.setComponentPopupMenu(popupMenu);

        JPanel buttonPane = new JPanel(new BorderLayout());
        Box horizontalBox = Box.createHorizontalBox();

        JButton save = new JButton("SALVA");
        save.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Session session = HibernateUtil.getSessionFactory().openSession();
                session.beginTransaction();
                ManuscriptScan manuscriptScan = (ManuscriptScan) session.load(ManuscriptScan.class, (Integer) table.getValueAt(table.getSelectedRow(), 0));

                Criteria criteria = session.createCriteria(ManuscriptTranscription.class);
                criteria.add(Restrictions.eq("user", transcription_user));
                criteria.add(Restrictions.eq("manuscriptScan", manuscriptScan));
                Object result = criteria.uniqueResult();
                ManuscriptTranscription manuscriptTranscription;
                if (result != null) {
                    if (Login.session_user.getRole().getAdministrator() || Login.session_user.getRole().getTranscriberRevisor()) {
                        manuscriptTranscription = (ManuscriptTranscription) result;
                    } else {
                        JOptionPane.showMessageDialog(rootPane, "Non si dispone dei diritti di revisione", "Attenzione", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                } else {
                    manuscriptTranscription = new ManuscriptTranscription();
                    manuscriptTranscription.setManuscriptScan(manuscriptScan);
                    manuscriptTranscription.setUser(transcription_user);
                    ManuscriptTranscriptionId id = new ManuscriptTranscriptionId(transcription_user.getId().getUsername(), transcription_user.getId().getRoleIdrole(), manuscriptScan.getIdmanuscriptScan());
                    manuscriptTranscription.setId(id);
                    manuscriptTranscription.setPubblication(false);
                }
                try {
                    manuscriptTranscription.setManuscriptXmlTei(htmlEditor.getHTMLContent());
                } catch (Exception ex) {
                    Logger.getLogger(Editor_Revisione_Pubblicazione_transcription.class.getName()).log(Level.SEVERE, null, ex);
                }

                session.save(manuscriptTranscription);
                session.getTransaction().commit();
            }
        });
        horizontalBox.add(Box.createGlue());
        horizontalBox.add(save);
        buttonPane.add(horizontalBox);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(600, 100));
        this.add(scrollPane, BorderLayout.NORTH);
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                imagePanel, htmlEditor);
        splitPane.setOneTouchExpandable(true);
        splitPane.setResizeWeight(0.5);
        this.add(splitPane, BorderLayout.CENTER);

        this.add(buttonPane, BorderLayout.SOUTH);
        this.setSize(600, 500);
        this.setPreferredSize(new Dimension(600, 500));
        this.pack();
        if (Login.session_user.getRole().getAdministrator() || Login.session_user.getRole().getTranscriber()) {
            this.setVisible(true);
        } else {
            this.setVisible(false);
            this.dispose();
            JOptionPane.showMessageDialog(rootPane, "Non si dispongono dei diritti necessari", "Attenzione", JOptionPane.WARNING_MESSAGE);
        }
    }

    private Object[][] tableData() {
        List<Object[]> l = HibernateUtil.getHQLQueryResult("select a.idmanuscriptScan, a.manuscript.title, a.manuscript.author, a.imageDescription, a.scanTime from ManuscriptScan a left join a.manuscriptTranscriptions b where a.pubblication=true and coalesce(b.pubblication,false) = false order by a.manuscript.author, a.manuscript.title");
        Object[][] rowData = new Object[l.size()][6];
        int i = 0;
        for (Object[] o : l) {
            rowData[i][0] = o[0];
            rowData[i][1] = o[1];
            rowData[i][2] = o[2];
            rowData[i][3] = o[3];
            rowData[i][4] = (Date) o[4];
            rowData[i][5] = false;
            i++;
        }
        return rowData;
    }
}
