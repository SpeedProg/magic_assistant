package com.reflexit.magiccards.ui.views;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.swt.graphics.Image;

public class ViewPageContribution {
	private String name;
	private String id;
	private Image image;
	private IViewPage instance;
	private IConfigurationElement conf;

	public ViewPageContribution(String id, String name, Image image, IViewPage instance) {
		super();
		this.name = name;
		this.id = id;
		this.image = image;
		this.instance = instance;
	}

	public ViewPageContribution(String id, String name, IConfigurationElement conf) {
		this(id, name, null, (IViewPage) null);
		this.conf = conf;
	}

	public boolean isInstantiated() {
		return instance != null;
	}

	public synchronized IViewPage getViewPage() {
		if (instance == null)
			instance = instantiate();
		return instance;
	}

	private IViewPage instantiate() {
		try {
			return (IViewPage) conf.createExecutableExtension("class");
		} catch (CoreException e) {
			throw new IllegalArgumentException(e);
		}
	}

	public static ViewPageContribution parseElement(IConfigurationElement elp) {
		String id = elp.getAttribute("id");
		String name = elp.getAttribute("name");
		ViewPageContribution page = new ViewPageContribution(id, name, elp);
		return page;
	}

	public String getName() {
		return name;
	}
}
