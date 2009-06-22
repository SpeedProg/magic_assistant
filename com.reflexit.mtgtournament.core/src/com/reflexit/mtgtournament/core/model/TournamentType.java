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

public enum TournamentType {
	ROUND_ROBIN,
	SWISS,
	ELIMINATION,
	RANDOM,
	COMPOSITE;
	/**
	 * @return
	 */
	public static String[] stringValues() {
		TournamentType[] values = values();
		String res[] = new String[values.length];
		for (int i = 0; i < res.length; i++) {
			res[i] = values[i].name();
		}
		return res;
	}

	/**
	 * @param intValue
	 * @return
	 */
	public static TournamentType valueOf(int intValue) {
		TournamentType[] values = values();
		for (TournamentType tt : values) {
			if (tt.ordinal() == intValue)
				return tt;
		}
		return null;
	}
}