/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.softql.apicem.service;

import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLContext;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import com.softql.apicem.model.ApicEmLoginForm;
import com.softql.apicem.model.DeviceDetails;
import com.softql.apicem.model.DiscoveryDevices;
import com.softql.apicem.model.Ticket;
import com.softql.apicem.model.TicketForm;

/**
 *
 * @author Ramesh
 */
@Service
public class ApicEmService {

	private static final Logger log = LoggerFactory.getLogger(ApicEmService.class);

	private RestTemplate restTemplate;

	private void getRestTemplate(String username, String password) {
		try {
			SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(null, new TrustSelfSignedStrategy())
					.useTLS().build();
			SSLConnectionSocketFactory connectionFactory = new SSLConnectionSocketFactory(sslContext,
					new AllowAllHostnameVerifier());
			BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
			credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));
			HttpClient httpClient = HttpClientBuilder.create().setSSLSocketFactory(connectionFactory)
					.setDefaultCredentialsProvider(credentialsProvider).build();

			HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(
					httpClient);
			restTemplate = new RestTemplate(requestFactory);
			restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
			restTemplate.getMessageConverters().add(new StringHttpMessageConverter());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String getToken(ApicEmLoginForm form, String url) {
		TicketForm ticketForm = new TicketForm();
		ticketForm.setUsername(form.getUsername());
		ticketForm.setPassword(form.getPassword());
		Ticket ticket = new Ticket();
		String token = "";
		try {
			getRestTemplate(form.getUsername(), form.getPassword());
			ticket = restTemplate.postForObject(url, ticketForm, Ticket.class);
			token = ticket.getResponse().getServiceTicket();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return token;
	}

	public List<DiscoveryDevices> getDevices(String url) {
		List<DiscoveryDevices> deviceList = new ArrayList<DiscoveryDevices>();
		DeviceDetails deviceDetails = restTemplate.getForObject(url, DeviceDetails.class);
		List<DiscoveryDevices> arrayToList = CollectionUtils.arrayToList(deviceDetails.getResponse());
		deviceList.addAll(arrayToList);
		return deviceList;
	}
}
