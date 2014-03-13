package regex_compiler;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

public class LexicalAnalyzer {
	public static void main(String args[]) throws FileNotFoundException {
		nfa[0][0][0] = 1;
		LexicalAnalyzer la = new LexicalAnalyzer();
	la.createNFAfromspecification(
				new File(
						"C:\\codezone\\java\\RegexFull\\src\\lexicalanalyzer\\terminals"),
				new File(
						"C:\\codezone\\java\\RegexFull\\src\\lexicalanalyzer\\tokens"));
		// la.printNFA();
		// la.printDFA();
		for (String tk : la.tokenToNFA.keySet()) {
			System.out.println("Token :  " + tk + " Start : "
					+ la.tokenToNFA.get(tk).start + " End: "
					+ la.tokenToNFA.get(tk).end);
		}
		for (String tk : la.tokenToDFA.keySet()) {
			System.out.println("Token :  " + tk + " Start : "
					+ la.tokenToDFA.get(tk).start + " End: "
					+ la.tokenToDFA.get(tk).end);
		}

		la.lexicalSimulator(new File(
				"C:\\codezone\\java\\RegexFull\\src\\lexicalanalyzer\\testfile2.c"));

		/*
		  //la.convertToNFA("\\+|-|/|;|=|{|}|\\(|\\)", 1); 
		la.convertToNFA("(a|b)*a", 1);
		  la.printNFA();
		  //la.convertToNFA("a|bk*|cd", la.cur_state); //la.printNFA();
		  la.convertToDFA();
		  la.printDFA(); 
		 // la.simulate(new String("+").toCharArray());
		  la.simulate(new String("aba").toCharArray()); */
		 

	}

	HashMap<Integer, String> stateToToken = new HashMap<Integer, String>();

	HashMap<String, NFA> tokenToNFA = new HashMap<String, NFA>();

	HashMap<String, DFA> tokenToDFA = new HashMap<String, DFA>();

	static final int MAX_BUFFER = 1000;

	static final int MAX_TRANS = 100;

	static final int MAX_BUFFER_DFA = MAX_BUFFER * 10;

	static int nfa[][][] = new int[MAX_BUFFER][128][MAX_TRANS];

	int cur_state_dfa = 0;
	int nfa_top = 0;

	int cur_state = 1;
	int last_start_state = 0;
	int nfa_start_state = 0;
	int node_top = 0;
	node nodes[] = new node[MAX_BUFFER];

	char terminalSeparatorStart = '<';
	char terminalSeparatorEnd = '>';
	dfa_stack dfa[] = new dfa_stack[MAX_BUFFER_DFA];

	private void convertNonTerminalsToNFA(String tokenName, String regex) {
		// TODO Auto-generated method stub

		convertToNFA(regex, cur_state);
		// printNFA();
		System.out.println("NFA done for : " + regex + " token : " + tokenName);

		DFA dfa = new DFA();
		dfa.start = cur_state_dfa;

		// start state for the dfa to be produced next...
		convertToDFA();// immediately saves the dfa for this nfa....
		// print
		System.out.println("DFA done for : " + regex + "token : " + tokenName);

		NFA nfa = new NFA();
		node end = pop_info_node();
		node start = pop_info_node();
		nfa.end = end.state;
		nfa.start = start.state;
		nfa.pattern = regex;
		dfa.pattern = regex;
		nfa.type = "token";
		dfa.type = "token";
		tokenToDFA.put(tokenName, dfa);
		tokenToNFA.put(tokenName, nfa);// store the nfa for this token type...
		stateToToken.put(nfa.end, tokenName);

	}

	private void convertTerminalsToNFA(String tokenName, String regex) {
		convertToNFA(regex, cur_state);
		NFA nfa = new NFA();
		node end = pop_info_node();
		node start = pop_info_node();
		nfa.end = end.state;
		nfa.start = start.state;
		nfa.pattern = regex;
		nfa.type = "terminal";
		tokenToNFA.put(tokenName, nfa);// store the nfa for this token type...
		// stateToToken.put(nfa.end, tokenName);

		// end state to token name..may be
		// used to invoke action!
		// the stack is now empty ...new nfa can be formed with new states...
		// note that cur_state is not changed as already created states should
		// not be overwritten!!

	}

