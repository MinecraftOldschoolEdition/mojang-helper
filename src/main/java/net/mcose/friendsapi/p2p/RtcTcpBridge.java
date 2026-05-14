package net.mcose.friendsapi.p2p;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * TCP-to-WebRTC data-channel bridge.
 *
 * Production use: host bridges WebRTC data-channel bytes to the local
 * UberBukkit TCP port; guest exposes a localhost TCP endpoint and forwards the
 * vanilla client's socket traffic over the data channel.
 */
public final class RtcTcpBridge {
    public interface DataChannel {
        void send(ByteBuffer bytes);
        void setReceiver(Receiver receiver);
        void close();
    }

    public interface Receiver {
        void onBytes(ByteBuffer bytes);
    }

    private RtcTcpBridge() {
    }

    public static BridgeEndpoint openLocalEndpoint(final DataChannel channel) throws IOException {
        final ServerSocket serverSocket = new ServerSocket(0, 1, InetAddress.getByName("127.0.0.1"));
        final AtomicBoolean closed = new AtomicBoolean(false);
        Thread acceptThread = new Thread(new Runnable() {
            public void run() {
                try {
                    final Socket socket = serverSocket.accept();
                    bridge(socket, channel, closed);
                } catch (IOException ignored) {
                }
            }
        }, "MCOSE-P2P-TCP-Accept");
        acceptThread.setDaemon(true);
        acceptThread.start();
        return new BridgeEndpoint("127.0.0.1", serverSocket.getLocalPort(), new Runnable() {
            public void run() {
                closed.set(true);
                try {
                    serverSocket.close();
                } catch (IOException ignored) {
                }
                channel.close();
            }
        });
    }

    private static void bridge(final Socket socket, final DataChannel channel, final AtomicBoolean closed) {
        channel.setReceiver(new Receiver() {
            public void onBytes(ByteBuffer bytes) {
                try {
                    OutputStream out = socket.getOutputStream();
                    byte[] copy = new byte[bytes.remaining()];
                    bytes.get(copy);
                    out.write(copy);
                    out.flush();
                } catch (IOException e) {
                    closed.set(true);
                }
            }
        });

        Thread pump = new Thread(new Runnable() {
            public void run() {
                byte[] buffer = new byte[8192];
                try {
                    InputStream in = socket.getInputStream();
                    int read;
                    while (!closed.get() && (read = in.read(buffer)) >= 0) {
                        channel.send(ByteBuffer.wrap(buffer, 0, read));
                    }
                } catch (IOException ignored) {
                } finally {
                    closed.set(true);
                    try {
                        socket.close();
                    } catch (IOException ignored) {
                    }
                    channel.close();
                }
            }
        }, "MCOSE-P2P-TCP-Pump");
        pump.setDaemon(true);
        pump.start();
    }

    public static final class BridgeEndpoint {
        public final String host;
        public final int port;
        private final Runnable closer;

        public BridgeEndpoint(String host, int port, Runnable closer) {
            this.host = host;
            this.port = port;
            this.closer = closer;
        }

        public void close() {
            if (closer != null) {
                closer.run();
            }
        }
    }
}
