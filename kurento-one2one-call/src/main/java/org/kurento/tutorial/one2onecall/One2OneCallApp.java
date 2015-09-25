/*
 * (C) Copyright 2014 Kurento (http://kurento.org/)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 */
package org.kurento.tutorial.one2onecall;

import org.kurento.client.KurentoClient;
import org.kurento.jsonrpc.internal.server.config.JsonRpcConfiguration;
import org.kurento.jsonrpc.server.JsonRpcConfigurer;
import org.kurento.jsonrpc.server.JsonRpcHandlerRegistry;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

/**
 * Video call 1 to 1 demo (main).
 * 
 * @author Boni Garcia (bgarcia@gsyc.es)
 * @author Micael Gallego (micael.gallego@gmail.com)
 * @since 4.3.1
 */
@Import(JsonRpcConfiguration.class)
@SpringBootApplication
public class One2OneCallApp implements JsonRpcConfigurer {

	final static String DEFAULT_KMS_WS_URI = "ws://localhost:8888/kurento";

	@Bean
	public CallHandler callHandler() {
		return new CallHandler();
	}

	@Bean
	public UserRegistry registry() {
		return new UserRegistry();
	}
        
        @Bean
	public UserControl userControl() {
		return new UserControl();
	}
        
        
	@Bean
	public KurentoClient kurentoClient() {
		return KurentoClient.create(System.getProperty("kms.ws.uri",
				DEFAULT_KMS_WS_URI));
	}


	public static void main(String[] args) throws Exception {
		new SpringApplication(One2OneCallApp.class).run(args);
	}

    @Override
    public void registerJsonRpcHandlers(JsonRpcHandlerRegistry jrhr) {
        jrhr.addHandler(callHandler(), "/call");
    }

}
