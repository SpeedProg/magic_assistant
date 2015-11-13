package com.reflexit.magiccards.core.model;

import java.util.Arrays;
import com.reflexit.magiccards.core.model.abs.ICardField;

public class GroupOrder implements Cloneable {
	private ICardField fields[];
	private final String label;
	private final String key;

	public GroupOrder(ICardField... fields) {
		this(null, fields);
	}

	public GroupOrder(String label, ICardField... fields) {
		if (fields != null && fields.length > 0) {
			this.fields = Arrays.copyOf(fields, fields.length);
			this.key = createGroupKey();
			if (label == null) {
				label = "";
				for (ICardField f : fields) {
					if (!label.isEmpty())
						label += "/";
					label += f.getLabel();
				}
			}
			this.label = label;
		} else {
			this.fields = new ICardField[0];
			this.key = "";
			this.label = label == null ? "None" : label;
		}
	}

	@Override
	public String toString() {
		return key;
	}

	public String getLabel() {
		return label;
	}

	@Override
	public int hashCode() {
		return key.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof GroupOrder))
			return false;
		GroupOrder other = (GroupOrder) obj;
		if (!key.equals(other.key))
			return false;
		return true;
	}

	public GroupOrder(String key) {
		this(getGroupFieldsByName(key));
	}

	private static ICardField[] getGroupFieldsByName(String name) {
		if (name == null || name.length() == 0)
			return null;
		String sfields[] = name.split("/");
		ICardField[] res = new ICardField[sfields.length];
		for (int i = 0; i < res.length; i++) {
			res[i] = MagicCardField.fieldByName(sfields[i]);
		}
		return res;
	}

	public ICardField[] getFields() {
		return fields;
	}

	public String getKey() {
		return key;
	}

	private String createGroupKey() {
		String res = "";
		for (int i = 0; i < fields.length; i++) {
			ICardField field = fields[i];
			if (field == null)
				continue;
			if (i != 0) {
				res += "/";
			}
			res += createGroupKey(field);
		}
		return res;
	}

	public static String createGroupKey(ICardField field) {
		return field.toString();
	}

	public static String createGroupKey(ICardField[] defGroup) {
		return new GroupOrder(defGroup).getKey();
	}

	@Override
	protected Object clone() {
		GroupOrder ret;
		try {
			ret = (GroupOrder) super.clone();
			ret.fields = Arrays.copyOf(fields, fields.length);
			return ret;
		} catch (CloneNotSupportedException e) {
			// not happening
			throw new RuntimeException(e);
		}
	}

	public ICardField getTop() {
		if (fields.length == 0)
			return null;
		return fields[0];
	}

	public int getPosition(ICardField field1) {
		for (int i = 0; i < fields.length; i++) {
			ICardField field = fields[i];
			if (field == field1)
				return i;
		}
		return -1;
	}

	public boolean isGroupped() {
		return fields.length > 0;
	}

	public void sortByGroupOrder(SortOrder order) {
		for (int i = fields.length - 1; i >= 0; i--) {
			ICardField field = fields[i];
			order.setSortField(field, order.isAccending(field));
		}
	}
}
