/*******************************************************************************
 * Copyright (c) 2008 Alena Laskavaia.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alena Laskavaia - initial API and implementation
 *******************************************************************************/
package com.reflexit.magiccards.core;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.BundleContext;

import com.reflexit.magiccards.core.exports.ImportExportFactory;

/**
 * The activator class controls the plug-in life cycle
 * Do not use class in the core code, I want to keep it eclipse free
 */
@Deprecated
public class Activator extends Plugin {
	// The plug-in ID
	static final String PLUGIN_ID = DataManager.ID;
	// The shared instance
	private static Activator plugin;
	private boolean TRACE_CORE = false;
	private boolean TRACE_PERF = false;

	/**
	 * The constructor
	 */
	public Activator() {
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.core.runtime.Plugins#start(org.osgi.framework.BundleContext)
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		TRACE_CORE = isDebugging();
		TRACE_PERF = isDebugging()
				&& "true".equalsIgnoreCase(Platform.getDebugOption(PLUGIN_ID + "/debug/profiling"));
		MagicLogger.setTracing(TRACE_PERF);
		MagicLogger.setDebugging(TRACE_CORE);
		MagicLogger.info("Magic Assistant started. Version " + plugin.getBundle().getVersion());
		new Thread("Loading database") {
			@Override
			public void run() {
				ImportExportFactory.getImportTypes(); // load extensions for report types
				DataManager.getInstance().asyncInitDb();
			};
		}.start();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	static void log(String message) {
		if (getDefault() == null) {
			System.err.println(message);
		} else
			getDefault().getLog().log(new Status(IStatus.ERROR, PLUGIN_ID, message));
	}

	static void info(String message) {
		if (getDefault() == null) {
			System.out.println(message);
		} else
			getDefault().getLog().log(new Status(IStatus.INFO, PLUGIN_ID, message));
	}

	static void log(Throwable e) {
		if (getDefault() == null) {
			e.printStackTrace();
		} else
			getDefault().getLog().log(new Status(IStatus.ERROR, PLUGIN_ID, 1, e.getMessage(), e));
	}
}
