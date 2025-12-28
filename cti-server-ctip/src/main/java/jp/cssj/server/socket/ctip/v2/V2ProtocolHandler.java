package jp.cssj.server.socket.ctip.v2;

import java.io.IOException;
import java.net.URI;

import jp.cssj.cti2.CTIDriver;
import jp.cssj.cti2.CTIDriverManager;
import jp.cssj.server.socket.ProtocolHandler;
import jp.cssj.server.socket.ProtocolProcessor;

/**
 * 
 * @author MIYABE Tatsuhiko
 * @version $Id: V2ProtocolHandler.java 1552 2018-04-26 01:43:24Z miyabe $
 */
public class V2ProtocolHandler implements ProtocolHandler {
	protected final URI uri;
	protected final CTIDriver driver;

	public V2ProtocolHandler(URI uri, CTIDriver driver) {
		this.uri = uri;
		this.driver = driver;
	}

	public V2ProtocolHandler(URI uri) {
		this.uri = uri;
		this.driver = CTIDriverManager.getDriver(uri);
	}

	public boolean accepts(String firstLine) {
		return firstLine.startsWith("CTIP/2.0 ");
	}

	public ProtocolProcessor newProcesor() throws IOException, SecurityException {
		return new V2ProtocolProcessor(this.uri, this.driver);
	}
}
