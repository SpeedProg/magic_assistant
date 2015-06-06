package com.reflexit.magiccards.ui.utils;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ProgressMonitorWrapper;
import org.eclipse.core.runtime.SubProgressMonitor;

import com.reflexit.magiccards.core.monitor.ICoreProgressMonitor;

public class CoreMonitorAdapter extends ProgressMonitorWrapper implements ICoreProgressMonitor {
	public CoreMonitorAdapter(IProgressMonitor monitor) {
		super(monitor);
	}

	public static CoreMonitorAdapter submon(IProgressMonitor monitor, int ticks) {
		return new CoreMonitorAdapter(new SubProgressMonitor(monitor, ticks));
	}

	public static CoreMonitorAdapter mon(IProgressMonitor monitor) {
		return new CoreMonitorAdapter(monitor);
	}
}
