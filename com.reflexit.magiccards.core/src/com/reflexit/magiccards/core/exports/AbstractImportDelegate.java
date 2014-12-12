package com.reflexit.magiccards.core.exports;

import java.io.IOException;
import java.io.InputStream;

import com.reflexit.magiccards.core.model.Editions;
import com.reflexit.magiccards.core.model.ICardField;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.Location;
import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.MagicCardPhysical;
import com.reflexit.magiccards.core.monitor.ICoreProgressMonitor;
import com.reflexit.magiccards.core.monitor.ICoreRunnableWithProgress;

public abstract class AbstractImportDelegate implements ICoreRunnableWithProgress, IImportDelegate<IMagicCard> {
	private InputStream stream;
	private boolean header;
	private Location location;
	private boolean virtual;
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

	@Override
	public void init(InputStream st, Location location, boolean virtual) {
		this.stream = st;
		this.location = location;
		this.virtual = virtual;
		this.importResult = new ImportResult();
		importResult.setType(getType());
		importResult.setFields(getNonTransientFeilds());
		lineNum = 0;
	}

	public Location getSideboardLocation() {
		if (location == null)
			return Location.createLocation("sideboard");
		Location sideboard = location.toSideboard();
		return sideboard;
	}

	@Override
	public void setHeader(boolean header) {
		this.header = header;
	}

	@Override
	public void run(ICoreProgressMonitor monitor) {
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

	@Override
	public ImportResult getResult() {
		return importResult;
	}

	protected MagicCardPhysical createDefaultCard() {
		MagicCardPhysical card = new MagicCardPhysical(new MagicCard(), getLocation());
		card.setOwn(!virtual);
		return card;
	}

	protected void importCard(MagicCardPhysical card) {
		if (card == null)
			return;
		if (!card.isMigrated()) {
			MagicCardPhysical ncard = card.tradeSplit(card.getCount(), card.getForTrade());
			if (ncard != null)
				importResult.add(ncard);
		}
		importResult.add(card);
	}

	@Override
	public void setFieldValue(MagicCardPhysical card, ICardField field, int i, String value) {
		if (field == MagicCardField.EDITION_ABBR) {
			String nameByAbbr = Editions.getInstance().getNameByAbbr(value);
			if (nameByAbbr == null)
				nameByAbbr = "Unknown";
			card.set(MagicCardField.SET, nameByAbbr);
		} else if (field == MagicCardField.LOCATION || field == MagicCardField.CTYPE || field == MagicCardField.CMC
				|| field == MagicCardField.COLOR) {
			// ignore this field
		} else if (field == MagicCardField.SIDEBOARD) {
			if (Boolean.valueOf(value).booleanValue()) {
				card.setLocation(getSideboardLocation());
			}
		} else {
			card.set(field, value);
		}
	}

	protected Location getLocation() {
		return location;
	}

	static ICardField[] getNonTransientFeilds() {
		return MagicCardField.allNonTransientFields(true);
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
