package lou.alex.zoom.net;

import java.util.Objects;

public final class ServiceInstance {
    private final String serviceName;
    private final String serviceInstance;
    private final String env;
    private final String host;
    private final int port;

    public ServiceInstance(final String serviceName, final String serviceInstance, final String env, final String host, final int port) {
        Objects.requireNonNull(serviceName, "serviceName cannot be null");
        Objects.requireNonNull(serviceInstance, "serviceInstance cannot be null");
        Objects.requireNonNull(host, "host cannot be null");
        Objects.requireNonNull(env, "env cannot be null");

        this.serviceName = serviceName;
        this.serviceInstance = serviceInstance;
        this.env = env;
        this.host = host;
        this.port = port;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ServiceInstance that = (ServiceInstance) o;

        if (port != that.port) return false;
        if (serviceName != null ? !serviceName.equals(that.serviceName) : that.serviceName != null) return false;
        if (serviceInstance != null ? !serviceInstance.equals(that.serviceInstance) : that.serviceInstance != null)
            return false;
        if (env != null ? !env.equals(that.env) : that.env != null) return false;
        return host != null ? host.equals(that.host) : that.host == null;

    }

    @Override
    public int hashCode() {
        int result = serviceName != null ? serviceName.hashCode() : 0;
        result = 31 * result + (serviceInstance != null ? serviceInstance.hashCode() : 0);
        result = 31 * result + (env != null ? env.hashCode() : 0);
        result = 31 * result + (host != null ? host.hashCode() : 0);
        result = 31 * result + port;
        return result;
    }
}
