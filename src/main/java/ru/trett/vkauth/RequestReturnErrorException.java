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
public class RequestReturnErrorException extends Exception{

    public RequestReturnErrorException() {
        super();
    }

    public RequestReturnErrorException(String message) {
        super(message);
    }

    public RequestReturnErrorException(String message, Throwable cause) {
        super(message, cause);
    }

    public RequestReturnErrorException(Throwable cause) {
        super(cause);
    }
}
