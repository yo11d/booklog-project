package com.booklog.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.booklog.dao.ApiDAO;

@Service
public class BookSearchService {
	
	@Autowired
	private ApiDAO apiDAO;
	
public String getJSON(String keyword) {
		
		String clientId = apiDAO.getBookSearchApi().getId();
		String clientSecret = apiDAO.getBookSearchApi().getKey();
		String encodedKeyword = null;
		
		try {
			encodedKeyword = URLEncoder.encode(keyword,"UTF-8");
		}catch(UnsupportedEncodingException e){
			throw new RuntimeException("검색 인코딩 실패",e);
		}
		
		String apiURL = "https://openapi.naver.com/v1/search/book.json?query=" + encodedKeyword+ "&display=60&start=1";
		
		Map<String,String> requestHeaders = new HashMap<>();
		requestHeaders.put("X-Naver-Client-Id", clientId);
		requestHeaders.put("X-Naver-Client-Secret", clientSecret);
		String responseBody = get(apiURL, requestHeaders);
		
		return responseBody;
	}

	public String get(String apiURL, Map<String,String> requestHeaders) {
	HttpURLConnection con = connect(apiURL);
	try {
		con.setRequestMethod("GET");
		for(Map.Entry<String, String> header : requestHeaders.entrySet()) {
			con.setRequestProperty(header.getKey(), header.getValue());
		}
		
		int responseCode = con.getResponseCode();
		if(responseCode == HttpURLConnection.HTTP_OK) {
			return readBody(con.getInputStream());
		} else { //에러 발생
			return readBody(con.getErrorStream());
		}
		
	} catch (IOException e) {
		throw new RuntimeException("API 요청과 응답 실패", e);
	}finally {
		con.disconnect();
	}
	
}
	
	public HttpURLConnection connect(String apiURL) {
		try {
			URL url = new URL(apiURL);
			return (HttpURLConnection)url.openConnection();
		} catch (MalformedURLException e) {
			throw new RuntimeException("API URL이 잘못되었습니다. : " + apiURL, e);
		} catch (IOException e) {
			throw new RuntimeException("연결이 실패했습니다. :"+apiURL,e);
		}
	}
	
	public String readBody(InputStream body) {
		InputStreamReader streamReader = new InputStreamReader(body);
		
		try{
			BufferedReader lineReader = new BufferedReader(streamReader);
			StringBuilder responseBody = new StringBuilder();
			
			String line;
			while((line = lineReader.readLine()) != null) {
				responseBody.append(line);
			}
			
			return responseBody.toString();
		}catch(IOException e) {
			throw new RuntimeException("API 응답을 읽는데 실패했습니다.", e);
		}
	}

}
