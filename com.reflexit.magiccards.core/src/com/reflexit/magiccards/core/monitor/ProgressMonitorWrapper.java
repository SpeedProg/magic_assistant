package com.reflexit.magiccards.core.monitor;

/**
 * An abstract wrapper around a progress monitor which, unless overridden, forwards
 * <code>ICoreProgressMonitor</code> and <code>ICoreProgressMonitorWithBlocking</code> methods to
 * the wrapped progress monitor.
 * <p>
 * This class can be used without OSGi running.
 * </p>
 * <p>
 * Clients may subclass.
 * </p>
 */
public abstract class ProgressMonitorWrapper implements ICoreProgressMonitor {
	/** The wrapped progress monitor. */
	private ICoreProgressMonitor progressMonitor;

	/**
	 * Creates a new wrapper around the given monitor.
	 * 
	 * @param monitor
	 *            the progress monitor to forward to
	 */
	protected ProgressMonitorWrapper(ICoreProgressMonitor monitor) {
		progressMonitor = monitor;
	}

	/**
	 * This implementation of a <code>ICoreProgressMonitor</code> method forwards to the wrapped
	 * progress monitor. Clients may override this method to do additional processing.
	 * 
	 * @see ICoreProgressMonitor#beginTask(String, int)
	 */
	@Override
	public void beginTask(String name, int totalWork) {
		progressMonitor.beginTask(name, totalWork);
	}

	/**
	 * This implementation of a <code>ICoreProgressMonitor</code> method forwards to the wrapped
	 * progress monitor. Clients may override this method to do additional processing.
	 * 
	 * @see ICoreProgressMonitor#done()
	 */
	@Override
	public void done() {
		progressMonitor.done();
	}

	/**
	 * Returns the wrapped progress monitor.
	 * 
	 * @return the wrapped progress monitor
	 */
	public ICoreProgressMonitor getWrappedProgressMonitor() {
		return progressMonitor;
	}

	/**
	 * This implementation of a <code>ICoreProgressMonitor</code> method forwards to the wrapped
	 * progress monitor. Clients may override this method to do additional processing.
	 * 
	 * @see ICoreProgressMonitor#internalWorked(double)
	 */
	@Override
	public void internalWorked(double work) {
		progressMonitor.internalWorked(work);
	}

	/**
	 * This implementation of a <code>ICoreProgressMonitor</code> method forwards to the wrapped
	 * progress monitor. Clients may override this method to do additional processing.
	 * 
	 * @see ICoreProgressMonitor#isCanceled()
	 */
	@Override
	public boolean isCanceled() {
		return progressMonitor.isCanceled();
	}

	/**
	 * This implementation of a <code>ICoreProgressMonitor</code> method forwards to the wrapped
	 * progress monitor. Clients may override this method to do additional processing.
	 * 
	 * @see ICoreProgressMonitor#setCanceled(boolean)
	 */
	@Override
	public void setCanceled(boolean b) {
		progressMonitor.setCanceled(b);
	}

	/**
	 * This implementation of a <code>ICoreProgressMonitor</code> method forwards to the wrapped
	 * progress monitor. Clients may override this method to do additional processing.
	 * 
	 * @see ICoreProgressMonitor#setTaskName(String)
	 */
	@Override
	public void setTaskName(String name) {
		progressMonitor.setTaskName(name);
	}

	/**
	 * This implementation of a <code>ICoreProgressMonitor</code> method forwards to the wrapped
	 * progress monitor. Clients may override this method to do additional processing.
	 * 
	 * @see ICoreProgressMonitor#subTask(String)
	 */
	@Override
	public void subTask(String name) {
		progressMonitor.subTask(name);
	}

	/**
	 * This implementation of a <code>ICoreProgressMonitor</code> method forwards to the wrapped
	 * progress monitor. Clients may override this method to do additional processing.
	 * 
	 * @see ICoreProgressMonitor#worked(int)
	 */
	@Override
	public void worked(int work) {
		progressMonitor.worked(work);
	}
}
