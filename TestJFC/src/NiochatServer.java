

import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public class NiochatServer implements Runnable {
	private final int port;
	private ServerSocketChannel ssc;
	private Selector selector;
	private ByteBuffer buf = ByteBuffer.allocate(256);

	NiochatServer(int port) throws IOException {
		this.port = port;
		this.ssc = ServerSocketChannel.open();
		this.ssc.socket().bind(new InetSocketAddress(port));
		this.ssc.configureBlocking(false);
		this.selector = Selector.open();

		this.ssc.register(selector, SelectionKey.OP_ACCEPT);
	}

	@Override public void run() {
		try {
			System.out.println("Server starting on port " + this.port);

			Iterator<SelectionKey> iter;
			SelectionKey key;
			while(this.ssc.isOpen()) {
				selector.select();
				iter=this.selector.selectedKeys().iterator();
				while(iter.hasNext()) {
					key = iter.next();
					iter.remove();

					if(key.isAcceptable()) this.handleAccept(key);
					if(key.isWritable()) this.handleWrite(key);
					//if(key.isReadable()) this.handleRead(key);
				}
			}
		} catch(IOException e) {
			System.out.println("IOException, server of port " +this.port+ " terminating. Stack trace:");
			e.printStackTrace();
		}
	}

	private final ByteBuffer welcomeBuf = ByteBuffer.wrap("Welcome to NioChat!".getBytes());
	private void handleAccept(SelectionKey key) throws IOException {
		SocketChannel sc = ((ServerSocketChannel) key.channel()).accept();
		String address = (new StringBuilder( sc.socket().getInetAddress().toString() )).append(":").append( sc.socket().getPort() ).toString();
		sc.configureBlocking(false);
		sc.register(selector, SelectionKey.OP_WRITE, address);
		sc.write(welcomeBuf);
		welcomeBuf.rewind();
		System.out.println("accepted connection from: "+address);
	}
	
	private void handleWrite(SelectionKey key) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		StringBuilder sb = new StringBuilder();
		ByteBuffer msgBuf=ByteBuffer.allocate(100000);
		msgBuf.order(ByteOrder.BIG_ENDIAN);
		SocketChannel sch = (SocketChannel) key.channel();
        System.out.print("Enter Packet: ");
        String s = br.readLine();
        System.out.println("you entered: " + s);
		if (s.compareTo("AD1") == 0) {
			msgBuf.putChar('A');
			msgBuf.putChar('D');
			for (double i=-20; i<20; i+=0.0016) {
				Double value = (Math.sin(i*1000)+1)*2048-1;
				msgBuf.putShort(value.shortValue());
			}
			System.out.println("Sending Sine Data");
		} else if (s.compareTo("AD2") == 0) {
			msgBuf.putChar('A');
			msgBuf.putChar('D');
			for (int i=0; i<25000; i++) {
				msgBuf.putShort((short) 3000);
			}
			System.out.println("Sending Line Data");
		} else if (s.compareTo("ACF") == 0) {
			msgBuf.putChar('A');
			msgBuf.putChar('C');
			msgBuf.putChar('F');
			msgBuf.putChar('G');
			msgBuf.putShort((short) 1);
			msgBuf.putShort((short) 2);
			msgBuf.putShort((short) 100);
			msgBuf.putShort((short) 100);
			msgBuf.putShort((short) 20000);
			System.out.println("Sending Fx Gen Data");
		} else if (s.compareTo("ACC") == 0) {
			msgBuf.putChar('A');
			msgBuf.putChar('C');
			msgBuf.putChar('C');
			msgBuf.putChar('D');
			msgBuf.putShort((short) 200);
			msgBuf.putInt(50000);
			System.out.println("Sending Ch Data");
		} else if (s.compareTo("ACT") == 0) {
			
			System.out.println("Sending Trig Data");
		} else {
			msgBuf.wrap(s.getBytes());
		}
		sch.write(msgBuf);
		msgBuf.rewind();
	}

	private void handleRead(SelectionKey key) throws IOException {
		SocketChannel ch = (SocketChannel) key.channel();
		StringBuilder sb = new StringBuilder();

		buf.clear();
		int read = 0;
		while( (read = ch.read(buf)) > 0 ) {
			buf.flip();
			byte[] bytes = new byte[buf.limit()];
			buf.get(bytes);
			sb.append(new String(bytes));
			buf.clear();
		}
		String msg;
		if(read<0) {
			msg = key.attachment()+" left the chat.\n";
			ch.close();
		}
		else {
			msg = key.attachment()+": "+sb.toString();
		}

		System.out.println(msg);
		broadcast(msg);
	}

	private void broadcast(String msg) throws IOException {
		ByteBuffer msgBuf=ByteBuffer.wrap(msg.getBytes());
		for(SelectionKey key : selector.keys()) {
			if(key.isValid() && key.channel() instanceof SocketChannel) {
				SocketChannel sch=(SocketChannel) key.channel();
				sch.write(msgBuf);
				msgBuf.rewind();
			}
		}
	}

	public static void main(String[] args) throws IOException {
		NiochatServer server = new NiochatServer(10523);
		(new Thread(server)).start();
	}
}
