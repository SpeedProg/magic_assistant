package com.reflexit.magiccards.ui.dialogs;

import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.swt.widgets.Shell;

import com.reflexit.magiccards.core.model.GroupOrder;

public class MagicGroupsSelectoryDialog extends MagicFieldSelectorDialog {
	public MagicGroupsSelectoryDialog(Shell parentShell, String value) {
		super(parentShell, null);
		setPreferenceStore(new PreferenceStore());
		if (value != null) {
			String gvalue = value.replace(',', '/');
			GroupOrder groupOrder = new GroupOrder(gvalue);
			String key = groupOrder.getKey();
			getPreferenceStore().setDefault(getPreferenceId(), key.replace('/', ','));
		}
	}

	@Override
	protected String getLabelText() {
		return "Group By Fields and Order";
	}

	public String getValue() {
		String value = getPreferenceStore().getString(getPreferenceId());
		String gvalue = value.replace(',', '/');
		return gvalue;
	}
}
