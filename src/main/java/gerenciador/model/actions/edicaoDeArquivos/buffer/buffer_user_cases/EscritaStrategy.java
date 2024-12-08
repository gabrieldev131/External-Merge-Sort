package gerenciador.model.actions.edicaoDeArquivos.buffer.buffer_user_cases;

import gerenciador.model.actions.edicaoDeArquivos.buffer.buffer_interface.IBufferModeStrategy;
import gerenciador.model.actions.edicaoDeArquivos.manipularArquivo.*;
import gerenciador.model.cliente.Cliente;

import java.util.Optional;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

public class EscritaStrategy implements IBufferModeStrategy<Cliente> {

    private ArquivoSequencial<Cliente> arquivoSequencial;
    private Queue<Cliente> buffer;
    private final int TAMANHO_BUFFER = 10000;

    public EscritaStrategy() {
        this.buffer = new LinkedList<>();
    }

    @Override
    public void inicializa(String nomeArquivo) throws IOException {
        arquivoSequencial = new ArquivoCliente();
        arquivoSequencial.abrirArquivo(nomeArquivo, "escrita", Cliente.class);
    }

    public ArquivoSequencial<Cliente> getArquivoCliente(){
        return this.arquivoSequencial;
    }
    @Override
    public void carregaBuffer() {
        throw new UnsupportedOperationException("Modo escrita não suporta leitura.");
    }

    @Override
    public void escreveBuffer() {
        try {
            arquivoSequencial.escreveNoArquivo(new LinkedList<>(buffer));
            buffer.clear(); // Limpa o buffer após a escrita
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void fechaBuffer() {
        try {
            arquivoSequencial.fechaArquivo();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void adicionaAoBuffer(Cliente item) {
        buffer.add(item);

        // Se o buffer atingir o tamanho máximo, escreve no arquivo
        if (buffer.size() >= TAMANHO_BUFFER) {
            escreveBuffer();
        }
    }

    @Override
    public Optional<Cliente> proximoCliente() {
        throw new UnsupportedOperationException("Modo escrita não suporta leitura.");
    }

    @Override
    public Cliente[] proximosClientes(int quantidade) {
        throw new UnsupportedOperationException("Modo escrita não suporta leitura.");
    }

    @Override
    public void associaBuffer(ArquivoSequencial<Cliente> arquivoSequencial) {
        if (arquivoSequencial instanceof ArquivoSequencial) {
            this.arquivoSequencial =  arquivoSequencial;
        } else {
            throw new IllegalArgumentException("ArquivoSequencial inválido.");
        }
    }
}
