package com.reflexit.magiccards.core.exports;

import org.eclipse.core.runtime.IProgressMonitor;

import java.io.BufferedReader;
import java.io.File;
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

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.model.ICardField;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.MagicCardFieldPhysical;
import com.reflexit.magiccards.core.model.MagicCardPhisical;
import com.reflexit.magiccards.core.model.storage.ICardStore;
import com.reflexit.magiccards.core.model.storage.IStorage;
import com.reflexit.magiccards.core.model.storage.IStorageContainer;

public class ImportWorker implements ICoreRunnableWithProgress {
	InputStream stream;
	boolean header;
	String location;
	private ReportType type;
	private ICardField[] fields;
	private ICardStore lookupStore;
	private boolean previewMode = false;
	private PreviewResult previewResult;
	private ArrayList<IMagicCard> toImport;
	private int line;
	public ImportWorker(ReportType type, InputStream st, boolean header, String location, ICardStore lookupStore) {
		super();
		this.stream = st;
		this.location = location;
		this.type = type;
		this.lookupStore = lookupStore;
		this.header = header;
		previewResult = new PreviewResult();
	}

	public ImportWorker(ReportType type, InputStream st, boolean header) {
		super();
		this.stream = st;
		this.type = type;
		this.header = header;
		this.previewMode = true;
		previewResult = new PreviewResult();
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
			toImport = new ArrayList<IMagicCard>();
			if (type == ReportType.CSV)
				runCsvImport(monitor);
			else if (type == ReportType.TEXT_DECK_CLASSIC) {
				runDeckImport(monitor);
			} else if (type == ReportType.TABLE_PIPED) {
				runTablePipedImport(monitor);
			} else if (type == ReportType.XML) {
				runXmlImport(monitor);
			} else {
				throw new IllegalArgumentException("Format is not supported: " + type);
			}
		} catch (Exception e) {
			previewResult.setError(e);
		} finally {
			monitor.done();
		}
	}

	/**
	 * @param monitor
	 * @throws IOException 
	 */
	protected void runXmlImport(IProgressMonitor monitor) throws IOException {
		try {
			previewResult.setType(type);
			previewResult.setFields(fields = getNonTransientFeilds());
			File tmp = File.createTempFile("magic", "xml");
			tmp.deleteOnExit();
			try {
				FileUtils.copyFile(stream, tmp);
				ICardStore store = DataManager.getCardHandler().loadFromXml(tmp.getAbsolutePath());
				IStorage<IMagicCard> storage = ((IStorageContainer<IMagicCard>) store).getStorage();
				String location = storage.getLocation();
				previewResult.setLocation(location);
				Iterator iterator = store.iterator();
				while (iterator.hasNext()) {
					line++;
					Object next = iterator.next();
					if (next instanceof MagicCardPhisical) {
						MagicCardPhisical card = (MagicCardPhisical) next;
						importCard(card);
						card.setLocation(getLocation());
					} else if (next instanceof IMagicCard)
						importCard(new MagicCardPhisical((IMagicCard) next));
					if (previewMode && line >= 10)
						break;
					monitor.worked(1);
				}
			} catch (IOException e) {
				throw e;
			} finally {
				tmp.delete();
			}
		} catch (IOException e) {
			throw e;
		}
	}

	public PreviewResult getPreview() {
		return previewResult;
	}

	public ArrayList<IMagicCard> getImportedCards() {
		return toImport;
	}

	/**
	 * format:
	 *   Card Name (Set) x4
	 *   4 x Card Name (Set)
	 * @param monitor
	 * @throws IOException 
	 * @throws InvocationTargetException
	 */
	public void runDeckImport(IProgressMonitor monitor) throws IOException {
		previewResult.setType(type);
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
				previewResult.setFields(parser.getCurrentFields());
				if (card == null)
					break;
				importCard(card);
				if (previewMode && line >= 10)
					break;
				monitor.worked(1);
			} catch (IOException e) {
				throw e;
			}
		} while (true);
		parser.close();
	}

	protected MagicCardPhisical createDefaultCard() {
		MagicCardPhisical card = new MagicCardPhisical(new MagicCard());
		card.setLocation(getLocation());
		return card;
	}

	protected void importCard(MagicCardPhisical card) {
		if (previewMode) {
			String[] res = new String[previewResult.getFields().length];
			for (int i = 0; i < previewResult.getFields().length; i++) {
				ICardField fi = previewResult.getFields()[i];
				Object o = card.getObjectByField(fi);
				res[i] = o == null ? null : o.toString();
			}
			previewResult.getValues().add(res);
		} else {
			MagicCard ref = findRef(card.getCard());
			if (ref != null) {
				if (card.getSet() == null || ref.getSet().equals(card.getSet()))
					card.setMagicCard(ref);
				else if (card.getSet() != null) {
					MagicCard newCard = (MagicCard) ref.clone();
					newCard.setSet(card.getSet());
					newCard.setId("0");
					card.setMagicCard(newCard);
				}
			}
			toImport.add(card);
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

	public void runCsvImport(IProgressMonitor monitor) throws IOException {
		try {
			previewResult.setType(type);
			CsvImporter importer = null;
			if (fields == null)
				fields = getNonTransientFeilds();
			previewResult.setFields(fields);
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
				throw e;
			} finally {
				if (importer != null)
					importer.close();
			}
		} catch (IOException e) {
			throw e;
		}
	}

	private MagicCardPhisical createCard(List<String> list) {
		MagicCardPhisical card = createDefaultCard();
		for (int i = 0; i < fields.length && i < list.size(); i++) {
			ICardField f = fields[i];
			String value = list.get(i);
			if (value != null && value.length() > 0) {
				try {
					card.setObjectByField(f, value);
				} catch (Exception e) {
					throw new IllegalArgumentException("Error: Line " + line + ",Field " + (i + 1) + ": Expecting " + f
					        + ", text was: " + value);
				}
			}
		}
		if (card.getName() == null || card.getName().trim().length() == 0) {
			throw new IllegalArgumentException("Error: Line " + line + ", Field 2: Expected NAME value is empty");
		}
		card.setLocation(getLocation());
		return card;
	}

	protected String getLocation() {
		return location;
	}

	static ICardField[] getNonTransientFeilds() {
		return MagicCardFieldPhysical.allNonTransientFields();
	}

	public void runTablePipedImport(IProgressMonitor monitor) throws IOException {
		try {
			previewResult.setType(type);
			if (fields == null)
				fields = getNonTransientFeilds();
			previewResult.setFields(fields);
			BufferedReader importer = null;
			try {
				importer = new BufferedReader(new InputStreamReader(stream));
				do {
					line++;
					String input = importer.readLine();
					if (input == null)
						break;
					String[] split = input.split("\\|");
					if (split.length > 1) {
						MagicCardPhisical card = createCard(Arrays.asList(split));
						importCard(card);
					} else {
						throw new IllegalArgumentException("Error: Line " + line
						        + ". Fields seprated by | are not found: " + input);
					}
					if (previewMode && line >= 10)
						break;
					monitor.worked(1);
				} while (true);
			} catch (FileNotFoundException e) {
				throw e;
			} finally {
				if (importer != null)
					importer.close();
			}
		} catch (IOException e) {
			throw e;
		}
	}
}
