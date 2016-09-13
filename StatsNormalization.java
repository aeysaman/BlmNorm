package normalization;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import general.Datum;
import general.Read;
import general.Tools;

public class StatsNormalization {
	private List<String> ratioIndex;
	private List<String> returnIndex;
	private Map<Integer,List<Datum>> data;
	
	private final double percentClip = .05;
	
	public static void main(String[] args) {
		StatsNormalization funct = new StatsNormalization();
		System.out.println("reading");
		funct.readData(new File("rawData.csv"));
		System.out.println("normalizing Times");
		funct.normalizeTimes();
		System.out.println("normalizing All");
		funct.normalizeAll();
		System.out.println("printing");
		funct.printData(new File("normedData.csv"));
		System.out.println("all done");
	}
	public StatsNormalization(){
		ratioIndex = new ArrayList<String>();
		returnIndex = new ArrayList<String>();
	}
	private void normalizeTimes() {
		for(Integer t: data.keySet())
			for(String ratio: ratioIndex)
				normalize(data.get(t), ratio, "time");
	}
	private void normalizeAll() {
		List<Datum> all = new ArrayList<Datum>();
		for(List<Datum> x: data.values())
			all.addAll(x);
		
		for(String ratio: ratioIndex)
			normalize(all, ratio, "all");
	}
	private void normalize(List<Datum> dirtyList, String ratio, String type) {
		List<Datum> list = removeNulls(dirtyList, ratio);
		
		double median = calcMedian(list, ratio);
		double stdev = calcAdjStDev(list, ratio);
				
		for(Datum s : removeNulls(dirtyList, ratio)){
			Double x = s.getValue(ratio);
			x= mathify(x, median, stdev);
			s.enterValue(ratio + "_" + type, x);
		}
	}
	private List<Datum> removeNulls(List<Datum> list, String ratio) {
		List<Datum> result = new ArrayList<Datum>();
		for(Datum x: list){
			if(x.hasValue(ratio))
				result.add(x);
		}
		return result;
	}
	private double calcAdjStDev(List<Datum> ls, String ratio){
		List<Double> result = new ArrayList<Double>();
		for(Datum s : ls)
			result.add(s.getValue(ratio));
		Collections.sort(result);
		int n = result.size();
		List<Double> shorter = result.subList((int)(n * percentClip), (int)(n *(1-percentClip)));
		
		double mean = Tools.calcD(shorter, ratio, false);
		double stdev = Math.sqrt(Tools.calcD(shorter, ratio, true) - mean *mean);
		return stdev;
	}
	private double mathify(double x, double center, double stdev){
		double result = (x-center)/(stdev);
		double answer = 2/(1+ Math.exp(-result)) - 1;
		return answer;
	}
	private double calcMedian(List<Datum> ls, String ratio){
		List<Double> result = new ArrayList<Double>();
		for(Datum s : ls)
			result.add(s.getValue(ratio));
		Collections.sort(result);
		int mid = result.size()/2;
		if(result.size()%2 ==1)
			return result.get(mid);
		else
			return (result.get(mid-1) + result.get(mid))/2.0;
	}
	
	public void printData(File f){
		try{
			BufferedWriter fileWriter = new BufferedWriter(new FileWriter(f));
			fileWriter.write(String.format("Security,Date,%s,%s,%s\n",String.join(",", returnIndex), String.join("_time,", ratioIndex)+ "_time,",String.join("_all,", ratioIndex) +"_all"));
			for(List<Datum> foo: data.values())
				for(Datum bar : foo){
					String rtn = bar.exportDataJoined(returnIndex);
					String time = bar.exportDataJoined(Tools.appendStringToList(ratioIndex,"time"));
					String all = bar.exportDataJoined(Tools.appendStringToList(ratioIndex,"all"));
					fileWriter.write(String.format("%s,%s,%s,%s,%s\n", bar.name, bar.date,rtn,time,all));
				}
			fileWriter.close();
		}
		catch(Exception e){
			Tools.exceptionEnd("error in printing", e);
		}
	}
	public void readData(File f){
		Scanner scan = Read.openScan(f);
		Map<Integer, List<Datum>> result = new HashMap<Integer, List<Datum>>();
		
		List<String> topLine = Read.readTopLine(scan.nextLine());
		parseColumns(topLine);
		
		while(scan.hasNextLine()){
			Datum foo = Datum.createDatum(scan.nextLine(), topLine, "yyyy/mm/dd");

			if(!result.containsKey(foo.getDateNumQrt()))
				result.put(foo.getDateNumQrt(), new ArrayList<Datum>());
			result.get(foo.getDateNumQrt()).add(foo);
		}
		this.data = result;
	}
	//builds the return and ratio indexes
	private void parseColumns(List<String> index){
		for(String s: index){
			if(s.contains("Forward") || s.contains("Premium"))
				returnIndex.add(s);
			else if(!(s.contains("Date") || s.contains("Security")|| s.contains("DateNum") || s.contains("Price")))
				ratioIndex.add(s);
		}
	}
}