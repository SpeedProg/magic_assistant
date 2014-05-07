package com.reflexit.magiccards.ui.utils;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ProgressMonitorWrapper;

import com.reflexit.magiccards.core.monitor.ICoreProgressMonitor;

public class CoreMonitorAdapter extends ProgressMonitorWrapper implements ICoreProgressMonitor {
	public CoreMonitorAdapter(IProgressMonitor monitor) {
		super(monitor);
	}
}
