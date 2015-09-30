package com.reflexit.magiccards.core.exports;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;
import com.reflexit.magiccards.core.FileUtils;
import com.reflexit.magiccards.core.MagicLogger;
import com.reflexit.magiccards.core.monitor.ICoreProgressMonitor;

@SuppressWarnings("rawtypes")
public class ReportType {
	private String label;
	private Properties properties;
	private boolean custom;
	Object exportWorker;
	Object importWorker;
	public static final String EXT_PROP = "ext";
	public static final String XML_PROP = "xml";

	ReportType(String label, boolean xml, String extension) {
		this.label = label;
		properties = new Properties();
		setXml(xml);
		setExtension(extension == null ? "txt" : extension);
	}

	private void setXml(boolean xml) {
		if (xml)
			properties.setProperty(XML_PROP, String.valueOf(xml));
	}

	/**
	 * Return true if given format is table format. Table format can have
	 * header.
	 */
	public boolean isXmlFormat() {
		return Boolean.valueOf(properties.getProperty(XML_PROP));
	}

	@Override
	public String toString() {
		return label;
	}

	public String getLabel() {
		return label;
	}

	public String getExtension() {
		return properties.getProperty(EXT_PROP);
	}

	public Properties getProperties() {
		return properties;
	}

	public void setProperty(String key, int value) {
		this.properties.setProperty(key, String.valueOf(value));
	}

	public void setProperty(String key, String value) {
		this.properties.setProperty(key, value);
	}

	public String getProperty(String key) {
		return properties.getProperty(key);
	}

	public void setExtension(String ext) {
		properties.setProperty(EXT_PROP, ext);
	}

	public void setCustom(boolean b) {
		this.custom = b;
	}

	public boolean isCustom() {
		return custom;
	}

	public File getFile() {
		File dir = ReportType.getStorageFile();
		String name = getLabel() + ".ini";
		return new File(dir, name);
	}

	public void save() throws IOException {
		if (!isCustom())
			throw new IOException("Cannot save non-custom type");
		FileOutputStream fs = new FileOutputStream(getFile());
		getProperties().store(fs, "export/import " + getLabel());
		try {
			fs.close();
		} catch (Exception e) {
			// ignore
		}
	}

	public void delete() throws IOException {
		if (!isCustom())
			throw new IOException("Cannot delete non-custom type");
		getFile().delete();
		ImportExportFactory.remove(getLabel());
	}

	public void load() throws IOException {
		File file = getFile();
		FileInputStream fs = new FileInputStream(file);
		getProperties().load(fs);
		fs.close();
		setCustom(true);
		setExportDelegate(new CustomExportDelegate(this));
	}

	public static ReportType load(File file) throws IOException {
		String name = file.getName().replaceAll("\\.ini$", "");
		ReportType old = ImportExportFactory.getByLabel(name);
		if (old != null && !old.isCustom())
			throw new IOException("Cannot override non-custom type");
		ReportType type = ImportExportFactory.createReportType(name);
		type.load();
		return type;
	}

	public static File getStorageFile() {
		File file = new File(FileUtils.getMagicCardsDir(), ".settings/exporters");
		file.mkdirs();
		File oldFile = new File(FileUtils.getStateLocationFile(), "exporters");
		try {
			FileUtils.migrate(file, oldFile);
		} catch (IOException e) {
			MagicLogger.log(e);
		}
		return file;
	}

	public IExportDelegate getExportDelegate() {
		Object className = exportWorker;
		if (className instanceof IExportDelegate) {
			return (IExportDelegate) className;
		}
		if (className instanceof String) {
			try {
				Class loadClass = getClass().getClassLoader().loadClass((String) className);
				IExportDelegate newInstance = (IExportDelegate) loadClass.newInstance();
				newInstance.setReportType(this);
				exportWorker = newInstance;
				return newInstance;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	public IImportDelegate getImportDelegate() {
		Object className = importWorker;
		if (className instanceof IImportDelegate) {
			return (IImportDelegate) className;
		}
		if (className instanceof String) {
			try {
				Class loadClass = getClass().getClassLoader().loadClass((String) className);
				IImportDelegate newInstance = (IImportDelegate) loadClass.newInstance();
				newInstance.setReportType(this);
				importWorker = newInstance;
				return newInstance;
			} catch (Throwable e) {
				MagicLogger.log(e);
			}
		}
		return null;
	}

	public void setExportDelegate(IExportDelegate delegate) {
		exportWorker = delegate;
	}

	public void setExportDelegate(String delegate) {
		exportWorker = delegate;
	}

	public void setImportDelegate(String delegate) {
		importWorker = delegate;
	}

	public void setImportDelegate(IImportDelegate delegate) {
		importWorker = delegate;
	}

	public static ReportType autoDetectType(File file, Collection<ReportType> types) {
		String fileName = file.getPath();
		if (fileName == null || fileName.trim().length() == 0)
			return null;
		Collection<ReportType> candidates = new ArrayList<ReportType>();
		int k = fileName.lastIndexOf('.');
		String ext = "";
		if (k > 0 && k < fileName.length() - 1) {
			ext = fileName.substring(k + 1, fileName.length());
		}
		for (ReportType reportType : types) {
			if (ext.equalsIgnoreCase(reportType.getExtension())) {
				candidates.add(reportType);
			}
		}
		if (candidates.size() == 1)
			return candidates.iterator().next();
		if (file.exists()) {
			try {
				String contents = FileUtils.readFileAsString(file);
				return autoDetectType(contents, candidates);
			} catch (IOException e) {
				// fall through
			}
		}
		if (candidates.size() > 0)
			return candidates.iterator().next();
		return null;
	}

	public static ReportType autoDetectType(URL url, Collection<ReportType> types) {
		if (url == null || url.getPath().isEmpty())
			return null;
		Collection<ReportType> candidates = new ArrayList<ReportType>();
		for (ReportType reportType : types) {
			reportType.getImportDelegate(); // instanciate delegate // deelgate
			String regex = reportType.getProperty("url_regex");
			if (regex == null)
				continue;
			if (url.toExternalForm().matches(regex)) {
				candidates.add(reportType);
			}
		}
		if (candidates.size() > 0)
			return candidates.iterator().next();
		return null;
	}

	public static ReportType autoDetectType(String contents, Collection<ReportType> candidates) {
		ReportType selected = null;
		int errors = Integer.MAX_VALUE;
		for (ReportType reportType : candidates) {
			IImportDelegate id = reportType.getImportDelegate();
			ImportData importData = new ImportData();
			importData.setText(contents);
			id.init(importData);
			try {
				id.run(ICoreProgressMonitor.NONE);
				ImportData result = id.getResult();
				if (result.getError() == null && result.getList().size() > 0) {
					int err = result.getErrorCount();
					if (err < errors) {
						selected = reportType;
						if (err == 0)
							break;
						errors = err;
					}
				}
			} catch (InvocationTargetException e) {
				// continue
			} catch (InterruptedException e) {
				// continue
			}
		}
		return selected;
	}

	public String getExample() {
		IExportDelegate export = getExportDelegate();
		if (export != null) {
			return export.getExample();
		}
		if (getImportDelegate() != null) {
			return getImportDelegate().getExample();
		}
		return null;
	}
}
