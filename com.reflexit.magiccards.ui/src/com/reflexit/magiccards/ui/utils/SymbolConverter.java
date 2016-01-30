package com.reflexit.magiccards.ui.utils;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Control;

import com.reflexit.magiccards.core.MagicException;
import com.reflexit.magiccards.core.sync.GatherHelper;
import com.reflexit.magiccards.ui.MagicUIActivator;

public class SymbolConverter {
	private static final int SYMBOL_SIZE = 15;
	private static String bundleBase;
	static Map<String, String> manaMap = new HashMap<String, String>();
	static {
		manaMap.put("{T}", "icons/tap.gif");
		manaMap.put("{Q}", "icons/untap.gif");
		manaMap.put("{U}", "icons/mana/Symbol_U_mana.gif");
		manaMap.put("{W}", "icons/mana/Symbol_W_mana.gif");
		manaMap.put("{B}", "icons/mana/Symbol_B_mana.gif");
		manaMap.put("{G}", "icons/mana/Symbol_G_mana.gif");
		manaMap.put("{R}", "icons/mana/Symbol_R_mana.gif");
		manaMap.put("{C}", "icons/mana/Symbol_C_mana.png");
		manaMap.put("{S}", "icons/mana/Symbol_snow_mana.gif");
		manaMap.put("{X}", "icons/mana/Symbol_X_mana.gif");
		manaMap.put("{Y}", "icons/mana/Symbol_Y_mana.gif");
		manaMap.put("{Z}", "icons/mana/Symbol_Z_mana.gif");
		manaMap.put("{0.5}", "icons/mana/Symbol_500_mana.gif");
		manaMap.put("{05}", "icons/mana/Symbol_500_mana.gif");
		manaMap.put("{500}", "icons/mana/Symbol_500_mana.gif");
		manaMap.put("{R/W}", "icons/mana/Symbol_RW_mana.gif");
		manaMap.put("{R/G}", "icons/mana/Symbol_RG_mana.gif");
		manaMap.put("{B/R}", "icons/mana/Symbol_BR_mana.gif");
		manaMap.put("{B/G}", "icons/mana/Symbol_BG_mana.gif");
		manaMap.put("{G/U}", "icons/mana/Symbol_GU_mana.gif");
		manaMap.put("{G/W}", "icons/mana/Symbol_GW_mana.gif");
		manaMap.put("{W/U}", "icons/mana/Symbol_WU_mana.gif");
		manaMap.put("{W/B}", "icons/mana/Symbol_WB_mana.gif");
		manaMap.put("{U/B}", "icons/mana/Symbol_UB_mana.gif");
		manaMap.put("{U/R}", "icons/mana/Symbol_UR_mana.gif");
		manaMap.put("{0}", "icons/mana/Symbol_0_mana.gif");
		manaMap.put("{1}", "icons/mana/Symbol_1_mana.gif");
		manaMap.put("{2}", "icons/mana/Symbol_2_mana.gif");
		manaMap.put("{3}", "icons/mana/Symbol_3_mana.gif");
		manaMap.put("{4}", "icons/mana/Symbol_4_mana.gif");
		manaMap.put("{5}", "icons/mana/Symbol_5_mana.gif");
		manaMap.put("{6}", "icons/mana/Symbol_6_mana.gif");
		manaMap.put("{7}", "icons/mana/Symbol_7_mana.gif");
		manaMap.put("{8}", "icons/mana/Symbol_8_mana.gif");
		manaMap.put("{9}", "icons/mana/Symbol_9_mana.gif");
		manaMap.put("{10}", "icons/mana/Symbol_10_mana.gif");
		manaMap.put("{11}", "icons/mana/Symbol_11_mana.gif");
		manaMap.put("{12}", "icons/mana/Symbol_12_mana.gif");
		manaMap.put("{14}", "icons/mana/Symbol_14_mana.gif");
		manaMap.put("{15}", "icons/mana/Symbol_15_mana.gif");
		manaMap.put("{16}", "icons/mana/Symbol_16_mana.gif");
		manaMap.put("{1000000}", "icons/mana/Symbol_1000000_mana.gif");
		manaMap.put("{2/W}", "icons/mana/Symbol_2W_mana.gif");
		manaMap.put("{2/U}", "icons/mana/Symbol_2U_mana.gif");
		manaMap.put("{2/B}", "icons/mana/Symbol_2B_mana.gif");
		manaMap.put("{2/G}", "icons/mana/Symbol_2G_mana.gif");
		manaMap.put("{2/R}", "icons/mana/Symbol_2R_mana.gif");
		manaMap.put("{UP}", "icons/mana/Symbol_UP_mana.gif");
		manaMap.put("{WP}", "icons/mana/Symbol_WP_mana.gif");
		manaMap.put("{BP}", "icons/mana/Symbol_BP_mana.gif");
		manaMap.put("{GP}", "icons/mana/Symbol_GP_mana.gif");
		manaMap.put("{RP}", "icons/mana/Symbol_RP_mana.gif");
	}
	static {
		// init
		try {
			URL url = MagicUIActivator.getDefault().getBundle().getEntry("/");
			bundleBase = FileLocator.resolve(url).toString();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static String getHtmlStyle(Control con) {
		FontData fontData = con.getFont().getFontData()[0];
		int height = fontData.getHeight();
		String fontName = fontData.getName();
		RGB rgb = con.getBackground().getRGB();
		String bgColor = "rgb(" + rgb.red + "," + rgb.green + "," + rgb.blue + ")";
		rgb = con.getForeground().getRGB();
		String fgColor = "rgb(" + rgb.red + "," + rgb.green + "," + rgb.blue + ")";
		String style = "font-size:" + height + "pt;" + "background-color: " + bgColor + ";" + "color: "
				+ fgColor + ";" + "font-family:"
				+ fontName + ";";
		return style;
	}

	public static Image buildCostImage(String cost) {
		if (cost == null || cost.length() == 0)
			return null;
		ImageRegistry imageRegistry = MagicUIActivator.getDefault().getImageRegistry();
		String key = "[" + cost;
		Image costImage = imageRegistry.get(key);
		if (costImage != null)
			return costImage;
		try {
			Collection<Image> manaImages = getManaImages(cost);
			costImage = ImageCreator.joinImages(manaImages, SYMBOL_SIZE * 10, SYMBOL_SIZE);
			return costImage;
		} catch (Exception e) {
			MagicUIActivator.log(e);
			costImage = ImageCreator.createTransparentImage(SYMBOL_SIZE * cost.length() / 2, SYMBOL_SIZE);
			GC gc = new GC(costImage);
			gc.drawText(cost, 0, 0, true);
			gc.dispose();
			return costImage;
		} finally {
			if (costImage != null) {
				imageRegistry.remove(key);
				imageRegistry.put(key, costImage);
			}
		}
	}

	private static Collection<Image> getManaImages(String cost) {
		if (cost.length() == 0)
			return Collections.emptyList();
		Collection<Image> res = new ArrayList<>();
		String text = cost.trim();
		if (text.equals("*"))
			return res;
		while (text.length() > 0) {
			boolean cut = false;
			for (Iterator<String> iterator = manaMap.keySet().iterator(); iterator.hasNext()
					&& text.length() > 0;) {
				String sym = iterator.next();
				if (sym.length() == 0)
					throw new MagicException();
				while (text.startsWith(sym)) {
					String im = manaMap.get(sym);
					Image symImage = ImageCreator.getManaSymbolImage(sym, im);
					if (symImage != null) {
						res.add(symImage);
					}
					text = text.substring(sym.length()).trim();
					cut = true;
				}
			}
			if (!cut)
				throw new MagicException("Cannot build mana images for '" + text + "'");
		}
		return res;
	}

	public static String wrapHtml(String text, Control con) {
		// URL url =
		// Activator.getDefault().getBundle().getResource("icons/blue_mana.gif");
		if (bundleBase != null) {
			for (Iterator<String> iterator = manaMap.keySet().iterator(); iterator.hasNext();) {
				String sym = iterator.next();
				text = insertCostImages(text, sym, manaMap.get(sym));
			}
		}
		String style = getHtmlStyle(con);
		String html = "<html>" //
				+ "<head><base href=\"" + (bundleBase == null ? "." : bundleBase) + "\"/></head>" + //
				"<body style='overflow:auto;" + style + "'>" + text + "</body></html>";
		return html;
	}

	public static String insertCostImages(String text, String sym, String icon) {
		if (text == null)
			return "";
		return text.replaceAll("\\Q" + sym, "<img src=\"" + icon + "\" alt=\"" + sym + "\">");
	}

	public static void main(String[] args) throws MalformedURLException {
		// get mana symbols
		// http://gatherer.wizards.com/Handlers/Image.ashx?size=small&name=X&type=symbol
		// ImageCreator creator = ImageCreator.getInstance();
		File dir = new File("/tmp/symbols");
		dir.mkdirs();
		for (Iterator<String> iterator = manaMap.keySet().iterator(); iterator.hasNext();) {
			String sym = iterator.next();
			sym = sym.substring(1);
			sym = sym.substring(0, sym.length() - 1);
			sym = sym.replaceAll("\\W", "");
			if (sym.equals("T") || sym.equals("Q"))
				continue; // no tap and untap
			String name = sym;
			GatherHelper.saveManaSymbol(dir, name);
		}
		GatherHelper.saveManaSymbol(dir, "snow");
		GatherHelper.saveManaSymbol(dir, "500");
	}
}
