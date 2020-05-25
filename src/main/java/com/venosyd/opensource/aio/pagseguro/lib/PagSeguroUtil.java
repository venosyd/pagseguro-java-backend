package com.venosyd.opensource.aio.pagseguro.lib;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import com.venosyd.opensource.aio.pagseguro.logic.CreditCardBS;

/**
 * @author sergio lisan <sels@venosyd.com>
 */
public abstract class PagSeguroUtil {

    /**
     * retorna o valor de uma chave de configuracao do pagseguro no config.yaml
     */
    public static String getValue(String key) {
        return (String) PagSeguroConfig.INSTANCE.get(key);
    }

    /**
     * transforma uma resposta xml em json
     */
    public static String fromXMLtoJSON(String xml) throws Exception {
        var xmlMapper = new XmlMapper();
        var node = xmlMapper.readTree(xml.getBytes());

        var jsonMapper = new ObjectMapper();
        return jsonMapper.writeValueAsString(node);
    }

    /**
     * dada sessionID, quantidade inicial, bandeira do cartao e parcelas, retorna
     * uma relacao com o valor parcela e juros calculados
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static Map<String, Object> getParcelamento(String sessionID, String amount, String ccBrand, int parcelas) {
        List<Map<String, Object>> parcelamento = (List<Map<String, Object>>) ((Map) ((Map) CreditCardBS.INSTANCE
                .getParcelas(sessionID, amount, ccBrand)).get("installments")).get(ccBrand);

        return parcelamento.get(parcelas - 1);
    }
}