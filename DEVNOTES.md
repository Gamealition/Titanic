For anybody who wants to implement a similar fix, these are my experiences from developing Titanic:

* When a boat is not ridden by a player, the server is in full control of its velocity and position.
  This makes it easy to prevent sinking by just applying an upward velocity. The movement ends up
  being smooth on the client-side.
* Initially, I thought VehicleMoveEvent did not fire if ridden by player. Spigot used to handle it
  only via PlayerMoveEvent, [but since fixed that](https://hub.spigotmc.org/jira/browse/SPIGOT-2043).
* VehicleMoveEvent is not cancellable, so cancelling sinking packets is not an option
* When a boat is ridden by a player, the client is almost in full control:
  * It seems to ignore entity teleport packets for the ridden boat
  * Using Entity.teleport will force a dismount; re-attaching in the same event is too jerky
  * Server does not track velocity but does track position changes
  * Client only responds to Entity.setVelocity
  * Client will force a dismount if underwater too long; no way to tell if it's a manual dismount
  * Attempting to push up whilst boat is just in/on a slope makes the ride very jumpy
  
# Release diary - 21st May 2016

I started work on this plugin three weeks ago. Titanic is actually based on [ToughBoats]
(https://github.com/Gamealition/Titanic/tree/legacy) by [Cyclometh](https://github.com/Cyclometh/).
We used ToughBoats for 1.8 to prevent boat breaking, player death on certain boat conditions and
boat resync. I maintained a fork of ToughBoats to keep it updated and add some more fixes.

Although virtually none of ToughBoats' original code is now in Titanic, I opted to keep attribution
to Cyclometh. Without learning from and using ToughBoats' code, I would not have been able to do
this project.

Attempting to solve this issue by plugin was tough; when riding, boat movement is handled
client-side in 1.9. The most accurate way of solving this would be a client-side plugin, but it's
unreasonable to ask or expect players to install one for an SMP.

At one point, I tried using [ProtocolLib](https://github.com/dmulloy2/ProtocolLib) to try to cancel 
or modify incoming boat packets, or send entity teleport/move ones. This didn't work, but turned out
to be unnecessary; all I needed was a [VehicleMoveEvent](https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/event/vehicle/VehicleMoveEvent.html) handler.

The only way to move a boat whilst it's being ridden seems to be by setting a velocity. [The server 
does not track velocity of ridden boats](https://github.com/Gamealition/Titanic/blob/master/src/main/java/com/cyclometh/bukkit/plugins/toughboats/Titanic.java#L82),
and setting any causes the boat to suddenly lose momentum client-side. I couldn't just set an upward
velocity all the time; it made the boat jumpy and travel very slow.

So I had to write a slightly complicated algorithm, to handle various situations a ridden boat may 
be in. It took a lot of trial and error; debug logging, breakpointing, live code changes, rowing
back and forth and into waterfalls, etc.