	void convertToDFA() {
		int s0[];
		int dfa_start = cur_state_dfa;
		System.out.println("DFA start " + cur_state_dfa);
		// start from here!!

		s0 = eclosure(nodes[node_top - 2].state);
		dfa_stack temp = newdfanode();
		temp.nfa_nodes = s0;
		temp.id = dfa_start;
		// for(int x=0;x<128;x++)
		// temp->transit[x]=-1;
		dfa[cur_state_dfa++] = temp;
		int marked[] = new int[MAX_BUFFER_DFA];
		dfa_stack T;
		while ((T = getunmarkedstate(marked, dfa_start)) != null) {
			marked[T.id] = 1;
			for (int a = 1; a < 128; a++) {
				dfa_stack s = newdfanode();
				if (T.nfa_nodes[0] == 0)
					continue;
				s.nfa_nodes = eclosureT(move(T.nfa_nodes, a));

				if (s.nfa_nodes[0] != 0 && statenotindfa(s, dfa_start)) {
					s.id = cur_state_dfa;
					dfa[cur_state_dfa++] = s;
				} else if (s.nfa_nodes[0] != 0)
					s.id = getexistingidindfa(s, dfa_start);

				if (s.nfa_nodes[0] != 0)
					T.transit[a] = s.id;

				// printf("a=%d\n",a);

			}

		}
		// set accepting states!
		for (int i = dfa_start; i < cur_state_dfa; i++) {
			if (hasfinalstate(dfa[i])) {
				dfa[i].type = 'f';
			}
		}
	}

