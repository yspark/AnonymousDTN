/* 
 * Copyright 2010 Aalto University, ComNet
 * Released under GPLv3. See LICENSE.txt for details. 
 */
package core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import movement.MovementModel;
import movement.Path;
import routing.MessageRouter;
import routing.RoutingInfo;

//import BloomFilter;

/**
 * A DTN capable host.
 */
public class DTNHost implements Comparable<DTNHost> {
	private static int nextAddress = 0;
	//YSPARK
	//private int address;
	private int permanentAddress;
	
	private Coord location; 	// where is the host
	private Coord destination;	// where is it going

	private MessageRouter router;
	private MovementModel movement;
	private Path path;
	private double speed;
	private double nextTimeToMove;
	private String name;
	private List<MessageListener> msgListeners;
	private List<MovementListener> movListeners;
	private List<NetworkInterface> net;
	private ModuleCommunicationBus comBus;

	/********************************************************/
	// YSPARK
	//private BloomFilter<Integer> bloomFilter;
	//private ArrayList<Integer> anonymityGroupIDList;	
	//private List<HashMap<Integer, Integer>> anonymityGroupList;
	
	private int ephemeralAddress;
	private List<HashMap<Integer, Integer>> trustedNodesLists;
	
	private double epochInterval;
	private int validEpochNum;
	
	private int bloomFilterDepth;
	
	// attenuated bloom filter: validEpochNum * bloomFilterDepth
	private ArrayList<ArrayList<BloomFilter<Integer>>> attenuatedBloomFilter;
	
	// bloom filter received from NBR nodes
	//private HashMap<Integer, ArrayList<BloomFilter<Integer>>> nbrAttenuatedBloomFilter;	

	//private List<List<Integer>> neighborEphemeralAddressesLists;
	
	
	// received packet id list and its size limit
	//private int receivedPacketListSize;
	//private List<Integer> receivedPacketList;
	/********************************************************/
	
	static {
		DTNSim.registerForReset(DTNHost.class.getCanonicalName());
		reset();
	}
	/**
	 * Creates a new DTNHost.
	 * @param msgLs Message listeners
	 * @param movLs Movement listeners
	 * @param groupId GroupID of this host
	 * @param interf List of NetworkInterfaces for the class
	 * @param comBus Module communication bus object
	 * @param mmProto Prototype of the movement model of this host
	 * @param mRouterProto Prototype of the message router of this host
	 */
	public DTNHost(List<MessageListener> msgLs,
			List<MovementListener> movLs,
			String groupId, List<NetworkInterface> interf,
			ModuleCommunicationBus comBus, 
			MovementModel mmProto, MessageRouter mRouterProto) {
		this.comBus = comBus;
		this.location = new Coord(0,0);
		
		//YSPARK
		//this.address = getNextAddress();
		//this.name = groupId+address;
		this.permanentAddress = getNextAddress();
		this.name = groupId+permanentAddress;
		
		this.net = new ArrayList<NetworkInterface>();

		for (NetworkInterface i : interf) {
			NetworkInterface ni = i.replicate();
			ni.setHost(this);
			net.add(ni);
		}	

		// TODO - think about the names of the interfaces and the nodes
		//this.name = groupId + ((NetworkInterface)net.get(1)).getAddress();

		this.msgListeners = msgLs;
		this.movListeners = movLs;

		// create instances by replicating the prototypes
		this.movement = mmProto.replicate();
		this.movement.setComBus(comBus);
		setRouter(mRouterProto.replicate());

		this.location = movement.getInitialLocation();

		this.nextTimeToMove = movement.nextPathAvailable();
		this.path = null;

		if (movLs != null) { // inform movement listeners about the location
			for (MovementListener l : movLs) {
				l.initialLocation(this, this.location);
			}
		}		
	}
	
