package com.reflexit.magiccards.ui.views.lib;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionFactory;

import java.util.Iterator;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.model.IFilteredCardStore;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.events.CardEvent;
import com.reflexit.magiccards.core.model.events.ICardEventListener;
import com.reflexit.magiccards.ui.preferences.LibViewPreferencePage;
import com.reflexit.magiccards.ui.preferences.PreferenceConstants;
import com.reflexit.magiccards.ui.views.AbstractCardsView;
import com.reflexit.magiccards.ui.views.LazyTableViewerManager;
import com.reflexit.magiccards.ui.views.ViewerManager;

public class LibView extends AbstractCardsView implements ICardEventListener {
	public static final String ID = "com.reflexit.magiccards.ui.views.lib.LibView";
	Action delete;

	/**
	 * The constructor.
	 */
	public LibView() {
	}

	@Override
	protected void makeActions() {
		super.makeActions();
		this.delete = new Action("Remove") {
			@Override
			public void run() {
				removeSelected();
			}
		};
		IActionBars actionBars = getViewSite().getActionBars();
		actionBars.setGlobalActionHandler(ActionFactory.DELETE.getId(), this.delete);
	}

	protected void removeSelected() {
		ISelection selection = getViewer().getSelection();
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection sel = (IStructuredSelection) selection;
			if (!sel.isEmpty()) {
				for (Iterator iterator = sel.iterator(); iterator.hasNext();) {
					Object o = iterator.next();
					if (o instanceof IMagicCard)
						this.manager.getFilteredStore().getCardStore().removeCard(o);
				}
			}
		}
	}

	@Override
	protected void fillContextMenu(IMenuManager manager) {
		super.fillContextMenu(manager);
		manager.add(this.delete);
	}

	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
	}

	/* (non-Javadoc)
	 * @see com.reflexit.magiccards.ui.views.AbstractCardsView#init(org.eclipse.ui.IViewSite)
	 */
	@Override
	public void init(IViewSite site) throws PartInitException {
		super.init(site);
		this.manager.getFilteredStore().getCardStore().addListener(this);
	}

	/* (non-Javadoc)
	 * @see com.reflexit.magiccards.ui.views.AbstractCardsView#dispose()
	 */
	@Override
	public void dispose() {
		this.manager.getFilteredStore().getCardStore().removeListener(this);
		super.dispose();
	}

	@Override
	public ViewerManager doGetViewerManager(AbstractCardsView abstractCardsView) {
		return new LazyTableViewerManager(this);
	}

	@Override
	public IFilteredCardStore doGetFilteredStore() {
		return DataManager.getCardHandler().getMagicLibraryHandler();
	}

	public void handleEvent(CardEvent event) {
		//if (event.getType() == CardEvent.ADD)
		this.manager.loadData();
	}

	@Override
	protected String getPreferencePageId() {
		return LibViewPreferencePage.class.getName();
	}

	@Override
	protected String getPrefenceColumnsId() {
		return PreferenceConstants.LIBVIEW_COLS;
	}
}