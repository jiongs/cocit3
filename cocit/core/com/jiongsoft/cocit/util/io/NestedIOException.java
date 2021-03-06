/*
 * Copyright 2002-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jiongsoft.cocit.util.io;

import java.io.IOException;

public class NestedIOException extends IOException {
	private static final long serialVersionUID = 5391535318931879852L;

	/**
	 * Construct a <code>NestedIOException</code> with the specified detail
	 * content.
	 * 
	 * @param msg
	 *            the detail content
	 */
	public NestedIOException(String msg) {
		super(msg);
	}

	/**
	 * Construct a <code>NestedIOException</code> with the specified detail
	 * content and nested exception.
	 * 
	 * @param msg
	 *            the detail content
	 * @param cause
	 *            the nested exception
	 */
	public NestedIOException(String msg, Throwable cause) {
		super(msg);
		initCause(cause);
	}

	/**
	 * Return the detail content, including the content from the nested
	 * exception if there is one.
	 */
	public String getMessage() {
		return buildMessage(super.getMessage(), getCause());
	}

	public static String buildMessage(String message, Throwable cause) {
		if (cause != null) {
			StringBuffer buf = new StringBuffer();
			if (message != null) {
				buf.append(message).append("; ");
			}
			buf.append("nested exception is ").append(cause);
			return buf.toString();
		} else {
			return message;
		}
	}
}
