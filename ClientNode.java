import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*; 
import java.lang.System; 
public class ClientNode { 
    Socket requestSocket; 
    BufferedOutputStream out; 
    BufferedInputStream in; 
    public static int CLIENT_NUM=0;
    public ClientNode(int clientnum){ 
        CLIENT_NUM = clientnum; 
    } 
    void run(){ 
        try{ 
            requestSocket = new Socket("localhost", 8000); 
            System.out.println("Connected to localhost 8000"); 

            while(true){ 
                byte[] resultBuff = new byte[0]; 
                byte[] buff = new byte[4096]; 
                int k = -1; 
                out = new BufferedOutputStream(requestSocket.getOutputStream()); 
                out.flush(); 
                in = new BufferedInputStream(requestSocket.getInputStream()); 
                while((k = in.read(buff, 0, buff.length)) > -1){ 
                    System.out.println("block"); 
                    byte[] tBuff = new byte[resultBuff.length + k]; 
                    System.out.println("k size is: " + k);
                    System.out.println("resultBuff.length is: " + resultBuff.length);
                    System.out.println("buff.length " + buff.length);
                    System.arraycopy(resultBuff, 0, tBuff, 0, resultBuff.length); 
                    System.arraycopy(buff, 0, tBuff, resultBuff.length, k); 
                    resultBuff = tBuff; 
                    System.out.println("read size is: " + k); 
                }
                // System.out.println("rcvName is:xxxxx "); 
                if(resultBuff.length > 0){
                    int nextBytes = 0;
                    int rcvBytes = 14;
                    byte[] rcvNameByte = new byte[rcvBytes]; 
                    System.arraycopy(resultBuff, 0, rcvNameByte, 0, rcvBytes);
                    nextBytes += rcvBytes; 
                    String rcvName = new String(rcvNameByte); 
                    System.out.println("rcvName is: " + rcvName); 
                    byte[] chunkNumByte = new byte[1]; 
                    System.arraycopy(resultBuff, nextBytes, chunkNumByte, 0, 1);
                    nextBytes += 1; 
                    int chunkNum = Integer.parseInt(new String(chunkNumByte)); 

                    byte[] clientIpListByte = new byte[chunkNum * 16]; 
                    System.arraycopy(resultBuff, nextBytes, clientIpListByte, 0, clientIpListByte.length); 
                    nextBytes += clientIpListByte.length; 
                    String clientIpList = new String(clientIpListByte); 
                    System.out.println("clientIpList is: " + clientIpList);

                    //file name is "data" + number + ".bin" 
                    System.out.println("begin write");
                    int fileBytes = 9; 
                    byte[] fileNameByte = new byte[fileBytes]; 
                    System.arraycopy(resultBuff, nextBytes, fileNameByte, 0, fileBytes);
                    nextBytes += fileBytes; 
                    byte[] dataByte = new byte[resultBuff.length - nextBytes]; 
                    System.arraycopy(resultBuff, nextBytes, dataByte, 0, dataByte.length); 
                    String fileName = new String(fileNameByte); 
                    System.out.println("receive file is: " + fileName); 
                    String desFileName = "/home/administrator/CNT5106/project/client" + Integer.toString(CLIENT_NUM)+ "/file/" + fileName; 
                    try{ 
                        BufferedOutputStream output = null; 
                        try{ 
                            FileOutputStream fos = new FileOutputStream(desFileName); 
                            fos.write(dataByte); 
                            //output = new BufferedOutputStream(new FileOutputStream(new File(desFileName))); 
                            //output.write(dataByte); 
                            //String data = new String(dataByte); 
                            //System.out.println("receive data is: " + data); 
                            //System.out.println(dataByte); 
                            System.out.println("Writing Process was performed:" + desFileName); 
                        } 
                        finally{ 
                            in.close(); 
                            out.close(); 
                            requestSocket.close(); 
                            return; 
                        } 
                    }catch(FileNotFoundException ex){ 
                        ex.printStackTrace(); 
                    }catch(IOException ex){ 
                        ex.printStackTrace(); 
                    } 
                } 
            } 
        }catch(ConnectException e){ 
            System.err.println("Connection refused. You need to initiate a server first."); 
        }catch(UnknownHostException unknownHost){ 
            System.err.println("You are trying to connect to an unknown host!");
        }catch(IOException ioException){ 
            ioException.printStackTrace();
        } 
        finally{ 
            try{ 
                in.close(); 
                out.close(); 
                requestSocket.close(); 
            }
            catch(IOException ex){ 
                ex.printStackTrace(); 
            } 
        } 
    }

    public static void main(String args[]){
        int portnum = 8001; 
        int peernum = 1; 
        int chunknum = 2;
        ClientNode client = new ClientNode(peernum);
        client.run(); 
        PeerServer s = new PeerServer(portnum,peernum);
        s.start(); 
        PeerClient c = new PeerClient(peernum,chunknum);
        c.run();
    } 

} 



