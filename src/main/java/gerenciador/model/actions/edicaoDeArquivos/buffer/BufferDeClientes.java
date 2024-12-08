package gerenciador.model.actions.edicaoDeArquivos.buffer;

import gerenciador.model.actions.edicaoDeArquivos.buffer.buffer_interface.IBuffer;
import gerenciador.model.actions.edicaoDeArquivos.buffer.buffer_interface.IBufferModeStrategy;
import gerenciador.model.actions.edicaoDeArquivos.manipularArquivo.*;
import gerenciador.model.cliente.Cliente;

import java.io.IOException;
import java.util.Optional;

public class BufferDeClientes implements IBuffer<Cliente> {
    private IBufferModeStrategy<Cliente> estrategiaAtual;

    public void setEstrategia(IBufferModeStrategy<Cliente> estrategia) {
        this.estrategiaAtual = estrategia;
    }

    public void inicializa(String nomeArquivo) throws IOException {
        try{
            estrategiaAtual.inicializa(nomeArquivo);
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }
  
    // Delegação de métodos
    @Override
    public void carregaBuffer() {
        estrategiaAtual.carregaBuffer();
    }

    public ArquivoSequencial<Cliente> getArquivoCliente(){
        return estrategiaAtual.getArquivoCliente();
    }
    @Override
    public void escreveBuffer() {
        estrategiaAtual.escreveBuffer();
    }

    @Override
    public Optional<Cliente> proximoCliente() {
        return estrategiaAtual.proximoCliente();
    }

    @Override
    public Cliente[] proximosClientes(int quantidade) {
        return estrategiaAtual.proximosClientes(quantidade);
    }


    @Override
    public void associaBuffer(ArquivoSequencial<Cliente> arquivoSequencial) {
        estrategiaAtual.associaBuffer(arquivoSequencial);
    }

    public void fechaBuffer() {
        estrategiaAtual.fechaBuffer();
    }
}

