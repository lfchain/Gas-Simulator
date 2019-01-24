package ParticleSim.ParticleSim;

import java.applet.Applet;
import java.awt.Choice;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Random;
import java.util.Vector;
import static java.lang.Math.*;
import java.lang.Math;


public class Gas extends Applet implements ComponentListener, ActionListener, AdjustmentListener, ItemListener {
  
  Thread engine = null;
  int molCount;

  Dimension winSize;
  Image dbimage;
  
  public static final int defaultPause = 10;
  int heaterSize;
  int pause;
  Random random;

  public static int gridEltWidth = 60; 
  public static int gridEltHeight = 60;

  int gridWidth;
  int gridHeight;
  Molecule mols[];
  Molecule grid[][];
  Molecule bigmol;
  Molecule bigmols[];
  
  double gravityBar = 0;
  double speedBar = 5;
  double molCountBar = 500;
  double colorBar = 5;
  
  Choice setupChooser;
  Vector setupList;
  Setup setup;
  double gravity;
  double colorMult;
  int upperBound;
  double topWallPos;
  double topWallVel;
  int areaHeight;
  double heatstate;
  double heaterTemp;
  double heaterMove;
  double wallF, wallFMeasure;
  Color heaterColor;
  Color colors[];
  int heaterTop;
  int heaterLeft;
  int heaterRight;
  final int maxMolCount = 10000;
  NumberFormat showFormat;

  int getrand(int x) {
	int q = random.nextInt();
	if (q < 0) q = -q;
	return q % x;
  }
  GasCanvas cv;

  public void init() {
	setupList = new Vector();
	Setup s = new Setup1Random();
	while (s != null) {
	    setupList.addElement(s);
	    s = s.createNext();
	}
	showFormat = DecimalFormat.getInstance();
	showFormat.setMaximumFractionDigits(3);
	
	int ci = 0;
	heatstate = 0;
	colors = new Color[5];
	colors[ci++] = new Color(255 ,106,213);
	colors[ci++] = new Color(199 ,116,232);
	colors[ci++] = new Color(173,140,255);
	colors[ci++] = new Color(135,149,232);
	colors[ci++] = new Color(148,208,255);

	gravity = 0;
	setLayout(new GasLayout());
	cv = new GasCanvas(this);
	cv.addComponentListener(this);
	add(cv);

	setupChooser = new Choice();
	for (int i = 0; i != setupList.size(); i++)	setupChooser.add("Setup: " + ((Setup) setupList.elementAt(i)).getName());
	setupChooser.addItemListener(this);
	add(setupChooser);
	
	cv.setBackground(Color.white);
	cv.setForeground(heaterColor = Color.lightGray);
	
	random = new Random();
	pause = defaultPause;
	
	try {
		String param = getParameter("PAUSE");
	    if (param != null)	pause = Integer.parseInt(param);
	} catch (Exception e) { }
	reinit(true);
	repaint();
  }

  static final int SPEED_RANDOM  = 0;
  static final int SPEED_EQUAL   = 1;
  static final int SPEED_EXTREME = 2;

  void reinit(boolean newsetup) {
	if (cv.getSize().width == 0 || setupChooser == null)	return;
	bigmol = null;
	bigmols = null;
	setup = (Setup) setupList.elementAt(setupChooser.getSelectedIndex());
	if (newsetup) {
	    
	    setup.select();
	}
	setup.reinit();
  }
  
  void expand() {
	topWallPos -= 50;
	if (topWallPos < 0)	topWallPos = 0;
  }

