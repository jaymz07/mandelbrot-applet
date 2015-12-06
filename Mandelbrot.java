	import java.awt.*;
import java.applet.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;

public class Mandelbrot extends Applet implements MouseListener, MouseMotionListener, KeyListener
{
	int XBOUND, YBOUND;
	int numIterations=20,COLORMODE0=0,COLORMODE1=0,dragging=0,calcMode=1,maxValue=0,iterateMode=1;
	double rW=2,iW=2,cutOff=10000;
	Point cent=new Point(-0.743643887037158704752191506114774, 0.131825904205311970493132056385139),boxPoint;
	boolean DRAWZOOM=false;

	boolean init=true,SMOOTHING=false,recompute=true,absColor=true;

	BufferedImage image;

	Graphics iPage,page;

	Point mP=new Point(0,0),mPP=new Point(0,0);

	Matrix vals;

	Thread loading=null;
	ZoomScreen zS=null;

	public double maxAbs(double d1, double d2)
	{
		if(Math.abs(d1)>Math.abs(d2))
			return d1;
		return d2;
	}

	public Complex function(Complex z, Complex c)
	{
		//return z.times(z).plus(c.exp()); //fractal wall
		//return z.exp().plus(c.exp()); //wtf flowers?
		//return z.exp().plus(c.times(c));
		//return z.times(z.exp().cos()).plus(c.times(c.plus(z).exp()));
		//return z.pow(3).plus(c);
		//return z.times(z).plus(c.dividedBy(z.plus(1)));
		//return z.times(c.times(z.exp())).plus(c.dividedBy(z.sin().plus(1.001)));
		return z.times(z).plus(c);
	}

	public void calculate()
	{
		computeMatrix();
		if(SMOOTHING)
			vals=vals.smoothData(1,10);
	}