	/*************************************************/
	//YSPARK	
	public DTNHost(List<MessageListener> msgLs,
			List<MovementListener> movLs,
			String groupId, List<NetworkInterface> interf,
			ModuleCommunicationBus comBus, 
			MovementModel mmProto, MessageRouter mRouterProto,
			double epochInterval, int validEpochNum, int bloomFilterDepth) {
		
		this(msgLs, movLs, groupId, interf, comBus, mmProto, mRouterProto);
		
		this.epochInterval = epochInterval;
		this.validEpochNum = validEpochNum;
				
		//this.anonymityGroupID = -1;
		
		this.trustedNodesLists = new ArrayList<HashMap<Integer, Integer>>();		
												
		
		// Attenuated Bloom Filter
		this.bloomFilterDepth = bloomFilterDepth;
		
		this.attenuatedBloomFilter = new ArrayList<ArrayList<BloomFilter<Integer>>>(validEpochNum);
		
		for(int i = 0; i < this.validEpochNum; i++) {
			createAttenuatedBloomFilterPerEpoch(i);
		}
		
		/*
		for(ArrayList<BloomFilter<Integer>> attenuatedBloomFilterPerEpoch : this.attenuatedBloomFilter) {		
			attenuatedBloomFilterPerEpoch = new ArrayList<BloomFilter<Integer>>(bloomFilterDepth);
		
			for(int i=0; i<bloomFilterDepth; i++) { 
				attenuatedBloomFilterPerEpoch.add(new BloomFilter<Integer>(0.05, 100));
			}
		}
		*/
		
		//this.nbrAttenuatedBloomFilter = new HashMap<Integer, ArrayList<BloomFilter<Integer>>>();

	}
	

	/*************************************************/
	
	
	/**
	 * Returns a new network interface address and increments the address for
	 * subsequent calls.
	 * @return The next address.
	 */
	private synchronized static int getNextAddress() {
		return nextAddress++;	
	}

	/**
	 * Reset the host and its interfaces
	 */
	public static void reset() {
		nextAddress = 0;
	}

	/**
	 * Returns true if this node is active (false if not)
	 * @return true if this node is active (false if not)
	 */
	public boolean isActive() {
		return this.movement.isActive();
	}

	/**
	 * Set a router for this host
	 * @param router The router to set
	 */
	private void setRouter(MessageRouter router) {
		router.init(this, msgListeners);
		this.router = router;
	}

	/**
	 * Returns the router of this host
	 * @return the router of this host
	 */
	public MessageRouter getRouter() {
		return this.router;
	}

	/********************************************************/
	// YSPARK 	
	/**
	 * Returns the network-layer address of this host.
	 */	
	/*
	public int getAddress() { 	
		return this.address;
		
	}
	*/
	
	
	/*
	public int getAnonymityGroupID() {	
		return this.anonymityGroupID;
	}
	
	public void setAnonymityGroupID(int id) {	
		this.anonymityGroupID = id;
	}
	*/

	
	public int getPermanentAddress() {
		return this.permanentAddress;
	}
	
	public int getEphemeralAddress() {
		return this.ephemeralAddress;
	}
		
	private int generateEphemeralAddress(int permanentAddress, int epoch) {
		return Integer.valueOf(epoch*100 + permanentAddress).hashCode();
	}
	
	
	/**
	 * Add trusted nodes 
	 * Currently, DTNHost maintains only 1 hashmap of trusted nodes. 
	 * @param trustedNodes
	 */
	public void addTrustedNodes(List<Integer> trustedNodes) {
		//System.out.printf("addTrustedNodes: %d (%d)\n", this.permanentAddress, trustedNodes.size());
		
		
		if(trustedNodesLists.isEmpty()) {
			trustedNodesLists.add(new HashMap<Integer, Integer>());
		}
		
						
		for(Integer nodePermanentAddress : trustedNodes) {
			
			int index = trustedNodesLists.size() -1;
			
			HashMap<Integer, Integer> trustedNodeMap = trustedNodesLists.get(index);
			
			if(!trustedNodeMap.containsKey(nodePermanentAddress)) {
				trustedNodeMap.put(nodePermanentAddress, generateEphemeralAddress(nodePermanentAddress, 0));
			}			
		}		
		return;
	}
	
