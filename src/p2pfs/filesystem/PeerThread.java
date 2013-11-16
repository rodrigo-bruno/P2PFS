package p2pfs.filesystem;

import java.io.IOException;
import java.net.Inet4Address;

import net.tomp2p.connection.Bindings;
import net.tomp2p.futures.FutureBootstrap;
import net.tomp2p.p2p.Peer;
import net.tomp2p.p2p.PeerMaker;
import net.tomp2p.peers.Number160;

/**
 * Class that will hold the server side functionality.
 * It will use directly the DHT and will receive work requests and deliver results
 * through asynchronous calls.
 * This class is used by the DHT bridge and the communication is made through 
 * sockets.
 */
public class PeerThread extends Thread {
	
	/**
	 * The DHT object representing this particular peer.
	 */
	final private Peer peer;
	
	/**
	 * Port to be used for communication within the DHT.
	 */
	final private int listeningPort = 9999;
	
	/**
	 * Arrays of addresses for the bootstraping nodes.
	 */
	final private String[] bootstrapNodes = {"127.0.0.1"};
	
	/**
	 * Network interface to be used by the DHT implementation.
	 */
	final private String iface = "eth0";
	
	/**
	 * Constructor.
	 * Tries to enter the DHT.
	 * @param peerId
	 * @throws IOException
	 */
	public PeerThread(String peerId) throws IOException {
		super("PeerThread");
        peer = new PeerMaker(Number160.createHash(peerId)).
        		setPorts(this.listeningPort).
        		setBindings(new Bindings(this.iface)).
        		makeAndListen();
        for(String addr : this.bootstrapNodes)
        {
            FutureBootstrap fb = peer.
            		bootstrap().
            		setInetAddress(Inet4Address.getByName(addr)).
            		setPorts(this.listeningPort).
            		start();
            if (fb.getBootstrapTo() != null) {
                peer.
                discover().
                setPeerAddress(fb.getBootstrapTo().iterator().next()).
                start().
                awaitUninterruptibly();
                break;
            }	
        }
	}
	
	/**
	 * Method that encapsulates the main functionality.
	 */
	@Override
	public void run() {
		/**
		 * TODO: 
		 * 1 - create socket
		 * 2 - listen for connections
		 * 3 - accept and make request to the DTH, store client socket with the future object
		 * 4 - once the future object is ready, put the answer inside the client socket and close it.
		 */
	}

}
