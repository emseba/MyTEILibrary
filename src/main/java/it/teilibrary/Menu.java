package it.teilibrary;

import it.teilibrary.util.HibernateUtil;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

/**
 * Build menu, for logged user
 * @author emiliano
 */
@SuppressWarnings("serial")
public class Menu extends JFrame {

    private JMenuBar menuBar;
    private JMenu menu;

    Menu() {
        this.setTitle("Menu | " + Login.session_user.getId().getUsername());
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        this.setIconImage(new ImageIcon(this.getClass().getResource("/it/teilibrary/img/TEI.jpg")).getImage());

        this.setJMenuBar(createMenuBar(Login.session_user.getId().getUsername()));
        this.setContentPane(createContentPane());
        this.setBackground(Color.white);
        this.setSize(400, 300);

        this.setVisible(true);
    }

    public Container createContentPane() {
        ImageIcon icon = new ImageIcon(this.getClass().getResource("/it/teilibrary/img/TEI.jpg"));
        JLabel emptyLabel = new JLabel(icon);

        JPanel contentPane = new JPanel(new BorderLayout());
        contentPane.setOpaque(true);
        contentPane.setBackground(Color.white);
        contentPane.add(emptyLabel);

        return contentPane;
    }

    /**
     * creates menu for username, set as valid query login result from Login mask
     * @param username username logged in
     * @return gui for user's menu
     */
    private JMenuBar createMenuBar(String username) {
        menuBar = new JMenuBar();

        List l = HibernateUtil.getHQLQueryResult("select a.menutext from Menu a, User b where a.menuda is null and a.role = b.role and b.id.username='" + username + "'");
        for (Object o : l) {
            menu = new JMenu(String.valueOf(o));
            menuBar.add(menu);

            List<Object[]> l1 = HibernateUtil.getHQLQueryResult("select a.menutext, a.comando from Menu a, Menu b, User c where a.menuda = b.id.menuid and a.id.roleIdrole=c.id.roleIdrole and b.menutext='" + String.valueOf(o) + "' and c.id.username='" + username + "'");
            for (final Object[] o1 : l1) {
                JMenuItem menu1 = new JMenuItem(String.valueOf(o1[0]));
                menu1.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        openProcess("it.teilibrary.gui." + o1[1]);
                    }
                });
                menu.add(menu1);
            }
        }
        return menuBar;
    }

    /**
     * Open new instance of comando class, query result from menu build for current user
     * @param comando class name, for the function to launch from menu
     */
    private void openProcess(String comando) {
        Class aClass;
        try {
            aClass = Class.forName(comando);
            aClass.newInstance();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
            Logger.getLogger(Menu.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
