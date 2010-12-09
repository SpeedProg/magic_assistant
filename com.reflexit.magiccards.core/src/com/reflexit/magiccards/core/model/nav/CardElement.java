package com.reflexit.magiccards.core.model.nav;

import java.io.File;

import org.eclipse.core.commands.common.EventManager;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import com.reflexit.magiccards.core.Activator;
import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.MagicException;
import com.reflexit.magiccards.core.model.Location;
import com.reflexit.magiccards.core.model.events.CardEvent;
import com.reflexit.magiccards.core.model.events.ICardEventListener;

public abstract class CardElement extends EventManager {
	private String name; // name
	private IPath path; // project relative path
	private CardOrganizer parent;

	public CardElement(String name, IPath path) {
		this.name = name;
		this.path = path;
	}

	protected void setParentInit(CardOrganizer parent) {
		this.parent = parent;
		if (parent != null) {
			if (parent != DataManager.getModelRoot())
				this.path = parent.getPath().append(path.lastSegment());
			parent.addChild(this);
			if (parent.getResource() != null) {
				try {
					parent.getResource().refreshLocal(1, null);
				} catch (Exception e) {
					throw new MagicException(e);
				}
			}
		}
	}

	public CardElement(String filename, CardOrganizer parent, boolean addToParent) {
		this(nameFromFile(filename), parent == null ? new Path(filename) : parent.getPath().append(filename));
		if (addToParent)
			setParentInit(parent);
	}

	public IPath getPath() {
		return this.path;
	}

	public Location getLocation() {
		return Location.createLocation(getPath());
	}

	/**
	 * @return
	 */
	public IResource getResource() {
		if (this.path == null)
			return null;
		try {
			return DataManager.getProject().findMember(getPath());
		} catch (CoreException e) {
			return null;
		}
	}

	/**
	 * @return
	 * @throws CoreException
	 */
	public File getFile() throws CoreException {
		IPath p = getPath();
		if (p == null)
			return null;
		IPath projectLoc = DataManager.getProject().getLocation();
		IPath full = projectLoc.append(p);
		return full.toFile();
	}

	public String getName() {
		return this.name;
	}

	public CardOrganizer getParent() {
		return this.parent;
	}

	public void addListener(ICardEventListener lis) {
		addListenerObject(lis);
	}

	public void removeListener(ICardEventListener lis) {
		removeListenerObject(lis);
	}

	protected void fireEvent(CardEvent event) {
		Object[] listeners = getListeners();
		for (Object listener : listeners) {
			ICardEventListener lis = (ICardEventListener) listener;
			try {
				lis.handleEvent(event);
			} catch (Throwable t) {
				Activator.log(t);
			}
		}
		if (this.parent != null)
			this.parent.fireEvent(event);
	}

	public void remove() {
		if (getParent() != null) {
			getParent().removeChild(this);
		}
		if (getResource() != null) {
			try {
				getResource().delete(true, null);
			} catch (CoreException e) {
				Activator.log(e);
			}
		}
	}

	public static String nameFromFile(String filename) {
		return new Path(filename).removeFileExtension().lastSegment();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return getName();
	}

	/**
	 * @param value
	 * @return
	 */
	public CardElement rename(String value) {
		Location oldName = getLocation();
		if (getParent() != null) {
			getParent().removeChild(this);
		}
		if (getResource() != null) {
			try {
				getResource().move(new Path(value + ".xml"), true, null);
			} catch (CoreException e) {
				Activator.log(e);
			}
		}
		CardElement x = newElement(value, getParent());
		fireEvent(new CardEvent(x, CardEvent.RENAME_CONTAINER, oldName));
		return x;
	}

	public CardElement newParent(CardOrganizer parent) {
		Location oldName = getLocation();
		if (getParent() != null) {
			getParent().removeChild(this);
		}
		if (getResource() != null) {
			try {
				IPath newPath = DataManager.getProject().getFullPath()
						.append(parent.getPath().addTrailingSeparator().append(getPath().lastSegment()));
				getResource().move(newPath, true, null);
			} catch (CoreException e) {
				Activator.log(e);
			}
		}
		setParentInit(parent);
		fireEvent(new CardEvent(this, CardEvent.RENAME_CONTAINER, oldName));
		return this;
	}

	public abstract CardElement newElement(String name, CardOrganizer parent);
}
