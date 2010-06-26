package com.reflexit.magiccards.core.exports;

import org.eclipse.core.runtime.IProgressMonitor;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.reflexit.magiccards.core.model.Editions;
import com.reflexit.magiccards.core.model.ICardField;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.Location;
import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.MagicCardFieldPhysical;
import com.reflexit.magiccards.core.model.MagicCardPhisical;
import com.reflexit.magiccards.core.model.storage.ICardStore;

public abstract class AbstractImportDelegate implements ICoreRunnableWithProgress, IImportDelegate {
	private InputStream stream;
	private boolean header;
	private Location location;
	private ReportType type;
	private ICardField[] fields = getNonTransientFeilds();
	private ICardStore lookupStore = null;
	protected boolean previewMode = false;
	protected PreviewResult previewResult = new PreviewResult();
	private ArrayList<IMagicCard> toImport = new ArrayList<IMagicCard>();
	protected int line = 0;

	public AbstractImportDelegate() {
		previewResult.setType(getType());
		previewResult.setFields(getNonTransientFeilds());
	}

	public ReportType getType() {
		return type;
	}

	public void setType(ReportType type) {
		this.type = type;
	}

	public InputStream getStream() {
		return stream;
	}

	public void setStream(InputStream stream) {
		this.stream = stream;
	}

	public void init(InputStream st, boolean preview, Location location, ICardStore lookupStore) {
		this.stream = st;
		this.location = location;
		this.lookupStore = lookupStore;
		this.previewMode = preview;
	}

	public void setHeader(boolean header) {
		this.header = header;
	}

	public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		monitor.beginTask("Importing...", 100);
		try {
			doRun(monitor);
		} catch (Exception e) {
			previewResult.setError(e);
		} finally {
			monitor.done();
		}
	}

	protected abstract void doRun(IProgressMonitor monitor) throws IOException;

	public PreviewResult getPreview() {
		return previewResult;
	}

	public ArrayList<IMagicCard> getImportedCards() {
		return toImport;
	}

	protected MagicCardPhisical createDefaultCard() {
		MagicCardPhisical card = new MagicCardPhisical(new MagicCard(), getLocation());
		return card;
	}

	protected void importCard(MagicCardPhisical card) {
		if (card == null)
			return;
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
		if (previewMode) {
			String[] res = new String[previewResult.getFields().length];
			for (int i = 0; i < previewResult.getFields().length; i++) {
				ICardField fi = previewResult.getFields()[i];
				Object o = card.getObjectByField(fi);
				res[i] = o == null ? null : o.toString();
			}
			previewResult.getValues().add(res);
		} else {
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

	protected MagicCardPhisical createCard(List<String> list) {
		MagicCardPhisical card = createDefaultCard();
		for (int i = 0; i < fields.length && i < list.size(); i++) {
			ICardField f = fields[i];
			String value = list.get(i);
			if (value != null && value.length() > 0 && f != null) {
				try {
					setFieldValue(card, f, i, value);
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

	protected void setFieldValue(MagicCardPhisical card, ICardField field, int i, String value) {
		if (field == MagicCardField.EDITION_ABBR) {
			String nameByAbbr = Editions.getInstance().getNameByAbbr(value);
			card.setObjectByField(MagicCardField.SET, nameByAbbr);
		} else {
			card.setObjectByField(field, value);
		}
	}

	protected Location getLocation() {
		return location;
	}

	static ICardField[] getNonTransientFeilds() {
		return MagicCardFieldPhysical.allNonTransientFields();
	}

	public ICardStore getLookupStore() {
		return lookupStore;
	}

	public void setLookupStore(ICardStore lookupStore) {
		this.lookupStore = lookupStore;
	}

	public boolean isPreviewMode() {
		return previewMode;
	}

	public void setPreviewMode(boolean previewMode) {
		this.previewMode = previewMode;
	}

	public PreviewResult getPreviewResult() {
		return previewResult;
	}

	public void setPreviewResult(PreviewResult previewResult) {
		this.previewResult = previewResult;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	public boolean isHeader() {
		return header;
	}

	public ICardField[] getFields() {
		return fields;
	}

	public void setFields(ICardField[] fields) {
		this.fields = fields;
	}
}
