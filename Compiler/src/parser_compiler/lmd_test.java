package parser_compiler;

import java.util.Scanner;
import java.util.Stack;

/**
 * Created by Creative Devil on 3/13/14.
 */

// TODO This class is for testing left-most-derivation of an input token stream using the parse-table created in Parser_compatible

public class lmd_test {
    static Scanner in=new Scanner(System.in);
    public static Parser_compatible pc;
    public static Stack<String> e_stack=new Stack<>();
    public static String token_stream[];
    public static void main(String args[]) {
        lmd();
    }

    public static void lmd(){
        // Taking input
        System.out.print("Enter the token stream : ");
        token_stream=(in.nextLine()+" $").trim().split(" ");   // $ is inserted as the end token
        // Building the Parse Table
        pc=new Parser_compatible();
        System.out.println("\n\n\n          LMD stack content : ");
        System.out.println("..............................................\n\n");
        // Initialize the stack
        e_stack.push("$");
        e_stack.push(pc.first_rule);
        // Evaluate
        for(int i=0;i<token_stream.length;i++){
            System.out.println(e_stack);
            if(!e_stack.isEmpty()) {
                if(!pc.isTerminal(e_stack.peek())){
                String rule_token=null;
                    try{
                        rule_token=pc.parse_Table.get(e_stack.pop(),token_stream[i]).right;
                    }
                    catch(NullPointerException ne ){
                    System.out.println("\n\nError !\nThere is no entry in parse table for the current context !!!\n");
                    System.exit(-1);
                    }
                    if(!rule_token.equals("empty")) {
                        String to_put[]=rule_token.split(" ");
                        for(int j=to_put.length-1;j>=0;j--)
                            e_stack.push(to_put[j]);
                        }
                    i--;
            }
            else {
                    if(e_stack.peek().equals(token_stream[i]))
                        e_stack.pop();
                    else {
                        System.out.println("\n\nError ! Something is wrong !!!\n");
                        System.exit(-2);
                    }
                }
        }
            else {
                System.out.println("\n\nError ! Stack empty !\n");
                System.exit(-3);
            }
        }
        if(e_stack.isEmpty())
            System.out.println("\n\nExpression is correct syntactically !!\n\n");
        else
            System.out.println("Error !");
    }
}
