package jp.cssj.cti2.results;

import java.io.File;
import java.io.OutputStream;

import jp.cssj.resolver.MetaSource;
import jp.cssj.rsr.RandomBuilder;
import jp.cssj.rsr.impl.FileRandomBuilder;
import jp.cssj.rsr.impl.NopRandomBuilder;
import jp.cssj.rsr.impl.StreamRandomBuilder;

/**
 * 単一の結果を出力するResultsです。
 * 
 * @author MIYABE Tatsuhiko
 * @version $Id: SingleResult.java 1552 2018-04-26 01:43:24Z miyabe $
 */
public class SingleResult implements Results {
	/**
	 * 出力先のデータ構築オブジェクトです。
	 */
	protected RandomBuilder builder;

	/**
	 * 1つのデータ構築オブジェクトに対して出力します。
	 * 
	 * @param builder
	 */
	public SingleResult(RandomBuilder builder) {
		this.builder = builder;
	}

	/**
	 * OutputStreamにデータを出力します。
	 * 
	 * @param out
	 */
	public SingleResult(OutputStream out) {
		this(new StreamRandomBuilder(out));
	}

	/**
	 * ファイルにデータを出力します。
	 * 
	 * @param file
	 */
	public SingleResult(File file) {
		this(new FileRandomBuilder(file));
	}

	public boolean hasNext() {
		return this.builder != null;
	}

	public RandomBuilder nextBuilder(MetaSource metaSource) {
		if (this.builder == null) {
			return NopRandomBuilder.SHARED_INSTANCE;
		}
		try {
			return this.builder;
		} finally {
			this.builder = null;
		}
	}

	public void end() {
		// NOP
	}
}
