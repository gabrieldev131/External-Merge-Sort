package gerenciador.view;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Map;

class BottonBuilder {
    public static JPanel construirPainelComBotoes(Map<String, JButton> botoes) {
        JPanel panel = new JPanel(new FlowLayout());
        botoes.values().forEach(panel::add); // Adiciona todos os botões ao painel
        return panel;
    }

    public static JScrollPane construirTabela(DefaultTableModel model, JTable tabela) {
        tabela.setModel(model);
        return new JScrollPane(tabela);
    }

    public static JPanel construirInterface(Map<String, JButton> botoes, JScrollPane scrollPane) {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel botoesPanel = construirPainelComBotoes(botoes); // Constrói o painel de botões
        panel.add(botoesPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }
}

