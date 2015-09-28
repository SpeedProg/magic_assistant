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
		parse(mimport);
	}

	private void preview() {
		preview(mimport);
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

	// http://magic.tcgplayer.com/db/search_result.asp?Set_Name=From%20the%20Vault:%20Twenty
	/*-
	 <table border="0" width="700" align="left">
	<tbody><tr>
		<td class="default_9" valign="middle" width="385" align="left">
						<img src="/images/sets/noset.jpg" onerror="this.src='/images/sets/noset.jpg'"><br><br>
						<h1>From the Vault: Twenty</h1>
		</td>
		<td valign="middle" width="150" align="center"><img src="http://magic.tcgplayer.com/images/TCGlogo_black.png"></td></tr>
	<tr>
	<td colspan="2" class="default_8" width="700"><br>
	<center><b>Always buy singles for the lowest price at TCGplayer.com!</b></center>

	</td></tr></tbody></table>
	<br clear="all">

	<table bordercolorlight="#303030" style="font-size:13px;" border="1" cellpadding="0" cellspacing="0" height="28" width="700" align="left" bgcolor="black">
	<tbody><tr style="page-break-inside: avoid;">
		<td class="default_8" valign="middle" width="200" align="center"><font color="white">Name</font></td>
		<td class="default_8" valign="middle" width="80" align="center"><font color="white">Cost</font></td>
		<td class="default_8" valign="middle" width="120" align="center"><font color="white">Type</font></td>
		<td class="default_8" valign="middle" width="50" align="center"><font color="white">Color</font></td>
		<td class="default_8" valign="middle" width="30" align="center"><font color="white">Rar</font></td>
		<td class="default_8" valign="middle" width="55" align="center"><font color="white">High</font></td>
		<td class="default_8" valign="middle" width="55" align="center"><font color="white">Mid</font></td>
		<td class="default_8" valign="middle" width="55" align="center"><font color="white">Low</font></td>
	</tr>
	</tbody></table>
	<br clear="all">
	<table style="font-size:11px;" border="1" cellpadding="0" cellspacing="0" width="700" align="left">


	<tbody><tr height="20"><td valign="center" width="200" align="left"><font class="default_7">&nbsp;Akroma's Vengeance</font></td><td valign="center" width="80" align="left"><font class="default_7">&nbsp;4WW</font></td><td valign="center" width="120" align="left"><font class="default_7">&nbsp;Sorcery</font></td><td valign="center" width="50" align="left"><font class="default_7">&nbsp;White</font></td><td valign="center" width="30" align="left"><font class="default_7">&nbsp;M</font></td><td valign="center" width="55" align="right"><font class="default_7">$3.45&nbsp;</font></td><td valign="center" width="55" align="right"><font class="default_7">$1.99&nbsp;</font></td><td valign="center" width="55" align="right"><font class="default_7">$1.49&nbsp;</font></td></tr>
	<tr height="20"><td valign="center" width="200" align="left"><font class="default_7">&nbsp;Chainer's Edict</font></td><td valign="center" width="80" align="left"><font class="default_7">&nbsp;1B</font></td><td valign="center" width="120" align="left"><font class="default_7">&nbsp;Sorcery</font></td><td valign="center" width="50" align="left"><font class="default_7">&nbsp;Black</font></td><td valign="center" width="30" align="left"><font class="default_7">&nbsp;M</font></td><td valign="center" width="55" align="right"><font class="default_7">$2.34&nbsp;</font></td><td valign="center" width="55" align="right"><font class="default_7">$1.36&nbsp;</font></td><td valign="center" width="55" align="right"><font class="default_7">$0.89&nbsp;</font></td></tr>
	<tr height="20"><td valign="center" width="200" align="left"><font class="default_7">&nbsp;Chameleon Colossus</font></td><td valign="center" width="80" align="left"><font class="default_7">&nbsp;2GG</font></td><td valign="center" width="120" align="left"><font class="default_7">&nbsp;Creature</font></td><td valign="center" width="50" align="left"><font class="default_7">&nbsp;Green</font></td><td valign="center" width="30" align="left"><font class="default_7">&nbsp;M</font></td><td valign="center" width="55" align="right"><font class="default_7">$4.11&nbsp;</font></td><td valign="center" width="55" align="right"><font class="default_7">$1.75&nbsp;</font></td><td valign="center" width="55" align="right"><font class="default_7">$0.88&nbsp;</font></td></tr>
	<tr height="20"><td valign="center" width="200" align="left"><font class="default_7">&nbsp;Char</font></td><td valign="center" width="80" align="left"><font class="default_7">&nbsp;2R</font></td><td valign="center" width="120" align="left"><font class="default_7">&nbsp;Instant</font></td><td valign="center" width="50" align="left"><font class="default_7">&nbsp;Red</font></td><td valign="center" width="30" align="left"><font class="default_7">&nbsp;M</font></td><td valign="center" width="55" align="right"><font class="default_7">$2.64&nbsp;</font></td><td valign="center" width="55" align="right"><font class="default_7">$0.78&nbsp;</font></td><td valign="center" width="55" align="right"><font class="default_7">$0.25&nbsp;</font></td></tr>
	<tr height="20"><td valign="center" width="200" align="left"><font class="default_7">&nbsp;Cruel Ultimatum</font></td><td valign="center" width="80" align="left"><font class="default_7">&nbsp;UUBBBRR</font></td><td valign="center" width="120" align="left"><font class="default_7">&nbsp;Sorcery</font></td><td valign="center" width="50" align="left"><font class="default_7">&nbsp;Gold</font></td><td valign="center" width="30" align="left"><font class="default_7">&nbsp;M</font></td><td valign="center" width="55" align="right"><font class="default_7">$4.95&nbsp;</font></td><td valign="center" width="55" align="right"><font class="default_7">$1.47&nbsp;</font></td><td valign="center" width="55" align="right"><font class="default_7">$0.89&nbsp;</font></td></tr>
	<tr height="20"><td valign="center" width="200" align="left"><font class="default_7">&nbsp;Dark Ritual</font></td><td valign="center" width="80" align="left"><font class="default_7">&nbsp;B</font></td><td valign="center" width="120" align="left"><font class="default_7">&nbsp;Instant</font></td><td valign="center" width="50" align="left"><font class="default_7">&nbsp;Black</font></td><td valign="center" width="30" align="left"><font class="default_7">&nbsp;M</font></td><td valign="center" width="55" align="right"><font class="default_7">$18.95&nbsp;</font></td><td valign="center" width="55" align="right"><font class="default_7">$4.74&nbsp;</font></td><td valign="center" width="55" align="right"><font class="default_7">$2.75&nbsp;</font></td></tr>
	<tr height="20"><td valign="center" width="200" align="left"><font class="default_7">&nbsp;Fact or Fiction</font></td><td valign="center" width="80" align="left"><font class="default_7">&nbsp;3UU</font></td><td valign="center" width="120" align="left"><font class="default_7">&nbsp;Instant</font></td><td valign="center" width="50" align="left"><font class="default_7">&nbsp;Blue</font></td><td valign="center" width="30" align="left"><font class="default_7">&nbsp;M</font></td><td valign="center" width="55" align="right"><center><font class="default_7">-</font></center></td><td valign="center" width="55" align="right"><center><font class="default_7">SOON</font></center></td><td valign="center" width="55" align="right"><center><font class="default_7">-</font></center></td></tr>
	<tr height="20"><td valign="center" width="200" align="left"><font class="default_7">&nbsp;Fact or Fiction</font></td><td valign="center" width="80" align="left"><font class="default_7">&nbsp;3U</font></td><td valign="center" width="120" align="left"><font class="default_7">&nbsp;Instant</font></td><td valign="center" width="50" align="left"><font class="default_7">&nbsp;Blue</font></td><td valign="center" width="30" align="left"><font class="default_7">&nbsp;M</font></td><td valign="center" width="55" align="right"><font class="default_7">$4.49&nbsp;</font></td><td valign="center" width="55" align="right"><font class="default_7">$1.86&nbsp;</font></td><td valign="center" width="55" align="right"><font class="default_7">$0.99&nbsp;</font></td></tr>
	<tr height="20"><td valign="center" width="200" align="left"><font class="default_7">&nbsp;Fyndhorn Elves</font></td><td valign="center" width="80" align="left"><font class="default_7">&nbsp;G</font></td><td valign="center" width="120" align="left"><font class="default_7">&nbsp;Creature</font></td><td valign="center" width="50" align="left"><font class="default_7">&nbsp;Green</font></td><td valign="center" width="30" align="left"><font class="default_7">&nbsp;M</font></td><td valign="center" width="55" align="right"><font class="default_7">$4.98&nbsp;</font></td><td valign="center" width="55" align="right"><font class="default_7">$3.15&nbsp;</font></td><td valign="center" width="55" align="right"><font class="default_7">$2.46&nbsp;</font></td></tr>
	<tr height="20"><td valign="center" width="200" align="left"><font class="default_7">&nbsp;Gilded Lotus</font></td><td valign="center" width="80" align="left"><font class="default_7">&nbsp;5</font></td><td valign="center" width="120" align="left"><font class="default_7">&nbsp;Artifact</font></td><td valign="center" width="50" align="left"><font class="default_7">&nbsp;Colorless</font></td><td valign="center" width="30" align="left"><font class="default_7">&nbsp;M</font></td><td valign="center" width="55" align="right"><font class="default_7">$10.95&nbsp;</font></td><td valign="center" width="55" align="right"><font class="default_7">$8.52&nbsp;</font></td><td valign="center" width="55" align="right"><font class="default_7">$7.07&nbsp;</font></td></tr>
	<tr height="20"><td valign="center" width="200" align="left"><font class="default_7">&nbsp;Green Sun's Zenith</font></td><td valign="center" width="80" align="left"><font class="default_7">&nbsp;XG</font></td><td valign="center" width="120" align="left"><font class="default_7">&nbsp;Sorcery</font></td><td valign="center" width="50" align="left"><font class="default_7">&nbsp;Green</font></td><td valign="center" width="30" align="left"><font class="default_7">&nbsp;M</font></td><td valign="center" width="55" align="right"><font class="default_7">$9.72&nbsp;</font></td><td valign="center" width="55" align="right"><font class="default_7">$7.65&nbsp;</font></td><td valign="center" width="55" align="right"><font class="default_7">$6.00&nbsp;</font></td></tr>
	<tr height="20"><td valign="center" width="200" align="left"><font class="default_7">&nbsp;Hymn to Tourach</font></td><td valign="center" width="80" align="left"><font class="default_7">&nbsp;BB</font></td><td valign="center" width="120" align="left"><font class="default_7">&nbsp;Sorcery</font></td><td valign="center" width="50" align="left"><font class="default_7">&nbsp;Black</font></td><td valign="center" width="30" align="left"><font class="default_7">&nbsp;M</font></td><td valign="center" width="55" align="right"><font class="default_7">$14.00&nbsp;</font></td><td valign="center" width="55" align="right"><font class="default_7">$6.25&nbsp;</font></td><td valign="center" width="55" align="right"><font class="default_7">$4.00&nbsp;</font></td></tr>
	<tr height="20"><td valign="center" width="200" align="left"><font class="default_7">&nbsp;Impulse</font></td><td valign="center" width="80" align="left"><font class="default_7">&nbsp;1U</font></td><td valign="center" width="120" align="left"><font class="default_7">&nbsp;Instant</font></td><td valign="center" width="50" align="left"><font class="default_7">&nbsp;Blue</font></td><td valign="center" width="30" align="left"><font class="default_7">&nbsp;M</font></td><td valign="center" width="55" align="right"><font class="default_7">$3.15&nbsp;</font></td><td valign="center" width="55" align="right"><font class="default_7">$1.76&nbsp;</font></td><td valign="center" width="55" align="right"><font class="default_7">$0.99&nbsp;</font></td></tr>
	<tr height="20"><td valign="center" width="200" align="left"><font class="default_7">&nbsp;Ink-Eyes, Servant of Oni</font></td><td valign="center" width="80" align="left"><font class="default_7">&nbsp;4BB</font></td><td valign="center" width="120" align="left"><font class="default_7">&nbsp;Creature</font></td><td valign="center" width="50" align="left"><font class="default_7">&nbsp;Black</font></td><td valign="center" width="30" align="left"><font class="default_7">&nbsp;M</font></td><td valign="center" width="55" align="right"><font class="default_7">$6.95&nbsp;</font></td><td valign="center" width="55" align="right"><font class="default_7">$4.26&nbsp;</font></td><td valign="center" width="55" align="right"><font class="default_7">$2.99&nbsp;</font></td></tr>
	<tr height="20"><td valign="center" width="200" align="left"><font class="default_7">&nbsp;Jace, the Mind Sculptor</font></td><td valign="center" width="80" align="left"><font class="default_7">&nbsp;2UU</font></td><td valign="center" width="120" align="left"><font class="default_7">&nbsp;Planeswalker</font></td><td valign="center" width="50" align="left"><font class="default_7">&nbsp;Blue</font></td><td valign="center" width="30" align="left"><font class="default_7">&nbsp;M</font></td><td valign="center" width="55" align="right"><font class="default_7">$149.99&nbsp;</font></td><td valign="center" width="55" align="right"><font class="default_7">$88.14&nbsp;</font></td><td valign="center" width="55" align="right"><font class="default_7">$74.89&nbsp;</font></td></tr>
	<tr height="20"><td valign="center" width="200" align="left"><font class="default_7">&nbsp;Kessig Wolf Run</font></td><td valign="center" width="80" align="left"><font class="default_7">&nbsp;</font></td><td valign="center" width="120" align="left"><font class="default_7">&nbsp;Land</font></td><td valign="center" width="50" align="left"><font class="default_7">&nbsp;Land</font></td><td valign="center" width="30" align="left"><font class="default_7">&nbsp;M</font></td><td valign="center" width="55" align="right"><font class="default_7">$4.99&nbsp;</font></td><td valign="center" width="55" align="right"><font class="default_7">$1.75&nbsp;</font></td><td valign="center" width="55" align="right"><font class="default_7">$1.00&nbsp;</font></td></tr>
	<tr height="20"><td valign="center" width="200" align="left"><font class="default_7">&nbsp;Swords to Plowshares</font></td><td valign="center" width="80" align="left"><font class="default_7">&nbsp;W</font></td><td valign="center" width="120" align="left"><font class="default_7">&nbsp;Instant</font></td><td valign="center" width="50" align="left"><font class="default_7">&nbsp;White</font></td><td valign="center" width="30" align="left"><font class="default_7">&nbsp;M</font></td><td valign="center" width="55" align="right"><font class="default_7">$15.00&nbsp;</font></td><td valign="center" width="55" align="right"><font class="default_7">$6.99&nbsp;</font></td><td valign="center" width="55" align="right"><font class="default_7">$5.00&nbsp;</font></td></tr>
	<tr height="20"><td valign="center" width="200" align="left"><font class="default_7">&nbsp;Tangle Wire</font></td><td valign="center" width="80" align="left"><font class="default_7">&nbsp;3</font></td><td valign="center" width="120" align="left"><font class="default_7">&nbsp;Artifact</font></td><td valign="center" width="50" align="left"><font class="default_7">&nbsp;</font></td><td valign="center" width="30" align="left"><font class="default_7">&nbsp;M</font></td><td valign="center" width="55" align="right"><font class="default_7">$4.32&nbsp;</font></td><td valign="center" width="55" align="right"><font class="default_7">$2.74&nbsp;</font></td><td valign="center" width="55" align="right"><font class="default_7">$1.93&nbsp;</font></td></tr>
	<tr height="20"><td valign="center" width="200" align="left"><font class="default_7">&nbsp;Thran Dynamo</font></td><td valign="center" width="80" align="left"><font class="default_7">&nbsp;4</font></td><td valign="center" width="120" align="left"><font class="default_7">&nbsp;Artifact</font></td><td valign="center" width="50" align="left"><font class="default_7">&nbsp;</font></td><td valign="center" width="30" align="left"><font class="default_7">&nbsp;M</font></td><td valign="center" width="55" align="right"><font class="default_7">$15.27&nbsp;</font></td><td valign="center" width="55" align="right"><font class="default_7">$7.25&nbsp;</font></td><td valign="center" width="55" align="right"><font class="default_7">$5.95&nbsp;</font></td></tr>
	<tr height="20"><td valign="center" width="200" align="left"><font class="default_7">&nbsp;Venser, Shaper Savant</font></td><td valign="center" width="80" align="left"><font class="default_7">&nbsp;2UU</font></td><td valign="center" width="120" align="left"><font class="default_7">&nbsp;Creature</font></td><td valign="center" width="50" align="left"><font class="default_7">&nbsp;Blue</font></td><td valign="center" width="30" align="left"><font class="default_7">&nbsp;M</font></td><td valign="center" width="55" align="right"><font class="default_7">$14.56&nbsp;</font></td><td valign="center" width="55" align="right"><font class="default_7">$11.99&nbsp;</font></td><td valign="center" width="55" align="right"><font class="default_7">$9.50&nbsp;</font></td></tr>
	<tr height="20"><td valign="center" width="200" align="left"><font class="default_7">&nbsp;Wall of Blossoms</font></td><td valign="center" width="80" align="left"><font class="default_7">&nbsp;1G</font></td><td valign="center" width="120" align="left"><font class="default_7">&nbsp;Creature</font></td><td valign="center" width="50" align="left"><font class="default_7">&nbsp;Green</font></td><td valign="center" width="30" align="left"><font class="default_7">&nbsp;M</font></td><td valign="center" width="55" align="right"><font class="default_7">$2.60&nbsp;</font></td><td valign="center" width="55" align="right"><font class="default_7">$1.65&nbsp;</font></td><td valign="center" width="55" align="right"><font class="default_7">$1.24&nbsp;</font></td></tr>




	</tbody></table>
	 */
	@Test
	public void testTCGPlayerPrices() {
		addLine(getAboveComment());
		preview();
		assertEquals(21, resSize);
	}

	/*-
	 <table style="background: #0E5B93;" align="center" width="540">
				<tbody><tr>
					<td align="left" valign="middle" width="160"><a style="color:#FFF;font-weight:bold;font-size:11px;" href="javascript: SortOrder('name');" class="default_7w">Name</a></td>
					<td align="left" valign="middle" width="60"><a style="color:#FFF;font-weight:bold;font-size:11px;" href="javascript: SortOrder('Cost');" class="default_7w">Cost</a></td>
					<td align="left" valign="middle" width="140"><a style="color:#FFF;font-weight:bold;font-size:11px;" href="javascript: SortOrder('Set_name');" class="default_7w">Set Name</a></td>
					<td align="center" valign="middle" width="15"><a style="color:#FFF;font-weight:bold;font-size:11px;" href="javascript: SortOrder('Rarity');" class="default_7w">R</a></td>
					<td align="center" valign="middle" width="55"><a style="color:#FFF;font-weight:bold;font-size:11px;" href="javascript: SortOrder('MaxPrice DESC');" class="default_7w">High</a></td>
					<td align="center" valign="middle" width="55"><a style="color:#FFF;font-weight:bold;font-size:11px;" href="javascript: SortOrder('MeanPrice DESC');" class="default_7w">Med</a></td>
					<td align="center" valign="middle" width="55"><a style="color:#FFF;font-weight:bold;font-size:11px;" href="javascript: SortOrder('MinPrice DESC');" class="default_7w">Low</a></td>
				</tr>
			</tbody></table>	
	<table style="font-size:11px" align="center" border="0" cellpadding="1" cellspacing="0" width="540">

	<tbody><tr>

		<td class="default_8" align="left" bgcolor="#EEEEEE" valign="center" width="160">
			&nbsp;<a href="magic_single_card.asp?cn=Akroma's Vengeance&amp;sn=From the Vault: Twenty">Akroma's Vengeance</a></td>

		<td class="default_8" align="left" bgcolor="#EEEEEE" valign="center" width="60">
			4WW
		</td>

		<td class="default_8" align="left" bgcolor="#EEEEEE" valign="center" width="140">
			&nbsp;<a href="/db/search_result.asp?Set_Name=From+the+Vault%3A+Twenty">From the Vault: Twenty</a>
		</td>
		<td class="default_8" align="left" bgcolor="#EEEEEE" valign="center" width="15">
			M
		</td>


		<td class="default_8" align="right" bgcolor="#D9FCD1" valign="center" width="55">
		<a href="magic_single_card.asp?cn=Akroma's Vengeance&amp;sn=From the Vault: Twenty">$3.45</a>
		</td>

		<td class="default_8" align="right" bgcolor="#D1DFFC" valign="center" width="55">
		<a href="magic_single_card.asp?cn=Akroma's Vengeance&amp;sn=From the Vault: Twenty">$1.99</a>
		</td>

		<td class="default_8" align="right" bgcolor="#FCD1D1" valign="center" width="55">
		<a href="magic_single_card.asp?cn=Akroma's Vengeance&amp;sn=From the Vault: Twenty">$1.49</a>
		</td>


	</tr>
	
	

	<tr>

		<td class="default_8" align="left" bgcolor="#EEEEEE" valign="center" width="160">
			&nbsp;<a href="magic_single_card.asp?cn=Chameleon Colossus&amp;sn=From the Vault: Twenty">Chameleon Colossus</a></td>

		<td class="default_8" align="left" bgcolor="#EEEEEE" valign="center" width="60">
			2GG
		</td>

		<td class="default_8" align="left" bgcolor="#EEEEEE" valign="center" width="140">
			&nbsp;<a href="/db/search_result.asp?Set_Name=From+the+Vault%3A+Twenty">From the Vault: Twenty</a>
		</td>
		<td class="default_8" align="left" bgcolor="#EEEEEE" valign="center" width="15">
			M
		</td>


		<td class="default_8" align="right" bgcolor="#D9FCD1" valign="center" width="55">
		<a href="magic_single_card.asp?cn=Chameleon Colossus&amp;sn=From the Vault: Twenty">$4.11</a>
		</td>

		<td class="default_8" align="right" bgcolor="#D1DFFC" valign="center" width="55">
		<a href="magic_single_card.asp?cn=Chameleon Colossus&amp;sn=From the Vault: Twenty">$1.75</a>
		</td>

		<td class="default_8" align="right" bgcolor="#FCD1D1" valign="center" width="55">
		<a href="magic_single_card.asp?cn=Chameleon Colossus&amp;sn=From the Vault: Twenty">$0.95</a>
		</td>


	</tr>
	 */
	@Test
	public void testTCGPlayerPrices2() {
		addLine(getAboveComment());
		preview();
		assertEquals(2, resSize);
		assertEquals("From the Vault: Twenty", card1.getSet());
		assertEquals("From the Vault: Twenty", card2.getSet());
	}
}