	void convertToNFA(String regexinput, int start_state) {
		// TODO add escape characters!
		System.out.println("Input :"+ regexinput);
		int i = 0;
		int stack_start_state = node_top;// note the stack top at entry
		int length = regexinput.length();
		@SuppressWarnings("unused")
		char escape_char = 0;
		int escape_index = -100;

		// initialize the nfa
		while (i < length) {
			char cur = regexinput.charAt(i);
			if (cur == '('&&(escape_index!=(i-1))) {
				push_info_node(cur_state, 's', '(');

			} else if (cur == '\\' && (escape_index!=(i-1)) ) {
				// escape character...
			//	i++;
				// the next character is to be escaped....
				escape_char = regexinput.charAt(i);
				escape_index = i;// store where the escape was found and what
									// was it...
				
				// i think it only has to be considered as alphabetic...just
				// make sure of that...

			}

			else if (cur == terminalSeparatorStart&&(escape_index!=(i-1))) {
				String x = "";
				i++;
				while (i < length
						&& regexinput.charAt(i) != terminalSeparatorEnd) {
					x = x + regexinput.charAt(i);
					i++;
				}
				// got the terminal
				if (tokenToNFA.containsKey(x)) {

					NFA nfa = tokenToNFA.get(x);
					// create the nfa fr the pattern and use it from stack!
					convertToNFA(nfa.pattern, cur_state);
					node terminalend = pop_info_node();
					node terminalstart = pop_info_node();
					// nfa=copyNFA(nfa.start, nfa.end); deprecated! :P takes
					// hell lot of time!

					// create another nfa of same kind but with new states..
					push_info_node(terminalstart.state, 's', '<');
					push_info_node(terminalend.state, 'e', '>');
					// pushed the terminal...now do anything on it! :D

				} else {
					System.out.println("Illegal terminal specified!");
					System.exit(1);// wrong terminal specified...
				}

			} else if (cur == '['&&(escape_index!=(i-1))) {
				// optional characters
				// create a string with | of characters and call this function
				// recursively...
				// push_info_node(cur_state,'s','[');
				// TODO pushing [ check for more test cases...
				i++;
				char range[] = new char[MAX_BUFFER * 2];// for considering
														// ORs...
				int ind = 0;
				while (regexinput.charAt(i) != ']'&&i<regexinput.length()) {
					// TODO assuming for now that the string doesnot end before
					// ]
					if(regexinput.charAt(i)=='\\'){
						range[ind++]='\\';
						range[ind++]=regexinput.charAt(i+1);
						range[ind++]='|';
						i++;
						//put in escape character...
						//TODO ...test please...!
						
					}
					else if (regexinput.charAt(i) == '-') {
						char next_char = regexinput.charAt(i + 1); // TODO keep
																	// checks
																	// here!
						if (next_char <= regexinput.charAt(i - 1)) {
							System.out.print("Invalid Range specified!\n");
							System.exit(1);
						}
						for (char c = (char) (regexinput.charAt(i - 1) + 1); c <= next_char; c++) {
							range[ind++] = c;
							range[ind++] = '|';
							// what if range is incorrect?
						}
						i++;// skip character after -

					} else {
						range[ind++] = regexinput.charAt(i);
						range[ind++] = '|';

					}
					i++;

				}
				if(i==regexinput.length()){
					System.out.println("Missing '[' |");
					System.out.println("Pattern : "+regexinput);
					System.exit(2);
					
				}
				if (range[ind - 1] == '|')
					range[ind - 1] = 0;
				else
					range[ind] = 0;
				// putting a null at the end...
				System.out.println("Range exp: "
						+ String.valueOf(range).substring(0, ind - 1));
				convertToNFA(String.valueOf(range).substring(0, ind - 1),
						cur_state);

			} else if (cur == '?'&&(escape_index!=(i-1))) {
				if (isAlphabet(regexinput.charAt(i - 1))||escape_index==(i-2)) {
					//if the last character was alphabetic or an escaped character
					node alphaend = pop_info_node();
					// this is not the end node..a new one must be assigned!
					int r = 0;
					while (nfa[alphaend.state][0][r] != 0)
						r++;
					nfa[alphaend.state][0][r] = cur_state;
					// add epsilon edge from end node to new node

					while (nfa[last_start_state][0][r] != 0)
						r++;

					nfa[last_start_state][0][r] = cur_state;
					// add edge from previous start state to new state
					push_info_node(cur_state, 'e', '?');
					cur_state++;
					// seems ok

				} else if ((regexinput.charAt(i - 1) == ')'
						|| regexinput.charAt(i - 1) == ']'
						|| regexinput.charAt(i - 1) == '>')&&escape_index!=(i-2)) {
					node braceend = pop_info_node();
					node bracestart = pop_info_node();
					nfa[cur_state][0][0] = bracestart.state;
					nfa[cur_state][0][1] = cur_state + 1;

					int r = 0;
					while (nfa[braceend.state][0][r] != 0)
						r++;
					nfa[braceend.state][0][r] = cur_state + 1;
					push_info_node(cur_state, 's', (char) 0);
					push_info_node(cur_state + 1, 'e', (char) 0);
					cur_state = cur_state + 1;

				} else if (regexinput.charAt(i - 1) == '*'
						&& isAlphabet(regexinput.charAt(i - 2))) {
					// TODO looks easy..check for bigger cases ab?*c form..looks
					// meaningless

				} else if (regexinput.charAt(i - 1) == '*'
						&& regexinput.charAt(i - 2) == ')') {

				} else {
					System.out.print("Unexpected Case "
							+ regexinput.charAt(i - 1));
					System.exit(1);// unhandled case! :P
				}

			} else if (cur == ')'&&(escape_index!=(i-1))) {
				node tend = pop_info_node();
				node tprev = null;
				if (tend.input == '(') {
					// nothing to do...empty brace
				} else {
					tprev = pop_info_node();// store last start state!
					node tcur;
					while ((tcur = pop_info_node()).input != '(') {
						if (tcur.type == 'e') {// some end state..put an epsilon
												// transition!
							int r = 0;
							while (nfa[tcur.state][0][r] != 0)
								r++;// find next empty slot!
							nfa[tcur.state][0][r] = tprev.state;// concat by
																// epsilon
																// transition!

						} else
							tprev = tcur;
					}

					// we have end node, we need the start node

				}
				push_info_node(tprev.state, tprev.type, tprev.input);// push
																		// start
																		// node
				push_info_node(tend.state, tend.type, tend.input);// push end
																	// node

			} else if (cur == '*'&&(escape_index!=(i-1))) {
				if (i > 0 && isAlphabet(regexinput.charAt(i - 1))||(escape_index==(i-1))) {
					// apply closure to just one character-might be an escaped special character as well! :)
					int r = 0;
					while (nfa[last_start_state][0][r] != 0) {
						r++;
					}
					nfa[last_start_state][0][r] = cur_state;
					node temp = pop_info_node();
					r = 0;
					while (nfa[temp.state][0][r] != 0)
						r++;
					nfa[temp.state][0][r] = cur_state;
					nfa[temp.state][0][r + 1] = last_start_state;
					push_info_node(cur_state, 'e', (char) 0);
					cur_state++;

				} else {
					// closure is to be applied on a group..very easy! :D
					node end_node = pop_info_node();
					node start_node = pop_info_node();
					int r = 0;
					while (nfa[end_node.state][0][r] != 0)
						r++;
					nfa[end_node.state][0][r] = start_node.state;
					// back link
					nfa[end_node.state][0][r + 1] = cur_state;
					cur_state++;
					nfa[cur_state][0][0] = start_node.state;
					nfa[cur_state][0][1] = cur_state - 1;
					cur_state++;
					push_info_node(cur_state - 1, 's', (char) 0);
					push_info_node(cur_state - 2, 'e', (char) 0);
					// push new start and end into the stack!

				}

			} else if (cur == '|'&&(escape_index!=(i-1))) {
				// find unbalanced portion
				int level = 0;
				char or_part[] = new char[MAX_BUFFER];
				int index = 0;
				i++;
				while (level >= 0 && i < regexinput.length()) {
					if(regexinput.charAt(i)=='\\'){
						or_part[index++]='\\';
						i++;
						//add escaped character too...
					}
					else if (regexinput.charAt(i) == '(')
						level++;
					else if (regexinput.charAt(i) == ')') {
						level--;
						if (level < 0) {

							break;
						}
					} else if (regexinput.charAt(i) == '|' && level == 0) {

						break;
					}
					or_part[index++] = regexinput.charAt(i);
					i++;

				}
				// now find the nfa for this part and put in stack!
				or_part[index] = 0;

				// IMPORTANT need to consolidate the left part....
				node groupend = null;
				node prevstart = null;
				while (node_top > 0) {
					if (nodes[node_top - 1].input == '('
							| nodes[node_top - 1].state < start_state)
						// continue upto this!
						// TODO may not work in longer run! checkpoint!
						break;
					node leftend = pop_info_node();
					node leftstart = pop_info_node();
					if (groupend == null)
						groupend = leftend;// this will be the actual end of the
											// group
					if (prevstart != null) {
						int r = 0;
						while (nfa[leftend.state][0][r] != 0)
							r++;
						nfa[leftend.state][0][r] = prevstart.state;
						// join this start to next's end
					}
					prevstart = leftstart;

				}
				push_info_node(prevstart.state, 's', prevstart.input);
				push_info_node(groupend.state, 'e', groupend.input);// push
																	// start and
																	// end!

				convertToNFA(String.valueOf(or_part).substring(0, index),
						cur_state);
				i--;// i has advanced by 1 while checking..prevent it..

				// TODO check if this works
				node part2end = pop_info_node();
				node part2start = pop_info_node();
				node part1end = pop_info_node();
				node part1start = pop_info_node();
				nfa[cur_state][0][0] = part1start.state;
				nfa[cur_state][0][1] = part2start.state;
				cur_state++;
				int r = 0;
				while (nfa[part2end.state][0][r] != 0)
					r++;

				nfa[part2end.state][0][r] = cur_state;
				// epsilon transition from part 2 to new state
				r = 0;
				while (nfa[part1end.state][0][r] != 0)
					r++;

				nfa[part1end.state][0][r] = cur_state;
				// epsilon transition from part 1 to new state
				// now push new start and end
				push_info_node(cur_state - 1, 's', (char) 0);
				push_info_node(cur_state, 'e', (char) 0);
				cur_state++;

			} else {
				int r = 0;
				if (i > 0
						&& (isAlphabet(regexinput.charAt(i - 1),escape_index)||(escape_index==(i-2)&&i>1) || regexinput
								.charAt(i - 1) == '*')) {// TODO may not work in
															// long run..once
															// check!
															// concat this
															// character...if
															// previous
															// character was a
															// character or a
															// closure or an escaped special character
					node temp = pop_info_node();
					last_start_state = temp.state;// this character transits
													// from this state to a new
													// state
					r = 0;

					while (nfa[temp.state][regexinput.charAt(i) - 1][r] != 0)
						r++;
					nfa[temp.state][regexinput.charAt(i) - 1][r] = cur_state;
					cur_state++;
					push_info_node(cur_state - 1, 'e', regexinput.charAt(i));

				} else {
					// concat not started yet
					while (nfa[cur_state][cur - 1][r] != 0) {
						r++;// find empty slot
					}

					nfa[cur_state][cur - 1][r] = cur_state + 1;
					push_info_node(cur_state, 's', cur);
					last_start_state = cur_state;
					push_info_node(cur_state + 1, 'e', cur);
					cur_state = cur_state + 2;
					// else it must be a character

				}
			}

			i++;
		}

		// patch.....now it will work even without brackets! :) :) gud job!
		if (node_top - stack_start_state > 2) {

			// need to concatanate the states which have been pushed...
			int pop_through = node_top - stack_start_state;
			node end_nodex = pop_info_node();
			// store the last node
			pop_through--;
			while (pop_through > 1) {
				node start_nodex = pop_info_node();
				// get a start node
				node prev_nodex = pop_info_node();
				int r = 0;
				while (nfa[prev_nodex.state][0][r] != 0)
					r++;

				nfa[prev_nodex.state][0][r] = start_nodex.state;

				pop_through = pop_through - 2;
			}
			// push the end state...intermediate nodes have been joined! ;)
			push_info_node(end_nodex.state, end_nodex.type, end_nodex.input);
		}
	}

