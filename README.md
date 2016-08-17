Titanic is a Bukkit plugin for 1.9.4, 1.10.2 and above. Its sole function is to try to fix [MC-91206]
(https://bugs.mojang.com/browse/MC-91206), a regression where boats are sunk by entering sloped
water, currents and waterfalls.

This plugin is not perfect. Since 1.9, boat movement whilst riding is handled by the client. The
plugin has to fight against the client's movements by setting velocity. It is too difficult to
emulate pre-1.9 behavior.

Titanic used to be ToughBoats; a plugin originally coded by [Cyclometh]
(https://github.com/Cyclometh). Before 1.9, ToughBoats fixed a variety of boat problems. It lives
on in the `legacy` branch.

# Links

* [Downloads](https://github.com/Gamealition/Titanic/releases)
* [BukkitDev](http://dev.bukkit.org/bukkit-plugins/titanic-boatfix/)
* [Spigot resource](https://www.spigotmc.org/resources/titanic.23680/)

# Behavior

Currently, Titanic is able to push boats upwards if they:

* Spawn or otherwise end up underwater
* Attempt to enter a current or slope
* Float down a current or stream
* Enter a waterfall
* Start sinking because of partial exposure to waterfall

Titanic has trouble with:

* Smooth rides; Titanic works by setting velocity on ridden boats. This interrupts momentum on the
  client side, making movement jerky or unpredictable.
* When entering a water ramp, it takes a while before a ridden boat jumps up to the surface
* Keeping players attached; if a boat has been underwater too long, the client will force a 
  dismount. This cannot be solved by cancelling `VehicleExitEvent` as there is no reliable way to
  tell when a player is intentionally dismounting.
* Waterfalls/elevators; sometimes boats may shoot too far up too fast, or shoot through the water
* Sometimes, riding on a boat that runs aground from current gets stuck in a rapid up and down
  client-side movement

# Building

Titanic uses Maven for dependency management and building. These instructions are simply for
building a jar file of Titanic. This is useful for use with CI servers (e.g. Jenkins) or for
checking if the code builds in your development environment.

## Command line (Win/Linux)

*Assuming Maven is [installed to or available in PATH](https://maven.apache.org/install.html)*

1. [Clone this repository using your git client (e.g. 
`git clone https://github.com/Gamealition/Titanic.git`)](http://i.imgur.com/VB7dE6d.png)
* Go into repository directory
* [Execute `mvn clean package`](http://i.imgur.com/UOzULcl.png)
* [Built jar file will be located in the new `target` directory](http://i.imgur.com/bDGVDwW.png)

## IntelliJ

1. [Clone this repository using your git client](http://i.imgur.com/VB7dE6d.png)
* In IntelliJ, go to `File > Open`
* [Navigate to the repository and open the `pom.xml` file](http://i.imgur.com/zcVkyAm.png)
* [Look for and open the "Maven Projects" tab, expand "Titanic" and then "Lifecycle"](http://i.imgur.com/TB3Ab4T.png)
* [Double-click "Clean" and wait for the process to finish. This will ensure there are no left-over
files from previous Maven builds that may interfere with the final build.](http://i.imgur.com/Lx5yPdc.png)
* Double-click "Package" and wait for the process to finish
* [Built jar file will be located in the new `target` directory](http://i.imgur.com/bDGVDwW.png)

# Debugging

These instructions are for running and debugging Titanic from within your development environment.
These will help you debug Titanic and reload code changes as it runs. [Each of these steps assumes
you have a Bukkit/Spigot/PaperSpigot server locally installed.](http://i.imgur.com/q0B28cR.png)

## IntelliJ

1. [Clone this repository using your git client](http://i.imgur.com/VB7dE6d.png)
* In IntelliJ, go to `File > Open`
* [Navigate to the repository and open the `pom.xml` file](http://i.imgur.com/zcVkyAm.png)
* Go to `File > Project Structure... > Artifacts`
* [Click `Add > JAR > Empty`, then configure as such:](http://i.imgur.com/kXsbr3C.png)
    * Set Name to "Titanic"
    * Set Output directory to the "plugins" folder of your local server
    * Check "Build on make"
* Right-click "'Titanic' compile output" and then click "Put into Output Root", then click OK
* Go to `Run > Edit Configurations...`
* [Click `Add New Configuration > JAR Application`, then configure as such:](http://i.imgur.com/smuYOFs.png)
    * Set Name to "Server" (or "Spigot" or "PaperSpigot", etc)
    * Set Path to JAR to the full path of your local server's executable JAR
        * e.g. `C:\Users\TitanicDev\AppData\Local\Programs\Spigot\spigot-1.10.2.jar`
    * Set VM options to "-Xmx2G" (allocates 2GB RAM)
    * Set Working directory to the full path of your local server
        * e.g. `C:\Users\TitanicDev\AppData\Local\Programs\Spigot\`
    * Checkmark "Single instance only" on the top right corner
* Under "Before launch", click `Add New Configuration > Build Artifacts`
* Check "Titanic" and then click OK twice

After setting up IntelliJ for debugging, all you need to do is press SHIFT+F9 to begin debugging.
This will automatically build a jar, put it in your local server's plugins folder and then start
your server automatically.

## Debug logging

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
