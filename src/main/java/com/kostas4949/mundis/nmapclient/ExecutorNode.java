package com.kostas4949.mundis.nmapclient;
import java.util.concurrent.ExecutorService;
/**
 * Keep tracks of which thread has which periodic job
 * 
 *
 */

public class ExecutorNode {
	private final ExecutorService myexec;
	private final int nmapjob_id;
	/**
	 * Takes thread and jobid as parameters.
	 *@param myexec Periodicthread
	 * @param nmapjob_id Id of periodic job
	 */
	public ExecutorNode(ExecutorService myexec,int nmapjob_id){
		this.myexec=myexec;
		this.nmapjob_id=nmapjob_id;
	}
	/**
	 * Gets thread for a certain thread-job combination
	 * @return ExecutorService type thread
	 */
	public ExecutorService get_exec(){
		return myexec;
	}
	/**
	 * Gets id of periodic job based on certain thread-job combination
	 * @return Job id as integer
	 */
	public int get_nmapjobid(){
		return nmapjob_id;
	}
}