	public NFA copyNFA(int start_state, int end_state) {
		// create a copy of NFA with new states!
		Set<Integer> states = new HashSet<Integer>();
		states.add(start_state);
		states.add(end_state);

		Set<Integer> covered = new HashSet<Integer>();
		while (covered.size() < states.size()) {
			Integer i = states.iterator().next();
			// states.remove(i);
			if (!covered.contains(i))
				covered.add(i);
			for (int y = 0; y < 128; y++) {
				for (int z = 0; z < MAX_TRANS; z++) {
					if (nfa[i][y][z] != 0 && !states.contains(nfa[i][y][z])) {
						states.add(nfa[i][y][z]);
						System.out.println(nfa[i][y][z]);
					}

				}
			}

		}
		// got all states to copy! :D
		int min = 0;
		for (int i : covered) {
			if (min > i)
				min = i;
		}
		int diff = cur_state - min;
		for (int i : covered) {
			for (int y = 0; y < 128; y++)
				for (int z = 0; z < MAX_TRANS; z++) {
					if (nfa[i][y][z] != 0)
						nfa[i + diff][y][z] = nfa[i][y][z] + diff;// if non zero
																	// then only
																	// put the
																	// value
																	// with the
																	// difference!
																	// hope this
																	// works!
				}

			cur_state++;
		}
		NFA nfa = new NFA();
		nfa.start = start_state + diff;
		nfa.end = end_state + diff;
		return nfa;

	}

