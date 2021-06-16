import java.util.LinkedList;

public class Semantico {
    private LinkedList<Token>  variaveisStringList   = new LinkedList<>(); //guarda variáveis STRING
    private LinkedList<Token>  variaveisIntegerList  = new LinkedList<>(); //guarda variáveis INTEGER
    private LinkedList<Token>  variaveisRealList     = new LinkedList<>(); //guarda variáveis REAL
    private LinkedList<String> stringExpressoesList  = new LinkedList<>(); //guarda expressões (aritméticas/relacionais)
    //
    private String expressaoAtual = ""; //guarda código de três endereços
    private int cont = 1; //usado nas variáveis temporárias: TMP# + 'cont'
    
    public Semantico(LinkedList<Token> out_VariaveisSList,
                     LinkedList<Token> out_VariaveisIList,
                     LinkedList<Token> out_VariaveisRList) throws NovaException{ //construtor
        
        this.variaveisStringList = out_VariaveisSList;
        this.variaveisIntegerList = out_VariaveisIList;
        this.variaveisRealList = out_VariaveisRList;
        
        SEMANTICS_CHECK_ERRO6(variaveisStringList, variaveisRealList);
        SEMANTICS_CHECK_ERRO6(variaveisStringList, variaveisIntegerList);
        SEMANTICS_CHECK_ERRO6(variaveisStringList, variaveisStringList);
        SEMANTICS_CHECK_ERRO6(variaveisRealList, variaveisIntegerList);
        SEMANTICS_CHECK_ERRO6(variaveisRealList, variaveisRealList);
        SEMANTICS_CHECK_ERRO6(variaveisIntegerList, variaveisIntegerList);
        //observa se as variáveis quebram o ERRO 6, ou seja, se são duplicadas
    }
    
    public LinkedList<String> SEMANTICS(Nodulo out_exp) throws NovaException{ //retorna código de tres endereços
        String temp = "";
        
        stringExpressoesList.clear();
        if(out_exp.raiz.getId().equals(":=")){ //verifica se a expressao é aritmética (TRUE) ou relacional (FALSE)
            temp = temp.concat(out_exp.esq.raiz.getLexema() + " " + out_exp.raiz.getId());
            if(!temFolhas(out_exp.dir)){ //verifica se a folha direita da arvore não tem filhos, indicando uma atribuição simples. Ex: y := 1;
                if(out_exp.dir.raiz.getId().equals("NUMERICO")){
                    temp = temp.concat(" " + out_exp.dir.raiz.getValor());
                }else{
                    temp = temp.concat(" " + out_exp.dir.raiz.getLexema());
                }
                stringExpressoesList.add(temp);
                temp = "";
            }else{ //caso a folha direita da arvore tenha filhos, indica uma atribuição complexa. Ex: y := 1 + (x * 2);
                PercorreArvore(out_exp.dir);
                temp = temp.concat(" TMP#" + Integer.toString(cont - 1));
                stringExpressoesList.add(temp);
                temp = "";
            }
        }else{
            PercorreArvore(out_exp.dir);
                stringExpressoesList.add("IF NOT TMP#" + Integer.toString(cont - 1) + " GOTO");
            }
        return stringExpressoesList;
    }
    
    public void SEMANTICS_CHECK_ALL(Token tokenAtual) throws NovaException{ //usado para observar erros de semântica na expressão ALL();
        boolean variavel_declarada = false;
        
        for (int i = 0; i < variaveisIntegerList.size(); i++) {
            if (tokenAtual.getLexema().equals(variaveisIntegerList.get(i).getLexema())) {
                throw new NovaException("ERRO 3: Tipos incompatíveis, a variavel '" 
                        + tokenAtual.getLexema() + "' nao e <STRING>, linha: " + tokenAtual.getPos());
            }
        }
        
        for (int i = 0; i < variaveisRealList.size(); i++) {
            if (tokenAtual.getLexema().equals(variaveisRealList.get(i).getLexema())) {
                throw new NovaException("ERRO 3: Tipos incompatíveis, a variavel '" 
                        + tokenAtual.getLexema() + "' nao e <STRING>, linha: " + tokenAtual.getPos());
            }
        }
        
        for (int i = 0; i < variaveisStringList.size(); i++) {
            if (tokenAtual.getLexema().equals(variaveisStringList.get(i).getLexema())) {
                variavel_declarada = true;
            }
        }
        
        if(variavel_declarada == false){
            throw new NovaException("ERRO 4: Variavel '" 
                    + tokenAtual.getLexema() + "' nao foi declarada, linha: " + tokenAtual.getPos());
        }
    }
    
