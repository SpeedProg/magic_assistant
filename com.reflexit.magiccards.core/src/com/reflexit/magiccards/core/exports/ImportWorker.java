package com.reflexit.magiccards.core.exports;

import org.eclipse.core.runtime.IProgressMonitor;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
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
	private boolean previewMode = false;
	private PreviewResult result;
	private int line;
	public static class PreviewResult {
		public ArrayList<String[]> values = new ArrayList<String[]>();
		public ICardField[] fields;
		public ReportType type;
	}

	public ImportWorker(ReportType type, InputStream st, boolean header, IFilteredCardStore<IMagicCard> store,
	        ICardStore lookupStore) {
		super();
		this.stream = st;
		this.saveStore = store;
		this.type = type;
		this.lookupStore = lookupStore;
		this.header = header;
		result = new PreviewResult();
	}

	public ImportWorker(ReportType type, InputStream st, boolean header) {
		super();
		this.stream = st;
		this.type = type;
		this.header = header;
		this.previewMode = true;
		result = new PreviewResult();
	}

	public void setHeader(boolean header) {
		this.header = header;
	}

	public void setFields(ICardField[] fields) {
		this.fields = fields;
	}

	public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		monitor.beginTask("Importing...", 100);
		try {
			if (type == ReportType.CSV)
				runCsvImport(monitor);
			else if (type == ReportType.TEXT_DECK_CLASSIC) {
				runDeckImport(monitor);
			} else if (type == ReportType.TABLE_PIPED) {
				runTablePipedImport(monitor);
			}
		} finally {
			monitor.done();
		}
	}

	public PreviewResult getPreview() {
		return result;
	}

	/**
	 * format:
	 *   Card Name (Set) x4
	 *   4 x Card Name (Set)
	 * @param monitor
	 * @throws InvocationTargetException
	 */
	public void runDeckImport(IProgressMonitor monitor) throws InvocationTargetException {
		result.type = type;
		DeckParser parser = new DeckParser(stream);
		parser.addPattern(Pattern.compile("\\s*(.*?)\\s*(?:\\(([^)]*)\\))?\\s+[xX]\\s*(\\d+)"), new ICardField[] {
		        MagicCardField.NAME,
		        MagicCardField.SET,
		        MagicCardFieldPhysical.COUNT });
		parser.addPattern(Pattern.compile("\\s*(\\d+)\\s*[xX]\\s+([^(]*[^\\s(])(?:\\s*\\(([^)]*)\\))?"),
		        new ICardField[] { MagicCardFieldPhysical.COUNT, MagicCardField.NAME, MagicCardField.SET, });
		do {
			line++;
			try {
				MagicCardPhisical card = createDefaultCard();
				card = parser.readLine(card);
				result.fields = parser.getCurrentFields();
				if (card == null)
					break;
				importCard(card);
				if (previewMode && line >= 10)
					break;
				monitor.worked(1);
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
		if (previewMode) {
			String[] res = new String[result.fields.length];
			for (int i = 0; i < result.fields.length; i++) {
				ICardField fi = result.fields[i];
				Object o = card.getObjectByField(fi);
				res[i] = o == null ? null : o.toString();
			}
			result.values.add(res);
		} else {
			MagicCard ref = findRef(card.getCard());
			if (ref != null)
				card.setMagicCard(ref);
			saveStore.getCardStore().add(card);
		}
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
			result.type = type;
			CsvImporter importer = null;
			if (fields == null)
				fields = getNonTransientFeilds();
			result.fields = fields;
			try {
				importer = new CsvImporter(stream);
				do {
					line++;
					List<String> list = importer.readLine();
					if (list == null)
						break;
					if (header && line == 1)
						continue;
					MagicCardPhisical card = createCard(list);
					importCard(card);
					if (previewMode && line >= 10)
						break;
					monitor.worked(1);
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
			result.type = type;
			if (fields == null)
				fields = getNonTransientFeilds();
			result.fields = fields;
			BufferedReader importer = null;
			try {
				importer = new BufferedReader(new InputStreamReader(stream));
				do {
					line++;
					String input = importer.readLine();
					if (input == null)
						break;
					String[] split = input.split("|");
					if (split.length > 0) {
						MagicCardPhisical card = createCard(Arrays.asList(split));
						importCard(card);
					}
					if (previewMode && line >= 10)
						break;
					monitor.worked(1);
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
