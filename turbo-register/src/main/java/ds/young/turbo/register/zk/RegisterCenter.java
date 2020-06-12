package ds.young.turbo.register.zk;

import ds.young.turbo.common.RegisterInfoConsumer;
import ds.young.turbo.common.RegisterInfoProvider;
import ds.young.turbo.register.RegisterCenter4Consumer;
import ds.young.turbo.register.RegisterCenter4Provider;
import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.serialize.SerializableSerializer;
import org.apache.zookeeper.common.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @program: turbo-netty
 * @description
 * @author: zhangds
 * <p>
 * 有道无术，术尚可求，有术无道，止于术
 * @create: 2020-05-27 17:43
 **/
public class RegisterCenter implements RegisterCenter4Consumer, RegisterCenter4Provider{

    private static final Logger logger = LoggerFactory.getLogger(RegisterCenter.class);

    private static RegisterCenter registerCenter = new RegisterCenter();

    private RegisterCenter(){};

    public static RegisterCenter getInstance(String zkServer){
        ZK_SERVER = zkServer;
        return registerCenter;
    }
    //服务提供者列表，key：服务提供者接口，value：服务提供者服务方法列表
    private static final Map<String,List<RegisterInfoProvider>> providerServiceMap = new ConcurrentHashMap();

    //服务端 zookeeper 元信息，选择服务（第一次从zookeeper 拉取，后续由zookeeper监听机制主动更新）
    private static final Map<String,List<RegisterInfoProvider>> serviceData4Consumer = new ConcurrentHashMap();

    //从配置文件中获取 zookeeper 服务地址列表
    private static String  ZK_SERVER;

    //从配置文件中获取 zookeeper 会话超时时间配置
    private static int ZK_SESSION_TIME_OUT = 5000;

    //从配置文件中获取 zookeeper 连接超时事件配置
    private static int  ZK_CONNECTION_TIME_OUT = 5000;

    private static String ROOT_PATH = "/turbo-register";
    public  static String PROVIDER_TYPE = "/provider";
    public  static String CONSUMER_TYPE = "/consumer";

    private static volatile ZkClient zkClient = null;

    @Override
    public void initProviderMap() {
        if(serviceData4Consumer.isEmpty()){
            serviceData4Consumer.putAll(fetchOrUpdateServiceMetaData());
        }

    }

    /**
     * 消费端获取服务提供者信息
     *
     * @return
     */
    @Override
    public Map<String, List<RegisterInfoProvider>> getServiceMetaDataMap4Consumer() {
        return serviceData4Consumer;
    }


    @Override
    public void registerConsumer(List<RegisterInfoConsumer> consumers) {
        if(consumers == null || consumers.size() == 0){
            return;
        }

        //连接 zookeeper ，注册服务
        synchronized (RegisterCenter.class){
            if(zkClient == null){
                zkClient = new ZkClient(ZK_SERVER,ZK_SESSION_TIME_OUT,ZK_CONNECTION_TIME_OUT, new SerializableSerializer());
            }
            //创建  zookeeper 命名空间
            boolean exist = zkClient.exists(ROOT_PATH);
            if(!exist){
                zkClient.createPersistent(ROOT_PATH,true);
            }
            //创建服务提供者节点
            exist = zkClient.exists((ROOT_PATH));
            if(!exist){
                zkClient.createPersistent(ROOT_PATH);
            }

            for(int i = 0; i< consumers.size();i++) {
                RegisterInfoConsumer consumer = consumers.get(i);
                //创建服务消费者节点
                String serviceNode = consumer.getConsumer().getName();
                String servicePath = ROOT_PATH + CONSUMER_TYPE + "/" + serviceNode;

                exist = zkClient.exists(servicePath);
                if (!exist) {
                    zkClient.createPersistent(servicePath, true);
                }

                //创建当前服务器节点
                InetAddress addr = null;
                try {
                    addr = InetAddress.getLocalHost();
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }
                String ip = addr.getHostAddress();
                String currentServiceIpNode = servicePath + "/" + ip;
                exist = zkClient.exists(currentServiceIpNode);
                if (!exist) {
                    zkClient.createEphemeral(currentServiceIpNode);
                }
            }
        }
    }

