public class Token {
    
    public static final String FINAL = ""; //representa o fim do parser
    public static final String PONTOVIRGULA = ";";
    public static final String DOISPONTOS_IGUAL = ":=";
    public static final String VIRGULA = ",";
    public static final String PONTO = ".";
    public static final String ADD = "+";
    public static final String SUB = "-";
    public static final String MULT = "*";
    public static final String DIV = "/";
    public static final String ABRE_PARENTESES = "(";
    public static final String FECHA_PARENTESES = ")";
    public static final String PROGRAM = "PROGRAM";
    public static final String INTEGER = "INTEGER";
    public static final String REAL = "REAL";
    public static final String STRING = "STRING";
    public static final String BEGIN = "BEGIN";
    public static final String END = "END";
    public static final String IF = "IF";
    public static final String THEN = "THEN";
    public static final String ELSE = "ELSE";
    public static final String WHILE = "WHILE";
    public static final String DO = "DO";
    public static final String REPEAT = "REPEAT";
    public static final String UNTIL = "UNTIL";
    public static final String OR = "OR";
    public static final String AND = "AND";
    public static final String ALL = "ALL";
    public static final String MAIORQ = ">";
    public static final String MENORQ = "<";
    public static final String MENORQ_IGUAL = "<=";
    public static final String MAIORQ_IGUAl = ">=";
    public static final String IGUAL = "=";
    public static final String DIFERENTE = "<>";
    public static final String ESPACO = "ESPACO";
    
    private String id, lexema;
    private int pos;
    private float valor;
    
    public Token(String id, String lexema, int pos, Float valor){
        this.id = id;
        this.lexema = lexema;
        this.pos = pos;
        this.valor = valor;
    }
    
    public Token(String id, String lexema, int pos){
        this.id = id;
        this.lexema = lexema;
        this.pos = pos;
        this.valor = 0;
    }
    
    public Token(){
        this.id = null;
        this.lexema = null;
        this.pos = 0;
        this.valor = 0;
    }
    
    public String getId(){
	return this.id;
    }
    
    public String getLexema(){
	return this.lexema;
    }
    
    public int getPos(){
	return this.pos;
    }
    
    public float getValor(){
	return this.valor;
    }
    
    public void dados(){
        System.out.println("DEBUG: "
                + "\nID: " + this.getId()
                + "\nLEXEMA: " + this.getLexema()
                + "\nPOSIÇÃO: " + this.getPos()
                + "\nVALOR: " + this.getValor() + "\n");
    }
}
