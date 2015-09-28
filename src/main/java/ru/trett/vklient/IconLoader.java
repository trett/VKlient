/*
 * (C) Copyright Tretyakov Roman.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 */

package ru.trett.vklient;

import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * @author Roman Tretyakov
 * @since 15.08.2015
 */

public class IconLoader {

    private ClassLoader classLoader;

    IconLoader() {
        classLoader = getClass().getClassLoader();
    }

    /**
     * Get image bu URI
     *
     * @param url String URI
     * @return Node Graphic
     */
    public Node getImageFromUrl(String url) {
        ImageView image = new ImageView(new Image(url));
        image.setFitWidth(32);
        image.setPreserveRatio(true);
        image.setSmooth(true);
        image.setCache(true);
        return image;
    }

    /**
     * Get icon my name and resize to given width
     *
     * @param iconName Icon name without format
     * @param width    Preferred with
     * @return Node Graphic
     */
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

}
