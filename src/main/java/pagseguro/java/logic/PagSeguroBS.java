package pagseguro.java.logic;

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

        /**
         * Aderir ao plano de beneficios com base na deducao do imposto de renda
         */
        Map<String, Object> doCheckout(String sessionID, String clienteNome, String clienteCPF, String clienteDDD,
                        String clientePhone, String clienteEmail, String clienteHash, String amount, String ccNumero,
                        String ccCVV, String ccMesExpiracao, String ccAnoExpiracao, String ccDiaNascimento,
                        String parcelas, String database);

        /**  */
        Map<String, Object> seeCheckout(String checkoutCode);

        /**
         * Aderir ao plano de mensalidade fixa
         */
        Map<String, Object> doSubcription(String sessionID, String clienteNome, String clienteCPF, String clienteDDD,
                        String clientePhone, String clienteEmail, String clienteHash, String enderecoRua,
                        String enderecoNumero, String enderecoDistrito, String enderecoCidade, String enderecoEstado,
                        String enderecoCEP, String ccNumero, String ccCVV, String ccMesExpiracao, String ccAnoExpiracao,
                        String ccDiaNascimento, String database);

        /**
         * Verifica se a assinatura foi paga
         */
        Map<String, Object> seeSubscription(String subscriptionCode);

}