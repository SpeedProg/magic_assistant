package com.reflexit.magiccards.ui.commands;

import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import com.reflexit.magiccards.core.DataManager;

public class SplitHandler extends AbstractHandler {
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		ISelection selection = window.getSelectionService().getSelection();
		if (selection.isEmpty() || !(selection instanceof IStructuredSelection)) {
			return null;
		}
		String scount = event.getParameter("com.reflexit.magiccards.ui.count");
		int count = 1;
		try {
			count = Integer.parseInt(scount);
		} catch (NumberFormatException e) {
		}
		IStructuredSelection iss = (IStructuredSelection) selection;
		List list = iss.toList();
		try {
			DataManager.getInstance().splitCards(list, count);
		} catch (Exception e) {
			MessageDialog.openError(window.getShell(), "Error", e.getLocalizedMessage());
		}
		return null;
	}
}
