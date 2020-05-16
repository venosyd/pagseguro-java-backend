package pagseguro.java.logic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import bequiend.commons.http.Http;
import bequiend.commons.log.Debuggable;
import bequiend.commons.util.Config;
import bequiend.commons.util.JSONUtil;
import bequiend.repository.Repository;

/**
 * @author sergio lisan <sels@venosyd.com>
 */
public class PagSeguroBSImpl implements PagSeguroBS, Debuggable {

    @Override
    public String token() {
        return _getpagseguroconfigvalue("token");
    }

    @Override
    public String credential() {
        return _getpagseguroconfigvalue("credential");
    }

    @Override
    public String createSession() {
        var email = credential();
        var token = token();

        var baseURL = _getpagseguroconfigvalue("ws");
        var sessionURL = _getpagseguroconfigvalue("create-session");
        sessionURL = sessionURL.replace("{{email}}", email);
        sessionURL = sessionURL.replace("{{token}}", token);

        try {
            var response = Http.post(baseURL + sessionURL, new HashMap<>());
            var body = response.getStringBody();

            var json = JSONUtil.<String, Object>fromJSONToMap(_fromxmltojson(body));
            return (String) json.get("id");

        } catch (Exception e) {
            err.exception("PAGSEGURO CREATE-SESSION", e);
        }

        return "INVALID_SESSION_ID";
    }

    @Override
    @SuppressWarnings("rawtypes")
    public String getCCBrand(String sessionID, String ccBin) {
        var ccBrandURL = _getpagseguroconfigvalue("cc-brand");
        ccBrandURL = ccBrandURL.replace("{{sessionID}}", sessionID);
        ccBrandURL = ccBrandURL.replace("{{ccBin}}", ccBin);

        try {
            var response = Http.get(ccBrandURL);
            var body = response.getStringBody();

            var json = JSONUtil.<String, Object>fromJSONToMap(body);
            return (String) ((Map) ((Map) json.get("bin")).get("brand")).get("name");

        } catch (Exception e) {
            err.exception("PAGSEGURO CC-BRAND", e);
        }

        return "INVALID_CARD_BRAND";
    }

    @Override
    public String getCCToken(String sessionID, String amount, String ccNumero, String ccCVV, String ccMesExpiracao,
            String ccAnoExpiracao) {
        var ccTokenURL = _getpagseguroconfigvalue("cc-token");
        var ccBrand = getCCBrand(sessionID, ccNumero.substring(0, 6));

        try {
            var form = new HashMap<String, Object>();
            form.put("sessionId", sessionID);
            form.put("amount", amount);
            form.put("cardNumber", ccNumero);
            form.put("cardBrand", ccBrand);
            form.put("cardCvv", ccCVV);
            form.put("cardExpirationMonth", ccMesExpiracao);
            form.put("cardExpirationYear", ccAnoExpiracao);

            var response = Http.postForm(ccTokenURL, form, new HashMap<>());
            var body = response.getStringBody();

            var json = JSONUtil.<String, Object>fromJSONToMap(_fromxmltojson(body));

            return (String) json.get("token");

        } catch (Exception e) {
            err.exception("PAGSEGURO CC-TOKEN", e);
        }

        return "INVALID_CARD_TOKEN";
    }

    @Override
    public Map<String, Object> getParcelas(String sessionID, String amount, String ccBrand) {
        var baseURL = _getpagseguroconfigvalue("url");
        var getParcelasURL = _getpagseguroconfigvalue("get-parcelas");
        getParcelasURL = getParcelasURL.replace("{{sessionID}}", sessionID);
        getParcelasURL = getParcelasURL.replace("{{amount}}", amount);
        getParcelasURL = getParcelasURL.replace("{{ccBrand}}", ccBrand);

        try {
            var response = Http.get(baseURL + getParcelasURL);
            var body = response.getStringBody();

            return JSONUtil.fromJSONToMap(body);

        } catch (Exception e) {
            err.exception("PAGSEGURO GET-PARCELAS", e);
        }

        return null;
    }

