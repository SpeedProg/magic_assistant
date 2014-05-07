package com.reflexit.mtgtournament.ui.app;

import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;

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
		this.windowAction = ActionFactory.PREFERENCES.create(window);
		register(this.windowAction);
		this.aboutAction = ActionFactory.ABOUT.create(window);
		register(this.aboutAction);
		this.newAction = ActionFactory.NEW.create(window);
		register(this.newAction);
		this.resetAction = ActionFactory.RESET_PERSPECTIVE.create(window);
		register(this.resetAction);
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
		winMenu.add(this.resetAction);
		winMenu.add(this.windowAction);
		// file
		fileMenu.add(this.exitAction);
		// help
		helpMenu.add(this.help_contents);
		helpMenu.add(this.aboutAction);
	}
}
