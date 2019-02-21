package com.kostas4949.mundis.nmapclient;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
/**
 * Periodical threads rerun the nmap command on standard intervals.
 */
import java.util.concurrent.ConcurrentLinkedQueue;
public class PeriodicThread implements Runnable {
	private String NmapJob;
	private int PeriodicTime,number;
	private ConcurrentLinkedQueue<String> clq;
	/**
	 * Periodical thread takes 3 parameters.
	 * @param NmapJob String that has to run as an nmap command.
	 * @param PeriodicTime Time between periods of running the nmap command.
	 * @param clq     A queue where the result will be sent.
	 * @param number Job id that is being executed on thread.
	 */
	public PeriodicThread(String NmapJob,int PeriodicTime,ConcurrentLinkedQueue<String> clq,int number){
		this.clq=clq;
		int firstcomma=NmapJob.indexOf(",");
		//this.number=Integer.parseInt(NmapJob.substring(0,firstcomma));
		this.number=number;
		this.PeriodicTime=PeriodicTime;
		if(NmapJob.contains("-oX")){
			this.NmapJob="nmap " + NmapJob.substring(firstcomma+1);
		}
		else{
			this.NmapJob="nmap -oX - " + NmapJob.substring(firstcomma+1);
		}
	}
	/**
	 * Runs every PeriodicTime the same command and puts results in the clq queue.
	 */
	public void run() {
		String[] nmaparg=NmapJob.split(" ");
		try{
			while(Main.checkifworking()){	
				if(Thread.currentThread().isInterrupted()){ //shuthook started to run or stopped from gui
					break;
				}
				Process process = Runtime.getRuntime().exec(nmaparg);   
				BufferedReader pin=new BufferedReader(new InputStreamReader(process.getInputStream())); //Read the output from nmap
				StringBuilder sb=new StringBuilder();
				String line = null;
	 			while ((line=pin.readLine())!=null){  //print results in console
	 				sb.append(line).append("\n");
				}
	 			String s="1\n"+String.valueOf(number)+"\n"+sb.toString(); //Add periodical (1 for periodic job) and id to results
                pin.close();
				clq.offer(s);
				try{
					Thread.sleep(1000*PeriodicTime); //seconds
				}catch(InterruptedException ex){
					Thread.currentThread().interrupt();
					break;
				}
			}
		}
		catch (IOException e){
			System.out.println("Thrown exception when calling nmap: "+e);
		}
		System.out.println("Finishing periodic thread for nmapjob with number "+number);
	}
}