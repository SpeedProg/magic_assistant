package com.reflexit.magiccards.ui.widgets;

import org.eclipse.jface.fieldassist.ComboContentAdapter;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.eclipse.jface.fieldassist.IControlContentAdapter;
import org.eclipse.jface.fieldassist.SimpleContentProposalProvider;
import org.eclipse.jface.fieldassist.TextContentAdapter;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

public class ContextAssist {
	static class MultiWordContentProposalProvider extends SimpleContentProposalProvider {
		MultiWordContentProposalProvider(String[] proposals) {
			super(proposals);
			setFiltering(true);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.fieldassist.SimpleContentProposalProvider#
		 * getProposals(java.lang.String, int)
		 */
		@Override
		public IContentProposal[] getProposals(String contents, int position) {
			int k = contents.lastIndexOf(' ');
			if (k >= 0) {
				return super.getProposals(contents.substring(k + 1), position);
			}
			k = contents.lastIndexOf(',');
			if (k >= 0) {
				return super.getProposals(contents.substring(k + 1), position);
			}
			return super.getProposals(contents, position);
		}
	}

	public static ContentProposalAdapter addContextAssist(Control t, String[] proposals, final boolean multi) {
		IControlContentAdapter controlContentAdapter = null;
		if (t instanceof Text) {
			controlContentAdapter = new TextContentAdapter() {
				@Override
				public void insertControlContents(Control control, String text, int cursorPosition) {
					Text textCon = (Text) control;
					if (multi == false) {
						textCon.setText(text);
						textCon.setSelection(cursorPosition, cursorPosition);
						return;
					}
					Point selection = textCon.getSelection();
					String old = textCon.getText();
					int k = old.lastIndexOf(' ');
					int kz = old.lastIndexOf(',');
					if (kz > k)
						k = kz;
					int l = old.length() - k - 1;
					textCon.insert(text.substring(l));
					// Insert will leave the cursor at the end of the inserted text.
					// If this
					// is not what we wanted, reset the selection.
					if (cursorPosition < text.length()) {
						textCon.setSelection(selection.x + cursorPosition, selection.x + cursorPosition);
					}
				}
			};
		} else if (t instanceof Combo) {
			controlContentAdapter = new ComboContentAdapter() {
				@Override
				public void insertControlContents(Control control, String text, int cursorPosition) {
					Combo combo = (Combo) control;
					Point selection = combo.getSelection();
					combo.setText(text);
					selection.x = cursorPosition;
					selection.y = cursorPosition;
					combo.setSelection(selection);
				}
			};
		}
		IContentProposalProvider proposalProvider = multi ? new MultiWordContentProposalProvider(proposals)
				: new SimpleContentProposalProvider(proposals) {
					{
						setFiltering(true);
					}
				};
		return new ContentProposalAdapter(t, controlContentAdapter, proposalProvider, null, null);
	}
}