    public void SEMANTICS_CHECK_ERRO3_ERRO4(Token tokenAtual, boolean ehExpressao) throws NovaException{ //usado para observar ERRO 3 e 4;
        boolean variavelExiste1 = false;
        boolean variavelExiste2 = false;
        boolean variavelExiste3 = false;
        
        for (int i = 0; i < variaveisIntegerList.size(); i++) {
            if (tokenAtual.getLexema().equals(variaveisIntegerList.get(i).getLexema())) {
                variavelExiste1 = true;
                i = variaveisIntegerList.size();
            }
        }
        
        for (int i = 0; i < variaveisStringList.size(); i++) {
            if (tokenAtual.getLexema().equals(variaveisStringList.get(i).getLexema())) {
                variavelExiste2 = true;
                i = variaveisStringList.size();
                if(ehExpressao){
                    throw new NovaException("ERRO 3: Tipos incompativeis, a variavel '" + tokenAtual.getLexema() 
                            + "' nao e <INTEGER> ou <REAL>, linha: " + tokenAtual.getPos());
                }
            }
        }
        
        for (int i = 0; i < variaveisRealList.size(); i++) {
            if (tokenAtual.getLexema().equals(variaveisRealList.get(i).getLexema())) {
                variavelExiste3 = true;
                i = variaveisRealList.size();
            }
        }
        
        if((variavelExiste1 || variavelExiste2 || variavelExiste3) == false){
            throw new NovaException("ERRO 4: Variavel '" 
                    + tokenAtual.getLexema() + "' nao foi declarada, linha: " + tokenAtual.getPos());
        }
    }
    
    private void SEMANTICS_CHECK_ERRO6(LinkedList<Token> l1,LinkedList<Token> l2) throws NovaException{ //usado para observar ERRO 6
        boolean temp = true; //usado quando as listas forem iguais
        if(l1 == l2){
            temp = false;
        }
        
        for (int i = 0; i < l1.size(); i++) {
            for (int j = 0; j < l2.size(); j++) {
                if(l1.get(i).getLexema().equals(l2.get(j).getLexema()) && temp == true){
                    throw new NovaException("ERRO 6: Variavel '"+ l1.get(i).getLexema() 
                            +"' declarada em duplicidade, linha: " + l1.get(i).getPos());
                }else if(l1.get(i).getLexema().equals(l2.get(j).getLexema()) && temp == false){
                    temp = true;
                }
            }
            if(l1 == l2){
                temp = false;
            }
        }
    }
    
    private void PercorreArvore(Nodulo arvore) { //transforma uma arvore em uma lista de código de três endereços
        if(temFolhas(arvore)){
            if(temFolhas(arvore.esq) || temFolhas(arvore.dir)){
                PercorreArvore(arvore.esq);
                PercorreArvore(arvore.dir);
                PercorreArvore(arvore);
            }else{
                if(!arvore.esq.raiz.getId().equals("NUMERICO")){
                    expressaoAtual = expressaoAtual.concat(arvore.esq.raiz.getLexema());
                }else{
                    expressaoAtual = expressaoAtual.concat(Float.toString(arvore.esq.raiz.getValor()));
                }
                
                expressaoAtual = expressaoAtual.concat(" " + arvore.raiz.getId() + " ");
                
                if(!arvore.dir.raiz.getId().equals("NUMERICO")){
                    expressaoAtual = expressaoAtual.concat(arvore.dir.raiz.getLexema());
                }else{
                    expressaoAtual = expressaoAtual.concat(Float.toString(arvore.dir.raiz.getValor()));
                }
                
                arvore.raiz = new Token(expressaoAtual ,"TMP#" + Integer.toString(cont),arvore.raiz.getPos());
                stringExpressoesList.add(arvore.raiz.getLexema() + " := " + expressaoAtual);
                expressaoAtual = "";
                arvore.esq = null;
                arvore.dir = null;
                cont++;
            }
        }
    }
    
    private boolean temFolhas(Nodulo arvore){ //retorna FALSE se a arvore não tem nenhuma folha; TRUE se tem ao menos uma folha 
        return !(arvore.esq == null && arvore.dir == null);
    }
}