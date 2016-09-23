package it.teilibrary.gui;

import it.teilibrary.Login;
import it.teilibrary.entity.Manuscript;
import it.teilibrary.entity.ManuscriptTranscription;
import it.teilibrary.util.HibernateUtil;
import it.teilibrary.util.TEIElaborator;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.BadLocationException;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

/**
 * Maschera visualizzazioni titoli e contenuto delle opere trascritte.
 * Viene consentita l'esportazione in formato TEI se le autorizzazioni utente lo consentono, 
 * altrimenti è possibile la sola consultazione dei titoli e autori delle opere trascritte.
 */
@SuppressWarnings("serial")
public class Visualizzazione_opere extends JFrame {

    private final JFrame frame;
    private final JTextPane textHTML;

    public Visualizzazione_opere() {
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        frame = (JFrame) SwingUtilities.getRoot(this);

        Object[][] rowData = tableData();
        Object columnNames[] = {"ID", "Titolo", "Autore"};
        final DefaultTableModel model = new DefaultTableModel(rowData, columnNames);
        final JTable table = new JTable(model) {
            @Override
            public Class getColumnClass(int column) {
                switch (column) {
                    case 0:
                        return Integer.class;
                    default:
                        return String.class;
                }
            }
        };
        table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (table.getSelectedRow() > -1) {
                    if (Login.session_user.getRole().getAdministrator() || Login.session_user.getRole().getAdvancedUser()) {
                        List<ManuscriptTranscription> l = HibernateUtil.getHQLQueryResult("select t from ManuscriptScan a left join a.manuscriptTranscriptions t where a.manuscript.idmanuscript=" + table.getValueAt(table.getSelectedRow(), 0).toString() + " and a.pubblication=true and coalesce(t.pubblication,false)=true order by a.idmanuscriptScan ASC");

                        textHTML.setText("");
                        HTMLDocument doc = (HTMLDocument) textHTML.getStyledDocument();
                        HTMLEditorKit kit = (HTMLEditorKit) textHTML.getEditorKit();
                        for (ManuscriptTranscription html : l) {
                            try {
                                kit.insertHTML(doc, doc.getLength(), html.getManuscriptXmlTei(), 0, 0, null);
                            } catch (BadLocationException | IOException ex) {
                                Logger.getLogger(Visualizzazione_opere.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                    } else {
                        JOptionPane.showMessageDialog(rootPane, "Non si dispongono dei diritti necessari", "Attenzione", JOptionPane.WARNING_MESSAGE);
                    }
                }
            }
        });
        final JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem deleteItem = new JMenuItem("Esporta TEI");
        deleteItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fc = new JFileChooser();
                File destFile = null;

                FileFilter filter = new FileFilter() {

                    @Override
                    public boolean accept(File f) {
                        if (f.isAbsolute()) {
                            return true;
                        }
                        String extension = f.getName();
                        if (extension != null) {
                            if (extension.endsWith("xml.tei")) {
                                return true;
                            } else {
                                return false;
                            }
                        }

                        return false;
                    }

                    @Override
                    public String getDescription() {
                        return "File TEI";
                    }
                };
                fc.addChoosableFileFilter(filter);
                fc.setFileFilter(filter);

                int returnVal = fc.showSaveDialog(frame);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    destFile = new File(fc.getSelectedFile().toString().endsWith("xml.tei") ? fc.getSelectedFile().toString() : fc.getSelectedFile().toString() + ".xml.tei");

                    try {
                        FileWriter fw = new FileWriter(destFile);
                        List<ManuscriptTranscription> l = HibernateUtil.getHQLQueryResult("select t from ManuscriptScan a left join a.manuscriptTranscriptions t where a.manuscript.idmanuscript=" + table.getValueAt(table.getSelectedRow(), 0).toString() + " order by a.idmanuscriptScan ASC");

                        TEIElaborator.setPublisher(Login.session_user.getId().getUsername());
                        fw.write(TEIElaborator.export(l));
                        fw.flush();
                        fw.close();
                    } catch (IOException ex) {
                        Logger.getLogger(Visualizzazione_opere.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (Exception ex) {
                        Logger.getLogger(Visualizzazione_opere.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        });
        popupMenu.add(deleteItem);
        table.setComponentPopupMenu(popupMenu);

        textHTML = new JTextPane();
        textHTML.setContentType("text/html");

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(600, 200));
        this.add(scrollPane, BorderLayout.NORTH);
        this.add(textHTML, BorderLayout.CENTER);
        this.setSize(600, 400);
        this.pack();
        if (Login.session_user.getRole().getAdministrator() || Login.session_user.getRole().getBaseUser()) {
            this.setVisible(true);
        } else {
            this.setVisible(false);
            this.dispose();
            JOptionPane.showMessageDialog(rootPane, "Non si dispongono dei diritti necessari", "Attenzione", JOptionPane.WARNING_MESSAGE);
        }
    }

    private Object[][] tableData() {
        List<Manuscript> l = HibernateUtil.getHQLQueryResult("select distinct m from ManuscriptTranscription t left join t.manuscriptScan s left join s.manuscript m  order by m.title ASC");
        Object[][] rowData = new Object[l.size()][3];
        int i = 0;
        for (Manuscript m : l) {
            rowData[i][0] = m.getIdmanuscript();
            rowData[i][1] = m.getTitle();
            rowData[i][2] = m.getAuthor();
            i++;
        }
        return rowData;
    }
}
