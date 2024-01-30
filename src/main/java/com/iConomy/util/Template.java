package com.iConomy.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.logging.Logger;

import com.iConomy.iConomy;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

public class Template {
    private YamlConfiguration tpl = null;

    File file = null;
    Logger log = iConomy.instance.getLogger();

    public Template(String directory, String filename) {
        this.tpl = new YamlConfiguration();
        try {
            file = new File(directory, filename);
            this.tpl.load(file);
            upgrade(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidConfigurationException e) {
            e.printStackTrace();
        }

    }

    public void upgrade(File file) throws IOException {
        LinkedHashMap<String, String> nodes = new LinkedHashMap<String, String>();
        if (this.tpl.getString("interest.announcement") == null) {
            nodes.put("interest.announcement", "+amount <green>interest gained.");
        }

        if (!nodes.isEmpty()) {
            log.info(" - Upgrading Template.yml");
            int count = 1;

            for (String node : nodes.keySet()) {
                log.info("   Adding node [" + node + "] #" + count + " of " + nodes.size());
                this.tpl.set(node, nodes.get(node));
                count++;
            }

            this.tpl.save(file);
            log.info(" + Messages Upgrade Complete.");
        }
    }

    /**
     * Grab the raw template line by the key, and don't save anything.
     *
     * @param key The template key we wish to grab.
     *
     * @return <code>String</code> - Template line / string.
     */
    public String raw(String key) {
        return this.tpl.getString(key);
    }

    /**
     * Grab the raw template line and save data if no key existed.
     *
     * @param key The template key we are searching for.
     * @param line The line to be placed if no key was found.
     * 
     * @return
     */
    public String raw(String key, String line) {
        return this.tpl.getString(key, line);
    }

    public void save(String key, String line) throws IOException {
        this.tpl.set(key, line);
        this.tpl.save(file);
    }

    public String color(String key) {
        return Messaging.parse(Messaging.colorize(raw(key)));
    }

    public String parse(String key, Object[] argument, Object[] points) {
        return Messaging.parse(Messaging.colorize(Messaging.argument(raw(key), argument, points)));
    }

    public String parse(String key, String line, Object[] argument, Object[] points) {
        return Messaging.parse(Messaging.colorize(Messaging.argument(raw(key, line), argument, points)));
    }
}
