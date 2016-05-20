Titanic is a Bukkit plugin for 1.9.4 and above. Its sole function is to try to fix [MC-91206]
(https://bugs.mojang.com/browse/MC-91206), a regression where boats are sunk by entering sloped
water, currents and waterfalls.

This plugin is not perfect. Since 1.9, boat movement whilst riding is handled by the client. The
plugin has to fight against the client's movements by setting velocity. It is too difficult to
emulate pre-1.9 behavior.

Titanic used to be ToughBoats; a plugin originally coded by [Cyclometh]
(https://github.com/Cyclometh). Before 1.9, ToughBoats fixed a variety of boat problems. It lives
on in the `legacy` branch.

# Behavior

Currently, Titanic is able to push boats upwards if they:

* Spawn or otherwise end up underwater
* Attempt to enter a current or slope
* Float down a current or stream
* Enter a waterfall
* Start sinking because of partial exposure to waterfall

Titanic has trouble with:

* Keeping players attached; if a boat has been underwater too long, the client will force a 
  dismount. This cannot be solved by cancelling `VehicleExitEvent` as there is no reliable way to
  tell when a player is intentionally dismounting.
* Smooth rides; Titanic works by setting velocity on ridden boats. This interrupts momentum on the
  client side, making movement jerky or unpredictable.
* Waterfalls/elevators; sometimes boats may shoot too far up too fast, or shoot through the water
    
# Dev notes

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

# Building

1. Install Maven on your system
    * Windows: https://maven.apache.org/download.cgi
    * Linux: Install the `maven` package (e.g. `sudo apt-get install maven`)
2. Clone this repository into a folder
3. Inside the folder, execute `mvn clean package`
4. Look in the new `target` folder for the built JAR file

# Debugging

Titanic makes use of `FINE` logging levels for debugging. To enable these messages, append this line
to the server's JVM arguments:

> `-Dlog4j.configurationFile=log4j.xml`

Then in the root directory of the server, create the file `log4j.xml` with these contents:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<Configuration monitorInterval="5" packages="com.mojang.util">
  <Appenders>
    <Queue name="TerminalConsole">
      <PatternLayout pattern="[%d{HH:mm:ss} %level]: %msg%n"/>
    </Queue>
  </Appenders>
  <Loggers>
    <Root level="INFO">
      <AppenderRef ref="TerminalConsole"/>
    </Root>
    <Logger additivity="false" level="ALL" name="com.cyclometh.bukkit.plugins.toughboats.Titanic">
      <AppenderRef ref="TerminalConsole"/>
    </Logger>
  </Loggers>
</Configuration>
```