package com.reflexit.magiccards.ui.dialogs;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

/**
 * @author Alena
 *
 */
public class LoadExtrasDialog extends TitleAreaDialog {
	private Button rulingsCheck;
	private Button ratingsCheck;
	private Button artistCheck;
	
	private boolean rulings;
	private boolean ratings;
	private boolean artists;

	/**
	 * @param parentShell
	 * @param max 
	 */
	public LoadExtrasDialog(Shell parentShell) {
		super(parentShell);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		getShell().setText("Load Additional Info...");
		setTitle("Load Additional Info");
		setMessage("Choose which features to load.");
		Composite area = (Composite) super.createDialogArea(parent);
		Composite buttons = new Composite(area, SWT.NONE);
		buttons.setLayout(new GridLayout(2, false));
		GridDataFactory buttonGridData = GridDataFactory.fillDefaults().span(2, 1);
		this.rulingsCheck = new Button(buttons, SWT.CHECK);
		this.rulingsCheck.setText("Rulings");
		buttonGridData.applyTo(this.rulingsCheck);
		this.ratingsCheck = new Button(buttons, SWT.CHECK);
		this.ratingsCheck.setText("Ratings");
		buttonGridData.applyTo(this.ratingsCheck);
		this.artistCheck = new Button(buttons, SWT.CHECK);
		this.artistCheck.setText("Artists");
		buttonGridData.applyTo(this.artistCheck);

		return area;
	}

	@Override
	protected void okPressed() {
		this.artists = this.artistCheck.getSelection();
		this.rulings = this.rulingsCheck.getSelection();
		this.ratings = this.ratingsCheck.getSelection();
		super.okPressed();
	}
	
	public boolean getRatings() {
		return this.ratings;
	}
	
	public boolean getArtists() {
		return this.artists;
	}
	
	public boolean getRulings() {
		return this.rulings;
	}
}
