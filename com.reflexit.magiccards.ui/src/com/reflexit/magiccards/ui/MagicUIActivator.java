package com.reflexit.magiccards.ui;

import org.eclipse.core.runtime.Status;
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
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
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
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}

	public static void log(Throwable e) {
		getDefault().getLog().log(
		        new Status(Status.ERROR, MagicUIActivator.getDefault().getBundle().getSymbolicName(), 1,
		                e.getMessage(), e));
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
}
