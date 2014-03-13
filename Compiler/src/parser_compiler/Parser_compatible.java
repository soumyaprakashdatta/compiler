package parser_compiler;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Scanner;


public class Parser_compatible {
    private static Scanner in = new Scanner(System.in);

    // data structures used //

    private static HashMap<String,formula> rules=new HashMap<>();
    private static HashMap<String,String> first_list=new HashMap<>();
    private static HashMap<String,String> follow_list=new HashMap<>();
    public static Table<String,String,formula> parse_Table=HashBasedTable.create();
    private static String first_rule=null;

    public static void main(String[] args) {
        createParseTable();
   }

    private static boolean isTerminal(String token){
        if(rules.get(token)==null)
            return true;
        return false;
    }

    @SuppressWarnings("unused")
	private static void takeFormulas(){
        System.out.print("Enter number of formulas : ");
        int num=in.nextInt();
        in.nextLine();

        // storing formulas in an array-list //

        for(int i=0;i<num;i++){
            System.out.print("Formulae : ");
            String arguments[]=in.nextLine().split("->");
            formula f=new formula(arguments[0],arguments[1]);
            rules.put(f.left,f);
            if(i==0)
                first_rule=new String(f.left).trim();
        }
    }

    private static void createFirstList(){
        for(String key:rules.keySet()){
            createFirst(key);
        }
    }

    private static void createFirst(String A){
        A=A.trim();
        if(first_list.get(A)==null)
        {
            formula f=rules.get(A);
            String params[]=f.right.split("\\|");
                    for(String param:params){
                        int epsilon_flag=0;  // explained later
                        param=param.trim();
                        String tokens[]=param.split(" ");

                        int i=0;  // needed to be defined outside the loop , explained later
                        for(String token:tokens) {
                        // first(terminal)=terminal
                            if(isTerminal(token)){
                                if(first_list.get(A)!=null)
                                    first_list.put(A,first_list.get(A)+" "+token);
                                else
                                    first_list.put(A,token);
                                break;
                            }

                            // else first(Non-terminal) is added
                            else{
                                if(tokens[i].equals(""))
                                    continue;
                                // First check whether the non-terminals first has already been calculated or not
                                // if not then calculate it
                                if(first_list.get(token)==null)
                                    createFirst(token);

                                String nt_first=first_list.get(token);
                                if(nt_first==null)
                                    nt_first="";  // just a precaution
                                String to_add="";  // this will contain first(Non-terminal)-epsilon

                                String nt_tokens[]=nt_first.split(" ");
                                for(String nToken:nt_tokens){
                                    if(nToken.equals("empty"))
                                        epsilon_flag=1;
                                    else
                                        to_add+=" "+nToken;
                                }
                                to_add=to_add.trim();

                                if(first_list.get(A)!=null)
                                    first_list.put(A,first_list.get(A)+" "+to_add);
                                else
                                    first_list.put(A,to_add);

                                // if epsilon flag is not set then we don't have to look for first for other characters
                                if(epsilon_flag==0)
                                    break;
                            }
                            i++;
                        }

                        // if we have finished all the characters in the param and for that reason came out of loop
                        // then that means that for every character in param first(character) contains epsilon
                        // so we have to add it to the list

                        if(i==tokens.length)
                            first_list.put(A,first_list.get(A)+" "+"empty");
                    }
                }


    }

    private static void createFollowList(){
        for(String key:rules.keySet()){
            createFollow(key);
        }
    }

    private static void createFollow(String A){
        A=A.trim();
        if(follow_list.get(A)==null){

            // add $ to the start symbol
            if(A.equals(first_rule))
                follow_list.put(A,"$");
            else
                follow_list.put(A,"");

            // In the modified system rules are stored in a hash map
            // we have to go through all the formulas in the hash map and search for the required token in the right hand side of all the formulas

            for(String rule_key:rules.keySet()){
                formula f=rules.get(rule_key);
                String params[]=f.right.split("\\|");
                for(String param:params){
                    param=param.trim();
                    String tokens[]=param.split(" ");
                    for(int index=0;index<tokens.length;index++){
                    if(tokens[index].equals(A)){
                        int epsilon_flag=0;
                        int i;
                        for(i=index+1;i<tokens.length;i++) {
                                if(isTerminal(tokens[i])){   // fail-safe
                                follow_list.put(A,follow_list.get(A)+" "+tokens[i]);
                                break;
                            }
                            else {
                                if(tokens[i].equals(" ") || tokens[i].equals(""))
                                    continue;
                                String first_next=first_list.get(tokens[i]);
                                if(first_next==null)
                                    first_next="";  // just a precaution
                                String to_add="";
                                String fn_tokens[]=first_next.split(" ");
                                    for(String fnToken:fn_tokens){
                                        if(fnToken.equals("empty"))
                                            epsilon_flag=1;
                                        else if(follow_list.get(A).indexOf(fnToken)==-1)
                                            to_add+=" "+fnToken;
                                    }
                                    to_add=to_add.trim();

                                follow_list.put(A,follow_list.get(A)+" "+to_add);

                                if(epsilon_flag==0)
                                    break;
                            }
                        }
                        if(i==tokens.length){
                            if(follow_list.get(f.left)==null)
                                createFollow(f.left);
                            String follow_left=follow_list.get(f.left);
                            if(follow_left==null)
                                follow_left="";  // just a precaution
                            String to_add="";
                            String fl_tokens[]=follow_left.split(" ");
                            for(String flToken:fl_tokens){
                                if(follow_list.get(A).indexOf(flToken)==-1)
                                    to_add+=" "+flToken;
                            }
                            to_add=to_add.trim();
                            follow_list.put(A,follow_list.get(A)+" "+to_add);
                        }
                    }
                    }
                }
            }
        }
    }

