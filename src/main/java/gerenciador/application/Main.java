package gerenciador.application;

import javax.swing.SwingUtilities;
import java.io.IOException;

import gerenciador.view.ClienteGUI2;

public class Main {
    public static void main(String[] args) throws IOException {
        //GeradorDeArquivosDeClientes gerador = new GeradorDeArquivosDeClientes();
        //gerador.geraGrandeDataSetDeClientes("finalTest.txt", 10000000);
        SwingUtilities.invokeLater(() -> {
            ClienteGUI2 gui;
            try {
                gui = new ClienteGUI2();
                gui.setVisible(true);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        });
    }

}