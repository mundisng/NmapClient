import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
/**
 * Shutdown hook to catch ctrl+c.
 * 
 *
 */
public class ShutHook {
	private ArrayList<ExecutorNode> elist;
	private Thread mymain;;
	private ExecutorService threadpool,sender;
	/**
	 * Shutdown hook takes 2 parameters.
	 * @param elist List of all available threads.
	 * @param mymain Main thread.
	 * @param threadpool All available threads running jobs.
	 * @param sender SenderThread
	 */
	public ShutHook(ArrayList<ExecutorNode> elist,Thread mymain,ExecutorService threadpool,ExecutorService sender){
		this.elist=elist;
		this.mymain=mymain;
		this.threadpool=threadpool;
		this.sender=sender;
	}
	/**
	 * Attach hook to Main thread.
	 */
	public void attachShutHook(){
		Runtime.getRuntime().addShutdownHook(new Thread(){
			@Override
			/**
			 * Hook shuts down all executor threads, interrupts main and then waits for main and threads to finish before exiting.
			 */
			public void run(){
				Main.stopworking(); //change variable for whiles
				mymain.interrupt(); //interrupt main thread
				threadpool.shutdownNow(); //one time job threads
				sender.shutdownNow();	//sender thread
				for(int i=0;i<elist.size();i++){
					(elist.get(i).get_exec()).shutdownNow(); //interrupt executor threads (periodic jobs)
				}
				try {
					mymain.join(); //wait for main thread
					if(!(threadpool.awaitTermination(60,TimeUnit.SECONDS))){ //wait for executor threadpool (onetime jobs)
						System.out.println("Still waiting after 60s(threadpool)..");
					}
					if(!(sender.awaitTermination(60,TimeUnit.SECONDS))){ //wait for executor sender thread
						System.out.println("Still waiting after 60s(sender)..");
					}
				} catch (InterruptedException e1) {
					System.out.println("Interrupted main/threadpool/sender join (shutdown hook)..");
					e1.printStackTrace();
				}
				for(int i=0;i<elist.size();i++){
					try {
						if(!(elist.get(i).get_exec()).awaitTermination(60,TimeUnit.SECONDS)){ //wait for executor threads
							System.out.println("Still waiting after 60s(periodic)..");
						}
					} catch (InterruptedException e) {
						System.out.println("Interrupted awaitterm shutdown hook..");
						e.printStackTrace();
					}
				}
				System.out.println("Exiting shutdown hook..");
			}
		});
	}
}