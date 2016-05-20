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
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;

import java.util.HashMap;
import java.util.logging.Logger;

/**
 * Monitors the movement of boats being driven by players and attempts to prevent their
 * sinking by higher currents. See https://bugs.mojang.com/browse/MC-91206
 */
public class BoatPacketListener extends PacketAdapter implements Listener
{
    private static ToughBoats      PLUGIN;
    private static Logger          LOGGER;
    private static ProtocolManager PROTOCOL;

    private HashMap<Integer, BoatPacketState> tracking = new HashMap<>();

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
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        LOGGER.fine("Unsinkable occupied boats enabled; listening for boat move packets");
    }

    @EventHandler
    public void onBoatEnter(VehicleEnterEvent event)
    {
        Boat   boat;
        Player player;

        if (event.getVehicle().getType() != EntityType.BOAT)
            return;

        if (event.getEntered().getType() != EntityType.PLAYER)
            return;

        boat   = (Boat)   event.getVehicle();
        player = (Player) event.getEntered();

        tracking.put( boat.getEntityId(), new BoatPacketState(boat) );

        LOGGER.finer(String.format("Tracking occupied boat %s by %s",
            boat.getEntityId(), player.getName()
        ));
    }

    @EventHandler
    public void onBoatLeave(VehicleExitEvent event)
    {
        Boat   boat;
        Player player;

        if (event.getVehicle().getType() != EntityType.BOAT)
            return;

        if (event.getExited().getType() != EntityType.PLAYER)
            return;

        boat   = (Boat)   event.getVehicle();
        player = (Player) event.getExited();

        tracking.remove( boat.getEntityId() );

        LOGGER.finer(String.format("No longer tracking boat %s by %s",
            boat.getEntityId(), player.getName()
        ));
    }

    @Override
    public void onPacketReceiving(PacketEvent event)
    {
        PacketContainer packet;
        BoatPacketState state;
        Boat            boat;

        Player player  = event.getPlayer();
        Entity vehicle = player.getVehicle();

        if (vehicle == null || vehicle.getType() != EntityType.BOAT)
            return;

        boat   = (Boat) vehicle;
        packet = event.getPacket();
        state  = tracking.get( boat.getEntityId() );

        if (state == null)
            return;

        double x = packet.getDoubles().read(0);
        double y = packet.getDoubles().read(1);
        double z = packet.getDoubles().read(2);

        float yaw   = packet.getFloat().read(0);
        float pitch = packet.getFloat().read(1);

        state.setPos(x, y, z);

        // Proceed only if movement is large enough
        if (state.velY > -0.08)
            return;

        LOGGER.finer(String.format("Client %s packet. Pos: X%.3f Y%.3f Z%.3f Pitch %.3f Yaw %.3f",
            boat, x, y, z, yaw, pitch
        ));

        LOGGER.finer(String.format("Client %s packet. Vel: X%.3f Y%.3f Z%.3f",
            boat, state.velX, state.velY, state.velZ
        ));

//        event.setCancelled(true);

        PacketContainer newMove = new PacketContainer(PacketType.Play.Server.ENTITY_TELEPORT);

        newMove.getIntegers()
            .write(0, boat.getEntityId());
        newMove.getDoubles()
            .write(0, x)
            .write(1, y + 10)
            .write(2, z);

        PacketContainer newVel = new PacketContainer(PacketType.Play.Server.ENTITY_VELOCITY);

        newVel.getIntegers()
            .write(0, boat.getEntityId())
            .write(1, (int) (state.velX * 8000))
            .write(2, 5000)
            .write(3, (int) (state.velZ * 8000));

//        try
//        {
//            PROTOCOL.sendServerPacket(event.getPlayer(), newMove);
//            PROTOCOL.sendServerPacket(event.getPlayer(), newVel);
//        }
//        catch (InvocationTargetException e)
//        {
//            throw new RuntimeException("Cannot send packet " + newMove, e);
//        }
    }

}