	public void computeMatrix()
	{
		double [][] stuff = new double[YBOUND][XBOUND];
		if(dragging==2)
		{
			loading=new Thread(zS);
			loading.start();
		}
		maxValue=0;
		for(int i=0;i<stuff.length;i++) {
			for(int j=0;j<stuff[0].length;j++)
			{
				Complex c= new Complex(cent.x-rW/2+rW/XBOUND*j,cent.y-iW/2+iW/YBOUND*i), num=new Complex(0,0);
				double d=Math.PI;
				if(calcMode==0) {
					for(int k=0;k<numIterations;k++)
						num=function(num,c);
					stuff[i][j]=num.abs();
				}
				if(calcMode==1)
				{
					if(iterateMode==1) {
						num = new Complex(cent.x-rW/2+rW/XBOUND*j,cent.y-iW/2+iW/YBOUND*i);
						c = new Complex(0.3,0.008); 
					}
					for(int k=0;k<numIterations;k++){
						num=function(num,c);
						if(num.abs()>cutOff) {
							stuff[i][j]=k+1;
							maxValue=Math.max(maxValue,k+1);
							break;
						}
						if(k==numIterations-1)
							stuff[i][j]=0;
					}
				}

			}
			if(dragging==2)
				zS.progress=((double)i)/(stuff.length-1);
		}
		if(dragging==2) {
			zS.cont=false;
			System.out.println("iW:\t"+iW+"\nrW:\t"+rW+"\n"+cent+"\n----------------------\n");
		}
		dragging=0;
		vals=new Matrix(stuff);
	}
	public Point getScreenPoint(Point in)
	{
		return new Point(cent.x+(in.x-XBOUND/2)/XBOUND*rW,cent.y+(in.y-YBOUND/2)/YBOUND*iW);
	}
	public void paint(Graphics p)
	{
		if(dragging==2){
			double width=maxAbs(mP.x-boxPoint.x,mP.y-boxPoint.y);
			zS=new ZoomScreen(p,XBOUND,YBOUND,image,boxPoint,new Point(boxPoint.x+width,boxPoint.y+width));
		}
		if(init)
		{
			Dimension appletSize = this.getSize();
  	 		YBOUND = appletSize.height;
   			XBOUND = appletSize.width;

	   		image = new BufferedImage(XBOUND,YBOUND,BufferedImage.TYPE_3BYTE_BGR);

			init=false;
			this.addKeyListener(this);
			this.addMouseListener( this );
	 		this.addMouseMotionListener( this );

		}
		Dimension appletSize = this.getSize();
		if(XBOUND!=appletSize.width||YBOUND!=appletSize.height)
			recompute=true;
 		YBOUND = appletSize.height;
		XBOUND = appletSize.width;
   		image = new BufferedImage(XBOUND,YBOUND,BufferedImage.TYPE_3BYTE_BGR);

		iPage=image.getGraphics();

		iPage.setColor(Color.BLACK);
		iPage.fillRect(0,0,XBOUND,YBOUND);

		if(recompute) {
			calculate();
			recompute=false;
		}

		for(int i=0;i<XBOUND;i++)
			for(int j=0;j<YBOUND;j++)
			{
				if(calcMode==0) {
					if(vals.data[j][i]<100000){
						if(COLORMODE0==0)
							image.setRGB(i,j,getColorValue(Math.exp(-vals.data[j][i]),0,1));
						if(COLORMODE0==1)
							image.setRGB(i,j,getColorValue(1.0/(1+Math.exp(-vals.data[j][i])),.5,1));
						if(COLORMODE0==2)
							image.setRGB(i,j,getColorValue(Math.exp(-vals.data[j][i]*vals.data[j][i]),.5,1));
						if(COLORMODE0==3)
							image.setRGB(i,j,getColorValue(1.0/(1+vals.data[j][i]*vals.data[j][i]),.5,1));
					}
					else
						image.setRGB(i,j,Color.BLACK.getRGB());
				}
				else if(calcMode==1)
				{
					if(Math.abs(vals.data[j][i])>.1){
						double val=numIterations;
						if(!absColor)
							val=maxValue;
						if(COLORMODE1==0)
							image.setRGB(i,j,getColorValue(vals.data[j][i],0,val));
						else if(COLORMODE1==1)
							image.setRGB(i,j,getColorValue(Math.log(vals.data[j][i]),0,Math.log(val)));
						else if(COLORMODE1==2)
							image.setRGB(i,j,getColorValue(1.0/(1+Math.exp(-.01*vals.data[j][i])),0,1.0/(1+Math.exp(-.01*val))));
						else if(COLORMODE1==3)
							image.setRGB(i,j,getColorValue(Math.exp(.0001*vals.data[j][i]),0,Math.exp(.0001*val)));
						else if(COLORMODE1==4)
							image.setRGB(i,j,getColorValue(Math.pow(val+5,3),Math.pow(5,3),Math.pow(6,3)));
					}
					else
						image.setRGB(i,j,Color.BLACK.getRGB());
				}
			}
		int size=50;

		BufferedImage zoom=null;
		boolean drawZoom=mP.x+size/2<XBOUND&&mP.y+size/2<YBOUND&&mP.x-size/2>=0&&mP.y-size/2>=0&&DRAWZOOM;
		if(drawZoom)
			zoom = image.getSubimage((int)(mP.x-size/2),(int)(mP.y-size/2),size,size);
		iPage.setColor(Color.WHITE);
		if(dragging==1) {
			double width=maxAbs((mP.x-boxPoint.x),(mP.y-boxPoint.y));
			if(width>0)
				iPage.drawRect((int)boxPoint.x,(int)boxPoint.y,(int)(width),(int)(width));
			else
				iPage.drawRect((int)mP.x,(int)mP.y,(int)(width),(int)(width));
		}

		if(drawZoom)
			iPage.drawImage(zoom,0,0,size*2,size*2,null);
		p.drawImage(image,0,0,new Color(255,255,255),null);

	}

	public void stop()
	{

	}

