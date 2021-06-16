import java.io.IOException;
import javax.swing.JOptionPane;

public class CompiladorJava {
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        String nome;
        
        do{
            nome = JOptionPane.showInputDialog("Informe o nome do arquivo de texto:");
            if (nome != null){
                nome = nome.concat(".txt");
                try{
                    Lexico sc;
                    sc = new Lexico();
                    
                    sc.SCANNER(nome); //chamada ao LÉXICO
                }catch (IOException e){
                    JOptionPane.showMessageDialog(null, "Erro na abertura do arquivo, tente novamente!");
                }catch (NovaException e){
                    JOptionPane.showMessageDialog(null, e.getMessage());
                    JOptionPane.showMessageDialog(null, "Compilação encerrada com erros!");
                }
            }
        }while(nome != null);
    }
}