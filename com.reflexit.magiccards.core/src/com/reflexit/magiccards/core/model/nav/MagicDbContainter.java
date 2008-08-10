package com.reflexit.magiccards.core.model.nav;

import org.eclipse.core.runtime.Path;

public class MagicDbContainter extends CardOrganizer {
	public MagicDbContainter(CardOrganizer parent) {
		super("All Cards", new Path("MagicDB"), parent);
	}
}
