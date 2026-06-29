package com.datacleanpro.exception;

/**
 * 网络异常类
 * 当网络通信失败时抛出
 */
public class NetworkException extends DataCleanException {
    private String host;
    private int port;
    private String operation;

    public NetworkException(String message) {
        super("NETWORK_ERROR", message);
    }

    public NetworkException(String message, Throwable cause) {
        super("NETWORK_ERROR", message, cause);
    }

    public NetworkException(String message, String host, int port, String operation) {
        super("NETWORK_ERROR", message);
        this.host = host;
        this.port = port;
        this.operation = operation;
    }

    public NetworkException(String message, Throwable cause, String host, int port, String operation) {
        super("NETWORK_ERROR", message, cause);
        this.host = host;
        this.port = port;
        this.operation = operation;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    @Override
    public String toString() {
        return "NetworkException{" +
                "host='" + host + '\'' +
                ", port=" + port +
                ", operation='" + operation + '\'' +
                ", errorCode='" + getErrorCode() + '\'' +
                ", errorMessage='" + getErrorMessage() + '\'' +
                '}';
    }
}
