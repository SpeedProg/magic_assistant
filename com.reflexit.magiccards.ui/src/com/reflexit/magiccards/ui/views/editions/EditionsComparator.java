package com.reflexit.magiccards.ui.views.editions;

import java.util.Comparator;
import java.util.Date;

import com.reflexit.magiccards.core.model.Edition;

public class EditionsComparator implements Comparator<Edition> {
	private EditionField field;
	private int dir;

	public EditionsComparator(EditionField field, boolean asc) {
		this.field = field;
		this.dir = asc ? -1 : 1;
	}

	public int compare(Edition a1, Edition a2, EditionField field, int dir) {
		if (a1 == a2)
			return 0; // this is only case it is 0
		int d = 0;
		switch (field) {
			case NAME:
				d = a1.getName().compareTo(a2.getName());
				break;
			case DATE:
				Date r1 = a1.getReleaseDate();
				Date r2 = a2.getReleaseDate();
				if (r1 == null && r2 == null)
					d = 0;
				else if (r1 == null && r2 != null)
					d = 1;
				else if (r1 != null && r2 == null)
					d = -1;
				else if (r1 != null && r2 != null) {
					d = r1.compareTo(r2);
				}
				break;
			case TYPE: {
				String t1 = a1.getType();
				String t2 = a2.getType();
				if (t1 != null && t2 != null) {
					d = t1.compareTo(t2);
				}
				break;
			}
			case BLOCK: {
				String t1 = a1.getBlock();
				String t2 = a2.getBlock();
				if (t1 != null && t2 != null) {
					d = t1.compareTo(t2);
				}
				break;
			}
			case FORMAT: {
				d = a1.getLegalityMap().compareTo(a2.getLegalityMap());
				break;
			}
			case ALIASES: {
				d = a1.getExtraAliases().compareTo(a2.getExtraAliases());
				break;
			}
		}
		if (d == 0 && field != EditionField.DATE) {
			d = compare(a1, a2, EditionField.DATE, -1);
		}
		if (d == 0)
			d = (System.identityHashCode(a1) - System.identityHashCode(a2));
		return dir * d;
	}

	public static EditionsComparator getComparator(EditionField field, boolean asc) {
		return new EditionsComparator(field, asc);
	}

	@Override
	public int compare(Edition o1, Edition o2) {
		return compare(o1, o2, field, dir);
	}
}
