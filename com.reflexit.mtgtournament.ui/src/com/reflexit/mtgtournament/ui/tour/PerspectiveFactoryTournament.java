package com.reflexit.mtgtournament.ui.tour;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.BaseNewWizardMenu;

import com.reflexit.mtgtournament.ui.tour.views.PlayersView;
import com.reflexit.mtgtournament.ui.tour.views.TNavigatorView;
import com.reflexit.mtgtournament.ui.tour.views.TournamentView;
import com.reflexit.mtgtournament.ui.views.TimerView;

public class PerspectiveFactoryTournament implements IPerspectiveFactory {
	public static String PERSPECTIVE_ID = "com.reflexit.mtgtournament.ui.perspective.tournament";

	public void createInitialLayout(IPageLayout layout) {
		String editorArea = layout.getEditorArea();
		layout.setEditorAreaVisible(false);
		IFolderLayout left = layout.createFolder("left", IPageLayout.LEFT, (float) 0.25, editorArea);
		IFolderLayout right = layout.createFolder("right", IPageLayout.RIGHT, (float) 0.75, editorArea);
		IFolderLayout leftB = layout.createFolder("leftB", IPageLayout.BOTTOM, (float) 0.65, "left");
		left.addView(TNavigatorView.ID);
		leftB.addView(TimerView.ID);
		layout.addShowViewShortcut(TNavigatorView.ID);
		right.addView(TournamentView.ID);
		layout.addShowViewShortcut(TournamentView.ID);
		right.addView(PlayersView.ID);
		layout.addShowViewShortcut(PlayersView.ID);
		//layout.getViewLayout(PlayersView.ID).setCloseable(false);
	}

	/**
	 * Creates a "New..." menu
	 * 
	 * @param window
	 *            - workbench window
	 * @param menu
	 * @return
	 * 
	 */
	public static MenuManager createNewMenu(IWorkbenchWindow window) {
		// create the New submenu, using the same id for it as the New action
		String newText = "New...";
		String newId = ActionFactory.NEW.getId();
		MenuManager newMenu = new MenuManager(newText, newId);
		newMenu.setActionDefinitionId("org.eclipse.ui.file.newQuickMenu"); //$NON-NLS-1$
		newMenu.add(new Separator(newId));
		BaseNewWizardMenu newWizardMenu = new BaseNewWizardMenu(window, null);
		newMenu.add(newWizardMenu);
		newMenu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		return newMenu;
	}
}
