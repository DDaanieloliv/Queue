package com.ddaaniel.queue.domain.model.enuns;


public enum Prioridade {
    GRAVIDA(1), // Emergência
    IDOSO(2),  // Urgência
    PESSOA_DEFICIENTE(3),    // Pouca urgência
    NENHUM(4);     // Não urgência

    private final int prioridade;

    Prioridade(int prioridade) {
        this.prioridade = prioridade;
    }

    //  O construtor permite que você inicialize atributos associados a cada constante do enum no
    //  momento em que o enum é declarado. No caso de CategoriaTriagem, o atributo prioridade é
    //  inicializado com um valor específico para cada constante. Sem o construtor, não haveria
    //  como passar esses valores ao criar as constantes.

    public int getPrioridade() {
        return prioridade;
    }
}

/*
@Data
@Entity
@Table(name = "categoria_triagem")
public class CategoriaTriagem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String cor;

    @Column(nullable = false)
    private Integer prioridade;

    @Column(nullable = false)
    private Integer tempoMaximoEspera; // em minutos

    @Column(length = 255)
    private String descricao;
}
*/