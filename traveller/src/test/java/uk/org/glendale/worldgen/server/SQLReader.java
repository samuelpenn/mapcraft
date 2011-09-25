/*
 * Copyright (C) 2011 Samuel Penn, sam@glendale.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2.
 * See the file COPYING.
 */
package uk.org.glendale.worldgen.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Reads a file containing SQL statements, and executes them one at a time.
 * Tries to cope with SQL statements which are split across multiple lines.
 * Stops when the end of file is reached, or the line "-- EXIT" is found.
 * 
 * @author Samuel Penn
 */
public final class SQLReader {
	/** Reader to read the file contents. */
	private BufferedReader reader;
	/** Database connection. */
	private Connection cx;

	/**
	 * Define an SQLReader for the specified file.
	 * 
	 * @param file
	 *            File containing SQL to be read.
	 * @param cx
	 *            Connection to the database.
	 * @throws FileNotFoundException
	 */
	public SQLReader(final File file, final Connection cx)
			throws FileNotFoundException {

		this.cx = cx;
		reader = new BufferedReader(new FileReader(file));
	}

	/**
	 * Execute the statements in the file one at a time. Each statement must end
	 * with a ';'. Comments are skipped.
	 * 
	 * @throws SQLException
	 * @throws IOException
	 */
	public void execute() throws SQLException, IOException {
		Statement stmt = cx.createStatement();
		String line = null;
		String sql = "";

		while ((line = reader.readLine()) != null) {
			if (line.trim().equals("-- EXIT")) {
				// Stop processing at this point.
				break;
			}
			if (line.length() == 0 || line.startsWith("--")) {
				// Ignore empty lines and comments.
				continue;
			}

			sql += " " + line;

			if (sql.trim().endsWith(";")) {
				try {
					stmt.execute(sql);
				} catch (SQLException t) {
					// Make sure we know which line we barfed on.
					System.out.println(sql);
					throw t;
				}
				sql = "";
			}
		}
	}

	public static void setupTestDatabase() {
		try {
			AppManager.setConfig("test");
		} catch (Throwable t) {
			t.printStackTrace();
		}
		Connection cx = null;//AppManager.getInstance().getDatabaseConnection();

		File file = new File("docs/database.sql");

		try {
			SQLReader reader = new SQLReader(file, cx);

			reader.execute();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
