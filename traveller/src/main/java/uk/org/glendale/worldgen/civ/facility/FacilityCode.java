/*
 * Copyright (C) 2011 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2.
 * See the file COPYING.
 */
package uk.org.glendale.worldgen.civ.facility;

/**
 * Used to define where a facility may be found. <br/>
 * Tx: Tech level for the facility. <br/>
 * Px: Population level for the facility (log 10). <br/>
 * H0: Hostility of world, H0 = Earth, H6 = really nasty. <br/>
 * 
 * @author Samuel Penn
 */
public enum FacilityCode {
	// @formatter:off
	T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, 
	T13, T14, T15, T16, T17, T18, T19, T20, T21, T22, T23,
	P0, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12,
	H0, H1, H2, H3, H4, H5, H6
	// @formatter:on
}
