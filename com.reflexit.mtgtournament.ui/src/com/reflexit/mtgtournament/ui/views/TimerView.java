package com.reflexit.mtgtournament.ui.views;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.part.ViewPart;

import com.reflexit.mtgtournament.ui.tour.Activator;

/**
 * This sample class demonstrates how to plug-in a new workbench view. The view
 * shows data obtained from the model. The sample creates a dummy model on the
 * fly, but a real implementation would connect to the model available either in
 * this or another plug-in (e.g. the workspace). The view is connected to the
 * model using a content provider.
 * <p>
 * The view uses a label provider to define how model objects should be presented in the view. Each view can
 * present the same model objects using different labels and icons, if needed. Alternatively, a single label
 * provider can be shared between views in order to ensure that objects of the same type are presented in the
 * same way everywhere.
 * <p>
 */
public class TimerView extends ViewPart {
	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "com.reflexit.mtgtournament.ui.views.TimerView";
	private Action pause;
	private Action play;
	private Action doubleClickAction;
	private Composite area;
	private Label clockLabel;
	private Label timerLabel;
	private SimpleDateFormat timerFormat = new SimpleDateFormat("HH:mm:ss");
	private SimpleDateFormat clockFormat = new SimpleDateFormat("hh:mm a");
	private long timeLeft;
	private long timeZero;
	private long lastTime;
	private boolean paused = true;
	private String lastTimerValue = "00:30:00";
	private Thread thread = new Thread("Tournament Timer") {
		@Override
		public void run() {
			while (!area.isDisposed()) {
				area.getDisplay().syncExec(new Runnable() {
					public void run() {
						updateTime();
					}
				});
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					break;
				}
			}
		}
	};
	private Action setTimer;

	/**
	 * The constructor.
	 */
	public TimerView() {
		try {
			timeZero = timerFormat.parse("00:00:00").getTime();
			timeLeft = timerFormat.parse(lastTimerValue).getTime();
			lastTime = System.currentTimeMillis();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void dispose() {
		thread.interrupt();
		super.dispose();
	}

	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	@Override
	public void createPartControl(Composite parent) {
		area = new Composite(parent, SWT.NONE);
		area.setLayoutData(new GridData(GridData.FILL_BOTH));
		area.setLayout(new GridLayout());
		clockLabel = new Label(area, SWT.NONE);
		clockLabel.setToolTipText("Local time clock can be adjusted using computer clock");
		clockLabel.setFont(new Font(area.getDisplay(), area.getFont().getFontData()));
		clockLabel.setForeground(area.getDisplay().getSystemColor(SWT.COLOR_GRAY));
		GridData ld = new GridData(GridData.FILL_HORIZONTAL);
		ld.horizontalAlignment = SWT.CENTER;
		clockLabel.setLayoutData(ld);
		timerLabel = new Label(area, SWT.NONE);
		timerLabel.setToolTipText("Double click on it to set timer");
		GridData ld1 = new GridData(GridData.FILL_HORIZONTAL);
		ld1.horizontalAlignment = SWT.CENTER;
		timerLabel.setLayoutData(ld1);
		area.addControlListener(new ControlListener() {
			public void controlResized(ControlEvent e) {
				Point size = area.getSize();
				FontData fontData = clockLabel.getFont().getFontData()[0];
				int height = fontData.getHeight();
				int w = size.x / clockLabel.getText().length();
				int newHeight = Math.max(10, Math.min(size.y - 20, w));
				if (height != newHeight) {
					clockLabel.getFont().dispose();
					fontData.setHeight(newHeight);
					Font font = new Font(e.display, fontData);
					clockLabel.setFont(font);
					timerLabel.setFont(font);
					area.layout(true);
				}
			}

			public void controlMoved(ControlEvent e) {
				// TODO Auto-generated method stub
			}
		});
		makeActions();
		hookContextMenu();
		hookDoubleClickAction();
		contributeToActionBars();
		timerLabel.setText(timerFormat.format(new Date(timeLeft)));
		updateTime();
		updateActionsEnablement();
		thread.start();
	}

	private void updateTime() {
		Date time = Calendar.getInstance().getTime();
		String label = clockFormat.format(time);
		if (clockLabel.isDisposed())
			return;
		clockLabel.setText(label);
		if (paused == false) {
			long curTime = System.currentTimeMillis();
			long diff = curTime - lastTime;
			timeLeft -= diff;
			if (timeLeft < timeZero)
				timeLeft = timeZero;
			timerLabel.setText(timerFormat.format(new Date(timeLeft)));
			lastTime = curTime;
		}
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				TimerView.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(timerLabel);
		timerLabel.setMenu(menu);
		// getSite().registerContextMenu(menuMgr, viewer);
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager) {
		// manager.add(pause);
		// manager.add(play);
		// manager.add(setTimer);
		// manager.add(new Separator());
	}

	private void fillContextMenu(IMenuManager manager) {
		manager.add(pause);
		manager.add(play);
		manager.add(setTimer);
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(pause);
		manager.add(play);
		manager.add(setTimer);
	}

	protected void updateActionsEnablement() {
		pause.setEnabled(!paused && isTimerSet());
		play.setEnabled(paused && isTimerSet());
	}

	private boolean isTimerSet() {
		return timeLeft > timeZero;
	}

	private void makeActions() {
		pause = new Action() {
			@Override
			public void run() {
				lastTime = System.currentTimeMillis();
				paused = true;
				updateActionsEnablement();
			}
		};
		pause.setText("Pause");
		pause.setToolTipText("Pause Timer");
		pause.setImageDescriptor(Activator.getImageDescriptor("icons/suspend_co.png"));
		play = new Action() {
			@Override
			public void run() {
				lastTime = System.currentTimeMillis();
				paused = false;
				updateActionsEnablement();
			}
		};
		play.setText("Resume");
		play.setToolTipText("Resume timer");
		play.setImageDescriptor(Activator.getImageDescriptor("icons/resume_co.png"));
		setTimer = new Action() {
			@Override
			public void run() {
				setTimer();
			}
		};
		setTimer.setText("Set");
		setTimer.setToolTipText("Set Timer");
		setTimer.setImageDescriptor(Activator.getImageDescriptor("icons/launch_profile.gif"));
		doubleClickAction = new Action() {
			@Override
			public void run() {
				setTimer();
			}
		};
	}

	protected void setTimer() {
		InputDialog d = new InputDialog(
				area.getShell(),
				"Set Timer",
				"Enter timer value hh:mm:ss. After timer is set press \"Play\" button to start the countdown.",
				lastTimerValue, null);
		if (d.open() == Dialog.OK) {
			pause.run();
			String t = d.getValue();
			try {
				long tt = timerFormat.parse(t).getTime();
				lastTimerValue = t;
				timeLeft = tt;
				timerLabel.setText(timerFormat.format(new Date(timeLeft)));
			} catch (ParseException e) {
				MessageDialog.openError(area.getShell(), "Timer Error",
						"Cannot parse timer value, format is hh:mm:ss");
			}
		}
	}

	private void hookDoubleClickAction() {
		timerLabel.addMouseListener(new MouseListener() {
			public void mouseUp(MouseEvent e) {
				// nothing
			}

			public void mouseDown(MouseEvent e) {
				// nothing
			}

			public void mouseDoubleClick(MouseEvent e) {
				doubleClickAction.run();
			}
		});
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	@Override
	public void setFocus() {
		clockLabel.setFocus();
	}
}