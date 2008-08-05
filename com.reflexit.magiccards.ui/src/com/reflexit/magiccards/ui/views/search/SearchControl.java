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
package com.reflexit.magiccards.ui.views.search;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import com.reflexit.magiccards.ui.MagicUIActivator;

/**
 * Control for searching stuff
 */
public class SearchControl {
	private ISearchRunnable runnable;
	private GridData gridData;
	private Text searchText;
	private Composite parent;
	private SearchContext context;
	private Label status;
	private Label statusImage;

	/**
	 * 
	 */
	public SearchControl(ISearchRunnable runnable) {
		this.runnable = runnable;
		this.context = new SearchContext();
		this.context.wrapAround = true;
	}

	public void setVisible(boolean vis) {
		if (!vis)
			this.gridData.heightHint = 0;
		else {
			this.gridData.heightHint = SWT.DEFAULT;
			setFocus();
			this.searchText.setSelection(0, this.searchText.getText().length());
		}
		this.parent.layout(true);
	}

	/**
	 * 
	 */
	private void setFocus() {
		this.searchText.setFocus();
	}

	/**
	 * @param partParent
	 */
	public void createFindBar(final Composite partParent) {
		this.parent = partParent;
		final Composite comp = new Composite(partParent, SWT.NONE);
		GridLayout gridLayout = new GridLayout(1, false);
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		gridLayout.marginBottom = 5;
		gridLayout.marginTop = 0;
		comp.setLayout(gridLayout);
		this.gridData = new GridData(GridData.FILL_HORIZONTAL);
		// gridData.heightHint = 0;
		comp.setLayoutData(this.gridData);
		Label label = new Label(comp, SWT.SEPARATOR | SWT.HORIZONTAL);
		label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		// toolbar composite
		final Composite bar = new Composite(comp, SWT.NONE);
		GridLayout gridLayout2 = new GridLayout(3, false);
		gridLayout2.marginHeight = 0;
		gridLayout2.marginWidth = 0;
		bar.setLayout(gridLayout2);
		bar.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		// toolbar
		ToolBar toolbar = new ToolBar(bar, SWT.FLAT);
		// hide
		ToolItem hideButton = new ToolItem(toolbar, SWT.PUSH);
		hideButton.setImage(getPlugin().getImage("icons/clcl16/delete_obj.gif"));
		hideButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setVisible(false);
			}
		});
		// search field
		this.searchText = new Text(toolbar, SWT.BORDER);
		this.searchText.setText("search...");
		this.searchText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		this.searchText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				SearchControl.this.searchText.setBackground(SearchControl.this.parent.getDisplay().getSystemColor(
				        SWT.COLOR_WHITE));
				SearchControl.this.searchText.setForeground(SearchControl.this.parent.getDisplay().getSystemColor(
				        SWT.COLOR_BLACK));
				SearchControl.this.searchText.redraw();
				SearchControl.this.context.text = SearchControl.this.searchText.getText();
			}
		});
		this.searchText.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				search();
			}
		});
		ToolItem text = new ToolItem(toolbar, SWT.SEPARATOR);
		text.setControl(this.searchText);
		text.setWidth(300);
		// next
		ToolItem next = new ToolItem(toolbar, SWT.PUSH);
		next.setImage(getPlugin().getImage("icons/clcl16/arrow_down.png"));
		next.setToolTipText("Next");
		next.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				SearchControl.this.context.forward = true;
				search();
			}
		});
		// prev
		ToolItem prev = new ToolItem(toolbar, SWT.PUSH);
		prev.setImage(getPlugin().getImage("icons/clcl16/arrow_up.png"));
		prev.setToolTipText("Previous");
		prev.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				SearchControl.this.context.forward = false;
				search();
			}
		});
		// case check
		final ToolItem csCheck = new ToolItem(toolbar, SWT.CHECK);
		csCheck.setImage(getPlugin().getImage("icons/clcl16/match_case.png"));
		csCheck.setToolTipText("Match case");
		csCheck.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				SearchControl.this.context.matchCase = csCheck.getSelection();
			}
		});
		// word check
		final ToolItem wordCheck = new ToolItem(toolbar, SWT.CHECK);
		wordCheck.setImage(getPlugin().getImage("icons/clcl16/whole_word.png"));
		wordCheck.setToolTipText("Whole word");
		wordCheck.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				SearchControl.this.context.wholeWord = wordCheck.getSelection();
			}
		});
		// status
		// new ToolItem(toolbar, SWT.SEPARATOR);
		this.statusImage = new Label(bar, SWT.FLAT);
		this.statusImage.setLayoutData(new GridData(16, 16));
		this.status = new Label(bar, SWT.NONE);
		this.status.setText("");
		this.status.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	}

	protected MagicUIActivator getPlugin() {
		return MagicUIActivator.getDefault();
	}

	/**
	 * 
	 */
	protected void search() {
		if (this.context.text == null)
			return;
		// System.err.println("search " + context.text + " forward=" + context.forward + " case=" + context.matchCase);
		this.context.status = false;
		this.context.didWrap = false;
		this.status.setText("");
		this.statusImage.setImage(null);
		this.runnable.run(this.context);
		if (this.context.didWrap) {
			this.status.setText("Wrap around");
			this.statusImage.setImage(getPlugin().getImage("icons/clcl16/wrap_around.png"));
		}
		if (this.context.status == false) {
			this.status.setText("String not found");
			this.statusImage.setImage(getPlugin().getImage("icons/clcl16/showwarn_tsk.gif"));
			this.searchText.setBackground(this.parent.getDisplay().getSystemColor(SWT.COLOR_RED));
			this.searchText.setForeground(this.parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));
		}
	}

	public void findNext() {
		this.context.forward = true;
		search();
	}

	/**
	 * For testing and external search if needed. Ui buttons would not be updated.
	 * 
	 * @param text
	 * @param matchCase
	 * @param wholeWord
	 * @param forward
	 */
	public SearchContext search(String text, boolean matchCase, boolean wholeWord, boolean forward) {
		this.context.text = text;
		this.searchText.setText(text);
		this.context.status = false;
		this.context.didWrap = false;
		this.context.matchCase = matchCase;
		this.context.wholeWord = wholeWord;
		this.context.forward = forward;
		search();
		return this.context;
	}

	/**
	 * @return
	 */
	public IAction findNextAction() {
		return new Action() {
			@Override
			public void run() {
				findNext();
			}
		};
	}

	public SearchContext getContext() {
		return this.context;
	}
}
