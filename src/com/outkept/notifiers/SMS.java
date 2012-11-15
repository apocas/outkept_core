package com.outkept.notifiers;

/**
 *
 * @author pedrodias
 */
public class SMS {

    public String msg;
    public String[] numbers;

    public SMS(String msg, String[] numbers) {
        this.msg = msg;
        this.numbers = numbers;
    }
}
