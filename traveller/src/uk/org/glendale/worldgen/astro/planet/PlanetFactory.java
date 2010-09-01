package uk.org.glendale.worldgen.astro.planet;

import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.hibernate.Session;
import org.hibernate.Transaction;

import uk.org.glendale.worldgen.server.AppManager;

/**
 * Factory class for obtaining existing Planet objects.
 * 
 * @author Samuel Penn
 */
@ManagedBean
public class PlanetFactory {
	EntityManager	em;
	
	public PlanetFactory(EntityManager hibernateEntityManager) {
		em = hibernateEntityManager;
	}
	
	public PlanetFactory() {
		em = AppManager.getInstance().getEntityManager();
	}
	
	/**
	 * Gets a planet identified by its unique id.
	 * @param id
	 * @return
	 */
	public Planet getPlanet(int id) {
		return em.find(Planet.class, id);
	}
	
	public byte[] getPlanetImage(int id, MapImage.Projection projection) {
		Query query = em.createQuery("from MapImage m where m.id = :i and m.type = :p");
		query.setParameter("i", id);
		query.setParameter("p", projection);
		
		MapImage	image = (MapImage) query.getSingleResult();
		
		return image.getData();
	}
	
	
	public void close() {
		em.close();
	}
}
