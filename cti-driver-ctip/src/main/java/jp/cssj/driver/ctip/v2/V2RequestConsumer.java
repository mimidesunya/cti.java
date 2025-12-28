package jp.cssj.driver.ctip.v2;

import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

import jp.cssj.cti2.TranscoderException;
import jp.cssj.driver.ctip.common.ChannelIO;

/**
 * @author MIYABE Tatsuhiko
 * @version $Id: V2RequestConsumer.java 1554 2018-04-26 03:34:02Z miyabe $
 */
public class V2RequestConsumer {
	private final String charset;

	private final byte[] buff = new byte[V2Session.BUFFER_SIZE + 5];

	private final ChannelIO io;

	private int pos = 0;

	private V2Session session;

	V2RequestConsumer(ChannelIO io, String charset) throws IOException {
		this.io = io;
		this.charset = charset;
	}

	protected void setCTIPSession(V2Session session) {
		this.session = session;
	}

	/**
	 * プロパティを送ります。
	 * 
	 * @param name
	 *            プロパティ名。
	 * @param value
	 *            値。
	 * @throws IOException
	 */
	public void property(String name, String value) throws IOException {
		this.flush();
		byte[] nameBytes = ChannelIO.toBytes(name, this.charset);
		byte[] valueBytes = ChannelIO.toBytes(value, this.charset);

		int payload = 1 + 2 + nameBytes.length + 2 + valueBytes.length;
		ByteBuffer src = ByteBuffer.allocate(4 + payload);
		src.putInt(payload);
		src.put(V2ClientPackets.PROPERTY);
		src.putShort((short) nameBytes.length);
		src.put(nameBytes);
		src.putShort((short) valueBytes.length);
		src.put(valueBytes);
		this.io.writeAll(src);
	}

	/**
	 * クライアント側でリソースを解決するモードを設定します。
	 * 
	 * @param on
	 *            trueであれば切り替え、falseであれば解除。
	 * @throws IOException
	 */
	public void clientResource(boolean on) throws IOException {
		this.flush();

		int payload = 2;
		ByteBuffer src = ByteBuffer.allocate(4 + payload);
		src.putInt(payload);
		src.put(V2ClientPackets.CLIENT_RESOURCE);
		src.put((byte) (on ? 1 : 0));
		this.io.writeAll(src);
	}

	/**
	 * 本体の開始を通知します。
	 * 
	 * @param uri
	 *            仮想URI。
	 * @param mimeType
	 *            MIME型。
	 * @param encoding
	 *            キャラクタ・エンコーディング。
	 * @throws IOException
	 */
	public void startMain(URI uri, String mimeType, String encoding, long length) throws IOException {
		this.flush();
		byte[] uriBytes = ChannelIO.toBytes(uri.toString(), this.charset);
		byte[] mimeTypeBytes = ChannelIO.toBytes(mimeType, this.charset);
		byte[] encodingBytes = ChannelIO.toBytes(encoding, this.charset);

		int payload = 1 + 2 + uriBytes.length + 2 + mimeTypeBytes.length + 2 + encodingBytes.length + 8;
		ByteBuffer src = ByteBuffer.allocate(4 + payload);
		src.putInt(payload);
		src.put(V2ClientPackets.START_MAIN);
		src.putShort((short) uriBytes.length);
		src.put(uriBytes);
		src.putShort((short) mimeTypeBytes.length);
		src.put(mimeTypeBytes);
		src.putShort((short) encodingBytes.length);
		src.put(encodingBytes);
		src.putLong(length);
		this.io.writeAll(src);
	}

	/**
	 * サーバー側でメインドキュメントを取得します。
	 * 
	 * @param uri
	 *            メインドキュメントのURI。
	 * @throws IOException
	 */
	public void serverMain(URI uri) throws IOException {
		this.flush();
		byte[] uriBytes = ChannelIO.toBytes(uri.toString(), this.charset);

		int payload = 1 + 2 + uriBytes.length;
		ByteBuffer src = ByteBuffer.allocate(4 + payload);
		src.putInt(payload);
		src.put(V2ClientPackets.SERVER_MAIN);
		src.putShort((short) uriBytes.length);
		src.put(uriBytes);
		this.io.writeAll(src);
	}

	/**
	 * データパケットを送ります。
	 * 
	 * @param b
	 *            バイト列バッファ。
	 * @param off
	 *            データの開始位置。
	 * @param len
	 *            データの長さ。
	 * @throws IOException
	 */
	public void data(byte[] b, int off, int len) throws IOException {
		for (int i = 0; i < len; ++i) {
			if (this.pos >= V2Session.BUFFER_SIZE) {
				this.flush();
			}
			this.buff[(this.pos++) + 4 + 1] = b[i + off];
		}
	}

	private void flush() throws IOException, TranscoderException {
		if (this.pos > 0) {
			int payload = 1 + this.pos;
			ByteBuffer src = ByteBuffer.wrap(this.buff, 0, 4 + payload);
			src.putInt(payload);
			src.put(V2ClientPackets.DATA);
			src.position(0);

			SelectableChannel channel = this.io.getSelectable();
			if (channel.isBlocking()) {
				try (Selector selector = channel.provider().openSelector()) {
					channel.configureBlocking(false);
					SelectionKey key = channel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
					try {
						for (;;) {
							selector.select();
							selector.selectedKeys().clear();
							if (src.remaining() > 0 && key.isWritable()) {
								this.io.getChannel().write(src);
							}
							if (key.isReadable()) {
								this.session.buildNext();
							}
							if (src.remaining() <= 0) {
								break;
							}
						}
					} finally {
						key.cancel();
					}
				} finally {
					channel.configureBlocking(true);
				}
			} else {
				for (;;) {
					SelectionKey key = this.io.rwselect();
					if (src.remaining() > 0 && key.isWritable()) {
						this.io.getChannel().write(src);
					}
					if (key.isReadable()) {
						this.session.buildNext();
					}
					if (src.remaining() <= 0) {
						break;
					}
				}
			}

			this.pos = 0;
		}
	}

