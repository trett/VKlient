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

package ru.trett.vkapi.Exceptions;

/**
 * @author Roman Tretyakov
 * @since 15.08.2015
 */

public class RequestReturnNullException extends Exception {

    public RequestReturnNullException() {
        super();
    }

    public RequestReturnNullException(String message) {
        super(message);
    }

    public RequestReturnNullException(String message, Throwable cause) {
        super(message, cause);
    }

    public RequestReturnNullException(Throwable cause) {
        super(cause);
    }
}
