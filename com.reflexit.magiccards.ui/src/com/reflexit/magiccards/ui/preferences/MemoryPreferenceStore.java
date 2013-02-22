package com.reflexit.magiccards.ui.preferences;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceStore;

public class MemoryPreferenceStore extends PreferenceStore implements IPreferenceStore {
	private String ini = "";

	public MemoryPreferenceStore() {
		super();
	}

	@Override
	public void load() throws IOException {
		ByteArrayInputStream st = new ByteArrayInputStream(ini.getBytes(ini));
		super.load(st);
		st.close();
	}

	@Override
	public void save() throws IOException {
		ByteArrayOutputStream st = new ByteArrayOutputStream();
		super.save(st, null);
		ini = st.toString();
		st.close();
	}
}
