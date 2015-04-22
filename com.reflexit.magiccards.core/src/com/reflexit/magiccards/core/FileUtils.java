package com.reflexit.magiccards.core;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
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
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import com.reflexit.magiccards.db.DbActivator;

public class FileUtils {
	public static final int DEFAULT_BUFFER_SIZE = 30 * 1024;
	public static final String MAGICCARDS = "magiccards";
	public static final String BACKUP = ".backup";
	public static final String UTF8 = "UTF-8";
	public static Charset CHARSET_UTF_8 = Charset.forName("utf-8");
	//
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

	/**
	 * Create a ZIP file including specified source and its content in case of a
	 * directory. Files in {@code exclude} will not be include in the resulting
	 * ZIP file. If a directory is excluded all it children will excluded also.
	 * Empty directories will be add.
	 * 
	 * @param src
	 *            Source can be a directory or a single file. Directories
	 *            includes all it content.
	 * @param dest
	 *            The resulting ZIP file.
	 * @param exclude
	 *            Files or directories that should not be added to ZIP file. Can
	 *            be null, if nothing to exclude.
	 * @throws IOException
	 */
	public static void zip(File src, File dest, List<File> exclude) throws IOException {
		final int buffer = 2048;

		// make sure destination ZIP file will not be added to itself.
		if (exclude == null) {
			exclude = new ArrayList<File>();
		}
		exclude.add(dest);

		List<File> zipFiles = new ArrayList<File>();
		if (src.isDirectory()) {
			zipFiles = listFiles(src);
		} else {
			zipFiles.add(src);
		}

		if (!zipFiles.isEmpty()) {
			FileOutputStream destZip = new FileOutputStream(dest);
			ZipOutputStream zipOut = new ZipOutputStream(new BufferedOutputStream(destZip));
			BufferedInputStream fileIn = null;
			byte data[] = new byte[buffer];
			try {
				for (File f : zipFiles) {

					// check excludes
					if (exclude != null && !exclude.isEmpty()) {
						boolean skip = false;
						for (File ex : exclude) {
							if (f.equals(ex)) {
								skip = true;
								break;
							}
							if (ex.isDirectory() && f.getAbsolutePath().startsWith(ex.getAbsolutePath())) {
								skip = true;
								break;
							}
						}
						if (skip) {
							continue;
						}
					}

					if (f.isDirectory()) {
						// add empty directories
						if(src.equals(f)){
							continue;
						}
						String entry = f.getAbsolutePath().substring(src.getAbsolutePath().length() + 1);
						ZipEntry zEntry = new ZipEntry(entry + "/");
						zipOut.putNextEntry(zEntry);
						zipOut.closeEntry();
					} else if (f.isFile()) {
						// add all file
						String entry = null;
						if (src.isFile()) {
							entry = src.getName();
						} else {
							entry = f.getAbsolutePath().substring(src.getAbsolutePath().length() + 1);
						}
						FileInputStream fis = new FileInputStream(f);
						fileIn = new BufferedInputStream(fis, buffer);
						ZipEntry zEntry = new ZipEntry(entry);
						zipOut.putNextEntry(zEntry);
						int count;
						while ((count = fileIn.read(data, 0, buffer)) != -1) {
							zipOut.write(data, 0, count);
						}
						fileIn.close();
					}
				}
				zipOut.close();
			} finally {
				if (fileIn != null) {
					fileIn.close();
				}
				if (zipOut != null) {
					zipOut.close();
				}
			}
		}
	}

	public static void unzip(File src, File dest) throws IOException {
		final int BUFFER = 2048;
		if(dest.isFile()){
			throw new IOException("Not a valid target to extract to "+dest.getAbsolutePath());
		}
		if(dest.isDirectory() && !dest.exists()){
			if(!dest.mkdirs()){
				throw new IOException("Could not create directory "+dest.getAbsolutePath());
			}
		}
		BufferedOutputStream destOut = null;
		try {
			BufferedInputStream is = null;
			ZipEntry entry;
			ZipFile zipfile = new ZipFile(src);
			Enumeration<?> e = zipfile.entries();
			while (e.hasMoreElements()) {
				entry = (ZipEntry) e.nextElement();
				File destFile = new File(dest.getAbsolutePath()+File.separatorChar+entry.getName());
				if(!entry.isDirectory()){
					if(!destFile.getParentFile().exists()){
						destFile.getParentFile().mkdirs();
					}
					is = new BufferedInputStream(zipfile.getInputStream(entry));
					int count;
					byte data[] = new byte[BUFFER];
					FileOutputStream fos = new FileOutputStream(destFile);
					destOut = new BufferedOutputStream(fos, BUFFER);
					while ((count = is.read(data, 0, BUFFER)) != -1) {
						destOut.write(data, 0, count);
					}
					destOut.flush();
					destOut.close();
					is.close();
				}else{
					destFile.mkdirs();
				}
			}
			zipfile.close();
		}finally{
			if (destOut != null) {
				destOut.close();
			}
		}
	}

	/**
	 * Get a list of all files included in specified file if it is a directory,
	 * if it is a file only that file will be returned.
	 * 
	 * @param file
	 * @return If file is a directory, file and all childs will be returned, if
	 *         it is a file, only that file is returned.
	 */
	private static List<File> listFiles(File file) {
		List<File> result = new ArrayList<File>();
		File[] files = file.listFiles();
		if (files != null && files.length > 0) {
			for (File f : files) {
				if (f.isDirectory()) {
					result.addAll(listFiles(f));
				} else {
					result.add(f);
				}
			}
		}
		result.add(file);
		return result;
	}

