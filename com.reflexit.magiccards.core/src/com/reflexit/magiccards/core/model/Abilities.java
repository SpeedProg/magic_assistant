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
import java.util.regex.Pattern;

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

    private static final String notBetweenBracketsRegex = "(?![^\\(]*\\))";
    private static final IAbilityMatcher[] abilities = new IAbilityMatcher[] {
            /*
             * List from: http://wizards.custhelp.com/app/answers/detail/a_id/17/related/1
             */
            // Ability Words
            createKeywordAbility("Channel"),
            createKeywordAbility("Chroma"),
            createKeywordAbility("Domain"),
            createKeywordAbility("Grandeur"),
            createKeywordAbility("Hellbent"),
            createKeywordAbility("Imprint"),
            createKeywordAbility("Join forces"),
            createKeywordAbility("Kinship"),
            createKeywordAbility("Landfall"),
            createKeywordAbility("Metalcraft"),
            createKeywordAbility("Morbid"),
            createKeywordAbility("Radiance"),
            createKeywordAbility("Sweep"),
            createKeywordAbility("Threshold"),
            // Ability keywords
            createKeywordAbility("Affinity"),
            createKeywordAbility("Amplify"),
            createKeywordAbility("Annihilator"),
            createKeywordAbility("Aura swap"),
            createKeywordAbility("Banding"),
            createKeywordAbility("Battle cry"),
            createKeywordAbility("Bloodthirst"),
            createKeywordAbility("Bushido"),
            createKeywordAbility("Buyback"),
            createKeywordAbility("Cascade"),
            createKeywordAbility("Champion"),
            createKeywordAbility("Changeling"),
            createKeywordAbility("Conspire"),
            createKeywordAbility("Convoke"),
            createKeywordAbility("Cumulative upkeep"),
            createKeywordAbility("Cycling"),
            createKeywordAbility("Deathtouch"),
            createKeywordAbility("Defender"),
            createKeywordAbility("Delve"),
            createKeywordAbility("Devour"),
            createKeywordAbility("Double strike"),
            createKeywordAbility("Dredge"),
            createKeywordAbility("Echo"),
            createKeywordAbility("Enchant"),
            createKeywordAbility("Entwine"),
            createKeywordAbility("Epic"),
            createKeywordAbility("Equip"),
            createKeywordAbility("Evoke"),
            createKeywordAbility("Exalted"),
            createKeywordAbility("Fading"),
            createKeywordAbility("Fear"),
            createKeywordAbility("First strike"),
            createKeywordAbility("Flanking"),
            createKeywordAbility("Flash"),
            createKeywordAbility("Flashback"),
            createKeywordAbility("Flying"),
            createKeywordAbility("Forecast"),
            createKeywordAbility("Fortify"),
            createKeywordAbility("Frenzy"),
            createKeywordAbility("Graft"),
            createKeywordAbility("Gravestorm"),
            createKeywordAbility("Haste"),
            createKeywordAbility("Haunt"),
            createKeywordAbility("Hexproof"),
            createKeywordAbility("Hideaway"),
            createKeywordAbility("Horsemanship"),
            createKeywordAbility("Infect"),
            createKeywordAbility("Intimidate"),
            createKeywordAbility("Kicker"),
            createKeywordAbility("Multikicker"),
            createKeywordAbility("Landwalk"),
            createKeywordAbility("Level up"),
            createKeywordAbility("Lifelink"),
            createKeywordAbility("Living weapon"),
            createKeywordAbility("Landcycling"), // Mined
                                                 // keyword
            createKeywordAbility("Madness"),
            createKeywordAbility("Modular"),
            createKeywordAbility("Morph"),
            createKeywordAbility("Ninjutsu"),
            createKeywordAbility("Offering"),
            createKeywordAbility("Persist"),
            createKeywordAbility("Phasing"),
            createKeywordAbility("Poisonous"),
            createKeywordAbility("Protection"),
            createKeywordAbility("Provoke"),
            createKeywordAbility("Prowl"),
            createKeywordAbility("Rampage"),
            createKeywordAbility("Reach"),
            createKeywordAbility("Rebound"),
            createKeywordAbility("Recover"),
            createKeywordAbility("Reinforce"),
            createKeywordAbility("Replicate"),
            createKeywordAbility("Retrace"),
            createKeywordAbility("Regenerate"), // Mined
                                                // keyword
            createKeywordAbility("Ripple"), createKeywordAbility("Shroud"), createKeywordAbility("Soulshift"),
            createKeywordAbility("Splice"), createKeywordAbility("Split second"), createKeywordAbility("Storm"),
            createKeywordAbility("Sunburst"), createKeywordAbility("Suspend"), createKeywordAbility("Totem armor"),
            createKeywordAbility("Trample"), createKeywordAbility("Transfigure"), createKeywordAbility("Transmute"),
            createKeywordAbility("Unearth"), createKeywordAbility("Vanishing"), createKeywordAbility("Vigilance"),
            createKeywordAbility("Wither"),
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
