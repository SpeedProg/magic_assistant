package com.reflexit.magiccards.ui.views.analyzers;

import java.util.HashMap;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.widgets.Composite;

import com.reflexit.magiccards.core.model.CardGroup;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.model.utils.CardStoreUtils;
import com.reflexit.magiccards.ui.chart.CreatureChart;
import com.reflexit.magiccards.ui.chart.IChartGenerator;
import com.reflexit.magiccards.ui.views.lib.AbstractDeckStatsPage;

public class CreaturePage extends AbstractDeckStatsPage {
    @Override
    public Composite createContents(Composite parent) {
        Composite area = super.createContents(parent);
        TreeViewerColumn col4 = new TreeViewerColumn(stats, SWT.NONE);
        col4.getColumn().setText("Power (Avg)");
        col4.getColumn().setWidth(80);
        TreeViewerColumn col5 = new TreeViewerColumn(stats, SWT.NONE);
        col5.getColumn().setText("Toughness (Avg)");
        col5.getColumn().setWidth(150);
        col4.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                if (element instanceof CardGroup) {
                    CardGroup node = (CardGroup) element;
                    int cc = node.getCreatureCount();
                    if (cc > 0) {
                        String pow = node.getBase().getPower();
                        Float fpow = MagicCard.convertFloat(pow);
                        return pow + " (" + String.format("%.2f", 0.009 + fpow / cc) + ")";
                    }
                } else if (element instanceof IMagicCard) {
                    return ((IMagicCard) element).getPower();
                }
                return null;
            }
        });
        col5.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                if (element instanceof CardGroup) {
                    CardGroup node = (CardGroup) element;
                    int cc = node.getCreatureCount();
                    if (cc > 0) {
                        String tou = node.getBase().getToughness();
                        Float ftou = MagicCard.convertFloat(tou);
                        return String.valueOf(tou) + " (" + String.format("%.2f", 0.009 + ftou / cc) + ")";
                    }
                } else if (element instanceof IMagicCard) {
                    return ((IMagicCard) element).getToughness();
                }
                return null;
            }
        });
        SashForm sashForm = (SashForm) canvas.getParent();
        sashForm.setWeights(new int[] { 50, 50 });
        return area;
    }

    @Override
    protected IChartGenerator createChartGenerator() {
        HashMap<String, Integer> creatureStatsCount = CardStoreUtils.buildCreatureStats(store);
        IChartGenerator gen = new CreatureChart(creatureStatsCount.values().toArray(new Integer[0]), creatureStatsCount.keySet().toArray(
                new String[0]));
        return gen;
    }

    @Override
    protected CardGroup buildTree() {
        return CardStoreUtils.buildCreatureGroups(store);
    }
}
