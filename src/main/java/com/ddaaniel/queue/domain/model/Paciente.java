package com.ddaaniel.queue.domain.model;


import com.ddaaniel.queue.domain.model.enuns.Prioridade;
import com.ddaaniel.queue.domain.model.enuns.Role;
import com.ddaaniel.queue.domain.model.enuns.Sexo;
import com.ddaaniel.queue.exception.validation.NotEmpty;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.br.CPF;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@NoArgsConstructor
@Data
@Entity //Define que a classe será mapeada no BD como uma entidade.
public class Paciente{

    @Id //Define que o atributo anotado como chave primaria.
    @GeneratedValue(strategy = GenerationType.IDENTITY) //Estrategia de geração de chave.
    private Long id_paciente;

    @Column(name = "nome_completo")
    @NotEmpty(message = "{campo.nomeCompleto.obrigatorio}")
    private String nomeCompleto;

    @NotEmpty(message = "{campo.dataNascimento.obrigatorio}")
    private String dataNascimento;

    @Column(nullable = false, length = 1)
    @Enumerated(EnumType.STRING)
    private Sexo sexo;

    @Column(unique = true , nullable = false)
    @NotEmpty(message = "{campo.cpf.obrigatorio}")
    //@CPF(message = "{campo.cpf.invalido}")
    private String cpf;

    @Column(length = 255)
    @NotEmpty(message = "{campo.email.obrigatorio}")
    private String email;

    @Column(length = 20)
    @NotEmpty(message = "{campo.telefone.obrigatorio}")
    private String telefone;

    //@OneToMany(mappedBy = "paciente", cascade = CascadeType.ALL)
    //private Consulta consulta_desejada;

    @OneToOne
    @JoinColumn(name = "conta_id", unique = true) // Relacionamento com Conta
    private Conta conta;

    private Boolean presencaConfirmado = false;


    private LocalDateTime dataHoraChegada;

    private String prontuario;

    @Column(unique = true, length = 8) // Definindo como único e com tamanho de 8 caracteres
    private String codigoCodigo;


    @Enumerated(EnumType.STRING)
    //@NotEmpty(message = "{campo.categoriaTriagem.obrigatorio}")
    private Prioridade prioridade = Prioridade.NENHUM;

    @Enumerated(EnumType.STRING)
    private Role role = Role.PACIENTE;

    @OneToMany(mappedBy = "paciente", fetch = FetchType.EAGER)
    private List<Agendamento> agendamentos = new ArrayList<>();




    // Gera o código aleatório de 5 caracteres (letras e números)
    public void gerarCodigoCodigo() {
        this.codigoCodigo = gerarCodigo();
    }




    // Função privada para gerar um código de 5 caracteres
    private String gerarCodigo() {
            String caracteres = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
            Random random = new Random();
            StringBuilder codigo = new StringBuilder();
            for (int i = 0; i < 8; i++) { // Alterado para gerar 8 caracteres
                codigo.append(caracteres.charAt(random.nextInt(caracteres.length())));
            }
        return codigo.toString();
    }


