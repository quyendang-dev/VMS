package com.cms.base.servicecaller;

import com.opensymphony.xwork2.util.logging.Logger;
import com.opensymphony.xwork2.util.logging.LoggerFactory;
import org.apache.http.Consts;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CookieStore;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.*;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.CodingErrorAction;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by thiendn1 on 1/5/2015.
 */
public class WebServiceCaller {

    protected final int DEFAULT_TIME_OUT = 10;
    protected final int DEFAULT_KEEP_ALIVE = 1;
    protected final WebServiceSchema webServiceSchema = new WebServiceSchema();
    protected int timeCheckConnection = 10000;//s
    protected int timeOutConnection = 10000; //s
    protected int keepAlive = 1;
    Logger logger = LoggerFactory.getLogger(WebServiceCaller.class);

    protected final CookieStore cookieStore;
    protected final CredentialsProvider credentialsProvider;
    protected final PoolingHttpClientConnectionManager connManager;

    public WebServiceCaller() {
        KeyStore trustStore = null;
        try {
            trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null, null);
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Trust own CA and all self-signed certs
        SSLContext sslcontext = null;
        try {
            sslcontext = SSLContexts.custom()
                    .loadTrustMaterial(trustStore, new TrustSelfSignedStrategy())
                    .build();
            // Allow TLSv1 protocol only
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
                sslcontext, SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
//        // SSL context for secure connections can be created either based on
//        // system or application specific properties.
////        SSLContext sslcontext = SSLContexts.createSystemDefault();
//        // Use custom hostname verifier to customize SSL hostname verification.
//        X509HostnameVerifier hostnameVerifier = new AllowAllHostnameVerifier();
//        // Create a registry of custom connection socket factories for supported
//        // protocol schemes.
        Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.INSTANCE)
                .register("https", sslsf)
                .build();
        // Create a connection manager with custom configuration.
        connManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
        // Create socket configuration
        SocketConfig socketConfig = SocketConfig.custom()
                .setTcpNoDelay(true)
                .build();
        // Configure the connection manager to use socket configuration either
        // by default or for a specific host.
        connManager.setDefaultSocketConfig(socketConfig);
        // Create message constraints
        MessageConstraints messageConstraints = MessageConstraints.custom()
                .setMaxHeaderCount(200)
                .setMaxLineLength(2000)
                .build();

        // Create connection configuration
        ConnectionConfig connectionConfig = ConnectionConfig.custom()
                .setMalformedInputAction(CodingErrorAction.IGNORE)
                .setUnmappableInputAction(CodingErrorAction.IGNORE)
                .setCharset(Consts.UTF_8)
                .setMessageConstraints(messageConstraints)
                .build();
        // Configure the connection manager to use connection configuration either
        // by default or for a specific host.
        connManager.setDefaultConnectionConfig(connectionConfig);
        // Configure total max or per route limits for persistent connections
        // that can be kept in the pool or leased by the connection manager.
        connManager.setMaxTotal(100);
        connManager.setDefaultMaxPerRoute(10);

        // Use custom cookie store if necessary.
        cookieStore = new BasicCookieStore();
        // Use custom credentials provider if necessary.
        credentialsProvider = new BasicCredentialsProvider();

    }


    public String webServiceCaller(String wsUrl,
                                   String wsOperator, String wsTargetNamespace, List<Object> wsParameters,
                                   String username, String password) throws Exception {
        WsRequestCreator wsConfig = new WsRequestCreator(wsUrl, wsTargetNamespace, wsOperator, username, password);
        wsConfig.setCloseOnResponse(false);
        wsConfig.setBodyParameters(wsParameters);
        return webServiceCaller(wsConfig);
    }

    public String webServiceCaller(String wsUrl,
                                   String wsOperator, String wsTargetNamespace, String parameters, String username, String password) throws IOException, WSException, HttpException, Exception {
        WsRequestCreator wsConfig = new WsRequestCreator(wsUrl, wsTargetNamespace, wsOperator, username, password);
        wsConfig.setCloseOnResponse(false);
        wsConfig.setBodyParameterTxt(parameters);
        return webServiceCaller(wsConfig);
    }


    public String webServiceCaller(String wsUrl,
                                   String wsOperator, String wsTargetNamespace, String wsParameters,
                                   String header) throws Exception {
        WsRequestCreator wsConfig = new WsRequestCreator(wsUrl, wsTargetNamespace, wsOperator);
        wsConfig.setCloseOnResponse(false);
        wsConfig.setBodyParameterTxt(wsParameters);
        wsConfig.setHeaderParameterTxt(header);
        return webServiceCaller(wsConfig);

    }

