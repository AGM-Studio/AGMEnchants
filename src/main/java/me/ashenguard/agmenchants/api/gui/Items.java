package me.ashenguard.agmenchants.api.gui;

public enum Items {
    TopBorder,
    BottomBorder,
    LeftButton,
    RightButton;

    private String path;
    private String ID = "Stone";
    private String value = null;
    private short data = 0;

    Items() {
        this.path = "GUI." + this.name();
    }

    Items(String path) {
        this.path = path;
    }

    Items(String path, String ID, String value) {
        this.path = path;
        this.ID = ID;
        this.value = value;
    }

    Items(String path, String ID, short data) {
        this.path = path;
        this.ID = ID;
        this.data = data;
    }

    public String getPath() {
        return path;
    }

    public String getID() {
        return ID;
    }

    public String getValue() {
        return value;
    }

    public short getData() {
        return data;
    }
}
