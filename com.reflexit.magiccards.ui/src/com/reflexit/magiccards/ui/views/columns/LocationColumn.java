package com.reflexit.magiccards.ui.views.columns;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.DialogCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.dialogs.ISelectionValidator;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCardFieldPhysical;
import com.reflexit.magiccards.core.model.MagicCardPhisical;
import com.reflexit.magiccards.core.model.nav.CardCollection;
import com.reflexit.magiccards.core.model.nav.CardElement;
import com.reflexit.magiccards.core.model.nav.Deck;
import com.reflexit.magiccards.core.model.storage.ICardStore;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;
import com.reflexit.magiccards.ui.dialogs.CardNavigatorSelectionDialog;

/**
 * @author Alena
 *
 */
public class LocationColumn extends GenColumn {
	/**
	 * @param columnName
	 */
	public LocationColumn() {
		super(MagicCardFieldPhysical.LOCATION, "Location");
	}

	@Override
	public String getText(Object element) {
		if (element instanceof MagicCardPhisical) {
			MagicCardPhisical m = (MagicCardPhisical) element;
			String loc = m.getLocation();
			if (loc == null)
				return "";
			if (loc.endsWith(".xml")) {
				return loc.replaceFirst("\\.xml$", "");
			}
			return loc;
		} else {
			return super.getText(element);
		}
	}

	/* (non-Javadoc)
	 * @see com.reflexit.magiccards.ui.views.columns.ColumnManager#getEditingSupport(org.eclipse.jface.viewers.ColumnViewer)
	 */
	@Override
	public EditingSupport getEditingSupport(final ColumnViewer viewer) {
		return new EditingSupport(viewer) {
			@Override
			protected boolean canEdit(Object element) {
				if (element instanceof MagicCardPhisical)
					return true;
				else
					return false;
			}

			@Override
			protected CellEditor getCellEditor(final Object element) {
				final String iniLoc = (String) getValue(element);
				DialogCellEditor editor = new DialogCellEditor((Composite) viewer.getControl(), SWT.NONE) {
					@Override
					protected Object openDialogBox(Control cellEditorWindow) {
						CardNavigatorSelectionDialog d = new CardNavigatorSelectionDialog(cellEditorWindow.getShell(),
						        DataManager.getModelRoot(), false, "Select location");
						//d.setInitialLocation(iniLoc);
						d.setValidator(new ISelectionValidator() {
							public String isValid(Object selection) {
								if (selection instanceof IStructuredSelection) {
									IStructuredSelection iss = (IStructuredSelection) selection;
									if (iss.isEmpty())
										return null;
									if (iss.size() != 1)
										return "Only one location can be chosen";
									Object el = iss.getFirstElement();
									if (el instanceof CardCollection)
										return null;
									if (el instanceof Deck)
										return null;
									return "Invalid location: Select Collection or Deck";
								}
								return null;
							}
						});
						if (d.open() == Window.OK) {
							Object[] result = d.getResult();
							if (result.length == 0)
								return null;
							Object res = result[0];
							if (res instanceof CardElement) {
								return ((CardElement) res).getLocation();
							}
							return null;
						}
						return null;
					}
				};
				return editor;
			}

			@Override
			protected Object getValue(Object element) {
				if (element instanceof MagicCardPhisical) {
					MagicCardPhisical card = (MagicCardPhisical) element;
					String loc = card.getLocation();
					return loc;
				}
				return null;
			}

			@Override
			protected void setValue(Object element, Object value) {
				if (element instanceof MagicCardPhisical) {
					MagicCardPhisical card = (MagicCardPhisical) element;
					// move
					IFilteredCardStore target = (IFilteredCardStore) getViewer().getInput();
					ICardStore<IMagicCard> cardStore = target.getCardStore();
					cardStore.remove(card);
					card.setLocation((String) value);
					cardStore.add(card);
					// update
					viewer.update(element, null);
				}
			}
		};
	}
}