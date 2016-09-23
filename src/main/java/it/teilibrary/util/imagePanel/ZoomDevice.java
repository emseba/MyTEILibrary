package it.teilibrary.util.imagePanel;

/**
 * <p>
 * Defines zoom devices.</p>
 */
public class ZoomDevice {

    /**
     * <p>
     * Identifies that the panel does not implement zooming, but the component
     * using the panel does (programmatic zooming method).</p>
     */
    public static ZoomDevice NONE = new ZoomDevice("none");
    /**
     * <p>
     * Identifies the left and right mouse buttons as the zooming device.</p>
     */
    public static ZoomDevice MOUSE_BUTTON = new ZoomDevice("mouseButton");
    /**
     * <p>
     * Identifies the mouse scroll wheel as the zooming device.</p>
     */
    public static ZoomDevice MOUSE_WHEEL = new ZoomDevice("mouseWheel");
    private final String zoomDevice;

    private ZoomDevice(String zoomDevice) {
        this.zoomDevice = zoomDevice;
    }

    @Override
    public String toString() {
        return zoomDevice;
    }
}
