package server;

import handler.*;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http2.*;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import java.net.InetSocketAddress;

/**
 * Ref{@link=https://medium.com/@chanakadkb/lets-make-server-push-enabled-http-2-server-with-netty-1e54134dc90b}
 */
public class Http2nettyServer {

    private String host = "localhost";
    private int port = 20000;

    private EventLoopGroup parentGroup;
    private EventLoopGroup childGroup;

    private ServerBootstrap serverBootstrap;

    private ChannelFuture cf;

    private Boolean useSsl = false;

    private String certPath;
    private String certPassword;

    public Http2nettyServer(String host, int port) {
        this.host = host;
        this.port = port;

        init();
    }

    private void init() {

        parentGroup = new NioEventLoopGroup(1);
        childGroup = new NioEventLoopGroup();

        serverBootstrap = new ServerBootstrap();


        serverBootstrap.group(parentGroup, childGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 100)
                .handler(new LoggingHandler(LogLevel.DEBUG))
                .childHandler(new ServerInitializer(useSsl));

    }


    public void bind() {

        try {
            cf = serverBootstrap.bind(new InetSocketAddress(host, port)).sync();

            //Binding Done
//            cf.isDone();

            if (cf.isSuccess()) {
                System.out.println("Server Binding Address =" + cf.channel().localAddress());
                cf.channel().closeFuture().sync();
            } else {
                shutdown();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            shutdown();
        }
    }

    public void shutdown() {
        cf.channel().close();
        childGroup.shutdownGracefully();
        parentGroup.shutdownGracefully();
    }

    public Boolean getUseSsl() {
        return useSsl;
    }

    public void setUseSsl(Boolean useSsl) {
        this.useSsl = useSsl;
    }

    public void setUseSsl(String path, String password, Boolean useSsl) {
        this.certPath = path;
        this.certPassword = password;
        this.useSsl = useSsl;
    }
}