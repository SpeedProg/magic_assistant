/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Baltasar Belyavsky - fix for 300539 - Add ability to specify filter-path     
 *******************************************************************************/
package com.reflexit.magiccards.ui.preferences.feditors;

import java.io.File;

import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.jface.preference.StringButtonFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;

/**
 * A field editor for a file path type preference. A standard file dialog appears when the user
 * presses the change button.
 */
public class FileSaveFieldEditor extends FileFieldEditor {
	/**
	 * List of legal file extension suffixes, or <code>null</code> for system defaults.
	 */
	private String[] extensions = null;
	/**
	 * Initial path for the Browse dialog.
	 */
	private File filterPath = null;

	/**
	 * Creates a new file field editor
	 */
	protected FileSaveFieldEditor() {
	}

	/**
	 * Creates a file field editor.
	 * 
	 * @param name
	 *            the name of the preference this field editor works on
	 * @param labelText
	 *            the label text of the field editor
	 * @param parent
	 *            the parent of the field editor's control
	 */
	public FileSaveFieldEditor(String name, String labelText, Composite parent) {
		this(name, labelText, false, parent);
	}

	/**
	 * Creates a file field editor.
	 * 
	 * @param name
	 *            the name of the preference this field editor works on
	 * @param labelText
	 *            the label text of the field editor
	 * @param enforceAbsolute
	 *            <code>true</code> if the file path must be absolute, and <code>false</code> otherwise
	 * @param parent
	 *            the parent of the field editor's control
	 */
	public FileSaveFieldEditor(String name, String labelText, boolean enforceAbsolute, Composite parent) {
		this(name, labelText, enforceAbsolute, VALIDATE_ON_FOCUS_LOST, parent);
	}

	/**
	 * Creates a file field editor.
	 * 
	 * @param name
	 *            the name of the preference this field editor works on
	 * @param labelText
	 *            the label text of the field editor
	 * @param enforceAbsolute
	 *            <code>true</code> if the file path must be absolute, and <code>false</code> otherwise
	 * @param validationStrategy
	 *            either {@link StringButtonFieldEditor#VALIDATE_ON_KEY_STROKE} to perform on the
	 *            fly checking, or {@link StringButtonFieldEditor#VALIDATE_ON_FOCUS_LOST} (the
	 *            default) to perform validation only after the text has been typed in
	 * @param parent
	 *            the parent of the field editor's control.
	 * @since 3.4
	 * @see StringButtonFieldEditor#VALIDATE_ON_KEY_STROKE
	 * @see StringButtonFieldEditor#VALIDATE_ON_FOCUS_LOST
	 */
	public FileSaveFieldEditor(String name, String labelText, boolean enforceAbsolute,
			int validationStrategy, Composite parent) {
		super(name, labelText, enforceAbsolute, validationStrategy, parent);
	}

	/*
	 * (non-Javadoc) Method declared on StringButtonFieldEditor. Opens the file chooser dialog and
	 * returns the selected file.
	 */
	@Override
	protected String changePressed() {
		File f = new File(getTextControl().getText());
		if (!f.exists()) {
			f = null;
		}
		File d = getFile(f);
		if (d == null) {
			return null;
		}
		return d.getAbsolutePath();
	}

	/**
	 * Helper to open the file chooser dialog.
	 * 
	 * @param startingDirectory
	 *            the directory to open the dialog on.
	 * @return File The File the user selected or <code>null</code> if they do not.
	 */
	private File getFile(File startingDirectory) {
		FileDialog dialog = new FileDialog(getShell(), SWT.SAVE | SWT.SHEET);
		if (startingDirectory != null) {
			dialog.setFileName(startingDirectory.getPath());
		} else if (filterPath != null) {
			dialog.setFilterPath(filterPath.getPath());
		}
		if (extensions != null) {
			dialog.setFilterExtensions(extensions);
		}
		String file = dialog.open();
		if (file != null) {
			file = file.trim();
			if (file.length() > 0) {
				return new File(file);
			}
		}
		return null;
	}

	/**
	 * Sets this file field editor's file extension filter.
	 * 
	 * @param extensions
	 *            a list of file extension, or <code>null</code> to set the filter to the system's
	 *            default value
	 */
	@Override
	public void setFileExtensions(String[] extensions) {
		this.extensions = extensions;
		super.setFileExtensions(extensions);
	}

	/**
	 * Sets the initial path for the Browse dialog.
	 * 
	 * @param path
	 *            initial path for the Browse dialog
	 * @since 3.6
	 */
	@Override
	public void setFilterPath(File path) {
		filterPath = path;
		super.setFilterPath(path);
	}
}
