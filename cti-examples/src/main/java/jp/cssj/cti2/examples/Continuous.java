package jp.cssj.cti2.examples;

import java.io.File;
import java.net.URI;

import jp.cssj.cti2.CTIDriverManager;
import jp.cssj.cti2.CTISession;
import jp.cssj.cti2.helpers.CTISessionHelper;

/**
 * クライアントから送ったデータを変換します。
 */
public class Continuous {
	/** 接続先。 */
	private static final URI SERVER_URI = URI.create("ctip://127.0.0.1:8101/");

	/** ユーザー。 */
	private static final String USER = "user";

	/** パスワード。 */
	private static final String PASSWORD = "kappa";

	public static void main(String[] args) throws Exception {
		// 接続する
		try (CTISession session = CTIDriverManager.getSession(SERVER_URI, USER, PASSWORD)) {
			// test.pdfに結果を出力する
			File file = new File("test.pdf");
			CTISessionHelper.setResultFile(session, file);
			session.setContinuous(true);

			// リソースの送信
			CTISessionHelper.sendResourceFile(session, new File("a/common.css"), "text/css", null);
			CTISessionHelper.sendResourceFile(session, new File("a/MEM093110.css"), "text/css", null);
			session.property("processing.page-references", "true");

			session.property("processing.middle-pass", "true");
			// 文書の送信
			CTISessionHelper.transcodeFile(session, new File("a/sample1.html"), "text/html", null);

			session.property("processing.middle-pass", "false");
			// 文書の送信
			CTISessionHelper.transcodeFile(session, new File("a/sample2.html"), "text/html", null);

			// 結果を結合
			session.join();
		}
	}
}