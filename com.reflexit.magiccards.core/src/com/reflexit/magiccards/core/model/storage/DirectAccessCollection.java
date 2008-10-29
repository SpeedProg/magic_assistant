package com.reflexit.magiccards.core.model.storage;

import java.util.ArrayList;

public class DirectAccessCollection<T> extends ArrayList<T> {
	private static final long serialVersionUID = 2220293238430511165L;

	@Override
	public T set(int index, T card) {
		if (index >= size()) {
			int l = size();
			for (int i = l - 1; i < index; i++) {
				add(null);
			}
		}
		return super.set(index, card);
	}
}
