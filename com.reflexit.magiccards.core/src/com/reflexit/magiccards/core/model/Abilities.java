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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;
import java.util.regex.Pattern;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.MagicLogger;
import com.reflexit.magiccards.core.locale.CardTextLocal;

/**
 * Class that contains abilities functionality
 */
public class Abilities {
	private static final String MINED = "mined";
	private static final String KEYWORD = "keyword";

	public static interface IAbilityMatcher {
		public boolean match(String subject);

		public String getDisplayName();

		public boolean isKeyword();

		public Pattern getPattern();
	}

	public static class RegexAbilityMatcher implements IAbilityMatcher {
		private String displayName;
		private Pattern compiledPattern;
		private boolean keyword;

		public RegexAbilityMatcher(String displayName, String searchPattern, boolean keyword) {
			this.displayName = displayName;
			this.compiledPattern = Pattern.compile(searchPattern, Pattern.CASE_INSENSITIVE
					| Pattern.UNICODE_CASE | Pattern.DOTALL);
			this.keyword = keyword;
		}

		@Override
		public boolean match(String text) {
			if (text.indexOf('(') >= 0) {
				text = text.replaceAll("\\([^)]*\\)", " ");
			}
			return compiledPattern.matcher(text).find();
		}

		@Override
		public String getDisplayName() {
			return displayName;
		}

		@Override
		public boolean isKeyword() {
			return keyword;
		}

		@Override
		public Pattern getPattern() {
			return compiledPattern;
		}
	}

	private static final CardTextLocal AB = CardTextLocal.getCardText(Locale.ENGLISH);
	private static IAbilityMatcher[] abilities = new IAbilityMatcher[] {
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
			createKeywordAbility(AB.Ability_Extort),// "Extort"
			createKeywordAbility(AB.Ability_Cipher),// "Cipher"
			createKeywordAbility(AB.Ability_Bloodrush),// "Bloodrush"
			createKeywordAbility(AB.Ability_Battalion),// "Battalion"
			createKeywordAbility(AB.Ability_Evolve),// "Evolve"
			createKeywordAbility(AB.Ability_Gates),// "Gates"
			/*
			 * Hand mined abilities from A to Ardent Militia name initialized cards
			 */
			createMinedAbility("Tap/Untap", "(tap(s|ped)?|untap(s|ped)?)"), //
			createMinedAbilityS("Sacrifice"), //
			createMinedAbility("Search"), //
			createMinedAbilityS("Reveal"), //
			createMinedAbility("Hand"), //
			createMinedAbility("Shuffle", "(re)?shuffles?"), //
			createMinedAbility("Target"), //
			createMinedAbility("Creature", "(creatures?|[0-9]*/[0-9]*)"), //
			createMinedAbilityS("Player"), //
			createMinedAbilityRegex("Power/Toughness",
					"([\\+\\-][0-9]/[\\+\\-][0-9])|\\b(power|toughness)\\b"), //
			createMinedAbility("Upkeep"), //
			createMinedAbilityD("Name"), //
			createMinedAbility("Remove"), //
			createMinedAbility("Turn"), //
			createMinedAbility("Mana"), //
			createMinedAbility("Attack", "(non)?attack(ing|s|ed)?"), //
			createMinedAbility("Defend", "(non)?defend(ing|s|ed)?"), //
			createMinedAbility("Block", "(non)?block(ing|s|ed)?"), //
			createMinedAbilityS("Discard"), //
			createMinedAbility("Look"), //
			createMinedAbility("Choose"), //
			createMinedAbility("Color", "color(s|ed|less)?"), //
			createMinedAbility("Life"), //
			createMinedAbility("Control", "control(s|ler)?"), //
			createMinedAbilityS("Owner"), //
			createMinedAbility("Graveyard", "graveyard"), //
			createMinedAbility("Activation", "activat(e|ion)"), //
			createMinedAbilityS("Draw"), //
			createMinedAbilityS("Permanent"), //
			createMinedAbility("Blue", "(non)?blue"), //
			createMinedAbility("Black", "(non)?black"), //
			createMinedAbility("White", "(non)?white"), //
			createMinedAbility("Green", "(non)?green"), //
			createMinedAbility("Red", "(non)?red"), //
			createMinedAbility("Counter target spells", "counter\\s+target\\s+spell"), //
			createMinedAbility("Destroy"), //
			createMinedAbility("Cost"), //
			createMinedAbility("Cumulative"), //
			createMinedAbility("Counter", "counters?"), //
			createMinedAbility("Combat"), //
			createMinedAbility("Battlefield"), //
			createMinedAbility("Game"), //
			createMinedAbility("Attach", "attach(ed)?"), //
			createMinedAbility("Step/Phase", "(step|phase)"), //
			createMinedAbility("Random"), //
			createMinedAbility("Mountain", "mountains?(walk)?"), //
			createMinedAbility("Plain", "plains?(walk)?"), //
			createMinedAbility("Swamp", "swamps?(walk)?"), //
			createMinedAbility("Forest", "forests?(walk)?"), //
			createMinedAbility("Island", "islands?(walk)?"), //
			createMinedAbility("Bury"), //
			createMinedAbility("Token", "(non)?tokens?"), //
			createMinedAbility("Library"), //
			createMinedAbilityS("Gain"), //
			createMinedAbility("Flip"), //
			createMinedAbilityD("Exile"), //
			createMinedAbility("Summon", "summon(ing)?\\s+sick(ness)?"), //
			createMinedAbility("Ante"), //
			createMinedAbility("Type"), //
			createMinedAbility("Return"), //
			createMinedAbility("Indestructible"), //
			createMinedAbilityS("Opponent"), //
			createMinedAbility("Face"), //
			createMinedAbility("Pay"), //
			createMinedAbility("Interrupt"), };
	private static final String ABILITIES_FILE = "abilities.txt";

