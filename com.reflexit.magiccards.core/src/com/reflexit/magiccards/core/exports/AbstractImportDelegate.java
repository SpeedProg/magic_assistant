package com.reflexit.magiccards.core.exports;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import com.reflexit.magiccards.core.model.Editions;
import com.reflexit.magiccards.core.model.ICardField;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.Location;
import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.MagicCardFieldPhysical;
import com.reflexit.magiccards.core.model.MagicCardPhysical;
import com.reflexit.magiccards.core.monitor.ICoreProgressMonitor;
import com.reflexit.magiccards.core.monitor.ICoreRunnableWithProgress;

public abstract class AbstractImportDelegate implements ICoreRunnableWithProgress, IImportDelegate<IMagicCard> {
	private InputStream stream;
	private boolean header;
	private Location location;
	protected boolean previewMode = false;
	protected ImportResult importResult;
	protected int lineNum = 0;
	private ReportType type;

	public AbstractImportDelegate() {
	}

	public InputStream getStream() {
		return stream;
	}

	@Override
	public void setReportType(ReportType reportType) {
		this.type = reportType;
	}

	@Override
	public ReportType getType() {
		return type;
	}

	public void init(InputStream st, boolean preview, Location location) {
		this.stream = st;
		this.location = location;
		this.previewMode = preview;
		this.importResult = new ImportResult();
		importResult.setType(getType());
		importResult.setFields(getNonTransientFeilds());
	}

	public Location getSideboardLocation() {
		if (location == null)
			return Location.createLocation("sideboard");
		Location sideboard = location.toSideboard();
		return sideboard;
	}

	public void setHeader(boolean header) {
		this.header = header;
	}

	public void run(ICoreProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		monitor.beginTask("Importing...", 100);
		try {
			doRun(monitor);
		} catch (Exception e) {
			importResult.setError(e);
		} finally {
			monitor.done();
		}
	}

	protected abstract void doRun(ICoreProgressMonitor monitor) throws IOException;

	public ImportResult getPreview() {
		return importResult;
	}

	public List getImportedCards() {
		return importResult.getList();
	}

	protected MagicCardPhysical createDefaultCard() {
		MagicCardPhysical card = new MagicCardPhysical(new MagicCard(), getLocation());
		card.setOwn(true);
		return card;
	}

	protected void importCard(MagicCardPhysical card) {
		if (card == null)
			return;
		ImportUtils.updateCardReference(card);
		importResult.add(card);
	}

	public void setFieldValue(MagicCardPhysical card, ICardField field, int i, String value) {
		if (field == MagicCardField.EDITION_ABBR) {
			String nameByAbbr = Editions.getInstance().getNameByAbbr(value);
			if (nameByAbbr == null)
				nameByAbbr = "Unknown";
			card.setObjectByField(MagicCardField.SET, nameByAbbr);
		} else if (field == MagicCardFieldPhysical.LOCATION) {
			// ignore this field
		} else if (field == MagicCardFieldPhysical.SIDEBOARD) {
			if (Boolean.valueOf(value).booleanValue()) {
				card.setLocation(getSideboardLocation());
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

	public boolean isPreviewMode() {
		return previewMode;
	}

	public void setPreviewMode(boolean previewMode) {
		this.previewMode = previewMode;
	}

	public ImportResult getPreviewResult() {
		return importResult;
	}

	public void setPreviewResult(ImportResult previewResult) {
		this.importResult = previewResult;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	public boolean isHeader() {
		return header;
	}
}
