package lou.alex.zoom.net;

import lou.alex.zoom.logging.LoggerFactory;
import org.slf4j.Logger;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayDeque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class IOEngine {
    private static final Logger LOG = LoggerFactory.contextLogger();

    protected static final byte[] HEARTBEAT = new byte[]{1, 1, 1, 1};
    protected static final int DEFAULT_BUFFER_SIZE = 2 * 1000 * 1024;

    protected final ServiceLocator locator;
    protected final long heartbeatInterval;
    protected final long inactivityTimeout;
    protected final ConnectionListener connectionListener;

    protected SocketAddress address;
    protected boolean started;
    protected Selector selector;
    protected Map<String, Object> preferences = new ConcurrentHashMap<String, Object>(){{
        put("SO_RCV", DEFAULT_BUFFER_SIZE);
        put("SO_SND", DEFAULT_BUFFER_SIZE);
    }};


    public IOEngine(final SocketAddress address, final long heartbeatInterval, final long inactivityTimeout, ConnectionListener connectionListener) {
        this(new ServiceLocator() {
            @Override
            public void unregister(String service) {}

            @Override
            public void register(String service) {}

            @Override
            public SocketAddress lookup(String service) {
                return address;
            }

            @Override
            public void addListener(Listener lis) {}
        }, null, heartbeatInterval, inactivityTimeout, connectionListener);

        this.address = address;

    }

    public IOEngine(ServiceLocator locator, String service, long heartbeatInterval, long inactivityTimeout, ConnectionListener connectionListener) {
        this.locator = locator;
        this.heartbeatInterval = heartbeatInterval;
        this.inactivityTimeout = inactivityTimeout;
        this.connectionListener = connectionListener;
        this.address = locator.lookup(service);
    }

    public synchronized SocketAddress start() throws IOException {
        if (started) {
            throw new IllegalStateException("The service has already been started");
        }

        selector = Selector.open();


        SocketAddress actualAddress = startConnection();
        started = true;
        return actualAddress;

    }

    protected abstract SocketAddress startConnection() throws IOException;

    public synchronized void shutDown() throws IOException {
        if (!started) {
            throw new IllegalStateException("The service has never been started");
        }

        LOG.info("Shutting down engine");

        try {
            selector.close();
        } finally {
            LOG.info("Engine is shutdown");
            started = false;
        }
    }

    class ConnectionContext {
        final SocketChannel channel;
        SelectionKey selectionKey;
        boolean hbPending;
        long lastHeartbeatSent = 0;
        long lastHeartbeatReceived;
        long lastDataReceived;
        long lastDataSent;
        long lastStatusReported;
        boolean isDead = false;

        final ByteBuffer lenReadB = ByteBuffer.allocateDirect(4);
        final ByteBuffer lenWriteB = ByteBuffer.allocateDirect(4);


        private byte[] currentOutgoing;
        private int currentWritten;
        private byte[] currentIncoming;
        private int currentRead;

        private final ArrayDeque<byte[]> pendingSends;
        private final String toStr;


    }
}
