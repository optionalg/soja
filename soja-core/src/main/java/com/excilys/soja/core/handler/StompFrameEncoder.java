/**
 * Copyright 2010-2011 eBusiness Information, Groupe Excilys (www.excilys.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.excilys.soja.core.handler;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;

import com.excilys.soja.core.model.Frame;
import com.excilys.soja.core.model.Header;
import com.excilys.soja.core.model.frame.HeartBeatFrame;

/**
 * @author dvilleneuve
 * 
 */
public class StompFrameEncoder extends OneToOneEncoder {

	@Override
	protected Object encode(ChannelHandlerContext ctx, Channel channel, Object msg) throws Exception {
		if (!(msg instanceof Frame)) {
			return msg;
		} else if (msg instanceof HeartBeatFrame) {
			return ChannelBuffers.wrappedBuffer(new byte[] { '\n' });
		}

		Frame frame = (Frame) msg;

		// COMMAND
		StringBuilder formatedFrame = new StringBuilder();
		String command = frame.getCommand();
		if (command != null && command.length() > 0) {
			formatedFrame.append(command).append(Frame.EOL_COMMAND);
		} else {
			throw new IllegalArgumentException("Command can't be empty");
		}

		// HEADER
		Header header = frame.getHeader();
		if (header != null) {
			if (header.size() > 0) {
				List<String> keys = new ArrayList<String>(header.keySet());
				Collections.sort(keys);

				for (String key : keys) {
					formatedFrame.append(escapeHeader(key)) //
							.append(Frame.SEPARATOR_HEADER) //
							.append(escapeHeader(header.get(key))) //
							.append(Frame.EOL_HEADER);
				}
			}
		}
		formatedFrame.append(Frame.EOL_HEADERS);

		// BODY
		String body = frame.getBody();
		if (body != null && body.length() > 0) {
			formatedFrame.append(body);
		}

		formatedFrame.append(Frame.EOL_FRAME);
		String frameString = formatedFrame.toString();

		return ChannelBuffers.copiedBuffer((String) frameString, Charset.forName("UTF-8"));
	}

	public static String escapeHeader(String headerValue) {
		if (headerValue == null)
			return null;

		headerValue = headerValue.replace("\\", "\\\\");
		headerValue = headerValue.replace("\n", "\\n");
		headerValue = headerValue.replace(":", "\\c");

		return headerValue;
	}
}
