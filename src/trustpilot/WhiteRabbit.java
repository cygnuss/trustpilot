package trustpilot;

import java.io.*;
import java.math.*;
import java.security.*;
import java.util.*;

public class WhiteRabbit {
	
	private static final String SOURCE_PHRASE = "poultryoutwitsants"; // removed spaces - spaces not needed for char map
	
	private static HashMap<Character, Integer> charMap;
	private static List<String> wordList;
	
	private static void initCharMap(){
		charMap = new HashMap<Character, Integer>();
		for (int i = 0; i < SOURCE_PHRASE.length(); i++) {
			char c = SOURCE_PHRASE.charAt(i);
			int count = charMap.getOrDefault(c, 0);
			charMap.put(c, count + 1);
		}
	}
	
	private static void initDictionary(String wordlistPath) throws IOException{
		FileInputStream fs = new FileInputStream(wordlistPath);
		BufferedReader br = new BufferedReader(new InputStreamReader(fs));
		
		List<String> words = new ArrayList<String>();
		String line = null;
		while((line = br.readLine()) != null){
			// only add the relevant words to our list
			if(checkSubAnagram(line)){
				words.add(line);
			}
		}
		br.close();
		wordList = words;
		
	}
	
	/**
	 * Verifies if a string is a valid 'sub-anagram'. 
	 * @param str
	 * @return true if str is a sub-anagram of the source phrase 
	 */
	private static boolean checkSubAnagram(String str){
		HashMap<Character, Integer> currMap = new HashMap<Character, Integer>();
		for(int i=0; i < str.length(); i++){
			char c = str.charAt(i);
			int count = currMap.getOrDefault(c, 0);
			if(!charMap.containsKey(c) || count >= charMap.get(c)){
				return false;
			}
			currMap.put(c, count+1);
		}
		return true;
	}
	
	/**
	 * Returns all anagrams of the source phrase. 
	 * Note that there is a slight inefficiency here, as it is possible for duplicate anagrams to be generated.
	 * See comment in code.  
	 * @param words
	 * @return all valid anagrams of the source phrase
	 */
	private static List<List<String>> generateAnagrams(List<String> words){
		List<List<String>> grams = new ArrayList<List<String>>();
		TreeMap<Integer,List<String>> map = new TreeMap<Integer, List<String>>();
		// bucket words by string length
		for(int i=0; i < words.size(); i++){
			int len = words.get(i).length();
			List<String> list = map.getOrDefault(len,new ArrayList<String>());
			list.add(words.get(i));
			map.put(len, list);
		}
		Integer[] keys = map.keySet().toArray(new Integer[map.size()]);
		for(int i=0; i < keys.length; i++){
			int j=i;
			int k=keys.length-1;
			while(j <= k){
				int totalLen = keys[i]+keys[j]+keys[k];
				if(totalLen == SOURCE_PHRASE.length()){
					// if we pick any of the three lists we pass to generateAnagrams are equal, then we will get duplicate anagrams
					grams.addAll(generateAnagrams(map.get(keys[i]), map.get(keys[j]), map.get(keys[k])));
					j++;
					k--;
				}else if(totalLen < SOURCE_PHRASE.length()){
					j++;
				}else{
					k--;
				}
			}
		}
		return grams;
	}
	
	/**
	 * @param list1
	 * @param list2
	 * @param list3
	 * @return all valid anagrams taken from words in list1, list2, list3
	 * 
	 */
	private static List<List<String>> generateAnagrams(List<String> list1, List<String> list2, List<String> list3){
		List<List<String>> grams = new ArrayList<List<String>>();
		for(int i=0; i < list1.size(); i++){
			for(int j=0; j < list2.size(); j++){
				for(int k=0; k < list3.size(); k++){
					String a = list1.get(i);
					String b = list2.get(j);
					String c = list3.get(k);
					// this is in fact checking if a+b+c is a perfect anagram (not just a sub-anagram) since len(a+b+c) = len(PHRASE). 					
					if(checkSubAnagram(a+b+c)){ 
						List<String> gram = new ArrayList<String>();
						gram.add(a);
						gram.add(b);
						gram.add(c);
						grams.add(gram);
					}
				}
			}
		}
		return grams;
	}
	
	/**
	 * 
	 * @param candidate: list of 3 strings that together form an anagram of the source phrase 
	 * @param target: the target MD5 value
	 * @return if any permutation of the 3 strings is the target phrase then we return it. Otherwise null. 
	 * @throws UnsupportedEncodingException
	 * @throws NoSuchAlgorithmException
	 */
	private static String checkMD5(List<String> candidate, String target) throws UnsupportedEncodingException, NoSuchAlgorithmException{
		List<String> perms = new ArrayList<String>();
		perms.add(String.format("%s %s %s", candidate.get(0), candidate.get(1), candidate.get(2)));
		perms.add(String.format("%s %s %s", candidate.get(0), candidate.get(2), candidate.get(1)));
		perms.add(String.format("%s %s %s", candidate.get(1), candidate.get(0), candidate.get(2)));
		perms.add(String.format("%s %s %s", candidate.get(1), candidate.get(2), candidate.get(0)));
		perms.add(String.format("%s %s %s", candidate.get(2), candidate.get(0), candidate.get(1)));
		perms.add(String.format("%s %s %s", candidate.get(2), candidate.get(1), candidate.get(0)));

		for(int i=0; i < perms.size(); i++){
			byte[] bytesOfMessage = perms.get(i).getBytes("UTF-8");

			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] digest = md.digest(bytesOfMessage);
			
			BigInteger bigInt = new BigInteger(1,digest);
			String hashtext = bigInt.toString(16);
			if(hashtext.equals(target)){
				return perms.get(i);
			}
		}
		return null;
	}
	
	public static void main(String[] args){
		try {
			initCharMap();
			initDictionary("wordlist");
			long startTime = System.currentTimeMillis();
			List<List<String>> grams = generateAnagrams(wordList);
			for(int i=0; i < grams.size(); i++){
				String solution = checkMD5(grams.get(i), "4624d200580677270a54ccff86b9610e");
				if(solution != null){
					System.out.println(solution);
				}
			}
			long endTime = System.currentTimeMillis();
			System.out.println(String.format("Execution Time: %d", endTime-startTime));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}
	
}
