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
package com.reflexit.mtgtournament.ui.tour.dialogs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.printing.PrintDialog;
import org.eclipse.swt.printing.Printer;
import org.eclipse.swt.printing.PrinterData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.reflexit.mtgtournament.core.model.PlayerRoundInfo;
import com.reflexit.mtgtournament.core.model.PlayerTourInfo;
import com.reflexit.mtgtournament.core.model.Round;
import com.reflexit.mtgtournament.core.model.TableInfo;
import com.reflexit.mtgtournament.core.model.Tournament;
import com.reflexit.mtgtournament.ui.tour.Activator;

/**
 * @author Alena
 * 
 */
public class CubePrintDialog extends Dialog {
	private Object input;

	enum WhatToPrint {
		ROUND_SCHEDULE,
		ROUND_RESULTS,
		TOURNAMENT_RESULTS,
		// PLAYER_LIST
	}

	private WhatToPrint toPrint;
	private Round selectedRound;
	private Text text;
	private String textToPrint;
	private Tournament tournament;
	private Combo typeCombo;
	private Combo roundCombo;

	public void setToPrint(WhatToPrint toPrint) {
		this.toPrint = toPrint;
	}

	public void setSelectedRound(Round selectedRound) {
		this.selectedRound = selectedRound;
	}

	/**
	 * @param parentShell
	 */
	public CubePrintDialog(Shell parentShell, Object o) {
		super(parentShell);
		setShellStyle(getShellStyle() | SWT.RESIZE);
		this.input = o;
		this.toPrint = WhatToPrint.ROUND_SCHEDULE;
		this.selectedRound = null; // all
		if (input instanceof Round) {
			this.selectedRound = (Round) input;
			this.tournament = selectedRound.getTournament();
		} else if (input instanceof Tournament) {
			this.tournament = (Tournament) input;
		}
	}