    @Override
    public Map<String, Object> doCheckout(String sessionID, String clienteNome, String clienteCPF, String clienteDDD,
            String clientePhone, String clienteEmail, String clienteHash, String amount, String ccNumero, String ccCVV,
            String ccMesExpiracao, String ccAnoExpiracao, String ccDiaNascimento, String parcelas, String database) {
        var ccToken = getCCToken(sessionID, amount, ccNumero, ccCVV, ccMesExpiracao, ccAnoExpiracao);
        var ccBrand = getCCBrand(sessionID, ccNumero.substring(0, 6));

        var baseURL = _getpagseguroconfigvalue("ws");
        var doCheckoutURL = _getpagseguroconfigvalue("do-checkout");
        doCheckoutURL = doCheckoutURL.replace("{{email}}", credential());
        doCheckoutURL = doCheckoutURL.replace("{{token}}", token());

        var parcela = _getparcelamento(sessionID, amount, ccBrand, Integer.parseInt(parcelas));

        try {
            var form = new HashMap<String, Object>();

            // checkout payment data
            form.put("paymentMode", "default");
            form.put("paymentMethod", "CREDIT_CARD");
            form.put("currency", "BRL");
            form.put("itemId1", "0001");
            form.put("itemDescription1", _getpagseguroconfigvalue("ir-description"));
            form.put("itemAmount1", amount);
            form.put("itemQuantity1", "1");
            form.put("notificationURL", _getpagseguroconfigvalue("redirection-url"));
            form.put("reference", _getpagseguroconfigvalue("ir-reference"));

            // sender data
            form.put("senderName", clienteNome);
            form.put("senderCPF", clienteCPF);
            form.put("senderAreaCode", clienteDDD);
            form.put("senderPhone", clientePhone);
            form.put("senderEmail", clienteEmail);
            form.put("senderHash", clienteHash);

            form.put("shippingAddressRequired", "false");

            // user creditcard
            form.put("creditCardToken", ccToken);
            form.put("installmentQuantity", parcelas);
            form.put("installmentValue",
                    String.format("%.2f", Double.parseDouble(parcela.get("installmentAmount") + "")));
            form.put("creditCardHolderName", clienteNome);
            form.put("creditCardHolderCPF", clienteCPF);
            form.put("creditCardHolderBirthDate", ccDiaNascimento);
            form.put("creditCardHolderAreaCode", clienteDDD);
            form.put("creditCardHolderPhone", clientePhone);

            // endereco da empresa
            form.put("billingAddressStreet", _getpagseguroconfigvalue("addr-rua"));
            form.put("billingAddressNumber", _getpagseguroconfigvalue("addr-numero"));
            form.put("billingAddressDistrict", _getpagseguroconfigvalue("addr-bairro"));
            form.put("billingAddressPostalCode", _getpagseguroconfigvalue("addr-cep"));
            form.put("billingAddressCity", _getpagseguroconfigvalue("addr-cidade"));
            form.put("billingAddressState", _getpagseguroconfigvalue("addr-estado"));
            form.put("billingAddressCountry", "BRA");

            var response = Http.postForm(baseURL + doCheckoutURL, form, new HashMap<>());
            var body = response.getStringBody();

            var result = JSONUtil.<String, Object>fromJSONToMap(_fromxmltojson(body));

            if (!result.containsKey("error")) {
                result.put("id", result.get("code"));
                result.put("type", "Checkout");
                result.put("collection_key", "Transaction");

                Repository.INSTANCE.save(database, JSONUtil.toJSON(result));
            }

            return result;

        } catch (Exception e) {
            err.exception("PAGSEGURO DO-CHECKOUT", e);
        }

        return null;
    }

    @Override
    public Map<String, Object> seeCheckout(String checkoutCode) {
        var email = credential();
        var token = token();

        var baseURL = _getpagseguroconfigvalue("ws");
        var seeCheckoutURL = _getpagseguroconfigvalue("see-checkout");
        seeCheckoutURL = seeCheckoutURL.replace("{{transactionID}}", checkoutCode);
        seeCheckoutURL = seeCheckoutURL.replace("{{email}}", email);
        seeCheckoutURL = seeCheckoutURL.replace("{{token}}", token);

        try {
            var response = Http.get(baseURL + seeCheckoutURL);
            var body = response.getStringBody();

            return JSONUtil.fromJSONToMap(_fromxmltojson(body));

        } catch (Exception e) {
            err.exception("PAGSEGURO SEE-CHECKOUT", e);
        }

        return null;
    }