    @Override
    public void registerProvider(List<RegisterInfoProvider> serivceList) {
        if(serivceList == null || serivceList.size() == 0){
            return;
        }

        //连接 zookeeper，注册服务,加锁，将所有需要注册的服务放到providerServiceMap里面
        synchronized (RegisterCenter.class){
            for(RegisterInfoProvider provider:serivceList){
                //获取接口名称
                String serviceItfKey = provider.getProvider().getName();
                //先从当前服务提供者的集合里面获取
                List<RegisterInfoProvider> providers = providerServiceMap.get(serviceItfKey);
                if(providers == null){
                    providers = new ArrayList();
                }
                providers.add(provider);
                providerServiceMap.put(serviceItfKey,providers);
            }

            if(zkClient == null){
                zkClient = new ZkClient(ZK_SERVER,ZK_SESSION_TIME_OUT,ZK_CONNECTION_TIME_OUT,new SerializableSerializer());
            }

            //创建当前应用 zookeeper 命名空间
            boolean exist = zkClient.exists(ROOT_PATH);
            if(!exist){
                zkClient.createPersistent(ROOT_PATH,true);
            }

            //服务提供者节点
            exist = zkClient.exists((ROOT_PATH));
            if(!exist){
                zkClient.createPersistent(ROOT_PATH);
            }

            for(Map.Entry<String,List<RegisterInfoProvider>> entry:providerServiceMap.entrySet()){
                //创建服务提供者节点
                String serviceNode = entry.getKey();
                String servicePath = ROOT_PATH +PROVIDER_TYPE +"/" + serviceNode;
                exist = zkClient.exists(servicePath);
                if(!exist){
                    zkClient.createPersistent(servicePath,true);
                }

                //创建当前服务器节点，这里是注册时使用，一个接口对应的ServiceProvider 只有一个
                int serverPort = entry.getValue().get(0).getPort();
                InetAddress addr = null;
                try {
                    addr = InetAddress.getLocalHost();
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }
                String ip = addr.getHostAddress();
                String impl = (String)entry.getValue().get(0).getServiceObject();
                String serviceIpNode = servicePath +"/" + ip + "|" + serverPort + "|" + impl;
                exist = zkClient.exists(serviceIpNode);
                if(!exist){
                    //创建临时节点
                    zkClient.createEphemeral(serviceIpNode);
                    logger.info("[ TURBO ] zk服务已注册，服务名：{}", serviceIpNode);
                }
                //监听注册服务的变化，同时更新数据到本地缓存
                zkClient.subscribeChildChanges(servicePath, new IZkChildListener() {
                    @Override
                    public void handleChildChange(String s, List<String> list) throws Exception {
                        if(list  == null){
                            list = new ArrayList();
                        }
                        //存活的服务 IP 列表
                        List<String> activeServiceIpList = new ArrayList();
                        for(String input:list){
                            String ip = StringUtils.split(input, "|").get(0);
                            activeServiceIpList.add(ip);
                        }
                        refreshActivityService(activeServiceIpList);
                    }
                });
            }
        }
    }

    /**
     *
     * 在某个服务端获取自己暴露的服务
     */
    @Override
    public Map<String, List<RegisterInfoProvider>> getProviderService() {
        return providerServiceMap;
    }


    //利用ZK自动刷新当前存活的服务提供者列表数据
    private void refreshActivityService(List<String> serviceIpList) {
        if (serviceIpList == null||serviceIpList.isEmpty()) {
            serviceIpList = new ArrayList();
        }

        Map<String, List<RegisterInfoProvider>> currentServiceMetaDataMap = new HashMap();
        for (Map.Entry<String, List<RegisterInfoProvider>> entry : providerServiceMap.entrySet()) {
            String key = entry.getKey();
            List<RegisterInfoProvider> providerServices = entry.getValue();

            List<RegisterInfoProvider> serviceMetaDataModelList = currentServiceMetaDataMap.get(key);
            if (serviceMetaDataModelList == null) {
                serviceMetaDataModelList = new ArrayList();
            }

            for (RegisterInfoProvider serviceMetaData : providerServices) {
                if (serviceIpList.contains(serviceMetaData.getIp())) {
                    serviceMetaDataModelList.add(serviceMetaData);
                }
            }
            currentServiceMetaDataMap.put(key, serviceMetaDataModelList);
        }
        providerServiceMap.clear();
        providerServiceMap.putAll(currentServiceMetaDataMap);
    }


