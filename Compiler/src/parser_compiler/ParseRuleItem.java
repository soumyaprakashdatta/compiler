package parser_compiler;
public class ParseRuleItem {
    String name;
    String type;
    ParseRule parserule;
    //may be empty for a normal item..will contain a reference to the rule if it is a parserule
    public ParseRuleItem(String name,String type,ParseRule parserule) {
        this.name=name;
        this.type=type;
        this.parserule=parserule;
    }

}
