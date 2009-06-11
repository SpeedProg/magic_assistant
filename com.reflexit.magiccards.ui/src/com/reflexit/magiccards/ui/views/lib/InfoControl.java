package com.reflexit.magiccards.ui.views.lib;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.reflexit.magiccards.core.model.storage.ICardStore;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;
import com.reflexit.magiccards.core.model.storage.IStorage;
import com.reflexit.magiccards.core.model.storage.IStorageContainer;
import com.reflexit.magiccards.core.model.storage.IStorageInfo;

public class InfoControl extends Composite implements IDeckPage {
	private Text text;
	private ICardStore store;

	InfoControl(Composite parent) {
		super(parent, SWT.NONE);
		this.setLayout(new GridLayout());
		Label label = new Label(this, SWT.NONE);
		label.setText("Description:");
		text = new Text(this, SWT.WRAP | SWT.BORDER);
		text.setLayoutData(new GridData(GridData.FILL_BOTH));
		text.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				setComment(text.getText());
			}
		});
	}

	protected void setComment(String text2) {
		IStorageInfo si = getInfo();
		if (si == null)
			return;
		si.setComment(text2);
	}

	public void setFilteredStore(IFilteredCardStore store) {
		this.store = store.getCardStore();
		updateFromStore();
	}

	private IStorageInfo getInfo() {
		if (store instanceof IStorageContainer) {
			IStorage storage = ((IStorageContainer) store).getStorage();
			if (storage instanceof IStorageInfo) {
				IStorageInfo si = ((IStorageInfo) storage);
				return si;
			}
		}
		return null;
	}

	public Control getControl() {
		return this;
	}

	public void updateFromStore() {
		IStorageInfo si = getInfo();
		if (si != null) {
			String comment = si.getComment();
			if (comment != null)
				text.setText(comment);
		}
	}

	public String getStatusMessage() {
		return "";
	};
}
