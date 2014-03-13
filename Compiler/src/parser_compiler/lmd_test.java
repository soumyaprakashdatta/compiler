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
        System.out.println("No of tokens : "+token_stream.length);
        // Building the Parse Table
        pc=new Parser_compatible();
        // Initialise the stack
        e_stack.push("$");
        e_stack.push(pc.first_rule);
        // Evaluate
        for(int i=0;i<token_stream.length;i++){
            if(!e_stack.isEmpty()) {
                if(!pc.isTerminal(e_stack.peek())){
                String rule_token=pc.parse_Table.get(e_stack.pop(),token_stream[i]).right;
                if(rule_token==null){
                    System.out.println("Error ! Something is wrong !!!\n");
                    System.exit(-1);
                }
                String to_put[]=rule_token.split(" ");
                for(int j=to_put.length-1;j>=0;j--)
                    e_stack.push(to_put[j]);
                    i--;
            }
            else {
                    if(e_stack.peek().equals(token_stream[i]))
                        e_stack.pop();
                    else {
                        System.out.println("Error ! Something is wrong !!!\n");
                        System.exit(-2);
                    }
                }
        }
            else {
                System.out.println("Error ! Stack empty !\n");
                System.exit(-3);
            }
        }
        if(e_stack.isEmpty())
            System.out.println("Expression is correct syntactically !!\n\n");
        else
            System.out.println("Error !");
    }
}
