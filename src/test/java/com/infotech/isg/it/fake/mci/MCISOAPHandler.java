package com.infotech.isg.it.fake.mci;

import java.util.Set;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPBodyElement;
import javax.xml.soap.SOAPElement;

/**
 * handling specific condition where request soap body is empty
 * in this case, SOAPAction header determines the operation.
 *
 * @author Sevak Gahribian
 */
public class MCISOAPHandler implements SOAPHandler<SOAPMessageContext> {

    @Override
    public boolean handleMessage(SOAPMessageContext context) {
        Boolean isResponse = (Boolean) context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);

        if (!isResponse) {
            // request
            try {
                SOAPMessage request = context.getMessage();
                SOAPBody body = request.getSOAPBody();
                if (!(body.getChildElements().hasNext())) {
                    // soap body is empty
                    body.addBodyElement(new QName("http://mci.service/", "GetToken"));
                }
            } catch (SOAPException e) {
                throw new RuntimeException("error manipulating soap messsage in handler", e);
            }
        }

        return true;
    }

    @Override
    public boolean handleFault(SOAPMessageContext context) {
        return true;
    }

    @Override
    public void close(MessageContext context) {
    }

    @Override
    public Set<QName> getHeaders() {
        return null;
    }
}
