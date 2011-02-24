package uk.org.glendale.worldgen.test;

import java.awt.GraphicsEnvironment;

import javax.persistence.EntityManager;

import org.hibernate.Session;

import uk.org.glendale.worldgen.astro.planet.Planet;
import uk.org.glendale.worldgen.astro.planet.PlanetFactory;
import uk.org.glendale.worldgen.astro.planet.builders.PlanetBuilder;
import uk.org.glendale.worldgen.astro.planet.builders.barren.Hadean;
import uk.org.glendale.worldgen.astro.sector.Sector;
import uk.org.glendale.worldgen.astro.sector.SectorFactory;
import uk.org.glendale.worldgen.astro.sector.SectorGenerator;
import uk.org.glendale.worldgen.astro.starsystem.StarSystemFactory;
import uk.org.glendale.worldgen.server.AppManager;
import uk.org.glendale.worldgen.text.Names;

/**
 * Class to test Hibernate connectivity.
 * 
 * @author Samuel Penn
 */
public class Hibernate {
	public static void main(String[] args) throws Exception {
		// Session session = HibernateUtil.getSessionFactory().openSession();
		// Transaction tx = session.beginTransaction();

		/*
		 * Message message = new Message("Hello World"); Long msgId =
		 * (Long)session.save(message);
		 * 
		 * tx.commit(); session.close();
		 */

		// Second unit of work

		AppManager appManager = new AppManager();

		Session newSession = appManager.getHibernate().openSession();

		EntityManager em = appManager.getEntityManager();
		PlanetFactory pf = new PlanetFactory(em);
		Planet p = pf.getPlanet(601534);
		System.out.println(GraphicsEnvironment.isHeadless());
		PlanetBuilder barren = new Hadean();
		StarSystemFactory ssf = new StarSystemFactory(em);
		barren.setEntityManager(em);
		barren.setStar(ssf.getStarSystem(40389).getStars().get(0));
		barren.setPlanet(p);
		barren.generate();
		System.exit(0);

		/*
		 * Planet p = em.find(Planet.class, 595082);
		 * System.out.println(p.getName()); List<Resource> l = p.getResources();
		 * if (l == null) { System.out.println("No resources"); } else {
		 * System.out.println("Resources: "+l.size());
		 * System.out.println(l.get(0).getCommodity().getName()); }
		 */

		// Sector s = em.find(Sector.class, 132);
		// System.out.println(s.getName());

		SectorFactory sf = new SectorFactory(em);
		SectorGenerator sg = new SectorGenerator(em);
		/*
		 * StarSystemFactory ssf = new StarSystemFactory(em); StarSystem ss =
		 * ssf.getStarSystem(7545);
		 * 
		 * for (Planet planet : ss.getPlanets()) {
		 * System.out.println(planet.getName()); }
		 */
		Sector sector = sf.getSector("Test");
		sg.clearSector(sector);
		sg.fillRandomSector(sector, new Names("names"), 30);
		/*
		 * PlanetFactory pf = new PlanetFactory(em); Planet p =
		 * pf.getPlanet(597483); System.out.println(p.getName()); byte[] image =
		 * p.getFlatImage(); System.out.println(image.length);
		 */
		/*
		 * StarSystem ss = new StarSystemFactory(em).getStarSystem(39227);
		 * System.out.println(ss.getName()); for (Star star : ss.getStars()) {
		 * System.out.println(star.getName()+" ("+star.getId()+")"); }
		 */
		// sg.clearSector(sector);

		// System.out.println(sf.getSector("Xaagr").getId());
		// System.out.println(sf.getSector(132).getName());

		/*
		 * StarSystem ss = em.find(StarSystem.class, 37967);
		 * System.out.println(ss.getName());
		 * System.out.println(ss.getSector().getName()+": "+ss.getZone());
		 * System.out.println(ss.getPlanets().size()); for (Planet p :
		 * ss.getPlanets()) { System.out.println(p.getName()); }
		 */

		// StarSystemFactory ssf = new StarSystemFactory(em);
		// ssf.getStarSystemsInSector(s);

		/*
		 * Transaction newTransaction = newSession.beginTransaction();
		 * 
		 * List sectors =
		 * newSession.createQuery("from Sector m order by m.name asc").list();
		 * 
		 * System.out.println(sectors.size()+" sectors found:"); for (Iterator
		 * iter = sectors.iterator(); iter.hasNext(); ) { Sector loadedMsg =
		 * (Sector) iter.next(); System.out.println(loadedMsg.getName()); }
		 * 
		 * newTransaction.commit();
		 */
		newSession.close();

		appManager.closeHibernate();
	}
}
