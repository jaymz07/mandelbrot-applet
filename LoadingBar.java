import java.awt.*;
import java.awt.image.BufferedImage;

public class LoadingBar extends Thread
{
	int XBOUND, YBOUND;

	public volatile double progress=0;

	Graphics page;

	BufferedImage image;

	public boolean cont=true;
	
	int ratio=6,width;

	LoadingBar(Graphics p, int xB, int yB, BufferedImage im)
	{
		page=p;
		XBOUND=xB;
		YBOUND=yB;
		image=im;
		width=YBOUND/15;

	}

	public void printScreen()
	{
		BufferedImage overlay = new BufferedImage(XBOUND,YBOUND,BufferedImage.TYPE_4BYTE_ABGR);
		Graphics iPage=overlay.getGraphics();
		iPage.drawImage(image,0,0,new Color(255,255,255),null);
		iPage.setColor(new Color(255,255,255,200));
		iPage.drawRect(XBOUND/ratio,YBOUND/2-width/2,XBOUND*(ratio-2)/ratio,width);
		iPage.fillRect(XBOUND/ratio,YBOUND/2-width/2,(int)(XBOUND*(ratio-2)/ratio*progress),width);
		page.drawImage(overlay,0,0,new Color(255,255,255),null);
	}

	public void run()
	{
		while(cont) {
			printScreen();
			try {
			  Thread.sleep(15);
			}
			catch(InterruptedException e) {}			
		}
	}
}