	@Override
	protected Control createDialogArea(Composite par) {
		par.getShell().setText("Print...");
		Composite parent = (Composite) super.createDialogArea(par);
		Composite comp = new Composite(parent, SWT.NONE);
		comp.setLayoutData(new GridData(GridData.FILL_BOTH));
		comp.setLayout(new GridLayout(2, false));
		// type combo
		createTypeCombo(comp);
		createRoundCombo(comp);
		// preview
		Label preview = new Label(comp, SWT.NONE);
		preview.setText("Preview:");
		GridData d = new GridData();
		preview.setLayoutData(GridDataFactory.createFrom(d).span(2, 1).create());
		text = new Text(comp, SWT.MULTI | SWT.WRAP | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		GridData td = new GridData(GridData.FILL_BOTH);
		td.horizontalSpan = 2;
		td.heightHint = 600;
		text.setLayoutData(td);
		FontData origData = text.getFont().getFontData()[0];
		FontData defaultFont = new FontData("Courier", origData.getHeight(), origData.getStyle());
		text.setFont(new Font(text.getDisplay(), defaultFont));
		generatePreview();
		return comp;
	}

	private void createTypeCombo(Composite comp) {
		Label typeLabel = new Label(comp, SWT.NONE);
		typeLabel.setText("What to print:");
		typeCombo = new Combo(comp, SWT.READ_ONLY);
		WhatToPrint[] stringValues = WhatToPrint.values();
		for (WhatToPrint string : stringValues) {
			typeCombo.add(string.name());
		}
		typeCombo.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
		typeCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				toPrint = WhatToPrint.valueOf(typeCombo.getText());
				generatePreview();
			}
		});
		typeCombo.setText(toPrint.name());
	}

	private void createRoundCombo(Composite comp) {
		Label typeLabel = new Label(comp, SWT.NONE);
		typeLabel.setText("Round:");
		roundCombo = new Combo(comp, SWT.READ_ONLY);
		roundCombo.add(getRoundName(null));
		for (Round round : tournament.getRounds()) {
			roundCombo.add(getRoundName(round));
		}
		roundCombo.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
		roundCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				selectedRound = getRoundByName(roundCombo.getText());
				generatePreview();
			}
		});
		roundCombo.setText(getRoundName(selectedRound));
	}

	/**
	 * @param round
	 * @return
	 */
	private String getRoundName(Round round) {
		if (round == null)
			return "All";
		if (round.getNumber() == 0)
			return "Draft";
		return "Round " + round.getNumber();
	}

	private Round getRoundByName(String name) {
		if (name.equals("All"))
			return null;
		if (name.equals("Draft"))
			return tournament.getRound(0);
		String sn = name.substring(6);
		int n = Integer.parseInt(sn);
		return tournament.getRound(n);
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		// create OK and Cancel buttons by default
		createButton(parent, IDialogConstants.OK_ID, "Print", true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}

	@Override
	protected void okPressed() {
		print();
		super.okPressed();
	}

	/**
	 * @return
	 */
	protected String getHeader() {
		switch (toPrint) {
			case ROUND_RESULTS:
				return "Round Results for " + (selectedRound == null ? "All" : ("Round " + selectedRound.getNumber()));
			case ROUND_SCHEDULE:
				return "Round Schedule for " + (selectedRound == null ? "All" : ("Round " + selectedRound.getNumber()));
			case TOURNAMENT_RESULTS:
				return "Turnament Results";
				// case PLAYER_LIST:
				// return "Players";
			default:
				break;
		}
		return "";
	}

	/**
	 * 
	 */
	protected void generatePreview() {
		switch (toPrint) {
			case ROUND_RESULTS:
				textToPrint = roundSchedule(true);
				break;
			case ROUND_SCHEDULE:
				textToPrint = roundSchedule(false);
				break;
			case TOURNAMENT_RESULTS:
				textToPrint = tournamentResults();
				break;
			// case PLAYER_LIST:
			// textToPrint = playersList();
			// break;
			default:
				break;
		}
		text.setText(textToPrint);
	}

	/**
	 * @return
	 */
	private String playersList() {
		// TODO Auto-generated method stub
		return "not implemented";
	}

	/**
	 * @return
	 */
	private String tournamentResults() {
		List<PlayerTourInfo> playersInfo = new ArrayList<PlayerTourInfo>(tournament.getPlayersInfo());
		Collections.sort(playersInfo, new Comparator<PlayerTourInfo>() {
			public int compare(PlayerTourInfo a, PlayerTourInfo b) {
				return Tournament.comparePlayers(a, b);
			}
		});
		StringBuffer buf = new StringBuffer();
		buf.append(String.format("%2s %-20s %s\n", "Place", "Name", "Stats (Points)"));
		for (PlayerTourInfo pi : playersInfo) {
			buf.append(String.format("%5d %-20s %d-%d-%d (%2d)\n", pi.getPlace(), pi.getPlayer().getName(), pi.getWin(), pi.getDraw(),
					pi.getLost(), pi.getPoints()));
		}
		return buf.toString();
	}

	/**
	 * @param b
	 * @return
	 */
	private String roundSchedule(boolean results) {
		StringBuffer buf = new StringBuffer();
		Round r = selectedRound;
		if (r == null) {
			for (Round r1 : tournament.getRounds()) {
				buf.append("\n");
				buf.append("Round " + r1.getNumber());
				buf.append("\n");
				buf.append("\n");
				buf.append(roundSchedule(r1, results));
			}
		} else {
			buf.append(roundSchedule(r, results));
		}
		return buf.toString();
	}

	class TableInfoComparator implements Comparator<TableInfo> {
		private int p;

		public TableInfoComparator(int i) {
			this.p = i;
		}

		public int compare(TableInfo o1, TableInfo o2) {
			return o1.getPlayerInfo(p).getPlayer().getName().compareTo(o2.getPlayerInfo(p).getPlayer().getName());
		}
	}

	/**
	 * @param round
	 * @param results
	 * @return
	 */
	private String roundSchedule(Round round, boolean results) {
		StringBuffer buf = new StringBuffer();
		List<TableInfo> tables = round.getTables();
		List<TableInfo> sorted = new ArrayList<TableInfo>(tables);
		if (sorted.size() >= 5) {
			// replicate by switching players
			for (TableInfo ti : tables) {
				TableInfo nti = new TableInfo(ti.getPlayerInfo(2), ti.getPlayerInfo(1));
				nti.setNumber(ti.getTableNumber());
				sorted.add(nti);
			}
		}
		Collections.sort(sorted, new TableInfoComparator(1));
		for (TableInfo ti : sorted) {
			buf.append(printTableInfo(ti, true, results));
			buf.append("\n");
		}
		return buf.toString();
	}

	/**
	 * @param ti
	 * @param results
	 * @param results
	 * @return
	 */
	private String printTableInfo(TableInfo ti, boolean p1first, boolean results) {
		int x = p1first ? 0 : 1;
		PlayerRoundInfo pi1 = ti.getPlayerInfo(x % 2 + 1);
		PlayerRoundInfo pi2 = ti.getPlayerInfo((x + 1) % 2 + 1);
		String name1 = pi1.getPlayer().getName();
		String name2 = pi2.getPlayer().getName();
		if (results == false)
			return String.format("[%3d] %-20s - %-20s", ti.getTableNumber(), name1, name2);
		else {
			return String.format("[%3d] %-20s %s - %-20s %s", ti.getTableNumber(), name1, pi1.getWinStrDetails(),
					name2, pi2.getWinStrDetails());
		}
	}

	@Override
	protected IDialogSettings getDialogBoundsSettings() {
		return Activator.getDefault().getDialogSettings(getClass().getSimpleName());
	}

	private void print() {
		generatePreview();
		// Have user select a printer
		PrintDialog dialog = new PrintDialog(getShell());
		PrinterData printerData = dialog.open();
		if (printerData != null) {
			// Create the printer
			Printer printer = new Printer(printerData);
			try {
				// Print the contents of the file
				new WrappingPrinter(printer, getHeader(), textToPrint).print();
				MessageDialog.openInformation(getShell(), "Info", "Sent to printer");
			} catch (Exception e) {
				MessageDialog.openError(getShell(), "Error", e.getMessage());
			}
			// Dispose the printer
			printer.dispose();
		}
	}
}
