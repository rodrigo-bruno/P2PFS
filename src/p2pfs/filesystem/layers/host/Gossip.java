package p2pfs.filesystem.layers.host;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.List;

import net.tomp2p.futures.FutureDHT;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.PeerAddress;
import p2pfs.filesystem.types.dto.*;

class ClientSocketThread extends Thread {
	Gossip sv;
	Socket s;

	public ClientSocketThread(Gossip sv, Socket s) {
		this.sv = sv;
		this.s = s;
	}

	@Override
	public void run() {
		ObjectInputStream ois;
		try {
			while (true) {
				ois = new ObjectInputStream(s.getInputStream());
				GossipDTO msg = (GossipDTO) ois.readObject();
				sv.handleMsg(msg, s);
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			System.exit(1);
		} catch (Exception e) {
			//e.printStackTrace();
			//sv.removePeer(s);
			try {
				s.close();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
	}
}

public class Gossip {

	private PeerThread peerThread;

	public static final int RESET = 30000;
	public static final int START_CHECK = 100; //freq at which node zero checks if it has peers to start gossip
	public static final int SEND = 2000;
	public static final int LISTENING_PORT = 40004;
	
	private ServerSocket serverSocket;
	
	public static Number160 id;
	public static int currentGossip = 0;

	public static float localSu = 0;
	public static float localSa = 0;
	public static float localSs = 0;
	public static float localSm = 0;

	public static double W1 = 0;
	public static float Su = 0; //COMMAND num users
	public static float Sn = 1; // nodes
	public static float Sa = 0; // active users
	public static double W2 = 1;
	public static float Ss = 0; //COMMAND num files stored, for the avg
	public static float Sm = 0; // MB stored per node, for the avg
	
	public Gossip(PeerThread peerThread) {
		this.peerThread = peerThread;
		Gossip.id = peerThread.getPeerID();
		try {
			serverSocket = new ServerSocket(LISTENING_PORT); //use the same port for gossip listening on all nodes ServerSocket(LISTENING_PORT);
			//serverSocket.bind(null);
			System.out.println("Listening on port " + serverSocket.getLocalPort());

			initThreads();
	
		} catch (IOException e) {
			System.out.println("Could not open socket");
			System.exit(-1);
		}
	}

	public void showPeers(){
//		for(Socket s: peers){	
//			System.out.println(s.toString());	
//		}	
	}

	public void showGossip(){
			System.out.printf("Num of nodes= %f\n", getCountNodes());
			System.out.printf("Total num users= %f\n", getNumUsers());
			System.out.printf("Total num active users= %f\n", getNumActive());
			System.out.printf("Avg num files per node= %f\n", getNumFiles());
			System.out.printf("Avg num MB per node= %f\n", getNumMB());
	}

	/* When there are no peers, the response to the user query is the self values */
	public void showSelf(){
			System.out.printf( "Num of nodes= 1\n" );
			System.out.printf( "Total num users= %f\n", Gossip.localSu );
			System.out.printf( "Total num active users= %f\n", Gossip.localSa);
			System.out.printf( "Avg num files per node= %f\n", Gossip.localSs );
			System.out.printf( "Avg num MB per node= %f\n", Gossip.localSm );
	}		

	public double getCountNodes(){
		return Gossip.Sn/Gossip.W1;
	}

	public void updateNumUsers(float users){
		Gossip.Su += users;	
		Gossip.localSu += users;
	}
	public double getNumUsers(){
		return Gossip.Su/Gossip.W1; 
	}

	public void updateNumActive(float users){
		Gossip.Sa += users;
		Gossip.localSa += users;
	}
	public double getNumActive(){
		return Gossip.Sa/Gossip.W1; 
	}

	public void updateNumFiles(float files){
		Gossip.Ss += files;
		Gossip.localSs += files;
	}
	public double getNumFiles(){
		return Gossip.Ss/Gossip.W2; 
	}

	public void updateNumMB(float mb){
		Gossip.Sm += mb;
		Gossip.localSm += mb;
	}
	public double getNumMB(){
		return Gossip.Sm/Gossip.W2; 
	}


	public Socket connect(String ip, int port) throws UnknownHostException,
			IOException, ClassNotFoundException {
		return new Socket(ip, port);
	}

	void acceptConnections() throws IOException {
		while (true) {
			Socket sock = serverSocket.accept();
			new ClientSocketThread(this, sock).start();
		}
	}

	synchronized void handleMsg(GossipDTO gmsg, Socket s) throws IOException {

		int gossipId = gmsg.getGossipId();
		// Update itself with the info from msg received	
		if(gossipId == Gossip.currentGossip){	
			Gossip.W1 += gmsg.getW1();
			Gossip.Su += gmsg.getSu();
			Gossip.Sn += gmsg.getSn();
			Gossip.Sa += gmsg.getSa();
			Gossip.W2 += gmsg.getW2();
			Gossip.Ss += gmsg.getSs();
			Gossip.Sm += gmsg.getSm();
		// Else update currentGossip and sum msg received with local values
		} else if(gossipId > Gossip.currentGossip){
			Gossip.currentGossip = gossipId;
			Gossip.W1 = 0 + gmsg.getW1();
			Gossip.Su = Gossip.localSu + gmsg.getSu();
			Gossip.Sn = 1 + gmsg.getSn();
			Gossip.Sa = Gossip.localSa + gmsg.getSa();
			Gossip.W2 = 1 + gmsg.getW2();
			Gossip.Ss = Gossip.localSs + gmsg.getSs();
			Gossip.Sm = Gossip.localSm + gmsg.getSm();
		}
	}

	//Returns the index of the next peer to send a message to in the peers list
	public PeerAddress getNextPeer(){
		List<PeerAddress> list = peerThread.getPeerList();
		int numPeers = peerThread.getPeerSize();
		if(numPeers == 1){
			return list.get(0);
		}
		
		Double r = Math.random();
		r *= numPeers;
	
		return r==numPeers ? list.get(numPeers-1) : list.get((int)Math.floor(r));
	}

	@SuppressWarnings("deprecation")
	public void initThreads() throws IOException{

		/* Commandline thread */
		Thread cmdThread = new Thread() {				
				@Override
				public void run() {
					BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
					while(true) {
						try	{
							
							String line = br.readLine();
							// ADD REMOVE USERS
							if(line.length() > 5 && line.substring(0,5).equals("users")){		
								updateNumUsers(Integer.parseInt(line.substring(6,line.length())));					
								//System.out.println(Integer.parseInt(line.substring(6,line.length())));
							}
							// ADD REMOVE ACTIVE USERS
							if(line.length() > 5 && line.substring(0,5).equals("activ")){		
								updateNumActive(Integer.parseInt(line.substring(6,line.length())));					
								//System.out.println(Integer.parseInt(line.substring(6,line.length())));
							}
							// ADD REMOVE FILES
							else if(line.length() > 5 && line.substring(0,5).equals("files")){		
								updateNumFiles(Integer.parseInt(line.substring(6,line.length())));					
							}
							// ADD REMOVE MB
							else if(line.length() > 5 && line.substring(0,5).equals("megab")){		
								updateNumMB(Integer.parseInt(line.substring(6,line.length())));					
							}
							// SHOW GOSSIP
							else if(line.equals("gossip")){
								showGossip();					
							}
							// SHOW PEERS
							else if(line.equals("peers")){	
								showPeers();
							}
						} 
						catch (IOException e) {  e.printStackTrace(); }
					}
				}
			}; 
			cmdThread.start();

	        
			/* Reset gossip thread which will only run for node zero, responsible for */
			/* sending gossip messages with a new id once a certain amount of time */
			/* has passed, so that any connection issues will not make the gossip values */
			/* diverge from the real values */  

			Thread resetThread = new Thread() {
				@Override
				public void run() {	
					try {
						while(true){
							sleep(RESET); 
							if(peerThread.getPeerSize() > 0){
								System.out.println("Reset: peerSize > 0");
								// Get the peerId which is currently responsible for reset
								Number160 resetId = new Number160(0);
								FutureDHT futureDHT = peerThread.getPeer().get(resetId).start();
						        futureDHT.awaitUninterruptibly();
						        Collection<Number160> keys = futureDHT.getKeys();
						        for(Number160 key : keys){
						        	System.out.println("key = " + key.toString());
						        }
						        // If it's us, we send the new gossip message
								if(Gossip.id.compareTo(resetId) == 0){
									System.out.println("GOSSIP RESET!");
									String ip = getNextPeer().getInetAddress().getHostAddress();
									sendReset(ip, LISTENING_PORT);
								}	
							}
						}
					} 
					catch (Exception ie){ ie.printStackTrace(); }
				}
			};
			resetThread.start();
			

			/* Thread responsible for sending a gossip message to one of this node's peers */
			Thread sendThread = new Thread(){
				@Override
				public void run() {	
					try {
						while(true){
							sleep(SEND); 
							// Send a new gossip message to another peer
							if(peerThread.getPeerSize() > 0){
								String ip = getNextPeer().getInetAddress().getHostAddress();
								sendGossip(ip, LISTENING_PORT);
							}
						}
					} 
					catch (Exception ie){ ie.printStackTrace(); }
				}
			};
			sendThread.start();

			/* Thread responsible for listening to incoming connections for gossip */
			Thread recThread = new Thread(){
				@Override
				public void run() {	
					try{
						acceptConnections();
					}catch(Exception e){ e.printStackTrace(); }
				}
			};
			recThread.start();
	}
	public void sendGossip(String ip, int port) throws IOException{
		Gossip.W1 /= 2;
		Gossip.Su /= 2;
		Gossip.Sn /= 2;
		Gossip.Sa /= 2;
		Gossip.W2 /= 2;
		Gossip.Ss /= 2;
		Gossip.Sm /= 2;


		try{
			Socket cs = connect(ip, port);

			System.out.printf("Sending Gossip Message to"+ cs.toString() + " with gossipId %d\n", Gossip.currentGossip);
			new ObjectOutputStream(cs.getOutputStream()).writeObject(new GossipDTO(Gossip.currentGossip , Gossip.id, Gossip.W1, Gossip.Su, Gossip.Sn, Gossip.Sa, Gossip.W2, Gossip.Ss, Gossip.Sm));
			cs.close();
		}catch (ClassNotFoundException cnfe) {  cnfe.printStackTrace(); }
	}

	public void sendReset(String ip, int port) throws IOException{
		try{
			Socket cs = connect(ip, port);

			System.out.println("Gossip reset!");
			new ObjectOutputStream(cs.getOutputStream()).writeObject(new GossipDTO(Gossip.currentGossip + 1, Gossip.id, 1, 0, 0, 0, 0, 0, 0));
			cs.close();
		}catch (ClassNotFoundException cnfe){  cnfe.printStackTrace(); }
	}

}