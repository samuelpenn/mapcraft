/*
 * Copyright (C) 2002 Samuel Penn, sam@bifrost.demon.co.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2,
 * or (at your option) any later version. See the file COPYING.
 *
 * $Revision$
 * $Date$
 */
package uk.co.demon.bifrost.rpg.mapcraft.xml;

/**
* Exception class, raised when an error occurs during
* processing of an XML document. Used as a generic
* exception, to make things easier to catch.
*/
public class XMLException extends Exception {
    public
    XMLException() {
        super();
    }

    public
    XMLException(String msg) {
        super(msg);
    }
}
