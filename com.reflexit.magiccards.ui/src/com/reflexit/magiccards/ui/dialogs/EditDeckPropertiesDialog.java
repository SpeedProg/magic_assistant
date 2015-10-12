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
package com.reflexit.magiccards.ui.dialogs;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.reflexit.magiccards.core.MagicException;
import com.reflexit.magiccards.core.model.storage.IStorageInfo;

/**
 * Dialog to edit properties of a deck/collection
 */
public class EditDeckPropertiesDialog extends TitleAreaDialog {
	private IStorageInfo info;
	private Combo type;
	private Button virtual;
	private Text text;
	private Button protection;

	public EditDeckPropertiesDialog(Shell shell, IStorageInfo info) {
		super(shell);
		if (info == null)
			throw new NullPointerException();
		this.info = info;
		setShellStyle(getShellStyle() | SWT.RESIZE);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		getShell().setText("Edit...");
		// setTitleImage(MagicUIActivator.getDefault().getImage("icons/Book-1-icon.gif"));
		setTitle("Edit Properties");
		setMessage("You can modify deck/collection properties here. Press OK to save.");
		Composite area = (Composite) super.createDialogArea(parent);
		Composite comp = new Composite(area, SWT.NONE);
		comp.setLayoutData(new GridData(GridData.FILL_BOTH));
		GridLayout layout = new GridLayout(4, false);
		comp.setLayout(layout);
		{
			Label label = new Label(comp, SWT.NONE);
			label.setText("Type:");
			type = new Combo(comp, SWT.READ_ONLY | SWT.DROP_DOWN);
			type.add(IStorageInfo.DECK_TYPE);
			type.add(IStorageInfo.COLLECTION_TYPE);
			type.setText(IStorageInfo.DECK_TYPE.equals(info.getType()) ? IStorageInfo.DECK_TYPE
					: IStorageInfo.COLLECTION_TYPE);
			GridData gd = new GridData(GridData.FILL_HORIZONTAL);
			type.setLayoutData(gd);
		}
		{
			virtual = new Button(comp, SWT.CHECK);
			virtual.setSelection(info.isVirtual());
			virtual.setText("Virtual");
			GridData gd = new GridData(GridData.FILL_HORIZONTAL);
			virtual.setLayoutData(gd);
		}
		{
			protection = new Button(comp, SWT.CHECK);
			protection.setSelection(info.isReadOnly());
			protection.setText("Read Only");
			GridData gd = new GridData(GridData.FILL_HORIZONTAL);
			protection.setLayoutData(gd);
		}
		createTextArea(comp);
		return comp;
	}

	private void createTextArea(Composite area) {
		Group group = new Group(area, SWT.NONE);
		group.setText("Description");
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan = ((GridLayout) area.getLayout()).numColumns;
		group.setLayoutData(gd);
		group.setLayout(new GridLayout());
		text = new Text(group, SWT.WRAP | SWT.BORDER);
		text.setLayoutData(GridDataFactory.fillDefaults().hint(600, 200).create());
		text.setText(info.getComment() == null ? "" : info.getComment());
	}

	@Override
	protected void okPressed() {
		try {
			save();
			super.okPressed();
		} catch (MagicException e) {
			MessageDialog.openError(getParentShell(), "Error", "Cannot save: " + e.getMessage());
		}
	}

	private void save() {
		boolean isReadOnly = protection.getSelection();
		info.setReadOnly(isReadOnly);
		if (!isReadOnly) {
			info.setComment(text.getText());
			info.setVirtual(virtual.getSelection());
			info.setType(type.getText());
		}
	}
}