	public List<HashMap<Integer, Integer>> getTrustedNodesLists() {	
		return trustedNodesLists;
	}
	
	
	
	
	/**
	 * update a host when epoch is changed.
	 * 1. update ephemeral address
	 * 2. update ephemeral addresses of its trusted nodes
	 * 3. update packet destinations
	 * 4. update local attenuate bloom filter
	 * 5. clear NBR list and NBR attenuate bloom filters
	 * @param seed
	 */
	public void updateDueToEpochChange(int nextEpochStartTime, int epochMargin) {
		// 1. Update ephemeral address of each host
		this.updateEphemeralID(nextEpochStartTime);
	
		// 2. Update ephemeral addresses of trusted nodes of each host */
		this.updateTrustedNodesLists((int)nextEpochStartTime);
	
		// 3. Update packet destinations stored in each host
		this.updatePacketDestinations((int)nextEpochStartTime, epochMargin);
		
		// 4. Update local bloom filter of each host
		this.updateLocalBloomFilter();
				
		// 5. Clear NBR list
		//this.nbrAttenuatedBloomFilter.clear();
	}
	
		
	/**
	 * update ephemeral address
	 * 1) ephemeral address of its own
	 * 2) ephemeral addresses of its trusted nodes
	 * 3) ephemeral addresses of packet destinations
	 * @param seed
	 */
	private void updateEphemeralID(int epoch) {
		if(this.trustedNodesLists.isEmpty())
			this.ephemeralAddress = this.permanentAddress;
		else
			this.ephemeralAddress = generateEphemeralAddress(this.permanentAddress, epoch);

		//System.out.printf("Node %d, %d\n", address, ephemeralAddress);						
	}
	
	
	/**
	 * Update trustedNodesLists stored in host
	 * @param epoch
	 */
	private void updateTrustedNodesLists(int epoch) {
		
		if(!this.trustedNodesLists.isEmpty()) {
				 
			HashMap<Integer, Integer> newList = new HashMap<Integer, Integer>();
						
					
			for(int permanentAddress : trustedNodesLists.get(trustedNodesLists.size()-1).keySet() ) {								
				newList.put(permanentAddress, generateEphemeralAddress(permanentAddress, epoch));
				
				//System.out.printf("%d, %d\n", nodeAddress, Integer.valueOf(nodeAddress + seed).hashCode());
			}
						
			this.trustedNodesLists.add(newList);
			
			if(this.trustedNodesLists.size() > this.validEpochNum) {
				this.trustedNodesLists.remove(0);
			}
			
		}
			
	}
	
	
	
