
import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

public class DoneSignal{
    private int doneSinalCount ;//CHUNK_NUM 
    public DoneSignal(int val){
        doneSinalCount = val;
    }

    public synchronized void setSignalCount(int val){
        doneSinalCount = val;
    }

    public synchronized void decrease(){
        doneSinalCount -= 1;
    }

    public synchronized int getSignalCount(){
        return doneSinalCount;
    }
}

