/*
 * (C) Copyright Tretyakov Roman.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 */

package ru.trett.vklient;

import javafx.application.Platform;
import org.controlsfx.control.Notifications;

/**
 * @author Roman Tretyakov
 * @since 03.09.15
 */
public class NotificationProvider {

    public static void showNotification(String from) {
        Platform.runLater(() -> Notifications.create().title("New Message from").text(from).show());
    }
}
