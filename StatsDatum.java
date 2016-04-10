package norm;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StatsDatum {
	public String security;
	public String sDate;
	public double date;
	private Map<String, Double> rawValues;
	private Map<String, Double> normTimeValues;
	private Map<String, Double> normAllValues;
	private Map<String, Double> returns;
	@SuppressWarnings("unused")
	private StatsNormalization funct;
	public StatsDatum(StatsNormalization funct, String security,String sDate, double date){
		this.funct = funct;
		this.security = security;
		this.sDate = sDate;
		this.date = date;
		this.rawValues = new HashMap<String, Double>();
		this.normTimeValues = new HashMap<String, Double>();
		this.normAllValues = new HashMap<String, Double>();
		this.returns = new HashMap<String, Double>();
	}
	public Double getVal(String s){
		if(!rawValues.containsKey(s))
			return null;
		return rawValues.get(s);
	}
	public boolean hasVal(String s){
		return rawValues.get(s)!=null;
	}
	public void setVal(String s, Double d){
		rawValues.replace(s, d);
	}
	public void enterRatio(Double d, String s){
		this.rawValues.put(s,d);
	}
	public void enterTimeNorm(Double d, String s){
		this.normTimeValues.put(s,d);
	}
	public void enterAllNorm(Double d, String s){
		this.normAllValues.put(s,d);
	}
	public void enterNorm(Double d, String s, char type){
		switch(type){
			case 't': 
				this.normTimeValues.put(s,d); break;
			case 'a': 
				this.normAllValues.put(s,d); break;	
			default:
				System.out.println("invalid char");
		}
		
	}
	public void enterReturn(Double d,String s){
		this.returns.put(s,d);
	}
	//t -> normTimeValues, a-> normAllValues w -> rawValues, r -> returns
	public List<String> exportData(List<String> index, char code){
		List<String> result = new ArrayList<String>();
		for(int i = 0; i<index.size(); i++){
			try{
				String x = 	Double.toString(
						code == 't' ? normTimeValues.get(index.get(i)):
						code == 'a' ? normAllValues.get(index.get(i)):
						code == 'w' ? rawValues.get(index.get(i)):
						code == 'r' ? returns.get(index.get(i)) :
						null);
				result.add(x);
			}
			catch(NullPointerException e){
				result.add("null");
			}
		}
		return result;
	}
	public List<String> exportInfo(){
		List<String> result = new ArrayList<String>();
		result.add(security);
		result.add(sDate);
		result.add(Double.toString(date));
		return result;
	}
}
