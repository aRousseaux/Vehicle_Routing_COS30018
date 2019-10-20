
package data;

public class Vehicle 
{
	public int x;
	public int y;
	
	private int fIdentification; // for identification
	private int fCapacity; // vehicle capacity
	private int fVelocity;
	
	public Vehicle( int aIdentification, int aCapacity, int aVelocity )
	{
		x = 0;
		y = 0;
		
		fIdentification = aIdentification;
		fCapacity = aCapacity;
		fVelocity = aVelocity;
	}
	
	public Vehicle( int aX, int aY )
	{
		x = aX;
		y = aY;
	}
	
	public int getIdentification()
	{
		return fIdentification;
	}
	
	public int getCapacity()
	{
		return fCapacity;
	}
	
	public int getVelocity()
	{
		return fVelocity;
	}
}