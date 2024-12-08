package gerenciador.view;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import gerenciador.controller.ordenar.ExternalSort;
import gerenciador.model.actions.edicaoDeArquivos.buffer.BufferDeClientes;
import gerenciador.model.actions.edicaoDeArquivos.buffer.buffer_user_cases.LeituraStrategy;
import gerenciador.model.cliente.Cliente;

import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

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
            bufferDeClientes.inicializa(arquivoSelecionado); // Passa o nome do arquivo aqui
            registrosCarregados = 0; // Reseta o contador
            tableModel.setRowCount(0); // Limpa a tabela
            carregarMaisClientes(); // Carrega os primeiros clientes
            arquivoCarregado = true; // Marca que o arquivo foi carregado
        }
    }

    private void criarInterface() throws IOException {
        // Criação do mapa de botões
        Map<String, JButton> botoes = new LinkedHashMap<>();
        insereBotoes(botoes);

        // Configuração da tabela e do painel de rolagem
        String[] clienteTableModel = {"#", "Nome", "Sobrenome", "Endereço", "Telefone", "Credit Score"};
        tableModel = new DefaultTableModel(clienteTableModel, 0);
        table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);

        // Listener para carregamento incremental
        ajustarScrollPane(scrollPane);

        // Uso do InterfaceBuilder para construir o painel principal
        JPanel panel = BottonBuilder.construirInterface(botoes, scrollPane);

        // Adiciona o painel principal à janela
        add(panel);
    }

    private void insereBotoes(Map<String, JButton> botoes){
        botoes.put("Carregar Clientes", criarBotao("Carregar Clientes", () -> {
            try {
                carregarArquivo();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }));
        botoes.put("Ordenar por Nome", criarBotao("Ordenar por Nome", this::ordenarClientes));
        botoes.put("Pesquisar Cliente", criarBotao("Pesquisar Cliente", this::pesquisarCliente));
        botoes.put("Adicionar Cliente", criarBotao("Adicionar Cliente", this::adicionarCliente));
        botoes.put("Remover Cliente", criarBotao("Remover Cliente", this::removerCliente));
    }

    protected JButton criarBotao(String texto, Runnable acao) {
        JButton botao = new JButton(texto);
        botao.addActionListener(e -> acao.run());
        return botao;
    }

    private void ajustarScrollPane(JScrollPane scrollPane){
        scrollPane.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener() {
            @Override
            public void adjustmentValueChanged(AdjustmentEvent e) {
                if (!scrollPane.getVerticalScrollBar().getValueIsAdjusting()) {
                    if (arquivoCarregado &&
                        scrollPane.getVerticalScrollBar().getValue() +
                        scrollPane.getVerticalScrollBar().getVisibleAmount() >=
                        scrollPane.getVerticalScrollBar().getMaximum()) {
                        carregarMaisClientes();
                    }
                }
            }
        });
    }

    private void carregarMaisClientes() {
        new ClienteLoaderTemplate() {
            @Override
            protected void processarCliente(Cliente cliente, DefaultTableModel tableModel) {
                tableModel.addRow(new Object[]{tableModel.getRowCount() + 1, cliente.getNome(), cliente.getSobrenome(), cliente.getEndereco(), cliente.getTelefone(), cliente.getCreditScore()});
            }
        }.carregarClientes(bufferDeClientes, tableModel, TAMANHO_BUFFER);
    }    
    
    private void ordenarClientes() {
        if (!validarArquivoCarregado()) return;
    
        try {
            String arquivoOrdenado = ordenarArquivoClientes();
            atualizarDadosTabela(arquivoOrdenado);
            exibirMensagem("Clientes ordenados com sucesso!");
        } catch (Exception e) {
            exibirMensagemErro("Erro ao ordenar os clientes: " + e.getMessage());
        }
    }
    
    private boolean validarArquivoCarregado() {
        if (!arquivoCarregado) {
            exibirMensagemErro("Nenhum arquivo carregado para ordenar.");
            return false;
        }
        return true;
    }
    
    private String ordenarArquivoClientes() throws Exception {
        ExternalSort merger = new ExternalSort();
        return merger.ordenarArquivoClientes(arquivoSelecionado, "clientes_ordenados.txt");
    }
    
    private void atualizarDadosTabela(String arquivoOrdenado) throws IOException {
        bufferDeClientes.setEstrategia(new LeituraStrategy());
        bufferDeClientes.inicializa(arquivoOrdenado);
    
        tableModel.setRowCount(0); // Limpa a tabela
        registrosCarregados = 0;  // Reseta o contador
        carregarMaisClientes();   // Recarrega os clientes
    }
    
    private void exibirMensagem(String mensagem) {
        JOptionPane.showMessageDialog(this, mensagem);
    }
    
    private void exibirMensagemErro(String mensagem) {
        JOptionPane.showMessageDialog(this, mensagem, "Erro", JOptionPane.ERROR_MESSAGE);
    }
    

    private void pesquisarCliente() {
        if (!validarArquivoCarregado()) return;
    
        String nome = obterEntrada("Digite o nome do cliente:");
        if (nome == null) return;
    
        String sobrenome = obterEntradaOpcional("Digite o sobrenome do cliente (opcional):");
    
        try {
            int linha = buscarCliente(nome, sobrenome);
            if (linha >= 0) {
                destacarClienteNaTabela(linha);
            } else {
                exibirMensagem("Nenhum cliente encontrado com os critérios especificados.");
            }
        } catch (Exception e) {
            exibirMensagemErro("Erro ao pesquisar o cliente: " + e.getMessage());
        }
    }
    
    private String obterEntrada(String mensagem) {
        String entrada = JOptionPane.showInputDialog(this, mensagem);
        if (entrada == null || entrada.trim().isEmpty()) {
            exibirMensagemErro("A entrada não pode ser vazia.");
            return null;
        }
        return entrada.trim();
    }
    
    private String obterEntradaOpcional(String mensagem) {
        String entrada = JOptionPane.showInputDialog(this, mensagem);
        return (entrada != null) ? entrada.trim() : null;
    }
    
    private int buscarCliente(String nome, String sobrenome) {
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String nomeTabela = (String) tableModel.getValueAt(i, 1);
            String sobrenomeTabela = (String) tableModel.getValueAt(i, 2);
    
            if (nomeTabela.equalsIgnoreCase(nome) && 
                (sobrenome == null || sobrenome.isEmpty() || sobrenomeTabela.equalsIgnoreCase(sobrenome))) {
                return i; // Retorna o índice da linha encontrada
            }
        }
        return -1; // Retorna -1 se o cliente não for encontrado
    }
    
    private void destacarClienteNaTabela(int linha) {
        table.setRowSelectionInterval(linha, linha);
        table.scrollRectToVisible(table.getCellRect(linha, 0, true));
    }
    
    private void adicionarCliente() {
        if (!validarArquivoCarregado()) return;
    
        try {
            // Obter informações do cliente
            String nome = obterEntradaObrigatoria("Digite o nome do cliente:");
            String sobrenome = obterEntradaObrigatoria("Digite o sobrenome do cliente:");
            String endereco = obterEntradaObrigatoria("Digite o endereço do cliente:");
            String telefone = obterEntradaObrigatoria("Digite o telefone do cliente:");
            int creditScore = obterCreditScore();
    
            // Criar cliente e persistir no arquivo
            Cliente novoCliente = new Cliente(nome, sobrenome, endereco, telefone, creditScore);
            adicionarClienteAoArquivo(novoCliente);
    
            // Atualizar a tabela
            atualizarTabelaComCliente(novoCliente);
            exibirMensagem("Cliente adicionado com sucesso!");
    
        } catch (Exception e) {
            exibirMensagemErro("Erro ao adicionar o cliente: " + e.getMessage());
        }
    }
    
    private String obterEntradaObrigatoria(String mensagem) throws Exception {
        String entrada = JOptionPane.showInputDialog(this, mensagem);
        if (entrada == null || entrada.trim().isEmpty()) {
            throw new Exception("A entrada para \"" + mensagem + "\" não pode ser vazia.");
        }
        return entrada.trim();
    }
    
    private int obterCreditScore() throws Exception {
        String creditScoreStr = JOptionPane.showInputDialog(this, "Digite o credit score (0 a 100):");
        try {
            int creditScore = Integer.parseInt(creditScoreStr);
            if (creditScore < 0 || creditScore > 100) {
                throw new Exception("O credit score deve estar entre 0 e 100.");
            }
            return creditScore;
        } catch (NumberFormatException e) {
            throw new Exception("O credit score deve ser um número válido.");
        }
    }
    
    private void adicionarClienteAoArquivo(Cliente cliente) throws IOException {
        bufferDeClientes.getArquivoCliente().abrirArquivo(arquivoSelecionado, "append", Cliente.class);
        bufferDeClientes.getArquivoCliente().escreveNoArquivo(Collections.singletonList(cliente));
        bufferDeClientes.getArquivoCliente().fechaArquivo();
    }
    
    private void atualizarTabelaComCliente(Cliente cliente) {
        tableModel.addRow(new Object[]{
            tableModel.getRowCount() + 1,
            cliente.getNome(),
            cliente.getSobrenome(),
            cliente.getEndereco(),
            cliente.getTelefone(),
            cliente.getCreditScore()
        });
    }
    
    private void removerCliente() {
        int selectedRow = table.getSelectedRow();
        verifySelectedRow(selectedRow);
        // Obtém o cliente selecionado na tabela
        Cliente clienteExcluir = clienteSelecionadoNaTabela(selectedRow);
        // Remove o cliente da tabela
        tableModel.removeRow(selectedRow);
        // Chama o método para excluir o cliente do arquivo
        excluiDoArquivo(clienteExcluir);
    }
    
    private void verifySelectedRow(int selectedRow){
        if (selectedRow == -1) {
            exibirMensagem("Nenhum cliente selecionado.");
            return;
        }
    }

    private Cliente clienteSelecionadoNaTabela(int selectedRow){
        String nome = tableModel.getValueAt(selectedRow, 1).toString();
        String sobrenome = tableModel.getValueAt(selectedRow, 2).toString();
        String endereco = tableModel.getValueAt(selectedRow, 3).toString();
        String telefone = tableModel.getValueAt(selectedRow, 4).toString();
        int creditScore = Integer.parseInt(tableModel.getValueAt(selectedRow, 5).toString());
    
        return new Cliente(nome, sobrenome, endereco, telefone, creditScore);
    }

    private void excluiDoArquivo(Cliente clienteExcluir){
        try {
            bufferDeClientes.getArquivoCliente().excluirCliente(arquivoSelecionado, TAMANHO_BUFFER, clienteExcluir);
            exibirMensagem("Cliente removido com sucesso.");
        } catch (Exception e) {
            exibirMensagemErro("Erro ao remover o cliente: " + e.getMessage());
        }
    }
}
