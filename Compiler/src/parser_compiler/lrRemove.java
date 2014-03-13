package parser_compiler;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Scanner;

public class lrRemove {
    public static LinkedHashMap<String, ParseRule> parseRules=new LinkedHashMap<>();
    public static String start_symbol=null;

    void readRules(File grammarFile) throws FileNotFoundException{
        @SuppressWarnings("resource")
		Scanner sc=new Scanner(grammarFile);
        boolean start_symbol_flag=true;
        while(sc.hasNext()){
            String rule=sc.nextLine();
            //take a rule
            String parts[]=rule.split("->");
            if(parseRules.get(parts[0].trim())==null)
            addParseRuleToMap(parts[0],parts[1]);
            else
            addParseRuleToMap(parts[0],parseRules.get(parts[0].trim())+" | "+parts[1]);
            //add to hash map...
            if(start_symbol_flag){
                start_symbol=parts[0].trim();
                start_symbol_flag=false;
            }

        }
        recon();
    }

    private void addParseRuleToMap(String rulename,String ruleRHS) {
        ParseRule parserule=new ParseRule();
        parserule.name=rulename.trim();

        String itemsInRule[]=ruleRHS.trim().split("[\\s]+");

        for(String i:itemsInRule){
            ParseRuleItem pri=new ParseRuleItem(i.trim(), null, null);
            //only assign name for now...
            parserule.items.add(pri);

            //add to parserule
        }
        parseRules.put(parserule.name, parserule);
    }

    /**
     *
     */
    private void recon() {
        for(String i:parseRules.keySet()){
            for(ParseRuleItem pri:parseRules.get(i).items){
                if(parseRules.containsKey(pri.name)){
                    pri.type="Rule";
                    pri.parserule=parseRules.get(pri.name);
                    //categorize items in each
                }
                else
                    pri.type=pri.name;
            }
        }
    }
    void removeLeftRecursion(){

        int rules=parseRules.size();
        ArrayList<ParseRule> allrules=new ArrayList<>();
        allrules.addAll(parseRules.values());

        for(int i=0;i<rules;i++){
            //fetch from parseRules to stay updated!
            for(int j=0;j<i;j++){
                String rule=parseRules.get(allrules.get(i).name).toString();
                String groups[]=rule.trim().split("\\|");
                String rulej=parseRules.get(allrules.get(j).name).toString();

                for(int x=0;x<groups.length;x++){
                    if(groups[x].trim().split(" ").length>1&&groups[x].trim().startsWith(allrules.get(j).name+" ")){
                        //if Ai starts with Aj
                        String xx=rulej+" | ";
                        String rep=xx.replaceAll("\\|",groups[x].replace(allrules.get(j).name, "")+" | ") ;

                        int len=rep.length();
                        groups[x]=rep.substring(0,(len-3)).trim();


                    }

                }
                String rep="";
                for(int x=0;x<groups.length;x++){
                    rep=rep+groups[x]+" | ";
                }
                addParseRuleToMap(allrules.get(i).name, rep.substring(0,rep.length()-2));

            }
            removeImmediateLeftRecursion(parseRules.get(allrules.get(i).name));
        }



    }
    void removeImmediateLeftRecursion(ParseRule pr){
        //remove immediate left recursion from given rule

        String rule=pr.toString();
        ArrayList<String> groups=new ArrayList<String>();
        groups.addAll(Arrays.asList(rule.split("\\|")));
        //String groups[]=rule.split("|");
        if(true){
            //think of a name first..
            String rep_name=pr.name;
            int in=1;
            while(parseRules.containsKey(rep_name)){
                rep_name=pr.name+"_"+in;
                in++;
            }

            //there is an immediate left recursion here..

            String rep_rule="",original_rule="";
            int ind=0;
            int recursionsfound=0;
            for(String i=groups.get(ind);;i=groups.get(ind)){
                if(i.trim().startsWith(pr.name+" ")){
                    recursionsfound++;
                    //identify a group with left recursion
                    String replaced_group=i.trim().substring(pr.name.length()+1).trim()+" "+rep_name;

                    rep_rule=rep_rule+" "+replaced_group+" | ";

                    groups.remove(i);//remove it from current group...

                }
                else
                {if(i.trim().startsWith("empty"))
                    original_rule=original_rule+" " +rep_name+" | ";
                else
                    original_rule=original_rule+i+" " +rep_name+" | ";
                    ind++;
                }
                if(ind==groups.size())
                    break;
            }
            //now add new parse rule to the hashmap
            rep_rule=rep_rule+" empty";

            ParseRule rep_rulex=new ParseRule();
            rep_rulex.name=rep_name;
            for(String i:rep_rule.trim().split("[\\s]+")){
                ParseRuleItem pri=new ParseRuleItem(i.trim(), null, null);
                //only assign name for now...
                rep_rulex.items.add(pri);

                //add to parserule
            }

            if(recursionsfound>0){
                //proceed only if you found a recursion... :)
                original_rule=original_rule.substring(0,original_rule.length()-2).trim();
                ParseRule original_rulex=new ParseRule();
                original_rulex.name=pr.name;
                for(String i:original_rule.trim().split("[\\s]+")){
                    ParseRuleItem pri=new ParseRuleItem(i.trim(), null, null);
                    //only assign name for now...
                    original_rulex.items.add(pri);

                    //add to parserule
                }

                parseRules.put(rep_name, rep_rulex);
                //	parseRules.remove(pr.name);
                parseRules.put(pr.name, original_rulex);
                recon();
            }


        }
    }

    public static void rl_func(){
        Scanner in=new Scanner(System.in);
        lrRemove p=new lrRemove();
        String file_name=null;
        System.out.print("Enter grammar source : ");
        file_name=in.next();

        try {
            p.readRules(new File("C:\\Users\\Creative Devil\\git\\Compiler\\Compiler\\src\\parser_compiler\\"+file_name));
            System.out.println("\n\n\nReading grammar !!");
            for(String i:lrRemove.parseRules.keySet()){
                ParseRule pr=lrRemove.parseRules.get(i);
                System.out.print(pr.name+" -> ");
                System.out.println(pr.toString());
            }
        } catch (FileNotFoundException e) {
            System.out.println("Error !\n" + e.getMessage());
            System.exit(-1);
        }
        p.removeLeftRecursion();
        System.out.println("\n\n\nGrammer after removing left recursion !!");
        for(String i:lrRemove.parseRules.keySet()){
            ParseRule pr=lrRemove.parseRules.get(i);
            System.out.print(pr.name+" -> ");
            System.out.println(pr.toString());
        }
        in.close();
    }

}

