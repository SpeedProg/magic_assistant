package com.reflexit.mtgtournament.ui.tour.dialogs;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.reflexit.mtgtournament.core.model.Player;
import com.reflexit.mtgtournament.ui.tour.Activator;
import com.reflexit.mtgtournament.ui.tour.views.PlayersListComposite;

public class SelectPlayerDialog extends TrayDialog {
	private PlayersListComposite playersListComposite;
	private Text pinFilter;
	private Text nameFilter;
	private PlayerViewerFilter filter;
	private IStructuredSelection sel;
	private Object input;
	class PlayerViewerFilter extends ViewerFilter {
		String name;
		String pin;

		@Override
		public boolean select(Viewer viewer, Object parentElement, Object element) {
			if (element instanceof Player) {
				Player p = (Player) element;
				if (name != null && name.trim().length() > 0) {
					return p.getName().contains(name);
				}
				if (pin != null && pin.trim().length() > 0) {
					return p.getName().contains(pin);
				}
			}
			return true;
		}
	};

	public SelectPlayerDialog(Shell shell) {
		super(shell);
		setShellStyle(getShellStyle() | SWT.RESIZE);
	}

	@Override
	protected IDialogSettings getDialogBoundsSettings() {
		return Activator.getDefault().getDialogSettings();
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		parent.getShell().setText("Find Player");
		Composite comp = (Composite) super.createDialogArea(parent);
		comp.setLayout(new GridLayout(2, false));
		GridDataFactory hor = GridDataFactory.fillDefaults().grab(true, false);
		pinFilter = createLabelText(comp, "PIN:");
		pinFilter.setLayoutData(hor.create());
		nameFilter = createLabelText(comp, "Name:");
		nameFilter.setLayoutData(hor.create());
		nameFilter.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				String text = nameFilter.getText();
				filter.name = text;
				playersListComposite.getViewer().setFilters(new ViewerFilter[] { filter });
				playersListComposite.getViewer().refresh();
			}
		});
		playersListComposite = new PlayersListComposite(comp, SWT.SINGLE | SWT.FULL_SELECTION | SWT.BORDER, true);
		playersListComposite.getViewer().addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				setSelection((IStructuredSelection) event.getSelection());
			}
		});
		playersListComposite.setLayoutData(GridDataFactory.fillDefaults().span(2, 0).grab(true, true).create());
		playersListComposite.getViewer().setInput(input);
		return comp;
	}

	public void setInput(Object input) {
		this.input = input;
	}

	protected void setSelection(IStructuredSelection selection) {
		this.sel = selection;
	}

	public Player getPlayer() {
		if (sel.isEmpty())
			return null;
		return (Player) sel.getFirstElement();
	}

	private Text createLabelText(Composite comp, String string) {
		Label label = new Label(comp, SWT.NONE);
		label.setText(string);
		Text text = new Text(comp, SWT.BORDER);
		return text;
	}
}
