package com.datacleanpro.network;

import com.datacleanpro.exception.NetworkException;
import com.datacleanpro.service.DataImportService;
import com.datacleanpro.util.ConfigUtil;
import com.datacleanpro.util.LogUtil;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 服务器类
 * 处理客户端连接和任务分发
 */
public class Server {
    
    private ServerSocket serverSocket;
    private ExecutorService threadPool;
    private boolean running;
    private int port;
    private DataImportService importService;
    
    public Server() {
        this.port = ConfigUtil.getServerPort();
        this.threadPool = Executors.newFixedThreadPool(ConfigUtil.getThreadPoolSize());
        this.importService = new DataImportService();
    }
    
    public Server(int port) {
        this.port = port;
        this.threadPool = Executors.newFixedThreadPool(ConfigUtil.getThreadPoolSize());
        this.importService = new DataImportService();
    }
    
    /**
     * 启动服务器
     */
    public void start() {
        try {
            serverSocket = new ServerSocket(port);
            running = true;
            LogUtil.info("服务器启动成功，端口: " + port);
            
            // 接受客户端连接
            while (running) {
                Socket clientSocket = serverSocket.accept();
                LogUtil.info("客户端连接: " + clientSocket.getInetAddress().getHostAddress());
                
                // 使用线程池处理客户端请求
                threadPool.submit(() -> handleClient(clientSocket));
            }
        } catch (IOException e) {
            LogUtil.error("服务器启动失败", e);
            throw new NetworkException("服务器启动失败", e, "localhost", port, "START");
        }
    }
    
    /**
     * 停止服务器
     */
    public void stop() {
        running = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            if (threadPool != null) {
                threadPool.shutdown();
            }
            LogUtil.info("服务器已停止");
        } catch (IOException e) {
            LogUtil.error("停止服务器失败", e);
        }
    }
    
    /**
     * 处理客户端请求
     * @param clientSocket 客户端Socket
     */
    private void handleClient(Socket clientSocket) {
        try {
            // 接收请求
            TaskRequest request = ProtocolUtil.receiveObject(clientSocket, TaskRequest.class);
            LogUtil.info("收到请求: " + request);
            
            // 处理请求
            TaskResponse response = processRequest(request);
            
            // 发送响应
            ProtocolUtil.sendObject(clientSocket, response);
            LogUtil.info("发送响应: " + response);
            
        } catch (NetworkException e) {
            LogUtil.error("处理客户端请求失败", e);
            try {
                TaskResponse errorResponse = new TaskResponse();
                errorResponse.setSuccess(false);
                errorResponse.setMessage(e.getMessage());
                ProtocolUtil.sendObject(clientSocket, errorResponse);
            } catch (NetworkException ex) {
                LogUtil.error("发送错误响应失败", ex);
            }
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                LogUtil.error("关闭客户端连接失败", e);
            }
        }
    }
    
    /**
     * 处理请求
     * @param request 请求对象
     * @return 响应对象
     */
    private TaskResponse processRequest(TaskRequest request) {
        TaskResponse response = new TaskResponse();
        response.setRequestId(request.getRequestId());
        
        try {
            switch (request.getAction()) {
                case "IMPORT":
                    // 处理文件导入请求
                    response.setSuccess(true);
                    response.setMessage("文件导入请求已接收");
                    response.setProgress(100);
                    break;
                    
                case "CLEAN":
                    // 处理数据清洗请求
                    response.setSuccess(true);
                    response.setMessage("数据清洗请求已接收");
                    response.setProgress(100);
                    break;
                    
                case "VALIDATE":
                    // 处理数据验证请求
                    response.setSuccess(true);
                    response.setMessage("数据验证请求已接收");
                    response.setProgress(100);
                    break;
                    
                case "EXPORT":
                    // 处理数据导出请求
                    response.setSuccess(true);
                    response.setMessage("数据导出请求已接收");
                    response.setProgress(100);
                    break;
                    
                default:
                    response.setSuccess(false);
                    response.setMessage("未知的操作: " + request.getAction());
                    break;
            }
        } catch (Exception e) {
            response.setSuccess(false);
            response.setMessage("处理请求失败: " + e.getMessage());
            LogUtil.error("处理请求失败", e);
        }
        
        return response;
    }
    
    /**
     * 检查服务器是否运行
     * @return 是否运行
     */
    public boolean isRunning() {
        return running;
    }
    
    /**
     * 获取服务器端口
     * @return 端口
     */
    public int getPort() {
        return port;
    }
}
