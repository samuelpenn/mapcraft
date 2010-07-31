package uk.org.glendale.worldgen.astro.sector;

import java.util.Iterator;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.hibernate.Session;
import org.hibernate.Transaction;

import uk.org.glendale.worldgen.server.AppManager;

/**
 * Factory class for obtaining Sector objects.
 * 
 * @author Samuel Penn
 */
@ManagedBean
public class SectorFactory {
	EntityManager	em;
	
	public SectorFactory(EntityManager hibernateEntityManager) {
		em = hibernateEntityManager;
	}
	
	public SectorFactory() {
		em = AppManager.getInstance().getEntityManager();
	}
	
	@SuppressWarnings("unchecked")
	public List<Sector>	getAllSectors() {
		List<Sector>		sectors = em.createQuery("from Sector s order by s.name asc").getResultList();
		
		System.out.println(sectors.size()+" sectors found:");
		for (Iterator iter = sectors.iterator(); iter.hasNext(); ) {
			Sector		loadedMsg = (Sector) iter.next();
			System.out.println(loadedMsg.getName());
		}
		
		return sectors;
	}

	/**
	 * Gets a sector identified by its unique id.
	 * @param id
	 * @return
	 */
	public Sector getSector(int id) {
		return em.find(Sector.class, id);
	}
	
	/**
	 * Gets a sector identified by its name. If the name is entirely
	 * numeric, then it is assumed to be an id, and an id based search
	 * is returned instead.
	 * 
	 * @param name
	 * @return
	 */
	public Sector getSector(String name) {
		if (name.matches("[0-9]+")) {
			return getSector(Integer.parseInt(name));
		} else {
			Query q = em.createQuery("from Sector where name = :n");
			q.setParameter("n", name);
			return (Sector)q.getSingleResult();
		}
	}
	
	public void close() {
		em.close();
	}
}
