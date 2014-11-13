import java.net.*; 
import java.io.*; 
import java.nio.*; 
import java.nio.channels.*;
import java.util.*; 

public class Server{ 
    private static final int SERVER_PORT = 8000; 
    public static final int CHUNK_NUM = 5;
    public static void main(String args[]){ 
        System.out.println("The server is running on port:" + SERVER_PORT);
        ArrayList<String> nameList = null; 
        try{ 
            nameList = readFragment("/home/administrator/CNT5106/project/example/examplefile", CHUNK_NUM);
        }
        catch(IOException ex){ 
            ex.printStackTrace(); 
        } 
        try{  
            ServerSocket listener = new ServerSocket(SERVER_PORT); 
            int clientNum = 1; 
            try{ 
                while(true){ 
                    new Handler(listener.accept(), clientNum, nameList).start(); 
                    System.out.println("Client" + clientNum + "is connected"); 
                    clientNum++; 
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

    public static ArrayList <String> readFragment(String SourceFile, int CHUNK_NUM) throws IOException{ 
        File inputFile = new File(SourceFile); 
        int FILE_SIZE = (int) inputFile.length();
        System.out.println("File size: " + FILE_SIZE); 
        int CHUNK_SIZE = FILE_SIZE/CHUNK_NUM; 
        int CHUNK_COUNT = 1; 
        ArrayList<String> nameList = new ArrayList<String>(); 
        try{ 
            InputStream input = null; 
            int totalBytesRead = 0; 
            int remainBytes = FILE_SIZE - totalBytesRead; 
            try{ 
                input = new BufferedInputStream( new FileInputStream(inputFile)); 
                while(CHUNK_COUNT <= CHUNK_NUM){ 
                    String partName = "data" + CHUNK_COUNT + ".bin";
                    if(CHUNK_COUNT == CHUNK_NUM){ 
                        CHUNK_SIZE =remainBytes; 
                    } 
                    byte[] temporary = new byte[CHUNK_SIZE]; 
                    int readBytes = input.read(temporary, 0, CHUNK_SIZE);
                    totalBytesRead += readBytes; 
                    remainBytes = FILE_SIZE - totalBytesRead; 
                    String desFile = "/home/administrator/CNT5106/project/file/" + partName; 
                    write(temporary, desFile); 
                    nameList.add(partName); 
                    CHUNK_COUNT++; 
                } 
            }
            finally{ 
                input.close(); 
            } 
        } 
        catch (FileNotFoundException ex)
        { 
            ex.printStackTrace(); 
        } 
        catch (IOException ex){ 
            ex.printStackTrace(); 
        } 
        return nameList; 
    } 

    public static void write(byte[] temporary, String desFile){ 
        try{ 
            OutputStream output = null; 
            try{ 
                output = new BufferedOutputStream( new FileOutputStream(desFile)); 
                output.write(temporary); 
            } 
            finally{ 
                output.close(); 
            } 
        } 
        catch (FileNotFoundException ex){ 
            ex.printStackTrace(); 
        } 
        catch (IOException ex){ 
            ex.printStackTrace(); 
        } 
    } 



    private static class Handler extends Thread { 
        private BufferedInputStream in; 
        private BufferedOutputStream out; 
        private int clientNum; 
        private ArrayList<String> nameList;
        private Socket connection;
        private static ArrayList<String> clientIpList = new ArrayList<String>(); 
        public Handler(Socket connection, int clientNum, ArrayList<String> nameList){ 
            this.connection = connection; 
            this.clientNum = clientNum; 
            this.nameList = nameList; 
            clientIpList.add(connection.getRemoteSocketAddress().toString()); 
        } 

        public void run(){ 
            try{ 
                out = new BufferedOutputStream(connection.getOutputStream()); 
                out.flush(); 
                in = new BufferedInputStream(connection.getInputStream());
                
                out.flush();
                //out.close(); 
                //out = new BufferedOutputStream(connection.getOutputStream()); 
                while(true){
                    try{ 
                    Thread.sleep(100);
                    } 
                    catch(InterruptedException ex){
                    } 
                    if(clientIpList.size()==Server.CHUNK_NUM){ 
                        sendClientIp();
                        System.out.println("clientIpList.size: " + clientIpList.size());
                        sendChunks(); 
                        break; 
                    }
                } 
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
                        if(clientIpList.size()==Server.CHUNK_NUM){//error+1 
                            in.close();
                            out.close(); 
                            connection.close(); 
                        } 
                    } 
                    catch(IOException ioException){ 
                        System.out.println("Disconnect with Client" + clientNum); 
                    } 
                } 
            } 

            public void sendClientIp(){
                String str = "clientIpList: " + Integer.toString(CHUNK_NUM); 
                for(String s : clientIpList) 
                    str+=s; 
                try{ 
                    byte[] temporary = str.getBytes(); 
                    out.write(temporary); 
                    str = new String(temporary);
                    System.out.println("write clientIpList overi: " + str); 
                }
                catch(IOException ioException){ 
                    ioException.printStackTrace(); 
                }
            } 


            public void sendChunks(){
                String fileName =  nameList.get(clientNum-1);//clientNum start from one 
                File file = new File("/home/administrator/CNT5106/project/file/" + fileName); 
                int fileSize = (int) file.length(); 
                try{ 
                    InputStream inStream = new BufferedInputStream( new FileInputStream(file)); 
                    //add fileName at Head 
                    byte[] fileNameByte = fileName.getBytes();  
                    byte[] temporary = new byte[fileNameByte.length + fileSize]; 
                    System.arraycopy(fileNameByte, 0, temporary, 0, fileNameByte.length); 
                    inStream.read(temporary, fileNameByte.length, fileSize); 

                    out.write(temporary); 
                    out.flush(); 
                    System.out.println("Send chunks: " + clientNum + "to Client: " + clientNum); 
                    inStream.close();  
                } 
                catch(IOException ioException){ 
                    ioException.printStackTrace(); 
                } 
            } 
        } 
    }


