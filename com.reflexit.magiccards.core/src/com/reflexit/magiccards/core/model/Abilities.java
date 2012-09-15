/*******************************************************************************
 * Copyright (c) 2008 Alena Laskavaia.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alena Laskavaia - initial API and implementation
 *******************************************************************************/
package com.reflexit.magiccards.core.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;
import java.util.regex.Pattern;

import com.reflexit.magiccards.core.locale.CardTextLocal;

/**
 * Class that contains abilities functionality
 */
public class Abilities {
	public static interface IAbilityMatcher {
		public boolean match(String subject);

		public String getDisplayName();

		public boolean isKeyword();
	}

	public static class RegexAbilityMatcher implements IAbilityMatcher {
		private String displayName;
		private Pattern compiledPattern;
		private boolean keyword;

		public RegexAbilityMatcher(String displayName, String searchPattern, boolean keyword) {
			this.displayName = displayName;
			this.compiledPattern = Pattern.compile(searchPattern, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
			this.keyword = keyword;
		}

		public boolean match(String subject) {
			return compiledPattern.matcher(subject).find();
		}

		public String getDisplayName() {
			return displayName;
		}

		public boolean isKeyword() {
			return keyword;
		}
	}

	private static final CardTextLocal AB = CardTextLocal.getCardText(Locale.ENGLISH);
	private static final String notBetweenBracketsRegex = "(?![^\\(]*\\))";
	private static final IAbilityMatcher[] abilities = new IAbilityMatcher[] {
			/*
			 * List from: http://wizards.custhelp.com/app/answers/detail/a_id/17/related/1
			 */
			// Ability Words
			createKeywordAbility(AB.Ability_Channel),// "Channel"
			createKeywordAbility(AB.Ability_Chroma),// "Chroma"
			createKeywordAbility(AB.Ability_Domain),// "Domain"
			createKeywordAbility(AB.Ability_Grandeur),// "Grandeur"
			createKeywordAbility(AB.Ability_Hellbent),// "Hellbent"
			createKeywordAbility(AB.Ability_Imprint),// "Imprint"
			createKeywordAbility(AB.Ability_Join_forces),// "Join forces"
			createKeywordAbility(AB.Ability_Kinship),// "Kinship"
			createKeywordAbility(AB.Ability_Landfall),// "Landfall"
			createKeywordAbility(AB.Ability_Metalcraft),// "Metalcraft"
			createKeywordAbility(AB.Ability_Morbid),// "Morbid"
			createKeywordAbility(AB.Ability_Radiance),// "Radiance"
			createKeywordAbility(AB.Ability_Sweep),// "Sweep"
			createKeywordAbility(AB.Ability_Threshold),// "Threshold"
			createKeywordAbility(AB.Ability_Affinity),// "Affinity"
			createKeywordAbility(AB.Ability_Amplify),// "Amplify"
			createKeywordAbility(AB.Ability_Annihilator),// "Annihilator"
			createKeywordAbility(AB.Ability_Aura_swap),// "Aura swap"
			createKeywordAbility(AB.Ability_Banding),// "Banding"
			createKeywordAbility(AB.Ability_Battle_cry),// "Battle cry"
			createKeywordAbility(AB.Ability_Bloodthirst),// "Bloodthirst"
			createKeywordAbility(AB.Ability_Bushido),// "Bushido"
			createKeywordAbility(AB.Ability_Buyback),// "Buyback"
			createKeywordAbility(AB.Ability_Cascade),// "Cascade"
			createKeywordAbility(AB.Ability_Champion),// "Champion"
			createKeywordAbility(AB.Ability_Changeling),// "Changeling"
			createKeywordAbility(AB.Ability_Conspire),// "Conspire"
			createKeywordAbility(AB.Ability_Convoke),// "Convoke"
			createKeywordAbility(AB.Ability_Cumulative_upkeep),// "Cumulative upkeep"
			createKeywordAbility(AB.Ability_Cycling),// "Cycling"
			createKeywordAbility(AB.Ability_Deathtouch),// "Deathtouch"
			createKeywordAbility(AB.Ability_Defender),// "Defender"
			createKeywordAbility(AB.Ability_Delve),// "Delve"
			createKeywordAbility(AB.Ability_Devour),// "Devour"
			createKeywordAbility(AB.Ability_Double_strike),// "Double strike"
			createKeywordAbility(AB.Ability_Dredge),// "Dredge"
			createKeywordAbility(AB.Ability_Echo),// "Echo"
			createKeywordAbility(AB.Ability_Enchant),// "Enchant"
			createKeywordAbility(AB.Ability_Entwine),// "Entwine"
			createKeywordAbility(AB.Ability_Epic),// "Epic"
			createKeywordAbility(AB.Ability_Equip),// "Equip"
			createKeywordAbility(AB.Ability_Evoke),// "Evoke"
			createKeywordAbility(AB.Ability_Exalted),// "Exalted"
			createKeywordAbility(AB.Ability_Fading),// "Fading"
			createKeywordAbility(AB.Ability_Fear),// "Fear"
			createKeywordAbility(AB.Ability_First_strike),// "First strike"
			createKeywordAbility(AB.Ability_Flanking),// "Flanking"
			createKeywordAbility(AB.Ability_Flash),// "Flash"
			createKeywordAbility(AB.Ability_Flashback),// "Flashback"
			createKeywordAbility(AB.Ability_Flying),// "Flying"
			createKeywordAbility(AB.Ability_Forecast),// "Forecast"
			createKeywordAbility(AB.Ability_Fortify),// "Fortify"
			createKeywordAbility(AB.Ability_Frenzy),// "Frenzy"
			createKeywordAbility(AB.Ability_Graft),// "Graft"
			createKeywordAbility(AB.Ability_Gravestorm),// "Gravestorm"
			createKeywordAbility(AB.Ability_Haste),// "Haste"
			createKeywordAbility(AB.Ability_Haunt),// "Haunt"
			createKeywordAbility(AB.Ability_Hexproof),// "Hexproof"
			createKeywordAbility(AB.Ability_Hideaway),// "Hideaway"
			createKeywordAbility(AB.Ability_Horsemanship),// "Horsemanship"
			createKeywordAbility(AB.Ability_Infect),// "Infect"
			createKeywordAbility(AB.Ability_Intimidate),// "Intimidate"
			createKeywordAbility(AB.Ability_Kicker),// "Kicker"
			createKeywordAbility(AB.Ability_Multikicker),// "Multikicker"
			createKeywordAbility(AB.Ability_Landwalk),// "Landwalk"
			createKeywordAbility(AB.Ability_Level_up),// "Level up"
			createKeywordAbility(AB.Ability_Lifelink),// "Lifelink"
			createKeywordAbility(AB.Ability_Living_weapon),// "Living weapon"
			createKeywordAbility(AB.Ability_Landcycling),// "Landcycling"
			createKeywordAbility(AB.Ability_Madness),// "Madness"
			createKeywordAbility(AB.Ability_Modular),// "Modular"
			createKeywordAbility(AB.Ability_Morph),// "Morph"
			createKeywordAbility(AB.Ability_Ninjutsu),// "Ninjutsu"
			createKeywordAbility(AB.Ability_Offering),// "Offering"
			createKeywordAbility(AB.Ability_Persist),// "Persist"
			createKeywordAbility(AB.Ability_Phasing),// "Phasing"
			createKeywordAbility(AB.Ability_Poisonous),// "Poisonous"
			createKeywordAbility(AB.Ability_Protection),// "Protection"
			createKeywordAbility(AB.Ability_Provoke),// "Provoke"
			createKeywordAbility(AB.Ability_Prowl),// "Prowl"
			createKeywordAbility(AB.Ability_Rampage),// "Rampage"
			createKeywordAbility(AB.Ability_Reach),// "Reach"
			createKeywordAbility(AB.Ability_Rebound),// "Rebound"
			createKeywordAbility(AB.Ability_Recover),// "Recover"
			createKeywordAbility(AB.Ability_Reinforce),// "Reinforce"
			createKeywordAbility(AB.Ability_Replicate),// "Replicate"
			createKeywordAbility(AB.Ability_Retrace),// "Retrace"
			createKeywordAbility(AB.Ability_Regenerate),// "Regenerate"
			createKeywordAbility(AB.Ability_Ripple),// "Ripple"
			createKeywordAbility(AB.Ability_Shroud),// "Shroud"
			createKeywordAbility(AB.Ability_Soulshift),// "Soulshift"
			createKeywordAbility(AB.Ability_Splice),// "Splice"
			createKeywordAbility(AB.Ability_Split_second),// "Split second"
			createKeywordAbility(AB.Ability_Storm),// "Storm"
			createKeywordAbility(AB.Ability_Sunburst),// "Sunburst"
			createKeywordAbility(AB.Ability_Suspend),// "Suspend"
			createKeywordAbility(AB.Ability_Totem_armor),// "Totem armor"
			createKeywordAbility(AB.Ability_Trample),// "Trample"
			createKeywordAbility(AB.Ability_Transfigure),// "Transfigure"
			createKeywordAbility(AB.Ability_Transmute),// "Transmute"
			createKeywordAbility(AB.Ability_Unearth),// "Unearth"
			createKeywordAbility(AB.Ability_Vanishing),// "Vanishing"
			createKeywordAbility(AB.Ability_Vigilance),// "Vigilance"
			createKeywordAbility(AB.Ability_Wither),// "Wither"
			/*
			 * Hand mined abilities from A to Ardent Militia name initialized cards
			 */
			createMinedAbility("Tap/Untap", "(\\b(tap([s]|ped)?)|(untap([s]|ped)?)\\b)"),
			createMinedAbility("Sacrifice", "(\\bsacrifices?\\bb)"), createMinedAbility("Search", "(\\bsearch\\b)"),
			createMinedAbility("Reveal", "(\\breveal\\b)"), createMinedAbility("Hand", "(\\bhand\\b)"),
			createMinedAbility("Shuffle", "(\\b(re)?shuffles?\\b)"), createMinedAbility("Target", "(\\btarget\\b)"),
			createMinedAbility("Creature", "((\\bcreatures?\\b|[0-9]*/[0-9]*))"), createMinedAbility("Player", "(\\bplayers?\\b)"),
			createMinedAbility("Power/Toughness", "([\\+\\-][0-9]/[\\+\\-][0-9])"),
			createMinedAbility("Power/Toughness", "(\\b(power|toughness)\\b)"), createMinedAbility("Upkeep", "(\\bupkeep\\b)"),
			createMinedAbility("Name", "(\\bnamed?\\b)"), createMinedAbility("Remove", "(\\bremove\\b)"),
			createMinedAbility("Turn", "(\\bturn\\b)"), createMinedAbility("Mana", "(\\bmana\\b)"),
			createMinedAbility("Attack", "(\\b(non)?attack(ing|s|ed)?\\b)"),
			createMinedAbility("Defend", "(\\b(non)?defend(ing|s|ed)?\\b)"), createMinedAbility("Block", "(\\b(non)?block(ing|s|ed)?\\b)"),
			createMinedAbility("Discard", "(\\bdiscards?\\b)"), createMinedAbility("Look", "(\\blook\\b)"),
			createMinedAbility("Choose", "(\\bchooses?\\b)"), createMinedAbility("Color", "(\\bcolor(s|ed|less)?\\b)"),
			createMinedAbility("Life", "(\\blife\\b)"), createMinedAbility("Control", "(\\bcontrol(s|ler)?\\b)"),
			createMinedAbility("Owner", "(\\bowners?\\b)"), createMinedAbility("Graveyard", "(\\bgraveyard\\b)"),
			createMinedAbility("Activation", "(\\bactivat(e|ion){1}\\b)"), createMinedAbility("Draw", "(\\bdraws?\\b)"),
			createMinedAbility("Permanent", "(\\bpermanents?\\b)"), createMinedAbility("Blue", "(\\b(non)?blue\\b)"),
			createMinedAbility("Black", "(\\b(non)?black\\b)"), createMinedAbility("White", "(\\b(non)?white\\b)"),
			createMinedAbility("Green", "(\\b(non)?green\\b)"), createMinedAbility("Red", "(\\b(non)?red\\b)"),
			createMinedAbility("Counter target spells", "(\\bcounter target spell\\b)"), createMinedAbility("Destroy", "(\\bdestroy\\b)"),
			createMinedAbility("Cost", "(\\bcost\\b)"), createMinedAbility("Cumulative", "(\\bcumulative\\b)"),
			createMinedAbility("Counter", "(\\bcounters?\\b)"), createMinedAbility("Combat", "(\\bcombat\\b)"),
			createMinedAbility("Battlefield", "(\\bbattlefield\\b)"), createMinedAbility("Game", "(\\bgame\\b)"),
			createMinedAbility("Attach", "(\\battach(ed)?\\b)"), createMinedAbility("Step/Phase", "(\\b(step|phase)\\b)"),
			createMinedAbility("Random", "(\\brandom\\b)"), createMinedAbility("Mountain", "(\\bmontains?(walk)?\\b)"),
			createMinedAbility("Plain", "(\\bplains?(walk)?\\b)"), createMinedAbility("Swamp", "(\\bswamps?(walk)?\\b)"),
			createMinedAbility("Forest", "(\\bforests?(walk)?\\b)"), createMinedAbility("Island", "(\\bislands?(walk)?\\b)"),
			createMinedAbility("Bury", "(\\bbury\\b)"), createMinedAbility("Token", "(\\b(non)?tokens?\\b)"),
			createMinedAbility("Library", "(\\blibrary\\b)"), createMinedAbility("Gain", "(\\bgains?\\b)"),
			createMinedAbility("Flip", "(\\bflip\\b)"), createMinedAbility("Exile", "(\\bexiled?\\b)"),
			createMinedAbility("Summon", "(\\bsummon(ing)? sick(ness)?\\b)"), createMinedAbility("Ante", "(\\bante\\b)"),
			createMinedAbility("Type", "(\\btype\\b)"), createMinedAbility("Return", "(\\breturn\\b)"),
			createMinedAbility("Indestructible", "(\\bindestructible\\b)"), createMinedAbility("Opponent", "(\\bopponents?\\b)"),
			createMinedAbility("Face", "(\\bface\\b)"), createMinedAbility("Pay", "(\\bpay\\b)"),
			createMinedAbility("Interrupt", "(\\binterrupt\\b)"), };

