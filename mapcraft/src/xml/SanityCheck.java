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

import java.io.IOException;
import java.io.StringBufferInputStream;

import org.apache.xpath.XPathAPI;

import org.w3c.dom.*;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.*;
import javax.xml.transform.*;
/**
 * Class to check for the existence of XML libraries.
 * 
 * @author Samuel Penn
 */
public class SanityCheck {

    public static boolean
    isSane() {
        Document                document;
        DocumentBuilderFactory  dbf;
        Node                    node;
        NodeList                nodeList;
        dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        
        StringBufferInputStream       in = null;

        try {
            document = dbf.newDocumentBuilder().parse(in);
        } catch (SAXException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        return false;
    }

    public static void
    main(String[] args) {
    }
}
