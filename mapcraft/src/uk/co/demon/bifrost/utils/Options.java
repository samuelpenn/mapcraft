/*
 * Copyright (C) 2002 Samuel Penn, sam@glendale.org.uk
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2,
 * or (at your option) any later version. See the file COPYING.
 */
package net.sourceforge.mapcraft.uk.co.demon.bifrost.utils;


/**
 * Provides an API for handling command line options passed
 * to a class. Use as follows:
 *
 * public static void main(String args[]) {
 *     Options options = new Options(args);
 *     if (options.isOption("-version")) {
 *         System.out.println("Version 0.1");
 *     }
 *     if (options.isOption("-echo")) {
 *         System.out.println(options.getString("-echo"));
 *     }
 * }
 */
public class Options {
    String[]	args = null;
    int			count = 0;

    public
    Options(String args[]) {
        this.args = args;
        this.count = args.length;
    }

    public int
    getIndex(String option) {
        int		i = 0;

        for (i=0; i < count; i++) {
            if (args[i].compareTo(option)==0) {
                return i;
            }
        }

        return -1;
    }


    public boolean
    isOption(String option) {
        if (getIndex(option) >= 0) {
            return true;
        }

        return false;
    }


    public String
    getString(String option) {
        int		idx = getIndex(option);

        if (idx < 0) {
            return null;
        }

        if (idx == count-1) {
            return null;
        }

        return args[idx+1];
    }

    public int
    getInt(String option) {
        int		idx = getIndex(option);
        int		value = 0;

        if (idx < 0) {
            return 0;
        }

        if (idx == count-1) {
            return 0;
        }

        try {
            value = Integer.parseInt(args[idx+1]);
        } catch (NumberFormatException e) {
            value = 0;
        }

        return value;
    }

}
