package lou.alex.zoom.net;

import lou.alex.zoom.logging.LoggerFactory;
import org.slf4j.Logger;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.util.Iterator;

public class ServerIOEngine extends IOEngine implements Runnable {
    private static final Logger LOG = LoggerFactory.contextLogger();

    private Thread ioThread;
    private ServerSocketChannel channel;

    public ServerIOEngine(SocketAddress address, long heartbeatInterval, long inactivityTimeout, ConnectionListener connectionListener) {
        super(address, heartbeatInterval, inactivityTimeout, connectionListener);
    }

    @Override
    protected SocketAddress startConnection() throws IOException {
        channel = ServerSocketChannel.open();
        final ServerSocketChannel serverSocketChannel = channel;
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        serverSocketChannel.socket().setReceiveBufferSize((Integer) preferences.get("SO_RCV"));
        serverSocketChannel.bind(address);

        final SocketAddress actualSocketAddress = serverSocketChannel.getLocalAddress();

        LOG.info("Starting server at {} with receiver buffer {}. It will heartbeat every {} ms",
                actualSocketAddress, serverSocketChannel.socket().getReceiveBufferSize(), heartbeatInterval);

        ioThread = new Thread(this);
        ioThread.setDaemon(true);
        ioThread.start();
        return actualSocketAddress;
    }

    @Override
    public void run() {
        ByteBuffer readBuffer = ByteBuffer.allocateDirect(DEFAULT_BUFFER_SIZE);
        ByteBuffer writeBuffer = ByteBuffer.allocateDirect(DEFAULT_BUFFER_SIZE);

        for (;;) {
            try {

                final int selected = selector.select(heartbeatInterval/2);

                if (LOG.isDebugEnabled()) {
                    LOG.debug("Selector woke, Selected {} ", selected);
                }

                long now = System.nanoTime();

                if (selected > 0) {
                    for (Iterator<SelectionKey> iter = selector.selectedKeys().iterator(); iter.hasNext();) {
                        SelectionKey sk = iter.next();
                        iter.remove();
                        if (!sk.isValid()) {
                            if (LOG.isDebugEnabled()) {
                                LOG.debug("Invalid selection key {}", sk);
                            }
                            continue;
                        }

                        if (sk.isAcceptable()) {
                            handleClientConnection(sk, now);
                        }

                        Object attachment = sk.attachment();
                        if(attachment instanceof ConnectionContext) {
                            ConnectionContext cc = (ConnectionContext) attachment;
                            try {
                                if (sk.isWritable()) {
                                    writeData(now, cc, writeBuffer);
                                }
                                if (sk.isReadable()) {

                                    readData(now, cc, readBuffer);
                                }
                            } catch (Exception e) {
                                LOG.info("Unexpected error occurred", e);
                                disposeConnection(cc);
                            }
                        }
                    }
                }

                sendHeartbeats(now);
                checkClientConnection(now);
            } catch (Throwable th) {
                if (!selector.isOpen()) {
                    LOG.info("Selector was closed - shutting down all client connection");
                    disposeAllConnections();
                    try {
                        LOG.info("Shutting down server socket");
                        channel.close();
                        channel.socket().close();
                    } catch (Exception ex) {
                        LOG.error("Unexpected error occurred", ex);ß
                    }
                    break;
                } else {
                    LOG.error("Unexpected error occurred", th);
                }
            }
        }

        LOG.info("IO thread ended");
    }
}
