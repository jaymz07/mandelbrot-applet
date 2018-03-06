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
	int XBOUND, YBOUND, NUMFRAMES=300;
	int numIterations=200,COLORMODE0=0,COLORMODE1=0,dragging=0,calcMode=1,maxValue=0,ANIMATING=0;
	double rW=2,iW=2,cutOff=10000,power=2,pStart=2,pEnd=3,scale=1;
	float contrast=1;
	Point cent=new Point(-0.743643887037158704752191506114774, 0.131825904205311970493132056385139),boxPoint;
	boolean DRAWZOOM=false;

	boolean init=true,SMOOTHING=false,recompute=true,absColor=true;

	BufferedImage image;

	Graphics iPage,page;

	Point mP=new Point(0,0),mPP=new Point(0,0);

	int [][] vals;
	int numThreads=Runtime.getRuntime().availableProcessors();

	LoadingBar loading=null;
	MIterator [] threads= new MIterator[numThreads];

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
		return z.times(z).plus(c.times(scale));
	}
	public Complex powFunction(Complex z, Complex c, double pow)
	{
		//return z.times(z).plus(c.exp()); //fractal wall
		//return z.exp().plus(c.exp()); //wtf flowers?
		//return z.exp().plus(c.times(c));
		//return z.times(z.exp().cos()).plus(c.times(c.plus(z).exp()));
		//return z.pow(3).plus(c);
		//return z.times(z).plus(c.dividedBy(z.plus(1)));
		return z.pow(pow).plus(c);
	}

	public void calculate()
	{
	    vals=new int[YBOUND][XBOUND];
	   loading.start();
	   for(int i=0;i<threads.length;i++)
	   {
	      threads[i]=new MIterator(XBOUND,YBOUND,rW,iW,cent,numThreads,i,numIterations);
	      if(i==0)
		threads[i].loading=loading;
	      threads[i].start();
	   }
	   int [][][] outs = new int[numThreads][][];
	   maxValue=0;
	   for(int i=0;i<threads.length;i++)
	   {
	      try {
		  threads[i].join();
		  maxValue=Math.max(threads[i].maxIterations,maxValue);
	      }
	      catch(InterruptedException e){
	      }
	    outs[i]=threads[i].out;
	   }
	   int prog=0;
	   for(int i=0;i<threads.length;i++)
	   {
	    int count=0;
	    for(int j=i;j<YBOUND;j+=numThreads)
	    {
	      for(int k=0;k<XBOUND;k++) {
		vals[j][k]=outs[i][count][k];
	      }
	      count++;
	      prog++;
	      loading.progress=.5+((double)prog)/YBOUND/2;
	    }
	   }
	  loading.cont=false;
	  try {
		loading.join();
	  }
	  catch(InterruptedException e){
	  }
	  dragging=0;
	}
	public Point getScreenPoint(Point in)
	{
		return new Point(cent.x+(in.x-XBOUND/2)/XBOUND*rW,cent.y+(in.y-YBOUND/2)/YBOUND*iW);
	}
	public void paint(Graphics p)
	{
		if(init)
		{
			Dimension appletSize = this.getSize();
  	 		YBOUND = appletSize.height;
   			XBOUND = appletSize.width;

	   		image = new BufferedImage(XBOUND,YBOUND,BufferedImage.TYPE_4BYTE_ABGR);
	   		System.out.println("Using "+numThreads+" threads for computation...");

			init=false;
			this.addKeyListener(this);
			this.addMouseListener( this );
	 		this.addMouseMotionListener( this );

		}
		if(recompute){
		    if(dragging==2){
		      double width=maxAbs(mP.x-boxPoint.x,mP.y-boxPoint.y);
		      loading=new ZoomScreen(p,XBOUND,YBOUND,image,boxPoint,new Point(boxPoint.x+width,boxPoint.y+width));
		    }
		    else
		      loading=new LoadingBar(p,XBOUND,YBOUND,image);
		}
		Dimension appletSize = this.getSize();
		if(XBOUND!=appletSize.width||YBOUND!=appletSize.height)
			recompute=true;
 		YBOUND = appletSize.height;
		XBOUND = appletSize.width;

		image = new BufferedImage(XBOUND,YBOUND,BufferedImage.TYPE_4BYTE_ABGR);

		iPage=image.getGraphics();

		iPage.setColor(Color.BLACK);
		iPage.fillRect(0,0,XBOUND,YBOUND);

		if(recompute) {
			double timeP=System.nanoTime();
			calculate();
			timeP=System.nanoTime()-timeP;
			System.out.println(timeP+"ns for this frame");
			recompute=false;
		}

		generateImage();

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
	public void generateImage()
	{
	  for(int i=0;i<XBOUND;i++)
			for(int j=0;j<YBOUND;j++)
			{
				if(calcMode==0) {
					if(vals[j][i]<100000){
						if(COLORMODE0==0)
							image.setRGB(i,j,getColorValue(Math.exp(-vals[j][i]),0,1));
						if(COLORMODE0==1)
							image.setRGB(i,j,getColorValue(1.0/(1+Math.exp(-vals[j][i])),.5,1));
						if(COLORMODE0==2)
							image.setRGB(i,j,getColorValue(Math.exp(-vals[j][i]*vals[j][i]),.5,1));
						if(COLORMODE0==3)
							image.setRGB(i,j,getColorValue(1.0/(1+vals[j][i]*vals[j][i]),.5,1));
					}
					else
						image.setRGB(i,j,Color.BLACK.getRGB());
				}
				else if(calcMode==1)
				{
					if(Math.abs(vals[j][i])>.1){
						double val=numIterations;
						if(!absColor)
							val=maxValue;
						if(COLORMODE1==0)
							image.setRGB(i,j,getColorValue(vals[j][i],0,val));
						else if(COLORMODE1==1)
							image.setRGB(i,j,getColorValue(Math.log(vals[j][i]),0,Math.log(val)));
						else if(COLORMODE1==2)
							image.setRGB(i,j,getColorValue(1.0/(1+Math.exp(-.01*vals[j][i])),0,1.0/(1+Math.exp(-.01*val))));
						else if(COLORMODE1==3)
							image.setRGB(i,j,getColorValue(Math.exp(.0001*vals[j][i]),0,Math.exp(.0001*val)));
					}
					else
						image.setRGB(i,j,Color.BLACK.getRGB());
				}
			}
	}

   public void keyPressed(KeyEvent e) {
		if(e.getKeyCode()==KeyEvent.VK_M)
   		{
			if(numThreads!=1)
			  numThreads=1;
			else
			  numThreads=4;
			recompute=true;
   		}
   		if(e.getKeyCode()==KeyEvent.VK_UP)
   		{
			//cent=getScreenPoint(mP);
			iW/=2;
			rW/=2;
			recompute=true;
   		}
   		if(e.getKeyCode()==KeyEvent.VK_1&&contrast>1)
   		{
			contrast/=2;
   		}
   		if(e.getKeyCode()==KeyEvent.VK_2)
   		{
			contrast*=2;
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
   		if(e.getKeyCode()==KeyEvent.VK_H)
   		{
   			numIterations*=100;
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
	   			if(COLORMODE0>3)
	   				COLORMODE0=0;
   			}
   			if(calcMode==1) {
	   			COLORMODE1++;
	   			if(COLORMODE1>3)
	   				COLORMODE1=0;
   			}
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
   		if(e.getKeyCode()==KeyEvent.VK_F1)
   		{
			if(ANIMATING==0)
			  ANIMATING=1;
			else
			   ANIMATING=0;
   		}
   		if(e.getKeyCode()==KeyEvent.VK_F)
   		{
			scale/=2;
			cent.x*=2;
			cent.y*=2;
			rW*=2;
			iW*=2;
			recompute=true;
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
		//System.out.println("iW:\t"+iW+"\nrW:\t"+rW+"\n"+cent+"\n----------------------\n");
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


    		Color c = Color.getHSBColor(val*contrast,1.0f,1.0f);
    		return c.getRGB();
    }


}
