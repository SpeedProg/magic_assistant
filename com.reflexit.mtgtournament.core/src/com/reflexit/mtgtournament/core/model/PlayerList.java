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
package com.reflexit.mtgtournament.core.model;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Alena
 * 
 */
public class PlayerList {
	transient int maxId = 0;
	private Collection<Player> players = new ArrayList<Player>();

	public void addAllPlayers(Collection<Player> players) {
		for (Player player : players) {
			addPlayer(player);
		}
	}

	public boolean exists(Player p) {
		if (players.contains(p))
			return true;
		return false;
	}

	public void addPlayer(Player player) {
		if (!players.contains(player))
			players.add(player);
	}

	public Collection<Player> getPlayers() {
		return players;
	}

	/**
	 * @param object
	 */
	public void removePlayer(Player player) {
		players.remove(player);
	}

	/**
	 * @return
	 */
	public int size() {
		return players.size();
	}

	/**
	 * @return
	 */
	public String getNewId() {
		if (maxId == 0)
			setMaxId();
		maxId++;
		return "id" + maxId;
	}

	/**
	 * 
	 */
	private void setMaxId() {
		for (Player player : players) {
			String id = player.getId();
			if (id.startsWith("id")) {
				String snum = id.substring(2);
				try {
					int num = Integer.parseInt(snum);
					if (num > maxId)
						maxId = num;
				} catch (NumberFormatException e) {
					continue;
				}
			}
		}
	}

	/**
	 * @param player
	 */
	public Player findPlayer(Player player) {
		if (player.getId().equals(Player.DUMMY.getId()))
			return Player.DUMMY;
		for (Player p : players) {
			if (p.equals(player))
				return p;
		}
		return null;
	}
}
