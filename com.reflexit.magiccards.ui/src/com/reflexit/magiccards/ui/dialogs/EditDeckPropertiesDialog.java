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

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.reflexit.magiccards.core.model.storage.IStorageInfo;

/**
 * Dialog to edit properties of a deck/collection
 */
public class EditDeckPropertiesDialog extends TitleAreaDialog {
	private IStorageInfo info;
	private Combo type;
	private Button virtual;
	private Text text;

	public EditDeckPropertiesDialog(Shell shell, IStorageInfo info) {
		super(shell);
		this.info = info;
		setShellStyle(getShellStyle() | SWT.RESIZE);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		setMessage("Edit decl/collection properties. Press OK to save. Cancel to drop changes.");
		Composite area = (Composite) super.createDialogArea(parent);
		Composite comp = new Composite(area, SWT.NONE);
		comp.setLayoutData(new GridData(GridData.FILL_BOTH));
		GridLayout layout = new GridLayout(3, false);
		comp.setLayout(layout);
		{
			Label label = new Label(comp, SWT.NONE);
			label.setText("Type:");
			type = new Combo(comp, SWT.READ_ONLY | SWT.DROP_DOWN);
			type.add(IStorageInfo.DECK_TYPE);
			type.add(IStorageInfo.COLLECTION_TYPE);
			type.setText(info.getType() == IStorageInfo.DECK_TYPE
			        ? IStorageInfo.DECK_TYPE
			        : IStorageInfo.COLLECTION_TYPE);
			GridData gd = new GridData(GridData.FILL_HORIZONTAL);
			type.setLayoutData(gd);
		}
		{
			virtual = new Button(comp, SWT.CHECK);
			virtual.setSelection(info.isVirtual());
			virtual.setText("Virtual");
			GridData gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.horizontalSpan = 2;
			virtual.setLayoutData(gd);
		}
		createTextArea(comp);
		return comp;
	}

	private void createTextArea(Composite area) {
		Label label = new Label(area, SWT.NONE);
		label.setText("Description:");
		GridData gd1 = new GridData(GridData.FILL_HORIZONTAL);
		gd1.horizontalSpan = 3;
		label.setLayoutData(gd1);
		text = new Text(area, SWT.WRAP | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan = 3;
		text.setLayoutData(gd);
		text.setText(info.getComment());
	}

	@Override
	protected void okPressed() {
		save();
		super.okPressed();
	}

	private void save() {
		info.setComment(text.getText());
		info.setVirtual(virtual.getSelection());
		info.setType(type.getText());
	}
}
