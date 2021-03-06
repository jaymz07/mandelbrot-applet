import java.io.*;
import java.util.*;
import java.math.*;

public class Complex
{
	public double re;
	public double im;

	public Complex(double r, double i)
	{
		re=r;
		im=i;
	}
	public Complex plus(Complex b)
	{
		return new Complex(re+b.re,im+b.im);
	}
	public Complex plus(double b)
	{
		return new Complex(re+b,im);
	}
	public Complex times(double b)
	{
		return new Complex(re*b,im*b);
	}
	public Complex times(Complex b)
	{
		return new Complex(re*b.re-im*b.im,re*b.im+im*b.re);
	}
	public Complex dividedBy(Complex b)
	{
		double denom=b.abs();
		return (new Complex(b.re/denom,-b.im/denom)).times(this);
	}
	public String toString()
	{
		return re+"+"+im+"*i";
	}
	public double abs()
	{
		return Math.sqrt(re*re+im*im);
	}
	public double getAngle()
	{
		return Math.atan2(im,re);
	}
	public Complex pow(double b)
	{
		double mag=Math.pow(abs(),b),ang=getAngle();
		return new Complex(mag*Math.cos(ang*b),mag*Math.sin(ang*b));
	}
	public Complex exp()
	{
		return (new Complex(Math.cos(im),Math.sin(im))).times(Math.exp(re));
	}
	public Complex sin()
	{
		return ((new Complex(0,1)).times(this)).exp().plus((((new Complex(0,-1)).times(this)).times(-1)).exp()).dividedBy(new Complex(0,2));
	}
	public Complex cos()
	{
		return ((new Complex(0,1)).times(this)).exp().plus(((new Complex(0,-1)).times(this)).exp()).times(.5);
	}
}




