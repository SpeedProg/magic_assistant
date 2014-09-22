package com.reflexit.magiccards.core.exports;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.FileUtils;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.monitor.ICoreProgressMonitor;
import com.reflexit.magiccards.core.test.assist.MemCardHandler;
import com.reflexit.magiccards.core.test.assist.TestFileUtils;

public class AbstarctImportTest extends junit.framework.TestCase {
	protected MemCardHandler deck;
	protected String line = "";
	protected int resSize;
	protected IMagicCard card1;
	protected IMagicCard card2;
	protected IMagicCard card3;
	protected IMagicCard cardN;
	protected List<IMagicCard> result;
	protected boolean virtual = true;
	protected boolean resolve = true;

	@Override
	protected void setUp() throws Exception {
		this.deck = new MemCardHandler();
	}

	static {
		try {
			DataManager.getInstance().reset();
		} catch (NullPointerException e) {
			setLocalDbDir();
		}
	}

	protected static void setLocalDbDir() {
		File temp = new File("/tmp/magiccards");
		FileUtils.deleteTree(temp);
		DataManager.getInstance().reset(temp);
		DataManager.getModelRoot().clear();
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

	protected void parse(IImportDelegate<IMagicCard> worker) {
		parse(true, worker);
	}

	protected void parse(boolean header, IImportDelegate<IMagicCard> worker) {
		try {
			if (resolve == false)
				throw new IllegalArgumentException("Cannot test");
			ImportUtils.performImport(new ByteArrayInputStream(line.getBytes()), worker, header, virtual, deck.getLocation(),
					deck.getCardStore(), ICoreProgressMonitor.NONE);
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		result = extractStorageCards();
		setout(result);
	}

	protected void preview(boolean header, IImportDelegate<IMagicCard> worker) {
		try {
			ImportUtils.performPreImport(new ByteArrayInputStream(line.getBytes()), worker, header, virtual, deck.getLocation(), resolve,
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

	protected String getAboveComment() {
		return getContents(1)[0].toString();
	}

	protected StringBuilder[] getContents(int sections) {
		try {
			return TestFileUtils.getContentsForTest("src", getClass(), getName(), sections);
		} catch (Exception e) {
			fail(e.getMessage());
			return null;
		}
	}
}
