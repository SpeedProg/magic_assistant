package com.reflexit.magiccards.ui;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.DialogSettings;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.reflexit.magiccards.core.sync.CardCache;
import com.reflexit.magiccards.ui.preferences.PreferenceConstants;

/**
 * The activator class controls the plug-in life cycle
 */
public class MagicUIActivator extends AbstractUIPlugin {
	// The plug-in ID
	public static final String PLUGIN_ID = "com.reflexit.magiccards.ui";
	// The shared instance
	private static MagicUIActivator plugin;

	/**
	 * The constructor
	 */
	public MagicUIActivator() {
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext
	 * )
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		activateCoreSettings();
	}

	private void activateCoreSettings() {
		// start the Netwotk plugin to set proxy
		org.eclipse.ui.internal.net.Activator.getDefault();
		CardCache.setCahchingEnabled(getPluginPreferences().getBoolean(PreferenceConstants.CACHE_IMAGES));
		CardCache.setLoadingEnabled(getPluginPreferences().getBoolean(PreferenceConstants.LOAD_IMAGES));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext
	 * )
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
	public static MagicUIActivator getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given plug-in
	 * relative path
	 * 
	 * @param path
	 *            the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}

	public static void log(Throwable e) {
		log(getStatus(e));
	}

	public static void log(String s) {
		log(new Status(Status.ERROR, PLUGIN_ID, s));
	}

	public static void log(IStatus s) {
		if (plugin == null) {
			System.err.println("error: " + s.getMessage());
		} else {
			plugin.getLog().log(s);
		}
	}

	public Image getImage(String key) {
		ImageRegistry registry = getImageRegistry();
		Image image = registry.get(key);
		if (image == null) {
			ImageDescriptor descriptor = imageDescriptorFromPlugin(PLUGIN_ID, key);
			if (descriptor == null) {
				ISharedImages sharedImages = PlatformUI.getWorkbench().getSharedImages();
				return sharedImages.getImage(key);
			}
			registry.put(key, descriptor);
			image = registry.get(key);
		}
		return image;
	}

	/**
	 * @param string
	 * @param desc
	 * @return
	 */
	public Image getImage(String key, ImageDescriptor desc) {
		ImageRegistry registry = getImageRegistry();
		Image image = registry.get(key);
		if (image == null) {
			registry.put(key, desc);
			image = registry.get(key);
		}
		return image;
	}

	/**
	 * Puts image in a registy using key
	 * 
	 * @param key
	 * @param desc
	 * @return
	 */
	public Image getImage(String key, Image desc) {
		ImageRegistry registry = getImageRegistry();
		Image image = registry.get(key);
		if (image == null && desc != null) {
			registry.put(key, desc);
			image = registry.get(key);
		}
		return image;
	}

	public synchronized static void trace(String debugInfo) {
		Plugin plugin = getDefault();
		if (plugin != null && plugin.isDebugging()) {
			printMsg(debugInfo);
		} else {
			printMsg(debugInfo);
		}
	}

	private synchronized static void printMsg(String debugInfo) {
		String msg = "<" + PLUGIN_ID + ">: ";
		System.out.print(msg);
		while (debugInfo.length() > 200) {
			String partial = debugInfo.substring(0, 100);
			debugInfo = debugInfo.substring(100);
			System.out.println(partial + "\\"); //$NON-NLS-1$
		}
		System.out.println(debugInfo);
	}

	public IDialogSettings getDialogSettings(String id) {
		IPath path = getDefault().getStateLocation();
		String filename = path.append("settings.txt").toOSString();
		DialogSettings settings = new DialogSettings(id);
		if (new File(filename).exists()) {
			try {
				settings.load(filename);
			} catch (IOException e) {
				log(e);
			}
		} else {
			// empty settings
		}
		return settings;
	}

	public void saveDialogSetting(IDialogSettings dialogSettings) throws IOException {
		IPath path = getDefault().getStateLocation();
		String filename = path.append("settings.txt").toOSString();
		dialogSettings.save(filename);
	}

	public static IStatus getStatus(Throwable e) {
		return new Status(Status.ERROR, PLUGIN_ID, 1, e.getMessage(), e);
	}

	public void saveDialogSetting() {
		// TODO Auto-generated method stub
	}
}
