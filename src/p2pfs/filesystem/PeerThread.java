package p2pfs.filesystem;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Inet4Address;
import java.net.ServerSocket;
import java.net.Socket;

import p2pfs.filesystem.types.dto.FileSystemDTO;

import net.tomp2p.connection.Bindings;
import net.tomp2p.futures.BaseFuture;
import net.tomp2p.futures.BaseFutureListener;
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
	final private int dhtPort = 9999;
	
	/**
	 * Arrays of addresses for the bootstraping nodes.
	 */
	final private String[] bootstrapNodes = {"127.0.0.1"};
	
	/**
	 * Network interface to be used by the DHT implementation.
	 */
	final private String iface = "eth0";
	
	/**
	 * Socket where requests can be received.
	 */
	final private ServerSocket fsSocket;
	
	/**
	 * Port to be used for the file system access socket.
	 */
	final private int fsPort = 9998;
	
	/**
	 * Number of connection waiting in the queue.
	 */
	final private int backlog = 10;
	
	/**
	 * Constructor.
	 * Tries to enter the DHT.
	 * @param peerId
	 * @throws IOException
	 */
	public PeerThread(String peerId) throws IOException {
		super("PeerThread");
		// setup the dht connection
        peer = new PeerMaker(Number160.createHash(peerId)).
        		setPorts(this.dhtPort).
        		setBindings(new Bindings(this.iface)).
        		makeAndListen();
        for(String addr : this.bootstrapNodes)
        {
            FutureBootstrap fb = peer.
            		bootstrap().
            		setInetAddress(Inet4Address.getByName(addr)).
            		setPorts(this.dhtPort).
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
		// setup the fs socket
		this.fsSocket = new ServerSocket(fsPort, backlog);
	}
	
	/**
	 * Method that encapsulates the main functionality.
	 */
	@Override
	public void run() {
		while(true) {
			Socket clientConnection = null;
			try {
				clientConnection = this.fsSocket.accept();
				ObjectInputStream in = 
						new ObjectInputStream(clientConnection.getInputStream());
				FileSystemDTO dto = 
						(FileSystemDTO)in.readObject();
				dto.execute(peer).addListener(
						// TODO - implementation needs the client socket!
						new BaseFutureListener<BaseFuture>() {

							@Override
							public void operationComplete(BaseFuture future)
									throws Exception {
								// TODO Auto-generated method stub

							}

							@Override
							public void exceptionCaught(Throwable t) 
									throws Exception {
								// TODO Auto-generated method stub

							}
						});
			} catch (IOException e) {
				// if any socket operation fails
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// if in.readObject fails
				e.printStackTrace();
			}
		}
	}
}
