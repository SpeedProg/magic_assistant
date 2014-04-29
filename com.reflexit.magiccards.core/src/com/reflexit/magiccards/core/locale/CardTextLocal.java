package com.reflexit.magiccards.core.locale;

import java.util.HashMap;
import java.util.Locale;

public class CardTextLocal extends LocalizedText {
	private static final String BUNDLE_NAME = CardText.class.getName();
	public String Type_Artifact;
	public String Type_Basic;
	public String Type_Non_Basic;
	public String Type_Creature;
	public String Type_Enchantment;
	public String Type_Instant;
	public String Type_Interrupt;
	public String Type_Land;
	public String Type_Non_Creature;
	public String Type_Planeswalker;
	public String Type_Sorcery;
	public String Type_Spell;
	public String Type_Summon;
	public String Type_Unknown;
	// Abilities
	public String Ability_Channel;
	public String Ability_Chroma;
	public String Ability_Domain;
	public String Ability_Grandeur;
	public String Ability_Hellbent;
	public String Ability_Imprint;
	public String Ability_Join_forces;
	public String Ability_Kinship;
	public String Ability_Landfall;
	public String Ability_Metalcraft;
	public String Ability_Morbid;
	public String Ability_Radiance;
	public String Ability_Sweep;
	public String Ability_Threshold;
	public String Ability_Affinity;
	public String Ability_Amplify;
	public String Ability_Annihilator;
	public String Ability_Aura_swap;
	public String Ability_Banding;
	public String Ability_Battle_cry;
	public String Ability_Bloodthirst;
	public String Ability_Bushido;
	public String Ability_Buyback;
	public String Ability_Cascade;
	public String Ability_Champion;
	public String Ability_Changeling;
	public String Ability_Conspire;
	public String Ability_Convoke;
	public String Ability_Cumulative_upkeep;
	public String Ability_Cycling;
	public String Ability_Deathtouch;
	public String Ability_Defender;
	public String Ability_Delve;
	public String Ability_Devour;
	public String Ability_Double_strike;
	public String Ability_Dredge;
	public String Ability_Echo;
	public String Ability_Enchant;
	public String Ability_Entwine;
	public String Ability_Epic;
	public String Ability_Equip;
	public String Ability_Evoke;
	public String Ability_Exalted;
	public String Ability_Fading;
	public String Ability_Fear;
	public String Ability_First_strike;
	public String Ability_Flanking;
	public String Ability_Flash;
	public String Ability_Flashback;
	public String Ability_Flying;
	public String Ability_Forecast;
	public String Ability_Fortify;
	public String Ability_Frenzy;
	public String Ability_Graft;
	public String Ability_Gravestorm;
	public String Ability_Haste;
	public String Ability_Haunt;
	public String Ability_Hexproof;
	public String Ability_Hideaway;
	public String Ability_Horsemanship;
	public String Ability_Infect;
	public String Ability_Intimidate;
	public String Ability_Kicker;
	public String Ability_Multikicker;
	public String Ability_Landwalk;
	public String Ability_Level_up;
	public String Ability_Lifelink;
	public String Ability_Living_weapon;
	public String Ability_Landcycling;
	public String Ability_Madness;
	public String Ability_Modular;
	public String Ability_Morph;
	public String Ability_Ninjutsu;
	public String Ability_Offering;
	public String Ability_Persist;
	public String Ability_Phasing;
	public String Ability_Poisonous;
	public String Ability_Protection;
	public String Ability_Provoke;
	public String Ability_Prowl;
	public String Ability_Rampage;
	public String Ability_Reach;
	public String Ability_Rebound;
	public String Ability_Recover;
	public String Ability_Reinforce;
	public String Ability_Replicate;
	public String Ability_Retrace;
	public String Ability_Regenerate;
	public String Ability_Ripple;
	public String Ability_Shroud;
	public String Ability_Soulshift;
	public String Ability_Splice;
	public String Ability_Split_second;
	public String Ability_Storm;
	public String Ability_Sunburst;
	public String Ability_Suspend;
	public String Ability_Totem_armor;
	public String Ability_Trample;
	public String Ability_Transfigure;
	public String Ability_Transmute;
	public String Ability_Unearth;
	public String Ability_Vanishing;
	public String Ability_Vigilance;
	public String Ability_Wither;
	public String Ability_Extort;
	public String Ability_Cipher;
	public String Ability_Gates;
	public String Ability_Bloodrush;
	public String Ability_Evolve;
	public String Ability_Battalion;
	protected static HashMap<Locale, CardTextLocal> map = new HashMap<Locale, CardTextLocal>();

	public static CardTextLocal getCardText(Locale locale) {
		CardTextLocal ct = map.get(locale);
		if (ct == null) {
			ct = new CardTextLocal(locale);
			map.put(locale, ct);
		}
		return ct;
	}

	private CardTextLocal(Locale locale) {
		super(locale);
	}

	@Override
	public String getBundleName() {
		return BUNDLE_NAME;
	}

	public static CardTextLocal getCardText(String language) {
		Locale loc = getLocale(language);
		return getCardText(loc);
	}
}
