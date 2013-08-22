/* 
 * Copyright 2010 Aalto University, ComNet
 * Released under GPLv3. See LICENSE.txt for details. 
 */
package core;

import java.util.ArrayList;
import java.util.List;

import routing.MessageRouter;

/**
 * A connection between two DTN nodes.
 */
public abstract class Connection {
	protected DTNHost toNode;
	protected NetworkInterface toInterface;
	protected DTNHost fromNode;
	protected NetworkInterface fromInterface;
	protected DTNHost msgFromNode;

	private boolean isUp;
	protected Message msgOnFly;
	/** how many bytes this connection has transferred */
	protected int bytesTransferred;

	//YSPARK
	private List<Integer> fromNodeForwardableEphemeralAddresses;
	private List<Integer> toNodeForwardableEphemeralAddresses;
	
	
	/**
	 * Creates a new connection between nodes and sets the connection
	 * state to "up".
	 * @param fromNode The node that initiated the connection
	 * @param fromInterface The interface that initiated the connection
	 * @param toNode The node in the other side of the connection
	 * @param toInterface The interface in the other side of the connection
	 */
	public Connection(DTNHost fromNode, NetworkInterface fromInterface, 
			DTNHost toNode, NetworkInterface toInterface) {
		this.fromNode = fromNode;
		this.fromInterface = fromInterface;
		this.toNode = toNode;
		this.toInterface = toInterface;
		this.isUp = true;
		this.bytesTransferred = 0;
		
		/***************************************************************/
		/*
		// YSPARK
		if(fromNode.getMessageCollection().size() > 0) {
			this.fromNodeForwardableEphemeralAddresses = new ArrayList<Integer>();
			buildForwardableEphemeralAddresses(fromNode, toNode, fromNodeForwardableEphemeralAddresses);
		}
				
		if(toNode.getMessageCollection().size() > 0) {
			this.toNodeForwardableEphemeralAddresses = new ArrayList<Integer>();
			buildForwardableEphemeralAddresses(toNode, fromNode, toNodeForwardableEphemeralAddresses);
		}
		*/
		/***************************************************************/
	}


	/***************************************************************/
	// YSPARK
	protected void buildForwardableEphemeralAddresses(DTNHost fromNode, DTNHost toNode, List<Integer> forwardableEphemeralAddresses) {
				
		for(Message m : fromNode.getRouter().getMessageCollection()) {
			
			if(toNode.getReceivableEphemeralAddresses().size() != 125) 
				System.out.printf("wrong receivableEphemeralAddresses: %d, %d\n", toNode.getPermanentAddress(), toNode.getReceivableEphemeralAddresses().size());
			
			if(toNode.getReceivableEphemeralAddresses().contains(m.getToEphemeralAddress())) {
				forwardableEphemeralAddresses.add(m.getToEphemeralAddress());
			}
		}
		
		
		/*
		if(forwardableEphemeralAddresses.size() == 0) { 
			System.out.printf("wrong forwardableEphemeralAddresses: %d->%d: %d\n", 
					fromNode.getPermanentAddress(), toNode.getPermanentAddress(), 
					toNode.getReceivableEphemeralAddresses().size()
					);
			
			for(Message m : fromNode.getRouter().getMessageCollection()) {					
				System.out.printf("message:%d\n", m.getToEphemeralAddress());
			}
			System.out.println("-----");
			System.out.println(toNode.getReceivableEphemeralAddresses().toString());
			
		}
		*/

	}
	
	public List<Integer> getForwarableEphemeralAddresses(int permanentAddress) {
		if(permanentAddress == this.fromNode.getPermanentAddress()) {
			
			this.fromNodeForwardableEphemeralAddresses = new ArrayList<Integer>();
			buildForwardableEphemeralAddresses(fromNode, toNode, fromNodeForwardableEphemeralAddresses);			
			return fromNodeForwardableEphemeralAddresses;
		}
		else {
			this.toNodeForwardableEphemeralAddresses = new ArrayList<Integer>();
			buildForwardableEphemeralAddresses(toNode, fromNode, toNodeForwardableEphemeralAddresses);
			return toNodeForwardableEphemeralAddresses;
		}
	}
	
	/***************************************************************/
	
	
	/**
	 * Returns true if the connection is up
	 * @return state of the connection
	 */
	public boolean isUp() {
		return this.isUp;
	}

	/**
	 * Returns true if the given node is the initiator of the connection, false
	 * otherwise
	 * @param node The node to check
	 * @return true if the given node is the initiator of the connection
	 */
	public boolean isInitiator(DTNHost node) {
		return node == this.fromNode;
	}

	/**
	 * Sets the state of the connection.
	 * @param state True if the connection is up, false if not
	 */
	public void setUpState(boolean state) {
		this.isUp = state;
	}

