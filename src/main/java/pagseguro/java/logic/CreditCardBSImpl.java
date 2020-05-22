package pagseguro.java.logic;

import java.util.HashMap;
import java.util.Map;

import bequiend.commons.http.Http;
import bequiend.commons.log.Debuggable;
import bequiend.commons.util.JSONUtil;
import pagseguro.java.lib.PagSeguroUtil;

/**
 * @author sergio lisan <sels@venosyd.com>
 */
public class CreditCardBSImpl implements CreditCardBS, Debuggable {

    @Override
    @SuppressWarnings("rawtypes")
    public String getCCBrand(String sessionID, String ccBin) {
        var ccBrandURL = PagSeguroUtil.getValue("cc-brand");
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
        var ccTokenURL = PagSeguroUtil.getValue("cc-token");
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

            var json = JSONUtil.<String, Object>fromJSONToMap(PagSeguroUtil.fromXMLtoJSON(body));

            return (String) json.get("token");

        } catch (Exception e) {
            err.exception("PAGSEGURO CC-TOKEN", e);
        }

        return "INVALID_CARD_TOKEN";
    }

    @Override
    public Map<String, Object> getParcelas(String sessionID, String amount, String ccBrand) {
        var baseURL = PagSeguroUtil.getValue("url");
        var getParcelasURL = PagSeguroUtil.getValue("get-parcelas");
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

}