package lou.alex.zoom.net;

import java.net.SocketAddress;

public interface ServiceLocator {
    void unregister(String service);
    void register(String service);

    /**
     * non-blocking call, if no service available return null
     */
    SocketAddress lookup(String service);

    void addListener(Listener lis);

    interface Listener {
        void onNew(ServiceInstance instance);
        void onRemove(ServiceInstance instance);
    }
}
