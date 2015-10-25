package com.reflexit.magiccards.ui.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.FormColors;
import org.eclipse.ui.forms.widgets.FormToolkit;

public class MagicToolkit extends FormToolkit {
	private static MagicToolkit instance;

	private MagicToolkit() {
		super(new FormColors(Display.getCurrent()) {
			@Override
			protected void initialize() {
				background = display.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND);
				foreground = display.getSystemColor(SWT.COLOR_WIDGET_FOREGROUND);
				initializeColorTable();
				updateBorderColor();
			}
		});
	}

	public static MagicToolkit getInstance() {
		if (instance == null)
			instance = new MagicToolkit();
		return instance;
	}

	@Override
	public Label createLabel(Composite parent, String text) {
		Label label = super.createLabel(parent, text);
		label.setBackground(parent.getBackground());
		return label;
	}
	
	public Button createButton(Composite parent, String text, int style, ISingleSelectionListener lis) {
		Button button = new Button(parent, style);
		if (text != null)
			button.setText(text);
		if ((style & SWT.RADIO) == 0) {
			adapt(button, false, false);
		} else {
			// button.setBackground(parent.getBackground());
			button.setForeground(parent.getForeground());
		}
		if (lis != null)
			button.addSelectionListener(lis);
		return button;
	}

	public Composite createGroup(Composite parent, String string) {
		Group group = new Group(parent, SWT.BORDER);
		group.setText(string);
		adapt(group, false, false);
		return group;
	}
}
