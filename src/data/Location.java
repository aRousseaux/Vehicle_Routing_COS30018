package data;

import java.util.Date;

public class Location
{
	public int x;
	public int y;
	public int fWeight;

	//set as delivered when vehicles reach location
	private boolean fDelivered;
	private Date fDeadline;
	private int fLocationID; //Primary ID for location, uses for identification
	public int fCapacity;

	public int getfLocationID() {
		return fLocationID;
	}

	public Location(int aX, int aY)
	{
		x = aX;
		y = aY;
		fWeight = 0;

		fLocationID = 0;

		fDelivered = false;
		fDeadline = null;
	}

	public Location(int ID, int aX, int aY)
	{
		x = aX;
		y = aY;
		fWeight = 0;

		fLocationID = ID;

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

	public int getWeight()
	{
		return fWeight;
	}

	public int getCapacity()
	{
		return fCapacity;
	}
}
