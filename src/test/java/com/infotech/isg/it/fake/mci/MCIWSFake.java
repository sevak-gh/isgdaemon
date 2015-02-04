package com.infotech.isg.it.fake.mci;

import com.infotech.isg.proxy.mci.MCIProxy;
import com.infotech.isg.proxy.mci.MCIProxyGetTokenResponse;
import com.infotech.isg.proxy.mci.MCIProxyGetRemainedBrokerRechargeResponse;

import javax.jws.WebService;
import javax.jws.HandlerChain;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.Style;
import javax.jws.soap.SOAPBinding.Use;
import javax.xml.ws.Endpoint;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

/**
 * fake web service for MCI, used for integration tests
 * annotated as spring component so that app properties can be used
 *
 * @author Sevak Gharibian
 */
@WebService(name = "MCIWSFake", targetNamespace = "http://mci.service/")
@HandlerChain(file = "handler-chain.xml")
@SOAPBinding(style = Style.DOCUMENT, use = Use.LITERAL)
@Component
public class MCIWSFake {

    private MCIProxy mciService;
    private Endpoint ep1;
    private Endpoint ep2;

    @Value("${mci1.url}")
    private String url1;
    @Value("${mci2.url}")
    private String url2;

    @WebMethod(exclude = true)
    public void setServiceImpl(MCIProxy mciService) {
        this.mciService = mciService;
    }

    @WebMethod(operationName = "GetToken", action = "http://mci.service/GetToken")
    @WebResult(name = "GetTokenResult")
    public MCIProxyGetTokenResponse getToken() {
        return mciService.getToken();
    }

    @WebMethod(operationName = "GetRemainedBrokerRecharge", action = "http://mci.service/GetRemainedBrokerRecharge")
    @WebResult(name = "GetRemainedBrokerRechargeResult")
    public MCIProxyGetRemainedBrokerRechargeResponse getRemainedBrokerRecharge(@WebParam(name = "BrokerID") String token,
            @WebParam(name = "CardAmount") int amount) {
        return mciService.getRemainedBrokerRecharge(token, amount);
    }

    @WebMethod(exclude = true)
    public void publish() {
        if ((ep1 == null) || (!ep1.isPublished())) {
            ep1 = Endpoint.publish(url1, this);
        }
        if ((ep2 == null) || (!ep2.isPublished())) {
            ep2 = Endpoint.publish(url2, this);
        }
    }

    @WebMethod(exclude = true)
    public void stop() {
        if (ep1 != null) {
            ep1.stop();
        }
        if (ep2 != null) {
            ep2.stop();
        }
    }
}
