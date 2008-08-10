/**
 * 
 */
package com.reflexit.magiccards.core.xml;

import java.io.File;
import java.util.ArrayList;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.model.IMagicCard;
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
		if (this.list == null)
			this.list = new ArrayList<IMagicCard>();
		if (this.key == null) {
			this.key = DataManager.getModelRoot().getCollectionsContainer().getPath().append(this.file.getName())
			        .toPortableString();
		}
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