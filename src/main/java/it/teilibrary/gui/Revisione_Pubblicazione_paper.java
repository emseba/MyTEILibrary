package it.teilibrary.gui;

import it.teilibrary.Login;
import it.teilibrary.entity.ManuscriptScan;
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
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import org.hibernate.Session;

/**
 * Maschera di revisione scansione paper caricata a sistema
 */
@SuppressWarnings("serial")
public class Revisione_Pubblicazione_paper extends JFrame {

    private final JFrame frame;

    public Revisione_Pubblicazione_paper() {
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        frame = (JFrame) SwingUtilities.getRoot(this);

        final NavigableImagePanel imagePanel = new NavigableImagePanel();

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
                            Session session = HibernateUtil.getSessionFactory().openSession();
                            session.beginTransaction();
                            ManuscriptScan manuscriptScan = (ManuscriptScan) session.load(ManuscriptScan.class, (Integer) table.getValueAt(table.getSelectedRow(), 0));
                            manuscriptScan.setPubblication(true);
                            System.out.println(manuscriptScan.getImageDescription());
                            session.save(manuscriptScan);

                            session.getTransaction().commit();
                            if (n == JOptionPane.YES_OPTION) {
                                model.setRowCount(0);
                                for (Object[] row : tableData()) {
                                    model.addRow(row);
                                }
                            } else {
                                table.setValueAt(false, table.getSelectedRow(), 5);
                            }
                        }
                    } else {
                        try {
                            List<byte[]> l = HibernateUtil.getHQLQueryResult("select a.imageScan from ManuscriptScan a where a.idmanuscriptScan=" + table.getValueAt(table.getSelectedRow(), 0).toString());

                            InputStream in = new ByteArrayInputStream(l.get(0));
                            BufferedImage bImageFromConvert;
                            bImageFromConvert = ImageIO.read(in);

                            imagePanel.setImage(bImageFromConvert);
                            imagePanel.repaint();
                        } catch (IOException ex) {
                            Logger.getLogger(Revisione_Pubblicazione_paper.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    table.clearSelection();
                }
            }
        });
        final JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem deleteItem = new JMenuItem("Delete");
        deleteItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                Session session = HibernateUtil.getSessionFactory().openSession();
                session.beginTransaction();
                ManuscriptScan manuscriptScan = (ManuscriptScan) session.load(ManuscriptScan.class, (Integer) table.getValueAt(table.getSelectedRow(), 0));
                session.delete(manuscriptScan);
                session.getTransaction().commit();

                model.setRowCount(0);
                for (Object[] row : tableData()) {
                    model.addRow(row);
                }
            }
        });
        popupMenu.add(deleteItem);
        table.setComponentPopupMenu(popupMenu);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(600, 100));
        this.add(scrollPane, BorderLayout.NORTH);
        this.add(imagePanel, BorderLayout.CENTER);
        this.setSize(600, 400);
        this.pack();
        if (Login.session_user.getRole().getAdministrator() || Login.session_user.getRole().getScanRevisor()) {
            this.setVisible(true);
        } else {
            this.setVisible(false);
            this.dispose();
            JOptionPane.showMessageDialog(rootPane, "Non si dispongono dei diritti necessari", "Attenzione", JOptionPane.WARNING_MESSAGE);
        }
    }

    private Object[][] tableData() {
        List<Object[]> l = HibernateUtil.getHQLQueryResult("select a.idmanuscriptScan, b.title, b.author, a.imageDescription, a.scanTime from ManuscriptScan a, Manuscript b, User c where a.manuscript.idmanuscript=b.idmanuscript and a.user.id.username = c.id.username and a.pubblication=false and c.id.username='" + Login.session_user.getId().getUsername() + "'");
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
