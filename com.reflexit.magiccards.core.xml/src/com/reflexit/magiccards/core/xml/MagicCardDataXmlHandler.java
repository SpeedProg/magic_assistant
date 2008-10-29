package com.reflexit.magiccards.core.xml;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;

import java.io.File;
import java.util.ArrayList;

import com.reflexit.magiccards.core.Activator;
import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.MagicException;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.nav.MagicDbContainter;
import com.reflexit.magiccards.core.model.storage.AbstractFilteredCardStore;
import com.reflexit.magiccards.core.model.storage.ICardStore;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;

public class MagicCardDataXmlHandler extends AbstractFilteredCardStore<IMagicCard> {
	private static MagicCardDataXmlHandler instance;
	private ArrayList<File> files;
	private MultiFileCardStore table;

	@Override
	public int getSize() {
		return super.getSize();
	}

	public ICardStore<IMagicCard> getCardStore() {
		return this.table;
	}

	private MagicCardDataXmlHandler() {
		instance = this;
		this.table = new MultiFileCardStore();
	}

	@Override
	protected void doInitialize() throws MagicException {
		this.files = new ArrayList<File>();
		IResource[] members;
		try {
			new XmlCardHolder().loadInitialIfNot(new NullProgressMonitor());
			MagicDbContainter con = DataManager.getModelRoot().getMagicDBContainer();
			members = con.getContainer().members();
			for (int i = 0; i < members.length; i++) {
				IResource resource = members[i];
				File file = resource.getLocation().toFile();
				if (file.getName().endsWith(".xml"))
					this.files.add(file);
			}
		} catch (CoreException e) {
			Activator.log(e);
			return;
		} catch (MagicException e) {
			Activator.log(e);
			return;
		}
		// init super
		for (File file : this.files) {
			this.table.addFile(file, file.getName());
		}
		this.table.initialize();
	}

	public static IFilteredCardStore getInstance() {
		if (instance == null)
			new MagicCardDataXmlHandler();
		return instance;
	}
}
