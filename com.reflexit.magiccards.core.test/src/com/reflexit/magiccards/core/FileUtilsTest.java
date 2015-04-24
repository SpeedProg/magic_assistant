package com.reflexit.magiccards.core;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import junit.framework.TestCase;

import org.junit.Test;

import com.reflexit.magiccards.core.model.nav.CardCollection;
import com.reflexit.magiccards.core.model.nav.CollectionsContainer;
import com.reflexit.magiccards.core.model.nav.ModelRoot;

public class FileUtilsTest extends TestCase {
	private ModelRoot root;
	private CollectionsContainer fLib;
	private CardCollection defaultLib;

	@Override
	protected void setUp() {
		root = DataManager.getInstance().getModelRoot();
		fLib = root.getCollectionsContainer();
		defaultLib = root.getDefaultLib();
	}

	@Test
	public void testZip() {
		final File ws = FileUtils.getWorkspace();
		File backupDir = FileUtils.getBackupDir();
		File restoreDir = new File(ws, ".backup_restore");
		// special test for empty directories in ZIP file.
		File emptyTestDir = FileUtils.getWorkspaceFile("empty_dir_test");
		if (!backupDir.exists()) {
			backupDir.mkdirs();
		} else {
			// make sure that nothing is in backup directory due to previous failed tests
			FileUtils.deleteTree(backupDir);
			backupDir.mkdirs();
		}
		if (!restoreDir.exists()) {
			restoreDir.mkdirs();
		} else {
			// make sure that nothing is in restore directory due to previous failed tests
			FileUtils.deleteTree(restoreDir);
			restoreDir.mkdirs();
		}
		if (!emptyTestDir.exists()) {
			emptyTestDir.mkdirs();
		} else {
			// make sure that nothing is in empty directory due to previous failed tests
			FileUtils.deleteTree(emptyTestDir);
			emptyTestDir.mkdirs();
		}
		SimpleDateFormat format = new SimpleDateFormat("YYYY_MMdd_HHmmss");
		final File backup = new File(backupDir, format.format(new Date()) + ".zip");
		List<File> exclude = new ArrayList<File>();
		exclude.add(FileUtils.getBackupDir());
		exclude.add(FileUtils.getWorkspaceFile(".metadata"));
		exclude.add(FileUtils.getWorkspaceFile(".restore"));
		try {
			// zip something (workspace)
			FileUtils.zip(ws, backup, exclude);
			assertTrue(backup.length() > 0);
			// unzip the created zip above
			FileUtils.unzip(backup, restoreDir);
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		// make some tests in uncompressed file structure
		// check excludes
		assertTrue(!new File(restoreDir, ".metadata").exists());
		assertTrue(!new File(restoreDir, FileUtils.BACKUP).exists());
		assertTrue(!new File(restoreDir, ".restore").exists());
		// check for magiccards/Collections/main.xml in restore path
		assertTrue(new File(restoreDir, FileUtils.MAGICCARDS + "/" + fLib.getLocation().getPath() + "/"
				+ defaultLib.getLocation().getBaseFileName()).exists());
		// check for empty directory
		File emptyRestoredDir = new File(restoreDir, emptyTestDir.getName());
		assertTrue(emptyRestoredDir.exists());
		assertTrue(emptyRestoredDir.isDirectory());
		assertTrue(emptyRestoredDir.list().length == 0);
		// delete stuff for this test
		assertTrue(FileUtils.deleteTree(restoreDir));
		assertTrue(backup.delete());
		// do it again but this time exclude magiccards/Collections/main.xml also
		exclude.add(FileUtils.getWorkspaceFile(FileUtils.MAGICCARDS + "/" + fLib.getLocation().getPath()
				+ "/" + defaultLib.getLocation().getBaseFileName()));
		if (!restoreDir.exists()) {
			restoreDir.mkdirs();
		}
		try {
			// zip something (workspace)
			FileUtils.zip(ws, backup, exclude);
			assertTrue(backup.length() > 0);
			// unzip the created zip above
			FileUtils.unzip(backup, restoreDir);
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		// check for magiccards/Collections/main.xml in restore path
		assertTrue(!new File(restoreDir, FileUtils.MAGICCARDS + "/" + fLib.getLocation().getPath() + "/"
				+ defaultLib.getLocation().getBaseFileName()).exists());
		assertTrue(new File(restoreDir, FileUtils.MAGICCARDS + "/" + fLib.getLocation().getPath()).exists());
		// final delete
		assertTrue(FileUtils.deleteTree(restoreDir));
		assertTrue(backup.delete());
		assertTrue(emptyTestDir.delete());
	}

	@Test
	public void testZipFile() throws IOException {
		File tmpFile = File.createTempFile("aaa", ".txt");
		File tmpFileZip = File.createTempFile("aaa", ".zip");
		tmpFile.deleteOnExit();
		tmpFileZip.deleteOnExit();
		tmpFileZip.delete();
		assertFalse(tmpFileZip.exists());
		FileUtils.zip(tmpFile, tmpFileZip, null);
		assertTrue(tmpFileZip.exists());
		assertTrue(tmpFileZip.length() > 0);
		tmpFileZip.delete();
		tmpFile.delete();
	}

	@Test
	public void testZipFileIntoExisting() throws IOException {
		File tmpFile = File.createTempFile("aaa", ".txt");
		File tmpFileZip = File.createTempFile("aaa", ".zip");
		tmpFile.deleteOnExit();
		tmpFileZip.deleteOnExit();
		assertTrue(tmpFileZip.exists());
		FileUtils.zip(tmpFile, tmpFileZip, null);
		assertTrue(tmpFileZip.exists());
		assertTrue(tmpFileZip.length() > 0);
		tmpFileZip.delete();
		tmpFile.delete();
	}

	@Test
	public void testZipFileIntoDir() throws IOException {
		File tmpFile = File.createTempFile("aaa", ".txt");
		File tmpFileZip = File.createTempFile("aaa", ".zip");
		tmpFile.deleteOnExit();
		tmpFileZip.deleteOnExit();
		tmpFileZip.delete();
		tmpFileZip.mkdir();
		try {
			FileUtils.zip(tmpFile, tmpFileZip, null);
			fail("Expecting exception");
		} catch (IOException e) {
			// good
		}
		assertTrue(tmpFileZip.exists());
		assertTrue(tmpFileZip.isDirectory());
		assertTrue(tmpFileZip.list().length == 0);
		tmpFileZip.delete();
		tmpFile.delete();
	}

	@Test
	public void testZipSelf() throws IOException {
		File tmpFile = File.createTempFile("aaa", "dir");
		tmpFile.delete();
		tmpFile.mkdir();
		File tmpFileZip = new File(tmpFile, "out.zip");
		tmpFile.deleteOnExit();
		tmpFileZip.deleteOnExit();
		assertTrue(tmpFile.exists());
		FileUtils.zip(tmpFile, tmpFileZip, null);
		assertTrue(tmpFileZip.exists());
		assertTrue(tmpFileZip.length() > 0);
		tmpFileZip.delete();
		FileUtils.deleteTree(tmpFile);
	}
}
