import java.io.*;
import java.util.concurrent.ConcurrentLinkedQueue;
/**
 * OnetimeJob thread only runs the nmap command once.
 */
public class OneTimeJob implements Runnable{
	private String nmap;
	private int number;
	private ConcurrentLinkedQueue<String> clq;
	/**
	 * OneTimeJob takes 2 parameters.
	 * @param nmap String that has to run as an nmap command.
	 * @param clq  A queue where the result will be sent.
	 * @param number Job id that is being executed on thread.
	 */
	public OneTimeJob(String nmap, ConcurrentLinkedQueue<String> clq,int number){
		this.clq=clq;
		int firstcomma=nmap.indexOf(",");
		  //Takes the id (first number from string, to use it in the future)
		this.number=number;
		if(nmap.contains("-oX")){
			this.nmap="nmap " + nmap.substring(firstcomma+1);     //Takes the string, without the first number but with the nmap command added
		}
		else{
			this.nmap="nmap -oX - " + nmap.substring(firstcomma+1);   //Add -oX if it doesn't exist to the start of the parameters
		}
	}
	/**
	 * Runs one time the nmap commands and puts results in clq queue.
	 */
	public void run(){
		String[] nmaparg=nmap.split(" ");  //Split string into parameters to feed to exec
		try {
			Process process = Runtime.getRuntime().exec(nmaparg);    //exec("nmap" "parameter1" "Parameter2" ....)
			BufferedReader pin=new BufferedReader(new InputStreamReader(process.getInputStream())); //Read the output from nmap
			StringBuilder sb=new StringBuilder();
			String line = null;
 			while ((line=pin.readLine())!=null){  //Read output of nmap line by line and save it into sb
 				sb.append(line).append("\n");
			}
 			String s="0\n"+String.valueOf(number)+"\n"+sb.toString();  //Add periodical (0 for non periodic job) and id to results
			pin.close();
 			clq.offer(s);    //Send it as a string over the linked queue
		}
		catch (IOException e){
			System.out.println("Thrown exception when calling nmap: "+e);
		}
		System.out.println("Finishing onetime thread for nmapjob with number "+number);
	}
}