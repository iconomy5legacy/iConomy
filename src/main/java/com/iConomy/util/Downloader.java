package com.iConomy.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;

import org.bukkit.Bukkit;

public class Downloader {
    private static final Class<?>[] parameters = new Class[] { URL.class };

    protected int count;
    protected int total;
    protected int itemCount;
    protected int itemTotal;
    protected long lastModified;
    protected String error;

    public void install(String location, String filename) {
        File dest = new File("lib" + File.separator + filename);

        try {
            count = this.total = this.itemCount = this.itemTotal = 0;
            Bukkit.getLogger().info("[iConomy] Downloading Dependencies");
            Bukkit.getLogger().info("   + " + filename + " downloading...");
            download(location, dest);
            Bukkit.getLogger().info("   - " + filename + " finished.");
            
            this.addURLToClassLoader(dest.toURI().toURL());
            
        } catch (IOException ex) {
            Bukkit.getLogger().severe("[iConomy] Error Downloading File: " + ex);
        }
    }

    protected synchronized void download(String location, File destination) throws IOException {
        URLConnection connection = new URL(location).openConnection();
        connection.setUseCaches(false);
        lastModified = connection.getLastModified();
        File parentDirectory = destination.getParentFile();

        if (parentDirectory != null) {
            parentDirectory.mkdirs();
        }

        InputStream in = connection.getInputStream();
        OutputStream out = new FileOutputStream(destination);

        byte[] buffer = new byte[65536];
        Integer currentCount = 0;

        while (true) {
            int count = in.read(buffer);

            if (count < 0) {
                break;
            }
            out.write(buffer, 0, count);
            currentCount += count;
        }

        in.close();
        out.close();
    }

    public long getLastModified() {
        return lastModified;
    }

    //See: https://stackoverflow.com/a/1011126
    private void addURLToClassLoader(URL u) throws IOException {
        URLClassLoader sysloader = (URLClassLoader) this.getClass().getClassLoader();
        Class<?> sysclass = URLClassLoader.class;

        try {
            Method method = sysclass.getDeclaredMethod("addURL", parameters);
            method.setAccessible(true);
            method.invoke(sysloader, new Object[] { u });
            
        } catch (Throwable t) {
            t.printStackTrace();
            // Restart the server as we must be on Java 16+
            Bukkit.spigot().restart();
        }
    }
}
