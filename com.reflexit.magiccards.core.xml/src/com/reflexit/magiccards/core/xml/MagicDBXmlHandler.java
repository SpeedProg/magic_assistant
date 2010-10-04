package com.reflexit.magiccards.core.xml;

import java.io.File;
import java.util.ArrayList;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;

import com.reflexit.magiccards.core.Activator;
import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.MagicException;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.Location;
import com.reflexit.magiccards.core.model.nav.MagicDbContainter;
import com.reflexit.magiccards.core.model.storage.AbstractFilteredCardStore;
import com.reflexit.magiccards.core.model.storage.ICardStore;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;

public class MagicDBXmlHandler extends AbstractFilteredCardStore<IMagicCard> {
	private static MagicDBXmlHandler instance;
	private ArrayList<File> files;
	private VirtualMultiFileCardStore table;

	@Override
	public int getSize() {
		return super.getSize();
	}

	public ICardStore<IMagicCard> getCardStore() {
		return this.table;
	}

	private MagicDBXmlHandler() {
		instance = this;
		this.table = new VirtualMultiFileCardStore();
	}

	@Override
	protected void doInitialize() throws MagicException {
		new XmlCardHolder().loadInitialIfNot(new NullProgressMonitor());
		if (!this.table.isInitialized()) {
			synchronized (table) {
				this.files = new ArrayList<File>();
				IResource[] members;
				try {
					MagicDbContainter con = DataManager.getModelRoot().getMagicDBContainer();
					members = con.getContainer().members();
					for (IResource resource : members) {
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
				this.table.initialize();
				this.table.setInitialized(false);
				try {
					for (File file : this.files) {
						Location setLocation = Location.createLocation(file, Location.NO_WHERE);
						this.table.addFile(file, setLocation, true);
					}
				} finally {
					this.table.setInitialized(true);
				}
			}
		}
	}

	public static IFilteredCardStore getInstance() {
		if (instance == null)
			new MagicDBXmlHandler();
		return instance;
	}
}