	/**
	 * Update packets' destination ephemeral addresses
	 * @param epoch
	 */
	private void updatePacketDestinations(int epoch, int validEpochNum) {
		List<String> messagesToDelete = new ArrayList<String>();
		boolean isAnonymizedNode = !trustedNodesLists.isEmpty();
		
		// uncheck newly received messages
		for(Message m : this.router.getMessageCollection()) {
			if(m.isNewlyReceived())
				m.setNewlyReceived(false);
		} 
		
			
		// update packet destinations
		if(isAnonymizedNode) {
								
			for(Message m : this.router.getMessageCollection()) {
							
				// No need to update packets destined for untrusted nodes
				if(!m.isAnonymized()) {
					// Packets destined for untrusted node and received from untrusted node are DELETED.
					if(m.isReceivedFromUntrustedNode()) {
						messagesToDelete.add(m.getId());
					}
											
					continue;
				} 

				
				boolean messageUpdated = false;

				// Packets destined for trusted nodes are assigned a new destination ephemeral addresses
				for(HashMap<Integer, Integer> list : this.trustedNodesLists) {					
					if(list.containsValue(m.getToEphemeralAddress())) {										
						m.setToEphemeralAddress(generateEphemeralAddress(m.getTo().getPermanentAddress(), epoch));
						messageUpdated = true;
						break;					
					}					
				}
				
				
				if(messageUpdated == false) {
										
					// Destination ephemeral address update failed 
					if(DTNSim.ANONYMOUS_DTN_DEBUG >= 1) {					
						System.out.printf("update (PAddr %d, EAddr %d), MID: %s, toEAddr: %d, m.to.PAddr: %d, m.to.EAddr: %d, NonUpdateEpoch: %d\n", permanentAddress, ephemeralAddress, m.getId(),m.getToEphemeralAddress(), m.getTo().getPermanentAddress(), m.getTo().getEphemeralAddress(), m.getNonUpdateEpochCount());
						
						for(HashMap<Integer, Integer> list : this.trustedNodesLists) {
							System.out.printf("(%d): ", list.size());
							System.out.println(list.toString());
						}
					}
					
					m.increaseNonUpdateEpochCount();
				
					if(m.getNonUpdateEpochCount() >= validEpochNum) {
						messagesToDelete.add(m.getId());
					}
					
					// workaround
					/*
					if(m.getNonUpdateEpochCount() == 0) {				
						if(this.trustedNodesLists.get(0).containsKey(m.getTo().getPermanentAddress())) {
							m.setToEphemeralAddress(generateEphemeralAddress(m.getTo().getPermanentAddress(), epoch));
						}
					}
					else {
						// Destination ephemeral address update failed 
						if(DTNSim.ANONYMOUS_DTN_DEBUG >= 1) {					
							System.out.printf("update (PAddr %d, EAddr %d), MID: %s, toEAddr: %d, m.to.PAddr: %d, m.to.EAddr: %d, NonUpdateEpoch: %d\n", permanentAddress, ephemeralAddress, m.getId(),m.getToEphemeralAddress(), m.getTo().getPermanentAddress(), m.getTo().getEphemeralAddress(), m.getNonUpdateEpochCount());
							
							for(HashMap<Integer, Integer> list : this.trustedNodesLists) {
								System.out.printf("(%d): ", list.size());
								System.out.println(list.toString());
							}
						}
						
						m.increaseNonUpdateEpochCount();
					
						if(m.getNonUpdateEpochCount() >= validEpochNum) {
							messagesToDelete.add(m.getId());
						}		
					}
					*/
				}
				
						
			}

			
			/** Update ephemeral addresses of incoming messages */
			for(Message m: this.router.getIncomingMessageCollection()) {
				if(!m.isAnonymized())
					continue;
				
				for(HashMap<Integer, Integer> list : this.trustedNodesLists) {				
					if(list.containsValue(m.getToEphemeralAddress())) {									
						m.setToEphemeralAddress(generateEphemeralAddress(m.getTo().getPermanentAddress(), epoch));
						break;					
					}					
				}		
			}
		}
		else {
			
			for(Message m : this.router.getMessageCollection()) {
				if(!m.isAnonymized())
					continue;
				
				m.increaseNonUpdateEpochCount();
				
				if(m.getNonUpdateEpochCount() >= validEpochNum) {
					messagesToDelete.add(m.getId());
				}
			}		
		}
		
		
		
		/** Delete expired packets */
		for(String msgId : messagesToDelete) {
			if(DTNSim.ANONYMOUS_DTN_DEBUG >= 1) {
				if(!trustedNodesLists.isEmpty()) {
					System.out.printf("Packet expired in %d\n",  this.permanentAddress);
				}
			}
			
			// Remove packets with expired destination
			this.router.deleteMessage(msgId, true);
			
			// Report 
			this.router.countDeleteMessageReason(1);
		}			

		

		
	}

	
	
	
	private void createAttenuatedBloomFilterPerEpoch(int index) {
		this.attenuatedBloomFilter.add(index, new ArrayList<BloomFilter<Integer>>(this.bloomFilterDepth));
		
		for(int i=0; i<this.bloomFilterDepth; i++) { 
			this.attenuatedBloomFilter.get(index).add(new BloomFilter<Integer>(0.05, 127));
		}
	}
	
	
	
	/**
	 * Update attenuate bloom filter of a host at epoch change
	 * @param epoch
	 */
	private void updateLocalBloomFilter() {
		
		// remove the oldest 
		while(this.attenuatedBloomFilter.size() >= this.validEpochNum)
			this.attenuatedBloomFilter.remove(this.attenuatedBloomFilter.size()-1);
		
		// add a new attenuated bloom filter
		createAttenuatedBloomFilterPerEpoch(0);
		
		
		/*
		// clear whole attenuate bloom filter
		for(BloomFilter<Integer> bloomFilter : attenuatedBloomFilter) {
			bloomFilter.clear();
		}
		*/
				
		// add all trusted nodes to local bloom filter (depth = 0)
		for(HashMap<Integer, Integer> trustedNodes : trustedNodesLists) {
			this.attenuatedBloomFilter.get(0).get(0).addAll(trustedNodes.values());
		}
	}
		
	
	
