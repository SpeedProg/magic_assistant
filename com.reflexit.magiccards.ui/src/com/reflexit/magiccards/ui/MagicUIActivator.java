package com.reflexit.magiccards.ui;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.dialogs.DialogSettings;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.preference.IPersistentPreferenceStore;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.activities.IActivityManager;
import org.eclipse.ui.activities.IWorkbenchActivitySupport;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.eclipse.ui.themes.ITheme;
import org.eclipse.ui.themes.IThemeManager;
import org.osgi.framework.BundleContext;

import com.reflexit.magiccards.core.Activator;
import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.sync.CurrencyConvertor;
import com.reflexit.magiccards.core.sync.WebUtils;
import com.reflexit.magiccards.ui.preferences.PreferenceConstants;
import com.reflexit.magiccards.ui.preferences.PriceProviderManager;

/**
 * The activator class controls the plug-in life cycle
 */
public class MagicUIActivator extends AbstractUIPlugin {
	// The plug-in ID
	public static final String PLUGIN_ID = "com.reflexit.magiccards.ui";
	// The shared instance
	private static MagicUIActivator plugin;
	public static boolean TRACE_EXPORT = false;
	public static boolean TRACE_UI = false;
	public static boolean TRACE_TESTING = false;
	public static Color COLOR_GREENISH;
	public static Color COLOR_PINKINSH;
	private IPersistentPreferenceStore preferenceStoreCore;

	/**
	 * The constructor
	 */
	public MagicUIActivator() {
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.
	 * BundleContext )
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		activateCoreSettings();
		TRACE_EXPORT = isDebugging() && "true".equalsIgnoreCase(Platform.getDebugOption(PLUGIN_ID + "/debug/export"));
		TRACE_UI = isDebugging() && "true".equalsIgnoreCase(Platform.getDebugOption(PLUGIN_ID + "/debug/ui"));
		TRACE_TESTING = (isDebugging()
				&& "true".equalsIgnoreCase(Platform.getDebugOption(PLUGIN_ID + "/debug/testing"))) || isJunitRunning();
		Device device = Display.getDefault();
		COLOR_GREENISH = new Color(device, 255 - 64, 255, 255 - 64);
		COLOR_PINKINSH = new Color(device, 255, 255 - 64, 255 - 64);
	}

	public static boolean isJunitRunning() {
		return Boolean.valueOf(System.getProperty("junit.testing"));
	}

	@SuppressWarnings({ "restriction" })
	private void activateCoreSettings() {
		// start the Network plugin to set proxy
		org.eclipse.ui.internal.net.Activator.getDefault();
		// load core activator to start db init
		Activator.getDefault();
		IPreferenceStore globalStore = getPreferenceStore();
		WebUtils.setWorkOffline(globalStore.getBoolean(PreferenceConstants.WORK_OFFLINE));
		DataManager.getInstance().setOwnCopyEnabled(globalStore.getBoolean(PreferenceConstants.OWNED_COPY));
		CurrencyConvertor.setCurrency(globalStore.getString(PreferenceConstants.CURRENCY));
		PriceProviderManager.getInstance().sync(globalStore);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.
	 * BundleContext )
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		if (preferenceStoreCore != null)
			preferenceStoreCore.save();
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

	public static Image getImage(String key) {
		if (getDefault() == null) {
			try {
				return new Image(Display.getDefault(), key);
			} catch (Throwable e) {
				return null;
			}
		}
		ImageRegistry registry = getDefault().getImageRegistry();
		Image image = registry.get(key);
		if (image == null) {
			ImageDescriptor descriptor = imageDescriptorFromPlugin(PLUGIN_ID, key);
			if (descriptor == null && Workbench.getInstance() != null) {
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

	public static String helpId(String string) {
		return "com.reflexit.magiccards.help." + string;
	}

	public Font getFont() {
		IThemeManager themeManager = PlatformUI.getWorkbench().getThemeManager();
		ITheme currentTheme = themeManager.getCurrentTheme();
		FontRegistry fontRegistry = currentTheme.getFontRegistry();
		Font font = fontRegistry.get("com.reflexit.magiccards.ui.preferences.font");
		return font;
	}

	public Color getTextColor() {
		IThemeManager themeManager = PlatformUI.getWorkbench().getThemeManager();
		ITheme currentTheme = themeManager.getCurrentTheme();
		ColorRegistry registry = currentTheme.getColorRegistry();
		Color color = registry.get("com.reflexit.magiccards.ui.preferences.color");
		return color;
	}

	public Color getBgColor(boolean own) {
		IThemeManager themeManager = PlatformUI.getWorkbench().getThemeManager();
		ITheme currentTheme = themeManager.getCurrentTheme();
		ColorRegistry registry = currentTheme.getColorRegistry();
		Color color = own ? registry.get("com.reflexit.magiccards.ui.preferences.ocard.bgcolor")
				: registry.get("com.reflexit.magiccards.ui.preferences.vcard.bgcolor");
		return color;
	}

	@Override
	public IPreferenceStore getPreferenceStore() {
		return super.getPreferenceStore();
	}

	public IPreferenceStore getCorePreferenceStore() {
		if (preferenceStoreCore == null) {
			preferenceStoreCore = new ScopedPreferenceStore(InstanceScope.INSTANCE, DataManager.ID);
		}
		return preferenceStoreCore;
	}

	public static String ACTIVITY_DB_EXTEND = "com.reflexit.magiccards.activity.dbextend";

	public static boolean isActivityEnabled(String id) {
		IWorkbenchActivitySupport activitySupport = PlatformUI.getWorkbench().getActivitySupport();
		IActivityManager activityManager = activitySupport.getActivityManager();
		return activityManager.getActivity(id).isEnabled();
	}

	public static void setActivityEnabled(String id, boolean enabled) {
		IWorkbenchActivitySupport activitySupport = PlatformUI.getWorkbench().getActivitySupport();
		Set<String> enabledActivities = new HashSet<String>();
		if (enabled)
			enabledActivities.add(id);
		else
			enabledActivities.remove(id);
		activitySupport.setEnabledActivityIds(enabledActivities);
	}

	public static Shell getShell() {
		Shell shell = null;
		try {
			shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		} catch (Exception e) {
		}
		if (shell != null)
			return shell;
		Display display = Display.getDefault();
		shell = display.getActiveShell();
		if (shell == null) {
			Shell[] shells = display.getShells();
			for (Shell shell1 : shells) {
				if (shell1.getShells().length == 0) {
					shell = shell1;
					break;
				}
			}
		}
		if (shell != null)
			return shell;
		return new Shell();
	}
}
