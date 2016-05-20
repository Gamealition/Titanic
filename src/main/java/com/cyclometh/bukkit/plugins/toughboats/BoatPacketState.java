package com.cyclometh.bukkit.plugins.toughboats;

import org.bukkit.Location;
import org.bukkit.entity.Boat;

/**
 * State attached to each occupied boat, used by {@see BoatPacketListener}. This is necessary
 * because the server does not track velocity of occupied boats.
 **/
public class BoatPacketState
{
    private double posX = 0;
    private double posY = 0;
    private double posZ = 0;

    public double velX = 0;
    public double velY = 0;
    public double velZ = 0;

    public BoatPacketState(Boat boat)
    {
        Location loc = boat.getLocation();
        setPos( loc.getX(), loc.getY(), loc.getZ() );
    }

    /** Updates position of boat, automatically calculating velocity */
    public void setPos(double newX, double newY, double newZ)
    {
        velX = newX - posX;
        velY = newY - posY;
        velZ = newZ - posZ;

        posX = newX;
        posY = newY;
        posZ = newZ;
    }
}
