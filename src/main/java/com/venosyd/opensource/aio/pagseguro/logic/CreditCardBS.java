package com.venosyd.opensource.aio.pagseguro.logic;

import java.util.Map;

/**
 * @author sergio lisan <sels@venosyd.com>
 */
public interface CreditCardBS {

        /**  */
        CreditCardBS INSTANCE = new CreditCardBSImpl();

        /**
         * com uma ID de sessao e os 6 primeiros numeros do cartao de credito, retorna a
         * bandeira do cartao
         */
        String getCCBrand(String sessionID, String ccBin);

        /**
         * com ID de sessao, amount e brand retorna uma lista de parcelamentos e seus
         * valores
         */
        Map<String, Object> getParcelas(String sessionID, String amount, String ccBrand);

        /**
         * retorna o token do cartao de credito do cliente
         */
        String getCCToken(String sessionID, String amount, String ccNumero, String ccCVV, String ccMesExpiracao,
                        String ccAnoExpiracao);

}