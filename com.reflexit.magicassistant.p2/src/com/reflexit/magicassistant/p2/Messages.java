/*******************************************************************************
 * Copyright (c) 2007, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.reflexit.magicassistant.p2;

import org.eclipse.osgi.util.NLS;

/**
 * Message class for provisioning UI messages.
 * 
 * @since 3.4
 */
public class Messages extends NLS {
	private static final String BUNDLE_NAME = "com.reflexit.magicassistant.p2.messages"; //$NON-NLS-1$
	static {
		// load message values from bundle file
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}
	public static String Handler_CannotLaunchUI;
	public static String Handler_SDKUpdateUIMessageTitle;
	public static String InstallNewSoftwareHandler_ProgressTaskName;
	public static String PreferenceInitializer_Error;
	public static String ProvisioningPreferencePage_AlwaysOpenWizard;
	public static String ProvisioningPreferencePage_BrowsingPrefsGroup;
	public static String ProvisioningPreferencePage_ShowLatestVersions;
	public static String ProvisioningPreferencePage_ShowAllVersions;
	public static String ProvisioningPreferencePage_NeverOpenWizard;
	public static String ProvisioningPreferencePage_OpenWizardIfInvalid;
	public static String ProvisioningPreferencePage_PromptToOpenWizard;
	public static String SDKPolicy_PrefPageName;
	public static String UpdateHandler_NoSitesMessage;
	public static String UpdateHandler_NoSitesTitle;
	public static String UpdateHandler_ProgressTaskName;
}
