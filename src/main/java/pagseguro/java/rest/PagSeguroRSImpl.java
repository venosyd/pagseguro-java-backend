package pagseguro.java.rest;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import bequiend.commons.util.Config;
import bequiend.commons.util.JSONUtil;
import bequiend.commons.util.RESTService;
import pagseguro.java.logic.PagSeguroBS;

/**
 * @author sergio lisan <sels@venosyd.com>
 */
@Path(PagSeguroRS.PAGSEGURO_BASE_URL)
public class PagSeguroRSImpl implements PagSeguroRS, RESTService {

    @Context
    private HttpHeaders headers;

    public PagSeguroRSImpl() {

    }

    @Override
    public Response pagseguroToken() {
        return process(_unwrap(), getauthcode(headers), null, (request) -> {
            var response = new HashMap<String, String>();
            response.put("status", "ok");
            response.put("payload", PagSeguroBS.INSTANCE.token());

            return makeResponse(response);
        });
    }

    @Override
    public Response pagseguroCredential() {
        return process(_unwrap(), getauthcode(headers), null, (request) -> {
            var response = new HashMap<String, String>();
            response.put("status", "ok");
            response.put("payload", PagSeguroBS.INSTANCE.credential());

            return makeResponse(response);
        });
    }

    @Override
    public Response createSession() {
        return process(_unwrap(), getauthcode(headers), null, (request) -> {
            var response = new HashMap<String, String>();

            var result = PagSeguroBS.INSTANCE.createSession();

            if (!result.equals("INVALID_SESSION_ID")) {
                response.put("status", "ok");
                response.put("payload", result);
            } else {
                response.put("status", "error");
                response.put("message", result);
            }

            return makeResponse(response);
        });
    }

    @Override
    public Response ccBrand(String body) {
        return process(_unwrap(body), getauthcode(headers), Arrays.asList("sessionID", "ccBin"), (request) -> {
            var response = new HashMap<String, String>();

            var sessionID = request.get("sessionID");
            var ccBin = request.get("ccBin");

            var result = PagSeguroBS.INSTANCE.getCCBrand(sessionID, ccBin);

            if (!result.equals("INVALID_CARD_BRAND")) {
                response.put("status", "ok");
                response.put("payload", result);
            } else {
                response.put("status", "error");
                response.put("message", result);
            }

            return makeResponse(response);
        });
    }

    @Override
    public Response ccToken(String body) {
        List<String> arguments = Arrays.asList("sessionID", "amount", "ccNumero", "ccCVV", "ccMesExpiracao",
                "ccAnoExpiracao");

        return process(_unwrap(body), getauthcode(headers), arguments, (request) -> {
            var response = new HashMap<String, String>();

            var sessionID = request.get("sessionID");
            var amount = String.format("%.2f", Double.parseDouble(request.get("amount")));
            var ccNumero = request.get("ccNumero");
            var ccCVV = request.get("ccCVV");
            var ccMesExpiracao = request.get("ccMesExpiracao");
            var ccAnoExpiracao = request.get("ccAnoExpiracao");

            var result = PagSeguroBS.INSTANCE.getCCToken(sessionID, amount, ccNumero, ccCVV, ccMesExpiracao,
                    ccAnoExpiracao);

            if (!result.equals("INVALID_CARD_TOKEN")) {
                response.put("status", "ok");
                response.put("payload", result);
            } else {
                response.put("status", "error");
                response.put("message", result);
            }

            return makeResponse(response);
        });
    }

    @Override
    public Response getParcelas(String body) {
        List<String> arguments = Arrays.asList("sessionID", "amount", "ccBrand");

        return process(_unwrap(body), getauthcode(headers), arguments, (request) -> {
            var response = new HashMap<String, String>();

            var sessionID = request.get("sessionID");
            var amount = String.format("%.2f", Double.parseDouble(request.get("amount")));
            var ccBrand = request.get("ccBrand");

            var result = PagSeguroBS.INSTANCE.getParcelas(sessionID, amount, ccBrand);

            if (result != null && result.get("error").equals("false")) {
                response.put("status", "ok");
                response.put("payload", JSONUtil.toJSON(result));
            } else {
                response.put("status", "error");
                response.put("message", JSONUtil.toJSON(result));
            }

            return makeResponse(response);
        });
    }

