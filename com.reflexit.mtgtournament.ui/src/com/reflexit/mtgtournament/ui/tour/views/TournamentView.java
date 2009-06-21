package com.reflexit.mtgtournament.ui.tour.views;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.ManagedForm;
import org.eclipse.ui.forms.widgets.ColumnLayout;
import org.eclipse.ui.forms.widgets.ColumnLayoutData;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.part.ViewPart;

import com.reflexit.mtgtournament.core.model.Tournament;

public class TournamentView extends ViewPart {
	public static final String ID = TournamentView.class.getName();
	private ScrolledForm form;
	private ISelectionListener selListener;
	private OverviewSection overviewSectionPart;
	private RegisteredPlayersSection regPlayersSectionPart;
	private ManagedForm managedForm;
	private RoundSection roundSectionPart;

	/**
	 * The constructor.
	 */
	public TournamentView() {
	}

	@Override
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		super.init(site, memento);
		selListener = new ISelectionListener() {
			public void selectionChanged(IWorkbenchPart part, ISelection selection) {
				setInput(selection);
			}
		};
		getSite().getWorkbenchWindow().getSelectionService().addSelectionListener(TNavigatorView.ID, selListener);
	}

	/**
	 * Disposes the toolkit
	 */
	@Override
	public void dispose() {
		getSite().getWorkbenchWindow().getSelectionService().removeSelectionListener(TNavigatorView.ID, selListener);
		super.dispose();
	}

	protected void setInput(ISelection selection) {
		if (selection.isEmpty())
			return;
		if (!(selection instanceof IStructuredSelection))
			return;
		Object x = ((IStructuredSelection) selection).getFirstElement();
		if (x instanceof Tournament) {
			Tournament t = (Tournament) x;
			form.setText("Tournament: " + t.getName());
			managedForm.setInput(t);
			managedForm.reflow(true);
		}
	}

	/**
	 * This is a callback that will allow us to create the viewer and
	 * initialize it.
	 */
	@Override
	public void createPartControl(Composite parent) {
		managedForm = new ManagedForm(parent);
		form = managedForm.getForm();
		form.setText("Tournament: xxx");
		ColumnLayout layout = new ColumnLayout();
		form.getBody().setLayout(layout);
		overviewSectionPart = new OverviewSection(managedForm);
		overviewSectionPart.getSection().setLayoutData(new ColumnLayoutData());
		managedForm.addPart(overviewSectionPart);
		regPlayersSectionPart = new RegisteredPlayersSection(managedForm);
		regPlayersSectionPart.getSection().setLayoutData(new ColumnLayoutData());
		managedForm.addPart(regPlayersSectionPart);
		roundSectionPart = new RoundSection(managedForm);
		//roundSectionPart.getSection().setLayoutData(new ColumnLayoutData());
		managedForm.addPart(roundSectionPart);
	}

	/**
	 * Passing the focus request to the form.
	 */
	@Override
	public void setFocus() {
		form.setFocus();
	}
}