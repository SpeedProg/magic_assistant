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
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.PlatformUI;

import com.reflexit.magiccards.ui.MagicUIActivator;
import com.reflexit.magiccards.ui.widgets.SearchContextFocusListener;

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
	private boolean searchAsYouType;
	private Composite comp;

	public SearchControl(ISearchRunnable runnable) {
		this.runnable = runnable;
		this.context = new SearchContext();
		this.context.setWrapAround(true);
	}

	public void setSearchAsYouType(boolean value) {
		searchAsYouType = value;
	}

	public void setVisible(boolean vis) {
		comp.setVisible(vis);
		if (!vis) {
			this.gridData.heightHint = 0;
			this.gridData.exclude = true;
		} else {
			this.gridData.exclude = false;
			this.gridData.heightHint = SWT.DEFAULT;
			this.gridData.minimumHeight = 32;
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
		comp = new Composite(partParent, SWT.NONE);
		GridLayout gridLayout = new GridLayout(1, false);
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		gridLayout.marginBottom = 0;
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
		toolbar.setLayoutData(new GridData(GridData.FILL_BOTH));
		// hide
		ToolItem hideButton = new ToolItem(toolbar, SWT.PUSH);
		hideButton.setImage(getPlugin().getImage("icons/clcl16/delete_obj.gif"));
		hideButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setVisible(false);
			}
		});
		this.searchText = new Text(toolbar, SWT.SEARCH);
		// this.searchText.setText("search...");
		GridData td = new GridData(GridData.FILL_HORIZONTAL);
		this.searchText.setLayoutData(td);
		this.searchText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				resetBoxColor();
				context.setCancelled(true);
				context.setText(SearchControl.this.searchText.getText());
				if (searchAsYouType) {
					search();
				}
			}
		});
		this.searchText.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				search();
			}
		});
		this.searchText.addFocusListener(new SearchContextFocusListener());
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
				SearchControl.this.context.setForward(true);
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
				SearchControl.this.context.setForward(false);
				search();
			}
		});
		// // case check
		// final ToolItem csCheck = new ToolItem(toolbar, SWT.CHECK);
		// csCheck.setImage(getPlugin().getImage("icons/clcl16/match_case.png"));
		// csCheck.setToolTipText("Match case");
		// csCheck.addSelectionListener(new SelectionAdapter() {
		// @Override
		// public void widgetSelected(SelectionEvent e) {
		// SearchControl.this.context.setMatchCase(csCheck.getSelection());
		// }
		// });
		// word check
		final ToolItem wordCheck = new ToolItem(toolbar, SWT.CHECK);
		wordCheck.setImage(getPlugin().getImage("icons/clcl16/whole_word.png"));
		wordCheck.setToolTipText("Whole word");
		wordCheck.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				SearchControl.this.context.setWholeWord(wordCheck.getSelection());
				search();
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

	protected void search() {
		if (this.context.getText() == null)
			return;
		preRunnable();
		new Thread("Searching " + context.getText()) {
			@Override
			public void run() {
				try {
					context.setFound(false);
					context.setDidWrap(false);
					context.setCancelled(false);
					runnable.run(context);
				} finally {
					PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
						@Override
						public void run() {
							postRunnable();
						}
					});
				}
			}
		}.start();
	}

	private void preRunnable() {
		synchronized (context) {
			// System.err.println("search " + context.text + " forward=" +
			// context.forward + " case=" + context.matchCase);
			this.status.setText("");
			this.statusImage.setImage(null);
			resetBoxColor();
		}
	}

	protected void postRunnable() {
		synchronized (context) {
			if (this.context.isDidWrap()) {
				this.status.setText("Wrap around");
				this.statusImage.setImage(getPlugin().getImage("icons/clcl16/wrap_around.png"));
			}
			if (this.context.isFound() == false) {
				this.status.setText("String not found");
				this.statusImage.setImage(getPlugin().getImage("icons/clcl16/showwarn_tsk.gif"));
				this.searchText.setBackground(this.parent.getDisplay().getSystemColor(SWT.COLOR_RED));
				this.searchText.setForeground(this.parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));
			}
		}
	}

	public void findNext() {
		this.context.setForward(true);
		search();
	}

	/**
	 * For testing and external search if needed. Ui buttons would not be
	 * updated.
	 *
	 * @param text
	 * @param matchCase
	 * @param wholeWord
	 * @param forward
	 */
	public SearchContext search(String text, boolean matchCase, boolean wholeWord, boolean forward) {
		this.context.setText(text);
		this.searchText.setText(text);
		this.context.setFound(false);
		this.context.setDidWrap(false);
		this.context.setMatchCase(matchCase);
		this.context.setWholeWord(wholeWord);
		this.context.setForward(forward);
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

	public boolean isVisible() {
		return comp.isVisible();
	}

	public void resetBoxColor() {
		Color white = SearchControl.this.parent.getDisplay().getSystemColor(SWT.COLOR_WHITE);
		if (searchText.getBackground() != white) {
			searchText.setBackground(white);
			searchText.setForeground(SearchControl.this.parent.getDisplay().getSystemColor(SWT.COLOR_BLACK));
			searchText.redraw();
		}
	}

	public Control getControl() {
		return comp;
	}
}
