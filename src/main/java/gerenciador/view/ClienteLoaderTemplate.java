package gerenciador.view;

import gerenciador.model.actions.edicaoDeArquivos.buffer.BufferDeClientes;
import gerenciador.model.cliente.Cliente;
import javax.swing.table.DefaultTableModel;

public abstract class ClienteLoaderTemplate {
    public final void carregarClientes(BufferDeClientes buffer, DefaultTableModel tableModel, int bufferSize) {
        Cliente[] clientes = buffer.proximosClientes(bufferSize);
        for (Cliente cliente : clientes) {
            processarCliente(cliente, tableModel); // Nenhum cliente será nulo
        }
    }
    

    protected abstract void processarCliente(Cliente cliente, DefaultTableModel tableModel);
}