	public void addNeighborNode(int ephemeralAddress, ArrayList<BloomFilter<Integer>> nbrAttenuatedBloomFilter) {
		
		// add NBR to nbrAttenuateBloomFilter
		//this.nbrAttenuatedBloomFilter.put(ephemeralAddress, attenuateBloomFilter);
		
		// add attenuateBloomFiler of the NBR to the aggreated bloom filter of the host
		this.attenuatedBloomFilter.get(0).get(1).add(ephemeralAddress);
		
		for(int depth = 1; depth < this.bloomFilterDepth - 1; depth++) {
			this.attenuatedBloomFilter.get(0).get(depth+1).getBitSet().or(nbrAttenuatedBloomFilter.get(depth).getBitSet());						
		}

		/*
		if(neighborEphemeralAddressesLists.isEmpty()) {
			List<Integer> newNeighborList = new ArrayList<Integer>();
			neighborEphemeralAddressesLists.add(newNeighborList);
		}
		
		
		int latestIndex = neighborEphemeralAddressesLists.size()-1;
		if(neighborEphemeralAddressesLists.get(latestIndex).contains(ephemeralAddress) == false) { 
			neighborEphemeralAddressesLists.get(latestIndex).add(ephemeralAddress);
		}
		*/				
	}
	
	/*	
	public void updateNeighborNodesLists() {
								
		// Add new neighbor nodes list 		
		this.neighborEphemeralAddressesLists.add(new ArrayList<Integer>());
		
		if(neighborEphemeralAddressesLists.size() > this.validEpochNum) {
			neighborEphemeralAddressesLists.remove(0);
		}
		
		
		
		//this.nbrAttenuateBloomFilter.clear();
	}
	*/
	
	

	
	
	
	/*
	public List<Integer> getReceivableEphemeralAddresses() {
		List<Integer> receivableEphemeralAddresses = new ArrayList<Integer>();
								
		if(!trustedNodesLists.isEmpty()) {
			for(HashMap<Integer, Integer> list : trustedNodesLists)
				receivableEphemeralAddresses.addAll(list.values());
		}
				
		if(neighborEphemeralAddressesLists.isEmpty() == false) {
			for(List<Integer> list : neighborEphemeralAddressesLists) {			
				for(int neighbor : list) {
					if(receivableEphemeralAddresses.contains(neighbor) == false)
						receivableEphemeralAddresses.add(neighbor);
				}
			}
		}			
		
		// remove myself from the list
		//while(receivableEphemeralAddresses != null && receivableEphemeralAddresses.contains(this.ephemeralAddress))
		//	receivableEphemeralAddresses.remove(receivableEphemeralAddresses.indexOf(this.ephemeralAddress));
		
		return receivableEphemeralAddresses;
		
	}
	*/
	
	

	
	
	
	
	
	
	
	/*
	public void addBloomFilter(List<Integer> nodeList, int depth) {
		BloomFilter<Integer> bloomFilter = attenuatedBloomFilter.get(depth);		
		
		for(Integer nodeId : nodeList)
			bloomFilter.add(nodeId);		
	}
	
	
	public void addBloomFilter(int nodeId, int depth) {
		BloomFilter<Integer> bloomFilter = attenuatedBloomFilter.get(depth);		
		
		bloomFilter.add(nodeId);		
	}
	
	
	public void clearNbrAttenuatedBloomFilter() {
		
		// clear all the nbr hosts at the end of an epoch
		this.nbrAttenuatedBloomFilter.clear();
	}
	 */
	
	
	public ArrayList<ArrayList<BloomFilter<Integer>>> getAttenuatedBloomFilter() {
		
		return this.attenuatedBloomFilter;
	}
	
	
	
	/********************************************************/
	
	
	/**
	 * Returns this hosts's ModuleCommunicationBus
	 * @return this hosts's ModuleCommunicationBus
	 */
	public ModuleCommunicationBus getComBus() {
		return this.comBus;
	}
	
    /**
	 * Informs the router of this host about state change in a connection
	 * object.
	 * @param con  The connection object whose state changed
	 */
	public void connectionUp(Connection con) {
		this.router.changedConnection(con);
	}

	public void connectionDown(Connection con) {
		this.router.changedConnection(con);
	}

	/**
	 * Returns a copy of the list of connections this host has with other hosts
	 * @return a copy of the list of connections this host has with other hosts
	 */
	public List<Connection> getConnections() {
		List<Connection> lc = new ArrayList<Connection>();

		for (NetworkInterface i : net) {
			lc.addAll(i.getConnections());
		}

		return lc;
	}

	/**
	 * Returns the current location of this host. 
	 * @return The location
	 */
	public Coord getLocation() {
		return this.location;
	}

