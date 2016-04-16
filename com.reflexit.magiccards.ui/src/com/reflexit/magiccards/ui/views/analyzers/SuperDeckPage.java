package com.reflexit.magiccards.ui.views.analyzers;

import java.util.Arrays;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.preference.IPersistentPreferenceStore;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Composite;

import com.reflexit.magiccards.core.model.MagicCardFilter;
import com.reflexit.magiccards.ui.actions.ViewAsAction;
import com.reflexit.magiccards.ui.preferences.PreferenceConstants;
import com.reflexit.magiccards.ui.views.AbstractGroupPageCardsViewPage;
import com.reflexit.magiccards.ui.views.IMagicCardListControl;
import com.reflexit.magiccards.ui.views.Presentation;
import com.reflexit.magiccards.ui.views.ViewPageContribution;
import com.reflexit.magiccards.ui.views.lib.DeckListControl;
import com.reflexit.magiccards.ui.views.lib.DeckView;
import com.reflexit.magiccards.ui.views.lib.IDeckPage;

public class SuperDeckPage extends AbstractGroupPageCardsViewPage implements IDeckPage, IMagicCardListControl {
	public SuperDeckPage() {
	}

	@Override
	protected void createPages() {
		addPage(Presentation.TABLE);
		addPage(Presentation.SPLITTREE);
		addPage(Presentation.TREE);
		addPage(Presentation.GALLERY);
	}

	@Override
	protected void createPageContents(Composite parent) {
		super.createPageContents(parent);
		String cur = getLocalPreferenceStore().getString(PreferenceConstants.PRESENTATION_VIEW);
		if (cur != null) {
			Presentation pres = Presentation.valueOf(cur);
			int i = getPageGroup().getPageIndex(pres.getLabel());
			getPageGroup().setActivePageIndex(i);
		}
	}

	protected void addPage(Presentation pres) {
		getPageGroup().add(new ViewPageContribution("", pres.getLabel(), null, new DeckPagePresentation(pres)));
	}

	@Override
	public void setContextMenuManager(MenuManager menuMgr) {
		super.setContextMenuManager(menuMgr);
		getPageGroup().getActivePage().setContextMenuManager(menuMgr);
	}

	class DeckPagePresentation extends DeckListControl {
		public DeckPagePresentation(Presentation type) {
			super(type);
		}

		@Override
		protected void hookDoubleClickAction() {
			viewer.addDoubleClickListener(new IDoubleClickListener() {
				@Override
				public void doubleClick(DoubleClickEvent event) {
					// MyCardsView.this.runDoubleClick();
				}
			});
		}

		@Override
		protected void makeActions() {
			super.makeActions();
			this.actionViewAs = new ViewAsAction(Arrays.asList(Presentation.values()), this::onViewChange) {
				@Override
				public boolean isChecked(Object object) {
					String cur = getLocalPreferenceStore().getString(PreferenceConstants.PRESENTATION_VIEW);
					if (cur != null && cur.equals(((Presentation) object).key())) {
						return true;
					}
					return super.isChecked();
				}
			};
			if (getPresentation() == Presentation.TABLE)
				getGroupAction().setEnabled(false);
		}

		private void onViewChange(Presentation selected) {
			getLocalPreferenceStore().setValue(PreferenceConstants.PRESENTATION_VIEW, selected.key());
			int i = getPageGroup().getPageIndex(selected.getLabel());
			getPageGroup().setActivePageIndex(i);
			getDeckView().activate();
		}
	}

	public DeckView getDeckView() {
		return (DeckView) getViewPart();
	}

	@Override
	public MagicCardFilter getFilter() {
		return getFilteredStore().getFilter();
	}

	@Override
	public ISelection getSelection() {
		return ((IMagicCardListControl) getActivePage()).getSelection();
	}

	@Override
	public IPersistentPreferenceStore getColumnsPreferenceStore() {
		return getLocalPreferenceStore();
	}

	@Override
	public IPersistentPreferenceStore getElementPreferenceStore() {
		return getFilterPreferenceStore();
	}

	@Override
	public void setNextSelection(ISelection structuredSelection) {
		((IMagicCardListControl) getActivePage()).setNextSelection(structuredSelection);
	}

	@Override
	public void setStatus(String string) {
		((IMagicCardListControl) getActivePage()).setStatus(string);
	}
}
