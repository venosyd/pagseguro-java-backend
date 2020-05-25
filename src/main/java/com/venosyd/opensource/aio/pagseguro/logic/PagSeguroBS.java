package com.venosyd.opensource.aio.pagseguro.logic;

import java.util.Map;

/**
 * @author sergio lisan <sels@venosyd.com>
 */
public interface PagSeguroBS {

        /**  */
        PagSeguroBS INSTANCE = new PagSeguroBSImpl();

        /**
         * retorna o token unico do cliente no pagseguro
         */
        String token();

        /**
         * retorna a credencial usada na conta principal do pagseguro
         */
        String credential();

        /**
         * inicia uma sessao usando token e credencial para fazer as operacoes abaixo
         */
        String createSession();

        /**
         * Cria um plano
         */
        Map<String, Object> createPlan(String planoNome, String planoSigla, String planoURLCancelamento,
                        String planoPreco);

}