  void initMolecules(int speed) {
    Dimension d = winSize = cv.getSize();
	molCount 	= 500;
	upperBound 	= (int) (winSize.height*(1-setup.getVolume())-1);
	topWallPos 	= upperBound;
	areaHeight 	= winSize.height-upperBound;
	mols 		= new Molecule[maxMolCount];
	dbimage 	= createImage(d.width, d.height);
	gridWidth 	= d.width /gridEltWidth+1;
	gridHeight 	= d.height/gridEltHeight+1;
	grid 		= new Molecule[gridWidth][gridHeight];
	int i, j;
	for (i = 0; i != gridWidth; i++) {
	    for (j = 0; j != gridHeight; j++) {
			grid[i][j] = new Molecule();
			grid[i][j].listHead = true;
	    }
	}
	for (i = 0; i != maxMolCount; i++) {
	    Molecule m = new Molecule();
	    mols[i] = m;
	    m.r = random.nextInt(6)+1;
	    m.x = m.r*6/winSize.getWidth();
	    m.y = getrand(areaHeight*10)*.1+upperBound;
	    m.dx = (getrand(100)/99.0-.5);
	    m.dy = java.lang.Math.sqrt(1-m.dx*m.dx);
	    if (getrand(10) > 4)	m.dy = -m.dy;
	    if (speed == SPEED_EXTREME) {
			double q = ((i & 2) > 0) ? 2 : .1;
			m.dx *= q;
			m.dy *= q;
	    }
	    if (speed == SPEED_RANDOM) {
			double q = getrand(101)/70.;
			m.dx *= q;
			m.dy *= q;
	    }
	    if (Double.isNaN(m.dx) || Double.isNaN(m.dy))	System.out.println("nan1");
	    setColor(m);
	    if (i < molCount)	gridAdd(m);
	}
	
	cv.repaint();

  }

  void setMoleculeTypes(double mult, int typeCount) {
	int i;
	for (i = 0; i != maxMolCount; i++) {
	    Molecule m = mols[i];
	    m.r *= mult;
	    m.mass *= mult*mult;
	    if (typeCount > 1) {
			int n = (i % typeCount);
			m.type = n;
			if (n == 2) {
			    m.r *= 3;
			    m.mass *= 9; // was 27
			} else if (n == 1) {
			    m.r *= 2;
			    m.mass *= 4; // was 8
			}
	    }
	    setColor(m);
	}
  }

  long secTime, lastTime;
  double t, lastSecT, totalKE, temp, totalV;
  
