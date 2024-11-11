package com.ddaaniel.queue.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic"); // O servidor enviará mensagens com este prefixo
        config.setApplicationDestinationPrefixes("/app"); // Prefixo de destino para mensagens do cliente
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/fila-websocket")
                .setAllowedOrigins("*")
                .withSockJS(); // Endpoint WebSocket com SockJS fallback
    }


}

//  Essa classe WebSocketConfig é a responsável por configurar o WebSocket, ela habilita a
//  comunicação em tempo real entre o servidor e os clientes (navegadores, por exemplo). Ela
//  utiliza o protocolo STOMP (Simple Text Oriented Messaging Protocol), que é um protocolo de
//  mensagem baseado em texto para WebSockets.
//
//
//  @Configuration: Informa ao Spring que esta é uma classe de configuração. Ela é responsável
//      por definir e configurar beans, ou seja, objetos gerenciados pelo Spring.
//
//  @EnableWebSocketMessageBroker: Habilita a funcionalidade de WebSocket com suporte a mensagens
//      STOMP. Isso permite que o Spring lide com mensagens baseadas no protocolo WebSocket.
//
//  WebSocketMessageBrokerConfigurer: Interface que você implementa para configurar os detalhes
//      do WebSocket. Ela fornece métodos para configurar o broker de mensagens, os endpoints de
//      WebSocket, etc.
//
//
//      /topic: Prefixo usado para as mensagens enviadas do servidor para os clientes. O
//          servidor envia atualizações (como chamar o próximo paciente) usando destinos que
//          começam com /topic.
//
//      /fila-websocket: Endpoint de conexão WebSocket. Os clientes usam esse endpoint para
//          estabelecer a conexão WebSocket com o servidor.
//
//      SockJS: Biblioteca que garante um fallback (alternativa) para o WebSocket, caso ele não
//          esteja disponível no navegador ou rede..