package gerenciador.model.factories;

public class AbstractFactory implements IFactory <T>{

    protected String pacoteBase = "gerenciador.model.";
    
    @Override
    public T criar(String opcao) {
        try {
            // Mapeamento da opção para o nome da classe correspondente
            String pacoteBase = this.pacoteBase + opcao;

            // Usa reflexão para instanciar a classe correspondente
            Class<?> classeOperacao = Class.forName(pacoteBase);
            T Pedreiro = (T) classeOperacao.getDeclaredConstructor().newInstance();

        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | NoSuchMethodException |
                 InvocationTargetException e) {
            throw new OperacaoInvalidaException("Erro ao criar instância da operação: " + e.getMessage());
        }
        return Pedreiro;
    }
}
