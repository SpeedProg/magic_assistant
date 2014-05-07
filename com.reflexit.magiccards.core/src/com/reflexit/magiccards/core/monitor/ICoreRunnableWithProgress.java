package com.reflexit.magiccards.core.monitor;

import java.lang.reflect.InvocationTargetException;

/**
 * This is the same as IRunnableWithProgress but do not require jface dependancy which is good
 * because core plugin may want to use monitors too
 */
public interface ICoreRunnableWithProgress {
	public void run(ICoreProgressMonitor monitor) throws InvocationTargetException, InterruptedException;
}
