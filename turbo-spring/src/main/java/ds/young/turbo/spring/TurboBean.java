package ds.young.turbo.spring;

/**
 * @author ：zhangds5
 * @date ：Created in 2020/5/24 21:07
 * @description：
 * @modified By：
 * @version: $
 */

public class TurboBean {
    private String id;
    private String serviceInterface;
    private String ref;
    private String version;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getServiceInterface() {
        return serviceInterface;
    }

    public void setServiceInterface(String serviceInterface) {
        this.serviceInterface = serviceInterface;
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
