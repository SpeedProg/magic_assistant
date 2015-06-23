package com.reflexit.magiccards.ui.preferences.feditors;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import com.reflexit.magiccards.core.model.FilterField;
import com.reflexit.magiccards.core.model.SpecialTags;
import com.reflexit.magiccards.ui.widgets.ContextAssist;

public class UserFieldsPreferenceGroup extends MFieldEditorPreferencePage {
	private Collection<String> ids = new ArrayList<String>(6);

	@Override
	public Collection<String> getIds() {
		return ids;
	}

	// private Group group;
	@Override
	protected void createFieldEditors() {
		String id = FilterField.PRICE.getPrefConstant();
		getPreferenceStore().setDefault(id, "0");
		addField(new NumericalComparisonFieldEditor(id, "User Price", getFieldEditorParent()));
		ids.add(id);
		// count
		id = FilterField.COUNT.getPrefConstant();
		getPreferenceStore().setDefault(id, "0");
		addField(new NumericalComparisonFieldEditor(id, "Count", getFieldEditorParent()));
		ids.add(id);
		// for sale
		id = FilterField.FORTRADECOUNT.getPrefConstant();
		getPreferenceStore().setDefault(id, "0");
		addField(new NumericalComparisonFieldEditor(id, "For Trade", getFieldEditorParent()));
		ids.add(id);
		// comment
		id = FilterField.COMMENT.getPrefConstant();
		getPreferenceStore().setDefault(id, "");
		StringFieldEditor nameSfe = new StringFieldEditor(id, "Comment", getFieldEditorParent());
		addField(nameSfe);
		String toolTip = "Search expression can contain words separated by spaces,\n"
				+ "which would be searched using AND connector.\n" //
				+ "Adding '-' in front of the word makes it NOT.\n";
		addTooltip(nameSfe, toolTip);
		ids.add(id);
		// special
		id = FilterField.SPECIAL.getPrefConstant();
		getPreferenceStore().setDefault(id, "");
		StringFieldEditor var = new StringFieldEditor(id, "Special Tags", getFieldEditorParent());
		addField(var);
		addTooltip(var, "Card tags, i.e. foil, premium, mint, online, etc\n" + toolTip);
		Text tags = var.getTextControl(getFieldEditorParent());
		ContextAssist.addContextAssist(tags, SpecialTags.getTags(), true);
		ids.add(id);
		// ownership
		id = FilterField.OWNERSHIP.getPrefConstant();
		RadioGroupFieldEditor radios = new RadioGroupFieldEditor(id, "Ownership", 1, new String[][] {
				{ "Show all cards", "", },
				{ "Show only own cards (determined by ownership attribute)", "true", },
				{ "Show only virtual cards", "false", }, },
				getFieldEditorParent(), true);
		// getPreferenceStore().setDefault(id, Boolean.FALSE);
		// BooleanFieldEditor x = new BooleanFieldEditor(id, "Only Own", getFieldEditorParent());
		addField(radios);
		ids.add(id);
	}

	@Override
	protected void adjustGridLayout() {
		GridLayout layout = (GridLayout) ((Composite) this.getControl()).getLayout();
		layout.marginHeight = 5;
		layout.marginWidth = 5;
		super.adjustGridLayout();
	}
}
