/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.zhining.barcodescanner.client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author billzhu
 */
public class MultiCastSender {
    private final String multicast_ip="226.1.3.5";//广播地址
    private final int multicast_port=7777;//广播端口
    
    private int clientPortUPD=456789;
    
    private String localIp;
    private String hostName;
    
    DatagramSocket clientUdpSocket;
    MulticastSocket ms;
    InetAddress group;
    
    private static final String cmd_find="find";
    private static final String sperator="@";
    
    public MultiCastSender(){
        try {
            InetAddress localAddress=InetAddress.getLocalHost();
            group=InetAddress.getByName(multicast_ip);
            hostName=localAddress.getHostName().toString();
            localIp=localAddress.getHostAddress().toString();
            //建立数据据套接字 ,基于UDP
//            senderSocket=new DatagramSocket();
            clientUdpSocket=new MulticastSocket();
            Logger.getLogger(MultiCastSender.class.getName()).log(Level.INFO,
                    "sender 构造 :"+localIp+sperator
                            +getPort()+sperator
                            +hostName);
        } catch (UnknownHostException ex) {
            //本地主机没有地址ip
            Logger.getLogger(MultiCastSender.class.getName()).log(Level.SEVERE, "本地主机没有地址ip", ex);
        }catch (SocketException ex) {
            //DatagramSocket不能打开或不能绑定某个端口
            Logger.getLogger(MultiCastSender.class.getName()).log(Level.SEVERE, "不能打开或不能绑定某个端口", ex);
        } catch (IOException ex) {
            Logger.getLogger(MultiCastSender.class.getName()).log(Level.SEVERE, null, ex);
        } 
    }
    public int getPort(){
        return clientUdpSocket.getPort();
    }
    public boolean send(Object... datas){
        boolean success=true;
        byte[] bytes;//=new byte[1024];
        StringBuilder sb=new StringBuilder();
        for (Object data : datas) {
            sb.append(data);
            if(!datas[datas.length-1].equals(data))
                sb.append(sperator);
        }
        bytes=sb.toString().getBytes();
        Logger.getLogger(MultiCastSender.class.getName()).log(Level.INFO, sb.toString());
        DatagramPacket sendBoardCastPacket;
        sendBoardCastPacket=new DatagramPacket(bytes, bytes.length, group, multicast_port);
        
        DatagramPacket receivePacket=new DatagramPacket(new byte[512],512);
        try {
            clientUdpSocket.send(sendBoardCastPacket);
            clientUdpSocket.receive(receivePacket);
            InetAddress serverAddress= receivePacket.getAddress();
            Logger.getLogger(MultiCastSender.class.getName()).log(Level.INFO,
                    "服务器地址 "+serverAddress.getHostAddress());
        } catch (IOException ex) {
            success=false;
            Logger.getLogger(MultiCastSender.class.getName()).log(Level.SEVERE, null, ex);
        }
        return success;
    }
    public static void main(String [] args){
        MultiCastSender sender=new MultiCastSender();
        sender.send(cmd_find,
                sender.localIp,
                sender.getPort(),
                sender.hostName);
//        sender.send(cmd_find,sender.localIp,sender.hostName);
//        sender.send(cmd_find,sender.localIp,sender.hostName);
    }
}
