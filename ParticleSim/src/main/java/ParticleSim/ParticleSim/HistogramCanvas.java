package ParticleSim.ParticleSim;

import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Graphics;

class HistogramCanvas extends Canvas {
    Gas pg;
    HistogramCanvas(Gas p) {
	pg = p;
    }
    public Dimension getPreferredSize() {
	return new Dimension(125,50);
    }
    public void update(Graphics g) {
	pg.updateHistogram(g);
    }
};