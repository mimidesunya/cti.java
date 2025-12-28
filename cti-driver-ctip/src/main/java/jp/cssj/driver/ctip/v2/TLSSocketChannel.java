package jp.cssj.driver.ctip.v2;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class TLSSocketChannel extends SelectableChannel implements ByteChannel {
	private ByteBuffer peerAppData, appData, netData, peerNetData;
	private final SocketChannel channel;
	private SSLEngineResult res;
	private SSLEngine engine;
	
	public TLSSocketChannel(SocketChannel sc) {
		this.channel = sc;
	}

	public boolean connect(SocketAddress remote) throws IOException {
		if (!this.channel.connect(remote)) {
			return false;
		}
		SSLContext sslContext = null;
		try {
			sslContext = SSLContext.getInstance("TLS");

			if (System.getProperty("jp.cssj.driver.tls.trust", "true").equalsIgnoreCase("true")) {
				TrustManager[] tm = { new X509TrustManager() {
					public void checkClientTrusted(X509Certificate[] chain, String authType)
							throws CertificateException {
					}

					public void checkServerTrusted(X509Certificate[] chain, String authType)
							throws CertificateException {
					}

					public X509Certificate[] getAcceptedIssuers() {
						return null;
					}
				} };
				sslContext.init(null, tm, null);
			} else {
				sslContext.init(null, null, null);
			}

			this.engine = sslContext.createSSLEngine();
			this.engine.setUseClientMode(true);
			this.engine.setEnableSessionCreation(true);
			SSLSession session = this.engine.getSession();
			int appBufferMax = session.getApplicationBufferSize();
			int netBufferMax = session.getPacketBufferSize();

			this.appData = ByteBuffer.allocate(appBufferMax);
			this.netData = ByteBuffer.allocate(netBufferMax);
			this.peerAppData = ByteBuffer.allocate(appBufferMax);
			this.peerNetData = ByteBuffer.allocate(netBufferMax);
			this.appData.clear();

			this.engine.beginHandshake();
			SSLEngineResult.HandshakeStatus hs = engine.getHandshakeStatus();
			LOOP: for (;;) {
				switch (hs) {
				case NEED_UNWRAP:
					this.peerNetData.clear();
					while (this.channel.read(this.peerNetData) < 1) {
						Thread.sleep(30);
					}
					this.peerNetData.flip();
					this.unwrap(this.peerNetData);
					if (this.res.getHandshakeStatus() != SSLEngineResult.HandshakeStatus.FINISHED) {
						this.appData.clear();
						this.write(this.appData);
					}
					break;

				case NEED_WRAP:
					this.appData.clear();
					this.write(this.appData);
					break;

				case NEED_TASK:
					Runnable task;
					while ((task = this.engine.getDelegatedTask()) != null) {
						task.run();
					}
					break;

				case FINISHED:
				case NOT_HANDSHAKING:
					break LOOP;

				default:
					throw new IllegalStateException();
				}
				hs = this.res.getHandshakeStatus();
			}
			this.peerAppData.clear();
			this.peerAppData.flip();
		} catch (Exception e) {
			IOException ioe = new IOException();
			ioe.initCause(e);
			throw ioe;
		}
		return true;
	}

	private synchronized ByteBuffer unwrap(ByteBuffer b) throws SSLException, IOException {
		this.peerAppData.clear();
		LOOP: while (b.hasRemaining()) {
			this.res = this.engine.unwrap(b, this.peerAppData);
			switch (this.res.getHandshakeStatus()) {
			case NEED_TASK:
				Runnable task;
				while ((task = this.engine.getDelegatedTask()) != null) {
					task.run();
				}
				break;
			case FINISHED:
			case NOT_HANDSHAKING:
			case NEED_UNWRAP:
			case NEED_WRAP:
				switch (this.res.getStatus()) {
				case BUFFER_OVERFLOW:
					ByteBuffer tmp = ByteBuffer.allocate(this.peerAppData.capacity() * 3 / 2);
					this.peerAppData.flip();
					tmp.put(this.peerAppData);
					this.peerAppData = tmp;
					break;
				case BUFFER_UNDERFLOW:
					break LOOP;
				default:
					break;
				}
				break;
			default:
				throw new IllegalStateException(String.valueOf(this.res.getHandshakeStatus()));
			}
		}
		return this.peerAppData;
	}

	public synchronized int write(ByteBuffer src) throws IOException {
		this.netData.clear();
		this.res = this.engine.wrap(src, this.netData);
		this.netData.flip();
		return this.channel.write(this.netData);
	}

	public synchronized int read(ByteBuffer dest) throws IOException {
		int amount = 0, limit;
		if (this.peerAppData.hasRemaining()) {
			limit = Math.min(this.peerAppData.remaining(), dest.remaining());
			for (int i = 0; i < limit; i++) {
				dest.put(this.peerAppData.get());
				amount++;
			}
			return amount;
		}
		if (this.peerNetData.hasRemaining()) {
			this.unwrap(this.peerNetData);
			this.peerAppData.flip();
			limit = Math.min(this.peerAppData.limit(), dest.remaining());
			for (int i = 0; i < limit; i++) {
				dest.put(this.peerAppData.get());
				amount++;
			}
			if (this.res.getStatus() != SSLEngineResult.Status.BUFFER_UNDERFLOW) {
				this.peerNetData.clear();
				this.peerNetData.flip();
				return amount;
			}
		}
		if (!this.peerNetData.hasRemaining()) {
			this.peerNetData.clear();
		} else {
			this.peerNetData.compact();
		}

		if (this.channel.read(this.peerNetData) == -1) {
			this.peerNetData.clear();
			this.peerNetData.flip();
			return -1;
		}
		this.peerNetData.flip();
		this.unwrap(this.peerNetData);
		this.peerAppData.flip();
		limit = Math.min(this.peerAppData.limit(), dest.remaining());
		for (int i = 0; i < limit; i++) {
			dest.put(this.peerAppData.get());
			amount++;
		}
		return amount;
	}

	public boolean isConnected() {
		return this.channel.isConnected();
	}

	public SelectableChannel configureBlocking(boolean b) throws IOException {
		return this.channel.configureBlocking(b);
	}

	public Object blockingLock() {
		return this.channel.blockingLock();
	}

	public boolean isBlocking() {
		return this.channel.isBlocking();
	}

	public boolean isRegistered() {
		return this.channel.isRegistered();
	}

	public SelectionKey keyFor(Selector sel) {
		return this.channel.keyFor(sel);
	}

	public SelectorProvider provider() {
		return this.channel.provider();
	}

	public SelectionKey register(Selector sel, int ops, Object att) throws ClosedChannelException {
		return this.channel.register(sel, ops, att);
	}

	public int validOps() {
		return this.channel.validOps();
	}

	protected synchronized void implCloseChannel() throws IOException {
		this.engine.closeOutbound();
		LOOP: while (!this.engine.isOutboundDone()) {
			this.appData.clear();
			this.netData.clear();
			SSLEngineResult res = this.engine.wrap(this.appData, this.netData);
			switch (res.getStatus()) {
			case BUFFER_OVERFLOW:
				ByteBuffer tmp = ByteBuffer.allocate(this.netData.capacity() * 3 / 2);
				this.netData.flip();
				tmp.put(this.netData);
				this.netData = tmp;
				break;
			case CLOSED:
				break LOOP;
			default:
				break;
			}

			while (this.netData.hasRemaining()) {
				int num = this.channel.write(this.netData);
				if (num == -1) {
					break;
				} else if (num == 0) {
					try {
						Thread.sleep(30);
					} catch (InterruptedException e) {
						continue;
					}
				}
				this.netData.compact();
			}
		}
		this.channel.close();
	}
}