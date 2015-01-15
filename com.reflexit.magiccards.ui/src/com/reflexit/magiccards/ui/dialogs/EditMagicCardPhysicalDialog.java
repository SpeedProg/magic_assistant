package com.reflexit.magiccards.ui.dialogs;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.swt.widgets.Shell;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.MagicCardPhysical;
import com.reflexit.magiccards.core.model.abs.ICardField;

public class EditMagicCardPhysicalDialog extends EditCardsPropertiesDialog {
	private Collection<MagicCardPhysical> cards;

	public EditMagicCardPhysicalDialog(Shell parentShell, MagicCardPhysical card) {
		this(parentShell, Collections.singletonList(card));
	}

	public EditMagicCardPhysicalDialog(Shell parentShell, Collection<MagicCardPhysical> cards) {
		super(parentShell, new PreferenceStore());
		this.cards = cards;
		// populate store
		boolean first = true;
		for (Iterator<MagicCardPhysical> iterator = cards.iterator(); iterator.hasNext();) {
			MagicCardPhysical card = iterator.next();
			if (first) {
				ICardField[] allFields = MagicCardField.allFields();
				for (ICardField f : allFields) {
					Object value = card.get(f);
					String svalue = String.valueOf(value == null ? "" : value);
					store.setDefault(f.name(), svalue);
				}
				first = false;
			} else {
				ICardField[] allFields = MagicCardField.allFields();
				for (ICardField f : allFields) {
					Object value = card.get(f);
					String svalue = String.valueOf(value == null ? "" : value);
					if (!svalue.equals(store.getDefaultString(f.name()))) {
						store.setDefault(f.name(), UNCHANGED);
					}
				}
				store.setDefault(EditCardsPropertiesDialog.NAME_FIELD, "<Multiple Cards>: " + cards.size());
			}
		}
	}

	@Override
	protected void okPressed() {
		super.okPressed();
		new Job("Updating edited cards") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				for (Iterator<MagicCardPhysical> iterator = cards.iterator(); iterator.hasNext();) {
					MagicCardPhysical card = iterator.next();
					editCard(card, store, false);
				}
				DataManager.getInstance().updateList((Collection) cards, null);
				// DataManager.reconcile();
				return Status.OK_STATUS;
			}
		}.schedule();
	}

	private void editCard(MagicCardPhysical card, PreferenceStore store, boolean update) {
		boolean modified = false;
		modified = setField(card, store, MagicCardField.COUNT) || modified;
		modified = setField(card, store, MagicCardField.PRICE) || modified;
		modified = setField(card, store, MagicCardField.COMMENT) || modified;
		modified = setField(card, store, MagicCardField.OWNERSHIP) || modified;
		String special = card.getSpecial();
		String especial = store.getString(EditCardsPropertiesDialog.SPECIAL_FIELD);
		if (!UNCHANGED.equals(especial) && !especial.equals(special)) {
			card.setSpecialTag(especial);
			modified = true;
		}
		if (modified && update) {
			DataManager.getInstance().update(card, null);
		}
	}

	protected boolean setField(MagicCardPhysical card, PreferenceStore store, ICardField field) {
		Boolean modified = false;
		String orig = String.valueOf(card.get(field));
		String edited = store.getString(field.name());
		if (!UNCHANGED.equals(edited) && !edited.equals(orig)) {
			try {
				card.set(field, edited);
				modified = true;
			} catch (Exception e) {
				// was bad value
				MessageDialog.openError(getShell(), "Error", "Invalid value for " + field + ": " + edited);
			}
		}
		return modified;
	}
}
