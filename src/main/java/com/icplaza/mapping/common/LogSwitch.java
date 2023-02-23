package com.icplaza.mapping.common;

public class LogSwitch {
    public static boolean open;
    public static boolean pushLogOpen = false;
    public static boolean getOpen() {
        return open;
    }
    public static void setOpen(boolean _open) {
        open = _open;
    }
    public static boolean getPushLogOpen() {
        return pushLogOpen;
    }
    public static void setPushLogOpen(boolean _open) {
        pushLogOpen = _open;
    }
}
