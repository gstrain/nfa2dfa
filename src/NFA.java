import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Scanner;

/**
 * Program to convert NFA to DFA
 * 
 * run should be: java NFA nfa2 inputStrings.txt
 * 
 * @version 1.0
 * @author gstrain
 */
public class NFA {

	static HashMap<Integer, HashMap<String, List<Integer>>> nfa = new HashMap<Integer, HashMap<String, List<Integer>>>();
	static HashMap<Integer, HashMap<String, Integer>> dfa_final = new HashMap<Integer, HashMap<String, Integer>>();
	static List<Integer> dfa_final_accept = new ArrayList<Integer>();
	static Integer dfa_final_start = 0;

	public static void main(String[] args) {

		if (args.length > 1) {
			String in1 = args[0];
			String inputString = args[1];

			File toConvert = new File(in1);
			File input = new File(inputString);

			/* Try/catch for conversion file */

			try {
				Scanner sc = new Scanner(toConvert);

				// number of states
				int states = Integer.parseInt(sc.nextLine());

				// sigma values
				String next = sc.nextLine().replaceAll("\\s", "");
				next = next + " ";
				String[] sigmas = next.split("");

				// print the sigmas
				printSigmas(sigmas);

				int count = 0;

				while (count < states) {
					HashMap<String, List<Integer>> trans = new HashMap<String, List<Integer>>();
					String[] line = sc.nextLine().replaceAll("\\s", "").split(":|}");

					for (int i = 1; i < line.length; i++) {
						String[] val = line[i].replace("{", "").split(",");

						List<Integer> toAdd = new ArrayList<Integer>();
						for (int j = 0; j < val.length; j++) {
							if (!val[j].equals("")) {
								toAdd.add(Integer.parseInt(val[j]));
							}
						}

						trans.put(sigmas[i - 1], toAdd);
					}

					nfa.put(count, trans);
					count++;
				}

				// print current NFA
				printNFA(sigmas);

				// print initial state and accepting states
				String init = sc.nextLine();

				List<String> con = Arrays.asList(sc.nextLine().replaceAll("[{}]", "").split(","));
				List<Integer> accept = new ArrayList<Integer>();

				for (String a : con) {
					accept.add(Integer.parseInt(a));
				}

				System.out.println(init + ": Initial State");

				printAccept(accept);

				// convert to dfa
				toDfa(sigmas, init, accept);

				// print dfa to specs
				System.out.println("To DFA:");
				System.out.print(" Sigma:");

				for (int j = 0; j < sigmas.length - 1; j++) {
					System.out.print("    " + sigmas[j]);
				}

				System.out.println();

				printDFADashes(sigmas);

				// print the actual dfa
				printDFA(sigmas);

				printDFADashes(sigmas);
				System.out.println(init + ": Initial State");
				printAccept(dfa_final_accept);

				sc.close();

			} catch (FileNotFoundException e) {
				System.out.println("NFA file could not be found.");
			}

			/* Try/catch to show the files that are part of the language */

			try {

				Scanner sc = new Scanner(input);

				System.out.println("The following strings are accepted: ");

				while (sc.hasNextLine()) {
					String line = sc.nextLine();

					boolean ret = checkSat(line);

					if (ret) {
						System.out.println(line);
					}
				}

				sc.close();

			} catch (FileNotFoundException e) {
				System.out.println("Input String file could not be found.");
			}

		}
	}

	static boolean checkSat(String line) {
		Queue<String> q = new LinkedList<String>(Arrays.asList(line.split("")));
		Integer current = dfa_final_start;

		while (!q.isEmpty()) {
			String val = q.remove();

			if (dfa_final.get(current).containsKey(val) && !val.equals(" ")) {
				current = dfa_final.get(current).get(val);
			} else {
				return false;
			}
		}

		if (dfa_final_accept.contains(current)) {
			return true;
		} else {
			return false;
		}
	}

	static void printAccept(List<Integer> accept) {
		int i = 1;

		for (Integer a : accept) {
			if (i < accept.size()) {
				System.out.print(a + ",");
			} else {
				System.out.println(a + ": Accepting State(s)\n");
			}

			i++;
		}

	}

	private static void printDFADashes(String[] sigmas) {
		// print dashes
		int dash_size = 6 + (sigmas.length - 1) + (4 * (sigmas.length - 1));
		for (int j = 0; j < dash_size; j++) {
			if (j == 0) {
				System.out.print(" -");
			} else {
				System.out.print("-");
			}
		}

		System.out.println();
	}

