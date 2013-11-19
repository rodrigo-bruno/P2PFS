package p2pfs.filesystem;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Inet4Address;
import java.net.ServerSocket;
import java.net.Socket;

import p2pfs.filesystem.types.dto.ExceptionDTO;
import p2pfs.filesystem.types.dto.OperationCompleteDTO;
import p2pfs.filesystem.types.dto.RequestDTO;

import net.tomp2p.connection.Bindings;
import net.tomp2p.futures.BaseFutureListener;
import net.tomp2p.futures.FutureBootstrap;
import net.tomp2p.futures.FutureDHT;
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
	 * Base class for out Futures. These classes will be used to provide an 
	 * asynchronous behavior to our implementation.
	 * Remember that this thread may be receiving requests from multiple sources
	 * like the local host or any other client,
	 */
	public static abstract class FSFuture implements BaseFutureListener<FutureDHT> {
		
		/**
		 * Client Socket. Used to send the answer.
		 */
		protected Socket clientConnection;
		
		/**
		 * Constructor. 
		 * @param clientConnection
		 */
		public FSFuture(Socket clientConnection) 
		{ this.clientConnection = clientConnection; }

		/**
		 * Default method for handling exceptions.
		 * This method will send the exception to the client side.
		 * @param t - the exception to be forwarded.
		 */
		@Override
		public void exceptionCaught(Throwable t) throws Exception {
			ObjectOutputStream out = 
					new ObjectOutputStream(this.clientConnection.getOutputStream());
			out.writeObject(new ExceptionDTO(t));
			out.flush();
		}
	}
	
	/**
	 * Get Future.
	 */
	public static class GetFuture extends FSFuture {
		
		/**
		 * Constructor. 
		 * @param clientConnection
		 */
		public GetFuture(Socket clientConnection) 
		{ super(clientConnection); }

		/**
		 * Method that will be called when a get operation completes.
		 * The procedure is to return an OperationCompleteDTO with the answer.
		 * Note that if the operation was not successful, a null object is sent.
		 * @param future - the future object with information regarding the 
		 * operation.
		 */
		@Override
		public void operationComplete(FutureDHT future) throws Exception {
			ObjectOutputStream out = 
					new ObjectOutputStream(this.clientConnection.getOutputStream());
			if(future.isSuccess()) {
				out.writeObject(new OperationCompleteDTO(future.getData().getObject(), true));
			} else {
				out.writeObject(new OperationCompleteDTO(null, false));
			}
			out.flush();
		}
	}
	
	/**
	 * Put Future.
	 */
	public static class PutFuture extends FSFuture {

		/**
		 * Constructor.
		 * @param clientConnection
		 */
		public PutFuture(Socket clientConnection) 
		{ super(clientConnection); }

		/**
		 * Method that will be called when a put operation completes.
		 * The procedure is to return an OperationCompleteDTO with the answer.
		 * Note that the object will always be set to null since the put operation
		 * don't return an actual object.
		 * @param future - the future object with information regarding the 
		 * operation.
		 */
		@Override
		public void operationComplete(FutureDHT future) throws Exception {
			ObjectOutputStream out = 
					new ObjectOutputStream(this.clientConnection.getOutputStream());
			out.writeObject(new OperationCompleteDTO(null, future.isSuccess()));
			out.flush();
		}
	}
	
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
	 * This variable is static since it needs to be public (so that the client
	 * implementation can see the port number).
	 * 
	 * FIXME: maybe all this configuration stuff should be moved to a configuration
	 * file.
	 */
	final public static int FILESYSTEM_PORT = 9998;
	
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
		// setup the FS socket
		this.fsSocket = new ServerSocket(FILESYSTEM_PORT, backlog);
	}
	
	/**
	 * Method that encapsulates the main functionality.
	 */
	@Override
	public void run() {
		while(true) {
			Socket clientConnection = null;
			try {
				// FIXME: create a thread to client!
				clientConnection = this.fsSocket.accept();
				ObjectInputStream in = 
						new ObjectInputStream(clientConnection.getInputStream());
				RequestDTO dto = (RequestDTO)in.readObject();
				// FIXME: access to peer object should be synchronized!
				dto.execute(peer, clientConnection);
			} catch (IOException e) {
				// if any socket operation fails
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// if in.readObject fails
				e.printStackTrace();
			} finally {
				try {
					clientConnection.close();
				} catch (IOException e) {
					// if the socket fails to close.
					e.printStackTrace();
				}
			}
		}
	}
}
