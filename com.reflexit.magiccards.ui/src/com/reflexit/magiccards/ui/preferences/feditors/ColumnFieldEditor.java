package com.reflexit.magiccards.ui.preferences.feditors;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.dialogs.PatternFilter;

import com.reflexit.magiccards.core.model.abs.ICardField;
import com.reflexit.magiccards.ui.views.columns.AbstractColumn;
import com.reflexit.magiccards.ui.views.columns.ColumnCollection;
import com.reflexit.magiccards.ui.views.model.TreeViewerContentProvider;

public class ColumnFieldEditor extends CheckedTreeEditor {
	private ColumnCollection columns;
	private Button selectButton;
	private Button deselectButton;
	private Button upButton;
	private Button downButton;
	private SelectionAdapter selectionListener;
	ArrayList<AbstractColumn> sortedColumns = new ArrayList<>();

	@Override
	protected Object modelFromString(String stringList) {
		columns.updateColumnsFromPropery(stringList);
		int order[] = columns.getColumnsOrder();
		int l = order.length;
		sortedColumns.clear();
		for (int j = 0; j < l; j++) {
			AbstractColumn col = columns.getColumn(order[j]);
			sortedColumns.add(col);
		}
		return sortedColumns;
	}

	@Override
	protected CheckboxTreeViewer doCreateTreeViewer(Composite parent, int style) {
		createSelectionListener();
		PatternFilter filter = new PatternFilter();
		filter.setIncludeLeadingWildcard(true);
		FilteredTree filteredTree = new FilteredTree(parent, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL
				| SWT.H_SCROLL | SWT.FULL_SELECTION,
				filter, true) {
			@Override
			protected TreeViewer doCreateTreeViewer(Composite parent, int style) {
				return new CheckboxTreeViewer(parent, style);
			}
		};
		CheckboxTreeViewer viewer = (CheckboxTreeViewer) filteredTree.getViewer();
		viewer.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof AbstractColumn) {
					AbstractColumn col = (AbstractColumn) element;
					return col.getColumnFullName() + " (" + col.getDataField() + ")";
				}
				return super.getText(element);
			}
		});
		viewer.setContentProvider(new TreeViewerContentProvider());
		viewer.setCheckStateProvider(new ICheckStateProvider() {
			@Override
			public boolean isGrayed(Object element) {
				return false;
			}

			@Override
			public boolean isChecked(Object element) {
				if (element instanceof AbstractColumn) {
					return ((AbstractColumn) element).isVisible();
				}
				return false;
			}
		});
		return viewer;
	}

	@Override
	public void checkStateChanged(CheckStateChangedEvent event) {
		super.checkStateChanged(event);
		Object element = event.getElement();
		if (element instanceof AbstractColumn) {
			((AbstractColumn) element).setVisible(event.getChecked());
		}
	}

	@Override
	protected String modelToString(Object model) {
		List<AbstractColumn> sortedColumns = (List<AbstractColumn>) model;
		int order[] = new int[sortedColumns.size()];
		int i = 0;
		for (AbstractColumn tableItem : sortedColumns) {
			String key = tableItem.getColumnFullName();
			AbstractColumn col = columns.getColumn(key);
			col.setVisible(tableItem.isVisible());
			order[i++] = col.getColumnIndex();
		}
		columns.setColumnOrder(order);
		return columns.getColumnLayoutProperty();
	}

	@Override
	protected void doFillBoxIntoGrid(Composite parent, int numColumns) {
		Composite listBox = new Composite(parent, SWT.NONE);
		listBox.setLayout(GridLayoutFactory.swtDefaults().numColumns(2).margins(0, 0).create());
		super.doFillBoxIntoGrid(listBox, 1);
		getViewer().getControl().setLayoutData(GridDataFactory.fillDefaults().hint(300, 300).create());
		Composite butBox = new Composite(listBox, SWT.NONE);
		butBox.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
		butBox.setLayout(new GridLayout());
		createButtons(butBox);
	}

	protected void createButtons(Composite box) {
		this.upButton = createPushButton(box, "Up");//$NON-NLS-1$
		this.downButton = createPushButton(box, "Down");//$NON-NLS-1$
		this.selectButton = createPushButton(box, "Select All");//$NON-NLS-1$
		this.deselectButton = createPushButton(box, "Deselect All");//$NON-NLS-1$
		selectButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				modelFromString("+");
				getViewer().refresh();
			}
		});
		deselectButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				modelFromString("-");
				getViewer().refresh();
			}
		});
	}

	/**
	 * Helper method to create a push button.
	 *
	 * @param parent
	 *            the parent control
	 * @param key
	 *            the resource name used to supply the button's label text
	 * @return Button
	 */
	protected Button createPushButton(Composite parent, String key) {
		Button button = new Button(parent, SWT.PUSH);
		button.setText(JFaceResources.getString(key));
		button.setFont(parent.getFont());
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		int widthHint = convertHorizontalDLUsToPixels(button, IDialogConstants.BUTTON_WIDTH);
		data.widthHint = Math.max(widthHint, button.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x);
		button.setLayoutData(data);
		button.addSelectionListener(getSelectionListener());
		return button;
	}

	private SelectionListener getSelectionListener() {
		return selectionListener;
	}

	/**
	 * Creates a selection listener.
	 */
	public void createSelectionListener() {
		this.selectionListener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				Widget widget = event.widget;
				if (widget == upButton) {
					upPressed();
				} else if (widget == downButton) {
					downPressed();
				} else if (widget == getViewer().getControl()) {
					selectionChanged();
				}
			}
		};
	}

	/**
	 * Moves the currently selected item up or down.
	 *
	 * @param up
	 *            <code>true</code> if the item should move up, and <code>false</code> if it should
	 *            move down
	 */
	private void swap(boolean up) {
		setPresentsDefaultValue(false);
		int index = getSelectionIndex();
		int target = up ? index - 1 : index + 1;
		if (index >= 0 && target >= 0 && target < sortedColumns.size()) {
			AbstractColumn col = sortedColumns.get(index);
			sortedColumns.remove(index);
			sortedColumns.add(target, col);
			getViewer().refresh();
		}
	}

	private int getSelectionIndex() {
		IStructuredSelection sselection = (IStructuredSelection) getViewer().getSelection();
		if (sselection.isEmpty()) return -1;
		return sortedColumns.indexOf(sselection.getFirstElement());
	}

	/**
	 * Notifies that the Up button has been pressed.
	 */
	private void upPressed() {
		swap(true);
	}

	/**
	 * Notifies that the Down button has been pressed.
	 */
	private void downPressed() {
		swap(false);
	}

	public ColumnFieldEditor(String name, String labelText, Composite parent, ColumnCollection collection) {
		super(name, labelText, parent);
		this.columns = collection;
	}

	public ICardField[] getColumnFields() {
		return columns.getColumnFields();
	}

	public String[] getColumnIds() {
		return columns.getColumnIds();
	}

	/**
	 * Notifies that the list selection has changed.
	 */
	private void selectionChanged() {
		int index = getSelectionIndex();
		int size = sortedColumns.size();
		this.upButton.setEnabled(size > 1 && index > 0);
		this.downButton.setEnabled(size > 1 && index >= 0 && index < size - 1);
	}
}
