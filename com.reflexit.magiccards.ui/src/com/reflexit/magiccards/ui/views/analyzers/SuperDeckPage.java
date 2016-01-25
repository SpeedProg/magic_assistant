package com.reflexit.magiccards.ui.views.analyzers;

import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.preference.IPersistentPreferenceStore;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;

import com.reflexit.magiccards.core.model.MagicCardFilter;
import com.reflexit.magiccards.ui.views.AbstractGroupPageCardsViewPage;
import com.reflexit.magiccards.ui.views.IMagicCardListControl;
import com.reflexit.magiccards.ui.views.Presentation;
import com.reflexit.magiccards.ui.views.ViewPageContribution;
import com.reflexit.magiccards.ui.views.lib.DeckListControl;
import com.reflexit.magiccards.ui.views.lib.DeckView;
import com.reflexit.magiccards.ui.views.lib.IDeckPage;
import com.reflexit.magiccards.ui.views.lib.PresentationComboContributionItem;

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

	protected void addPage(Presentation pres) {
		getPageGroup().add(new ViewPageContribution("", pres.getLabel(), null, new DeckPagePresentation(pres)));
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
		public void fillLocalToolBar(IToolBarManager manager) {
			manager.add(new PresentationComboContributionItem(getPresentation().getLabel()) {
				@Override
				protected void onSelect(String text) {
					int i = getPageGroup().getPageIndex(text);
					getPageGroup().setActivePageIndex(i);
					getDeckView().activate();
				}
			});
			super.fillLocalToolBar(manager);
		}

		@Override
		protected void makeActions() {
			super.makeActions();
			if (getPresentation() == Presentation.TABLE)
				getGroupAction().setEnabled(false);
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
