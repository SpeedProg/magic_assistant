package com.reflexit.mtgtournament.ui.tour.views;

import org.eclipse.swt.SWT;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.SectionPart;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ImageHyperlink;
import org.eclipse.ui.forms.widgets.Section;

public class TSectionPart extends SectionPart {
	protected FormToolkit toolkit;

	public TSectionPart(IManagedForm managedForm, int style) {
		super(managedForm.getForm().getBody(), managedForm.getToolkit(), Section.DESCRIPTION | Section.TITLE_BAR
		        | Section.TWISTIE | style);
		initialize(managedForm);
		this.toolkit = managedForm.getToolkit();
	}

	private void addHeaderButton(Section section) {
		ImageHyperlink info = new ImageHyperlink(section, SWT.NULL);
		toolkit.adapt(info, true, true);
		info.setImage(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_ELEMENT));
		info.addHyperlinkListener(new HyperlinkAdapter() {
			public void linkActivated(HyperlinkEvent e) {
				// do something here
			}
		});
		info.setBackground(section.getTitleBarGradientBackground());
		section.setTextClient(info);
	}

	@Override
	public boolean setFormInput(Object input) {
		return super.setFormInput(input);
	}
}
