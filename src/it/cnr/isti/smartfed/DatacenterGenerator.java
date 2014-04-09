package it.cnr.isti.smartfed;

import it.cnr.isti.smartfed.federation.resources.FederationDatacenter;
import it.cnr.isti.smartfed.federation.resources.FederationDatacenterFactory;
import it.cnr.isti.smartfed.federation.resources.FederationDatacenterProfile;
import it.cnr.isti.smartfed.federation.resources.FederationDatacenterProfile.DatacenterParams;
import it.cnr.isti.smartfed.federation.resources.HostFactory;
import it.cnr.isti.smartfed.federation.resources.HostProfile;
import it.cnr.isti.smartfed.federation.resources.HostProfile.HostParams;
import it.cnr.isti.smartfed.federation.utils.DistributionAssignment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import org.apache.commons.math3.distribution.AbstractIntegerDistribution;
import org.apache.commons.math3.distribution.AbstractRealDistribution;
import org.apache.commons.math3.distribution.ExponentialDistribution;
import org.apache.commons.math3.distribution.UniformIntegerDistribution;
import org.apache.commons.math3.distribution.UniformRealDistribution;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;

public class DatacenterGenerator 
{
	
	// dc variables
	protected AbstractRealDistribution costPerMem;
	protected AbstractRealDistribution costPerSto;
	protected AbstractRealDistribution costPerSec;
	protected AbstractRealDistribution costPerBw;
	
	// host variables
	protected AbstractIntegerDistribution ramAmount;
	protected AbstractIntegerDistribution bwAmount;
	protected AbstractIntegerDistribution stoAmount;
	
	// pes variables
	protected AbstractIntegerDistribution coreAmount;
	protected AbstractIntegerDistribution mipsAmount;
	
	protected long seed;
	
	public DatacenterGenerator()
	{
		costPerMem = new UniformRealDistribution(0.01, 0.10);
		costPerSto = new UniformRealDistribution(0.0002, 0.0020);
		costPerSec = new UniformRealDistribution(0.10, 0.80); //not used, see below
		costPerBw = new UniformRealDistribution(0.001, 0.05);
		
		ramAmount = new UniformIntegerDistribution(512, 1024*16);
		bwAmount = new UniformIntegerDistribution(10*1024, 10*1024*1024);
		stoAmount = new UniformIntegerDistribution(4096, 10*1024*1024); // 10TB max
		coreAmount = new UniformIntegerDistribution(1, 8);
		mipsAmount = new UniformIntegerDistribution(1000, 25000);
	}
	
	public DatacenterGenerator(long seed)
	{
		this();
		resetSeed(seed);
	}
	
	public void resetSeed(long seed)
	{
		costPerMem.reseedRandomGenerator(seed);
		costPerSto.reseedRandomGenerator(seed);
		costPerSec.reseedRandomGenerator(seed);
		costPerBw.reseedRandomGenerator(seed);
		
		ramAmount.reseedRandomGenerator(seed);
		bwAmount.reseedRandomGenerator(seed);
		stoAmount.reseedRandomGenerator(seed);
		coreAmount.reseedRandomGenerator(seed);
		mipsAmount.reseedRandomGenerator(seed);
		
		this.seed = seed;
	}

	/**
	 * Generates the list of datacenters, and assigns the host to datacenters according
	 * to a uniform distribution. If a datacenter will result with 0 hosts, it will not
	 * be created.
	 * 
	 * @param numOfDatacenters
	 * @param numHost
	 * @return
	 */
	public List<FederationDatacenter> getDatacenters(int numOfDatacenters, int numHost)
	{
		UniformRealDistribution urd = new UniformRealDistribution();
		urd.reseedRandomGenerator(this.seed);
		
		return getDatacenters(numOfDatacenters, numHost, urd);
	}
	
	/**
	 * Generates the list of datacenters, and assigns the host to datacenters according
	 * the given distribution. If a datacenter will result with 0 hosts, it will not
	 * be created.
	 * @param numOfDatacenters
	 * @param numHost
	 * @param distribution
	 * @return
	 */
	public List<FederationDatacenter> getDatacenters(int numOfDatacenters, int numHost, AbstractRealDistribution distribution)
	{
		// create the list
		List<FederationDatacenter> list = new ArrayList<FederationDatacenter>(numOfDatacenters);
		
		// Here get the assignment vector
		int[] assign = DistributionAssignment.getAssignmentArray(numOfDatacenters, numHost, distribution);
		
		for (int i=0; i<numOfDatacenters; i++)
		{
			if (assign[i] == 0)
				continue;
			
			// create the virtual processor (PE)
			List<Pe> peList = new ArrayList<Pe>();
			int numCore = coreAmount.sample();
			int mips = mipsAmount.sample();

			for (int j=0; j<numCore; j++)
			{
				peList.add(new Pe(j, new PeProvisionerSimple(mips)));
			}
			
			// create the hosts
			List<Host> hostList = new ArrayList<Host>();
			HostProfile prof = HostProfile.getDefault();
			
			prof.set(HostParams.RAM_AMOUNT_MB, ramAmount.sample()+"");
			prof.set(HostParams.BW_AMOUNT, bwAmount.sample()+"");
			prof.set(HostParams.STORAGE_MB, stoAmount.sample()+"");
					
			
			for (int k=0; k<assign[i]; k++)
			{
				hostList.add(HostFactory.get(prof, peList));
			}
			
			// create the storage
			List<Storage> storageList = new ArrayList<Storage>(); // if empty, no SAN attached
			
			// create the datacenters
			FederationDatacenterProfile profile = FederationDatacenterProfile.getDefault();
			profile.set(DatacenterParams.COST_PER_BW, costPerBw.sample()+"");
			profile.set(DatacenterParams.COST_PER_STORAGE, costPerSto.sample()+"");
			// profile.set(DatacenterParams.COST_PER_SEC, costPerSec.sample()+"");
			profile.set(DatacenterParams.COST_PER_SEC, "0");
			profile.set(DatacenterParams.COST_PER_MEM, costPerMem.sample()+"");
			
			list.add(FederationDatacenterFactory.get(profile, hostList, storageList));
		}
		
				
		return list;
	}


}
