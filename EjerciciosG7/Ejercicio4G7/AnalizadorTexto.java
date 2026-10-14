package cod_Jv.PracticasF2.EjerciciosG7.Ejercicio4G7;
import javax.swing.JFileChooser;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
public class AnalizadorTexto 
{
    public static void main(String[] args) 
    {
        JFileChooser fileChooser=new JFileChooser();
        int seletc =fileChooser.showOpenDialog(null);
        if(seletc==JFileChooser.APPROVE_OPTION)
        {
            File selectFile=fileChooser.getSelectedFile();
            int totalWords=0;
            int totalLines=0;
            int totalCharacters=0;
            try(Scanner sc=new Scanner(selectFile))
            {
                while (sc.hasNextLine()) 
                {
                    String line=sc.nextLine();
                    String[] words=line.trim().split("\\s+");
                    if(!line.trim().isEmpty())totalWords+=words.length;
                    totalLines++;
                    totalCharacters+=line.length();
                }
            }catch(FileNotFoundException e){System.out.println("Error"+e.getMessage());}
            double average=(double)totalWords/totalLines;
            System.out.println("Total de palabras:"+totalWords+ "Total caracteres:"+totalCharacters+
            "|Total lineas"+totalLines+"|Promedio:"+average);  
        }
    }
    
}
