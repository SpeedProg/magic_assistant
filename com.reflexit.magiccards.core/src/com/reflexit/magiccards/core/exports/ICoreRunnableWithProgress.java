package com.reflexit.magiccards.core.exports;

import org.eclipse.core.runtime.IProgressMonitor;

import java.lang.reflect.InvocationTargetException;

/**
 * This is the same as IRunnableWithProgress but do not require jface dependancy
 * which is good because core plugin may want to use monitors too
 */
public interface ICoreRunnableWithProgress {
	public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException;
}
