package com.reflexit.magiccards.core.exports;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import com.reflexit.magiccards.core.FileUtils;
import com.reflexit.magiccards.core.model.Editions;
import com.reflexit.magiccards.core.model.Location;
import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.MagicCardPhysical;
import com.reflexit.magiccards.core.model.abs.ICardField;
import com.reflexit.magiccards.core.monitor.ICoreProgressMonitor;
import com.reflexit.magiccards.core.monitor.ICoreRunnableWithProgress;

public abstract class AbstractImportDelegate implements ICoreRunnableWithProgress, IImportDelegate {
	private InputStream stream;
	protected ImportData importData;
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
	public void init(ImportData result) {
		this.importData = result;
		this.stream = new ByteArrayInputStream(result.getText().getBytes(FileUtils.CHARSET_UTF_8));
		importData.clear();
		importData.setType(getType());
		importData.setFields(getNonTransientFeilds());
		lineNum = 0;
	}

	public Location getSideboardLocation() {
		if (getLocation() == null)
			return Location.createLocation("sideboard");
		Location sideboard = getLocation().toSideboard();
		return sideboard;
	}

	@Override
	public void run(ICoreProgressMonitor monitor) {
		monitor.beginTask("Importing...", 100);
		try {
			doRun(monitor);
		} catch (Exception e) {
			importData.setError(e);
		} finally {
			monitor.done();
		}
	}

	protected abstract void doRun(ICoreProgressMonitor monitor) throws IOException;

	@Override
	public ImportData getResult() {
		return importData;
	}

	protected MagicCardPhysical createDefaultCard() {
		MagicCardPhysical card = new MagicCardPhysical(new MagicCard(), getLocation());
		card.setOwn(!isVirutal());
		return card;
	}

	protected boolean isVirutal() {
		return importData.isVirtual();
	}

	protected void importCard(MagicCardPhysical card) {
		if (card == null)
			return;
		if (!card.isMigrated()) {
			MagicCardPhysical ncard = card.tradeSplit(card.getCount(), card.getForTrade());
			if (ncard != null)
				importData.add(ncard);
		}
		importData.add(card);
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
			if (field == MagicCardField.FORTRADECOUNT || field == MagicCardField.LEGALITY
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

	protected Location getLocation() {
		return importData.getLocation();
	}

	static ICardField[] getNonTransientFeilds() {
		return MagicCardField.allNonTransientFields(true);
	}

	public void setLocation(Location location) {
		this.importData.setLocation(location);
	}

	@Override
	public String getExample() {
		return null;
	}
}
