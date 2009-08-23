/*
 * Copyright (C) 2009 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation version 2.
 * See the file COPYING.
 */
package uk.org.glendale.rpg.traveller.servlets;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Hashtable;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.swing.text.NumberFormatter;

import uk.org.glendale.rpg.traveller.Config;
import uk.org.glendale.rpg.traveller.civilisation.trade.Commodity;
import uk.org.glendale.rpg.traveller.civilisation.trade.Facility;
import uk.org.glendale.rpg.traveller.civilisation.trade.Trade;
import uk.org.glendale.rpg.traveller.civilisation.trade.TradeGood;
import uk.org.glendale.rpg.traveller.database.GlossaryFactory;
import uk.org.glendale.rpg.traveller.database.ObjectFactory;
import uk.org.glendale.rpg.traveller.database.ObjectNotFoundException;
import uk.org.glendale.rpg.traveller.systems.Planet;

/**
 * A RESTful implementation of a web service that enables access to market
 * information about a planet.
 * 
 * A request should always be of the format /market/<planetId>. Individual
 * methods will define further requirements.
 * 
 * @author Samuel Penn
 * @created 2009/01/3
 *
 */
public class Market extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	/**
	 * Get information on a resource. By default, an XML description of the
	 * planet is returned, however the format can be modified by appending a
	 * filename extension to the id, e.g.:
	 *   /market/6801.html   An HTML page describing the planet.
	 *   /market/6801.txt    Return XML data as content type text.
	 *   
	 * For specific data, the txt format returns a simple text string.
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String		uri = request.getPathInfo();
		String		format = "xml";
		int			id = 0;
		
		// Very quick test to ensure that the URL format is correct.
		if (!uri.matches("/[0-9]+([/.].*)?")) {
			response.sendError(400, "Incorrect request format ["+uri+"]");
			return;
		}
		
		try {
			id = Integer.parseInt(uri.replaceAll("/([0-9]+)([/.].*)?", "$1"));
		} catch (NumberFormatException e) {
			// Should be impossible, since the regexp will have failed by this point.
			response.sendError(400, "Badly formatted planet id");
			return;
		}

		if (uri.matches(".*\\.[a-z]+")) {
			format = uri.replaceAll(".*\\.([a-z]+)", "$1");
			if (format.equals("txt") || format.equals("xml") || format.equals("html") || format.equals("jpg") || format.equals("rss")) {
				// Okay.
			} else {
				response.sendError(415, "Unrecognised format type ["+format+"]");
				return;
			}
		}

		try {
			ObjectFactory		factory = new ObjectFactory();
			if (id == 0) {
				outputCommodities(factory, request, response);
			} else {
				Planet				planet = factory.getPlanet(id);
	
				getFullPage(factory, planet, format, request, response);
			}
		} catch (ObjectNotFoundException e) {
			response.sendError(404, "Cannot find planet with id ["+id+"]");
			return;
		} catch (Throwable t) {
			t.printStackTrace();
			response.sendError(500, "Exception ("+t.getMessage()+")");
			return;
		}
	}
	
	/**
	 * Output a full page of data to the client. The format of tha data will
	 * depend on the format requested.
	 * 
	 * @param factory		ObjectFactory giving access to the universe.
	 * @param planet		Planet to get information on.
	 * @param format		Format of the response.
	 * @param request		Request object.
	 * @param response		Response object.
	 * @throws IOException
	 */
	private void getFullPage(ObjectFactory factory, Planet planet, String format, HttpServletRequest request, HttpServletResponse response) throws IOException {
		if (format.equals("html")) {
			// Output information as HTML.
			response.setContentType("text/html");
			response.getOutputStream().print(outputHTML(factory, planet, request.getContextPath()));
		} else if (format.equals("rss")) {
			response.setContentType("application/rss+xml");
			response.getOutputStream().print(outputRSS(factory, planet, request.getContextPath()));
		} else if (format.equals("txt")) {
			// Not currently supported.
		} else {
			// Not currently supported.
		}
	}

	/**
	 * Output information about the planet as HTML. Also adds HTML header
	 * and footer since the raw planet HTML is only an HTML snippet.
	 * 
	 * @param planet	Planet to output data on.
	 * @return			String containing the HTML page.
	 */
	private String outputHTML(ObjectFactory factory, Planet planet, String contextPath) {
		StringBuffer		buffer = new StringBuffer();
		String				stylesheet = contextPath+"/css/system.css";
		String				imageBase = contextPath+"/images/";
		Trade				trade = new Trade(factory, planet);
		DecimalFormat		f = new DecimalFormat();
		
		buffer.append("<html>\n<head>\n<title>"+planet.getName()+"</title>\n");
		buffer.append("<link rel=\"STYLESHEET\" type=\"text/css\" media=\"screen\" href=\""+stylesheet+"\" />\n");
		buffer.append("</head><body>\n");
		
		buffer.append("<div class=\"header\">");
		buffer.append("<h1>"+planet.getName()+"</h1>");
		buffer.append("</div>");
		
		buffer.append("<div class=\"planet\">");

		String[]	tradeCodes = planet.getTradeCodes();
		if (tradeCodes != null && tradeCodes.length > 0) {
			buffer.append("<p>");
			for (int c=0; c < tradeCodes.length; c++) {
				buffer.append("<img width=\"16\" height=\"16\" src=\""+Config.getBaseUrl()+"images/symbols/trade_"+tradeCodes[c].toLowerCase()+".png\"/>");
			}
			buffer.append("</p>\n");
		}

		// Physical data
		buffer.append("<p>\n");
		buffer.append("<b>Distance:</b> "+planet.getDistance()+" "+(planet.isMoon()?"km":"MKm")+"; <b>Type:</b> "+planet.getType()+"; ");
		NumberFormat format = NumberFormat.getInstance();
		format.setMaximumFractionDigits(2);
		if (!planet.getType().isBelt()) {
			buffer.append("<b>Radius:</b> "+planet.getRadius()+"km; ");
			buffer.append("<b>Gravity:</b> "+format.format(planet.getSurfaceGravity())+"ms<sup>-2</sup>; <b>Day:</b> "+planet.getDayAsString(false));
		}
		buffer.append(" <b>Year:</b> "+planet.getYearAsString(false));
		buffer.append("</p>\n");

		// Biosphere/Atmosphere
		if (!planet.getType().isBelt()) {
			buffer.append("<p>");
			buffer.append("<b>Atmosphere:</b> "+planet.getAtmosphereType()+"; <b>Pressure:</b> "+planet.getAtmospherePressure()+"; <b>Hydrographics:</b> "+planet.getHydrographics()+"%; ");
			buffer.append("<b>Temperature:</b> "+planet.getTemperature()+"; <b>Life:</b> "+planet.getLifeLevel());
			buffer.append("</p>\n");
		}
		
		if (planet.getPopulation() > 0) {
			// Civilisation
			format.setGroupingUsed(true);
			buffer.append("<p><b>Population:</b> "+format.format(planet.getPopulation())+"</p>\n");
			// Government
			buffer.append("<p><b>Government:</b> "+planet.getGovernment()+"</p>\n");
			// Law level
			buffer.append("<p><b>Law level:</b> "+planet.getLawLevel()+"</p>\n");
			// Tech level
			buffer.append("<p><b>Tech level:</b> "+planet.getTechLevel()+"</p>\n");
			// Starport
			buffer.append("<p><b>Starport:</b> "+planet.getStarport()+"</p>\n");
		}
				
		Hashtable<Integer,Facility>		allFacilities = factory.getFacilities();
		Hashtable<Integer,Long>			facilities = planet.getFacilities();
		
		buffer.append("<p>");
		for (int facilityId : facilities.keySet()) {
			Facility	facility = allFacilities.get(facilityId);
			if (facility != null) {
				String label = facility.getName()+" ("+facilities.get(facilityId)+"%)";
				buffer.append("<img src='"+imageBase+"facilities/"+facility.getImage()+".png' title='"+label+"'/> ");
			}
		}
		buffer.append("</p>");
		buffer.append("</div>");
		
		Hashtable<Integer,Commodity>		commodities = new Hashtable<Integer,Commodity>();

		buffer.append("<div class=\"resources\">");
		buffer.append("<h3>Planetary Resources</h3>");
		Hashtable<Integer,Integer>			resources = factory.getResources(planet.getId());
		
		buffer.append("<table width=\"100%\">");
		int		column = 0;
		for (int i : resources.keySet()) {
			Commodity		c = commodities.get(i);
			if (c == null) {
				c = factory.getCommodity(i);
				if (c == null) continue;
				commodities.put(i, c);
			}
			if (column == 0) {
				buffer.append("<tr>");
			} else if (column %6 == 0) {
				buffer.append("</tr><tr>");
			}
			column++;
			buffer.append("<td style=\"text-align: center\">");
			buffer.append("<img src=\""+imageBase+"trade/"+c.getImage()+".png\" title=\""+c.getName()+"\"/><br/>");
			buffer.append(c.getName()+" ("+resources.get(i)+")");
			buffer.append("</td>");
		}
		
		buffer.append("</tr></table></div>");
		
		
		buffer.append("<div class=\"marketdata\">");
		buffer.append("<h3>Market Information</h3>");
		Hashtable<Integer,TradeGood>	goods = factory.getCommoditiesByPlanet(planet.getId());
		
		buffer.append("<table class='good'>\n");
		buffer.append("<tr><th colspan='2'>Trade Good</th><th>Amount</th><th>Price</th><th>Consumed</th></tr>\n");
		for (int i : goods.keySet()) {
			TradeGood		good = goods.get(i);
			Commodity		c = factory.getCommodity(good.getCommodityId());
			
			if (c != null) {
				buffer.append("<tr>");
				try {
					buffer.append("<td><img src=\""+imageBase+"trade/"+c.getImage()+".png\" width='32' height='32' align=\"left\"/></td>");
					buffer.append("<td><b>"+c.getName()+"</b></td>");
					buffer.append("<td>"+f.format(good.getAmount())+"</td><td>"+f.format(good.getPrice())+"Cr</td>");
					buffer.append("<td>"+f.format(good.getConsumed())+"/wk</td>");
					//buffer.append("<b>Production: </b>"+f.format(trade.getProductionRate(c))+"/wk");
					buffer.append("</p>");
				} catch (Throwable e) {
					buffer.append("Oops");
				}
				buffer.append("</tr>\n");
			}
		}
		buffer.append("</table>\n");
		buffer.append("</div>");
		
		buffer.append("</body></html>\n");
		
		return buffer.toString();
	}
	
	private String outputRSS(ObjectFactory factory, Planet planet, String contextPath) {
		StringBuffer		buffer = new StringBuffer();
		String				imageBase = contextPath+"/images/";
		DecimalFormat		f = new DecimalFormat();

		buffer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		buffer.append("<rss version='2.0'>\n");
		buffer.append("<channel><title>"+planet.getName()+"</title>\n");
		buffer.append("<link>http://dev.glendale.org.uk/traveller/market/"+planet.getId()+".jsp</link>");
		buffer.append("<description>Current market data for "+planet.getName()+"</description>");

		Hashtable<Integer,TradeGood>	goods = factory.getCommoditiesByPlanet(planet.getId());
		
		for (int i : goods.keySet()) {
			TradeGood		good = goods.get(i);
			Commodity		c = factory.getCommodity(good.getCommodityId());
			
			if (c != null) {
				buffer.append("<item>");
				try {
					buffer.append("<title>"+c.getName()+"</title>");
					buffer.append("<link>"+imageBase+"trade/"+c.getImage()+".png</link>");
					buffer.append("<description>");
					buffer.append("&lt;img src='http://dev.glendale.org.uk/traveller/images/trade/"+c.getImage()+".png' align='left' width='32' height='32'/&gt;");
					buffer.append(f.format(good.getAmount())+"dt @ "+f.format(good.getPrice())+"Cr");
					buffer.append("&lt;br/&gt;");
					if (good.getConsumed() > 0) {
						buffer.append(f.format(good.getConsumed())+"dt consumed last week");
					}
					buffer.append("</description>");
				} catch (Throwable e) {
				}
				buffer.append("</item>\n");
			}
		}

		buffer.append("</channel></rss>\n");
		return buffer.toString();
	}

	private void outputCommodities(ObjectFactory factory, HttpServletRequest request, HttpServletResponse response) throws IOException {
		response.setContentType("text/html");
		StringBuffer		buffer = new StringBuffer();
		String				stylesheet = request.getContextPath()+"/css/system.css";
		String				imageBase = request.getContextPath()+"/images/";
		DecimalFormat		f = new DecimalFormat();
		
		buffer.append("<html>\n<head>\n<title>Commodities</title>\n");
		buffer.append("<link rel=\"STYLESHEET\" type=\"text/css\" media=\"screen\" href=\""+stylesheet+"\" />\n");
		buffer.append("</head><body>\n");
		
		buffer.append("<div class=\"header\">");
		buffer.append("<h1>Commodities</h1>");
		buffer.append("</div>");
		
		buffer.append("<div class=\"commodities\"><ul>");
		Hashtable<Integer,Commodity>		list = factory.getAllCommodities();
		for (Commodity c : list.values()) {
			// Output top level commodities.
			if (c.getParentId() == 0) {
				outputCommodity(buffer, list, c, imageBase);
			}
		}
		buffer.append("</ul></div>");
		
		buffer.append("</body></html>\n");

		response.getOutputStream().print(buffer.toString());
	}
	
	private void outputCommodity(StringBuffer buffer, Hashtable<Integer,Commodity> list, Commodity parent, String imageBase) {
		buffer.append("<li>");
		buffer.append("<img src='"+imageBase+"trade/"+parent.getImage()+".png' title='"+parent.getName()+"'/>");
		buffer.append(parent.getName()+" ("+parent.getSource()+")");
		buffer.append("</li>");
		buffer.append("<ul>");
		for (Commodity c : list.values()) {
			// Output top level commodities.
			if (c.getParentId() == parent.getId()) {
				outputCommodity(buffer, list, c, imageBase);
			}
		}
		buffer.append("</ul>");		
	}	
}
