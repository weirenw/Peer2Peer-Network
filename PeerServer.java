

import java.net.*; 
import java.io.*; 
import java.nio.*; 
import java.nio.channels.*;
import java.util.*; 
 


public class PeerServer extends Thread{ 
    private  int SERVER_PORT = 0; 
    public  int PEER_NUM = 0;// 8001 1, 8002 2, 8003 3
    public PeerServer(int port, int peer_num){ 
        SERVER_PORT = port; 
        PEER_NUM = peer_num; 
    } 

    public static void main(String args[]){ 
        PeerServer p = new PeerServer(8001,1); 
        p.run(); 
    } 

    public void run(){
        System.out.println("The server is running on port:" + SERVER_PORT);
        try{  
            ServerSocket listener = new ServerSocket(SERVER_PORT); 
            int clientCount = 1; 
            DoneSignal ds = new DoneSignal(4); //CHUNK_NUM-1
            try{ 
                while(true){ 
                    try{
                        Thread.sleep(100);
                    }catch(InterruptedException ex){
                    }
                    System.out.println("in ServerMain, current SignalCount: " + ds.getSignalCount()); 
                    if(ds.getSignalCount() == 0){
                        System.out.println("All clients connected to Server"); 
                        break;
                    } 
                    new Handler(listener.accept(), clientCount, PEER_NUM, ds).start(); 
                    System.out.println("Client" + clientCount + "is connected"); 
                    clientCount++;
                } 
            } 
            catch(IOException ex){ 
                ex.printStackTrace(); 
            } 
            finally { 
                try{ 
                    listener.close();
                } 
                catch(IOException ex){ 
                    ex.printStackTrace(); 
                } 
            } 
        } 
        catch(IOException ex){
            ex.printStackTrace();
        } 

    } 



    private static class Handler extends Thread { 
        private BufferedInputStream in; 
        private BufferedOutputStream out; 
        private Socket connection;
        private int PEER_NUM = 0;
        private int clientNum = 0;
        private DoneSignal ds; 
        public Handler(Socket connection, int clientNum, int pnum, DoneSignal rcvds){ 
            this.connection = connection; 
            this.clientNum = clientNum; 
            this.PEER_NUM = pnum;
            this.ds = rcvds; 
        } 

        public void run(){ 
            try{ 
                out = new BufferedOutputStream(connection.getOutputStream()); 
                out.flush(); 
                in = new BufferedInputStream(connection.getInputStream());

                out.flush();
                //out.close(); 
                //out = new BufferedOutputStream(connection.getOutputStream()); 
                sendChunks(); 
                /*
                   if(clientIpList.size()==Server.CHUNK_NUM){//chunkNumber==clientNumber 
                   System.out.println("clientIpList0"); 
                   System.out.println("clientIpList1"); 
                   System.out.println("clientIpList2"); 
                   System.out.println(clientIpList); 
                   out.flush();
                   sendClientIp();
                   }
                   */
            } 
            catch(IOException ioException){ 
                System.out.println("Disconnect with Client" + clientNum);
            } 
            finally{ 
                try{ 
                    in.close();
                    out.close(); 
                    connection.close(); 
                    ds.decrease(); 
                    System.out.println("current donesignal value: " + ds.getSignalCount()); 
                    return ; 
                } 
                catch(IOException ioException){ 
                    System.out.println("Disconnect with Client"); 
                } 
            } 
        } 


        public void sendChunks(){
            String fileName =  "data" + Integer.toString(PEER_NUM) + ".bin"; 
            File file = new File("/home/administrator/CNT5106/project/client" + Integer.toString(PEER_NUM) + "/file/" + fileName); 
            int fileSize = (int) file.length(); 
            try{ 
                InputStream inStream = new BufferedInputStream( new FileInputStream(file)); 
                //add fileName at Head 
                byte[] fileNameByte = fileName.getBytes();  
                byte[] temporary = new byte[fileNameByte.length + fileSize]; 
                System.arraycopy(fileNameByte, 0, temporary, 0, fileNameByte.length); 
                inStream.read(temporary, fileNameByte.length, fileSize); 

                out.write(temporary); 
                //out.flush(); 

                System.out.println("Send chunks: "+"data" + Integer.toString(PEER_NUM) + ".bin");
                //String str = new String(temporary); 
                //System.out.println(str); 
                inStream.close(); 
                out.close(); 
                return ;
            } 
            catch(IOException ioException){ 
                ioException.printStackTrace(); 
            } 
        } 
    } 
}


