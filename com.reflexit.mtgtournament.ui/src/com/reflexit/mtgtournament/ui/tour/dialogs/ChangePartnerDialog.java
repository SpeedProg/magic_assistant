package com.reflexit.mtgtournament.ui.tour.dialogs;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.reflexit.mtgtournament.core.model.Player;
import com.reflexit.mtgtournament.core.model.PlayerRoundInfo;
import com.reflexit.mtgtournament.core.model.PlayerTourInfo;
import com.reflexit.mtgtournament.core.model.TableInfo;
import com.reflexit.mtgtournament.ui.tour.Activator;

public class ChangePartnerDialog extends TrayDialog {
	private TableInfo input;
	private List<PlayerTourInfo> playersInfo;
	private Combo combo;
	private int origIndex;
	private PlayerTourInfo result;

	public ChangePartnerDialog(Shell shell, TableInfo info) {
		super(shell);
		setShellStyle(getShellStyle() | SWT.RESIZE);
		input = info;
	}

	@Override
	protected IDialogSettings getDialogBoundsSettings() {
		return Activator.getDefault().getDialogSettings(getClass().getSimpleName());
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		parent.getShell().setText("Override Scheduler - Change Partner");
		Composite comp = (Composite) super.createDialogArea(parent);
		comp.setLayout(new GridLayout());
		GridDataFactory hor = GridDataFactory.fillDefaults().grab(true, false);
		Label label = new Label(comp, SWT.NONE);
		PlayerRoundInfo playerInfo = input.getOpponent(0);
		label.setText(playerInfo.getPlayer().getName() + "  vs. ");
		label.setLayoutData(hor.create());
		combo = new Combo(parent, SWT.READ_ONLY);
		playersInfo = new ArrayList<PlayerTourInfo>(input.getRound().getTournament().getPlayersInfo());
		playersInfo.add(new PlayerTourInfo(Player.DUMMY));
		for (Iterator iterator = playersInfo.iterator(); iterator.hasNext();) {
			PlayerTourInfo pti = (PlayerTourInfo) iterator.next();
			if (!pti.isActive() || pti.getPlayer() == playerInfo.getPlayer())
				iterator.remove();
			else
				combo.add(pti.getPlayer().getName());
		}
		combo.setText(input.getOpponent(1).getPlayer().getName());
		combo.setLayoutData(hor.create());
		origIndex = combo.getSelectionIndex();
		return comp;
	}

	@Override
	protected void okPressed() {
		int selectionIndex = combo.getSelectionIndex();
		if (origIndex != selectionIndex) {
			// do something
			result = playersInfo.get(selectionIndex);
		}
		super.okPressed();
	}

	public PlayerTourInfo getNewPlayer() {
		return result;
	}
}