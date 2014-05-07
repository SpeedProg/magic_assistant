package com.reflexit.magiccards.core.model.nav;

/**
 * Organizer for magic database
 * 
 * @author Alena
 * 
 */
public class MagicDbContainter extends CardOrganizer {
	public MagicDbContainter(CardOrganizer parent) {
		super("MTG Database", new LocationPath("MagicDB"), parent);
	}
}
