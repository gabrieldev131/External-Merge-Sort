package gerenciador.model.actions.edicaoDeArquivos.manipularArquivo;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import gerenciador.model.cliente.Cliente;

    public class ArquivoCliente implements ArquivoSequencial<Cliente> {

        private BufferedReader inputStream;
        private BufferedWriter outputStream;
        private File file;
        private int MAX_TAM_BUFFER = 10000;
        @Override
    public void abrirArquivo(String nomeDoArquivo, String modoDeLeitura, Class<Cliente> classeBase) {
        try {
            this.file = new File(nomeDoArquivo);

            switch (modoDeLeitura.toLowerCase()) {
                case "leitura":
                    if (!file.exists() || file == null) {
                        throw new FileNotFoundException("Arquivo " + nomeDoArquivo + " não encontrado.");
                    }
                    this.inputStream = new BufferedReader(new FileReader(file));
                    break;

                case "escrita":
                    this.outputStream = new BufferedWriter(new FileWriter(file));
                    break;
                
                case "append":
                    this.outputStream = new BufferedWriter(new FileWriter(file, true)); // O `true` ativa o modo append
                    break;
                default:
                    throw new IllegalArgumentException("Modo de leitura inválido. Use 'leitura', 'escrita' ou 'leitura/escrita'.");
            }

        } catch (FileNotFoundException e) {
            System.err.println("Erro: O arquivo especificado não foi encontrado.");
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            System.err.println("Erro: O modo de leitura fornecido é inválido. Use 'leitura', 'escrita' ou 'leitura/escrita'.");
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("Erro: Problema ao abrir o arquivo " + nomeDoArquivo);
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Erro inesperado: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public List<Cliente> leiaDoArquivo(int numeroDeRegistros) throws IOException {
        List<Cliente> registros = new ArrayList<>();
        String linha;
        int registrosLidos = 0;
        String[] dados;
        // Ler linhas do arquivo até atingir o número de registros solicitado ou chegar ao fim do arquivo
        while (registrosLidos < numeroDeRegistros && (linha = inputStream.readLine()) != null) {
            dados = linha.split(",");
            if (dados.length == 7) { // Certificar-se de que os dados têm o formato esperado
                Cliente cliente = ajustaCliente(dados);
                registros.add(cliente);
                registrosLidos++;
            }
        }
        return registros; // Retorna os registros lidos; vazio se não houver mais dados
    }
    
    private Cliente ajustaCliente(String[] dados){
        String nome = dados[0];
        String sobrenome = dados[1];
        String endereco = dados[2] + "," + dados[3] + "," + dados[4];
        String telefone = dados[5];
        int creditScore = Integer.parseInt(dados[6]);

        return new Cliente(nome, sobrenome, endereco, telefone, creditScore);
    }

    @Override
    public void escreveNoArquivo(List<Cliente> dados) throws IOException {

        if (outputStream == null) {
            throw new IOException("O fluxo de saída não foi inicializado.");
        }
        for (Cliente cliente : dados) {
            escreveCliente(cliente);
        }
    }

    private void escreveCliente(Cliente cliente) throws IOException{
        outputStream.write( cliente.getNome() + "," +
                            cliente.getSobrenome() + "," +
                            cliente.getEndereco() + "," +
                            cliente.getTelefone() + "," +
                            cliente.getCreditScore());
        outputStream.newLine();
    }
    @Override
    public void fechaArquivo() throws IOException {
        if (inputStream != null) {
            inputStream.close();
        }
        if (outputStream != null) {
            outputStream.close();
        }
    }

    public List<String> dividirArquivo(String nomeArquivo, int tamanhoBuffer) throws IOException {
        List<String> arquivosTemporarios = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(nomeArquivo))) {
            int contadorArquivo = 0;
            String linha;
            List<String> buffer = new ArrayList<>();
    
            while ((linha = reader.readLine()) != null) {
                buffer.add(linha);
                if (buffer.size() == tamanhoBuffer) {
                    String arquivoTemp = "temp_" + contadorArquivo++ + ".txt";
                    salvarBloco(buffer, arquivoTemp);
                    arquivosTemporarios.add(arquivoTemp);
                    buffer.clear();
                }
            }
    
            // Salvar qualquer sobra no buffer
            if (!buffer.isEmpty()) {
                String arquivoTemp = "temp_" + contadorArquivo++ + ".txt";
                salvarBloco(buffer, arquivoTemp);
                arquivosTemporarios.add(arquivoTemp);
            }
        }
        return arquivosTemporarios;
    }
    
    private void salvarBloco(List<String> bloco, String nomeArquivo) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(nomeArquivo))) {
            for (String registro : bloco) {
                writer.write(registro);
                writer.newLine();
            }
        }
    }

    public void excluirCliente(String nomeArquivo, int tamanhoBuffer, Cliente clienteParaExcluir) throws IOException {
        // Dividir o arquivo em partes
        List<String> arquivosTemporarios = dividirArquivo(nomeArquivo, tamanhoBuffer);
    
        // Remover o cliente
        excluirClienteDoHashMap(arquivosTemporarios, clienteParaExcluir);
    
        // Fundir os arquivos temporários em um arquivo final
        fundirArquivos(arquivosTemporarios, nomeArquivo);
    }
    
    private void excluirClienteDoHashMap(List<String> arquivosTemporarios, Cliente clienteParaExcluir) throws IOException {
        Map<String, String> registros = new HashMap<>();
        // Carregar registros no HashMap
        carregarRegistrosDoHashMap(registros, arquivosTemporarios);

        // Localizar e remover o cliente do HashMap
        localizarERemoverClienteDoHashMap(registros, clienteParaExcluir);

        // Salvar novamente os registros em arquivos temporários
        salvarRegistrosTemporarios(registros);
    }

    private void carregarRegistrosDoHashMap(Map<String, String> registros, List<String> arquivosTemporarios){
        for (String arquivo : arquivosTemporarios) {
            try (BufferedReader reader = new BufferedReader(new FileReader(arquivo))) {
                String linha;
                while ((linha = reader.readLine()) != null) {
                    // Assumindo que o primeiro campo é a chave (ex: CPF ou ID)
                    String[] campos = linha.split(",");
                    String chave = campos[0]; // Ajuste conforme a estrutura do arquivo
                    registros.put(chave, linha);
                }
            }
            catch(IOException e){
                e.printStackTrace();
            }
        }
    }

    private void localizarERemoverClienteDoHashMap(Map<String, String> registros, Cliente clienteParaExcluir){
        for (Map.Entry<String, String> entry : registros.entrySet()) {
            String[] campos = entry.getValue().split(",");
            Cliente clienteAtual = ajustaCliente(campos);
            // Verificar se o cliente atual corresponde ao cliente a ser excluído
            if (clienteAtual.toString().equals(clienteParaExcluir.toString())) {
                registros.remove(clienteAtual.getNome());
                break;
            }
        }
    }

    private void salvarRegistrosTemporarios(Map<String, String> registros) throws IOException{
        int contadorArquivo = 0;
        List<String> buffer = new ArrayList<>();
        for (String registro : registros.values()) {
            buffer.add(registro);
            if (buffer.size() == MAX_TAM_BUFFER || registros.isEmpty()) { // Salvar a cada 1000 registros ou tamanho desejado
                String arquivoTemp = "temp_" + contadorArquivo++ + ".txt";
                salvarBloco(buffer, arquivoTemp);
                buffer.clear();
            }
        }
        if (!buffer.isEmpty()) {
            String arquivoTemp = "temp_" + contadorArquivo++ + ".txt";
            salvarBloco(buffer, arquivoTemp);
        }
    }

    public void fundirArquivos(List<String> arquivosTemporarios, String arquivoFinal) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(arquivoFinal))) {
            for (String arquivoTemp : arquivosTemporarios) {
                try (BufferedReader reader = new BufferedReader(new FileReader(arquivoTemp))) {
                    String linha;
                    while ((linha = reader.readLine()) != null) {
                        writer.write(linha);
                        writer.newLine();
                    }
                }
                catch(IOException e){
                    e.printStackTrace();
                }
                // Excluir o arquivo temporário após fundir
                new File(arquivoTemp).delete();
            }
        }
    }
    
}
