/* 
 * Copyright 2010 Aalto University, ComNet
 * Released under GPLv3. See LICENSE.txt for details. 
 */
package core;

import input.EventQueue;
import input.EventQueueHandler;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

//YSPARK
import java.util.Random;

import movement.MapBasedMovement;
import movement.MovementModel;
import movement.map.SimMap;
import routing.MessageRouter;

/**
 * A simulation scenario used for getting and storing the settings of a
 * simulation run.
 */
public class SimScenario implements Serializable {
	
	/** a way to get a hold of this... */	
	private static SimScenario myinstance=null;

	/** namespace of scenario settings ({@value})*/
	public static final String SCENARIO_NS = "Scenario";
	/** number of host groups -setting id ({@value})*/
	public static final String NROF_GROUPS_S = "nrofHostGroups";
	/** number of interface types -setting id ({@value})*/
	public static final String NROF_INTTYPES_S = "nrofInterfaceTypes";
	/** scenario name -setting id ({@value})*/
	public static final String NAME_S = "name";
	/** end time -setting id ({@value})*/
	public static final String END_TIME_S = "endTime";
	/** update interval -setting id ({@value})*/
	public static final String UP_INT_S = "updateInterval";
	/** simulate connections -setting id ({@value})*/
	public static final String SIM_CON_S = "simulateConnections";

	/** namespace for interface type settings ({@value}) */
	public static final String INTTYPE_NS = "Interface";
	/** interface type -setting id ({@value}) */
	public static final String INTTYPE_S = "type";
	/** interface name -setting id ({@value}) */
	public static final String INTNAME_S = "name";

	/** namespace for application type settings ({@value}) */
	public static final String APPTYPE_NS = "Application";
	/** application type -setting id ({@value}) */
	public static final String APPTYPE_S = "type";
	/** setting name for the number of applications */
	public static final String APPCOUNT_S = "nrofApplications";
	
	/** namespace for host group settings ({@value})*/
	public static final String GROUP_NS = "Group";
	/** group id -setting id ({@value})*/
	public static final String GROUP_ID_S = "groupID";
	/** number of hosts in the group -setting id ({@value})*/
	public static final String NROF_HOSTS_S = "nrofHosts";
	/** scanning interval -setting id ({@value})*/
	public static final String SCAN_INTERVAL_S = "scanInterval";
	/** movement model class -setting id ({@value})*/
	public static final String MOVEMENT_MODEL_S = "movementModel";
	/** router class -setting id ({@value})*/
	public static final String ROUTER_S = "router";
	/** number of interfaces in the group -setting id ({@value})*/
	public static final String NROF_INTERF_S = "nrofInterfaces";
	/** interface name in the group -setting id ({@value})*/
	public static final String INTERFACENAME_S = "interface";
	/** application name in the group -setting id ({@value})*/
	public static final String GAPPNAME_S = "application";

	
	/***************************************************/
	// YSPARK
	
	/** (Namespace: Scenario) number of anonymity groups */
	public static final String N_ANONYMITY_GROUPS = "nAnonymityGroups";
	/** (Namespace: Scenario) epoch interval*/
	public static final String EPOCH_INTERVAL = "epochInterval";	
	/** (Namespace: Scenario) epoch interval*/
	public static final String VALID_EPOCH_NUM = "validEpochNum";
	/** (Namespace: Scenario) attenuate bloom filter depth */
	public static final String BLOOM_FILTER_DEPTH = "bloomFilterDepth";
	
	
		
	/** Namespace for anonymity group settings ({@value}) */
	public static final String ANONYMITY_NS = "Anonymity";
	/** (Namespace: ANONYMITY_NS) number of hosts in anonymity group */
	public static final String N_ANONYMITY_HOSTS = "nAnonymityHosts";
	/** (Namespace: ANONYMITY+i) Trusted hosts */
	public static final String TRUSTED_HOSTS = "trustedHosts";
			 
	
	/** Number of anonymity groups */
	private int nAnonymityGroups;
	/** List of hosts in this simulation */
	protected List<List<Integer>> anonymityGroupList;
	
	/** Epoch interval */
	private double epochInterval;
	
	/** Keep unupdated packets during valid epochs */
	private int validEpochNum;
	
	/** Bloom Filter depth */
	private int bloomFilterDepth;
	/***************************************************/
	
	
	/** package where to look for movement models */
	private static final String MM_PACKAGE = "movement.";
	/** package where to look for router classes */
	private static final String ROUTING_PACKAGE = "routing.";

	/** package where to look for interface classes */
	private static final String INTTYPE_PACKAGE = "interfaces.";
	
	/** package where to look for application classes */
	private static final String APP_PACKAGE = "applications.";
	
