package com.reflexit.magiccards.core.model;

import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;

import com.reflexit.magiccards.core.model.Abilities.IAbilityMatcher;

public class AbilitiesTest extends TestCase {
	private Set<String> set;
	private String text;

	@Override
	@Before
	protected void setUp() throws Exception {
		super.setUp();
	}

	private void text(String string) {
		if (text == null)
			text = string;
		else
			text += string;
	}

	private void check(String ab, String text) {
		boolean match = Abilities.getAbility(ab).match(text);
		assertTrue("Cannot match [" + ab + "] with [" + text + "]", match);
	}

	private void checkNot(String ab, String text) {
		boolean match = Abilities.getAbility(ab).match(text);
		assertFalse("Can match [" + ab + "] with [" + text + "]", match);
	}

	private Set<String> calcset(String text) {
		set = new HashSet<String>();
		IAbilityMatcher[] abilities = Abilities.getAbilities();
		for (IAbilityMatcher m : abilities) {
			if (m.match(text))
				set.add(m.getDisplayName());
		}
		return set;
	}

	private void checkInSet(String ab, String text) {
		calcset(text);
		assertTrue(set.contains(ab));
	}

	private void checkInSet(String ab) {
		assertTrue(set.contains(ab));
	}

	@Test
	public void testFlying() {
		check("Flying", "Flying");
	}

	public void testCostNot() {
		checkNot("Cost", "Holacost");
	}

	public void testFlyingNot() {
		checkNot("Flying", "Reach (It can block creatures with flying.)");
	}

	public void testReach() {
		checkInSet("Reach", "Reach (It can block creatures with flying.)");
		assertEquals(1, set.size());
	}

	public void testBunch() {
		text("Graft 2 (This creature enters the battlefield with two +1/+1 counters on it. Whenever another creature enters the battlefield, you may move a +1/+1 counter from this creature onto it.)\r\n"
				+ "{G}: Target creature with a +1/+1 counter on it gains reach until end of turn. (It can block creatures with flying.)\r\n");
		calcset(text);
		System.err.println(set);
		assertEquals(8, set.size());
		checkInSet("Reach");
		checkInSet("Graft");
		checkInSet("Counter");
		checkInSet("Power/Toughness");
		checkInSet("Target");
		checkInSet("Creature");
		checkInSet("Counter");
	}

	private void checkMinedAbility(String string, String string2) {
		check(string, string2);
	}

	private void checkNotMinedAbility(String string, String string2) {
		checkNot(string, string2);
	}

	public void testMined() {
	}

	public void testTap_Untap() {
		checkMinedAbility("Tap/Untap", "tap([s]|ped)?)|untap([s]|ped?");
		checkMinedAbility("Tap/Untap", "tap");
		checkMinedAbility("Tap/Untap", "taps");
		checkMinedAbility("Tap/Untap", "tapped");
		checkMinedAbility("Tap/Untap", "untaps");
		checkMinedAbility("Tap/Untap", "untapped");
		checkMinedAbility("Tap/Untap", "Untap");
		checkNotMinedAbility("Tap/Untap", "tapa");
	}

	public void testSacrifice() {
		checkMinedAbility("Sacrifice", "sacrifices");
		checkMinedAbility("Sacrifice", "sacrifice creature");
	}

	public void testSearch() {
		checkMinedAbility("Search", "search");
	}

	public void testReveal() {
		checkMinedAbility("Reveal", "reveal");
	}

	public void testHand() {
		checkMinedAbility("Hand", "hand");
	}

	public void testShuffle() {
		checkMinedAbility("Shuffle", "shuffle deck");
		checkMinedAbility("Shuffle", "reshuffle");
		checkMinedAbility("Shuffle", "shuffles");
	}

	public void testTarget() {
		checkMinedAbility("Target", "target");
	}

	public void testCreature() {
		// checkMinedAbility("Creature", "(creatures?\\b|[0-9]*/[0-9]*))"); // XXX
		checkMinedAbility("Creature", "a/b"); // XXX
		checkMinedAbility("Creature", "1/1");
	}

	public void testPlayer() {
		checkMinedAbility("Player", "players");
	}

	public void testPower_Toughness() {
		checkMinedAbility("Power/Toughness", "power");
		checkMinedAbility("Power/Toughness", "toughness");
		checkMinedAbility("Power/Toughness", "+1/+1");
		checkMinedAbility("Power/Toughness", "-9/+9");
		checkNotMinedAbility("Power/Toughness", "+10/+10"); // XXX
	}

	public void testUpkeep() {
		checkMinedAbility("Upkeep", "upkeep");
	}

	public void testName() {
		checkMinedAbility("Name", "named?");
	}

	public void testRemove() {
		checkMinedAbility("Remove", "Remove");
		checkMinedAbility("Remove", "remove");
	}

