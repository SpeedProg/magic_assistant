package com.reflexit.magiccards.core.exports;

import java.io.IOException;
import java.io.InputStream;

import com.reflexit.magiccards.core.model.Editions;
import com.reflexit.magiccards.core.model.Location;
import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.MagicCardPhysical;
import com.reflexit.magiccards.core.model.abs.ICardField;
import com.reflexit.magiccards.core.monitor.ICoreProgressMonitor;
import com.reflexit.magiccards.core.monitor.ICoreRunnableWithProgress;

public abstract class AbstractImportDelegate implements ICoreRunnableWithProgress,
		IImportDelegate {
	private InputStream stream;
	private boolean header;
	private Location location;
	private boolean virtual;
	protected ImportData importResult;
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
	public void init(InputStream st, ImportData result) {
		this.stream = st;
		this.importResult = result;
		importResult.setType(getType());
		importResult.setFields(getNonTransientFeilds());
		this.location = importResult.getLocation();
		this.virtual = importResult.isVirtual();
		lineNum = 0;
		this.header = result.isHeader();
		result.getList().clear();
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
	public ImportData getResult() {
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
				nameByAbbr = value;
			card.set(MagicCardField.SET, nameByAbbr);
		} else if (field == MagicCardField.SIDEBOARD) {
			if (Boolean.valueOf(value).booleanValue()) {
				card.setLocation(getSideboardLocation());
			}
		} else if (field == MagicCardField.LOCATION) {
			// ignore
		} else if (field.isTransient()) {
			if (field == MagicCardField.FORTRADECOUNT
					|| field == MagicCardField.LEGALITY
					|| field == MagicCardField.IMAGE_URL) {
				// special handling for transient fields, we will set it
				card.set(field, value);
			} else {
				// ignore
			}
		} else {
			card.set(field, value);
		}
	}

	public void setFieldValue1(MagicCardPhysical card, ICardField field, int i, String value) {
		if (field == MagicCardField.EDITION_ABBR) {
			String nameByAbbr = Editions.getInstance().getNameByAbbr(value);
			if (nameByAbbr == null)
				nameByAbbr = value;
			card.set(MagicCardField.SET, nameByAbbr);
		} else if (field == MagicCardField.FORTRADECOUNT || field == MagicCardField.LOCATION) {
			// special handling
			card.set(field, value);
		} else if (field == MagicCardField.SIDEBOARD) {
			if (Boolean.valueOf(value).booleanValue()) {
				card.setLocation(getSideboardLocation());
			}
		} else if (field.isTransient()) {
			// ignore this field
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

	public void setLocation(Location location) {
		this.location = location;
	}

	public boolean isHeader() {
		return header;
	}
}
