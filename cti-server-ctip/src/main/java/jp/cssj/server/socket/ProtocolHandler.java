package jp.cssj.server.socket;

import java.io.IOException;

public interface ProtocolHandler {
	public boolean accepts(String firstLine);

	public ProtocolProcessor newProcesor() throws IOException;
}
