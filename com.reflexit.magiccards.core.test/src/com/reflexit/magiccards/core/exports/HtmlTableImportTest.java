/*******************************************************************************
 * Copyright (c) 2008 Alena Laskavaia. All rights reserved. This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Alena Laskavaia - initial API and implementation
 *******************************************************************************/
package com.reflexit.magiccards.core.exports;

import org.junit.Test;

import com.reflexit.magiccards.core.model.MagicCardPhysical;

public class HtmlTableImportTest extends AbstarctImportTest {
	private HtmlTableImportDelegate mimport = new HtmlTableImportDelegate();

	private void parse() {
		parse(true, mimport);
	}

	private void preview() {
		preview(true, mimport);
		if (exception != null)
			fail(exception.getMessage());
	}

	/*-
	<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
	<html><head><title>main</title><meta http-equiv="Content-Type" content="text/html;charset=UTF-8"></head><body>
	<table>
	<tr><th>NAME</th><th>COUNT</th><th>FORTRADECOUNT</th><th>OWN_COUNT</th><th>SPECIAL</th><th>COST</th><th>TYPE</th><th>P</th><th>T</th><th>OWNERSHIP</th><th>SIDEBOARD</th></tr>
	<tr>
	<td colspan=11>main</td>
	</tr>
	<tr><td><img  style="float:left"  src="http://gatherer.wizards.com/Handlers/Image.ashx?type=symbol&set=5DN&size=small&rarity=U" alt=""/>&nbsp;Vedalken Mastermind</td><td>3</td><td>0</td><td>3</td><td></td><td><img  style="float:left"  src="http://gatherer.wizards.com/Handlers/Image.ashx?size=small&name=U&type=symbol" alt="{U}"/><img  style="float:left"  src="http://gatherer.wizards.com/Handlers/Image.ashx?size=small&name=U&type=symbol" alt="{U}"/></td><td>Creature - Vedalken Wizard</td><td>1</td><td>2</td><td>true</td><td>false</td></tr>
	<tr><td><img  style="float:left"  src="http://gatherer.wizards.com/Handlers/Image.ashx?type=symbol&set=M15&size=small&rarity=C" alt=""/>&nbsp;Проклятый Дух</td><td>2</td><td>0</td><td>2</td><td></td><td><img  style="float:left"  src="http://gatherer.wizards.com/Handlers/Image.ashx?size=small&name=3&type=symbol" alt="{3}"/><img  style="float:left"  src="http://gatherer.wizards.com/Handlers/Image.ashx?size=small&name=B&type=symbol" alt="{B}"/></td><td>Существо - Дух</td><td>3</td><td>2</td><td>true</td><td>false</td></tr>
	<tr><td><img  style="float:left"  src="http://gatherer.wizards.com/Handlers/Image.ashx?type=symbol&set=10E&size=small&rarity=R" alt=""/>&nbsp;Reya Dawnbringer</td><td>12</td><td>0</td><td>12</td><td></td><td><img  style="float:left"  src="http://gatherer.wizards.com/Handlers/Image.ashx?size=small&name=6&type=symbol" alt="{6}"/><img  style="float:left"  src="http://gatherer.wizards.com/Handlers/Image.ashx?size=small&name=W&type=symbol" alt="{W}"/><img  style="float:left"  src="http://gatherer.wizards.com/Handlers/Image.ashx?size=small&name=W&type=symbol" alt="{W}"/><img  style="float:left"  src="http://gatherer.wizards.com/Handlers/Image.ashx?size=small&name=W&type=symbol" alt="{W}"/></td><td>Legendary Creature - Angel</td><td>4</td><td>6</td><td>true</td><td>false</td></tr>
	<tr><td><img  style="float:left"  src="http://gatherer.wizards.com/Handlers/Image.ashx?type=symbol&set=10E&size=small&rarity=U" alt=""/>&nbsp;Angel of Mercy</td><td>3</td><td>0</td><td>3</td><td></td><td><img  style="float:left"  src="http://gatherer.wizards.com/Handlers/Image.ashx?size=small&name=4&type=symbol" alt="{4}"/><img  style="float:left"  src="http://gatherer.wizards.com/Handlers/Image.ashx?size=small&name=W&type=symbol" alt="{W}"/></td><td>Creature - Angel</td><td>3</td><td>3</td><td>true</td><td>false</td></tr>
	<tr><td><img  style="float:left"  src="http://gatherer.wizards.com/Handlers/Image.ashx?type=symbol&set=M10&size=small&rarity=M" alt=""/>&nbsp;Platinum Angel</td><td>9</td><td>0</td><td>9</td><td></td><td><img  style="float:left"  src="http://gatherer.wizards.com/Handlers/Image.ashx?size=small&name=7&type=symbol" alt="{7}"/></td><td>Artifact Creature - Angel</td><td>4</td><td>4</td><td>true</td><td>false</td></tr>
	<tr><td><img  style="float:left"  src="http://gatherer.wizards.com/Handlers/Image.ashx?type=symbol&set=NPH&size=small&rarity=U" alt=""/>&nbsp;Arm with Æther</td><td>1</td><td>0</td><td>1</td><td></td><td><img  style="float:left"  src="http://gatherer.wizards.com/Handlers/Image.ashx?size=small&name=2&type=symbol" alt="{2}"/><img  style="float:left"  src="http://gatherer.wizards.com/Handlers/Image.ashx?size=small&name=U&type=symbol" alt="{U}"/></td><td>Sorcery</td><td></td><td></td><td>true</td><td>false</td></tr>
	<tr><td><img  style="float:left"  src="http://gatherer.wizards.com/Handlers/Image.ashx?type=symbol&set=NPH&size=small&rarity=U" alt=""/>&nbsp;Alloy Myr</td><td>1</td><td>0</td><td>1</td><td></td><td><img  style="float:left"  src="http://gatherer.wizards.com/Handlers/Image.ashx?size=small&name=3&type=symbol" alt="{3}"/></td><td>Artifact Creature - Myr</td><td>2</td><td>2</td><td>true</td><td>false</td></tr>
	<tr><td><img  style="float:left"  src="http://gatherer.wizards.com/Handlers/Image.ashx?type=symbol&set=NPH&size=small&rarity=U" alt=""/>&nbsp;Argent Mutation</td><td>1</td><td>0</td><td>1</td><td></td><td><img  style="float:left"  src="http://gatherer.wizards.com/Handlers/Image.ashx?size=small&name=2&type=symbol" alt="{2}"/><img  style="float:left"  src="http://gatherer.wizards.com/Handlers/Image.ashx?size=small&name=U&type=symbol" alt="{U}"/></td><td>Instant</td><td></td><td></td><td>true</td><td>false</td></tr>
	<tr><td><img  style="float:left"  src="http://gatherer.wizards.com/Handlers/Image.ashx?type=symbol&set=NPH&size=small&rarity=C" alt=""/>&nbsp;Artillerize</td><td>11</td><td>11</td><td>11</td><td>fortrade</td><td><img  style="float:left"  src="http://gatherer.wizards.com/Handlers/Image.ashx?size=small&name=3&type=symbol" alt="{3}"/><img  style="float:left"  src="http://gatherer.wizards.com/Handlers/Image.ashx?size=small&name=R&type=symbol" alt="{R}"/></td><td>Instant</td><td></td><td></td><td>true</td><td>false</td></tr>
	<tr><td><img  style="float:left"  src="http://gatherer.wizards.com/Handlers/Image.ashx?type=symbol&set=M15&size=small&rarity=C" alt=""/>&nbsp;Accursed Spirit</td><td>2</td><td>0</td><td>2</td><td></td><td><img  style="float:left"  src="http://gatherer.wizards.com/Handlers/Image.ashx?size=small&name=3&type=symbol" alt="{3}"/><img  style="float:left"  src="http://gatherer.wizards.com/Handlers/Image.ashx?size=small&name=B&type=symbol" alt="{B}"/></td><td>Creature - Spirit</td><td>3</td><td>2</td><td>true</td><td>false</td></tr>

	<tr><td><img  style="float:left"  src="http://gatherer.wizards.com/Handlers/Image.ashx?type=symbol&set=M15&size=small&rarity=L" alt=""/>&nbsp;Болото</td><td>1</td><td>0</td><td>1</td><td></td><td></td><td>Базовая Земля - Болото</td><td></td><td></td><td>true</td><td>false</td></tr>
	<tr><td><img  style="float:left"  src="http://gatherer.wizards.com/Handlers/Image.ashx?type=symbol&set=C14&size=small&rarity=R" alt=""/>&nbsp;Angel of the Dire Hour</td><td>2</td><td>0</td><td>2</td><td></td><td><img  style="float:left"  src="http://gatherer.wizards.com/Handlers/Image.ashx?size=small&name=5&type=symbol" alt="{5}"/><img  style="float:left"  src="http://gatherer.wizards.com/Handlers/Image.ashx?size=small&name=W&type=symbol" alt="{W}"/><img  style="float:left"  src="http://gatherer.wizards.com/Handlers/Image.ashx?size=small&name=W&type=symbol" alt="{W}"/></td><td>Creature - Angel</td><td>5</td><td>4</td><td>true</td><td>false</td></tr>
	<tr><td><img  style="float:left"  src="http://gatherer.wizards.com/Handlers/Image.ashx?type=symbol&set=C14&size=small&rarity=L" alt=""/>&nbsp;Palude</td><td>1</td><td>0</td><td>1</td><td></td><td></td><td>Terra Base - Palude</td><td></td><td></td><td>true</td><td>false</td></tr>
	</table>
	</body></html>
	 */
	@Test
	public void testMA() {
		addLine(getAboveComment());
		preview();
		assertEquals(13, resSize);
		assertEquals("Vedalken Mastermind", card1.getName());
		assertEquals("Проклятый Дух", card2.getName());
		assertEquals(3, ((MagicCardPhysical) card1).getCount());
		parse();
		assertEquals(13, resSize);
		assertEquals("Vedalken Mastermind", card1.getName());
		assertEquals(3, ((MagicCardPhysical) card1).getCount());
	}

