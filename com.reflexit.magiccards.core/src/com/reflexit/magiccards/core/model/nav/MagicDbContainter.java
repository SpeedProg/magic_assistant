package com.reflexit.magiccards.core.model.nav;

import org.eclipse.core.runtime.Path;

/**
 * Organizer for magic database
 * 
 * @author Alena
 * 
 */
public class MagicDbContainter extends CardOrganizer {
	public MagicDbContainter(CardOrganizer parent) {
		super("MTG Database", new Path("MagicDB"), parent);
	}
}
