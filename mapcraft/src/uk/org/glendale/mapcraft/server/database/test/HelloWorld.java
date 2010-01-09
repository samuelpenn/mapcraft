package uk.org.glendale.mapcraft.server.database.test;

import java.util.*;
import org.hibernate.*;

public class HelloWorld {
	public static void main(String[] args) {
		Session		session = HibernateUtil.getSessionFactory().openSession();
		Transaction	tx = session.beginTransaction();
		
		Message message = new Message("Hello World");
		Long msgId = (Long)session.save(message);
		
		tx.commit();
		session.close();
		
		// Second unit of work
		Session		newSession = HibernateUtil.getSessionFactory().openSession();
		Transaction	newTransaction = newSession.beginTransaction();
		
		List		messages = newSession.createQuery("from Message m order by m.text asc").list();
		
		System.out.println(messages.size()+" message(s) found:");
		for (Iterator iter = messages.iterator(); iter.hasNext(); ) {
			Message		loadedMsg = (Message) iter.next();
			System.out.println(loadedMsg.getText());
		}
		
		newTransaction.commit();
		newSession.close();
		
		HibernateUtil.shutdown();
	}
}
