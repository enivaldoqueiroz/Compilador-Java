import java.util.LinkedList;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;
import javax.swing.JOptionPane;
import java.awt.Desktop;
import java.io.File;

public class Sintatico {
    private LinkedList<Token>   tokens;
    private LinkedList<String>  stringExpressoesList   = new LinkedList<>(); //guarda expressões (aritméticas/relacionais)
    private LinkedList<Token>   variaveisStringList    = new LinkedList<>(); //guarda variáveis STRING
    private LinkedList<Token>   variaveisIntegerList   = new LinkedList<>(); //guarda variáveis INTEGER
    private LinkedList<Token>   variaveisRealList      = new LinkedList<>(); //guarda variáveis REAL
    private LinkedList<Token>   variaveisList          = new LinkedList<>(); //guarda todos os identificadores utilizados no corpo do código (não só os declarados)
    private LinkedList<Boolean> variaveisAuxList       = new LinkedList<>(); //guarda um booleano para cada identificador no corpo do código  
    private LinkedList<Integer> linhas_IF_List         = new LinkedList<>(); //guarda em que linha se encontra o goto nas expressoes IF e para onde esse goto aponta
    private LinkedList<Integer> linhas_WHILE_List      = new LinkedList<>(); //guarda em que linha se encontra o goto nas expressoes WHILE e para onde esse goto aponta
    private LinkedList<Integer> linhas_REPEAT_List     = new LinkedList<>(); //guarda em que linha se encontra o goto nas expressoes REPEAT e para onde esse goto aponta
    //
    private Token tokenAtual; //o ultimo token a ser lido da fila
    private FileWriter arq;
    private PrintWriter gravarArq;
    private int linha = 1;
    
    Scanner ler = new Scanner(System.in);
    
    public void PARSER(LinkedList<Token> tokenFila, String arquivo) throws NovaException, IOException{
        this.tokens = tokenFila;
        tokenAtual = this.tokens.getFirst();
        Semantico sem;
        Goto goto_;
        
        arquivo = arquivo.replaceAll(".txt", "");
        arq = new FileWriter(arquivo + "_compilado.txt"); //cria o arquivo de texto
        gravarArq = new PrintWriter(arq);
        
        program_();     //Program  <id> ;
        espaco_opc();
        while (!tokenAtual.getId().equals(Token.BEGIN)){
            espaco_opc();
            decl_var(); //[ <decl_var> ]*
        }
        sem = new Semantico(variaveisStringList,
                            variaveisIntegerList,
                            variaveisRealList);
        begin_();       //Begin
        bloco(sem);     //Begin [<comando>  [ <comando>]*]? End ;
        end_();         //End
        ponto();        //.
        
        for (int i = 0; i < variaveisList.size(); i++) { //envia as variáveis declaradas e observa o ERRO 3 e 4
            sem.SEMANTICS_CHECK_ERRO3_ERRO4(variaveisList.get(i), variaveisAuxList.get(i));
        }
        
        arq.close();
        goto_ = new Goto(linhas_IF_List, linhas_WHILE_List, linhas_REPEAT_List, arquivo + "_compilado.txt");
        // ^ envia todos os dados para a classe Goto que concatena os endereços dps dos GOTO
        
        JOptionPane.showMessageDialog(null, "Arquivo compilado com sucesso! "
                    + "\n\nSeu arquivo: " + arquivo + "_compilado.txt");
        
        if (Desktop.isDesktopSupported()) { //verifica se o sistema do usuário aceita essa função
            try {
                Desktop desktop = Desktop.getDesktop();
                File arquivoCriado = new File(arquivo + "_compilado.txt");
                desktop.open(arquivoCriado); //abre o arquivo de texto automaticamente
            }catch (IOException e){

            }
        }
    }
    
    private void proxToken(){ //remove o TOKEN atual da fila e pega o proximo
        int linha;
        
        linha = tokenAtual.getPos();
        tokens.pop();

        if (tokens.isEmpty())
            tokenAtual = new Token(Token.FINAL, "", linha);
        else
            tokenAtual = tokens.getFirst();
    }
    
