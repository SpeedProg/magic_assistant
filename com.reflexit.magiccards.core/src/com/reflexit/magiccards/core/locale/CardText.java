package com.reflexit.magiccards.core.locale;

import java.util.Locale;

public final class CardText extends NLSLocal {
	private static final String BUNDLE_NAME = CardText.class.getName();
	// Types
	public static String Type_Artifact;
	public static String Type_Basic;
	public static String Type_Non_Basic;
	public static String Type_Creature;
	public static String Type_Enchantment;
	public static String Type_Instant;
	public static String Type_Interrupt;
	public static String Type_Land;
	public static String Type_Non_Creature;
	public static String Type_Planeswalker;
	public static String Type_Sorcery;
	public static String Type_Spell;
	public static String Type_Summon;
	public static String Type_Unknown;
	// Abilities
	public static String Ability_Channel;
	public static String Ability_Chroma;
	public static String Ability_Domain;
	public static String Ability_Grandeur;
	public static String Ability_Hellbent;
	public static String Ability_Imprint;
	public static String Ability_Join_forces;
	public static String Ability_Kinship;
	public static String Ability_Landfall;
	public static String Ability_Metalcraft;
	public static String Ability_Morbid;
	public static String Ability_Radiance;
	public static String Ability_Sweep;
	public static String Ability_Threshold;
	public static String Ability_Affinity;
	public static String Ability_Amplify;
	public static String Ability_Annihilator;
	public static String Ability_Aura_swap;
	public static String Ability_Banding;
	public static String Ability_Battle_cry;
	public static String Ability_Bloodthirst;
	public static String Ability_Bushido;
	public static String Ability_Buyback;
	public static String Ability_Cascade;
	public static String Ability_Champion;
	public static String Ability_Changeling;
	public static String Ability_Conspire;
	public static String Ability_Convoke;
	public static String Ability_Cumulative_upkeep;
	public static String Ability_Cycling;
	public static String Ability_Deathtouch;
	public static String Ability_Defender;
	public static String Ability_Delve;
	public static String Ability_Devour;
	public static String Ability_Double_strike;
	public static String Ability_Dredge;
	public static String Ability_Echo;
	public static String Ability_Enchant;
	public static String Ability_Entwine;
	public static String Ability_Epic;
	public static String Ability_Equip;
	public static String Ability_Evoke;
	public static String Ability_Exalted;
	public static String Ability_Fading;
	public static String Ability_Fear;
	public static String Ability_First_strike;
	public static String Ability_Flanking;
	public static String Ability_Flash;
	public static String Ability_Flashback;
	public static String Ability_Flying;
	public static String Ability_Forecast;
	public static String Ability_Fortify;
	public static String Ability_Frenzy;
	public static String Ability_Graft;
	public static String Ability_Gravestorm;
	public static String Ability_Haste;
	public static String Ability_Haunt;
	public static String Ability_Hexproof;
	public static String Ability_Hideaway;
	public static String Ability_Horsemanship;
	public static String Ability_Infect;
	public static String Ability_Intimidate;
	public static String Ability_Kicker;
	public static String Ability_Multikicker;
	public static String Ability_Landwalk;
	public static String Ability_Level_up;
	public static String Ability_Lifelink;
	public static String Ability_Living_weapon;
	public static String Ability_Landcycling;
	public static String Ability_Madness;
	public static String Ability_Modular;
	public static String Ability_Morph;
	public static String Ability_Ninjutsu;
	public static String Ability_Offering;
	public static String Ability_Persist;
	public static String Ability_Phasing;
	public static String Ability_Poisonous;
	public static String Ability_Protection;
	public static String Ability_Provoke;
	public static String Ability_Prowl;
	public static String Ability_Rampage;
	public static String Ability_Reach;
	public static String Ability_Rebound;
	public static String Ability_Recover;
	public static String Ability_Reinforce;
	public static String Ability_Replicate;
	public static String Ability_Retrace;
	public static String Ability_Regenerate;
	public static String Ability_Ripple;
	public static String Ability_Shroud;
	public static String Ability_Soulshift;
	public static String Ability_Splice;
	public static String Ability_Split_second;
	public static String Ability_Storm;
	public static String Ability_Sunburst;
	public static String Ability_Suspend;
	public static String Ability_Totem_armor;
	public static String Ability_Trample;
	public static String Ability_Transfigure;
	public static String Ability_Transmute;
	public static String Ability_Unearth;
	public static String Ability_Vanishing;
	public static String Ability_Vigilance;
	public static String Ability_Wither;
	public static String Ability_Extort;
	public static String Ability_Cipher;
	public static String Ability_Gates;
	public static String Ability_Bloodrush;
	public static String Ability_Evolve;
	public static String Ability_Battalion;
	static {
		// initialize resource bundle
		try {
			// NLS.initializeMessages(BUNDLE_NAME, CardText.class);
			Locale curLoc = Locale.getDefault();
			initializeMessages(BUNDLE_NAME, CardText.class, curLoc);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	private CardText() {
	}
}
