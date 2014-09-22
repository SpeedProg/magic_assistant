package com.reflexit.magiccards.core.exports;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import com.reflexit.magiccards.core.model.ICardField;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.monitor.ICoreProgressMonitor;

public class DeckBoxExportDelegate extends CsvExportDelegate {

	/*-
	 Count,Tradelist Count,Name,Foil,Textless,Promo,Signed,Edition,Condition,Language,Card Number
	 1,0,Angel of Mercy,,,,,,Near Mint,English,
	 3,0,Platinum Angel,,,,,Magic 2010,Near Mint,English,218
	 4,1,Reya Dawnbringer,,,,,Duel Decks: Divine vs. Demonic,Near Mint,English,13
	 */
	@Override
	public void printHeader() {
		stream.println("Count,Tradelist Count,Name,Foil,Textless,Promo,Signed,Edition,Condition,Language");
	}
	protected ICardField[] doGetFields() {
		ICardField fields[] = new ICardField[10];
		fields[0] = MagicCardField.COUNT;
		fields[1] = MagicCardField.FORTRADECOUNT;
		fields[2] = MagicCardField.NAME;
		fields[7] = MagicCardField.SET;
		fields[8] = MagicCardField.SPECIAL;
		fields[9] = MagicCardField.LANG;
		//fields[10] = MagicCardField.COLLNUM;
		return fields;
	}

	@Override
	public void run(ICoreProgressMonitor monitor)
			throws InvocationTargetException, InterruptedException {
		setColumns(doGetFields());
		super.run(monitor);
	}

	@Override
	public boolean isColumnChoiceSupported() {
		return false;
	}
}
