package com.hejiale.common.exception;

/**
 * 登录失败
 */
public class AccountException extends BaseException{
    public AccountException() {
    }
    public AccountException(String msg){
        super(msg);
    }
}
