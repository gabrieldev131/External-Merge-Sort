package gerenciador.model.factories;

public interface IFactory<T>{

    T criar(String opcao);

}
