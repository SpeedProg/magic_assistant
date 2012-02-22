package com.reflexit.magiccards.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;

import com.reflexit.magiccards.core.model.ICardHandler;
import com.reflexit.magiccards.core.model.MagicCardFilter;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;
import com.reflexit.magiccards.core.monitor.ICoreProgressMonitor;
import com.reflexit.magiccards.db.DbActivator;

public class FileUtils {
	public static final String UTF8 = "UTF-8";
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
		StringBuffer fileData = new StringBuffer(1024 * 4);
		char[] buf = new char[1024 * 4];
		int numRead = 0;
		while ((numRead = reader.read(buf)) != -1) {
			fileData.append(buf, 0, numRead);
		}
		return fileData.toString();
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
		ICardHandler cardHandler = DataManager.getCardHandler();
		cardHandler.loadInitialIfNot(ICoreProgressMonitor.NONE);
		IFilteredCardStore fstore = cardHandler.getMagicDBFilteredStore();
		fstore.update(new MagicCardFilter());
		System.err.println("Loaded " + fstore.getSize() + " cards");
	}
}
