import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;
import java.lang.System;
import java.util.concurrent.CountDownLatch;
public class PeerClient extends Thread{
    private int Peer_Num = -1;// 8001 should be 1 8002 should be 2 
    private int Chunk_Num = 0; //all trunk numbers 5
    private static final int[] peerList = new int[]{8001, 8002, 8003, 8004, 8005}; 
    public PeerClient(int peerNum, int chunkNum){ 
        Peer_Num = peerNum; 
        Chunk_Num = chunkNum; 
    } 

    public static void main(String argsp[]){ 
        PeerClient p = new PeerClient(1,2); 
        p.run(); 
    } 

    public void run(){ 
        CountDownLatch startSignal = new CountDownLatch(1);
        CountDownLatch doneSignal = new CountDownLatch(Chunk_Num-1);
        for(int i=0;i<Chunk_Num;i++){ 
            if(i+1==Peer_Num) continue; 
            new Handler(peerList[i],Peer_Num,startSignal, doneSignal).start();  
        } 
        startSignal.countDown();
        try{
            doneSignal.await();
        }catch(InterruptedException ex){}
        MergeChunk m = new MergeChunk();
        m.mergeParts(Peer_Num, Chunk_Num);
    } 

    private static class Handler extends Thread { 
        private int serverPort; 
        private int Peer_Num;
        private final CountDownLatch startSignal;
        private final CountDownLatch doneSignal;
        public Handler(int sPort,int pnum,CountDownLatch startSignal, CountDownLatch doneSignal){
            this.startSignal = startSignal;
            this.doneSignal = doneSignal;
            serverPort = sPort; 
            Peer_Num = pnum; 
        } 
        public void run(){
            try{
                try{
                    startSignal.await();
                }catch(InterruptedException ex){}
                System.out.println("connect to " + serverPort);  
                Socket requestSocket = null; 
                while(true){ 
                    try{ 
                        requestSocket = new Socket("localhost", serverPort);
                    }catch(ConnectException e){
                        System.err.println("Connection refused. You need to initiate a server first.");
                    }catch(UnknownHostException unknownHost){
                        System.err.println("You are trying to connect to an unknown host!"); 
                    } 

                        if(requestSocket == null){ 
                            System.out.println("connect to " + serverPort + "refused retry 5 seconds later");
                            try{
                                Thread.sleep(5000);
                            }catch(InterruptedException ex){
                            }
                            continue; 
                        } 
                        else { 
                            System.out.println("connect to " + serverPort + " successfully");
                            break;
                        } 
                    } 
                    while(true){
                        try{
                            Thread.sleep(200);
                        }catch(InterruptedException ex){ 
                        }
                        byte[] resultBuff = new byte[0];
                        byte[] buff = new byte[4096];
                        int k = -1;
                        BufferedInputStream in = new BufferedInputStream(requestSocket.getInputStream());

                        while((k = in.read(buff, 0, buff.length)) > -1){
                            byte[] tBuff = new byte[resultBuff.length + k];
                            //System.out.println("k size is: " + k);
                            //System.out.println("resultBuff.length is: " + resultBuff.length);
                            //System.out.println("buff.length " + buff.length);
                            System.arraycopy(resultBuff, 0, tBuff, 0, resultBuff.length);
                            System.arraycopy(buff, 0, tBuff, resultBuff.length, k);
                            resultBuff = tBuff;
                            //System.out.println("read size is: " + k);
                        } 

                        if(resultBuff.length>0){ 
                            System.out.println("begin write");
                            int fileBytes = 9;
                            byte[] fileNameByte = new byte[fileBytes];
                            int nextBytes = 0; //initialization 
                            System.arraycopy(resultBuff, nextBytes, fileNameByte, 0, fileBytes);
                            nextBytes += fileBytes;
                            byte[] dataByte = new byte[resultBuff.length - nextBytes];
                            System.arraycopy(resultBuff, nextBytes, dataByte, 0, dataByte.length);
                            String fileName = new String(fileNameByte);
                            System.out.println("receive file is: " + fileName);
                            String desFileName = "/home/administrator/CNT5106/project/client"+Peer_Num+"/file/"+fileName; 
                            try{
                                OutputStream output = null; 
                                try{ 
                                    output = new BufferedOutputStream(new FileOutputStream(desFileName));
                                    output.write(dataByte); 
                                    // String data = new String(dataByte);
                                    // System.out.println("receive data is: " + data+".end");
                                    System.out.println("Writing Process was performed");
                                }   
                                finally{
                                    output.close();
                                    in.close();
                                    requestSocket.close(); 
                                    doneSignal.countDown();

                                    return ;
                                } 
                            }catch(FileNotFoundException ex){
                                ex.printStackTrace();
                            }catch(IOException ex){
                                ex.printStackTrace();
                            }   
                        }   
                    }
                    //requestSocket.close(); 
                    //return; 
                }catch(ConnectException e){
                    System.err.println("Connection refused. You need to initiate a server first.");
                }catch(UnknownHostException unknownHost){
                    System.err.println("You are trying to connect to an unknown host!");
                }catch(IOException ioException){
                    ioException.printStackTrace();
                }
            } 
        }
    }







