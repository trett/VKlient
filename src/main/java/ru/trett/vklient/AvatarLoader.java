package ru.trett.vklient;

/**
 * @author Roman Tretyakov
 * @since 15.08.2015
 */

import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.IOException;

public class AvatarLoader {
    //TODO: Make a cache for images
    public static Node getImageFromUrl(String url) throws IOException {
        ImageView avatar = new ImageView(new Image(url));
        avatar.setFitWidth(32);
        avatar.setPreserveRatio(true);
        avatar.setSmooth(true);
        avatar.setCache(true);
        return avatar;
    }

}
