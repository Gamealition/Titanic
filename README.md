A Bukkit plugin intended to make boats a little less like spun sugar and more like,
well... a boat.

Originally coded by [Cyclometh](https://github.com/Cyclometh/), updated to Spigot 1.8.3
with changes by [Roy Curtis](https://github.com/RoyCurtis). Licensed under MIT.

## Building

1. Install Maven on your system
    * Windows: https://maven.apache.org/download.cgi
    * Linux: Install the `maven` package (e.g. `sudo apt-get install maven`)
2. Clone this repository into a folder
3. Inside the folder, execute `mvn clean package`
4. Look in the new `target` folder for the built JAR file

## Features

* Prevents boats from breaking unless they...
    * ...are hit by player
    * ...are in fire or lava
    * ...collide with a cactus
* Keeps boat positions in sync between client and server (fixes [MC-2931]
(https://bugs.mojang.com/browse/MC-2931))

## Commands

* `/toughboats reload` - Reloads the plugin and config.yml. Requires the permission
**toughboats.reload** or op.