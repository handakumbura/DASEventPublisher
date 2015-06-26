package com.dumiduh.das;

import java.io.File;
import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.HttpTransportProperties;
import org.wso2.carbon.analytics.stream.persistence.EventStreamPersistenceAdminServiceEventStreamPersistenceAdminServiceExceptionException;
import org.wso2.carbon.analytics.stream.persistence.EventStreamPersistenceAdminServiceStub;
import org.wso2.carbon.databridge.agent.thrift.Agent;
import org.wso2.carbon.databridge.agent.thrift.DataPublisher;
import org.wso2.carbon.databridge.agent.thrift.conf.AgentConfiguration;
import org.wso2.carbon.databridge.agent.thrift.exception.AgentException;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.exception.AuthenticationException;
import org.wso2.carbon.databridge.commons.exception.TransportException;
import org.wso2.carbon.event.receiver.admin.EventReceiverAdminServiceStub;
import org.wso2.carbon.event.stream.admin.internal.EventStreamAdminServiceStub;

/**
 * Hello world!
 *
 */
public class App 
{
    private static String us;
    private static String pwd;
    private static String thriftURL;
    private static String streamName;
    private static String version;
    private static String STREAMADMINURL;
    private static String RECEIVERURL;
    private static String PERSISTURL;
    private static int eventCount;
    public static void main( String[] args ) throws Exception
    {
        if(args.length!=7)
        {
            throw new Exception("not all parameters required were provided.");
        }
        else
        {
        int thriftPort = 7611+Integer.parseInt(args[1]);
        int carbonHTTPSPort = 9443+Integer.parseInt(args[1]);
        eventCount = Integer.parseInt(args[6]);        
        thriftURL="tcp://"+args[0]+":"+thriftPort;
        us = args[2];
        pwd = args[3];
        streamName=args[4];
        version=args[5];     
        STREAMADMINURL= "https://"+args[0]+":"+carbonHTTPSPort+"/services/EventStreamAdminService";
        RECEIVERURL= "https://"+args[0]+":"+carbonHTTPSPort+"/services/EventReceiverAdminService";
        PERSISTURL="https://"+args[0]+":"+carbonHTTPSPort+"/services/EventStreamPersistenceAdminService";
        

        String trustStore = new File("").getAbsolutePath()+"/resources/client-truststore.jks";
        
        System.setProperty("javax.net.ssl.trustStore",  trustStore );

        System.setProperty("javax.net.ssl.trustStorePassword", "wso2carbon");
        
        createStreamDefinition();       
        persistStream();
        createReceiver();
        publishData();
        }
        
    }
    
