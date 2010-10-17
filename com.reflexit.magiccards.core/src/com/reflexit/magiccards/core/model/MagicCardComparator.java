package com.reflexit.magiccards.core.model;

import java.util.Comparator;

public class MagicCardComparator implements Comparator {
	ICardField sort;
	int dir = 1;

	public MagicCardComparator(ICardField field, boolean asc) {
		this.sort = field;
		this.dir = asc ? -1 : 1;
	}

	public int compare(Object o1, Object o2) {
		if (o1 == o2)
			return 0; // this is only case it is 0
		if (o1 instanceof IMagicCard && o2 instanceof IMagicCard) {
			IMagicCard c1 = (IMagicCard) o1;
			IMagicCard c2 = (IMagicCard) o2;
			ICardField sby = this.sort;
			Object a1 = c1.getObjectByField(sby);
			Object a2 = c2.getObjectByField(sby);
			int d = 0;
			if (this.sort == MagicCardField.COST) {
				a1 = Colors.getColorName((String) a1);
				a2 = Colors.getColorName((String) a2);
			}
			if (a1 == null && a2 != null) {
				d = 1;
			} else if (a1 != null && a2 == null) {
				d = -1;
			} else if (this.sort == MagicCardField.POWER || this.sort == MagicCardField.TOUGHNESS) {
				float f1 = MagicCard.convertFloat((String) a1);
				float f2 = MagicCard.convertFloat((String) a2);
				d = Float.compare(f1, f2);
			} else if (this.sort == MagicCardField.RARITY) {
				d = Rarity.compare((String) a1, (String) a2);
			} else if (this.sort == MagicCardField.COLLNUM && a1 instanceof String) {
				String s1 = (String) a1;
				String s2 = (String) a2;
				try {
					d = Integer.valueOf(s1) - Integer.valueOf(s2);
				} catch (NumberFormatException e) {
					d = s1.compareTo(s2);
				}
			} else if (a1 instanceof Comparable) {
				if (a2 == null)
					d = 1;
				else
					d = ((Comparable) a1).compareTo(a2);
			}
			if (d == 0 && c1.getCardId() != 0) {
				d = c1.getCardId() - c2.getCardId();
			}
			if (d != 0)
				return this.dir * d;
		}
		return this.dir * (System.identityHashCode(o1) - System.identityHashCode(o2));
	}

	public static Comparator getComparator(ICardField field, boolean asc) {
		return new MagicCardComparator(field, asc);
	}
}
