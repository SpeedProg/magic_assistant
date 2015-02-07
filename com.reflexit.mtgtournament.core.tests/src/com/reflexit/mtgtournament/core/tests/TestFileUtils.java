/*******************************************************************************
 * $QNXLicenseC:
 * Copyright 2008, QNX Software Systems. All Rights Reserved.
 *
 * You must obtain a written license from and pay applicable license fees to QNX
 * Software Systems before you may reproduce, modify or distribute this software,
 * or any work that includes all or part of this software.   Free development
 * licenses are available for evaluation and non-commercial purposes.  For more
 * information visit http://licensing.qnx.com or email licensing@qnx.com.
 *
 * This file may contain contributions from others.  Please review this entire
 * file for other proprietary rights or license notices, as well as the QNX
 * Development Suite License Guide at http://licensing.qnx.com/license-guide/
 * for other information.
 * $
 *******************************************************************************/
/*
 * Created by: Elena Laskavaia
 * Created on: 2011-01-27
 * Last modified by: $Author: elaskavaia $
 */
package com.reflexit.mtgtournament.core.tests;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * static utils
 */
public class TestFileUtils {
	public static boolean deleteOnExit = true;

	public static void readWriteStream(InputStream readStream, OutputStream writeStream) throws IOException {
		byte[] buffer = new byte[1024 * 4];
		int bytesRead = readStream.read(buffer);
		// write the required bytes
		while (bytesRead > 0) {
			writeStream.write(buffer, 0, bytesRead);
			bytesRead = readStream.read(buffer);
		}
	}

	public static File saveResourceToDir(String path, File dir) throws IOException {
		return saveResource(path, new File(dir, path));
	}

	public static File saveResourceToDirFlat(String path, File dir) throws IOException {
		return saveResource(path, new File(dir, new File(path).getName()));
	}

	public static String saveResourceToString(String path) throws IOException {
		InputStream resourceAsStream = TestFileUtils.class.getClassLoader().getResourceAsStream(path);
		if (resourceAsStream == null)
			throw new FileNotFoundException("Resource not found " + path);
		ByteArrayOutputStream writeStream = new ByteArrayOutputStream();
		readWriteStream(resourceAsStream, writeStream);
		resourceAsStream.close();
		return writeStream.toString();
	}

	public static File saveStringToFile(String string, File saveTo) throws IOException {
		InputStream resourceAsStream = new ByteArrayInputStream(string.getBytes());
		saveTo.getParentFile().mkdirs();
		FileOutputStream out = new FileOutputStream(saveTo);
		readWriteStream(resourceAsStream, out);
		out.close();
		resourceAsStream.close();
		if (deleteOnExit)
			saveTo.deleteOnExit();
		return saveTo;
	}

	public static String loadStringFromFile(File loadFrom) throws IOException {
		InputStream resourceAsStream = new FileInputStream(loadFrom);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		readWriteStream(resourceAsStream, out);
		out.close();
		resourceAsStream.close();
		return out.toString();
	}

	public static File saveResource(String path, File saveTo) throws IOException {
		InputStream resourceAsStream = TestFileUtils.class.getClassLoader().getResourceAsStream(path);
		if (resourceAsStream == null)
			throw new FileNotFoundException("Resource not found " + path);
		saveTo.getParentFile().mkdirs();
		FileOutputStream out = new FileOutputStream(saveTo);
		readWriteStream(resourceAsStream, out);
		out.close();
		resourceAsStream.close();
		if (deleteOnExit)
			saveTo.deleteOnExit();
		return saveTo;
	}

	/**
	 * Returns an array of StringBuilder objects for each comment section found preceding the named test in
	 * the source code.
	 *
	 * @param bundle
	 *            the bundle containing the source, if {@code null} can try to load using classpath (source
	 *            folder has to be in the
	 *            classpath for this to work)
	 * @param srcRoot
	 *            the directory inside the bundle containing the packages
	 * @param clazz
	 *            the name of the class containing the test
	 * @param testName
	 *            the name of the test
	 * @param numSections
	 *            the number of comment sections preceding the named test to return. Pass zero to get all
	 *            available sections.
	 * @return an array of StringBuilder objects for each comment section found preceding the named test in
	 *         the source code.
	 * @throws IOException
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 */
	public static StringBuilder[] getContentsForTest(String srcRoot, Class clazz, final String testName,
			int numSections)
			throws IOException, NoSuchMethodException, SecurityException {
		// Walk up the class inheritance chain until we find the test method.
		while (clazz.getMethod(testName).getDeclaringClass() != clazz) {
			clazz = clazz.getSuperclass();
		}
		while (true) {
			// Find and open the .java file for the class clazz.
			String fqn = clazz.getName().replace('.', '/');
			fqn = fqn.indexOf("$") == -1 ? fqn : fqn.substring(0, fqn.indexOf("$"));
			String classFile = fqn + ".java";
			Class superclass = clazz.getSuperclass();
			InputStream in = clazz.getResourceAsStream('/' + classFile);
			if (in == null) {
				throw new IOException(classFile + " is not found");
			}
			StringBuilder[] comments = extractComments(testName, numSections, in);
			if (comments != null)
				return comments;
			if (superclass == null || !superclass.getPackage().equals(clazz.getPackage())) {
				throw new IOException("Test data not found for " + clazz.getName() + "." + testName);
			}
			clazz = superclass;
		}
	}

	public static StringBuilder[] extractComments(final String testName, int numSections, InputStream in)
			throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		try {
			// Read the java file collecting comments until we encounter the
			// test method.
			List<StringBuilder> contents = new ArrayList<StringBuilder>();
			StringBuilder content = new StringBuilder();
			boolean inComment = false;
			for (String line1 = br.readLine(); line1 != null; line1 = br.readLine()) {
				// Replace leading whitespace, preserve trailing
				String line = line1.replaceFirst("^\\s*", "");
				if (inComment && line.contains("*/")) {
					inComment = false;
				} else if (inComment) {
					content.append(line1.replaceFirst("\\t+", "") + "\n");
				} else if (line.startsWith("//")) {
					content.append(line.substring(2) + "\n");
				} else if (line.startsWith("@")) {
					// ignore annotations
				} else if (line.trim().isEmpty()) {
					// ignore empty lines
				} else if (line.contains("/*-")) {
					inComment = true;
				} else {
					if (content.length() > 0) {
						// add new section
						contents.add(content);
						if (numSections > 0 && contents.size() == numSections + 1)
							contents.remove(0);
						content = new StringBuilder();
					}
					if (line.matches("^[^=]*\\b" + testName + "\\b.*")) {
						return contents.toArray(new StringBuilder[contents.size()]);
					}
					// was not our function, reset sections
					contents.clear();
				}
			}
		} finally {
			br.close();
		}
		return null;
	}
}
