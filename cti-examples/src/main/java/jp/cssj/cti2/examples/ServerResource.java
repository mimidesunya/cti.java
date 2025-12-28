package jp.cssj.cti2.examples;

import java.io.File;
import java.net.URI;

import jp.cssj.cti2.CTIDriverManager;
import jp.cssj.cti2.CTISession;
import jp.cssj.cti2.helpers.CTIMessageHelper;
import jp.cssj.cti2.helpers.CTISessionHelper;

/**
 * サーバー側でデータを取得して変換します。
 */
public class ServerResource {
	/** 接続先。 */
	private static final URI SERVER_URI = URI.create("ctip://127.0.0.1:8099/");

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

			// エラーメッセージを標準エラー出力に表示する
			session.setMessageHandler(CTIMessageHelper.createStreamMessageHandler(System.err));

			// ハイパーリンクとブックマークを作成する
			session.property("output.pdf.hyperlinks", "true");
			session.property("output.pdf.bookmarks", "true");

			// http://copper-pdf.com/以下にあるリソースへのアクセスを許可する
			session.property("input.include", "http://copper-pdf.com/**");

			// ウェブページを変換
			session.transcode(URI.create("http://copper-pdf.com/"));
		}
	}
}