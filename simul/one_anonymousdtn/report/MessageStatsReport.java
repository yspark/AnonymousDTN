/* 
 * Copyright 2010 Aalto University, ComNet
 * Released under GPLv3. See LICENSE.txt for details. 
 */
package report;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import core.DTNHost;
import core.Message;
import core.MessageListener;

/**
 * Report for generating different kind of total statistics about message
 * relaying performance. Messages that were created during the warm up period
 * are ignored.
 * <P><strong>Note:</strong> if some statistics could not be created (e.g.
 * overhead ratio if no messages were delivered) "NaN" is reported for
 * double values and zero for integer median(s).
 */
public class MessageStatsReport extends Report implements MessageListener {
	private Map<String, Double> creationTimes;
	private List<Double> latencies;
	private List<Integer> hopCounts;
	private List<Double> msgBufferTime;
	private List<Double> rtt; // round trip times
	
	private int nrofDropped;
	private int nrofRemoved;
	private int nrofStarted;
	private int nrofAborted;
	private int nrofRelayed;
	private int nrofCreated;
	private int nrofResponseReqCreated;
	private int nrofResponseDelivered;
	private int nrofDelivered;
	
	
	/******************************/
	//YSPARK
	private int nrofRelayedTrustTrust;
	private int nrofRelayedTrustUntrust;
	private int nrofRelayedUntrustTrust;
	private int nrofRelayedUntrustUntrust;
	
	private int nrofDeliveredWithUntrustedHop;
	
	private int nrofDroppedBufferFull;
	private int nrofDroppedTTLExpiry;
	private int nrofDroppedEphemeralIDExipiry;
	
	private int nrofRemovedDelivered;
	private int nrofRemovedAck;
	/******************************/	
	
	/**
	 * Constructor.
	 */
	public MessageStatsReport() {
		init();
	}

	@Override
	protected void init() {
		super.init();
		this.creationTimes = new HashMap<String, Double>();
		this.latencies = new ArrayList<Double>();
		this.msgBufferTime = new ArrayList<Double>();
		this.hopCounts = new ArrayList<Integer>();
		this.rtt = new ArrayList<Double>();
		
		this.nrofDropped = 0;
		this.nrofRemoved = 0;
		this.nrofStarted = 0;
		this.nrofAborted = 0;
		this.nrofRelayed = 0;
		this.nrofCreated = 0;
		this.nrofResponseReqCreated = 0;
		this.nrofResponseDelivered = 0;
		this.nrofDelivered = 0;
		
		/******************************/
		//YSPARK
		this.nrofRelayedTrustTrust = 0;
		this.nrofRelayedTrustUntrust = 0;
		this.nrofRelayedUntrustTrust = 0;
		this.nrofRelayedUntrustUntrust = 0;
		
		this.nrofDeliveredWithUntrustedHop = 0;
		
		
		this.nrofDroppedBufferFull = 0;
		this.nrofDroppedTTLExpiry = 0;
		this.nrofDroppedEphemeralIDExipiry = 0;
		
		this.nrofRemovedDelivered = 0;
		this.nrofRemovedAck = 0;
		/******************************/
	}

	
	public void messageDeleted(Message m, DTNHost where, boolean dropped) {
		if (isWarmupID(m.getId())) {
			return;
		}
		
		if (dropped) {
			this.nrofDropped++;
		}
		else {
			this.nrofRemoved++;
		}
		
		this.msgBufferTime.add(getSimTime() - m.getReceiveTime());
	}

	
	public void messageTransferAborted(Message m, DTNHost from, DTNHost to) {
		if (isWarmupID(m.getId())) {
			return;
		}
		
		this.nrofAborted++;
	}

	
	public void messageTransferred(Message m, DTNHost from, DTNHost to,
			boolean finalTarget) {
		if (isWarmupID(m.getId())) {
			return;
		}

		this.nrofRelayed++;
		
		/******************************/
		//YSPARK
		boolean fromTrusted = false, toTrusted = false;
		
		int destinationAddress = m.getTo().getPermanentAddress();
		
		/* from is trusted by the packet destination */
		if(from.getTrustedNodesLists() != null) {
			for(HashMap<Integer, Integer> list : from.getTrustedNodesLists()) {
				if(list.containsKey(destinationAddress)) {
					fromTrusted = true;
					break;
				}					
			}
		}
			
				
		/* to is trusted by the packet destination */
		if(to.getTrustedNodesLists() != null) {
			for(HashMap<Integer, Integer> list : to.getTrustedNodesLists()) {
				if(list.containsKey(destinationAddress)) {
					toTrusted = true;
					break;
				}					
			}
		}
		
		
		if(fromTrusted && toTrusted)
			nrofRelayedTrustTrust++;
		else if(fromTrusted && !toTrusted)
			nrofRelayedTrustUntrust++;
		else if(!fromTrusted && toTrusted)
			nrofRelayedUntrustTrust++;
		else
			nrofRelayedUntrustUntrust++;
						
		/******************************/

		if (finalTarget) {
			this.latencies.add(getSimTime() - 
				this.creationTimes.get(m.getId()) );
			this.nrofDelivered++;
			this.hopCounts.add(m.getHops().size() - 1);
			
			/******************************/
			//YSPARK						
			//System.out.printf("finalTarget: %d\n", to.getPermanentAddress());
			//System.out.println(m.getHops().toString());
						
			for(DTNHost host : m.getHops()) {
				boolean bTrustedHop = false;
				
				if(host.getPermanentAddress() == to.getPermanentAddress())
					continue;
												
				/* to is trusted by the packet destination */
				if(to.getTrustedNodesLists() != null) {
					for(HashMap<Integer, Integer> list : to.getTrustedNodesLists()) {
						if(list.containsKey(host.getPermanentAddress())) {
							bTrustedHop = true;
							break;
						}					
					}					
				}
				
				if(bTrustedHop == false) { 
					nrofDeliveredWithUntrustedHop++;
					break;
				}
			}
			
			
				
			
			//System.out.printf("\n");
			/******************************/
			
			if (m.isResponse()) {
				this.rtt.add(getSimTime() -	m.getRequest().getCreationTime());
				this.nrofResponseDelivered++;
			}
		}
	}


