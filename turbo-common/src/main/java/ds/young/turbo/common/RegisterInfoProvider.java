package ds.young.turbo.common;

import java.io.Serializable;
import java.lang.reflect.Method;


public class RegisterInfoProvider implements Serializable{
	
	private Class<?> provider;
	private transient Object impl;
	private transient Method serviceMethod;
	private String ip;
	private int port;
	private long timeout;
	//服务唯一标识
	private String applicationName;
	//服务分组名称
	private String groupName;

	public Class<?> getProvider() {
		return provider;
	}

	public void setProvider(Class<?> provider) {
		this.provider = provider;
	}

	public Object getServiceObject() {
		return impl;
	}

	public void setServiceObject(Object serviceObject) {
		this.impl = serviceObject;
	}

	public Method getServiceMethod() {
		return serviceMethod;
	}

	public void setServiceMethod(Method serviceMethod) {
		this.serviceMethod = serviceMethod;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public long getTimeout() {
		return timeout;
	}

	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}

	public String getApplicationName() {
		return applicationName;
	}

	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}
}