    private void valor(boolean ehExpressao) throws NovaException{ //apenas TOKEN 'ID ou NUMERICO' podem passar
        espaco_opc();
        if (tokenAtual.getId().equals("NUMERICO")){
            proxToken();
            espaco_opc();
        }else if (tokenAtual.getId().equals("ID")){
            variaveisList.add(tokenAtual);
            variaveisAuxList.add(ehExpressao);
            proxToken();
            espaco_opc();
        }else{
            throw new NovaException("Erro 2: Símbolo "
                    + tokenAtual.getId() + " inesperado. Esperando: 'ID ou NUMERICO'. "
                    + "Linha: " + tokenAtual.getPos());
        }
    }
    
    private void id_(boolean ehExpressao/*esse token está em uma expressão?*/) 
            throws NovaException{ //apenas TOKEN 'ID' pode passar
        espaco_opc();
        if (tokenAtual.getId().equals("ID")){
            variaveisList.add(tokenAtual);
            variaveisAuxList.add(ehExpressao);
            gravaToken();
            proxToken();
            espaco_opc();
        }else{
            throw new NovaException("Erro 2: Símbolo "
                    + tokenAtual.getId() + " inesperado. Esperando: 'ID'. "
                    + "Linha: " + tokenAtual.getPos());
        }
    }
    
    private boolean e_valor(String compara) throws NovaException{ //retorna TRUE caso o token seja 'ID ou NUMERICO'
        if (compara.equals("NUMERICO") || compara.equals("ID")){
            return true;
        }else{
            return false;
        }
    }

    private void pontovirgula() throws NovaException{ ////apenas TOKEN ';' pode passar
        espaco_opc();
        if (!tokenAtual.getId().equals(Token.PONTOVIRGULA)){
            throw new NovaException("Erro 2: Símbolo "
                    + tokenAtual.getId() + " inesperado. Esperando: ';'. "
                    + "Linha: " + tokenAtual.getPos());
        }else{
            
        }
    }
      
    private void virgula() throws NovaException{ //apenas TOKEN ',' pode passar
        espaco_opc();
        if (tokenAtual.getId().equals(Token.VIRGULA)){
            gravaToken();
            proxToken();
            espaco_opc();
        }else{
            throw new NovaException("Erro 2: Símbolo "
                    + tokenAtual.getId() + " inesperado. Esperando: ','. "
                    + "Linha: " + tokenAtual.getPos());
        }
    }
    
    private void begin_() throws NovaException{ //apenas TOKEN 'BEGIN' pode passar
        espaco_opc();        
        if (tokenAtual.getId().equals(Token.BEGIN)){
            gravaLinha();
            gravaToken();
            pulaLinha();
            proxToken();
            espaco_opc();
        }else{
            throw new NovaException("Erro 2: Símbolo "
                    + tokenAtual.getId() + " inesperado. Esperando: 'BEGIN'. "
                    + "Linha: " + tokenAtual.getPos());
        }
    }
    
    private void end_() throws NovaException{ //apenas TOKEN 'END' pode passar
        espaco_opc();
        if (tokenAtual.getId().equals(Token.END)){            
            proxToken();
            espaco_opc();
        }else{
            throw new NovaException("Erro 2: Símbolo "
                    + tokenAtual.getId() + " inesperado. Esperando: 'END'. "
                    + "Linha: " + tokenAtual.getPos());
        }
    }
    
    private void ponto() throws NovaException{ //apenas TOKEN '.' pode passar
        espaco_opc();
        if (tokenAtual.getId().equals(Token.PONTO)){
            proxToken();
            gravaLinha();
            espaco_opc();
            gravarArq.printf(" FIM");
        }else{
            throw new NovaException("Erro 2: Símbolo "
                    + tokenAtual.getId() + " inesperado. Esperando: '.'. "
                    + "Linha: " + tokenAtual.getPos());
        }
    }
    
