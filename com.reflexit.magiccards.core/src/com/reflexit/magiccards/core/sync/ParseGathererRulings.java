/*******************************************************************************
 * Copyright (c) 2008 Alena Laskavaia.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alena Laskavaia - initial API and implementation
 *    Terry Long - refactored ParseGathererLegality to instead retrieve rulings on cards
 *    
 *******************************************************************************/
package com.reflexit.magiccards.core.sync;

import org.eclipse.core.runtime.IProgressMonitor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.model.MagicCardPhisical;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;
import com.reflexit.magiccards.core.model.storage.IStorage;
import com.reflexit.magiccards.core.model.storage.IStorageContainer;

/**
 * Retrieve legality info
 */
public class ParseGathererRulings {
	private static final String GATHERER_URL_BASE = "http://gatherer.wizards.com/";
	private static final String RULINGS_QUERY_URL_BASE = GATHERER_URL_BASE + "Pages/Card/Details.aspx?multiverseid=";
	private static Charset UTF_8 = Charset.forName("utf-8");
	/*-
	 <div id="ctl00_ctl00_ctl00_MainContent_SubContent_SubContent_rulingsContainer" class="postContainer" style="display:block;">
	                        <table cellpadding="0" cellspacing="0">
	                            
	                                    <tr class="post evenItem" style="background-color: #efefef;">
	                                        <td id="ctl00_ctl00_ctl00_MainContent_SubContent_SubContent_rulingsRepeater_ctl00_rulingDate" style="width: 70px; padding-left: 10px; font-weight: bold;">2/1/2007</td>
	
	                                        <td id="ctl00_ctl00_ctl00_MainContent_SubContent_SubContent_rulingsRepeater_ctl00_rulingText" style="width: 610px; padding-right: 5px;">Removes all creature abilities. This includes mana abilities. Animated lands will also lose the ability to tap for mana. </td>
	
	                                    </tr>
	                                
	                        </table>
	                    </div>

	 */
	private static Pattern rulingPattern = Pattern.compile("<td.*?rulingText.*?5px;\">(.+?)</td>");
	private static Pattern ratingPattern = Pattern.compile("class=\"textRatingValue\">([0-9.]{1,5})</span");
	private static Pattern artistPattern = Pattern.compile("ArtistCredit\"\\sclass=\"value\">.*?\">(.*?)</a>");

	private void parseSingleCard(IMagicCard card, boolean updateRatings, boolean updateRulings, boolean updateArtists)
	        throws IOException {
		String rulings = "";
		URL url = new URL(RULINGS_QUERY_URL_BASE + card.getCardId());
		InputStream openStream = url.openStream();
		BufferedReader st = new BufferedReader(new InputStreamReader(openStream, UTF_8));
		String line;
		String html = "";
		while ((line = st.readLine()) != null) {
			html += line + " ";
		}
		Matcher matcher;
		if (updateRulings) {
			matcher = rulingPattern.matcher(html);
			while (matcher.find()) {
				String ruling = matcher.group(1).trim();
				if (ruling.length() == 0) {
					continue;
				}
				rulings += ruling + "\n";
			}
			if (rulings.length() > 0) {
				if (card instanceof MagicCard) {
					((MagicCard) card).setRulings(rulings);
				} else if (card instanceof MagicCardPhisical) {
					((MagicCardPhisical) card).setRulings(rulings);
				}
			}
		}
		if (updateRatings) {
			matcher = ratingPattern.matcher(html);
			if (matcher.find()) {
				String rating = matcher.group(1).trim();
				if (rating.length() != 0) {
					if (card instanceof MagicCard) {
						((MagicCard) card).setCommunityRating(Float.parseFloat(rating));
					} else if (card instanceof MagicCardPhisical) {
						((MagicCardPhisical) card).setCommunityRating(Float.parseFloat(rating));
					}
				}
			}
		}
		if (updateArtists) {
			matcher = artistPattern.matcher(html);
			if (matcher.find()) {
				String artist = matcher.group(1).trim();
				if (artist.length() != 0) {
					if (card instanceof MagicCard) {
						((MagicCard) card).setArtist(artist);
					} else if (card instanceof MagicCardPhisical) {
						((MagicCardPhisical) card).setArtist(artist);
					}
				}
			}
		}
		st.close();
	}

	public void updateCard(IMagicCard magicCard, IProgressMonitor monitor, boolean ratings, boolean rulings,
	        boolean artists) throws IOException {
		monitor.beginTask("Loading additional info...", 10);
		monitor.worked(1);
		try {
			// load individual card
			try {
				parseSingleCard(magicCard, ratings, rulings, artists);
			} catch (IOException e) {
				System.err.println("Cannot load card " + e.getMessage() + " " + magicCard.getCardId());
			}
			monitor.worked(8);
		} finally {
			monitor.done();
		}
	}

	public void updateStore(IFilteredCardStore<IMagicCard> fstore, IProgressMonitor monitor, boolean ratings,
	        boolean rulings, boolean artists) throws IOException {
		monitor.beginTask("Loading additional info...", fstore.getSize() + 10);
		IStorage storage = null;
		if (fstore.getCardStore() instanceof IStorageContainer) {
			storage = ((IStorageContainer) fstore.getCardStore()).getStorage();
			storage.setAutoCommit(false);
		}
		monitor.worked(5);
		try {
			for (IMagicCard magicCard : fstore) {
				if (monitor.isCanceled())
					return;
				// load individual card
				try {
					parseSingleCard(magicCard, ratings, rulings, artists);
				} catch (IOException e) {
					System.err.println("Cannot load card " + e.getMessage() + " " + magicCard.getCardId());
				}
				fstore.getCardStore().update(magicCard);
				monitor.worked(1);
			}
		} finally {
			if (storage != null) {
				storage.setAutoCommit(true);
				storage.save();
			}
			monitor.worked(5);
			monitor.done();
		}
	}

	public static void main(String[] args) throws IOException {
		MagicCard card = new MagicCard();
		card.setCardId(11179);
		ParseGathererRulings parser = new ParseGathererRulings();
		parser.parseSingleCard(card, true, true, true);
		System.err.println(card.getRulings() + " " + card.getArtist() + " " + card.getCommunityRating());
	}
}
