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
package com.reflexit.mtgtournament.core.schedule;

import java.util.ArrayList;

import com.reflexit.mtgtournament.core.model.PlayerTourInfo;
import com.reflexit.mtgtournament.core.model.Round;

/**
 * @author Alena
 * 
 */
public class CompositeScheduler extends AbstractScheduler {
	@Override
	protected void scheduleRound(Round r, ArrayList<PlayerTourInfo> players) {
		throw new IllegalStateException("Composite is not a real scheduler, select a specific scheduler buy editing round info");
	}
}
