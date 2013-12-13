package p2pfs.filesystem.layers.host;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;

import net.tomp2p.peers.Number160;
import net.tomp2p.peers.Number480;
import net.tomp2p.peers.PeerAddress;
import net.tomp2p.storage.Data;
import net.tomp2p.storage.KeyLock;
import net.tomp2p.storage.Storage;
import p2pfs.filesystem.Main;
import p2pfs.filesystem.types.dto.*;
import p2pfs.filesystem.types.fs.Directory;
import p2pfs.filesystem.types.fs.File;

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
	public static final int GOSSIP_MGMT_PORT = 40005;
	public static final int REPLICATION_FACTOR = 5;
	public static final String[] responsibleNodes = {"planetlab-1.tagus.ist.utl.pt", "planetlab-2.tagus.ist.utl.pt"};
	
	private ServerSocket serverSocket;
	private ServerSocket mgmtSocket;
	
	public static Number160 id;
	public static int currentGossip = 0;
	public static String hostname;

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
			//use the same port for gossip listening on all nodes
			serverSocket = new ServerSocket(LISTENING_PORT); 
			//serverSocket.bind(null);
			System.out.println("Listening on port " + serverSocket.getLocalPort());
			
			//use the same port for gossip management on all nodes
			mgmtSocket = new ServerSocket(GOSSIP_MGMT_PORT); 
			//serverSocket.bind(null);
			System.out.println("Management listening on port " + mgmtSocket.getLocalPort());
			hostname = InetAddress.getLocalHost().getHostName();
			System.out.println("Hostname: "+ hostname);

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
		int rep = (int) (getCountNodes() < REPLICATION_FACTOR ? getCountNodes() : REPLICATION_FACTOR);
		System.out.printf("Num of nodes= %f\n", getCountNodes());
		System.out.printf("Total num users= %f\n", getNumUsers()/rep);
		System.out.printf("Total num active users= %f\n", getNumActive()); // not calculated based on storage
		System.out.printf("Avg num files per node= %f\n", getNumFiles()/rep);
		System.out.printf("Avg num MB per node= %f\n", getNumMB()/rep);
	}

	/* When there are no peers, the response to the user query is the self values */
	public void showSelf(){
		System.out.printf( "Num of nodes= 1\n" );
		System.out.printf( "Local num users= %f\n", Gossip.localSu );
		System.out.printf( "Local num active users= %f\n", Gossip.localSa);
		System.out.printf( "Local num files= %f\n", Gossip.localSs);
		System.out.printf( "Local num MB= %f\n", Gossip.localSm);
	}		

	public double getCountNodes(){
		return Gossip.Sn/Gossip.W1;
	}

	public void updateNumUsers(float users){
		Gossip.Su += users;	
	}
	public double getNumUsers(){
		return Gossip.Su/Gossip.W1; 
	}

	public void updateNumActive(float users){
		Gossip.Sa += users;
	}
	public double getNumActive(){
		return Gossip.Sa/Gossip.W1; 
	}

	public void updateNumFiles(float files){
		Gossip.Ss += files;
	}
	public double getNumFiles(){
		return Gossip.Ss/Gossip.W2; 
	}

	public void updateNumMB(float mb){
		Gossip.Sm += mb;
	}
	public double getNumMB(){
		return Gossip.Sm/Gossip.W2; 
	}
	public void updateGossip(int usern, int activen, int filesn, int blockn){

		float oldSu = Gossip.localSu;
		float oldSa = Gossip.localSa;
		float oldSs = Gossip.localSs;
		float oldSm = Gossip.localSm;
		Gossip.localSu = usern;
		Gossip.localSa = activen;
		Gossip.localSs = filesn;
		Gossip.localSm = blockn*131099/1024/1024;
		updateNumUsers(Gossip.localSu - oldSu);
		updateNumActive(Gossip.localSa - oldSa);
		updateNumFiles(Gossip.localSs - oldSs);
		updateNumMB(Gossip.localSm - oldSm);
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

	public void initThreads() throws IOException{
      
		/* Reset gossip thread which will only run for node zero, responsible for */
		/* sending gossip messages with a new id once a certain amount of time */
		/* has passed, so that any connection issues will not make the gossip values */
		/* diverge from the real values */  
		
		Thread resetThread = new Thread() {
			@Override
			public void run() {	
				while(true){
					if(peerThread.getPeerSize() > 0){
						// Get the peerId which is currently responsible for reset
						for(String node : Gossip.responsibleNodes){
							try{
						        // If it's us, we send the new gossip message
								if(Gossip.hostname.equals(node)){
									System.out.println("GOSSIP RESET!\n");
									String ip = getNextPeer().getInetAddress().getHostAddress();
									sendReset(ip, LISTENING_PORT);
									break;
								}	
								// If not we check the connectivity of that node to see if it's available
								else{
									Socket cs = connect(node, GOSSIP_MGMT_PORT);
									System.out.printf("RESET by node: "+node+"\n");
									cs.close();
									break;
								}
							}
							catch(UnknownHostException uhe) {}
							catch(ConnectException ce){  }
							catch(Exception ie){ ie.printStackTrace(); }
						}

					}
					try{
						sleep(RESET); 
					}catch(InterruptedException ie){ie.printStackTrace();}
				}
 
			}
		};
		resetThread.start();
		

		/* Thread responsible for sending a gossip message to one of this node's peers */
		Thread sendThread = new Thread(){
			@Override
			public void run() {	
				while(true){
					try {
						sleep(SEND); 
						// Send a new gossip message to another peer
						if(peerThread.getPeerSize() > 0){
							String ip = getNextPeer().getInetAddress().getHostAddress();
							sendGossip(ip, LISTENING_PORT);
						}
					} 
					catch(ConnectException ce){  }
					catch (Exception ie){ ie.printStackTrace(); }
				}
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
		
		int blockn = 0;
		int usern = 0;
		int filesn = 0;
		int runningn = peerThread.getNumberClients(); // the host is a self client
		int activen = runningn + (Main.USERNAME == null ? -1 : 0);
		KeyLock<Storage> keylock = peerThread.getStorage().getLockStorage();
		
		Lock lock = keylock.lock(peerThread.getStorage());
		for(Map.Entry<Number480, Data> entry : peerThread.storage.map().entrySet()) {
			int size = entry.getValue().getLength();
			if(size == File.BLOCK_SIZE + 27) { blockn++; }
			else 
				try{
					if(entry.getValue().getObject() instanceof Directory){
						filesn += ((Directory) entry.getValue().getObject()).getTotalNumberFiles();
						usern++; 
					}
					else { System.out.println("WARNING: Storage object not recognized (size="+size+"!"); }
				}catch(ClassNotFoundException cnfe){cnfe.printStackTrace();}
		}
		keylock.unlock(peerThread.storage, lock);
		updateGossip(usern, activen, filesn, blockn);
		//System.out.println("Users="+usern+", blocks="+blockn+", files="+filesn+", running="+runningn+", active="+activen);

		
		
		try{
			Socket cs = connect(ip, port);

			System.out.printf("Sending Gossip msg to "+ cs.getInetAddress().getHostAddress()+":"+
					cs.getPort() + " with gossipId %d\n", Gossip.currentGossip);
			new ObjectOutputStream(cs.getOutputStream()).writeObject(new GossipDTO(Gossip.currentGossip , Gossip.id, Gossip.W1, Gossip.Su, Gossip.Sn, Gossip.Sa, Gossip.W2, Gossip.Ss, Gossip.Sm));
			cs.close();
		}catch (ClassNotFoundException cnfe) {  cnfe.printStackTrace(); }
	}

	public void sendReset(String ip, int port) throws IOException{
		try{
			Socket cs = connect(ip, port);

			new ObjectOutputStream(cs.getOutputStream()).writeObject(new GossipDTO(Gossip.currentGossip + 1, Gossip.id, 1, 0, 0, 0, 0, 0, 0));
			cs.close();
		}catch (ClassNotFoundException cnfe){  cnfe.printStackTrace(); }
	}

}