	/**
	 * Returns the Path this node is currently traveling or null if no
	 * path is in use at the moment.
	 * @return The path this node is traveling
	 */
	public Path getPath() {
		return this.path;
	}


	/**
	 * Sets the Node's location overriding any location set by movement model
	 * @param location The location to set
	 */
	public void setLocation(Coord location) {
		this.location = location.clone();
	}

	/**
	 * Sets the Node's name overriding the default name (groupId + netAddress)
	 * @param name The name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Returns the messages in a collection.
	 * @return Messages in a collection
	 */
	public Collection<Message> getMessageCollection() {
		return this.router.getMessageCollection();
	}

	/**
	 * Returns the number of messages this node is carrying.
	 * @return How many messages the node is carrying currently.
	 */
	public int getNrofMessages() {
		return this.router.getNrofMessages();
	}

	/**
	 * Returns the buffer occupancy percentage. Occupancy is 0 for empty
	 * buffer but can be over 100 if a created message is bigger than buffer 
	 * space that could be freed.
	 * @return Buffer occupancy percentage
	 */
	public double getBufferOccupancy() {
		double bSize = router.getBufferSize();
		double freeBuffer = router.getFreeBufferSize();
		return 100*((bSize-freeBuffer)/bSize);
	}

	/**
	 * Returns routing info of this host's router.
	 * @return The routing info.
	 */
	public RoutingInfo getRoutingInfo() {
		return this.router.getRoutingInfo();
	}

	/**
	 * Returns the interface objects of the node
	 */
	public List<NetworkInterface> getInterfaces() {
		return net;
	}

	/**
	 * Find the network interface based on the index
	 */
	protected NetworkInterface getInterface(int interfaceNo) {
		NetworkInterface ni = null;
		try {
			ni = net.get(interfaceNo-1);
		} catch (IndexOutOfBoundsException ex) {
			System.out.println("No such interface: "+interfaceNo);
			System.exit(0);
		}
		return ni;
	}

	/**
	 * Find the network interface based on the interfacetype
	 */
	protected NetworkInterface getInterface(String interfacetype) {
		for (NetworkInterface ni : net) {
			if (ni.getInterfaceType().equals(interfacetype)) {
				return ni;
			}
		}
		return null;	
	}

	/**
	 * Force a connection event
	 */
	public void forceConnection(DTNHost anotherHost, String interfaceId, 
			boolean up) {
		NetworkInterface ni;
		NetworkInterface no;

		if (interfaceId != null) {
			ni = getInterface(interfaceId);
			no = anotherHost.getInterface(interfaceId);

			assert (ni != null) : "Tried to use a nonexisting interfacetype "+interfaceId;
			assert (no != null) : "Tried to use a nonexisting interfacetype "+interfaceId;
		} else {
			ni = getInterface(1);
			no = anotherHost.getInterface(1);
			
			assert (ni.getInterfaceType().equals(no.getInterfaceType())) : 
				"Interface types do not match.  Please specify interface type explicitly";
		}
		
		if (up) {
			ni.createConnection(no);
		} else {
			ni.destroyConnection(no);
		}
	}

	/**
	 * for tests only --- do not use!!!
	 */
	public void connect(DTNHost h) {
		System.err.println(
				"WARNING: using deprecated DTNHost.connect(DTNHost)" +
		"\n Use DTNHost.forceConnection(DTNHost,null,true) instead");
		forceConnection(h,null,true);
				
	}

	/**
	 * Updates node's network layer and router.
	 * @param simulateConnections Should network layer be updated too
	 */
	public void update(boolean simulateConnections) {
		if (!isActive()) {
			return;
		}
		
		if (simulateConnections) {
			for (NetworkInterface i : net) {
				i.update();
			}
		}
		this.router.update();
	}
	
	
	/**
	 * Moves the node towards the next waypoint or waits if it is
	 * not time to move yet
	 * @param timeIncrement How long time the node moves
	 */
	public void move(double timeIncrement) {		
		double possibleMovement;
		double distance;
		double dx, dy;

		if (!isActive() || SimClock.getTime() < this.nextTimeToMove) {
			return; 
		}
		if (this.destination == null) {
			if (!setNextWaypoint()) {
				return;
			}
		}

		possibleMovement = timeIncrement * speed;
		distance = this.location.distance(this.destination);

		while (possibleMovement >= distance) {
			// node can move past its next destination
			this.location.setLocation(this.destination); // snap to destination
			possibleMovement -= distance;
			if (!setNextWaypoint()) { // get a new waypoint
				return; // no more waypoints left
			}
			distance = this.location.distance(this.destination);
		}

		// move towards the point for possibleMovement amount
		dx = (possibleMovement/distance) * (this.destination.getX() -
				this.location.getX());
		dy = (possibleMovement/distance) * (this.destination.getY() -
				this.location.getY());
		this.location.translate(dx, dy);
	}	

