package kr.co.itcen.network.http;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.file.Files;

/*
 * 과제
 * 400Error  
 * 404Error 구현!!!
 */

public class RequestHandler extends Thread {
	private static String documentRoot = "";
	
	//WebApp는 Jar파일화 시킨다음 Tomcat에 올리면 압축이 풀리면서 실행이 된다. 
	static {
		documentRoot = RequestHandler.class.getClass().getResource("/webapp").getPath();
	}
	
	private Socket socket;

	public RequestHandler(Socket socket) {
		this.socket = socket;
	}

	@Override
	public void run() {
		try {
			// get IOStream
			OutputStream outputStream = socket.getOutputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));

			// logging Remote Host IP Address & Port
			InetSocketAddress inetSocketAddress = (InetSocketAddress) socket.getRemoteSocketAddress();
			consoleLog("connected from " + inetSocketAddress.getAddress().getHostAddress() + ":"
					+ inetSocketAddress.getPort());

			String request = null;

			while (true) {
				String line = br.readLine();

				// 브라우저가 연결을 끊으면...
				if (line == null) {
					System.out.println("Event발생");
					break;
				}

				// header만 읽음
				if ("".equals(line)) {
					System.out.println("line Event발생");
					break;
				}

				if (request == null) {
					//처음 실행시에 발생
					//기본 경로일때 발생
					System.out.println("Request Event발생");
					request = line;
					break;
				}
			}

			String[] tokens = request.split(" ");
			System.out.println();
			for(String i : tokens) {
				System.out.println("token :" + i);
			}
			System.out.println("----------------------------------------------");
			System.out.println("token[0] :" + tokens[0]);
			System.out.println("token[1] :" + tokens[1]);
			System.out.println("token[2] :" + tokens[2]);
			
			if ("GET".equals(tokens[0])) {
				consoleLog("request:" + request);
				responseStaticResource(outputStream, tokens[1], tokens[2]);
			} else { // POST, PUT, DELETE 명령은 무시 //Action 처리가 안되었기에 오류가 발생하는거다!!!
				consoleLog("bad request:" + request); // 과제 // 경로를 읽어서!! 표시
				response400Error(outputStream, tokens[2]); // 400에러 관련된 메시지가 나오도록 과제!!!
			}

			// 예제 응답입니다.
			// 서버 시작과 테스트를 마친 후, 주석 처리 합니다.
//			outputStream.write( "HTTP/1.1 200 OK\r\n".getBytes( "UTF-8" ) );
//			outputStream.write( "Content-Type:text/html; charset=utf-8\r\n".getBytes( "UTF-8" ) );
//			outputStream.write( "\r\n".getBytes() );
//			outputStream.write( "<h1>이 페이지가 잘 보이면 실습과제 SimpleHttpServer를 시작할 준비가 된 것입니다.</h1>".getBytes( "UTF-8" ) );

		} catch (Exception ex) {
			consoleLog("error:" + ex);
		} finally {
			// clean-up
			try {
				if (socket != null && socket.isClosed() == false) {
					socket.close();
				}

			} catch (IOException ex) {
				consoleLog("error:" + ex);
			}
		}
	}

	private void responseStaticResource(OutputStream outputStream, String url, String protocol) throws IOException {

		if ("/".equals(url)) {
			url = "/index.html";
		}

		File file = new File("documentRoot" + url);
		if (file.exists() == false) {
			consoleLog("File Not Found:" + url);
			response404Error(outputStream, protocol);
			return;
		}

		// nio
		byte[] body = Files.readAllBytes(file.toPath());
		String contentType = Files.probeContentType(file.toPath());

		// 응답
		outputStream.write((protocol + " 200 OK\r\n").getBytes("UTF-8"));
		outputStream.write(("Content-Type:" + contentType + "; charset=utf-8\r\n").getBytes("UTF-8"));
		outputStream.write("\r\n".getBytes());
		outputStream.write(body);
	}

	private void response400Error(OutputStream outputStream, String protocol) throws IOException {
		// TODO Auto-generated method stub
		File file = new File("documentRoot" + "/error/400.html");

		byte[] body = Files.readAllBytes(file.toPath());
		String contentType = Files.probeContentType(file.toPath());

		// 응답 - 우선순위로 인해서 (protocol + " 200 OK\r\n") 이렇게 해주어야 한다.
		outputStream.write((protocol + " 200 OK\r\n").getBytes("UTF-8"));
		// CSS를 렌더링 하기 위해서는 Content-Type을 CSS로 처리해야 된다.
		outputStream.write(("Content-Type:" + contentType + "; charset=utf-8\r\n").getBytes("UTF-8"));
		outputStream.write("\r\n".getBytes());
		outputStream.write(body);
	}

	private void response404Error(OutputStream outputStream, String protocol) throws IOException {

		// TODO Auto-generated method stub
		File file = new File("documentRoot" + "/error/404.html");

		byte[] body = Files.readAllBytes(file.toPath());
		String contentType = Files.probeContentType(file.toPath());

		// 응답 - 우선순위로 인해서 (protocol + " 200 OK\r\n") 이렇게 해주어야 한다.
		outputStream.write((protocol + " 200 OK\r\n").getBytes("UTF-8"));
		// CSS를 렌더링 하기 위해서는 Content-Type을 CSS로 처리해야 된다.
		outputStream.write(("Content-Type:" + contentType + "; charset=utf-8\r\n").getBytes("UTF-8"));
		outputStream.write("\r\n".getBytes());
		outputStream.write(body);
	}

	public void consoleLog(String message) {
		System.out.println("[RequestHandler#" + getId() + "] " + message);
	}

}
