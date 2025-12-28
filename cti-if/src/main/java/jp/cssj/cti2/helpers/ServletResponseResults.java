package jp.cssj.cti2.helpers;

import java.io.IOException;

import javax.servlet.ServletResponse;

import jp.cssj.cti2.results.Results;
import jp.cssj.resolver.MetaSource;
import jp.cssj.rsr.RandomBuilder;
import jp.cssj.rsr.helpers.RandomBuilderMeasurer;
import jp.cssj.rsr.impl.StreamRandomBuilder;

/**
 * 構築したデータをサーブレットのレスポンスとして送り出します。
 * 
 * @author MIYABE Tatsuhiko
 * @version $Id: ServletResponseResults.java 1552 2018-04-26 01:43:24Z miyabe $
 */
public class ServletResponseResults implements Results {
	protected final ServletResponse response;
	protected RandomBuilderMeasurer builder = null;

	public ServletResponseResults(ServletResponse response) {
		this.response = response;
	}

	public boolean hasNext() {
		return this.builder == null;
	}

	public RandomBuilder nextBuilder(MetaSource metaSource) throws IOException {
		if (this.builder != null) {
			throw new IllegalStateException();
		}

		String mimeType = metaSource.getMimeType();
		long length = metaSource.getLength();
		if (mimeType != null) {
			this.response.setContentType(ServletHelper.getContentType(metaSource));
		}
		if (length != -1L) {
			this.response.setContentLengthLong(length);
		}
		RandomBuilder builder = new StreamRandomBuilder(this.response.getOutputStream());
		this.builder = new RandomBuilderMeasurer(builder) {
			public void finish() throws IOException {
				ServletResponseResults.this.finish();
				super.finish();
			}
		};
		return this.builder;
	}

	protected void finish() {
		long length = this.builder.getLength();
		this.response.setContentLengthLong(length);
	}

	public void end() {
		// NOP
	}
}
