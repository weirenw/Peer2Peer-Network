import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*; 
import java.lang.System; 
public class ClientNode2 { 
    Socket requestSocket; 
    BufferedOutputStream out; 
    BufferedInputStream in; 

    void run(){ 
        boolean endServerConnection = false;
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
                    byte[] tBuff = new byte[resultBuff.length + k]; 
                    System.out.println("k size is: " + k);
                    System.out.println("resultBuff.length is: " + resultBuff.length);
                    System.out.println("buff.length " + buff.length);
                    System.arraycopy(resultBuff, 0, tBuff, 0, resultBuff.length); 
                    System.arraycopy(buff, 0, tBuff, resultBuff.length, k); 
                    resultBuff = tBuff; 
                    System.out.println("read size is: " + k); 
                }
                if(resultBuff.length > 0){ 
                    int rcvBytes = 12;
                    byte[] rcvNameByte = new byte[rcvBytes]; 
                    System.arraycopy(resultBuff, 0, rcvNameByte, 0, rcvBytes);
                    String rcvName = new String(rcvNameByte); 
                    System.out.println("rcvName is: " + rcvName);
                    if(rcvName.equals("clientIpList")){ 
                        endServerConnection = true;

                        byte[] dataByte = new byte[resultBuff.length - rcvBytes]; 
                        System.arraycopy(resultBuff, rcvBytes, dataByte, 0, resultBuff.length - rcvBytes); 
                        String data = new String(dataByte); 
                        System.out.println("clientIpList is: " + data);
                    } 
                    else{  
                        //file name is "data" + number + ".bin" 
                        int fileBytes = 9; 
                        byte[] fileNameByte = new byte[fileBytes]; 
                        System.arraycopy(resultBuff, 0, fileNameByte, 0, fileBytes);
                        byte[] dataByte = new byte[resultBuff.length - fileBytes]; 
                        System.arraycopy(resultBuff, fileBytes, dataByte, 0, resultBuff.length - fileBytes); 
                        String fileName = new String(fileNameByte); 
                        System.out.println("receive file is: " + fileName); 
                        String desFileName = "/home/administrator/CNT5106/project/client1/file/" + fileName; 
                        try{ 
                            OutputStream output = null; 
                            try{ 
                                output = new BufferedOutputStream(new FileOutputStream(desFileName)); 
                                output.write(dataByte); 
                                System.out.println("Writing Process was performed"); 
                            } 
                            finally{ 
                                output.close(); 
                            } 
                        }catch(FileNotFoundException ex){ 
                            ex.printStackTrace(); 
                        }catch(IOException ex){ 
                            ex.printStackTrace(); 
                        } 
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
                if(endServerConnection){ 
                    in.close(); 
                    out.close(); 
                    requestSocket.close(); 
                    return ;
                } 
            }
            catch(IOException ex){ 
                ex.printStackTrace(); 
            } 
        } 
    }

    public static void main(String args[]){ 
        int portnum = 8002; 
        int peernum = 2; 
        int chunknum = 5;
        ClientNode client = new ClientNode(peernum); 
        client.run(); 

        PeerServer s = new PeerServer(portnum,peernum); 
        s.start(); 
        System.out.println("client going to start");
        PeerClient c = new PeerClient(peernum,chunknum); 
        c.start(); 
        System.out.println("client has started");
    } 

} 