    private void refreshServiceMetaDataMap(List<String> serviceIpList) {
        if (serviceIpList == null) {
            serviceIpList = new ArrayList();
        }

        Map<String, List<RegisterInfoProvider>> currentServiceMetaDataMap = new HashMap();
        for (Map.Entry<String, List<RegisterInfoProvider>> entry : serviceData4Consumer.entrySet()) {
            String serviceItfKey = entry.getKey();
            List<RegisterInfoProvider> serviceList = entry.getValue();

            List<RegisterInfoProvider> providerServiceList = currentServiceMetaDataMap.get(serviceItfKey);
            if (providerServiceList == null) {
                providerServiceList = new ArrayList();
            }

            for (RegisterInfoProvider serviceMetaData : serviceList) {
                if (serviceIpList.contains(serviceMetaData.getIp())) {
                    providerServiceList.add(serviceMetaData);
                }
            }
            currentServiceMetaDataMap.put(serviceItfKey, providerServiceList);
        }

        serviceData4Consumer.clear();
        serviceData4Consumer.putAll(currentServiceMetaDataMap);
    }


    private Map<String, List<RegisterInfoProvider>> fetchOrUpdateServiceMetaData() {

        final Map<String, List<RegisterInfoProvider>> providerServiceMap = new ConcurrentHashMap();
        //连接zk
        synchronized (RegisterCenter.class) {
            if (zkClient == null) {
                zkClient = new ZkClient(ZK_SERVER, ZK_SESSION_TIME_OUT, ZK_CONNECTION_TIME_OUT, new SerializableSerializer());
            }
        }

        //从ZK获取服务提供者列表
        String providePath = ROOT_PATH + PROVIDER_TYPE;
        logger.info("[ TURBO ] 服务提供者地址 :"+providePath);
        List<String> providerServices = zkClient.getChildren(providePath);
        logger.info(providerServices.toString());
        for (String serviceName : providerServices) {
            String servicePath = providePath +"/"+ serviceName;
            logger.info("[ TURBO ] 服务地址 :"+servicePath);
            List<String> ipPathList = zkClient.getChildren(servicePath);
            logger.info("[ TURBO ] IP地址集合 :"+ipPathList.toString());
            for (String ipPath : ipPathList) {
                String serverIp = ipPath.split("\\|")[0];
                String serverPort = ipPath.split("\\|")[1];
                String impl = ipPath.split("\\|")[2];
                List<RegisterInfoProvider> providerServiceList = providerServiceMap.get(serviceName);
                if (providerServiceList == null) {
                    providerServiceList = new ArrayList();
                }
                RegisterInfoProvider providerService = new RegisterInfoProvider();

                try {
                    Class clazz = Class.forName(serviceName);
                    providerService.setProvider(clazz);
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }

                providerService.setIp(serverIp);
                providerService.setPort(Integer.parseInt(serverPort));
                providerService.setServiceObject(impl);
                providerService.setGroupName("");
                providerServiceList.add(providerService);

                providerServiceMap.put(serviceName, providerServiceList);
            }

            //监听注册服务的变化,同时更新数据到本地缓存
            zkClient.subscribeChildChanges(servicePath, new IZkChildListener() {
                @Override
                public void handleChildChange(String parentPath, List<String> currentChilds) throws Exception {
                    if (currentChilds == null) {
                        currentChilds = new ArrayList();
                    }
                    List<String> activeServiceIpList = new ArrayList();
                    for(String input:currentChilds){
                        String ip = StringUtils.split(input, "|").get(0);
                        activeServiceIpList.add(ip);
                    }
                    refreshServiceMetaDataMap(activeServiceIpList);
                }
            });
        }
        return providerServiceMap;
    }
}
