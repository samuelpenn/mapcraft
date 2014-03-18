/*
 * Copyright (C) 2007 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation version 2.
 * See the file COPYING.
 *
 * $Revision: 1.4 $
 * $Date: 2007/12/09 17:45:17 $
 */
package uk.org.glendale.rpg.traveller.servlets;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

class Handle {
    /**
     * Get the integer value of a Http parameter. If it doesn't exist, or isn't
     * a number, then assume that the value is zero.
     * 
     * @param request           Servlet request data.
     * @param parameter         Parameter to read.
     * @return                  The value of the parameter, or zero if unavailable.
     */
    int getInt(String parameter) {
    	int i = 0;
        
        try {
        	String     value = request.getParameter(parameter);
            if (value != null) {
                i = Integer.parseInt(value);
            }
        } catch (NumberFormatException e) {
        	// Just set to zero if this isn't a number.
            i = 0;
        }
            
        return i;
    }

	HttpServletRequest     request = null;
    HttpServletResponse    response = null;
    PrintWriter     	   out = null;
    StringBuffer           content = new StringBuffer();
    
    String				   name = null;
    String				   requestType = null;
    String				   format = null;
    String				   action = null;
    int					   id = 0;
    int					   x = 0;
    int					   y = 0;
    int					   detailX = 0;
    int					   detailY = 0;
    
    Handle(HttpServletRequest request, HttpServletResponse response) throws IOException {
		this.request = request;
        this.response = response;
        
        out = response.getWriter();
        
        action = request.getParameter("action");
        requestType = request.getParameter("type");
        name = request.getParameter("name");
        format = request.getParameter("format");
        id = getInt("id");
        x = getInt("x");
        y = getInt("y");
        detailX = getInt("detailX");
        detailY = getInt("detailY");
        
        if (format == null) {
        	format = "text";
        }
    }
    
    String getRequestType() {
    	return requestType;
    }
    
    String getFormat() {
    	return format;
    }
    
    String getName() {
    	return name;
    }
    
    int getId() {
    	return id;
    }
    
    int getX() {
    	return x;
    }
    
    int getY() {
    	return y;
    }
    
    int getDetailX() {
    	return detailX;
    }
    
    int getDetailY() {
    	return detailY;
    }
    
    
    String getAction() {
    	return action;
    }
    
    /**
     * Is the requested format XML?
     * @return
     */
    boolean isXML() {
    	if (format != null && format.equalsIgnoreCase("xml")) {
    		return true;
    	}
    	
    	return false;
    }
    
    boolean isHTML() {
    	if (format != null && format.equalsIgnoreCase("html")) {
    		return true;
    	}
    	
    	return false;
    }
    
    PrintWriter getWriter() {
    	return out;
    }
}