    public String webServiceCaller(String wsUrl,
                                   String wsOperator, String wsTargetNamespace,
                                   List<Object> wsParameters, String header) throws Exception {
        WsRequestCreator wsConfig = new WsRequestCreator(wsUrl, wsTargetNamespace, wsOperator);
        wsConfig.setCloseOnResponse(false);
        wsConfig.setBodyParameters(wsParameters);
        wsConfig.setHeaderParameterTxt(header);
        return webServiceCaller(wsConfig);
    }


    public String webServiceCaller(WsRequestCreator wsConfig) throws Exception {
        validateWs(wsConfig);
        return soapSecurityServiceCallerByConfig(wsConfig);

    }

    public String webServiceCaller(WsRequestCreator wsConfig, String header) throws Exception {
        validateWs(wsConfig);
        return soapSecurityServiceCallerByConfig(wsConfig, header);

    }


    /*
     System.out.println("protocol = " + aURL.getProtocol());
     System.out.println("authority = " + aURL.getAuthority());
     System.out.println("host = " + aURL.getHost());
     System.out.println("port = " + aURL.getPort());
     System.out.println("path = " + aURL.getPath());
     System.out.println("query = " + aURL.getQuery());
     System.out.println("filename = " + aURL.getFile());
     System.out.println("ref = " + aURL.getRef()); */
    public String execute(String requestTxt, WsRequestCreator wsConfig)
            throws Exception {
        String urlAddress = wsConfig.getWsAddress();
        boolean closeOnResponse = wsConfig.isCloseOnResponse();
        String username = wsConfig.getUsername();
        String password = wsConfig.getPassword();

        URL aURL = new URL(urlAddress);
        int timeOutConnection = this.timeOutConnection;
        int keepAlive = this.keepAlive;
        this.timeOutConnection = DEFAULT_TIME_OUT;
        this.keepAlive = DEFAULT_KEEP_ALIVE;
        CloseableHttpClient httpClient = null;

        CredentialsProvider credsProvider = null;
        if (username != null || password != null) {
            credsProvider = new BasicCredentialsProvider();
            credsProvider.setCredentials(
                    new AuthScope(aURL.getHost(), aURL.getPort()),
                    new UsernamePasswordCredentials(username, password));
        }
        // Create global request configuration
        RequestConfig defaultRequestConfig = RequestConfig.custom()
//                .setCookieSpec(CookieSpecs.BEST_MATCH)
//                .setExpectContinueEnabled(true)
//                .setStaleConnectionCheckEnabled(true)
//                .setTargetPreferredAuthSchemes(Arrays.asList(AuthSchemes.NTLM, AuthSchemes.DIGEST))
//                .setProxyPreferredAuthSchemes(Arrays.asList(AuthSchemes.BASIC))
                .setConnectTimeout(timeOutConnection)
                .build();
        // Create an HttpClient with the given custom dependencies and configuration.
        if (credsProvider == null) {
            httpClient = HttpClients.custom()
                    .setConnectionManager(connManager)
                    .setDefaultCookieStore(cookieStore)
                    .setDefaultCredentialsProvider(credentialsProvider)
                    .setDefaultRequestConfig(defaultRequestConfig)
                    .build();
        } else {
            httpClient = HttpClients.custom()
                    .setConnectionManager(connManager)
                    .setDefaultCookieStore(cookieStore)
                    .setDefaultCredentialsProvider(credsProvider)
                    .setDefaultRequestConfig(defaultRequestConfig)
                    .build();
        }

        HttpPost httpPost = new HttpPost(urlAddress);
        StringEntity reqEntity = new StringEntity(requestTxt, "UTF-8");
        httpPost.setEntity(reqEntity);
        // get response
        HttpResponse httpResponse = httpClient.execute(httpPost);
        int statusCode = httpResponse.getStatusLine().getStatusCode();
        String response = EntityUtils.toString(httpResponse.getEntity());
        if (WebServiceHandler.showResponse) {
            logger.info(XmlSchema.formatXML(response));
        }
        if (statusCode != 200) {
            Document doc = XmlSchema.parseXmlFile(response);
            NodeList nList = doc.getElementsByTagName("soap:Fault");
            Element eElement = (Element) nList.item(0);
            throw new WSException(eElement.getTextContent());

        }
        if (closeOnResponse) {
            httpClient.close();
        }
        return response;

    }

    public String soapSecurityServiceCallerByConfig(WsRequestCreator wsConfig) throws Exception {
        wsConfig.setBodyParameterTxt(XmlSchema.formatParameters(wsConfig));
        wsConfig.setHeaderParameterTxt(XmlSchema.formatHeaderParameters(wsConfig));
        String request = webServiceSchema.parseParameters(wsConfig);
        System.out.println(XmlSchema.formatXML(request));
        return execute(request, wsConfig);
    }

    public String soapSecurityServiceCallerByConfig(WsRequestCreator wsConfig, String header) throws Exception {
        wsConfig.setBodyParameterTxt(XmlSchema.formatParameters(wsConfig));
        wsConfig.setHeaderParameterTxt(header);
        String request = webServiceSchema.parseParameters(wsConfig);
        return execute(request, wsConfig);
    }

