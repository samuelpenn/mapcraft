/*
 * Copyright (C) 2004 Samuel Penn, sam@bifrost.demon.co.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation version 2.
 * See the file COPYING.
 *
 * $Revision$
 * $Date$
 */
package uk.co.demon.bifrost.rpg.mapcraft.xml;

import java.io.*;

import org.apache.xpath.XPathAPI;
import org.w3c.dom.*;
import org.xml.sax.InputSource;

import javax.xml.parsers.*;

/**
 * Class to check for the existence of XML libraries.
 * 
 * @author Samuel Penn
 */
public class SanityCheck {

    private String
    getDocument() {
		StringBuffer    buffer = new StringBuffer();

		buffer.append("<?xml version=\"1.0\"?>");
		buffer.append("<root>");
		buffer.append("<text title=\"greeting\">");
		buffer.append("Hello world");
		buffer.append("</text>");
		buffer.append("</root>");

		return buffer.toString();
    }
    
    public boolean
    isSane() {
        boolean     result = true;
        
        try {
            InputSource             in;
            DocumentBuilderFactory  dbf;
            Node                    node;
            NodeList                nodeList;
            String                  content = getDocument();
            Document                document;

            in = new InputSource(new StringReader(content));
            dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);

            document = dbf.newDocumentBuilder().parse(in);
            
            node = XPathAPI.selectSingleNode(document, "/root/text");
            String  value = node.getFirstChild().getNodeValue();
            if (value == null || !value.equals("Hello world")) {
                throw new Exception("Failed to find string");
            }

            node = XPathAPI.selectSingleNode(document, "/root/text");
            NamedNodeMap    attrs = node.getAttributes();
            Node            a = attrs.getNamedItem("title");

            if (a == null) {
                throw new Exception("Failed to find attribute");
            }
            
            value = a.getFirstChild().getNodeValue();
            if (value == null || !value.equals("greeting")) {
                throw new Exception("Failed to get attribute");
            }
        } catch (Exception e) {
            result = false;
        }
        
        return result;
    }

    public static void
    main(String[] args) {
       SanityCheck  sc = new SanityCheck();
       System.out.println(sc.isSane());
    }
}
