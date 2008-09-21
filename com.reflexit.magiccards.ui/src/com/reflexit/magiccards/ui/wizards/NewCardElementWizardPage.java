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
package com.reflexit.magiccards.ui.wizards;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogPage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import java.util.ArrayList;

import com.reflexit.magiccards.core.model.nav.CardElement;
import com.reflexit.magiccards.core.model.nav.CardOrganizer;
import com.reflexit.magiccards.ui.dialogs.CardNavigatorSelectionDialog;
import com.reflexit.magiccards.ui.views.nav.CardsNavigatorContentProvider;

/**
 * @author Alena
 *
 */
public abstract class NewCardElementWizardPage extends WizardPage {
	private Text containerText;
	private Text fileText;
	protected ISelection selection;

	/**
	 * @param pageName
	 */
	public NewCardElementWizardPage(ISelection selection) {
		super("wizardPage1");
		this.selection = selection;
		updateInitialSelection();
	}

	protected void updateInitialSelection() {
		if (this.selection != null && !this.selection.isEmpty()) {
		} else {
			this.selection = new StructuredSelection(getRootContainer());
		}
	}

	/**
	 * @param pageName
	 * @param title
	 * @param titleImage
	 */
	public NewCardElementWizardPage(String pageName, String title, ImageDescriptor titleImage) {
		super(pageName, title, titleImage);
	}

	/**
	 * @see IDialogPage#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		layout.numColumns = 3;
		layout.verticalSpacing = 9;
		Label label = new Label(container, SWT.NULL);
		label.setText("&Container:");
		this.containerText = new Text(container, SWT.BORDER | SWT.SINGLE);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		this.containerText.setLayoutData(gd);
		this.containerText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				dialogChanged();
			}
		});
		Button button = new Button(container, SWT.PUSH);
		button.setText("Browse...");
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				handleBrowse();
			}
		});
		label = new Label(container, SWT.NULL);
		label.setText("&Name:");
		this.fileText = new Text(container, SWT.BORDER | SWT.SINGLE);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		this.fileText.setLayoutData(gd);
		this.fileText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				dialogChanged();
			}
		});
		initialize();
		dialogChanged();
		setControl(container);
	}

	/**
	 * Tests if the current workbench selection is a suitable container to use.
	 */
	private void initialize() {
		if (this.selection != null && this.selection.isEmpty() == false
		        && this.selection instanceof IStructuredSelection) {
			IStructuredSelection ssel = (IStructuredSelection) this.selection;
			if (ssel.size() > 1)
				return;
			Object obj = ssel.getFirstElement();
			if (obj instanceof CardElement) {
				CardOrganizer container;
				if (obj instanceof CardOrganizer)
					container = (CardOrganizer) obj;
				else
					container = ((CardElement) obj).getParent();
				this.containerText.setText(container.getLocation());
			}
		}
		this.fileText.setText(getResourceNameHint());
	}

	public String getResourceNameHint() {
		return "Sample " + getElementCapitalTypeName();
	}

	public abstract String getElementTypeName();

	public abstract String getElementCapitalTypeName();

	/**
	 * Uses the standard container selection dialog to choose the new value for
	 * the container field.
	 */
	private void handleBrowse() {
		CardOrganizer root = getRootContainer();
		ArrayList sup = new ArrayList();
		sup.add(root);
		CardNavigatorSelectionDialog dialog = new CardNavigatorSelectionDialog(getShell(), sup, true,
		        "Select a container");
		dialog.setFilters(new ViewerFilter[] { CardsNavigatorContentProvider.getContainerFilter() });
		if (dialog.open() == Dialog.OK) {
			Object[] result = dialog.getResult();
			if (result.length == 1) {
				CardOrganizer org = (CardOrganizer) result[0];
				this.containerText.setText(org.getLocation());
			}
		}
	}

	protected abstract CardOrganizer getRootContainer();

	/**
	 * Ensures that both text fields are set.
	 */
	protected void dialogChanged() {
		String fileName = getElementName();
		String containerName = getContainerName();
		if (containerName.length() == 0) {
			updateStatus("Container must be specified");
			return;
		}
		if (fileName.length() == 0) {
			updateStatus("Name must be specified");
			return;
		}
		if (fileName.replace('\\', '/').indexOf('/', 1) > 0) {
			updateStatus("Invalid name");
			return;
		}
		updateStatus(null);
	}

	protected void updateStatus(String message) {
		setErrorMessage(message);
		setPageComplete(message == null);
	}

	public String getContainerName() {
		return this.containerText.getText();
	}

	public String getElementName() {
		return this.fileText.getText();
	}
}