package com.reflexit.magiccards.core.model;

import java.util.Comparator;

import com.reflexit.magiccards.core.legality.Format;

class MagicCardComparator implements Comparator {
	private ICardField field;
	private boolean accending;

	public MagicCardComparator(ICardField sortField, boolean accending) {
		if (sortField == null)
			throw new NullPointerException();
		this.field = sortField;
		this.accending = accending;
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
		ICardField sort = field;
		Object a1 = c1.get(sort);
		Object a2 = c2.get(sort);
		int d = 0;
		if (a1 != a2) {
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
							String co1 = (String) c1.get(MagicCardField.COST);
							String co2 = (String) c2.get(MagicCardField.COST);
							d = Colors.getColorType(co1).compareTo(Colors.getColorType(co2));
							if (d == 0) {
								int i1 = Colors.getColorSort(co1);
								int i2 = Colors.getColorSort(co2);
								d = i1 - i2;
							}
							break;
						}
						case POWER:
						case TOUGHNESS:
							float f1 = AbstractMagicCard.convertFloat((String) a1);
							float f2 = AbstractMagicCard.convertFloat((String) a2);
							d = Float.compare(f1, f2);
							break;
						case RARITY:
							d = Rarity.compare((String) a1, (String) a2);
							break;
						case COLLNUM:
							d = ((IMagicCard) c1).getCollectorNumberId() - ((IMagicCard) c2).getCollectorNumberId();
							if (d != 0)
								break;
							generic = true;
							break;
						case LEGALITY:
							Format fo1 = ((LegalityMap) a1).getFirstLegal();
							Format fo2 = ((LegalityMap) a2).getFirstLegal();
							if (fo1 == null && fo2 != null)
								d = 1;
							else if (fo2 == null && fo1 != null)
								d = -1;
							else if (fo1 == null && fo2 == null)
								d = 0;
							else
								d = fo1.ordinal() - fo2.ordinal();
							break;
						default:
							generic = true;
							break;
					}
				}
				if (generic) {
					if (a1 instanceof Comparable) {
						d = ((Comparable) a1).compareTo(a2);
					}
				}
			}
		}
		if (d == 0) { // secondary key
			if (sort == MagicCardField.CMC) {
				int d1 = Colors.getColorSort((String) c1.get(MagicCardField.COST));
				int d2 = Colors.getColorSort((String) c2.get(MagicCardField.COST));
				d = d1 - d2;
			} else if (sort == MagicCardField.OWN_COUNT) {
				if (c1 instanceof MagicCardPhysical && c2 instanceof MagicCardPhysical) {
					d = ((MagicCardPhysical) c1).getOwnTotal() - ((MagicCardPhysical) c2).getOwnTotal();
				}
			}
		}
		if (d != 0)
			return dir * d;
		return 0;
	}
}