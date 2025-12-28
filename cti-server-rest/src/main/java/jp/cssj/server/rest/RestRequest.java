package jp.cssj.server.rest;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;

public class RestRequest {
	// クエリ文字列、または読み込み済みのフィールド
	public static class FormField {
		public final String name, value;
		public final byte[] data;

		public FormField(String name, String value, byte[] data) {
			this.name = name;
			this.value = value;
			this.data = data;
		}

		public String toString() {
			return "" + this.name + ";length=" + this.value.length();
		}
	}

	public final HttpServletRequest req;
	public final FileItemIterator iter;
	private Object nextItem = null;
	private byte nextType = NONE;

	// 読み込み済みの値
	private Map<String, String> nameToValue = null;
	// クエリ文字列、読み込み済みのフィールドのリスト
	private List<FormField> fields = new ArrayList<FormField>();

	public static final byte NONE = 0;
	public static final byte FIELD = 1;
	public static final byte FILE = 2;
	private int pos = 0;

	public static final RestRequest getRestRequest(HttpServletRequest req) {
		RestRequest restReq = (RestRequest) req.getAttribute(RestRequest.class.getName());
		return restReq;
	}

	public RestRequest(HttpServletRequest req) throws IOException, FileUploadException {
		this.req = req;
		if (ServletFileUpload.isMultipartContent(req)) {
			ServletFileUpload upload = new ServletFileUpload();
			this.iter = upload.getItemIterator(req);
		} else {
			this.iter = null;
		}
		req.setAttribute(RestRequest.class.getName(), this);
		for (@SuppressWarnings("unchecked")
		Enumeration<String> i = req.getParameterNames(); i.hasMoreElements();) {
			String name = i.nextElement();
			if (name.startsWith("rest.")) {
				this.fields.add(new FormField(name, req.getParameter(name), null));
			} else {
				String[] values = req.getParameterValues(name);
				for (int j = 0; j < values.length; ++j) {
					this.fields.add(new FormField(name, values[j], null));
				}
			}
		}
	}

	private FormField toFormField(FileItemStream item) throws IOException, FileUploadException {
		String charset = this.req.getCharacterEncoding();
		if (charset == null) {
			charset = RestServlet.CHARSET;
		}
		FormField field;
		try (InputStream in = item.openStream()) {
			byte[] data = IOUtils.toByteArray(in);
			String value = new String(data, charset);
			field = new FormField(item.getFieldName(), value, data);
		}
		return field;
	}

	public byte getType() throws IOException, FileUploadException {
		if (this.pos < this.fields.size()) {
			return FIELD;
		}
		if (this.nextItem != null) {
			return this.nextType;
		}
		return NONE;
	}

	public Object getItem() throws IOException, FileUploadException {
		if (this.pos < this.fields.size()) {
			FormField field = this.fields.get(this.pos);
			return field;
		}
		return this.nextItem;
	}

	public void nextItem() throws IOException, FileUploadException {
		if (this.pos < this.fields.size()) {
			++this.pos;
			if (this.pos < this.fields.size()) {
				return;
			}
			if (this.nextItem != null) {
				return;
			}
		}
		if (this.iter == null || !this.iter.hasNext()) {
			this.nextItem = null;
			return;
		}
		FileItemStream item = this.iter.next();
		if (!item.isFormField()) {
			this.nextItem = item;
			this.nextType = FILE;
			return;
		}
		this.nextItem = this.toFormField(item);
		this.nextType = FIELD;
	}

	public String[] getParameterNames() {
		if (this.nameToValue == null) {
			return new String[0];
		}
		return this.nameToValue.keySet().toArray(new String[this.nameToValue.size()]);
	}

	public String getParameter(String name) throws IOException, FileUploadException {
		String value = this.req.getParameter(name);
		if (value == null && this.nameToValue != null) {
			value = this.nameToValue.get(name);
		}
		if (value != null || this.iter == null || this.nextItem != null) {
			return value;
		}
		while (this.iter.hasNext()) {
			FileItemStream item = this.iter.next();
			if (!item.isFormField()) {
				this.nextItem = item;
				this.nextType = FILE;
				break;
			}
			this.nextItem = null;
			FormField field = this.toFormField(item);
			if (this.nameToValue == null) {
				this.nameToValue = new HashMap<String, String>();
			}
			this.nameToValue.put(field.name, field.value);
			this.fields.add(field);
			if (field.name.equals(name)) {
				return value;
			}
		}
		return value;
	}
}
