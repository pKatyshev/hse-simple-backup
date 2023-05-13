package ru.katyshev;

public class Main {

    public static void main(String[] args) {
        Backup backup = new Backup();
        PropertiesLoader loader = new PropertiesLoader(backup, args[0]);
        backup.setPropertiesLoader(loader);
        backup.start();
    }
}