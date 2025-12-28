package jp.cssj.cti2.progress;

/**
 * サーバ側でのメインドキュメントの処理状況を受け取ります。
 * 
 * @author MIYABE Tatsuhiko
 * @version $Id: ProgressListener.java 1552 2018-04-26 01:43:24Z miyabe $
 */
public interface ProgressListener {
	/**
	 * <p>
	 * サーバ側で見積もられたメインドキュメントの大きさが渡されます。
	 * </p>
	 * <p>
	 * このメソッドは呼ばれないことがあり、不正確な値が渡される可能性もあります。
	 * </p>
	 * 
	 * @param sourceLength
	 *            メインドキュメントのバイト数。
	 */
	public void sourceLength(long sourceLength);

	/**
	 * 処理されたメインドキュメントのバイト数が渡されます。
	 * <p>
	 * このメソッドは呼ばれないことがあり、不正確な値が渡される可能性もあります。
	 * </p>
	 * 
	 * @param serverRead
	 *            読み込み済みバイト数。
	 */
	public void progress(long serverRead);
}