    private void abre_parenteses() throws NovaException{ //apenas TOKEN '(' pode passar
        espaco_opc();
        if (tokenAtual.getId().equals(Token.ABRE_PARENTESES)){
            proxToken();
            espaco_opc();
        }else{
            throw new NovaException("Erro 2: Símbolo "
                    + tokenAtual.getId() + " inesperado. Esperando: '('. "
                    + "Linha: " + tokenAtual.getPos());
        }
    }
    
    private void fecha_parenteses() throws NovaException{ //apenas TOKEN ')' pode passar
        espaco_opc();
        if (tokenAtual.getId().equals(Token.FECHA_PARENTESES)){
            proxToken();
            espaco_opc();
        }else{
            throw new NovaException("Erro 2: Símbolo "
                    + tokenAtual.getId() + " inesperado. Esperando: ')'. "
                    + "Linha: " + tokenAtual.getPos());
        }
    }
    
    private void doispontos_igual() throws NovaException{ //apenas TOKEN ':=' pode passar
        espaco_opc();
        if (tokenAtual.getId().equals(Token.DOISPONTOS_IGUAL)){
            proxToken();
            espaco_opc();
        }else{
            throw new NovaException("Erro 2: Símbolo "
                    + tokenAtual.getId() + " inesperado. Esperando: ':='. "
                    + "Linha: " + tokenAtual.getPos());
        }
    }
    
    private void program_() throws NovaException{ //apenas TOKEN 'PROGRAM' pode passar
        espaco_opc();
        if (tokenAtual.getId().equals(Token.PROGRAM)){
            gravaLinha();
            gravaToken();
            proxToken();
            espaco_obg();
            id_(false);
            variaveisList.pop();
            variaveisAuxList.pop();
            pontovirgula();
            pulaLinha();
            proxToken();
        }else{
            throw new NovaException("Erro 2: Símbolo "
                    + tokenAtual.getId() + " inesperado. Esperando: 'PROGRAM'. "
                    + "Linha: " + tokenAtual.getPos());
        }
    }
    
    private void decl_var() throws NovaException{ //todo o processo de declaração de variáveis, pode ser repetido
        if (tokenAtual.getId().equals(Token.INTEGER)){
            gravaLinha();
            gravaToken();
            proxToken();
            espaco_obg();
            variaveisIntegerList.add(tokenAtual);
            id_(false);
            while(!tokenAtual.getId().equals(";")){
                virgula();
                variaveisIntegerList.add(tokenAtual);
                id_(false);
            }
            pulaLinha();
            proxToken();
            espaco_opc();
        }else if(tokenAtual.getId().equals(Token.REAL)){
            gravaLinha();
            gravaToken();
            proxToken();
            espaco_obg();
            variaveisRealList.add(tokenAtual);
            id_(false);
            while(!tokenAtual.getId().equals(";")){
                virgula();
                variaveisRealList.add(tokenAtual);
                id_(false);
            }
            pulaLinha();
            proxToken();
            espaco_opc();
        }else if(tokenAtual.getId().equals(Token.STRING)){
            gravaLinha();
            gravaToken();
            proxToken();
            espaco_obg();
            variaveisStringList.add(tokenAtual);
            id_(false);
            while(!tokenAtual.getId().equals(";")){
                virgula();
                variaveisStringList.add(tokenAtual);
                id_(false);
            }
            pulaLinha();
            proxToken();
            espaco_opc();
        }else{
            throw new NovaException("Erro 2: Símbolo "
                    + tokenAtual.getId() + " inesperado. Esperando: 'INTEGER, STRING ou REAL'. "
                    + "Linha: " + tokenAtual.getPos());
        }
    }
    
    private void bloco(Semantico sem) throws NovaException{ //Begin [<comando>  [ <comando>]*]? End ;
        begin_();
            comando(sem);
            espaco_opc();
            while (!tokenAtual.getId().equals(Token.END)) {
                espaco_opc();
                comando(sem);
                espaco_opc();
            }
        end_();
        pontovirgula();
        proxToken();
    }
    
