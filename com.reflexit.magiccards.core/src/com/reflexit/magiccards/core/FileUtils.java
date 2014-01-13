package com.reflexit.magiccards.core;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;

import com.reflexit.magiccards.core.model.ICardHandler;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;
import com.reflexit.magiccards.db.DbActivator;

public class FileUtils {
	public static final String UTF8 = "UTF-8";
	public static Charset CHARSET_UTF_8 = Charset.forName("utf-8");
	static {
		File stateLocationFile = getStateLocationFile();
		if (!stateLocationFile.exists() && !stateLocationFile.mkdirs()) {
			System.err.println("Cannot create " + stateLocationFile);
		}
	}

	public static void copyFile(File in, File out) throws IOException {
		FileInputStream ins = new FileInputStream(in);
		try {
			FileChannel inChannel = ins.getChannel();
			FileOutputStream outs = new FileOutputStream(out);
			try {
				FileChannel outChannel = outs.getChannel();
				try {
					inChannel.transferTo(0, inChannel.size(), outChannel);
				} catch (IOException e) {
					throw e;
				} finally {
					if (inChannel != null)
						inChannel.close();
					if (outChannel != null)
						outChannel.close();
				}
			} finally {
				outs.close();
			}
		} finally {
			ins.close();
		}
	}

	public static BufferedReader openFileReader(File file) throws FileNotFoundException {
		BufferedReader st = new BufferedReader(new InputStreamReader(new FileInputStream(file), FileUtils.CHARSET_UTF_8));
		return st;
	}

	public static BufferedReader openStringReader(String str) {
		BufferedReader st = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(str.getBytes()), FileUtils.CHARSET_UTF_8));
		return st;
	}

	/**
	 * @param in
	 * @param out
	 * @throws IOException
	 */
	public static void saveStream(InputStream in, File out) throws IOException {
		out.getAbsoluteFile().getParentFile().mkdirs();
		FileOutputStream fos = new FileOutputStream(out);
		try {
			copyStream(in, fos);
		} finally {
			fos.close();
		}
	}

	public static void copyStream(InputStream in, OutputStream out) throws IOException {
		int count;
		byte[] buffer = new byte[1024 * 4];
		while ((count = in.read(buffer)) > 0)
			out.write(buffer, 0, count);
	}

	public static String readFileAsString(BufferedReader reader) throws IOException {
		int bufSize = 1024 * 256;
		StringBuilder fileData = new StringBuilder(1024 * 4);
		char[] buf = new char[bufSize];
		int numRead = 0;
		while ((numRead = reader.read(buf)) != -1) {
			fileData.append(buf, 0, numRead);
		}
		return fileData.toString();
	}

	public static int readFileAsBytes(File file, byte[] buf) throws IOException {
		InputStream st = new FileInputStream(file);
		int numRead = 0;
		int offset = 0;
		int len = buf.length;
		while ((numRead = st.read(buf, offset, len)) != -1) {
			// just fill the buffer
			offset += numRead;
			len -= numRead;
			if (len >= 0)
				break;
		}
		st.close();
		return offset;
	}

	public static File getWorkspaceFile() {
		String str = System.getProperty("osgi.instance.area");
		if (str != null) {
			return new File(str.replaceFirst("^file:", ""));
		} else {
			return new File(System.getProperty("user.home"), "MagicAssistant");
		}
	}

	public static File getStateLocationFile() {
		String inEclipse = System.getProperty("eclipse.home.location");
		if (inEclipse != null) {
			// System.err.println("Eclipse home: " + inEclipse);
			return Activator.getDefault().getStateLocation().toFile();
		} else {
			return new File(getWorkspaceFile() + "/.metadata/.plugins/" + DataManager.ID);
		}
	}

	public static InputStream loadDbResource(String name) throws IOException {
		if (System.getProperty("eclipse.home.location") != null) {
			return DbActivator.loadResource(name);
		} else {
			return FileUtils.class.getClassLoader().getResourceAsStream(name);
		}
	}

	public static void main(String[] args) {
		MagicLogger.log("aaa");
		ICardHandler cardHandler = DataManager.getCardHandler();
		IFilteredCardStore fstore = cardHandler.getMagicDBFilteredStore();
		fstore.update();
		System.err.println("Loaded " + fstore.getSize() + " cards");
	}

	public static boolean deleteTree(File rootDir) {
		File[] listFiles = rootDir.listFiles();
		if (listFiles != null) {
			for (int i = 0; i < listFiles.length; i++) {
				File file = listFiles[i];
				deleteTree(file);
			}
		}
		return rootDir.delete();
	}

	public static void saveString(String textResult, File file) throws IOException {
		FileOutputStream fos = new FileOutputStream(file);
		try {
			fos.write(textResult.getBytes(CHARSET_UTF_8));
		} finally {
			fos.close();
		}
	}
}
