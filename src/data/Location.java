
package data;

import java.util.Date;

public class Location 
{
	public int x;
	public int y;
	public int fCapacity;
	
	//set as delivered when vehicles reach location
	private boolean fDelivered;
	private Date fDeadline;
	
	public Location( int aX, int aY )
	{
		x = aX;
		y = aY;
		fCapacity = 0;
		
		fDelivered = false;
		fDeadline = null;
	}
	
	public void setDelivered()
	{
		fDelivered = true;
		fDeadline = null;
	}
	
	public boolean beenDelivered()
	{
		return fDelivered;
	}
	
	public Date getDeadline()
	{
		return fDeadline;
	}
	
	public int getCapacity()
	{
		return fCapacity;
	}
}