	private static IAbilityMatcher createKeywordAbility(String string) {
		return new RegexAbilityMatcher(string, "\\b" + string.toLowerCase(Locale.ENGLISH) + "\\b", true);
	}

	private static IAbilityMatcher createMinedAbility(String string, String reg) {
		return new RegexAbilityMatcher(string, "\\b" + reg + "\\b", false);
	}

	private static IAbilityMatcher createMinedAbilityRegex(String string, String reg) {
		return new RegexAbilityMatcher(string, reg, false);
	}

	private static IAbilityMatcher createMinedAbilityS(String string) {
		return new RegexAbilityMatcher(string, "\\b" + string.toLowerCase(Locale.ENGLISH) + "s?\\b", false);
	}

	private static IAbilityMatcher createMinedAbilityD(String string) {
		return new RegexAbilityMatcher(string, "\\b" + string.toLowerCase(Locale.ENGLISH) + "d?\\b", false);
	}

	private static IAbilityMatcher createMinedAbility(String string) {
		return new RegexAbilityMatcher(string, "\\b" + string.toLowerCase(Locale.ENGLISH) + "s?\\b", false);
	}

	static {
		try {
			load();
		} catch (Throwable e) {
			MagicLogger.log(e);
		}
	}

	static class TextSeach implements ISearchableProperty {
		@Override
		public String getIdPrefix() {
			return getFilterField().toString();
		}

		@Override
		public FilterField getFilterField() {
			return FilterField.TEXT_LINE;
		}

		@Override
		public Collection getIds() {
			Collection list = new ArrayList<String>();
			list.add(FilterField.TEXT_LINE);
			list.add(FilterField.TEXT_LINE_2);
			list.add(FilterField.TEXT_LINE_3);
			return list;
		}

		@Override
		public String getNameById(String id) {
			// TODO Auto-generated method stub
			return null;
		}
	}

	static class TextSeachNot implements ISearchableProperty {
		@Override
		public String getIdPrefix() {
			return getFilterField().toString();
		}

