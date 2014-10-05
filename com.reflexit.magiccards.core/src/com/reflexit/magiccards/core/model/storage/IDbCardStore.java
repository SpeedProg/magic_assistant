package com.reflexit.magiccards.core.model.storage;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

import com.reflexit.magiccards.core.monitor.ICoreProgressMonitor;
import com.reflexit.magiccards.core.monitor.ICoreRunnableWithProgress;

public interface IDbCardStore<T> extends ICardStore<T> {
	Collection<T> getCandidates(String name);

	T getPrime(String name);

	boolean isInitialized();

	/**
	 * Run update operation without saving into storage, up until the end
	 * 
	 * @param run
	 * @throws InterruptedException
	 * @throws InvocationTargetException
	 */
	public void updateOperation(ICoreRunnableWithProgress run, ICoreProgressMonitor monitor) throws
			InterruptedException;
}
