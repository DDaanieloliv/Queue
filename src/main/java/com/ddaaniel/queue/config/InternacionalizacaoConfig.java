package com.ddaaniel.queue.config;


import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.Locale;

@Configuration
public class InternacionalizacaoConfig {

    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource =
                new ReloadableResourceBundleMessageSource();
        messageSource.setBasename("classpath:messages");
        messageSource.setDefaultEncoding("ISO-8859-1");
        messageSource.setDefaultLocale(Locale.getDefault());

        return messageSource;
    }


    @Bean
    public LocalValidatorFactoryBean validatorFactoryBean() {
        LocalValidatorFactoryBean bean = new LocalValidatorFactoryBean();
        bean.setValidationMessageSource(messageSource());

        return bean;
    }
}


/*
Classe InternacionalizacaoConfig

    A InternacionalizacaoConfig configura o sistema de mensagens de validação do Spring para
    que as mensagens de erro sejam traduzidas e customizadas. Veja os elementos chave dessa
    classe:

    messageSource():
        cria um bean que define a localização das mensagens. Aqui:

    setBasename("classpath:messages"):
        indica que as mensagens de validação são buscadas no arquivo messages.properties,
        localizado na pasta resources.

    setDefaultEncoding("ISO-8859-1"):
        configura a codificação das mensagens, garantindo que caracteres especiais (como
        acentos) sejam exibidos corretamente.

    setDefaultLocale(Locale.getDefault()):
        define o idioma padrão de acordo com o sistema local, mas o idioma também pode ser
        alterado conforme necessário.

    validatorFactoryBean():
        define o MessageSource para ser usado nas validações. Quando o Spring executa
        validações em campos anotados (por exemplo, com @NotEmpty ou @Size), ele usa essas
        mensagens.

Se removêssemos InternacionalizacaoConfig:

As mensagens de erro de validação perderiam as personalizações e ficariam limitadas ao padrão
do Spring, que geralmente retorna as mensagens em inglês e em um formato técnico e mais
genérico.
*/