    @Override
    public Map<String, Object> doSubcription(String sessionID, String clienteNome, String clienteCPF, String clienteDDD,
            String clientePhone, String clienteEmail, String clienteHash, String enderecoRua, String enderecoNumero,
            String enderecoDistrito, String enderecoCidade, String enderecoEstado, String enderecoCEP, String ccNumero,
            String ccCVV, String ccMesExpiracao, String ccAnoExpiracao, String ccDiaNascimento, String database) {
        var amount = _getpagseguroconfigvalue("plan-price");
        var planID = _getpagseguroconfigvalue("plan-id");
        var reference = _getpagseguroconfigvalue("plan-reference");

        var ccToken = getCCToken(sessionID, amount, ccNumero, ccCVV, ccMesExpiracao, ccAnoExpiracao);

        var baseURL = _getpagseguroconfigvalue("ws");
        var doSubscriptionURL = _getpagseguroconfigvalue("do-subscription");
        doSubscriptionURL = doSubscriptionURL.replace("{{email}}", credential());
        doSubscriptionURL = doSubscriptionURL.replace("{{token}}", token());

        try {
            var form = new HashMap<String, Object>();
            form.put("plan", planID);
            form.put("reference", reference);

            var senderForm = new HashMap<String, Object>();
            senderForm.put("name", clienteNome);
            senderForm.put("email", clienteEmail);
            senderForm.put("hash", clienteHash);

            // sender
            var senderPhone = new HashMap<String, Object>();
            senderPhone.put("areaCode", clienteDDD);
            senderPhone.put("number", clientePhone);

            senderForm.put("phone", senderPhone);

            var senderAddress = new HashMap<String, Object>();
            senderAddress.put("street", enderecoRua);
            senderAddress.put("number", enderecoNumero);
            senderAddress.put("complement", "");
            senderAddress.put("district", enderecoDistrito);
            senderAddress.put("city", enderecoCidade);
            senderAddress.put("state", enderecoEstado);
            senderAddress.put("country", "BRA");
            senderAddress.put("postalCode", enderecoCEP);

            senderForm.put("address", senderAddress);

            var docs = new ArrayList<Map<String, Object>>();
            var docsForm = new HashMap<String, Object>();
            docsForm.put("type", "CPF");
            docsForm.put("value", clienteCPF);
            docs.add(docsForm);
            senderForm.put("documents", docs);

            form.put("sender", senderForm);

            // /-sender

            // payment
            var paymentForm = new HashMap<String, Object>();
            paymentForm.put("type", "CREDITCARD");

            var ccForm = new HashMap<String, Object>();
            ccForm.put("token", ccToken);

            var ccHolder = new HashMap<String, Object>();
            ccHolder.put("name", clienteNome);
            ccHolder.put("birthDate", ccDiaNascimento);
            ccHolder.put("documents", docs);
            ccHolder.put("phone", senderPhone);
            ccHolder.put("billingAddress", senderAddress);

            ccForm.put("holder", ccHolder);
            paymentForm.put("creditCard", ccForm);

            form.put("paymentMethod", paymentForm);

            // /-payment

            var header = new HashMap<String, String>();
            header.put("Accept", "application/vnd.pagseguro.com.br.v1+json;charset=ISO-8859-1");

            var response = Http.post(baseURL + doSubscriptionURL, form, header);
            var result = JSONUtil.<String, Object>fromJSONToMap(response.getStringBody());

            if (!result.containsKey("error")) {
                result.put("id", result.get("code"));
                result.put("type", "Subscription");
                result.put("collection_key", "Transaction");

                Repository.INSTANCE.save(database, JSONUtil.toJSON(result));
            }

            return result;

        } catch (Exception e) {
            err.exception("PAGSEGURO DO-SUBSCRIPTION", e);
        }

        return null;
    }

    @Override
    public Map<String, Object> seeSubscription(String subscriptionCode) {
        var email = credential();
        var token = token();

        var baseURL = _getpagseguroconfigvalue("ws");
        var seeSubscriptionURL = _getpagseguroconfigvalue("see-subscription");
        seeSubscriptionURL = seeSubscriptionURL.replace("{{subscriptionID}}", subscriptionCode);
        seeSubscriptionURL = seeSubscriptionURL.replace("{{email}}", email);
        seeSubscriptionURL = seeSubscriptionURL.replace("{{token}}", token);

        try {
            var header = new HashMap<String, String>();
            header.put("Accept", "application/vnd.pagseguro.com.br.v3+json;charset=ISO-8859-1");

            var response = Http.get(baseURL + seeSubscriptionURL, header);
            return JSONUtil.fromJSONToMap(response.getStringBody());

        } catch (Exception e) {
            err.exception("PAGSEGURO SEE-SUBSCRIPTION", e);
        }

        return null;
    }

    /**
     * dada sessionID, quantidade inicial, bandeira do cartao e parcelas, retorna
     * uma relacao com o valor parcela e juros calculados
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private Map<String, Object> _getparcelamento(String sessionID, String amount, String ccBrand, int parcelas) {
        List<Map<String, Object>> parcelamento = (List<Map<String, Object>>) ((Map) ((Map) getParcelas(sessionID,
                amount, ccBrand)).get("installments")).get(ccBrand);

        return parcelamento.get(parcelas - 1);
    }

    /**
     * retorna o valor de uma chave de configuracao do pagseguro no config.yaml
     */
    @SuppressWarnings("rawtypes")
    private String _getpagseguroconfigvalue(String key) {
        return (String) ((Map) Config.INSTANCE.get("pagseguro")).get(key);
    }

    /**
     * transforma uma resposta xml em json
     */
    private String _fromxmltojson(String xml) throws Exception {
        var xmlMapper = new XmlMapper();
        var node = xmlMapper.readTree(xml.getBytes());

        var jsonMapper = new ObjectMapper();
        return jsonMapper.writeValueAsString(node);
    }

}