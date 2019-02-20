import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

import org.json.simple.JSONObject;

/**
 * Creating and handling all identifications
 * 
 *
 */

public class SAIndentification {
	private int hash;
	private String computerName="NULL",macadd="NULL",OS="NULL",version="NULL",ipaddress="NULL",temp;
	private static String[] nmapver = {Main.propreader.getNmapLocation(), "-version"};
	JSONObject array;
	
	
	/**
	 * Creates all identification that's needed for client.
	 * 
	 */
	public SAIndentification(){
	   try {
           computerName = InetAddress.getLocalHost().getHostName();  //Get Hotsname
       }
       catch(Exception ex) {
           System.out.println("Couldn't get hostname!");
       }
       try {
    	   Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
    	   while (interfaces.hasMoreElements()){
    		   NetworkInterface networkInterface = interfaces.nextElement();
    		   if (networkInterface.isUp()){
    			   byte[] mac=networkInterface.getHardwareAddress();
    			   if (mac!= null) {
    			   StringBuilder sb = new StringBuilder();
    			   for (int i = 0; i < mac.length; i++) {   //Get MAC
    				   sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
    			   }
    			   macadd=sb.toString();
    			   Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
    			   while (inetAddresses.hasMoreElements()){
    				   String ipaddr=inetAddresses.nextElement().toString().substring(1); 
    				   if (!ipaddr.contains(":") && ipaddr.indexOf("25.")!=0) {   //Only use IPv4 and avoid hamachi address space (25.0.0.0/8)
    				        ipaddress=ipaddr;
    				   }
    			   }
    			   if (ipaddress!="NULL") {
    			   break;
    			   }
    			   }
    		   }
    	   }
       }
       catch(Exception ee){
    	   System.out.println("Couldn't get network interface.Error: "+ee);
       }
       OS=System.getProperty("os.name")+"  "+System.getProperty("os.version");
       try {
    	   Process process = Runtime.getRuntime().exec(nmapver);    //exec("nmap" "parameter1" "Parameter2" ....)
    	   BufferedReader pin=new BufferedReader(new InputStreamReader(process.getInputStream()));
    	   temp=pin.readLine();
    	   while (temp.isEmpty()){
    		   temp=pin.readLine();
    	   }
    	   version=temp.substring(13, 17);
    	   pin.close();
       }
       catch (IOException eee){
		   System.out.println("Thrown exception when calling nmap: "+eee);
	   }
       RuntimeMXBean runtimeBean=ManagementFactory.getRuntimeMXBean();
       hash=0;
       hash=hash*31+runtimeBean.getName().hashCode();
       System.out.println("JVM Name = " +runtimeBean.getName());
       hash=hash*31+computerName.hashCode();
       hash=hash*31+ipaddress.hashCode();
       hash=hash*31+macadd.hashCode();
       hash=hash*31+OS.hashCode();
       hash=hash*31+version.hashCode();
	   System.out.println("Hostname is : " + computerName);
       System.out.println("Ip address is : " + ipaddress);
	   System.out.println("Mac address is : " + macadd);
	   System.out.println("OS is : " + OS);
	   System.out.println("Nmap version is : " + version);
	   System.out.println("Created hash : " + hash);
	   array= new JSONObject();
       array.put("hostname",computerName);
 	   array.put("IP",ipaddress);
 	   array.put("Mac",macadd);
 	   array.put("OS",OS);
 	   array.put("Nmap",version);
 	   array.put("Hash",hash);
	}
	/**
	 * Get all identification for this SA.
	 * @return Returns a json object with all identification
	 */
	public JSONObject getSAIndentification(){
		return array;
	}
	/**
	 * Gets only the hash identification of this SA.
	 * @return Returns unique hash of this SA.
	 */
	public int getHash(){
		return hash;
	}
}