  public void updateGas(Graphics realg) {
	if (winSize == null)	return;
	Graphics g = dbimage.getGraphics();
	g.setColor(cv.getBackground());
	g.fillRect(0, 0, winSize.width, winSize.height);
	
	int j;
	double dt = speedBar/100.;
	// if (!stoppedCheck.getState()) {
	    long sysTime = System.currentTimeMillis();
	    if (lastTime != 0) {
			int inc = (int) (sysTime-lastTime);
			dt *= inc/8.;
	    }
	    if (sysTime-secTime >= 1000) {
			if (t > 0)	wallF /= t-lastSecT;
			wallFMeasure = wallF;
			wallF = 0;
			secTime = sysTime;
			lastSecT = t;
	    }
	    lastTime = sysTime;
	
	for (short i = 0; i != molCount; i++) {
	    Molecule m = mols[i];
	    boolean bounce = false;
	    j = 0;
	    for (; j < 5; j++) {
			m.dy += gravity*dt;
			m.x += m.dx*dt;
			m.y += m.dy*dt;
			if (Double.isNaN(m.dx) || Double.isNaN(m.dy))	System.out.println("nan2");
			int r = m.r;
			if(bigmols != null && m.mass >100){
		    	m.dx = 0;
		    	m.dy = 0;
		    }else{
				if (m.x < r || m.x >= winSize.width-r) {
				    wallF += Math.abs(m.dx)*m.mass;
				    m.dx = -m.dx;
				    if (m.x < m.r) m.x = m.r;
				    if (m.x >= winSize.width-r)
					m.x = winSize.width-r-1;
				    // setColor(m);
				    bounce = true;
				}
				if (m.y < upperBound+r || m.y >= winSize.height-r) {
				    wallF += Math.abs(m.dy)*m.mass;
				    if (m.y < upperBound+r) m.y = upperBound+r;
				    if (m.y >= winSize.height-r)
					m.y = winSize.height-r-1;
					m.dy = -m.dy;
				    // setColor(m);
				    bounce = true;
				}
		    }
			
			Molecule m2 = (bounce) ? null : checkCollision(m);
			if (m2 != null) {
			    // handle a collision
			    // first, find exact moment they collided by solving a quadratic equation:
			    // [(x1-x2)+t(dx1-dx2)]^2 + [(y1-y2)+...]^2 = mindist^2
			    // (first deal with degenerate case where molecules are on top of each other)
			    if (m.dx == m2.dx && m.dy == m2.dy) {
					if (m.dx == 0 && m.dy == 0)	continue;
					m.dx += .001;
			    }
			    
			    double sdx = m.dx-m2.dx;
			    double sx  = m.x -m2.x;
			    double sdy = m.dy-m2.dy;
			    double sy  = m.y -m2.y;
			    int mindist = m.r + m2.r;
			    double a = sdx*sdx + sdy*sdy;
			    double b = 2*(sx*sdx+sy*sdy);
			    double c = sx*sx + sy*sy - mindist*mindist;
			    double t = (-b-java.lang.Math.sqrt(b*b-4*a*c))/a;
			    double t2 = (-b+java.lang.Math.sqrt(b*b-4*a*c))/a;
			    if (java.lang.Math.abs(t) > java.lang.Math.abs(t2))	t = t2;
			    if (Double.isNaN(t))	System.out.print("nan " + m.dx + " " + m.dy + " " + m2.dx 
			    		+ " " + m2.dy + " " + a + " " + b + " " +c + " " + t + " " + t2 + "\n");
	
			    // backtrack m to where they collided.
			    // (t is typically negative.)
			    if(bigmols != null && m.mass >100){
			    	m.dx = 0;
			    	m.dy = 0;
			    }else{
				    m.x += t*m.dx;
				    m.y += t*m.dy;
			    }
	
			    // ok, so now they are just touching.  find vector
			    // separating their centers and normalize it.
			    sx = m.x-m2.x;
			    sy = m.y-m2.y;
			    double sxynorm = java.lang.Math.sqrt(sx*sx+sy*sy);
			    double sxn = sx/sxynorm;
			    double syn = sy/sxynorm;
			    
			    // find velocity of center of mass
			    double totmass = m.mass + m2.mass;
			    double comdx = (m.mass*m.dx+m2.mass*m2.dx)/totmass;
			    double comdy = (m.mass*m.dy+m2.mass*m2.dy)/totmass;
			    //System.out.print("<x " + (m.dx-comdx) + " " + (m2.dx-comdx) + "\n");
			    //System.out.print("<y " + (m.dy-comdy) + " " + (m2.dy-comdy) + "\n");
	
			    // subtract COM velocity from m's momentum and
			    // project result onto the vector separating them.
			    // This is the component of m's momentum which
			    // must be turned the other way.  Double the
			    // result.  This is the momentum that is
			    // transferred.
			    double pn = (m.dx-comdx)*sxn + (m.dy-comdy)*syn;
			    double px = 2*sxn*pn;
			    double py = 2*syn*pn;
	
			    // subtract this vector from m's momentum
			    if(bigmols != null && m.mass >100){
			    	
			    }else{
			    	 m.dx -= px;
					 m.dy -= py;
			    }
			   
			    if (Double.isNaN(m.dx))
				System.out.println("nan0 " + sxynorm + " " + pn);
	
			    if(bigmols != null && m.mass >10 ||bigmols != null && m2.mass >10 ){
			    	continue;
			    }
			    // adjust m2's momentum so that total momentum
			    // is conserved
			    double mult = m.mass/m2.mass;
			    m2.dx += px*mult;
			    m2.dy += py*mult;
			   
			    // send m on its way
			    if (t < 0) {
			    	if(bigmols != null && m.mass >100){
			    		m.x -= t*m.dx;
						m.y -= t*m.dy;
				    }else{
				    	m.x -= t*m.dx;
						m.y -= t*m.dy;
				    }
			    }
			    if (m.x < r)
				m.x = r;
			    if (m.x >= winSize.width-r)
				m.x = winSize.width-r;
			    if (m.y < upperBound+r) m.y = upperBound+r;
			    if (m.y >= winSize.height-r)
				m.y = winSize.height-r-1;
			    if (Double.isNaN(m.dx) || Double.isNaN(m.dy))	System.out.println("nan4");
			    if (Double.isNaN(m2.dx) || Double.isNaN(m2.dy))	System.out.println("nan5");
			    
			}
	    }
	    if(bigmols != null && m.mass >100){
	    	g.setColor(Color.black);
	        g.fillRect((int) (m.x-m.r), (int) (m.y-m.r), m.r, m.r*2+5);
	    }else{
	    g.setColor(m.color);
	    g.fillOval((int)m.x-m.r, (int)m.y-m.r, m.r*2, m.r*2);
	    }
	 
	    gridRemove(m);
	    gridAdd(m);
	}
	t += dt*5;
	totalKE = 0;
	totalV = 0;
	for (short i = 0; i != molCount; i++) {
	    Molecule m = mols[i];
	    totalKE += m.ke;
	    totalV += m.r*m.r;
	}
	totalV *= Math.PI;
	temp = totalKE/molCount; 
	if (topWallVel > .5)
	    topWallVel = .5;
	topWallPos += topWallVel*5;
	if (topWallPos < 0) {
	    topWallPos = 0;
	    if (topWallVel < 0)
		topWallVel = 0;
	}
	if (topWallPos > (winSize.height*4/5)) {
	    topWallPos = (winSize.height*4/5);
	    if (topWallVel > 0)	topWallVel = 0;
	}
	upperBound = (int) topWallPos;
	
	
	g.setColor(Color.lightGray);
	g.drawRect(0, upperBound, winSize.width-1, winSize.height-1-upperBound);
	g.fillRect(winSize.width/2 - 20, 0, 40, upperBound);
	realg.drawImage(dbimage, 0, 0, this);
    heatstate += heaterMove;
    cv.repaint(pause);
  }

