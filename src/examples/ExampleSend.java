package examples;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.jboss.netty.buffer.ByteBufferBackedChannelBuffer;

import net.tomp2p.p2p.Peer;
import net.tomp2p.p2p.PeerMaker;
import net.tomp2p.p2p.builder.SendBuilder;
import net.tomp2p.p2p.builder.SendDirectBuilder;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.PeerAddress;
import net.tomp2p.rpc.ObjectDataReply;

public class ExampleSend {
	
	public static void main(String[] args) throws IOException {
		int port1 = 10000;
		int port2 = 9999;
		Peer peer1 = null;
		Peer peer2 = null;
		try {
		    peer1 = new PeerMaker(new Number160(port1)).setPorts(port1).makeAndListen();
		    peer2 = new PeerMaker(new Number160(port2)).setPorts(port2).makeAndListen();
		    //attach reply handler
		    peer2.setObjectDataReply(new ObjectDataReply() {
		        @Override
		        public Object reply(final PeerAddress sender, final Object request) throws Exception {
		            return "world!";
		        }
		    });
		    SendDirectBuilder sb = peer1.sendDirect(
		    		peer2.getPeerAddress()).setBuffer(
		    				new ByteBufferBackedChannelBuffer(
		    						ByteBuffer.wrap(
		    								new String("ola").getBytes())));
		    sb.getFutureChannelCreator().awaitUninterruptibly();
		} finally {
		    peer1.shutdown();
		    peer2.shutdown();
		}		
	}
	
}

