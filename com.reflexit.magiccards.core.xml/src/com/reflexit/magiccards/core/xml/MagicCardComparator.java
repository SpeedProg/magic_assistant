package com.reflexit.magiccards.core.xml;

import java.util.Comparator;

import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCard;

public class MagicCardComparator implements Comparator {
	int sort = 0;
	int dir = 1;

	public MagicCardComparator(int sortIndex, boolean asc) {
		this.sort = sortIndex;
		this.dir = asc ? -1 : 1;
	}

	public int compare(Object o1, Object o2) {
		if (o1.equals(o2))
			return 0;
		if (o1 instanceof IMagicCard && o2 instanceof IMagicCard) {
			IMagicCard c1 = (IMagicCard) o1;
			IMagicCard c2 = (IMagicCard) o2;
			int by = this.sort;
			if (this.sort == IMagicCard.INDEX_COST) {
				by = IMagicCard.INDEX_CMC;
			}
			Object a1 = c1.getObjectByIndex(by);
			Object a2 = c2.getObjectByIndex(by);
			if (this.sort == IMagicCard.INDEX_POWER || this.sort == IMagicCard.INDEX_TOUGHNESS) {
				float f1 = MagicCard.convertFloat((String) a1);
				float f2 = MagicCard.convertFloat((String) a2);
				int d = Float.compare(f1, f2);
				if (d != 0)
					return d;
			} else if (a1 instanceof Comparable) {
				int range = ((Comparable) a1).compareTo(a2);
				if (range != 0)
					return range * this.dir;
			}
		}
		return this.dir * (System.identityHashCode(o1) - System.identityHashCode(o2));
	}

	public static Comparator getComparator(int sortIndex, boolean asc) {
		return new MagicCardComparator(sortIndex, asc);
	}
}