  void gridAdd(Molecule m) {
	int gx = (int) (m.x/gridEltWidth);
	int gy = (int) (m.y/gridEltHeight);
	Molecule g = grid[gx][gy];
	m.next = g;
	m.prev = g.prev;
	g.prev = m;
	m.prev.next = m;
  }

  void gridRemove(Molecule m) {
	m.next.prev = m.prev;
	m.prev.next = m.next;
  }

  Molecule checkCollision(Molecule m) {
	if (bigmol != null) {
	    Molecule q = checkCollision(m, grid[(int) (bigmol.x/gridEltWidth)][(int) (bigmol.y/gridEltHeight)]);
	    if (q != null)	return q;
	}
	if (bigmols != null) {
		for (int i=0; i<bigmols.length; i++) {
			Molecule q = checkCollision(m, grid[(int) (bigmols[i].x/gridEltWidth)][(int) (bigmols[i].y/gridEltHeight)]);
		    if (q != null)	return q;
		}
	}
	int gx = (int) (m.x/gridEltWidth);
	int gy = (int) (m.y/gridEltHeight);
	int i, j;
	for (i = -1; i <= 1; i++)
	    for (j = -1; j <= 1; j++) {
		if (gx+i < 0 || gy+j < 0 || gx+i >= gridWidth || gy+j >= gridHeight)	continue;
		Molecule n = checkCollision(m, grid[gx+i][gy+j]);
		if (n != null)	return n;
	    }
	return null;
  }

  Molecule checkCollision(Molecule m, Molecule list) {
	Molecule l = list.next;
	for (; !l.listHead; l = l.next) {
	    if (m == l)
		continue;
	    int mindist = m.r+l.r;
	    double dx = m.x-l.x;
	    double dy = m.y-l.y;
	    if (dx > mindist || dy > mindist || dx < -mindist || dy < -mindist)	continue;
	    double dist = java.lang.Math.sqrt(dx*dx+dy*dy);
	    if (dist > mindist)	continue;
	    return l;
	}
	return null;
  }