	public static void copyTree(File src, File dest) throws IOException {
		if (dest.getCanonicalPath().startsWith(src.getCanonicalPath()))
			throw new IOException("Cannot backup inside of workspace");
		// if directory not exists, create it
		if (src.isDirectory()) {
			if (!dest.exists()) {
				dest.mkdirs();
			}
			// list all the directory contents
			String files[] = src.list();
			if (files != null) {
				for (String file : files) {
					// construct the src and dest file structure
					File srcFile = new File(src, file);
					File destFile = new File(dest, file);
					// recursive copy
					copyTree(srcFile, destFile);
				}
			}
		} else {
			// if file, then copy it
			// Use bytes stream to support all file types
			copyFile(src, dest);
		}
	}

	public static BufferedReader openBuferedReader(InputStream inst) {
		BufferedReader st = new BufferedReader(new InputStreamReader(inst,
				FileUtils.CHARSET_UTF_8), DEFAULT_BUFFER_SIZE);
		return st;
	}

	public static BufferedReader openBuferedReader(File file) throws FileNotFoundException {
		BufferedReader st = new BufferedReader(new InputStreamReader(new FileInputStream(file),
				FileUtils.CHARSET_UTF_8), DEFAULT_BUFFER_SIZE);
		return st;
	}

	public static BufferedReader openBufferedReader(String str) {
		BufferedReader st = new BufferedReader(new InputStreamReader(
				new ByteArrayInputStream(str.getBytes()), FileUtils.CHARSET_UTF_8), DEFAULT_BUFFER_SIZE);
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
		byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
		while ((count = in.read(buffer)) > 0)
			out.write(buffer, 0, count);
	}

	public static String readFileAsString(File file) throws IOException {
		BufferedReader st = openBuferedReader(file);
		try {
			return readFileAsString(st);
		} finally {
			try {
				st.close();
			} catch (Exception e) {
				// ignore
			}
		}
	}

	public static String readFileAsString(BufferedReader reader) throws IOException {
		StringBuilder fileData = new StringBuilder(DEFAULT_BUFFER_SIZE);
		char[] buf = new char[DEFAULT_BUFFER_SIZE];
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

	public static File getWorkspace() {
		String str = System.getProperty("osgi.instance.area");
		if (str != null) {
			return new File(str.replaceFirst("^file:", ""));
		} else {
			return new File(System.getProperty("user.home"), "MagicAssistant");
		}
	}

	public static File getWorkspaceFile(String path) {
		if (path == null || path.isEmpty())
			return getWorkspace();
		return new File(getWorkspace(), path);
	}

	public static File getMagicCardsDir() {
		String str = System.getProperty("ma.magiccards.area");
		if (str == null)
			return getLocationPropery(CorePreferenceConstants.DIR_MAGICCARDS, MAGICCARDS);
		return new File(str);
	}

	public static File getBackupDir() {
		return getLocationPropery(CorePreferenceConstants.DIR_BACKUP, BACKUP);
	}

	public static File getLocationPropery(String propertyKey, String defaultValue) {
		File preferenceFile = getPreferenceFile();
		if (preferenceFile.isFile()) {
			try {
				Properties prop = new Properties();
				FileInputStream inStream = new FileInputStream(preferenceFile);
				prop.load(inStream);
				inStream.close();
				String sdir = prop.getProperty(propertyKey);
				if (sdir != null) {
					File dir = new File(sdir);
					if (!dir.isAbsolute())
						return getWorkspaceFile(sdir);
					else
						return dir;
				}
			} catch (Exception e) {
				// sad
			}
		}
		return getWorkspaceFile(defaultValue);
	}

	public static File getPreferenceFile() {
		return new File(getWorkspace(), ".metadata/.plugins/org.eclipse.core.runtime/.settings/"
				+ DataManager.ID + ".prefs");
	}

	public static File getStateLocationFile() {
		if (runningInWorkbench()) {
			// System.err.println("Eclipse home: " + inEclipse);
			return Activator.getDefault().getStateLocation().toFile();
		} else {
			return getWorkspaceFile(".metadata/.plugins/" + DataManager.ID);
		}
	}

	protected static boolean runningInWorkbench() {
		return System.getProperty("eclipse.home.location") != null;
	}

	public static InputStream loadDbResource(String name) throws IOException {
		if (runningInWorkbench()) {
			return DbActivator.loadResource(name);
		} else {
			return FileUtils.class.getClassLoader().getResourceAsStream(name);
		}
	}

	public static void migrate(File newFile, File oldFile) throws IOException {
		if (newFile.isDirectory() && oldFile.isDirectory()) {
			File[] newFiles = newFile.listFiles();
			if (newFiles != null && newFiles.length == 0) {
				File[] oldFiles = oldFile.listFiles();
				if (oldFiles != null && oldFiles.length > 0) {
					FileUtils.copyTree(oldFile, newFile);
				}
			}
		} else {
			if (oldFile.exists() && !oldFile.isDirectory() && !newFile.exists()) {
				FileUtils.copyFile(oldFile, newFile);
			}
		}
	}

	public static void main(String[] args) {
		MagicLogger.log("aaa");
		DataManager.getInstance().waitForInit(10);
		System.err.println("Loaded " + DataManager.getInstance().getMagicDBStore().size() + " cards");
	}
}
