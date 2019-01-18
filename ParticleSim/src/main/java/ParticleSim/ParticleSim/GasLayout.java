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
		int cw = target.getSize().width;
		target.getComponent(0).setLocation(0, 0);
		target.getComponent(0).setSize(cw, target.getSize().height-100);
		target.getComponent(1).setLocation(0, target.getSize().height-100);
		target.getComponent(1).setSize(cw, 100);
		int i;
		int h = 0;
		for (i = 2; i < target.getComponentCount(); i++) {
		    Component m = target.getComponent(i);
		    if (m.isVisible()) {
				Dimension d = m.getPreferredSize();
				if (m instanceof Scrollbar)	d.width = target.getSize().width - cw;
				int c = 0;
				if (m instanceof Label) {
				    h += d.height/3;
				    c = (target.getSize().width-cw-d.width)/2;
				}
				m.setLocation(cw+c, h);
				m.setSize(d.width, d.height);
				h += d.height;
		    }
		}
    }
    
};
