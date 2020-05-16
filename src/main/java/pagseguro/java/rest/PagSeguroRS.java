package pagseguro.java.rest;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * @author sergio lisan <sels@venosyd.com>
 */
public interface PagSeguroRS {

    /**  */
    String PAGSEGURO_BASE_URL = "/pagseguro";

    /**  */
    String PAGSEGURO_TOKEN = "/token";

    /**  */
    String PAGSEGURO_CREDENTIAL = "/credential";

    /**  */
    String PAGSEGURO_SESSION = "/session";

    /**  */
    String PAGSEGURO_CC_BRAND = "/ccbrand";

    /**  */
    String PAGSEGURO_CC_TOKEN = "/cctoken";

    /**  */
    String PAGSEGURO_GET_PARCELAS = "/get-parcelas";

    /**  */
    String PAGSEGURO_DO_CHECKOUT = "/do-checkout";

    /**  */
    String PAGSEGURO_SEE_CHECKOUT = "/see-checkout";

    /**  */
    String PAGSEGURO_DO_SUBSCRIPTION = "/do-subscription";

    /**  */
    String PAGSEGURO_SEE_SUBSCRIPTION = "/see-subscription";

    @GET
    @Path(PAGSEGURO_TOKEN)
    @Produces({ MediaType.APPLICATION_JSON })
    Response pagseguroToken();

    @GET
    @Path(PAGSEGURO_CREDENTIAL)
    @Produces({ MediaType.APPLICATION_JSON })
    Response pagseguroCredential();

    @GET
    @Path(PAGSEGURO_SESSION)
    @Produces({ MediaType.APPLICATION_JSON })
    Response createSession();

    @POST
    @Path(PAGSEGURO_CC_BRAND)
    @Produces({ MediaType.APPLICATION_JSON })
    Response ccBrand(String body);

    @POST
    @Path(PAGSEGURO_CC_TOKEN)
    @Produces({ MediaType.APPLICATION_JSON })
    Response ccToken(String body);

    @POST
    @Path(PAGSEGURO_GET_PARCELAS)
    @Produces({ MediaType.APPLICATION_JSON })
    Response getParcelas(String body);

    @POST
    @Path(PAGSEGURO_DO_CHECKOUT)
    @Produces({ MediaType.APPLICATION_JSON })
    Response doCheckout(String body);

    @POST
    @Path(PAGSEGURO_SEE_CHECKOUT)
    @Produces({ MediaType.APPLICATION_JSON })
    Response seeCheckout(String body);

    @POST
    @Path(PAGSEGURO_DO_SUBSCRIPTION)
    @Produces({ MediaType.APPLICATION_JSON })
    Response doSubscription(String body);

    @POST
    @Path(PAGSEGURO_SEE_SUBSCRIPTION)
    @Produces({ MediaType.APPLICATION_JSON })
    Response seeSubscription(String body);
}
