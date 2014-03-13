package parser_compiler;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;


public class Parser {
	private static Scanner in = new Scanner(System.in);

    // data structures used //

    private static ArrayList<formula> rules=new ArrayList<>();
    private static HashMap<String,String> first_list=new HashMap<>();
    private static HashMap<String,String> follow_list=new HashMap<>();
    public static String parse_table[][]=null;

	public static void main(String[] args) {
		createParseTable();
	}

    private static boolean isTerminal(char c){
        return (c>='a' && c<='z')||(c>='0' && c<='9')||(c=='!')||(c=='+' || c=='-' || c=='*' || c=='/' || c=='(' || c==')');   // here say ! is acting as the epsilon
    }

    private static void takeFormulas(){
        System.out.print("Enter number of formulas : ");
        int num=in.nextInt();
        in.nextLine();

        // storing formulas in an array-list //

        for(int i=0;i<num;i++){
            System.out.print("Formulae : ");
            String arguments[]=in.nextLine().split("->");
            formula f=new formula(arguments[0],arguments[1]);
            rules.add(f);
        }
    }

    static void removeLeftRecursion(){
        for(int i=0;i<rules.size();i++){
            for(int j=0;j<i;j++){
                System.out.println("i : "+i+"\tj : "+j);
                formula f_j=rules.get(j);
                formula f_i=rules.get(i);
                String tokens[]=f_i.right.split("|");
                for(String token:tokens){
                    System.out.println("Token before : "+token);
                    if(token.trim().startsWith(f_j.left)){
                       // token.replace(f_j.left,f_j.right);
                    System.out.println("Token after : "+token);
                    }
                }
                immediate_LeftRecursion_Remove();
            }
        }

    }

    static void immediate_LeftRecursion_Remove(){

    }

    private static void createFirstList(){
        for(formula f:rules){
            createFirst(f.left);
        }
    }

    private static void createFirst(String A){
        if(first_list.get(A)==null)
        {
            for(formula f:rules){
                if(f.left.equals(A)){
                    String params[]=f.right.split("\\|");
                    for(String param:params){
                        int epsilon_flag=0;  // explained later
                        int i;  // needed to be defined outside the loop , explained later
                        for(i=0;i<param.length();i++){

                            // first(terminal)=terminal
                            if(isTerminal(param.charAt(i))){
                                if(first_list.get(A)!=null)
                                    first_list.put(A,first_list.get(A)+""+param.charAt(i));
                                else
                                    first_list.put(A,""+param.charAt(i));
                                break;
                            }

                            // else first(Non-terminal) is added
                            else{
                                // First check whether the non-terminals first has already been calculated or not
                                // if not then calculate it
                                if(first_list.get(param.charAt(i)+"")==null)
                                    createFirst(param.charAt(i)+"");

                                String nt_first=first_list.get(param.charAt(i)+"");
                                if(nt_first==null)
                                    nt_first="";  // just a precaution
                                String to_add="";  // this will contain first(Non-terminal)-epsilon
                                for(int k=0;k<nt_first.length();k++){
                                    // If we find an epsilon in the first
                                    // set the epsilon flag
                                    // it will ensure that we will look for the first for next character
                                    if(nt_first.charAt(k)=='!')
                                        epsilon_flag=1;
                                    else
                                        to_add+=""+nt_first.charAt(k);
                                }

                                if(first_list.get(A)!=null)
                                    first_list.put(A,first_list.get(A)+to_add);
                                else
                                    first_list.put(A,to_add);

                                // if epsilon flag is not set then we don't have to look for first for other characters
                                if(epsilon_flag==0)
                                    break;
                            }
                        }

                        // if we have finished all the characters in the param and for that reason came out of loop
                        // then that means that for every character in param first(character) contains epsilon
                        // so we have to add it to the list
                        if(i==param.length())
                            first_list.put(A,first_list.get(A)+"!");
                    }
                }
            }
        }
    }

    private static void createFollowList(){
        for(formula f:rules){
            createFollow(f.left);
        }
    }

    private static void createFollow(String A){
        if(follow_list.get(A)==null){
            // add $ to the start symbol
            if(A.equals(rules.get(0).left))
                follow_list.put(A,"$");
            else
                follow_list.put(A,"");

            for(formula f:rules){
                String params[]=f.right.split("\\|");
                for(String param:params){
                    int index=param.indexOf(A);
                    if(index!=-1){
                            int epsilon_flag=0;
                            int i;
                            for(i=index+1;i<param.length();i++) {
                                if(isTerminal(param.charAt(i)) && follow_list.get(A).indexOf(param.charAt(i))==-1){
                                    follow_list.put(A,follow_list.get(A)+""+param.charAt(i));
                                    break;
                                }
                                else {
                                    String first_next=first_list.get(param.charAt(i)+"");
                                    if(first_next==null)
                                        first_next="";  // just a precaution
                                    String to_add="";
                                    for(int k=0;k<first_next.length();k++){
                                        if(first_next.charAt(k)=='!')
                                            epsilon_flag=1;
                                        else if(follow_list.get(A).indexOf(first_next.charAt(k))==-1)
                                            to_add+=""+first_next.charAt(k);
                                    }

                                    follow_list.put(A,follow_list.get(A)+to_add);

                                    if(epsilon_flag==0)
                                        break;
                                }
                            }
                            if(i==param.length()){
                                if(follow_list.get(f.left)==null)
                                    createFollow(f.left);
                                String follow_left=follow_list.get(f.left);
                                if(follow_left==null)
                                    follow_left="";  // just a precaution
                                String to_add="";
                                for(int k=0;k<follow_left.length();k++){
                                    if(follow_list.get(A).indexOf(follow_left.charAt(k))==-1)
                                        to_add+=""+follow_left.charAt(k);
                                }
                                follow_list.put(A,follow_list.get(A)+to_add);
                            }
                    }
                }
            }
        }
    }

    public static void createParseTable(){
        takeFormulas();
        //removeLeftRecursion();
        createFirstList();
        createFollowList();
        print_first_list();
        print_follow_list();
    }

    static void print_first_list(){
        int entry_num=first_list.size();
        System.out.println("\n......................................");
        System.out.println("\t\tFirst set");
        System.out.println("....................................\n");
        System.out.println("First list has "+entry_num+" entries .\n");
        if(entry_num>0){
            System.out.println("Name\t\tEntry");
            System.out.println("....................................");
            for(String key:first_list.keySet()){
                System.out.print(key+"\t\t\t");
                String first=first_list.get(key);
                for(int i=0;i<first.length();i++){
                    System.out.print(first.charAt(i));
                    if(i<first.length()-1)
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
            System.out.println("Name\t\tEntry");
            System.out.println("....................................");
            for(String key:follow_list.keySet()){
                System.out.print(key+"\t\t\t");
                String follow=follow_list.get(key);
                for(int i=0;i<follow.length();i++){
                    System.out.print(follow.charAt(i));
                    if(i<follow.length()-1)
                        System.out.print(",");
                }
                System.out.println();
            }
        }
    }
}
