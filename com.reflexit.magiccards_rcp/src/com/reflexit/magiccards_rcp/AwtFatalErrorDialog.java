package com.reflexit.magiccards_rcp;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Label;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

@SuppressWarnings("serial")
public class AwtFatalErrorDialog {
	static class AwtFatalErrorDialog1 extends Frame implements ActionListener {
		public AwtFatalErrorDialog1(String message) {
			super("Fatal Eror");
			setSize(600, 250);
			addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosing(WindowEvent e) {
					dispose();
				}
			});
			Panel toolbar = new Panel();
			toolbar.setLayout(new FlowLayout(FlowLayout.LEFT));
			Button sucksButton = new Button("That sucks");
			sucksButton.addActionListener(this);
			toolbar.add(sucksButton);
			Label label = new Label(message);
			add(label, BorderLayout.NORTH);
			add(toolbar, BorderLayout.SOUTH);
		}

		@Override
		public void actionPerformed(ActionEvent ae) {
			setVisible(false);
			dispose();
		}
	}

	public static Thread openError(final String message) {
		System.err.println("FATAL ERROR: " + message);
		Thread thread = new Thread() {
			@Override
			public void run() {
				try {
					new AwtFatalErrorDialog1(message).setVisible(true);
					Thread.sleep(15000);
				} catch (Throwable e2) {
					e2.printStackTrace();
				}
			};
		};
		thread.start();
		try {
			thread.join(15000);
		} catch (InterruptedException e) {
			// ok
		}
		return thread;
	}

	public static void main(String args[]) throws InterruptedException {
		openError("Your default java is not 64 bit. Please check installation guide for solution.").join(5000);
	}
}