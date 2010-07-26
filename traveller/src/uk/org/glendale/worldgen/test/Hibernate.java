package uk.org.glendale.worldgen.test;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.hibernate.Session;
import org.hibernate.Transaction;

import uk.org.glendale.worldgen.astro.sector.Sector;
import uk.org.glendale.worldgen.astro.sector.SectorFactory;
import uk.org.glendale.worldgen.astro.starsystem.StarSystem;
import uk.org.glendale.worldgen.server.AppManager;
import uk.org.glendale.worldgen.server.HibernateUtil;


/**
 * Class to test Hibernate connectivity.
 * 
 * @author Samuel Penn
 */
public class Hibernate {
	public static void main(String[] args) {
		//Session		session = HibernateUtil.getSessionFactory().openSession();
		//Transaction	tx = session.beginTransaction();
		
		/*
		Message message = new Message("Hello World");
		Long msgId = (Long)session.save(message);
		
		tx.commit();
		session.close();
		*/
		
		// Second unit of work
		
		
		AppManager	appManager = new AppManager();
		
		Session		newSession = appManager.getHibernate().openSession();
		
		EntityManager em = appManager.getEntityManager();
		Sector s = em.find(Sector.class, 132);
		System.out.println(s.getName());
		
		SectorFactory	sf = new SectorFactory(em);
		
		System.out.println(sf.getSector("Xaagr").getId());
		System.out.println(sf.getSector(132).getName());
		
		StarSystem ss = em.find(StarSystem.class, 37967);
		System.out.println(ss.getName());
		System.out.println(ss.getSector().getName());
		
		/*
		Transaction	newTransaction = newSession.beginTransaction();
		
		List		sectors = newSession.createQuery("from Sector m order by m.name asc").list();
		
		System.out.println(sectors.size()+" sectors found:");
		for (Iterator iter = sectors.iterator(); iter.hasNext(); ) {
			Sector		loadedMsg = (Sector) iter.next();
			System.out.println(loadedMsg.getName());
		}
		
		newTransaction.commit();
		*/
		newSession.close();
		
		appManager.closeHibernate();		
	}
}
