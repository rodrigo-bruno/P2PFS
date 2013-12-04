package p2pfs.filesystem.types.dto;
import java.io.Serializable;

public class GossipDTO implements Serializable {

	/**
	 * serialization id.
	 */
	private static final long serialVersionUID = 1L;

	private int gossipId;
	private int source;
	private double W1;
	private float Su;
	private float Sn;
	private float Sa;
	private double W2;
	private float Ss;
	private float Sm;

	public GossipDTO(
			int gossipId, 
			int source, 
			double W1, 
			float Su, 
			float Sn, 
			float Sa, 
			double W2, 
			float Ss, 
			float Sm) { 
		this.gossipId = gossipId;
		this.source = source;
		this.W1 = W1;
		this.Su = Su;
		this.Sn = Sn;
		this.Sa = Sa;
		this.W2 = W2;
		this.Ss = Ss;
		this.Sm = Sm;
	}
	
	public int getGossipId() { return this.gossipId; }
	public int getSource() { return this.source; }
	public double getW1() { return this.W1; }
	public float getSu() { return this.Su; }
	public float getSn() { return this.Sn; }
	public float getSa() { return this.Sa; }
	public double getW2() { return this.W2; }
	public float getSs() { return this.Ss; }
	public float getSm() { return this.Sm; }
	
}