    public static boolean createStreamDefinition()
    {
        boolean status = false;
        try {
            ConfigurationContext configContext = ConfigurationContextFactory.createConfigurationContextFromFileSystem( null, null);
      
            EventStreamAdminServiceStub stub = new EventStreamAdminServiceStub(configContext, STREAMADMINURL);
            //EventReceiverAdminServiceStub stub = new EventReceiverAdminServiceStub(configContext, URL);
            ServiceClient client = stub._getServiceClient();
            Options option = client.getOptions();
            
            option.setProperty(HTTPConstants.COOKIE_STRING, null);
            HttpTransportProperties.Authenticator auth = new HttpTransportProperties.Authenticator();
            auth.setUsername(us);
            auth.setPassword(pwd);
            auth.setPreemptiveAuthentication(true);
            option.setProperty(org.apache.axis2.transport.http.HTTPConstants.AUTHENTICATE, auth);
            option.setManageSession(true);
            
            EventStreamAdminServiceStub.AddEventStreamDefinitionAsDto addDto = new EventStreamAdminServiceStub.AddEventStreamDefinitionAsDto();
            EventStreamAdminServiceStub.EventStreamDefinitionDto dto =  new EventStreamAdminServiceStub.EventStreamDefinitionDto();
            dto.setName(streamName);
            dto.setVersion(version);
            dto.setDescription("desc");
            dto.setNickName("nick");
            
            EventStreamAdminServiceStub.EventStreamAttributeDto attribute1 = new EventStreamAdminServiceStub.EventStreamAttributeDto();
            attribute1.setAttributeName("entry");
            attribute1.setAttributeType("string");
            
            EventStreamAdminServiceStub.EventStreamAttributeDto[] payload = new EventStreamAdminServiceStub.EventStreamAttributeDto[]{attribute1};
            dto.setPayloadData(payload);
            
            addDto.setEventStreamDefinitionDto(dto);
            
            EventStreamAdminServiceStub.AddEventStreamDefinitionAsDtoResponse response = stub.addEventStreamDefinitionAsDto(addDto);
            status = response.get_return();
            System.out.println("putting thread to sleep for 10s to allow artifact deployment");
            Thread.sleep(10000);
            
        } catch (AxisFault ex) {
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
        } catch (RemoteException ex) {
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return status;
    }
    
    public static boolean createReceiver()
    {
        boolean status = false;
        ConfigurationContext configContext;
        try {
            configContext = ConfigurationContextFactory.createConfigurationContextFromFileSystem( null, null);
            EventReceiverAdminServiceStub stub = new EventReceiverAdminServiceStub(configContext, RECEIVERURL);
            ServiceClient client = stub._getServiceClient();
            Options option = client.getOptions();
            
            option.setProperty(HTTPConstants.COOKIE_STRING, null);
            HttpTransportProperties.Authenticator auth = new HttpTransportProperties.Authenticator();
            auth.setUsername(us);
            auth.setPassword(pwd);
            auth.setPreemptiveAuthentication(true);
            option.setProperty(org.apache.axis2.transport.http.HTTPConstants.AUTHENTICATE, auth);
            option.setManageSession(true);
            
            EventReceiverAdminServiceStub.DeployWso2EventReceiverConfiguration res = new EventReceiverAdminServiceStub.DeployWso2EventReceiverConfiguration();
            res.setEventAdapterType("wso2event");
            res.setStreamNameWithVersion(streamName+":"+version);
            res.setEventReceiverName(streamName+"adapter");
            res.setMappingEnabled(false);
            
            EventReceiverAdminServiceStub.DeployWso2EventReceiverConfigurationResponse resp = stub.deployWso2EventReceiverConfiguration(res);
            status = resp.get_return();
            System.out.println("putting thread to sleep for 10s to allow artifact deployment");
            Thread.sleep(10000);
        } catch (AxisFault ex) {
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
        } catch (RemoteException ex) {
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
        }
      
            
            
        return status;
    }
    
    public static void persistStream()
    {
      
      ConfigurationContext configContext;
        try {
            configContext = ConfigurationContextFactory.createConfigurationContextFromFileSystem( null, null);
            EventStreamPersistenceAdminServiceStub stub = new EventStreamPersistenceAdminServiceStub(configContext,PERSISTURL);
            ServiceClient client = stub._getServiceClient();
            Options option = client.getOptions();
            
            
            option.setProperty(HTTPConstants.COOKIE_STRING, null);
            HttpTransportProperties.Authenticator auth = new HttpTransportProperties.Authenticator();
            auth.setUsername(us);
            auth.setPassword(pwd);
            auth.setPreemptiveAuthentication(true);
            option.setProperty(org.apache.axis2.transport.http.HTTPConstants.AUTHENTICATE, auth);
            option.setManageSession(true);
            
            EventStreamPersistenceAdminServiceStub.AnalyticsTable table = new EventStreamPersistenceAdminServiceStub.AnalyticsTable();
            EventStreamPersistenceAdminServiceStub.AnalyticsTableRecord rec1 = new EventStreamPersistenceAdminServiceStub.AnalyticsTableRecord();
            rec1.setColumnName("entry");
            rec1.setColumnType("STRING");
            rec1.setPersist(true);
            table.addAnalyticsTableRecords(rec1);
            table.setStreamVersion(version);
            table.setTableName(streamName);
            table.setPersist(true);

            EventStreamPersistenceAdminServiceStub.AddAnalyticsTable add = new EventStreamPersistenceAdminServiceStub.AddAnalyticsTable();
            add.setAnalyticsTable(table);
            stub.addAnalyticsTable(add);
            System.out.println("putting thread to sleep for 10s to allow artifact deployment");
            Thread.sleep(10000);
            

        } catch (AxisFault ex) {
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
        } catch (RemoteException ex) {
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
        } catch (EventStreamPersistenceAdminServiceEventStreamPersistenceAdminServiceExceptionException ex) {
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
        }
      
      
      
    }

   public static void publishData()
    {
        AgentConfiguration agentConfiguration = new AgentConfiguration();
        Agent agent = new Agent(agentConfiguration);
        
         try {
            DataPublisher dataPublisher = new DataPublisher(thriftURL, us, pwd, agent);
            System.out.println("Sending data...");
            Event event;
            
            for(int x=0;x<eventCount;x++) {
                event = new Event(streamName+":"+version, System.currentTimeMillis(),
                        null, null, new Object[] { "val " + Math.random() });
                dataPublisher.publish(event);
                
            }
            //increase sleep time if event count is large
            Thread.sleep(10000);
            dataPublisher.stop();
            agent.shutdown();
            
        } catch (AgentException e) {
             e.printStackTrace();
        } catch (InterruptedException ex) {
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MalformedURLException ex) {
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
        } catch (AuthenticationException ex) {
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
        } catch (TransportException ex) {
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
}