    private void comando(Semantico sem) throws NovaException{ //<comando_basico> | <iteracao> | if 
        if (tokenAtual.getId().equals(Token.IF)){
            espaco_opc();
            if_(sem);
            espaco_opc();
        }else if(tokenAtual.getId().equals(Token.WHILE)){
            espaco_opc();
            while_(sem);
            espaco_opc();
        }else if(tokenAtual.getId().equals(Token.REPEAT)){
            espaco_opc();
            repeat_(sem);
            espaco_opc();
        }else{
            comando_basico(sem);
        }
    }
    
    private void comando_basico(Semantico sem) throws NovaException{ //<atribuicao> | <bloco> | All ( <id>  [, <id>]* );
        Nodulo temp = new Nodulo(null);
        
        espaco_opc();
        if (tokenAtual.getId().equals("ID")){ //<id> := <expr_arit> ;
            variaveisList.add(tokenAtual);
            variaveisAuxList.add(true);
            temp.esq = new Nodulo(tokenAtual);
            proxToken();
            espaco_opc();
            temp.raiz = tokenAtual;
            doispontos_igual();
                temp.dir = expr_arit();
                stringExpressoesList = sem.SEMANTICS(temp);
                while(!stringExpressoesList.isEmpty()){
                    gravaLinha();
                    gravarArq.printf(" " + stringExpressoesList.pop()); //escreve expressões no arquivo
                    pulaLinha();
                }
            pontovirgula();
            proxToken();
        }else if(tokenAtual.getId().equals(Token.ALL)){
            espaco_opc();
            all(sem);
            espaco_opc();
        }else{
            bloco(sem);
        }
    }
    
    private void all(Semantico sem) throws NovaException{
        gravaLinha();
        gravaToken();
        proxToken();
        espaco_opc();
        gravaToken();
        abre_parenteses();
            sem.SEMANTICS_CHECK_ALL(tokenAtual); //observa ERRO 3 e 4
            gravaToken();
            valor(false);
            while (!tokenAtual.getId().equals(")")) {
                virgula();
                sem.SEMANTICS_CHECK_ALL(tokenAtual); //observa ERRO 3 e 4
                gravaToken();
                valor(false);
            }
        gravaToken();
        fecha_parenteses();
        pontovirgula();
        pulaLinha();
        proxToken();
    }
    
    private void if_(Semantico sem) throws NovaException{ //if (<expr_relacional>) then <comando> [else <comando>]?
        Nodulo temp = new Nodulo(null);
        temp.raiz = tokenAtual;
        
        proxToken();
        espaco_opc();
        abre_parenteses();
            temp.dir = expr_relacional();
            stringExpressoesList = sem.SEMANTICS(temp);
            while(!stringExpressoesList.isEmpty()){
                gravaLinha();
                gravarArq.printf(" " + stringExpressoesList.pop()); //escreve expressões no arquivo
                pulaLinha();
            }
        fecha_parenteses();
        then_();
        linhas_IF_List.add(linha - 1);
        comando(sem);
        linhas_IF_List.add(linha);
        espaco_opc();
        if(tokenAtual.getId().equals(Token.ELSE)){
            linhas_IF_List.pollLast();
            linhas_IF_List.add(linha + 1);
            gravaLinha();
            gravarArq.printf(" GOTO");
            linhas_IF_List.add(linha);
            pulaLinha();
            gravaLinha();
            gravarArq.printf(" ELSE");
            proxToken();
            espaco_opc();
            pulaLinha();
            comando(sem);
            linhas_IF_List.add(linha);
        }
    }
    
    private void then_() throws NovaException{ //apenas TOKEN 'THEN' pode passar
        espaco_opc();
        if (tokenAtual.getId().equals(Token.THEN)){
            proxToken();
            espaco_opc();
        }else{
            throw new NovaException("Erro 2: Símbolo "
                    + tokenAtual.getId() + " inesperado. Esperando: 'THEN'. "
                    + "Linha: " + tokenAtual.getPos());
        }
    }
    
