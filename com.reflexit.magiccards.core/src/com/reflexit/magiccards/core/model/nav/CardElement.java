package com.reflexit.magiccards.core.model.nav;

import org.eclipse.core.commands.common.EventManager;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import java.io.File;

import com.reflexit.magiccards.core.Activator;
import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.model.events.CardEvent;
import com.reflexit.magiccards.core.model.events.ICardEventListener;

public class CardElement extends EventManager {
	private String filename;
	private CardOrganizer parent;

	public CardElement(String name, CardOrganizer parent) {
		this.filename = name;
		this.parent = parent;
	}

	public IPath getPath() {
		if (this.parent != null) {
			IPath p = this.parent.getPath();
			return p.append(this.filename);
		}
		return new Path(this.filename);
	}

	/**
	 * @return
	 */
	public IResource getResource() {
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
		IPath path = new Path(this.filename).removeFileExtension();
		return path.toString();
	}

	public String getFileName() {
		return this.filename;
	}

	public void setFileName(String name) {
		this.filename = name;
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
}
