import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class StatsNormalization {
	private List<String> index;
	private List<String> ratioIndex;
	private List<String> returnIndex;
	private Map<Double,List<StatsDatum>> data;
	private double limit = .5;
	private double stopAt = 2;
	public static void main(String[] args) {
		StatsNormalization funct = new StatsNormalization();
		System.out.println("reading");
		funct.readData(new File("rawData.csv"));
		System.out.println("normalizing Times");
		funct.normalizeTimes();
		System.out.println("normalizing All");
		funct.normalizeAll();
		System.out.println("printing");
		funct.printData(new File("cleanedData.csv"));
		System.out.println("all done");
	}
	private void normalizeTimes() {
		for(Double t: data.keySet()){
			for(String ratio: ratioIndex){
				normalize(data.get(t), ratio, 't');
			}
		}
	}
	private void normalizeAll() {
		List<StatsDatum> all = new ArrayList<StatsDatum>();
		for(List<StatsDatum> x: data.values())
			all.addAll(x);
		
		for(String ratio: ratioIndex){
			normalize(all, ratio, 'a');
		}
	}
	private void normalize(List<StatsDatum> dirtyList, String ratio, char type) {
		List<StatsDatum> list = removeNulls(dirtyList, ratio);
		
		double median = calcMedian(list, ratio);
		double stdev = calcAdjStDev(list, ratio);
				
		for(StatsDatum s : removeNulls(dirtyList, ratio)){
			Double x = s.getVal(ratio);
			x= mathify(x, median, stdev *.75);
			s.enterNorm(x, ratio, type);
		}
	}
	private List<StatsDatum> removeNulls(List<StatsDatum> list, String ratio) {
		List<StatsDatum> result = new ArrayList<StatsDatum>();
		for(StatsDatum x: list){
			if(x.hasVal(ratio))
				result.add(x);
		}
		return result;
	}
	private double calcAdjStDev(List<StatsDatum> ls, String ratio){
		List<Double> result = new ArrayList<Double>();
		for(StatsDatum s : ls)
			result.add(s.getVal(ratio));
		Collections.sort(result);
		int n = result.size();
		List<Double> shorter = result.subList(n/50, 49*n /50);
		
		double mean = tools.calcD(shorter, ratio, false);
		double stdev = Math.sqrt(tools.calcD(shorter, ratio, true) - mean *mean);
		return stdev;
	}
	private double mathify(double x, double center, double stdev){
		double result = (x-center)/(stdev);
		double answer = 2/(1+ Math.exp(-result)) - 1;
		return answer;
	}
	private double calcMedian(List<StatsDatum> ls, String ratio){
		List<Double> result = new ArrayList<Double>();
		for(StatsDatum s : ls)
			result.add(s.getVal(ratio));
		Collections.sort(result);
		int mid = result.size()/2;
		if(result.size()%2 ==1)
			return result.get(mid);
		else
			return (result.get(mid-1) + result.get(mid))/2.0;
	}
	public StatsNormalization(){
		index = new ArrayList<String>();
		ratioIndex = new ArrayList<String>();
		returnIndex = new ArrayList<String>();
	}
	public void printData(File f){
		try{
			BufferedWriter fileWriter = new BufferedWriter(new FileWriter(f));
			fileWriter.write("Security,Date,DateNum," + String.join(",", returnIndex) + "," + String.join("_time,", ratioIndex)+ "_time," + String.join("_all,", ratioIndex) +"_all\n");
			for(List<StatsDatum> foo: data.values()){
				for(StatsDatum bar : foo){
					List<String> rtn = bar.exportData(returnIndex,'r');
					List<String> ratTime = bar.exportData(ratioIndex,'t');
					List<String> ratAll = bar.exportData(ratioIndex,'a');
					fileWriter.write(String.join(",", bar.exportInfo()) +"," +String.join(",", rtn) +"," +String.join(",", ratTime)+"," +String.join(",", ratAll)+ "\n");
				}
			}
			fileWriter.close();
		}
		catch(Exception e){
			tools.exceptionEnd("error in printing", e);
		}
	}
	public void readData(File f){
		Scanner scan = tools.openScan(f);
		Map<Double, List<StatsDatum>> result = new HashMap<Double, List<StatsDatum>>();
		
		//read top line
		List<String> topLine = readLine(scan.nextLine());
		this.index = topLine;
		parseColumns(topLine);
		
		//loop through data
		while(scan.hasNextLine()){
			List<String> line = readLine(scan.nextLine());
			StatsDatum x = createStatsDatum(line);
			enterIntoMap(result, x);
		}
		this.data = result;
	}
	private void enterIntoMap(Map<Double, List<StatsDatum>> map, StatsDatum x){
		if(!map.containsKey(x.date))
			map.put(x.date, new ArrayList<StatsDatum>());
		map.get(x.date).add(x);
	}
	private void parseColumns(List<String> index){
		for(int i = 3; i<index.size(); i++){
			String s = index.get(i);
			if(s.contains("Forward") || s.contains("Premium"))
				returnIndex.add(s);
			else if(!s.equals("Price"))
				ratioIndex.add(s);
		}
	}
	private StatsDatum createStatsDatum(List<String> line) {
		String sec = line.get(this.getI("Security"));
		String sDate = line.get(this.getI("Date"));
		double date = Double.parseDouble(line.get(this.getI("DateNum")));
		StatsDatum datum = new StatsDatum(this,sec,sDate,date);
		
		for(int i = 3; i<index.size(); i++){
			String s = index.get(i);
			String x = line.get(i);
			Double d = null;
			if(!x.equals("null"))
				d = Double.parseDouble(x);
			if(ratioIndex.contains(s))
				datum.enterRatio(d, s);
			if(returnIndex.contains(s))
				datum.enterReturn(d, s);
			
		}
		return datum;
	}
	public List<String> readLine(String s){
		String[] foo = s.split(",");
		List<String> bar = new ArrayList<String>();
		for(int i = 0; i<foo.length; i++)
			bar.add(foo[i]);
		return bar;
	}
	public int getI(String s){
		return index.indexOf(s);
	}
	private List<StatsDatum> cleanList(List<StatsDatum> list, String ratio, double mean, double stdev) {
		List<StatsDatum> result = new ArrayList<StatsDatum>();
		double uLim = mean + stdev * limit;
		double lLim = mean - stdev * limit;
		for(StatsDatum x : list){
			if(!(x.getVal(ratio)>uLim) &&!(x.getVal(ratio)<lLim))
				result.add(x);
		}
		return result;
	}
}