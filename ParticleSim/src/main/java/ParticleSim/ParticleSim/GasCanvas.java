package ParticleSim.ParticleSim;

import java.io.InputStream;
import java.awt.*;
import java.awt.image.ImageProducer;
import java.applet.Applet;
import java.applet.AudioClip;
import java.util.Vector;
import java.util.Hashtable;
import java.util.Enumeration;
import java.io.File;
import java.net.URL;
import java.util.Random;
import java.awt.image.MemoryImageSource;
import java.lang.Math;
import java.awt.event.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;

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
