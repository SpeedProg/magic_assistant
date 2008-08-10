/**
 * 
 */
package com.reflexit.magiccards.core.xml;

import java.io.File;
import java.util.ArrayList;

import com.reflexit.magiccards.core.xml.data.CardCollectionStoreObject;

public class SubTable {
	String key;
	File file;
	ArrayList list;

	/**
	 * @param obj
	 */
	public SubTable(CardCollectionStoreObject obj) {
		this.key = obj.key;
		this.file = obj.file;
		this.list = obj.list;
	}

	/**
	 * 
	 */
	public SubTable() {
	}

	/**
	 * @return
	 */
	public CardCollectionStoreObject toCardCollectionStoreObject() {
		CardCollectionStoreObject obj = new CardCollectionStoreObject();
		obj.file = this.file;
		obj.key = this.key;
		obj.list = this.list;
		return obj;
	}
}