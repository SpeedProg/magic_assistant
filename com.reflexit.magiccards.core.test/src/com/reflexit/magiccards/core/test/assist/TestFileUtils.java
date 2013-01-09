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
package com.reflexit.magiccards.core.test.assist;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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
}
