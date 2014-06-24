package com.reflexit.mtgtournament.ui.tour.dialogs;

import java.util.List;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import com.reflexit.mtgtournament.core.model.PlayerTourInfo;
import com.reflexit.mtgtournament.core.model.Round;
import com.reflexit.mtgtournament.core.xml.TournamentManager;
import com.reflexit.mtgtournament.ui.tour.Activator;

public class ByesDialog extends TitleAreaDialog {
	PlayerTourInfo info;

	public ByesDialog(Shell parentShell, IStructuredSelection sel) {
		super(parentShell);
		info = (PlayerTourInfo) sel.getFirstElement();
		setShellStyle(getShellStyle() | SWT.RESIZE);
	}

	@Override
	protected IDialogSettings getDialogBoundsSettings() {
		return Activator.getDefault().getDialogSettings(getClass().getSimpleName());
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		setTitle(info.getPlayer().getName());
		setMessage("Selects rounds where player receive a Bye (automatic Win)");
		Composite area = (Composite) super.createDialogArea(parent);
		Composite rc = new Composite(area, SWT.NONE);
		rc.setLayout(new GridLayout());
		List<Round> rounds = info.getTournament().getRounds();
		for (Round round : rounds) {
			createRound(rc, round);
		}
		return area;
	}

	private void createRound(Composite area, final Round round) {
		if (round.getNumber() == 0)
			return; // draft
		final Button button = new Button(area, SWT.CHECK);
		button.setText("Round " + round.getNumber() + " (" + round.getType() + ")");
		button.setSelection(info.getBye(round.getNumber()));
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				info.getByes().put(round.getNumber(), button.getSelection());
			}
		});
		button.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	}

	@Override
	protected void okPressed() {
		try {
			TournamentManager.save(info.getTournament());
		} catch (Exception e) {
			Activator.log(e);
		}
		super.okPressed();
	}
}
