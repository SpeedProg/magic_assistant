package com.reflexit.magiccards.ui.views.columns;

import com.reflexit.magiccards.core.model.IMagicCardPhysical;
import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.MagicCardPhysical;

/**
 * @author Alena
 * 
 */
public class OwnCountColumn extends CountColumn {
	public OwnCountColumn() {
		super(MagicCardField.OWN_COUNT, "Own Count");
	}

	@Override
	public int getColumnWidth() {
		return 60;
	}

	@Override
	public String getText(Object element) {
		if (element instanceof MagicCardPhysical) {
			MagicCardPhysical mcp = (MagicCardPhysical) element;
			int ocount = mcp.getOwnCount();
			int tcount = mcp.getOwnTotal();
			int acount = mcp.getOwnTotalAll();
			if (ocount == tcount && tcount == acount)
				return String.valueOf(ocount);
			if (tcount == acount)
				return ocount + "/" + tcount;
			return ocount + "/" + tcount + "/" + acount;
		} else if (element instanceof MagicCard) {
			MagicCard mc = (MagicCard) element;
			int ocount = mc.getOwnCount();
			int acount = mc.getOwnTotalAll();
			if (ocount == acount)
				return String.valueOf(ocount);
			return ocount + "/" + acount;
		} else if (element instanceof IMagicCardPhysical) {
			IMagicCardPhysical mcp = (IMagicCardPhysical) element;
			int ocount = mcp.getOwnCount();
			return String.valueOf(ocount);
		} else {
			return "";
		}
	}

	@Override
	public String getToolTipText(Object element) {
		if (element instanceof MagicCardPhysical) {
			return "X/Y/Z:\nX - own count in this deck;\nY - own count in all cards for this set;\nZ - own count in all sets";
		}
		if (element instanceof MagicCard) {
			return "Y/Z:\nY - own count in all cards for this set;\nZ - own count in all sets";
		}
		return "count of own cards in this deck/collection";
	}

	@Override
	protected boolean canEditElement(Object element) {
		return element instanceof MagicCardPhysical && ((MagicCardPhysical) element).isOwn();
	}
}