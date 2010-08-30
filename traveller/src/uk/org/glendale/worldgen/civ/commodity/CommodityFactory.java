package uk.org.glendale.worldgen.civ.commodity;

import java.io.File;
import java.io.IOException;

import javax.faces.bean.ManagedBean;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;
import javax.transaction.Transaction;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import uk.org.glendale.rpg.traveller.civilisation.trade.CommodityCode;
import uk.org.glendale.rpg.traveller.civilisation.trade.Source;
import uk.org.glendale.worldgen.astro.sector.Sector;
import uk.org.glendale.worldgen.server.AppManager;

@ManagedBean
public class CommodityFactory {
	EntityManager	em;
	
	public CommodityFactory(EntityManager hibernateEntityManager) {
		if (em == null) {
			em = AppManager.getInstance().getEntityManager();
		}
		em = hibernateEntityManager;
	}
	
	public CommodityFactory() {
		em = AppManager.getInstance().getEntityManager();
	}
	
	public Commodity getCommodity(int id) {
		return em.find(Commodity.class, id);
	}
	
	public Commodity getCommodity(String name) {
		Query q = em.createQuery("from Commodity where name = :n");
		q.setParameter("n", name);
		return (Commodity)q.getSingleResult();
	}
	
	private void createCommodities(File file) throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilder	db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document		document = db.parse(file);
		NodeList		list = document.getElementsByTagName("commodity");
		
		EntityTransaction transaction = em.getTransaction();
		transaction.begin();
		
		for (int i=0; i < list.getLength(); i++) {
			Node		node = list.item(i);
			Commodity	commodity = new Commodity();
			String		name = node.getAttributes().getNamedItem("name").getNodeValue();
			commodity.setName(name);
			NodeList	params = node.getChildNodes();
			System.out.println(name);
			for (int j=0; j < params.getLength(); j++) {
				Node	p = params.item(j);
				if (p.getNodeType() == Node.ELEMENT_NODE) {
					String	pName = p.getNodeName();
					String  value = p.getTextContent().trim();
					System.out.println("  "+pName+": "+value);
					if (pName.equals("source")) {
						commodity.setSource(Source.valueOf(value));
					} else if (pName.equals("image")) {
						commodity.setImagePath(value);
					} else if (pName.equals("cost")) {
						commodity.setCost(Integer.parseInt(value));
					} else if (pName.equals("volume")) {
						commodity.setVolume(Integer.parseInt(value));
					} else if (pName.equals("law")) {
						commodity.setLawLevel(Integer.parseInt(value));
					} else if (pName.equals("tech")) {
						commodity.setTechLevel(Integer.parseInt(value));
					} else if (pName.equals("pr")) {
						commodity.setProductionRating(Integer.parseInt(value));
					} else if (pName.equals("cr")) {
						commodity.setConsumptionRating(Integer.parseInt(value));
					} else if (pName.equals("codes")) {
						String[] codes = value.split(" ");
						for (String c : codes) {
							System.out.println("["+c+"]");
							commodity.addCode(CommodityCode.valueOf(c));
						}
					}
				}
			}
			em.persist(commodity);
		}
		transaction.commit();
	}
	
	
	public static void main(String[] args) throws Exception {
		CommodityFactory	factory = new CommodityFactory();
		factory.createCommodities(new File("docs/commodities.xml"));
		
		Commodity 			c = factory.getCommodity(1);
		
		System.out.println(c.getName()+" ("+c.getSource()+")");
		while (c.getParent() != null) {
			c = c.getParent();
			System.out.println(c.getName());
		}
	}

}