	public void createNFAfromspecification(File terminals, File nonterminals)
			throws FileNotFoundException {
		@SuppressWarnings("resource")
		Scanner sc = new Scanner(terminals);// create nfa for terminals!
		while (sc.hasNext()) {
			String line = sc.nextLine();
			String name[] = line.split("->");
			// name[0] holds name of the pattern
			// name[1] holds the pattern
			convertTerminalsToNFA(name[0].trim(), name[1].trim());
			// create the nfa and store in universal hash table

		}
		// now let us create the nfa for nonterminals....
		sc.close();
		sc = new Scanner(nonterminals);
		while (sc.hasNext()) {
			String line = sc.nextLine();
			String name[] = line.split("->");
			//System.out.println(name[1].trim());
			convertNonTerminalsToNFA(name[0].trim(), name[1].trim());
			// separate function for the sole reason that it might contain
			// terminals/non tokens...

		}
		

	}

	private int[] eclosure(int s) {
		int state_stack[] = new int[MAX_BUFFER];
		int group[] = new int[MAX_BUFFER];

		int group_top = 1;
		group[0] = s;
		state_stack[0] = s;
		int state_stack_top = 1;

		int marked[] = new int[MAX_BUFFER];// unmark all states of NFA
		marked[s] = 1;
		while (state_stack_top > 0) {
			state_stack_top--;
			marked[state_stack[state_stack_top]] = 1;
			int i = 0;
			int cur = state_stack[state_stack_top];
			while (nfa[cur][0][i] != 0) {
				if (marked[nfa[cur][0][i]] != 1) {
					group[group_top++] = nfa[cur][0][i];
					state_stack[state_stack_top++] = nfa[cur][0][i];
					marked[nfa[cur][0][i]] = 1;
				}
				i++;
			}
		}
		// for (int x = 0; x < group_top; x++)
		// System.out.printf("%d\n", group[x]);
		// for (int x = group_top; x < MAX_BUFFER_DFA; x++)
		// group[x] = 0;

		return group;
	}

