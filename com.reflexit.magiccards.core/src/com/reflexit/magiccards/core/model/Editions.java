package com.reflexit.magiccards.core.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import com.reflexit.magiccards.core.Activator;
import com.reflexit.magiccards.db.DbActivator;

public class Editions implements ISearchableProperty {
	private static final String EDITIONS_FILE = "editions.txt";
	private static Editions instance = new Editions();

	public static class Edition {
		String name;
		String abbrs[];
		Date release;
		String type = "?";
		static final SimpleDateFormat formatter = new SimpleDateFormat("MMMM yyyy", Locale.ENGLISH);

		public Edition(String name, String abbr) {
			if (abbr == null)
				abbr = "_" + name.replaceAll("\\W", "_"); // fake
			abbrs = new String[1];
			abbrs[0] = abbr;
			this.name = name;
		}

		public void setReleaseDate(String date) throws ParseException {
			if (date == null || date.length() == 0 || date.equals("?"))
				release = null;
			release = formatter.parse(date);
		}

		public Date getReleaseDate() {
			return release;
		}

		public boolean abbreviationOf(String abbr) {
			if (abbr == null || abbrs.length == 0)
				return false;
			for (int i = 0; i < abbrs.length; i++) {
				String a = abbrs[i];
				if (abbr.equals(a))
					return true;
			}
			return false;
		}

		public void addAbbreviation(String abbr) {
			if (abbr == null)
				return;
			if (isAbbreviationFake()) {
				abbrs[0] = abbr;
			} else {
				for (int i = 0; i < abbrs.length; i++) {
					if (abbrs[i].equals(abbr))
						return;
				}
				String[] arr = new String[abbrs.length + 1];
				System.arraycopy(abbrs, 0, arr, 0, abbrs.length);
				arr[abbrs.length] = abbr;
			}
		}

		private boolean isAbbreviationFake() {
			return getMainAbbreviation().startsWith("_");
		}

		public String getMainAbbreviation() {
			return abbrs[0];
		}

		public void setType(String type) {
			if (type == null || type.length() == 0)
				this.type = "?";
			this.type = type;
		}

		public String getName() {
			return name;
		}

		public void setReleaseDate(Date time) {
			release = time;
		}

		public String getType() {
			return type;
		}
	}

	private HashMap<String, Edition> name2ed;

	private Editions() {
		this.name2ed = new HashMap();
		try {
			load();
		} catch (Exception e) {
			Activator.log(e);
		}
	}

	public static Editions getInstance() {
		return instance;
	}

	public Collection<Edition> getEditions() {
		return this.name2ed.values();
	}

	public String getNameByAbbr(String abbr) {
		for (Iterator iterator = name2ed.keySet().iterator(); iterator.hasNext();) {
			String name = (String) iterator.next();
			Edition value = name2ed.get(name);
			if (value != null && value.abbreviationOf(abbr)) {
				return name;
			}
		}
		if (abbr.equals("8E"))
			return getNameByAbbr("8ED");
		return null;
	}

	public synchronized Edition addAbbr(String name, String abbr) {
		Edition edition = name2ed.get(name);
		if (edition == null) {
			edition = new Edition(name, abbr);
			this.name2ed.put(name, edition);
		} else {
			if (abbr != null)
				edition.addAbbreviation(abbr);
		}
		return edition;
	}

	public String getAbbrByName(String name) {
		Edition edition = getEditionByName(name);
		if (edition == null)
			return null;
		return edition.getMainAbbreviation();
	}

	public Edition getEditionByName(String name) {
		return this.name2ed.get(name);
	}

	public synchronized void load() throws IOException {
		IPath path = Activator.getStateLocationAlways().append(EDITIONS_FILE);
		String strfile = path.toOSString();
		if (DbActivator.getDefault() != null) {
			InputStream ist = FileLocator.openStream(DbActivator.getDefault().getBundle(), new Path("resources/" + EDITIONS_FILE), true);
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
				try {
					String[] attrs = line.split("\\|");
					String name = attrs[0].trim();
					String abbr1 = attrs[1].trim();
					Edition set = addAbbr(name, abbr1);
					if (attrs.length < 3)
						continue; // old style
					String abbrOther = attrs[2].trim();
					if (abbrOther.equals("en-us") || abbrOther.equals("EN"))
						continue; // old style
					if (abbrOther.length() > 0)
						set.addAbbreviation(abbrOther);
					String releaseDate = attrs[3].trim();
					if (releaseDate != null && releaseDate.length() > 0)
						set.setReleaseDate(releaseDate);
					else
						System.err.println("Missing release date " + line);
					String type = attrs[4].trim();
					if (type != null && type.length() > 0)
						set.setType(type);
					else
						System.err.println("Missing type " + line);
				} catch (Exception e) {
					System.err.println("bad editions record: " + line);
					e.printStackTrace();
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
			for (Iterator iterator = this.name2ed.keySet().iterator(); iterator.hasNext();) {
				String name = (String) iterator.next();
				Edition ed = getEditionByName(name);
				String rel = "";
				if (ed.getReleaseDate() != null)
					rel = Edition.formatter.format(ed.getReleaseDate());
				String type = "";
				if (ed.getType() != null) {
					type = ed.getType();
				}
				st.println(name + "|" + ed.getMainAbbreviation() + "||" + rel + "|" + type + "|");
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
		for (Iterator iterator = this.name2ed.values().iterator(); iterator.hasNext();) {
			Edition ed = (Edition) iterator.next();
			String abbr = ed.getMainAbbreviation();
			list.add(getPrefConstant(abbr));
		}
		return list;
	}

	public String getPrefConstant(String abbr) {
		return FilterHelper.getPrefConstant(getIdPrefix(), abbr);
	}

	public String getPrefConstantByName(String name) {
		String abbr = getAbbrByName(name);
		return FilterHelper.getPrefConstant(getIdPrefix(), abbr);
	}

	public String getNameById(String id) {
		HashMap idToName = new HashMap();
		for (Iterator iterator = this.name2ed.keySet().iterator(); iterator.hasNext();) {
			String name = (String) iterator.next();
			String id1 = getPrefConstantByName(name);
			idToName.put(id1, name);
		}
		return (String) idToName.get(id);
	}

	public Collection<String> getNames() {
		return new ArrayList(this.name2ed.keySet());
	}
}
