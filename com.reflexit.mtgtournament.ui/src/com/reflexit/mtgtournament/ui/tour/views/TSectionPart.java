/*******************************************************************************
 * Copyright (c) 2008 Alena Laskavaia.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alena Laskavaia - initial API and implementation
 *******************************************************************************/
package com.reflexit.mtgtournament.ui.tour.views;

import org.eclipse.jface.action.IAction;
import org.eclipse.swt.SWT;
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
		super(managedForm.getForm().getBody(), managedForm.getToolkit(), Section.TITLE_BAR | Section.TWISTIE | style);
		initialize(managedForm);
		this.toolkit = managedForm.getToolkit();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.forms.AbstractFormPart#commit(boolean)
	 */
	@Override
	public void commit(boolean onSave) {
		getManagedForm().commit(onSave);
	}

	protected void save() {
		markDirty();
		commit(false);
	}

	protected void addHeaderButton(Section section, final IAction action) {
		ImageHyperlink info = new ImageHyperlink(section, SWT.NULL);
		toolkit.adapt(info, true, true);
		info.setImage(action.getImageDescriptor().createImage());
		info.addHyperlinkListener(new HyperlinkAdapter() {
			public void linkActivated(HyperlinkEvent e) {
				action.run();
			}
		});
		info.setBackground(section.getTitleBarGradientBackground());
		section.setTextClient(info);
	}

	public void reload() {
		getManagedForm().setInput(getManagedForm().getInput());
		getManagedForm().refresh();
	}

	@Override
	public boolean setFormInput(Object input) {
		return super.setFormInput(input);
	}
}
