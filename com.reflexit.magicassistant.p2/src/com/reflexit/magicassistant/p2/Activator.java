package com.reflexit.magicassistant.p2;

import org.eclipse.equinox.p2.ui.Policy;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {
	// The plug-in ID
	public static final String PLUGIN_ID = "com.reflexit.magicassistant.p2"; //$NON-NLS-1$
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
		registerP2Policy(context);
		// getPreferenceStore().addPropertyChangeListener(getPreferenceListener());
	}

	private void registerP2Policy(BundleContext context) {
		MaPolicy policy = new MaPolicy();
		context.registerService(Policy.class.getName(), policy, null);
	}

	public class MaPolicy extends Policy {
		public MaPolicy() {
			if (System.getProperty("ma.showrepo") != null) {
				setRepositoriesVisible(true);
			}
			setRestartPolicy(RESTART_POLICY_PROMPT);
			setShowDrilldownRequirements(true);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
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
	public static Activator getDefault() {
		return plugin;
	}
}