	private int[] eclosureT(int T[]) {
		int i = 0;
		int group[] = new int[MAX_BUFFER];
		int group_top = 0;
		int state_stack[] = new int[MAX_BUFFER];
		int marked[] = new int[MAX_BUFFER];// unmark all states of NFA
		// state_stack[0] = s;
		int state_stack_top = 0;

		while (T[i] != 0) {
			group[group_top++] = T[i];
			state_stack[state_stack_top++] = T[i];
			// marked[T[i]]=1;
			i++;// add all states to group
		}

		// marked[s] = 1;
		while (state_stack_top > 0) {
			state_stack_top--;
			marked[state_stack[state_stack_top]] = 1;
			i = 0;
			int cur = state_stack[state_stack_top];
			while (nfa[cur][0][i] != 0) {
				if (marked[nfa[cur][0][i]] != 1) {
					group[group_top++] = nfa[cur][0][i];
					state_stack[state_stack_top++] = nfa[cur][0][i];
					marked[nfa[cur][0][i]] = 1;
				}
				i++;
			}
		}
		// for (int x = 0; x < group_top; x++)
		// System.out.printf("%d\n", group[x]);
		// for (int x = group_top; x < MAX_BUFFER_DFA; x++)
		// group[x] = 0;

		return group;
	}

	private boolean equal(int[] n1, int[] n2) {
		int n3[] = new int[MAX_BUFFER_DFA];
		int i = 0;
		while (n1[i] != 0) {
			n3[n1[i]] = 1;
			i++;
		}
		i = 0;
		while (n2[i] != 0) {
			// wtf error! 21-01-2014 ! even silliest mistakes make u go crazy!
			// >.< :P
			// previous line : n3[n2[i]]=0; n3[n2[i]]=1; etc.... :P
			if (n3[n2[i]] == 0)
				n3[n2[i]] = 1;
			else
				n3[n2[i]] = 0;
			i++;
		}

		for (i = 0; i < MAX_BUFFER_DFA; i++) {
			if (n3[i] != 0)
				return false;
		}
		// TODO optimize please! :P
		return true;
	}

	private int getexistingidindfa(dfa_stack t, int dfa_start) {
		for (int i = dfa_start; i < cur_state_dfa; i++) {

			if (equal(dfa[i].nfa_nodes, t.nfa_nodes)) {
				return i;

			}
		}
		return -1;
	}

	private dfa_stack getunmarkedstate(int[] marked, int dfa_start) {
		for (int i = dfa_start; i < cur_state_dfa; i++) {
			if (marked[i] != 1) {
				return dfa[i];
			}
		}
		// return NULL;
		return null;
	}

	private boolean hasfinalstate(dfa_stack t) {
		for (int i = 0; t.nfa_nodes[i] != 0; i++) {
			if (t.nfa_nodes[i] == nodes[node_top - 1].state) {
				return true;
			}
		}
		return false;
	}

	private boolean isAlphabet(char x) {
		if((x!='[')&&(x!=']')&&(x!='(')&&(x!=')')&&(x!='*')&&(x!='+')&&(x!='|')&&(x!='?')&&(x!=terminalSeparatorStart)&&(x!='\\'))
			return true;
		else 
			return false;
		// return false;
	}

