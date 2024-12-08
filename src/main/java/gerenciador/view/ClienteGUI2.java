package gerenciador.view;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import gerenciador.model.actions.edicaoDeArquivos.buffer.BufferDeClientes;
import gerenciador.model.actions.edicaoDeArquivos.buffer.buffer_user_cases.LeituraStrategy;
import gerenciador.model.actions.ordenar.ExternalSort;
import gerenciador.model.cliente.Cliente;

import java.awt.*;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.io.IOException;
import java.util.Collections;

public class ClienteGUI2 extends JFrame {
    private JTable table;
    private DefaultTableModel tableModel;
    private BufferDeClientes bufferDeClientes;
    private final int TAMANHO_BUFFER = 10000;
    private int registrosCarregados = 0; // Contador de registros já carregados
    private String arquivoSelecionado;
    private boolean arquivoCarregado = false; // Para verificar se o arquivo foi carregado

    public ClienteGUI2() throws IOException{
        setTitle("Gerenciamento de Clientes");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        bufferDeClientes = new BufferDeClientes();
        criarInterface();
    }


    private void carregarArquivo() throws IOException{
        JFileChooser fileChooser = new JFileChooser();
        int retorno = fileChooser.showOpenDialog(this);
        if (retorno == JFileChooser.APPROVE_OPTION) {
            arquivoSelecionado = fileChooser.getSelectedFile().getAbsolutePath();
            bufferDeClientes.setEstrategia(new LeituraStrategy());
            bufferDeClientes.inicializa(arquivoSelecionado); // Substitua por sua implementação
             // Passa o nome do arquivo aqui
            registrosCarregados = 0; // Reseta o contador
            tableModel.setRowCount(0); // Limpa a tabela
            carregarMaisClientes(); // Carrega os primeiros clientes
            arquivoCarregado = true; // Marca que o arquivo foi carregado
        }
    }
    private void criarInterface() throws IOException{
        JPanel panel = new JPanel(new BorderLayout());
        JButton btnCarregar = new JButton("Carregar Clientes");
        JButton btnOrdenar = new JButton("Ordenar por Nome");
        JButton btnPesquisar = new JButton("Pesquisar Cliente");
        JButton btnAdicionar = new JButton("Adicionar Cliente");
        JButton btnRemover = new JButton("Remover Cliente");

        // Layout para os botões
        JPanel botoesPanel = new JPanel(new FlowLayout());
        botoesPanel.add(btnCarregar);
        botoesPanel.add(btnOrdenar);
        botoesPanel.add(btnPesquisar);
        botoesPanel.add(btnAdicionar);
        botoesPanel.add(btnRemover);

        tableModel = new DefaultTableModel(new String[]{"#", "Nome", "Sobrenome", "Endereço", "Telefone", "Credit Score"}, 0);
        table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);

