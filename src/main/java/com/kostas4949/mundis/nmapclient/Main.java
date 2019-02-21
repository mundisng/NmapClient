package com.kostas4949.mundis.nmapclient;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.ws.rs.core.MediaType;

import java.util.concurrent.ConcurrentLinkedQueue;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
/**
 * 
 * @author Konstantinos Skyvalos and Nikos Gkountas
 *
 */
public class Main {
	private static volatile boolean keepworking=true;
	public static PropertyReader propreader=new PropertyReader("src/main/resources/project1.properties");
	/**
	 * 
	 * Main thread, handling connection between server and client
	 */
	public static void main(String[] args) {
		int secondcomma,periodictime,qq=0;
		propreader.readpropertyfile();
		SAIndentification sai= new SAIndentification();  //Create all identifications(IP,MAC, etc)
		String line2,resulttoserver;
		ArrayList<String> list2=new ArrayList<String>();
        JSONObject array=sai.getSAIndentification();  //Get all identifications, which are in json format
		Client client = Client.create();
		WebResource webResource = client.resource(propreader.getBASE_URI()+"info/post");
		while (qq==0){
			try {
				ClientResponse response = webResource.type(MediaType.APPLICATION_JSON).post(ClientResponse.class,array.toString()); //Send identifications to server
				if (response.getStatus() == 403) {
					System.out.println("Access denied!Retrying in 5 seconds..");  //Don't give up till being accepted by server
					try {
						Thread.sleep(5000);
					} catch (InterruptedException ie) {
					    System.out.println("Tried to interrupt sleep");
					}
				}
				else if(response.getStatus()== 200){
					qq=1;
					System.out.println("Got correct response from http server!Response was : "+response.getEntity(String.class));
					break;
				}
				else{
					throw new RuntimeException("Failed : HTTP error code : "+ response.getStatus());
				}
			}catch (Exception e){   //Keep trying while server is down
				System.out.println("Failed to connect to http server,retrying in 5 seconds");
				try {
					Thread.sleep(5000);
				} catch (InterruptedException ie) {
					System.out.println("Tried to interrupt sleep");
				}
			}
		}
		ArrayList<ExecutorNode> elist=new ArrayList<ExecutorNode>();   //ArrayList of all available periodic threads
		ConcurrentLinkedQueue <String>q = new ConcurrentLinkedQueue<String>(); //for nmapjobs outputs
		ConcurrentLinkedQueue <String>q2 = new ConcurrentLinkedQueue<String>();
		ExecutorService Pool = Executors.newFixedThreadPool(propreader.getthreadnum());  //The fixed pool of OneTimeJobs
		ExecutorService exsender=Executors.newSingleThreadExecutor();   //SenderThread creation
		exsender.execute(new SenderThread(q,propreader.getsleeptime(),q2));
		ShutHook myhook=new ShutHook(elist,Thread.currentThread(),Pool,exsender);   //Shutdownhook for ctrl+c
		myhook.attachShutHook();
		
		while (keepworking){
			int jsonnum=0;
			if (!q2.isEmpty()){ //if there are results in queue placed by senderthread
				JSONObject resultinjson= new JSONObject();
			while (!q2.isEmpty()){    //Read shared memory till it's empty
				System.out.println("Sending results to server..");
				resulttoserver=q2.poll();     //Get results from queue
				resulttoserver=sai.getHash()+"\n"+resulttoserver; //Add hash of SA at the start of the results
				resultinjson.put(Integer.toString(jsonnum), resulttoserver);//Create the jsonobject which will be sent
				jsonnum++;
			    }
			webResource = client.resource(propreader.getBASE_URI()+"jobs/post");
				try {
					ClientResponse response = webResource.type(MediaType.APPLICATION_JSON).post(ClientResponse.class,resultinjson.toString()); //Send results to server
					if (response.getStatus()!=200){
						throw new RuntimeException("Failed : HTTP error code : "+ response.getStatus());
					}
					else {
						System.out.println("Sent results correctly to server");
					}
				}catch (Exception e){
					System.out.println("Couldn't send results to server");
				}
			}
		   webResource = client.resource(propreader.getBASE_URI()+"jobs/get"); //Asking for jobs from server
		   try {
			   ClientResponse response = webResource.type(MediaType.TEXT_PLAIN).post(ClientResponse.class,Integer.toString(sai.getHash()));
	            if(response.getStatus()==200){ //if we got jobs
	    	        String x=response.getEntity(String.class);
	    	        JSONObject jjson = (JSONObject)new JSONParser().parse(x); //use jsonparser
	    	        System.out.println("Got "+jjson.size()+" jobs from server");
	    	        for (int zz=0; zz<jjson.size(); zz++){   //For as many lines of results
	    	           list2.add(jjson.get(Integer.toString(zz)).toString()); //convert results to string
	    	           line2=list2.get(zz);
	    	           System.out.println("Element " + line2);
	    	           secondcomma=line2.indexOf(",");//*index of first comma
	    	           int nmapjob_id=Integer.parseInt(line2.substring(0,secondcomma)); //get id from job
	    	           if(", Stop, true, periodic".equals(line2.substring(secondcomma))){  //Check if server asked for a periodic thread to stop
	    	        	   boolean found_periodicjob=false;
	    	        	   for(int ii=0;ii<elist.size();ii++){ 
	    	        		   if(elist.get(ii).get_nmapjobid()==nmapjob_id){  //Check if this periodic job exists in SA
	    	        			   (elist.get(ii).get_exec()).shutdownNow();
	    	        			   elist.remove(ii);
	    	        			   found_periodicjob=true;
	    	        			   break;
	    	        		   }
	    	        	   }
	    	        	   if(found_periodicjob){
	    	        		   System.out.println("Found periodic job with id: "+nmapjob_id);
	    	        	   }
	    	        	   else{
	    	        		   System.out.println("Didnt find periodic job with id: "+nmapjob_id);
	    	        	   }
	    	           }
	    	           else if ("-1, exit(0), true, -1".equals(line2)){ //Check if job was sent to shutdown SA
	    	        	   System.out.println("Time to terminate everything");
	    	        	   System.exit(0);
	    	           }
	    	           else{   //If not a job to end a periodic thread or shutdown SA is sent, take the results and place them in appropriate threads
			           secondcomma=line2.indexOf(",",secondcomma+1); //Find the comma which appears when the parameters for nmap end
			           if (line2.substring(secondcomma+1).trim().startsWith("f")==true){ //after that comma it's either false or true for periodic threads
				       periodictime=0;
				       Pool.submit(new OneTimeJob(line2.substring(0,secondcomma),q,nmapjob_id));  //Send line2 which has the number at the start,but get rid of the ",false,number"
			           }
			           else{
				          periodictime=Integer.parseInt(line2.substring(line2.lastIndexOf(",")+1).trim());
				          ExecutorService experiodic=Executors.newSingleThreadExecutor();
				          elist.add(new ExecutorNode(experiodic,nmapjob_id));    //Add PeriodicThread to list of available threads
				          experiodic.execute(new PeriodicThread(line2.substring(0,secondcomma),periodictime,q,nmapjob_id));
			           }
	    	           }
			           System.out.println("Line is: "+ line2.substring(0, secondcomma));
		            }
	    	        list2.clear();      //Empty ArrayString for next read
    	       }
	           else{
	        	   throw new RuntimeException("Failed : HTTP error code : "+ response.getStatus());
	           }
	       }catch (Exception e){
	    	   System.out.println("Server didn't send jobs correctly,terminating...");
	           e.printStackTrace();
	           System.exit(0);
	       }
			try {
				Thread.sleep(1000*(propreader.getsleeptime()));
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				break;
		    }
	    }
		System.out.println("Main Done");
	}
	/**
	 * Changes variable keepworking so that threads stops working.
	 */
	public static void stopworking(){
		Main.keepworking=false;
	}
	/**
	 * Check if the program should keep working.
	 * @return true if it still works, false if it stopped.
	 */
	public static boolean checkifworking(){
		return Main.keepworking;
	}
}