    private void while_(Semantico sem) throws NovaException{ //while (<expr_relacional>) do <comando>
        Nodulo temp = new Nodulo(null);
        temp.raiz = tokenAtual;
        int temp2;
        
        temp2 = linha;
        proxToken();
        espaco_opc();
        abre_parenteses();
            temp.dir = expr_relacional();
            stringExpressoesList = sem.SEMANTICS(temp);
            while(!stringExpressoesList.isEmpty()){
                gravaLinha();
                gravarArq.printf(" " + stringExpressoesList.pop()); //escreve expressões no arquivo
                pulaLinha();
            }
        fecha_parenteses();
        do_();
        linhas_WHILE_List.add(linha - 1);
        comando(sem);
        linhas_WHILE_List.add(linha + 1);
        gravaLinha();
        gravarArq.printf(" GOTO " + temp2);
        pulaLinha();
    }
    
    private void do_() throws NovaException{ //apenas TOKEN 'DO' pode passar
        espaco_opc();
        if (tokenAtual.getId().equals(Token.DO)){
            proxToken();
            espaco_opc();
        }else{
            throw new NovaException("Erro 2: Símbolo "
                    + tokenAtual.getId() + " inesperado. Esperando: 'DO'. "
                    + "Linha: " + tokenAtual.getPos());
        }
    }
    
    private void repeat_(Semantico sem) throws NovaException{ //repeat <comando> until (<expr_relacional>);
        Nodulo temp = new Nodulo(null);
        temp.raiz = tokenAtual;
        int temp2;
        
        temp2 = linha;
        gravaLinha();
        gravaToken();
        proxToken();
        espaco_opc();
        pulaLinha();        
        comando(sem);
        until_();
        abre_parenteses();
            temp.dir = expr_relacional();
            stringExpressoesList = sem.SEMANTICS(temp);
            while(!stringExpressoesList.isEmpty()){
                gravaLinha();
                gravarArq.printf(" " + stringExpressoesList.pop());
                pulaLinha();
            }
        fecha_parenteses();
        pontovirgula();
        linhas_REPEAT_List.add(linha - 1);
        linhas_REPEAT_List.add(temp2);
        proxToken();
    }
    
    private void until_() throws NovaException{ //apenas TOKEN 'UNTIL' pode passar
        espaco_opc();
        if (tokenAtual.getId().equals(Token.UNTIL)){
            proxToken();
            espaco_opc();
        }else{
            throw new NovaException("Erro 2: Símbolo "
                    + tokenAtual.getId() + " inesperado. Esperando: 'UNTIL'. "
                    + "Linha: " + tokenAtual.getPos());
        }
    }
    
    private Nodulo expr_relacional() throws NovaException{ //retorna uma arvore binaria de expressao relacional
    //<val> <op_relacionais> <val> | (<expr_relacional>) [<op_booleanos> (<expr_relacional>)] ?
        Nodulo arvore = new Nodulo(null);
        
        espaco_opc();
        if(e_valor(tokenAtual.getId())){
            arvore.esq = new Nodulo(tokenAtual);
            valor(true);
            arvore.raiz = tokenAtual;
            op_relacionais();
            arvore.dir = new Nodulo(tokenAtual);
            valor(true);
            
            return arvore;
        }else{
            abre_parenteses();
                arvore.esq = expr_relacional();
            fecha_parenteses();
            do{                
                arvore.raiz = tokenAtual;                
                proxToken();
                abre_parenteses();
                arvore.dir = expr_relacional();
                fecha_parenteses();
                if(tokenAtual.getId().equals(Token.AND) || tokenAtual.getId().equals(Token.OR)){
                    arvore.esq = new Nodulo(arvore.raiz, arvore.esq, arvore.dir);
                    arvore.raiz = null;
                    arvore.dir = null;
                }
            }while(tokenAtual.getId().equals(Token.AND) || tokenAtual.getId().equals(Token.OR));

            return arvore;
        }
    }
    
