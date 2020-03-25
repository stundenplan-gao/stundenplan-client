package util;

import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Insets;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.Rectangle2D;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

import javax.swing.ImageIcon;

public class GUIUtil {

    /**
     * automatically install application icons for every new window.
     */
    public static void installApplicationIcons() {
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addPropertyChangeListener("activeWindow", evt -> {
            final Window window = KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow();
            if (window != null) {
                setWindowIcons(window, "stundenplan");
            }
        });
    }

    /**
     * Try to restore previously saved windows bounds for this window and on failure
     * use the given defaults. If the defaults are used the window will be centered
     * on the screen. Finally install listeners, which will save changes to the
     * window bounds in the user section under the given preference path using the
     * Java Preference API {@link Preferences}.
     *
     * @param mainWindow
     * @param preferencePath
     * @param defaultWidth
     * @param defaultHeight
     */
    public static void installBoundsPersistence(final Window mainWindow, final String preferencePath,
            final int defaultWidth, final int defaultHeight) {
        final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        final int defaultX = (screenSize.width - defaultWidth) / 2;
        final int defaultY = (screenSize.height - defaultHeight) / 2;
        installBoundsPersistence(mainWindow, preferencePath, defaultX, defaultY, defaultWidth, defaultHeight);
    }

    /**
     * Try to restore previously saved windows bounds for this window and on failure
     * use the given defaults. Finally install listeners, which will save changes to
     * the window bounds in the user section under the given preference path using
     * the Java Preference API {@link Preferences}.
     *
     * @param mainWindow
     * @param preference
     * @param defaultX
     * @param defaultY
     * @param defaultWidth
     * @param defaultHeight
     */
    public static void installBoundsPersistence(final Window mainWindow, final String preference, final int defaultX,
            final int defaultY, final int defaultWidth, final int defaultHeight) {

        final String preferencePath = "/gao/stundenplan/" + preference;
        int x = defaultX;
        int y = defaultY;
        int w = defaultWidth;
        int h = defaultHeight;
        try {
            if (Preferences.userRoot().nodeExists(preferencePath)) {
                final Preferences node = Preferences.userRoot().node(preferencePath);
                x = node.getInt("x", x);
                y = node.getInt("y", y);
                w = node.getInt("w", w);
                h = node.getInt("h", h);
            }
        } catch (final Exception e1) {
            // use defaults
        }
        setWindowBounds(mainWindow, x, y, w, h);

        // handle main window sizing and moving
        mainWindow.addComponentListener(new ComponentAdapter() {

            @Override
            public void componentMoved(final ComponentEvent e) {
                final Preferences node = Preferences.userRoot().node(preferencePath);
                node.putInt("x", e.getComponent().getX());
                node.putInt("y", e.getComponent().getY());
            }

            @Override
            public void componentResized(final ComponentEvent e) {
                final Preferences node = Preferences.userRoot().node(preferencePath);
                node.putInt("w", e.getComponent().getWidth());
                node.putInt("h", e.getComponent().getHeight());
            }
        });

    }

    private static final int screenMinWidth = 5;

    private static final int screenMinHeight = 5;

    private static final int windowMinWidth = 20;

    private static final int windowMinHeight = 20;

    private static final boolean enforceWindowSize = true;

    private static boolean validateScreenSize(Rectangle virtualBounds) {
        return virtualBounds.width >= screenMinWidth && virtualBounds.height >= screenMinHeight;
    }

    private static boolean validateWindowSize(Rectangle virtualBounds) {
        return virtualBounds.width >= windowMinWidth && virtualBounds.height >= windowMinHeight;
    }

