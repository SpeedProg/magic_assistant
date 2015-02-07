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

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.SectionPart;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ImageHyperlink;
import org.eclipse.ui.forms.widgets.Section;

public class TSectionPart extends SectionPart {
	protected FormToolkit toolkit;

	public TSectionPart(IManagedForm managedForm, int style) {
		super(managedForm.getForm().getBody(), managedForm.getToolkit(), Section.TITLE_BAR | Section.TWISTIE
				| style);
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

	class PanelAction extends Action {
		PanelAction(String name) {
			super(name);
		}

		public PanelAction() {
		}

		@Override
		public void run() {
			try {
				if (execute())
					postExecute();
			} catch (Exception ex) {
				MessageDialog.openError(new Shell(), "Error", ex.getMessage());
			}
		}

		protected void postExecute() {
			reload();
		}

		public void attach(final Button control, final boolean checkText) {
			control.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					if (checkText == false || control.getText().equals(getText()))
						run();
				}
			});
		}

		public void attach(Combo control) {
			control.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					run();
				}
			});
			control.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					run();
				}
			});
		}

		public void attach(Text control) {
			control.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					run();
				}
			});
		}

		/**
		 * @return true if action successful and reload required
		 */
		protected boolean execute() {
			return false;
		}
	}

	protected void addHeaderButton(Section section, final IAction action) {
		ImageHyperlink info = new ImageHyperlink(section, SWT.NULL);
		toolkit.adapt(info, true, true);
		info.setImage(action.getImageDescriptor().createImage());
		info.addHyperlinkListener(new HyperlinkAdapter() {
			@Override
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

	protected void showError(String message) {
		MessageDialog.openError(getSection().getShell(), "Error", message);
	}

	protected FormText createLink(Composite sectionClient) {
		FormText formText = toolkit.createFormText(sectionClient, true);
		formText.setWhitespaceNormalized(true);
		//	formText.setImage("image", FormArticlePlugin.getDefault().getImageRegistry().get(FormArticlePlugin.IMG_SAMPLE));
		formText.setColor("header", toolkit.getColors().getColor(IFormColors.TITLE));
		formText.setFont("header", JFaceResources.getHeaderFont());
		formText.setFont("code", JFaceResources.getTextFont());
		return formText;
	}
}
