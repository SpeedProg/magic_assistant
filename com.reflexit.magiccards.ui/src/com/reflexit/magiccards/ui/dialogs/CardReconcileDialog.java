package com.reflexit.magiccards.ui.dialogs;

import java.util.List;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import com.reflexit.magiccards.ui.views.IMagicColumnViewer;
import com.reflexit.magiccards.ui.views.SimpleTableViewer;
import com.reflexit.magiccards.ui.views.columns.AbstractColumn;
import com.reflexit.magiccards.ui.views.columns.ColumnCollection;
import com.reflexit.magiccards.ui.views.columns.CountColumn;
import com.reflexit.magiccards.ui.views.columns.GroupColumn;
import com.reflexit.magiccards.ui.views.columns.LocationColumn;
import com.reflexit.magiccards.ui.views.columns.OwnCountColumn;
import com.reflexit.magiccards.ui.views.columns.SetColumn;

public class CardReconcileDialog extends TitleAreaDialog {
	private IMagicColumnViewer viewer;
	private ColumnCollection columns = new ColumnCollection() {
		@Override
		protected void createColumns(List<AbstractColumn> columns) {
			columns.add(new GroupColumn(false, false, false));
			columns.add(new SetColumn(true));
			columns.add(new CountColumn());
			columns.add(new LocationColumn());
			columns.add(new OwnCountColumn());
		}
	};
	protected Object elements;

	public CardReconcileDialog(Shell parentShell) {
		super(parentShell);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		setTitle("Preview");
		setMessage("Check that cards are pulled from correct locations");
		Composite comp = (Composite) super.createDialogArea(parent);
		viewer = new SimpleTableViewer(comp, columns);
		GridData tld = new GridData(GridData.FILL_BOTH);
		tld.widthHint = 100 * 5;
		viewer.getControl().setLayoutData(tld);
		viewer.setInput(elements);
		return comp;
	}

	public void setInput(Object elements) {
		this.elements = elements;
	}
}
