package com.tle.webtests.framework;

import java.lang.reflect.Method;
import java.util.List;
import javax.xml.namespace.QName;
import org.apache.cxf.aegis.databinding.AegisDatabinding;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.frontend.ClientProxyFactoryBean;
import org.apache.cxf.message.Message;
import org.apache.cxf.service.model.OperationInfo;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.apache.cxf.wsdl.service.factory.AbstractServiceConfiguration;

public class SoapHelper {
  private final PageContext context;

  public SoapHelper(PageContext context) {
    this.context = context;
  }

  @SuppressWarnings("unchecked")
  public <T> T createSoap(
      Class<T> serviceClass, String path, String namespace, Object previousSession) {
    String baseUrl = context.getBaseUrl();
    ClientProxyFactoryBean factory = new ClientProxyFactoryBean();
    factory.setServiceClass(serviceClass);
    factory.setServiceName(new QName(namespace, serviceClass.getSimpleName()));
    factory.setAddress(baseUrl + path);
    List<AbstractServiceConfiguration> configs =
        factory.getServiceFactory().getServiceConfigurations();
    configs.add(0, new XFireReturnTypeConfig());
    factory.setDataBinding(new AegisDatabinding());
    T service = (T) factory.create();
    Client client = ClientProxy.getClient(service);
    client.getRequestContext().put(Message.MAINTAIN_SESSION, true);
    HTTPClientPolicy policy = new HTTPClientPolicy();
    policy.setReceiveTimeout(600000);
    policy.setAllowChunking(false);
    HTTPConduit conduit = (HTTPConduit) client.getConduit();
    TestConfig testConfig = context.getTestConfig();
    String proxyHost = testConfig.getProperty("proxy.host");
    if (proxyHost != null) {
      policy.setProxyServer(proxyHost);
      policy.setProxyServerPort(testConfig.getIntProperty("proxy.port", 8888));
    }
    conduit.setClient(policy);
    if (previousSession != null) {
      copyCookiesInt(conduit, previousSession);
    }
    return service;
  }

  public void clearCookies(Object service) {
    ((HTTPConduit) ClientProxy.getClient(service).getConduit()).getCookies().clear();
  }

  public void copyCookies(Object from, Object to) {
    copyCookiesInt((HTTPConduit) ClientProxy.getClient(to).getConduit(), from);
  }

  private void copyCookiesInt(HTTPConduit conduit, Object previousSession) {
    Client sessionClient = ClientProxy.getClient(previousSession);
    HTTPConduit srcConduit = (HTTPConduit) sessionClient.getConduit();
    conduit.getCookies().putAll(srcConduit.getCookies());
  }

  public static class XFireReturnTypeConfig extends AbstractServiceConfiguration {
    @Override
    public QName getInParameterName(OperationInfo op, Method method, int paramNumber) {
      return new QName(op.getName().getNamespaceURI(), "in" + paramNumber);
    }

    @Override
    public QName getOutParameterName(OperationInfo op, Method method, int paramNumber) {
      return new QName(op.getName().getNamespaceURI(), "out");
    }
  }
}
