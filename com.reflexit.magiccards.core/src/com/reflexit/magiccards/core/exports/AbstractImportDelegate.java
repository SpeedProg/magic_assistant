package com.reflexit.magiccards.core.exports;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import com.reflexit.magiccards.core.model.Editions;
import com.reflexit.magiccards.core.model.ICardField;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.Location;
import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.MagicCardFieldPhysical;
import com.reflexit.magiccards.core.model.MagicCardPhysical;
import com.reflexit.magiccards.core.model.storage.ICardStore;
import com.reflexit.magiccards.core.monitor.ICoreProgressMonitor;
import com.reflexit.magiccards.core.monitor.ICoreRunnableWithProgress;

public abstract class AbstractImportDelegate implements ICoreRunnableWithProgress, IImportDelegate<IMagicCard> {
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

	public void run(ICoreProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		monitor.beginTask("Importing...", 100);
		try {
			doRun(monitor);
		} catch (Exception e) {
			previewResult.setError(e);
		} finally {
			monitor.done();
		}
	}

	protected abstract void doRun(ICoreProgressMonitor monitor) throws IOException;

	public PreviewResult getPreview() {
		return previewResult;
	}

	public ArrayList<IMagicCard> getImportedCards() {
		return toImport;
	}

	protected MagicCardPhysical createDefaultCard() {
		MagicCardPhysical card = new MagicCardPhysical(new MagicCard(), getLocation());
		return card;
	}

	protected void importCard(MagicCardPhysical card) {
		if (card == null)
			return;
		ImportUtils.updateCardReference(card, lookupStore);
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

	protected MagicCardPhysical createCard(List<String> list) {
		MagicCardPhysical card = createDefaultCard();
		for (int i = 0; i < fields.length && i < list.size(); i++) {
			ICardField f = fields[i];
			String value = list.get(i);
			if (value != null && value.length() > 0 && f != null) {
				try {
					setFieldValue(card, f, i, value.trim());
				} catch (Exception e) {
					throw new IllegalArgumentException("Error: Line " + line + ",CardFieldExpr " + (i + 1) + ": Expecting " + f
							+ ", text was: " + value);
				}
			}
		}
		if (card.getName() == null || card.getName().length() == 0) {
			throw new IllegalArgumentException("Error: Line " + line + ", CardFieldExpr 2: Expected NAME value is empty");
		}
		return card;
	}

	public void setFieldValue(MagicCardPhysical card, ICardField field, int i, String value) {
		if (field == MagicCardField.EDITION_ABBR) {
			String nameByAbbr = Editions.getInstance().getNameByAbbr(value);
			if (nameByAbbr == null)
				nameByAbbr = "Unknown";
			card.setObjectByField(MagicCardField.SET, nameByAbbr);
		}
		if (field == MagicCardFieldPhysical.LOCATION) {
			// ignore this field
			return;
		}
		if (field == MagicCardFieldPhysical.SIDEBOARD) {
			if (Boolean.valueOf(value).booleanValue()) {
				card.setLocation(getLocation().toSideboard());
			}
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
