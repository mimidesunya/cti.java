package jp.cssj.cti2.progress;

/**
 * 進行状況を表すインターフェースです。
 * 
 * @author MIYABE Tatsuhiko
 * @version $Id: Progressive.java 1552 2018-04-26 01:43:24Z miyabe $
 */
public interface Progressive {
	/**
	 * 元のデータで処理済のバイト数を返します。
	 * 
	 * @return 処理済のバイト数。
	 */
	public long getProgress();
}