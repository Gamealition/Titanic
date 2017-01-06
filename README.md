Titanic is a Bukkit plugin for 1.9.4 and above. Its sole function is to try to fix [MC-91206]
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

# Building, debugging and debug logging

For instructions and screenshots on how to. . .

* Compile this plugin from scratch
* Build a JAR of this plugin
* Debug this plugin on a server
* Enable debug logging levels such as `FINE` and `FINER`

. . .[please follow the linked guide on this Google document.](https://docs.google.com/document/d/1TTDXG7IZ9M0D2-rzbILAWg1CKjynHK8fNGxbf3W4wBk/view)
