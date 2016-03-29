import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

public class tools {
	public static void printSetToFile(File f, Set<String> ls){
		try {
			BufferedWriter fileWriter = new BufferedWriter(new FileWriter(f));
			for(String s: ls)
				fileWriter.write(s + "\n");
			fileWriter.close();
		} catch (IOException e) {
			tools.exceptionEnd("error in printing", e);
		}
	}
	public static String joinListD(List<Double> foo){
		String x = "";
		int i = 0;
		while(i<foo.size()){
			Double d = foo.get(i);
			if(d ==null)
				x = x + "null";
			else
				x = x + d.toString();
			if(i!=foo.size()-1)
				x = x+ ",";
			i++;
		}
		return x;
	}
	public static String joinListS(List<String> foo){
		String x = "";
		int i = 0;
		while(i<foo.size()){
			x = x +foo.get(i);
			if(i!=foo.size()-1)
				x = x+ ",";
			i++;
		}
		return x;
	}
	//systematic error handling, ending the program
	public static void exceptionEnd(String code, Exception e){
		System.out.println(code);
		e.printStackTrace();
		System.exit(1);
	}
	public static void printMap(Map<?, ?> x){
		for(Object key: x.keySet()){
			System.out.println(key.toString() + " -> " + x.get(key));
		}
	}
	public static List<String> readCSVtoList(File f){
		List<String> result = new ArrayList<String>();
		Scanner fileReader = openScan(f);
		while (fileReader.hasNextLine()){ 
			String[] items = fileReader.nextLine().split(",");
			result.add(items[0]);
		}
		fileReader.close();
		return result;
	}
	public static Map<String, String> readCSVtoMap (File f){
		Map<String, String> result = new HashMap<String,String>();
		Scanner fileReader = openScan(f);
		while (fileReader.hasNextLine()){ 
			String[] items = fileReader.nextLine().split(",");
			result.put(items[0], items[1]);
		}
		fileReader.close();
		return result;
	}
	public static Scanner openScan(File f){
		Scanner scan = null;
		try {
			scan = new Scanner(f);
		} catch (FileNotFoundException e) {
			exceptionEnd("error in reading index",e);
		}
		return scan;
	}
	public static Map<Integer, List<String>> readAllSecurities(File f) {
		Map<Integer, List<String>> result = new HashMap<Integer, List<String>>();
		Scanner scan = openScan(f);
		Integer[] index = readTopLine(scan.nextLine());
		for(Integer i : index)
			result.put(i, new ArrayList<String>());
		while(scan.hasNextLine()){
			String[] s = scan.nextLine().split(",");
			for(int i = 0; i<s.length; i++){
				String x = s[i];
				if(x.length()>1)
					result.get(index[i]).add(x);
			}
		}
		return result;
	}
	public static Integer[] readTopLine(String s){
		String[] strIndex = s.split(",");
		Integer[] intIndex = new Integer[strIndex.length];
		for(int i = 0; i<strIndex.length;i++)
			intIndex[i] = Integer.parseInt(strIndex[i]);
		return intIndex;
	}
	//takes the list of raw index values and converts them to fwd percentages of length q
	public static Map<Integer, Double> convertToPerc(Map<Integer, Double> index, int q) {
		Map<Integer, Double> result = new HashMap<Integer,Double>();
		for(Integer i : index.keySet()){
			double curr = index.get(i);
			int futureDate = iterateDate(i, q);
			double fwd = curr;
			if(index.containsKey(futureDate))
				fwd = index.get(futureDate);
			result.put(i, (fwd-curr)/curr);
		}
		return result;
	}
	public static int iterateDate(int code, int i){
		int year = code/10;
		int qrt = code%10 +i;
		while(qrt>4){
			qrt-=4;
			year++;
		}
		return year *10 + qrt;
	}
	static double calc(List<StatsDatum> ls, String ratio, boolean squared){
		List<Double> dubs = new ArrayList<Double>();
		for(StatsDatum x : ls){
			dubs.add( x.getVal(ratio));
		}
		return calcD(dubs, ratio, squared);
	}
	static double calcD(List<Double> ls, String ratio, boolean squared){
		double result = 0;
		int count = 0;
		for(Double x : ls){
			count++;
			if(squared)
				x *=x;
			result +=x;
		}
		result /= count;
		return result;
	}
}