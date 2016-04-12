package com.reflexit.magiccards.ui.actions;

import java.util.Collection;
import java.util.function.Consumer;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.resource.ImageDescriptor;

public class DropDownAction<T> extends Action implements IPlainLabelProvider {
	private Consumer<T> reload;
	private Collection<T> elements;
	private T selected;

	public DropDownAction(Collection<T> pres, String name, ImageDescriptor desc, Consumer<T> reload) {
		super(name, IAction.AS_DROP_DOWN_MENU);
		this.reload = reload;
		this.elements = pres;
		setImageDescriptor(desc);
		setMenuCreator(new MenuCreator(this::createMenuManager));
		selected = getDefault();
	}

	@Override
	public void run() { // button itself
		actionOnSelectItem(getDefault());
	}

	protected T getDefault() {
		return elements.iterator().next();
	}

	protected void populateMenu(IMenuManager groupMenu) {
		for (T pres : elements) {
			groupMenu.add(createItemAction(pres));
		}
	}

	public MenuManager createMenuManager() {
		MenuManager groupMenu = new MenuManager(getText(), getImageDescriptor(), null);
		groupMenu.setRemoveAllWhenShown(true);
		groupMenu.addMenuListener(new IMenuListener() {
			@Override
			public void menuAboutToShow(IMenuManager manager) {
				populateMenu(manager);
			}
		});
		return groupMenu;
	}

	public Action createItemAction(T pres) {
		return new RadioAction<>(pres, isChecked(pres), this::actionOnSelectItem, this);
	}

	@Override
	public String getText(Object element) {
		return String.valueOf(element);
	}

	public boolean isChecked(Object element) {
		return element == getDefault();
	}

	protected void actionOnSelectItem(T pres) {
		selected = pres;
		if (reload != null)
			reload.accept(selected);
	}

	protected T getSelected() {
		return selected;
	}
}