package test;

/**
 * Created by Creative Devil on 3/13/14.
 */
public class btree {
    public static void main(String args[]){
        node t_head=new node(1);
        addChild(t_head,2);
        addChild(t_head,3);
        addChild(t_head.child[0],5);
        addChild(t_head,4);
    }

    public static void addChild(node parent,int val){
        if(parent.child[0]==null)
            parent.child[0]=new node(val);
        else if(parent.child[1]==null)
            parent.child[1]=new node(val);
        else
            System.out.println("Already has two child.\n");
    }

}

class node {
    int val;
   // int max_count;                     // Not necessary in b-tree
    node child[]=new node[2];

    public node(int val){
        this.val=val;
        //max_count=0;
        child[0]=null;
        child[1]=null;
    }
}