	/**
	 * Sets the next destination and speed to correspond the next waypoint
	 * on the path.
	 * @return True if there was a next waypoint to set, false if node still
	 * should wait
	 */
	private boolean setNextWaypoint() {
		if (path == null) {
			path = movement.getPath();
		}

		if (path == null || !path.hasNext()) {
			this.nextTimeToMove = movement.nextPathAvailable();
			this.path = null;
			return false;
		}

		this.destination = path.getNextWaypoint();
		this.speed = path.getSpeed();

		if (this.movListeners != null) {
			for (MovementListener l : this.movListeners) {
				l.newDestination(this, this.destination, this.speed);
			}
		}

		return true;
	}

	/**
	 * Sends a message from this host to another host
	 * @param id Identifier of the message
	 * @param to Host the message should be sent to
	 */
	public void sendMessage(String id, DTNHost to) {
		this.router.sendMessage(id, to);
	}

	/**
	 * Start receiving a message from another host
	 * @param m The message
	 * @param from Who the message is from
	 * @return The value returned by 
	 * {@link MessageRouter#receiveMessage(Message, DTNHost)}
	 */
	public int receiveMessage(Message m, DTNHost from) {
		int retVal = this.router.receiveMessage(m, from); 

		if (retVal == MessageRouter.RCV_OK) {
			m.addNodeOnPath(this);	// add this node on the messages path
		}

		return retVal;	
	}

	/**
	 * Requests for deliverable message from this host to be sent trough a
	 * connection.
	 * @param con The connection to send the messages trough
	 * @return True if this host started a transfer, false if not
	 */
	public boolean requestDeliverableMessages(Connection con) {
		return this.router.requestDeliverableMessages(con);
	}

	/**
	 * Informs the host that a message was successfully transferred.
	 * @param id Identifier of the message
	 * @param from From who the message was from
	 */
	public void messageTransferred(String id, DTNHost from) {
		this.router.messageTransferred(id, from);
	}

	/**
	 * Informs the host that a message transfer was aborted.
	 * @param id Identifier of the message
	 * @param from From who the message was from
	 * @param bytesRemaining Nrof bytes that were left before the transfer
	 * would have been ready; or -1 if the number of bytes is not known
	 */
	public void messageAborted(String id, DTNHost from, int bytesRemaining) {
		this.router.messageAborted(id, from, bytesRemaining);
	}

	/**
	 * Creates a new message to this host's router
	 * @param m The message to create
	 */
	public void createNewMessage(Message m) {
		this.router.createNewMessage(m);
	}

	/**
	 * Deletes a message from this host
	 * @param id Identifier of the message
	 * @param drop True if the message is deleted because of "dropping"
	 * (e.g. buffer is full) or false if it was deleted for some other reason
	 * (e.g. the message got delivered to final destination). This effects the
	 * way the removing is reported to the message listeners.
	 */
	public void deleteMessage(String id, boolean drop) {
		this.router.deleteMessage(id, drop);
	}

	/**
	 * Returns a string presentation of the host.
	 * @return Host's name
	 */
	public String toString() {
		return name;
	}

	/**
	 * Checks if a host is the same as this host by comparing the object
	 * reference
	 * @param otherHost The other host
	 * @return True if the hosts objects are the same object
	 */
	public boolean equals(DTNHost otherHost) {
		return this == otherHost;
	}

	/**
	 * Compares two DTNHosts by their addresses.
	 * @see Comparable#compareTo(Object)
	 */
	public int compareTo(DTNHost h) {
		// YSPARK
		//return this.getAddress() - h.getAddress();
		return this.getPermanentAddress() - h.getPermanentAddress();
	}
	
}
