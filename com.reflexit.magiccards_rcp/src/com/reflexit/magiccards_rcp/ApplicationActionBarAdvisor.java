package com.reflexit.magiccards_rcp;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.actions.ContributionItemFactory;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.internal.WorkbenchMessages;

import com.reflexit.magiccards.ui.PerspectiveFactoryMagic;
import com.reflexit.mtgtournament.ui.tour.PerspectiveFactoryTournament;

/**
 * An action bar advisor is responsible for creating, adding, and disposing of
 * the actions added to a workbench window. Each window will be populated with
 * new actions.
 */
public class ApplicationActionBarAdvisor extends ActionBarAdvisor {
	// Actions - important to allocate these only in makeActions, and then use
	// them
	// in the fill methods. This ensures that the actions aren't recreated
	// when fillActionBars is called with FILL_PROXY.
	private IWorkbenchAction exitAction;
	private IWorkbenchAction help_contents;
	private IWorkbenchAction windowAction;
	private IWorkbenchAction aboutAction;
	private IWorkbenchAction newAction;
	private IWorkbenchWindow window;
	private IAction resetAction;
	private IWorkbenchAction searchHelpAction;
	private IWorkbenchAction dynamicHelpAction;
	private IWorkbenchAction importAction;
	private IAction exportAction;
	private MenuManager showViewMenuMgr;
	private IContributionItem showViewItem;

	public ApplicationActionBarAdvisor(IActionBarConfigurer configurer) {
		super(configurer);
		this.window = configurer.getWindowConfigurer().getWindow();
	}

	/**
	 * Returns the window to which this action builder is contributing.
	 */
	private IWorkbenchWindow getWindow() {
		return this.window;
	}

	@Override
	protected void makeActions(final IWorkbenchWindow window) {
		// Creates the actions and registers them.
		// Registering is needed to ensure that key bindings work.
		// The corresponding commands keybindings are defined in the plugin.xml
		// file.
		// Registering also provides automatic disposal of the actions when
		// the window is closed.
		this.exitAction = ActionFactory.QUIT.create(window);
		register(this.exitAction);
		this.help_contents = ActionFactory.HELP_CONTENTS.create(window);
		register(this.help_contents);
		searchHelpAction = ActionFactory.HELP_SEARCH.create(window);
		register(searchHelpAction);
		dynamicHelpAction = ActionFactory.DYNAMIC_HELP.create(window);
		register(dynamicHelpAction);
		this.windowAction = ActionFactory.PREFERENCES.create(window);
		register(this.windowAction);
		this.aboutAction = ActionFactory.ABOUT.create(window);
		register(this.aboutAction);
		this.newAction = ActionFactory.NEW.create(window);
		register(this.newAction);
		this.resetAction = ActionFactory.RESET_PERSPECTIVE.create(window);
		register(this.resetAction);
		this.importAction = ActionFactory.IMPORT.create(window);
		register(this.importAction);
		this.exportAction = ActionFactory.EXPORT.create(window);
		register(this.exportAction);
		showViewMenuMgr = new MenuManager("Show View", "showView");
		showViewItem = ContributionItemFactory.VIEWS_SHORTLIST.create(window);
	}

	class OpenPerspectiveAction extends Action {
		private String perspectiveId;

		OpenPerspectiveAction(String id, String name) {
			super(name);
			this.perspectiveId = id;
		}

		@Override
		public void run() {
			final IWorkbench workbench = PlatformUI.getWorkbench();
			try {
				workbench.showPerspective(perspectiveId, window, ResourcesPlugin.getWorkspace());
			} catch (WorkbenchException e) {
				ErrorDialog.openError(new Shell(), WorkbenchMessages.ChangeToPerspectiveMenu_errorTitle, e.getMessage(), e.getStatus());
			}
		}
	}

	@Override
	protected void fillMenuBar(IMenuManager menuBar) {
		MenuManager fileMenu = new MenuManager("&File", IWorkbenchActionConstants.M_FILE);
		MenuManager winMenu = new MenuManager("&Window", IWorkbenchActionConstants.M_WINDOW);
		MenuManager helpMenu = new MenuManager("&Help", IWorkbenchActionConstants.M_HELP);
		menuBar.add(fileMenu);
		menuBar.add(winMenu);
		// Add a group marker indicating where action set menus will appear.
		menuBar.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
		menuBar.add(helpMenu);
		// windows
		winMenu.add(new OpenPerspectiveAction(PerspectiveFactoryMagic.PERSPECTIVE_ID, "Cards Organizer"));
		winMenu.add(new OpenPerspectiveAction(PerspectiveFactoryTournament.PERSPECTIVE_ID, "Tournament Organizer"));
		winMenu.add(new Separator());

		showViewMenuMgr.add(showViewItem);
		winMenu.add(showViewMenuMgr);
		winMenu.add(this.resetAction);
		winMenu.add(new Separator());
		winMenu.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
		winMenu.add(this.windowAction);
		// file
		fileMenu.add(PerspectiveFactoryMagic.createNewMenu(getWindow()));
		fileMenu.add(new Separator());
		fileMenu.add(this.importAction);
		fileMenu.add(this.exportAction);
		fileMenu.add(new Separator());
		fileMenu.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
		fileMenu.add(this.exitAction);
		// help
		helpMenu.add(this.help_contents);
		helpMenu.add(searchHelpAction);
		helpMenu.add(dynamicHelpAction);
		helpMenu.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
		helpMenu.add(this.aboutAction);
	}
}
