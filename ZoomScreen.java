import java.awt.*;
import java.awt.image.BufferedImage;

public class ZoomScreen implements Runnable
{
	int XBOUND, YBOUND;

	public volatile double progress=0;

	Graphics page;

	BufferedImage image;

	Point p1,p2;

	public boolean cont=true;

	ZoomScreen(Graphics p, int xB, int yB, BufferedImage im, Point pt1, Point pt2)
	{
		page=p;
		XBOUND=xB;
		YBOUND=yB;
		image=im;
		if(pt1.getDist(new Point(0,0))<pt2.getDist(new Point(0,0))) {
			p1=pt1;
			p2=pt2;
		}
		else {
			p1=pt2;
			p2=pt1;
		}

	}

	public void printScreen()
	{
		page.drawImage(image.getSubimage((int)(p1.x*progress),(int)(p1.y*progress),(int)(XBOUND+(p2.x-p1.x-XBOUND)*progress),
			(int)(YBOUND+(p2.y-p1.y-YBOUND)*progress)),0,0,XBOUND,YBOUND,null);
	}

	public void run()
	{
		while(cont) {
			printScreen();
		}
	}
}