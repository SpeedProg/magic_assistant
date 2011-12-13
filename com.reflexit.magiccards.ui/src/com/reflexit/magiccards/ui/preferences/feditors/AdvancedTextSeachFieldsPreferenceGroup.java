package com.reflexit.magiccards.ui.preferences.feditors;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import com.reflexit.magiccards.core.model.FilterHelper;

public class AdvancedTextSeachFieldsPreferenceGroup extends MFieldEditorPreferencePage {
	private Collection<String> ids = new ArrayList<String>(6);

	// private Group group;
	@Override
	protected void createFieldEditors() {
		createTextField("Text", FilterHelper.TEXT_LINE);
		createTextField("Or", FilterHelper.TEXT_LINE_2);
		createTextField("Or", FilterHelper.TEXT_LINE_3);
		createTextField("Excluding", FilterHelper.TEXT_NOT_1);
		createTextField("Excluding", FilterHelper.TEXT_NOT_2);
		createTextField("Excluding", FilterHelper.TEXT_NOT_3);
	}

	private StringFieldEditor createTextField(String label, String id1) {
		String id = FilterHelper.getPrefConstant(id1, FilterHelper.TEXT_POSTFIX);
		ids.add(id);
		getPreferenceStore().setDefault(id, "");
		StringFieldEditor nameSfe = new StringFieldEditor(id, label, getFieldEditorParent());
		addField(nameSfe);
		String toolTip = "Search expression can contain words separated by spaces,\n" + "which would be searched using AND connector.\n" //
				+ "Special symbols can be search using {X} type syntax (i.e. {T} for tap).\n" //
				+ "See help for details.";
		addTooltip(nameSfe, toolTip);
		return nameSfe;
	}

	@Override
	protected void adjustGridLayout() {
		GridLayout layout = (GridLayout) ((Composite) this.getControl()).getLayout();
		layout.marginHeight = 5;
		layout.marginWidth = 5;
		super.adjustGridLayout();
	}

	@Override
	public Collection<String> getIds() {
		return ids;
	}
}
