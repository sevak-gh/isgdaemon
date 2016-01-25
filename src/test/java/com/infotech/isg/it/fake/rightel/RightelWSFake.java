package com.infotech.isg.it.fake.rightel;

import com.infotech.isg.proxy.rightel.RightelProxy;
import com.infotech.isg.proxy.rightel.RightelProxySubmitChargeRequestResponse;
import com.infotech.isg.proxy.rightel.RightelProxyConfirmChargeRequestResponse;
import com.infotech.isg.proxy.rightel.RightelProxyInquiryChargeResponse;
import com.infotech.isg.proxy.rightel.RightelProxyGetAccountBalanceResponse;

import java.util.List;
import java.util.ArrayList;
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
 * fake web service for Rightel, used for integration tests
 * annotated as spring component so that app properties can be used
 *
 * @author Sevak Gharibian
 */
@WebService(name = "RightelWSFake", targetNamespace = "http://topup.org/webservices/")
@SOAPBinding(style = Style.DOCUMENT, use = Use.LITERAL)
@Component
public class RightelWSFake {

    private RightelProxy rightelService;
    private Endpoint ep;

    @Value("${rightel.url}")
    private String url;

    @WebMethod(exclude = true)
    public void setServiceImpl(RightelProxy rightelService) {
        this.rightelService = rightelService;
    }

    @WebMethod(operationName = "SubmitChargeRequest")
    @WebResult(name = "SubmitChargeRequestResult")
    public RightelProxySubmitChargeRequestResponse submitChargeRequest(@WebParam(name = "telno") String consumer,
                                                                       @WebParam(name = "amount") int amount,
                                                                       @WebParam(name = "chargeChanel") int channel) {
        return rightelService.submitChargeRequest(consumer, amount, channel);
    }

    @WebMethod(operationName = "ConfirmChargeRequest")
    @WebResult(name = "ConfirmChargeRequestResult")
    public RightelProxyConfirmChargeRequestResponse confirmChargeRequest(@WebParam(name = "requestId") String requestId,
                                                                         @WebParam(name = "transactionId") long trId) {
        return rightelService.confirmChargeRequest(requestId, trId);
    }

    @WebMethod(operationName = "InquiryCharge")
    @WebResult(name = "InquiryChargeResult")
    public RightelProxyInquiryChargeResponse inquiryCharge(@WebParam(name = "transactionId") long trId) {
        return rightelService.inquiryCharge(trId);
    }

    @WebMethod(operationName = "GetAccountBalance")
    @WebResult(name = "GetAccountBalanceResult")
    public RightelProxyGetAccountBalanceResponse getAccountBalance() {
        return rightelService.getAccountBalance();
    }

    @WebMethod(exclude = true)
    public void publish() {
        if ((ep != null) && (ep.isPublished())) {
            throw new RuntimeException("EP already published");
        }

        ep = Endpoint.publish(url, this);
   }

    @WebMethod(exclude = true)
    public void stop() {
        if (ep != null) {
            ep.stop();
        }
   }
}
