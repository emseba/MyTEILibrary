package it.teilibrary.gui;

import it.teilibrary.Login;
import it.teilibrary.entity.Manuscript;
import it.teilibrary.entity.ManuscriptScan;
import it.teilibrary.util.HibernateUtil;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

/**
 * Maschera di acquisizione paper. Viene semplificato il processo di caricamento
 * delle immagini da scanner planetario, caricando le immagini direttamente da
 * JFileChooser
 */
@SuppressWarnings("serial")
public class Acquisizione_paper extends JFrame {

	byte[] bFile;
	private final JComboBox textTITOLO;
	private final JComboBox textAUTORE;
	private boolean selection_change = false;

	public Acquisizione_paper() {
		this.setTitle("File Scan Acquisition");
		this.setSize(350, 200);
		this.setResizable(false);
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		Container c = this.getContentPane();
		c.setLayout(new GridLayout(0, 1));

		JPanel panel = new JPanel(new GridLayout(4, 2));
		JButton openButton = new JButton("Open");

		final JLabel statusbar = new JLabel("Output file selection");

		// Create a file chooser that opens up as an Open dialog
		openButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {

				FileFilter filter = new FileFilter() {
					@Override
					public String getDescription() {
						return "Immagini";
					}

					@Override
					public boolean accept(File f) {
						return f.getName().toLowerCase().endsWith(".jpg");
					}
				};

				JFileChooser chooser = new JFileChooser();
				chooser.addChoosableFileFilter(filter);
				chooser.setFileFilter(filter);
				chooser.setMultiSelectionEnabled(false);
				int option = chooser.showOpenDialog(Acquisizione_paper.this);
				if (option == JFileChooser.APPROVE_OPTION) {
					File file = chooser.getSelectedFile();
					statusbar.setText("You chose " + file.getName());

					// save image into database
					bFile = new byte[(int) file.length()];

					FileInputStream fileInputStream;
					try {
						// convert file into array of bytes
						fileInputStream = new FileInputStream(file);
						fileInputStream.read(bFile);
						fileInputStream.close();
					} catch (FileNotFoundException ex) {
						Logger.getLogger(Acquisizione_paper.class.getName()).log(Level.SEVERE, null, ex);
					} catch (IOException ex) {
						Logger.getLogger(Acquisizione_paper.class.getName()).log(Level.SEVERE, null, ex);
					}
				} else {
					statusbar.setText("You canceled.");
				}
			}
		});

		JLabel title = new JLabel("Titolo ");
		panel.add(title);
		List titoli = HibernateUtil.getHQLQueryResult("select a.title from Manuscript a");
		textTITOLO = new JComboBox(titoli.toArray(new String[0]));
		textTITOLO.setEditable(true);
		textTITOLO.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (!selection_change) {
					selection_change = true;
					Session session = HibernateUtil.getSessionFactory().openSession();
					session.beginTransaction();
					Criteria criteria = session.createCriteria(Manuscript.class);
					criteria.setFirstResult(0);
					criteria.setMaxResults(1);
					criteria.add(Restrictions.like("title", textTITOLO.getSelectedItem() + "%").ignoreCase());
					List<Manuscript> manuscript = criteria.list();
					if (manuscript.size() > 0) {
						textAUTORE.setSelectedItem(manuscript.get(0).getAuthor());
						textTITOLO.setSelectedItem(manuscript.get(0).getTitle());
					}
					selection_change = false;
				}
			}
		});
		panel.add(textTITOLO);
		JLabel autore = new JLabel("Autore ");
		panel.add(autore);
		List autori = HibernateUtil.getHQLQueryResult("select a.author from Manuscript a");
		textAUTORE = new JComboBox(autori.toArray(new String[0]));
		textAUTORE.setEditable(true);
		textAUTORE.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				if (!selection_change) {
					selection_change = true;
					Session session = HibernateUtil.getSessionFactory().openSession();
					session.beginTransaction();
					Criteria criteria = session.createCriteria(Manuscript.class);
					criteria.setFirstResult(0);
					criteria.setMaxResults(1);
					criteria.add(Restrictions.like("author", textAUTORE.getSelectedItem() + "%").ignoreCase());
					List<Manuscript> manuscript = criteria.list();
					if (manuscript.size() > 0) {
						textAUTORE.setSelectedItem(manuscript.get(0).getAuthor());
						textTITOLO.setSelectedItem(manuscript.get(0).getTitle());
					}
					selection_change = false;
				}
			}
		});

		panel.add(textAUTORE);
		JLabel descrizione = new JLabel("Descrizione immagine");
		panel.add(descrizione);
		final JTextField textDESCRIPTION = new JTextField("", 10);
		panel.add(textDESCRIPTION);

		panel.add(openButton);
		panel.add(statusbar);

		JButton save = new JButton("SALVA");
		save.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (bFile != null) {
					Session session = HibernateUtil.getSessionFactory().openSession();
					session.beginTransaction();

					Manuscript manuscript;

					Criteria criteria = session.createCriteria(Manuscript.class);
					criteria.add(Restrictions.eq("author", textAUTORE.getSelectedItem()));
					criteria.add(Restrictions.eq("title", textTITOLO.getSelectedItem()));
					Object result = criteria.uniqueResult();
					if (result != null) {
						manuscript = (Manuscript) result;
						System.out.println("Title = " + manuscript.getTitle());
					} else {
						manuscript = new Manuscript();
						manuscript.setAuthor((String) textAUTORE.getSelectedItem());
						manuscript.setTitle((String) textTITOLO.getSelectedItem());
						session.save(manuscript);
					}

					ManuscriptScan scan = new ManuscriptScan();
					scan.setUser(Login.session_user);
					scan.setImageScan(bFile);
					scan.setScanTime(new Date());
					scan.setImageDescription(textDESCRIPTION.getText());
					scan.setPubblication(false);
					scan.setManuscript(manuscript);
					session.save(scan);

					session.getTransaction().commit();
				}
			}
		});
		c.add(panel);
		c.add(save);

		if (Login.session_user.getRole().getAdministrator() || Login.session_user.getRole().getScan()) {
			this.setVisible(true);
		} else {
			this.setVisible(false);
			this.dispose();
			JOptionPane.showMessageDialog(rootPane, "Non si dispongono dei diritti necessari", "Attenzione",
					JOptionPane.WARNING_MESSAGE);
		}
	}
}