	public void newMessage(Message m) {
		if (isWarmup()) {
			addWarmupID(m.getId());
			return;
		}
		
		this.creationTimes.put(m.getId(), getSimTime());
		this.nrofCreated++;
		if (m.getResponseSize() > 0) {
			this.nrofResponseReqCreated++;
		}
	}
	
	
	public void messageTransferStarted(Message m, DTNHost from, DTNHost to) {
		if (isWarmupID(m.getId())) {
			return;
		}

		this.nrofStarted++;
	}
	

	@Override
	public void done() {
		write("Message stats for scenario " + getScenarioName() + 
				"\nsim_time: " + format(getSimTime()));
		double deliveryProb = 0; // delivery probability
		double responseProb = 0; // request-response success probability
		double overHead = Double.NaN;	// overhead ratio
		
		if (this.nrofCreated > 0) {
			deliveryProb = (1.0 * this.nrofDelivered) / this.nrofCreated;
		}
		if (this.nrofDelivered > 0) {
			overHead = (1.0 * (this.nrofRelayed - this.nrofDelivered)) /
				this.nrofDelivered;
		}
		if (this.nrofResponseReqCreated > 0) {
			responseProb = (1.0* this.nrofResponseDelivered) / 
				this.nrofResponseReqCreated;
		}
		
		String statsText = "created: " + this.nrofCreated + 
			"\nstarted: " + this.nrofStarted + 
			"\nrelayed: " + this.nrofRelayed +
			"\n\tnrelayed_t_to_t: " + this.nrofRelayedTrustTrust +
			"\n\tnrelayed_t_to_ut: " + this.nrofRelayedTrustUntrust + 
			"\n\tnrelayed_ut_to_t: " + this.nrofRelayedUntrustTrust + 
			"\n\tnrelayed_ut_to_ut: " + this.nrofRelayedUntrustUntrust + 
			"\naborted: " + this.nrofAborted +
			"\ndropped: " + this.nrofDropped +
			"\n\tdropped_buffer_full: " + this.nrofDroppedBufferFull +
			"\n\tdropped_ttl_expiry: " + this.nrofDroppedTTLExpiry +
			"\n\tdropped_ephmeral_expiry: " + this.nrofDroppedEphemeralIDExipiry +
			"\nremoved: " + this.nrofRemoved +
			"\ndelivered: " + this.nrofDelivered +
			"\n\tdelivered_with_ut_hops: " + this.nrofDeliveredWithUntrustedHop +
			"\ndelivery_prob: " + format(deliveryProb) +
			"\nresponse_prob: " + format(responseProb) + 
			"\noverhead_ratio: " + format(overHead) + 
			"\nlatency_avg: " + getAverage(this.latencies) +
			"\nlatency_med: " + getMedian(this.latencies) + 
			"\nhopcount_avg: " + getIntAverage(this.hopCounts) +
			"\nhopcount_med: " + getIntMedian(this.hopCounts) + 
			"\nbuffertime_avg: " + getAverage(this.msgBufferTime) +
			"\nbuffertime_med: " + getMedian(this.msgBufferTime) +
			"\nrtt_avg: " + getAverage(this.rtt) +
			"\nrtt_med: " + getMedian(this.rtt)
			;
		
		write(statsText);
		super.done();
	}
	
	/************************************************/
	//YSPARK
	/**
	 * 
	 * @param kind
	 * 			0: buffer full
	 * 			1: Ephemeral ID Expiry
	 * 			2: TTL Expiry
	 */
	public void increaseDropCount(int kind) {
		switch(kind) {
		case 0:
			this.nrofDroppedBufferFull++;
			break;
		case 1:
			this.nrofDroppedEphemeralIDExipiry++;
			break;
		case 2:
			this.nrofDroppedTTLExpiry++;
			break;
		}
	}
	/************************************************/
	
}