         // Adiciona um listener ao JScrollPane para carregar mais clientes ao rolar
         scrollPane.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener() {
            @Override
            public void adjustmentValueChanged(AdjustmentEvent e) {
                if (!scrollPane.getVerticalScrollBar().getValueIsAdjusting()) {
                    // Verifica se estamos no final da tabela e se o arquivo foi carregado
                    if (arquivoCarregado && 
                        scrollPane.getVerticalScrollBar().getValue() + 
                        scrollPane.getVerticalScrollBar().getVisibleAmount() >= 
                        scrollPane.getVerticalScrollBar().getMaximum()) {
                        carregarMaisClientes();
                    }
                }
            }
        });

        btnCarregar.addActionListener(e -> {
            try {
                carregarArquivo();
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        });

        btnOrdenar.addActionListener(e -> ordenarClientes());

        btnPesquisar.addActionListener(e -> pesquisarCliente());

        btnAdicionar.addActionListener(e -> adicionarCliente());

        btnRemover.addActionListener(e -> removerCliente());

        panel.add(botoesPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        add(panel);
    }

    private void carregarMaisClientes() {
        // Carrega apenas 10.000 registros de cada vez
        Cliente[] clientes = bufferDeClientes.proximosClientes(TAMANHO_BUFFER); // Chama o método com o tamanho do buffer
        if (clientes != null && clientes.length > 0) {
            for (Cliente cliente : clientes) {
                if (cliente != null) { // Verifica se o cliente não é nulo
                    tableModel.addRow(new Object[]{tableModel.getRowCount() + 1, cliente.getNome(), cliente.getSobrenome(), cliente.getEndereco(), cliente.getTelefone(), cliente.getCreditScore()});
                }
            }
            registrosCarregados += clientes.length; // Atualiza o contador
        }
    }
    
    private void ordenarClientes() {
        if (!arquivoCarregado) {
            JOptionPane.showMessageDialog(this, "Nenhum arquivo carregado para ordenar.");
            return;
        }

        String arquivoOrdenado = "clientes_ordenados.txt"; // Nome do arquivo de saída ordenado
        try {
            // Chama o método para ordenar o arquivo
            ExternalSort merger = new ExternalSort();
            arquivoOrdenado = merger.ordenarArquivoClientes(arquivoSelecionado, arquivoOrdenado);

            // Atualiza o buffer e recarrega os dados na tabela
            bufferDeClientes.setEstrategia(new LeituraStrategy());
            bufferDeClientes.inicializa(arquivoOrdenado);
            tableModel.setRowCount(0); // Limpa a tabela
            registrosCarregados = 0; // Reseta o contador
            carregarMaisClientes(); // Recarrega os clientes
            JOptionPane.showMessageDialog(this, "Clientes ordenados com sucesso!");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao ordenar os clientes: " + e.getMessage());
        }
    }
    private void pesquisarCliente() {
        if (!arquivoCarregado) {
            JOptionPane.showMessageDialog(this, "Nenhum arquivo carregado para realizar a pesquisa.");
            return;
        }
    
        String nome = JOptionPane.showInputDialog(this, "Digite o nome do cliente:");
        if (nome == null || nome.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "O nome não pode ser vazio.");
            return;
        }
    
        String sobrenome = JOptionPane.showInputDialog(this, "Digite o sobrenome do cliente (opcional):");
        if (sobrenome != null) sobrenome = sobrenome.trim();
    
        try {
            // Percorre as linhas da tabela para encontrar o cliente correspondente
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                String nomeTabela = (String) tableModel.getValueAt(i, 1); // Nome na coluna 1
                String sobrenomeTabela = (String) tableModel.getValueAt(i, 2); // Sobrenome na coluna 2
    
                if (nomeTabela.equalsIgnoreCase(nome) && 
                    (sobrenome == null || sobrenome.isEmpty() || sobrenomeTabela.equalsIgnoreCase(sobrenome))) {
                    
                    // Seleciona e destaca a linha correspondente
                    table.setRowSelectionInterval(i, i);
                    table.scrollRectToVisible(table.getCellRect(i, 0, true));
                    return; // Sai do método após encontrar o cliente
                }
            }
    
            // Caso o cliente não seja encontrado
            JOptionPane.showMessageDialog(this, "Nenhum cliente encontrado com os critérios especificados.");
    
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao pesquisar o cliente: " + e.getMessage());
        }
    }
    
    

    private void adicionarCliente() {
        if (!arquivoCarregado) {
            JOptionPane.showMessageDialog(this, "Nenhum arquivo carregado para adicionar clientes.");
            return;
        }
    
        // Obtém as informações do cliente por meio de caixas de diálogo
        String nome = JOptionPane.showInputDialog(this, "Digite o nome do cliente:");
        if (nome == null || nome.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "O nome não pode ser vazio.");
            return;
        }
    
        String sobrenome = JOptionPane.showInputDialog(this, "Digite o sobrenome do cliente:");
        if (sobrenome == null || sobrenome.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "O sobrenome não pode ser vazio.");
            return;
        }
    
        String endereco = JOptionPane.showInputDialog(this, "Digite o endereço do cliente:");
        if (endereco == null || endereco.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "O endereço não pode ser vazio.");
            return;
        }
    
        String telefone = JOptionPane.showInputDialog(this, "Digite o telefone do cliente:");
        if (telefone == null || telefone.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "O telefone não pode ser vazio.");
            return;
        }

        String creditScoreStr = JOptionPane.showInputDialog(this, "Digite o credit score (0 a 100):");
        int creditScore;
        try {
            creditScore = Integer.parseInt(creditScoreStr);
            if (creditScore < 0 || creditScore > 100) {
                JOptionPane.showMessageDialog(this, "O credit score deve estar entre 0 e 100.");
                return;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "O credit score deve ser um número válido.");
            return;
        }
    
        // Cria um novo cliente
        Cliente novoCliente = new Cliente(nome, sobrenome, endereco, telefone, creditScore);
    
        try {
            // Abre o arquivo em modo escrita
            bufferDeClientes.getArquivoCliente().abrirArquivo(arquivoSelecionado, "append", Cliente.class);
    
            // Adiciona o cliente ao arquivo
            bufferDeClientes.getArquivoCliente().escreveNoArquivo(Collections.singletonList(novoCliente));
    
            // Fecha o arquivo
            bufferDeClientes.getArquivoCliente().fechaArquivo();
    
            // Atualiza a tabela
            tableModel.addRow(new Object[]{tableModel.getRowCount() + 1, nome, sobrenome, endereco, telefone, creditScore});
            JOptionPane.showMessageDialog(this, "Cliente adicionado com sucesso!");


        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Erro ao adicionar o cliente: " + e.getMessage());
        }
    }
    

    


    private void removerCliente() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Nenhum cliente selecionado.");
            return;
        }
    
        // Obtém o cliente selecionado na tabela
        String nome = tableModel.getValueAt(selectedRow, 1).toString();
        String sobrenome = tableModel.getValueAt(selectedRow, 2).toString();
        String endereco = tableModel.getValueAt(selectedRow, 3).toString();
        String telefone = tableModel.getValueAt(selectedRow, 4).toString();
        int creditScore = Integer.parseInt(tableModel.getValueAt(selectedRow, 5).toString());
    
        Cliente clienteExcluir = new Cliente(nome, sobrenome, endereco, telefone, creditScore);
    
        // Remove o cliente da tabela
        tableModel.removeRow(selectedRow);
    
        // Chama o método para excluir o cliente do arquivo
        try {
            bufferDeClientes.getArquivoCliente().excluirCliente(arquivoSelecionado, TAMANHO_BUFFER, clienteExcluir);
            JOptionPane.showMessageDialog(this, "Cliente removido com sucesso.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao remover o cliente: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
}
