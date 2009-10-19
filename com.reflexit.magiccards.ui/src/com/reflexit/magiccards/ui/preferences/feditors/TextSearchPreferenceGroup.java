package com.reflexit.magiccards.ui.preferences.feditors;

import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.SimpleContentProposalProvider;
import org.eclipse.jface.fieldassist.TextContentAdapter;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

import com.reflexit.magiccards.core.model.CardTypes;
import com.reflexit.magiccards.core.model.FilterHelper;

public class TextSearchPreferenceGroup extends MFieldEditorPreferencePage {
	//private Group group;
	@Override
	protected void createFieldEditors() {
		//		this.group = new Group(getFieldEditorParent(), SWT.NONE);
		//		this.group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		//		this.group.setText("Subtype");
		//		Composite parent = this.group;
		// addCheckBox("Any", parent);
		String id;
		id = FilterHelper.getPrefConstant(FilterHelper.NAME_LINE, FilterHelper.TEXT_POSTFIX);
		getPreferenceStore().setDefault(id, "");
		StringFieldEditor nameSfe = new StringFieldEditor(id, "Name", getFieldEditorParent());
		addField(nameSfe);
		String toolTip = "Search expression can contain words separated by spaces,\n"
		        + "which would be searched using AND connector.\n" // 
		        + "Adding '-' in front of the word makes it NOT.\n"
		        + "Special symbols can be search using {X} type syntax (i.e. {T} for tap).\n" // 
		        + "See help for details.";
		addTooltip(nameSfe, toolTip);
		// type
		String typeId = FilterHelper.getPrefConstant(FilterHelper.TYPE_LINE, FilterHelper.TEXT_POSTFIX);
		getPreferenceStore().setDefault(typeId, "");
		StringFieldEditor sfe = new StringFieldEditor(typeId, "Type", getFieldEditorParent());
		addContextAssist(sfe, CardTypes.getProposals());
		addField(sfe);
		addTooltip(sfe, toolTip);
		// text
		String textId = FilterHelper.getPrefConstant(FilterHelper.TEXT_LINE, FilterHelper.TEXT_POSTFIX);
		getPreferenceStore().setDefault(textId, "");
		StringFieldEditor textSfe = new StringFieldEditor(textId, "Text", getFieldEditorParent());
		addContextAssist(textSfe, getTextProposals());
		addField(textSfe);
		addTooltip(textSfe, toolTip);
	}
	static String[] textProposals = new String[] {
	        "Flying",
	        "Haste",
	        "Persist",
	        "Wither",
	        "Lifelink",
	        "First Strike",
	        "Double Strike",
	        "Protection",
	        "Reach",
	        "Deathtouch",
	        "Unblockable",
	        "Fear",
	        "Changeling",
	        "Trample",
	        "Vigilance", };

	/**
	 * TODO: refactor 
	 * @return
	 */
	private String[] getTextProposals() {
		// TODO Auto-generated method stub
		return textProposals;
	}

	private void addContextAssist(StringFieldEditor sfe, String[] proposals) {
		Text t = sfe.getTextControl(getFieldEditorParent());
		SimpleContentProposalProvider proposalProvider = new SimpleContentProposalProvider(proposals) {
			/* (non-Javadoc)
			 * @see org.eclipse.jface.fieldassist.SimpleContentProposalProvider#getProposals(java.lang.String, int)
			 */
			@Override
			public IContentProposal[] getProposals(String contents, int position) {
				int k = contents.lastIndexOf(' ');
				if (k >= 0) {
					return super.getProposals(contents.substring(k + 1), position);
				}
				return super.getProposals(contents, position);
			}
		};
		proposalProvider.setFiltering(true);
		TextContentAdapter controlContentAdapter = new TextContentAdapter() {
			@Override
			public void insertControlContents(Control control, String text, int cursorPosition) {
				Text textCon = (Text) control;
				Point selection = textCon.getSelection();
				String old = textCon.getText();
				int k = old.lastIndexOf(' ');
				int l = old.length() - k - 1;
				textCon.insert(text.substring(l));
				// Insert will leave the cursor at the end of the inserted text. If this
				// is not what we wanted, reset the selection.
				if (cursorPosition < text.length()) {
					textCon.setSelection(selection.x + cursorPosition, selection.x + cursorPosition);
				}
			}
		};
		ContentProposalAdapter adapter = new ContentProposalAdapter(t, controlContentAdapter, proposalProvider, null,
		        null);
	}

	@Override
	protected void adjustGridLayout() {
		GridLayout layout = (GridLayout) ((Composite) this.getControl()).getLayout();
		layout.marginHeight = 5;
		layout.marginWidth = 5;
		super.adjustGridLayout();
	}
}
