package com.reflexit.magiccards.db;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.osgi.framework.BundleContext;

public class DbActivator extends Plugin {
	// The plug-in ID
	public static final String PLUGIN_ID = "com.reflexit.magiccards.db";
	// The shared instance
	private static DbActivator plugin;
	// global preferences keys
	public static final String DB_LOADED = "db_loaded";

	/**
	 * The constructor
	 */
	public DbActivator() {
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.core.runtime.Plugins#start(org.osgi.framework.BundleContext)
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		getEclipsePreferences().flush();
		plugin = null;
		super.stop(context);
	}

	public IEclipsePreferences getEclipsePreferences() {
		// Platform.getPreferencesService().getInt();
		return new InstanceScope().getNode(PLUGIN_ID);
	}

	public IEclipsePreferences getEclipseDefaultPreferences() {
		return new DefaultScope().getNode(PLUGIN_ID);
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static DbActivator getDefault() {
		return plugin;
	}

	public static void log(Throwable e) {
		DbActivator pl = getDefault();
		if (pl == null) {
			e.printStackTrace();
		} else
			pl.getLog().log(new Status(Status.ERROR, DbActivator.getDefault().getBundle().getSymbolicName(), 1, e.getMessage(), e));
	}

	public IStatus getStatus(Throwable e) {
		return new Status(Status.ERROR, PLUGIN_ID, e.getMessage(), e);
	}
}
