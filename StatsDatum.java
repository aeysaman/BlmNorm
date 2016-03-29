import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StatsDatum {
	public String security;
	public String sDate;
	public double date;
	private Map<String, Double> rawValues;
	private Map<String, Double> normValues;
	private Map<String, Double> returns;
	private StatsNormalization funct;
	public StatsDatum(StatsNormalization funct, String security,String sDate, double date){
		this.funct = funct;
		this.security = security;
		this.sDate = sDate;
		this.date = date;
		this.rawValues = new HashMap<String, Double>();
		this.normValues = new HashMap<String, Double>();
		this.returns = new HashMap<String, Double>();
	}
	public Double getVal(String s){
		if(!rawValues.containsKey(s))
			return null;
		return rawValues.get(s);
	}
	public boolean hasVal(String s){
		//if(!rawValues.containsKey(s))
		//	return false;
		return rawValues.get(s)!=null;
	}
	public void setVal(String s, Double d){
		rawValues.replace(s, d);
	}
	public void enterRatio(Double d, String s){
		this.rawValues.put(s,d);
	}
	public void enterNorm(Double d, String s){
		this.normValues.put(s,d);
	}
	public void enterReturn(Double d,String s){
		this.returns.put(s,d);
	}
	//n -> normValues, r -> rawValues, t -> returns
	public List<String> exportData(List<String> index, char code){
		List<String> result = new ArrayList<String>();
		for(int i = 0; i<index.size(); i++){
			try{
				String x = 	Double.toString(
						code == 'n' ? normValues.get(index.get(i)):
						code == 'r' ? rawValues.get(index.get(i)):
						code == 't' ? returns.get(index.get(i)) :
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
