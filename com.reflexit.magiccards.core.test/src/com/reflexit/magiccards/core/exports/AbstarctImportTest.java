package com.reflexit.magiccards.core.exports;

import java.io.ByteArrayInputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.monitor.ICoreProgressMonitor;
import com.reflexit.magiccards.core.test.assist.AbstractMagicTest;
import com.reflexit.magiccards.core.test.assist.MemCardHandler;
import com.reflexit.magiccards.core.test.assist.TestFileUtils;

@FixMethodOrder(MethodSorters.JVM)
public class AbstarctImportTest extends AbstractMagicTest {
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
	protected Exception exception;

	@Before
	public void setUp() throws Exception {
		this.deck = new MemCardHandler();
	}

	@BeforeClass
	public static void setUpBeforeClass() {
		TestFileUtils.resetDb();
		DataManager.getInstance().waitForInit(10);
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
			parseonly(header, worker);
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	protected void parseonly(boolean header, IImportDelegate<IMagicCard> worker) throws InvocationTargetException, InterruptedException {
		if (resolve == false)
			throw new IllegalArgumentException("Cannot test");
		ImportUtils.performImport(
				new ByteArrayInputStream(line.getBytes()), worker, header,
				virtual, deck.getLocation(), deck.getCardStore(),
				ICoreProgressMonitor.NONE);
		result = extractStorageCards();
		setout(result);
	}

	protected void preview(boolean header, IImportDelegate<IMagicCard> worker) {
		try {
			ImportUtils.performPreImport(
					new ByteArrayInputStream(line.getBytes()), worker, header,
					virtual, deck.getLocation(), resolve,
					ICoreProgressMonitor.NONE);
		} catch (Exception e) {
			fail(e.getMessage());
		}
		result = (List) worker.getResult().getList();
		exception = worker.getResult().getError();
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
		for (Object element : preimport)
			cardN = (IMagicCard) element;
	}

	protected void addLine(String string) {
		line += string + "\n";
	}

	@Override
	protected String getAboveComment() {
		return getContents(1)[0].toString();
	}

	@Override
	protected StringBuilder[] getContents(int sections) {
		try {
			return TestFileUtils.getContentsForTest("src", getClass(),
					getName(), sections);
		} catch (Exception e) {
			fail(e.getMessage());
			return null;
		}
	}
}
