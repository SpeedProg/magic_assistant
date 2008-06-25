package com.reflexit.magiccards.core.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.reflexit.magiccards.core.Activator;
import com.reflexit.magiccards.core.MagicException;

/**
 * Class that implements IFilteredCardStore, it is only contains filtered filteredList
 * and no phisical media
 * @author Alena
 *
 * @param <T>
 */
public abstract class AbstractFilteredCardStore<T> implements IFilteredCardStore<T> {
	protected Collection<T> filteredList = null;
	protected boolean initialized = false;

	/* (non-Javadoc)
	 * @see com.reflexit.magiccards.core.model.IFilteredCardStore#getSize()
	 */
	public int getSize() {
		initialize();
		return getFilteredList().size();
	}

	public T getCard(int index) {
		initialize();
		return doGetCard(index);
	}

	protected synchronized final void initialize() {
		if (this.initialized == false) {
			try {
				doInitialize();
			} catch (MagicException e) {
				Activator.log(e);
			} finally {
				this.initialized = true;
			}
		}
	}

	protected void doInitialize() throws MagicException {
	}

	protected T doGetCard(int index) {
		Collection<T> l = getFilteredList();
		if (l instanceof List) {
			return ((List<T>) getFilteredList()).get(index);
		} else {
			throw new UnsupportedOperationException(l.getClass() + " is not direct access type");
		}
	}

	protected synchronized void addFilteredCard(T card) {
		getFilteredList().add(card);
	}

	protected synchronized void removeFilteredCard(T card) {
		getFilteredList().remove(card);
	}

	public Object[] getElements() {
		initialize();
		return getFilteredList().toArray();
	}

	public Object getElement(int index) {
		return getCard(index);
	}

	protected void resetFilteredList() {
		this.filteredList = null;
	}

	protected synchronized Collection<T> getFilteredList() {
		if (this.filteredList == null)
			this.filteredList = doCreateList();
		return this.filteredList;
	}

	protected Collection<T> doCreateList() {
		return new ArrayList<T>();
	}
}
