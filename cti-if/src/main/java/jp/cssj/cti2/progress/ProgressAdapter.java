package jp.cssj.cti2.progress;

/**
 * ProgressListenterの実装を容易にするためのアダプタです。
 * 
 * @author MIYABE Tatsuhiko
 * @version $Id: ProgressAdapter.java 1552 2018-04-26 01:43:24Z miyabe $
 */
public class ProgressAdapter implements ProgressListener {

	public void sourceLength(long sourceLength) {
		// ignore
	}

	public void progress(long serverRead) {
		// ignore
	}
}