	private boolean isAlphabet(char x, int escape_index) {
		//it just should not be a special character for regex patterns....
		if((x!='[')&&(x!=']')&&(x!='(')&&(x!=')')&&(x!='*')&&(x!='+')&&(x!='|')&&(x!='?')&&(x!=terminalSeparatorStart)&&(x!='\\'))
			return true;
		else 
			return false;
		
	}

	void lexicalSimulator(File sourceFile) throws FileNotFoundException {
		// will simulate the input file over whole automaton...looks very
		// ambitious! :O
		// hope it works! :|

		@SuppressWarnings("resource")
		Scanner sc = new Scanner(sourceFile);
		StringBuffer sb = new StringBuffer();
		while (sc.hasNext()) {
			sb.append(sc.nextLine() + "\n");
		}
		//System.out.println(sb);
		//System.exit(0);
		String token_selected = "";
		int start_index = 0;
		int end_index = 0;
		int line_no=1;
		int line_start=0;
		System.out.println("\n\n.........................................................................................");
		System.out.println("\n\t\t\t\tLexical Analyzer");
		System.out.println(".........................................................................................\n\n");
		System.out.println("Token\t\t\tValue\t\t\tType\t\tLine no\t\tColumn no");
		System.out.println("......................................................................................\n");
		while (start_index < sb.length()) {
			if (Character.isWhitespace(sb.charAt(start_index))) {
				if(sb.charAt(start_index)=='\n')
					{
					line_start=start_index;
					line_no++;
					}
				start_index++;
				end_index++;
				continue;
				// must ignore whitespace....
			}
			if(sb.charAt(start_index)=='/'){
				if(start_index!=sb.length()-1){
					if(sb.charAt(start_index+1)=='/'){
						start_index++;
						while(start_index<sb.length() && sb.charAt(start_index)!='\n')
							start_index++;
						continue;
					}
				}
			}
			if(start_index<sb.length()-1 && sb.charAt(start_index)=='/' && sb.charAt(start_index+1)=='*'){
						while(!(( start_index<sb.length()-1 && sb.charAt(start_index)=='*' && sb.charAt(start_index+1)=='/') || (start_index==sb.length()-1)))
							start_index++;
						start_index+=2;
						continue;
					}
				
			DFA prev_match_dfa = null;
			for (String cur_token : tokenToDFA.keySet()) {
				int max_match = simulateDFA(tokenToDFA.get(cur_token),
						sb.toString(), start_index);
				if (max_match == -1)
					continue;
				if (end_index < max_match) {
					end_index = max_match;
					token_selected = cur_token;
					prev_match_dfa = tokenToDFA.get(cur_token);
				} else if (end_index == max_match && prev_match_dfa != null) {
					// again a match but with higher priority token..then
					// change....
					if (prev_match_dfa.start > tokenToDFA.get(cur_token).start) {
						prev_match_dfa = tokenToDFA.get(cur_token);
						token_selected = cur_token;
					}

				}

			}
			if (end_index == start_index) {
				System.out.println("Cant match token at " + start_index);
				System.exit(1);
			} else {
				//System.out.println(token_selected + " : "
						//+ sb.substring(start_index, end_index));
				
				// Lets have a proper formatting 
				
				if(token_selected.equals("ID"))
				System.out.printf("ID      \t\t%-8s\t\t%s\t\t%d\t\t%d\n", sb.subSequence(start_index, end_index),token_selected,line_no,(start_index-line_start));
				else
					System.out.printf("%-8s\t\t%-8s\t\t%s\t\t%d\t\t%d\n", sb.subSequence(start_index, end_index),sb.subSequence(start_index, end_index),token_selected,line_no,(start_index-line_start));
	
				/*if(sb.substring(start_index, end_index).length()<=7 && token_selected.length()<=7)
				System.out.println(sb.substring(start_index,end_index)+"\t\t\t  "+token_selected+"\t\t\t"+line_no+"\t\t\t"+(start_index-line_start));
				else if(sb.substring(start_index, end_index).length()>7 && token_selected.length()<=7)
					System.out.println(sb.substring(start_index,end_index)+"\t\t  "+token_selected+"\t\t\t"+line_no+"\t\t\t"+(start_index-line_start));
				else if(sb.substring(start_index, end_index).length()<=7 && token_selected.length()>7)
					System.out.println(sb.substring(start_index,end_index)+"\t\t\t  "+token_selected+"\t\t"+line_no+"\t\t\t"+(start_index-line_start));
				else if(sb.substring(start_index, end_index).length()>7 && token_selected.length()>7)
					System.out.println(sb.substring(start_index,end_index)+"\t\t  "+token_selected+"\t\t"+line_no+"\t\t\t"+(start_index-line_start));
				*/
				start_index = end_index;
			}

		}
		// System.out.println(sb);

	}

