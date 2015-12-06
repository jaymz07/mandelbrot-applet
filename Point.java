import java.awt.*;

public class Point
{
	public double x;
	public double y;
	public Color color;

	public Point(double a, double b)
	{
		x=a;
		y=b;
		color=Color.RED;
	}

	public Point(double a, double b, Color c)
	{
		this(a,b);
		color=c;
	}

	public double getX()
	{
		return x;
	}

	public double getY()
	{
		return y;
	}

	public void setX(double a)
	{
		x=a;
	}

	public void setY(double a)
	{
		y=a;
	}

	public Color getColor()
	{
		return color;
	}

	public String toString()
	{
		return "("+x+","+y+")";
	}

	public Point inverse()
	{
		return new Point(y,x);
	}
	public boolean equalsPoint(Point p)
	{
		return p.x==x&&p.y==y;
	}
	public double getDist(Point p)
	{
		return Math.sqrt(Math.pow(x-p.x,2)+Math.pow(y-p.y,2));
	}
	public Point getMidpoint(Point p)
	{
		return new Point((p.x+x)/2,(p.y+y)/2);
	}
	public void rotate(double ang)
	{
		double xnew=x*Math.cos(ang)-y*Math.sin(ang);
		y=y*Math.cos(ang)+x*Math.sin(ang);
		x=xnew;
	}
}