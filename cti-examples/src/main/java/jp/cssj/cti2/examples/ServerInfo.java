package jp.cssj.cti2.examples;

import java.io.InputStream;
import java.net.URI;

import jp.cssj.cti2.CTIDriverManager;
import jp.cssj.cti2.CTISession;

import org.apache.commons.io.IOUtils;

/**
 * サーバー情報を取得します。
 */
public class ServerInfo {
	/** 接続先。 */
	private static final URI SERVER_URI = URI.create("ctip://127.0.0.1:8099/");

	/** ユーザー。 */
	private static final String USER = "user";

	/** パスワード。 */
	private static final String PASSWORD = "kappa";

	public static void main(String[] args) throws Exception {
		// 接続する
		try (CTISession session = CTIDriverManager.getSession(SERVER_URI, USER, PASSWORD)) {
			// バージョン情報
			{
				URI uri = URI.create("http://www.cssj.jp/ns/ctip/version");
				System.out.println("-- " + uri);
				InputStream in = session.getServerInfo(uri);
				IOUtils.copy(in, System.out);
			}
			// 出力タイプ
			{
				URI uri = URI.create("http://www.cssj.jp/ns/ctip/output-types");
				System.out.println("-- " + uri);
				InputStream in = session.getServerInfo(uri);
				IOUtils.copy(in, System.out);
			}
		}
	}
}