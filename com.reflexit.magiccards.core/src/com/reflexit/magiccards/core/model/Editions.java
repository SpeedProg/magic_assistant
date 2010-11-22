package com.reflexit.magiccards.core.model;

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

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import com.reflexit.magiccards.core.Activator;

public class Editions implements ISearchableProperty {
	private static final String EDITIONS_FILE = "editions.txt";
	private static Editions instance = new Editions();
	private HashMap<String, String> name2abbr;

	private Editions() {
		this.name2abbr = new HashMap();
		try {
			load();
		} catch (Exception e) {
			Activator.log(e);
		}
	}

	public static Editions getInstance() {
		return instance;
	}

	public Collection<String> getEditions() {
		return this.name2abbr.keySet();
	}

	public String getNameByAbbr(String abbr) {
		for (Iterator iterator = name2abbr.keySet().iterator(); iterator.hasNext();) {
			String name = (String) iterator.next();
			String abbrValue = name2abbr.get(name);
			if (abbrValue != null && abbrValue.equals(abbr)) {
				return name;
			}
		}
		if (abbr.equals("8E"))
			return getNameByAbbr("8ED");
		return null;
	}

	public synchronized void addAbbr(String name, String abbr) {
		String guessed = name.replaceAll("\\W", "_");
		if (!name2abbr.containsKey(name) || name2abbr.get(name).equals(guessed)) {
			if (abbr != null)
				this.name2abbr.put(name, abbr);
			else {
				this.name2abbr.put(name, guessed);
			}
		}
	}

	public String getAbbrByName(String name) {
		return this.name2abbr.get(name);
	}

	public synchronized void load() throws IOException {
		IPath path = Activator.getStateLocationAlways().append(EDITIONS_FILE);
		String strfile = path.toOSString();
		if (Activator.getDefault() != null) {
			InputStream ist = FileLocator.openStream(Activator.getDefault().getBundle(), new Path("resources/" + EDITIONS_FILE), true);
			loadEditions(ist);
		}
		if (!new File(strfile).exists()) {
			save();
		}
		InputStream st = new FileInputStream(strfile);
		loadEditions(st);
	}

	private synchronized void loadEditions(InputStream st) throws IOException {
		BufferedReader r = new BufferedReader(new InputStreamReader(st));
		try {
			String line;
			while ((line = r.readLine()) != null) {
				String[] attrs = line.split("\\|");
				String name = attrs[0].trim();
				String abbr1 = attrs[1].trim();
				addAbbr(name, abbr1);
				try {
					if (attrs.length < 3)
						continue; // old style
					String abbrOther = attrs[2].trim();
					if (abbrOther.equals("en-us") || abbrOther.equals("EN"))
						continue; // old style
				} catch (Exception e) {
					System.err.println("bad record: " + line);
				}
			}
		} finally {
			r.close();
		}
	}

	public synchronized void save() throws FileNotFoundException {
		IPath path = Activator.getStateLocationAlways().append(EDITIONS_FILE);
		PrintStream st = new PrintStream(path.toPortableString());
		try {
			for (Iterator iterator = this.name2abbr.keySet().iterator(); iterator.hasNext();) {
				String name = (String) iterator.next();
				String abbr = this.name2abbr.get(name);
				st.println(name + "|" + abbr + "|||");
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
		String abbr = this.name2abbr.get(name);
		return FilterHelper.getPrefConstant(getIdPrefix(), abbr);
	}

	public String getNameById(String id) {
		HashMap idToName = new HashMap();
		for (Iterator iterator = this.name2abbr.keySet().iterator(); iterator.hasNext();) {
			String name = (String) iterator.next();
			String abbr = this.name2abbr.get(name);
			String id1 = getPrefConstant(abbr);
			idToName.put(id1, name);
		}
		return (String) idToName.get(id);
	}

	public Collection getNames() {
		return new ArrayList(this.name2abbr.keySet());
	}
}