	private static IAbilityMatcher createKeywordAbility(String string) {
		return new RegexAbilityMatcher(string, "\\b" + string + "\\b" + notBetweenBracketsRegex, true);
	}

	private static IAbilityMatcher createMinedAbility(String string, String reg) {
		return new RegexAbilityMatcher(string, reg + notBetweenBracketsRegex, false);
	}

	static class TextSeach implements ISearchableProperty {
		public String getIdPrefix() {
			return FilterHelper.TEXT_LINE;
		}

		public Collection getIds() {
			Collection list = new ArrayList<String>();
			list.add(FilterHelper.TEXT_LINE);
			list.add(FilterHelper.TEXT_LINE_2);
			list.add(FilterHelper.TEXT_LINE_3);
			return list;
		}

		public String getNameById(String id) {
			// TODO Auto-generated method stub
			return null;
		}

		public Collection getNames() {
			// TODO Auto-generated method stub
			return null;
		}
	}

	static class TextSeachNot implements ISearchableProperty {
		public String getIdPrefix() {
			return FilterHelper.TEXT_LINE;
		}

		public Collection getIds() {
			Collection list = new ArrayList<String>();
			list.add(FilterHelper.TEXT_NOT_1);
			list.add(FilterHelper.TEXT_NOT_2);
			list.add(FilterHelper.TEXT_NOT_3);
			return list;
		}

		public String getNameById(String id) {
			// TODO Auto-generated method stub
			return null;
		}

		public Collection getNames() {
			// TODO Auto-generated method stub
			return null;
		}
	}

	static public IAbilityMatcher[] getAbilities() {
		return abilities;
	}

	static ISearchableProperty getTextFields() {
		return new TextSeach();
	}

	static ISearchableProperty getTextNotFields() {
		return new TextSeachNot();
	}
}
