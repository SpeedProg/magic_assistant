package com.reflexit.magiccards.core.model.nav;

import org.eclipse.core.commands.common.EventManager;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import java.io.File;

import com.reflexit.magiccards.core.Activator;
import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.MagicException;
import com.reflexit.magiccards.core.model.events.CardEvent;
import com.reflexit.magiccards.core.model.events.ICardEventListener;

public abstract class CardElement extends EventManager {
	private String name; // name
	private IPath path; // project relative path
	private CardOrganizer parent;

	public CardElement(String name, IPath path, CardOrganizer parent) {
		this.name = name;
		this.path = path;
		this.parent = parent;
		if (parent != null) {
			parent.addChild(this);
		}
		try {
			File file = getFile();
			if (!file.exists()) {
				if (!(this instanceof CardOrganizer))
					file.createNewFile();
				else
					file.mkdir();
			}
			if (parent != null && parent.getResource() != null) {
				parent.getResource().refreshLocal(1, null);
			}
		} catch (Exception e) {
			throw new MagicException(e);
		}
	}

	public CardElement(String filename, CardOrganizer parent) {
		this(nameFromFile(filename), parent == null ? new Path(filename) : parent.getPath().append(filename), parent);
	}

	public IPath getPath() {
		return this.path;
	}

	public String getLocation() {
		return getPath().toPortableString();
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

	public void setParent(CardOrganizer parent) {
		this.parent = parent;
	}

	public void addListener(ICardEventListener lis) {
		addListenerObject(lis);
	}

	public void removeListener(ICardEventListener lis) {
		removeListenerObject(lis);
	}

	protected void fireEvent(CardEvent event) {
		Object[] listeners = getListeners();
		for (int i = 0; i < listeners.length; i++) {
			ICardEventListener lis = (ICardEventListener) listeners[i];
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

	/* (non-Javadoc)
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
		if (getParent() != null) {
			getParent().removeChild(this);
		}
		if (getResource() != null) {
			try {
				getResource().move(new Path(value + ".xml"), true, null);
				return newElement(value, getParent());
			} catch (CoreException e) {
				Activator.log(e);
			}
		}
		return null;
	}

	public abstract CardElement newElement(String name2, CardOrganizer parent2);
}
