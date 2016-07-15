/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.zhining.barcodescanner.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Administrator
 */
public class Server {
    //228.9.6.8
    //192.168.1.255
    private final String multicast_ip="226.1.3.5";
    private final int multicast_port=7777;
    
    private String localIp;
    private String hostName;
    
    private static final String cmd_find="find";
    private static final String sperator="@";
    
    MulticastSocket multicastSocket;
    InetAddress multicastAddress;
    Thread listenerThread;
    public Server(){
        try {
            InetAddress addr=InetAddress.getLocalHost();
            localIp=addr.getHostAddress().toString();
            hostName=addr.getHostName().toString();
            multicastAddress=InetAddress.getByName(multicast_ip);
            multicastSocket=new MulticastSocket(multicast_port);
            multicastSocket.setTimeToLive(1);
            Logger.getLogger(Server.class.getName()).log(Level.INFO,"Server构造完成..."
            +localIp+"@"+hostName);
        }catch(UnknownHostException ex){
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
    }
    public void startListener(){
        try {
            multicastSocket.joinGroup(multicastAddress);
            Logger.getLogger(Server.class.getName()).log(Level.INFO,"Server所在本机加入广播组..."+multicastAddress);
            listenerThread=new Thread(new GetClientRunnable());
            listenerThread.start();
            Logger.getLogger(Server.class.getName()).log(Level.INFO,"开始线程");
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        
    }
    private void tellClientMyIP(String clientIP){
        
    }
    //指令@参数1@参数2...
    private void cmdExcute(DatagramPacket packet,String ... args){
        String cmd;
        if (args.length==0) return ;
        switch (args[0]){
            case cmd_find:// find@客户端ip地址@端口@客户端主机名
                cmd=args[0];//指令
                String newIP=args[1];//客户端IP
                String clientPort=args[2];//客户端端口
                String hostName=args[3];//客户主机名
                Logger.getLogger(Server.class.getName()).log(Level.INFO, "发现新客户端:"
                        +newIP+sperator
                        +clientPort+sperator
                        +hostName);
                byte[] data="server".getBytes();
                DatagramPacket tellClientIamServer=new DatagramPacket(data, data.length,packet.getAddress(),packet.getPort());
                try {
                    multicastSocket.send(tellClientIamServer);
                } catch (IOException ex) {
                    Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                }
                
                break;
            default:
                Logger.getLogger(Server.class.getName()).log(Level.INFO, "未知识指令");
                //nothing to do;
        }
    }
    public static void main(String [] args){
        Server server=new Server();
        server.startListener();
//        try {
//            Thread.sleep(100000);
//        } catch (InterruptedException ex) {
//            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
//        }
    }
    class GetClientRunnable implements Runnable{

        @Override
        public void run() {
            DatagramPacket inPacket;
            String[] args;
            Logger.getLogger(Server.class.getName()).log(Level.INFO,"开始监听客户端发出的广播...");
            while(true){
                inPacket=new DatagramPacket(new byte[1024],1024);
                try {
                    multicastSocket.receive(inPacket);//exception haddle
                    int clientPort= inPacket.getPort();
                    Logger.getLogger(Server.class.getName()).log(Level.INFO,
                            "收到一个广播 packet 端口"+clientPort);
                    String orginal=new String(inPacket.getData(),0,inPacket.getLength());
                    args=orginal.split(sperator);
                    Logger.getLogger(Server.class.getName()).log(Level.INFO,
                            "收到一个广播..."+orginal);
                    cmdExcute(inPacket,args);
                } catch (IOException ex) {
                    Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        
    }//runnable class end
}
