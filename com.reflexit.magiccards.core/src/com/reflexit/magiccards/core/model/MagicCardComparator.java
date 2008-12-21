package com.reflexit.magiccards.core.model;

import java.util.Comparator;

public class MagicCardComparator implements Comparator {
	int sort = 0;
	int dir = 1;

	public MagicCardComparator(int sortIndex, boolean asc) {
		this.sort = sortIndex;
		this.dir = asc ? -1 : 1;
	}

	public int compare(Object o1, Object o2) {
		if (o1 == o2)
			return 0; // this is only case it is 0
		if (o1 instanceof IMagicCard && o2 instanceof IMagicCard) {
			IMagicCard c1 = (IMagicCard) o1;
			IMagicCard c2 = (IMagicCard) o2;
			int by = this.sort;
			Object a1 = c1.getObjectByIndex(by);
			Object a2 = c2.getObjectByIndex(by);
			if (this.sort == IMagicCard.INDEX_COST) {
				a1 = Colors.getColorName((String) a1);
				a2 = Colors.getColorName((String) a2);
			}
			if (this.sort == IMagicCard.INDEX_POWER || this.sort == IMagicCard.INDEX_TOUGHNESS) {
				float f1 = MagicCard.convertFloat((String) a1);
				float f2 = MagicCard.convertFloat((String) a2);
				int d = Float.compare(f1, f2);
				if (d != 0)
					return d;
			}
			if (this.sort == IMagicCard.INDEX_RARITY) {
				int d = Rarity.compare((String) a1, (String) a2);
				if (d != 0)
					return d;
			}
			if (a1 instanceof Comparable) {
				if (a2 == null)
					return this.dir;
				int range = ((Comparable) a1).compareTo(a2);
				if (range != 0)
					return range * this.dir;
			}
			if (c1.getCardId() != 0) {
				int idd = c1.getCardId() - c2.getCardId();
				if (idd != 0)
					return idd;
			}
		}
		return this.dir * (System.identityHashCode(o1) - System.identityHashCode(o2));
	}

	public static Comparator getComparator(int sortIndex, boolean asc) {
		return new MagicCardComparator(sortIndex, asc);
	}
}