	/** The world instance */
	private World world;
	/** List of hosts in this simulation */
	protected List<DTNHost> hosts;
	/** Name of the simulation */
	private String name;
	/** number of host groups */
	int nrofGroups;
	/** Width of the world */
	private int worldSizeX;
	/** Height of the world */
	private int worldSizeY;
	/** Largest host's radio range */
	private double maxHostRange;
	/** Simulation end time */
	private double endTime;
	/** Update interval of sim time */
	private double updateInterval;
	/** External events queue */
	private EventQueueHandler eqHandler;
	/** Should connections between hosts be simulated */
	private boolean simulateConnections;
	/** Map used for host movement (if any) */
	private SimMap simMap;

	/** Global connection event listeners */
	private List<ConnectionListener> connectionListeners;
	/** Global message event listeners */
	private List<MessageListener> messageListeners;
	/** Global movement event listeners */
	private List<MovementListener> movementListeners;
	/** Global update event listeners */
	private List<UpdateListener> updateListeners;
	/** Global application event listeners */
	private List<ApplicationListener> appListeners;

	

	
	
	static {
		DTNSim.registerForReset(SimScenario.class.getCanonicalName());
		reset();
	}
	
	public static void reset() {
		myinstance = null;
	}

	/**
	 * Creates a scenario based on Settings object.
	 */
	protected SimScenario() {
		Settings s = new Settings(SCENARIO_NS);
		nrofGroups = s.getInt(NROF_GROUPS_S);

		this.name = s.valueFillString(s.getSetting(NAME_S));
		this.endTime = s.getDouble(END_TIME_S);
		this.updateInterval = s.getDouble(UP_INT_S);
		this.simulateConnections = s.getBoolean(SIM_CON_S);

		ensurePositiveValue(nrofGroups, NROF_GROUPS_S);
		ensurePositiveValue(endTime, END_TIME_S);
		ensurePositiveValue(updateInterval, UP_INT_S);

		this.simMap = null;
		this.maxHostRange = 1;

		this.connectionListeners = new ArrayList<ConnectionListener>();
		this.messageListeners = new ArrayList<MessageListener>();
		this.movementListeners = new ArrayList<MovementListener>();
		this.updateListeners = new ArrayList<UpdateListener>();
		this.appListeners = new ArrayList<ApplicationListener>();
		this.eqHandler = new EventQueueHandler();

		/*************************************************************/
		// YSPARK		
		this.epochInterval = s.getDouble(EPOCH_INTERVAL);
		this.validEpochNum = s.getInt(VALID_EPOCH_NUM);
		this.nAnonymityGroups = s.getInt(N_ANONYMITY_GROUPS);
		this.bloomFilterDepth = s.getInt(BLOOM_FILTER_DEPTH);
		
		if(DTNSim.epoch_interval != 0.0)
			this.epochInterval = DTNSim.epoch_interval;
		if(DTNSim.valid_epoch_num != 0) 
			this.validEpochNum = DTNSim.valid_epoch_num;		
		if(DTNSim.nAnonymityGroups != 0)
			this.nAnonymityGroups = DTNSim.nAnonymityGroups;
		if(DTNSim.bloomFilterDepth != 0)
			this.bloomFilterDepth = DTNSim.bloomFilterDepth;
		
		
		System.out.printf("Number of Anonymity Groups: %d\n", this.nAnonymityGroups);
		System.out.printf("Epoch interval: %f\n", this.epochInterval);
		System.out.printf("Valid epoch num: %d\n", this.validEpochNum);
		/*************************************************************/		
		
		/* TODO: check size from movement models */
		s.setNameSpace(MovementModel.MOVEMENT_MODEL_NS);
		int [] worldSize = s.getCsvInts(MovementModel.WORLD_SIZE, 2);
		this.worldSizeX = worldSize[0];
		this.worldSizeY = worldSize[1];
						
		createHosts();
		
		/*************************************************************/
		//YSPARK
		// create the list of anonymity groups.  
		createAnonymityGroups();
		
		// copy the list of anonymity groups to packet generator 
		insertAnonymityGroupsIntoEventQueues();
		/*************************************************************/
		
		this.world = new World(hosts, worldSizeX, worldSizeY, updateInterval,
				//YSPARK
				epochInterval, validEpochNum, bloomFilterDepth,
				updateListeners, simulateConnections, 
				eqHandler.getEventQueues());
		
	}
	
	/**
	 * Returns the SimScenario instance and creates one if it doesn't exist yet
	 */
	public static SimScenario getInstance() {
		if (myinstance == null) {
			myinstance = new SimScenario();
		}
		return myinstance;
	}

