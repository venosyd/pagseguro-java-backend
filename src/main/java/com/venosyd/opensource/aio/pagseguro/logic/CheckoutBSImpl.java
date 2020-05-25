package com.venosyd.opensource.aio.pagseguro.logic;

import java.util.HashMap;
import java.util.Map;

import com.venosyd.opensource.aio.commons.http.Http;
import com.venosyd.opensource.aio.commons.log.Debuggable;
import com.venosyd.opensource.aio.commons.util.JSONUtil;
import com.venosyd.opensource.aio.repository.Repository;
import com.venosyd.opensource.aio.pagseguro.lib.PagSeguroUtil;

/**
 * @author sergio lisan <sels@venosyd.com>
 */
public class CheckoutBSImpl implements CheckoutBS, Debuggable {

    @Override
    public Map<String, Object> doCheckout(String sessionID, String itemDescricao, String itemSigla, String clienteNome,
            String clienteCPF, String clienteDDD, String clientePhone, String clienteEmail, String clienteHash,
            String amount, String ccNumero, String ccCVV, String ccMesExpiracao, String ccAnoExpiracao,
            String ccDiaNascimento, String parcelas, String database) {
        var ccToken = CreditCardBS.INSTANCE.getCCToken(sessionID, amount, ccNumero, ccCVV, ccMesExpiracao,
                ccAnoExpiracao);
        var ccBrand = CreditCardBS.INSTANCE.getCCBrand(sessionID, ccNumero.substring(0, 6));

        var baseURL = PagSeguroUtil.getValue("ws");
        var doCheckoutURL = PagSeguroUtil.getValue("do-checkout");
        doCheckoutURL = doCheckoutURL.replace("{{email}}", PagSeguroBS.INSTANCE.credential());
        doCheckoutURL = doCheckoutURL.replace("{{token}}", PagSeguroBS.INSTANCE.token());

        var parcela = PagSeguroUtil.getParcelamento(sessionID, amount, ccBrand, Integer.parseInt(parcelas));

        try {
            var form = new HashMap<String, Object>();

            // checkout payment data
            form.put("paymentMode", "default");
            form.put("paymentMethod", "CREDIT_CARD");
            form.put("currency", "BRL");
            form.put("itemId1", "0001");
            form.put("itemDescription1", itemDescricao);
            form.put("itemAmount1", amount);
            form.put("itemQuantity1", "1");
            form.put("notificationURL", PagSeguroUtil.getValue("redirection-url"));
            form.put("reference", itemSigla);

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
            form.put("billingAddressStreet", PagSeguroUtil.getValue("addr-rua"));
            form.put("billingAddressNumber", PagSeguroUtil.getValue("addr-numero"));
            form.put("billingAddressDistrict", PagSeguroUtil.getValue("addr-bairro"));
            form.put("billingAddressPostalCode", PagSeguroUtil.getValue("addr-cep"));
            form.put("billingAddressCity", PagSeguroUtil.getValue("addr-cidade"));
            form.put("billingAddressState", PagSeguroUtil.getValue("addr-estado"));
            form.put("billingAddressCountry", "BRA");

            var response = Http.postForm(baseURL + doCheckoutURL, form, new HashMap<>());
            var body = response.getStringBody();

            var result = JSONUtil.<String, Object>fromJSONToMap(PagSeguroUtil.fromXMLtoJSON(body));

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
        var email = PagSeguroBS.INSTANCE.credential();
        var token = PagSeguroBS.INSTANCE.token();

        var baseURL = PagSeguroUtil.getValue("ws");
        var seeCheckoutURL = PagSeguroUtil.getValue("see-checkout");
        seeCheckoutURL = seeCheckoutURL.replace("{{transactionID}}", checkoutCode);
        seeCheckoutURL = seeCheckoutURL.replace("{{email}}", email);
        seeCheckoutURL = seeCheckoutURL.replace("{{token}}", token);

        try {
            var response = Http.get(baseURL + seeCheckoutURL);
            var body = response.getStringBody();

            return JSONUtil.fromJSONToMap(PagSeguroUtil.fromXMLtoJSON(body));

        } catch (Exception e) {
            err.exception("PAGSEGURO SEE-CHECKOUT", e);
        }

        return null;
    }

}