package com.reflexit.magiccards.core.exports;

import java.net.URL;
import java.util.List;

import org.junit.Test;

import com.reflexit.magiccards.core.monitor.ICoreProgressMonitor;
import com.reflexit.magiccards.core.sync.WebUtils;

public class MagicCardsInfoImportTest extends AbstarctImportTest {
	private MagicCardsInfoImportDelegate worker = new MagicCardsInfoImportDelegate();

	private void parse() {
		parse(worker);
	}

	private void preview() {
		exception = null;
		preview(worker);
		if (exception != null)
			fail(exception.getMessage());
	}

	protected void previewUrl(String url) {
		try {
			ImportData importData = new ImportData();
			importData.setImportSource(ImportSource.URL);
			importData.setProperty(ImportSource.URL.name(), url);
			URL uurl = new URL(url);
			importData.setText(WebUtils.openUrlText(uurl));
			ImportUtils.performPreImport(worker, importData, ICoreProgressMonitor.NONE);
		} catch (Exception e) {
			fail(e.getMessage());
		}
		result = (List) worker.getResult().getList();
		exception = worker.getResult().getError();
		setout(result);
	}

	@Test
	public void testVlist() {
		previewUrl("http://magiccards.info/query?q=e%3Awmcq%2Fen&v=list&s=cname");
		assertEquals(3, resSize);
	}

	@Test
	public void testVlist2() {
		previewUrl("http://magiccards.info/query?q=e:wmcq/en&v=list&s=cname");
		assertEquals(3, resSize);
	}

	@Test
	public void testVspoiler() {
		previewUrl("http://magiccards.info/query?q=e:wmcq/en&v=spoiler&s=cname");
		assertEquals(3, resSize);
	}
}
