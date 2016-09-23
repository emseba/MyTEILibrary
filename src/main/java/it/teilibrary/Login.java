package it.teilibrary;

import chrriis.dj.nativeswing.swtimpl.NativeInterface;
import it.teilibrary.entity.User;
import it.teilibrary.util.HibernateUtil;
import it.teilibrary.util.Splasher;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

/**
 * Login
 * @author emiliano
 */
@SuppressWarnings("serial")
public class Login extends JFrame {

    private final JTextField textUTENTE;
    private final JPasswordField textPASSWORD;
    private final JButton OKButton;
    private final JButton HELPButton;
    private final JLabel labelUTENTE;
    private final JLabel labelPASSWORD;
    public static final Splasher splasher = new Splasher();
    private final Container contentPane;
    private final JFrame frame;
    /**
     * Variabile di sessione utente
     */
    public static User session_user;

    /**
     * Create mask for login users, which have defined roles and permits.
     * If query result from login is correct, a static variable session_user is set with current loggen user information, user for authorization purposes.
     */
    public Login() {
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setResizable(false);

        this.setTitle("Login");

        frame = (JFrame) SwingUtilities.getRoot(this);

        textUTENTE = new JTextField(10);
        textUTENTE.setDocument(new PlainDocument() {
            @Override
            public void insertString(int offset, String string, AttributeSet attributeSet) throws BadLocationException {
                super.insertString(offset, string.toUpperCase(), attributeSet);
            }
        });
        textUTENTE.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                textUTENTE.transferFocus();
            }
        });
        textUTENTE.setText("");
        textUTENTE.setToolTipText("Inserire utente");
        textUTENTE.requestFocus();
        labelUTENTE = new JLabel();
        labelUTENTE.setLabelFor(textUTENTE);
        labelUTENTE.setText("Utente: ");

        textPASSWORD = new JPasswordField(10);
        textPASSWORD.setDocument(new PlainDocument() {

			@Override
            public void insertString(int offset, String string, AttributeSet attributeSet) throws BadLocationException {
                super.insertString(offset, string.toUpperCase(), attributeSet);
            }
        });
        textPASSWORD.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                textPASSWORD.transferFocus();
            }
        });
        textPASSWORD.setEchoChar('*');
        textPASSWORD.setText("");
        labelPASSWORD = new JLabel();
        labelPASSWORD.setLabelFor(textPASSWORD);
        labelPASSWORD.setText("Password: ");

        JPanel loginPane = new JPanel(new FlowLayout(FlowLayout.TRAILING));
        loginPane.add(labelUTENTE);
        loginPane.add(textUTENTE);
        loginPane.add(labelPASSWORD);
        loginPane.add(textPASSWORD);

        JPanel buttonPane = new JPanel(new GridLayout(0, 1));
        OKButton = new JButton("Login");
        OKButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                splasher.splashShow();
                new TaskMenuLauncher().execute();
            }
        });
        HELPButton = new JButton("Aiuto");

        buttonPane.add(OKButton);
        buttonPane.add(HELPButton);

        JPanel listaPanel = new JPanel(new BorderLayout());
        listaPanel.add(loginPane, BorderLayout.WEST);
        listaPanel.add(buttonPane, BorderLayout.EAST);

        contentPane = this.getContentPane();
        contentPane.setBackground(Color.WHITE); //contrasting bg

        contentPane.add(listaPanel, BorderLayout.LINE_START);
        this.pack();

        this.setVisible(true);
        textUTENTE.requestFocus();
    }

    /**
     * class user for tasks demonstrations with splash wait Window panel
     */
    private class TaskMenuLauncher extends SwingWorker<Void, Void> {

        @Override
        public Void doInBackground() {
            List<User> l = HibernateUtil.getHQLQueryResult("from User where UPPER(username)='" + textUTENTE.getText() + "' and UPPER(password)='" + String.valueOf(textPASSWORD.getPassword()) + "'");
            if (l.size() == 1) {
                User u = l.get(0);
                session_user = u;
                new Menu();
                frame.setVisible(false);
                frame.dispose();
            } else {
                JOptionPane.showMessageDialog(contentPane, "Username o password errata", "Attenzione", JOptionPane.WARNING_MESSAGE);
            }
            return null;
        }

        @Override
        public void done() {
            splasher.splashHide();
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
            UIManager.put("TextArea.font", "SansSerif 12");

        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            Logger.getLogger(Login.class
                    .getName()).log(Level.SEVERE, null, ex);
        }

        //browser panel initialization
        NativeInterface.initialize();

        javax.swing.SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                new Login();
            }
        });
    }
}
