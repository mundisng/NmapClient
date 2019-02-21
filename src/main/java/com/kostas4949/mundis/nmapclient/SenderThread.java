package com.kostas4949.mundis.nmapclient;
import java.util.concurrent.ConcurrentLinkedQueue;
/**
 * Thread that reads and prints the queue for results.
 * 
 *
 */
public class SenderThread implements Runnable{
	private ConcurrentLinkedQueue<String> clq,clq2;
	private String resultt;
	private int sleeptime;
	/**
	 * Takes 2 parameters.
	 * @param clq The queue from where strings will be put between threads executing jobs and sender thread.
	 * @param sleeptime How often it will check for results in the queue.
	 * @param clq2 The queue where strings will be read from main.
	 */
	public SenderThread(ConcurrentLinkedQueue<String> clq,int sleeptime,ConcurrentLinkedQueue<String> clq2){
		this.clq=clq;
		this.sleeptime=sleeptime;
		this.clq2=clq2;
	}
	/**
	 * As long as Main.checkifworking() is true,every sleeptime poll the clq queue for results.
	 * 
	 */
	public void run () {
    	try {
    		while(Main.checkifworking()) { //ends when shuthook changes the variable or interrupts the thread
    			if(Thread.currentThread().isInterrupted()){
					break;
				}
    			Thread.sleep(1000*sleeptime);   //sleep for sleeptime seconds
    			while (!clq.isEmpty()){    //Read shared memory till it's empty
    				resultt=clq.poll();    //Get Result
    				clq2.offer(resultt);
    			}
    		}
    	}catch(InterruptedException ex){
    		Thread.currentThread().interrupt();
    	}
    	System.out.println("Finishing sender thread..");
    }
}