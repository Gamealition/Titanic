package com.cyclometh.bukkit.plugins.toughboats;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.logging.Logger;

/**
 * Monitors the movement of boats being driven by players and attempts to prevent their
 * sinking by higher currents. See https://bugs.mojang.com/browse/MC-91206
 */
public class BoatPacketListener extends PacketAdapter
{
    private static ToughBoats      PLUGIN;
    private static Logger          LOGGER;
    private static ProtocolManager PROTOCOL;

    public BoatPacketListener(ToughBoats plugin)
    {
        super(plugin, ListenerPriority.NORMAL, PacketType.Play.Client.VEHICLE_MOVE);

        PLUGIN   = plugin;
        LOGGER   = ToughBoats.LOGGER;
        PROTOCOL = ProtocolLibrary.getProtocolManager();

        PROTOCOL.addPacketListener(
            new PacketAdapter(PLUGIN, ListenerPriority.NORMAL, PacketType.Play.Server.ENTITY_TELEPORT)
            {
                @Override
                public void onPacketSending(PacketEvent event)
                {
                    PacketContainer packet = event.getPacket();

                    int    id = packet.getIntegers().read(0);
                    double x  = packet.getDoubles().read(0);
                    double y  = packet.getDoubles().read(1);
                    double z  = packet.getDoubles().read(2);


                    LOGGER.finer(String.format("Server teleport ID %s packet. Pos: X%.3f Y%.3f Z%.3f",
                        id, x, y, z
                    ));
                }
            }
        );

        PROTOCOL.addPacketListener(this);
        LOGGER.fine("Unsinkable occupied boats enabled; listening for boat move packets");

    }

    @Override
    public void onPacketReceiving(PacketEvent event)
    {
        Player player  = event.getPlayer();
        Entity vehicle = player.getVehicle();

        if (vehicle == null || vehicle.getType() != EntityType.BOAT)
            return;

        Boat            boat   = (Boat) vehicle;
        PacketContainer packet = event.getPacket();

        double x = packet.getDoubles().read(0);
        double y = packet.getDoubles().read(1);
        double z = packet.getDoubles().read(2);

        float  yaw   = packet.getFloat().read(0);
        float  pitch = packet.getFloat().read(1);

        LOGGER.finer(String.format("Client vehicle %s packet. Pos: X%.3f Y%.3f Z%.3f Pitch%.3f Yaw%.3f",
                event.getPlayer().getVehicle(),
                x, y, z, yaw, pitch
        ));

//        event.setCancelled(true);
//
//        PacketContainer newMove = new PacketContainer(PacketType.Play.Server.VEHICLE_MOVE);
//
//        newMove.getDoubles().
//                write(0, x).
//                write(1, y + 0.01).
//                write(2, z);
//        newMove.getFloat()
//                .write(0, 3.0F)
//                .write(1, 3.0F);
//
//        try
//        {
//            PROTOCOL.sendServerPacket(event.getPlayer(), newMove);
//        } catch (InvocationTargetException e)
//        {
//            throw new RuntimeException("Cannot send packet " + newMove, e);
//        }
    }

}

