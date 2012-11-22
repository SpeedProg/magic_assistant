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
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;

import com.reflexit.magiccards.core.model.ICardHandler;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;
import com.reflexit.magiccards.core.monitor.ICoreProgressMonitor;
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
		FileChannel inChannel = new FileInputStream(in).getChannel();
		FileChannel outChannel = new FileOutputStream(out).getChannel();
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
			byte[] buf = new byte[1024 * 4];
			int i = 0;
			while ((i = in.read(buf)) != -1) {
				fos.write(buf, 0, i);
			}
		} catch (IOException e) {
			throw e;
		} finally {
			fos.close();
		}
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
		if (System.getProperty("eclipse.home.location") != null) {
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

	public static void main(String[] args) throws IOException {
		MagicLogger.log("aaa");
		ICardHandler cardHandler = DataManager.getCardHandler();
		cardHandler.loadInitialIfNot(ICoreProgressMonitor.NONE);
		IFilteredCardStore fstore = cardHandler.getMagicDBFilteredStore();
		fstore.update();
		System.err.println("Loaded " + fstore.getSize() + " cards");
	}

	public static void deleteTree(File rootDir) {
		File[] listFiles = rootDir.listFiles();
		if (listFiles != null) {
			for (int i = 0; i < listFiles.length; i++) {
				File file = listFiles[i];
				deleteTree(file);
			}
		}
		rootDir.delete();
	}
}
