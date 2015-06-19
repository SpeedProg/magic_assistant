package com.reflexit.magiccards.core.exports;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import com.reflexit.magiccards.core.model.Location;
import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.MagicCardPhysical;
import com.reflexit.magiccards.core.model.abs.ICard;
import com.reflexit.magiccards.core.model.abs.ICardField;
import com.reflexit.magiccards.core.model.storage.ILocatable;
import com.reflexit.magiccards.core.monitor.ICoreProgressMonitor;

public abstract class AbstractExportDelegatePerLine<T extends ICard> extends AbstractExportDelegate<T> {
	@Override
	public void export(ICoreProgressMonitor monitor) {
		try {
			if (monitor == null)
				monitor = ICoreProgressMonitor.NONE;
			monitor.beginTask("Exporting to " + getLabel() + "...", store.getSize());
			if (columns == null) {
				columns = deterimeColumns();
			}
			location = store.getLocation();
			if (store.getSize() > 0)
				location = ((ILocatable) store.iterator().next()).getLocation();
			if (location == null)
				location = Location.NO_WHERE;
			printHeader();
			printLocationHeader();
			for (T card : store) {
				Location curLocation = ((ILocatable) card).getLocation();
				if (!location.equals(curLocation)) {
					printLocationFooter();
					location = ((ILocatable) card).getLocation();
					if (location == null)
						location = Location.NO_WHERE;
					printLocationHeader();
				}
				printCard(card);
				monitor.worked(1);
			}
			printLocationFooter();
			printFooter();
		} finally {
			monitor.done();
		}
	}

	public void printCard(T card) {
		Object[] values = getValues(card);
		printLine(values);
	}

	public Object[] getValues(T card) {
		Object values[] = new Object[columns.length];
		int i = 0;
		for (ICardField field : columns) {
			values[i] = getObjectByField(card, field);
			i++;
		}
		return values;
	}

	public Object getObjectByField(T card, ICardField field) {
		if (field == null) return null;
		return ((ICard) card).get(field);
	}

	public void printLine(Object[] values) {
		for (int i = 0; i < values.length; i++) {
			Object element = values[i];
			stream.print(escape(toString(element)));
			if (i + 1 < values.length)
				stream.print(getSeparator());
		}
		stream.println();
	}

	public String getSeparator() {
		return " ";
	}

	protected String escape(String element) {
		return element;
	}

	protected String toString(Object element) {
		if (element == null)
			return "";
		return element.toString();
	}

	protected boolean printLocation = true;

	public void printLocationHeader() {
		// nothing
	}

	public void printLocationFooter() {
		// nothing
	}

	@Override
	public String export(Iterable<T> cards) {
		printLocation = false;
		return super.export(cards);
	}

	public void printHeader() {
		if (header) {
			printLine(columns);
		}
	}

	public void printFooter() {
		// nothing
	}

	public ICardField[] deterimeColumns() {
		Collection<ICardField> fields = new ArrayList<ICardField>();
		for (T card : store) {
			Collection<ICardField> allFields;
			if (card instanceof MagicCardPhysical) {
				allFields = Arrays.asList(MagicCardField.allNonTransientFields(true));
			} else if (card instanceof MagicCard) {
				allFields = Arrays.asList(MagicCardField.allNonTransientFields(false));
			} else
				continue;
			for (ICardField field : allFields) {
				if (isForExport(field))
					fields.add(field);
			}
			break;
		}
		return fields.toArray(new ICardField[fields.size()]);
	}

	protected boolean isForExport(ICardField field) {
		return !field.isTransient();
	}

	public String escapeQuot(String str) {
		// fields containing " must be in quotes and all " changed to ""
		if (str.indexOf('"') >= 0) {
			return "\"" + str.replaceAll("\"", "\"\"") + "\"";
		}
		// fields containing carriage return must be surrounded by double quotes
		if (str.indexOf('\n') >= 0)
			return "\"" + str + "\"";
		// fields that contain separator must be surrounded by double quotes
		if (str.indexOf(getSeparator()) >= 0)
			return "\"" + str + "\"";
		// fields starts or ends with spaces must be in double quotes
		if (str.startsWith(" ") || str.endsWith(" "))
			return "\"" + str + "\"";
		return str;
	}
}
