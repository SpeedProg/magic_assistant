package com.reflexit.magiccards.ui.wizards;

import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogPage;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import java.util.ArrayList;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.model.nav.CardElement;
import com.reflexit.magiccards.core.model.nav.CardOrganizer;
import com.reflexit.magiccards.core.model.nav.Deck;
import com.reflexit.magiccards.core.model.nav.DecksContainer;
import com.reflexit.magiccards.core.model.nav.ModelRoot;

/**
 * The "New" wizard page allows setting the container for the new file as well
 * as the file name. The page will only accept file name without the extension
 * OR with the extension that matches the expected one (deck).
 */
public class NewDeckWizardPage extends WizardPage {
	private Text containerText;
	private Text fileText;
	private ISelection selection;

	/**
	 * Constructor for SampleNewWizardPage.
	 * 
	 * @param pageName
	 */
	public NewDeckWizardPage(ISelection selection) {
		super("wizardPage");
		setTitle("Create a new deck");
		setDescription("This wizard creates a new deck with a given name and place it in specified deck container.");
		if (selection != null && !selection.isEmpty())
			this.selection = selection;
		else {
			this.selection = new StructuredSelection(DataManager.getModelRoot().getDeckContainer());
		}
	}

	/**
	 * @see IDialogPage#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		layout.numColumns = 3;
		layout.verticalSpacing = 9;
		Label label = new Label(container, SWT.NULL);
		label.setText("&Container:");
		this.containerText = new Text(container, SWT.BORDER | SWT.SINGLE);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		this.containerText.setLayoutData(gd);
		this.containerText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				dialogChanged();
			}
		});
		Button button = new Button(container, SWT.PUSH);
		button.setText("Browse...");
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				handleBrowse();
			}
		});
		label = new Label(container, SWT.NULL);
		label.setText("&File name:");
		this.fileText = new Text(container, SWT.BORDER | SWT.SINGLE);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		this.fileText.setLayoutData(gd);
		this.fileText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				dialogChanged();
			}
		});
		initialize();
		dialogChanged();
		setControl(container);
	}

	/**
	 * Tests if the current workbench selection is a suitable container to use.
	 */
	private void initialize() {
		if (this.selection != null && this.selection.isEmpty() == false
		        && this.selection instanceof IStructuredSelection) {
			IStructuredSelection ssel = (IStructuredSelection) this.selection;
			if (ssel.size() > 1)
				return;
			Object obj = ssel.getFirstElement();
			if (obj instanceof CardElement) {
				CardOrganizer container;
				if (obj instanceof CardOrganizer)
					container = (CardOrganizer) obj;
				else
					container = ((CardElement) obj).getParent();
				this.containerText.setText(container.getPath().toString());
			}
		}
		this.fileText.setText("Sample Deck");
	}

	/**
	 * Uses the standard container selection dialog to choose the new value for
	 * the container field.
	 */
	private void handleBrowse() {
		DecksContainer root = DataManager.getModelRoot().getDeckContainer();
		ArrayList sup = new ArrayList();
		sup.add(root);
		CardNavigatorSelectionDialog dialog = new CardNavigatorSelectionDialog(getShell(), sup, true,
		        "Select new deck container");
		if (dialog.open() == Dialog.OK) {
			Object[] result = dialog.getResult();
			if (result.length == 1) {
				this.containerText.setText(result[0].toString());
			}
		}
	}

	/**
	 * Ensures that both text fields are set.
	 */
	private void dialogChanged() {
		String fileName = getFileName();
		String containerName = getContainerName();
		if (containerName.length() == 0) {
			updateStatus("File container must be specified");
			return;
		}
		if (fileName.length() == 0) {
			updateStatus("Deck name must be specified");
			return;
		}
		if (fileName.replace('\\', '/').indexOf('/', 1) > 0) {
			updateStatus("Invalid deck name");
			return;
		}
		ModelRoot root = DataManager.getModelRoot();
		CardElement parent = root.findElement(new Path(containerName));
		if (!(parent instanceof DecksContainer)) {
			updateStatus("Parent folder is not a deck container");
			return;
		}
		Deck old = DataManager.getModelRoot().getDeckContainer().findDeck(fileName + ".xml");
		if (old != null) {
			updateStatus("Deck with this name already exists");
			return;
		}
		updateStatus(null);
	}

	private void updateStatus(String message) {
		setErrorMessage(message);
		setPageComplete(message == null);
	}

	public String getContainerName() {
		return this.containerText.getText();
	}

	public String getFileName() {
		return this.fileText.getText();
	}
}