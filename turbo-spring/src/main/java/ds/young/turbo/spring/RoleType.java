package ds.young.turbo.spring;

/**
 * @program: turbo
 * @description
 * @author: admin
 * @create: 2020-06-10 17:00
 **/
public enum RoleType {
    PROVIDER("1"),
    CONSUMER("2");

    private String code;

    private RoleType(String code){
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