    @Override
    @SuppressWarnings("rawtypes")
    public Response doCheckout(String body) {
        List<String> arguments = Arrays.asList("sessionID", "clienteNome", "clienteCPF", "clienteDDD", "clientePhone",
                "clienteEmail", "clienteHash", "amount", "parcelas", "ccNumero", "ccCVV", "ccMesExpiracao",
                "ccAnoExpiracao", "ccDiaNascimento");

        return process(_unwrap(body), getauthcode(headers), arguments, (request) -> {
            var response = new HashMap<String, String>();

            var sessionID = request.get("sessionID");
            var clienteNome = request.get("clienteNome");
            var clienteCPF = request.get("clienteCPF");
            var clienteDDD = request.get("clienteDDD");
            var clientePhone = request.get("clientePhone");
            var clienteEmail = request.get("clienteEmail");
            var clienteHash = request.get("clienteHash");
            var amount = String.format("%.2f", Double.parseDouble(request.get("amount")));
            var parcelas = request.get("parcelas");
            var ccNumero = request.get("ccNumero");
            var ccCVV = request.get("ccCVV");
            var ccMesExpiracao = request.get("ccMesExpiracao");
            var ccAnoExpiracao = request.get("ccAnoExpiracao");
            var ccDiaNascimento = request.get("ccDiaNascimento");

            String database = (String) ((Map) Config.INSTANCE.get("pagseguro")).get("bancodedados");

            var result = PagSeguroBS.INSTANCE.doCheckout(sessionID, clienteNome, clienteCPF, clienteDDD, clientePhone,
                    clienteEmail, clienteHash, amount, ccNumero, ccCVV, ccMesExpiracao, ccAnoExpiracao, ccDiaNascimento,
                    parcelas, database);

            if (result != null && !result.containsKey("error")) {
                response.put("status", "ok");
                response.put("payload", JSONUtil.toJSON(result));
            } else {
                response.put("status", "error");
                response.put("message", JSONUtil.toJSON(result));
            }

            return makeResponse(response);
        });
    }

    @Override
    public Response seeCheckout(String body) {
        return process(_unwrap(body), getauthcode(headers), Arrays.asList("transactionID"), (request) -> {
            var response = new HashMap<String, String>();
            var result = PagSeguroBS.INSTANCE.seeCheckout(request.get("transactionID"));

            if (result != null && !result.containsKey("error")) {
                response.put("status", "ok");
                response.put("payload", JSONUtil.toJSON(result));
            } else {
                response.put("status", "error");
                response.put("message", JSONUtil.toJSON(result));
            }

            return makeResponse(response);
        });
    }

    @Override
    @SuppressWarnings("rawtypes")
    public Response doSubscription(String body) {
        var arguments = Arrays.asList("sessionID", "clienteNome", "clienteCPF", "clienteDDD", "clientePhone",
                "clienteEmail", "clienteHash", "enderecoRua", "enderecoDistrito", "enderecoCidade", "enderecoEstado",
                "enderecoCEP", "ccNumero", "ccCVV", "ccMesExpiracao", "ccAnoExpiracao", "ccDiaNascimento");

        return process(_unwrap(body), getauthcode(headers), arguments, (request) -> {
            var response = new HashMap<String, String>();

            var sessionID = request.get("sessionID");
            var clienteNome = request.get("clienteNome");
            var clienteCPF = request.get("clienteCPF");
            var clienteDDD = request.get("clienteDDD");
            var clientePhone = request.get("clientePhone");
            var clienteEmail = request.get("clienteEmail");
            var clienteHash = request.get("clienteHash");
            var enderecoRua = request.get("enderecoRua");
            var enderecoNumero = request.get("enderecoNumero");
            var enderecoDistrito = request.get("enderecoDistrito");
            var enderecoCidade = request.get("enderecoCidade");
            var enderecoEstado = request.get("enderecoEstado");
            var enderecoCEP = request.get("enderecoCEP");
            var ccNumero = request.get("ccNumero");
            var ccCVV = request.get("ccCVV");
            var ccMesExpiracao = request.get("ccMesExpiracao");
            var ccAnoExpiracao = request.get("ccAnoExpiracao");
            var ccDiaNascimento = request.get("ccDiaNascimento");

            String database = (String) ((Map) Config.INSTANCE.get("pagseguro")).get("bancodedados");

            var result = PagSeguroBS.INSTANCE.doSubcription(sessionID, clienteNome, clienteCPF, clienteDDD,
                    clientePhone, clienteEmail, clienteHash, enderecoRua, enderecoNumero, enderecoDistrito,
                    enderecoCidade, enderecoEstado, enderecoCEP, ccNumero, ccCVV, ccMesExpiracao, ccAnoExpiracao,
                    ccDiaNascimento, database);

            if (result != null && !result.containsKey("error")) {
                response.put("status", "ok");
                response.put("payload", JSONUtil.toJSON(result));
            } else {
                response.put("status", "error");
                response.put("message", JSONUtil.toJSON(result));
            }

            return makeResponse(response);
        });
    }

    @Override
    public Response seeSubscription(String body) {
        return process(_unwrap(body), getauthcode(headers), Arrays.asList("subscriptionID"), (request) -> {
            var response = new HashMap<String, String>();
            var result = PagSeguroBS.INSTANCE.seeSubscription(request.get("subscriptionID"));

            if (result != null && !result.containsKey("error")) {
                response.put("status", "ok");
                response.put("payload", JSONUtil.toJSON(result));
            } else {
                response.put("status", "error");
                response.put("message", JSONUtil.toJSON(result));
            }

            return makeResponse(response);
        });
    }

    /**
     * traduz o JSON pra mapa e insere um dado importante que eh a base de dados
     * para o servidor saber direcionar o fluxo de persistencia e consulta
     * corretamente
     */
    @SuppressWarnings("rawtypes")
    private Map<String, String> _unwrap(String body) {
        var request = JSONUtil.<String, String>fromJSONToMap(body);

        String database = (String) ((Map) Config.INSTANCE.get("pagseguro")).get("bancodedados");
        request.put("database", database);

        return request;
    }

    private Map<String, String> _unwrap() {
        return _unwrap("{}");
    }

}
