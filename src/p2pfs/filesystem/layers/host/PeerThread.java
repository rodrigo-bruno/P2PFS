package p2pfs.filesystem.layers.host;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Inet4Address;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import p2pfs.filesystem.Main;
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
import net.tomp2p.peers.PeerAddress;
import net.tomp2p.storage.StorageGeneric;

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
		 * Client object output stream. Used to send the answer.
		 */
		protected ObjectOutputStream oos;
		
		/**
		 * Constructor. 
		 * @param clientConnection
		 */
		public FSFuture(ObjectOutputStream oos) { this.oos = oos; }

		/**
		 * Default method for handling exceptions.
		 * This method will send the exception to the client side.
		 * @param t - the exception to be forwarded.
		 */
		@Override
		public void exceptionCaught(Throwable t) throws Exception {
			oos.writeObject(new ExceptionDTO(t));
			oos.flush();
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
		public GetFuture(ObjectOutputStream oos) { super(oos); }

		/**
		 * Method that will be called when a get operation completes.
		 * The procedure is to return an OperationCompleteDTO with the answer.
		 * Note that if the operation was not successful, a null object is sent.
		 * @param future - the future object with information regarding the 
		 * operation.
		 */
		@Override
		public void operationComplete(FutureDHT future) throws Exception {
			if(future.isSuccess()) {
				oos.writeObject(new OperationCompleteDTO(future.getData().getObject(), true));
			} else {
				oos.writeObject(new OperationCompleteDTO(null, false));
			}
			oos.flush();
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
		public PutFuture(ObjectOutputStream oos) 
		{ super(oos); }

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
			oos.writeObject(new OperationCompleteDTO(null, future.isSuccess()));
			oos.flush();
		}
	}
	
	/**
	 * The DHT object representing this particular peer.
	 */
	final private Peer peer;
	
	/**
	 * FIXME
	 */
	final public StorageGeneric storage;
	
	/**
	 * Port to be used for communication within the DHT.
	 */
	final private int dhtPort = 9999;
	
	/**
	 * Network interface to be used by the DHT implementation.
	 * FIXME: check if this is necessary.
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
	 * This list will hold all the client threads ever created.
	 * This is needed for cleaning all threads properly.
	 * Waring: in a long running execution, this list might get big. So it would
	 * be nice to have some mechanism to clean it periodically.
	 */
	private ArrayList<Thread> clientThreads = null; 
	
	/**
	 * Flag that indicates if the thread was interrupted.
	 */
	private boolean interrupted = false;
	
	/**
	 * Constructor.
	 * Tries to enter the DHT.
	 * @param peerId
	 * @throws IOException
	 */
	public PeerThread(Number160 peerId) throws IOException {
		super("PeerThread");
		// setup the dht connection
		PeerMaker pm = new PeerMaker(peerId);
		this.storage = pm.getStorage();
        peer = 	pm.setPorts(this.dhtPort).
        		setEnableIndirectReplication(true).
        		setBindings(new Bindings()).
        		makeAndListen();
        for(String addr : Main.BOOTSTRAP_NODES) {
            FutureBootstrap fb = peer.
            		bootstrap().
            		setInetAddress(Inet4Address.getByName(addr)).
            		setPorts(this.dhtPort).
            		start();
            fb.awaitUninterruptibly();

            if (fb.getBootstrapTo() != null) {
                peer.
                discover().
                setPeerAddress(fb.getBootstrapTo().iterator().next()).
                start().
                awaitUninterruptibly();
                
                // Print known DHT nodes.
                List<PeerAddress> list = peer.getPeerBean().getPeerMap().getAll();
                System.out.println("Successful bootstrap");
                for(PeerAddress adr : list ) {
                	System.out.println("Peer node in bean: " + adr.getInetAddress().getHostAddress());
                }
                
                break;
            }            
        }
		// setup the FS socket
		this.fsSocket = new ServerSocket(FILESYSTEM_PORT, backlog);
		// initialize the client threads list
		this.clientThreads = new ArrayList<Thread>();
	}
	
	// Used by the Gossip
	public Number160 getPeerID(){
		return this.peer.getPeerID();
	}
	public List<PeerAddress> getPeerList(){
		return this.peer.getPeerBean().getPeerMap().getAll();
	}
	public int getPeerSize(){
		return getPeerList().size();
	}
	public Peer getPeer(){
		return peer;
	}
	public int getNumberClients() {
		return clientThreads.size();
	}
	public StorageGeneric getStorage() {
		return storage;
	}
	
	/**
	 * Method that encapsulates the main functionality.
	 * For each new incoming connection, a new thread is created.
	 */
	@Override
	public void run() {
		try {
			System.out.println("Peer Thread Running!");
			this.fsSocket.setSoTimeout(1000);
			while(	!this.fsSocket.isClosed() && 
					!Thread.currentThread().isInterrupted()) {
				Socket clientConnection = null;
				try { clientConnection = this.fsSocket.accept(); }
				// This is the standard procedure to stop this thread.
				// Just let the finally block clean the stuff and exit.
				catch (SocketTimeoutException e) { }
				if(clientConnection != null) {
					Thread t = new Thread(this.getClientRunnable(clientConnection));
					this.clientThreads.add(t);	
					t.start();
					System.out.println("New client!");
				}
			}
		}
		// if any other socket operation fails
		catch (IOException e)  { e.printStackTrace(); }
		finally {
			System.out.println("Peer Thread Finalizing!");
			// threads already dead will not feel the interrupt =)
			for(Thread t : this.clientThreads) { t.interrupt(); }
			try { 
				this.fsSocket.close();
				this.peer.shutdown();
			}
			// if closing the socket fails
			catch (IOException e) { e.printStackTrace(); } 
		}
	}
	
	/**
	 * Re-implementation of the interrupt method.
	 * We use this implementation because we don't want an exception. Note that
	 * the peer.shutdown() may be blocking.
	 */
	@Override
	public void interrupt() { this.interrupted = true; }
	
	/**
	 * Re-implementation of the isInterrupted method.
	 */
	@Override
	public boolean isInterrupted() { return this.interrupted; }
	
	/**
	 * Method that returns the Runnable that will handle a client connection.
	 * @param clientConnection - the client connection.
	 * @return a runnable object.
	 */
	public Runnable getClientRunnable(final Socket clientConnection) {
		return new Runnable() {
			
			/**
			 * The actual code that handles a client connection.
			 * The thread running this code will be in an infinite loop waiting
			 * for requests to arrive. Once a request arrives, it is executed
			 * and a future object will have a listener ready to react when the
			 * answer is ready (which is handled by other thread).
			 * 
			 * The communication protocol is simple: 
			 * - this thread listens for incoming objects and executes requests 
			 * over the DHT;
			 * - the future listener answer to the client connection;
			 * Note that if the client issues multiple requests at the same time,
			 * we don't assure ordering! 
			 */
			@Override
			public void run() {
				try {
					ObjectInputStream ois = new ObjectInputStream(clientConnection.getInputStream());
					ObjectOutputStream oos = new ObjectOutputStream(clientConnection.getOutputStream());
					clientConnection.setSoTimeout(1000);
					while(	!clientConnection.isClosed() && 
							!Thread.currentThread().isInterrupted()) {
						RequestDTO dto = null;
						try { dto = (RequestDTO)ois.readObject(); }
						// Don't worry, this exception will happen every second.
						// This is used to make the thread check if it was 
						// interrupted.
						catch (SocketTimeoutException e) { }
						// FIXME: this call should be synchronized just for safety.
						if (dto != null) { dto.execute(peer, oos); }
					}
				}
				// if the client side closes the socket.
				catch(EOFException e) { }
				// if in.readObject fails
				catch (ClassNotFoundException e) { e.printStackTrace();	}
				// if any socket operation fails
				catch (IOException e) {	e.printStackTrace(); } 
				finally {
					System.out.println("Client killed!");
					try { clientConnection.close(); }
					// if closing the socket fails
					catch (IOException e) { e.printStackTrace(); }
				}
			}
		};
	}
	
}