    private static void parseTableGeneration(){
        for(String rules_key:rules.keySet()){
            formula f=rules.get(rules_key);
            String sections[]=f.right.split("\\|");
            for(String section:sections){
                section=section.trim();
                ArrayList<String> to_add_token=new ArrayList<>();
                String tokens[]=section.split(" ");
                int i;
                int epsilon_flag=0;

                for(i=0;i<tokens.length;i++){
                    if(tokens[i].equals("empty"))
                        epsilon_flag=1;
                    else if(isTerminal(tokens[i])){
                        formula f1=new formula(f.left,section);
                        parse_Table.put(f.left,tokens[i],f1);
                        for(String keys:to_add_token){
                            f1=new formula(f.left,section);
                            parse_Table.put(f.left,keys,f1);
                        }
                        break;
                    }
                    else {
                        String add_tokens[]=first_list.get(tokens[i]).split(" ");
                        for(String t:add_tokens){
                            if(t.equals("")||t.equals(" "))
                                continue;
                            if(t.equals("empty"))
                                epsilon_flag=1;
                            else
                                to_add_token.add(t);
                        }

                        if(epsilon_flag==0){
                            for(String keys:to_add_token){
                                formula f1=new formula(f.left,section);
                                parse_Table.put(f.left,keys,f1);
                            }
                            break;
                        }

                    }

                    if(i==tokens.length || epsilon_flag==1){
                        String add_tokens[]=follow_list.get(f.left).split(" ");
                        for(String t:add_tokens){
                            if(t.equals("")||t.equals(" "))
                                continue;
                            else
                                to_add_token.add(t);
                        }

                    for(String key:to_add_token){
                        formula f1=new formula(f.left,section);
                        parse_Table.put(f.left,key,f1);
                    }
                    }
                }
            }
        }
    }

    public static void createParseTable(){
        //takeFormulas();
        useLR();
        createFirstList();
        createFollowList();
        parseTableGeneration();
        print_first_list();
        print_follow_list();
        printParseTable();
    }

    static void useLR(){
        lrRemove.rl_func();
        first_rule=lrRemove.start_symbol;
        makeItCompatible(lrRemove.parseRules);
    }

    static void makeItCompatible(LinkedHashMap<String,ParseRule> source){
        for(String key:source.keySet()){
            formula f=new formula(key,source.get(key).toString().trim());
            rules.put(key,f);
        }
    }

    static void print_first_list(){
        int entry_num=first_list.size();
        System.out.println("\n......................................");
        System.out.println("\t\tFirst set");
        System.out.println("......................................\n");
        System.out.println("First list has "+entry_num+" entries .\n");
        if(entry_num>0){
            System.out.println("Name\t\t\t\tEntry");
            System.out.println(".....................................................");
            for(String key:first_list.keySet()){
                System.out.print(key+"\t\t\t\t\t");
                String first=first_list.get(key);
                String tokens[]=first.split(" ");
                for(int i=0;i<tokens.length;i++){
                    System.out.print(tokens[i]);
                    if(i<tokens.length-1)
                        System.out.print(",");
                }
                System.out.println();
            }
        }
    }

    static void print_follow_list(){
        int entry_num=follow_list.size();
        System.out.println("\n......................................");
        System.out.println("\t\tFollow set");
        System.out.println("......................................\n");
        System.out.println("Follow list has "+entry_num+" entries .\n");
        if(entry_num>0){
            System.out.println("Name\t\t\t\tEntry");
            System.out.println("........................................................");
            for(String key:follow_list.keySet()){
                System.out.print(key+"\t\t\t\t\t");
                String follow=follow_list.get(key);
                String tokens[]=follow.split(" ");
                for(int i=0;i<tokens.length;i++){
                    if(tokens[i].equals(""))
                        continue;
                    System.out.print(tokens[i]);
                    if(i<tokens.length-1)
                        System.out.print(",");
                }
                System.out.println();
            }
        }
    }


    static void printParseTable(){
        System.out.println("\n");
        System.out.println("....................................");
        System.out.println("\t\tParse Table");
        System.out.println("....................................");
        System.out.println("\nNon-terminal\t\tTerminal\t\tFormula");
        System.out.println("........................................................................................");
        for(String rowKey:parse_Table.rowKeySet()){
            for(String colKey:parse_Table.columnKeySet()){
                if(parse_Table.get(rowKey,colKey)!=null)
                    System.out.println(rowKey+"\t\t\t"+colKey+"\t\t\t"+parse_Table.get(rowKey,colKey).left+" -> "+parse_Table.get(rowKey,colKey).right);
            }
        }
    }
}
