package com.reflexit.magiccards.ui.actions;

import java.util.Collection;
import java.util.Iterator;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.ui.dnd.MagicCardTransfer;
import com.reflexit.magiccards.ui.utils.TextConvertor;

public class MagicCopyAction extends AbstractMagicAction {
	public MagicCopyAction(ISelectionProvider provider) {
		super(provider, "Copy");
		setId(ActionFactory.COPY.getId());
		setActionDefinitionId(ActionFactory.COPY.getCommandId());
	}

	public void run() {
		try {
			runCopy();
		} catch (Exception e) {
			MessageDialog.openError(getShell(), "Error", e.getMessage());
		}
	}

	protected void runCopy() {
		Control fc = getDisplay().getFocusControl();
		if (fc instanceof Text)
			((Text) fc).copy();
		else if (fc instanceof Combo)
			((Combo) fc).copy();
		else {
			IStructuredSelection sel = getStructuredSelection();
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
		}
	}
}
