package com.reflexit.magiccards.ui.views.columns;

import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.EditingSupport;

import com.reflexit.magiccards.core.model.IMagicCardPhysical;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.MagicCardPhysical;

/**
 * @author Alena
 * 
 */
public class CommentColumn extends GenColumn {
	public CommentColumn() {
		super(MagicCardField.COMMENT, "Comment");
	}

	@Override
	public String getText(Object element) {
		if (element instanceof MagicCardPhysical) {
			IMagicCardPhysical m = (IMagicCardPhysical) element;
			String comm = m.getComment();
			if (comm == null)
				return "";
			return comm;
		} else {
			return super.getText(element);
		}
	}

	@Override
	public EditingSupport getEditingSupport(final ColumnViewer viewer) {
		return getGenericEditingSupport(viewer);
	}
}