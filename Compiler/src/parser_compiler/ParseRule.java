package parser_compiler;
import java.util.ArrayList;

public class ParseRule {
    String name;

    ArrayList<ParseRuleItem> items=new ArrayList<ParseRuleItem>();
    public String toString(){
        String result="";
        for(ParseRuleItem pri:items){
            result=result+pri.name+" ";
        }
        result=result.trim();
        return result;

    }
}
