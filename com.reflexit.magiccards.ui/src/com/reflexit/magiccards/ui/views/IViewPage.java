package com.reflexit.magiccards.ui.views;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.services.IDisposable;

/**
 * Interface which is used for views which multiple pages, only one of which is
 * visible at a time (mostly). These pages can be arranged in tabs or stack
 * layout with some other control that switches them.
 * 
 * @author elaskavaia
 *
 */
public interface IViewPage extends IDisposable {
	/**
	 * Create page contents (composite). Method getControl() should return same
	 * composite as this method returns.
	 */
	public Control createContents(Composite parent);

	/**
	 * Return main page control (created by {@link #createContents(Composite)}
	 * method)
	 */
	public Control getControl();

	/**
	 * Sets parent view
	 */
	public void init(IViewPart view);

	/**
	 * 
	 * @return view part associated with this page
	 */
	public IViewPart getViewPart();

	/**
	 * Method is called when page is activated. This method is responsible for
	 * re-contribution all actions to actions bars, registering context, context
	 * menu and action handlers.
	 */
	public void activate();

	public void deactivate();

	/**
	 * Disposes of this page. All resources must be freed. All listeners must be
	 * detached. Dispose will only be called once during the life cycle of a
	 * object.
	 */
	@Override
	public void dispose();

	public MenuManager getContextMenuManager();

	public void setContextMenuManager(MenuManager menuMgr);

	public ISelectionProvider getSelectionProvider();

	public void saveState(IMemento memento);

	public abstract void refresh();
}