    /**
     * Set window bounds with checking, whether they can possibly be set on this
     * screen.
     *
     * @param mainWindow
     * @param x
     * @param y
     * @param width
     * @param height
     */
    public static void setWindowBounds(final Window mainWindow, int x, int y, int width, int height) {
        GraphicsConfiguration gc = mainWindow.getGraphicsConfiguration();
        Rectangle virtualBounds = deriveVirtualBounds(gc);

        if (gc == null || virtualBounds == null || !validateScreenSize(virtualBounds)) {
            final GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            final GraphicsDevice sd = ge.getDefaultScreenDevice();
            gc = sd.getDefaultConfiguration();
            virtualBounds = deriveVirtualBounds(gc);

            if (gc == null || virtualBounds == null || !validateScreenSize(virtualBounds)) {
                return;
            }
        }

        // normalize (just in case)
        if (width < 0) {
            x += width;
            width = -width;
        }
        if (height < 0) {
            y += height;
            height = -height;
        }
        final Rectangle windowBounds = new Rectangle(x, y, width, height);

        // make window fit into the screen
        if (windowBounds.width > virtualBounds.width) {
            windowBounds.width = virtualBounds.width;
        }
        if (windowBounds.height > virtualBounds.height) {
            windowBounds.height = virtualBounds.height;
        }

        // move (if necessary) upper left corner into screen rectangle
        int outcode = virtualBounds.outcode(windowBounds.getLocation());
        if ((outcode & Rectangle2D.OUT_LEFT) != 0) {
            windowBounds.x = virtualBounds.x;
        }
        if ((outcode & Rectangle2D.OUT_TOP) != 0) {
            windowBounds.y = virtualBounds.y;
        }

        // finally move (if necessary) lower right corner into screen rectangle
        final Point location = windowBounds.getLocation();
        location.translate(windowBounds.width, windowBounds.height);
        outcode = virtualBounds.outcode(location);
        int dx = 0;
        int dy = 0;
        if ((outcode & Rectangle2D.OUT_RIGHT) != 0) {
            dx = virtualBounds.x + virtualBounds.width - windowBounds.x - windowBounds.width;
        }
        if ((outcode & Rectangle2D.OUT_BOTTOM) != 0) {
            dy = virtualBounds.y + virtualBounds.height - windowBounds.y - windowBounds.height;
        }
        windowBounds.translate(dx, dy);

        // done
        if (!validateWindowSize(windowBounds)) {
            if (!enforceWindowSize) {
                return;
            }
            windowBounds.width = windowMinWidth;
            windowBounds.height = windowMinHeight;
        }
        mainWindow.setBounds(windowBounds);
    }

    protected static Rectangle deriveVirtualBounds(GraphicsConfiguration gc) {
        if (gc == null) {
            return null;
        }
        // I assume gc.getBounds() is normalized
        final Rectangle virtualBounds = gc.getBounds();
        if (virtualBounds == null) {
            return null;
        }

        final Insets screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(gc);
        if (screenInsets != null) {
            virtualBounds.x += screenInsets.left;
            virtualBounds.y += screenInsets.top;
            virtualBounds.width -= screenInsets.left + screenInsets.right;
            virtualBounds.height -= screenInsets.top + screenInsets.bottom;
        }
        return virtualBounds;
    }

    private static void addImage(final List<ImageIcon> images, final String name) {
        try {
            final URL resource = GUIUtil.class.getResource("/" + name);
            if (resource == null) {
                return;
            }
            images.add(new ImageIcon(resource));

        } catch (final Throwable th) {
            // ignored, because not resolution must be provided
        }
    }

    /**
     * @param frame
     * @param baseName
     */
    public static void setWindowIcons(final Window frame, final String baseName) {
        if (frame == null) {
            return;
        }

        final List<ImageIcon> iconImages = new ArrayList<>();
        addImage(iconImages, baseName + "-16x16.png");
        addImage(iconImages, baseName + "-24x24.png");
        addImage(iconImages, baseName + "-32x32.png");
        addImage(iconImages, baseName + "-48x48.png");
        addImage(iconImages, baseName + "-64x64.png");
        addImage(iconImages, baseName + "-128x128.png");

        if (iconImages.size() == 0) {
            System.err.println("no icons found");
            return;
        }

        List<Image> images = iconImages.stream().map(ImageIcon::getImage).collect(Collectors.toList());
        frame.setIconImages(images);
    }
}