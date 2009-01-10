package com.reflexit.magiccards.core.exports;

import org.eclipse.core.runtime.IProgressMonitor;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import com.reflexit.magiccards.core.model.ICardField;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.MagicCardFieldPhysical;
import com.reflexit.magiccards.core.model.MagicCardPhisical;
import com.reflexit.magiccards.core.model.storage.ICardStore;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;
import com.reflexit.magiccards.core.model.storage.ILocatable;

public class ImportWorker implements ICoreRunnableWithProgress {
	InputStream stream;
	boolean header;
	IFilteredCardStore<IMagicCard> saveStore;
	private ReportType type;
	private ICardField[] fields;
	private ICardStore lookupStore;

	public ImportWorker(InputStream st, IFilteredCardStore<IMagicCard> store, ReportType type, ICardStore lookupStore) {
		super();
		this.stream = st;
		this.saveStore = store;
		this.type = type;
		this.lookupStore = lookupStore;
	}

	public void setHeader(boolean header) {
		this.header = header;
	}

	public void setFields(ICardField[] fields) {
		this.fields = fields;
	}

	public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		if (type == ReportType.CSV)
			runCsvImport(monitor);
		else if (type == ReportType.TEXT_DECK_CLASSIC) {
			runDeckImport(monitor);
		} else if (type == ReportType.TABLE_PIPED) {
			runTablePipedImport(monitor);
		}
	}

	public void runDeckImport(IProgressMonitor monitor) throws InvocationTargetException {
		DeckParser parser = new DeckParser(stream);
		parser.addPattern(Pattern.compile("\\s*(.*?)\\s*(?:\\(([^)]*)\\))?\\s+[xX]\\s*(\\d+)"), new ICardField[] {
		        MagicCardField.NAME,
		        MagicCardField.SET,
		        MagicCardFieldPhysical.COUNT });
		parser.addPattern(Pattern.compile("\\s*(\\d+)\\s*[xX]\\s+([^(]*[^\\s(])(?:\\s*\\(([^)]*)\\))?"),
		        new ICardField[] { MagicCardFieldPhysical.COUNT, MagicCardField.NAME, MagicCardField.SET, });
		do {
			try {
				MagicCardPhisical card = createDefaultCard();
				card = parser.readLine(card);
				if (card == null)
					break;
				importCard(card);
			} catch (IOException e) {
				throw new InvocationTargetException(e);
			}
		} while (true);
		parser.close();
	}

	protected MagicCardPhisical createDefaultCard() {
		MagicCardPhisical card = new MagicCardPhisical(new MagicCard());
		String location = getLocation();
		if (location != null) {
			card.setLocation(location);
		}
		return card;
	}

	protected void importCard(MagicCardPhisical card) {
		MagicCard ref = findRef(card.getCard());
		if (ref != null)
			card.setMagicCard(ref);
		saveStore.getCardStore().add(card);
	}

	protected MagicCard findRef(MagicCard card) {
		if (lookupStore == null)
			return card;
		MagicCard cand = null;
		for (Iterator iterator = lookupStore.iterator(); iterator.hasNext();) {
			MagicCard a = (MagicCard) iterator.next();
			if (card.getCardId() != 0 && a.getCardId() == card.getCardId())
				return a;
			if (card.getName() != null && card.getName().equals(a.getName())) {
				if (card.getSet() == null)
					return a;
				if (card.getSet().equals(a.getSet()))
					return a;
				if (cand == null || cand.getCardId() < a.getCardId())
					cand = a;
			}
		}
		return cand;
	}

	public void runCsvImport(IProgressMonitor monitor) throws InvocationTargetException {
		try {
			CsvImporter importer = null;
			if (fields == null)
				fields = getNonTransientFeilds();
			try {
				importer = new CsvImporter(stream);
				do {
					List<String> list = importer.readLine();
					if (list == null)
						break;
					MagicCardPhisical card = createCard(list);
					importCard(card);
				} while (true);
			} catch (FileNotFoundException e) {
				throw new InvocationTargetException(e);
			} finally {
				if (importer != null)
					importer.close();
			}
		} catch (IOException e) {
			throw new InvocationTargetException(e);
		}
	}

	private MagicCardPhisical createCard(List<String> list) {
		MagicCardPhisical card = createDefaultCard();
		for (int i = 0; i < fields.length; i++) {
			ICardField f = fields[i];
			String value = list.get(i);
			card.setObjectByField(f, value);
		}
		return card;
	}

	protected String getLocation() {
		if (saveStore instanceof ILocatable) {
			String getLocation = ((ILocatable) saveStore).getLocation();
			return getLocation;
		}
		return null;
	}

	private ICardField[] getNonTransientFeilds() {
		return MagicCardFieldPhysical.allNonTransientFields();
	}

	public void runTablePipedImport(IProgressMonitor monitor) throws InvocationTargetException {
		try {
			if (fields == null)
				fields = getNonTransientFeilds();
			BufferedReader importer = null;
			try {
				importer = new BufferedReader(new InputStreamReader(stream));
				do {
					String line = importer.readLine();
					if (line == null)
						break;
					String[] split = line.split("|");
					if (split.length > 0) {
						MagicCardPhisical card = createCard(Arrays.asList(split));
						importCard(card);
					}
				} while (true);
			} catch (FileNotFoundException e) {
				throw new InvocationTargetException(e);
			} finally {
				if (importer != null)
					importer.close();
			}
		} catch (IOException e) {
			throw new InvocationTargetException(e);
		}
	}
}
