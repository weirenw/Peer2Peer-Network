import java.io.*; 
import java.util.*; 

public class MergeChunk{ 

    public void mergeParts ( int PEER_NUM, int CHUNK_NUM )
    {
        String DESTINATION_PATH = "/home/administrator/CNT5106/project/client" + Integer.toString(PEER_NUM) + "/file/";  

        ArrayList<String> nameList = new ArrayList<String>();  

        for(int i=1;i<=CHUNK_NUM;i++)
            nameList.add(DESTINATION_PATH+"data"+Integer.toString(i)+".bin"); 

        File[] file = new File[nameList.size()];
        byte AllFilesContent[] = null;

        int TOTAL_SIZE = 0;
        int FILE_NUMBER = nameList.size();
        int FILE_LENGTH = 0;
        int CURRENT_LENGTH=0;

        for ( int i=0; i<FILE_NUMBER; i++)
        {
            file[i] = new File (nameList.get(i));
            TOTAL_SIZE+=file[i].length();
        }

        try {
            AllFilesContent= new byte[TOTAL_SIZE]; // Length of All Files, Total Size
            InputStream inStream = null;

            for ( int j=0; j<FILE_NUMBER; j++)
            {
                inStream = new BufferedInputStream ( new FileInputStream( file[j] ));
                FILE_LENGTH = (int) file[j].length();
                inStream.read(AllFilesContent, CURRENT_LENGTH, FILE_LENGTH);
                CURRENT_LENGTH+=FILE_LENGTH;
                inStream.close();
            }

        }
        catch (FileNotFoundException e)
        {
            System.out.println("File not found " + e);
        }
        catch (IOException ioe)
        {
            System.out.println("Exception while reading the file " + ioe);
        }
        finally 
        {
            write (AllFilesContent,DESTINATION_PATH+"finalfile");
        }

        System.out.println("Merge was executed successfully.!");

    }

    void write(byte[] DataByteArray, String DestinationFileName){
        try {
            OutputStream output = null;
            try {
                output = new BufferedOutputStream(new FileOutputStream(DestinationFileName));
                output.write( DataByteArray );
                System.out.println("Writing Process Was Performed");
            }
            finally {
                output.close();
            }
        }
        catch(FileNotFoundException ex){
            ex.printStackTrace();
        }
        catch(IOException ex){
            ex.printStackTrace();
        }
    }
}

