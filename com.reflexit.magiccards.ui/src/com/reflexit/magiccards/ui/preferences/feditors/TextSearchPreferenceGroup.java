package com.reflexit.magiccards.ui.preferences.feditors;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import com.reflexit.magiccards.core.model.CardTypes;
import com.reflexit.magiccards.core.model.FilterField;
import com.reflexit.magiccards.core.model.Languages;
import com.reflexit.magiccards.ui.widgets.ContextAssist;

public class TextSearchPreferenceGroup extends MFieldEditorPreferencePage {
	private Collection<String> ids = new ArrayList<String>(6);

	@Override
	public Collection<String> getIds() {
		return ids;
	}

	// private Group group;
	@Override
	protected void createFieldEditors() {
		// this.group = new Group(getFieldEditorParent(), SWT.NONE);
		// this.group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		// this.group.setText("Subtype");
		// Composite parent = this.group;
		// addCheckBox("Any", parent);
		String id;
		id = FilterField.NAME_LINE.getPrefConstant();
		getPreferenceStore().setDefault(id, "");
		ids.add(id);
		StringFieldEditor nameSfe = new StringFieldEditor(id, "Name", getFieldEditorParent());
		addField(nameSfe);
		String toolTip = "Search expression can contain words separated by spaces,\n"
				+ "which would be searched using AND connector.\n" //
				+ "Adding '-' in front of the word makes it NOT.\n"
				+ "Special symbols can be search using {X} type syntax (i.e. {T} for tap).\n" //
				+ "See help for details.";
		addTooltip(nameSfe, toolTip);
		// type
		String typeId = FilterField.TYPE_LINE.getPrefConstant();
		getPreferenceStore().setDefault(typeId, "");
		StringFieldEditor sfe = new StringFieldEditor(typeId, "Type", getFieldEditorParent());
		addContextAssist(sfe, CardTypes.getProposals());
		addField(sfe);
		addTooltip(sfe, toolTip);
		ids.add(typeId);
		// text
		String textId = FilterField.TEXT_LINE.getPrefConstant();
		getPreferenceStore().setDefault(textId, "");
		StringFieldEditor textSfe = new StringFieldEditor(textId, "Text", getFieldEditorParent());
		addContextAssist(textSfe, getTextProposals());
		addField(textSfe);
		addTooltip(textSfe, toolTip);
		ids.add(textId);
		// artist
		String artistId = FilterField.ARTIST.getPrefConstant();
		getPreferenceStore().setDefault(artistId, "");
		StringFieldEditor artistSfe = new StringFieldEditor(artistId, "Artist", getFieldEditorParent());
		addField(artistSfe);
		addTooltip(artistSfe, toolTip);
		ids.add(artistId);
		// language
		String langId = FilterField.LANG.getPrefConstant();
		getPreferenceStore().setDefault(langId, "");
		String[][] langs;
		String[] langValues = Languages.getInstance().getLangValues();
		langs = new String[langValues.length + 1][2];
		langs[0][0] = langs[0][1] = "";
		for (int i = 0; i < langs.length - 1; i++) {
			langs[i + 1][0] = langs[i + 1][1] = langValues[i];
		}
		ComboFieldEditor langSfe = new ComboFieldEditor(langId, "Language", langs, getFieldEditorParent());
		addField(langSfe);
		ids.add(langId);
	}

	static String[] textProposals = new String[] { "Flying", "Haste", "Persist", "Wither", "Lifelink",
			"First Strike", "Double Strike",
			"Protection", "Reach", "Deathtouch", "Unblockable", "Fear", "Changeling", "Trample", "Vigilance", };

	/**
	 * TODO: refactor
	 * 
	 * @return
	 */
	private String[] getTextProposals() {
		// TODO Auto-generated method stub
		return textProposals;
	}

	private void addContextAssist(StringFieldEditor sfe, String[] proposals) {
		Text t = sfe.getTextControl(getFieldEditorParent());
		ContextAssist.addContextAssist(t, proposals, true);
	}

	@Override
	protected void adjustGridLayout() {
		GridLayout layout = (GridLayout) ((Composite) this.getControl()).getLayout();
		layout.marginHeight = 5;
		layout.marginWidth = 5;
		super.adjustGridLayout();
	}
}
