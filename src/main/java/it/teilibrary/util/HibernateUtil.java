package it.teilibrary.util;

import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;

/**
 * Hibernate Utility class with a convenient method to get Session Factory
 * object.
 *
 */
public class HibernateUtil {

	private static HibernateUtil instance = null;
	private static SessionFactory SESSIONFACTORY;

	// to disallow creating objects by other classes.
	private HibernateUtil() {
		Configuration configuration = new Configuration().configure("hibernate.cfg.xml");
		StandardServiceRegistryBuilder builder = new StandardServiceRegistryBuilder()
				.applySettings(configuration.getProperties());
		SESSIONFACTORY = configuration.buildSessionFactory(builder.build());
	}

	// making the Hibernate SessionFactory object as singleton
	public static synchronized SessionFactory getSessionFactory() {
		if (instance == null) {
			instance = new HibernateUtil();
		}
		return SESSIONFACTORY;
	}
}
