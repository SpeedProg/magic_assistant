package com.reflexit.magiccards.core.model;

import java.util.Comparator;

import com.reflexit.magiccards.core.legality.Format;
import com.reflexit.magiccards.core.model.abs.ICard;
import com.reflexit.magiccards.core.model.abs.ICardField;

public class MagicCardComparator implements Comparator {
	private ICardField field;
	private boolean accending;

	public MagicCardComparator(ICardField sortField, boolean accending) {
		if (sortField == null)
			throw new NullPointerException();
		this.field = sortField;
		this.accending = accending;
	}

	public void reverse() {
		accending = !accending;
	}

	public ICardField getField() {
		return field;
	}

	public boolean isAccending() {
		return accending;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((field == null) ? 0 : field.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MagicCardComparator other = (MagicCardComparator) obj;
		if (field == null) {
			if (other.field != null)
				return false;
		} else if (!field.equals(other.field))
			return false;
		return true;
	}

	@Override
	public int compare(Object o1, Object o2) {
		if (o1 == o2)
			return 0;
		if (o1 instanceof ICard && o2 instanceof ICard) {
			ICard c1 = (ICard) o1;
			ICard c2 = (ICard) o2;
			return compare(c1, c2);
		}
		return 0;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public int compare(ICard c1, ICard c2) {
		if (c1 == c2)
			return 0;
		int dir = accending ? 1 : -1;
		if (c1.getClass() != c2.getClass())
			return dir * c1.getClass().getName().compareTo(c2.getClass().getName());
		int d = compare(c1, c2, field);
		if (d != 0)
			return dir * d;
		return 0;
	}

	protected int compare(ICard c1, ICard c2, ICardField sort) {
		Object a1 = c1.get(sort);
		Object a2 = c2.get(sort);
		if (a1 == null && a2 == null)
			return 0;
		int d = 0;
		if (a1 == null) {
			d = 1;
		} else if (a2 == null) {
			d = -1;
		} else {
			boolean generic = true;
			if (sort instanceof MagicCardField) {
				generic = false;
				switch ((MagicCardField) sort) {
				case COST: {
					int i1 = Colors.getColorSort((String) a1);
					int i2 = Colors.getColorSort((String) a2);
					d = i1 - i2;
					break;
				}
				case COLOR: {
					d = compare(c1, c2, MagicCardField.CTYPE);
					if (d == 0) {
						d = compare(c1, c2, MagicCardField.COST);
					}
					break;
				}
				case CMC: {
					if (a1 instanceof Integer && a2 instanceof Integer) {
						d = (Integer) a1 - (Integer) a2;
					} else {
						a1 = String.valueOf(a1);
						a2 = String.valueOf(a2);
						d = ((Comparable) a1).compareTo(a2);
					}
					if (d == 0) {
						d = compare(c1, c2, MagicCardField.CTYPE);
					}
					if (d == 0) {
						d = compare(c1, c2, MagicCardField.COST);
					}
					// System.err.println("cms sort " + c1 + ":" + a1 + " vs " +
					// c2 + ":" + a2 + " = " + d);
					break;
				}
				case CTYPE: {
					String ct1 = (String) a1;
					String ct2 = (String) a2;
					d = ct1.compareTo(ct2);
					if (d != 0) {
						if (ct1.equals("land"))
							d = -1;
						else if (ct2.equals("land"))
							d = 1;
					}
					break;
				}
				case COLOR_IDENTITY: {
					String co1 = (String) a1;
					String co2 = (String) a2;
					d = Colors.getColorType((String) a1).compareTo(Colors.getColorType((String) a2));
					if (d == 0) {
						int i1 = Colors.getColorSort(co1);
						int i2 = Colors.getColorSort(co2);
						d = i1 - i2;
					}
					break;
				}
				case POWER:
				case TOUGHNESS:
					if (a1 != a2) {
						float f1 = AbstractMagicCard.convertFloat((String) a1);
						float f2 = AbstractMagicCard.convertFloat((String) a2);
						d = Float.compare(f1, f2);
					}
					break;
				case RARITY:
					if (a1 != a2) {
						d = Rarity.compare((String) a1, (String) a2);
					}
					break;
				case COLLNUM:
					if (a1 != a2) {
						d = ((IMagicCard) c1).getCollectorNumberId() - ((IMagicCard) c2).getCollectorNumberId();
						if (d == 0)
							generic = true;
					}
					break;
				case LEGALITY:
					if (a1 != a2) {
						Format fo1 = ((LegalityMap) a1).getFirstLegal();
						Format fo2 = ((LegalityMap) a2).getFirstLegal();
						if (fo1 == null && fo2 != null)
							d = 1;
						else if (fo2 == null && fo1 != null)
							d = -1;
						else if (fo1 == null && fo2 == null)
							d = 0;
						else if (fo2 != null && fo1 != null)
							d = fo1.ordinal() - fo2.ordinal();
					}
					break;
				default:
					generic = true;
					break;
				}
			}
			if (a1 != a2 && d == 0) {
				if (generic) {
					if (a1 instanceof Comparable) {
						d = ((Comparable) a1).compareTo(a2);
					}
				}
			}
		}
		if (d == 0) { // secondary key
			if (sort == MagicCardField.OWN_COUNT) {
				if (c1 instanceof MagicCardPhysical && c2 instanceof MagicCardPhysical) {
					d = ((MagicCardPhysical) c1).getOwnTotal() - ((MagicCardPhysical) c2).getOwnTotal();
				}
			}
		}
		return d;
	}

	@Override
	public String toString() {
		return field.name() + (accending ? "^" : "v");
	}
}