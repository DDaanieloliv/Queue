package com.ddaaniel.queue.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
public class AsyncConfig {

    @Bean(name = "asyncExecutor")
    public Executor asyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("AsyncThread-");
        executor.initialize();
        return executor;
    }

    //  O objetivo dessa configuração é garantir que você tenha um pool de threads dedicado
    //  para executar tarefas assíncronas de forma eficiente. O ThreadPoolTaskExecutor permite
    //  que você controle o número de threads disponíveis e como as tarefas são enfileiradas e
    //  executadas.
    //
    //
    //  Configurar um executor personalizado evita sobrecarregar o pool de threads padrão do
    //  Spring (ou da JVM) e te dá controle total sobre o comportamento de execução.

    //  Essa classe AsyncConfig te permite configurar um pool de threads dedicado para operações
    //  assíncronas, ajudando a melhorar a escalabilidade e a eficiência da sua aplicação.
    //  Através de configurações como o número mínimo e máximo de threads, a capacidade da fila
    //  e o nome das threads, você consegue ajustar o comportamento da execução assíncrona para
    //  atender às necessidades específicas do seu projeto.

}
