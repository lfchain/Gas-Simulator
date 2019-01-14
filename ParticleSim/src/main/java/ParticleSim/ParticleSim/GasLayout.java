package ParticleSim.ParticleSim;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Label;
import java.awt.LayoutManager;
import java.awt.Scrollbar;

public class GasLayout implements LayoutManager {
    public GasLayout() {}
    public void addLayoutComponent(String name, Component c) {}
    public void removeLayoutComponent(Component c) {}
    public Dimension preferredLayoutSize(Container target) {
	return new Dimension(500, 500);
    }
    public Dimension minimumLayoutSize(Container target) {
	return new Dimension(100,100);
    }
    public void layoutContainer(Container target) {
	int cw = target.size().width ;
	target.getComponent(0).move(0, 0);
	target.getComponent(0).resize(cw, target.size().height-100);
	target.getComponent(1).move(0, target.size().height-100);
	target.getComponent(1).resize(cw, 100);
	int i;
	int h = 0;
	for (i = 2; i < target.getComponentCount(); i++) {
	    Component m = target.getComponent(i);
	    if (m.isVisible()) {
		Dimension d = m.getPreferredSize();
		if (m instanceof Scrollbar)
		    d.width = target.size().width - cw;
		int c = 0;
		if (m instanceof Label) {
		    h += d.height/3;
		    c = (target.size().width-cw-d.width)/2;
		}
		m.move(cw+c, h);
		m.resize(d.width, d.height);
		h += d.height;
	    }
	}
    }
};