	private int[] move(int[] T, int alpha) {
		int group[] = new int[MAX_BUFFER_DFA];
		int group_top = 0;
		int i = 0;
		while (T[i] != 0) {

			int r = 0;
			while (nfa[T[i]][alpha][r] != 0) {
				group[group_top++] = nfa[T[i]][alpha][r];
				r++;

			}
			i++;

		}
		// for (int x = group_top; x < MAX_BUFFER_DFA; x++)
		// group[x] = 0;

		return group;
	}

	private dfa_stack newdfanode() {
		dfa_stack temp = new dfa_stack();
		for (int i = 0; i < 128; i++) {
			temp.transit[i] = -1;
			// initialize everything to -1
		}
		return temp;
	}

	private node pop_info_node() {
		if (node_top > 0)
			return nodes[--node_top];
		else
			return null;

	}

	void printDFA() {
		System.out.printf("\nDFA state status\n");
		for (int i = 0; i < cur_state_dfa; i++) {
			int j = 0;
			System.out.printf("state %d (", i);
			while (dfa[i].nfa_nodes[j] != 0) {
				System.out.printf("%d ", dfa[i].nfa_nodes[j]);
				j++;
			}
			System.out.printf(")\n");
		}

		for (int i = 0; i < cur_state_dfa; i++) {
			for (int j = 0; j < 128; j++) {
				if (dfa[i].transit[j] != -1)
					System.out.printf("state %d ( %c ) -> %d\n", i, (j + 1),
							dfa[i].transit[j]);
			}
		}
	}

	void printNFA() {
		int i = 0;
		while (i < cur_state) {
			for (int j = 0; j < 128; j++) {
				for (int k = 0; k < MAX_TRANS; k++) {
					if (nfa[i][j][k] != 0) {
						System.out.printf("state %d ( %c ) -> %d\n", i,
								(j + 1), nfa[i][j][k]);
					}
				}
			}
			i++;
		}
	}

	private void push_info_node(int state, char type, char input) {
		node temp = new node();
		temp.state = state;
		temp.input = input;
		temp.type = type;
		nodes[node_top] = temp;
		node_top++;

	}

	void simulate(char str[]) {
		dfa_stack start = dfa[0];
		char cur_char = str[0];
		int next_char = 1;
		while (cur_char != 0) {

			if (start.transit[cur_char - 1] == -1) {
				start = null;
				break;
			}
			start = dfa[start.transit[cur_char - 1]];
			if (next_char < str.length)
				cur_char = str[next_char++];
			else
				break;
		}
		if (start == null)
			System.out.printf("Sorry...invalid string!\n");
		else if (start.type == 'f')
			System.out.printf("Yes you got it right! :D\n");
		else
			System.out.printf("Sorry...invalid string!\n");
	}

	int simulateDFA(DFA curDFA, String source, int start) {
		dfa_stack start_state = dfa[curDFA.start];
		while (start < source.length()) {
			if (start_state.transit[source.charAt(start) - 1] != -1) {
				// if(start_state.type=='f')
				start_state = dfa[start_state.transit[source.charAt(start) - 1]];

				start++;
			} else
				break;

		}

		if (start_state.type == 'f')// if the substring was accepted..then
									// return the end of match +1 IMPORTANT!!
			return start;
		else
			return -1;

	}

	private boolean statenotindfa(dfa_stack t, int dfa_start) {
		for (int i = dfa_start; i < cur_state_dfa; i++) {

			if (equal(dfa[i].nfa_nodes, t.nfa_nodes)) {
				return false;

				// TODO
			}
		}
		return true;
		// TODO Auto-generated method stub

	}

}
