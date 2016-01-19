package com.reflexit.magiccards.ui.gallery;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;

public interface IImageOverlayRenderer {
	void drawAllOverlays(GC gc, Object element, int x, int y, Point imageSize, int xShift, int yShift);
}
