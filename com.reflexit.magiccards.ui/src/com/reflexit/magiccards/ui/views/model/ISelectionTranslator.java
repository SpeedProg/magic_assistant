package com.reflexit.magiccards.ui.views.model;

import org.eclipse.jface.viewers.IStructuredSelection;

public interface ISelectionTranslator {
	IStructuredSelection translateSelection(IStructuredSelection selection, int level);
}