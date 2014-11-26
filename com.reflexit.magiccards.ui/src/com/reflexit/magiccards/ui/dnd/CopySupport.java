package com.reflexit.magiccards.ui.dnd;

import java.util.Collection;
import java.util.Iterator;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.PlatformUI;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.ui.MagicUIActivator;
import com.reflexit.magiccards.ui.utils.TextConvertor;

public class CopySupport {
	public static void runCopy(String text) {
		final Clipboard cb = new Clipboard(PlatformUI.getWorkbench().getDisplay());
		TextTransfer textTransfer = TextTransfer.getInstance();
		cb.setContents(new Object[] { text }, new Transfer[] { textTransfer });
	}

	public static void runCopy(Control focusControl) {
		if (focusControl instanceof Text)
			((Text) focusControl).copy();
		else if (focusControl instanceof Combo)
			((Combo) focusControl).copy();
		else if (focusControl instanceof Tree || focusControl instanceof Table) {
			ISelectionService s = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService();
			IStructuredSelection sel = (IStructuredSelection) s.getSelection();
			if (sel.isEmpty())
				return;
			StringBuffer buf = new StringBuffer();
			for (Iterator iterator = sel.iterator(); iterator.hasNext();) {
				Object line = iterator.next();
				buf.append(TextConvertor.toText(line));
			}
			String textData = buf.toString();
			if (textData.length() > 0) {
				final Clipboard cb = new Clipboard(PlatformUI.getWorkbench().getDisplay());
				TextTransfer textTransfer = TextTransfer.getInstance();
				MagicCardTransfer mt = MagicCardTransfer.getInstance();
				Collection list = DataManager.expandGroups(sel.toList());
				IMagicCard[] cards = (IMagicCard[]) list.toArray(new IMagicCard[sel.size()]);
				cb.setContents(new Object[] { textData, cards }, new Transfer[] { textTransfer, mt });
			}
		} else {
			MagicUIActivator.log("Copy from " + focusControl + " failed");
		}
	}

	public static void runPaste(Control fc) {
		MagicCardTransfer mt = MagicCardTransfer.getInstance();
		Object contents = mt.fromClipboard();
		if (contents instanceof Collection) {
			MagicUIActivator.log("Paste into " + fc + " failed - cannot find store");
		} else {
			if (fc instanceof Text)
				((Text) fc).paste();
			else if (fc instanceof Combo)
				((Combo) fc).paste();
			else
				MagicUIActivator.log("Paste into " + fc + " failed");
		}
	}
}