	/**
	 * Makes sure that a value is positive
	 * @param value Value to check
	 * @param settingName Name of the setting (for error's message)
	 * @throws SettingsError if the value was not positive
	 */
	private void ensurePositiveValue(double value, String settingName) {
		if (value < 0) {
			throw new SettingsError("Negative value (" + value + 
					") not accepted for setting " + settingName);
		}
	}

	/**
	 * Returns the name of the simulation run
	 * @return the name of the simulation run
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Returns true if connections should be simulated
	 * @return true if connections should be simulated (false if not)
	 */
	public boolean simulateConnections() {
		return this.simulateConnections;
	}

	/**
	 * Returns the width of the world
	 * @return the width of the world
	 */
	public int getWorldSizeX() {
		return this.worldSizeX;
	}

	/**
	 * Returns the height of the world
	 * @return the height of the world
	 */
	public int getWorldSizeY() {
		return worldSizeY;
	}

	/**
	 * Returns simulation's end time
	 * @return simulation's end time
	 */
	public double getEndTime() {
		return endTime;
	}

	/**
	 * Returns update interval (simulated seconds) of the simulation
	 * @return update interval (simulated seconds) of the simulation
	 */
	public double getUpdateInterval() {
		return updateInterval;
	}

	/**
	 * Returns how long range the hosts' radios have
	 * @return Range in meters
	 */
	public double getMaxHostRange() {
		return maxHostRange;
	}

	/**
	 * Returns the (external) event queue(s) of this scenario or null if there 
	 * aren't any
	 * @return External event queues in a list or null
	 */
	public List<EventQueue> getExternalEvents() {
		return this.eqHandler.getEventQueues();
	}

	/**
	 * Returns the SimMap this scenario uses, or null if scenario doesn't
	 * use any map
	 * @return SimMap or null if no map is used
	 */
	public SimMap getMap() {
		return this.simMap;
	}

	/**
	 * Adds a new connection listener for all nodes
	 * @param cl The listener
	 */
	public void addConnectionListener(ConnectionListener cl){
		this.connectionListeners.add(cl);
	}

	/**
	 * Adds a new message listener for all nodes
	 * @param ml The listener
	 */
	public void addMessageListener(MessageListener ml){
		this.messageListeners.add(ml);
	}

	/**
	 * Adds a new movement listener for all nodes
	 * @param ml The listener
	 */
	public void addMovementListener(MovementListener ml){
		this.movementListeners.add(ml);
	}

	/**
	 * Adds a new update listener for the world
	 * @param ul The listener
	 */
	public void addUpdateListener(UpdateListener ul) {
		this.updateListeners.add(ul);
	}

	/**
	 * Returns the list of registered update listeners
	 * @return the list of registered update listeners
	 */
	public List<UpdateListener> getUpdateListeners() {
		return this.updateListeners;
	}

	/** 
	 * Adds a new application event listener for all nodes.
	 * @param al The listener
	 */
	public void addApplicationListener(ApplicationListener al) {
		this.appListeners.add(al);
	}
	
	/**
	 * Returns the list of registered application event listeners
	 * @return the list of registered application event listeners
	 */
	public List<ApplicationListener> getApplicationListeners() {
		return this.appListeners;
	}
	
