package parser_compiler;

import java.util.ArrayList;
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

    public static int check_status=1;           // 1 means alright, 0 means problem !
    public static node head_node;

    public static void main(String args[]) {
        lmd();
        if(check_status==0)
            System.out.println("Not correct !\n");
        else
            lmd_parseTree();            // if syntax is correct then build the parse tree
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
                    check_status=0;
                    break;
                    //System.exit(-1);
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
                        check_status=0;
                        break;
                        //System.exit(-2);
                    }
                }
        }
            else {
                System.out.println("\n\nError ! Stack empty !\n");
                check_status=0;
                break;
                //System.exit(-3);
            }
        }
        if(e_stack.isEmpty())
            System.out.println("\n\nExpression is correct syntactically !!\n\n");
        else {
            System.out.println("Error !");
            check_status=0;
        }
    }

    public static void lmd_parseTree(){

        // There is no need to check for errors in this function !
        // All errors are detected in the function lmd , which is to be used before this

        // Input token stream and parse table has been generated at the function lmd that is to be called prior to this function
        // lmd can verify whether a given token stream is valid syntactically
        // If yes, then only we will proceed to the next step and build the parse tree

        // Initialize the stack
        // Initializing the tree as well

        e_stack.push("$");
        e_stack.push(pc.first_rule);
        head_node=new node(pc.first_rule);

        // Evaluate
        // Building the tree as well

        node cur=head_node;

        for(int i=0;i<token_stream.length;i++){
            System.out.println(e_stack);
            if(!pc.isTerminal(e_stack.peek())){
                String rule_token =pc.parse_Table.get(e_stack.pop(),token_stream[i]).right;

                if(!rule_token.equals("empty")) {
                    String to_put[]=rule_token.split(" ");
                    for(int j=to_put.length-1;j>=0;j--)
                      e_stack.push(to_put[j]);

                    // add children
                    for(int j=0;j<to_put.length;j++)
                        addNode(cur,to_put[j]);
                    // set cur
                    cur=cur.next;
                }
                else {
                    // if rule_token is empty
                    addNode(cur,"empty");
                    cur=cur.next.next;   // as "empty" node will not have any children
                }
                i--;
            }
            else {
                   // if(e_stack.peek().equals(token_stream[i]))
               e_stack.pop();
               if(cur.next!=null ) cur=cur.next;
            }
        }
    }


    public static void addNode(node parent,String val){
        node to_add=new node(val);
        if(parent.child.isEmpty()){
            to_add.next=parent.next;
            parent.next=to_add;
        }
        else
            to_add.next=parent.child.get(parent.child.size()-1);
        parent.child.add(to_add);
    }

}

class node {
    String val;
    ArrayList<node> child;
    node next;

    public node(String val){
        this.val=val;
        child=new ArrayList<>();
        next=null;
    }
}