	public void testTurn() {
		checkMinedAbility("Turn", "turn");
	}

	public void testMana() {
		checkMinedAbility("Mana", "mana");
	}

	public void testAttack() {
		checkMinedAbility("Attack", "(non)?attack(ing|s|ed)?");
	}

	public void testDefend() {
		checkMinedAbility("Defend", "(non)?defend(ing|s|ed)?");
	}

	public void testBlock() {
		checkMinedAbility("Block", "(non)?block(ing|s|ed)?");
	}

	public void testDiscard() {
		checkMinedAbility("Discard", "discards?");
	}

	public void testLook() {
		checkMinedAbility("Look", "look");
	}

	public void testChoose() {
		checkMinedAbility("Choose", "chooses?");
	}

	public void testColor() {
		checkMinedAbility("Color", "color(s|ed|less)?");
	}

	public void testLife() {
		checkMinedAbility("Life", "life");
	}

	public void testControl() {
		checkMinedAbility("Control", "control(s|ler)?");
	}

	public void testOwner() {
		checkMinedAbility("Owner", "owners?");
	}

	public void testGraveyard() {
		checkMinedAbility("Graveyard", "graveyard");
	}

	public void testActivation() {
		// checkMinedAbility("Activation", "activat(e|ion){1}");
		checkMinedAbility("Activation", "activate");
		checkMinedAbility("Activation", "activation");
	}

	public void testDraw() {
		checkMinedAbility("Draw", "draws?");
	}

	public void testPermanent() {
		checkMinedAbility("Permanent", "permanents?");
	}

	public void testBlue() {
		String lands[] = { "Blue", "Black", "White", "Green", "Red" };
		for (int i = 0; i < lands.length; i++) {
			String land = lands[i];
			checkMinedAbility(land, land);
			checkMinedAbility(land, "non" + land);
		}
	}

	public void testCounter_target_spells() {
		checkMinedAbility("Counter target spells", "counter target spell");
		checkMinedAbility("Counter target spells", "counter\n\r target spell");
	}

	public void testDestroy() {
		checkMinedAbility("Destroy", "destroy");
	}

	public void testCost() {
		checkMinedAbility("Cost", "cost");
	}

	public void testCumulative() {
		checkMinedAbility("Cumulative", "cumulative");
	}

	public void testCounter() {
		checkMinedAbility("Counter", "counters?");
	}

	public void testCombat() {
		checkMinedAbility("Combat", "combat");
	}

	public void testBattlefield() {
		checkMinedAbility("Battlefield", "battlefield");
	}

	public void testGame() {
		checkMinedAbility("Game", "game");
	}

	public void testAttach() {
		checkMinedAbility("Attach", "attach(ed)?");
	}

	public void testStep_Phase() {
		checkMinedAbility("Step/Phase", "step");
		checkMinedAbility("Step/Phase", "phase");
		checkMinedAbility("Step/Phase", "step|phase");
	}

	public void testRandom() {
		checkMinedAbility("Random", "random");
	}

	public void testLand() {
		String lands[] = { "Mountain", "Plain", "Swamp", "Forest", "Island" };
		for (int i = 0; i < lands.length; i++) {
			String land = lands[i];
			checkMinedAbility(land, land);
			checkMinedAbility(land, land + "s");
			checkMinedAbility(land, land + "walk");
			checkMinedAbility(land, land.toLowerCase());
		}
	}

	public void testBury() {
		checkMinedAbility("Bury", "bury");
	}

	public void testToken() {
		checkMinedAbility("Token", "(non)?tokens?");
	}

	public void testLibrary() {
		checkMinedAbility("Library", "library");
	}

	public void testGain() {
		checkMinedAbility("Gain", "gains?");
	}

	public void testFlip() {
		checkMinedAbility("Flip", "flip");
	}

	public void testExile() {
		checkMinedAbility("Exile", "exiled?");
	}

	public void testSummon() {
		// checkMinedAbility("Summon", "summon(ing)? sick(ness)?");
		checkMinedAbility("Summon", "summon sick");
		checkMinedAbility("Summon", "summoning sickness");
	}

	public void testAnte() {
		checkMinedAbility("Ante", "ante");
	}

	public void testType() {
		checkMinedAbility("Type", "type");
	}

	public void testReturn() {
		checkMinedAbility("Return", "return");
	}

	public void testIndestructible() {
		checkMinedAbility("Indestructible", "indestructible");
	}

	public void testOpponent() {
		checkMinedAbility("Opponent", "opponents?");
	}

	public void testFace() {
		checkMinedAbility("Face", "face");
	}

	public void testPay() {
		checkMinedAbility("Pay", "pay");
	}

	public void testInterrupt() {
		checkMinedAbility("Interrupt", "interrupt");
	}
}
