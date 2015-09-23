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

package ru.trett.vkapi.Exceptions;

/**
 * @author Roman Tretyakov
 * @since 23.09.15
 */
public class TokenErrorException extends Exception {

    public TokenErrorException() {
        super();
    }

    public TokenErrorException(String message) {
        super(message);
    }

    public TokenErrorException(String message, Throwable cause) {
        super(message, cause);
    }

    public TokenErrorException(Throwable cause) {
        super(cause);
    }

}
