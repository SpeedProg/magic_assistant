package com.reflexit.magiccards.ui.dialogs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.reflexit.magiccards.core.CannotDetermineSetAbbriviation;
import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.Location;
import com.reflexit.magiccards.core.model.MagicCardPhysical;
import com.reflexit.magiccards.core.model.abs.ICardCountable;
import com.reflexit.magiccards.ui.utils.ImageCreator;
import com.reflexit.magiccards.ui.widgets.SlidingPaneAnimation;

public class CountConfirmationDialog extends Dialog {
	private SlidingPaneAnimation animation = new SlidingPaneAnimation();
	private Map<IMagicCard, Integer> countMap = new HashMap<IMagicCard, Integer>();
	private IMagicCard cards[];
	private Composite panels[];
	private Composite confirmer;
	private int answer = 0;
	private int activeIndex = 0;
	private Text answerInput;
	private Label confirmerLabel;
	private Text countLabel;

	protected CountConfirmationDialog(Shell parent, IStructuredSelection selection) {
		super(parent);
		if (selection.isEmpty())
			throw new IllegalArgumentException();
		Collection res = DataManager.expandGroups(selection.toList());
		cards = new IMagicCard[res.size()];
		int i = 0;
		for (Object object : res) {
			cards[i++] = (IMagicCard) object;
		}
		activeIndex = 0;
	}

	@Override
	protected Control createContents(Composite parent) {
		Control c = super.createContents(parent);
		enableAdvanceButtons(true);
		return c;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		area.setLayout(new GridLayout(2, false));
		Composite cardArea = new Composite(area, SWT.NONE);
		cardArea.setLayoutData(new GridData(GridData.FILL_BOTH));
		int i = 0;
		panels = new Composite[cards.length];
		for (IMagicCard card : cards) {
			Composite comp = createCardComposite(cardArea, card, i > 2);
			panels[i++] = comp;
		}
		confirmer = createConfirmer(cardArea);
		animation.setAnimationLayoutOn(cardArea);
		Composite sideButtons = new Composite(area, SWT.NONE);
		createSideButtons(sideButtons);
		return area;
	}

	private Composite createCardComposite(Composite cardArea, final IMagicCard card, boolean lazy) {
		final Composite comp = new Composite(cardArea, SWT.BORDER);
		comp.setLayout(new GridLayout());
		comp.setData(card);
		Label label = new Label(comp, SWT.WRAP);
		label.setText(card.getName() + " (" + card.getSet() + ") " + " x "
				+ ((ICardCountable) card).getCount());
		if (lazy)
			new Thread("Loading image for " + card) {
				@Override
				public void run() {
					Display.getDefault().asyncExec(new Runnable() {
						@Override
						public void run() {
							try {
								sleep(1000);
							} catch (InterruptedException e) {
								// ignore
							}
							createCardImage(comp, card);
						}
					});
				};
			}.start();
		else
			createCardImage(comp, card);
		return comp;
	}

