# NmapClient
Multithreaded Java REST client using Jersey Framework.


Uses threads depending on the type of nmap job the client gets and executes that job. Has a periodic thread that checks for the results and sends them back to the server. 
