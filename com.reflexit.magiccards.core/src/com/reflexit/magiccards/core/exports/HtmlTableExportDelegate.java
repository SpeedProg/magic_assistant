/*******************************************************************************
 * Copyright (c) 2008 Alena Laskavaia.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Wingård  - initial API and implementation
 *******************************************************************************/
package com.reflexit.magiccards.core.exports;

import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

import com.reflexit.magiccards.core.Activator;
import com.reflexit.magiccards.core.model.ICardField;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.Location;
import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.model.MagicCardPhisical;
import com.reflexit.magiccards.core.model.storage.ICardStore;
import com.reflexit.magiccards.core.model.storage.ILocatable;
import com.reflexit.magiccards.core.model.storage.IStorage;
import com.reflexit.magiccards.core.model.storage.IStorageContainer;
import com.reflexit.magiccards.core.model.storage.IStorageInfo;

/**
 * Export to Wagic: The Homebrew (http://wololo.net/wagic/) TODO: add description
 */
public class HtmlTableExportDelegate extends AbstractExportDelegate<IMagicCard> {
	public HtmlTableExportDelegate() {
	}

	public String getName() {
		if (store != null) {
			return ((ILocatable) store).getLocation().getName();
		}
		ICardStore store1 = store.getCardStore();
		IStorage storage = ((IStorageContainer) store1).getStorage();
		if (storage instanceof IStorageInfo) {
			IStorageInfo si = ((IStorageInfo) storage);
			return si.getName();
		}
		return "deck";
	}

	public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		if (monitor == null)
			monitor = new NullProgressMonitor();
		monitor.beginTask("Exporting to wagic...", store.getSize());
		PrintStream stream = new PrintStream(st);
		String ename = getName();
		Location location = null;
		stream.println("<html><head><title>" + ename + "</title></head><body>");
		stream.println("<table>");
		String line = "<tr>";
		for (int i = 0; i < columns.length; i++) {
			ICardField field = columns[i];
			line += "<th>" + field.name() + "</th>";
		}
		stream.println(line + "</tr>");
		for (IMagicCard mc : store) {
			line = "<tr>";
			if (mc instanceof MagicCardPhisical) {
				MagicCardPhisical card = ((MagicCardPhisical) mc);
				if (location != card.getLocation()) {
					location = card.getLocation();
					stream.println("<h2>" + location.getName() + "</h2>");
				}
				for (int i = 0; i < columns.length; i++) {
					ICardField field = columns[i];
					Object value = card.getObjectByField(field);
					if (value == null)
						value = "";
					line += ("<td>" + value + "</td>");
				}
			} else if (mc instanceof MagicCard) {
				Activator.log("Skipping " + mc);
			}
			stream.println(line + "</tr>");
			monitor.worked(1);
		}
		stream.println("</table>");
		stream.println("</body></html>");
		stream.close();
		monitor.done();
	}

	public ReportType getType() {
		return ReportType.createReportType("html", "HTML Table");
	}
}