		@Override
		public FilterField getFilterField() {
			return FilterField.TEXT_LINE;
		}

		@Override
		public Collection getIds() {
			Collection list = new ArrayList<String>();
			list.add(FilterField.TEXT_NOT_1);
			list.add(FilterField.TEXT_NOT_2);
			list.add(FilterField.TEXT_NOT_3);
			return list;
		}

		@Override
		public String getNameById(String id) {
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

	public static Pattern getPattern(String value) {
		IAbilityMatcher a = getAbility(value);
		if (a == null)
			return null;
		return a.getPattern();
	}

	public static synchronized void save() throws FileNotFoundException {
		File file = getAbilitiesFile();
		save(file);
	}

	public static File getAbilitiesFile() {
		File file = new File(DataManager.getInstance().getTablesDir(), ABILITIES_FILE);
		return file;
	}

	public synchronized static void save(File file) throws FileNotFoundException {
		PrintStream st = new PrintStream(file);
		try {
			for (IAbilityMatcher a : abilities) {
				st.print(a.getDisplayName() + "|" + (a.isKeyword() ? KEYWORD : MINED) + "|"
						+ a.getPattern());
				st.print('\n');// unix ending
			}
		} finally {
			st.close();
		}
	}

	private static synchronized void load() throws IOException {
		File file = getAbilitiesFile();
		if (!file.exists()) {
			initialize();
			save();
		} else {
			try {
				initialize();
			} catch (Exception e) {
				// ignore
			}
			InputStream st = new FileInputStream(file);
			load(st);
		}
	}

	private static void initialize() throws IOException, FileNotFoundException {
		// InputStream ist = FileUtils.loadDbResource(ABILITIES_FILE);
		// if (ist != null) {
		// loadEditions(ist);
		// }
	}

	private static synchronized void load(InputStream st) throws IOException {
		BufferedReader r = new BufferedReader(new InputStreamReader(st));
		try {
			String line;
			ArrayList<IAbilityMatcher> abs = new ArrayList<Abilities.IAbilityMatcher>();
			while ((line = r.readLine()) != null) {
				try {
					String[] attrs = line.split("\\|", 3);
					String name = attrs[0].trim();
					String props = attrs.length >= 2 ? attrs[1].trim() : "";
					String pattern = attrs.length >= 3 ? attrs[2].trim() : "";
					boolean isKeyword = !props.equals(MINED);
					IAbilityMatcher a;
					if (isKeyword)
						a = createKeywordAbility(name);
					else
						a = createMinedAbilityRegex(name, pattern);
					abs.add(a);
				} catch (Exception e) {
					MagicLogger.log("bad abilities record: " + line);
					MagicLogger.log(e);
				}
			}
			addAbilities(abs);
		} finally {
			r.close();
		}
	}

	public static void addAbilities(Collection<IAbilityMatcher> nabs) {
		for (Iterator iterator = nabs.iterator(); iterator.hasNext();) {
			IAbilityMatcher a = (IAbilityMatcher) iterator.next();
			int i = indexAbility(a.getDisplayName());
			if (i >= 0) {
				iterator.remove();
				abilities[i] = a;
			}
		}
		IAbilityMatcher[] nabilities = new IAbilityMatcher[abilities.length + nabs.size()];
		System.arraycopy(abilities, 0, nabilities, 0, abilities.length);
		System.arraycopy(nabs.toArray(), 0, nabilities, abilities.length, nabs.size());
		abilities = nabilities;
	}

	public static int indexAbility(String displayName) {
		int i = 0;
		for (IAbilityMatcher a : abilities) {
			if (displayName.equalsIgnoreCase(a.getDisplayName()))
				return i;
			i++;
		}
		return -1;
	}

	public static IAbilityMatcher getAbility(String displayName) {
		int i = indexAbility(displayName);
		if (i < 0)
			return null;
		return abilities[i];
	}
}