   public void keyPressed(KeyEvent e) {

   		if(e.getKeyCode()==KeyEvent.VK_UP)
   		{
			//cent=getScreenPoint(mP);
			iW/=2;
			rW/=2;
			recompute=true;
   		}
   		if(e.getKeyCode()==KeyEvent.VK_DOWN)
   		{
		//	cent=getScreenPoint(mP);
			iW*=2;
			rW*=2;
			recompute=true;
   		}
   		if(e.getKeyCode()==KeyEvent.VK_RIGHT)
   		{
			numIterations+=10;
			recompute=true;
   		}
   		if(e.getKeyCode()==KeyEvent.VK_LEFT)
   		{
   			if(numIterations>10)
				numIterations-=10;
			recompute=true;
   		}
   		if(e.getKeyCode()==KeyEvent.VK_PAGE_UP)
   		{
   			numIterations*=2;
   			recompute=true;
   		}
   		if(e.getKeyCode()==KeyEvent.VK_PAGE_DOWN)
   		{
   			numIterations/=2;
   			recompute=true;
   		}
   		if(e.getKeyCode()==KeyEvent.VK_ESCAPE)
   		{
   			dragging=0;
   			boxPoint=mP;
   		}
   		if(e.getKeyCode()==KeyEvent.VK_C)
   		{
   			if(calcMode==0) {
	   			COLORMODE0++;
	   			if(COLORMODE0>4)
	   				COLORMODE0=0;
   			}
   			if(calcMode==1) {
	   			COLORMODE1++;
	   			if(COLORMODE1>3)
	   				COLORMODE1=0;
   			}
   		}
   		if(e.getKeyCode()==KeyEvent.VK_F1)
   		{
   			iterateMode++;
   			if(iterateMode>1)
   				iterateMode=0;
   			recompute=true;
   		}
   		if(e.getKeyCode()==KeyEvent.VK_O)
   		{
   			System.out.println(vals);
   		}
   		if(e.getKeyCode()==KeyEvent.VK_S)
   		{
   			SMOOTHING=!SMOOTHING;
   			recompute=true;
   		}
   		if(e.getKeyCode()==KeyEvent.VK_I)
   		{
   			calcMode=1-calcMode;
   			recompute=true;
   		}
   		if(e.getKeyCode()==KeyEvent.VK_Z)
   		{
   			DRAWZOOM=!DRAWZOOM;
   		}
   		if(e.getKeyCode()==KeyEvent.VK_A)
   		{
   			absColor=!absColor;
   		}
   		if(e.getKeyCode()==KeyEvent.VK_R)
   		{
   			cent=new Point(0,0);
   			iW=2;
   			rW=2;
   			recompute=true;
   		}
   		repaint();

	}

	public void keyReleased(KeyEvent e) {

  	}

	public void keyTyped(KeyEvent e) {

	}


	public void update( Graphics g ) {
		paint(g);
   }

	public void mouseEntered( MouseEvent e ) {
      // called when the pointer enters the applet's rectangular area
   }
   public void mouseExited( MouseEvent e ) {
      // called when the pointer leaves the applet's rectangular area
   }
   public void mouseClicked( MouseEvent e ) {

		cent=getScreenPoint(mP);
		recompute=true;
		System.out.println("iW:\t"+iW+"\nrW:\t"+rW+"\n"+cent+"\n----------------------\n");
		repaint();
   }
   public void mousePressed( MouseEvent e ) {  // called after a button is pressed down
		boxPoint=mP;
		repaint();
   }
   public void mouseReleased( MouseEvent e ) {  // called after a button is released
		if(dragging==1) {
			cent=getScreenPoint(boxPoint.getMidpoint(mP));
			Point mPS=getScreenPoint(mP),bPS=getScreenPoint(boxPoint);
			rW=Math.abs(mPS.x-bPS.x);
			iW=Math.abs(mPS.y-bPS.y);
			rW=Math.max(rW,iW);
			iW=rW;
			dragging=2;
		}
		//dragging=0;
		recompute=true;
		repaint();
	}
   public void mouseMoved( MouseEvent e ) {  // called during motion when no buttons are down

		mPP=mP;
		mP=new Point(e.getX(),e.getY());
		repaint();

   }
   public void mouseDragged( MouseEvent e ) {  // called during motion with buttons down
   	mPP=mP;
	mP=new Point(e.getX(),e.getY());
	if(boxPoint.getDist(mP)>10)
		dragging=1;

   	repaint();

   }
   public int getColorValue(double in, double min, double max)
    {
    	float val=(float)(Math.pow((in-min),1)/Math.pow((max-min),1));


    		Color c = Color.getHSBColor(val,1.0f,1.0f);
    		return c.getRGB();
    }


}