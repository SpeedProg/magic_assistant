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

import java.util.ArrayList;

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

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.model.nav.CardElement;
import com.reflexit.magiccards.core.model.nav.CardOrganizer;
import com.reflexit.magiccards.core.model.nav.ModelRoot;
import com.reflexit.magiccards.ui.dialogs.CardNavigatorSelectionDialog;
import com.reflexit.magiccards.ui.views.nav.CardsNavigatorContentProvider;

/**
 * @author Alena
 *
 */
public abstract class NewCardElementWizardPage extends WizardPage {
	private Text containerText;
	private Text nameText;
	protected Button virtual;
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
	@Override
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		layout.numColumns = 3;
		layout.verticalSpacing = 9;
		// name
		Label label = new Label(container, SWT.NULL);
		label.setText("&Name:");
		this.nameText = new Text(container, SWT.BORDER | SWT.SINGLE);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		this.nameText.setLayoutData(gd);
		this.nameText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				dialogChanged();
			}
		});
		// parent
		Label label2 = new Label(container, SWT.NULL);
		label2.setText("Parent &Container:");
		this.containerText = new Text(container, SWT.BORDER | SWT.SINGLE);
		GridData gd2 = new GridData(GridData.FILL_HORIZONTAL);
		this.containerText.setLayoutData(gd2);
		this.containerText.addModifyListener(new ModifyListener() {
			@Override
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
		createOptionsGroup(container);
		initialize();
		dialogChanged();
		setControl(container);
		setErrorMessage(null);
		this.nameText.setFocus();
	}

	protected void createOptionsGroup(Composite container) {
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
				String loc = container.getLocation().toString();
				if (loc.length() == 0)
					this.containerText.setText(container.getName()); // only fake ones
				else
					this.containerText.setText(loc);
			}
		}
		this.nameText.setText(getResourceNameHint());
	}

	public String getResourceNameHint() {
		return "";
	}

	public abstract String getElementTypeName();

	/**
	 * Uses the standard container selection dialog to choose the new value for the container field.
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
				this.containerText.setText(org.getLocation().toString());
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
		if (fileName.contains(".")) {
			updateStatus("Name cannot contain '.'");
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
		return this.nameText.getText();
	}

	public boolean isVirtual() {
		if (virtual != null)
			return virtual.getSelection();
		return false;
	}

	public ModelRoot getModelRoot() {
		return DataManager.getInstance().getModelRoot();
	}
}