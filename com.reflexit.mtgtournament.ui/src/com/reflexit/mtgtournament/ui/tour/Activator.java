package com.reflexit.mtgtournament.ui.tour;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {
	// The plug-in ID
	public static final String PLUGIN_ID = "com.reflexit.mtgtournament.ui";
	// The shared instance
	private static Activator plugin;

	/**
	 * The constructor
	 */
	public Activator() {
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

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

	public IDialogSettings getDialogSettings(String id) {
		IDialogSettings settings = getDialogSettings();
		IDialogSettings section = settings.getSection(id);
		if (section == null)
			section = settings.addNewSection(id);
		return section;
	}

	@Override
	public IDialogSettings getDialogSettings() {
		return super.getDialogSettings();
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

	public static IStatus getStatus(Throwable e) {
		return new Status(Status.ERROR, PLUGIN_ID, 1, e.getMessage(), e);
	}
}
