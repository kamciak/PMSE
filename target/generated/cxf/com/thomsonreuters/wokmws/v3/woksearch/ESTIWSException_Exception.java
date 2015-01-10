
package com.thomsonreuters.wokmws.v3.woksearch;

import javax.xml.ws.WebFault;


/**
 * This class was generated by Apache CXF 2.7.10
 * 2015-01-06T13:28:02.601+01:00
 * Generated source version: 2.7.10
 */

@WebFault(name = "ESTIWSException", targetNamespace = "http://woksearch.v3.wokmws.thomsonreuters.com")
public class ESTIWSException_Exception extends Exception {
    
    private com.thomsonreuters.wokmws.v3.woksearch.ESTIWSException estiwsException;

    public ESTIWSException_Exception() {
        super();
    }
    
    public ESTIWSException_Exception(String message) {
        super(message);
    }
    
    public ESTIWSException_Exception(String message, Throwable cause) {
        super(message, cause);
    }

    public ESTIWSException_Exception(String message, com.thomsonreuters.wokmws.v3.woksearch.ESTIWSException estiwsException) {
        super(message);
        this.estiwsException = estiwsException;
    }

    public ESTIWSException_Exception(String message, com.thomsonreuters.wokmws.v3.woksearch.ESTIWSException estiwsException, Throwable cause) {
        super(message, cause);
        this.estiwsException = estiwsException;
    }

    public com.thomsonreuters.wokmws.v3.woksearch.ESTIWSException getFaultInfo() {
        return this.estiwsException;
    }
}
