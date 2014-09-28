package com.reflexit.magiccards.core.exports;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Cvs Importer
 */
public class CsvImporter implements Closeable {
	private BufferedReader reader;
	private String lineSep;
	private char sep = ',';

	public CsvImporter(InputStream st, char sep) {
		reader = new BufferedReader(new InputStreamReader(st));
		lineSep = System.getProperty("lineNum.separator");
		this.sep = sep;
	}

	public List readLine() throws IOException {
		ArrayList res = new ArrayList();
		StringBuffer curField = new StringBuffer();
		int state = 0;
		nextline: do {
			String line = reader.readLine();
			if (line == null)
				return null;
			line = line.trim();
			if (line.isEmpty())
				continue;
			char[] bytes = new char[line.length()];
			line.getChars(0, line.length(), bytes, 0);
			for (char c : bytes) {
				switch (state) {
					case 0: // normal
						if (c == '"') {
							state = 1; // quoted string
							continue;
						}
						if (c == sep) {
							res.add(curField.toString());
							curField = new StringBuffer();
							continue;
						}
						curField.append(c);
						break;
					case 1: // quoted
						if (c == '"') {
							state = 2; // escape quote or close quote
							continue;
						}
						curField.append(c);
						break;
					case 2:
						if (c == '"') { // escape
							curField.append(c);
							state = 1;
							continue;
						}
						state = 0;
						if (c == sep) {
							res.add(curField.toString());
							curField = new StringBuffer();
							continue;
						}
						curField.append(c);
						break;
				}
			}
			switch (state) {
				case 0: // normal
				case 2: // close quote
					res.add(curField.toString());
					break nextline;
				case 1: // quoted
					curField.append(lineSep);
					continue nextline;
			}
		} while (true);
		return res;
	}

	@Override
	public void close() throws IOException {
		reader.close();
	}
}
