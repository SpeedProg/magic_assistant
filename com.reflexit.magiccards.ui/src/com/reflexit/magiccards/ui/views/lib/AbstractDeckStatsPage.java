package com.reflexit.magiccards.ui.views.lib;

import java.text.DecimalFormat;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import com.reflexit.magiccards.core.model.CardGroup;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.ui.chart.ChartCanvas;
import com.reflexit.magiccards.ui.chart.IChartGenerator;

public abstract class AbstractDeckStatsPage extends AbstractDeckPage {
    protected ChartCanvas canvas;
    protected TreeViewer stats;

    public class GroupContentProvider implements ITreeContentProvider {
        public Object[] getChildren(Object element) {
            if (element instanceof CardGroup) {
                CardGroup node = (CardGroup) element;
                if (node.size() > 0)
                    return node.getChildren();
            }
            return new Object[] {};
        }

        public Object getParent(Object element) {
            return null;
        }

        public boolean hasChildren(Object element) {
            if (element instanceof CardGroup) {
                CardGroup node = (CardGroup) element;
                if (node.size() > 0)
                    return true;
            }
            return false;
        }

        public Object[] getElements(Object inputElement) {
            return getChildren(inputElement);
        }

        public void dispose() {
            // TODO Auto-generated method stub
        }

        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
            // TODO Auto-generated method stub
        }
    }

    @Override
    public Composite createContents(Composite parent) {
        Composite area = super.createContents(parent);
        area.setLayout(new FillLayout());
        SashForm sashForm = new SashForm(area, SWT.HORIZONTAL);
        canvas = new ChartCanvas(sashForm, SWT.BORDER);
        canvas.setLayoutData(new GridData(GridData.FILL_BOTH));
        stats = new TreeViewer(sashForm, SWT.FULL_SELECTION | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
        stats.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));
        sashForm.setWeights(new int[] { 60, 40 });
        TreeViewerColumn col1 = new TreeViewerColumn(stats, SWT.NONE);
        col1.getColumn().setText("Name");
        col1.getColumn().setWidth(200);
        TreeViewerColumn col2 = new TreeViewerColumn(stats, SWT.NONE);
        col2.getColumn().setText("Count");
        col2.getColumn().setWidth(60);
        TreeViewerColumn col3 = new TreeViewerColumn(stats, SWT.NONE);
        col3.getColumn().setText("Percent");
        col3.getColumn().setWidth(60);
        stats.setAutoExpandLevel(3);
        stats.getTree().setHeaderVisible(true);
        stats.setContentProvider(new GroupContentProvider());
        stats.addSelectionChangedListener(new ISelectionChangedListener() {
            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                IStructuredSelection sel = (IStructuredSelection) event.getSelection();
                if (!sel.isEmpty()) {
                    view.getSite().getSelectionProvider().setSelection(sel);
                }
            }
        });
        col1.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                if (element instanceof CardGroup) {
                    CardGroup node = (CardGroup) element;
                    return node.getName();
                } else if (element instanceof IMagicCard) {
                    IMagicCard node = (IMagicCard) element;
                    return node.getName();
                }
                return null;
            }
        });
        col2.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                int count = getCount(element);
                return count + "";
            }
        });
        col3.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                int count = getCount(element);
                final int total = ((CardGroup) stats.getInput()).getCount();
                float per = count / (float) total;
                return new DecimalFormat("00.00%").format(per);
            }
        });
        return area;
    }

    @Override
    public void activate() {
        super.activate();
        IChartGenerator gen = createChartGenerator();
        canvas.setChartGenerator(gen);
        canvas.redraw();
        CardGroup root = buildTree();
        stats.setInput(root);
    }

    abstract protected CardGroup buildTree();

    abstract protected IChartGenerator createChartGenerator();
}
