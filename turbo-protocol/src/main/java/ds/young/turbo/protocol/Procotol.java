package ds.young.turbo.protocol;

import ds.young.turbo.common.TurboRequest;
import ds.young.turbo.common.URL;

public interface Procotol {

    void start(URL url);
    Object send(URL url, TurboRequest invocation);
}
