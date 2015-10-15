package de.rpi_controlcenter.shc.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * Stellt die daten eines Raum Elements zur verfügung
 */
public class RoomElement {

    private String type = "";

    private String id = "";

    private String name = "";

    private String icon = "";

    private int state = 0;

    private Map<String, String> additionalData = new HashMap<>();

    public RoomElement() {

    }

    public RoomElement(String type, String id, String name) {

        this.type = type;
        this.id = id;
        this.name = name;
    }

    public RoomElement(String type, String id, String name, String icon, int state) {

        this.type = type;
        this.id = id;
        this.name = name;
        this.icon = icon;
        this.state = state;
    }

    public String getType() {

        return type;
    }

    public void setType(String type) {

        this.type = type;
    }

    public String getId() {

        return id;
    }

    public void setId(String id) {

        this.id = id;
    }

    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }

    public String getIcon() {

        return icon;
    }

    public void setIcon(String icon) {

        this.icon = icon;
    }

    public int getState() {

        return state;
    }

    public void setState(int state) {

        this.state = state;
    }

    public boolean containsData(String key) {

        return this.additionalData.containsKey(key);
    }

    public String getData(String key) {

        return this.additionalData.get(key);
    }

    public void addData(String key, String value) {

        this.additionalData.put(key, value);
    }

    public void removeData(String key) {

        this.additionalData.remove(key);
    }

    public Map<String, String> getAdditionalData() {

        return additionalData;
    }

    @Override
    public String toString() {

        return "[Room Element \"" + getName() + "\" - " + getType() + "]";
    }
}
