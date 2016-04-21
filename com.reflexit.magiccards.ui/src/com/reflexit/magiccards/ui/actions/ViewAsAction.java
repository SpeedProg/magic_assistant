package com.reflexit.magiccards.ui.actions;

import java.util.Collection;
import java.util.function.Consumer;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;

import com.reflexit.magiccards.ui.MagicUIActivator;
import com.reflexit.magiccards.ui.preferences.PreferenceConstants;
import com.reflexit.magiccards.ui.views.Presentation;

public class ViewAsAction extends DropDownAction<Presentation> {
	private IPreferenceStore store;

	public ViewAsAction(Collection<Presentation> pres, IPreferenceStore store, Consumer<Presentation> onSelect) {
		super(pres, "View As", MagicUIActivator.getImageDescriptor("icons/obj16/pres_tree16.png"), onSelect);
		this.store = store;
		setToolTipText("View As");
	}

	@Override
	public Action createItemAction(Presentation pres) {
		Action action = super.createItemAction(pres);
		action.setImageDescriptor(getImageDesc(pres));
		return action;
	}

	private ImageDescriptor getImageDesc(Presentation pres) {
		switch (pres) {
		case TREE:
			return MagicUIActivator.getImageDescriptor("icons/obj16/pres_tree16.png");
		case TABLE:
			return MagicUIActivator.getImageDescriptor("icons/obj16/pres_list16.png");
		case SPLITTREE:
			return MagicUIActivator.getImageDescriptor("icons/obj16/pres_splittree16.png");
		case GALLERY:
			return MagicUIActivator.getImageDescriptor("icons/obj16/pres_gallery16.png");
		default:
			break;
		}
		return null;
	}

	@Override
	public String getText(Object element) {
		if (element instanceof Presentation) {
			return ((Presentation) element).getLabel();
		}
		return super.getText();
	}

	@Override
	protected Presentation getDefault() {
		return Presentation.TABLE;
	}

	@Override
	public boolean isChecked(Object element) {
		if (store != null) {
			String cur = store.getString(PreferenceConstants.PRESENTATION_VIEW);
			if (cur != null && cur.equals(((Presentation) element).key())) {
				return true;
			}
			return false;
		}
		return element.equals(getSelected());
	}

	@Override
	protected void actionOnSelectItem(Presentation pres) {
		if (store != null)
			store.setValue(PreferenceConstants.PRESENTATION_VIEW, pres.key());
		setImageDescriptor(getImageDesc(pres));
		super.actionOnSelectItem(pres);
	}
}