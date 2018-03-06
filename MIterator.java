import java.awt.*;
import java.awt.image.BufferedImage;

public class MIterator extends Thread
{
	int XBOUND, YBOUND;

	Graphics page;

	BufferedImage image;

	Point p1,p2;
	double rW,iW;
	int numThreads,threadNum,numIterations;
	Point cent;
	
	double cutOff=100000;
	
	public int [][] out;
	public boolean done=false;
	public int maxIterations=0;
	
	public LoadingBar loading=null;

	MIterator(int xB, int yB,double realWidth, double imaginaryWidth, Point center, int n,int t,int nI)
	{
	  XBOUND=xB;
	  YBOUND=yB;
	  rW=realWidth;
	  iW=imaginaryWidth;
	  numThreads=n;
	  threadNum=t;
	  numIterations=nI;
	  cent=center;
	  out=new int[(YBOUND-threadNum)/numThreads+1][XBOUND];
	}
	public void run()
	{
	  computeMatrix();
	  done=true;
	}
	public Complex function(Complex z, Complex c)
	{
	  return z.times(z).plus(c);
	}
	public void computeMatrix()
	{
		int countI=0;
		for(int i=threadNum;i<YBOUND;i+=numThreads) {
			int countJ=0;
			for(int j=0;j<XBOUND;j++)
			{
				Complex c= new Complex(cent.x+rW*(-1.0/2+1.0/XBOUND*j),cent.y+iW*(-1.0/2+1.0/YBOUND*i)), num=new Complex(0,0);
				for(int k=0;k<numIterations;k++){
					num=function(num,c);
					if(num.abs()>cutOff) {
						out[countI][countJ]=k+1;
						maxIterations=Math.max(maxIterations,k+1);
						break;
					}
					if(k==numIterations-1)
						out[countI][countJ]=0;
				}
				countJ++;

			}
			if(loading!=null)
			  loading.progress=((double)countI)/(out.length-1)/2;
			countI++;
		}
	}
	public int[][] getMatrix()
	{
	   return out;
	}
	
}