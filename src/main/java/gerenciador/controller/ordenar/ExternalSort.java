package gerenciador.controller.ordenar;

import gerenciador.model.actions.edicaoDeArquivos.manipularArquivo.ArquivoCliente;
import gerenciador.model.cliente.Cliente;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ExternalSort {

    private static final int TAMANHO_BUFFER = 100000; // Número de clientes que cabem na memória principal

    private final ArquivoCliente arquivoCliente = new ArquivoCliente();
    private final ArquivoCliente arquivoClienteTemp = new ArquivoCliente();

    public String ordenarArquivoClientes(String arquivoEntrada, String arquivoSaida) {
        try {
            // Passo 1: Dividir o arquivo em blocos menores e ordená-los
            List<String> arquivosTemporarios = dividirEOrdenar(arquivoEntrada);
            
            // Passo 2: Mesclar os arquivos temporários até que reste apenas um arquivo
            String name = mesclarArquivos(arquivosTemporarios, arquivoSaida);
            return name;
        } catch (IOException e) {
            e.printStackTrace();
            return "Erro";  // Retorna uma string de erro em caso de exceção
        }
    }

    private List<String> dividirEOrdenar(String arquivoEntrada) throws IOException {
        List<String> arquivosTemporarios = new ArrayList<>();
        arquivoCliente.abrirArquivo(arquivoEntrada, "leitura", Cliente.class);

        List<Cliente> buffer = new ArrayList<>();
        int contadorArquivo = 0;
        List<Cliente> clientesLidos;

        // Loop para ler o arquivo em blocos
        while (true) {
            clientesLidos = arquivoCliente.leiaDoArquivo(TAMANHO_BUFFER);
            if (clientesLidos.isEmpty()) break;

            buffer.addAll(clientesLidos);
            buffer.sort(Comparator.comparing(Cliente::getNome).thenComparing(Cliente::getSobrenome));

            String nomeArquivoTemp = "temp_" + contadorArquivo++ + ".txt";
            salvarBlocoOrdenado(buffer, nomeArquivoTemp);
            arquivosTemporarios.add(nomeArquivoTemp);

            buffer.clear();
        }
        arquivoCliente.fechaArquivo();
        return arquivosTemporarios;
    }

    private void salvarBlocoOrdenado(List<Cliente> buffer, String nomeArquivo) throws IOException {
        arquivoClienteTemp.abrirArquivo(nomeArquivo, "escrita", Cliente.class);
        arquivoClienteTemp.escreveNoArquivo(buffer);
        arquivoClienteTemp.fechaArquivo();
    }

    public String mesclarArquivos(List<String> arquivosTemporarios, String arquivoSaida) throws IOException {
        if (arquivosTemporarios == null || arquivosTemporarios.isEmpty()) {
            throw new IllegalArgumentException("Nenhum arquivo temporário foi fornecido.");
        }
    
        // Continua até que reste apenas um arquivo na lista
        while (arquivosTemporarios.size() > 1) {
            List<String> arquivosMesclados = new ArrayList<>();
    
            for (int i = 0; i < arquivosTemporarios.size(); i += 2) {

                String arquivo1 = arquivosTemporarios.get(i);
                String arquivo2 = (i + 1 < arquivosTemporarios.size()) ? arquivosTemporarios.get(i + 1) : null;
    
                String arquivoMesclado = "temp_merge_" + System.nanoTime() + ".txt";
                mesclarDoisArquivos(arquivo1, arquivo2, arquivoMesclado);
    
                // Adiciona o arquivo mesclado à lista temporária
                arquivosMesclados.add(arquivoMesclado);
    
                // Remove os arquivos mesclados após garantir que eles não são mais necessários
                new File(arquivo1).delete();
                if (arquivo2 != null) {
                    new File(arquivo2).delete();
                }
            }
    
            // Atualiza a lista de arquivos temporários
            arquivosTemporarios = arquivosMesclados;
        }
    
        // Renomear o último arquivo temporário para o arquivo de saída
        File arquivoFinal = new File(arquivosTemporarios.get(0));
        File arquivoDestino = new File(arquivoSaida);

        // Gerar um novo nome de arquivo com número incrementado, se necessário
        while (arquivoDestino.exists()) {
            arquivoDestino = incrementarNomeArquivo(arquivoDestino);
        }

        // Tentar renomear o arquivo final
        if (!arquivoFinal.renameTo(arquivoDestino)) {
            throw new IOException("Não foi possível renomear o arquivo final para: " + arquivoDestino.getAbsolutePath());
        }

        return arquivoDestino.getName();
    }
    

    private void mesclarDoisArquivos(String arquivo1, String arquivo2, String arquivoSaida) throws IOException {
        ArquivoCliente leitor1 = new ArquivoCliente();
        ArquivoCliente leitor2 = new ArquivoCliente();
        ArquivoCliente escritor = new ArquivoCliente();
    
        try {
            List<Cliente> buffer1 = new ArrayList<>();
            boolean arquivo1Concluido = true; // Indica se o arquivo1 foi totalmente lido
            if (arquivo1 != null) {
                leitor1.abrirArquivo(arquivo1, "leitura", Cliente.class);
                buffer1 = leitor1.leiaDoArquivo(TAMANHO_BUFFER / 2);
                arquivo1Concluido = buffer1.isEmpty();
            }
    
            List<Cliente> buffer2 = new ArrayList<>();
            boolean arquivo2Concluido = true; // Indica se o arquivo2 foi totalmente lido
            if (arquivo2 != null) {
                leitor2.abrirArquivo(arquivo2, "leitura", Cliente.class);
                buffer2 = leitor2.leiaDoArquivo(TAMANHO_BUFFER / 2);
                arquivo2Concluido = buffer2.isEmpty();
            }
    
            escritor.abrirArquivo(arquivoSaida, "escrita", Cliente.class);
    
            List<Cliente> listaOrdenada = new ArrayList<>();
    
            while (!buffer1.isEmpty() || !buffer2.isEmpty()) {
                if (!buffer1.isEmpty() || !buffer2.isEmpty()) {
                    Cliente menorCliente = retornaOMenor(buffer1, buffer2);
                    listaOrdenada.add(menorCliente);
                }
            
                // Escreve no arquivo quando o buffer atinge o limite ou ao final do processo
                if (listaOrdenada.size() == TAMANHO_BUFFER / 2 || (buffer1.isEmpty() && buffer2.isEmpty())) {
                    escritor.escreveNoArquivo(listaOrdenada);
                    listaOrdenada.clear();
                }
            
                // Reabastece buffer1, se necessário e possível
                if (buffer1.isEmpty() && !arquivo1Concluido) {
                    buffer1 = leitor1.leiaDoArquivo(TAMANHO_BUFFER / 2);
                    arquivo1Concluido = buffer1.isEmpty(); // Atualiza o estado do arquivo1
                }
            
                // Reabastece buffer2, se necessário e possível
                if (buffer2.isEmpty() && !arquivo2Concluido) {
                    buffer2 = leitor2.leiaDoArquivo(TAMANHO_BUFFER / 2);
                    arquivo2Concluido = buffer2.isEmpty(); // Atualiza o estado do arquivo2
                }
            }
            
        } finally {
            leitor1.fechaArquivo();
            if (arquivo2 != null) leitor2.fechaArquivo();
            escritor.fechaArquivo();
    
            // Deleta arquivos temporários
            if (arquivo1 != null) new File(arquivo1).delete();
            if (arquivo2 != null) new File(arquivo2).delete();
        }
    }

    private int compararClientes(Cliente cliente1, Cliente cliente2) {
        int comparacaoNome = cliente1.getNome().compareTo(cliente2.getNome());
        if (comparacaoNome != 0) {
            return comparacaoNome;
        }
        return cliente1.getSobrenome().compareTo(cliente2.getSobrenome());
    }

    private Cliente retornaOMenor(List<Cliente> buffer1, List<Cliente> buffer2) {
        if (!buffer1.isEmpty() && (buffer2.isEmpty() || compararClientes(buffer1.get(0), buffer2.get(0)) <= 0)) {
            return buffer1.remove(0);
        } else if (!buffer2.isEmpty()) {
            return buffer2.remove(0);
        } else {
            throw new IllegalStateException("Ambos os buffers estão vazios. Isso não deveria acontecer.");
        }
    }

    private File incrementarNomeArquivo(File arquivo) {
        String nome = arquivo.getName();
        String diretorio = arquivo.getParent();
    
        // Verificar se o nome já contém números antes da extensão
        int indicePonto = nome.lastIndexOf(".");
        String base = (indicePonto == -1) ? nome : nome.substring(0, indicePonto);
        String extensao = (indicePonto == -1) ? "" : nome.substring(indicePonto);
    
        // Encontrar número no final do nome
        int numero = 1; // Começar com 1, caso não exista número
        int indiceNumero = base.lastIndexOf("_");
        if (indiceNumero != -1) {
            String possivelNumero = base.substring(indiceNumero + 1);
            try {
                numero = Integer.parseInt(possivelNumero) + 1; // Incrementar número
                base = base.substring(0, indiceNumero); // Remover número antigo
            } catch (NumberFormatException e) {
                // Não havia um número válido no final
            }
        }
    
        // Construir novo nome com número incrementado
        String novoNome = base + "_" + numero + extensao;
        return new File(diretorio, novoNome);
    }
    

}
