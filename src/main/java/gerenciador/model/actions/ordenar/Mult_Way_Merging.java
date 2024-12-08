package gerenciador.model.actions.ordenar;

import gerenciador.model.actions.edicaoDeArquivos.manipularArquivo.ArquivoCliente;
import gerenciador.model.cliente.Cliente;

import java.util.Comparator;
import java.io.IOException;
import java.util.List;

public class Mult_Way_Merging {

    private final ArquivoCliente arquivoCliente = new ArquivoCliente();
    int buffer_size = 1000;

    // Método para ordenar um arquivo de texto com clientes
    public void ordenarArquivoClientes(String arquivoEntrada, String arquivoSaida) {
        try {
            // Abrir o arquivo de entrada no modo leitura
            arquivoCliente.abrirArquivo(arquivoEntrada, "leitura", Cliente.class);
            // Carregar todos os clientes do arquivo de texto
            List<Cliente> clientes = arquivoCliente.leiaDoArquivo(buffer_size);


            // Ordenar os clientes por nome e sobrenome
            clientes.sort(Comparator.comparing(Cliente::getNome)
                                     .thenComparing(Cliente::getSobrenome));

            // Fechar o arquivo de entrada
            arquivoCliente.fechaArquivo();

            arquivoCliente.abrirArquivo(arquivoSaida, "escrita", Cliente.class);
            // Escrever os clientes ordenados no arquivo de saída
            arquivoCliente.escreveNoArquivo(clientes);
            arquivoCliente.fechaArquivo();
            System.out.println("Arquivo ordenado e salvo em: " + arquivoSaida);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void printa_lista(List<Cliente> clientes){
        // Verifica se a lista está vazia
        if (clientes == null || clientes.isEmpty()) {
            System.out.println("Nenhum cliente na lista.");
            return;
        }
        
        // Itera sobre a lista e imprime os dados de cada cliente
        for (Cliente cliente : clientes) {
            System.out.println(cliente.toString());
        }
    }
}
