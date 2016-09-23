package it.teilibrary.util;

import java.util.List;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;

/**
 * Hibernate Utility class with a convenient method to get Session Factory
 * object.
 *
 */
public class HibernateUtil {

    private static SessionFactory SESSIONFACTORY;

    //to disallow creating objects by other classes.    
    private HibernateUtil() {
    }

    //making the Hibernate SessionFactory object as singleton
    public static synchronized SessionFactory getSessionFactory() {

        if (SESSIONFACTORY == null) {
            Configuration configuration = new Configuration().configure("hibernate.cfg.xml");
            StandardServiceRegistryBuilder builder = new StandardServiceRegistryBuilder().applySettings(configuration.getProperties());
            SESSIONFACTORY = configuration.buildSessionFactory(builder.build());
        }
        return SESSIONFACTORY;
    }

    /**
     * Utility function to run query
     *
     * @param <T> Generic type as return of query list
     * @param hql Query HQL format
     * @return List of Objects as query result
     */
    public static <T> List<T> getHQLQueryResult(String hql) {
        Session session = getSessionFactory().openSession();
        session.beginTransaction();
        Query q = session.createQuery(hql);
        session.getTransaction().commit();
        return q.list();
    }
}
