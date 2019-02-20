import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
/**
 * Reading the property file
 * 
 *
 */
public class PropertyReader {
	private String PropertyFileName,BASE_URI,nmap_location;
	private int threadnum,sleeptime;
	/**
	 * PropertyReader takes as an argument a string pointing to a property file.
	 * @param PropertyFileName Property file to be read.
	 */
	public PropertyReader(String PropertyFileName){
		this.PropertyFileName=PropertyFileName;
	}
	/**
	 * Reads the property file, getting onetimejobs thread number,sleep time for maind and sender and input file main will read.
	 */
	public void readpropertyfile(){
		try{
			FileInputStream propfileinput=new FileInputStream(PropertyFileName);
			Properties prop=new Properties();
			prop.load(propfileinput);                         
			propfileinput.close();
			threadnum=Integer.parseInt(prop.getProperty("one_time_job-threads"));
			sleeptime=Integer.parseInt(prop.getProperty("sleeptime"));
			BASE_URI=prop.getProperty("BASE_URI");
			nmap_location=prop.getProperty("nmap_location");
			System.out.println("Number of threads is "+threadnum);
			System.out.println("Main sleep time is "+sleeptime);
			System.out.println("BASE_URI for server is "+BASE_URI);
			System.out.println("Nmap location is "+nmap_location);
		} catch (FileNotFoundException e){
			System.out.println("Property file not found: "+e);
			System.exit(2);
		} catch (IOException e){
			System.out.println("Error: "+e);
			System.exit(1);
		}
	}
	/**
	 * 
	 * @return Returns the thread number of onetimejobs that will be used.
	 */
	public int getthreadnum(){
		return threadnum;
	}
	/**
	 * 
	 * @return Returns the amount of time the main thread and sender thread will sleep between reads.
	 */
	public int getsleeptime(){
		return sleeptime;
	}
	/**
	 * 
	 * @return Returns the file that will be used to get nmap-jobs.
	 */
	public String getBASE_URI(){
		return BASE_URI;
	}
	
	public String getNmapLocation() {
		return nmap_location;
	}
}