  void setColor(Molecule m) {
	if(bigmols != null && m.r >= winSize.getHeight()/30-1) { 
		m.vel = Math.sqrt(m.dx*m.dx+m.dy*m.dy);
		m.ke = .5*m.mass*m.vel*m.vel;
		
		m.color = Color.black;
	}else{
		m.vel = Math.sqrt(m.dx*m.dx+m.dy*m.dy);
		m.ke = .5*m.mass*m.vel*m.vel;
		int col = (int) (m.ke*colorMult);
		int maxcol = colors.length-1;
		if (col > maxcol) col = maxcol;
		m.color = colors[maxcol - random.nextInt(maxcol)];
	}
  }

  public void componentHidden(ComponentEvent e){}
  public void componentMoved(ComponentEvent e){}
  public void componentShown(ComponentEvent e){}
  public void componentResized(ComponentEvent e) {
	reinit(false);
	cv.repaint(100);

  }
  public void actionPerformed(ActionEvent e) {
	System.out.println(e);
  }
  
  public void adjustmentValueChanged(AdjustmentEvent e) { }
  
  public void itemStateChanged(ItemEvent e) {
	
	if (e.getItemSelectable() == setupChooser)
	    reinit(true);
  }

  void adjustMolCount() {
	int oldcount = molCount;
	// molCount = molCountBar.getValue();
	if (molCount == oldcount)
	    return;
	if (oldcount > molCount) {
	    int i;
	    for (i = molCount; i != oldcount; i++)
		gridRemove(mols[i]);
	} else {
	    int i;
	    for (i = oldcount; i != molCount; i++)
		gridAdd(mols[i]);
	}
  }
  
  class Molecule {
	public double x, y, dx, dy, mass, ke, vel;
	public int r, type;
	public Color color;
	public Molecule next, prev;
	public boolean listHead;
	Molecule() {
	    r = 2;
	    type = 0;
	    mass = 2;
	    next = prev = this;
	}
  };
  
  abstract class Setup {
	abstract String getName();
	void select() {}
	void reinit() {}
	void deselect() {}
	int getHistogramCount() { return 1; }
	double getVolume() { return 1; }
	abstract Setup createNext();
  };

  class Setup1Random extends Setup {
	String getName() { return "1 Gas, Random Speeds"; }
	void reinit() {
	    initMolecules(SPEED_RANDOM);
	    setMoleculeTypes(2, 1);
	}
	Setup createNext() { return new SetupBigMolecule(); }
  }

  class SetupBigMolecule extends Setup {
		String getName() { return "Big Molecule"; }
		void select() {
		    speedBar = 70;
		    colorBar = 210;
		}
		void reinit() {
		    initMolecules(SPEED_RANDOM);
		    bigmol = mols[0];
		    bigmol.r = winSize.height/15;
		    bigmol.mass = 40;
		    bigmol.dx = bigmol.dy = 0;
		    bigmol.vel = 0;
		    
		}
		Setup createNext() { return new SetupThreeSections(); }
  }
  class SetupThreeSections extends Setup {
		String getName() { return "Two Sections"; }
		void select() {
		    speedBar = 30;
		}
		void reinit() {
		    initMolecules(SPEED_RANDOM);
		    setMoleculeTypes(2, 1);
		    int numBigMols = 20;
		    bigmols = new Molecule[numBigMols];
		    int w = winSize.width;
		    int h = winSize.height;
		    int r = h/30 -1;
		    int[] startingPoints = {w/3, w/3, 2*w/3, 2*w/3, h/30, 7*h/10, h/30, 7*h/10};
		    for (int i=0; i<4; i++) {
		    	for (int j=0; j<5; j++) {
		    		bigmols[i*5+j] = mols[i*5+j];
			    	bigmols[i*5+j].r = r;
			    	bigmols[i*5+j].mass = 99999999;
			    	bigmols[i*5+j].dx = 0;
			    	bigmols[i*5+j].dy = 0;
			    	bigmols[i*5+j].vel = 0;
			    	bigmols[i*5+j].ke = 0;
			    	bigmols[i*5+j].x = startingPoints[i];
			    	bigmols[i*5+j].y = startingPoints[i+4] + j*h/15;
		    	}
		    }
		}
		Setup createNext() { return null; }
  }
}
