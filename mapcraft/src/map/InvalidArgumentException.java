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
package uk.co.demon.bifrost.rpg.mapcraft.map;


public class InvalidArgumentException extends MapException {
    public
    InvalidArgumentException() {
        super("Method arguments are invalid");
    }

    public
    InvalidArgumentException(String msg) {
        super(msg);
    }
}
