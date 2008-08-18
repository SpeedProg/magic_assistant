package com.reflexit.magiccards.ui;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.IPlaceholderFolderLayout;

import com.reflexit.magiccards.ui.views.MagicDbView;
import com.reflexit.magiccards.ui.views.card.CardDescView;
import com.reflexit.magiccards.ui.views.lib.DeckView;
import com.reflexit.magiccards.ui.views.lib.LibView;
import com.reflexit.magiccards.ui.views.nav.CardsNavigatorView;
import com.reflexit.magiccards.ui.wizards.NewCardCollectionWizard;
import com.reflexit.magiccards.ui.wizards.NewCollectionContainerWizard;
import com.reflexit.magiccards.ui.wizards.NewDeckContainerWizard;
import com.reflexit.magiccards.ui.wizards.NewDeckWizard;

public class PerspectiveFactoryMagic implements IPerspectiveFactory {
	public static String PERSPECTIVE_ID = "com.reflexit.magiccards.ui.perspective.magic";

	public void createInitialLayout(IPageLayout layout) {
		String editorArea = layout.getEditorArea();
		layout.setEditorAreaVisible(false);
		IFolderLayout left = layout.createFolder("left", IPageLayout.LEFT, (float) 0.25, editorArea);
		IFolderLayout right = layout.createFolder("right", IPageLayout.RIGHT, (float) 0.75, editorArea);
		IPlaceholderFolderLayout pf = layout.createPlaceholderFolder("up", IPageLayout.TOP, 0.5f, "right");
		left.addView(CardDescView.ID);
		left.addView(CardsNavigatorView.ID);
		right.addView(MagicDbView.ID);
		right.addView(LibView.ID);
		pf.addPlaceholder(DeckView.ID + ":*");
		// layout.addStandaloneView(MagicDbView.ID, false, IPageLayout.LEFT,
		// 1.0f, editorArea);
		layout.addShowViewShortcut(CardDescView.ID);
		layout.addShowViewShortcut(MagicDbView.ID);
		layout.addShowViewShortcut(CardsNavigatorView.ID);
		layout.addShowViewShortcut(LibView.ID);
		layout.addNewWizardShortcut(NewDeckWizard.ID);
		layout.addNewWizardShortcut(NewDeckContainerWizard.ID);
		layout.addNewWizardShortcut(NewCardCollectionWizard.ID);
		layout.addNewWizardShortcut(NewCollectionContainerWizard.ID);
		layout.getViewLayout(MagicDbView.ID).setCloseable(false);
		layout.getViewLayout(CardDescView.ID).setCloseable(false);
		layout.getViewLayout(CardsNavigatorView.ID).setCloseable(false);
	}
}
