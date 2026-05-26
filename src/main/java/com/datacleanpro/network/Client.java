package com.datacleanpro.network;

import com.datacleanpro.exception.NetworkException;
import com.datacleanpro.util.ConfigUtil;
import com.datacleanpro.util.LogUtil;

import java.io.*;
import java.net.Socket;

/**
 * 客户端类
 * 与服务器进行通信
 */
public class Client {
    
    private Socket socket;
    private String host;
    private int port;
    
    public Client() {
        this.host = ConfigUtil.getServerHost();
        this.port = ConfigUtil.getServerPort();
    }
    
    public Client(String host, int port) {
        this.host = host;
        this.port = port;
    }
    
    /**
     * 连接到服务器
     * @throws NetworkException 网络异常
     */
    public void connect() throws NetworkException {
        try {
            socket = new Socket(host, port);
            LogUtil.info("连接到服务器: " + host + ":" + port);
        } catch (IOException e) {
            throw new NetworkException("连接服务器失败", e, host, port, "CONNECT");
        }
    }
    
    /**
     * 断开连接
     */
    public void disconnect() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
                LogUtil.info("断开服务器连接");
            }
        } catch (IOException e) {
            LogUtil.error("断开连接失败", e);
        }
    }
    
    /**
     * 发送任务请求
     * @param request 任务请求
     * @return 任务响应
     * @throws NetworkException 网络异常
     */
    public TaskResponse sendRequest(TaskRequest request) throws NetworkException {
        if (socket == null || socket.isClosed()) {
            connect();
        }
        
        try {
            // 发送请求
            ProtocolUtil.sendObject(socket, request);
            LogUtil.info("发送请求: " + request);
            
            // 接收响应
            TaskResponse response = ProtocolUtil.receiveObject(socket, TaskResponse.class);
            LogUtil.info("收到响应: " + response);
            
            return response;
        } catch (NetworkException e) {
            LogUtil.error("发送请求失败", e);
            throw e;
        }
    }
    
    /**
     * 发送文件导入请求
     * @param fileName 文件名
     * @param fileType 文件类型
     * @param fileData 文件数据
     * @return 任务响应
     * @throws NetworkException 网络异常
     */
    public TaskResponse sendImportRequest(String fileName, String fileType, byte[] fileData) throws NetworkException {
        TaskRequest request = new TaskRequest("IMPORT");
        request.setFileName(fileName);
        request.setFileType(fileType);
        request.setFileData(fileData);
        return sendRequest(request);
    }
    
    /**
     * 发送数据清洗请求
     * @param fileId 文件ID
     * @return 任务响应
     * @throws NetworkException 网络异常
     */
    public TaskResponse sendCleanRequest(Long fileId) throws NetworkException {
        TaskRequest request = new TaskRequest("CLEAN");
        request.setParameters(String.valueOf(fileId));
        return sendRequest(request);
    }
    
    /**
     * 发送数据验证请求
     * @param fileId 文件ID
     * @return 任务响应
     * @throws NetworkException 网络异常
     */
    public TaskResponse sendValidateRequest(Long fileId) throws NetworkException {
        TaskRequest request = new TaskRequest("VALIDATE");
        request.setParameters(String.valueOf(fileId));
        return sendRequest(request);
    }
    
    /**
     * 发送数据导出请求
     * @param fileId 文件ID
     * @return 任务响应
     * @throws NetworkException 网络异常
     */
    public TaskResponse sendExportRequest(Long fileId) throws NetworkException {
        TaskRequest request = new TaskRequest("EXPORT");
        request.setParameters(String.valueOf(fileId));
        return sendRequest(request);
    }
    
    /**
     * 检查是否已连接
     * @return 是否已连接
     */
    public boolean isConnected() {
        return socket != null && !socket.isClosed();
    }
    
    /**
     * 获取服务器主机
     * @return 主机
     */
    public String getHost() {
        return host;
    }
    
    /**
     * 获取服务器端口
     * @return 端口
     */
    public int getPort() {
        return port;
    }
}
