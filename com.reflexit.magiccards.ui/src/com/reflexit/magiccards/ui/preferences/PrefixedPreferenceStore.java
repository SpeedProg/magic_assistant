/*******************************************************************************
 * Copyright (c) 2008 Alena Laskavaia.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alena Laskavaia - initial API and implementation
 *******************************************************************************/
package com.reflexit.magiccards.ui.preferences;

import java.util.ArrayList;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.osgi.service.prefs.BackingStoreException;

/**
 * @author Alena
 * 
 */
public class PrefixedPreferenceStore implements IPreferenceStore {
	private final IPreferenceStore store;
	private final String prefix;

	@Override
	public void addPropertyChangeListener(IPropertyChangeListener listener) {
		this.store.addPropertyChangeListener(listener);
	}

	public String[] preferenceNames() {
		String res[] = null;
		if (store instanceof PreferenceStore) {
			res = ((PreferenceStore) store).preferenceNames();
		} else if (store instanceof ScopedPreferenceStore) {
			IEclipsePreferences[] preferenceNodes = ((ScopedPreferenceStore) store).getPreferenceNodes(false);
			try {
				if (preferenceNodes.length > 0)
					res = preferenceNodes[0].keys();
			} catch (BackingStoreException e) {
				// res = null;
			}
		}
		if (res == null)
			return null;
		ArrayList<String> arr = new ArrayList<String>();
		int l = prefix.length() + 1;
		for (int i = 0; i < res.length; i++) {
			String full = res[i];
			if (full.startsWith(prefix))
				arr.add(full.substring(l));
		}
		return arr.toArray(new String[arr.size()]);
	}

	public void setToDefault() {
		String[] preferenceNames = preferenceNames();
		for (String id : preferenceNames) {
			store.setToDefault(toGlobal(id));
		}
	}

	public boolean isDefault() {
		String[] preferenceNames = preferenceNames();
		for (String id : preferenceNames) {
			if (!store.isDefault(toGlobal(id)))
				return false;
		}
		return true;
	}

	@Override
	public boolean contains(String name) {
		return this.store.contains(toGlobal(name));
	}

	@Override
	public void firePropertyChangeEvent(String name, Object oldValue, Object newValue) {
		this.store.firePropertyChangeEvent(toGlobal(name), oldValue, newValue);
	}

	@Override
	public boolean getBoolean(String name) {
		return this.store.getBoolean(toGlobal(name));
	}

	@Override
	public boolean getDefaultBoolean(String name) {
		return this.store.getDefaultBoolean(toGlobal(name));
	}

	@Override
	public double getDefaultDouble(String name) {
		return this.store.getDefaultDouble(toGlobal(name));
	}

	@Override
	public float getDefaultFloat(String name) {
		return this.store.getDefaultFloat(toGlobal(name));
	}

	@Override
	public int getDefaultInt(String name) {
		return this.store.getDefaultInt(toGlobal(name));
	}

	@Override
	public long getDefaultLong(String name) {
		return this.store.getDefaultLong(toGlobal(name));
	}

	@Override
	public String getDefaultString(String name) {
		return this.store.getDefaultString(toGlobal(name));
	}

	@Override
	public double getDouble(String name) {
		return this.store.getDouble(toGlobal(name));
	}

	@Override
	public float getFloat(String name) {
		return this.store.getFloat(toGlobal(name));
	}

	@Override
	public int getInt(String name) {
		return this.store.getInt(toGlobal(name));
	}

	@Override
	public long getLong(String name) {
		return this.store.getLong(toGlobal(name));
	}

	@Override
	public String getString(String name) {
		return this.store.getString(toGlobal(name));
	}

	@Override
	public boolean isDefault(String name) {
		return this.store.isDefault(toGlobal(name));
	}

	@Override
	public boolean needsSaving() {
		return this.store.needsSaving();
	}

	@Override
	public void putValue(String name, String value) {
		this.store.putValue(toGlobal(name), value);
	}

	@Override
	public void removePropertyChangeListener(IPropertyChangeListener listener) {
		this.store.removePropertyChangeListener(listener);
	}

	@Override
	public void setDefault(String name, boolean value) {
		this.store.setDefault(toGlobal(name), value);
	}

	@Override
	public void setDefault(String name, double value) {
		this.store.setDefault(toGlobal(name), value);
	}

	@Override
	public void setDefault(String name, float value) {
		this.store.setDefault(toGlobal(name), value);
	}

	@Override
	public void setDefault(String name, int value) {
		this.store.setDefault(toGlobal(name), value);
	}

	@Override
	public void setDefault(String name, long value) {
		this.store.setDefault(toGlobal(name), value);
	}

	@Override
	public void setDefault(String name, String defaultObject) {
		this.store.setDefault(toGlobal(name), defaultObject);
	}

	@Override
	public void setToDefault(String name) {
		this.store.setToDefault(toGlobal(name));
	}

	@Override
	public void setValue(String name, boolean value) {
		this.store.setValue(toGlobal(name), value);
	}

	@Override
	public void setValue(String name, double value) {
		this.store.setValue(toGlobal(name), value);
	}

	@Override
	public void setValue(String name, float value) {
		this.store.setValue(toGlobal(name), value);
	}

	@Override
	public void setValue(String name, int value) {
		this.store.setValue(toGlobal(name), value);
	}

	@Override
	public void setValue(String name, long value) {
		this.store.setValue(toGlobal(name), value);
	}

	@Override
	public void setValue(String name, String value) {
		this.store.setValue(toGlobal(name), value);
	}

	public String toGlobal(String name) {
		String id = (this.prefix + "." + name).intern();
		return id;
	}

	/**
	 * 
	 */
	public PrefixedPreferenceStore(IPreferenceStore parent, String prefix) {
		this.store = parent;
		if (prefix == null)
			this.prefix = "other";
		else
			this.prefix = prefix;
	}

	@Override
	public String toString() {
		String header = prefix + ":" + store.toString();
		String body = "";
		String[] preferenceNames = preferenceNames();
		for (String id : preferenceNames) {
			String gid = toGlobal(id);
			if (!store.isDefault(gid)) {
				body += "   " + id + "=" + store.getString(gid) + "\n";
			}
		}
		return header + body;
	}
}
