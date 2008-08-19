package com.reflexit.magiccards.core.model.nav;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

public class CollectionsContainer extends CardOrganizer {
	public CollectionsContainer(String name, CardOrganizer parent) {
		super(name, parent);
	}

	public void loadChildren() throws CoreException {
		IResource[] members = getContainer().members();
		for (int i = 0; i < members.length; i++) {
			IResource mem = members[i];
			String name = mem.getName();
			if (mem instanceof IContainer) {
				CollectionsContainer con = new CollectionsContainer(name, this);
				con.loadChildren();
			} else if (mem != null) {
				if (name.endsWith(".xml")) {
					if (!this.contains(name))
						new CardCollection(name, this);
				}
			}
		}
	}

	@Override
	public CardElement newElement(String name, CardOrganizer parent) {
		return new CollectionsContainer(name + ".xml", parent);
	}
}
