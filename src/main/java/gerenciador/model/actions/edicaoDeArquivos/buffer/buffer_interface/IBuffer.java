package gerenciador.model.actions.edicaoDeArquivos.buffer.buffer_interface;

import java.util.Optional;

import gerenciador.model.actions.edicaoDeArquivos.manipularArquivo.ArquivoSequencial;

public interface IBuffer<T> {

    // Associa o buffer a um arquivo sequencial
    void associaBuffer(ArquivoSequencial<T> arquivoSequencial);

    // Carrega dados do arquivo para o buffer, caso seja um buffer de leitura
    void carregaBuffer();

    // Escreve os dados do buffer no arquivo, caso seja um buffer de escrita
    void escreveBuffer();
    
    Optional<T> proximoCliente();

    T[] proximosClientes(int quantidade);
}
