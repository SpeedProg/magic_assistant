package com.reflexit.magiccards.core.exports;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.FileUtils;
import com.reflexit.magiccards.core.model.Editions;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.monitor.ICoreProgressMonitor;
import com.reflexit.magiccards.core.test.assist.MemCardHandler;

public class AbstarctImportTest extends junit.framework.TestCase {
	protected MemCardHandler deck;
	protected String line = "";
	protected int resSize;
	protected IMagicCard card1;
	protected IMagicCard card2;
	protected IMagicCard card3;
	protected IMagicCard cardN;
	protected List<IMagicCard> result;

	@Override
	protected void setUp() throws Exception {
		this.deck = new MemCardHandler();
	}

	{
		File file = new File(FileUtils.getStateLocationFile(), Editions.EDITIONS_FILE);
		file.delete();
		try {
			DataManager.getRootDir();
		} catch (NullPointerException e) {
			File temp = new File("/tmp/magiccards");
			DataManager.reset(temp);
			DataManager.getModelRoot().clear();
		}
	}

	protected ArrayList<IMagicCard> extractStorageCards() {
		ArrayList<IMagicCard> res = new ArrayList<IMagicCard>();
		Iterator<IMagicCard> iterator = this.deck.getCardStore().iterator();
		for (; iterator.hasNext();) {
			IMagicCard card = iterator.next();
			res.add(card);
		}
		return res;
	}

	protected void parse(boolean header, IImportDelegate<IMagicCard> worker) {
		try {
			ImportUtils.performImport(new ByteArrayInputStream(line.getBytes()), worker, header, deck.getLocation(), deck.getCardStore(),
					ICoreProgressMonitor.NONE);
		} catch (Exception e) {
			fail(e.getMessage());
		}
		result = extractStorageCards();
		setout(result);
	}

	protected void preview(boolean header, IImportDelegate<IMagicCard> worker) {
		try {
			ImportUtils.performPreview(new ByteArrayInputStream(line.getBytes()), worker, header, deck.getLocation(),
					ICoreProgressMonitor.NONE);
		} catch (Exception e) {
			fail(e.getMessage());
		}
		result = (List) worker.getPreview().getList();
		setout(result);
	}

	public void setout(Collection<IMagicCard> preimport) {
		resSize = preimport.size();
		Iterator<IMagicCard> iter = preimport.iterator();
		if (resSize >= 1)
			card1 = iter.next();
		if (resSize >= 2)
			card2 = iter.next();
		if (resSize >= 3)
			card3 = iter.next();
		for (Iterator iterator = preimport.iterator(); iterator.hasNext();) {
			cardN = (IMagicCard) iterator.next();
		}
	}

	protected void addLine(String string) {
		line += string + "\n";
	}
}
