/*
 *  *
 *  @author Roman Tretyakov
 *  @since 15.08.2015
 *
 */

package ru.trett.vkauth;

/**
 * Created by maat on 26.08.15.
 */
public class RequestReturnNullException extends Exception{

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
