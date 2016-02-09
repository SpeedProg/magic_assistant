package com.reflexit.magiccards.core.exports;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.reflexit.magiccards.core.sync.TextPrinterTest;

@RunWith(Suite.class)
@SuiteClasses({
	TablePipedImportTest.class,
	MtgoImportTest.class,
	MagicWorkstationImportTest.class,
	DeckParserTest.class,
	ImportUtilsTest.class,
	ManaDeckImportTest.class,
	ShandalarImportTest.class,
	MTGStudioImportTest.class,
	PipedTableExportText.class,
	CsvExportDelegateTest.class,
	CsvImportDelegateTest.class,
	ClassicExportDelegateTest.class,
	ClassicImportDelegateTest.class,
	CustomExportDelegateTest.class,
	DeckBoxImportTest.class,
	TextPrinterTest.class,
		HtmlTableImportTest.class, //
		ScryGlassImportDelegateTest.class
})
public class ExportImportSuite {
}
