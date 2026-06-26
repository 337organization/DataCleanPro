package com.datacleanpro.network;

import com.datacleanpro.exception.NetworkException;
import com.datacleanpro.util.LogUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * 协议工具类
 * 处理网络通信的序列化和反序列化
 */
public class ProtocolUtil {
    
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * 发送对象到Socket
     * @param socket Socket连接
     * @param obj 要发送的对象
     * @throws NetworkException 网络异常
     */
    public static void sendObject(Socket socket, Object obj) throws NetworkException {
        try {
            byte[] json = objectMapper.writeValueAsBytes(obj);
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
            dos.writeInt(json.length);
            dos.write(json);
            dos.flush();
            LogUtil.debug("发送对象成功: " + obj.getClass().getSimpleName());
        } catch (JsonProcessingException e) {
            throw new NetworkException("序列化对象失败", e, 
                    socket.getInetAddress().getHostAddress(), socket.getPort(), "SEND");
        } catch (IOException e) {
            throw new NetworkException("发送数据失败", e, 
                    socket.getInetAddress().getHostAddress(), socket.getPort(), "SEND");
        }
    }
    
    /**
     * 从Socket接收对象
     * @param socket Socket连接
     * @param type 目标类型
     * @param <T> 对象类型
     * @return 接收到的对象
     * @throws NetworkException 网络异常
     */
    public static <T> T receiveObject(Socket socket, Class<T> type) throws NetworkException {
        try {
            DataInputStream dis = new DataInputStream(socket.getInputStream());
            int length = dis.readInt();
            byte[] json = new byte[length];
            dis.readFully(json);
            T obj = objectMapper.readValue(json, type);
            LogUtil.debug("接收对象成功: " + type.getSimpleName());
            return obj;
        } catch (JsonProcessingException e) {
            throw new NetworkException("反序列化对象失败", e, 
                    socket.getInetAddress().getHostAddress(), socket.getPort(), "RECEIVE");
        } catch (IOException e) {
            throw new NetworkException("接收数据失败", e, 
                    socket.getInetAddress().getHostAddress(), socket.getPort(), "RECEIVE");
        }
    }
    
    /**
     * 将对象转换为JSON字符串
     * @param obj 对象
     * @return JSON字符串
     */
    public static String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            LogUtil.error("对象转JSON失败", e);
            return "{}";
        }
    }
    
    /**
     * 将JSON字符串转换为对象
     * @param json JSON字符串
     * @param type 目标类型
     * @param <T> 对象类型
     * @return 对象
     */
    public static <T> T fromJson(String json, Class<T> type) {
        try {
            return objectMapper.readValue(json, type);
        } catch (JsonProcessingException e) {
            LogUtil.error("JSON转对象失败", e);
            return null;
        }
    }
}
