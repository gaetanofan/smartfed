package it.cnr.isti.smartfed.metascheduler.resources;

import it.cnr.isti.smartfed.metascheduler.resources.iface.IMSProviderComputing;

import java.util.HashMap;




public class MSProviderComputing implements IMSProviderComputing, Cloneable {

	private int ID;
	
	private HashMap<String, Object> characteristic;
	
	public MSProviderComputing(){
		new MSProviderComputing(-1, new HashMap<String, Object>());
	}
	
	public MSProviderComputing(int id, HashMap<String, Object> characteristic){
		this.ID = id;
		this.characteristic = characteristic;
	}
	
	@Override
	public int compareTo(Object o) {
		if( o ==null )
			return 1;
		MSProviderComputing provC = (MSProviderComputing) o;
		return ID - provC.ID;
	}
	
	@SuppressWarnings("unchecked")
	public Object clone(){
		MSProviderComputing pComp = null;
		try {
			pComp = (MSProviderComputing) super.clone();
			pComp.characteristic = (HashMap<String, Object>) characteristic.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return pComp;
	}
	
	@Override
	public void setCharacteristic(HashMap<String, Object> characteristic) {
		this.characteristic = characteristic;
	}

	@Override
	public HashMap<String, Object> getCharacteristic() {
		return characteristic;
	}

	@Override
	public void setID(int id) {
		this.ID = id;
		
	}
	@Override
	public int getID() {
		return ID;
	}


}