	/**
	 * Sets a message that this connection is currently transferring. If message
	 * passing is controlled by external events, this method is not needed
	 * (but then e.g. {@link #finalizeTransfer()} and 
	 * {@link #isMessageTransferred()} will not work either). Only a one message
	 * at a time can be transferred using one connection.
	 * @param m The message
	 * @return The value returned by 
	 * {@link MessageRouter#receiveMessage(Message, DTNHost)}
	 */
	public abstract int startTransfer(DTNHost from, Message m);

	/**
	 * Calculate the current transmission speed from the information
	 * given by the interfaces, and calculate the missing data amount.
	 */
	public void update() {};

	/**
     * Aborts the transfer of the currently transferred message.
     */
	public void abortTransfer() {
		assert msgOnFly != null : "No message to abort at " + msgFromNode;	
		int bytesRemaining = getRemainingByteCount();

		this.bytesTransferred += msgOnFly.getSize() - bytesRemaining;

		getOtherNode(msgFromNode).messageAborted(this.msgOnFly.getId(),
				msgFromNode, bytesRemaining);
		clearMsgOnFly();
	}	

	/**
	 * Returns the amount of bytes to be transferred before ongoing transfer
	 * is ready or 0 if there's no ongoing transfer or it has finished
	 * already
	 * @return the amount of bytes to be transferred
	 */
	public abstract int getRemainingByteCount();

	/**
	 * Clears the message that is currently being transferred.
	 * Calls to {@link #getMessage()} will return null after this.
	 */
	protected void clearMsgOnFly() {
		this.msgOnFly = null;
		this.msgFromNode = null;		
	}

	/**
	 * Finalizes the transfer of the currently transferred message.
	 * The message that was being transferred can <STRONG>not</STRONG> be
	 * retrieved from this connections after calling this method (using
	 * {@link #getMessage()}).
	 */
	public void finalizeTransfer() {
		assert this.msgOnFly != null : "Nothing to finalize in " + this;
		assert msgFromNode != null : "msgFromNode is not set";
		
		this.bytesTransferred += msgOnFly.getSize();

		getOtherNode(msgFromNode).messageTransferred(this.msgOnFly.getId(),
				msgFromNode);
		
		
		//YSPARK
		System.out.printf("MSG Forward Done: From(%d/%d), To(%d/%d), Msg(%s, %d/%d->%d/%d)\n\n",
				this.fromNode.getPermanentAddress(), this.fromNode.getEphemeralAddress(),
				this.toNode.getPermanentAddress(), this.toNode.getEphemeralAddress(),
				msgOnFly.getId(),
				msgOnFly.getFrom().getPermanentAddress(), msgOnFly.getFrom().getEphemeralAddress(),
				msgOnFly.getTo().getPermanentAddress(), msgOnFly.getTo().getEphemeralAddress()
				);
		
		
		clearMsgOnFly();
	}

	/**
	 * Returns true if the current message transfer is done 
	 * @return True if the transfer is done, false if not
	 */
	public abstract boolean isMessageTransferred();

	/**
	 * Returns true if the connection is ready to transfer a message (connection
	 * is up and there is no message being transferred).
	 * @return true if the connection is ready to transfer a message
	 */
	public boolean isReadyForTransfer() {
		return this.isUp && this.msgOnFly == null; 
	}

	/**
	 * Gets the message that this connection is currently transferring.
	 * @return The message or null if no message is being transferred
	 */	
	public Message getMessage() {
		return this.msgOnFly;
	}

	/** 
	 * Gets the current connection speed
	 */
	public abstract double getSpeed();	

	/**
	 * Returns the total amount of bytes this connection has transferred so far
	 * (including all transfers).
	 */
	public int getTotalBytesTransferred() {
		if (this.msgOnFly == null) {
			return this.bytesTransferred;
		}
		else {
			if (isMessageTransferred()) {
				return this.bytesTransferred + this.msgOnFly.getSize();
			}
			else {
				return this.bytesTransferred + 
				(msgOnFly.getSize() - getRemainingByteCount());
			}
		}
	}

	/**
	 * Returns the node in the other end of the connection
	 * @param node The node in this end of the connection
	 * @return The requested node
	 */
	public DTNHost getOtherNode(DTNHost node) {
		if (node == this.fromNode) {
			return this.toNode;
		}
		else {
			return this.fromNode;
		}
	}

	/**
	 * Returns the interface in the other end of the connection
	 * @param i The interface in this end of the connection
	 * @return The requested interface
	 */
	public NetworkInterface getOtherInterface(NetworkInterface i) {
		if (i == this.fromInterface) {
			return this.toInterface;
		}
		else {
			return this.fromInterface;
		}
	}

	/**
	 * Returns a String presentation of the connection.
	 */
	public String toString() {
		return fromNode + "<->" + toNode + " (" + getSpeed() + "Bps) is " +
		(isUp() ? "up":"down") + 
		(this.msgOnFly != null ? " transferring " + this.msgOnFly  + 
				" from " + this.msgFromNode : "");
	}

}

