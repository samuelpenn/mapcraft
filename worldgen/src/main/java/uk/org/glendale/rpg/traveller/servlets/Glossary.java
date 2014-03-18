/*
 * Copyright (C) 2006 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation version 2.
 * See the file COPYING.
 *
 * $Revision: 1.2 $
 * $Date: 2007/05/13 16:36:26 $
 */

package uk.org.glendale.rpg.traveller.servlets;

import java.io.*;
import java.net.MalformedURLException;
import java.sql.*;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

import uk.org.glendale.rpg.traveller.Config;
import uk.org.glendale.rpg.traveller.Log;
import uk.org.glendale.rpg.traveller.database.*;
import uk.org.glendale.rpg.traveller.glossary.GlossaryEntry;
import uk.org.glendale.rpg.traveller.sectors.*;
import uk.org.glendale.rpg.traveller.systems.*;

/**
 * Servlet which returns information on a glossary item. This is a short snippet
 * of information about a term. Information is returned as an HTML page of text.
 * 
 * @author Samuel Penn
 */
public class Glossary extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doPost(request, response);
    }
    
    protected void error(PrintWriter out, String message) {
    	out.println("<error>");
    	out.println("<message>");
    	out.println(message);
    	out.println("</message>");
    	out.println("</error>");
    }

    /**
     * Process requests from a client.
     */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {

		Log.init(getServletContext().getRealPath("/"), 2);

        PrintWriter	out = response.getWriter();
      	response.setContentType("text/html");
      	
      	String		uri = request.getPathInfo();
      	uri = uri.replaceAll("[^a-zA-Z0-9\\-]", "");
      	
      	String		title = null;
      	String		text = null;
        
      	if (uri == null || uri.trim().length() == 0) {
      		title = "Glossary Index";
      		text = createIndex();
      	} else {
      		try {
	      		GlossaryFactory		factory = new GlossaryFactory();
	      		GlossaryEntry 		entry = factory.getEntry(uri);
	      		
	      		if (entry == null) {
	      			title = "Searching for "+uri.toLowerCase();
	      			text = "Term not found.";
	      		} else {
	      			title = entry.getTitle();
	      			text = entry.getText();
	      		}
      		} catch (GlossaryException e) {
      			title = "Error";
      			text = "ERROR: "+e.getMessage();
      		}
      	}
      	
      	outputHeader(out, title);
      	outputBody(out, title, text);
      	outputFooter(out);
    }
	
	/**
	 * Output an index of all the glossary terms.
	 * 
	 * @param out	Output stream.
	 */
	private String createIndex() {
		StringBuffer		buffer = new StringBuffer();
		try {
			GlossaryFactory		factory = new GlossaryFactory();
			
			buffer.append("<p>Index of all entries currently in this glossary.</p>\n");

			for (char az : "abcdefghijklmnopqrstuvwxyz".toCharArray()) {
				Iterator<GlossaryEntry>		iter = factory.iterator(az+"%");
				
				if (iter.hasNext()) {
					buffer.append("<h2>"+az+"</h2>\n");
					buffer.append("<p>");
					
					while (iter.hasNext()) {
						GlossaryEntry		entry = iter.next();
						
						buffer.append("<a href=\"");
						buffer.append(entry.getUri());
						buffer.append("\">");
						buffer.append(entry.getTitle());
						buffer.append("</a>");
						if (iter.hasNext()) {
							buffer.append("; ");
						} else {
							buffer.append(".");
						}
					}
					buffer.append("</p>");
				}
			}
		} catch (GlossaryException e) {
			Log.error("Error trying to create glossary index", e);
		}
		
		return buffer.toString();
	}
	
	private void outputHeader(PrintWriter out, String title) {
		out.println("<html>");
		out.println("<head>");
		out.println("<title>"+title+"</title>");
		out.println("<link rel=\"STYLESHEET\" type=\"text/css\" media=\"screen\" href=\"../css/glossary.css\" />");
		out.println("</head>");
		
		out.println("\n<body>");
	}
	
	private void outputFooter(PrintWriter out) {
		out.println("<p>Return to <a href=\".\">glossary index</a></p>");
		out.println("</body>");
		out.println("</html>");
	}
	
	private void outputBody(PrintWriter out, String title, String text) {
		out.println("<h1>"+title+"</h1>");

		out.println(text);
	}
}
