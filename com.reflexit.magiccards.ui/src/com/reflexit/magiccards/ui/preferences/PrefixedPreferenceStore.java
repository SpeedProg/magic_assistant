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
	private IPreferenceStore store;
	private String prefix;

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
		String arr[] = new String[res.length];
		int l = prefix.length() + 1;
		for (int i = 0; i < res.length; i++) {
			String full = res[i];
			if (full.length() > l)
				arr[i] = full.substring(l);
			else
				arr[i] = full; // should not happened
		}
		return arr;
	}

	public boolean contains(String name) {
		return this.store.contains(toGlobal(name));
	}

	public void firePropertyChangeEvent(String name, Object oldValue, Object newValue) {
		this.store.firePropertyChangeEvent(toGlobal(name), oldValue, newValue);
	}

	public boolean getBoolean(String name) {
		return this.store.getBoolean(toGlobal(name));
	}

	public boolean getDefaultBoolean(String name) {
		return this.store.getDefaultBoolean(toGlobal(name));
	}

	public double getDefaultDouble(String name) {
		return this.store.getDefaultDouble(toGlobal(name));
	}

	public float getDefaultFloat(String name) {
		return this.store.getDefaultFloat(toGlobal(name));
	}

	public int getDefaultInt(String name) {
		return this.store.getDefaultInt(toGlobal(name));
	}

	public long getDefaultLong(String name) {
		return this.store.getDefaultLong(toGlobal(name));
	}

	public String getDefaultString(String name) {
		return this.store.getDefaultString(toGlobal(name));
	}

	public double getDouble(String name) {
		return this.store.getDouble(toGlobal(name));
	}

	public float getFloat(String name) {
		return this.store.getFloat(toGlobal(name));
	}

	public int getInt(String name) {
		return this.store.getInt(toGlobal(name));
	}

	public long getLong(String name) {
		return this.store.getLong(toGlobal(name));
	}

	public String getString(String name) {
		return this.store.getString(toGlobal(name));
	}

	public boolean isDefault(String name) {
		return this.store.isDefault(toGlobal(name));
	}

	public boolean needsSaving() {
		return this.store.needsSaving();
	}

	public void putValue(String name, String value) {
		this.store.putValue(toGlobal(name), value);
	}

	public void removePropertyChangeListener(IPropertyChangeListener listener) {
		this.store.removePropertyChangeListener(listener);
	}

	public void setDefault(String name, boolean value) {
		this.store.setDefault(toGlobal(name), value);
	}

	public void setDefault(String name, double value) {
		this.store.setDefault(toGlobal(name), value);
	}

	public void setDefault(String name, float value) {
		this.store.setDefault(toGlobal(name), value);
	}

	public void setDefault(String name, int value) {
		this.store.setDefault(toGlobal(name), value);
	}

	public void setDefault(String name, long value) {
		this.store.setDefault(toGlobal(name), value);
	}

	public void setDefault(String name, String defaultObject) {
		this.store.setDefault(toGlobal(name), defaultObject);
	}

	public void setToDefault(String name) {
		this.store.setToDefault(toGlobal(name));
	}

	public void setValue(String name, boolean value) {
		this.store.setValue(toGlobal(name), value);
	}

	public void setValue(String name, double value) {
		this.store.setValue(toGlobal(name), value);
	}

	public void setValue(String name, float value) {
		this.store.setValue(toGlobal(name), value);
	}

	public void setValue(String name, int value) {
		this.store.setValue(toGlobal(name), value);
	}

	public void setValue(String name, long value) {
		this.store.setValue(toGlobal(name), value);
	}

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
		this.prefix = prefix;
		if (this.prefix == null)
			this.prefix = "other";
	}
}