	static void toDfa(String[] sigmas, String init, List<Integer> accept) {

		HashMap<Integer, HashMap<String, List<Integer>>> dfa = new HashMap<Integer, HashMap<String, List<Integer>>>();
		// list to keep track of where you have been
		List<List<Integer>> visited = new ArrayList<List<Integer>>();
		Queue<List<Integer>> q = new LinkedList<List<Integer>>();

		HashMap<List<Integer>, Integer> visit_map = new HashMap<List<Integer>, Integer>();

		// initialize starting list to do work
		Integer start = Integer.parseInt(init);
		List<Integer> val = getLambda(start);
		q.add(val);
		visited.add(val);

		int next = 0;
		int map_count = 0;
		visit_map.put(val, map_count++);

		while (!q.isEmpty()) {
			List<Integer> it = q.remove();
			dfa.put(next, new HashMap<String, List<Integer>>());

			// initialize new dfa table with sigmas
			for (int i = 0; i < sigmas.length - 1; i++) {
				dfa.get(next).put(sigmas[i], new ArrayList<Integer>());
			}

			for (int i = 0; i < sigmas.length - 1; i++) {
				for (Integer a : it) {
					for (Integer j : nfa.get(a).get(sigmas[i])) {
						List<Integer> lambda = getLambda(j);
						for (Integer b : lambda) {
							if (!dfa.get(next).get(sigmas[i]).contains(b)) {
								dfa.get(next).get(sigmas[i]).add(b);
							}
						}
					}
				}
			}

			// add things to the queue
			for (int i = 0; i < sigmas.length - 1; i++) {
				List<Integer> toAdd = dfa.get(next).get(sigmas[i]);
				Collections.sort(toAdd);
				if (!visited.contains(toAdd)) {
					visited.add(toAdd);
					visit_map.put(toAdd, map_count++);
					q.add(toAdd);
				}
			}
			next++;
		}

		// Convert all q values in dfa to p values of new machine
		// Ex: q0q2 = p0

		Object[] vals = visited.toArray();

		for (int i = 0; i < dfa.size(); i++) {
			dfa_final.put(i, new HashMap<String, Integer>());
			for (int j = 0; j < sigmas.length - 1; j++) {
				for (int k = 0; k < vals.length; k++) {
					if (vals[k].equals(dfa.get(i).get(sigmas[j]))) {
						dfa_final.get(i).put(sigmas[j], k);
					}
				}
			}
		}

		// Calculate the accepting states for the new machine

		Iterator<Map.Entry<List<Integer>, Integer>> iterator = visit_map.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<List<Integer>, Integer> pair = iterator.next();
			for (Integer i : pair.getKey()) {
				if (accept.contains(i)) {
					if (!dfa_final_accept.contains(pair.getValue())) {
						dfa_final_accept.add(pair.getValue());
					}
				}
			}
		}

		Collections.sort(dfa_final_accept);

	}

	static List<Integer> getLambda(Integer val) {
		// THIS FUNCTION SHOULD RETURN ALL LAMBDA ENCLOSURE
		List<Integer> toRet = new ArrayList<Integer>();
		toRet.add(val);

		Queue<Integer> q = new LinkedList<Integer>();

		for (Integer i : nfa.get(val).get(" ")) {
			q.add(i);
		}

		while (!q.isEmpty()) {
			Integer i = q.remove();
			if (!toRet.contains(i)) {
				toRet.add(i);
				q.addAll(nfa.get(i).get(" "));
			}
		}

		return toRet;
	}

	static void printSigmas(String[] sigmas) {
		System.out.print("Sigmas: ");
		for (int i = 0; i < sigmas.length; i++) {
			System.out.print(sigmas[i] + " ");
		}
		System.out.println();
		System.out.println("------");
	}

	static void printNFA(String[] sigmas) {
		Iterator<Map.Entry<Integer, HashMap<String, List<Integer>>>> iterator = nfa.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<Integer, HashMap<String, List<Integer>>> pair = iterator.next();
			System.out.print(pair.getKey() + ":    ");
			for (int i = 0; i < sigmas.length; i++) {
				StringBuilder s = new StringBuilder();
				int count = 1;
				for (Integer a : pair.getValue().get(sigmas[i])) {
					s.append(a);
					if (count < pair.getValue().get(sigmas[i]).size()) {
						s.append(",");
					}
					count++;
				}
				String toPrint = s.toString();
				System.out.print("(" + sigmas[i] + ",{" + toPrint + "}) ");
			}
			System.out.println();
		}

		System.out.println("------");
	}

	static void printDFA(String[] sigmas) {
		Iterator<Map.Entry<Integer, HashMap<String, Integer>>> iterator = dfa_final.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<Integer, HashMap<String, Integer>> pair = iterator.next();

			if (pair.getKey() < 10) {
				System.out.print(" " + pair.getKey() + ":    ");
			} else if (pair.getKey() < 100) {
				System.out.print(" " + pair.getKey() + ":   ");
			} else {
				System.out.print(" " + pair.getKey() + ":  ");
			}
			for (int i = 0; i < sigmas.length - 1; i++) {
				Integer val = pair.getValue().get(sigmas[i]);
				System.out.printf("%5d", val);
			}
			System.out.println();
		}
	}

}
