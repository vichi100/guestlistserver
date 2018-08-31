package com.mkanchwala.loggers.file;

import java.io.File;

public class DeleteFileExample {
    public static void main(String[] args)
    {	
    	try{
    		
    		File file = new File("/Users/vichi/my.log");
        	
    		if(file.renameTo(file)){
    			System.out.println(file.getName() + " is renamed!");
    		}else{
    			System.out.println("renamed operation is failed.");
    		}
    	   
    	}catch(Exception e){
    		
    		e.printStackTrace();
    		
    	}
    	
    }
}
