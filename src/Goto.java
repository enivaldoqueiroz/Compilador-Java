import java.util.LinkedList;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class Goto {
    String charArray = ""; //guarda o arquivo inalterado
    private LinkedList<String> stringList  = new LinkedList<>(); //guarda o arquivo com as alterações em formato String
    private FileWriter arq_escr;
    private PrintWriter gravarArq;
    int linha_goto = 1;
    
    public Goto(LinkedList<Integer> linhas_IF_List,
                LinkedList<Integer> linhas_WHILE_List,
                LinkedList<Integer> linhas_REPEAT_List,
                String arquivo) throws IOException{ //construtor
    
        FileReader arq = new FileReader(arquivo);
        BufferedReader lerArq = new BufferedReader(arq);
        String linha = lerArq.readLine();              
   
        while (linha != null) { //lê o arquivo
            for (int i = 0; i < linha.length(); i++) {
                    charArray = charArray.concat(Character.toString(linha.charAt(i)));
            }
            
            goto_check(linhas_IF_List);
            goto_check(linhas_WHILE_List);
            goto_check(linhas_REPEAT_List);
            //concatena linhas dps do GOTO no arquivo
            
            stringList.add(charArray);
            charArray = "";
            linha_goto++;
            linha = lerArq.readLine();
        }
        arq.close();
        
        arq_escr = new FileWriter(arquivo);
        gravarArq = new PrintWriter(arq_escr);
        
        while(!stringList.isEmpty()){ //escreve o novo arquivo com as alterações
            gravarArq.println(stringList.pop());
        }
        
        arq_escr.close();
    }
    
    private void goto_check(LinkedList<Integer> l1){ //observa se a linha atual tem um GOTO, se sim, concatena o endereço
        if (!l1.isEmpty()) {
            if (linha_goto == l1.peek()) {
                l1.pop();
                charArray = charArray.concat(" " + Integer.toString(l1.pop()));
            }
        }
    }
}
