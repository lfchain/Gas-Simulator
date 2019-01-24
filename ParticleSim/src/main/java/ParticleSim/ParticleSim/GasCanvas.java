package ParticleSim.ParticleSim;

import java.awt.*;

public class GasCanvas extends Canvas {
	
    Gas pg;
    
    GasCanvas(Gas p) {
    	pg = p;
    }
    
    public Dimension getPreferredSize() {
    	return new Dimension(300,400);
    }
    
    public void update(Graphics g) {
    	pg.updateGas(g);
    }
    
    public void paint(Graphics g) {
    	pg.updateGas(g);
    }

}
