package gerenciador.model.actions.edicaoDeArquivos.buffer.buffer_interface;

import java.io.IOException;

import gerenciador.model.actions.edicaoDeArquivos.manipularArquivo.ArquivoSequencial;
import gerenciador.model.cliente.Cliente;

public interface IBufferModeStrategy<T> extends IBuffer<T> {

    void inicializa(String nomeArquivo) throws IOException;
    
    void adicionaAoBuffer(T item);

    // Desassocia o buffer e fecha o arquivo
    void fechaBuffer();

    ArquivoSequencial<Cliente> getArquivoCliente();
}
