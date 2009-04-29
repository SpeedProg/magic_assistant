package com.reflexit.magiccards.core.model;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import com.reflexit.magiccards.core.Activator;

public class Editions implements ISearchableProperty {
	private static final String EDITIONS_FILE = "editions.txt";
	private static Editions instance = new Editions();
	private HashMap name2abbr;
	private HashMap name2locale;

	private Editions() {
		this.name2abbr = new HashMap();
		this.name2locale = new HashMap();
		addAbbr("Lorwyn", "LRW");
		addAbbrLocale("Planar Chaos", "PLC", "en-us");
		try {
			load();
		} catch (IOException e) {
			Activator.log(e);
		}
	}

	public static Editions getInstance() {
		return instance;
	}

	public Collection getEditions() {
		return this.name2abbr.keySet();
	}

	public void addAbbr(String name, String abbr) {
		String guessed = name.replaceAll("\\W", "_");
		if (!name2abbr.containsKey(name) || name2abbr.get(name).equals(guessed)) {
			if (abbr != null)
				this.name2abbr.put(name, abbr);
			else {
				this.name2abbr.put(name, guessed);
			}
		}
	}

	public void addLocale(String name, String locale) {
		this.name2locale.put(name, locale);
	}

	public void addAbbrLocale(String name, String abbr, String locale) {
		addAbbr(name, abbr);
		if (locale != null)
			addLocale(name, locale);
	}

	public String getAbbrByName(String name) {
		return (String) this.name2abbr.get(name);
	}

	public String getLocale(String edition) {
		String locale = (String) this.name2locale.get(edition);
		if (locale != null)
			return locale;
		return null;
	}

	public void load() throws IOException {
		IPath path = Activator.getStateLocationAlways().append(EDITIONS_FILE);
		String strfile = path.toOSString();
		if (Activator.getDefault() != null) {
			InputStream ist = FileLocator.openStream(Activator.getDefault().getBundle(), new Path("resources/"
			        + EDITIONS_FILE), true);
			loadEditions(ist);
		}
		if (!new File(strfile).exists()) {
			save();
		}
		InputStream st = new FileInputStream(strfile);
		loadEditions(st);
	}

	private void loadEditions(InputStream st) throws IOException {
		BufferedReader r = new BufferedReader(new InputStreamReader(st));
		try {
			String line;
			while ((line = r.readLine()) != null) {
				String[] attrs = line.split("\\|");
				addAbbrLocale(attrs[0].trim(), attrs[1].trim(), attrs.length >= 3 ? attrs[2].trim() : null);
			}
		} finally {
			r.close();
		}
	}

	public void save() throws FileNotFoundException {
		IPath path = Activator.getStateLocationAlways().append(EDITIONS_FILE);
		PrintStream st = new PrintStream(path.toPortableString());
		try {
			for (Iterator iterator = this.name2abbr.keySet().iterator(); iterator.hasNext();) {
				String name = (String) iterator.next();
				String abbr = (String) this.name2abbr.get(name);
				String locale = (String) this.name2locale.get(name);
				st.println(name + "|" + abbr + (locale == null ? "" : "|" + locale));
			}
		} finally {
			st.close();
		}
	}

	public String getIdPrefix() {
		return FilterHelper.EDITION;
	}

	public Collection getIds() {
		ArrayList list = new ArrayList();
		for (Iterator iterator = this.name2abbr.values().iterator(); iterator.hasNext();) {
			String abbr = (String) iterator.next();
			list.add(getPrefConstant(abbr));
		}
		return list;
	}

	public String getPrefConstant(String abbr) {
		return FilterHelper.getPrefConstant(getIdPrefix(), abbr);
	}

	public String getPrefConstantByName(String name) {
		String abbr = (String) this.name2abbr.get(name);
		return FilterHelper.getPrefConstant(getIdPrefix(), abbr);
	}

	public String getNameById(String id) {
		HashMap idToName = new HashMap();
		for (Iterator iterator = this.name2abbr.keySet().iterator(); iterator.hasNext();) {
			String name = (String) iterator.next();
			String abbr = (String) this.name2abbr.get(name);
			String id1 = getPrefConstant(abbr);
			idToName.put(id1, name);
		}
		return (String) idToName.get(id);
	}

	public Collection getNames() {
		return new ArrayList(this.name2abbr.keySet());
	}
}
