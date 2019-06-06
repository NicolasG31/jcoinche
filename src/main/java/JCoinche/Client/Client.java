package JCoinche.Client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

public class Client {

    private String host;
    private int port;
    private String name;

    public Client(String host, int port, String name) {
        this.host = host;
        this.port = port;
        this.name = name;
    }

    public static void main(String[] args) throws Exception {
        System.out.print("Launching client\n");
        if (args.length == 3) {
            try {
                new Client(args[0], Integer.parseInt(args[1]), args[2]).run();
            } catch (NumberFormatException e) {
                System.err.println(e.getMessage());
            }
        }
        else
            System.err.println("Error, wrong arguments, please input: Server ip, Port, Username");
    }

    public void run() throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();
        System.out.print("Trying to connect\n");
        try {
            Bootstrap bootstrap = new Bootstrap()
                    .group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ClientInitializer());

            Channel channel = bootstrap.connect(host, port).sync().channel();
            System.out.println("Connected");
            ClientHandler handle = channel.pipeline().get(ClientHandler.class);

            handle.sendName(this.name);
            while (true) {
            }
        }
        finally{
            group.shutdownGracefully();
        }

    }

}