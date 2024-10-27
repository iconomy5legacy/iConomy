### iConomy5 is now archived. For the same great plugin you've known all these years, check out [iConomyUnlocked](https://github.com/LlmDl/iConomyUnlocked)!

----
# iConomy 5

The original iConomy 5 plugin revamped and updated. Many people have found this plugin to be very stable, lasting the test of time with less than a dozen updates being required.

It uses an h2 database which installs itself with no setup required on your part. It has simple commands which can be aliased to anything you want using your server's [commands.yml](https://bukkit.fandom.com/wiki/Commands.yml) file. 

You will find that this plugin does not use UUIDs (*it's a really old plugin*) to track player/town/nation balances, for this reason you must use Towny in order for Towny to handle name changes.

Town and Nation bank accounts will not reset using this economy plugin.

#### Requirements:
This plugin requires Vault and Towny.

If you use Towny older than 0.96.5.2 you will also need TownyNameUpdater. 


#### Conversion:
Convert from EssentialsEconomy using `/icoimport` (run this from the console.)
  - This will convert player, town and nation accounts.

Convert from other economy plugins using the Vault command:
- Note that this will not convert town/nation accounts because Vault does not convert non-player accounts.

- `/vault-convert {economyplugin} iconomy5.11` (replace 5.11 with the iConomy version number.)

#### Commands:
![2024-08-27_12 38 41](https://github.com/user-attachments/assets/e05f8c49-df40-495a-ab02-8adcf5a5ec9d)


#### Permission Nodes:
Permission nodes can be gotten from the [plugin.yml](https://github.com/iconomy5legacy/iConomy/blob/master/src/main/resources/plugin.yml) They are already given out by default for the most part.
<br><br><br><br><br><br>



----

iConomy 5 was originally made by Nijikokun and has been forked by ElgarL as well as LlmDl.