    public Paciente(String nomeCompleto, String dataNascimento, Sexo sexo, String cpf, String email, String telefone) {
        this.nomeCompleto = nomeCompleto;
        this.dataNascimento = dataNascimento;
        this.sexo = sexo;
        this.cpf = cpf;
        this.email = email;
        this.telefone = telefone;
    }

}

    //@ManyToOne
    //@JoinColumn(name = "id_endereco")
    //public Endereco endereco;
    //  Indica um relacionamento Many-to-One (Muitos-para-Um) entre Paciente e Endereco.
    //  Isso significa que muitos pacientes podem estar associados a um único endereço.
    //  Na classe Java, esse é um relacionamento de associação bidirecional.
    //
    //  Define a coluna de chave estrangeira id_endereco na tabela Paciente que mapeia
    //  para a tabela Endereco. Isso especifica o nome da coluna que será usada para
    //  associar a entidade Endereco a Paciente.

    //@OneToMany(mappedBy = "paciente", cascade = CascadeType.ALL, orphanRemoval = true)
    //private List<Triagem> triagens;

    //  @OneToMany(mappedBy = "paciente"):
    //      Indica um relacionamento One-to-Many (Um-para-Muitos) entre Paciente e Triagem.
    //      Isso significa que um paciente pode ter muitas triagens. A propriedade mappedBy
    //      é usada para definir o campo proprietário da associação na classe Triagem.

    //  mappedBy = "paciente":
    //      Esta propriedade indica que a entidade Triagem é a "proprietária" do
    //      relacionamento, ou seja, a entidade Triagem é responsável por manter a chave
    //      estrangeira que associa cada triagem a um paciente.
    //
    //  O valor "paciente" em mappedBy refere-se ao nome do campo na classe Triagem que
    //  possui a chave estrangeira e que mapeia o relacionamento para a classe Paciente.
    //  Basicamente, o mappedBy diz ao JPA que o lado "inverso" da relação é o campo paciente
    //  da classe Triagem.

    //  cascade = CascadeType.ALL:
    //      Significa que todas as operações em cascata (persistir,
    //      remover, atualizar, etc.) aplicadas na entidade Paciente serão aplicadas nas
    //      entidades Triagem associadas. Por exemplo, ao remover um paciente, todas as
    //      triagens associadas também serão removidas.


    //  Relacionamento Bidirecional e unidirecional em JPA
    //
    //      Um relacionamento bidirecional em JPA é quando ambas as entidades envolvidas em
    //      um relacionamento têm referências uma para a outra. Em um relacionamento
    //      bidirecional, cada lado da relação conhece o outro lado. Em um relacionamento
    //      unidirecional, apenas uma entidade conhece a outra.

    //  Exemplos de Decisão
    //
    //  Exemplo de Relacionamento Bidirecional Necessário:
    //
    //      Em um sistema de gerenciamento de projetos, uma entidade Projeto tem muitas
    //      Tarefas, e cada Tarefa pertence a um Projeto. Frequentemente, você precisa
    //      navegar de Projeto para Tarefa e vice-versa. Então, um relacionamento
    //      bidirecional @OneToMany e @ManyToOne faz sentido.
    //
    //
    //  Exemplo de Relacionamento Unidirecional Adequado:
    //
    //      Em um sistema de pedidos, uma entidade Pedido pode ter muitas Linhas de Pedido,
    //      mas as Linhas de Pedido não precisam acessar o Pedido para funcionar. Nesse caso,
    //      um relacionamento @OneToMany unidirecional de Pedido para Linha de Pedido pode
    //      ser mais simples e eficiente.


    //  Relacional Unidirecional:
    //
    //      Apenas uma tabela tem a chave estrangeira, e o relacionamento é conhecido
    //      apenas em uma direção.
    //
    //  Relacional Bidirecional:
    //
    //      Mantém a integridade referencial com uma chave estrangeira na tabela
    //      proprietária, e permite navegação em ambas as direções no código da aplicação.
    //
    // Em termos de SQL, não há diferença na estrutura das tabelas entre relacionamentos
    // unidirecionais e bidirecionais, pois a chave estrangeira está sempre na tabela da
    // entidade que é o lado proprietário do relacionamento.





//  Relacionamento Bidirecional

//  Vamos considerar um relacionamento bidirecional entre Paciente e Triagem, onde um
//  Paciente pode ter muitas Triagens e cada Triagem está associada a um Paciente.
//
//
// - Definição das Entidades
//

//@Entity
//public class Paciente {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @OneToMany(mappedBy = "paciente", cascade = CascadeType.ALL, orphanRemoval = true)
//    private List<Triagem> triagens = new ArrayList<>();
//
//    // Outros atributos e métodos...
//}


//@Entity
//public class Triagem {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @ManyToOne
//    @JoinColumn(name = "paciente_id") // Chave estrangeira na tabela Triagem
//    private Paciente paciente;
//
//    // Outros atributos e métodos...
//}


//Navegação entre Entidades
//
// 1. Navegar de Paciente para Triagem
//
//  Se você tem um objeto Paciente, você pode acessar todas as Triagens associadas a ele
//  diretamente:
//

//  Paciente paciente = // Obter paciente de algum lugar, por exemplo, do banco de dados
//
//  -- Acessar todas as triagens associadas ao paciente
//  List<Triagem> triagens = paciente.getTriagens();
//
//
// Aqui, você usa o método getTriagens() da classe Paciente para obter a lista de Triagem
// associadas a esse paciente.
//


//2. Navegar de Triagem para Paciente
//
//  Se você tem um objeto Triagem, você pode acessar o Paciente associado a essa triagem
//  diretamente:
//

//  Triagem triagem = // Obter triagem de algum lugar, por exemplo, do banco de dados
//
//  -- Acessar o paciente associado a esta triagem
//  Paciente paciente = triagem.getPaciente();
//
// Aqui, você usa o método getPaciente() da classe Triagem para obter o Paciente associado
// a essa triagem.
//

//Por que isso é útil?
//
//  Facilidade de Acesso:
//      A navegação bidirecional facilita o acesso e manipulação dos dados. Se você
//      precisar de informações sobre as triagens de um paciente específico ou precisar
//      saber a quem pertence uma triagem, pode fazer isso de forma direta e simples.
//
//
//  Manutenção de Integridade:
//      O mapeamento bidirecional ajuda a garantir que as relações entre as entidades
//      sejam mantidas corretamente. Por exemplo, se você adicionar uma Triagem à lista
//      de triagens de um Paciente, o Paciente na Triagem deve ser atualizado
//      automaticamente para refletir essa mudança.
//
//
//  Consistência no Modelo de Domínio:
//      Permite que você mantenha o modelo de domínio mais consistente e intuitivo,
//      facilitando a compreensão e a manutenção do código.