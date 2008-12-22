package com.reflexit.magiccards.core.exports;

import org.eclipse.core.runtime.IProgressMonitor;

import java.lang.reflect.InvocationTargetException;

/**
 * This is the same as IRunnableWithProgress but do not require jface dependancy
 * which is good because core plugin may want to use monitors too
 */
public interface ICoreRunnableWithProgress {
	/**
	 * Runs this operation.  Progress should be reported to the given progress monitor.
	 * This method is usually invoked by an <code>IRunnableContext</code>'s <code>run</code> method,
	 * which supplies the progress monitor.
	 * A request to cancel the operation should be honored and acknowledged 
	 * by throwing <code>InterruptedException</code>.
	 *
	 * @param monitor the progress monitor to use to display progress and receive
	 *   requests for cancelation
	 * @exception InvocationTargetException if the run method must propagate a checked exception,
	 * 	it should wrap it inside an <code>InvocationTargetException</code>; runtime exceptions are automatically
	 *  wrapped in an <code>InvocationTargetException</code> by the calling context
	 * @exception InterruptedException if the operation detects a request to cancel, 
	 *  using <code>IProgressMonitor.isCanceled()</code>, it should exit by throwing 
	 *  <code>InterruptedException</code>
	 *
	 * @see IRunnableContext#run
	 */
	public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException;
}