    public void setTimeOutConnection(int timeOutConnection) {
        this.timeOutConnection = timeOutConnection;
    }

    public void setKeepAlive(int keepAlive) {
        this.keepAlive = keepAlive;
    }

    private void validateWs(WsRequestCreator wsConfig) throws Exception {
        if (wsConfig.getWsAddress() == null) {
            throw new Exception("WebService caller has no address");
        }
//        if (wsConfig.getTargetNameSpace() == null) {
//            throw new Exception("WebService caller has no targetNameSpace");
//        }
        if (wsConfig.getServiceName() == null) {
            throw new Exception("WebService caller has no method");
        }
        if (wsConfig.getBodyParameters() != null && wsConfig.getBodyParameterTxt() != null) {
            throw new Exception("WebService caller does not allowed to have both parameter list and text");
        }
    }

    /**
     * @param wsUrl
     * @param wsOperator
     * @param wsTargetNamespace
     * @param wsParameters
     * @param header
     * @return
     * @throws Exception
     * @author NamDX
     */
    @Deprecated
    public String webServiceRequest(String wsUrl,
                                    String wsOperator, String wsTargetNamespace, String wsParameters,
                                    String header) throws Exception {
        WsRequestCreator wsConfig = new WsRequestCreator(wsUrl, wsTargetNamespace, wsOperator);
        wsConfig.setBodyParameterTxt(wsParameters);
        wsConfig.setHeaderParameterTxt(header);
        return webServiceSchema.getSoapRequest(wsConfig);
    }


    @Deprecated
    public String webServiceRequest(String wsUrl,
                                    String wsOperator, String wsTargetNamespace, List<Object> wsParameters,
                                    String username, String password) throws Exception {
        WsRequestCreator wsConfig = new WsRequestCreator(wsUrl, wsTargetNamespace, wsOperator);
        wsConfig.setBodyParameterTxt(XmlSchema.formatParameters(wsParameters));
        return webServiceSchema.getSoapRequest(wsConfig);
    }

    public String webServiceRequest(WsRequestCreator wsConfig) throws Exception {
        return webServiceSchema.getSoapRequest(wsConfig);
    }

    public static void main(String[] args) throws Exception {


//        WsConfig wsConfig = new WsConfig();
//        wsConfig.setWsAddress("http://10.60.19.23:9898/wsocs");
//        wsConfig.setServiceName("Activation");
//        List<String> alias = new ArrayList<>();
//        alias.add("MSISDN");
//        alias.add("logFileName");
//        alias.add("PartyCode");
//        new WebServiceCaller().webServiceCaller(wsConfig);

    	

        WsRequestCreator wsConfig = new WsRequestCreator();
        wsConfig.setWsAddress("http://10.58.71.40:8280/services/billingService?wsdl");
        wsConfig.setServiceName("findCustomerPaging");
        wsConfig.setTargetNameSpace("http://ws.viettel.com/");

        List<Object> objects = new ArrayList<>();
        objects.add(5);
        objects.add(5);
        wsConfig.setBodyParameters(objects);
//        Map<String,Object> alias = new HashMap<>();
//        alias.put("arg2","Problem");
//        wsConfig.setBodyParameters(noAlias);
//        wsConfig.setBodyArgAlias(alias);
        System.out.println(WebServiceHandler.webServiceCaller(wsConfig));

//        WebServiceHandler.webServiceCaller("http://10.58.71.129:8007/OrderExplorer/bpm-service/orderService?wsdl",
//                "getOrderTypes", "http://server.activiti.bpm.viettel.com/", new ArrayList<Object>(), null, null);
//


////
//            List<Object> list = new ArrayList<>();
//            list.add(34201);
//            WebServiceHandler.webServiceCaller("http://10.58.71.129:9020/SaleWebservice/bpm/saleService",
//                    "findSub", "http://webservice.sale.viettel.com/", list, null, null);
//
//
//        }
//        WebServiceHandler.webServiceCaller("http://10.58.71.129:8007/OrderExplorer/bpm-service/orderService?wsdl",
//                "parseString", "http://server.activiti.bpm.viettel.com/", new ArrayList<Object>(), null, null);
//
//
// create SOAP

//
//        //username
//        SOAPElement nonce = usernameToken.addChildElement(new QName("Nonce"));
////        nonce.setPrefix(WSSE_PREDIX);
//        nonce.addTextNode(SecurityHelper.generateNonce());
//
//        //username
//        SOAPElement created = usernameToken.addChildElement(new QName("Created"));
////        created.setPrefix(WSSE_PREDIX);
//        created.addTextNode(SecurityHelper.generateTimestamp());
    }

}