    private Nodulo expr_arit() throws NovaException{ //retorna uma arvore binaria de expressao aritmetica
    //<val> | <val>  <op_aritmetico> <val> | (<expr_arit> ) <op_aritmetico> (<expr_arit>)
        Nodulo arvore = new Nodulo(null);
        Token temp;
    
        espaco_opc();
        if(e_valor(tokenAtual.getId())){
            temp = tokenAtual;
            
            arvore.raiz = temp;
            valor(true);
            
            if(tokenAtual.getId().equals(Token.ADD) 
                || tokenAtual.getId().equals(Token.SUB) 
                || tokenAtual.getId().equals(Token.DIV) 
                || tokenAtual.getId().equals(Token.MULT)){
                
                arvore.raiz = tokenAtual;
                arvore.esq = new Nodulo(temp);
                op_arit();
                arvore.dir = new Nodulo (tokenAtual);
                valor(true);
            }
            
            return arvore;
        }else{
            abre_parenteses();
                arvore.esq = expr_arit();
            fecha_parenteses();
                arvore.raiz = tokenAtual;
                op_arit();
            abre_parenteses();
                arvore.dir = expr_arit();
            fecha_parenteses();
            
            return arvore;
        }
    }
    
    private void op_arit() throws NovaException{ // + | - | * | /
        espaco_opc();
        if (tokenAtual.getId().equals(Token.ADD) 
                || tokenAtual.getId().equals(Token.SUB) 
                || tokenAtual.getId().equals(Token.DIV) 
                || tokenAtual.getId().equals(Token.MULT)){
            proxToken();
            espaco_opc();
        }else{
            throw new NovaException("Erro 7: Operador "
                    + tokenAtual.getId() + " invalido. Esperando: 'Operador aritmetico'. "
                    + "Linha: " + tokenAtual.getPos());
        }
    }
    
    private void op_relacionais() throws NovaException{ // < | > | <= | >= | = | <>
        espaco_opc();
        if (tokenAtual.getId().equals(Token.MENORQ) 
                || tokenAtual.getId().equals(Token.MAIORQ) 
                || tokenAtual.getId().equals(Token.MENORQ_IGUAL) 
                || tokenAtual.getId().equals(Token.MAIORQ_IGUAl) 
                || tokenAtual.getId().equals(Token.IGUAL) 
                || tokenAtual.getId().equals(Token.DIFERENTE)){
            proxToken();
            espaco_opc();
        }else{
            throw new NovaException("Erro 7: Operador "
                    + tokenAtual.getId() + " invalido. Esperando: 'Operador relacional'. "
                    + "Linha: " + tokenAtual.getPos());
        }
    }
    
    private void espaco_opc(){ //se houver um espaço, considera como um espaço opcional
        if(tokenAtual.getId().equals(Token.ESPACO)){
            proxToken();
        }
    }
    
    private void espaco_obg() throws NovaException{ //se houver um espaço, considera como um espaço obrigatório
        if(tokenAtual.getId().equals(Token.ESPACO)){
            proxToken();
        }else{
            throw new NovaException("Erro 2: Símbolo "
                    + tokenAtual.getId() + " inesperado. Observar o espacamento. "
                    + "Linha: " + tokenAtual.getPos());
        }
    }
    
    private void pulaLinha(){ //pula uma linha no arquivo
        gravarArq.printf("%n");
        linha++;
    }
    
    private void gravaLinha(){ //grava uma linha no arquivo
        if(linha < 10){
            gravarArq.printf("000" + Integer.toString(linha) + ":");
        }else if(linha < 100){
            gravarArq.printf("00" + Integer.toString(linha) + ":");
        }else if(linha < 1000){
            gravarArq.printf("0" + Integer.toString(linha) + ":");
        }else{
            gravarArq.printf(Integer.toString(linha) + ":");
        }
    }
    
    private void gravaToken(){ //grava um TOKEN no arquivo
        if(tokenAtual.getLexema().equals("")){
            gravarArq.printf(" " + tokenAtual.getId());
        }else{
            gravarArq.printf(" " + tokenAtual.getLexema());
        }
    }
}