	/**
	 * Creates hosts for the scenario
	 */
	protected void createHosts() {
		this.hosts = new ArrayList<DTNHost>();

		// YSPARK
		if(DTNSim.ANONYMOUS_DTN_DEBUG >= 0)
			System.out.printf("nrofGroups:%d\n", nrofGroups);
	
		
		for (int i=1; i<=nrofGroups; i++) {
			List<NetworkInterface> mmNetInterfaces = 
				new ArrayList<NetworkInterface>();
			Settings s = new Settings(GROUP_NS+i);
			s.setSecondaryNamespace(GROUP_NS);
			String gid = s.getSetting(GROUP_ID_S);
			int nrofHosts = s.getInt(NROF_HOSTS_S);
			int nrofInterfaces = s.getInt(NROF_INTERF_S);
			int appCount;

			// creates prototypes of MessageRouter and MovementModel
			MovementModel mmProto = 
				(MovementModel)s.createIntializedObject(MM_PACKAGE + 
						s.getSetting(MOVEMENT_MODEL_S));
			MessageRouter mRouterProto = 
				(MessageRouter)s.createIntializedObject(ROUTING_PACKAGE + 
						s.getSetting(ROUTER_S));
			
			// checks that these values are positive (throws Error if not)
			ensurePositiveValue(nrofHosts, NROF_HOSTS_S);
			ensurePositiveValue(nrofInterfaces, NROF_INTERF_S);

			// setup interfaces
			for (int j=1;j<=nrofInterfaces;j++) {
				String Intname = s.getSetting(INTERFACENAME_S+j);
				Settings t = new Settings(Intname); 
				NetworkInterface mmInterface = 
					(NetworkInterface)t.createIntializedObject(INTTYPE_PACKAGE + 
							t.getSetting(INTTYPE_S));
				mmInterface.setClisteners(connectionListeners);
				mmNetInterfaces.add(mmInterface);
			}

			// setup applications
			if (s.contains(APPCOUNT_S)) {
				appCount = s.getInt(APPCOUNT_S);
			} else {
				appCount = 0;
			}
			for (int j=1; j<=appCount; j++) {
				String appname = null;
				Application protoApp = null;
				try {
					// Get name of the application for this group
					appname = s.getSetting(GAPPNAME_S+j);
					// Get settings for the given application
					Settings t = new Settings(appname);
					// Load an instance of the application
					protoApp = (Application)t.createIntializedObject(
							APP_PACKAGE + t.getSetting(APPTYPE_S));
					// Set application listeners
					protoApp.setAppListeners(this.appListeners);
					// Set the proto application in proto router
					//mRouterProto.setApplication(protoApp);
					mRouterProto.addApplication(protoApp);
				} catch (SettingsError se) {
					// Failed to create an application for this group
					System.err.println("Failed to setup an application: " + se);
					System.err.println("Caught at " + se.getStackTrace()[0]);
					System.exit(-1);
				}
			}

			if (mmProto instanceof MapBasedMovement) {
				this.simMap = ((MapBasedMovement)mmProto).getMap();
			}

			// creates hosts of ith group
			for (int j=0; j<nrofHosts; j++) {
				ModuleCommunicationBus comBus = new ModuleCommunicationBus();

				/****************************************************************/
				//YSPARK
				// prototypes are given to new DTNHost which replicates
				// new instances of movement model and message router
				/*
				DTNHost host = new DTNHost(this.messageListeners, 
						this.movementListeners,	gid, mmNetInterfaces, comBus, 
						mmProto, mRouterProto);
				*/
				
				DTNHost host = new DTNHost(this.messageListeners, 
						this.movementListeners,	gid, mmNetInterfaces, comBus, 
						mmProto, mRouterProto, this.epochInterval, this.validEpochNum, this.bloomFilterDepth);
				
				//host.setEpoch(this.epochInterval, this.epochMargin);
				/****************************************************************/
				
				hosts.add(host);
			}
		}		
	}

	/**
	 * Returns the list of nodes for this scenario.
	 * @return the list of nodes for this scenario.
	 */
	public List<DTNHost> getHosts() {
		return this.hosts;
	}
	
	/**
	 * Returns the World object of this scenario
	 * @return the World object
	 */
	public World getWorld() {
		return this.world;
	}

	/*************************************************************/
	// YSPARK
	/**
	 * Creates anonymity groups (trusted groups)
	 */
	private void createAnonymityGroups() {
		anonymityGroupList = new ArrayList<List<Integer>>();
		
		Random randomGenerator = new Random(DTNSim.randomSeed);
				
		for (int i=1; i<=nAnonymityGroups; i++) {
			Settings s = new Settings(ANONYMITY_NS+i);
			double percentage = s.getDouble(N_ANONYMITY_HOSTS);
			
			if(DTNSim.host_percentage != 0.0)
				percentage = DTNSim.host_percentage;
			
			
			int nHosts = (int)(percentage * (double)(hosts.size()));

			
			//System.out.println(nHosts);
			
			
			List<Integer> trustedNodesList = new ArrayList<Integer>();
			
			for(int j = 0; j < nHosts; j++) {
				while(true) {
					int node = randomGenerator.nextInt(hosts.size());
				
					if(!trustedNodesList.contains(node)) {
						trustedNodesList.add(node);
						break;
					}
				}
			}
			
			Collections.sort(trustedNodesList);
			
			System.out.printf("AnonymityGroup %d: (%d,%d)\n",  i, nHosts, trustedNodesList.size());
			System.out.println(trustedNodesList.toString());
												
			anonymityGroupList.add(trustedNodesList);						
		}
		
		
		for(DTNHost host : hosts) {
			int anonymityGroupID = 0;
			
			for(List<Integer> anonymityGroup : anonymityGroupList) {
				if(anonymityGroup.contains(host.getPermanentAddress())) {					
					host.addTrustedNodes(anonymityGroup);
					
					//host.setAnonymityGroupID(anonymityGroupID);
				}
				
				//anonymityGroupID++;
			}
		}
			
	}
	
	
	private void insertAnonymityGroupsIntoEventQueues() {
		for(EventQueue queue : eqHandler.getEventQueues()) {						
			queue.setAnonymityGroupList(this.anonymityGroupList);
		}		
	}
	/*************************************************************/
}















