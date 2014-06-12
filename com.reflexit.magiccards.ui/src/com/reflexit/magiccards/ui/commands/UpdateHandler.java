/*******************************************************************************
 * Copyright (c) 2007, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.reflexit.magiccards.ui.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;

import com.reflexit.magicassistant.p2.UpdateHandlerP2;

/**
 * UpdateHandler invokes the check for updates UI
 * 
 * @since 3.4
 */
public class UpdateHandler extends AbstractHandler {
	@Override
	public Object execute(final ExecutionEvent event) {
		new BackupHandler().execute(event);
		return new UpdateHandlerP2().execute(event);
	}
}
