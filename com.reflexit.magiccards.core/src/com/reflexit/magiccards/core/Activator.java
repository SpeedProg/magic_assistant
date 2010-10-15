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

import java.io.File;
import java.io.IOException;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends Plugin {
	// The plug-in ID
	public static final String PLUGIN_ID = "com.reflexit.magiccards.core";
	// The shared instance
	private static Activator plugin;
	//global preferences keys
	public static final String DB_LOADED = "db_loaded";

	/**
	 * The constructor
	 */
	public Activator() {
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugins#start(org.osgi.framework.BundleContext)
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
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
	public static Activator getDefault() {
		return plugin;
	}

	public static void log(Throwable e) {
		Activator pl = getDefault();
		if (pl == null) {
			e.printStackTrace();
		} else
			pl.getLog()
			        .log(
			                new Status(Status.ERROR, Activator.getDefault().getBundle().getSymbolicName(), 1, e
			                        .getMessage(), e));
	}

	public static IPath getStateLocationAlways() {
		Activator pl = getDefault();
		IPath path = null;
		if (pl != null)
			path = pl.getStateLocation();
		if (path == null) {
			try {
				File temp = File.createTempFile(PLUGIN_ID, ".dir");
				temp.delete();
				temp = new File(temp.getParentFile(), PLUGIN_ID);
				temp.mkdir();
				//temp.deleteOnExit();
				return new Path(temp.getPath());
			} catch (IOException e) {
				return new Path("/tmp");
			}
		}
		return path;
	}


	public IStatus getStatus(Throwable e) {
		return new Status(Status.ERROR, PLUGIN_ID, e.getMessage(), e);
	}
}