	private void createCardImage(final Composite comp, final IMagicCard card) {
		try {
			final Label img = new Label(comp, SWT.NONE);
			String path = ImageCreator.getInstance().createCardPath(card, true, false);
			Image image = ImageCreator.getInstance().createCardImage(path, false);
			img.setImage(image);
			comp.layout(true);
		} catch (CannotDetermineSetAbbriviation e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	protected void createSideButtons(Composite parent) {
		GridLayout layout = new GridLayout();
		parent.setLayout(layout);
		countLabel = new Text(parent, SWT.BORDER | SWT.CENTER);
		countLabel.setEditable(false);
		countLabel.setText("0");
		countLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		createButton(parent, 10, "0 >", true);
		createButton(parent, 11, "1 >", true);
		createButton(parent, 12, "2 >", true);
		createButton(parent, 13, "3 >", true);
		createButton(parent, 14, "4 >", true);
		answerInput = new Text(parent, SWT.BORDER | SWT.CENTER);
		answerInput.setText(answer + "");
		answerInput.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		answerInput.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				answer = Integer.valueOf(answerInput.getText());
				enableAdvanceButtons(true);
			}
		});
		createButton(parent, 3, "X >", true);
		layout.numColumns = 1;
	}

	private Composite createConfirmer(Composite area) {
		Composite comp = new Composite(area, SWT.BORDER);
		comp.setLayout(new GridLayout());
		comp.setBackground(area.getDisplay().getSystemColor(SWT.COLOR_INFO_BACKGROUND));
		comp.setBackgroundMode(SWT.INHERIT_DEFAULT);
		confirmerLabel = new Label(comp, SWT.WRAP);
		confirmerLabel.setText("Done");
		confirmerLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		return comp;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		// create OK and Cancel buttons by default
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
		createButton(parent, 2, "Back", true);
		createButton(parent, IDialogConstants.OK_ID, "Next", true);
	}

	@Override
	protected void buttonPressed(int buttonId) {
		if (buttonId == 2) {
			backPressed();
		} else if (buttonId == 3) {
			answer = Integer.valueOf(answerInput.getText());
			okPressed();
		} else if (buttonId >= 10) {
			answer = buttonId - 10;
			okPressed();
		} else
			super.buttonPressed(buttonId);
	}

	protected void backPressed() {
		if (activeIndex == 0)
			return;
		getButton(0).setText("Next");
		if (activeIndex < cards.length)
			animation.popControl(panels[activeIndex], 1, 0);
		activeIndex--;
		enableAdvanceButtons(true);
	}

	@Override
	protected void okPressed() {
		if (activeIndex >= cards.length) {
			super.okPressed();
			runOperation();
			return;
		}
		enableAdvanceButtons(false);
		try {
			countMap.put(cards[activeIndex], answer);
			confirmerLabel.setText("Selected " + answer + " cards for " + cards[activeIndex].getName());
			answerInput.setText(answer + "");
			animation.pushControl(confirmer, 0f, 1f, 0f, 0.5f);
			animation.waitForAnimation();
			activeIndex++;
			if (activeIndex >= cards.length) {
				confirmerLabel.setText("Press Done to perform operation");
				getButton(0).setText("Done");
				return;
			}
			Composite current = panels[activeIndex];
			animation.pushControl(current, 0, 1);
			animation.waitForAnimation();
			confirmer.moveBelow(null);
		} finally {
			enableAdvanceButtons(true);
		}
	}

	protected void runOperation() {
		// now run something that actually does stuff
	}

	private String getCount(IMagicCard card) {
		if (card instanceof MagicCardPhysical)
			return ((MagicCardPhysical) card).getCount() + "";
		return "*";
	}

	private void enableAdvanceButtons(boolean a) {
		if (activeIndex >= cards.length) {
			getButton(3).setEnabled(false);
			for (int i = 10; i <= 14; i++) {
				getButton(i).setEnabled(false);
			}
			return;
		}
		boolean b = a;
		IMagicCard card = cards[activeIndex];
		int count = Integer.MAX_VALUE;
		if (card instanceof MagicCardPhysical) {
			count = ((MagicCardPhysical) card).getCount();
		}
		countLabel.setText(getCount(card));
		getButton(0).setEnabled(b && answer <= count);
		getButton(3).setEnabled(b && answer <= count);
		for (int i = 10; i <= 14; i++) {
			getButton(i).setEnabled(b && (i - 10) <= count);
		}
	}

	public Map<IMagicCard, Integer> getCountMap() {
		return countMap;
	}

	public static void main(String[] args) {
		Display display = new Display();
		// final Shell shell = new Shell(display, SWT.CLOSE | SWT.RESIZE |
		// SWT.DOUBLE_BUFFERED);
		// shell.setSize(600, 600);
		DataManager.getInstance().waitForInit(5000);
		Collection<IMagicCard> candidates = DataManager.getCardHandler().getMagicDBStore()
				.getCandidates("Forest");
		ArrayList<IMagicCard> list = new ArrayList<IMagicCard>();
		int i = 0;
		for (IMagicCard base : candidates) {
			if (i == 10)
				break;
			MagicCardPhysical mcp = new MagicCardPhysical(base, Location.NO_WHERE);
			mcp.setCount(i + 1);
			list.add(mcp);
			i++;
		}
		new CountConfirmationDialog(display.getActiveShell(), new StructuredSelection(list)).open();
	}
}
