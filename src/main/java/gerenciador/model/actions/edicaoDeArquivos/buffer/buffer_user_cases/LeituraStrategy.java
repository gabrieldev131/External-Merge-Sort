package gerenciador.model.actions.edicaoDeArquivos.buffer.buffer_user_cases;

import gerenciador.model.actions.edicaoDeArquivos.buffer.buffer_interface.*;
import gerenciador.model.actions.edicaoDeArquivos.manipularArquivo.*;
import gerenciador.model.cliente.Cliente;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;

public class LeituraStrategy implements IBufferModeStrategy<Cliente> {

    private ArquivoSequencial<Cliente> arquivoSequencial;
    private Queue<Cliente> buffer;
    private final int TAMANHO_BUFFER = 10000;

    public LeituraStrategy() {
        this.buffer = new LinkedList<>();
    }

    @Override
    public void inicializa(String nomeArquivo) throws IOException {
        try{
            arquivoSequencial = new ArquivoCliente();
            arquivoSequencial.abrirArquivo(nomeArquivo, "leitura", Cliente.class);
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }

    public ArquivoSequencial<Cliente> getArquivoCliente(){
        return this.arquivoSequencial;
    }
    
    @Override
    public void carregaBuffer() {
        try {
            buffer.addAll(arquivoSequencial.leiaDoArquivo(TAMANHO_BUFFER));
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void escreveBuffer() {
        throw new UnsupportedOperationException("Modo leitura não suporta escrita.");
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
    public Optional<Cliente> proximoCliente() {
        if (buffer.isEmpty()) {
            carregaBuffer();
        }
        return Optional.ofNullable(buffer.poll());
    }

    @Override
    public Cliente[] proximosClientes(int quantidade) {
        Cliente[] clientes = new Cliente[quantidade];
        int i = 0;
        while (i < quantidade) {
            Optional<Cliente> clienteOpt = proximoCliente();
            if (clienteOpt.isEmpty()) {
                break;
            }
            clientes[i] = clienteOpt.get();
            i++;
        }
        return clientes;
    }

    @Override
    public void adicionaAoBuffer(Cliente item) {
        throw new UnsupportedOperationException("Modo leitura não suporta adicionar itens.");
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

