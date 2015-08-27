/*
 *  *
 *  @author Roman Tretyakov
 *  @since 15.08.2015
 *
 */

package ru.trett.vklient;

import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * Created by maat on 27.08.15.
 */
public class IconLoader {

    private ClassLoader classLoader;

    IconLoader() {
           classLoader = getClass().getClassLoader();
    }

    public Node getIcon(String iconName, int width) {
        ImageView icon = new ImageView(
                new Image(classLoader.getResourceAsStream(iconName + ".png"))
        );
        icon.setFitWidth(width);
        icon.setPreserveRatio(true);
        icon.setSmooth(true);
        icon.setCache(true);
        return icon;
    }

    public static Node getImageFromUrl(String url) {
        ImageView image = new ImageView(new Image(url));
        image.setFitWidth(32);
        image.setPreserveRatio(true);
        image.setSmooth(true);
        image.setCache(true);
        return image;
    }

}
