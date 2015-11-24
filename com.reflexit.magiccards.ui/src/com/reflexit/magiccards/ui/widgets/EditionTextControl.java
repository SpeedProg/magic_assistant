package com.reflexit.magiccards.ui.widgets;

import java.util.Collection;

import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.eclipse.jface.fieldassist.SimpleContentProposalProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import com.reflexit.magiccards.core.model.Editions;

public class EditionTextControl extends Composite {
	private Text set;

	public EditionTextControl(Composite parent, int style) {
		super(parent, SWT.NONE);
		setLayout(new FillLayout());
		set = new Text(this, style);
		ContentProposalAdapter proposalAdapter = ContextAssist.addContextAssist(set, new String[0],
				false);
		updateProposals(proposalAdapter.getContentProposalProvider());
		set.addFocusListener(new FocusListener() {
			@Override
			public void focusLost(FocusEvent e) {
				// ignore
			}

			@Override
			public void focusGained(FocusEvent e) {
				updateProposals(proposalAdapter.getContentProposalProvider());
			}
		});
		set.addFocusListener(new SearchContextFocusListener());
	}

	public void setText(String text) {
		set.setText(text);
	}

	@Override
	public void setToolTipText(String string) {
		set.setToolTipText(string);
		super.setToolTipText(string);
	}

	public void updateProposals(IContentProposalProvider contentProposalProvider) {
		if (contentProposalProvider instanceof SimpleContentProposalProvider) {
			Collection<String> names = Editions.getInstance().getNames();
			String[] setProposals = new String[names.size()];
			int i = 0;
			for (String type : names) {
				setProposals[i++] = type;
			}
			((SimpleContentProposalProvider) contentProposalProvider).setProposals(setProposals);
		}
	}

	public void addModifyListener(ModifyListener modifyListener) {
		set.addModifyListener(modifyListener);
	}

	public void addSelectionListener(SelectionListener selectionListener) {
		set.addSelectionListener(selectionListener);
	}

	public String getText() {
		return set.getText();
	}
}