	/*-
	         <table>
	<thead><tr><th></th><th>Qty</th><th>Min</th><th>Avg</th><th>High</th></tr></thead>
	<tbody>
	<tr><td>Have</td><td class="quantity">764</td><td class="minPrice">$89.64</td><td class="avgPrice">$212.58</td><td class="maxPrice">$788.32</td></tr>
	</tbody>
	</table>

	    </div>
	    <div class="clear">
	    </div>
	 </div>
	<div id="collectionProcessingDiv" style="display: none; height: 30px;">
	    <img src="/content/images/loading.gif" alt="Loading...Please wait" title="Loading...Please wait">
	</div>
	    <div id="collectionContainer">
	        <table class="CollectionTable tablesorter"><colgroup><col style="width: 52px;"><col style="width: 432px;"><col style="width: 101px;"><col style="width: 177px;"><col style="width: 53px;"><col style="width: 53px;"><col style="width: 53px;"></colgroup><thead>
	<tr>
	<th class="CollectionTableCountHeader header" style="width:50px;">Have</th>
	<th class="header">Name</th>
	<th class="header">Game</th>
	<th class="header" style="width:175px;">Set</th>
	<th class="CollectionTablePriceHeader header" style="width:55px;">Low</th>
	<th class="CollectionTablePriceHeader header" style="width:55px;">Mid</th>
	<th class="CollectionTablePriceHeader header" style="width:55px;">High</th>
	</tr></thead><tbody>
	<tr id="StoreProductId_56263"><td>4</td><td class="textLeft"><img src="../../images/camera.png" rel="#imageHover" image="http://i.tcgplayer.com/56263.jpg" class="imageHover" height="11" width="11"> <a target="_blank" href="../../magic/innistrad/abattoir-ghoul">Abattoir Ghoul - [Foil]</a></td><td class="textLeft">Magic</td><td class="textLeft">Innistrad</td><td class="textRight minPrice" style="padding-right:5px;"><a href="/productcatalog/product/productsearch?id=56263">$0.03</a></td><td class="textRight avgPrice" style="padding-right:5px;"><a href="/productcatalog/product/productsearch?id=56263">$0.15</a></td><td class="textRight maxPrice" style="padding-right:5px;"><a href="/productcatalog/product/productsearch?id=56263">$0.75</a></td></tr>
	<tr id="StoreProductId_90970"><td>2</td><td class="textLeft"><img src="../../images/camera.png" rel="#imageHover" image="http://i.tcgplayer.com/90970.jpg" class="imageHover" height="11" width="11"> <a target="_blank" href="../../magic/magic-2015-(m15)/accursed-spirit">Accursed Spirit </a></td><td class="textLeft">Magic</td><td class="textLeft">Magic 2015 (M15)</td><td class="textRight minPrice" style="padding-right:5px;"><a href="/productcatalog/product/productsearch?id=90970">$0.03</a></td><td class="textRight avgPrice" style="padding-right:5px;"><a href="/productcatalog/product/productsearch?id=90970">$0.14</a></td><td class="textRight maxPrice" style="padding-right:5px;"><a href="/productcatalog/product/productsearch?id=90970">$0.70</a></td></tr>
	<tr id="StoreProductId_60008"><td>2</td><td class="textLeft"><img src="../../images/camera.png" rel="#imageHover" image="http://i.tcgplayer.com/60008.jpg" class="imageHover" height="11" width="11"> <a target="_blank" href="../../magic/magic-2013-(m13)/acidic-slime">Acidic Slime </a></td><td class="textLeft">Magic</td><td class="textLeft">Magic 2013 (M13)</td><td class="textRight minPrice" style="padding-right:5px;"><a href="/productcatalog/product/productsearch?id=60008">$0.05</a></td><td class="textRight avgPrice" style="padding-right:5px;"><a href="/productcatalog/product/productsearch?id=60008">$0.17</a></td><td class="textRight maxPrice" style="padding-right:5px;"><a href="/productcatalog/product/productsearch?id=60008">$0.85</a></td></tr>
	<tr id="StoreProductId_91255"><td>1</td><td class="textLeft"><img src="../../images/camera.png" rel="#imageHover" image="http://i.tcgplayer.com/91255.jpg" class="imageHover" height="11" width="11"> <a target="_blank" href="../../magic/magic-2015-(m15)/act-on-impulse">Act on Impulse </a></td><td class="textLeft">Magic</td><td class="textLeft">Magic 2015 (M15)</td><td class="textRight minPrice" style="padding-right:5px;"><a href="/productcatalog/product/productsearch?id=91255">$0.04</a></td><td class="textRight avgPrice" style="padding-right:5px;"><a href="/productcatalog/product/productsearch?id=91255">$0.20</a></td><td class="textRight maxPrice" style="padding-right:5px;"><a href="/productcatalog/product/productsearch?id=91255">$1.00</a></td></tr>
	<tr id="StoreProductId_91289"><td>5</td><td class="textLeft"><img src="../../images/camera.png" rel="#imageHover" image="http://i.tcgplayer.com/91289.jpg" class="imageHover" height="11" width="11"> <a target="_blank" href="../../magic/magic-2015-(m15)/aeronaut-tinkerer">Aeronaut Tinkerer </a></td><td class="textLeft">Magic</td><td class="textLeft">Magic 2015 (M15)</td><td class="textRight minPrice" style="padding-right:5px;"><a href="/productcatalog/product/productsearch?id=91289">$0.03</a></td><td class="textRight avgPrice" style="padding-right:5px;"><a href="/productcatalog/product/productsearch?id=91289">$0.13</a></td><td class="textRight maxPrice" style="padding-right:5px;"><a href="/productcatalog/product/productsearch?id=91289">$0.65</a></td></tr>
	<tr id="StoreProductId_57632"><td>1</td><td class="textLeft"><img src="../../images/camera.png" rel="#imageHover" image="http://i.tcgplayer.com/57632.jpg" class="imageHover" height="11" width="11"> <a target="_blank" href="../../magic/dark-ascension/afflicted-deserter">Afflicted Deserter </a></td><td class="textLeft">Magic</td><td class="textLeft">Dark Ascension</td><td class="textRight minPrice" style="padding-right:5px;"><a href="/productcatalog/product/productsearch?id=57632">$0.05</a></td><td class="textRight avgPrice" style="padding-right:5px;"><a href="/productcatalog/product/productsearch?id=57632">$0.16</a></td><td class="textRight maxPrice" style="padding-right:5px;"><a href="/productcatalog/product/productsearch?id=57632">$0.80</a></td></tr>
	<tr id="StoreProductId_90936"><td>2</td><td class="textLeft"><img src="../../images/camera.png" rel="#imageHover" image="http://i.tcgplayer.com/90936.jpg" class="imageHover" height="11" width="11"> <a target="_blank" href="../../magic/magic-2015-(m15)/zof-shade">Zof Shade </a></td><td class="textLeft">Magic</td><td class="textLeft">Magic 2015 (M15)</td><td class="textRight minPrice" style="padding-right:5px;"><a href="/productcatalog/product/productsearch?id=90936">$0.03</a></td><td class="textRight avgPrice" style="padding-right:5px;"><a href="/productcatalog/product/productsearch?id=90936">$0.14</a></td><td class="textRight maxPrice" style="padding-right:5px;"><a href="/productcatalog/product/productsearch?id=90936">$0.70</a></td></tr>
	<tr id="StoreProductId_59983"><td>1</td><td class="textLeft"><img src="../../images/camera.png" rel="#imageHover" image="http://i.tcgplayer.com/59983.jpg" class="imageHover" height="11" width="11"> <a target="_blank" href="../../magic/magic-2013-(m13)/zombie-goliath">Zombie Goliath </a></td><td class="textLeft">Magic</td><td class="textLeft">Magic 2013 (M13)</td><td class="textRight minPrice" style="padding-right:5px;"><a href="/productcatalog/product/productsearch?id=59983">$0.03</a></td><td class="textRight avgPrice" style="padding-right:5px;"><a href="/productcatalog/product/productsearch?id=59983">$0.14</a></td><td class="textRight maxPrice" style="padding-right:5px;"><a href="/productcatalog/product/productsearch?id=59983">$0.70</a></td></tr>
	</tbody></table>
	    </div>
	</form>
	 */
	@Test
	public void testTCGPlayer() {
		addLine(getAboveComment());
		preview();
		assertEquals(8, resSize);
		assertEquals("Abattoir Ghoul", card1.getName());
		assertEquals("Magic 2015 Core Set", card2.getSet());
		assertEquals(4, ((MagicCardPhysical) card1).getCount());
		parse();
		assertEquals(8, resSize);
		assertEquals("Magic 2015 Core Set", card2.getSet());
	}
}
