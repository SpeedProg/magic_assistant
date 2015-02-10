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
package com.reflexit.mtgtournament.ui.tour.views;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.Section;

import com.reflexit.mtgtournament.core.model.Player;

/**
 * @author Alena
 *
 */
public class PlayerDetailsSection extends TSectionPart {
	private Player player;
	private ModifyListener modLis;
	private Map<String, Text> fields = new HashMap<String, Text>();

	/**
	 * @param managedForm
	 */
	public PlayerDetailsSection(IManagedForm managedForm) {
		super(managedForm, Section.EXPANDED);
		createBody();
	}

	/**
	 *
	 */
	private void createBody() {
		Section section = this.getSection();
		section.setText("Player Details");
		Composite sectionClient = toolkit.createComposite(section);
		section.setClient(sectionClient);
		GridLayout layout = new GridLayout(2, false);
		sectionClient.setLayout(layout);
		modLis = new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				Text text = (Text) e.widget;
				textModified((String) text.getData("id"), text.getText());
				reload();
			}
		};
		// fields
		createInputField(sectionClient, "Name: ", "name");
		createInputField(sectionClient, "PIN: ", "pin");
		createInputField(sectionClient, "Points: ", "points");
		createInputField(sectionClient, "Games: ", "games");
	}

	/**
	 * @param data
	 * @param data2
	 */
	protected void textModified(String id, String text) {
		if (id.equals("name")) {
			player.setName(text);
		} else if (id.equals("pin")) {
			player.setId(text);
		} else if (id.equals("games")) {
			if (text.length() == 0)
				player.setGames(0);
			else
				player.setGames(Integer.parseInt(text));
		} else if (id.equals("points")) {
			if (text.length() == 0)
				player.setPoints(0);
			else
				player.setPoints(Integer.parseInt(text));
		}
	}

	private Text createInputField(Composite sectionClient, String label, String id) {
		Label labelName = toolkit.createLabel(sectionClient, label);
		Text text = toolkit.createText(sectionClient, "");
		text.setData("id", id);
		text.addModifyListener(modLis);
		text.addFocusListener(new FocusListener() {
			@Override
			public void focusLost(FocusEvent e) {
				save();
			}

			@Override
			public void focusGained(FocusEvent e) {
				// nothing
			}
		});
		GridData layoutData = new GridData(GridData.FILL_HORIZONTAL);
		layoutData.widthHint = 200;
		text.setLayoutData(layoutData);
		fields.put(id, text);
		return text;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.reflexit.mtgtournament.ui.tour.views.TSectionPart#setFormInput(java.lang.Object)
	 */
	@Override
	public boolean setFormInput(Object input) {
		if (input instanceof Player) {
			this.player = (Player) input;
			updateFields();
		} else if (input == null) {
			this.player = null;
			for (String key : fields.keySet()) {
				fields.get(key).setEnabled(false);
			}
		}
		return super.setFormInput(input);
	}

	/**
	 *
	 */
	private void updateFields() {
		for (String key : fields.keySet()) {
			fields.get(key).setEnabled(true);
		}
		fields.get("name").setText(player.getName());
		fields.get("pin").setText(player.getId());
		fields.get("points").setText(player.getPoints() + "");
		fields.get("games").setText(player.getGames() + "");
		//		DecorationSupport.bindText(fields.get("points"), player, "points",
		//				(i) -> {
		//					if (((Integer) i) < 0) return "Not negative please";
		//					return null;
		//				});
	}
}
