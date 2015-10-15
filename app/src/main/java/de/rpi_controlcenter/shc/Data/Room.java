package de.rpi_controlcenter.shc.Data;

/**
 * Created by oliver on 04.10.15.
 */
public class Room {

    private int id = 0;

    private String name = "";

    public Room() {}

    public Room(int id, String name) {

        this.id = id;
        this.name = name;
    }

    public int getId() {

        return id;
    }

    public void setId(int id) {

        this.id = id;
    }

    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }
}
