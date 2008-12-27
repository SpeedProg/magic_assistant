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
				res = null;
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
		return this.store.contains(getPropertyName(name));
	}

	public void firePropertyChangeEvent(String name, Object oldValue, Object newValue) {
		this.store.firePropertyChangeEvent(getPropertyName(name), oldValue, newValue);
	}

	public boolean getBoolean(String name) {
		return this.store.getBoolean(getPropertyName(name));
	}

	public boolean getDefaultBoolean(String name) {
		return this.store.getDefaultBoolean(getPropertyName(name));
	}

	public double getDefaultDouble(String name) {
		return this.store.getDefaultDouble(getPropertyName(name));
	}

	public float getDefaultFloat(String name) {
		return this.store.getDefaultFloat(getPropertyName(name));
	}

	public int getDefaultInt(String name) {
		return this.store.getDefaultInt(getPropertyName(name));
	}

	public long getDefaultLong(String name) {
		return this.store.getDefaultLong(getPropertyName(name));
	}

	public String getDefaultString(String name) {
		return this.store.getDefaultString(getPropertyName(name));
	}

	public double getDouble(String name) {
		return this.store.getDouble(getPropertyName(name));
	}

	public float getFloat(String name) {
		return this.store.getFloat(getPropertyName(name));
	}

	public int getInt(String name) {
		return this.store.getInt(getPropertyName(name));
	}

	public long getLong(String name) {
		return this.store.getLong(getPropertyName(name));
	}

	public String getString(String name) {
		return this.store.getString(getPropertyName(name));
	}

	public boolean isDefault(String name) {
		return this.store.isDefault(getPropertyName(name));
	}

	public boolean needsSaving() {
		return this.store.needsSaving();
	}

	public void putValue(String name, String value) {
		this.store.putValue(getPropertyName(name), value);
	}

	public void removePropertyChangeListener(IPropertyChangeListener listener) {
		this.store.removePropertyChangeListener(listener);
	}

	public void setDefault(String name, boolean value) {
		this.store.setDefault(getPropertyName(name), value);
	}

	public void setDefault(String name, double value) {
		this.store.setDefault(getPropertyName(name), value);
	}

	public void setDefault(String name, float value) {
		this.store.setDefault(getPropertyName(name), value);
	}

	public void setDefault(String name, int value) {
		this.store.setDefault(getPropertyName(name), value);
	}

	public void setDefault(String name, long value) {
		this.store.setDefault(getPropertyName(name), value);
	}

	public void setDefault(String name, String defaultObject) {
		this.store.setDefault(getPropertyName(name), defaultObject);
	}

	public void setToDefault(String name) {
		this.store.setToDefault(getPropertyName(name));
	}

	public void setValue(String name, boolean value) {
		this.store.setValue(getPropertyName(name), value);
	}

	public void setValue(String name, double value) {
		this.store.setValue(getPropertyName(name), value);
	}

	public void setValue(String name, float value) {
		this.store.setValue(getPropertyName(name), value);
	}

	public void setValue(String name, int value) {
		this.store.setValue(getPropertyName(name), value);
	}

	public void setValue(String name, long value) {
		this.store.setValue(getPropertyName(name), value);
	}

	public void setValue(String name, String value) {
		this.store.setValue(getPropertyName(name), value);
	}

	private String getPropertyName(String name) {
		String id = (this.prefix + "." + name).intern();
		return id;
	}

	/**
	 * 
	 */
	public PrefixedPreferenceStore(IPreferenceStore parent, String prefix) {
		this.store = parent;
		this.prefix = prefix;
	}
}
