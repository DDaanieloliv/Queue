package com.ddaaniel.queue.domain.model.dto;

import com.ddaaniel.queue.domain.model.Agendamento;
import com.ddaaniel.queue.domain.model.Especialista;
import com.ddaaniel.queue.domain.model.Paciente;
import com.ddaaniel.queue.domain.model.enuns.Sexo;
import com.ddaaniel.queue.domain.model.enuns.TipoEspecialista;
import com.ddaaniel.queue.domain.repository.EspecialistaRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.LocalTime;



public record RecordDtoAgendamento(
        String nomeCompleto,
        String dataNascimento,
        String telefone,
        String email,
        String cpf,
        Sexo sexo,
        Long especialista,
        LocalTime horaDisponivel,  // Hora que o especialista está disponível
        LocalDate dataDisponivel   // Data que o especialista está disponível
) {

   // @Autowired
   // private static EspecialistaRepository especialistaRepository;


    //public Agendamento fromDtoToEntitys(/*EspecialistaRepository especialistaRepository RecordDtoAgendamento record*/){

    //    var paciente = new Paciente(
    //            this.nomeCompleto(),
    //            this.dataNascimento(),
    //            this.sexo(),
    //            this.cpf(),
    //            this.email(),
    //            this.telefone()
    //    );

        // Busca o Especialista pelo ID usando o repositório
    //    Especialista especialista = especialistaRepository.findById(this.especialista())
    //            .orElseThrow(() -> new RuntimeException("Especialista não encontrado para o ID: " + this.especialista()));


    //    return  new Agendamento(
    //            paciente,
    //            especialista, // Setando na entidade agendamento o id do especialista associado a esse agendamento.
    //            this.dataDisponivel(),
    //            this.horaDisponivel()
    //    );
    //}


    public Agendamento fromDtoToEntitys(Especialista especialista) {

        var paciente = new Paciente(
                this.nomeCompleto(),
                this.dataNascimento(),
                this.sexo(),
                this.cpf(),
                this.email(),
                this.telefone()
        );

        return new Agendamento(
                paciente,
                especialista,
                this.dataDisponivel(),
                this.horaDisponivel()
        );
    }

}