	/**
	 * リソースの開始を通知します。
	 * 
	 * @param uri
	 *            仮想URI。
	 * @param mimeType
	 *            MIME型。
	 * @param encoding
	 *            キャラクタ・エンコーディング。
	 * @throws IOException
	 */
	public void startResource(URI uri, String mimeType, String encoding, long length) throws IOException {
		this.flush();
		byte[] uriBytes = ChannelIO.toBytes(uri.toString(), this.charset);
		byte[] mimeTypeBytes = ChannelIO.toBytes(mimeType, this.charset);
		byte[] encodingBytes = ChannelIO.toBytes(encoding, this.charset);

		int payload = 1 + 2 + uriBytes.length + 2 + mimeTypeBytes.length + 2 + encodingBytes.length + 8;
		ByteBuffer src = ByteBuffer.allocate(4 + payload);
		src.putInt(payload);
		src.put(V2ClientPackets.START_RESOURCE);
		src.putShort((short) uriBytes.length);
		src.put(uriBytes);
		src.putShort((short) mimeTypeBytes.length);
		src.put(mimeTypeBytes);
		src.putShort((short) encodingBytes.length);
		src.put(encodingBytes);
		src.putLong(length);
		this.io.writeAll(src);
	}

	/**
	 * 存在しないリソースとして通知します。
	 * 
	 * @param uri
	 *            リソースのURI。
	 * @throws IOException
	 */
	public void missingResource(URI uri) throws IOException {
		this.flush();
		byte[] uriBytes = ChannelIO.toBytes(uri.toString(), this.charset);

		int payload = 1 + 2 + uriBytes.length;
		ByteBuffer src = ByteBuffer.allocate(4 + payload);
		src.putInt(payload);
		src.put(V2ClientPackets.MISSING_RESOURCE);
		src.putShort((short) uriBytes.length);
		src.put(uriBytes);
		this.io.writeAll(src);
	}

	/**
	 * データの終了を通知します。
	 * 
	 * @throws IOException
	 */
	public void eof() throws IOException {
		this.flush();

		int payload = 1;
		ByteBuffer src = ByteBuffer.allocate(4 + payload);
		src.putInt(payload);
		src.put(V2ClientPackets.EOF);
		this.io.writeAll(src);
	}

	/**
	 * 複数の結果を結合するモードを切り替えます。
	 * 
	 * @param continuous
	 *            結合モード。
	 * @throws IOException
	 */
	public void continuous(boolean continuous) throws IOException {
		this.flush();

		int payload = 2;
		ByteBuffer src = ByteBuffer.allocate(4 + payload);
		src.putInt(payload);
		src.put(V2ClientPackets.CONTINUOUS);
		src.put((byte) (continuous ? 1 : 0));
		this.io.writeAll(src);
	}

	/**
	 * 結果の結合を要求します。
	 * 
	 * @throws IOException
	 */
	public void join() throws IOException {
		int payload = 1;
		ByteBuffer src = ByteBuffer.allocate(4 + payload);
		src.putInt(payload);
		src.put(V2ClientPackets.JOIN);
		this.io.writeAll(src);
	}

	/**
	 * 処理の中断を要求します。
	 * 
	 * @param mode
	 *            中断モード。
	 * @throws IOException
	 */
	public void abort(byte mode) throws IOException {
		this.flush();

		int payload = 2;
		ByteBuffer src = ByteBuffer.allocate(4 + payload);
		src.putInt(payload);
		src.put(V2ClientPackets.ABORT);
		src.put(mode);
		this.io.writeAll(src);
	}

	/**
	 * 状態をリセットします。
	 * 
	 * @throws IOException
	 */
	public void reset() throws IOException {
		this.flush();

		int payload = 1;
		ByteBuffer src = ByteBuffer.allocate(4 + payload);
		src.putInt(payload);
		src.put(V2ClientPackets.RESET);
		this.io.writeAll(src);
	}

	/**
	 * 通信を終了します。
	 * 
	 * @throws IOException
	 */
	public void close() throws IOException {
		this.flush();

		int payload = 1;
		ByteBuffer src = ByteBuffer.allocate(4 + payload);
		src.putInt(payload);
		src.put(V2ClientPackets.CLOSE);
		this.io.writeAll(src);
	}

	/**
	 * サーバー情報を要求します。
	 * 
	 * @param uri
	 *            サーバー情報のURI。
	 * @throws IOException
	 */
	public void serverInfo(URI uri) throws IOException {
		this.flush();
		byte[] uriBytes = ChannelIO.toBytes(uri.toString(), this.charset);

		int payload = 1 + 2 + uriBytes.length;
		ByteBuffer src = ByteBuffer.allocate(4 + payload);
		src.putInt(payload);
		src.put(V2ClientPackets.SERVER_INFO);
		src.putShort((short) uriBytes.length);
		src.put(uriBytes);
		this.io.writeAll(src);
	}
}